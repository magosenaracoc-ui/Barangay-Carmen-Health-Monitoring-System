package barangaycarmenhealthmonitoringsystem;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.Stack;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class ConsultationPanel extends JPanel {

    private static final Color DARK_GREEN = new Color(0, 105, 80);
    private static final Color LIGHT_GREEN = new Color(235, 247, 242);
    private static final Color BORDER_GREEN = new Color(0, 130, 100);
    private static final Color VIEW_BLUE = new Color(55, 90, 200);
    private static final Color EDIT_GREEN = new Color(45, 145, 45);
    private static final Color ARCHIVE_RED = new Color(200, 70, 70);

    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private String selectedPatient;

    // LIFO Stack: latest archived consultation is on top.
    private final Stack<Integer> archivedConsultationStack = new Stack<>();

    public ConsultationPanel(String role) {
        this(role, null);
    }

    public ConsultationPanel(String role, String selectedPatient) {
        this.selectedPatient = selectedPatient;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.add(UIUtils.title("Consultation / Health Monitoring"),
                BorderLayout.WEST);

        JButton addButton = UIUtils.button("+ New Consultation");
        addButton.setBackground(DARK_GREEN);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setOpaque(true);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> addConsultation());

        top.add(addButton, BorderLayout.EAST);

        JPanel filterPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 5));
        filterPanel.setBackground(Color.WHITE);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DARK_GREEN, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        statusFilter = new JComboBox<>(new String[]{
            "All", "Ongoing", "Completed", "Archived"
        });
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setForeground(DARK_GREEN);
        statusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilter.setBorder(new LineBorder(DARK_GREEN, 1));

        filterPanel.add(searchLabel);
        filterPanel.add(searchField);
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);

        // Keep the title/add button on top, with search and status filter
        // directly underneath it and above the consultation table.
        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(
                headerContainer, BoxLayout.Y_AXIS));
        headerContainer.setBackground(Color.WHITE);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerContainer.add(top);
        headerContainer.add(Box.createVerticalStrut(8));
        headerContainer.add(filterPanel);
        add(headerContainer, BorderLayout.NORTH);

        String[] columns = {
            "Patient Name", "Date", "Reason", "Height", "Weight",
            "Status", "Action", "DATA", "ArchivedFlag"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(DARK_GREEN);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setOpaque(true);
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(45, 45, 45));
        table.setSelectionBackground(LIGHT_GREEN);
        table.setSelectionForeground(DARK_GREEN);
        table.setGridColor(new Color(220, 225, 222));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        int[] widths = {145, 95, 145, 70, 70, 90, 155, 0, 0};

        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        table.getColumnModel().getColumn(6).setMinWidth(150);
        table.getColumnModel().getColumn(6).setMaxWidth(160);

        table.getColumnModel().getColumn(5).setCellRenderer(
                new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean selected,
                    boolean focus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        tbl, value, selected, focus, row, column);

                String status = value == null ? "" : value.toString();

                if (!selected) {
                    if (status.equalsIgnoreCase("Ongoing")) {
                        c.setForeground(VIEW_BLUE);
                    } else if (status.equalsIgnoreCase("Completed")) {
                        c.setForeground(EDIT_GREEN);
                    } else {
                        c.setForeground(new Color(45, 45, 45));
                    }
                }

                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        hideColumn(8);
        hideColumn(7);

        table.getColumnModel().getColumn(6)
                .setCellRenderer(new ActionButtonRenderer());
        table.getColumnModel().getColumn(6)
                .setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(DARK_GREEN));
        add(scrollPane, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {
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

    private void hideColumn(int modelColumn) {
        table.getColumnModel().getColumn(modelColumn).setMinWidth(0);
        table.getColumnModel().getColumn(modelColumn).setMaxWidth(0);
        table.getColumnModel().getColumn(modelColumn).setPreferredWidth(0);
    }

    // =====================================================
    // ACTION BUTTON RENDERER
    // =====================================================

    private class ActionButtonRenderer extends JPanel
            implements TableCellRenderer {

        private boolean archived;

        ActionButtonRenderer() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(155, 34));
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
            drawButton(g, "EDIT", 38, y, 34, 22, EDIT_GREEN);

            if (archived) {
                drawButton(g, "RESTORE", 74, y, 70, 22, DARK_GREEN);
            } else {
                drawButton(g, "ARCHIVE", 74, y, 70, 22, ARCHIVE_RED);
            }

            g.dispose();
        }

        private void drawButton(Graphics2D g, String text,
                int x, int y, int width, int height, Color color) {

            g.setColor(color);
            g.fillRect(x, y, width, height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 9));

            FontMetrics fm = g.getFontMetrics();
            int textX = x + (width - fm.stringWidth(text)) / 2;
            int textY = y + (height - fm.getHeight()) / 2
                    + fm.getAscent();

            g.drawString(text, textX, textY);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tbl, Object value, boolean selected,
                boolean focus, int row, int column) {

            int modelRow = tbl.convertRowIndexToModel(row);

            archived = Boolean.parseBoolean(
                    String.valueOf(model.getValueAt(modelRow, 8)));

            setBackground(selected ? LIGHT_GREEN : Color.WHITE);
            return this;
        }
    }

    // =====================================================
    // ACTION BUTTON EDITOR
    // =====================================================

    private class ActionButtonEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 2, 5));

        private final JButton viewButton =
                createActionButton("VIEW", VIEW_BLUE, 34);

        private final JButton editButton =
                createActionButton("EDIT", EDIT_GREEN, 34);

        private final JButton archiveButton =
                createActionButton("ARCHIVE", ARCHIVE_RED, 70);

        private int selectedRow = -1;
        private boolean archived;

        ActionButtonEditor() {
            panel.setOpaque(true);
            panel.setBackground(Color.WHITE);
            panel.add(viewButton);
            panel.add(editButton);
            panel.add(archiveButton);

            viewButton.addActionListener(e -> {
                int id = getConsultationIdFromRow(selectedRow);
                fireEditingStopped();

                if (id > 0) {
                    viewConsultation(id);
                }
            });

            editButton.addActionListener(e -> {
                int id = getConsultationIdFromRow(selectedRow);
                fireEditingStopped();

                if (id > 0) {
                    if (archived) {
                        JOptionPane.showMessageDialog(
                                ConsultationPanel.this,
                                "Restore this consultation before editing it.");
                    } else {
                        editConsultation(id);
                    }
                }
            });

            archiveButton.addActionListener(e -> {
                int id = getConsultationIdFromRow(selectedRow);
                boolean wasArchived = archived;
                fireEditingStopped();

                if (id > 0) {
                    if (wasArchived) {
                        restoreConsultation(id);
                    } else {
                        archiveConsultation(id);
                    }
                }
            });
        }

        private JButton createActionButton(
                String text, Color color, int width) {

            JButton button = new JButton(text);
            button.setPreferredSize(new Dimension(width, 22));
            button.setFont(new Font("Arial", Font.BOLD, 9));
            button.setForeground(Color.WHITE);
            button.setBackground(color);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            return button;
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable tbl, Object value, boolean selected,
                int row, int column) {

            selectedRow = tbl.convertRowIndexToModel(row);

            archived = Boolean.parseBoolean(
                    String.valueOf(model.getValueAt(selectedRow, 8)));

            archiveButton.setText(archived ? "RESTORE" : "ARCHIVE");
            archiveButton.setBackground(
                    archived ? DARK_GREEN : ARCHIVE_RED);

            editButton.setEnabled(!archived);
            panel.setBackground(selected ? LIGHT_GREEN : Color.WHITE);

            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    // =====================================================
    // REFRESH FROM DATABASE AND RESTORE STACK ORDER
    // =====================================================

    private void refresh() {
        model.setRowCount(0);
        archivedConsultationStack.clear();

        // Show consultations only when their patient is still active.
        // This mirrors Patient/Appointment behavior: archived or inactive
        // patients (for example, Naruto) are not shown in this panel.
        String sql =
                "SELECT c.id, c.patient_name, c.date, c.reason, c.height, c.weight, "
                + "c.status, c.is_archived "
                + "FROM consultations c "
                + "INNER JOIN patients p "
                + "ON LOWER(TRIM(p.name)) = LOWER(TRIM(c.patient_name)) "
                + "WHERE COALESCE(p.is_archived, 0) = 0 "
                + "AND LOWER(TRIM(COALESCE(p.status, ''))) = 'active' "
                + "ORDER BY c.id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                boolean archived = result.getBoolean("is_archived");

                model.addRow(new Object[]{
                    display(result.getString("patient_name")),
                    formatDateForDisplay(result.getString("date")),
                    display(result.getString("reason")),
                    display(result.getString("height")),
                    display(result.getString("weight")),
                    display(result.getString("status")),
                    "",
                    result.getInt("id"),
                    archived
                });
            }

            // Rebuild Stack from archived_at so order survives app restarts.
            String stackSql =
                    "SELECT c.id FROM consultations c "
                    + "INNER JOIN patients p "
                    + "ON LOWER(TRIM(p.name)) = LOWER(TRIM(c.patient_name)) "
                    + "WHERE c.is_archived = 1 "
                    + "AND COALESCE(p.is_archived, 0) = 0 "
                    + "AND LOWER(TRIM(COALESCE(p.status, ''))) = 'active' "
                    + "ORDER BY c.archived_at ASC, c.id ASC";

            try (PreparedStatement stackStatement =
                         connection.prepareStatement(stackSql);
                 ResultSet stackResult = stackStatement.executeQuery()) {

                while (stackResult.next()) {
                    archivedConsultationStack.push(
                            stackResult.getInt("id"));
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to read consultations from database.\n\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        applyFilter();
    }

    // =====================================================
    // FILTER
    // =====================================================

    private void applyFilter() {
        if (sorter == null) {
            return;
        }

        String search = searchField.getText().trim().toLowerCase();
        String selectedStatus = String.valueOf(statusFilter.getSelectedItem());

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(
                    Entry<? extends DefaultTableModel, ? extends Integer> entry) {

                String patient = entry.getStringValue(0).toLowerCase();
                String date = entry.getStringValue(1).toLowerCase();
                String reason = entry.getStringValue(2).toLowerCase();
                String height = entry.getStringValue(3).toLowerCase();
                String weight = entry.getStringValue(4).toLowerCase();
                String status = entry.getStringValue(5).toLowerCase();

                boolean archived = Boolean.parseBoolean(
                        entry.getStringValue(8));

                boolean matchesSearch = search.isEmpty()
                        || patient.contains(search)
                        || date.contains(search)
                        || reason.contains(search)
                        || height.contains(search)
                        || weight.contains(search)
                        || status.contains(search);

                boolean matchesStatus;

                if (selectedStatus.equalsIgnoreCase("Archived")) {
                    matchesStatus = archived;
                } else if (selectedStatus.equalsIgnoreCase("All")) {
                    matchesStatus = !archived;
                } else {
                    matchesStatus = !archived
                            && status.equalsIgnoreCase(selectedStatus);
                }

                return matchesSearch && matchesStatus;
            }
        });
    }

    private int getConsultationIdFromRow(int modelRow) {
        if (modelRow < 0 || modelRow >= model.getRowCount()) {
            return -1;
        }

        Object value = model.getValueAt(modelRow, 7);

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // =====================================================
    // DISPLAY AND MEASUREMENT HELPERS
    // =====================================================

    private String display(String value) {
        if (value == null || value.trim().isEmpty()
                || value.trim().equalsIgnoreCase("null")) {
            return "-";
        }

        return value.trim();
    }

    private JTextField measurementField(
            String value, String unit, String placeholder) {

        String text = value == null ? "" : value.trim();
        text = text.replace("🌡", "").trim();
        text = text.replaceAll("(?i)\\s*(cm|kg|bpm|°?c)$", "").trim();

        if (text.equals("-") || text.equalsIgnoreCase("N/A")) {
            text = "";
        }

        JTextField field = new JTextField(text);
        field.setForeground(DARK_GREEN);

        ((AbstractDocument) field.getDocument()).setDocumentFilter(
                new DocumentFilter() {

            private boolean allowed(String candidate) {
                if (placeholder.equals("___ / ___")) {
                    return candidate.matches("[0-9/ ]*");
                }

                return candidate.matches("[0-9.]*");
            }

            @Override
            public void insertString(
                    FilterBypass fb, int offset, String string,
                    AttributeSet attrs) throws BadLocationException {

                if (string == null) {
                    return;
                }

                String old = fb.getDocument().getText(
                        0, fb.getDocument().getLength());

                String candidate = old.substring(0, offset)
                        + string + old.substring(offset);

                if (allowed(candidate)) {
                    super.insertString(fb, offset, string, attrs);
                }
            }

            @Override
            public void replace(
                    FilterBypass fb, int offset, int length,
                    String text, AttributeSet attrs)
                    throws BadLocationException {

                String old = fb.getDocument().getText(
                        0, fb.getDocument().getLength());

                String inserted = text == null ? "" : text;

                String candidate = old.substring(0, offset)
                        + inserted + old.substring(offset + length);

                if (allowed(candidate)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        return field;
    }

    private JPanel measurementInput(JTextField field, String suffix) {
        JPanel input = new JPanel(new BorderLayout(5, 0));
        input.setBackground(Color.WHITE);
        input.add(field, BorderLayout.CENTER);

        if (suffix != null && !suffix.isEmpty()) {
            JLabel unitLabel = new JLabel(suffix);
            unitLabel.setForeground(DARK_GREEN);
            unitLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            input.add(unitLabel, BorderLayout.EAST);
        }

        return input;
    }

    private boolean missingMeasurement(String value) {
        return value == null || value.trim().isEmpty()
                || value.trim().equals("-")
                || value.trim().equalsIgnoreCase("N/A");
    }

    private boolean validMeasurement(
            String value, String placeholder) {

        if (missingMeasurement(value)) {
            return true;
        }

        String input = value.trim();

        if (placeholder.equals("___ / ___")) {
            if (!input.matches("\\d{1,3}\\s*/\\s*\\d{1,3}")) {
                return false;
            }

            String[] parts = input.split("/");

            try {
                int systolic = Integer.parseInt(parts[0].trim());
                int diastolic = Integer.parseInt(parts[1].trim());

                return systolic > 0 && systolic <= 300
                        && diastolic > 0 && diastolic <= 200;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (!input.matches("\\d+(\\.\\d+)?")) {
            return false;
        }

        try {
            double number = Double.parseDouble(input);

            if (number <= 0) {
                return false;
            }

            if (placeholder.equals("HEIGHT")) {
                return number <= 300;
            }

            if (placeholder.equals("WEIGHT")) {
                return number <= 700;
            }

            if (placeholder.equals("TEMPERATURE")) {
                return number >= 25 && number <= 45;
            }

            if (placeholder.equals("PULSE")) {
                return number <= 300;
            }

            return true;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String normalizeMeasurement(
            String input, String unit, String placeholder) {

        String value = input == null ? "" : input.trim();

        if (value.isEmpty() || value.equals("-")
                || value.equalsIgnoreCase("N/A")) {
            return "-";
        }

        if (placeholder.equals("___ / ___")) {
            value = value.replaceAll("\\s+", "");
            return value;
        }

        if (!value.matches("\\d+(\\.\\d+)?")) {
            return "-";
        }

        if (unit.equals(" cm")) {
            return value + " cm";
        }

        if (unit.equals(" kg")) {
            return value + " kg";
        }

        if (unit.equals(" bpm")) {
            return value + " bpm"; 
        }

        if (placeholder.startsWith("🌡")) {
            return value + " °C";
        }

        return value;
    }

    // Date display/storage is always ISO: yyyy-MM-dd.
    private String formatDateForDisplay(String value) {
        return normalizeDateForField(value);
    }

    private String normalizeDateForField(String value) {
        if (value == null || value.trim().isEmpty()
                || value.trim().equalsIgnoreCase("null")
                || value.trim().equals("-")) {
            return "";
        }

        String input = value.trim();
        String[] formats = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss",
            "MM/dd/yyyy", "M/d/yyyy", "MMMM d, yyyy",
            "MMM d, yyyy", "yyyy-MM-dd'T'HH:mm:ss"
        };

        for (String pattern : formats) {
            SimpleDateFormat parser = new SimpleDateFormat(pattern, Locale.ENGLISH);
            parser.setLenient(false);
            try {
                Date parsed = parser.parse(input);
                return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        .format(parsed);
            } catch (ParseException ignored) {
                // Try the next legacy format.
            }
        }
        return input;
    }

    // Date-only picker matching the Appointment form's dropdown format.
    // Month is numeric (01-12), followed by Day and Year; no time controls.
    private class ConsultationDatePicker extends JPanel {
        private final JComboBox<String> monthBox = new JComboBox<>();
        private final JComboBox<String> dayBox = new JComboBox<>();
        private final JComboBox<String> yearBox = new JComboBox<>();

        ConsultationDatePicker(String initialDate) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            setBackground(Color.WHITE);

            Calendar today = Calendar.getInstance();
            int currentYear = today.get(Calendar.YEAR);
            int selectedYear = currentYear;
            int selectedMonth = today.get(Calendar.MONTH) + 1;
            int selectedDay = today.get(Calendar.DAY_OF_MONTH);

            String normalized = normalizeDateForField(initialDate);
            if (validDate(normalized)) {
                try {
                    Calendar selected = Calendar.getInstance();
                    selected.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                            .parse(normalized));
                    selectedYear = selected.get(Calendar.YEAR);
                    selectedMonth = selected.get(Calendar.MONTH) + 1;
                    selectedDay = selected.get(Calendar.DAY_OF_MONTH);
                } catch (ParseException ignored) {
                    // Keep today's date as the fallback.
                }
            }

            for (int month = 1; month <= 12; month++) {
                monthBox.addItem(String.format(Locale.ENGLISH, "%02d", month));
            }

            int firstYear = Math.min(currentYear - 5, selectedYear);
            int lastYear = Math.max(currentYear + 10, selectedYear);
            for (int year = firstYear; year <= lastYear; year++) {
                yearBox.addItem(String.valueOf(year));
            }

            styleDateCombo(monthBox, 68);
            styleDateCombo(dayBox, 55);
            styleDateCombo(yearBox, 78);

            monthBox.setSelectedItem(String.format(Locale.ENGLISH, "%02d", selectedMonth));
            yearBox.setSelectedItem(String.valueOf(selectedYear));
            updateDays(selectedDay);

            monthBox.addActionListener(e -> updateDays(getSelectedDay()));
            yearBox.addActionListener(e -> updateDays(getSelectedDay()));

            add(monthBox);
            add(dayBox);
            add(yearBox);
        }

        private void styleDateCombo(JComboBox<String> combo, int width) {
            combo.setBackground(Color.WHITE);
            combo.setForeground(DARK_GREEN);
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            combo.setPreferredSize(new Dimension(width, 32));
            combo.setBorder(BorderFactory.createLineBorder(BORDER_GREEN));
        }

        private int getSelectedDay() {
            Object value = dayBox.getSelectedItem();
            if (value == null) {
                return 1;
            }
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException ex) {
                return 1;
            }
        }

        private void updateDays(int preferredDay) {
            if (monthBox.getSelectedItem() == null || yearBox.getSelectedItem() == null) {
                return;
            }

            int month = Integer.parseInt(monthBox.getSelectedItem().toString());
            int year = Integer.parseInt(yearBox.getSelectedItem().toString());
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            int maximumDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            dayBox.removeAllItems();
            for (int day = 1; day <= maximumDay; day++) {
                dayBox.addItem(String.format(Locale.ENGLISH, "%02d", day));
            }
            dayBox.setSelectedItem(String.format(
                    Locale.ENGLISH, "%02d", Math.min(Math.max(preferredDay, 1), maximumDay)));
        }

        String getDateValue() {
            String month = String.valueOf(monthBox.getSelectedItem());
            String day = String.valueOf(dayBox.getSelectedItem());
            String year = String.valueOf(yearBox.getSelectedItem());
            return year + "-" + month + "-" + day;
        }
    }

    private boolean validDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return false;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);

        try {
            Date parsed = format.parse(date.trim());
            return format.format(parsed).equals(date.trim());
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean validateMeasurements(
            String height, String weight, String bp,
            String temperature, String pulse) {

        if (!validMeasurement(height, "HEIGHT")) {
            showValidationError("Height must be a valid number from 0 to 300 cm.");
            return false;
        }

        if (!validMeasurement(weight, "WEIGHT")) {
            showValidationError("Weight must be a valid number from 0 to 700 kg.");
            return false;
        }

        if (!validMeasurement(bp, "___ / ___")) {
            showValidationError(
                    "Blood Pressure must use the format 120/80.");
            return false;
        }

        if (!validMeasurement(temperature, "TEMPERATURE")) {
            showValidationError(
                    "Temperature must be a valid number from 25 to 45 °C.");
            return false;
        }

        if (!validMeasurement(pulse, "PULSE")) {
            showValidationError(
                    "Pulse Rate must be a valid number from 1 to 300 BPM.");
            return false;
        }

        return true;
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Invalid Input",
                JOptionPane.WARNING_MESSAGE);
    }

    // =====================================================
    // PATIENT VALIDATION
    // =====================================================

    private boolean isActivePatient(String patientName) {
        String sql =
                "SELECT status, is_archived FROM patients "
                + "WHERE LOWER(TRIM(name))=LOWER(TRIM(?)) "
                + "ORDER BY id DESC LIMIT 1";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, patientName);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    showValidationError(
                            "Patient not found. Please select an existing patient.");
                    return false;
                }

                String patientStatus = result.getString("status");
                boolean archived = result.getBoolean("is_archived");

                if (archived
                        || patientStatus == null
                        || !patientStatus.equalsIgnoreCase("Active")) {
                    showValidationError(
                            "This patient is inactive or archived. "
                            + "Only active patients can have new consultations.");
                    return false;
                }

                return true;
            }

        } catch (SQLException e) {
            showDatabaseError("Unable to validate patient.", e);
            return false;
        }
    }

    // =====================================================
    // VIEW CONSULTATION
    // =====================================================

    private void viewConsultation(int id) {
        String sql =
                "SELECT patient_name, date, reason, height, weight, "
                + "blood_pressure, temperature, heart_rate, status, is_archived "
                + "FROM consultations WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    JOptionPane.showMessageDialog(
                            this, "Consultation record not found.");
                    return;
                }

                JPanel details = createConsultationDetailsPanel(
                        result.getString("patient_name"),
                        formatDateForDisplay(result.getString("date")),
                        result.getString("reason"),
                        result.getString("height"),
                        result.getString("weight"),
                        result.getString("blood_pressure"),
                        result.getString("temperature"),
                        result.getString("heart_rate"),
                        result.getString("status"),
                        result.getBoolean("is_archived"));

                showStyledViewDialog(details);
            }

        } catch (SQLException e) {
            showDatabaseError("Unable to view consultation.", e);
        }
    }

    private JPanel createConsultationDetailsPanel(
            String patientName, String date, String reason,
            String height, String weight, String bloodPressure,
            String temperature, String pulseRate, String status,
            boolean archived) {

        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel title = new JLabel("Consultation Details");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(10, 2, 10, 10));
        details.setBackground(Color.WHITE);

        addDetailRow(details, "Patient Name:", display(patientName));
        addDetailRow(details, "Date:", display(date));
        addDetailRow(details, "Reason / Complaint:", display(reason));
        addDetailRow(details, "Height:", display(height));
        addDetailRow(details, "Weight:", display(weight));
        addDetailRow(details, "Blood Pressure:", display(bloodPressure));
        addDetailRow(details, "Temperature:", display(temperature));
        addDetailRow(details, "Pulse Rate:", display(pulseRate));
        addDetailRow(details, "Status:", display(status));
        addDetailRow(details, "Archive State:", archived ? "Archived" : "Active");

        mainPanel.add(details, BorderLayout.CENTER);
        return mainPanel;
    }

    private void addDetailRow(
            JPanel panel, String labelText, String valueText) {

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(DARK_GREEN);

        JLabel value = new JLabel(valueText);
        value.setFont(new Font("Arial", Font.PLAIN, 13));
        value.setForeground(new Color(45, 45, 45));
        value.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_GREEN),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        panel.add(label);
        panel.add(value);
    }

    private void showStyledViewDialog(JPanel detailsPanel) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "View Consultation",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        content.add(detailsPanel, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Arial", Font.BOLD, 13));
        okButton.setBackground(DARK_GREEN);
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setOpaque(true);
        okButton.setBorderPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setPreferredSize(new Dimension(100, 32));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(okButton);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =====================================================
    // ADD CONSULTATION
    // =====================================================

    private void addConsultation() {
        JTextField patient = new JTextField(
                selectedPatient == null ? "" : selectedPatient);

        ConsultationDatePicker date = new ConsultationDatePicker(
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        JTextField reason = new JTextField();
        JTextField height = measurementField("", " cm", "");
        JTextField weight = measurementField("", " kg", "");
        JTextField bloodPressure = measurementField("", "", "___ / ___");
        JTextField temperature = measurementField("", "", "🌡 °C");
        JTextField pulseRate = measurementField("", " bpm", "");

        JComboBox<String> status = new JComboBox<>(
                new String[]{"Ongoing", "Completed"});

        styleTextField(patient);
        styleTextField(reason);
        styleTextField(height);
        styleTextField(weight);
        styleTextField(bloodPressure);
        styleTextField(temperature);
        styleTextField(pulseRate);
        styleComboBox(status);

        JPanel panel = createConsultationForm(
                "New Consultation", patient, date, reason,
                height, weight, bloodPressure, temperature, pulseRate, status);

        if (showStyledConsultationDialog(
                panel, "New Consultation", "Save Consultation")
                != JOptionPane.OK_OPTION) {
            return;
        }

        String patientName = patient.getText().trim();
        String consultationDate = date.getDateValue();
        String consultationReason = reason.getText().trim();

        if (patientName.isEmpty() || consultationReason.isEmpty()) {
            showValidationError("Patient name and Reason / Complaint are required.");
            return;
        }

        if (!validDate(consultationDate)) {
            showValidationError(
                    "Invalid date. Use yyyy-MM-dd, for example 2026-10-12.");
            return;
        }

        if (!validateMeasurements(
                height.getText(), weight.getText(),
                bloodPressure.getText(), temperature.getText(),
                pulseRate.getText())) {
            return;
        }

        if (!isActivePatient(patientName)) {
            return;
        }

        String heightValue =
                normalizeMeasurement(height.getText(), " cm", "");
        String weightValue =
                normalizeMeasurement(weight.getText(), " kg", "");
        String bpValue = normalizeMeasurement(
                bloodPressure.getText(), "", "___ / ___");
        String tempValue = normalizeMeasurement(
                temperature.getText(), "", "🌡 °C");
        String pulseValue =
                normalizeMeasurement(pulseRate.getText(), " bpm", "");
        String statusValue = String.valueOf(status.getSelectedItem());

        String sql =
                "INSERT INTO consultations "
                + "(patient_name, date, reason, height, weight, blood_pressure, "
                + "temperature, heart_rate, status, is_archived, archived_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0, NULL)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, patientName);
                    statement.setString(2, consultationDate);
                    statement.setString(3, consultationReason);
                    statement.setString(4, heightValue);
                    statement.setString(5, weightValue);
                    statement.setString(6, bpValue);
                    statement.setString(7, tempValue);
                    statement.setString(8, pulseValue);
                    statement.setString(9, statusValue);
                    statement.executeUpdate();
                }

                saveHealthRecord(connection, patientName, consultationDate,
                        heightValue, weightValue, bpValue, tempValue,
                        pulseValue, statusValue);

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

            refresh();

            JOptionPane.showMessageDialog(
                    this, "Consultation saved successfully.");

            if (selectedPatient != null && !selectedPatient.trim().isEmpty()) {
                openAppointment(patientName);
            }

        } catch (SQLException e) {
            showDatabaseError(
                    "Unable to save consultation and synchronize Health Records.",
                    e);
        }
    }

    // =====================================================
    // EDIT CONSULTATION
    // =====================================================

    private void editConsultation(int id) {
        String patientValue;
        String dateValue;
        String reasonValue;
        String heightValue;
        String weightValue;
        String bpValue;
        String tempValue;
        String pulseValue;
        String statusValue;

        String selectSql =
                "SELECT patient_name, date, reason, height, weight, "
                + "blood_pressure, temperature, heart_rate, status, is_archived "
                + "FROM consultations WHERE id=?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    JOptionPane.showMessageDialog(
                            this, "Consultation record not found.");
                    return;
                }

                if (result.getBoolean("is_archived")) {
                    JOptionPane.showMessageDialog(
                            this, "Restore this consultation before editing it.");
                    return;
                }

                patientValue = result.getString("patient_name");
                dateValue = result.getString("date");
                reasonValue = result.getString("reason");
                heightValue = result.getString("height");
                weightValue = result.getString("weight");
                bpValue = result.getString("blood_pressure");
                tempValue = result.getString("temperature");
                pulseValue = result.getString("heart_rate");
                statusValue = result.getString("status");
            }

        } catch (SQLException e) {
            showDatabaseError("Unable to load consultation.", e);
            return;
        }

        JTextField patient = new JTextField(display(patientValue));
        ConsultationDatePicker date = new ConsultationDatePicker(
                normalizeDateForField(dateValue));
        JTextField reason = new JTextField(display(reasonValue));
        JTextField height = measurementField(heightValue, " cm", "");
        JTextField weight = measurementField(weightValue, " kg", "");
        JTextField bloodPressure = measurementField(
                bpValue, "", "___ / ___");
        JTextField temperature = measurementField(
                tempValue, "", "🌡 °C");
        JTextField pulseRate = measurementField(pulseValue, " bpm", "");

        JComboBox<String> status = new JComboBox<>(
                new String[]{"Ongoing", "Completed"});
        status.setSelectedItem(statusValue);

        styleTextField(patient);
        styleTextField(reason);
        styleTextField(height);
        styleTextField(weight);
        styleTextField(bloodPressure);
        styleTextField(temperature);
        styleTextField(pulseRate);
        styleComboBox(status);

        JPanel panel = createConsultationForm(
                "Edit Consultation", patient, date, reason,
                height, weight, bloodPressure, temperature, pulseRate, status);

        if (showStyledConsultationDialog(
                panel, "Edit Consultation", "Save Consultation")
                != JOptionPane.OK_OPTION) {
            return;
        }

        String newPatient = patient.getText().trim();
        String newDate = date.getDateValue();
        String newReason = reason.getText().trim();

        if (newPatient.isEmpty() || newReason.isEmpty()) {
            showValidationError("Patient and Reason are required.");
            return;
        }

        if (!validDate(newDate)) {
            showValidationError("Invalid date. Use yyyy-MM-dd.");
            return;
        }

        if (!validateMeasurements(
                height.getText(), weight.getText(),
                bloodPressure.getText(), temperature.getText(),
                pulseRate.getText())) {
            return;
        }

        if (!isActivePatient(newPatient)) {
            return;
        }

        String newHeight =
                normalizeMeasurement(height.getText(), " cm", "");
        String newWeight =
                normalizeMeasurement(weight.getText(), " kg", "");
        String newBP = normalizeMeasurement(
                bloodPressure.getText(), "", "___ / ___");
        String newTemp = normalizeMeasurement(
                temperature.getText(), "", "🌡 °C");
        String newPulse =
                normalizeMeasurement(pulseRate.getText(), " bpm", "");
        String newStatus = String.valueOf(status.getSelectedItem());

        String updateSql =
                "UPDATE consultations SET patient_name=?, date=?, reason=?, "
                + "height=?, weight=?, blood_pressure=?, temperature=?, "
                + "heart_rate=?, status=? WHERE id=? AND is_archived=0";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {

                    statement.setString(1, newPatient);
                    statement.setString(2, newDate);
                    statement.setString(3, newReason);
                    statement.setString(4, newHeight);
                    statement.setString(5, newWeight);
                    statement.setString(6, newBP);
                    statement.setString(7, newTemp);
                    statement.setString(8, newPulse);
                    statement.setString(9, newStatus);
                    statement.setInt(10, id);

                    int updated = statement.executeUpdate();

                    if (updated == 0) {
                        throw new SQLException(
                                "No active consultation was updated.");
                    }
                }

                // Synchronize the old patient if the patient name changed.
                if (!patientValue.equalsIgnoreCase(newPatient)) {
                    rebuildHealthRecord(connection, patientValue);
                }

                saveHealthRecord(connection, newPatient, newDate,
                        newHeight, newWeight, newBP, newTemp,
                        newPulse, newStatus);

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

            refresh();

            JOptionPane.showMessageDialog(
                    this, "Consultation updated successfully.");

        } catch (SQLException e) {
            showDatabaseError(
                    "Unable to update consultation and Health Records.", e);
        }
    }

    // =====================================================
    // ARCHIVE CONSULTATION
    // =====================================================

    private void archiveConsultation(int id) {
        String patientName;

        String selectSql =
                "SELECT patient_name FROM consultations "
                + "WHERE id=? AND is_archived=0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    JOptionPane.showMessageDialog(
                            this, "Active consultation record not found.");
                    refresh();
                    return;
                }

                patientName = result.getString("patient_name");
            }

        } catch (SQLException e) {
            showDatabaseError("Unable to find consultation.", e);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Archive the consultation for " + patientName + "?\n"
                + "The record will remain in the database.",
                "Archive Consultation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String archiveSql =
                "UPDATE consultations SET is_archived=1, archived_at=NOW() "
                + "WHERE id=? AND is_archived=0";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int updated;

                try (PreparedStatement statement =
                             connection.prepareStatement(archiveSql)) {
                    statement.setInt(1, id);
                    updated = statement.executeUpdate();
                }

                if (updated == 0) {
                    throw new SQLException(
                            "The consultation was not archived.");
                }

                rebuildHealthRecord(connection, patientName);
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

            refresh();

            JOptionPane.showMessageDialog(
                    this,
                    "Consultation archived successfully.");

        } catch (SQLException e) {
            showDatabaseError(
                    "Unable to archive consultation and update Health Records.",
                    e);
        }
    }

    // =====================================================
    // RESTORE CONSULTATION - STRICT LIFO
    // =====================================================

    private void restoreConsultation(int id) {
        // Re-read stack from the database before deciding which ID is on top.
        refreshArchiveStack();

        if (archivedConsultationStack.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "There are no archived consultations to restore.");
            return;
        }

        int latestArchivedId = archivedConsultationStack.peek();

        if (id != latestArchivedId) {
            JOptionPane.showMessageDialog(
                    this,
                    "LIFO rule: restore the most recently archived "
                    + "consultation first.\n\n"
                    + "Consultation ID " + latestArchivedId
                    + " must be restored before this record.",
                    "LIFO Stack",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Restore this consultation to the active list?",
                "Restore Consultation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String patientName = null;

        String selectSql =
                "SELECT patient_name, date, reason, height, weight, "
                + "blood_pressure, temperature, heart_rate, status "
                + "FROM consultations WHERE id=? AND is_archived=1";

        String restoreSql =
                "UPDATE consultations SET is_archived=0, archived_at=NULL "
                + "WHERE id=? AND is_archived=1";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String date = null;
                String reason = null;
                String height = null;
                String weight = null;
                String bp = null;
                String temperature = null;
                String pulse = null;
                String status = null;

                try (PreparedStatement select =
                             connection.prepareStatement(selectSql)) {

                    select.setInt(1, id);

                    try (ResultSet result = select.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException(
                                    "Archived consultation was not found.");
                        }

                        patientName = result.getString("patient_name");
                        date = result.getString("date");
                        reason = result.getString("reason");
                        height = result.getString("height");
                        weight = result.getString("weight");
                        bp = result.getString("blood_pressure");
                        temperature = result.getString("temperature");
                        pulse = result.getString("heart_rate");
                        status = result.getString("status");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(restoreSql)) {

                    statement.setInt(1, id);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException(
                                "Consultation was not restored.");
                    }
                }

                saveHealthRecord(connection, patientName, date,
                        height, weight, bp, temperature, pulse, status);

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

            refresh();

            JOptionPane.showMessageDialog(
                    this,
                    "Consultation restored successfully.");

        } catch (SQLException e) {
            showDatabaseError(
                    "Unable to restore consultation and update Health Records.",
                    e);
        }
    }

    private void refreshArchiveStack() {
        archivedConsultationStack.clear();

        String sql =
                "SELECT c.id FROM consultations c "
                + "INNER JOIN patients p "
                + "ON LOWER(TRIM(p.name)) = LOWER(TRIM(c.patient_name)) "
                + "WHERE c.is_archived = 1 "
                + "AND COALESCE(p.is_archived, 0) = 0 "
                + "AND LOWER(TRIM(COALESCE(p.status, ''))) = 'active' "
                + "ORDER BY c.archived_at ASC, c.id ASC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                archivedConsultationStack.push(result.getInt("id"));
            }

        } catch (SQLException e) {
            showDatabaseError("Unable to refresh the archive stack.", e);
        }
    }

    // =====================================================
    // STYLED FORM
    // =====================================================

    private int showStyledConsultationDialog(
            JPanel panel, String title, String saveText) {

        final int[] result = {JOptionPane.CANCEL_OPTION};

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                title,
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
        content.add(panel, BorderLayout.CENTER);

        JButton saveButton = new JButton(saveText);
        saveButton.setFont(new Font("Arial", Font.BOLD, 13));
        saveButton.setBackground(EDIT_GREEN);
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setPreferredSize(new Dimension(160, 34));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 13));
        cancelButton.setBackground(ARCHIVE_RED);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setPreferredSize(new Dimension(100, 34));

        saveButton.addActionListener(e -> {
            result[0] = JOptionPane.OK_OPTION;
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            result[0] = JOptionPane.CANCEL_OPTION;
            dialog.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
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

    private JPanel createConsultationForm(
            String title, JTextField patient, ConsultationDatePicker date,
            JTextField reason, JTextField height, JTextField weight,
            JTextField bloodPressure, JTextField temperature,
            JTextField pulseRate, JComboBox<String> status) {

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

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
        form.setBackground(Color.WHITE);

        form.add(formLabel("Patient:"));
        form.add(patient);

        form.add(formLabel("Date:"));
        form.add(date);

        form.add(formLabel("Reason / Complaint:"));
        form.add(reason);

        form.add(formLabel("Height:"));
        form.add(measurementInput(height, "cm"));

        form.add(formLabel("Weight:"));
        form.add(measurementInput(weight, "kg"));

        form.add(formLabel("Blood Pressure:"));
        form.add(measurementInput(bloodPressure, "mmHg"));

        form.add(formLabel("Temperature:"));
        form.add(measurementInput(temperature, "°C"));

        form.add(formLabel("Pulse Rate:"));
        form.add(measurementInput(pulseRate, "bpm"));

        form.add(formLabel("Status:"));
        form.add(status);

        mainPanel.add(form, BorderLayout.CENTER);
        return mainPanel;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(DARK_GREEN);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setPreferredSize(new Dimension(230, 32));
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_GREEN));
    }

    // =====================================================
    // HEALTH RECORD SYNCHRONIZATION
    // Uses the caller's connection/transaction.
    // =====================================================

    private void saveHealthRecord(
            Connection connection, String patientName, String date,
            String height, String weight, String bloodPressure,
            String temperature, String pulseRate, String status)
            throws SQLException {

        String findSql =
                "SELECT id FROM health_records WHERE patient_name=? "
                + "ORDER BY id DESC LIMIT 1";

        try (PreparedStatement find =
                     connection.prepareStatement(findSql)) {

            find.setString(1, patientName);

            try (ResultSet result = find.executeQuery()) {
                if (result.next()) {
                    int recordId = result.getInt("id");

                    String updateSql =
                            "UPDATE health_records SET date=?, height=?, weight=?, "
                            + "blood_pressure=?, temperature=?, heart_rate=?, status=? "
                            + "WHERE id=?";

                    try (PreparedStatement update =
                                 connection.prepareStatement(updateSql)) {

                        update.setString(1, date);
                        update.setString(2, displayForStorage(height));
                        update.setString(3, displayForStorage(weight));
                        update.setString(4, displayForStorage(bloodPressure));
                        update.setString(5, displayForStorage(temperature));
                        update.setString(6, displayForStorage(pulseRate));
                        update.setString(7, status);
                        update.setInt(8, recordId);
                        update.executeUpdate();
                    }

                } else {
                    String insertSql =
                            "INSERT INTO health_records "
                            + "(patient_name, date, height, weight, blood_pressure, "
                            + "temperature, heart_rate, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement insert =
                                 connection.prepareStatement(insertSql)) {

                        insert.setString(1, patientName);
                        insert.setString(2, date);
                        insert.setString(3, displayForStorage(height));
                        insert.setString(4, displayForStorage(weight));
                        insert.setString(5, displayForStorage(bloodPressure));
                        insert.setString(6, displayForStorage(temperature));
                        insert.setString(7, displayForStorage(pulseRate));
                        insert.setString(8, status);
                        insert.executeUpdate();
                    }
                }
            }
        }
    }

    // Rebuild the patient's Health Record from the latest active consultation.
    private void rebuildHealthRecord(
            Connection connection, String patientName) throws SQLException {

        String sql =
                "SELECT date, height, weight, blood_pressure, temperature, "
                + "heart_rate, status FROM consultations "
                + "WHERE LOWER(TRIM(patient_name))=LOWER(TRIM(?)) "
                + "AND is_archived=0 ORDER BY date DESC, id DESC LIMIT 1";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patientName);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    saveHealthRecord(
                            connection,
                            patientName,
                            result.getString("date"),
                            result.getString("height"),
                            result.getString("weight"),
                            result.getString("blood_pressure"),
                            result.getString("temperature"),
                            result.getString("heart_rate"),
                            result.getString("status"));
                } else {
                    // Keep the existing Health Record, but mark it archived
                    // when no active consultation remains for this patient.
                    String archiveSql =
                            "UPDATE health_records SET status='Archived' "
                            + "WHERE patient_name=?";

                    try (PreparedStatement archive =
                                 connection.prepareStatement(archiveSql)) {
                        archive.setString(1, patientName);
                        archive.executeUpdate();
                    }
                }
            }
        }
    }

    private String displayForStorage(String value) {
        return missingMeasurement(value) ? "-" : value;
    }

    // =====================================================
    // OPEN APPOINTMENT
    // =====================================================

    private void openAppointment(String patientName) {
        Container parent = getParent();

        while (parent != null) {
            if (parent instanceof MainFrame) {
                ((MainFrame) parent).showAppointment(patientName);
                return;
            }

            parent = parent.getParent();
        }

        JOptionPane.showMessageDialog(
                this, "Appointment screen could not be opened.");
    }

    private void showDatabaseError(String message, SQLException e) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n\n" + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}