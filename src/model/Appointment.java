package model;

import exception.InvalidInputException;
import exception.InvalidStateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An initial appointment request OR an automatically created follow-up.
 * Encapsulates clinical scheduling state, cancellation guards, and transition logic.
 */
public class Appointment {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final int appointmentId;
    private final Patient patient;
    private final Doctor doctor;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private final boolean followUp;
    private String rejectionReason;
    private String cancellationReason;

    public Appointment(int appointmentId, Patient patient, Doctor doctor,
                       LocalDateTime dateTime, AppointmentStatus status, boolean followUp) {
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = status;
        this.followUp = followUp;
        this.rejectionReason = "";
        this.cancellationReason = "";
    }

    /** Only a PENDING appointment can be approved/rejected. */
    public void updateDecision(AppointmentStatus newStatus) {
        updateDecision(newStatus, null);
    }

    /** Overloaded decision update allowing clinical reason documentation upon rejection. */
    public void updateDecision(AppointmentStatus newStatus, String reason) {
        if (status != AppointmentStatus.PENDING) {
            throw new InvalidStateException(
                "Appointment #" + appointmentId + " is already " + status + ", cannot change it.");
        }
        if (newStatus != AppointmentStatus.APPROVED && newStatus != AppointmentStatus.REJECTED) {
            throw new InvalidStateException("Decision must be APPROVED or REJECTED.");
        }
        this.status = newStatus;
        if (newStatus == AppointmentStatus.REJECTED && reason != null && !reason.isBlank()) {
            this.rejectionReason = reason.trim();
        }
    }

    /** Cancels an appointment if it is currently PENDING or APPROVED. */
    public void cancel(String reason) {
        if (status == AppointmentStatus.CANCELLED) {
            throw new InvalidStateException("Appointment #" + appointmentId + " is already CANCELLED.");
        }
        if (status == AppointmentStatus.REJECTED) {
            throw new InvalidStateException("Cannot cancel an appointment that has already been REJECTED.");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = (reason != null && !reason.isBlank()) ? reason.trim() : "Cancelled by patient";
    }

    /** Reschedules a pending or approved appointment to a new validated future time. */
    public void reschedule(LocalDateTime newDateTime) {
        if (status != AppointmentStatus.PENDING && status != AppointmentStatus.APPROVED) {
            throw new InvalidStateException("Only PENDING or APPROVED appointments can be rescheduled.");
        }
        if (newDateTime == null || newDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidInputException("New appointment time cannot be in the past.");
        }
        this.dateTime = newDateTime;
    }

    public int getAppointmentId() { return appointmentId; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getDateTime() { return dateTime; }
    public AppointmentStatus getStatus() { return status; }
    public boolean isFollowUp() { return followUp; }
    public String getRejectionReason() { return rejectionReason; }
    public String getCancellationReason() { return cancellationReason; }

    @Override
    public String toString() {
        String base = "Appointment #" + appointmentId + " | " + patient.getName() + " with " + doctor
            + " | " + dateTime.format(FMT) + " | " + status + (followUp ? " | [FOLLOW-UP]" : "");
        if (status == AppointmentStatus.REJECTED && rejectionReason != null && !rejectionReason.isBlank()) {
            base += " (Rejection Reason: " + rejectionReason + ")";
        }
        if (status == AppointmentStatus.CANCELLED && cancellationReason != null && !cancellationReason.isBlank()) {
            base += " (Cancellation Reason: " + cancellationReason + ")";
        }
        return base;
    }
}
