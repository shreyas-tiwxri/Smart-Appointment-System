import model.Doctor;
import model.Patient;
import repository.FilePersistenceService;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;
import service.NotificationService;
import server.SimpleHttpServer;
import ui.DoctorMenu;
import ui.InputHelper;
import ui.MainWindow;
import ui.PatientMenu;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.Scanner;
import util.Logger;

/**
 * Main application bootstrap for the Doctor Smart Appointment System.
 * Developed by: Mohit Srivastav & Shreyas Tiwari
 */
public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--demo")) {
            DemoRunner.runDemo();
            return;
        }
        if (args.length > 0 && (args[0].equalsIgnoreCase("--gui") || args[0].equalsIgnoreCase("-g"))) {
            MainWindow.main(args);
            return;
        }
        if (args.length > 0 && (args[0].equalsIgnoreCase("--web") || args[0].equalsIgnoreCase("-w"))) {
            InMemoryRepository r = new InMemoryRepository();
            seed(r);
            AppointmentService as = new AppointmentService(r);
            try {
                SimpleHttpServer s = new SimpleHttpServer(8080, r, as);
                s.start();
                s.openInBrowser();
                System.out.println("Press Enter to stop web server...");
                System.in.read();
                s.stop();
            } catch (Exception ex) {
                System.err.println("Failed to start server: " + ex.getMessage());
            }
            return;
        }
        // If double-clicked directly as a runnable JAR without terminal attached, launch Desktop GUI
        if (System.console() == null && args.length == 0 && !GraphicsEnvironment.isHeadless()) {
            MainWindow.main(args);
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

        Logger.info("=================================================================");
        Logger.info("            DOCTOR SMART APPOINTMENT & CLINICAL SYSTEM           ");
        Logger.info("          Developed by Mohit Srivastav & Shreyas Tiwari          ");
        Logger.info("=================================================================");

        while (true) {
            System.out.println("\nMAIN SELECTION MENU:");
            System.out.println("  1. Login as Patient (Member 1 Portal)");
            System.out.println("  2. Login as Doctor  (Member 2 Clinical Workspace)");
            System.out.println("  3. Run Automated Demo Walkthrough (--demo mode)");
            System.out.println("  4. Export System Diagnostic Snapshot to Disk");
            System.out.println("  5. Launch Desktop Clinical GUI (Swing Core Java)");
            System.out.println("  6. Launch Local Web Portal in Browser (http://localhost:8080)");
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
                case "5" -> {
                    System.out.println("Launching Core Java Swing Desktop Application...");
                    MainWindow.main(new String[]{});
                }
                case "6" -> {
                    try {
                        SimpleHttpServer server = new SimpleHttpServer(8080, repo, appointmentService);
                        server.start();
                        server.openInBrowser();
                        System.out.println("Web dashboard running at http://localhost:8080/");
                        in.readLine("Press Enter to return to main menu (server keeps running)...");
                    } catch (Exception ex) {
                        System.out.println("Could not start server: " + ex.getMessage());
                    }
                }
                case "0" -> {
                    System.out.println("Thank you for using Doctor Smart Appointment System. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please enter 0-6.");
            }
        }
    }

    /** Sample clinical dataset for testing and immediate evaluation. */
    private static void seed(InMemoryRepository repo) {
        repo.addPatient(new Patient(1, "Aman Sharma", "+91 98765 00001"));
        repo.addPatient(new Patient(2, "Riya Patel", "+91 98765 00002"));
        repo.addPatient(new Patient(3, "Kabir Mehta", "+91 98765 00003"));
        repo.addPatient(new Patient(4, "Sneha Reddy", "+91 98765 00004"));
        repo.addPatient(new Patient(5, "Vikram Malhotra", "+91 98765 00005"));

        repo.addDoctor(new Doctor(1, "Rajesh Sharma", "Senior General Physician", 600.0));
        repo.addDoctor(new Doctor(2, "Pooja Verma", "Specialist Cardiologist", 850.0));
        repo.addDoctor(new Doctor(3, "Arjun Kapoor", "Orthopedic Surgeon", 750.0));
        repo.addDoctor(new Doctor(4, "Ananya Iyer", "Consultant Neurologist", 900.0));
    }
}
