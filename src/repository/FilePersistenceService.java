package repository;

import model.Appointment;
import model.Consultation;
import model.Doctor;
import model.Invoice;
import model.Patient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for local file system persistence, snapshot export, and prescription slip generation.
 * Demonstrates Java I/O streams and zero-dependency disk persistence.
 */
public class FilePersistenceService {
    private static final String DATA_DIR = "records";
    private static final String PRESCRIPTION_DIR = "records/prescriptions";
    private static final String INVOICE_DIR = "records/invoices";
    private static final String AUDIT_LOG_FILE = "records/system_audit_log.txt";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public FilePersistenceService() {
        initDirectories();
    }

    private void initDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(PRESCRIPTION_DIR));
            Files.createDirectories(Paths.get(INVOICE_DIR));
        } catch (IOException e) {
            System.err.println("Warning: Unable to create records directory: " + e.getMessage());
        }
    }

    /**
     * Appends an audit log entry for system traceability.
     */
    public void logAudit(String event) {
        String logLine = "[" + LocalDateTime.now().format(FMT) + "] " + event;
        try (PrintWriter pw = new PrintWriter(new FileWriter(AUDIT_LOG_FILE, true))) {
            pw.println(logLine);
        } catch (IOException e) {
            // Fail gracefully without crashing application
        }
    }

    /**
     * Saves an official digital prescription slip to the local disk.
     */
    public File savePrescriptionSlip(Consultation consultation) {
        initDirectories();
        String filename = String.format("%s/Rx_Patient%d_Appt%d.txt",
                PRESCRIPTION_DIR,
                consultation.getAppointment().getPatient().getPatientId(),
                consultation.getAppointment().getAppointmentId());
        File file = new File(filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.print(consultation.generatePrescriptionSlip());
            logAudit("Exported Prescription slip for Appointment #" + consultation.getAppointment().getAppointmentId() + " to " + filename);
            return file;
        } catch (IOException e) {
            System.err.println("Failed to write prescription slip: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves an official medical billing receipt to the local disk.
     */
    public File saveInvoiceReceipt(Invoice invoice) {
        initDirectories();
        String filename = String.format("%s/%s_Patient%d.txt",
                INVOICE_DIR,
                invoice.getInvoiceId(),
                invoice.getConsultation().getAppointment().getPatient().getPatientId());
        File file = new File(filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.print(invoice.generateReceipt());
            logAudit("Exported Medical Invoice " + invoice.getInvoiceId() + " to " + filename);
            return file;
        } catch (IOException e) {
            System.err.println("Failed to write invoice: " + e.getMessage());
            return null;
        }
    }

    /**
     * Exports a complete human-readable diagnostic snapshot of the repository state.
     */
    public File exportDatabaseSnapshot(IDataRepository repo) {
        initDirectories();
        String filename = DATA_DIR + "/database_snapshot.txt";
        File file = new File(filename);
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("================================================================================");
            pw.println("                DOCTOR SMART APPOINTMENT SYSTEM - DATA SNAPSHOT                 ");
            pw.println("                Generated: " + LocalDateTime.now().format(FMT));
            pw.println("================================================================================\n");

            pw.println("--- REGISTERED PATIENTS ---");
            for (Patient p : repo.getAllPatients()) {
                pw.printf("ID: %-4d | Name: %-25s | Contact: %s\n", p.getPatientId(), p.getName(), p.getContact());
            }

            pw.println("\n--- REGISTERED DOCTORS ---");
            for (Doctor d : repo.getAllDoctors()) {
                pw.printf("ID: %-4d | Name: Dr. %-21s | Specialty: %-25s | Fee: ₹%.2f\n",
                        d.getDoctorId(), d.getName(), d.getSpecialty(), d.getConsultationFee());
            }

            pw.println("\n--- APPOINTMENTS RECORD ---");
            List<Appointment> appts = repo.getAllAppointments();
            if (appts.isEmpty()) {
                pw.println("No appointments recorded.");
            } else {
                for (Appointment a : appts) {
                    pw.println(a.toString());
                }
            }

            pw.println("\n--- CONSULTATION & CLINICAL RECORDS ---");
            List<Consultation> consults = repo.getAllConsultations();
            if (consults.isEmpty()) {
                pw.println("No consultation records.");
            } else {
                for (Consultation c : consults) {
                    pw.printf("Consultation #%d (Appt #%d) | Diag: %s | Next: %s\n",
                            c.getConsultationId(), c.getAppointment().getAppointmentId(),
                            c.getDiagnosis(), c.getNextVisitDateTime());
                }
            }

            pw.println("\n--- MEDICAL BILLING & INVOICES ---");
            List<Invoice> invoices = repo.getAllInvoices();
            if (invoices.isEmpty()) {
                pw.println("No invoices issued.");
            } else {
                for (Invoice inv : invoices) {
                    pw.println(inv.toString());
                }
            }

            pw.println("\n================================================================================");
            pw.println("End of Snapshot. Total Appts: " + appts.size() + ", Consultations: " + consults.size() + ", Invoices: " + invoices.size());
            pw.println("================================================================================");

            logAudit("Exported complete database snapshot to " + filename);
            return file;
        } catch (IOException e) {
            System.err.println("Snapshot export failed: " + e.getMessage());
            return null;
        }
    }
}
