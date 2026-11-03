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
 * Provides booking, appointment tracking, and clinical consultation reviews.
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
            System.out.println("2. View My Appointments (Track Pending, Approved, Follow-up)");
            System.out.println("3. View Medical Diagnoses & Prescriptions");
            System.out.println("4. Download / Print Official Prescription Slip");
            System.out.println("0. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            try {
                int ch = in.readInt("Select Option: ");
                if (ch == 0) return;
                switch (ch) {
                    case 1 -> requestAppointment(patientId);
                    case 2 -> viewAppointments(patientId);
                    case 3 -> viewConsultations(patientId);
                    case 4 -> downloadPrescriptionSlip(patientId);
                    default -> System.out.println("Invalid choice. Please enter 0-4.");
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
}
