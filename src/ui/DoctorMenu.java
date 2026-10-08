package ui;

import model.*;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;
import java.util.List;

public class DoctorMenu {
    private final InMemoryRepository repo;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;
    private final InputHelper in;

    public DoctorMenu(InMemoryRepository repo, AppointmentService as,
                      ConsultationService cs, InputHelper in) {
        this.repo = repo;
        this.appointmentService = as;
        this.consultationService = cs;
        this.in = in;
    }

    public void run() {
        System.out.println("\nDoctors:");
        for (Doctor d : repo.getAllDoctors()) System.out.println("  [" + d.getDoctorId() + "] " + d);
        int doctorId;
        try {
            doctorId = in.readInt("Enter your doctor id: ");
            repo.getDoctor(doctorId);
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n--- DOCTOR MENU ---");
            System.out.println("1. View pending requests");
            System.out.println("2. Approve / Reject a request");
            System.out.println("3. Record consultation (diagnosis, prescription, next visit)");
            System.out.println("0. Back");
            try {
                int ch = in.readInt("Choice: ");
                if (ch == 0) return;
                switch (ch) {
                    case 1 -> showPending(doctorId);
                    case 2 -> decide(doctorId);
                    case 3 -> consult(doctorId);
                    default -> System.out.println("Invalid choice.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showPending(int doctorId) {
        List<Appointment> list = appointmentService.getPendingForDoctor(doctorId);
        if (list.isEmpty()) System.out.println("No pending requests.");
        list.forEach(System.out::println);
    }

    private void decide(int doctorId) {
        showPending(doctorId);
        if (appointmentService.getPendingForDoctor(doctorId).isEmpty()) return;
        int id = in.readInt("Appointment id: ");
        String d = in.readLine("Approve (a) or Reject (r)? ").toLowerCase();
        if (!d.equals("a") && !d.equals("r")) {
            System.out.println("Enter 'a' or 'r'.");
            return;
        }
        Appointment a = appointmentService.updateDecision(id, d.equals("a"));
        System.out.println("Updated -> " + a);
    }

    private void consult(int doctorId) {
        List<Appointment> list = appointmentService.getAwaitingConsultation(doctorId);
        if (list.isEmpty()) {
            System.out.println("No approved appointments awaiting consultation.");
            return;
        }
        list.forEach(System.out::println);
        int id = in.readInt("Appointment id: ");
        String diagnosis = in.readLine("Diagnosis: ");
        String prescription = in.readLine("Prescription: ");
        var next = in.readDateTime("Next visit");
        Consultation c = consultationService.recordConsultation(id, diagnosis, prescription, next);
        System.out.println("Saved.\n" + c);
        System.out.println("Follow-up appointment created automatically.");
    }
}
