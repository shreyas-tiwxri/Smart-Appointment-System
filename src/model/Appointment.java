package model;

import exception.InvalidStateException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** An initial appointment request OR an automatically created follow-up. */
public class Appointment {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final int appointmentId;
    private final Patient patient;
    private final Doctor doctor;
    private final LocalDateTime dateTime;
    private AppointmentStatus status;
    private final boolean followUp;

    public Appointment(int appointmentId, Patient patient, Doctor doctor,
                       LocalDateTime dateTime, AppointmentStatus status, boolean followUp) {
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = status;
        this.followUp = followUp;
    }

    /** Only a PENDING appointment can be approved/rejected. */
    public void updateDecision(AppointmentStatus newStatus) {
        if (status != AppointmentStatus.PENDING) {
            throw new InvalidStateException(
                "Appointment #" + appointmentId + " is already " + status + ", cannot change it.");
        }
        if (newStatus != AppointmentStatus.APPROVED && newStatus != AppointmentStatus.REJECTED) {
            throw new InvalidStateException("Decision must be APPROVED or REJECTED.");
        }
        this.status = newStatus;
    }

    public int getAppointmentId() { return appointmentId; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getDateTime() { return dateTime; }
    public AppointmentStatus getStatus() { return status; }
    public boolean isFollowUp() { return followUp; }

    @Override
    public String toString() {
        return "Appointment #" + appointmentId + " | " + patient.getName() + " with " + doctor
            + " | " + dateTime.format(FMT) + " | " + status + (followUp ? " | FOLLOW-UP" : "");
    }
}
