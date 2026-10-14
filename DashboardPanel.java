
package barangaycarmenhealthmonitoringsystem;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {

    // =====================================================
    // COLORS
    // =====================================================

    private final Color GREEN_BG = new Color(232, 247, 238);
    private final Color GREEN_ACCENT = new Color(45, 145, 45);

    private final Color BLUE_BG = new Color(232, 240, 252);
    private final Color BLUE_ACCENT = new Color(55, 90, 200);

    private final Color YELLOW_BG = new Color(255, 248, 220);
    private final Color YELLOW_ACCENT = new Color(210, 155, 25);

    private final Color PURPLE_BG = new Color(242, 235, 250);
    private final Color PURPLE_ACCENT = new Color(125, 75, 170);

    private static final Color DARK_GREEN = new Color(0, 105, 80);

    // =====================================================
    // LOWER LEFT TABLE AREA
    // System Overview stays on the right.
    // =====================================================

    private JPanel leftTableCards;
    private CardLayout leftTableLayout;
    private TitledBorder leftTableBorder;

    public DashboardPanel(String role) {

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // =====================================================
        // TITLE
        // =====================================================

        JLabel title = UIUtils.title("Dashboard - " + role);
        title.setForeground(DARK_GREEN);
        add(title, BorderLayout.NORTH);

        // =====================================================
        // CENTER
        // =====================================================

        JPanel center = new JPanel();
        center.setBackground(Color.WHITE);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // =====================================================
        // SUMMARY CARDS
        // =====================================================

        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 15));
        cards.setBackground(Color.WHITE);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        int totalPatients = getCount(
                "SELECT COUNT(*) FROM patients " +
                "WHERE COALESCE(is_archived, 0) = 0"
        );

        int totalAppointments = getCount(
                "SELECT COUNT(*) FROM appointments a " +
                "WHERE COALESCE(a.is_archived, 0) = 0 " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM patients p " +
                "WHERE p.name = a.patient_name " +
                "AND COALESCE(p.is_archived, 0) = 1)"
        );

        int totalConsultations = getCount(
                "SELECT COUNT(*) FROM consultations c " +
                "WHERE COALESCE(c.is_archived, 0) = 0 " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM patients p " +
                "WHERE p.name = c.patient_name " +
                "AND COALESCE(p.is_archived, 0) = 1)"
        );

        int activeMonitoring = getCount(
                "SELECT COUNT(*) FROM patients " +
                "WHERE status = 'Active' " +
                "AND COALESCE(is_archived, 0) = 0"
        );

        cards.add(createCard(
                "TOTAL PATIENTS",
                String.valueOf(totalPatients),
                GREEN_BG,
                GREEN_ACCENT,
                () -> showDashboardTable("patients")
        ));

        cards.add(createCard(
                "TODAY'S APPOINTMENTS",
                String.valueOf(totalAppointments),
                BLUE_BG,
                BLUE_ACCENT,
                () -> showDashboardTable("appointments")
        ));

        cards.add(createCard(
                "TODAY'S CONSULTATIONS",
                String.valueOf(totalConsultations),
                YELLOW_BG,
                YELLOW_ACCENT,
                () -> showDashboardTable("consultations")
        ));

        cards.add(createCard(
                "ACTIVE MONITORING",
                String.valueOf(activeMonitoring),
                PURPLE_BG,
                PURPLE_ACCENT,
                () -> showDashboardTable("active")
        ));

        center.add(cards);
        center.add(Box.createVerticalStrut(20));

        // =====================================================
        // LOWER AREA
        // LEFT: TABLE VIEWS
        // RIGHT: SYSTEM OVERVIEW (UNCHANGED POSITION)
        // =====================================================

        JPanel tables = new JPanel(new GridLayout(1, 2, 15, 15));
        tables.setBackground(Color.WHITE);
        tables.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTableLayout = new CardLayout();
        leftTableCards = new JPanel(leftTableLayout);
        leftTableCards.setBackground(Color.WHITE);

        // Default table
        leftTableCards.add(
                createConsultationTable(),
                "recent"
        );

        // Click Total Patients
        leftTableCards.add(
                createPatientsTable(false),
                "patients"
        );

        // Click Appointments
        leftTableCards.add(
                createAppointmentsTable(),
                "appointments"
        );

        // Click Consultations
        leftTableCards.add(
                createAllConsultationsTable(),
                "consultations"
        );

        // Click Active Monitoring
        leftTableCards.add(
                createPatientsTable(true),
                "active"
        );

        tables.add(leftTableCards);

        // System Overview stays on the right at all times.
        tables.add(createOverview());

        center.add(tables);
        add(center, BorderLayout.CENTER);
    }

    // =====================================================
    // SWITCH TABLE INSIDE DASHBOARD ONLY
    // =====================================================

    private void showDashboardTable(String tableName) {

        leftTableLayout.show(leftTableCards, tableName);
        leftTableCards.revalidate();
        leftTableCards.repaint();
    }

    // =====================================================
    // GET COUNT FROM DATABASE
    // =====================================================

    private int getCount(String sql) {

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            if (result.next()) {
                return result.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Dashboard SQL Error:");
            e.printStackTrace();
        }

        return 0;
    }

    // =====================================================
    // CLICKABLE SUMMARY CARD
    // =====================================================

    private JPanel createCard(
            String title,
            String value,
            Color background,
            Color accentColor,
            Runnable action) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(background);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 6, 0, 0, accentColor
                        ),
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(accentColor, 2),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)
                        )
                )
        );

        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(new Color(70, 70, 70));
        t.setOpaque(false);
        t.setBorder(BorderFactory.createEmptyBorder(8, 8, 3, 8));

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 30));
        v.setForeground(accentColor);
        v.setOpaque(false);
        v.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        panel.add(t, BorderLayout.NORTH);
        panel.add(v, BorderLayout.CENTER);

        Color hoverBackground = new Color(
                (background.getRed() * 3 + accentColor.getRed()) / 4,
                (background.getGreen() * 3 + accentColor.getGreen()) / 4,
                (background.getBlue() * 3 + accentColor.getBlue()) / 4
        );

        panel.setToolTipText("Click to view " + title.toLowerCase());

        java.awt.event.MouseAdapter cardMouse =
                new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.run();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(hoverBackground);
                panel.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(background);
                panel.repaint();
            }
        };

        panel.addMouseListener(cardMouse);
        t.addMouseListener(cardMouse);
        v.addMouseListener(cardMouse);

        t.setCursor(new Cursor(Cursor.HAND_CURSOR));
        v.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return panel;
    }

    // =====================================================
    // RECENT CONSULTATIONS
    // =====================================================

    private JPanel createConsultationTable() {

        String sql =
                "SELECT c.patient_name, c.date, c.reason, c.status " +
                "FROM consultations c " +
                "INNER JOIN patients p ON p.name = c.patient_name " +
                "WHERE COALESCE(c.is_archived, 0) = 0 " +
                "AND COALESCE(p.is_archived, 0) = 0 " +
                "ORDER BY c.id DESC LIMIT 10";

        return createReadOnlyTable(
                "RECENT CONSULTATIONS",
                new String[]{"Patient Name", "Date", "Reason", "Status"},
                sql,
                new String[]{"patient_name", "date", "reason", "status"},
                3
        );
    }

    // =====================================================
    // ALL PATIENTS
    // =====================================================

    private JPanel createPatientsTable(boolean activeOnly) {

        String title = activeOnly
                ? "ACTIVE MONITORING"
                : "ALL PATIENTS";

        String sql =
                "SELECT patient_id, name, age, sex, contact, status " +
                "FROM patients " +
                "WHERE COALESCE(is_archived, 0) = 0 " +
                (activeOnly ? "AND status = 'Active' " : "") +
                "ORDER BY id DESC";

        return createReadOnlyTable(
                title,
                new String[]{
                    "Patient ID", "Patient Name", "Age",
                    "Sex", "Contact", "Status"
                },
                sql,
                new String[]{
                    "patient_id", "name", "age",
                    "sex", "contact", "status"
                },
                5
        );
    }

    // =====================================================
    // ALL APPOINTMENTS
    // =====================================================

    private JPanel createAppointmentsTable() {

        String sql =
                "SELECT a.time, a.patient_name, a.purpose, a.status " +
                "FROM appointments a " +
                "WHERE COALESCE(a.is_archived, 0) = 0 " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM patients p " +
                "WHERE p.name = a.patient_name " +
                "AND COALESCE(p.is_archived, 0) = 1) " +
                "ORDER BY a.id DESC";

        return createReadOnlyTable(
                "ALL APPOINTMENTS",
                new String[]{"Time", "Patient Name", "Purpose", "Status"},
                sql,
                new String[]{"time", "patient_name", "purpose", "status"},
                3
        );
    }

    // =====================================================
    // ALL CONSULTATIONS
    // =====================================================

    private JPanel createAllConsultationsTable() {

        String sql =
                "SELECT c.patient_name, c.date, c.reason, c.status " +
                "FROM consultations c " +
                "WHERE COALESCE(c.is_archived, 0) = 0 " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM patients p " +
                "WHERE p.name = c.patient_name " +
                "AND COALESCE(p.is_archived, 0) = 1) " +
                "ORDER BY c.id DESC";

        return createReadOnlyTable(
                "ALL CONSULTATIONS",
                new String[]{"Patient Name", "Date", "Reason", "Status"},
                sql,
                new String[]{"patient_name", "date", "reason", "status"},
                3
        );
    }

    // =====================================================
    // REUSABLE READ-ONLY TABLE
    // =====================================================

    private JPanel createReadOnlyTable(
            String title,
            String[] columns,
            String sql,
            String[] fields,
            int statusColumn) {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(
                                new Color(180, 200, 220)
                        ),
                        title,
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        DARK_GREEN
                )
        );

        DefaultTableModel model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {

                Object[] row = new Object[fields.length];

                for (int i = 0; i < fields.length; i++) {
                    row[i] = result.getObject(fields[i]);
                }

                model.addRow(row);
            }

        } catch (SQLException e) {
            System.out.println("Unable to load dashboard table: " + title);
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        styleTable(table);

        if (statusColumn >= 0 && statusColumn < table.getColumnCount()) {
            table.getColumnModel()
                    .getColumn(statusColumn)
                    .setCellRenderer(createStatusRenderer());
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    // =====================================================
    // COMMON TABLE DESIGN
    // =====================================================

    private void styleTable(JTable table) {

        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(220, 225, 222));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setReorderingAllowed(false);
    }

    // =====================================================
    // STATUS COLOR RENDERER
    // =====================================================

    private DefaultTableCellRenderer createStatusRenderer() {

        return new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {

                Component component =
                        super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column
                        );

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));

                String status = value == null ? "" : value.toString();

                if (status.equalsIgnoreCase("Completed")
                        || status.equalsIgnoreCase("Active")
                        || status.equalsIgnoreCase("Confirmed")) {

                    component.setForeground(new Color(45, 145, 45));

                } else if (status.equalsIgnoreCase("Ongoing")
                        || status.equalsIgnoreCase("Rescheduled")) {

                    component.setForeground(new Color(55, 90, 200));

                } else if (status.equalsIgnoreCase("Pending")) {

                    component.setForeground(new Color(210, 155, 25));

                } else if (status.equalsIgnoreCase("Cancelled")
                        || status.equalsIgnoreCase("Inactive")) {

                    component.setForeground(new Color(200, 70, 70));

                } else {

                    component.setForeground(new Color(70, 70, 70));
                }

                component.setBackground(
                        isSelected
                                ? new Color(220, 240, 232)
                                : Color.WHITE
                );

                return component;
            }
        };
    }

    // =====================================================
    // SYSTEM OVERVIEW
    // STAYS ON THE RIGHT
    // =====================================================

    private JPanel createOverview() {

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(180, 200, 220)
                        ),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                )
        );

        JLabel overviewTitle = new JLabel("SYSTEM OVERVIEW");
        overviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        overviewTitle.setForeground(DARK_GREEN);
        overviewTitle.setBorder(
                BorderFactory.createEmptyBorder(0, 5, 3, 5)
        );

        panel.add(overviewTitle, BorderLayout.NORTH);

        JPanel itemsPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        itemsPanel.setBackground(Color.WHITE);

        int registeredStaff = getCount(
                "SELECT COUNT(*) FROM users WHERE role = 'Staff'"
        );

        itemsPanel.add(createOverviewItem(
                "Registered Staff:",
                String.valueOf(registeredStaff),
                GREEN_ACCENT,
                GREEN_BG
        ));

        int activeServices = getCount(
                "SELECT COUNT(*) FROM services WHERE status = 'Active'"
        );

        itemsPanel.add(createOverviewItem(
                "Active Services:",
                String.valueOf(activeServices),
                BLUE_ACCENT,
                BLUE_BG
        ));

        int pendingAppointments = getCount(
                "SELECT COUNT(*) FROM appointments a " +
                "WHERE a.status = 'Pending' " +
                "AND COALESCE(a.is_archived, 0) = 0 " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM patients p " +
                "WHERE p.name = a.patient_name " +
                "AND COALESCE(p.is_archived, 0) = 1)"
        );

        itemsPanel.add(createOverviewItem(
                "Pending Appointments:",
                String.valueOf(pendingAppointments),
                YELLOW_ACCENT,
                YELLOW_BG
        ));

        int completedConsultations = getCount(
                "SELECT COUNT(*) FROM consultations c " +
                "WHERE c.status = 'Completed' " +
                "AND COALESCE(c.is_archived, 0) = 0 " +
                "AND NOT EXISTS (" +
                "SELECT 1 FROM patients p " +
                "WHERE p.name = c.patient_name " +
                "AND COALESCE(p.is_archived, 0) = 1)"
        );

        itemsPanel.add(createOverviewItem(
                "Completed Consultations:",
                String.valueOf(completedConsultations),
                PURPLE_ACCENT,
                PURPLE_BG
        ));

        panel.add(itemsPanel, BorderLayout.CENTER);

        return panel;
    }

    // =====================================================
    // SYSTEM OVERVIEW ITEM
    // =====================================================

    private JPanel createOverviewItem(
            String title,
            String value,
            Color valueColor,
            Color backgroundColor) {

        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(backgroundColor);

        item.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 5, 0, 0, valueColor
                        ),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                )
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(70, 70, 70));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(valueColor);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        item.add(titleLabel, BorderLayout.WEST);
        item.add(valueLabel, BorderLayout.EAST);

        return item;
    }
}
