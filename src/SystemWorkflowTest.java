import exception.InvalidInputException;
import exception.InvalidStateException;
import exception.NotFoundException;
import model.*;
import repository.FilePersistenceService;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Self-contained unit & integration test suite.
 * Runs without external testing frameworks (like JUnit) using pure Core Java.
 * Verified for Course Evaluation by Mohit Srivastav (M1) and Shreyas Tiwari (M2).
 */
public class SystemWorkflowTest {
    private static int passed = 0;
    private static int failed = 0;

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        System.out.println(BOLD + "\nRunning Core Java System Workflow Test Suite (20 Test Scenarios)..." + RESET);
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

        // Tests for Member 2 & Persistence Enhancements
        testDoctorClinicalAnalytics();
        testDoctorConsultationHistory();
        testDoctorRejectionWithReason();
        testPolymorphicPersonAbstraction();
        testPrescriptionSlipAndFileExport();
        testThreadSafeAtomicSequenceIds();

        // Advanced Clinical Suite (Slot Collision, Invoicing, Cancellation & Observers)
        testSlotConflictDetection();
        testAppointmentCancellation();
        testAppointmentRescheduling();
        testMedicalInvoicingAndReceipt();

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
            LocalDateTime dt = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
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
            Appointment a = svc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            Appointment approved = svc.updateDecision(a.getAppointmentId(), true);

