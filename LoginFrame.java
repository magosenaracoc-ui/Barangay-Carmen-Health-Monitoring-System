package barangaycarmenhealthmonitoringsystem;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private String selectedRole = "Admin";

    private JButton adminButton;
    private JButton staffButton;
    private JButton eyeButton;

    // ROLE COLORS
    private final Color ADMIN_GREEN = new Color(45, 145, 45);
    private final Color STAFF_BLUE = new Color(55, 90, 200);

    private final Color LIGHT_GREEN = new Color(220, 245, 220);
    private final Color LIGHT_BLUE = new Color(220, 230, 255);

    // LOGIN SCREEN ANIMATION
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;

    private int animationFrame = 0;

    public LoginFrame() {

        setTitle("Barangay Carmen Health Monitoring System");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // =====================================================
        // MAIN PANEL
        // =====================================================

        mainPanel = new JPanel(null);
        mainPanel.setBackground(Color.WHITE);

        // =====================================================
        // LEFT GREEN SIDE
        // =====================================================

        leftPanel = new JPanel();
        leftPanel.setBackground(UIUtils.GREEN);
        leftPanel.setLayout(null);

        leftPanel.setBounds(
                -390,
                0,
                390,
                550
        );

        // =====================================================
        // CDO LOGO
        // =====================================================

        JLabel cdoLogo = new JLabel();

        try {

            ImageIcon icon = new ImageIcon(
                    getClass().getResource(
                            "/barangaycarmenhealthmonitoringsystem/cdy.png"
                    )
            );

            Image image = icon.getImage().getScaledInstance(
                    58,
                    58,
                    Image.SCALE_SMOOTH
            );

            cdoLogo.setIcon(
                    new ImageIcon(image)
            );

        } catch (Exception e) {

            System.out.println(
                    "Unable to load cdy.png"
            );
        }

        cdoLogo.setBounds(
                18,
                18,
                65,
                65
        );

        leftPanel.add(cdoLogo);

        // =====================================================
        // CARMEN LOGO
        // =====================================================

        JLabel carmenLogo = new JLabel();

        try {

            ImageIcon icon = new ImageIcon(
                    getClass().getResource(
                            "/barangaycarmenhealthmonitoringsystem/bg.png"
                    )
            );

            Image image = icon.getImage().getScaledInstance(
                    58,
                    58,
                    Image.SCALE_SMOOTH
            );

            carmenLogo.setIcon(
                    new ImageIcon(image)
            );

        } catch (Exception e) {

            System.out.println(
                    "Unable to load bg.png"
            );
        }

        carmenLogo.setBounds(
                307,
                18,
                65,
                65
        );

        leftPanel.add(carmenLogo);

        // =====================================================
        // WELCOME TEXT
        // =====================================================

        JLabel welcome = new JLabel(
                "<html>"
                + "WELCOME TO OUR<br>"
                + "HEALTH<br>"
                + "MONITORING<br>"
                + "SYSTEM"
                + "</html>"
        );

        welcome.setForeground(Color.WHITE);

        welcome.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        38
                )
        );

        welcome.setBounds(
                8,
                60,
                370,
                245
        );

        leftPanel.add(welcome);

        // =====================================================
        // SUBTITLE
        // =====================================================

        JLabel healthy = new JLabel(
                "Towards a Healthier Community"
        );

        healthy.setForeground(Color.WHITE);

        healthy.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        20
                )
        );

        healthy.setBounds(
                8,
                290,
                370,
                30
        );

        leftPanel.add(healthy);

        // =====================================================
        // DESCRIPTION
        // =====================================================

        JLabel description = new JLabel(
                "<html>"
                + "Access essential health services<br>"
                + "and community health information through one<br>"
                + "convenient system."
                + "</html>"
        );

        description.setForeground(Color.WHITE);

        description.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        15
                )
        );

        description.setBounds(
                8,
                330,
                370,
                70
        );

        leftPanel.add(description);

        // =====================================================
        // RIGHT WHITE SIDE
        // =====================================================

        rightPanel = new JPanel(null);
        rightPanel.setBackground(Color.WHITE);

        rightPanel.setBounds(
                900,
                0,
                510,
                550
        );

        // =====================================================
        // CARMEN LOGO
        // =====================================================

        JLabel logoLabel = new JLabel();

        try {

            ImageIcon logoIcon = new ImageIcon(
                    getClass().getResource(
                            "/barangaycarmenhealthmonitoringsystem/bg.png"
                    )
            );

            Image logoImage =
                    logoIcon.getImage().getScaledInstance(
                            90,
                            90,
                            Image.SCALE_SMOOTH
                    );

            logoLabel.setIcon(
                    new ImageIcon(logoImage)
            );

        } catch (Exception e) {

            System.out.println(
                    "Unable to load bg.png"
            );
        }

        logoLabel.setBounds(
                210,
                18,
                90,
                90
        );

        rightPanel.add(logoLabel);

        // =====================================================
        // TITLE
        // =====================================================

        JLabel title = new JLabel(
                "BARANGAY CARMEN",
                SwingConstants.CENTER
        );

        title.setForeground(
                UIUtils.GREEN
        );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        25
                )
        );

        title.setBounds(
                50,
                105,
                410,
                35
        );

        rightPanel.add(title);

        // =====================================================
        // SUB TITLE
        // =====================================================

        JLabel sub = new JLabel(
                "HEALTH MONITORING SYSTEM",
                SwingConstants.CENTER
        );

        sub.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        17
                )
        );

        sub.setBounds(
                50,
                138,
                410,
                28
        );

        rightPanel.add(sub);

        // =====================================================
        // SIGN IN TEXT
        // =====================================================

        JLabel loginText = new JLabel(
                "SIGN IN TO YOUR ACCOUNT",
                SwingConstants.CENTER
        );

        loginText.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        loginText.setBounds(
                50,
                180,
                410,
                25
        );

        rightPanel.add(loginText);

        // =====================================================
        // USERNAME LABEL
        // =====================================================

        JLabel userLabel = new JLabel(
                "Username"
        );

        userLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        userLabel.setBounds(
                82,
                212,
                330,
                20
        );

        rightPanel.add(userLabel);

        // =====================================================
        // USERNAME FIELD
        // =====================================================

        usernameField = new JTextField();

        usernameField.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        usernameField.setBounds(
                82,
                232,
                330,
                40
        );

        rightPanel.add(usernameField);

        // =====================================================
        // PASSWORD LABEL
        // =====================================================

        JLabel passLabel = new JLabel(
                "Password"
        );

        passLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        passLabel.setBounds(
                82,
                279,
                330,
                20
        );

        rightPanel.add(passLabel);

        // =====================================================
        // PASSWORD PANEL
        // =====================================================

        JPanel passwordPanel = new JPanel(
                new BorderLayout()
        );

        passwordPanel.setBackground(
                Color.WHITE
        );

        passwordPanel.setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                130,
                                130,
                                130
                        )
                )
        );

        passwordPanel.setBounds(
                82,
                299,
                330,
                40
        );

        // =====================================================
        // PASSWORD FIELD
        // =====================================================

        passwordField = new JPasswordField();

        passwordField.setEchoChar(
                '\u2022'
        );

        passwordField.setBorder(
                BorderFactory.createEmptyBorder(
                        0,
                        10,
                        0,
                        5
                )
        );

        passwordField.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        passwordPanel.add(
                passwordField,
                BorderLayout.CENTER
        );

        // =====================================================
        // EYE BUTTON
        // =====================================================

        eyeButton = new JButton(
                "👁"
        );

        eyeButton.setFont(
                new Font(
                        "Segoe UI Emoji",
                        Font.PLAIN,
                        16
                )
        );

        eyeButton.setPreferredSize(
                new Dimension(
                        38,
                        38
                )
        );

        eyeButton.setFocusPainted(false);

        eyeButton.setBorder(
                BorderFactory.createEmptyBorder()
        );

        eyeButton.setContentAreaFilled(false);

        eyeButton.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        passwordPanel.add(
                eyeButton,
                BorderLayout.EAST
        );

        rightPanel.add(
                passwordPanel
        );

        // =====================================================
        // SHOW / HIDE PASSWORD
        // =====================================================

        eyeButton.addActionListener(e -> {

            if (passwordField.getEchoChar() == 0) {

                passwordField.setEchoChar(
                        '\u2022'
                );

            } else {

                passwordField.setEchoChar(
                        (char) 0
                );
            }

            passwordField.requestFocusInWindow();
        });

        // =====================================================
        // ROLE BUTTONS
        // =====================================================

        adminButton =
                new JButton("ADMIN");

        staffButton =
                new JButton("STAFF");

        adminButton.setBounds(
                94,
                360,
                150,
                55
        );

        staffButton.setBounds(
                249,
                360,
                150,
                55
        );

        styleAdminButton();
        styleStaffButton();

        adminButton.addActionListener(e -> {

            selectedRole = "Admin";

            styleAdminButton();
            styleStaffButton();
        });

        staffButton.addActionListener(e -> {

            selectedRole = "Staff";

            styleAdminButton();
            styleStaffButton();
        });

        rightPanel.add(
                adminButton
        );

        rightPanel.add(
                staffButton
        );

        // =====================================================
        // LOGIN BUTTON
        // =====================================================

        JButton login =
                UIUtils.button("LOGIN");

        login.setBounds(
                82,
                425,
                330,
                45
        );

        login.addActionListener(
                e -> login()
        );

        rightPanel.add(
                login
        );

        // =====================================================
        // ADD PANELS
        // =====================================================

        mainPanel.add(
                leftPanel
        );

        mainPanel.add(
                rightPanel
        );

        add(
                mainPanel
        );

        // =====================================================
        // MAKE FRAME VISIBLE
        // =====================================================

        setVisible(true);

        // =====================================================
        // START LOGIN SCREEN ANIMATION
        // =====================================================

        SwingUtilities.invokeLater(
                this::startAnimation
        );
    }

    // =========================================================
    // LOGIN SCREEN SLIDE ANIMATION
    // =========================================================

    private void startAnimation() {

        animationFrame = 0;

        Timer timer =
                new Timer(
                        12,
                        null
                );

        timer.addActionListener(e -> {

            animationFrame++;

            double progress =
                    animationFrame / 45.0;

            if (progress >= 1.0) {

                progress = 1.0;

                timer.stop();

                leftPanel.setBounds(
                        0,
                        0,
                        390,
                        550
                );

                rightPanel.setBounds(
                        390,
                        0,
                        510,
                        550
                );

                mainPanel.repaint();

                usernameField.requestFocusInWindow();

                return;
            }

            double eased =
                    1.0
                    - Math.pow(
                            1.0 - progress,
                            3
                    );

            // LEFT PANEL
            int leftX =
                    (int) (
                            -390
                            + (390 * eased)
                    );

            // RIGHT PANEL
            int rightX =
                    (int) (
                            900
                            - (510 * eased)
                    );

            leftPanel.setBounds(
                    leftX,
                    0,
                    390,
                    550
            );

            rightPanel.setBounds(
                    rightX,
                    0,
                    510,
                    550
            );

            mainPanel.repaint();
        });

        timer.start();
    }

    // =========================================================
    // ADMIN BUTTON STYLE
    // =========================================================

    private void styleAdminButton() {

        if (selectedRole.equals("Admin")) {

            adminButton.setBackground(
                    ADMIN_GREEN
            );

            adminButton.setForeground(
                    Color.WHITE
            );

        } else {

            adminButton.setBackground(
                    LIGHT_GREEN
            );

            adminButton.setForeground(
                    Color.DARK_GRAY
            );
        }

        adminButton.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        adminButton.setFocusPainted(false);
        adminButton.setOpaque(true);
        adminButton.setBorderPainted(false);
    }

    // =========================================================
    // STAFF BUTTON STYLE
    // =========================================================

    private void styleStaffButton() {

        if (selectedRole.equals("Staff")) {

            staffButton.setBackground(
                    STAFF_BLUE
            );

            staffButton.setForeground(
                    Color.WHITE
            );

        } else {

            staffButton.setBackground(
                    LIGHT_BLUE
            );

            staffButton.setForeground(
                    Color.DARK_GRAY
            );
        }

        staffButton.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        staffButton.setFocusPainted(false);
        staffButton.setOpaque(true);
        staffButton.setBorderPainted(false);
    }

    // =========================================================
    // LOGIN FUNCTION - DATABASE
    // =========================================================

    private void login() {

        String username =
                usernameField.getText().trim();

        String password =
                new String(
                        passwordField.getPassword()
                );

        if (username.isEmpty()
                || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your username and password.",
                    "Login Failed",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String sql =
                "SELECT username, role FROM users "
                + "WHERE username = ? "
                + "AND password = ? "
                + "AND role = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    username
            );

            statement.setString(
                    2,
                    password
            );

            statement.setString(
                    3,
                    selectedRole
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (result.next()) {

                    String databaseUsername =
                            result.getString(
                                    "username"
                            );

                    String databaseRole =
                            result.getString(
                                    "role"
                            );

                    // =================================================
                    // SUCCESSFUL LOGIN
                    // =================================================

                    MainFrame mainFrame =
                            new MainFrame(
                                    databaseUsername,
                                    databaseRole
                            );

                    // Get the normal position of MainFrame
                    Point normalPosition =
                            mainFrame.getLocation();

                    // Start the Dashboard slightly below
                    mainFrame.setLocation(
                            normalPosition.x,
                            normalPosition.y + 100
                    );

                    // Show Dashboard
                    mainFrame.setVisible(true);

                    // =================================================
                    // DASHBOARD ENTRANCE ANIMATION
                    // =================================================

                    Timer dashboardTimer =
                            new Timer(
                                    10,
                                    null
                            );

                    final int[] frame =
                            {0};

                    final int totalFrames =
                            40;

                    final int startY =
                            normalPosition.y + 100;

                    final int endY =
                            normalPosition.y;

                    dashboardTimer.addActionListener(e -> {

                        frame[0]++;

                        double progress =
                                frame[0]
                                / (double) totalFrames;

                        if (progress > 1.0) {

                            progress = 1.0;
                        }

                        // Smooth ease-out
                        double eased =
                                1.0
                                - Math.pow(
                                        1.0 - progress,
                                        3
                                );

                        int currentY =
                                (int) (
                                        startY
                                        + (
                                                (endY - startY)
                                                * eased
                                        )
                                );

                        mainFrame.setLocation(
                                normalPosition.x,
                                currentY
                        );

                        if (frame[0] >= totalFrames) {

                            mainFrame.setLocation(
                                    normalPosition
                            );

                            dashboardTimer.stop();
                        }
                    });

                    dashboardTimer.start();

                    // =================================================
                    // CLOSE LOGIN AFTER DASHBOARD APPEARS
                    // =================================================

                    Timer closeTimer =
                            new Timer(
                                    120,
                                    null
                            );

                    closeTimer.addActionListener(e -> {

                        closeTimer.stop();

                        dispose();
                    });

                    closeTimer.setRepeats(false);
                    closeTimer.start();

                    return;
                }
            }

            // =================================================
            // INVALID LOGIN
            // =================================================

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid username, password, or user role.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to connect to the database.\n"
                    + "Please make sure XAMPP MySQL is running.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}
