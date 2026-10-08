import model.Doctor;
import model.Patient;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;
import ui.DoctorMenu;
import ui.InputHelper;
import ui.PatientMenu;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        InMemoryRepository repo = new InMemoryRepository();
        seed(repo);

        AppointmentService appointmentService = new AppointmentService(repo);
        ConsultationService consultationService = new ConsultationService(repo);
        Scanner sc = new Scanner(System.in);
        InputHelper in = new InputHelper(sc);

        PatientMenu patientMenu = new PatientMenu(repo, appointmentService, consultationService, in);
        DoctorMenu doctorMenu = new DoctorMenu(repo, appointmentService, consultationService, in);

        System.out.println("=== Doctor Smart Appointment System ===");
        while (true) {
            System.out.println("\n1. Login as Patient\n2. Login as Doctor\n0. Exit");
            String ch = in.readLine("Choice: ");
            switch (ch) {
                case "1" -> patientMenu.run();
                case "2" -> doctorMenu.run();
                case "0" -> {
                    System.out.println("Bye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /** Sample data so the demo works out of the box. */
    private static void seed(InMemoryRepository repo) {
        repo.addPatient(new Patient(1, "Aman", "9876500001"));
        repo.addPatient(new Patient(2, "Riya", "9876500002"));
        repo.addDoctor(new Doctor(1, "Sharma", "General Physician"));
    }
}
