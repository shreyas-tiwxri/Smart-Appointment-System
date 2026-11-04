package service;

import exception.InvalidInputException;
import exception.InvalidStateException;
import exception.NotFoundException;
import model.*;
import repository.IDataRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service orchestrating clinical consultation records, diagnosis/prescription entry,
 * medical invoice generation, and automatic follow-up appointment scheduling.
 * Managed by Member 2 (Shreyas Tiwari).
 */
public class ConsultationService {
    private final IDataRepository repo;
    private final NotificationService notificationService;

    public ConsultationService(IDataRepository repo) {
        this(repo, new NotificationService());
    }

    public ConsultationService(IDataRepository repo, NotificationService notificationService) {
        this.repo = repo;
        this.notificationService = notificationService;
    }

    /**
     * Doctor records diagnosis + prescription for an APPROVED appointment and gives the
     * next-visit date/time. A follow-up Appointment and Invoice are created automatically.
     * Follow-up status is APPROVED because the doctor scheduled it directly during consultation.
     */
    public Consultation recordConsultation(int appointmentId, String diagnosis,
                                           String prescription, LocalDateTime nextVisit) {
        return recordConsultation(appointmentId, diagnosis, prescription, nextVisit, "UPI / Card");
    }

    /**
     * Overloaded consultation recording with custom billing payment mode.
     */
    public Consultation recordConsultation(int appointmentId, String diagnosis,
                                           String prescription, LocalDateTime nextVisit,
                                           String paymentMode) {
        Appointment a = repo.getAppointment(appointmentId);

        if (a.getStatus() != AppointmentStatus.APPROVED)
            throw new InvalidStateException("Consultation can only be recorded for an APPROVED appointment.");
        for (Consultation c : repo.getAllConsultations())
            if (c.getAppointment().getAppointmentId() == appointmentId)
                throw new InvalidStateException("Consultation already recorded for this appointment.");

        if (diagnosis == null || diagnosis.isBlank())
            throw new InvalidInputException("Diagnosis is required.");
        if (prescription == null || prescription.isBlank())
            throw new InvalidInputException("Prescription is required.");
        if (nextVisit == null)
            throw new InvalidInputException("Next-visit date and time are required.");
        if (!nextVisit.isAfter(a.getDateTime()))
            throw new InvalidInputException("Next visit must be after the current appointment.");

        Consultation c = new Consultation(repo.nextConsultationId(), a,
                                          diagnosis.trim(), prescription.trim(), nextVisit);
        repo.saveConsultation(c);

        // Auto-create follow-up appointment (same patient + doctor)
        Appointment followUp = new Appointment(repo.nextAppointmentId(), a.getPatient(), a.getDoctor(),
                                               nextVisit, AppointmentStatus.APPROVED, true);
        repo.saveAppointment(followUp);

        // Auto-generate clinical billing invoice
        Invoice invoice = new Invoice(repo.nextInvoiceId(), c, paymentMode);
        repo.saveInvoice(invoice);

        // Dispatch clinical completion alert to patient
        if (notificationService != null) {
            notificationService.notify("PATIENT", a.getPatient().getPatientId(), "CONSULTATION_COMPLETED",
                    String.format("Consultation completed with Dr. %s. Auto follow-up booked for %s. Invoice issued: %s (Total: ₹%.2f)",
                            a.getDoctor().getName(), nextVisit, invoice.getInvoiceId(), invoice.getTotalAmount()));
        }

        return c;
    }

    public List<Consultation> getConsultationsForPatient(int patientId) {
        repo.getPatient(patientId); // validate patient exists
        List<Consultation> out = new ArrayList<>();
        for (Consultation c : repo.getAllConsultations())
            if (c.getAppointment().getPatient().getPatientId() == patientId) out.add(c);
        return out;
    }

    /**
     * Retrieves all clinical consultations conducted by a specific doctor.
     * Developed by Member 2 (Shreyas Tiwari).
     */
    public List<Consultation> getConsultationsForDoctor(int doctorId) {
        repo.getDoctor(doctorId); // validate doctor exists
        List<Consultation> out = new ArrayList<>();
        for (Consultation c : repo.getAllConsultations())
            if (c.getAppointment().getDoctor().getDoctorId() == doctorId) out.add(c);
        return out;
    }

    /**
     * Retrieves consultation details for a specific appointment ID.
     */
    public Consultation getConsultationByAppointmentId(int appointmentId) {
        for (Consultation c : repo.getAllConsultations()) {
            if (c.getAppointment().getAppointmentId() == appointmentId) {
                return c;
            }
        }
        throw new NotFoundException("No consultation found for appointment #" + appointmentId);
    }

    /**
     * Retrieves the billing invoice for a specific consultation ID.
     */
    public Invoice getInvoiceByConsultationId(int consultationId) {
        return repo.getInvoiceByConsultationId(consultationId);
    }

    /**
     * Retrieves all invoices issued for a given patient.
     */
    public List<Invoice> getInvoicesForPatient(int patientId) {
        repo.getPatient(patientId); // validate patient exists
        List<Invoice> out = new ArrayList<>();
        for (Invoice inv : repo.getAllInvoices()) {
            if (inv.getConsultation().getAppointment().getPatient().getPatientId() == patientId) {
                out.add(inv);
            }
        }
        return out;
    }
}
