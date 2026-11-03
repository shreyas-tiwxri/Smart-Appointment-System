package model;

/**
 * Represents a registered patient seeking medical appointments.
 * Extends Person (Inheritance & Polymorphism).
 */
public class Patient extends Person {
    private String contact;

    public Patient(int patientId, String name, String contact) {
        super(patientId, name);
        this.contact = (contact != null) ? contact.trim() : "";
    }

    public int getPatientId() {
        return getId();
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = (contact != null) ? contact.trim() : "";
    }

    @Override
    public String getRole() {
        return "PATIENT";
    }

    @Override
    public String getSummaryDetails() {
        return "Patient #" + id + ": " + name + " (Contact: " + contact + ")";
    }

    @Override
    public String toString() {
        return "Patient#" + id + " " + name + " (" + contact + ")";
    }
}
