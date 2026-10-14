package barangaycarmenhealthmonitoringsystem;

import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {

    private JPanel contentPanel;
    private String username;
    private String role;

    public MainFrame(String username, String role) {

        this.username = username;
        this.role = role;

        setTitle("Barangay Carmen Health Monitoring System");
        setSize(1250, 750);
        setMinimumSize(new Dimension(950, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        createTopBar();
        createSidebar();

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        add(contentPanel, BorderLayout.CENTER);

        showDashboard();
    }

    // ==========================================
    // TOP BAR
    // ==========================================

    private void createTopBar() {

        JPanel top = new JPanel(new BorderLayout());
        top.setPreferredSize(new Dimension(0, 65));
        top.setBackground(UIUtils.GREEN);

        JPanel leftHeader = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        10,
                        8
                )
        );

        leftHeader.setBackground(UIUtils.GREEN);

        JLabel logoLabel = new JLabel();

        try {

            ImageIcon icon = new ImageIcon(
                    getClass().getResource(
                            "/barangaycarmenhealthmonitoringsystem/bg.png"
                    )
            );

            Image image = icon.getImage().getScaledInstance(
                    45,
                    45,
                    Image.SCALE_SMOOTH
            );

            logoLabel.setIcon(
                    new ImageIcon(image)
            );

        } catch (Exception e) {

            System.out.println(
                    "Unable to load bg.png"
            );
        }

        logoLabel.setPreferredSize(
                new Dimension(45, 45)
        );

        leftHeader.add(logoLabel);

        JLabel system = new JLabel(
                "BARANGAY CARMEN HEALTH MONITORING SYSTEM"
        );

        system.setForeground(Color.WHITE);

        system.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17
                )
        );

        leftHeader.add(system);

        JLabel user = new JLabel(
                "Welcome, "
                + username
                + "  "
        );

        user.setForeground(Color.WHITE);

        user.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        top.add(
                leftHeader,
                BorderLayout.WEST
        );

        top.add(
                user,
                BorderLayout.EAST
        );

        add(
                top,
                BorderLayout.NORTH
        );
    }

    // ==========================================
    // SIDEBAR
    // ==========================================

    private void createSidebar() {

        JPanel side = new JPanel();

        side.setBackground(UIUtils.DARK_GREEN);

        side.setPreferredSize(
                new Dimension(190, 0)
        );

        side.setLayout(
                new BoxLayout(
                        side,
                        BoxLayout.Y_AXIS
                )
        );

        side.add(
                Box.createVerticalStrut(20)
        );

        // DASHBOARD
        addMenuButton(
                side,
                "DASHBOARD",
                e -> showDashboard()
        );

        // PATIENTS
        addMenuButton(
                side,
                "PATIENTS",
                e -> showPatients()
        );

        // APPOINTMENTS
        addMenuButton(
                side,
                "APPOINTMENTS",
                e -> showAppointments()
        );

        // CONSULTATIONS
        addMenuButton(
                side,
                "CONSULTATIONS",
                e -> showConsultation()
        );

        // HEALTH RECORDS
        addMenuButton(
                side,
                "HEALTH RECORDS",
                e -> showHealthRecords()
        );

        // REPORTS - ADMIN ONLY
        if (role != null &&
                role.equalsIgnoreCase("Admin")) {

            addMenuButton(
                    side,
                    "REPORTS",
                    e -> showReports()
            );
        }

        // LOGOUT
        side.add(
                Box.createVerticalGlue()
        );

        addMenuButton(
                side,
                "LOGOUT",
                e -> logout()
        );

        side.add(
                Box.createVerticalStrut(15)
        );

        add(
                side,
                BorderLayout.WEST
        );
    }

    // ==========================================
    // MENU BUTTON
    // ==========================================

    private void addMenuButton(
            JPanel panel,
            String text,
            java.awt.event.ActionListener action) {

        JButton button = new JButton(text);

        button.setMaximumSize(
                new Dimension(190, 45)
        );

        button.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        button.setBackground(
                UIUtils.DARK_GREEN
        );

        button.setForeground(Color.WHITE);

        button.setBorderPainted(false);
        button.setFocusPainted(false);

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.addActionListener(action);

        panel.add(button);
    }

    // ==========================================
    // CONTENT
    // ==========================================

    private void setContent(JPanel panel) {

        contentPanel.removeAll();

        contentPanel.add(
                panel,
                BorderLayout.CENTER
        );

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ==========================================
    // DASHBOARD
    // ==========================================

    private void showDashboard() {

        setContent(
                new DashboardPanel(role)
        );
    }

    // ==========================================
    // PATIENTS
    // PUBLIC - USED BY DASHBOARD
    // ==========================================

    public void showPatients() {

        setContent(
                new PatientPanel(role)
        );
    }

    // ==========================================
    // APPOINTMENTS
    // PUBLIC - USED BY DASHBOARD
    // ==========================================

    public void showAppointments() {

        setContent(
                new AppointmentPanel(role)
        );
    }

    // ==========================================
    // APPOINTMENT - FROM CONSULTATION
    // IMPORTANT: PUBLIC
    // ==========================================

    public void showAppointment(
            String patientName) {

        setContent(
                new AppointmentPanel(
                        role,
                        patientName
                )
        );
    }

    // ==========================================
    // CONSULTATION
    // PUBLIC - USED BY DASHBOARD
    // ==========================================

    public void showConsultation() {

        setContent(
                new ConsultationPanel(role)
        );
    }

    // ==========================================
    // CONSULTATION - FROM PATIENT
    // IMPORTANT: PUBLIC
    // ==========================================

    public void showConsultation(
            String patientName) {

        setContent(
                new ConsultationPanel(
                        role,
                        patientName
                )
        );
    }

    // ==========================================
    // HEALTH RECORDS
    // ==========================================

    private void showHealthRecords() {

        setContent(
                new HealthRecordsPanel(role)
        );
    }

    // ==========================================
    // REPORTS
    // ADMIN ONLY
    // ==========================================

    private void showReports() {

        setContent(
                new ReportsPanel()
        );
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    private void logout() {

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        "Do you want to logout?",
                        "Logout",
                        JOptionPane.YES_NO_OPTION
                );

        if (result == JOptionPane.YES_OPTION) {

            dispose();

            new LoginFrame().setVisible(true);
        }
    }
}
