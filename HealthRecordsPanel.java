package barangaycarmenhealthmonitoringsystem;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

public class HealthRecordsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    // =====================================================
    // SAME DARK GREEN THEME AS REPORTS
    // =====================================================

    private static final Color DARK_GREEN =
            new Color(0, 105, 80);

    private static final Color LIGHT_GREEN =
            new Color(232, 247, 240);

    private static final Color TABLE_HEADER =
            new Color(0, 105, 80);

    private static final Color LIGHT_BORDER =
            new Color(210, 220, 215);

    private static final Color TEXT =
            new Color(50, 50, 50);

    private static final Color ROW_ALT =
            new Color(250, 250, 250);

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public HealthRecordsPanel(String role) {

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);

        setBorder(
                BorderFactory.createEmptyBorder(
                        20, 20, 20, 20
                )
        );

        // =====================================================
        // TITLE
        // =====================================================

        JLabel title = UIUtils.title("Health Records");
        add(title, BorderLayout.NORTH);

        // =====================================================
        // TABLE COLUMNS
        // =====================================================

        String[] columns = {
            "Patient",
            "Last Visit",
            "Height",
            "Weight",
            "Blood Pressure",
            "Temperature",
            "Pulse Rate",
            "Status"
        };

        model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // =====================================================
        // TABLE
        // =====================================================

        table = new JTable(model);

        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(220, 240, 232));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 225, 222));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setAutoCreateRowSorter(true);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        // =====================================================
        // TABLE HEADER
        // =====================================================

        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12)
        );
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setPreferredSize(
                new Dimension(0, 32)
        );
        table.getTableHeader().setReorderingAllowed(false);

        // =====================================================
        // COLUMN WIDTH
        // =====================================================

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        // =====================================================
        // SORTER
        // =====================================================

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // =====================================================
        // TABLE RENDERER
        // =====================================================

        table.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {

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
                                table,
                                value,
                                isSelected,
                                hasFocus,
                                row,
                                column
                        );

                setBorder(
                        BorderFactory.createEmptyBorder(
                                0, 8, 0, 8
                        )
                );

                if (isSelected) {

                    component.setBackground(LIGHT_GREEN);
                    component.setForeground(Color.BLACK);

                } else {

                    if (row % 2 == 0) {
                        component.setBackground(Color.WHITE);
                    } else {
                        component.setBackground(ROW_ALT);
                    }

                    component.setForeground(TEXT);
                }

                // STATUS
                if (column == 7 && value != null) {

                    String status = value.toString();

                    setHorizontalAlignment(
                            SwingConstants.CENTER
                    );

                    setFont(
                            new Font("Arial", Font.BOLD, 12)
                    );

                    if (status.equalsIgnoreCase("Active")) {

                        if (!isSelected) {
                            component.setForeground(DARK_GREEN);
                        }

                    } else if (status.equalsIgnoreCase("Inactive")) {

                        if (!isSelected) {
                            component.setForeground(
                                    new Color(180, 80, 80)
                            );
                        }
                    }

                } else {

                    setHorizontalAlignment(
                            SwingConstants.LEFT
                    );

                    setFont(
                            new Font("Arial", Font.PLAIN, 12)
                    );
                }

                return component;
            }
        });

        // =====================================================
        // SEARCH + FILTER
        // =====================================================

        JPanel topPanel =
                new JPanel(new BorderLayout(15, 10));

        topPanel.setBackground(Color.WHITE);

        // =====================================================
        // SEARCH
        // =====================================================

        JPanel searchPanel =
                new JPanel(new BorderLayout(8, 0));

        searchPanel.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search Patient:");

        searchLabel.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        searchLabel.setForeground(TEXT);

        searchField = new JTextField();

        searchField.setPreferredSize(
                new Dimension(280, 36)
        );

        searchField.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        searchField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(LIGHT_BORDER),
                        BorderFactory.createEmptyBorder(
                                5, 10, 5, 10
                        )
                )
        );

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // =====================================================
        // STATUS FILTER
        // =====================================================

        JPanel filterPanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("Status:");

        filterLabel.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        filterLabel.setForeground(TEXT);

        statusFilter = new JComboBox<>(
                new String[]{
                    "All",
                    "Active",
                    "Inactive"
                }
        );

        statusFilter.setPreferredSize(
                new Dimension(120, 36)
        );

        statusFilter.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        statusFilter.setBackground(Color.WHITE);
        statusFilter.setForeground(DARK_GREEN);

        filterPanel.add(filterLabel);
        filterPanel.add(statusFilter);

        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(filterPanel, BorderLayout.EAST);

        // =====================================================
        // SEARCH FUNCTION
        // =====================================================

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

            private void search() {
                updateFilter(
                        searchField.getText().trim(),
                        statusFilter.getSelectedItem().toString()
                );
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }
        });

        // =====================================================
        // STATUS FILTER FUNCTION
        // =====================================================

        statusFilter.addActionListener(
                e -> updateFilter(
                        searchField.getText().trim(),
                        statusFilter.getSelectedItem().toString()
                )
        );

        // =====================================================
        // SCROLL PANE
        // =====================================================

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        LIGHT_BORDER,
                        1
                )
        );

        scrollPane.getViewport().setBackground(Color.WHITE);

        // =====================================================
        // CENTER
        // =====================================================

        JPanel centerPanel =
                new JPanel(new BorderLayout(10, 12));

        centerPanel.setBackground(Color.WHITE);

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // =====================================================
        // LOAD DATA FROM SQL
        // =====================================================

        refresh();
    }

    // =====================================================
    // REFRESH
    // =====================================================

    public void refresh() {

        model.setRowCount(0);

        String sql =
                "SELECT "
                + "hr.patient_name, "
                + "hr.date, "
                + "hr.height, "
                + "hr.weight, "
                + "hr.blood_pressure, "
                + "hr.temperature, "
                + "hr.heart_rate, "
                + "p.status "
                + "FROM health_records hr "
                + "LEFT JOIN patients p "
                + "ON hr.patient_name = p.name "
                + "ORDER BY hr.id";

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            if (connection == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Database connection failed.\n"
                        + "Please make sure XAMPP MySQL is running.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql);

                    ResultSet result =
                            statement.executeQuery()
            ) {

                while (result.next()) {

                    String status = result.getString("status");

                    if (status == null || status.trim().isEmpty()) {
                        status = "Inactive";
                    }

                    model.addRow(
                            new Object[]{
                                display(result.getString("patient_name")),

                                // DATE FORMAT: NUMERIC MONTH
                                displayDate(result.getString("date")),

                                displayHeight(result.getString("height")),
                                displayWeight(result.getString("weight")),
                                display(result.getString("blood_pressure")),
                                display(result.getString("temperature")),
                                display(result.getString("heart_rate")),
                                status
                            }
                    );
                }
            }

            updateFilter(
                    searchField == null
                            ? ""
                            : searchField.getText().trim(),

                    statusFilter == null
                            ? "All"
                            : statusFilter.getSelectedItem().toString()
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load health records from database.\n\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // DATE FORMAT: YYYY-MM-DD
    // EXAMPLE: 2026-10-06
    // =====================================================

    private String displayDate(String value) {

        if (value == null || value.trim().isEmpty()
                || value.trim().equalsIgnoreCase("N/A")) {
            return "-";
        }

        String dateValue = value.trim();

        // Handle ISO dates, including values containing a time.
        try {
            if (dateValue.length() >= 10) {

                String firstTenCharacters =
                        dateValue.substring(0, 10);

                LocalDate date = LocalDate.parse(
                        firstTenCharacters,
                        DateTimeFormatter.ISO_LOCAL_DATE
                );

                return date.format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                );
            }
        } catch (DateTimeParseException ignored) {
            // Try the other supported formats.
        }

        // Handle dates with a written month or numeric month.
        DateTimeFormatter[] formats = {
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("M/d/yyyy", Locale.US),
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US),
            DateTimeFormatter.ofPattern("d/M/yyyy", Locale.US),
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.US),
            DateTimeFormatter.ofPattern("yyyy/M/d", Locale.US),
            DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.US)
        };

        for (DateTimeFormatter formatter : formats) {
            try {
                LocalDate date = LocalDate.parse(dateValue, formatter);

                return date.format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                );

            } catch (DateTimeParseException ignored) {
                // Try the next format.
            }
        }

        // Keep an unknown value unchanged rather than risk
        // converting it into an incorrect date.
        return dateValue;
    }

    // =====================================================
    // DISPLAY VALUES WITH UNITS
    // =====================================================

    private String display(String value) {

        if (value == null || value.trim().isEmpty()
                || value.trim().equalsIgnoreCase("N/A")) {
            return "-";
        }

        return value.trim();
    }

    private String displayHeight(String value) {

        String result = display(value);

        if (result.equals("-")) {
            return "-";
        }

        if (!result.toLowerCase().contains("cm")) {
            result += " cm";
        }

        return result;
    }

    private String displayWeight(String value) {

        String result = display(value);

        if (result.equals("-")) {
            return "-";
        }

        if (!result.toLowerCase().contains("kg")) {
            result += " kg";
        }

        return result;
    }

    // =====================================================
    // FILTER
    // =====================================================

    private void updateFilter(
            String searchText,
            String status) {

        if (sorter == null) {
            return;
        }

        RowFilter<DefaultTableModel, Integer> filter =
                new RowFilter<DefaultTableModel, Integer>() {

            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                String patientName = entry.getStringValue(0);
                String patientStatus = entry.getStringValue(7);

                boolean matchesSearch =
                        searchText.isEmpty()
                        || patientName.toLowerCase().contains(
                                searchText.toLowerCase()
                        );

                boolean matchesStatus =
                        status.equals("All")
                        || patientStatus.equalsIgnoreCase(status);

                return matchesSearch && matchesStatus;
            }
        };

        sorter.setRowFilter(filter);
    }
}