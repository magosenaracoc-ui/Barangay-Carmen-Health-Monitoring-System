package barangaycarmenhealthmonitoringsystem;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class UIUtils {

    public static final Color GREEN = new Color(0, 105, 75);
    public static final Color DARK_GREEN = new Color(0, 83, 59);
    public static final Color LIGHT_GREEN = new Color(232, 246, 239);
    public static final Color BLUE = new Color(225, 237, 250);
    public static final Color YELLOW = new Color(255, 246, 214);
    public static final Color PURPLE = new Color(239, 232, 249);

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(GREEN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return b;
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(new Color(30, 60, 50));
        return label;
    }

    public static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return label;
    }

    public static JTextField field(String text) {
        JTextField field = new JTextField(text);
        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(5, 7, 5, 7)
                )
        );
        return field;
    }
}