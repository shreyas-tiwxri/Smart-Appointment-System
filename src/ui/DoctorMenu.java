package ui;

import model.*;
import repository.FilePersistenceService;
import repository.IDataRepository;
import service.AppointmentService;
import service.ConsultationService;

import java.io.File;
import java.util.List;

/**
 * Interactive CLI menu for Doctors.
 * Designed and implemented by Member 2 (Shreyas Tiwari).
 * Features clinical decisioning, consultation recording, analytics, billing, and Rx export.
 */
public class DoctorMenu {
    private final IDataRepository repo;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;
    private final FilePersistenceService persistenceService;
    private final InputHelper in;

    public DoctorMenu(IDataRepository repo, AppointmentService as,
                      ConsultationService cs, InputHelper in) {
        this(repo, as, cs, new FilePersistenceService(), in);
    }

    public DoctorMenu(IDataRepository repo, AppointmentService as,
                      ConsultationService cs, FilePersistenceService fps, InputHelper in) {
        this.repo = repo;
        this.appointmentService = as;
        this.consultationService = cs;
        this.persistenceService = fps;
        this.in = in;
    }

    public void run() {
        System.out.println("\n--- PRACTITIONER SELECTION ---");
        for (Doctor d : repo.getAllDoctors()) {
            System.out.println("  [" + d.getDoctorId() + "] " + d.getSummaryDetails());
        }
        int doctorId;
        try {
            doctorId = in.readInt("Enter your doctor ID: ");
            Doctor doc = repo.getDoctor(doctorId);
            System.out.println("Welcome, Dr. " + doc.getName() + " (" + doc.getSpecialty() + ")");
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("            DOCTOR WORKSPACE & CLINICAL PORTAL         ");
            System.out.println("=======================================================");
            System.out.println("1. View Pending Requests Queue");
            System.out.println("2. Review & Decide on Appointment (Approve / Reject)");
            System.out.println("3. Record Consultation (Diagnosis, Rx, Billing & Next Visit)");
            System.out.println("4. View Full Clinical Schedule / Patient Roster");
            System.out.println("5. View Completed Consultation History");
            System.out.println("6. View Clinical Analytics & Performance Metrics");
            System.out.println("7. Export / Print Digital Prescription Slip");
            System.out.println("8. View Practice Revenue & Medical Invoices");
            System.out.println("9. View Doctor Notification Alerts");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            try {
                int ch = in.readInt("Select Option: ");
                if (ch == 0) return;
                switch (ch) {
                    case 1 -> showPending(doctorId);
                    case 2 -> decide(doctorId);
                    case 3 -> consult(doctorId);
                    case 4 -> showFullSchedule(doctorId);
                    case 5 -> showConsultationHistory(doctorId);
                    case 6 -> showDoctorAnalytics(doctorId);
                    case 7 -> exportPrescription(doctorId);
                    case 8 -> showDoctorInvoices(doctorId);
                    case 9 -> showNotifications(doctorId);
                    default -> System.out.println("Invalid choice. Please enter 0-9.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showPending(int doctorId) {
        System.out.println("\n[Pending Appointments Awaiting Review]");
        List<Appointment> list = appointmentService.getPendingForDoctor(doctorId);
        if (list.isEmpty()) {
            System.out.println("  No pending requests in your queue.");
            return;
        }
        list.forEach(a -> System.out.println("  -> " + a));
    }

    private void decide(int doctorId) {
        List<Appointment> pending = appointmentService.getPendingForDoctor(doctorId);
        if (pending.isEmpty()) {
            System.out.println("No pending requests to decide on.");
            return;
        }
        pending.forEach(a -> System.out.println("  -> " + a));

        int id = in.readInt("\nEnter Appointment ID to decide: ");
        String d = in.readLine("Decision - Approve (a) or Reject (r)? ").toLowerCase();
        if (!d.equals("a") && !d.equals("r")) {
            System.out.println("Invalid decision. Enter 'a' to approve or 'r' to reject.");
            return;
        }

        boolean approve = d.equals("a");
        String reason = null;
        if (!approve) {
            reason = in.readLine("Enter clinical reason for rejection (e.g., Doctor unavailable, referral needed): ");
        }

        Appointment a = appointmentService.updateDecision(id, approve, reason);
        System.out.println("Successfully updated -> " + a);
        persistenceService.logAudit("Doctor #" + doctorId + " updated Appointment #" + id + " to " + a.getStatus());
    }

    private void consult(int doctorId) {
        List<Appointment> list = appointmentService.getAwaitingConsultation(doctorId);
        if (list.isEmpty()) {
            System.out.println("No approved appointments awaiting consultation.");
            return;
        }
        System.out.println("\n[Approved Appointments Ready For Consultation]");
        list.forEach(a -> System.out.println("  -> " + a));

        int id = in.readInt("\nEnter Appointment ID: ");
        String diagnosis = in.readLine("Clinical Diagnosis: ");
        String prescription = in.readLine("Prescription & Dosage: ");
        var next = in.readDateTime("Next Follow-up Visit");
        String payment = in.readLine("Payment Mode (UPI/Cash/Card) [Default: UPI]: ");
        if (payment.isBlank()) payment = "UPI";

        Consultation c = consultationService.recordConsultation(id, diagnosis, prescription, next, payment);
        System.out.println("\n✔ Consultation Recorded Successfully!");
        System.out.println(c);
        System.out.println("✔ Automatic follow-up appointment generated with status APPROVED.");

        // Automatically archive digital Rx slip
        File rxFile = persistenceService.savePrescriptionSlip(c);
        if (rxFile != null) {
            System.out.println("✔ Digital Rx Slip exported to: " + rxFile.getPath());
        }

        // Automatically archive medical invoice
        Invoice inv = consultationService.getInvoiceByConsultationId(c.getConsultationId());
        File invFile = persistenceService.saveInvoiceReceipt(inv);
        if (invFile != null) {
            System.out.println("✔ Medical Invoice generated: " + invFile.getPath() + " (Total: ₹" + inv.getTotalAmount() + ")");
        }
    }

    private void showFullSchedule(int doctorId) {
        System.out.println("\n[Doctor Clinical Roster & Complete Schedule]");
        List<Appointment> list = appointmentService.getAllAppointmentsForDoctor(doctorId);
        if (list.isEmpty()) {
            System.out.println("  No appointments found in your schedule.");
            return;
        }
        list.forEach(a -> System.out.println("  -> " + a));
    }

    private void showConsultationHistory(int doctorId) {
        System.out.println("\n[Completed Consultation Records]");
        List<Consultation> list = consultationService.getConsultationsForDoctor(doctorId);
        if (list.isEmpty()) {
            System.out.println("  No consultation records found.");
            return;
        }
        for (Consultation c : list) {
            System.out.println("-------------------------------------------------------");
            System.out.println(c);
        }
    }

    private void showDoctorAnalytics(int doctorId) {
        DoctorStats stats = appointmentService.getDoctorStats(doctorId);
        System.out.println(stats);
    }

    private void exportPrescription(int doctorId) {
        List<Consultation> list = consultationService.getConsultationsForDoctor(doctorId);
        if (list.isEmpty()) {
            System.out.println("No consultations available to export.");
            return;
        }
        System.out.println("\n[Select Consultation to Export / View Prescription]");
        for (Consultation c : list) {
            System.out.printf("  [#%d] Patient: %s | Date: %s | Diagnosis: %s\n",
                    c.getConsultationId(),
                    c.getAppointment().getPatient().getName(),
                    c.getAppointment().getDateTime(),
                    c.getDiagnosis());
        }

        int consultId = in.readInt("Enter Consultation ID: ");
        Consultation selected = null;
        for (Consultation c : list) {
            if (c.getConsultationId() == consultId) {
                selected = c;
                break;
            }
        }

        if (selected == null) {
            System.out.println("Consultation ID not found.");
            return;
        }

        System.out.println("\n" + selected.generatePrescriptionSlip());
        File f = persistenceService.savePrescriptionSlip(selected);
        if (f != null) {
            System.out.println("Prescription saved to disk at: " + f.getAbsolutePath());
        }
    }

    private void showDoctorInvoices(int doctorId) {
        System.out.println("\n[Practice Billing Records & Revenue]");
        double totalRevenue = 0.0;
        int count = 0;
        for (Invoice inv : repo.getAllInvoices()) {
            if (inv.getConsultation().getAppointment().getDoctor().getDoctorId() == doctorId) {
                System.out.println(inv);
                totalRevenue += inv.getTotalAmount();
                count++;
            }
        }
        if (count == 0) {
            System.out.println("  No billing records generated yet.");
        } else {
            System.out.printf("\nTotal Invoices Issued: %d | Gross Revenue Collected: ₹%.2f\n", count, totalRevenue);
        }
    }

    private void showNotifications(int doctorId) {
        System.out.println("\n[Doctor Clinical Alert Inbox]");
        List<NotificationEvent> events = appointmentService.getNotificationService().getNotificationsForDoctor(doctorId);
        if (events.isEmpty()) {
            System.out.println("  No alerts at this time.");
            return;
        }
        for (NotificationEvent ev : events) {
            System.out.println("  " + ev);
        }
    }
}
