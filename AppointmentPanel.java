
package barangaycarmenhealthmonitoringsystem;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

public class AppointmentPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private String selectedPatient;
    private String role;

    // LIFO: Last In, First Out
    private final Stack<Integer> archivedAppointmentStack = new Stack<>();

    private static final Color DARK_GREEN = new Color(0, 105, 80);
    private static final Color LIGHT_GREEN = new Color(235, 247, 242);
    private static final Color BORDER_GREEN = new Color(0, 130, 100);
    private static final Color VIEW_BLUE = new Color(55, 90, 200);
    private static final Color EDIT_GREEN = new Color(45, 145, 45);
    private static final Color DELETE_RED = new Color(200, 70, 70);

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm a";

    public AppointmentPanel(String role) {
        this(role, null);
    }

    public AppointmentPanel(String role, String selectedPatient) {
        this.role = role;
        this.selectedPatient = selectedPatient;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(UIUtils.title("Appointment Management"), BorderLayout.WEST);

        JButton addButton = UIUtils.button("+ New Appointment");
        addButton.setBackground(DARK_GREEN);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setOpaque(true);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> addAppointment());

        top.add(addButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // SEARCH AND FILTER
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filterPanel.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 13));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GREEN, 1),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));

        JLabel statusLabel = new JLabel("Filter:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));

        statusFilter = new JComboBox<>(new String[]{
            "All", "Pending", "Confirmed", "Rescheduled",
            "Cancelled", "Completed", "Archived"
        });
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setForeground(DARK_GREEN);

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);

        /*
         * DATA column:
         * appointment ID
         *
         * ArchivedFlag:
         * archive status ng mismong appointment
         *
         * PatientArchivedFlag:
         * archive status ng patient na may appointment
         */
        String[] columns = {
            "Date & Time",
            "Patient Name",
            "Purpose",
            "Status",
            "Action",
            "DATA",
            "ArchivedFlag",
            "PatientArchivedFlag"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setSelectionBackground(LIGHT_GREEN);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);

        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(165);
        table.getColumnModel().getColumn(2).setPreferredWidth(165);
        table.getColumnModel().getColumn(3).setPreferredWidth(105);

        table.getColumnModel().getColumn(4).setMinWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setMaxWidth(130);

        // Hide internal columns from the GUI.
        hideColumn(7);
        hideColumn(6);
        hideColumn(5);

        // STATUS COLORS
        table.getColumnModel().getColumn(3).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component component = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Arial", Font.BOLD, 12));

                String status = value == null ? "" : value.toString();

                if (!isSelected) {
                    if (status.equalsIgnoreCase("Pending")) {
                        component.setForeground(new Color(210, 155, 25));
                    } else if (status.equalsIgnoreCase("Confirmed")
                            || status.equalsIgnoreCase("Completed")) {
                        component.setForeground(EDIT_GREEN);
                    } else if (status.equalsIgnoreCase("Rescheduled")) {
                        component.setForeground(VIEW_BLUE);
                    } else if (status.equalsIgnoreCase("Cancelled")) {
                        component.setForeground(DELETE_RED);
                    } else {
                        component.setForeground(new Color(70, 70, 70));
                    }
                } else {
                    component.setForeground(Color.BLACK);
                }

                return component;
            }
        });

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(4).setCellRenderer(
                new ActionButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(
                new ActionButtonEditor());

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(filterPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_GREEN));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        statusFilter.addActionListener(e -> applyFilter());

        refresh();
    }

    private void hideColumn(int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setMinWidth(0);
        table.getColumnModel().getColumn(columnIndex).setMaxWidth(0);
        table.getColumnModel().getColumn(columnIndex).setPreferredWidth(0);
    }

    // ACTION BUTTON RENDERER
    private class ActionButtonRenderer extends JPanel
            implements TableCellRenderer {

        private boolean archived;

        ActionButtonRenderer() {
            setOpaque(true);
            setPreferredSize(new Dimension(130, 34));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int y = (getHeight() - 22) / 2;

            drawButton(g, "VIEW", 2, y, 34, 22, VIEW_BLUE);

            if (archived) {
                drawButton(g, "RESTORE", 38, y, 80, 22, EDIT_GREEN);
            } else {
                drawButton(g, "EDIT", 38, y, 34, 22, EDIT_GREEN);
                drawButton(g, "ARCHIVE", 74, y, 46, 22, DELETE_RED);
            }

            g.dispose();
        }

        private void drawButton(
                Graphics2D g, String text, int x, int y,
                int width, int height, Color color) {

            g.setColor(color);
            g.fillRect(x, y, width, height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 9));

            FontMetrics metrics = g.getFontMetrics();
            int textX = x + (width - metrics.stringWidth(text)) / 2;
            int textY = y + (height - metrics.getHeight()) / 2
                    + metrics.getAscent();

            g.drawString(text, textX, textY);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tbl, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            setBackground(isSelected ? LIGHT_GREEN : Color.WHITE);

            int modelRow = tbl.convertRowIndexToModel(row);
            archived = Boolean.TRUE.equals(model.getValueAt(modelRow, 6));

            return this;
        }
    }

    // ACTION BUTTON EDITOR
    private class ActionButtonEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JPanel panel;
        private final JButton viewButton;
        private final JButton editButton;
        private final JButton archiveButton;
        private int selectedRow;

        ActionButtonEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 5));
            panel.setOpaque(true);

            viewButton = createButton("VIEW", VIEW_BLUE, 34);
            editButton = createButton("EDIT", EDIT_GREEN, 34);
            archiveButton = createButton("ARCHIVE", DELETE_RED, 46);

            viewButton.addActionListener(e -> {
                fireEditingStopped();
                Integer id = getAppointmentIdFromRow(selectedRow);
                if (id != null) {
                    viewAppointment(id);
                }
            });

            editButton.addActionListener(e -> {
                fireEditingStopped();
                Integer id = getAppointmentIdFromRow(selectedRow);
                if (id != null) {
                    editAppointment(id);
                }
            });

            archiveButton.addActionListener(e -> {
                fireEditingStopped();
                Integer id = getAppointmentIdFromRow(selectedRow);

                if (id != null) {
                    boolean archived = Boolean.TRUE.equals(
                            model.getValueAt(selectedRow, 6));

                    if (archived) {
                        restoreAppointment(id);
                    } else {
                        archiveAppointment(id);
                    }
                }
            });

            panel.add(viewButton);
            panel.add(editButton);
            panel.add(archiveButton);
        }

        private JButton createButton(String text, Color color, int width) {
            JButton button = new JButton(text);
            button.setPreferredSize(new Dimension(width, 22));
            button.setMinimumSize(new Dimension(width, 22));
            button.setMaximumSize(new Dimension(width, 22));
            button.setFont(new Font("Arial", Font.BOLD, 9));
            button.setForeground(Color.WHITE);
            button.setBackground(color);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            return button;
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable tbl, Object value, boolean isSelected,
                int row, int column) {

            selectedRow = tbl.convertRowIndexToModel(row);

            boolean archived = Boolean.TRUE.equals(
                    model.getValueAt(selectedRow, 6));

            panel.setBackground(isSelected ? LIGHT_GREEN : Color.WHITE);

            editButton.setVisible(!archived);
            archiveButton.setText(archived ? "RESTORE" : "ARCHIVE");
            archiveButton.setBackground(archived ? EDIT_GREEN : DELETE_RED);

            Dimension size = new Dimension(archived ? 80 : 46, 22);
            archiveButton.setPreferredSize(size);
            archiveButton.setMinimumSize(size);
            archiveButton.setMaximumSize(size);

            panel.revalidate();
            panel.repaint();

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // REFRESH FROM DATABASE
    private void refresh() {
        model.setRowCount(0);

        /*
         * Match patient records by name because the current appointments
         * table stores patient_name rather than patient_id.
         *
         * Grouping patients by name prevents duplicate patient names
         * from multiplying appointment rows in the result.
         */
        String sql = "SELECT a.id, a.appointment_date, a.appointment_time, "
                + "a.time, a.patient_name, a.purpose, a.status, "
                + "a.is_archived, "
                + "COALESCE(p.is_archived, 0) AS patient_is_archived "
                + "FROM appointments a "
                + "LEFT JOIN ("
                + "SELECT name, MAX(is_archived) AS is_archived "
                + "FROM patients GROUP BY name"
                + ") p ON p.name = a.patient_name "
                + "ORDER BY a.id DESC";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Database connection failed.\n"
                        + "Please make sure XAMPP MariaDB is running.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    String dateTime = getDateTimeForDisplay(
                            result.getDate("appointment_date"),
                            result.getTime("appointment_time"),
                            result.getString("time"));

                    boolean appointmentArchived =
                            result.getBoolean("is_archived");

                    boolean patientArchived =
                            result.getBoolean("patient_is_archived");

                    model.addRow(new Object[]{
                        dateTime,
                        result.getString("patient_name"),
                        result.getString("purpose"),
                        result.getString("status"),
                        "",
                        result.getInt("id"),
                        appointmentArchived,
                        patientArchived
                    });
                }
            }

            applyFilter();
            table.repaint();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to read appointments from database.\n\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private String getDateTimeForDisplay(
            java.sql.Date date,
            java.sql.Time time,
            String legacyValue) {

        if (date != null) {
            Calendar calendar = Calendar.getInstance();

            if (time != null) {
                calendar.setTime(time);
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
            }

            String datePart = new SimpleDateFormat("yyyy-MM-dd")
                    .format(date);
            String timePart = new SimpleDateFormat("hh:mm a")
                    .format(calendar.getTime());

            return datePart + " " + timePart;
        }

        return formatDateTimeForDisplay(legacyValue);
    }

    private String formatDateTimeForDisplay(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String[] formats = {
            DATE_TIME_FORMAT,
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd hh:mm:ss a",
            "yyyy-MM-dd"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat parser = new SimpleDateFormat(format);
                parser.setLenient(false);
                Date date = parser.parse(value.trim());

                return new SimpleDateFormat(DATE_TIME_FORMAT).format(date);
            } catch (ParseException ignored) {
                // Try the next supported format.
            }
        }

        return value;
    }

    private Integer getAppointmentIdFromRow(int modelRow) {
        if (modelRow < 0 || modelRow >= model.getRowCount()) {
            return null;
        }

        Object data = model.getValueAt(modelRow, 5);
        return data instanceof Integer ? (Integer) data : null;
    }

    // SEARCH AND FILTER
    private void applyFilter() {
        if (sorter == null) {
            return;
        }

        String search = searchField.getText().trim().toLowerCase();
        String selectedFilter = String.valueOf(
                statusFilter.getSelectedItem());

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                String dateTime = entry.getStringValue(0).toLowerCase();
                String patient = entry.getStringValue(1).toLowerCase();
                String purpose = entry.getStringValue(2).toLowerCase();
                String status = entry.getStringValue(3).toLowerCase();

                boolean appointmentArchived = Boolean.parseBoolean(
                        entry.getStringValue(6));

                boolean patientArchived = Boolean.parseBoolean(
                        entry.getStringValue(7));

                boolean searchMatch = search.isEmpty()
                        || dateTime.contains(search)
                        || patient.contains(search)
                        || purpose.contains(search)
                        || status.contains(search);

                boolean filterMatch;

                if ("Archived".equals(selectedFilter)) {
                    // This view contains appointments explicitly archived
                    // from the Appointment Panel.
                    filterMatch = appointmentArchived;
                } else {
                    /*
                     * Active appointments must belong to active patients
                     * and must not themselves be archived.
                     */
                    filterMatch = !appointmentArchived
                            && !patientArchived
                            && ("All".equals(selectedFilter)
                            || status.equalsIgnoreCase(selectedFilter));
                }

                return searchMatch && filterMatch;
            }
        });

        table.repaint();
    }

    // VIEW APPOINTMENT
    private void viewAppointment(int id) {
        String sql = "SELECT appointment_date, appointment_time, time, "
                + "patient_name, purpose, status "
                + "FROM appointments WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        JOptionPane.showMessageDialog(
                                this, "Appointment record not found.");
                        return;
                    }

                    String dateTime = getDateTimeForDisplay(
                            result.getDate("appointment_date"),
                            result.getTime("appointment_time"),
                            result.getString("time"));

                    showViewDialog(
                            dateTime,
                            result.getString("patient_name"),
                            result.getString("purpose"),
                            result.getString("status"));
                }
            }
        } catch (SQLException e) {
            showDatabaseError("Unable to view appointment.", e);
        }
    }

    private void showViewDialog(
            String time, String patient, String purpose, String status) {

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "View Appointment",
                Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setSize(470, 390);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createDialogHeader("APPOINTMENT DETAILS"),
                BorderLayout.NORTH);

        JPanel details = new JPanel(new GridBagLayout());
        details.setBackground(Color.WHITE);
        details.setBorder(BorderFactory.createEmptyBorder(22, 25, 15, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addViewField(details, gbc, 0, "Date & Time:", time);
        addViewField(details, gbc, 1, "Patient Name:", patient);
        addViewField(details, gbc, 2, "Purpose:", purpose);
        addViewField(details, gbc, 3, "Status:", status);

        mainPanel.add(details, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 12, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton closeButton = createDialogButton("CLOSE", DELETE_RED, 90);
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private void addViewField(
            JPanel panel, GridBagConstraints gbc,
            int row, String labelText, String value) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(DARK_GREEN);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        JTextField field = new JTextField(value == null ? "" : value);
        field.setEditable(false);
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setBackground(new Color(248, 248, 248));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GREEN),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        panel.add(field, gbc);
    }

    // ADD APPOINTMENT
    private void addAppointment() {
        DateTimePicker dateTime = new DateTimePicker();
        JTextField patient = createFormField();
        JTextField purpose = createFormField();

        if (selectedPatient != null && !selectedPatient.trim().isEmpty()) {
            patient.setText(selectedPatient);
        }

        JComboBox<String> status = createStatusCombo();

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "New Appointment",
                Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setSize(500, 410);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createDialogHeader("NEW APPOINTMENT"),
                BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addFormField(form, gbc, 0, "Date & Time:", dateTime);
        addFormField(form, gbc, 1, "Patient:", patient);
        addFormField(form, gbc, 2, "Purpose:", purpose);
        addFormField(form, gbc, 3, "Status:", status);

        mainPanel.add(form, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = createDialogButton("CANCEL", DELETE_RED, 90);
        JButton saveButton = createDialogButton("SAVE", EDIT_GREEN, 90);

        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            String dateTimeValue = dateTime.getDateTimeValue();
            String patientValue = patient.getText().trim();
            String purposeValue = purpose.getText().trim();
            String statusValue = status.getSelectedItem().toString();

            if (!validateAppointment(
                    dialog, patient, patientValue, purpose, purposeValue)) {
                return;
            }

            // Only active patients may receive new appointments.
            if (!isActivePatient(patientValue)) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Patient not found or already archived.\n"
                        + "Please select an active patient.",
                        "Invalid Patient",
                        JOptionPane.WARNING_MESSAGE);
                patient.requestFocus();
                return;
            }

            String insertSql = "INSERT INTO appointments "
                    + "(appointment_date, appointment_time, time, "
                    + "patient_name, purpose, status, is_archived, archived_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, 0, NULL)";

            try (Connection connection = DatabaseConnection.getConnection()) {

                if (connection == null) {
                    JOptionPane.showMessageDialog(
                            dialog, "Database connection failed.",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(insertSql)) {

                    setDateTimeParameters(statement, 1, dateTimeValue);
                    statement.setString(3, dateTimeValue);
                    statement.setString(4, patientValue);
                    statement.setString(5, purposeValue);
                    statement.setString(6, statusValue);

                    statement.executeUpdate();
                }

                refresh();
                dialog.dispose();

                JOptionPane.showMessageDialog(
                        this, "Appointment added successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                if (selectedPatient != null
                        && !selectedPatient.trim().isEmpty()) {
                    returnToPatients();
                }

            } catch (SQLException ex) {
                showDatabaseError("Unable to save appointment.", ex);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    // CHECK PATIENT EXISTS AND IS NOT ARCHIVED
    private boolean isActivePatient(String patientName) {
        String sql = "SELECT COUNT(*) FROM patients "
                + "WHERE name = ? AND is_archived = 0";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                return false;
            }

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, patientName);

                try (ResultSet result = statement.executeQuery()) {
                    return result.next() && result.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            showDatabaseError("Unable to verify patient.", e);
            return false;
        }
    }

    // EDIT APPOINTMENT
    private void editAppointment(int id) {
        String oldDateTime;
        String patientValue;
        String purposeValue;
        String statusValue;

        String selectSql = "SELECT appointment_date, appointment_time, time, "
                + "patient_name, purpose, status, is_archived "
                + "FROM appointments WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                statement.setInt(1, id);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        JOptionPane.showMessageDialog(
                                this, "Appointment record not found.");
                        return;
                    }

                    if (result.getBoolean("is_archived")) {
                        JOptionPane.showMessageDialog(
                                this, "Restore this appointment before editing.");
                        return;
                    }

                    oldDateTime = getDateTimeForDisplay(
                            result.getDate("appointment_date"),
                            result.getTime("appointment_time"),
                            result.getString("time"));

                    patientValue = result.getString("patient_name");
                    purposeValue = result.getString("purpose");
                    statusValue = result.getString("status");
                }
            }
        } catch (SQLException e) {
            showDatabaseError("Unable to load appointment.", e);
            return;
        }

        // Do not edit appointments belonging to archived patients.
        if (!isActivePatient(patientValue)) {
            JOptionPane.showMessageDialog(
                    this,
                    "This patient's record is archived.\n"
                    + "Restore the patient before editing the appointment.",
                    "Archived Patient",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DateTimePicker dateTime = new DateTimePicker();
        dateTime.setDateTimeValue(oldDateTime);

        JTextField patient = createFormField();
        patient.setText(patientValue == null ? "" : patientValue);

        JTextField purpose = createFormField();
        purpose.setText(purposeValue == null ? "" : purposeValue);

        JComboBox<String> status = createStatusCombo();
        status.setSelectedItem(statusValue);

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Edit Appointment",
                Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setSize(500, 410);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(createDialogHeader("EDIT APPOINTMENT"),
                BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addFormField(form, gbc, 0, "Date & Time:", dateTime);
        addFormField(form, gbc, 1, "Patient:", patient);
        addFormField(form, gbc, 2, "Purpose:", purpose);
        addFormField(form, gbc, 3, "Status:", status);

        mainPanel.add(form, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = createDialogButton("CANCEL", DELETE_RED, 90);
        JButton updateButton = createDialogButton("UPDATE", EDIT_GREEN, 90);

        cancelButton.addActionListener(e -> dialog.dispose());

        updateButton.addActionListener(e -> {
            String newDateTime = dateTime.getDateTimeValue();
            String newPatient = patient.getText().trim();
            String newPurpose = purpose.getText().trim();
            String newStatus = status.getSelectedItem().toString();

            if (!validateAppointment(
                    dialog, patient, newPatient, purpose, newPurpose)) {
                return;
            }

            if (!isActivePatient(newPatient)) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Patient not found or already archived.\n"
                        + "Please select an active patient.",
                        "Invalid Patient",
                        JOptionPane.WARNING_MESSAGE);
                patient.requestFocus();
                return;
            }

            String updateSql = "UPDATE appointments SET "
                    + "appointment_date = ?, appointment_time = ?, time = ?, "
                    + "patient_name = ?, purpose = ?, status = ? "
                    + "WHERE id = ? AND is_archived = 0";

            try (Connection connection = DatabaseConnection.getConnection()) {

                if (connection == null) {
                    JOptionPane.showMessageDialog(
                            dialog, "Database connection failed.",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(updateSql)) {

                    setDateTimeParameters(statement, 1, newDateTime);
                    statement.setString(3, newDateTime);
                    statement.setString(4, newPatient);
                    statement.setString(5, newPurpose);
                    statement.setString(6, newStatus);
                    statement.setInt(7, id);

                    int updated = statement.executeUpdate();

                    if (updated == 0) {
                        JOptionPane.showMessageDialog(
                                dialog,
                                "Appointment was not updated. It may be archived.",
                                "Update Failed",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                refresh();
                dialog.dispose();

                JOptionPane.showMessageDialog(
                        this, "Appointment updated successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                showDatabaseError("Unable to update appointment.", ex);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(updateButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private boolean validateAppointment(
            Component parent,
            JTextField patient, String patientValue,
            JTextField purpose, String purposeValue) {

        if (patientValue.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent, "Patient name is required.",
                    "Required Field", JOptionPane.WARNING_MESSAGE);
            patient.requestFocus();
            return false;
        }

        if (purposeValue.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent, "Purpose is required.",
                    "Required Field", JOptionPane.WARNING_MESSAGE);
            purpose.requestFocus();
            return false;
        }

        return true;
    }

    // SET DATE AND TIME IN MYSQL COLUMNS
    private void setDateTimeParameters(
            PreparedStatement statement, int startIndex, String dateTimeValue)
            throws SQLException {

        try {
            SimpleDateFormat parser = new SimpleDateFormat(DATE_TIME_FORMAT);
            parser.setLenient(false);

            Date parsed = parser.parse(dateTimeValue);

            String datePart = new SimpleDateFormat("yyyy-MM-dd").format(parsed);
            String timePart = new SimpleDateFormat("HH:mm:ss").format(parsed);

            statement.setDate(
                    startIndex, java.sql.Date.valueOf(datePart));
            statement.setTime(
                    startIndex + 1, java.sql.Time.valueOf(timePart));

        } catch (ParseException ex) {
            throw new SQLException("Invalid appointment date or time.", ex);
        }
    }

    // ARCHIVE APPOINTMENT - NO PERMANENT DELETE
    private void archiveAppointment(int id) {
        String patientName;

        String selectSql = "SELECT patient_name FROM appointments "
                + "WHERE id = ? AND is_archived = 0";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                statement.setInt(1, id);

                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        JOptionPane.showMessageDialog(
                                this, "Active appointment record not found.");
                        return;
                    }

                    patientName = result.getString("patient_name");
                }
            }
        } catch (SQLException e) {
            showDatabaseError("Unable to find appointment.", e);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Archive appointment for " + patientName + "?",
                "Archive Appointment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String archiveSql = "UPDATE appointments "
                + "SET is_archived = 1, archived_at = NOW() "
                + "WHERE id = ? AND is_archived = 0";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(archiveSql)) {
                statement.setInt(1, id);
                int updated = statement.executeUpdate();

                if (updated > 0) {
                    archivedAppointmentStack.push(id);
                    refresh();

                    JOptionPane.showMessageDialog(
                            this,
                            "Appointment archived successfully.",
                            "Archived",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            showDatabaseError("Unable to archive appointment.", e);
        }
    }

    // RESTORE APPOINTMENT
    private void restoreAppointment(int id) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Restore this appointment?",
                "Restore Appointment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String restoreSql = "UPDATE appointments "
                + "SET is_archived = 0, archived_at = NULL "
                + "WHERE id = ? AND is_archived = 1";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement = connection.prepareStatement(restoreSql)) {
                statement.setInt(1, id);
                int updated = statement.executeUpdate();

                if (updated > 0) {
                    archivedAppointmentStack.removeElement(id);
                    refresh();

                    JOptionPane.showMessageDialog(
                            this, "Appointment restored successfully.",
                            "Restored", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(
                            this, "Archived appointment not found.");
                }
            }
        } catch (SQLException e) {
            showDatabaseError("Unable to restore appointment.", e);
        }
    }

    // DATE AND TIME PICKER
    private class DateTimePicker extends JPanel {

        private final JComboBox<String> monthCombo;
        private final JComboBox<Integer> dayCombo;
        private final JComboBox<Integer> yearCombo;

        private final JComboBox<String> hourCombo;
        private final JComboBox<String> minuteCombo;
        private final JComboBox<String> amPmCombo;

        private boolean updatingDate = false;

        private final String[] months = {
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        };

        DateTimePicker() {
            setLayout(new BorderLayout(0, 4));
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(280, 66));
            setMinimumSize(new Dimension(280, 66));

            Calendar calendar = Calendar.getInstance();
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            JPanel datePanel = new JPanel(
                    new FlowLayout(FlowLayout.LEFT, 4, 0));
            datePanel.setBackground(Color.WHITE);

            monthCombo = new JComboBox<>(months);
            dayCombo = new JComboBox<>();
            yearCombo = new JComboBox<>();

            for (int year = currentYear - 5; year <= currentYear + 10; year++) {
                yearCombo.addItem(year);
            }

            monthCombo.setSelectedIndex(currentMonth);
            yearCombo.setSelectedItem(currentYear);
            updateDayOptions(currentDay);

            styleCombo(monthCombo, 100);
            styleCombo(dayCombo, 48);
            styleCombo(yearCombo, 66);

            datePanel.add(monthCombo);
            datePanel.add(dayCombo);
            datePanel.add(yearCombo);

            JPanel timePanel = new JPanel(
                    new FlowLayout(FlowLayout.LEFT, 4, 0));
            timePanel.setBackground(Color.WHITE);

            hourCombo = new JComboBox<>();
            minuteCombo = new JComboBox<>();
            amPmCombo = new JComboBox<>(new String[]{"AM", "PM"});

            for (int hour = 1; hour <= 12; hour++) {
                hourCombo.addItem(String.format("%02d", hour));
            }

            for (int minute = 0; minute < 60; minute++) {
                minuteCombo.addItem(String.format("%02d", minute));
            }

            int hour24 = calendar.get(Calendar.HOUR_OF_DAY);
            int hour12 = hour24 % 12;
            if (hour12 == 0) {
                hour12 = 12;
            }

            hourCombo.setSelectedItem(String.format("%02d", hour12));
            minuteCombo.setSelectedItem(
                    String.format("%02d", calendar.get(Calendar.MINUTE)));
            amPmCombo.setSelectedItem(hour24 >= 12 ? "PM" : "AM");

            styleCombo(hourCombo, 55);
            styleCombo(minuteCombo, 55);
            styleCombo(amPmCombo, 55);

            JLabel colon = new JLabel(":");
            colon.setFont(new Font("Arial", Font.BOLD, 14));
            colon.setForeground(DARK_GREEN);

            timePanel.add(hourCombo);
            timePanel.add(colon);
            timePanel.add(minuteCombo);
            timePanel.add(amPmCombo);

            add(datePanel, BorderLayout.NORTH);
            add(timePanel, BorderLayout.CENTER);

            monthCombo.addActionListener(e -> {
                if (!updatingDate) {
                    Object selected = dayCombo.getSelectedItem();
                    int preferredDay = selected == null ? 1 : (Integer) selected;
                    updateDayOptions(preferredDay);
                }
            });

            yearCombo.addActionListener(e -> {
                if (!updatingDate) {
                    Object selected = dayCombo.getSelectedItem();
                    int preferredDay = selected == null ? 1 : (Integer) selected;
                    updateDayOptions(preferredDay);
                }
            });
        }

        private void styleCombo(JComboBox<?> combo, int width) {
            combo.setFont(new Font("Arial", Font.PLAIN, 12));
            combo.setBackground(Color.WHITE);
            combo.setForeground(DARK_GREEN);
            combo.setPreferredSize(new Dimension(width, 29));
            combo.setFocusable(true);
            combo.setBorder(BorderFactory.createLineBorder(BORDER_GREEN, 1));
        }

        private void updateDayOptions(int preferredDay) {
            if (monthCombo.getSelectedIndex() < 0
                    || yearCombo.getSelectedItem() == null) {
                return;
            }

            updatingDate = true;

            int month = monthCombo.getSelectedIndex();
            int year = (Integer) yearCombo.getSelectedItem();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);

            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            dayCombo.removeAllItems();

            for (int day = 1; day <= daysInMonth; day++) {
                dayCombo.addItem(day);
            }

            int selectedDay = Math.max(1, Math.min(preferredDay, daysInMonth));
            dayCombo.setSelectedItem(selectedDay);

            updatingDate = false;
        }

        String getDateTimeValue() {
            int year = (Integer) yearCombo.getSelectedItem();
            int month = monthCombo.getSelectedIndex();
            int day = (Integer) dayCombo.getSelectedItem();

            int hour = Integer.parseInt((String) hourCombo.getSelectedItem());
            int minute = Integer.parseInt((String) minuteCombo.getSelectedItem());
            String amPm = (String) amPmCombo.getSelectedItem();

            if ("PM".equals(amPm) && hour != 12) {
                hour += 12;
            } else if ("AM".equals(amPm) && hour == 12) {
                hour = 0;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return new SimpleDateFormat(DATE_TIME_FORMAT).format(calendar.getTime());
        }

        void setDateTimeValue(String value) {
            if (value == null || value.trim().isEmpty()) {
                return;
            }

            String[] formats = {
                "yyyy-MM-dd hh:mm a",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd hh:mm:ss a",
                "yyyy-MM-dd"
            };

            for (String format : formats) {
                try {
                    SimpleDateFormat parser = new SimpleDateFormat(format);
                    parser.setLenient(false);

                    Date parsedDate = parser.parse(value.trim());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(parsedDate);

                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    updatingDate = true;

                    boolean yearExists = false;
                    for (int i = 0; i < yearCombo.getItemCount(); i++) {
                        if (yearCombo.getItemAt(i) == year) {
                            yearExists = true;
                            break;
                        }
                    }

                    if (!yearExists) {
                        yearCombo.addItem(year);
                    }

                    yearCombo.setSelectedItem(year);
                    monthCombo.setSelectedIndex(month);
                    updatingDate = false;

                    updateDayOptions(day);

                    int hour24 = calendar.get(Calendar.HOUR_OF_DAY);
                    int hour12 = hour24 % 12;
                    if (hour12 == 0) {
                        hour12 = 12;
                    }

                    hourCombo.setSelectedItem(String.format("%02d", hour12));
                    minuteCombo.setSelectedItem(
                            String.format("%02d", calendar.get(Calendar.MINUTE)));
                    amPmCombo.setSelectedItem(hour24 >= 12 ? "PM" : "AM");

                    return;
                } catch (ParseException ignored) {
                    // Try the next supported format.
                }
            }
        }
    }

    // FORM DESIGN HELPERS
    private JTextField createFormField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(280, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GREEN, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return field;
    }

    private JComboBox<String> createStatusCombo() {
        JComboBox<String> combo = new JComboBox<>(new String[]{
            "Pending", "Confirmed", "Rescheduled", "Cancelled", "Completed"
        });

        combo.setFont(new Font("Arial", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(DARK_GREEN);
        combo.setPreferredSize(new Dimension(280, 34));
        combo.setBorder(BorderFactory.createLineBorder(BORDER_GREEN, 1));
        return combo;
    }

    private void addFormField(
            JPanel panel, GridBagConstraints gbc,
            int row, String labelText, Component field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(DARK_GREEN);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private JPanel createDialogHeader(String text) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel(text);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JButton createDialogButton(String text, Color color, int width) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, 34));
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showDatabaseError(String message, SQLException error) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n\n" + error.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // RETURN TO PATIENTS
    private void returnToPatients() {
        Container parent = getParent();

        while (parent != null) {
            if (parent instanceof MainFrame) {
                MainFrame frame = (MainFrame) parent;
                frame.showPatients();
                return;
            }

            parent = parent.getParent();
        }

        JOptionPane.showMessageDialog(
                this, "Unable to return to Patients screen.");
    }
}
