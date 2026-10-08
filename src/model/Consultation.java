package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Doctor's record for one visit, linked to an appointment. */
public class Consultation {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final int consultationId;
    private final Appointment appointment;
    private final String diagnosis;
    private final String prescription;
    private final LocalDateTime nextVisitDateTime;

    public Consultation(int consultationId, Appointment appointment, String diagnosis,
                        String prescription, LocalDateTime nextVisitDateTime) {
        this.consultationId = consultationId;
        this.appointment = appointment;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.nextVisitDateTime = nextVisitDateTime;
    }

    public int getConsultationId() { return consultationId; }
    public Appointment getAppointment() { return appointment; }
    public String getDiagnosis() { return diagnosis; }
    public String getPrescription() { return prescription; }
    public LocalDateTime getNextVisitDateTime() { return nextVisitDateTime; }

    @Override
    public String toString() {
        return "Consultation #" + consultationId + " (Appointment #" + appointment.getAppointmentId() + ")\n"
            + "  Diagnosis    : " + diagnosis + "\n"
            + "  Prescription : " + prescription + "\n"
            + "  Next visit   : " + nextVisitDateTime.format(FMT);
    }
}
