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
 * and automatic follow-up appointment generation.
 * Managed by Member 2 (Shreyas Tiwari).
 */
public class ConsultationService {
    private final IDataRepository repo;

    public ConsultationService(IDataRepository repo) {
        this.repo = repo;
    }

    /**
     * Doctor records diagnosis + prescription for an APPROVED appointment and gives the
     * next-visit date/time. A follow-up Appointment is created automatically.
     * Follow-up status is APPROVED because the doctor scheduled it directly during consultation.
     */
    public Consultation recordConsultation(int appointmentId, String diagnosis,
                                           String prescription, LocalDateTime nextVisit) {
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

        // auto-create follow-up appointment (same patient + doctor)
        Appointment followUp = new Appointment(repo.nextAppointmentId(), a.getPatient(), a.getDoctor(),
                                               nextVisit, AppointmentStatus.APPROVED, true);
        repo.saveAppointment(followUp);
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
}
