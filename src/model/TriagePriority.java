package model;

/**
 * Triage severity levels for clinical appointment prioritization.
 * Uses numerical rank for PriorityQueue heap ordering.
 */
public enum TriagePriority {
    EMERGENCY(1, "Emergency / Critical Care"),
    URGENT(2, "Urgent Review"),
    ROUTINE(3, "Routine Consultation");

    private final int rank;
    private final String label;

    TriagePriority(int rank, String label) {
        this.rank = rank;
        this.label = label;
    }

    public int getRank() {
        return rank;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
