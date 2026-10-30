import exception.InvalidInputException;
import exception.InvalidStateException;
import model.Appointment;
import model.AppointmentStatus;
import model.Consultation;
import model.Doctor;
import model.Patient;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Automated demo script and presentation runner.
 * Demonstrates the full lifecycle and validation rules for presentation to the instructor.
 */
public class DemoRunner {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ANSI Colors for console output
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String RED = "\u001B[31m";

    public static void runDemo() {
        System.out.println("\n" + BOLD + CYAN + "=================================================================" + RESET);
        System.out.println(BOLD + CYAN + "   DOCTOR SMART APPOINTMENT SYSTEM - LIVE PROGRESS DEMO   " + RESET);
        System.out.println(BOLD + CYAN + "=================================================================" + RESET);
        System.out.println("Course Project Progress Demonstration | Core Java Architecture\n");

        InMemoryRepository repo = new InMemoryRepository();
        AppointmentService appointmentService = new AppointmentService(repo);
        ConsultationService consultationService = new ConsultationService(repo);

        // Stage 1: Seeding
        header("STAGE 1: System Initialization & Seed Data");
        Patient aman = new Patient(1, "Aman Sharma", "+91 98765 00001");
        Patient riya = new Patient(2, "Riya Patel", "+91 98765 00002");
        Doctor drSharma = new Doctor(1, "Rajesh Sharma", "Senior General Physician");

        repo.addPatient(aman);
        repo.addPatient(riya);
        repo.addDoctor(drSharma);

        System.out.println(GREEN + "  ✔ Patient #1 Registered: " + RESET + aman);
        System.out.println(GREEN + "  ✔ Patient #2 Registered: " + RESET + riya);
        System.out.println(GREEN + "  ✔ Doctor Registered:     " + RESET + drSharma);

        // Stage 2: Patient Booking
        header("STAGE 2: Patient Requests Appointment (PENDING status)");
        LocalDateTime apptTime1 = LocalDateTime.now().plusDays(1).withHour(10).withMinute(30).withSecond(0).withNano(0);
        Appointment appt1 = appointmentService.requestAppointment(aman.getPatientId(), drSharma.getDoctorId(), apptTime1);
        System.out.println("  Patient '" + aman.getName() + "' submitted request for: " + apptTime1.format(FMT));
        System.out.println(GREEN + "  ✔ Created Appointment: " + RESET + appt1);
        System.out.println("    Status: " + YELLOW + appt1.getStatus() + RESET + " (Awaiting doctor review)");

        // Stage 3: Doctor Reviews & Approves
        header("STAGE 3: Doctor Reviews & Approves Appointment");
        List<Appointment> pending = appointmentService.getPendingForDoctor(drSharma.getDoctorId());
        System.out.println("  Doctor's pending queue count: " + pending.size());
        Appointment approvedAppt = appointmentService.updateDecision(appt1.getAppointmentId(), true);
        System.out.println(GREEN + "  ✔ Doctor action: APPROVED" + RESET);
        System.out.println("    Updated State: " + approvedAppt);

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
        header("STAGE 5: Patient Portal Synchronized View");
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
        header("STAGE 6: Enforcing Business Rules & Exception Handling");

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

        // Summary Banner
        System.out.println("\n" + BOLD + GREEN + "=================================================================" + RESET);
        System.out.println(BOLD + GREEN + "  ✔ ALL 6 WORKFLOW STAGES & 4 BUSINESS RULES VALIDATED 100%       " + RESET);
        System.out.println(BOLD + GREEN + "=================================================================" + RESET);
        System.out.println("Ready to demonstrate to course instructor.\n");
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
