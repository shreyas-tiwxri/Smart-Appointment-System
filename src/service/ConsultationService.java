package service;

import exception.InvalidInputException;
import exception.InvalidStateException;
import model.*;
import repository.InMemoryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsultationService {
    private final InMemoryRepository repo;

    public ConsultationService(InMemoryRepository repo) {
        this.repo = repo;
    }

    /**
     * Doctor records diagnosis + prescription for an APPROVED appointment and gives the
     * next-visit date/time. A follow-up Appointment is created automatically.
     * Follow-up status is APPROVED because the doctor scheduled it himself.
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
        List<Consultation> out = new ArrayList<>();
        for (Consultation c : repo.getAllConsultations())
            if (c.getAppointment().getPatient().getPatientId() == patientId) out.add(c);
        return out;
    }
}
