package model;

public class Doctor {
    private final int doctorId;
    private String name;
    private String specialty;

    public Doctor(int doctorId, String name, String specialty) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialty = specialty;
    }

    public int getDoctorId() { return doctorId; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }

    @Override
    public String toString() {
        return "Dr. " + name + " (" + specialty + ")";
    }
}
