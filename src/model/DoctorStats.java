package model;

/**
 * Data Transfer Object (DTO) holding aggregated clinical statistics for a Doctor.
 * Part of Member 2 (Shreyas)'s Doctor Analytics module.
 */
public class DoctorStats {
    private final int doctorId;
    private final String doctorName;
    private final int totalAssigned;
    private final int pendingCount;
    private final int approvedCount;
    private final int rejectedCount;
    private final int completedConsultations;
    private final int scheduledFollowUps;

    public DoctorStats(int doctorId, String doctorName, int totalAssigned, int pendingCount,
                       int approvedCount, int rejectedCount, int completedConsultations, int scheduledFollowUps) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.totalAssigned = totalAssigned;
        this.pendingCount = pendingCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
        this.completedConsultations = completedConsultations;
        this.scheduledFollowUps = scheduledFollowUps;
    }

    public int getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public int getTotalAssigned() { return totalAssigned; }
    public int getPendingCount() { return pendingCount; }
    public int getApprovedCount() { return approvedCount; }
    public int getRejectedCount() { return rejectedCount; }
    public int getCompletedConsultations() { return completedConsultations; }
    public int getScheduledFollowUps() { return scheduledFollowUps; }

    public double getApprovalRatePercent() {
        int decided = approvedCount + rejectedCount;
        if (decided == 0) return 0.0;
        return (approvedCount * 100.0) / decided;
    }

    public double getConsultationCompletionRatePercent() {
        if (approvedCount == 0) return 0.0;
        return (completedConsultations * 100.0) / approvedCount;
    }

    @Override
    public String toString() {
        return String.format(
            """
            ================================================================================
                           CLINICAL PERFORMANCE & APPOINTMENT ANALYTICS                     
            ================================================================================
            Practitioner       : Dr. %s (ID: #%d)
            Total Appointments : %d
            --------------------------------------------------------------------------------
            - Pending Reviews  : %d
            - Approved Visits  : %d
            - Rejected Visits  : %d
            - Approval Rate    : %.1f%%
            --------------------------------------------------------------------------------
            - Completed Consultations : %d (%.1f%% of approved visits)
            - Auto Follow-Ups Scheduled: %d
            ================================================================================
            """,
            doctorName, doctorId, totalAssigned, pendingCount, approvedCount, rejectedCount,
            getApprovalRatePercent(), completedConsultations, getConsultationCompletionRatePercent(),
            scheduledFollowUps
        );
    }
}

