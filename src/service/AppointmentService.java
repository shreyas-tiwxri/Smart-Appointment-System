package service;

import exception.InvalidInputException;
import model.*;
import repository.IDataRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing appointment lifecycles, doctor decisions, and scheduling queues.
 * Refactored by Member 2 (Shreyas Tiwari) to use IDataRepository and provide clinical analytics.
 */
public class AppointmentService {
    private final IDataRepository repo;

    public AppointmentService(IDataRepository repo) {
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
        return updateDecision(appointmentId, approve, null);
    }

    /** Overloaded doctor decision with clinical reason documentation on rejection. */
    public Appointment updateDecision(int appointmentId, boolean approve, String reason) {
        Appointment a = repo.getAppointment(appointmentId);
        a.updateDecision(approve ? AppointmentStatus.APPROVED : AppointmentStatus.REJECTED, reason);
        return a;
    }

    public List<Appointment> getPendingForDoctor(int doctorId) {
        repo.getDoctor(doctorId); // validate doctor exists
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : repo.getAllAppointments())
            if (a.getDoctor().getDoctorId() == doctorId && a.getStatus() == AppointmentStatus.PENDING)
                out.add(a);
        return out;
    }

    /** All appointments assigned to a doctor (Pending, Approved, Rejected, Follow-up). */
    public List<Appointment> getAllAppointmentsForDoctor(int doctorId) {
        repo.getDoctor(doctorId); // validate doctor exists
        List<Appointment> out = new ArrayList<>();
        for (Appointment a : repo.getAllAppointments())
            if (a.getDoctor().getDoctorId() == doctorId)
                out.add(a);
        return out;
    }

    /** Approved appointments of a doctor that have no consultation recorded yet. */
    public List<Appointment> getAwaitingConsultation(int doctorId) {
        repo.getDoctor(doctorId); // validate doctor exists
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

    /**
     * Aggregates real-time clinical workload statistics for a doctor.
     * Developed by Member 2 (Shreyas Tiwari).
     */
    public DoctorStats getDoctorStats(int doctorId) {
        Doctor d = repo.getDoctor(doctorId);
        int total = 0, pending = 0, approved = 0, rejected = 0, followUps = 0;
        for (Appointment a : repo.getAllAppointments()) {
            if (a.getDoctor().getDoctorId() == doctorId) {
                total++;
                if (a.getStatus() == AppointmentStatus.PENDING) pending++;
                else if (a.getStatus() == AppointmentStatus.APPROVED) approved++;
                else if (a.getStatus() == AppointmentStatus.REJECTED) rejected++;

                if (a.isFollowUp()) followUps++;
            }
        }

        int completedConsultations = 0;
        for (Consultation c : repo.getAllConsultations()) {
            if (c.getAppointment().getDoctor().getDoctorId() == doctorId) {
                completedConsultations++;
            }
        }

        return new DoctorStats(doctorId, d.getName(), total, pending, approved, rejected, completedConsultations, followUps);
    }
}
