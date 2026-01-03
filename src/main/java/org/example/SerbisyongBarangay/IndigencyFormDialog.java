package org.example.SerbisyongBarangay;


import lombok.Data;
import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Admin.SystemLogDAO;
import org.example.Documents.DocumentType;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class IndigencyFormDialog extends JDialog {


    // ✅ DECLARE ALL FORM FIELDS AS INSTANCE VARIABLES
    private JTextField txtName;
    private JTextField txtLastName;
    private JTextField txtMiddleName;
    private JTextField txtSuffix;
    private JComboBox<String> cmbSex;
    private JTextField txtAge;
    private JComboBox<String> cmbBirthDate;
    private JTextField txtCivilStatus;
    private JTextField txtDate;
    private JTextField txtPerYearIncome;
    private JTextField txtCurrentAddress;
    private JTextArea txtPurpose;
    private JTextField street;
    private JComboBox<String> txtPurok;

    // Store the current resident
    private Resident currentResident;
    private SystemConfigDAO dao;

    // Store form data after submission
    private IndigencyFormData formData;


    public IndigencyFormDialog(Frame owner) {
        super(owner, "Certificate of Indigency Form", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);


        // ✅ Load current resident
        currentResident = UserDataManager.getInstance().getCurrentResident();


        // Main panel setup
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 248, 250));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));


        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);


        setContentPane(mainPanel);
        pack();
        setSize(750, 750);
        setLocationRelativeTo(owner);


        // ✅ Load resident data into form
        loadResidentData();
    }


    // ✅ METHOD TO LOAD RESIDENT DATA INTO FORM
    private void loadResidentData() {
        if (currentResident != null) {
            txtName.setText(currentResident.getFirstName() != null ? currentResident.getFirstName() : "");
            txtLastName.setText(currentResident.getLastName() != null ? currentResident.getLastName() : "");
            txtMiddleName.setText(currentResident.getMiddleName() != null ? currentResident.getMiddleName() : "");
            txtAge.setText(String.valueOf(currentResident.getAge()));


            if (currentResident.getGender() != null) {
                cmbSex.setSelectedItem(currentResident.getGender());
            }


            if (currentResident.getCivilStatus() != null) {
                txtCivilStatus.setText(currentResident.getCivilStatus());
            }
            if (currentResident.getSuffix() != null) {
                txtSuffix.setText(currentResident.getSuffix());
            }
            txtPurok.setSelectedItem(currentResident.getPurok());
            street.setText(currentResident.getStreet());


            txtCurrentAddress.setText(currentResident.getAddress() != null ? currentResident.getAddress() : "");


            // Set today's date
            txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            txtDate.setEditable(false);

            // Set birth date from resident data
            String dob = (currentResident != null && currentResident.getDob() != null) ?
                    currentResident.getDob().toString() : "";
            cmbBirthDate.setSelectedItem(dob);
            cmbBirthDate.setEnabled(false);


            txtAge.setEditable(false);


            System.out.println("✅ Loaded resident data: " + currentResident.getFirstName());
        } else {
            System.out.println("⚠️ No resident logged in");
        }
    }


    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(70, 130, 180),
                        0, getHeight(), new Color(90, 150, 200)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        header.setPreferredSize(new Dimension(0, 60));

        JLabel title = new JLabel("<html><center><b>CERTIFICATE OF INDIGENCY FORM</b><br>" +
                "<span style='font-size:10px; font-weight:normal;'>Official Document Request</span></center></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(JLabel.CENTER);

        header.add(title, BorderLayout.CENTER);

        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(50, 110, 160)),
                BorderFactory.createEmptyBorder(0, 0, 2, 0)
        ));

        return header;
    }


    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 248, 250));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(15, 20, 15, 20),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;


        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Color labelColor = new Color(60, 60, 60);


        // Row 0: Name
        txtName = createStyledTextField(25);
        row = addField(formPanel, gbc, "Name:", txtName, row, 1, 3, labelFont, labelColor);


        // Row 1: Last Name
        txtLastName = createStyledTextField(25);
        row = addField(formPanel, gbc, "Last Name:", txtLastName, row, 1, 1, labelFont, labelColor);


        // Sex ComboBox
        cmbSex = new JComboBox<>(new String[] { "Male", "Female" });
        customizeComboBox(cmbSex);


        gbc.gridx = 2;
        gbc.gridy = row-1;
        gbc.gridheight = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(cmbSex, gbc);
        gbc.gridheight = 1;
        gbc.gridwidth = 1;


        // Row 2: Middle Name
        txtMiddleName = createStyledTextField(25);
        row = addField(formPanel, gbc, "Middle Initial:", txtMiddleName, row, 1, 3, labelFont, labelColor);


        // Row 3: Suffix
        txtSuffix = createStyledTextField(25);
        row = addField(formPanel, gbc, "Suffix:", txtSuffix, row, 1, 3, labelFont, labelColor);


        // Row 4: Age and Birth Date
        txtAge = createStyledTextField(10);

        String dob = (currentResident != null && currentResident.getDob() != null) ?
                currentResident.getDob().toString() : "";
        cmbBirthDate = new JComboBox<>(new String[] { dob });
        customizeComboBox(cmbBirthDate);


        // Age Label
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(labelFont);
        ageLabel.setForeground(labelColor);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(ageLabel, gbc);


        // Age Field
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtAge, gbc);


        // Birth Date Label
        JLabel birthDateLabel = new JLabel("Birth Date:");
        birthDateLabel.setFont(labelFont);
        birthDateLabel.setForeground(labelColor);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(birthDateLabel, gbc);


        // Birth Date ComboBox
        gbc.gridx = 3;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbBirthDate, gbc);


        row++;


        // Row 5: Civil Status and Date
        txtCivilStatus = createStyledTextField(12);
        txtDate = createStyledTextField(12);

        // Civil Status Label
        JLabel civilLabel = new JLabel("Civil Status:");
        civilLabel.setFont(labelFont);
        civilLabel.setForeground(labelColor);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(civilLabel, gbc);

        // Civil Status Field
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtCivilStatus, gbc);

        // Date Label
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(labelFont);
        dateLabel.setForeground(labelColor);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(dateLabel, gbc);

        // Date Field
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtDate, gbc);

        row++;


        // Row 6: Per Year Income
        txtPerYearIncome = createStyledTextField(25);
        txtPerYearIncome.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                }


                String text = txtPerYearIncome.getText();
                if (c == '.' && text.contains(".")) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        row = addField(formPanel, gbc, "Per Year Income:", txtPerYearIncome, row, 1, 3, labelFont, labelColor);


        // Row 7: Purok
        dao = new SystemConfigDAO();
        String[] puroks = dao.getOptionsNature("purok");
        txtPurok = new JComboBox<>(puroks);
        customizeComboBox(txtPurok);

        JLabel purokLabel = new JLabel("Purok:");
        purokLabel.setFont(labelFont);
        purokLabel.setForeground(labelColor);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(purokLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPurok, gbc);

        row++;


        // Row 8: Street
        street = createStyledTextField(25);
        row = addField(formPanel, gbc, "Street:", street, row, 1, 3, labelFont, labelColor);


        // Row 9: Current Address
        txtCurrentAddress = createStyledTextField(25);
        row = addField(formPanel, gbc, "Current Address:", txtCurrentAddress, row, 1, 3, labelFont, labelColor);


        // Row 10: Purpose
        JLabel purposeLabel = new JLabel("Purpose:");
        purposeLabel.setFont(labelFont);
        purposeLabel.setForeground(labelColor);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(purposeLabel, gbc);

        txtPurpose = new JTextArea(4, 25);
        txtPurpose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPurpose.setLineWrap(true);
        txtPurpose.setWrapStyleWord(true);
        txtPurpose.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        txtPurpose.setBackground(Color.WHITE);

        JScrollPane purposeScrollPane = new JScrollPane(txtPurpose);
        purposeScrollPane.setBorder(null);
        purposeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        purposeScrollPane.setPreferredSize(new Dimension(0, 70));

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(purposeScrollPane, gbc);
        gbc.weighty = 0.0;

        row++;


        // Add vertical spacer
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        formPanel.add(Box.createVerticalGlue(), gbc);


        return formPanel;
    }


    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        textField.setBackground(Color.WHITE);
        return textField;
    }


    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(new Color(245, 248, 250));
        buttonPanel.setBorder(new EmptyBorder(12, 20, 15, 20));


        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setBackground(new Color(220, 80, 80));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 60, 60), 1),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            dispose();

        });

        cancelButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                cancelButton.setBackground(new Color(230, 100, 100));
                cancelButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 80, 80), 1),
                        BorderFactory.createEmptyBorder(8, 0, 8, 0)
                ));
            }
            public void mouseExited(MouseEvent e) {
                cancelButton.setBackground(new Color(220, 80, 80));
                cancelButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 60, 60), 1),
                        BorderFactory.createEmptyBorder(8, 0, 8, 0)
                ));
            }
        });


        JButton proceedButton = new JButton("PROCEED");
        proceedButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        proceedButton.setBackground(new Color(70, 130, 180));
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 110, 160), 1),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)
        ));
        proceedButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        proceedButton.addActionListener(e -> {
            if (!validateForm()) {
                return;
            }


            if (txtPurpose.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a purpose.");
                return;
            }


            collectFormData();
            DocumentType certificateOfIndigency = UserDataManager.getInstance().getDocumentTypeByName("Certificate of Indigency");


            if (certificateOfIndigency == null) {
                JOptionPane.showMessageDialog(this, "Error: Document Type 'Certificate of Indigency' not found in database settings.");
                return;
            }


            String purpose = txtPurpose.getText().toString() + " | Income: " + txtPerYearIncome.getText();
            UserDataManager.getInstance().residentRequestsDocument(currentResident, null, certificateOfIndigency, purpose);
            Resident currentResident = UserDataManager.getInstance().getCurrentResident();
            String name = currentResident.getFirstName() + " " + currentResident.getLastName();
            int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
            new SystemLogDAO().addLog("Request Document",name,staffId);
            JOptionPane.showMessageDialog(this,
                    "Certificate of Indigency request submitted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);


            printFormData();
            dispose();

        });

        proceedButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                proceedButton.setBackground(new Color(90, 150, 200));
                proceedButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                        BorderFactory.createEmptyBorder(8, 0, 8, 0)
                ));
            }
            public void mouseExited(MouseEvent e) {
                proceedButton.setBackground(new Color(70, 130, 180));
                proceedButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(50, 110, 160), 1),
                        BorderFactory.createEmptyBorder(8, 0, 8, 0)
                ));
            }
        });


        buttonPanel.add(cancelButton);
        buttonPanel.add(proceedButton);


        return buttonPanel;
    }


    // ✅ VALIDATE FORM DATA
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();


        if (txtName.getText().trim().isEmpty()) {
            errors.append("• Name is required\n");
        }
        if (txtLastName.getText().trim().isEmpty()) {
            errors.append("• Last Name is required\n");
        }
        if (txtAge.getText().trim().isEmpty()) {
            errors.append("• Age is required\n");
        }
        if (txtCurrentAddress.getText().trim().isEmpty()) {
            errors.append("• Current Address is required\n");
        }
        if (txtPurpose.getText().trim().isEmpty()) {
            errors.append("• Purpose is required\n");
        }
        if (txtPurpose.getText().length() > 250) {
            errors.append("• Purpose length should not exceed 250 \n");
        }
        if (txtPerYearIncome.getText().trim().isEmpty()) {
            errors.append("• Per Year Income is required\n");
        }


        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Please fix the following errors:\n\n" + errors.toString(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }


        return true;
    }


    // ✅ COLLECT DATA FROM FORM FIELDS
    private void collectFormData() {
        formData = new IndigencyFormData();


        formData.setFirstName(txtName.getText().trim());
        formData.setLastName(txtLastName.getText().trim());
        formData.setMiddleName(txtMiddleName.getText().trim());
        formData.setSuffix(txtSuffix.getText().trim());
        formData.setSex((String) cmbSex.getSelectedItem());
        formData.setAge(txtAge.getText().trim());
        formData.setBirthDate((String) cmbBirthDate.getSelectedItem());
        formData.setCivilStatus(txtCivilStatus.getText().trim());
        formData.setDate(txtDate.getText().trim());
        formData.setPerYearIncome(txtPerYearIncome.getText().trim());
        formData.setCurrentAddress(txtCurrentAddress.getText().trim());
        formData.setPurpose(txtPurpose.getText().trim());


        if (currentResident != null) {
            formData.setResidentId(currentResident.getResidentId());
        }


        System.out.println("✅ Form data collected successfully!");
    }


    // ✅ GET COLLECTED FORM DATA
    public IndigencyFormData getFormData() {
        return formData;
    }


    // ✅ PRINT FORM DATA
    private void printFormData() {
        if (formData != null) {
            System.out.println("\n========== INDIGENCY FORM DATA ==========");
            System.out.println("Name: " + formData.getFullName());
            System.out.println("Sex: " + formData.getSex());
            System.out.println("Age: " + formData.getAge());
            System.out.println("Birth Date: " + formData.getBirthDate());
            System.out.println("Civil Status: " + formData.getCivilStatus());
            System.out.println("Date: " + formData.getDate());
            System.out.println("Per Year Income: " + formData.getPerYearIncome());
            System.out.println("Current Address: " + formData.getCurrentAddress());
            System.out.println("Purpose: " + formData.getPurpose());
            System.out.println("Resident ID: " + formData.getResidentId());
            System.out.println("=========================================\n");
        }
    }


    private int addField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField, int row,
                         int colSpanField, int colSpanEmpty, Font labelFont, Color labelColor) {
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(labelColor);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(label, gbc);


        gbc.gridx = 1;
        gbc.gridwidth = colSpanField;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(textField, gbc);


        gbc.gridx = 1 + colSpanField;
        gbc.gridwidth = colSpanEmpty;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(Box.createHorizontalGlue(), gbc);


        return ++row;
    }


    private void customizeComboBox(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                label.setBorder(new EmptyBorder(3, 8, 3, 8));
                return label;
            }
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(false);


            IndigencyFormDialog dialog = new IndigencyFormDialog(frame);
            dialog.setVisible(true);


            IndigencyFormData data = dialog.getFormData();
            if (data != null) {
                System.out.println("Retrieved form data: " + data.getFullName());
            }


            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                    System.exit(0);
                }
            });
        });
    }
}


// ✅ DATA CLASS TO STORE FORM DATA
@Data
class IndigencyFormData {
    private int residentId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String sex;
    private String age;
    private String birthDate;
    private String civilStatus;
    private String date;
    private String perYearIncome;
    private String currentAddress;
    private String purpose;


    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) fullName.append(firstName);
        if (middleName != null && !middleName.isEmpty()) fullName.append(" ").append(middleName);
        if (lastName != null && !lastName.isEmpty()) fullName.append(" ").append(lastName);
        return fullName.toString().trim();
    }


    @Override
    public String toString() {
        return "IndigencyFormData{" +
                "residentId=" + residentId +
                ", name='" + getFullName() + '\'' +
                ", sex='" + sex + '\'' +
                ", age='" + age + '\'' +
                ", purpose='" + purpose + '\'' +
                '}';
    }
}



