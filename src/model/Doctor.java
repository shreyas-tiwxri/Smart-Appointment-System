package model;

/**
 * Represents a medical professional managing clinical consults.
 * Extends Person (Inheritance & Polymorphism).
 */
public class Doctor extends Person {
    private String specialty;

    public Doctor(int doctorId, String name, String specialty) {
        super(doctorId, name);
        this.specialty = (specialty != null) ? specialty.trim() : "General Physician";
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

    @Override
    public String getRole() {
        return "DOCTOR";
    }

    @Override
    public String getSummaryDetails() {
        return "Dr. " + name + " [Specialty: " + specialty + ", License/ID: #" + id + "]";
    }

    @Override
    public String toString() {
        return "Dr. " + name + " (" + specialty + ")";
    }
}
