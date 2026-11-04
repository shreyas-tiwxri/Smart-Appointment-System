import model.Doctor;
import model.Patient;
import repository.FilePersistenceService;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;
import service.NotificationService;
import ui.DoctorMenu;
import ui.InputHelper;
import ui.PatientMenu;

import java.io.File;
import java.util.Scanner;

/**
 * Main application bootstrap for the Doctor Smart Appointment System.
 * Collaborative project by Mohit Srivastav (Member 1) and Shreyas Tiwari (Member 2).
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            DemoRunner.runDemo();
            return;
        }

        InMemoryRepository repo = new InMemoryRepository();
        FilePersistenceService persistenceService = new FilePersistenceService();
        NotificationService notificationService = new NotificationService();
        seed(repo);

        // Register Graceful JVM Shutdown Hook to snapshot clinical database
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            persistenceService.exportDatabaseSnapshot(repo);
            persistenceService.logAudit("System gracefully shut down and state snapshot saved.");
        }));

        AppointmentService appointmentService = new AppointmentService(repo, notificationService);
        ConsultationService consultationService = new ConsultationService(repo, notificationService);
        Scanner sc = new Scanner(System.in);
        InputHelper in = new InputHelper(sc);

        PatientMenu patientMenu = new PatientMenu(repo, appointmentService, consultationService, persistenceService, in);
        DoctorMenu doctorMenu = new DoctorMenu(repo, appointmentService, consultationService, persistenceService, in);

        System.out.println("=================================================================");
        System.out.println("            DOCTOR SMART APPOINTMENT & CLINICAL SYSTEM           ");
        System.out.println("          Developed by Mohit Srivastav & Shreyas Tiwari          ");
        System.out.println("=================================================================");

        while (true) {
            System.out.println("\nMAIN SELECTION MENU:");
            System.out.println("  1. Login as Patient (Member 1 Portal)");
            System.out.println("  2. Login as Doctor  (Member 2 Clinical Workspace)");
            System.out.println("  3. Run Automated Demo Walkthrough (--demo mode)");
            System.out.println("  4. Export System Diagnostic Snapshot to Disk");
            System.out.println("  0. Exit");
            System.out.println("-----------------------------------------------------------------");

            String ch = in.readLine("Enter Choice: ");
            switch (ch) {
                case "1" -> patientMenu.run();
                case "2" -> doctorMenu.run();
                case "3" -> DemoRunner.runDemo();
                case "4" -> {
                    File f = persistenceService.exportDatabaseSnapshot(repo);
                    if (f != null) {
                        System.out.println("✔ Diagnostic snapshot generated at: " + f.getAbsolutePath());
                    }
                }
                case "0" -> {
                    System.out.println("Thank you for using Doctor Smart Appointment System. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 0-4.");
            }
        }
    }

    /** Sample clinical dataset for testing and immediate evaluation. */
    private static void seed(InMemoryRepository repo) {
        repo.addPatient(new Patient(1, "Aman Sharma", "+91 98765 00001"));
        repo.addPatient(new Patient(2, "Riya Patel", "+91 98765 00002"));
        repo.addPatient(new Patient(3, "Kabir Mehta", "+91 98765 00003"));

        repo.addDoctor(new Doctor(1, "Rajesh Sharma", "Senior General Physician", 600.0));
        repo.addDoctor(new Doctor(2, "Pooja Verma", "Cardiology Specialist", 850.0));
    }
}
