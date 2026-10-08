package repository;

import exception.NotFoundException;
import model.*;
import java.util.*;

/** In-memory storage using Core Java collections (no database needed). */
public class InMemoryRepository {
    private final Map<Integer, Patient> patients = new HashMap<>();
    private final Map<Integer, Doctor> doctors = new HashMap<>();
    private final Map<Integer, Appointment> appointments = new LinkedHashMap<>();
    private final List<Consultation> consultations = new ArrayList<>();

    private int nextAppointmentId = 1;
    private int nextConsultationId = 1;

    // ---- patients / doctors ----
    public void addPatient(Patient p) { patients.put(p.getPatientId(), p); }
    public void addDoctor(Doctor d) { doctors.put(d.getDoctorId(), d); }
    public Collection<Patient> getAllPatients() { return patients.values(); }
    public Collection<Doctor> getAllDoctors() { return doctors.values(); }

    public Patient getPatient(int id) {
        Patient p = patients.get(id);
        if (p == null) throw new NotFoundException("No patient with id " + id);
        return p;
    }

    public Doctor getDoctor(int id) {
        Doctor d = doctors.get(id);
        if (d == null) throw new NotFoundException("No doctor with id " + id);
        return d;
    }

    // ---- appointments ----
    public int nextAppointmentId() { return nextAppointmentId++; }
    public void saveAppointment(Appointment a) { appointments.put(a.getAppointmentId(), a); }

    public Appointment getAppointment(int id) {
        Appointment a = appointments.get(id);
        if (a == null) throw new NotFoundException("No appointment with id " + id);
        return a;
    }

    public List<Appointment> getAllAppointments() { return new ArrayList<>(appointments.values()); }

    // ---- consultations ----
    public int nextConsultationId() { return nextConsultationId++; }
    public void saveConsultation(Consultation c) { consultations.add(c); }
    public List<Consultation> getAllConsultations() { return new ArrayList<>(consultations); }
}
