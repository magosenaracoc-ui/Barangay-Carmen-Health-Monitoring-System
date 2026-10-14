
package barangaycarmenhealthmonitoringsystem;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

public class ReportsPanel extends JPanel {

    private static final Color DARK_GREEN = new Color(0, 105, 80);
    private static final Color LIGHT_GREEN = new Color(232, 247, 240);
    private static final Color TABLE_HEADER = new Color(0, 105, 80);
    private static final Color LIGHT_BORDER = new Color(210, 220, 215);

    private JLabel totalPatientsLabel;
    private JLabel totalConsultationsLabel;
    private JLabel totalAppointmentsLabel;
    private JLabel activeMonitoringLabel;

    private DefaultTableModel conditionModel;
    private DefaultTableModel historyModel;

    private PieChartPanel pieChart;

    private JButton fromDateButton;
    private JButton toDateButton;

    private java.util.Date selectedFromDate;
    private java.util.Date selectedToDate;

    private JTable historyTable;

    // DATE ONLY: yyyy-MM-dd
    private final SimpleDateFormat displayDateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public ReportsPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = UIUtils.title("Reports");
        add(title, BorderLayout.NORTH);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);

        // SUMMARY CARDS
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        cardsPanel.setBackground(Color.WHITE);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel patientsCard = createCard(
                "TOTAL PATIENTS", "0", new Color(58, 157, 48));

        JPanel consultationsCard = createCard(
                "TOTAL CONSULTATIONS", "0", new Color(55, 90, 200));

        JPanel appointmentsCard = createCard(
                "APPOINTMENTS", "0", new Color(220, 165, 40));

        JPanel monitoringCard = createCard(
                "ACTIVE MONITORING", "0", new Color(125, 80, 175));

        totalPatientsLabel = getValueLabel(patientsCard);
        totalConsultationsLabel = getValueLabel(consultationsCard);
        totalAppointmentsLabel = getValueLabel(appointmentsCard);
        activeMonitoringLabel = getValueLabel(monitoringCard);

        cardsPanel.add(patientsCard);
        cardsPanel.add(consultationsCard);
        cardsPanel.add(appointmentsCard);
        cardsPanel.add(monitoringCard);

        main.add(cardsPanel);
        main.add(Box.createVerticalStrut(15));

        // DATE FILTER
        JPanel datePanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 8));

        datePanel.setBackground(Color.WHITE);
        datePanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(DARK_GREEN, 1),
                "REPORT DATE", 0, 0,
                new Font("Arial", Font.BOLD, 12), DARK_GREEN));
        datePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(new Font("Arial", Font.BOLD, 12));

        fromDateButton = createGreenButton("Select Date");
        fromDateButton.addActionListener(e -> chooseDate(true));

        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(new Font("Arial", Font.BOLD, 12));

        toDateButton = createGreenButton("Select Date");
        toDateButton.addActionListener(e -> chooseDate(false));

        JButton generateButton = createGreenButton("Generate");
        generateButton.addActionListener(e -> refreshReport());

        JButton clearButton = createGreenButton("Clear");
        clearButton.setBackground(new Color(210, 85, 85));
        clearButton.addActionListener(e -> clearDateFilter());

        datePanel.add(fromLabel);
        datePanel.add(fromDateButton);
        datePanel.add(toLabel);
        datePanel.add(toDateButton);
        datePanel.add(generateButton);
        datePanel.add(clearButton);

        main.add(datePanel);
        main.add(Box.createVerticalStrut(15));

        // TOP HEALTH CONDITIONS TABLE
        conditionModel = new DefaultTableModel(
                new Object[]{"Condition", "Count"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable conditionTable = new JTable(conditionModel);
        styleTable(conditionTable);

        JScrollPane conditionScroll = new JScrollPane(conditionTable);
        conditionScroll.setBorder(new LineBorder(LIGHT_BORDER, 1));

        JPanel conditionPanel = new JPanel(new BorderLayout());
        conditionPanel.setBackground(Color.WHITE);
        conditionPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(DARK_GREEN, 1),
                "TOP HEALTH CONDITIONS", 0, 0,
                new Font("Arial", Font.BOLD, 12), DARK_GREEN));

        conditionPanel.add(conditionScroll, BorderLayout.CENTER);

        // PIE CHART
        pieChart = new PieChartPanel();

        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(DARK_GREEN, 1),
                "HEALTH CONDITIONS", 0, 0,
                new Font("Arial", Font.BOLD, 12), DARK_GREEN));

        chartPanel.add(pieChart, BorderLayout.CENTER);

        // REPORT AREA
        JPanel reportArea = new JPanel(new GridLayout(1, 2, 15, 0));
        reportArea.setBackground(Color.WHITE);
        reportArea.setPreferredSize(new Dimension(900, 300));
        reportArea.setMinimumSize(new Dimension(700, 250));
        reportArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        reportArea.add(conditionPanel);
        reportArea.add(chartPanel);

        main.add(reportArea);
        main.add(Box.createVerticalStrut(15));

        // REPORT HISTORY
        historyModel = new DefaultTableModel(
                new Object[]{
                    "Date", "Condition / Purpose", "Record Type", "Status"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyModel);
        styleTable(historyTable);

        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setPreferredSize(new Dimension(900, 250));
        historyScroll.setBorder(new LineBorder(LIGHT_BORDER, 1));

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(DARK_GREEN, 1),
                "REPORT HISTORY", 0, 0,
                new Font("Arial", Font.BOLD, 12), DARK_GREEN));
        historyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        main.add(historyPanel);

        // OUTER SCROLL
        JScrollPane outerScroll = new JScrollPane(main);
        outerScroll.setBorder(BorderFactory.createEmptyBorder());
        outerScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(outerScroll, BorderLayout.CENTER);

        refreshReport();
    }

    private JButton createGreenButton(String text) {

        JButton button = new JButton(text);
        button.setBackground(DARK_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        return button;
    }

    private JPanel createCard(String title, String value, Color background) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(background);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(background.darker(), 1),
                BorderFactory.createEmptyBorder(15, 18, 15, 18)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);

        return card;
    }

    private JLabel getValueLabel(JPanel card) {

        for (Component component : card.getComponents()) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;

                if (label.getFont().getSize() >= 20) {
                    return label;
                }
            }
        }

        return new JLabel("0");
    }

    private void styleTable(JTable table) {

        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(220, 240, 232));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 225, 222));
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setOpaque(true);
    }

    // DATE PICKER: numeric month, day, and year only; no time selector
    private void chooseDate(boolean isFromDate) {

        Calendar calendar = Calendar.getInstance();

        if (isFromDate && selectedFromDate != null) {
            calendar.setTime(selectedFromDate);
        } else if (!isFromDate && selectedToDate != null) {
            calendar.setTime(selectedToDate);
        }

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(LIGHT_GREEN);

        JLabel monthLabel = new JLabel("Month:");
        JLabel dayLabel = new JLabel("Day:");
        JLabel yearLabel = new JLabel("Year:");

        String[] months = {
            "01", "02", "03", "04", "05", "06",
            "07", "08", "09", "10", "11", "12"
        };

        JComboBox<String> monthCombo = new JComboBox<>(months);
        JComboBox<Integer> dayCombo = new JComboBox<>();
        JComboBox<Integer> yearCombo = new JComboBox<>();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (int year = currentYear - 10; year <= currentYear + 10; year++) {
            yearCombo.addItem(year);
        }

        monthCombo.setSelectedIndex(calendar.get(Calendar.MONTH));
        yearCombo.setSelectedItem(calendar.get(Calendar.YEAR));

        updateDays(
                dayCombo,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        monthCombo.addActionListener(e -> {
            Integer year = (Integer) yearCombo.getSelectedItem();
            Integer selectedDay = (Integer) dayCombo.getSelectedItem();

            if (year != null) {
                updateDays(
                        dayCombo,
                        year,
                        monthCombo.getSelectedIndex(),
                        selectedDay);
            }
        });

        yearCombo.addActionListener(e -> {
            Integer year = (Integer) yearCombo.getSelectedItem();
            Integer selectedDay = (Integer) dayCombo.getSelectedItem();

            if (year != null) {
                updateDays(
                        dayCombo,
                        year,
                        monthCombo.getSelectedIndex(),
                        selectedDay);
            }
        });

        styleComboBox(monthCombo);
        styleComboBox(dayCombo);
        styleComboBox(yearCombo);

        panel.add(monthLabel);
        panel.add(monthCombo);
        panel.add(dayLabel);
        panel.add(dayCombo);
        panel.add(yearLabel);
        panel.add(yearCombo);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Select Report Date",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {

            int month = monthCombo.getSelectedIndex();
            Integer day = (Integer) dayCombo.getSelectedItem();
            Integer year = (Integer) yearCombo.getSelectedItem();

            if (day == null || year == null) {
                return;
            }

            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);

            if (isFromDate) {
                selectedFromDate = selected.getTime();
                fromDateButton.setText(
                        displayDateFormat.format(selectedFromDate));
            } else {
                selectedToDate = selected.getTime();
                toDateButton.setText(
                        displayDateFormat.format(selectedToDate));
            }
        }
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(DARK_GREEN);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    private void updateDays(
            JComboBox<Integer> dayCombo,
            int year,
            int month,
            Integer preferredDay) {

        dayCombo.removeAllItems();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= days; day++) {
            dayCombo.addItem(day);
        }

        if (preferredDay != null) {
            dayCombo.setSelectedItem(Math.min(preferredDay, days));
        }
    }

    private void clearDateFilter() {

        selectedFromDate = null;
        selectedToDate = null;

        fromDateButton.setText("Select Date");
        toDateButton.setText("Select Date");

        refreshReport();
    }

    private void refreshReport() {

        if (selectedFromDate != null
                && selectedToDate != null
                && selectedFromDate.after(selectedToDate)) {

            JOptionPane.showMessageDialog(
                    this,
                    "The From date cannot be later than the To date.",
                    "Invalid Date Range",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection == null) {
                showDatabaseError();
                return;
            }

            totalPatientsLabel.setText(String.valueOf(
                    getCount(connection, "SELECT COUNT(*) FROM patients")));

            activeMonitoringLabel.setText(String.valueOf(
                    getCount(
                            connection,
                            "SELECT COUNT(*) FROM patients WHERE status = ?",
                            "Active")));

            totalConsultationsLabel.setText(String.valueOf(
                    getConsultationCount(connection)));

            totalAppointmentsLabel.setText(String.valueOf(
                    getAppointmentCount(connection)));

            loadConditions(connection);
            loadHistory(connection);

        } catch (SQLException e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Database Error:\n" + e.getMessage()
                    + "\n\nPlease verify your database tables and columns.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getCount(Connection connection, String sql)
            throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    private int getCount(
            Connection connection,
            String sql,
            String parameter) throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, parameter);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    private int getConsultationCount(Connection connection)
            throws SQLException {

        int total = 0;
        String sql = "SELECT date FROM consultations";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                if (isWithinSelectedDates(rs.getString("date"))) {
                    total++;
                }
            }
        }

        return total;
    }

    private int getAppointmentCount(Connection connection)
            throws SQLException {

        int total = 0;
        String sql = "SELECT time FROM appointments";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                if (isWithinSelectedDates(rs.getString("time"))) {
                    total++;
                }
            }
        }

        return total;
    }

    private void loadConditions(Connection connection)
            throws SQLException {

        conditionModel.setRowCount(0);

        Map<String, Integer> conditionCounts = new LinkedHashMap<>();
        String sql = "SELECT date, reason FROM consultations";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String date = rs.getString("date");

                if (!isWithinSelectedDates(date)) {
                    continue;
                }

                String condition = rs.getString("reason");

                if (condition == null || condition.trim().isEmpty()) {
                    condition = "Others";
                }

                condition = condition.trim();

                conditionCounts.put(
                        condition,
                        conditionCounts.getOrDefault(condition, 0) + 1);
            }
        }

        ArrayList<Map.Entry<String, Integer>> sorted =
                new ArrayList<>(conditionCounts.entrySet());

        Collections.sort(
                sorted,
                (a, b) -> Integer.compare(b.getValue(), a.getValue()));

        LinkedHashMap<String, Integer> chartData = new LinkedHashMap<>();

        for (Map.Entry<String, Integer> entry : sorted) {
            conditionModel.addRow(new Object[]{
                entry.getKey(),
                entry.getValue()
            });

            chartData.put(entry.getKey(), entry.getValue());
        }

        pieChart.setData(chartData);
    }

    private void loadHistory(Connection connection)
            throws SQLException {

        historyModel.setRowCount(0);

        String consultationSQL =
                "SELECT date, reason, status FROM consultations ORDER BY id DESC";

        try (PreparedStatement ps =
                     connection.prepareStatement(consultationSQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String date = rs.getString("date");

                if (!isWithinSelectedDates(date)) {
                    continue;
                }

                String condition = rs.getString("reason");
                String status = rs.getString("status");

                if (condition == null || condition.trim().isEmpty()) {
                    condition = "Others";
                }

                if (status == null || status.trim().isEmpty()) {
                    status = "Completed";
                }

                historyModel.addRow(new Object[]{
                    formatHistoryDate(date),
                    condition,
                    "Consultation",
                    status
                });
            }
        }

        String appointmentSQL =
                "SELECT time, patient_name, purpose, status "
                + "FROM appointments ORDER BY id DESC";

        try (PreparedStatement ps =
                     connection.prepareStatement(appointmentSQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String date = rs.getString("time");

                if (!isWithinSelectedDates(date)) {
                    continue;
                }

                String patientName = rs.getString("patient_name");
                String purpose = rs.getString("purpose");
                String status = rs.getString("status");

                if (purpose == null || purpose.trim().isEmpty()) {
                    purpose = "Appointment";
                }

                if (patientName != null && !patientName.trim().isEmpty()) {
                    purpose += " - " + patientName.trim();
                }

                if (status == null || status.trim().isEmpty()) {
                    status = "Pending";
                }

                historyModel.addRow(new Object[]{
                    formatHistoryDate(date),
                    purpose,
                    "Appointment",
                    status
                });
            }
        }

        String healthSQL =
                "SELECT date, status FROM health_records ORDER BY id DESC";

        try (PreparedStatement ps =
                     connection.prepareStatement(healthSQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String date = rs.getString("date");

                if (!isWithinSelectedDates(date)) {
                    continue;
                }

                String status = rs.getString("status");

                if (status == null || status.trim().isEmpty()) {
                    status = "Active";
                }

                historyModel.addRow(new Object[]{
                    formatHistoryDate(date),
                    "Health Record",
                    "Health Record",
                    status
                });
            }
        }
    }

    private boolean isWithinSelectedDates(String dateText) {

        if (selectedFromDate == null && selectedToDate == null) {
            return true;
        }

        java.util.Date recordDate = parseRecordDate(dateText);

        if (recordDate == null) {
            return false;
        }

        if (selectedFromDate != null
                && recordDate.before(startOfDay(selectedFromDate))) {
            return false;
        }

        if (selectedToDate != null
                && recordDate.after(endOfDay(selectedToDate))) {
            return false;
        }

        return true;
    }

    // Accepts stored dates with or without time for filtering.
    // Displayed dates are always converted to yyyy-MM-dd.
    private java.util.Date parseRecordDate(String dateText) {

        if (dateText == null || dateText.trim().isEmpty()) {
            return null;
        }

        String value = dateText.trim();

        String[] formats = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd",
            "MM/dd/yyyy hh:mm a",
            "MM/dd/yyyy",
            "MMMM d, yyyy",
            "MMM d, yyyy"
        };

        for (String format : formats) {

            try {
                SimpleDateFormat sdf =
                        new SimpleDateFormat(format, Locale.ENGLISH);

                sdf.setLenient(false);

                java.text.ParsePosition position =
                        new java.text.ParsePosition(0);

                java.util.Date parsed = sdf.parse(value, position);

                if (parsed != null
                        && position.getIndex() == value.length()) {
                    return parsed;
                }

            } catch (IllegalArgumentException ignored) {
                // Try the next supported date format.
            }
        }

        return null;
    }

    private java.util.Date startOfDay(java.util.Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private java.util.Date endOfDay(java.util.Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTime();
    }

    // Always displays date only, without time.
    private String formatHistoryDate(String dateText) {

        if (dateText == null || dateText.trim().isEmpty()) {
            return "-";
        }

        java.util.Date date = parseRecordDate(dateText);

        if (date != null) {
            return displayDateFormat.format(date);
        }

        // Fallback for SQL date-time text beginning with yyyy-MM-dd.
        String value = dateText.trim();

        if (value.matches("^\\d{4}-\\d{2}-\\d{2}.*$")) {
            return value.substring(0, 10);
        }

        return value;
    }

    private void showDatabaseError() {

        JOptionPane.showMessageDialog(
                this,
                "Unable to read reports from database.\n"
                + "Please make sure XAMPP MariaDB is running.",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private static class PieChartPanel extends JPanel {

        private Map<String, Integer> data = new LinkedHashMap<>();

        private final Color[] colors = {
            new Color(46, 160, 67),
            new Color(55, 90, 200),
            new Color(235, 170, 45),
            new Color(130, 85, 180),
            new Color(205, 65, 65),
            new Color(35, 145, 145),
            new Color(120, 120, 120),
            new Color(230, 125, 45)
        };

        public PieChartPanel() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(400, 280));
        }

        public void setData(Map<String, Integer> newData) {
            data = new LinkedHashMap<>(newData);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            if (data.isEmpty()) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.PLAIN, 14));

                String message = "No data available";
                FontMetrics metrics = g.getFontMetrics();

                int x = (getWidth() - metrics.stringWidth(message)) / 2;
                int y = getHeight() / 2;

                g.drawString(message, Math.max(5, x), y);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int total = 0;

            for (Integer value : data.values()) {
                if (value != null && value > 0) {
                    total += value;
                }
            }

            if (total <= 0) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Arial", Font.PLAIN, 14));
                g2.drawString(
                        "No data available",
                        Math.max(5, getWidth() / 2 - 65),
                        getHeight() / 2);
                g2.dispose();
                return;
            }

            int size = Math.max(
                    1,
                    Math.min(getWidth() / 2, getHeight() - 60));

            int x = 25;
            int y = (getHeight() - size) / 2;
            int startAngle = 0;
            int index = 0;

            for (Map.Entry<String, Integer> entry : data.entrySet()) {

                int value = entry.getValue();

                if (value <= 0) {
                    continue;
                }

                int angle = (int) Math.round(360.0 * value / total);

                g2.setColor(colors[index % colors.length]);
                g2.fillArc(x, y, size, size, startAngle, angle);

                startAngle += angle;
                index++;
            }

            // LEGEND
            int legendX = x + size + 25;
            int legendY = 35;
            index = 0;

            int availableHeight = Math.max(1, getHeight() - 45);
            int maxLegendRows = Math.max(1, availableHeight / 22);

            for (Map.Entry<String, Integer> entry : data.entrySet()) {

                if (index >= maxLegendRows) {
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("Arial", Font.PLAIN, 11));
                    g2.drawString("...", legendX, legendY);
                    break;
                }

                Color color = colors[index % colors.length];

                g2.setColor(color);
                g2.fillRect(legendX, legendY - 10, 12, 12);

                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Arial", Font.PLAIN, 11));

                String legendText =
                        entry.getKey() + " (" + entry.getValue() + ")";

                int maxTextWidth = Math.max(
                        40, getWidth() - legendX - 25);

                FontMetrics metrics = g2.getFontMetrics();

                while (metrics.stringWidth(legendText) > maxTextWidth
                        && legendText.length() > 4) {

                    legendText = legendText.substring(
                            0, legendText.length() - 4) + "...";
                }

                g2.drawString(legendText, legendX + 18, legendY);

                legendY += 22;
                index++;
            }

            g2.dispose();
        }
    }
}
