package repository;

import exception.NotFoundException;
import model.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe In-Memory storage repository implementing IDataRepository.
 * Uses ConcurrentHashMap for thread-safe lookups and AtomicInteger for lock-free sequence IDs.
 */
public class InMemoryRepository implements IDataRepository {
    private final Map<Integer, Patient> patients = new ConcurrentHashMap<>();
    private final Map<Integer, Doctor> doctors = new ConcurrentHashMap<>();
    // LinkedHashMap wrapped in Collections.synchronizedMap to maintain chronological insertion order safely
    private final Map<Integer, Appointment> appointments = Collections.synchronizedMap(new LinkedHashMap<>());
    private final List<Consultation> consultations = Collections.synchronizedList(new ArrayList<>());

    private final AtomicInteger nextAppointmentIdSequence = new AtomicInteger(1);
    private final AtomicInteger nextConsultationIdSequence = new AtomicInteger(1);

    // ---- patients / doctors ----
    @Override
    public void addPatient(Patient p) { 
        patients.put(p.getPatientId(), p); 
    }

    @Override
    public void addDoctor(Doctor d) { 
        doctors.put(d.getDoctorId(), d); 
    }

    @Override
    public Collection<Patient> getAllPatients() { 
        return new ArrayList<>(patients.values()); 
    }

    @Override
    public Collection<Doctor> getAllDoctors() { 
        return new ArrayList<>(doctors.values()); 
    }

    @Override
    public Patient getPatient(int id) {
        Patient p = patients.get(id);
        if (p == null) throw new NotFoundException("No patient with id " + id);
        return p;
    }

    @Override
    public Doctor getDoctor(int id) {
        Doctor d = doctors.get(id);
        if (d == null) throw new NotFoundException("No doctor with id " + id);
        return d;
    }

    // ---- appointments ----
    @Override
    public int nextAppointmentId() { 
        return nextAppointmentIdSequence.getAndIncrement(); 
    }

    @Override
    public void saveAppointment(Appointment a) { 
        appointments.put(a.getAppointmentId(), a); 
    }

    @Override
    public Appointment getAppointment(int id) {
        Appointment a = appointments.get(id);
        if (a == null) throw new NotFoundException("No appointment with id " + id);
        return a;
    }

    @Override
    public List<Appointment> getAllAppointments() { 
        synchronized (appointments) {
            return new ArrayList<>(appointments.values()); 
        }
    }

    // ---- consultations ----
    @Override
    public int nextConsultationId() { 
        return nextConsultationIdSequence.getAndIncrement(); 
    }

    @Override
    public void saveConsultation(Consultation c) { 
        consultations.add(c); 
    }

    @Override
    public List<Consultation> getAllConsultations() { 
        synchronized (consultations) {
            return new ArrayList<>(consultations); 
        }
    }
}