            assertEquals(AppointmentStatus.APPROVED, approved.getStatus(), "Status should be APPROVED");
        });
    }

    private static void testDoctorRejectionWorkflow() {
        runTest("TC-04: Doctor can reject pending appointment", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);
            Appointment a = svc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            Appointment rejected = svc.updateDecision(a.getAppointmentId(), false);

            assertEquals(AppointmentStatus.REJECTED, rejected.getStatus(), "Status should be REJECTED");
        });
    }

    private static void testInvalidStateTransitions() {
        runTest("TC-05: Modifying already decided appointment throws InvalidStateException", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);
            Appointment a = svc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
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

            LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            Appointment a = apptSvc.requestAppointment(1, 1, apptTime);
            apptSvc.updateDecision(a.getAppointmentId(), true);

            LocalDateTime nextVisit = apptTime.plusWeeks(1);
            Consultation c = consSvc.recordConsultation(a.getAppointmentId(), "Viral fever", "Paracetamol 650mg", nextVisit);

            assertEquals("Viral fever", c.getDiagnosis(), "Diagnosis must match");
            assertEquals("Paracetamol 650mg", c.getPrescription(), "Prescription must match");

            List<Appointment> allAppts = repo.getAllAppointments();
            assertEquals(2, allAppts.size(), "Should have initial appointment + 1 follow-up");

            Appointment followUp = allAppts.get(1);
            assertEquals(true, followUp.isFollowUp(), "Follow-up flag must be true");
            assertEquals(AppointmentStatus.APPROVED, followUp.getStatus(), "Follow-up must be APPROVED automatically");
            assertEquals(nextVisit, followUp.getDateTime(), "Follow-up time must match nextVisit");
        });
    }

    private static void testConsultationGuards() {
        runTest("TC-07: Consultation cannot be recorded for non-APPROVED appointment", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            Appointment pending = apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            assertThrows(InvalidStateException.class, () -> {
                consSvc.recordConsultation(pending.getAppointmentId(), "Headache", "Rest", LocalDateTime.now().plusDays(5));
            }, "Cannot record consultation on PENDING appointment");

            Appointment rejected = apptSvc.requestAppointment(2, 1, LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
            apptSvc.updateDecision(rejected.getAppointmentId(), false);
            assertThrows(InvalidStateException.class, () -> {
                consSvc.recordConsultation(rejected.getAppointmentId(), "Headache", "Rest", LocalDateTime.now().plusDays(5));
            }, "Cannot record consultation on REJECTED appointment");
        });
    }

    private static void testDuplicateConsultationPrevention() {
        runTest("TC-08: Duplicate consultation on same appointment is blocked", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            Appointment a = apptSvc.requestAppointment(1, 1, apptTime);
            apptSvc.updateDecision(a.getAppointmentId(), true);

            consSvc.recordConsultation(a.getAppointmentId(), "Cough", "Syrup", apptTime.plusDays(3));

            assertThrows(InvalidStateException.class, () -> {
                consSvc.recordConsultation(a.getAppointmentId(), "Cough again", "Stronger Syrup", apptTime.plusDays(4));
            }, "Cannot record second consultation for the same appointment");
        });
    }

    private static void testNextVisitTimingGuard() {
        runTest("TC-09: Next visit date must be after current appointment date", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            LocalDateTime apptTime = LocalDateTime.now().plusDays(5).withHour(10).withMinute(0);
            Appointment a = apptSvc.requestAppointment(1, 1, apptTime);
            apptSvc.updateDecision(a.getAppointmentId(), true);

            assertThrows(InvalidInputException.class, () -> {
                consSvc.recordConsultation(a.getAppointmentId(), "Checkup", "None", apptTime.minusDays(1));
            }, "Next visit cannot be before current appointment date");
        });
    }

    private static void testPatientDataIsolation() {
        runTest("TC-10: Patient data isolation and invalid ID validation", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);

            apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            apptSvc.requestAppointment(2, 1, LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));

            assertEquals(1, apptSvc.getAppointmentsForPatient(1).size(), "Patient 1 should have exactly 1 appointment");
            assertEquals(1, apptSvc.getAppointmentsForPatient(2).size(), "Patient 2 should have exactly 1 appointment");

            assertThrows(NotFoundException.class, () -> {
                apptSvc.getAppointmentsForPatient(999);
            }, "Should throw NotFoundException for invalid patient id");
        });
    }

    private static void testDoctorClinicalAnalytics() {
        runTest("TC-11: Doctor clinical statistics calculated accurately (Member 2 Module)", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            // Create 3 appointments on separate days to avoid collision
            Appointment a1 = apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            Appointment a2 = apptSvc.requestAppointment(2, 1, LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));
            Appointment a3 = apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));

            assertEquals(AppointmentStatus.PENDING, a2.getStatus(), "a2 must remain PENDING");
            apptSvc.updateDecision(a1.getAppointmentId(), true);
            apptSvc.updateDecision(a3.getAppointmentId(), false, "Doctor unavailable");

            consSvc.recordConsultation(a1.getAppointmentId(), "Hypertension", "Amlodipine 5mg", LocalDateTime.now().plusWeeks(2));

            DoctorStats stats = apptSvc.getDoctorStats(1);
            assertEquals(4, stats.getTotalAssigned(), "Total appointments assigned must be 4");
            assertEquals(1, stats.getPendingCount(), "Pending count must be 1 (a2)");
            assertEquals(2, stats.getApprovedCount(), "Approved count must be 2 (a1 + follow-up)");
            assertEquals(1, stats.getRejectedCount(), "Rejected count must be 1 (a3)");
            assertEquals(1, stats.getCompletedConsultations(), "Completed consultations must be 1");
            assertEquals(1, stats.getScheduledFollowUps(), "Follow-ups must be 1");
            assertEquals(50.0, stats.getConsultationCompletionRatePercent(), "50% of approved appointments consulted");
        });
    }

    private static void testDoctorConsultationHistory() {
        runTest("TC-12: Doctor consultation history isolation", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            repo.addDoctor(new Doctor(2, "Pooja Verma", "Cardiology"));
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            Appointment a1 = apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            Appointment a2 = apptSvc.requestAppointment(2, 2, LocalDateTime.now().plusDays(2).withHour(10).withMinute(0));

            apptSvc.updateDecision(a1.getAppointmentId(), true);
            apptSvc.updateDecision(a2.getAppointmentId(), true);

            consSvc.recordConsultation(a1.getAppointmentId(), "Asthma", "Inhaler", LocalDateTime.now().plusWeeks(1));
            consSvc.recordConsultation(a2.getAppointmentId(), "Arrhythmia", "Beta-blocker", LocalDateTime.now().plusWeeks(2));

            List<Consultation> doc1Consults = consSvc.getConsultationsForDoctor(1);
            List<Consultation> doc2Consults = consSvc.getConsultationsForDoctor(2);

            assertEquals(1, doc1Consults.size(), "Doctor 1 must have exactly 1 consultation");
            assertEquals(1, doc2Consults.size(), "Doctor 2 must have exactly 1 consultation");
            assertEquals("Asthma", doc1Consults.get(0).getDiagnosis(), "Diagnosis for Doctor 1 must match");
            assertEquals("Arrhythmia", doc2Consults.get(0).getDiagnosis(), "Diagnosis for Doctor 2 must match");
        });
    }

    private static void testDoctorRejectionWithReason() {
        runTest("TC-13: Rejection with custom clinical audit reason", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);

            Appointment a = apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            String reason = "Emergency ICU round callout";
            Appointment rejected = apptSvc.updateDecision(a.getAppointmentId(), false, reason);

            assertEquals(AppointmentStatus.REJECTED, rejected.getStatus(), "Status must be REJECTED");
            assertEquals(reason, rejected.getRejectionReason(), "Rejection reason must be preserved");
        });
    }

    private static void testPolymorphicPersonAbstraction() {
        runTest("TC-14: Polymorphic Person hierarchy validation", () -> {
            Person patientPerson = new Patient(10, "Dev Mehta", "9876543210");
            Person doctorPerson = new Doctor(20, "Ananya Roy", "Neurology", 750.0);

            assertEquals("PATIENT", patientPerson.getRole(), "Patient role must be PATIENT");
            assertEquals("DOCTOR", doctorPerson.getRole(), "Doctor role must be DOCTOR");
            assertEquals(true, patientPerson.getSummaryDetails().contains("Dev Mehta"), "Summary must contain name");
            assertEquals(true, doctorPerson.getSummaryDetails().contains("Neurology"), "Summary must contain specialty");
        });
    }

    private static void testPrescriptionSlipAndFileExport() {
        runTest("TC-15: Digital prescription slip generation & disk export", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);
            FilePersistenceService fps = new FilePersistenceService();

            Appointment a = apptSvc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            apptSvc.updateDecision(a.getAppointmentId(), true);

            Consultation c = consSvc.recordConsultation(a.getAppointmentId(), "Bronchitis", "Azithromycin 500mg OD", LocalDateTime.now().plusDays(7));
            String slip = c.generatePrescriptionSlip();

            assertEquals(true, slip.contains("SMART CLINIC MEDICAL PRESCRIPTION SLIP"), "Prescription slip header must exist");
            assertEquals(true, slip.contains("Azithromycin 500mg OD"), "Prescription contents must exist");

            File exported = fps.savePrescriptionSlip(c);
            assertEquals(true, exported != null && exported.exists(), "Prescription file must be created on disk");
        });
    }

    private static void testThreadSafeAtomicSequenceIds() {
        runTest("TC-16: Thread-safe Atomic sequence ID generation under concurrency", () -> {
            InMemoryRepository repo = new InMemoryRepository();
            int threads = 10;
            int itemsPerThread = 50;
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            CountDownLatch latch = new CountDownLatch(threads);

            List<Integer> generatedIds = java.util.Collections.synchronizedList(new ArrayList<>());

            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        for (int i = 0; i < itemsPerThread; i++) {
                            generatedIds.add(repo.nextAppointmentId());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            pool.shutdown();

            assertEquals(threads * itemsPerThread, generatedIds.size(), "Total IDs generated must match expected");
            long uniqueCount = generatedIds.stream().distinct().count();
            assertEquals((long) (threads * itemsPerThread), uniqueCount, "All generated IDs must be strictly unique");
        });
    }

    private static void testSlotConflictDetection() {
        runTest("TC-17: Double-booking doctor within 30 minutes rejected by Slot Guard", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);

            LocalDateTime baseTime = LocalDateTime.now().plusDays(2).withHour(11).withMinute(0);
            svc.requestAppointment(1, 1, baseTime);

            // Attempt to book same doctor 15 minutes later (violates 30-min buffer)
            assertThrows(InvalidStateException.class, () -> {
                svc.requestAppointment(2, 1, baseTime.plusMinutes(15));
            }, "Should throw InvalidStateException for double-booking within 30-min window");
        });
    }

    private static void testAppointmentCancellation() {
        runTest("TC-18: Patient can cancel pending or approved appointment with reason audit", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);

            Appointment a = svc.requestAppointment(1, 1, LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            svc.cancelAppointment(a.getAppointmentId(), "Patient has out-of-town travel");

            assertEquals(AppointmentStatus.CANCELLED, a.getStatus(), "Status must update to CANCELLED");
            assertEquals("Patient has out-of-town travel", a.getCancellationReason(), "Cancellation reason must be recorded");

            assertThrows(InvalidStateException.class, () -> {
                svc.cancelAppointment(a.getAppointmentId(), "Again");
            }, "Cannot re-cancel an already CANCELLED appointment");
        });
    }

    private static void testAppointmentRescheduling() {
        runTest("TC-19: Appointment rescheduling updates time and validates collision", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService svc = new AppointmentService(repo);

            LocalDateTime originalTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            Appointment a = svc.requestAppointment(1, 1, originalTime);

            LocalDateTime newTime = LocalDateTime.now().plusDays(2).withHour(15).withMinute(30);
            svc.rescheduleAppointment(a.getAppointmentId(), newTime);

            assertEquals(newTime, a.getDateTime(), "Appointment time must update to rescheduled slot");
        });
    }

    private static void testMedicalInvoicingAndReceipt() {
        runTest("TC-20: Medical billing invoice and GST calculation generated on consultation", () -> {
            InMemoryRepository repo = createRepoWithSeed();
            AppointmentService apptSvc = new AppointmentService(repo);
            ConsultationService consSvc = new ConsultationService(repo);

            LocalDateTime dt = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            Appointment a = apptSvc.requestAppointment(1, 1, dt);
            apptSvc.updateDecision(a.getAppointmentId(), true);

            Consultation c = consSvc.recordConsultation(a.getAppointmentId(), "Migraine", "Tab Rizatriptan", dt.plusWeeks(2), "Credit Card");
            Invoice inv = consSvc.getInvoiceByConsultationId(c.getConsultationId());

            assertEquals(500.0, inv.getBaseFee(), "Doctor default fee must be ₹500.0");
            assertEquals(25.0, inv.getTaxAmount(), "5% GST on ₹500 must be ₹25.0");
            assertEquals(525.0, inv.getTotalAmount(), "Total invoice amount must be ₹525.0");
            assertEquals(true, inv.generateReceipt().contains("SMART CLINIC MEDICAL INVOICE"), "Receipt header must exist");
        });
    }

    // --- Helper Methods ---

    private static InMemoryRepository createRepoWithSeed() {
        InMemoryRepository repo = new InMemoryRepository();
        repo.addPatient(new Patient(1, "Aman", "9876500001"));
        repo.addPatient(new Patient(2, "Riya", "9876500002"));
        repo.addDoctor(new Doctor(1, "Dr. Sharma", "General Physician", 500.0));
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
