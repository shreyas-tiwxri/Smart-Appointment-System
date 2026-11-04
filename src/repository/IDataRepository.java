package repository;

import model.*;
import java.util.Collection;
import java.util.List;

/**
 * Data Access Object (DAO) interface abstraction for clinical data entities.
 * Demonstrates the Dependency Inversion Principle (DIP) and Interface Segregation (ISP):
 * High-level business services depend on this abstraction rather than concrete storage mechanisms.
 */
public interface IDataRepository {
    // Patient Operations
    void addPatient(Patient p);
    Patient getPatient(int id);
    Collection<Patient> getAllPatients();

    // Doctor Operations
    void addDoctor(Doctor d);
    Doctor getDoctor(int id);
    Collection<Doctor> getAllDoctors();

    // Appointment Operations
    int nextAppointmentId();
    void saveAppointment(Appointment a);
    Appointment getAppointment(int id);
    List<Appointment> getAllAppointments();

    // Consultation Operations
    int nextConsultationId();
    void saveConsultation(Consultation c);
    List<Consultation> getAllConsultations();

    // Invoicing Operations
    int nextInvoiceId();
    void saveInvoice(Invoice invoice);
    List<Invoice> getAllInvoices();
    Invoice getInvoiceByConsultationId(int consultationId);
}
