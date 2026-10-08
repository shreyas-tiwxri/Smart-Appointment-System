package service;

import exception.InvalidInputException;
import model.*;
import repository.InMemoryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private final InMemoryRepository repo;

    public AppointmentService(InMemoryRepository repo) {
        this.repo = repo;
    }

    /** Patient creates a request; starts as PENDING. */
    public Appointment requestAppointment(int patientId, int doctorId, LocalDateTime dateTime) {
        if (dateTime == null) throw new InvalidInputException("Appointment date and time are required.");
        if (dateTime.isBefore(LocalDateTime.now()))
            throw new InvalidInputException("Appointment date/time cannot be in the past.");
        Patient p = repo.getPatient(patientId);
        Doctor d = repo.getDoctor(doctorId);
        Appointment a = new Appointment(repo.nextAppointmentId(), p, d, dateTime,
                                        AppointmentStatus.PENDING, false);
        repo.saveAppointment(a);
        return a;
    }

    /** Doctor approves or rejects a pending request. */
    public Appointment updateDecision(int appointmentId, boolean approve) {
        Appointment a = repo.getAppointment(appointmentId);
        a.updateDecision(approve ? AppointmentStatus.APPROVED : AppointmentStatus.REJECTED);
        return a;
    }

    public List<Appointment> getPendingForDoctor(int doctorId) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : repo.getAllAppointments())
            if (a.getDoctor().getDoctorId() == doctorId && a.getStatus() == AppointmentStatus.PENDING)
                out.add(a);
        return out;
    }

    /** Approved appointments of a doctor that have no consultation recorded yet. */
    public List<Appointment> getAwaitingConsultation(int doctorId) {
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : repo.getAllAppointments()) {
            if (a.getDoctor().getDoctorId() != doctorId) continue;
            if (a.getStatus() != AppointmentStatus.APPROVED) continue;
            boolean done = false;
            for (Consultation c : repo.getAllConsultations())
                if (c.getAppointment().getAppointmentId() == a.getAppointmentId()) done = true;
            if (!done) out.add(a);
        }
        return out;
    }

    public List<Appointment> getAppointmentsForPatient(int patientId) {
        repo.getPatient(patientId); // validates patient exists
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : repo.getAllAppointments())
            if (a.getPatient().getPatientId() == patientId) out.add(a);
        return out;
    }
}
