package model;

import exception.InvalidStateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * An initial appointment request OR an automatically created follow-up.
 * Encapsulates clinical scheduling state and transition guards.
 */
public class Appointment {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final int appointmentId;
    private final Patient patient;
    private final Doctor doctor;
    private final LocalDateTime dateTime;
    private AppointmentStatus status;
    private final boolean followUp;
    private String rejectionReason;

    public Appointment(int appointmentId, Patient patient, Doctor doctor,
                       LocalDateTime dateTime, AppointmentStatus status, boolean followUp) {
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = status;
        this.followUp = followUp;
        this.rejectionReason = "";
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

    public int getAppointmentId() { return appointmentId; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getDateTime() { return dateTime; }
    public AppointmentStatus getStatus() { return status; }
    public boolean isFollowUp() { return followUp; }
    public String getRejectionReason() { return rejectionReason; }

    @Override
    public String toString() {
        String base = "Appointment #" + appointmentId + " | " + patient.getName() + " with " + doctor
            + " | " + dateTime.format(FMT) + " | " + status + (followUp ? " | [FOLLOW-UP]" : "");
        if (status == AppointmentStatus.REJECTED && rejectionReason != null && !rejectionReason.isBlank()) {
            base += " (Reason: " + rejectionReason + ")";
        }
        return base;
    }
}
