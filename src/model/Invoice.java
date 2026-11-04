package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an official clinical billing invoice and receipt.
 * Generated automatically upon medical consultation completion.
 */
public class Invoice {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final String invoiceId;
    private final Consultation consultation;
    private final double baseFee;
    private final double taxRate;
    private final double taxAmount;
    private final double totalAmount;
    private final LocalDateTime invoiceDate;
    private final String paymentMode;

    public Invoice(int sequenceId, Consultation consultation, String paymentMode) {
        this.invoiceId = String.format("INV-2026-%04d", sequenceId);
        this.consultation = consultation;
        this.baseFee = consultation.getAppointment().getDoctor().getConsultationFee();
        this.taxRate = 0.05; // 5% Healthcare GST / Clinic Cess
        this.taxAmount = Math.round(this.baseFee * this.taxRate * 100.0) / 100.0;
        this.totalAmount = this.baseFee + this.taxAmount;
        this.invoiceDate = LocalDateTime.now();
        this.paymentMode = (paymentMode != null && !paymentMode.isBlank()) ? paymentMode : "UPI / Cash";
    }

    public String getInvoiceId() { return invoiceId; }
    public Consultation getConsultation() { return consultation; }
    public double getBaseFee() { return baseFee; }
    public double getTaxAmount() { return taxAmount; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getInvoiceDate() { return invoiceDate; }
    public String getPaymentMode() { return paymentMode; }

    /**
     * Formats an official patient billing invoice receipt.
     */
    public String generateReceipt() {
        Appointment a = consultation.getAppointment();
        Doctor d = a.getDoctor();
        Patient p = a.getPatient();

        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                       SMART CLINIC MEDICAL INVOICE & RECEIPT                   \n");
        sb.append("================================================================================\n");
        sb.append(String.format("Invoice Number  : %-25s Date: %s\n", invoiceId, invoiceDate.format(FMT)));
        sb.append(String.format("Consultation Ref: #%-24d Appointment Ref: #%d\n", 
                  consultation.getConsultationId(), a.getAppointmentId()));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("PATIENT NAME    : %-30s ID  : #%d\n", p.getName(), p.getPatientId()));
        sb.append(String.format("CONTACT NUMBER  : %-30s Type: %s\n", p.getContact(), a.isFollowUp() ? "Follow-Up Visit" : "Primary Visit"));
        sb.append(String.format("ATTENDING DOCTOR: Dr. %-26s Specialty: %s\n", d.getName(), d.getSpecialty()));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("ITEMIZED CLINICAL CHARGES:\n");
        sb.append(String.format("  1. Specialist Consultation Fee (Dr. %-15s) : ₹%8.2f\n", d.getName(), baseFee));
        sb.append(String.format("  2. Healthcare Service GST (5%%)                          : ₹%8.2f\n", taxAmount));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("TOTAL AMOUNT PAID                                        : ₹%8.2f\n", totalAmount));
        sb.append(String.format("PAYMENT STATUS : [PAID - %s] (Confirmed)\n", paymentMode));
        sb.append("================================================================================\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Invoice %s | Patient: %s | Total: ₹%.2f | Status: PAID",
                invoiceId, consultation.getAppointment().getPatient().getName(), totalAmount);
    }
}

