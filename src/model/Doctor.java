package model;

/**
 * Represents a medical professional managing clinical consults.
 * Extends Person (Inheritance & Polymorphism).
 * Includes clinical fee structure for medical invoicing.
 */
public class Doctor extends Person {
    private String specialty;
    private double consultationFee;

    public Doctor(int doctorId, String name, String specialty) {
        this(doctorId, name, specialty, 500.0);
    }

    public Doctor(int doctorId, String name, String specialty, double consultationFee) {
        super(doctorId, name);
        this.specialty = (specialty != null) ? specialty.trim() : "General Physician";
        this.consultationFee = (consultationFee >= 0) ? consultationFee : 500.0;
    }

    public int getDoctorId() {
        return getId();
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = (specialty != null) ? specialty.trim() : "General Physician";
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = (consultationFee >= 0) ? consultationFee : 500.0;
    }

    @Override
    public String getRole() {
        return "DOCTOR";
    }

    @Override
    public String getSummaryDetails() {
        return String.format("Dr. %s [Specialty: %s, ID: #%d, Fee: ₹%.2f]", name, specialty, id, consultationFee);
    }

    @Override
    public String toString() {
        return String.format("Dr. %s (%s - Fee: ₹%.0f)", name, specialty, consultationFee);
    }
}
