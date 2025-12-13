package org.example.treasurer;


import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Captain.PersonalInformation;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.Objects;


public class TreasurerPersonalInformation extends JPanel {


    private JTextField txtFirstName, txtMiddleName, txtLastName, txtSuffix;
    private JComboBox<String> cmbSex, cmbCivilStatus, cmbMonth, cmbDay, cmbYear;
    private JTextField txtCitizenship, txtAge, txtPosition;
    private JTextField txtStreet, txtPhone, txtEmail, txtUniqueId, txtIdType;
    private JButton btnProfilePic, btnPhotoId, btnDone;


    // Gradient colors
    private final Color CERULEAN_BLUE = new Color(100, 149, 237);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    private final Color VERY_LIGHT_BLUE = new Color(225, 245, 254);
    private final Color DARK_CERULEAN = new Color(70, 130, 180);


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


            txtSuffix.setText(currentStaff.getSuffix());
            cmbCivilStatus.setSelectedItem(currentStaff.getCivilStatus());
            txtCitizenship.setText(currentStaff.getCitizenship());
            String uniqueId = ""+currentStaff.getIdNumber();
            txtUniqueId.setText(uniqueId);
            txtAge.setEditable(false);
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
    }


    public TreasurerPersonalInformation() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false); // Make transparent for gradient


        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);


        // Form
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // Create gradient from light blue to very light blue
        GradientPaint gradient = new GradientPaint(
                0, 0, LIGHT_BLUE,
                getWidth(), getHeight(), VERY_LIGHT_BLUE
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Create gradient for header
                GradientPaint gradient = new GradientPaint(
                        0, 0, CERULEAN_BLUE,
                        getWidth(), 0, DARK_CERULEAN
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(CERULEAN_BLUE, 2, true),
                new EmptyBorder(25, 40, 25, 40)
        ));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);


        JLabel lblDocumentary = new JLabel("Documentary");
        lblDocumentary.setFont(new Font("Arial", Font.BOLD, 26));
        lblDocumentary.setForeground(Color.WHITE);


        JLabel lblRequest = new JLabel("Request");
        lblRequest.setFont(new Font("Arial", Font.BOLD, 22));
        lblRequest.setForeground(Color.WHITE);


        titlePanel.add(lblDocumentary);
        titlePanel.add(lblRequest);


        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);


        BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
        String name = staff.getFirstName();
        String data = "";
        if(staff.getSex().equals("Male")){
            data = "Mr.";
        }else{
            data = "Ms.";
        }
        JLabel lblUser = new JLabel("Hi "+data+" "+name);
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblUser.setForeground(Color.WHITE);


        JPanel userIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Gradient for user icon
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), CERULEAN_BLUE
                );
                g2.setPaint(gradient);
                g2.fillOval(0, 0, 45, 45);


                g2.setColor(Color.WHITE);
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
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(35, 60, 35, 60));


        JLabel lblPersonalInfo = new JLabel("Personal Information");
        lblPersonalInfo.setFont(new Font("Arial", Font.BOLD, 18));
        lblPersonalInfo.setForeground(Color.DARK_GRAY);
        lblPersonalInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblPersonalInfo);
        formPanel.add(Box.createVerticalStrut(25));
        BarangayStaff staff=  UserDataManager.getInstance().getCurrentStaff();

        JPanel namePanel = new JPanel(new GridLayout(1, 3, 20, 0));
        namePanel.setOpaque(false);
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));


        namePanel.add(createFieldPanel("First Name *", txtFirstName = createTextField(staff.getFirstName())));
        namePanel.add(createFieldPanel("Middle Name *", txtMiddleName = createTextField(staff.getMiddleName())));
        namePanel.add(createFieldPanel("Last Name *", txtLastName = createTextField(staff.getLastName())));


        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(18));


        // Details Row
        JPanel detailsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));


        detailsPanel.add(createFieldPanel("Suffix", txtSuffix = createTextField("")));
        String [] arr = new SystemConfigDAO().getOptionsNature("sex");
        cmbSex = new JComboBox<>(arr);
        styleComboBox(cmbSex);
        cmbSex.setSelectedItem("Female");
        detailsPanel.add(createFieldPanel("Sex *", cmbSex));
        cmbSex.setEnabled(false);
        String [] arr1 = new SystemConfigDAO().getOptionsNature("civilStatus");
        cmbCivilStatus = new JComboBox<>(arr1);
        styleComboBox(cmbCivilStatus);
        cmbCivilStatus.setSelectedItem(staff.getSex()); // Changed to Single
        detailsPanel.add(createFieldPanel("Civil Status *", cmbCivilStatus));


        detailsPanel.add(createFieldPanel("Citizenships *", txtCitizenship = createTextField(staff.getCitizenship())));


        formPanel.add(detailsPanel);
        formPanel.add(Box.createVerticalStrut(18));


        // Birth Date Row
        JPanel birthPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        birthPanel.setOpaque(false);
        birthPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));


        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        cmbMonth = new JComboBox<>(months);
        styleComboBox(cmbMonth);
        cmbMonth.setSelectedItem("March"); // Changed birth month
        birthPanel.add(createFieldPanel("Birth Date *", cmbMonth));
        cmbMonth.setEnabled(false);

        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        cmbDay = new JComboBox<>(days);
        styleComboBox(cmbDay);
        cmbDay.setSelectedItem("12"); // Changed birth day
        JPanel dayPanel = createFieldPanel("", cmbDay);
        dayPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        birthPanel.add(dayPanel);
        cmbDay.setEnabled(false);

        String[] years = new String[100];
        for (int i = 0; i < 100; i++) years[i] = String.valueOf(2024 - i);
        cmbYear = new JComboBox<>(years);
        styleComboBox(cmbYear);
        cmbYear.setSelectedItem("1985"); // Changed birth year
        JPanel yearPanel = createFieldPanel("", cmbYear);
        yearPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        birthPanel.add(yearPanel);
        cmbYear.setEnabled(false);

        birthPanel.add(createFieldPanel("Age", txtAge = createTextField("39"))); // Changed age
        txtAge.setEditable(false);
        txtAge.setBackground(new Color(245, 245, 245));


        formPanel.add(birthPanel);
        formPanel.add(Box.createVerticalStrut(18));


        // Position Row - Changed to Treasurer position
        JPanel positionPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        positionPanel.setOpaque(false);
        positionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));


        txtPosition = createTextField("Barangay Treasurer"); // Changed position
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
        lblContactInfo.setForeground(Color.DARK_GRAY);
        lblContactInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(lblContactInfo);
        formPanel.add(Box.createVerticalStrut(25));


        JPanel streetPanel = new JPanel(new BorderLayout());
        streetPanel.setOpaque(false);
        streetPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        streetPanel.add(createFieldPanel("Street/Barangay/Municipality *",
                        txtStreet = createTextField(staff.getAddress())),
                BorderLayout.CENTER);


        formPanel.add(streetPanel);
        formPanel.add(Box.createVerticalStrut(18));


        JPanel contactPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        contactPanel.setOpaque(false);
        contactPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));


        contactPanel.add(createFieldPanel("Phone Number *", txtPhone = createTextField(staff.getContactNo())));
        contactPanel.add(createFieldPanel("Email Address", txtEmail = createTextField(staff.getEmail())));
        contactPanel.add(createFieldPanel("Unique ID NO.", txtUniqueId = createTextField(staff.getIdNumber())));

        txtUniqueId.setEditable(false);
        formPanel.add(contactPanel);
        formPanel.add(Box.createVerticalStrut(18));


        JPanel idPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        idPanel.setOpaque(false);
        idPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));


        idPanel.add(createFieldPanel("ID TYPE", txtIdType = createTextField("Brgy Treasurer ID"))); // Changed ID type


        btnPhotoId = createUploadButton("⬆");
        idPanel.add(createFieldPanel("Photo ID.", btnPhotoId));


        formPanel.add(idPanel);
        formPanel.add(Box.createVerticalStrut(35));


        // Done Button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));


        btnDone = new JButton("Done") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Create gradient for button
                GradientPaint gradient = new GradientPaint(
                        0, 0, getBackground(),
                        getWidth(), getHeight(), getBackground().brighter()
                );
                g2.setPaint(gradient);
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
        btnDone.setBackground(CERULEAN_BLUE);
        btnDone.setForeground(Color.WHITE);
        btnDone.setFocusPainted(false);
        btnDone.setBorder(new EmptyBorder(15, 60, 15, 60));
        btnDone.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDone.setOpaque(false);
        ((javax.swing.text.AbstractDocument) txtPhone.getDocument()).setDocumentFilter(new TreasurerPersonalInformation.PhoneDocumentFilter());
        btnDone.setContentAreaFilled(false);
        btnDone.addActionListener(e -> {updateMethod();
        });

        btnDone.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnDone.setBackground(new Color(90, 150, 200));
                btnDone.repaint();
            }
            public void mouseExited(MouseEvent e) {
                btnDone.setBackground(CERULEAN_BLUE);
                btnDone.repaint();
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
                BorderFactory.createLineBorder(CERULEAN_BLUE, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        textField.setBackground(Color.WHITE);
        return textField;
    }
    static class PhoneDocumentFilter extends javax.swing.text.DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (string == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);
            if (isValidPhone(newText)) super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
            if (text == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
            if (isValidPhone(newText)) super.replace(fb, offset, length, text, attrs);
        }

        private boolean isValidPhone(String text) {
            if (text.isEmpty()) return true;
            if (!text.matches("\\d*")) return false;
            if (text.length() > 11) return false;
            if (text.length() >= 2 && !text.startsWith("09")) return false;
            return true;
        }
    }
    private void updateMethod(){
        JOptionPane.showMessageDialog(this, "Form submitted!");

        BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();
        if (currentStaff != null) {
            // Update the staff object with form data
            BarangayStaff updatedStaff = currentStaff.toBuilder()
                    .firstName(txtFirstName.getText().trim())
                    .middleName(txtMiddleName.getText().trim())
                    .staffId(currentStaff.getStaffId())
                    .username(currentStaff.getUsername())
                    .password(currentStaff.getPassword())
                    .status(currentStaff.getStatus())
                    .lastName(txtLastName.getText().trim())
                    .suffix(txtSuffix.getText().trim())
                    .position("Treasurer")
                    .civilStatus(Objects.requireNonNull(cmbCivilStatus.getSelectedItem()).toString())
                    .address(txtStreet.getText().trim())
                    .contactNo(txtPhone.getText().trim())
                    .email(txtEmail.getText().trim())
                    .updatedAt(java.time.LocalDateTime.now()) // Mark the time
                    .build();
            UserDataManager.getInstance().updateStaff(updatedStaff);

            UserDataManager.getInstance().setCurrentStaff(updatedStaff);
            System.out.println("Staff data updated successfully!");
        }
    }


    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CERULEAN_BLUE, 1),
                new EmptyBorder(2, 10, 2, 10)
        ));
    }


    private JButton createUploadButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, Color.WHITE,
                        getWidth(), getHeight(), VERY_LIGHT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g2d);
            }
        };
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(new Color(100, 100, 100));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CERULEAN_BLUE, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showOpenDialog(this);
        });
        return button;
    }


    private JPanel createFieldPanel(String labelText, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 8));
        panel.setOpaque(false);


        if (!labelText.isEmpty()) {
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("Arial", Font.PLAIN, 13));
            label.setForeground(Color.DARK_GRAY);
            panel.add(label, BorderLayout.NORTH);
        }


        panel.add(component, BorderLayout.CENTER);
        return panel;
    }


    // Custom rounded border class
    static class RoundedBorder extends AbstractBorder {
        private int radius;
        private boolean top;
        private boolean bottom;


        RoundedBorder(int radius, boolean top, boolean bottom) {
            this.radius = radius;
            this.top = top;
            this.bottom = bottom;
        }


        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());


            if (top && bottom) {
                g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
            } else if (top) {
                g2.fillRoundRect(x, y, width - 1, height + radius, radius, radius);
            } else if (bottom) {
                g2.fillRoundRect(x, y - radius, width - 1, height + radius, radius, radius);
            }


            g2.dispose();
        }


        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }
    }
}

