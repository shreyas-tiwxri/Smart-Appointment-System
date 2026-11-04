package model;

/**
 * Strongly typed Enumeration representing appointment lifecycle states.
 * Enforces business domain integrity across scheduling, decisioning, and cancellation.
 */
public enum AppointmentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
