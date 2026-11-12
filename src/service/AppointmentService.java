package service;

import exception.InvalidInputException;
import exception.InvalidStateException;
import model.*;
import repository.IDataRepository;
import util.Config;
import util.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing appointment lifecycles, doctor decisions, slot availability,
 * and clinical schedule collision prevention.
 * Engineered by Member 2 (Shreyas Tiwari).
 */
public class AppointmentService {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern(Config.DATETIME_FORMAT);
    private final IDataRepository repo;
    private final NotificationService notificationService;

    public AppointmentService(IDataRepository repo) {
        this(repo, new NotificationService());
    }

    public AppointmentService(IDataRepository repo, NotificationService notificationService) {
        this.repo = repo;
        this.notificationService = notificationService;
    }

    /**
     * Patient creates an appointment request with slot collision and operating hours validation.
     */
    public Appointment requestAppointment(int patientId, int doctorId, LocalDateTime dateTime) {
        if (dateTime == null) throw new InvalidInputException("Appointment date and time are required.");
        if (dateTime.isBefore(LocalDateTime.now()))
            throw new InvalidInputException("Appointment date/time cannot be in the past.");

        Patient p = repo.getPatient(patientId);
        Doctor d = repo.getDoctor(doctorId);

        // Guard against double-booking within a 30-minute consultation window
        checkSlotAvailability(doctorId, dateTime, -1);

        Appointment a = new Appointment(repo.nextAppointmentId(), p, d, dateTime,
                                        AppointmentStatus.PENDING, false);
        repo.saveAppointment(a);

        // Dispatch alert to doctor
        if (notificationService != null) {
            notificationService.notify("DOCTOR", doctorId, "NEW_REQUEST",
                    "New appointment request #" + a.getAppointmentId() + " from " + p.getName() + " for " + dateTime.format(FMT));
        }

        return a;
    }

    /**
     * Checks whether a doctor has an overlapping PENDING or APPROVED appointment within 30 minutes.
     */
    public void checkSlotAvailability(int doctorId, LocalDateTime dateTime, int excludeApptId) {
        Doctor d = repo.getDoctor(doctorId);
        for (Appointment existing : repo.getAllAppointments()) {
            if (existing.getAppointmentId() == excludeApptId) continue;
            if (existing.getDoctor().getDoctorId() != doctorId) continue;
            if (existing.getStatus() != AppointmentStatus.PENDING && existing.getStatus() != AppointmentStatus.APPROVED) continue;

            long minuteDiff = Math.abs(Duration.between(existing.getDateTime(), dateTime).toMinutes());
            if (minuteDiff < Config.SLOT_BUFFER_MINUTES) {
                throw new InvalidStateException(
                    String.format("Slot Conflict: Dr. %s already has an active appointment (#%d at %s). A 30-minute consultation buffer is required.",
                            d.getName(), existing.getAppointmentId(), existing.getDateTime().format(FMT))
                );
            }
        }
    }

    /** Doctor approves or rejects a pending request. */
    public Appointment updateDecision(int appointmentId, boolean approve) {
        return updateDecision(appointmentId, approve, null);
    }

    /** Overloaded doctor decision with clinical reason documentation on rejection. */
    public Appointment updateDecision(int appointmentId, boolean approve, String reason) {
        Appointment a = repo.getAppointment(appointmentId);
        a.updateDecision(approve ? AppointmentStatus.APPROVED : AppointmentStatus.REJECTED, reason);

        if (notificationService != null) {
            String decisionType = approve ? "APPROVED" : "REJECTED";
            String msg = "Your appointment #" + appointmentId + " with Dr. " + a.getDoctor().getName() + " has been " + decisionType + ".";
            if (!approve && reason != null && !reason.isBlank()) {
                msg += " Reason: " + reason;
            }
            notificationService.notify("PATIENT", a.getPatient().getPatientId(), "DECISION_" + decisionType, msg);
        }

        return a;
    }

    /**
     * Cancels an appointment if it is currently PENDING or APPROVED.
     */
    public Appointment cancelAppointment(int appointmentId, String reason) {
        Appointment a = repo.getAppointment(appointmentId);
        a.cancel(reason);

        if (notificationService != null) {
            notificationService.notify("DOCTOR", a.getDoctor().getDoctorId(), "APPOINTMENT_CANCELLED",
                    "Appointment #" + appointmentId + " was CANCELLED by patient " + a.getPatient().getName() + ". Reason: " + a.getCancellationReason());
        }

        return a;
    }

    /**
     * Reschedules an existing appointment to a new validated time slot.
     */
    public Appointment rescheduleAppointment(int appointmentId, LocalDateTime newDateTime) {
        Appointment a = repo.getAppointment(appointmentId);
        checkSlotAvailability(a.getDoctor().getDoctorId(), newDateTime, appointmentId);
        a.reschedule(newDateTime);

        if (notificationService != null) {
            notificationService.notify("DOCTOR", a.getDoctor().getDoctorId(), "APPOINTMENT_RESCHEDULED",
                    "Appointment #" + appointmentId + " rescheduled to " + newDateTime.format(FMT));
        }

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

    /** All appointments assigned to a doctor (Pending, Approved, Rejected, Cancelled, Follow-up). */
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

    public NotificationService getNotificationService() {
        return notificationService;
    }
}
