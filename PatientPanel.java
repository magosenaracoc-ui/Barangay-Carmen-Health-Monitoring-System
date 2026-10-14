
package barangaycarmenhealthmonitoringsystem;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class PatientPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;
    private String role;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private static final Color DARK_GREEN = new Color(0, 105, 80);
    private static final Color LIGHT_GREEN = new Color(235, 247, 242);
    private static final Color BORDER_GREEN = new Color(0, 130, 100);
    private static final Color EDIT_GREEN = new Color(45, 145, 45);
    private static final Color ARCHIVE_RED = new Color(200, 70, 70);
    private static final Color DARK_GRAY = new Color(80, 80, 80);

    public PatientPanel(String role) {
        this.role = role;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBackground(Color.WHITE);
        top.add(UIUtils.title("Patient Management"), BorderLayout.WEST);

        JButton addButton = UIUtils.button("+ Add New Patient");
        addButton.setBackground(DARK_GREEN);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> addPatient());
        top.add(addButton, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        String[] columns = {
            "ID", "Patient Name", "Age", "Sex",
            "Contact", "Status", "Action", "ArchivedFlag"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 7 ? Boolean.class : Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setSelectionBackground(LIGHT_GREEN);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));

        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        int[] widths = {70, 180, 60, 80, 140, 90, 145, 0};

        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean selected,
                    boolean focus, int row, int column) {

                Component component =
                        super.getTableCellRendererComponent(
                                table, value, selected, focus, row, column);

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Arial", Font.BOLD, 12));

                int modelRow = table.convertRowIndexToModel(row);

                boolean archived = Boolean.TRUE.equals(
                        model.getValueAt(modelRow, 7));

                if (archived) {
                    setText("Archived");
                } else {
                    setText(value == null ? "-" : value.toString());
                }

                if (!selected) {
                    if (archived) {
                        component.setForeground(ARCHIVE_RED);
                    } else if ("Active".equalsIgnoreCase(
                            String.valueOf(value))) {
                        component.setForeground(EDIT_GREEN);
                    } else if ("Inactive".equalsIgnoreCase(
                            String.valueOf(value))) {
                        component.setForeground(ARCHIVE_RED);
                    } else {
                        component.setForeground(DARK_GRAY);
                    }
                } else {
                    component.setForeground(Color.BLACK);
                }

                return component;
            }
        });

        table.getColumnModel().getColumn(6)
                .setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(6)
                .setCellEditor(new ActionButtonEditor());

        // Hide the internal archive flag.
        table.removeColumn(table.getColumnModel().getColumn(7));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(250, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GREEN),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));

        statusFilter = new JComboBox<>(
                new String[]{"All", "Active", "Inactive", "Archived"});
        statusFilter.setPreferredSize(new Dimension(120, 35));
        statusFilter.setForeground(DARK_GREEN);
        statusFilter.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBackground(Color.WHITE);

        JPanel searchLeft = new JPanel(new BorderLayout(5, 5));
        searchLeft.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search Patient:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 13));

        searchLeft.add(searchLabel, BorderLayout.WEST);
        searchLeft.add(searchField, BorderLayout.CENTER);

        JPanel filterPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        filterPanel.setBackground(Color.WHITE);

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));

        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);

        searchPanel.add(searchLeft, BorderLayout.CENTER);
        searchPanel.add(filterPanel, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Color.WHITE);
        center.add(searchPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_GREEN));
        center.add(scrollPane, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {
            private void search() {
                applyFilter(
                        searchField.getText().trim(),
                        String.valueOf(statusFilter.getSelectedItem()));
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

        statusFilter.addActionListener(e -> applyFilter(
                searchField.getText().trim(),
                String.valueOf(statusFilter.getSelectedItem())));

        refreshTable();
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(230, 32));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GREEN),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    }

    private JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(DARK_GREEN);
        return label;
    }

    private JPanel createPatientForm(
            String title, JTextField id, JTextField name,
            JTextField age, JTextField sex, JTextField contact,
            JComboBox<String> status) {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        header.add(titleLabel, BorderLayout.WEST);
        mainPanel.add(header, BorderLayout.NORTH);

        int rows = status == null ? 5 : 6;

        JPanel form = new JPanel(new GridLayout(rows, 2, 10, 10));
        form.setBackground(Color.WHITE);

        form.add(formLabel("Patient ID:"));
        form.add(id);

        form.add(formLabel("Patient Name:"));
        form.add(name);

        form.add(formLabel("Age:"));
        form.add(age);

        form.add(formLabel("Sex:"));
        form.add(sex);

        form.add(formLabel("Contact:"));
        form.add(contact);

        if (status != null) {
            form.add(formLabel("Status:"));

            status.setPreferredSize(new Dimension(230, 32));
            status.setBackground(Color.WHITE);
            status.setForeground(DARK_GREEN);
            status.setFont(new Font("Arial", Font.PLAIN, 13));

            form.add(status);
        }

        mainPanel.add(form, BorderLayout.CENTER);
        return mainPanel;
    }

    // Shared Save/Cancel dialog design for Add and Edit.
    private int showPatientDialog(JPanel panel, String title) {
        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);

        optionPane.setOptions(new Object[]{"Save Patient", "Cancel"});

        JDialog dialog = optionPane.createDialog(this, title);
        dialog.setModal(true);

        optionPane.addPropertyChangeListener(e -> {
            if (dialog.isVisible()
                    && JOptionPane.VALUE_PROPERTY.equals(
                            e.getPropertyName())) {
                dialog.setVisible(false);
            }
        });

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                styleDialogButton(
                        findButton(dialog, "Save Patient"), EDIT_GREEN);
                styleDialogButton(
                        findButton(dialog, "Cancel"), ARCHIVE_RED);
            }
        });

        dialog.setVisible(true);

        return "Save Patient".equals(optionPane.getValue())
                ? JOptionPane.OK_OPTION
                : JOptionPane.CANCEL_OPTION;
    }

    private void styleDialogButton(JButton button, Color color) {
        if (button == null) {
            return;
        }

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JButton findButton(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton
                    && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            }

            if (component instanceof Container) {
                JButton found =
                        findButton((Container) component, text);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private class ActionButtonRenderer extends JPanel
            implements TableCellRenderer {

        private final JButton editButton;
        private final JButton archiveButton;

        ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 5));
            setBackground(Color.WHITE);

            editButton = createButton("EDIT", EDIT_GREEN, 45);
            archiveButton = createButton("ARCHIVE", ARCHIVE_RED, 65);

            add(editButton);
            add(archiveButton);
        }

        private JButton createButton(
                String text, Color color, int width) {

            JButton button = new JButton(text);
            button.setPreferredSize(new Dimension(width, 24));
            button.setFont(new Font("Arial", Font.BOLD, 9));
            button.setForeground(Color.WHITE);
            button.setBackground(color);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            return button;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {

            int modelRow = table.convertRowIndexToModel(row);

            boolean archived = Boolean.TRUE.equals(
                    model.getValueAt(modelRow, 7));

            editButton.setVisible(!archived);
            archiveButton.setText(archived ? "RESTORE" : "ARCHIVE");
            archiveButton.setBackground(
                    archived ? EDIT_GREEN : ARCHIVE_RED);

            setBackground(selected ? LIGHT_GREEN : Color.WHITE);

            return this;
        }
    }

    private class ActionButtonEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JPanel panel;
        private final JButton editButton;
        private final JButton archiveButton;
        private int selectedRow;

        ActionButtonEditor() {
            panel = new JPanel(
                    new FlowLayout(FlowLayout.CENTER, 2, 5));
            panel.setBackground(Color.WHITE);

            editButton = createButton("EDIT", EDIT_GREEN, 45);
            archiveButton = createButton("ARCHIVE", ARCHIVE_RED, 65);

            editButton.addActionListener(e -> {
                fireEditingStopped();
                editPatient(selectedRow);
            });

            archiveButton.addActionListener(e -> {
                fireEditingStopped();

                boolean archived = Boolean.TRUE.equals(
                        model.getValueAt(selectedRow, 7));

                if (archived) {
                    restorePatient(selectedRow);
                } else {
                    archivePatient(selectedRow);
                }
            });

            panel.add(editButton);
            panel.add(archiveButton);
        }

        private JButton createButton(
                String text, Color color, int width) {

            JButton button = new JButton(text);
            button.setPreferredSize(new Dimension(width, 24));
            button.setFont(new Font("Arial", Font.BOLD, 9));
            button.setForeground(Color.WHITE);
            button.setBackground(color);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            return button;
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean selected,
                int row, int column) {

            selectedRow = table.convertRowIndexToModel(row);

            boolean archived = Boolean.TRUE.equals(
                    model.getValueAt(selectedRow, 7));

            editButton.setVisible(!archived);
            archiveButton.setText(archived ? "RESTORE" : "ARCHIVE");
            archiveButton.setBackground(
                    archived ? EDIT_GREEN : ARCHIVE_RED);

            panel.setBackground(selected ? LIGHT_GREEN : Color.WHITE);
            panel.revalidate();
            panel.repaint();

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private void refreshTable() {
        model.setRowCount(0);

        String sql =
                "SELECT patient_id, name, age, sex, contact, status, "
                + "is_archived FROM patients ORDER BY id";

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

            try (PreparedStatement statement =
                         connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    model.addRow(new Object[]{
                        result.getString("patient_id"),
                        result.getString("name"),
                        result.getInt("age"),
                        result.getString("sex"),
                        result.getString("contact"),
                        result.getString("status"),
                        "",
                        result.getBoolean("is_archived")
                    });
                }
            }

            applyFilter(
                    searchField.getText().trim(),
                    String.valueOf(statusFilter.getSelectedItem()));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load patients from database.\n\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void applyFilter(String searchText, String status) {
        RowFilter<DefaultTableModel, Object> filter =
                new RowFilter<DefaultTableModel, Object>() {

            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel, ? extends Object> entry) {

                String id = safe(entry.getStringValue(0));
                String name = safe(entry.getStringValue(1));
                String contact = safe(entry.getStringValue(4));
                String patientStatus = safe(entry.getStringValue(5));

                boolean archived = Boolean.parseBoolean(
                        entry.getStringValue(7));

                String search = searchText.toLowerCase();

                boolean matchesSearch = search.isEmpty()
                        || id.toLowerCase().contains(search)
                        || name.toLowerCase().contains(search)
                        || contact.toLowerCase().contains(search);

                boolean matchesStatus;

                if ("Archived".equals(status)) {
                    matchesStatus = archived;
                } else if ("All".equals(status)) {
                    matchesStatus = !archived;
                } else {
                    matchesStatus = !archived
                            && patientStatus.equalsIgnoreCase(status);
                }

                return matchesSearch && matchesStatus;
            }
        };

        sorter.setRowFilter(filter);
    }

    private String safe(String value) {
        return value == null || "null".equalsIgnoreCase(value)
                ? "" : value;
    }

    // ADD PATIENT
    private void addPatient() {
        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JTextField age = new JTextField();
        JTextField sex = new JTextField();
        JTextField contact = new JTextField();

        styleTextField(id);
        styleTextField(name);
        styleTextField(age);
        styleTextField(sex);
        styleTextField(contact);

        setDigitsOnly(age);
        setContactCharactersOnly(contact);

        JPanel panel = createPatientForm(
                "Patient Details", id, name, age, sex, contact, null);

        int result = showPatientDialog(panel, "Patient Details");

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (id.getText().trim().isEmpty()
                || name.getText().trim().isEmpty()
                || age.getText().trim().isEmpty()
                || sex.getText().trim().isEmpty()
                || contact.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Please complete all fields.",
                    "Add Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientAge;

        try {
            patientAge = Integer.parseInt(age.getText().trim());

            if (patientAge <= 0) {
                JOptionPane.showMessageDialog(
                        this, "Age must be greater than zero.",
                        "Add Patient", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this, "Age must be a valid number.",
                    "Add Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patientId = id.getText().trim();
        String patientName = name.getText().trim();
        String patientSex = sex.getText().trim();
        String patientContact = contact.getText().trim();

        String sql =
                "INSERT INTO patients "
                + "(patient_id, name, age, sex, gender, contact, status, "
                + "is_archived, archived_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 0, NULL)";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, patientId);
                statement.setString(2, patientName);
                statement.setInt(3, patientAge);
                statement.setString(4, patientSex);
                statement.setString(5, patientSex);
                statement.setString(6, patientContact);
                statement.setString(7, "Active");

                statement.executeUpdate();
            }

            refreshTable();

            JOptionPane.showMessageDialog(
                    this, "Patient added successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            openConsultation(patientName);

        } catch (Exception e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("duplicate")) {
                JOptionPane.showMessageDialog(
                        this, "Patient ID already exists.",
                        "Add Patient", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                        this, "Unable to add patient.\n" + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }

            e.printStackTrace();
        }
    }

    // EDIT PATIENT
    private void editPatient(int index) {
        if (index < 0 || index >= model.getRowCount()) {
            return;
        }

        if (Boolean.TRUE.equals(model.getValueAt(index, 7))) {
            JOptionPane.showMessageDialog(
                    this, "Restore this patient before editing.",
                    "Archived Patient", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String oldId = String.valueOf(model.getValueAt(index, 0));
        String oldName = String.valueOf(model.getValueAt(index, 1));
        String currentAge = String.valueOf(model.getValueAt(index, 2));
        String currentSex = String.valueOf(model.getValueAt(index, 3));
        String currentContact = safe(
                String.valueOf(model.getValueAt(index, 4)));
        String currentStatus = String.valueOf(model.getValueAt(index, 5));

        JTextField id = new JTextField(oldId);
        JTextField name = new JTextField(oldName);
        JTextField age = new JTextField(currentAge);
        JTextField sex = new JTextField(currentSex);
        JTextField contact = new JTextField(currentContact);

        styleTextField(id);
        styleTextField(name);
        styleTextField(age);
        styleTextField(sex);
        styleTextField(contact);

        setDigitsOnly(age);
        setContactCharactersOnly(contact);

        JComboBox<String> status =
                new JComboBox<>(new String[]{"Active", "Inactive"});
        status.setSelectedItem(currentStatus);

        JPanel panel = createPatientForm(
                "Edit Patient", id, name, age, sex, contact, status);

        // Use the same custom Save/Cancel design as Add Patient.
        int result = showPatientDialog(panel, "Edit Patient");

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (id.getText().trim().isEmpty()
                || name.getText().trim().isEmpty()
                || age.getText().trim().isEmpty()
                || sex.getText().trim().isEmpty()
                || contact.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    this, "Please complete all fields.",
                    "Edit Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newAge;

        try {
            newAge = Integer.parseInt(age.getText().trim());

            if (newAge <= 0) {
                JOptionPane.showMessageDialog(
                        this, "Age must be greater than zero.",
                        "Edit Patient", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this, "Age must be a valid number.",
                    "Edit Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newId = id.getText().trim();
        String newName = name.getText().trim();
        String newSex = sex.getText().trim();
        String newContact = contact.getText().trim();
        String newStatus = String.valueOf(status.getSelectedItem());

        String updatePatientSql =
                "UPDATE patients SET patient_id = ?, name = ?, age = ?, "
                + "sex = ?, gender = ?, contact = ?, status = ? "
                + "WHERE patient_id = ? AND is_archived = 0";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int updated;

                try (PreparedStatement statement =
                             connection.prepareStatement(updatePatientSql)) {

                    statement.setString(1, newId);
                    statement.setString(2, newName);
                    statement.setInt(3, newAge);
                    statement.setString(4, newSex);
                    statement.setString(5, newSex);
                    statement.setString(6, newContact);
                    statement.setString(7, newStatus);
                    statement.setString(8, oldId);

                    updated = statement.executeUpdate();
                }

                if (updated == 0) {
                    connection.rollback();

                    JOptionPane.showMessageDialog(
                            this,
                            "Patient was not found or is archived.",
                            "Update Failed",
                            JOptionPane.WARNING_MESSAGE);

                    refreshTable();
                    return;
                }

                updateRelatedRecords(
                        connection, oldName, newName, newStatus);

                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }

            refreshTable();

            JOptionPane.showMessageDialog(
                    this, "Patient and related records updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("duplicate")) {
                JOptionPane.showMessageDialog(
                        this, "Patient ID already exists.",
                        "Update Patient", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to update patient and related records.\n"
                        + e.getMessage()
                        + "\n\nNo partial changes were saved.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            e.printStackTrace();
        }
    }

    private void updateRelatedRecords(
            Connection connection, String oldName,
            String newName, String newStatus) throws Exception {

        String consultationSql =
                "UPDATE consultations SET patient_name = ? "
                + "WHERE patient_name = ?";

        String healthRecordSql =
                "UPDATE health_records SET patient_name = ?, status = ? "
                + "WHERE patient_name = ?";

        String appointmentSql =
                "UPDATE appointments SET patient_name = ? "
                + "WHERE patient_name = ?";

        try (PreparedStatement consultationStatement =
                     connection.prepareStatement(consultationSql);
             PreparedStatement healthRecordStatement =
                     connection.prepareStatement(healthRecordSql);
             PreparedStatement appointmentStatement =
                     connection.prepareStatement(appointmentSql)) {

            consultationStatement.setString(1, newName);
            consultationStatement.setString(2, oldName);
            consultationStatement.executeUpdate();

            healthRecordStatement.setString(1, newName);
            healthRecordStatement.setString(2, newStatus);
            healthRecordStatement.setString(3, oldName);
            healthRecordStatement.executeUpdate();

            appointmentStatement.setString(1, newName);
            appointmentStatement.setString(2, oldName);
            appointmentStatement.executeUpdate();
        }
    }

    // ARCHIVE PATIENT
    private void archivePatient(int index) {
        if (index < 0 || index >= model.getRowCount()) {
            return;
        }

        String patientId = String.valueOf(model.getValueAt(index, 0));
        String patientName = String.valueOf(model.getValueAt(index, 1));

        if (Boolean.TRUE.equals(model.getValueAt(index, 7))) {
            restorePatient(index);
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "Archive " + patientName
                + "?\nThe patient record and medical history will be kept.",
                "Archive Patient",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "UPDATE patients SET is_archived = 1, archived_at = NOW() "
                + "WHERE patient_id = ? AND is_archived = 0";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, patientId);

                int updated = statement.executeUpdate();

                if (updated == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Patient was not found or is already archived.",
                            "Archive Failed",
                            JOptionPane.WARNING_MESSAGE);

                    refreshTable();
                    return;
                }
            }

            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Patient archived successfully.\n"
                    + "The patient record and medical history were kept.",
                    "Archived", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this, "Unable to archive patient.\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // RESTORE PATIENT
    private void restorePatient(int index) {
        if (index < 0 || index >= model.getRowCount()) {
            return;
        }

        String patientId = String.valueOf(model.getValueAt(index, 0));
        String patientName = String.valueOf(model.getValueAt(index, 1));

        int result = JOptionPane.showConfirmDialog(
                this,
                "Restore " + patientName + "?\n"
                + "The patient will return to the normal patient list.",
                "Restore Patient",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "UPDATE patients SET is_archived = 0, archived_at = NULL "
                + "WHERE patient_id = ? AND is_archived = 1";

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                JOptionPane.showMessageDialog(
                        this, "Database connection failed.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                statement.setString(1, patientId);

                int updated = statement.executeUpdate();

                if (updated == 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Patient was not found or is not archived.",
                            "Restore Failed",
                            JOptionPane.WARNING_MESSAGE);

                    refreshTable();
                    return;
                }
            }

            refreshTable();

            JOptionPane.showMessageDialog(
                    this, "Patient restored successfully.",
                    "Restored", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this, "Unable to restore patient.\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // NUMBERS-ONLY INPUT
    private void setDigitsOnly(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
                new DocumentFilter() {
            @Override
            public void insertString(
                    FilterBypass fb, int offset, String string,
                    AttributeSet attr) throws BadLocationException {

                if (string != null && string.matches("[0-9]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(
                    FilterBypass fb, int offset, int length,
                    String text, AttributeSet attrs)
                    throws BadLocationException {

                if (text != null && text.matches("[0-9]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    // CONTACT INPUT: DIGITS AND HYPHENS ONLY
    private void setContactCharactersOnly(JTextField field) {
        ((AbstractDocument) field.getDocument()).setDocumentFilter(
                new DocumentFilter() {
            @Override
            public void insertString(
                    FilterBypass fb, int offset, String string,
                    AttributeSet attr) throws BadLocationException {

                if (string != null && string.matches("[0-9-]*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(
                    FilterBypass fb, int offset, int length,
                    String text, AttributeSet attrs)
                    throws BadLocationException {

                if (text != null && text.matches("[0-9-]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    // OPEN CONSULTATION AFTER ADDING A PATIENT
    private void openConsultation(String patientName) {
        Container parent = getParent();

        while (parent != null) {
            if (parent instanceof MainFrame) {
                MainFrame frame = (MainFrame) parent;
                frame.showConsultation(patientName);
                return;
            }

            parent = parent.getParent();
        }

        JOptionPane.showMessageDialog(
                this, "Consultation screen could not be opened.");
    }
}
