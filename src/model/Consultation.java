package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Doctor's record for one visit, linked to an approved appointment.
 * Generates formatted clinical documentation and digital prescription slips.
 */
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

    /**
     * Formats an official clinical prescription discharge slip for patient export.
     */
    public String generatePrescriptionSlip() {
        long signature = Math.abs((long) consultationId * 31337 + appointment.getAppointmentId() * 1013 + diagnosis.hashCode());
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                     SMART CLINIC MEDICAL PRESCRIPTION SLIP                     \n");
        sb.append("================================================================================\n");
        sb.append(String.format("Consultation Ref: #%-6d                  Date: %s\n", consultationId, appointment.getDateTime().format(FMT)));
        sb.append(String.format("Appointment Ref : #%-6d                  Type: %s\n", appointment.getAppointmentId(), appointment.isFollowUp() ? "FOLLOW-UP VISIT" : "PRIMARY CONSULTATION"));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("DOCTOR : Dr. %-30s Specialty: %s (ID: #%d)\n", 
                  appointment.getDoctor().getName(), appointment.getDoctor().getSpecialty(), appointment.getDoctor().getDoctorId()));
        sb.append(String.format("PATIENT: %-34s Contact  : %s (ID: #%d)\n", 
                  appointment.getPatient().getName(), appointment.getPatient().getContact(), appointment.getPatient().getPatientId()));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("CLINICAL DIAGNOSIS:\n");
        sb.append("  ").append(diagnosis).append("\n\n");
        sb.append("RX / PRESCRIBED MEDICATIONS & INSTRUCTIONS:\n");
        sb.append("  ").append(prescription.replace("\n", "\n  ")).append("\n\n");
        sb.append("NEXT FOLLOW-UP REVIEW:\n");
        sb.append("  ").append(nextVisitDateTime.format(FMT)).append(" [Automated Status: APPROVED]\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("Digital Verification Hash: [SEC-MED-%08X]\n", signature));
        sb.append("================================================================================\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Consultation #" + consultationId + " (Appointment #" + appointment.getAppointmentId() + ")\n"
            + "  Diagnosis    : " + diagnosis + "\n"
            + "  Prescription : " + prescription + "\n"
            + "  Next visit   : " + nextVisitDateTime.format(FMT);
    }
}
