package model;

public class Patient {
    private final int patientId;
    private String name;
    private String contact;

    public Patient(int patientId, String name, String contact) {
        this.patientId = patientId;
        this.name = name;
        this.contact = contact;
    }

    public int getPatientId() { return patientId; }
    public String getName() { return name; }
    public String getContact() { return contact; }

    @Override
    public String toString() {
        return "Patient#" + patientId + " " + name + " (" + contact + ")";
    }
}
