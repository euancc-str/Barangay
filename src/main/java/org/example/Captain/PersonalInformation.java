package org.example.Captain;

import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;

public class PersonalInformation extends JPanel {

    private JTextField txtFirstName, txtMiddleName, txtLastName, txtSuffix;
    private JComboBox<String> cmbSex, cmbCivilStatus, cmbMonth, cmbDay, cmbYear;
    private JTextField txtCitizenship, txtAge, txtPosition;
    private JTextField txtStreet, txtPhone, txtEmail, txtUniqueId, txtIdType;
    private JButton btnProfilePic, btnPhotoId, btnDone;
    private void loadUserDataIntoForm() {
        // Try to get current staff first
        BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();

        if (currentStaff != null) {
            // Populate fields from staff data
            txtFirstName.setText(currentStaff.getFirstName() != null ? currentStaff.getFirstName() : "");
            txtLastName.setText(currentStaff.getLastName() != null ? currentStaff.getLastName() : "");
            txtPosition.setText(currentStaff.getPosition() != null ? currentStaff.getPosition() : "");
            txtEmail.setText(currentStaff.getEmail() != null ? currentStaff.getEmail() : "");
            txtPhone.setText(currentStaff.getContactNo() != null ? currentStaff.getContactNo() : "");


            String uniqueId = ""+currentStaff.getStaffId();
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

            System.out.println("Loaded staff data: " + currentStaff.getFirstName() + " " + currentStaff.getLastName());
            return;
        }
        Resident currentResident = UserDataManager.getInstance().getCurrentResident();

        if (currentResident != null) {
            // Populate fields from resident data
            txtFirstName.setText(currentResident.getFirstName() != null ? currentResident.getFirstName() : "");
            txtLastName.setText(currentResident.getLastName() != null ? currentResident.getLastName() : "");
            txtEmail.setText(currentResident.getEmail() != null ? currentResident.getEmail() : "");
            txtPhone.setText(currentResident.getPhoneNumber() != null ? currentResident.getPhoneNumber() : "");
           // txtAddress.setText(currentResident.getAddress() != null ? currentResident.getAddress() : "");
            txtAge.setText(String.valueOf(currentResident.getAge()));
            txtUniqueId.setText(currentResident.getNationalId() != null ? currentResident.getNationalId() : "");

            if (currentResident.getGender() != null) {
                setComboBoxValue(cmbSex, currentResident.getGender());
            }
            if (currentResident.getVoterStatus() != null) {
          //      setComboBoxValue(cmbStatus, currentResident.getVoterStatus());
            }
            if (currentResident.getMiddleName() != null) {
                txtMiddleName.setText(currentResident.getMiddleName());
            }

            System.out.println("Loaded resident data: " + currentResident.getFirstName() + " " + currentResident.getLastName());
        } else {
            System.out.println("No user logged in, loading from properties file");
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

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(243, 244, 246));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new CaptainDashboard.RoundedBorder(30, true, false),
            new EmptyBorder(25, 40, 25, 40)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(40, 40, 40));

        JLabel lblDocumentary = new JLabel("Documentary");
        lblDocumentary.setFont(new Font("Arial", Font.BOLD, 26));
        lblDocumentary.setForeground(Color.WHITE);

        JLabel lblRequest = new JLabel("Request");
        lblRequest.setFont(new Font("Arial", Font.BOLD, 22));
        lblRequest.setForeground(Color.WHITE);

        titlePanel.add(lblDocumentary);
        titlePanel.add(lblRequest);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(new Color(40, 40, 40));

        JLabel lblUser = new JLabel("Hi Mr. Dalisay");
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
                g2.setColor(new Color(40, 40, 40));
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

        namePanel.add(createFieldPanel("First Name *", txtFirstName = createTextField("Cardo")));
        namePanel.add(createFieldPanel("Middle Name *", txtMiddleName = createTextField("Demesa")));
        namePanel.add(createFieldPanel("Last Name *", txtLastName = createTextField("Dalisay")));

        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(18));

        // Details Row
        JPanel detailsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        detailsPanel.setBackground(new Color(229, 231, 235));
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        detailsPanel.add(createFieldPanel("Suffix", txtSuffix = createTextField("N/A")));

        cmbSex = new JComboBox<>(new String[]{"Male", "Female"});
        styleComboBox(cmbSex);
        detailsPanel.add(createFieldPanel("Sex *", cmbSex));

        cmbCivilStatus = new JComboBox<>(new String[]{"Single", "Married", "Widowed", "Separated"});
        styleComboBox(cmbCivilStatus);
        cmbCivilStatus.setSelectedItem("Married");
        detailsPanel.add(createFieldPanel("Civil Status *", cmbCivilStatus));

        detailsPanel.add(createFieldPanel("Citizenships *", txtCitizenship = createTextField("Filipino")));

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
        cmbMonth.setSelectedItem("May");
        birthPanel.add(createFieldPanel("Birth Date *", cmbMonth));

        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        cmbDay = new JComboBox<>(days);
        styleComboBox(cmbDay);
        cmbDay.setSelectedItem("15");
        JPanel dayPanel = createFieldPanel("", cmbDay);
        dayPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        birthPanel.add(dayPanel);

        String[] years = new String[100];
        for (int i = 0; i < 100; i++) years[i] = String.valueOf(2024 - i);
        cmbYear = new JComboBox<>(years);
        styleComboBox(cmbYear);
        cmbYear.setSelectedItem("1990");
        JPanel yearPanel = createFieldPanel("", cmbYear);
        yearPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        birthPanel.add(yearPanel);

        birthPanel.add(createFieldPanel("Age", txtAge = createTextField("45")));
        txtAge.setEditable(false);
        txtAge.setBackground(new Color(245, 245, 245));

        formPanel.add(birthPanel);
        formPanel.add(Box.createVerticalStrut(18));

        // Position Row
        JPanel positionPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        positionPanel.setBackground(new Color(229, 231, 235));
        positionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        txtPosition = createTextField("Barangay Captain");
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

        contactPanel.add(createFieldPanel("Phone Number *", txtPhone = createTextField("09123456789")));
        contactPanel.add(createFieldPanel("Email Address", txtEmail = createTextField("Dalisay@gmail.com")));
        contactPanel.add(createFieldPanel("Unique ID NO.", txtUniqueId = createTextField("1234-5678-9101-1213")));

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
        btnDone.setBackground(new Color(0, 51, 204));
        btnDone.setForeground(Color.WHITE);
        btnDone.setFocusPainted(false);
        btnDone.setBorder(new EmptyBorder(15, 60, 15, 60));
        btnDone.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDone.setOpaque(false);
        btnDone.setContentAreaFilled(false);
        btnDone.addActionListener(e -> JOptionPane.showMessageDialog(this, "Form submitted!"));

        btnDone.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnDone.setBackground(new Color(0, 68, 255));
            }
            public void mouseExited(MouseEvent e) {
                btnDone.setBackground(new Color(0, 51, 204));
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
}
