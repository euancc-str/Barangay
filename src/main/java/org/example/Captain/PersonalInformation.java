package org.example.Captain;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class PersonalInformation extends JPanel {

    private static final Color HEADER_BG = new Color(21, 101, 192);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private JTextField txtFirstName, txtMiddleName, txtLastName, txtSuffix;
    private JComboBox<String> cmbSex, cmbCivilStatus, cmbMonth, cmbDay, cmbYear;
    private JTextField txtCitizenship, txtAge, txtPosition;
    private JTextField txtStreet, txtPhone, txtEmail, txtUniqueId, txtIdType;
    private JButton btnProfilePic, btnPhotoId, btnDone;
    private JLabel lblPhoneError, lblEmailError;

    private void updateAge() {
        try {
            int birthYear = Integer.parseInt((String) cmbYear.getSelectedItem());
            int birthMonth = cmbMonth.getSelectedIndex() + 1;
            int birthDay = Integer.parseInt((String) cmbDay.getSelectedItem());

            LocalDate birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
            LocalDate today = LocalDate.now();

            int age = today.getYear() - birthDate.getYear();

            if (today.getMonthValue() < birthDate.getMonthValue() ||
                    (today.getMonthValue() == birthDate.getMonthValue() && today.getDayOfMonth() < birthDate.getDayOfMonth())) {
                age--;
            }

            txtAge.setText(String.valueOf(age));
        } catch (Exception e) {
            txtAge.setText("0");
        }
    }

    private void updateDaysInMonth() {
        try {
            int selectedMonth = cmbMonth.getSelectedIndex() + 1;
            int selectedYear = Integer.parseInt((String) cmbYear.getSelectedItem());

            String currentDay = (String) cmbDay.getSelectedItem();
            int currentDayInt = currentDay != null ? Integer.parseInt(currentDay) : 1;

            LocalDate date = LocalDate.of(selectedYear, selectedMonth, 1);
            int maxDays = date.lengthOfMonth();

            ActionListener[] listeners = cmbDay.getActionListeners();
            for (ActionListener listener : listeners) {
                cmbDay.removeActionListener(listener);
            }

            cmbDay.removeAllItems();
            for (int i = 1; i <= maxDays; i++) {
                cmbDay.addItem(String.valueOf(i));
            }

            if (currentDayInt <= maxDays) {
                cmbDay.setSelectedItem(String.valueOf(currentDayInt));
            } else {
                cmbDay.setSelectedItem(String.valueOf(maxDays));
            }

            for (ActionListener listener : listeners) {
                cmbDay.addActionListener(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validatePhoneNumber() {
        String phone = txtPhone.getText().trim();

        // Check if exactly 11 digits
        if (phone.length() != 11) {
            lblPhoneError.setText("Phone must be exactly 11 digits");
            lblPhoneError.setVisible(true);
            return false;
        }

        // Check if starts with 09
        if (!phone.startsWith("09")) {
            lblPhoneError.setText("Phone must start with 09");
            lblPhoneError.setVisible(true);
            return false;
        }

        // Check if all characters are digits
        if (!phone.matches("\\d+")) {
            lblPhoneError.setText("Phone must contain only numbers");
            lblPhoneError.setVisible(true);
            return false;
        }

        lblPhoneError.setVisible(false);
        return true;
    }

    private boolean validateEmail() {
        String email = txtEmail.getText().trim();

        if (email.isEmpty()) {
            lblEmailError.setText("Email is required");
            lblEmailError.setVisible(true);
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            lblEmailError.setText("Invalid email format");
            lblEmailError.setVisible(true);
            return false;
        }

        lblEmailError.setVisible(false);
        return true;
    }

    private void validateForm() {
        boolean isPhoneValid = validatePhoneNumber();
        boolean isEmailValid = validateEmail();

        btnDone.setEnabled(isPhoneValid && isEmailValid);

        if (btnDone.isEnabled()) {
            btnDone.setBackground(new Color(0, 51, 204));
        } else {
            btnDone.setBackground(new Color(150, 150, 150));
        }
    }

    private void loadUserDataIntoForm() {
        BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();

        if (currentStaff != null) {
            txtFirstName.setText(currentStaff.getFirstName() != null ? currentStaff.getFirstName() : "");
            txtLastName.setText(currentStaff.getLastName() != null ? currentStaff.getLastName() : "");
            txtPosition.setText(currentStaff.getPosition() != null ? currentStaff.getPosition() : "");
            txtEmail.setText(currentStaff.getEmail() != null ? currentStaff.getEmail() : "");
            txtPhone.setText(currentStaff.getContactNo() != null ? currentStaff.getContactNo() : "");
            txtSuffix.setText(currentStaff.getSuffix() != null ? currentStaff.getSuffix():"");
            cmbCivilStatus.setSelectedItem(currentStaff.getCivilStatus()!=null ?currentStaff.getCivilStatus():"");
            txtCitizenship.setText(currentStaff.getCitizenship()!=null?currentStaff.getCitizenship():"");

            String uniqueId = ""+currentStaff.getIdNumber();
            txtUniqueId.setText(uniqueId);
            LocalDate dob = currentStaff.getDob();
            if (dob != null) {
                System.out.println("Loading DOB: " + dob); // Debug

                int monthIndex = dob.getMonthValue() - 1; // Use getMonthValue() instead of getMonth()
                if (monthIndex >= 0 && monthIndex < 12) {
                    cmbMonth.setSelectedIndex(monthIndex);
                }
                int day = dob.getDayOfMonth()-1;
                cmbDay.setSelectedIndex(day);
                int yr = dob.getYear();

                cmbYear.setSelectedItem(""+yr);
                String  age = "" +(LocalDate.now().getYear() - yr);
                txtAge.setText(age);
                System.out.println("Set day: " + dob.getDayOfMonth() + ", month index: " + monthIndex + ", year: " + dob.getYear());
            } else {
                System.out.println("DOB is null!");
            }
            if (currentStaff.getAddress() != null) {
                txtStreet.setText(currentStaff.getAddress());
            }
            if (currentStaff.getMiddleName() != null) {
                txtMiddleName.setText(currentStaff.getMiddleName());
            }

            validateForm();
            return;
        }

    }

    private <T> void setComboBoxValue(JComboBox<T> comboBox, String value) {
        if (value != null && !value.isEmpty()) {
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                if (comboBox.getItemAt(i).toString().equalsIgnoreCase(value)) {
                    comboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    public PersonalInformation() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(229, 231, 235));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(243, 244, 246));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new CaptainDashboard.RoundedBorder(30, true, false),
                new EmptyBorder(25, 40, 25, 40)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);

        JLabel lblDocumentary = new JLabel("Documentary");
        lblDocumentary.setFont(new Font("Arial", Font.BOLD, 26));
        lblDocumentary.setForeground(Color.WHITE);

        JLabel lblRequest = new JLabel("Request");
        lblRequest.setFont(new Font("Arial", Font.BOLD, 22));
        lblRequest.setForeground(Color.WHITE);

        titlePanel.add(lblDocumentary);
        titlePanel.add(lblRequest);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(HEADER_BG);
        BarangayStaff staff = new StaffDAO().findStaffByPosition("Captain");

        JLabel lblUser = new JLabel("Hi Mr. "+staff.getFirstName());
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblUser.setForeground(Color.WHITE);

        JPanel userIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 45, 45);
                g2.setColor(HEADER_BG);
                g2.fillOval(12, 8, 20, 20);
                g2.fillArc(5, 25, 35, 30, 0, 180);
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(45, 45);
            }
        };
        userIcon.setOpaque(false);

        userPanel.add(lblUser);
        userPanel.add(userIcon);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);

        return headerPanel;
    }
    BarangayStaff brgyStaff = UserDataManager.getInstance().getCurrentStaff();
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(new Color(229, 231, 235));
        formPanel.setBorder(new EmptyBorder(35, 60, 35, 60));
        JLabel lblPersonalInfo = new JLabel("Personal Information");
        lblPersonalInfo.setFont(new Font("Arial", Font.BOLD, 18));
        lblPersonalInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblPersonalInfo);
        formPanel.add(Box.createVerticalStrut(25));

        // Name Row
        JPanel namePanel = new JPanel(new GridLayout(1, 3, 20, 0));
        namePanel.setBackground(new Color(229, 231, 235));
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        namePanel.add(createFieldPanel("First Name *", txtFirstName = createTextField(brgyStaff.getFirstName())));
        namePanel.add(createFieldPanel("Middle Name *", txtMiddleName = createTextField(brgyStaff.getMiddleName())));
        namePanel.add(createFieldPanel("Last Name *", txtLastName = createTextField(brgyStaff.getLastName())));

        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(18));

        // Details Row
        JPanel detailsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        detailsPanel.setBackground(new Color(229, 231, 235));
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        detailsPanel.add(createFieldPanel("Suffix", txtSuffix = createTextField(brgyStaff.getSuffix())));
        String [] arr = new SystemConfigDAO().getOptionsNature("sex");
        cmbSex = new JComboBox<>(arr);styleComboBox(cmbSex);
        detailsPanel.add(createFieldPanel("Sex *", cmbSex));
        String [] arr1 = new SystemConfigDAO().getOptionsNature("civilStatus");
        cmbCivilStatus = new JComboBox<>(arr1);  styleComboBox(cmbCivilStatus);
        cmbCivilStatus.setSelectedItem("Married");
        detailsPanel.add(createFieldPanel("Civil Status *", cmbCivilStatus));

        detailsPanel.add(createFieldPanel("Citizenship *", txtCitizenship = createTextField(brgyStaff.getCitizenship())));

        formPanel.add(detailsPanel);
        formPanel.add(Box.createVerticalStrut(18));

        // Birth Date Row
        JPanel birthPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        birthPanel.setBackground(new Color(229, 231, 235));
        birthPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        cmbMonth = new JComboBox<>(months);
        styleComboBox(cmbMonth);
        birthPanel.add(createFieldPanel("Birth Date *", cmbMonth));

        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        cmbDay = new JComboBox<>(days);
        styleComboBox(cmbDay);
        JPanel dayPanel = createFieldPanel("", cmbDay);
        dayPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        birthPanel.add(dayPanel);

        String[] years = new String[100];
        for (int i = 0; i < 100; i++) years[i] = String.valueOf(2024 - i);
        cmbYear = new JComboBox<>(years);
        styleComboBox(cmbYear);
        JPanel yearPanel = createFieldPanel("", cmbYear);
        yearPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        birthPanel.add(yearPanel);

        cmbMonth.setSelectedItem("May");
        cmbYear.setSelectedItem("1990");

        updateDaysInMonth();
        cmbDay.setSelectedItem("15");

        cmbMonth.addActionListener(e -> {
            updateDaysInMonth();
            updateAge();
        });
        cmbYear.addActionListener(e -> {
            updateDaysInMonth();
            updateAge();
        });
        cmbDay.addActionListener(e -> updateAge());

        birthPanel.add(createFieldPanel("Age", txtAge = createTextField("45")));
        txtAge.setEditable(false);
        txtAge.setBackground(new Color(245, 245, 245));

        formPanel.add(birthPanel);
        formPanel.add(Box.createVerticalStrut(18));

        // Position Row
        JPanel positionPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        positionPanel.setBackground(new Color(229, 231, 235));
        positionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        txtPosition = createTextField(brgyStaff.getPosition());
        txtPosition.setHorizontalAlignment(JTextField.CENTER);
        txtPosition.setFont(new Font("Arial", Font.BOLD, 14));
        txtPosition.setEditable(false);
        txtPosition.setBackground(Color.WHITE);
        positionPanel.add(createFieldPanel("Position", txtPosition));

        btnProfilePic = createUploadButton("Add a profile picture");
        positionPanel.add(createFieldPanel("Profile", btnProfilePic));

        formPanel.add(positionPanel);
        formPanel.add(Box.createVerticalStrut(30));

        // Contact Information
        JLabel lblContactInfo = new JLabel("Contact Information");
        lblContactInfo.setFont(new Font("Arial", Font.BOLD, 18));
        lblContactInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblContactInfo);
        formPanel.add(Box.createVerticalStrut(25));

        JPanel streetPanel = new JPanel(new BorderLayout());
        streetPanel.setBackground(new Color(229, 231, 235));
        streetPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        streetPanel.add(createFieldPanel("Street/Barangay/Municipality *",
                        txtStreet = createTextField("Purok 5 San Vicente rd Brg. Alawihao Daet Camarines Norte")),
                BorderLayout.CENTER);

        formPanel.add(streetPanel);
        formPanel.add(Box.createVerticalStrut(18));

        JPanel contactPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        contactPanel.setBackground(new Color(229, 231, 235));
        contactPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Phone Number with validation
        JPanel phoneFieldPanel = new JPanel(new BorderLayout());
        phoneFieldPanel.setBackground(new Color(229, 231, 235));

        txtPhone = createPhoneTextField(brgyStaff.getContactNo());
        lblPhoneError = new JLabel("");
        lblPhoneError.setFont(new Font("Arial", Font.PLAIN, 11));
        lblPhoneError.setForeground(new Color(220, 38, 38));
        lblPhoneError.setVisible(false);

        JPanel phoneWithError = new JPanel(new BorderLayout());
        phoneWithError.setBackground(new Color(229, 231, 235));
        phoneWithError.add(txtPhone, BorderLayout.CENTER);
        phoneWithError.add(lblPhoneError, BorderLayout.SOUTH);

        phoneFieldPanel.add(createFieldPanelWithComponent("Phone Number *", phoneWithError));
        contactPanel.add(phoneFieldPanel);

        // Email with validation
        JPanel emailFieldPanel = new JPanel(new BorderLayout());
        emailFieldPanel.setBackground(new Color(229, 231, 235));

        txtEmail = createTextField(brgyStaff.getEmail());
        lblEmailError = new JLabel("");
        lblEmailError.setFont(new Font("Arial", Font.PLAIN, 11));
        lblEmailError.setForeground(new Color(220, 38, 38));
        lblEmailError.setVisible(false);

        JPanel emailWithError = new JPanel(new BorderLayout());
        emailWithError.setBackground(new Color(229, 231, 235));
        emailWithError.add(txtEmail, BorderLayout.CENTER);
        emailWithError.add(lblEmailError, BorderLayout.SOUTH);

        emailFieldPanel.add(createFieldPanelWithComponent("Email Address *", emailWithError));
        contactPanel.add(emailFieldPanel);

        contactPanel.add(createFieldPanel("Unique ID NO.", txtUniqueId = createTextField(brgyStaff.getIdNumber())));

        formPanel.add(contactPanel);
        formPanel.add(Box.createVerticalStrut(18));

        JPanel idPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        idPanel.setBackground(new Color(229, 231, 235));
        idPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        idPanel.add(createFieldPanel("ID TYPE", txtIdType = createTextField("Brgy Captain ID")));

        btnPhotoId = createUploadButton("⬆");
        idPanel.add(createFieldPanel("Photo ID.", btnPhotoId));

        formPanel.add(idPanel);
        formPanel.add(Box.createVerticalStrut(35));

        // Done Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(229, 231, 235));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        btnDone = new JButton("Done") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnDone.setFont(new Font("Arial", Font.BOLD, 18));
        btnDone.setBackground(new Color(150, 150, 150));
        btnDone.setForeground(Color.WHITE);
        btnDone.setFocusPainted(false);
        btnDone.setBorder(new EmptyBorder(15, 60, 15, 60));
        btnDone.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDone.setOpaque(false);
        btnDone.setContentAreaFilled(false);
        btnDone.setEnabled(false);
        txtAge.setEditable(false);
        txtPosition.setEditable(false);
        txtUniqueId.setEditable(false);
        cmbSex.setEnabled(false);
        cmbMonth.setEnabled(false);
        cmbDay.setEnabled(false);
        cmbYear.setEnabled(false);
        txtUniqueId.setEditable(false);
        txtUniqueId.setEditable(false);
        btnDone.addActionListener(e -> {
            if (validatePhoneNumber() && validateEmail()) {
                JOptionPane.showMessageDialog(this, "Form submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();
            if (currentStaff != null && !currentStaff.getPosition().equals("Admin")) {
                // Update the staff object with form data
                BarangayStaff updatedStaff = currentStaff.toBuilder()
                        .firstName(txtFirstName.getText().trim())
                        .middleName(txtMiddleName.getText().trim())
                        .lastName(txtLastName.getText().trim())
                        .suffix(txtSuffix.getText().trim())
                        .position(txtPosition.getText().trim())
                        .suffix(txtSuffix.getText())
                        .contactNo(txtPhone.getText().trim())
                        .email(txtEmail.getText().trim())
                        .civilStatus(cmbCivilStatus.getSelectedItem().toString())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build();
                UserDataManager.getInstance().updateStaff(updatedStaff);

                UserDataManager.getInstance().setCurrentStaff(updatedStaff);
                System.out.println("Staff data updated successfully!");
            }
        });

        btnDone.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btnDone.isEnabled()) {
                    btnDone.setBackground(new Color(0, 68, 255));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btnDone.isEnabled()) {
                    btnDone.setBackground(new Color(0, 51, 204));
                } else {
                    btnDone.setBackground(new Color(150, 150, 150));
                }
            }
        });

        buttonPanel.add(btnDone);
        formPanel.add(buttonPanel);
        loadUserDataIntoForm();
        return formPanel;
    }

    private JTextField createTextField(String text) {
        JTextField textField = new JTextField(text);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                new EmptyBorder(10, 15, 10, 15)
        ));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    private JTextField createPhoneTextField(String text) {
        JTextField textField = new JTextField(text);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                new EmptyBorder(10, 15, 10, 15)
        ));
        textField.setBackground(Color.WHITE);

        // Add document filter to accept only numbers and limit to 11 digits
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;

                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + string + currentText.substring(offset);

                if (newText.matches("\\d*") && newText.length() <= 11) {
                    super.insertString(fb, offset, string, attr);
                    validateForm();
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
                if (string == null) return;

                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + string + currentText.substring(offset + length);

                if (newText.matches("\\d*") && newText.length() <= 11) {
                    super.replace(fb, offset, length, string, attrs);
                    validateForm();
                }
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
                validateForm();
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateForm();
            }
        });

        return textField;
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                new EmptyBorder(2, 10, 2, 10)
        ));
    }

    private JButton createUploadButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(180, 180, 180));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2),
                new EmptyBorder(10, 15, 10, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(this);
        });
        return button;
    }

    private JPanel createFieldPanel(String labelText, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBackground(new Color(229, 231, 235));

        if (!labelText.isEmpty()) {
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("Arial", Font.PLAIN, 13));
            label.setForeground(Color.BLACK);
            panel.add(label, BorderLayout.NORTH);
        }

        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFieldPanelWithComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBackground(new Color(229, 231, 235));

        if (!labelText.isEmpty()) {
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("Arial", Font.PLAIN, 13));
            label.setForeground(Color.BLACK);
            panel.add(label, BorderLayout.NORTH);
        }

        panel.add(component, BorderLayout.CENTER);
        return panel;
    }
}