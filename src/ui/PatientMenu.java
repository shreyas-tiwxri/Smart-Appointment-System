package ui;

import model.*;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;
import java.util.List;

public class PatientMenu {
    private final InMemoryRepository repo;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;
    private final InputHelper in;

    public PatientMenu(InMemoryRepository repo, AppointmentService as,
                       ConsultationService cs, InputHelper in) {
        this.repo = repo;
        this.appointmentService = as;
        this.consultationService = cs;
        this.in = in;
    }

    public void run() {
        System.out.println("\nPatients:");
        for (Patient p : repo.getAllPatients()) System.out.println("  " + p);
        int patientId;
        try {
            patientId = in.readInt("Enter your patient id: ");
            repo.getPatient(patientId);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n--- PATIENT MENU ---");
            System.out.println("1. Request appointment");
            System.out.println("2. View my appointments (approval/rejection + follow-ups)");
            System.out.println("3. View diagnosis & prescription");
            System.out.println("0. Back");
            try {
                int ch = in.readInt("Choice: ");
                if (ch == 0) return;
                switch (ch) {
                    case 1 -> requestAppointment(patientId);
                    case 2 -> viewAppointments(patientId);
                    case 3 -> viewConsultations(patientId);
                    default -> System.out.println("Invalid choice.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void requestAppointment(int patientId) {
        System.out.println("Doctors:");
        for (Doctor d : repo.getAllDoctors()) System.out.println("  [" + d.getDoctorId() + "] " + d);
        int doctorId = in.readInt("Doctor id: ");
        var dt = in.readDateTime("Preferred date & time");
        Appointment a = appointmentService.requestAppointment(patientId, doctorId, dt);
        System.out.println("Request submitted -> " + a);
    }

    private void viewAppointments(int patientId) {
        List<Appointment> list = appointmentService.getAppointmentsForPatient(patientId);
        if (list.isEmpty()) System.out.println("No appointments yet.");
        list.forEach(System.out::println);
    }

    private void viewConsultations(int patientId) {
        List<Consultation> list = consultationService.getConsultationsForPatient(patientId);
        if (list.isEmpty()) System.out.println("No consultation records yet.");
        list.forEach(System.out::println);
    }
}
