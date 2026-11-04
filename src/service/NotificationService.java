package service;

import model.NotificationEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Event-driven notification dispatcher adhering to the Observer / Pub-Sub pattern.
 * Manages clinical audit alerts and real-time updates for Patients and Doctors.
 */
public class NotificationService {
    private final List<NotificationEvent> events = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger eventSequence = new AtomicInteger(1);

    /** Dispatches a new notification alert to target recipient. */
    public void notify(String recipientRole, int recipientId, String eventType, String message) {
        NotificationEvent event = new NotificationEvent(
                eventSequence.getAndIncrement(),
                recipientRole,
                recipientId,
                eventType,
                message
        );
        events.add(event);
    }

    /** Retrieves notifications for a specific patient. */
    public List<NotificationEvent> getNotificationsForPatient(int patientId) {
        List<NotificationEvent> result = new ArrayList<>();
        synchronized (events) {
            for (NotificationEvent e : events) {
                if ("ALL".equalsIgnoreCase(e.getRecipientRole()) ||
                   ("PATIENT".equalsIgnoreCase(e.getRecipientRole()) && e.getRecipientId() == patientId)) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    /** Retrieves notifications for a specific doctor. */
    public List<NotificationEvent> getNotificationsForDoctor(int doctorId) {
        List<NotificationEvent> result = new ArrayList<>();
        synchronized (events) {
            for (NotificationEvent e : events) {
                if ("ALL".equalsIgnoreCase(e.getRecipientRole()) ||
                   ("DOCTOR".equalsIgnoreCase(e.getRecipientRole()) && e.getRecipientId() == doctorId)) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    /** Returns all events for system administration and telemetry. */
    public List<NotificationEvent> getAllNotifications() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }
}

