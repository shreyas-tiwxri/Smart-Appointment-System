package ui;

import model.*;
import repository.FilePersistenceService;
import repository.IDataRepository;
import service.AppointmentService;
import service.ConsultationService;

import java.io.File;
import java.util.List;

/**
 * Interactive CLI menu for Patients.
 * Maintained by Member 1 (Mohit Srivastav).
 * Provides booking, tracking, cancellation, medical invoices, and notification alerts.
 */
public class PatientMenu {
    private final IDataRepository repo;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;
    private final FilePersistenceService persistenceService;
    private final InputHelper in;

    public PatientMenu(IDataRepository repo, AppointmentService as,
                       ConsultationService cs, InputHelper in) {
        this(repo, as, cs, new FilePersistenceService(), in);
    }

    public PatientMenu(IDataRepository repo, AppointmentService as,
                       ConsultationService cs, FilePersistenceService fps, InputHelper in) {
        this.repo = repo;
        this.appointmentService = as;
        this.consultationService = cs;
        this.persistenceService = fps;
        this.in = in;
    }

    public void run() {
        System.out.println("\n--- PATIENT IDENTIFICATION ---");
        for (Patient p : repo.getAllPatients()) {
            System.out.println("  [" + p.getPatientId() + "] " + p.getSummaryDetails());
        }
        int patientId;
        try {
            patientId = in.readInt("Enter your patient ID: ");
            Patient patient = repo.getPatient(patientId);
            System.out.println("Welcome, " + patient.getName() + "!");
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("             PATIENT APPOINTMENT & HEALTH PORTAL       ");
            System.out.println("=======================================================");
            System.out.println("1. Request New Doctor Appointment");
            System.out.println("2. View My Appointments (Track Status & Follow-ups)");
            System.out.println("3. Cancel or Reschedule an Appointment");
            System.out.println("4. View Medical Diagnoses & Prescriptions");
            System.out.println("5. Download / Print Official Prescription Slip");
            System.out.println("6. View Medical Invoices & Billing Receipts");
            System.out.println("7. View Notification & Status Alerts");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            try {
                int ch = in.readInt("Select Option: ");
                if (ch == 0) return;
                switch (ch) {
                    case 1 -> requestAppointment(patientId);
                    case 2 -> viewAppointments(patientId);
                    case 3 -> cancelOrReschedule(patientId);
                    case 4 -> viewConsultations(patientId);
                    case 5 -> downloadPrescriptionSlip(patientId);
                    case 6 -> viewInvoices(patientId);
                    case 7 -> viewNotifications(patientId);
                    default -> System.out.println("Invalid choice. Please enter 0-7.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void requestAppointment(int patientId) {
        System.out.println("\n[Available Medical Practitioners]");
        for (Doctor d : repo.getAllDoctors()) {
            System.out.println("  [" + d.getDoctorId() + "] " + d.getSummaryDetails());
        }
        int doctorId = in.readInt("Select Doctor ID: ");
        var dt = in.readDateTime("Preferred Date & Time");
        Appointment a = appointmentService.requestAppointment(patientId, doctorId, dt);
        System.out.println("\n✔ Appointment Request Submitted Successfully!");
        System.out.println("Details: " + a);
        System.out.println("Current Status: " + a.getStatus() + " (Pending doctor confirmation)");
        persistenceService.logAudit("Patient #" + patientId + " requested Appointment #" + a.getAppointmentId());
    }

    private void viewAppointments(int patientId) {
        System.out.println("\n[My Appointment History & Follow-ups]");
        List<Appointment> list = appointmentService.getAppointmentsForPatient(patientId);
        if (list.isEmpty()) {
            System.out.println("  No appointments found on record.");
            return;
        }
        list.forEach(a -> System.out.println("  -> " + a));
    }

    private void cancelOrReschedule(int patientId) {
        List<Appointment> list = appointmentService.getAppointmentsForPatient(patientId);
        if (list.isEmpty()) {
            System.out.println("No appointments available to modify.");
            return;
        }
        System.out.println("\n[Select Appointment to Cancel or Reschedule]");
        for (Appointment a : list) {
            if (a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.APPROVED) {
                System.out.println("  -> " + a);
            }
        }

        int apptId = in.readInt("Enter Appointment ID: ");
        String action = in.readLine("Choose action - Cancel (c) or Reschedule (r)? ").toLowerCase();

        if (action.equals("c")) {
            String reason = in.readLine("Enter reason for cancellation: ");
            Appointment cancelled = appointmentService.cancelAppointment(apptId, reason);
            System.out.println("✔ Appointment successfully cancelled: " + cancelled);
            persistenceService.logAudit("Patient #" + patientId + " cancelled Appointment #" + apptId);
        } else if (action.equals("r")) {
            var newDt = in.readDateTime("Enter new preferred date & time");
            Appointment rescheduled = appointmentService.rescheduleAppointment(apptId, newDt);
            System.out.println("✔ Appointment successfully rescheduled: " + rescheduled);
            persistenceService.logAudit("Patient #" + patientId + " rescheduled Appointment #" + apptId + " to " + newDt);
        } else {
            System.out.println("Invalid choice. Enter 'c' or 'r'.");
        }
    }

    private void viewConsultations(int patientId) {
        System.out.println("\n[Clinical Diagnoses & Consultation History]");
        List<Consultation> list = consultationService.getConsultationsForPatient(patientId);
        if (list.isEmpty()) {
            System.out.println("  No consultation records available yet.");
            return;
        }
        for (Consultation c : list) {
            System.out.println("-------------------------------------------------------");
            System.out.println(c);
        }
    }

    private void downloadPrescriptionSlip(int patientId) {
        List<Consultation> list = consultationService.getConsultationsForPatient(patientId);
        if (list.isEmpty()) {
            System.out.println("No clinical records available to download.");
            return;
        }
        System.out.println("\nSelect consultation record to view/export slip:");
        for (Consultation c : list) {
            System.out.printf("  [#%d] Date: %s | Dr. %s | Diagnosis: %s\n",
                    c.getConsultationId(),
                    c.getAppointment().getDateTime(),
                    c.getAppointment().getDoctor().getName(),
                    c.getDiagnosis());
        }

        int cId = in.readInt("Enter Consultation ID: ");
        Consultation selected = null;
        for (Consultation c : list) {
            if (c.getConsultationId() == cId) {
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
            System.out.println("Prescription slip archived to disk: " + f.getAbsolutePath());
        }
    }

    private void viewInvoices(int patientId) {
        System.out.println("\n[Medical Invoices & Payment Receipts]");
        List<Invoice> invoices = consultationService.getInvoicesForPatient(patientId);
        if (invoices.isEmpty()) {
            System.out.println("  No invoices issued yet.");
            return;
        }
        for (Invoice inv : invoices) {
            System.out.println(inv.generateReceipt());
            persistenceService.saveInvoiceReceipt(inv);
        }
    }

    private void viewNotifications(int patientId) {
        System.out.println("\n[Patient Real-Time Notification Alerts]");
        List<NotificationEvent> events = appointmentService.getNotificationService().getNotificationsForPatient(patientId);
        if (events.isEmpty()) {
            System.out.println("  No new notification alerts.");
            return;
        }
        for (NotificationEvent ev : events) {
            System.out.println("  " + ev);
        }
    }
}
