package org.example.Interface;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.example.Admin.AdminSystemSettings;
import org.example.Users.Resident;
import org.example.Users.BarangayStaff;
import org.example.UserDataManager;
import org.example.utils.ResourceUtils;

import java.time.LocalDate;
import java.time.ZoneId;


public class Register {

    // ---------------------- Custom UI Utilities ----------------------
    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 10, 6, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 10;
            insets.top = insets.bottom = 6;
            return insets;
        }
    }
    static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        private boolean showingPlaceholder = true;

        PlaceholderTextField(String placeholder) {
            super(placeholder);
            this.placeholder = placeholder;
            setForeground(Color.GRAY);
            setOpaque(false);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.BLACK);
                        showingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        public String getText() {
            return showingPlaceholder ? "" : super.getText();
        }
    }

    static class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;
        private boolean showing = true;

        PlaceholderPasswordField(String placeholder) {
            super(placeholder);
            this.placeholder = placeholder;
            setForeground(Color.GRAY);
            setEchoChar((char) 0);
            setOpaque(false);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showing) {
                        setText("");
                        setForeground(Color.BLACK);
                        setEchoChar('•');
                        showing = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getPassword().length == 0) {
                        setEchoChar((char) 0);
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showing = true;
                    }
                }
            });
        }

        @Override
        public char[] getPassword() {
            return showing ? new char[0] : super.getPassword();
        }
    }

    static class RoundedComboBoxUI extends BasicComboBoxUI {
        private final int radius;

        RoundedComboBoxUI(int radius) { this.radius = radius; }

        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton("\u25BE"); // small arrow char
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setOpaque(false);
            b.setFocusable(false);
            return b;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // do nothing
        }
    }

    // ---------------------- Input Validation Utilities ----------------------

    static class NameDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);

            if (isValidName(newText)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

            if (isValidName(newText)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValidName(String text) {
            if (text.isEmpty()) return true;

            // Allow only letters, spaces, hyphens, and apostrophes
            // Minimum 2 characters for actual names (but allow empty for placeholder)
            if (!text.matches("^[a-zA-Z\\s\\-'.]*$")) {
                return false;
            }

            return true;
        }
    }

    static class IDDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);

            if (isValidID(newText)) {
                super.insertString(fb, offset, string, attr);
                formatIDField(fb);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

            if (isValidID(newText)) {
                super.replace(fb, offset, length, text, attrs);
                formatIDField(fb);
            }
        }

        private boolean isValidID(String text) {
            if (text.isEmpty()) return true;

            // Remove existing dashes for validation
            String cleanText = text.replace("-", "");

            // Check if all characters are digits and total digits don't exceed 12 (14 with dashes)
            if (!cleanText.matches("\\d{0,12}")) {
                return false;
            }

            return true;
        }

        private void formatIDField(FilterBypass fb) throws BadLocationException {
            String text = fb.getDocument().getText(0, fb.getDocument().getLength());
            String cleanText = text.replace("-", "");

            if (cleanText.length() > 12) {
                cleanText = cleanText.substring(0, 12);
            }

            // Format as XXXX-XXXX-XXXX
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < cleanText.length(); i++) {
                if (i == 4 || i == 8) {
                    formatted.append("-");
                }
                formatted.append(cleanText.charAt(i));
            }

            // Only update if formatting changed
            if (!text.equals(formatted.toString())) {
                super.replace(fb, 0, fb.getDocument().getLength(), formatted.toString(), null);
            }
        }
    }

    static class PhoneDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);

            if (isValidPhone(newText)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

            if (isValidPhone(newText)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValidPhone(String text) {
            if (text.isEmpty()) return true;

            // Check if all characters are digits
            if (!text.matches("\\d*")) {
                return false;
            }

            // Check length (max 11)
            if (text.length() > 11) {
                return false;
            }

            // Check if starts with "09" (only if we have at least 2 characters)
            if (text.length() >= 2 && !text.startsWith("09")) {
                return false;
            }

            return true;
        }
    }

    static class EmailDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);

            if (isValidEmail(newText)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

            if (isValidEmail(newText)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValidEmail(String text) {
            if (text.isEmpty()) return true;

            // Basic email validation - must end with @gmail.com
            if (text.contains("@")) {
                return text.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$");
            }

            // Allow partial input before @
            return text.matches("^[a-zA-Z0-9._%+-]*$");
        }
    }

    // ---------------------- Password Strength Checker ----------------------
    private static boolean isStrongPassword(char[] password) {
        if (password.length < 8) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }

    private static String getPasswordStrengthMessage(char[] password) {
        if (password.length == 0) {
            return "Enter a password";
        }

        if (password.length < 8) {
            return "Password must be at least 8 characters";
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }

        StringBuilder message = new StringBuilder("Password strength: ");
        if (hasUpper && hasLower && hasDigit) {
            message.append("Strong ✓");
        } else {
            message.append("Weak - needs:");
            if (!hasUpper) message.append(" uppercase letter,");
            if (!hasLower) message.append(" lowercase letter,");
            if (!hasDigit) message.append(" number,");
            // Remove trailing comma
            message.setLength(message.length() - 1);
        }

        return message.toString();
    }

    // ---------------------- Validation Function ----------------------
    private static boolean validateFields(JFrame frame, PlaceholderTextField nameField,
                                          PlaceholderTextField lNameField, PlaceholderTextField userField,
                                          PlaceholderPasswordField passField, PlaceholderTextField idField,
                                          JComboBox<String> posCombo, JComboBox<String> sexCombo,
                                          PlaceholderTextField phoneField, PlaceholderTextField emailField,
                                          JComboBox<String> statusCombo, PlaceholderTextField permField,
                                          JTextField photoText) {

        StringBuilder errorMessages = new StringBuilder();

        // Check each required field

        String firstName = nameField.getText().trim();
        if (firstName.isEmpty() || firstName.equals("First name")) {
            errorMessages.append("• First Name is required\n");
            nameField.setBorder(new RoundedBorder(14, Color.RED));
        } else if (firstName.length() < 2) {
            errorMessages.append("• First Name must be at least 2 characters\n");
            nameField.setBorder(new RoundedBorder(14, Color.RED));
        } else if (!firstName.matches("^[a-zA-Z\\s\\-'.]{2,}$")) {
            errorMessages.append("• First Name contains invalid characters\n");
            nameField.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            nameField.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        String lastName = lNameField.getText().trim();
        if (lastName.isEmpty() || lastName.equals("Last name")) {
            errorMessages.append("• Last Name is required\n");
            lNameField.setBorder(new RoundedBorder(14, Color.RED));
        } else if (lastName.length() < 2) {
            errorMessages.append("• Last Name must be at least 2 characters\n");
            lNameField.setBorder(new RoundedBorder(14, Color.RED));
        } else if (!lastName.matches("^[a-zA-Z\\s\\-'.]{2,}$")) {
            errorMessages.append("• Last Name contains invalid characters\n");
            lNameField.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            lNameField.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        if (userField.getText().trim().isEmpty() || userField.getText().equals("Username")) {
            errorMessages.append("• Username is required\n");
            userField.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            userField.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        char[] password = passField.getPassword();
        if (password.length == 0 || new String(password).equals("Enter password")) {
            errorMessages.append("• Password is required\n");
            passField.setBorder(new RoundedBorder(14, Color.RED));
        } else if (!isStrongPassword(password)) {
            errorMessages.append("• ").append(getPasswordStrengthMessage(password)).append("\n");
            passField.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            passField.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        if (idField.getText().trim().isEmpty() || idField.getText().equals("XXXX-XXXX-XXXX")) {
            errorMessages.append("• ID Number is required\n");
            idField.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            idField.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        if (posCombo.getSelectedIndex() == 0) { // "Select Position"
            errorMessages.append("• Position is required\n");
            posCombo.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            posCombo.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        if (sexCombo.getSelectedIndex() == 0) { // "Select"
            errorMessages.append("• Sex is required\n");
            sexCombo.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            sexCombo.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        String phone = phoneField.getText().trim();
        if (phone.isEmpty() || phone.equals("09XXXXXXXXX")) {
            errorMessages.append("• Phone Number is required\n");
            phoneField.setBorder(new RoundedBorder(14, Color.RED));
        } else if (phone.length() != 11) {
            errorMessages.append("• Phone Number must be 11 digits\n");
            phoneField.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            phoneField.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        String email = emailField.getText().trim();
        if (email.isEmpty() || email.equals("example@gmail.com")) {
            errorMessages.append("• Email Address is required\n");
            emailField.setBorder(new RoundedBorder(14, Color.RED));
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            errorMessages.append("• Email must be a valid @gmail.com address\n");
            emailField.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            emailField.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        if (statusCombo.getSelectedIndex() == -1) {
            errorMessages.append("• Status is required\n");
            statusCombo.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            statusCombo.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        if (permField.getText().trim().isEmpty() || permField.getText().equals("Complete address")) {
            errorMessages.append("• Permanent Address is required\n");
            permField.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            permField.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        if (photoText.getText().equals("No file chosen")) {
            errorMessages.append("• Photo ID is required\n");
            photoText.setBorder(new RoundedBorder(14, Color.RED));
        } else {
            photoText.setBorder(new RoundedBorder(14, Color.GRAY));
        }

        // If there are errors, show error message
        if (errorMessages.length() > 0) {
            JOptionPane.showMessageDialog(frame,
                    "Please fix the following issues:\n\n" + errorMessages.toString(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    // ---------------------- Overview Panel Function ----------------------
    private static JPanel createOverviewPanel(PlaceholderTextField nameField,
                                              PlaceholderTextField lNameField,
                                              PlaceholderTextField userField,
                                              PlaceholderPasswordField passField,
                                              PlaceholderTextField idField,
                                              JComboBox<String> posCombo,
                                              JSpinner dobSpinner,
                                              JComboBox<String> ageCombo,
                                              JComboBox<String> sexCombo,
                                              PlaceholderTextField phoneField,
                                              PlaceholderTextField emailField,
                                              JComboBox<String> statusCombo,
                                              PlaceholderTextField permField,
                                              JTextField photoText) {

        JPanel overviewPanel = new JPanel(new BorderLayout(10, 10));
        overviewPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Registration Overview - Required Fields");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        // Create two columns for the overview
        JPanel columnsPanel = new JPanel(new GridLayout(0, 2, 20, 8));
        columnsPanel.setBackground(Color.WHITE);

        // Format date for display
        Date birthDate = ( Date) dobSpinner.getValue();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String formattedDate = dateFormat.format(birthDate);

        // Left Column
        addOverviewRow(columnsPanel, "First Name:", nameField.getText(), "First name");
        addOverviewRow(columnsPanel, "Last Name:", lNameField.getText(), "Last name");
        addOverviewRow(columnsPanel, "Username:", userField.getText(), "Username");
        addOverviewRow(columnsPanel, "Password:", "••••••••", "Enter password");
        addOverviewRow(columnsPanel, "ID Number:", idField.getText(), "XXXX-XXXX-XXXX");
        addOverviewRow(columnsPanel, "Position:", posCombo.getSelectedItem().toString(), "Select Position");
        addOverviewRow(columnsPanel, "Date of Birth:", formattedDate, "");

        // Right Column
        addOverviewRow(columnsPanel, "Age:", ageCombo.getSelectedItem().toString(), "");
        addOverviewRow(columnsPanel, "Sex:", sexCombo.getSelectedItem().toString(), "Select");
        addOverviewRow(columnsPanel, "Phone Number:", phoneField.getText(), "09XXXXXXXXX");
        addOverviewRow(columnsPanel, "Email Address:", emailField.getText(), "example@gmail.com");
        addOverviewRow(columnsPanel, "Status:", statusCombo.getSelectedItem().toString(), "");
        addOverviewRow(columnsPanel, "Permanent Address:", permField.getText(), "Complete address");
        addOverviewRow(columnsPanel, "Photo ID:", photoText.getText(), "No file chosen");

        // Add components to main panel
        overviewPanel.add(titleLabel, BorderLayout.NORTH);
        overviewPanel.add(columnsPanel, BorderLayout.CENTER);

        // Add note about required fields
        JLabel noteLabel = new JLabel("Note: All fields marked with * are required");
        noteLabel.setFont(new Font("Dialog", Font.ITALIC, 12));
        noteLabel.setForeground(Color.RED);
        noteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noteLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        overviewPanel.add(noteLabel, BorderLayout.SOUTH);

        return overviewPanel;
    }

    private static void addOverviewRow(JPanel panel, String label, String value, String placeholder) {
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Dialog", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(value.isEmpty() || value.equals(placeholder) ? "Not filled" : value);
        valueLabel.setFont(new Font("Dialog", Font.PLAIN, 12));

        // Highlight missing required fields in red
        if (value.isEmpty() || value.equals(placeholder)) {
            valueLabel.setForeground(Color.RED);
            fieldLabel.setText(fieldLabel.getText() + " *");
            fieldLabel.setForeground(Color.RED);
        } else {
            valueLabel.setForeground(Color.BLACK);
            fieldLabel.setForeground(Color.BLACK);
        }

        panel.add(fieldLabel);
        panel.add(valueLabel);
    }

    // ---------------------- Helper to open Main (Login) ----------------------
    private static void openMainWindow(Window current) {
        try {
            Class<?> mainClass = Class.forName("org.example.Interface.Main");
            Method main = mainClass.getMethod("main", String[].class);
            if (current != null) current.dispose();
            main.invoke(null, (Object) new String[]{});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Could not open Login (interfaces.Main).\nMake sure Main.java exists in interfaces package.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    static PlaceholderTextField nameField = new PlaceholderTextField("First name");

    // ---------------------- Main UI ----------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Register - Serbisyong Barangay");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new GridBagLayout());

            // Set frame icon (optional)
            try {
                ImageIcon icon = new ImageIcon(ResourceUtils.getResourceAsBytes("logo.jpg"));
                frame.setIconImage(icon.getImage());
            } catch (Exception e) {
                System.out.println("Icon not found");
            }

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridy = 0;
            gbc.weighty = 1.0;

            // LEFT PANEL (black) - ~65%
            gbc.gridx = 0;
            gbc.weightx = 0.65;
            JPanel left = new JPanel(new GridBagLayout());
            left.setBackground(Color.BLACK);

            GridBagConstraints l = new GridBagConstraints();
            l.gridx = 0;
            l.insets = new Insets(10, 40, 10, 40);
            l.anchor = GridBagConstraints.CENTER;

            // Logo
            l.gridy = 0;
            try {
                ImageIcon logo = new ImageIcon(ResourceUtils.getResourceAsBytes("roundLogo.jpg"));
                Image img = logo.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(img));
                left.add(logoLabel, l);
            } catch (Exception ex) {
                JLabel logoLabel = new JLabel("[LOGO]");
                logoLabel.setForeground(Color.WHITE);
                logoLabel.setFont(new Font("Dialog", Font.BOLD, 22));
                left.add(logoLabel, l);
            }

            // Title
            l.gridy = 1;
            JLabel title = new JLabel("Serbisyong Barangay");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Dialog", Font.BOLD, 42));
            left.add(title, l);

            // Subtitle
            l.gridy = 2;
            JLabel subtitle = new JLabel("Documentary Request System");
            subtitle.setForeground(Color.WHITE);
            subtitle.setFont(new Font("Dialog", Font.PLAIN, 20));
            left.add(subtitle, l);

            frame.add(left, gbc);

            // RIGHT PANEL (gray) - ~35%
            gbc.gridx = 1;
            gbc.weightx = 0.35;
            JPanel right = new JPanel(new GridBagLayout());
            right.setBackground(new Color(230, 230, 230));

            // Create a container panel for centering content
            JPanel rightContent = new JPanel(new GridBagLayout());
            rightContent.setBackground(new Color(230, 230, 230));
            rightContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            GridBagConstraints rc = new GridBagConstraints();
            rc.gridx = 0;
            rc.fill = GridBagConstraints.HORIZONTAL;
            rc.anchor = GridBagConstraints.NORTH;

            // Top-right arrow (back) - placed in its own panel for proper positioning
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            JButton backBtn = new JButton("\u21A9");
            backBtn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2, true));
            backBtn.setContentAreaFilled(false);
            backBtn.setOpaque(true);
            backBtn.setBackground(new Color(230, 230, 230));
            backBtn.setPreferredSize(new Dimension(48, 48));
            backBtn.setFocusPainted(false);
            backBtn.addActionListener(e -> openMainWindow(frame));
            topPanel.add(backBtn, BorderLayout.EAST);

            rc.gridy = 0;
            rc.weightx = 1.0;
            rc.insets = new Insets(0, 0, 20, 0);
            rightContent.add(topPanel, rc);

            // Register header
            rc.gridy = 1;
            rc.insets = new Insets(0, 0, 30, 0);
            JLabel header = new JLabel("Register");
            header.setFont(new Font("Dialog", Font.BOLD, 26));
            header.setHorizontalAlignment(SwingConstants.CENTER);
            header.setForeground(Color.DARK_GRAY);
            rightContent.add(header, rc);

            // Form panel
            rc.gridy = 2;
            rc.insets = new Insets(0, 0, 0, 0);
            rc.weighty = 1.0;
            rc.fill = GridBagConstraints.BOTH;

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            GridBagConstraints f = new GridBagConstraints();
            f.insets = new Insets(8, 8, 8, 8);
            f.fill = GridBagConstraints.HORIZONTAL;
            int row = 0;

            // Name
            f.gridx = 0; f.gridy = row; f.weightx = 0.5;
            JLabel nameLabel = new JLabel("Name:");
            form.add(nameLabel, f);
            f.gridx = 1;

            nameField.setBorder(new RoundedBorder(14, Color.GRAY));
            // Apply document filter for name validation
            ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new NameDocumentFilter());
            form.add(nameField, f);

            // Last Name
            f.gridx = 2;
            JLabel lNameLabel = new JLabel("Last Name:");
            form.add(lNameLabel, f);
            f.gridx = 3;
            PlaceholderTextField lNameField = new PlaceholderTextField("Last name");
            lNameField.setBorder(new RoundedBorder(14, Color.GRAY));
            // Apply document filter for name validation
            ((AbstractDocument) lNameField.getDocument()).setDocumentFilter(new NameDocumentFilter());
            form.add(lNameField, f);
            row++;

            // Username
            f.gridx = 0; f.gridy = row;
            JLabel userLabel = new JLabel("Username:");
            form.add(userLabel, f);
            f.gridx = 1;
            PlaceholderTextField userField = new PlaceholderTextField("Username");
            userField.setBorder(new RoundedBorder(14, Color.GRAY));
            form.add(userField, f);

            // Password with toggle button and strength indicator at bottom
            f.gridx = 2;
            JLabel pLabel = new JLabel("Password:");
            form.add(pLabel, f);
            f.gridx = 3;

            // Main password container
            JPanel passwordContainer = new JPanel(new BorderLayout());
            passwordContainer.setOpaque(false);

            // Password field
            PlaceholderPasswordField passField = new PlaceholderPasswordField("Enter password");
            passField.setBorder(new RoundedBorder(14, Color.GRAY));

            // Bottom panel for toggle button and strength label - now in vertical layout
            JPanel passwordBottomPanel = new JPanel();
            passwordBottomPanel.setOpaque(false);
            passwordBottomPanel.setLayout(new BoxLayout(passwordBottomPanel, BoxLayout.Y_AXIS));

            // Toggle button for show/hide password - at top of bottom panel
            JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            togglePanel.setOpaque(false);
            JToggleButton toggleButton = new JToggleButton("👁 Show Password");
            toggleButton.setFont(new Font("Dialog", Font.PLAIN, 11));
            toggleButton.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            toggleButton.setContentAreaFilled(false);
            toggleButton.setFocusPainted(false);
            toggleButton.setForeground(new Color(80, 80, 200));

            toggleButton.addActionListener(e -> {
                if (toggleButton.isSelected()) {
                    passField.setEchoChar((char) 0);
                    toggleButton.setText("👁 Hide Password");
                } else {
                    passField.setEchoChar('•');
                    toggleButton.setText("👁 Show Password");
                }
            });

            togglePanel.add(toggleButton);

            // Password strength label - below toggle button
            JPanel strengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            strengthPanel.setOpaque(false);
            JLabel strengthLabel = new JLabel(" ");
            strengthLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
            strengthLabel.setForeground(Color.GRAY);
            strengthPanel.add(strengthLabel);

            // Add components to bottom panel
            passwordBottomPanel.add(togglePanel);
            passwordBottomPanel.add(strengthPanel);

            // Add components to main container
            passwordContainer.add(passField, BorderLayout.NORTH);
            passwordContainer.add(passwordBottomPanel, BorderLayout.SOUTH);

            // Password strength checker
            passField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }

                private void update() {
                    char[] password = passField.getPassword();
                    String message = getPasswordStrengthMessage(password);
                    strengthLabel.setText(message);

                    if (password.length > 0) {
                        if (isStrongPassword(password)) {
                            strengthLabel.setForeground(new Color(0, 128, 0)); // Green
                            passField.setBorder(new RoundedBorder(14, new Color(0, 128, 0)));
                        } else {
                            strengthLabel.setForeground(Color.RED);
                            passField.setBorder(new RoundedBorder(14, Color.RED));
                        }
                    } else {
                        strengthLabel.setForeground(Color.GRAY);
                        passField.setBorder(new RoundedBorder(14, Color.GRAY));
                    }
                }
            });

            form.add(passwordContainer, f);
            row++;

            // ID Number
            f.gridx = 0; f.gridy = row;
            JLabel idLabel = new JLabel("ID Number:");
            form.add(idLabel, f);
            f.gridx = 1;
            PlaceholderTextField idField = new PlaceholderTextField("XXXX-XXXX-XXXX");

            idField.setBorder(new RoundedBorder(14, Color.GRAY));
            // Apply document filter for ID formatting
            ((AbstractDocument) idField.getDocument()).setDocumentFilter(new IDDocumentFilter());
            form.add(idField, f);

            // Photo ID
            f.gridx = 2;
            JLabel photoLabel = new JLabel("Photo ID:");
            form.add(photoLabel, f);
            f.gridx = 3;
            JPanel photoPanel = new JPanel(new BorderLayout());
            photoPanel.setOpaque(false);
            JTextField photoText = new JTextField("No file chosen");
            photoText.setEditable(false);
            photoText.setBorder(new RoundedBorder(14, Color.GRAY));
            JButton browse = new JButton("⤴");
            browse.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                int res = chooser.showOpenDialog(frame);
                if (res == JFileChooser.APPROVE_OPTION)
                    photoText.setText(chooser.getSelectedFile().getName());
            });
            photoPanel.add(photoText, BorderLayout.CENTER);
            photoPanel.add(browse, BorderLayout.EAST);
            form.add(photoPanel, f);
            row++;

            // Position
            f.gridx = 0; f.gridy = row;
            JLabel posLabel = new JLabel("Position:");
            form.add(posLabel, f);
            f.gridx = 1;
            AdminSystemSettings systemSettings = new AdminSystemSettings();
            String[] positions = systemSettings.getLoginOptions();
            JComboBox<String> posCombo = new JComboBox<>(positions);
            posCombo.setUI(new RoundedComboBoxUI(12));
            posCombo.setBorder(new RoundedBorder(14, Color.GRAY));
            form.add(posCombo, f);
            idField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String currentText = idField.getText();
                    int foundIndex = systemSettings.getPositionIndexByUniqueId(currentText);
                    if (foundIndex != -1) {
                        posCombo.setSelectedIndex(foundIndex);
                    }
                }
            });


            // Date of Birth using JSpinnerDateModel
            f.gridx = 2;
            JLabel dobLabel = new JLabel("Date Of Birth:");
            form.add(dobLabel, f);
            f.gridx = 3;

            // Create SpinnerDateModel with date boundaries (from 1900 to current year)
            Calendar start = Calendar.getInstance();
            start.set(1900, Calendar.JANUARY, 1);
            Calendar end = Calendar.getInstance();
            end.set(Calendar.YEAR, LocalDate.now().getYear());

            // Set initial date to 18 years ago (typical adult age)
            Calendar initial = Calendar.getInstance();
            initial.add(Calendar.YEAR, -18);

            SpinnerDateModel dateModel = new SpinnerDateModel(
                    initial.getTime(),
                    start.getTime(),
                    end.getTime(),
                    Calendar.YEAR
            );

            JSpinner dobSpinner = new JSpinner(dateModel);

            // Set date format
            JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dobSpinner, "MM/dd/yyyy");
            dobSpinner.setEditor(dateEditor);

            // Apply rounded border
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) dobSpinner.getEditor();
            spinnerEditor.getTextField().setBorder(new RoundedBorder(14, Color.GRAY));

            form.add(dobSpinner, f);
            row++;

            // Age
            f.gridx = 0; f.gridy = row;
            JLabel ageLabel = new JLabel("Age:");
            form.add(ageLabel, f);
            f.gridx = 1;
            JComboBox<String> ageCombo = new JComboBox<>(generateAges());
            ageCombo.setUI(new RoundedComboBoxUI(12));
            ageCombo.setBorder(new RoundedBorder(14, Color.GRAY));
            ageCombo.setEnabled(false); // read-only
            form.add(ageCombo, f);

            // Auto calculate age from spinner
            dobSpinner.addChangeListener(e -> {
                Date selectedDate = (Date) dobSpinner.getValue();
                if (selectedDate != null) {
                    LocalDate birth = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    LocalDate now = LocalDate.now();
                    int age = Period.between(birth, now).getYears();
                    ageCombo.setSelectedItem(String.valueOf(age));
                }
            });

            // Set initial age calculation
            Date initialDate = (Date) dobSpinner.getValue();
            if (initialDate != null) {
                LocalDate birth = initialDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate now = LocalDate.now();
                int age = Period.between(birth, now).getYears();
                ageCombo.setSelectedItem(String.valueOf(age));
            }

            // Sex
            f.gridx = 2;
            JLabel sexLabel = new JLabel("Sex:");
            form.add(sexLabel, f);
            f.gridx = 3;
            JComboBox<String> sexCombo = new JComboBox<>(new String[]{"Select","Male","Female","Other"});
            sexCombo.setUI(new RoundedComboBoxUI(12));
            sexCombo.setBorder(new RoundedBorder(14, Color.GRAY));
            form.add(sexCombo, f);
            row++;

            // Phone
            f.gridx = 0; f.gridy = row;
            JLabel phoneLabel = new JLabel("Phone Number:");
            form.add(phoneLabel, f);
            f.gridx = 1;
            PlaceholderTextField phoneField = new PlaceholderTextField("09XXXXXXXXX");
            phoneField.setBorder(new RoundedBorder(14, Color.GRAY));
            // Apply document filter for phone validation
            ((AbstractDocument) phoneField.getDocument()).setDocumentFilter(new PhoneDocumentFilter());
            form.add(phoneField, f);

            // Email
            f.gridx = 2;
            JLabel emailLabel = new JLabel("Email Address:");
            form.add(emailLabel, f);
            f.gridx = 3;
            PlaceholderTextField emailField = new PlaceholderTextField("example@gmail.com");
            emailField.setBorder(new RoundedBorder(14, Color.GRAY));
            // Apply document filter for email validation
            ((AbstractDocument) emailField.getDocument()).setDocumentFilter(new EmailDocumentFilter());
            form.add(emailField, f);
            row++;

            // Status
            f.gridx = 0; f.gridy = row;
            JLabel statusLabel = new JLabel("Status:");
            form.add(statusLabel, f);
            f.gridx = 1;
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Single","Married","Widowed","Separated"});
            statusCombo.setUI(new RoundedComboBoxUI(12));
            statusCombo.setBorder(new RoundedBorder(14, Color.GRAY));
            form.add(statusCombo, f);

            // Permanent Address
            f.gridx = 2;
            JLabel permLabel = new JLabel("Permanent Address:");
            form.add(permLabel, f);
            f.gridx = 3;
            PlaceholderTextField permField = new PlaceholderTextField("Complete address");
            permField.setBorder(new RoundedBorder(14, Color.GRAY));
            form.add(permField, f);
            row++;

            // Bottom buttons
            f.gridx = 0; f.gridy = row; f.gridwidth = 4;
            JPanel bottom = new JPanel(new GridBagLayout());
            bottom.setOpaque(false);
            GridBagConstraints b = new GridBagConstraints();
            b.gridx = 0; b.gridy = 0; b.anchor = GridBagConstraints.WEST;
            JCheckBox terms = new JCheckBox("Terms of Service Agreement");
            terms.setOpaque(false);
            bottom.add(terms, b);

            b.gridy = 1; b.anchor = GridBagConstraints.CENTER;
            JButton signUp = new JButton("SIGN UP");
            signUp.setFont(new Font("Dialog", Font.BOLD, 16));
            signUp.setForeground(Color.WHITE);
            signUp.setBackground(new Color(80, 120, 255));
            signUp.setBorder(new RoundedBorder(16, new Color(80, 120, 255)));
            bottom.add(signUp, b);

            b.gridy = 2;
            JLabel bottomText = new JLabel("Already have an Account? ");
            JLabel loginLink = new JLabel("Log in");
            loginLink.setForeground(Color.BLUE.darker());
            loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JPanel loginPanel = new JPanel(new FlowLayout());
            loginPanel.setOpaque(false);
            loginPanel.add(bottomText);
            loginPanel.add(loginLink);
            bottom.add(loginPanel, b);

            form.add(bottom, f);

            // Add form to right content
            rightContent.add(form, rc);

            // Add right content to right panel with centering
            GridBagConstraints rightGbc = new GridBagConstraints();
            rightGbc.gridx = 0;
            rightGbc.gridy = 0;
            rightGbc.weightx = 1.0;
            rightGbc.weighty = 1.0;
            rightGbc.fill = GridBagConstraints.BOTH;
            right.add(rightContent, rightGbc);

            // Add right panel to frame
            frame.add(right, gbc);

            // Actions
            loginLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { openMainWindow(frame); }
            });

            signUp.addActionListener(e -> {
                // Check if terms of service is agreed
                if (!terms.isSelected()) {
                    JOptionPane.showMessageDialog(frame,
                            "Please check the Terms of Service Agreement to proceed.",
                            "Terms Agreement Required",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Validate all required fields
                if (!validateFields(frame, nameField, lNameField, userField, passField, idField,
                        posCombo, sexCombo, phoneField, emailField, statusCombo, permField, photoText)) {
                    return;
                }

                // Check if username already exists
                String username = userField.getText().trim();

                // Create and show overview panel
                JPanel overviewPanel = createOverviewPanel(nameField, lNameField, userField, passField, idField,
                        posCombo, dobSpinner, ageCombo, sexCombo, phoneField,
                        emailField, statusCombo, permField, photoText);

                int result = JOptionPane.showConfirmDialog(frame,
                        overviewPanel,
                        "Registration Overview - Confirm Details",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    // Get all form values
                    String firstName = nameField.getText().trim();
                    String lastName = lNameField.getText().trim();
                    String password = new String(passField.getPassword());
                    String idNumber = idField.getText().trim();
                    String position = (String) posCombo.getSelectedItem();
                    String sex = (String) sexCombo.getSelectedItem();
                    String phone = phoneField.getText().trim();
                    String email = emailField.getText().trim();
                    String status = (String) statusCombo.getSelectedItem();
                    String address = permField.getText().trim();
                    String photoId = photoText.getText();

                    // Get date of birth
                    Date dobDate = (Date) dobSpinner.getValue();

                    LocalDate birthDate = dobDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    int age = Integer.parseInt((String) ageCombo.getSelectedItem());

                    if (position.equals("Resident")) {
                        Resident resident = Resident.builder()
                                .firstName(firstName)
                                .lastName(lastName)
                                .username(username)
                                .password(password)
                                .nationalId(idNumber)
                                .gender(sex)
                                .age(age)
                                .email(email)
                                .contactNo(phone)
                                .voterStatus(status)
                                .position(position)
                                .status("Active")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                        resident.setAddress(address);
                        resident.setDob(birthDate);
                        // Save to data manager
                        UserDataManager.getInstance().addResident(resident);
                    } else {
                        // brgy staff
                        String role = mapPositionToRole(position);

                        BarangayStaff staffMember = BarangayStaff.builder()
                                .firstName(firstName)
                                .lastName(lastName)
                                .username(username)
                                .password(password)
                                .position(position)
                                .staffId(idNumber)
                                .role(role)
                                .contactNo(phone)
                                .age(age)
                                .email(email)
                                .status("Active")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        // Set additional fields (assuming UserInterface/Person class has these methods)
                        staffMember.setAddress(address);
                        staffMember.setDob(birthDate);

                        // Save to data manager
                        UserDataManager.getInstance().addStaff(staffMember);
                    }

                    JOptionPane.showMessageDialog(frame,
                            "Registration Successful!\n\n" +
                                    "Username: " + username + "\n" +
                                    "Position: " + position + "\n\n" +
                                    "You can now log in with your credentials.",
                            "Registration Complete",
                            JOptionPane.INFORMATION_MESSAGE);

                    openMainWindow(frame);
                }
            });

            frame.setVisible(true);
        });
    }

    private static String mapPositionToRole(String position) {
        switch (position) {
            case "Brgy.Captain":
                return "Brgy.Captain";
            case "Brgy.Secretary":
                return "Brgy.Secretary";
            case "Brgy.Treasurer":
                return "Brgy.Treasurer";
            case "Admin":
                return "Admin";
            default:
                return "Resident";
        }
    }

    // Helper to generate age list
    private static String[] generateAges() {
        String[] ages = new String[122];
        for (int i = 0; i < ages.length; i++) {
            ages[i] = String.valueOf(i);
        }
        return ages;
    }
}