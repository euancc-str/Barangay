package org.example.SerbisyongBarangay;

import lombok.Data;
import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Documents.DocumentType;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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
    private JTextField txtPurpose;
    private JTextField street;
    private JComboBox<String> txtPurok;
    // Store the current resident
    private Resident currentResident;

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
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        // --- Header Panel ---
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Form Panel (using GridBagLayout for robust alignment) ---
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // --- Buttons Panel ---
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
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

            System.out.println("✅ Loaded resident data: " + currentResident.getFirstName());
        } else {
            System.out.println("⚠️ No resident logged in");
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel(
                "<html><center><b>Please Fill Up the Form</b><br>Certificate of Indigency</center></html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Close button (X)
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setBackground(Color.WHITE);
        closeButton.setForeground(Color.BLACK);
        closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.addActionListener(e -> dispose());

        JPanel closeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButtonPanel.setOpaque(false);
        closeButtonPanel.add(closeButton);
        headerPanel.add(closeButtonPanel, BorderLayout.EAST);

        return headerPanel;
    }
    private SystemConfigDAO dao;
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ✅ 1. Name (Initialize instance variable)
        txtName = new JTextField(20);
        row = addField(formPanel, gbc, "Name:", txtName, row, 1, 3);

        // ✅ 2. Last Name
        txtLastName = new JTextField(20);
        row = addField(formPanel, gbc, "Last Name:", txtLastName, row, 1, 1);

        // ✅ 3. Sex ComboBox (Starting on row 1, spanning 2 rows)
        cmbSex = new JComboBox<>(new String[] { "Male", "Female" });
        customizeComboBox(cmbSex);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formPanel.add(cmbSex, gbc);

        gbc.gridheight = 1;
        gbc.gridwidth = 1;

        // ✅ 4. Middle Name
        txtMiddleName = new JTextField(20);
        row = addField(formPanel, gbc, "Middle Name:", txtMiddleName, row, 1, 3);

        // ✅ 5. Suffix
        txtSuffix = new JTextField(20);
        row = addField(formPanel, gbc, "Suffix:", txtSuffix, row, 1, 3);

        // ✅ 6. Age and Birth Date (Same Row)
        txtAge = new JTextField(10);
        cmbBirthDate = new JComboBox<>(new String[] {
             currentResident.getDob().toString()
        });
        customizeComboBox(cmbBirthDate);

        // Age Label (col 0)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Age:"), gbc);

        // Age Field (col 1)
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtAge, gbc);

        // Birth Date Label (col 2)
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Birth Date:"), gbc);

        // Birth Date ComboBox (col 3)
        gbc.gridx = 3;
        gbc.weightx = 0.7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbBirthDate, gbc);

        row++;

        // ✅ 7. Civil Status and Date
        txtCivilStatus = new JTextField(10);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Civil Status:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtCivilStatus, gbc);

        // Date (Right side)
        txtDate = new JTextField(10);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtDate, gbc);
        row++;

        // ✅ 8. Per year Income
        txtPerYearIncome = new JTextField(20);
        row = addField(formPanel, gbc, "Per year Income:", txtPerYearIncome, row, 1, 3);

        // ✅ 9. Current Address
        txtCurrentAddress = new JTextField(20);
        row = addField(formPanel, gbc, "Current Address:", txtCurrentAddress, row, 1, 3);
        dao=new SystemConfigDAO();
        String [] puroks = dao.getOptionsNature("purok");
        txtPurok = new JComboBox<>(puroks);
        customizeComboBox(txtPurok);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Purok:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPurok, gbc);
        row++;
        street = new JTextField(20);
        row = addField(formPanel, gbc, "Street:", street, row, 1, 3);

        // ✅ 10. Purpose
        txtPurpose = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Purpose:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPurpose, gbc);
        row++;

        // Filler
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        formPanel.add(Box.createVerticalGlue(), gbc);
        JPanel stackPanel = new JPanel();
        stackPanel.setLayout(new BoxLayout(stackPanel, BoxLayout.Y_AXIS));
        stackPanel.setBackground(Color.WHITE);


        return formPanel;
    }

    private int addField(JPanel panel, GridBagConstraints gbc, String labelText, JTextField textField, int row,
                         int colSpanField, int colSpanEmpty) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

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

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setBackground(new Color(220, 50, 50));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 18));
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> {
            dispose();
            requestaDocumentFrame frame = new requestaDocumentFrame();
            frame.setVisible(true);
        });

        JButton proceedButton = new JButton("PROCEED");
        proceedButton.setBackground(new Color(90, 180, 90));
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setFont(new Font("Arial", Font.BOLD, 18));
        proceedButton.setBorderPainted(false);
        proceedButton.setFocusPainted(false);
        proceedButton.addActionListener(e -> {
            // ✅ COLLECT AND VALIDATE DATA
            if (validateForm()) {
                collectFormData();
                DocumentType certificateOfIndigency = UserDataManager.getInstance().getDocumentTypeByName("Certificate of Indigency");
                String purpose = txtPurpose.getText().toString();
                UserDataManager.getInstance().residentRequestsDocument(currentResident,null,certificateOfIndigency,purpose);
                JOptionPane.showMessageDialog(this,
                        "Indigency certificate request submitted successfully!\n\n" +
                                "Applicant: " + formData.getFullName() + "\n" +
                                "Purpose: " + formData.getPurpose(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Print collected data (for debugging)
                printFormData();

                // TODO: Save to database
                // saveToDatabase();

                dispose();
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

        // Store resident ID if available

        if (currentResident != null) {
            formData.setResidentId(currentResident.getResidentId());
        }

        System.out.println("✅ Form data collected successfully!");
    }

    // ✅ GET COLLECTED FORM DATA (Call this from outside)
    public IndigencyFormData getFormData() {
        return formData;
    }

    // ✅ PRINT FORM DATA (For debugging)
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

    private void customizeComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                label.setBorder(new EmptyBorder(5, 5, 5, 5));
                return label;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(false);

            IndigencyFormDialog dialog = new IndigencyFormDialog(frame);
            dialog.setVisible(true);

            // Access form data after dialog closes
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