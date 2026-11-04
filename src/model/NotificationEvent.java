package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Event message dispatched to actors upon appointment lifecycle state transitions.
 * Demonstrates the Observer Pattern / Event Notification architecture.
 */
public class NotificationEvent {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final int eventId;
    private final LocalDateTime timestamp;
    private final String recipientRole;
    private final int recipientId;
    private final String eventType;
    private final String message;

    public NotificationEvent(int eventId, String recipientRole, int recipientId, String eventType, String message) {
        this.eventId = eventId;
        this.timestamp = LocalDateTime.now();
        this.recipientRole = recipientRole;
        this.recipientId = recipientId;
        this.eventType = eventType;
        this.message = message;
    }

    public int getEventId() { return eventId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getRecipientRole() { return recipientRole; }
    public int getRecipientId() { return recipientId; }
    public String getEventType() { return eventType; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return String.format("[%s] ALERT (%s): %s", timestamp.format(FMT), eventType, message);
    }
}

