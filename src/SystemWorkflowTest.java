import exception.InvalidInputException;
import exception.InvalidStateException;
import exception.NotFoundException;
import model.Appointment;
import model.AppointmentStatus;
import model.Consultation;
import model.Doctor;
import model.Patient;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Self-contained unit & integration test suite.
 * Runs without external testing frameworks (like JUnit) using pure Core Java.
 */
public class SystemWorkflowTest {
    private static int passed = 0;
    private static int failed = 0;

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        System.out.println(BOLD + "\nRunning Core Java System Workflow Test Suite..." + RESET);
        System.out.println("-----------------------------------------------------------------");

        testAppointmentCreation();
        testPastDateValidation();
        testDoctorApprovalWorkflow();
        testDoctorRejectionWorkflow();
        testInvalidStateTransitions();
        testConsultationAndAutoFollowUp();
        testConsultationGuards();
        testDuplicateConsultationPrevention();
        testNextVisitTimingGuard();
        testPatientDataIsolation();

        System.out.println("-----------------------------------------------------------------");
        if (failed == 0) {
            System.out.println(BOLD + GREEN + "ALL " + passed + " TESTS PASSED SUCCESSFULLY! (100% Passing)" + RESET + "\n");
        } else {
            System.out.println(BOLD + RED + failed + " TESTS FAILED out of " + (passed + failed) + RESET + "\n");
            System.exit(1);
        }
    }

    private static void testAppointmentCreation() {
        runTest("TC-01: Appointment request created with PENDING status", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);
            LocalDateTime dt = LocalDateTime.now().plusDays(2);
            Appointment a = svc.requestAppointment(1, 1, dt);

            assertEquals(AppointmentStatus.PENDING, a.getStatus(), "Initial status must be PENDING");
            assertEquals(false, a.isFollowUp(), "Initial appointment must not be followUp");
            assertEquals(1, a.getPatient().getPatientId(), "Patient ID must match");
            assertEquals(1, a.getDoctor().getDoctorId(), "Doctor ID must match");
        });
    }

    private static void testPastDateValidation() {
        runTest("TC-02: Appointment request with past date throws InvalidInputException", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);
            assertThrows(InvalidInputException.class, () -> {
                svc.requestAppointment(1, 1, LocalDateTime.now().minusHours(1));
            }, "Should throw InvalidInputException for past date");
        });
    }

    private static void testDoctorApprovalWorkflow() {
        runTest("TC-03: Doctor can approve pending appointment", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);
            Appointment a = svc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1));
            Appointment approved = svc.updateDecision(a.getAppointmentId(), true);

            assertEquals(AppointmentStatus.APPROVED, approved.getStatus(), "Status should be APPROVED");
        });
    }

    private static void testDoctorRejectionWorkflow() {
        runTest("TC-04: Doctor can reject pending appointment", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);
            Appointment a = svc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1));
            Appointment rejected = svc.updateDecision(a.getAppointmentId(), false);

            assertEquals(AppointmentStatus.REJECTED, rejected.getStatus(), "Status should be REJECTED");
        });
    }

    private static void testInvalidStateTransitions() {
        runTest("TC-05: Modifying already decided appointment throws InvalidStateException", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);
            Appointment a = svc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1));
            svc.updateDecision(a.getAppointmentId(), true);

            assertThrows(InvalidStateException.class, () -> {
                svc.updateDecision(a.getAppointmentId(), false);
            }, "Cannot reject an already APPROVED appointment");
        });
    }

    private static void testConsultationAndAutoFollowUp() {
        runTest("TC-06: Recording consultation creates record AND auto follow-up appointment", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            LocalDateTime apptTime = LocalDateTime.now().plusDays(1);
            Appointment a = apptSvc.requestAppointment(1, 1, apptTime);
            apptSvc.updateDecision(a.getAppointmentId(), true);

            LocalDateTime followUpTime = apptTime.plusWeeks(1);
            Consultation c = consSvc.recordConsultation(a.getAppointmentId(), "Routine Checkup", "Multivitamins daily", followUpTime);

            assertEquals("Routine Checkup", c.getDiagnosis(), "Diagnosis must match");
            assertEquals("Multivitamins daily", c.getPrescription(), "Prescription must match");

            // Verify auto follow-up creation
            List<Appointment> appts = apptSvc.getAppointmentsForPatient(1);
            assertEquals(2, appts.size(), "Patient should now have 2 appointments (original + follow-up)");

            Appointment followUp = appts.get(1);
            assertEquals(true, followUp.isFollowUp(), "FollowUp flag must be true");
            assertEquals(AppointmentStatus.APPROVED, followUp.getStatus(), "Follow-up must be APPROVED");
            assertEquals(followUpTime, followUp.getDateTime(), "Follow-up date must match next visit date");
        });
    }

    private static void testConsultationGuards() {
        runTest("TC-07: Consultation cannot be recorded for non-APPROVED appointment", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            Appointment a = apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1)); // Status is PENDING
            assertThrows(InvalidStateException.class, () -> {
                consSvc.recordConsultation(a.getAppointmentId(), "Cold", "Rest", LocalDateTime.now().plusDays(3));
            }, "Cannot record consultation for PENDING appointment");
        });
    }

    private static void testDuplicateConsultationPrevention() {
        runTest("TC-08: Duplicate consultation on same appointment is blocked", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            LocalDateTime dt = LocalDateTime.now().plusDays(1);
            Appointment a = apptSvc.requestAppointment(1, 1, dt);
            apptSvc.updateDecision(a.getAppointmentId(), true);

            consSvc.recordConsultation(a.getAppointmentId(), "Flu", "Paracetamol", dt.plusDays(3));

            assertThrows(InvalidStateException.class, () -> {
                consSvc.recordConsultation(a.getAppointmentId(), "Flu 2", "Paracetamol 2", dt.plusDays(4));
            }, "Cannot record second consultation for same appointment");
        });
    }

    private static void testNextVisitTimingGuard() {
        runTest("TC-09: Next visit date must be after current appointment date", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            LocalDateTime dt = LocalDateTime.now().plusDays(2);
            Appointment a = apptSvc.requestAppointment(1, 1, dt);
            apptSvc.updateDecision(a.getAppointmentId(), true);

            assertThrows(InvalidInputException.class, () -> {
                consSvc.recordConsultation(a.getAppointmentId(), "Checkup", "Meds", dt.minusHours(1));
            }, "Next visit before appointment must be rejected");
        });
    }

    private static void testPatientDataIsolation() {
        runTest("TC-10: Patient data isolation and invalid ID validation", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);

            apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1));
            apptSvc.requestAppointment(2, 1, LocalDateTime.now().plusDays(2));

            assertEquals(1, apptSvc.getAppointmentsForPatient(1).size(), "Patient 1 should have 1 appointment");
            assertEquals(1, apptSvc.getAppointmentsForPatient(2).size(), "Patient 2 should have 1 appointment");

            assertThrows(NotFoundException.class, () -> {
                apptSvc.getAppointmentsForPatient(999);
            }, "Non-existent patient query should throw NotFoundException");
        });
    }

    // --- Helper Methods ---

    private static InMemoryRepository createRepoWithSeed() {
        InMemoryRepository repo = new InMemoryRepository();
        repo.addPatient(new Patient(1, "Aman", "9876500001"));
        repo.addPatient(new Patient(2, "Riya", "9876500002"));
        repo.addDoctor(new Doctor(1, "Dr. Sharma", "General Physician"));
        return repo;
    }

    private static void runTest(String name, TestRunnable test) {
        try {
            test.run();
            System.out.println(GREEN + "  [PASS] " + RESET + name);
            passed++;
        } catch (Throwable t) {
            System.out.println(RED + "  [FAIL] " + RESET + name + " -> " + t.getMessage());
            failed++;
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }

    private static void assertThrows(Class<? extends Throwable> expected, TestRunnable action, String message) {
        try {
            action.run();
            throw new AssertionError(message + " (Expected exception: " + expected.getSimpleName() + " but none was thrown)");
        } catch (Throwable t) {
            if (!expected.isInstance(t)) {
                throw new AssertionError(message + " (Expected exception: " + expected.getSimpleName() + " but got: " + t.getClass().getSimpleName() + ")");
            }
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Throwable;
    }
}
