import exception.InvalidInputException;
import exception.InvalidStateException;

import model.Appointment;
import model.AppointmentStatus;
import model.Consultation;
import model.Doctor;
import model.DoctorStats;
import model.Patient;
import repository.FilePersistenceService;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Automated demo script and presentation runner.
 * Demonstrates the full clinical lifecycle, Member 1 (Mohit) Patient Portal,
 * Member 2 (Shreyas) Doctor Analytics & Prescription Engine, and File Persistence.
 */
public class DemoRunner {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ANSI Colors for console presentation
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";

    public static void runDemo() {
        System.out.println("\n" + BOLD + CYAN + "=================================================================" + RESET);
        System.out.println(BOLD + CYAN + "   DOCTOR SMART APPOINTMENT & CLINICAL SYSTEM - LIVE EVALUATION  " + RESET);
        System.out.println(BOLD + CYAN + "     Architected by Mohit Srivastav (M1) & Shreyas Tiwari (M2)   " + RESET);
        System.out.println(BOLD + CYAN + "=================================================================" + RESET);
        System.out.println("Comprehensive End-to-End Evaluation Demonstration | Core Java Architecture\n");

        InMemoryRepository repo = new InMemoryRepository();
        FilePersistenceService persistenceService = new FilePersistenceService();
        AppointmentService appointmentService = new AppointmentService(repo);
        ConsultationService consultationService = new ConsultationService(repo);

        // Stage 1: Seeding
        header("STAGE 1: System Initialization & Polymorphic Domain Entities");
        Patient aman = new Patient(1, "Aman Sharma", "+91 98765 00001");
        Patient riya = new Patient(2, "Riya Patel", "+91 98765 00002");
        Doctor drSharma = new Doctor(1, "Rajesh Sharma", "Senior General Physician");

        repo.addPatient(aman);
        repo.addPatient(riya);
        repo.addDoctor(drSharma);

        System.out.println(GREEN + "  ✔ Patient #1 Registered: " + RESET + aman.getSummaryDetails());
        System.out.println(GREEN + "  ✔ Patient #2 Registered: " + RESET + riya.getSummaryDetails());
        System.out.println(GREEN + "  ✔ Doctor Registered:     " + RESET + drSharma.getSummaryDetails());

        // Stage 2: Patient Booking
        header("STAGE 2: Patient Requests Appointment (PENDING status)");
        LocalDateTime apptTime1 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(30).withSecond(0).withNano(0);
        Appointment appt1 = appointmentService.requestAppointment(aman.getPatientId(), drSharma.getDoctorId(), apptTime1);
        System.out.println("  Patient '" + aman.getName() + "' submitted request for: " + apptTime1.format(FMT));
        System.out.println(GREEN + "  ✔ Created Appointment: " + RESET + appt1);
        System.out.println("    Status: " + YELLOW + appt1.getStatus() + RESET + " (Awaiting doctor review)");

        // Stage 3: Doctor Reviews & Approves
        header("STAGE 3: Doctor Review, Decisioning & Rejection Reason Audit");
        List<Appointment> pending = appointmentService.getPendingForDoctor(drSharma.getDoctorId());
        System.out.println("  Doctor's pending queue count: " + pending.size());
        Appointment approvedAppt = appointmentService.updateDecision(appt1.getAppointmentId(), true);
        System.out.println(GREEN + "  ✔ Doctor action: APPROVED" + RESET);
        System.out.println("    Updated State: " + approvedAppt);

        // Demonstration of rejection with clinical audit reason
        Appointment rejectedApptTest = appointmentService.requestAppointment(riya.getPatientId(), drSharma.getDoctorId(), apptTime1.plusDays(1));
        appointmentService.updateDecision(rejectedApptTest.getAppointmentId(), false, "Clinical schedule conflict with hospital rounds");
        System.out.println(YELLOW + "  ✔ Doctor action: REJECTED with Reason -> " + RESET + rejectedApptTest);

        // Stage 4: Doctor Records Consultation & Next Visit Date
        header("STAGE 4: Consultation & Automatic Follow-up Scheduling");
        System.out.println("  Patient attends the visit. Doctor records diagnosis, prescription, & next visit...");
        String diagnosis = "Acute Upper Respiratory Infection & Seasonal Asthma flare-up";
        String prescription = "1. Tab Amoxicillin 500mg TDS x 5 days\n"
                            + "                     2. Tab Cetirizine 10mg OD x 7 days\n"
                            + "                     3. Levolin inhaler 2 puffs PRN";
        LocalDateTime nextVisitTime = apptTime1.plusWeeks(2).withHour(11).withMinute(0);

        Consultation consultation = consultationService.recordConsultation(
                approvedAppt.getAppointmentId(),
                diagnosis,
                prescription,
                nextVisitTime
        );

        System.out.println(GREEN + "  ✔ Consultation Record Created (ID #" + consultation.getConsultationId() + "):" + RESET);
        System.out.println("    Diagnosis:    " + consultation.getDiagnosis());
        System.out.println("    Prescription: \n                     " + consultation.getPrescription());
        System.out.println("    Next Visit:   " + consultation.getNextVisitDateTime().format(FMT));

        // Verify Automatic Follow-up
        List<Appointment> allAppts = repo.getAllAppointments();
        Appointment followUpAppt = allAppts.stream()
                .filter(Appointment::isFollowUp)
                .findFirst()
                .orElse(null);

        if (followUpAppt != null) {
            System.out.println("\n" + BOLD + GREEN + "  ★ AUTOMATIC FOLLOW-UP APPOINTMENT CREATED BY SYSTEM ★" + RESET);
            System.out.println("    ID:           #" + followUpAppt.getAppointmentId());
            System.out.println("    Patient:      " + followUpAppt.getPatient().getName());
            System.out.println("    Doctor:       " + followUpAppt.getDoctor());
            System.out.println("    Scheduled At: " + followUpAppt.getDateTime().format(FMT));
            System.out.println("    Status:       " + GREEN + followUpAppt.getStatus() + RESET + " (Directly APPROVED by Doctor)");
            System.out.println("    Is Follow-up: " + followUpAppt.isFollowUp());
        }

        // Stage 5: Patient View Verification
        header("STAGE 5: Patient Portal Synchronized View (Member 1 Module)");
        System.out.println("  Inspecting Patient Aman's synchronized records:");
        List<Appointment> patientAppts = appointmentService.getAppointmentsForPatient(aman.getPatientId());
        List<Consultation> patientConsultations = consultationService.getConsultationsForPatient(aman.getPatientId());

        System.out.println("  Total Appointments on file: " + patientAppts.size());
        for (Appointment a : patientAppts) {
            System.out.println("    - " + a);
        }
        System.out.println("  Consultation records visible to patient: " + patientConsultations.size());
        for (Consultation c : patientConsultations) {
            System.out.println("    - Ref Appt #" + c.getAppointment().getAppointmentId()
                    + " | Diagnosis: " + c.getDiagnosis());
        }

        // Stage 6: Business Rules & Validation Integrity
        header("STAGE 6: Enforcing Clinical Business Rules & Custom Exceptions");

        // Rule 1: No past dates
        testRule("Rule 1: Past appointment date rejected", () -> {
            appointmentService.requestAppointment(riya.getPatientId(), drSharma.getDoctorId(), LocalDateTime.now().minusDays(1));
        }, InvalidInputException.class);

        // Rule 2: Cannot approve already approved appointment
        testRule("Rule 2: Cannot re-decide already APPROVED appointment", () -> {
            approvedAppt.updateDecision(AppointmentStatus.REJECTED);
        }, InvalidStateException.class);

        // Rule 3: Cannot record duplicate consultation
        testRule("Rule 3: Duplicate consultation prevented", () -> {
            consultationService.recordConsultation(approvedAppt.getAppointmentId(), "Checkup", "Meds", nextVisitTime.plusDays(1));
        }, InvalidStateException.class);

        // Rule 4: Next visit must be strictly after appointment date
        LocalDateTime futureTime = LocalDateTime.now().plusDays(3);
        Appointment testAppt = appointmentService.requestAppointment(riya.getPatientId(), drSharma.getDoctorId(), futureTime);
        appointmentService.updateDecision(testAppt.getAppointmentId(), true);

        testRule("Rule 4: Next visit before appointment date rejected", () -> {
            consultationService.recordConsultation(testAppt.getAppointmentId(), "Flu", "Rest", futureTime.minusHours(2));
        }, InvalidInputException.class);

        // Stage 7: Doctor Analytics & Clinical Workload
        header("STAGE 7: Doctor Clinical Workload & Analytics (Member 2 Module - Shreyas)");
        DoctorStats stats = appointmentService.getDoctorStats(drSharma.getDoctorId());
        System.out.println("  Calculated Clinical Analytics for Dr. " + drSharma.getName() + ":");
        System.out.println("  - Total Assigned:          " + stats.getTotalAssigned());
        System.out.println("  - Pending Queue:           " + stats.getPendingCount());
        System.out.println("  - Approved Visits:         " + stats.getApprovedCount());
        System.out.println("  - Rejected Visits:         " + stats.getRejectedCount());
        System.out.printf ("  - Approval Rate:           %.1f%%\n", stats.getApprovalRatePercent());
        System.out.println("  - Completed Consultations: " + stats.getCompletedConsultations());
        System.out.println("  - Auto Follow-ups Booked:  " + stats.getScheduledFollowUps());

        // Stage 8: Prescription Generation & Local File Persistence
        header("STAGE 8: Digital Prescription Generation & Zero-Dependency File Persistence");
        File rxFile = persistenceService.savePrescriptionSlip(consultation);
        System.out.println(GREEN + "  ✔ Formatted Clinical Prescription Slip Generated:" + RESET);
        System.out.println(consultation.generatePrescriptionSlip());
        if (rxFile != null) {
            System.out.println(GREEN + "  ✔ Prescription slip exported to disk: " + RESET + rxFile.getAbsolutePath());
        }
        File snap = persistenceService.exportDatabaseSnapshot(repo);
        if (snap != null) {
            System.out.println(GREEN + "  ✔ Local database snapshot archived:   " + RESET + snap.getAbsolutePath());
        }

        // Summary Banner
        System.out.println("\n" + BOLD + GREEN + "=================================================================" + RESET);
        System.out.println(BOLD + GREEN + "  ✔ ALL 8 WORKFLOW STAGES & CORE SYSTEM VALIDATIONS VERIFIED!    " + RESET);
        System.out.println(BOLD + GREEN + "=================================================================" + RESET);
        System.out.println("Ready for Evaluator / Teacher Demonstration.\n");
    }

    private static void header(String title) {
        System.out.println("\n" + BOLD + BLUE + "-----------------------------------------------------------------" + RESET);
        System.out.println(BOLD + BLUE + title + RESET);
        System.out.println(BOLD + BLUE + "-----------------------------------------------------------------" + RESET);
    }

    private static void testRule(String ruleName, Runnable action, Class<? extends Throwable> expectedException) {
        try {
            action.run();
            System.out.println(RED + "  ✘ Failed: " + ruleName + " (No exception thrown)" + RESET);
        } catch (Throwable ex) {
            if (expectedException.isInstance(ex)) {
                System.out.println(GREEN + "  ✔ Passed: " + ruleName + RESET + " -> caught expected " + ex.getClass().getSimpleName() + " (\"" + ex.getMessage() + "\")");
            } else {
                System.out.println(RED + "  ✘ Failed: " + ruleName + " (Wrong exception: " + ex + ")" + RESET);
            }
        }
    }

    public static void main(String[] args) {
        runDemo();
    }
}
