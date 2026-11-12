package ui;

import model.*;
import repository.FilePersistenceService;
import repository.InMemoryRepository;
import service.AppointmentService;
import service.ConsultationService;
import service.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Desktop Clinical GUI Application built with 100% Core Java Swing.
 * Provides interactive visual workflows for Patient Bookings, Doctor Approvals,
 * Clinical Consultations, and Live Analytics without external UI dependencies.
 *
 * Developed for Course Evaluation by Mohit Srivastav (M1) & Shreyas Tiwari (M2).
 */
public final class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final transient InMemoryRepository repo;
    private final transient AppointmentService appointmentService;
    private final transient ConsultationService consultationService;
    private final transient FilePersistenceService persistenceService;
    private final transient NotificationService notificationService;

    // UI Components
    private JComboBox<String> patientCombo;
    private JComboBox<String> doctorCombo;
    private JTextField dateTimeField;
    private DefaultTableModel appointmentTableModel;
    private DefaultTableModel consultationTableModel;
    private JTextArea logArea;

    public MainWindow(InMemoryRepository repo,
                      AppointmentService appointmentService,
                      ConsultationService consultationService,
                      FilePersistenceService persistenceService,
                      NotificationService notificationService) {
        super("Doctor Smart Appointment & Clinical Workflow System (Core Java Swing)");
        this.repo = repo;
        this.appointmentService = appointmentService;
        this.consultationService = consultationService;
        this.persistenceService = persistenceService;
        this.notificationService = notificationService;

        initUI();
        refreshTables();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🏥 Doctor Smart Appointment & Clinical System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Core Java Desktop GUI | Mohit Srivastav (M1) & Shreyas Tiwari (M2)");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 220, 245));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Tabbed Pane for Multi-Role Features
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabbedPane.addTab("📅 Patient Appointment Portal", createBookingPanel());
        tabbedPane.addTab("🩺 Doctor Clinical Workspace", createDoctorWorkspacePanel());
        tabbedPane.addTab("📋 Consultations & Invoices", createConsultationsPanel());
        tabbedPane.addTab("📊 System Audit & Log", createLogPanel());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Form Panel
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBorder(BorderFactory.createTitledBorder("Request New Medical Appointment"));

        patientCombo = new JComboBox<>();
        doctorCombo = new JComboBox<>();
        populateCombos();

        LocalDateTime defaultTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        dateTimeField = new JTextField(defaultTime.format(FMT));

        JButton bookBtn = new JButton("Submit Appointment Request");
        bookBtn.setBackground(new Color(33, 115, 70));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookBtn.setFocusPainted(false);
        bookBtn.addActionListener(e -> handleBooking());

        form.add(new JLabel("Select Patient:"));
        form.add(patientCombo);
        form.add(new JLabel("Select Attending Doctor:"));
        form.add(doctorCombo);
        form.add(new JLabel("Appointment Slot (dd-MM-yyyy HH:mm):"));
        form.add(dateTimeField);
        form.add(new JLabel(""));
        form.add(bookBtn);

        // Appointment Table
        String[] cols = {"Appt ID", "Patient", "Doctor", "Slot Date/Time", "Status", "Type"};
        appointmentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable apptTable = new JTable(appointmentTableModel);
        apptTable.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(apptTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Live Clinical Schedule & Patient Appointments"));

        panel.add(form, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDoctorWorkspacePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.add(new JLabel("Review Pending / Manage Action on Selected Appointment:"));

        JButton approveBtn = new JButton("✔ Approve");
        approveBtn.setBackground(new Color(40, 167, 69));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.addActionListener(e -> handleDecision(true));

        JButton rejectBtn = new JButton("✖ Reject");
        rejectBtn.setBackground(new Color(220, 53, 69));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.addActionListener(e -> handleDecision(false));

        JButton cancelBtn = new JButton("🚫 Cancel Appointment");
        cancelBtn.setBackground(new Color(108, 117, 125));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(e -> handleCancel());

        topBar.add(approveBtn);
        topBar.add(rejectBtn);
        topBar.add(cancelBtn);

        JTable table = new JTable(appointmentTableModel);
        table.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Doctor Schedule & Queue (Select row above to act)"));

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConsultationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        String[] cols = {"Consult ID", "Appt #", "Patient", "Doctor", "Diagnosis", "Next Visit"};
        consultationTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(consultationTableModel);
        table.setRowHeight(24);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Completed Clinical Consultations & Prescriptions"));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("🔄 Refresh Data");
        refreshBtn.addActionListener(e -> refreshTables());
        bottom.add(refreshBtn);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 249, 250));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Audit Trail & Clinical Event Log"));

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void populateCombos() {
        patientCombo.removeAllItems();
        for (Patient p : repo.getAllPatients()) {
            patientCombo.addItem(p.getPatientId() + ": " + p.getName());
        }
        doctorCombo.removeAllItems();
        for (Doctor d : repo.getAllDoctors()) {
            doctorCombo.addItem(d.getDoctorId() + ": Dr. " + d.getName() + " (" + d.getSpecialty() + ")");
        }
    }

    private void handleBooking() {
        try {
            int patientId = Integer.parseInt(((String) patientCombo.getSelectedItem()).split(":")[0]);
            int doctorId = Integer.parseInt(((String) doctorCombo.getSelectedItem()).split(":")[0]);
            LocalDateTime dt = LocalDateTime.parse(dateTimeField.getText().trim(), FMT);

            Appointment a = appointmentService.requestAppointment(patientId, doctorId, dt);
            log("New Appointment Requested: #" + a.getAppointmentId() + " (" + a.getPatient().getName() + " with Dr. " + a.getDoctor().getName() + ")");
            JOptionPane.showMessageDialog(this,
                    "Appointment #" + a.getAppointmentId() + " requested successfully!\nStatus: PENDING",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTables();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Validation Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDecision(boolean approve) {
        String input = JOptionPane.showInputDialog(this, "Enter Appointment ID to " + (approve ? "APPROVE" : "REJECT") + ":");
        if (input == null || input.isBlank()) return;

        try {
            int id = Integer.parseInt(input.trim());
            String reason = null;
            if (!approve) {
                reason = JOptionPane.showInputDialog(this, "Enter Clinical Reason for Rejection:");
            }
            Appointment a = appointmentService.updateDecision(id, approve, reason);
            log("Doctor Decision on #" + id + ": " + a.getStatus() + (reason != null ? " (Reason: " + reason + ")" : ""));
            JOptionPane.showMessageDialog(this, "Appointment #" + id + " updated to " + a.getStatus(), "Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshTables();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Action Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancel() {
        String input = JOptionPane.showInputDialog(this, "Enter Appointment ID to CANCEL:");
        if (input == null || input.isBlank()) return;

        try {
            int id = Integer.parseInt(input.trim());
            String reason = JOptionPane.showInputDialog(this, "Enter Reason for Cancellation:");
            Appointment a = appointmentService.cancelAppointment(id, reason);
            log("Appointment #" + id + " Cancelled. Reason: " + a.getCancellationReason());
            JOptionPane.showMessageDialog(this, "Appointment #" + id + " has been CANCELLED.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            refreshTables();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cancellation Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public final void refreshTables() {
        appointmentTableModel.setRowCount(0);
        for (Appointment a : repo.getAllAppointments()) {
            appointmentTableModel.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getPatient().getName(),
                    "Dr. " + a.getDoctor().getName(),
                    a.getDateTime().format(FMT),
                    a.getStatus(),
                    a.isFollowUp() ? "Follow-up" : "Primary"
            });
        }

        consultationTableModel.setRowCount(0);
        for (Consultation c : repo.getAllConsultations()) {
            consultationTableModel.addRow(new Object[]{
                    c.getConsultationId(),
                    c.getAppointment().getAppointmentId(),
                    c.getAppointment().getPatient().getName(),
                    "Dr. " + c.getAppointment().getDoctor().getName(),
                    c.getDiagnosis(),
                    c.getNextVisitDateTime().format(FMT)
            });
        }
    }

    private void log(String msg) {
        logArea.append("[" + LocalDateTime.now().format(FMT) + "] " + msg + "\n");
    }

    /**
     * Standalone main launcher for desktop GUI mode.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            InMemoryRepository repo = new InMemoryRepository();
            NotificationService ns = new NotificationService();
            FilePersistenceService fps = new FilePersistenceService();
            AppointmentService as = new AppointmentService(repo, ns);
            ConsultationService cs = new ConsultationService(repo, ns);

            // Seed initial data
            repo.addPatient(new Patient(1, "Aman Sharma", "+91 98765 00001"));
            repo.addPatient(new Patient(2, "Riya Patel", "+91 98765 00002"));
            repo.addPatient(new Patient(3, "Kabir Mehta", "+91 98765 00003"));
            repo.addPatient(new Patient(4, "Sneha Reddy", "+91 98765 00004"));
            repo.addPatient(new Patient(5, "Vikram Malhotra", "+91 98765 00005"));

            repo.addDoctor(new Doctor(1, "Rajesh Sharma", "Senior General Physician", 600.0));
            repo.addDoctor(new Doctor(2, "Pooja Verma", "Specialist Cardiologist", 850.0));
            repo.addDoctor(new Doctor(3, "Arjun Kapoor", "Orthopedic Surgeon", 750.0));
            repo.addDoctor(new Doctor(4, "Ananya Iyer", "Consultant Neurologist", 900.0));

            MainWindow win = new MainWindow(repo, as, cs, fps, ns);
            win.setVisible(true);
        });
    }
}
