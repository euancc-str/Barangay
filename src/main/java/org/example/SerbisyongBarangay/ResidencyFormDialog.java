package org.example.SerbisyongBarangay;

import lombok.Data;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Documents.DocumentType;
import org.example.UserDataManager;
import org.example.Users.Resident;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ResidencyFormDialog extends JDialog {

    // --- FORM FIELDS ---
    private JTextField txtName;
    private JTextField txtLastName;
    private JTextField txtMiddleName;
    private JTextField txtSuffix;
    private JComboBox<String> cmbSex;
    private JTextField txtAge;
    private JComboBox<String> cmbBirthDate;
    private JTextField txtCivilStatus;
    private JTextField txtDate;
    private JTextField txtCitizenship;     // NEW
    private JTextField txtYearsResiding;   // NEW
    private JTextField txtCurrentAddress;
    private JTextField street;
    private JComboBox<String> txtPurok;
    private JTextField txtPurpose;

    // Data
    private Resident currentResident;
    private ResidencyFormData formData;

    public ResidencyFormDialog(Frame owner) {
        super(owner, "Certificate of Residency Form", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);

        currentResident = UserDataManager.getInstance().getCurrentResident();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(owner);

        loadResidentData();
    }

    private void loadResidentData() {
        if (currentResident != null) {
            txtName.setText(getString(currentResident.getFirstName()));
            txtLastName.setText(getString(currentResident.getLastName()));
            txtMiddleName.setText(getString(currentResident.getMiddleName()));
            txtSuffix.setText(getString(currentResident.getSuffix()));
            txtAge.setText(String.valueOf(currentResident.getAge()));
            street.setText(currentResident.getStreet());
            if (currentResident.getGender() != null) cmbSex.setSelectedItem(currentResident.getGender());
            if (currentResident.getDob() != null) cmbBirthDate.setSelectedItem(currentResident.getDob().toString());

            txtCivilStatus.setText(getString(currentResident.getCivilStatus()));
            txtCurrentAddress.setText(getString(currentResident.getAddress()));

            // Load Purok if available
            if (currentResident.getPurok() != null) txtPurok.setSelectedItem(currentResident.getPurok());

            // Defaults
            txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            txtCitizenship.setText("Filipino"); // Default
        }
    }
    private SystemConfigDAO dao;
    private String getString(String str) { return str != null ? str : ""; }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        // 1. Names
        txtName = new JTextField(20);
        row = addField(formPanel, gbc, "Name:", txtName, row, 1, 3);

        txtLastName = new JTextField(20);
        row = addField(formPanel, gbc, "Last Name:", txtLastName, row, 1, 1);

        // Sex (Spanning)
        cmbSex = new JComboBox<>(new String[] { "Male", "Female" });
        customizeComboBox(cmbSex);
        gbc.gridx = 2; gbc.gridy = 1; gbc.gridheight = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(cmbSex, gbc);
        gbc.gridheight = 1; gbc.gridwidth = 1;

        txtMiddleName = new JTextField(20);
        row = addField(formPanel, gbc, "Middle Name:", txtMiddleName, row, 1, 3);

        txtSuffix = new JTextField(20);
        row = addField(formPanel, gbc, "Suffix:", txtSuffix, row, 1, 3);

        // 2. Age & Birth Date
        txtAge = new JTextField(10);
        String dob = (currentResident != null && currentResident.getDob() != null) ? currentResident.getDob().toString() : "";
        cmbBirthDate = new JComboBox<>(new String[] { dob });
        customizeComboBox(cmbBirthDate);

        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=1; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        formPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx=1; gbc.weightx=0.3; gbc.fill=GridBagConstraints.HORIZONTAL;
        formPanel.add(txtAge, gbc);

        gbc.gridx=2; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        formPanel.add(new JLabel("Birth Date:"), gbc);
        gbc.gridx=3; gbc.weightx=0.7; gbc.fill=GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbBirthDate, gbc);
        row++;

        // 3. Civil Status & Date
        txtCivilStatus = new JTextField(10);
        txtDate = new JTextField(10);
        gbc.gridx=0; gbc.gridy=row; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        formPanel.add(new JLabel("Civil Status:"), gbc);
        gbc.gridx=1; gbc.weightx=0.5; gbc.fill=GridBagConstraints.HORIZONTAL;
        formPanel.add(txtCivilStatus, gbc);

        gbc.gridx=2; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx=3; gbc.weightx=0.5; gbc.fill=GridBagConstraints.HORIZONTAL;
        formPanel.add(txtDate, gbc);
        row++;

        // 4. Citizenship & Years (Specific to Residency)
        txtCitizenship = new JTextField(20);
        row = addField(formPanel, gbc, "Citizenship:", txtCitizenship, row, 1, 3);

        txtYearsResiding = new JTextField(20);
        row = addField(formPanel, gbc, "Years Residing:", txtYearsResiding, row, 1, 3);

        // 5. Address
        street = new JTextField(20);
        row = addField(formPanel, gbc, "Street:", street, row, 1, 3);
        dao=new SystemConfigDAO();
        String[] puroks = dao.getOptionsNature("purok");
        txtPurok = new JComboBox<>(puroks);
        customizeComboBox(txtPurok);
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=1; gbc.weightx=0; gbc.fill=GridBagConstraints.NONE;
        formPanel.add(new JLabel("Purok:"), gbc);
        gbc.gridx=1; gbc.gridwidth=3; gbc.weightx=1.0; gbc.fill=GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPurok, gbc);
        row++;

        txtCurrentAddress = new JTextField(20);
        row = addField(formPanel, gbc, "Full Address:", txtCurrentAddress, row, 1, 3);

        // 6. Purpose
        txtPurpose = new JTextField(20);
        row = addField(formPanel, gbc, "Purpose:", txtPurpose, row, 1, 3);

        // Spacer
        gbc.gridx=0; gbc.gridy=row; gbc.weighty=1.0; gbc.fill=GridBagConstraints.VERTICAL;
        formPanel.add(Box.createVerticalGlue(), gbc);

        // NOTE: No Photo Section here!

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton btnCancel = new JButton("CANCEL");
        btnCancel.setBackground(new Color(220, 50, 50));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("Arial", Font.BOLD, 18));
        btnCancel.addActionListener(e ->{ dispose();
            requestaDocumentFrame frame = new requestaDocumentFrame();
            frame.setVisible(true);
         });

        JButton btnProceed = new JButton("PROCEED");
        btnProceed.setBackground(new Color(90, 180, 90));
        btnProceed.setForeground(Color.WHITE);
        btnProceed.setFont(new Font("Arial", Font.BOLD, 18));

        btnProceed.addActionListener(e -> {
            if (validateForm()) {
                collectFormData();
                DocumentType docType = UserDataManager.getInstance().getDocumentTypeByName("Certificate of Residency");

                // Append residency details to purpose for printing
                String purpose = txtPurpose.getText() +
                        " | Years: " + formData.getYearsResiding() +
                        " | Citizenship: " + formData.getCitizenship();

                UserDataManager.getInstance().residentRequestsDocument(currentResident, null, docType, purpose);

                JOptionPane.showMessageDialog(this, "Request Submitted Successfully!");
                System.out.println(formData.toString());
                dispose();
            }
        });

        panel.add(btnCancel);
        panel.add(btnProceed);
        return panel;
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty() || txtYearsResiding.getText().trim().isEmpty() || txtPurpose.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void collectFormData() {
        formData = new ResidencyFormData();
        formData.setFirstName(txtName.getText().trim());
        formData.setLastName(txtLastName.getText().trim());
        formData.setCitizenship(txtCitizenship.getText().trim());
        formData.setYearsResiding(txtYearsResiding.getText().trim());
        formData.setStreet(street.getText().trim());
        formData.setPurok(txtPurok.getSelectedItem().toString());
        formData.setPurpose(txtPurpose.getText().trim());
    }

    // Helpers
    private int addField(JPanel p, GridBagConstraints c, String l, JTextField t, int r, int w, int e) {
        c.gridx=0; c.gridy=r; c.gridwidth=1; c.weightx=0; c.fill=GridBagConstraints.NONE; p.add(new JLabel(l), c);
        c.gridx=1; c.gridwidth=w; c.weightx=1.0; c.fill=GridBagConstraints.HORIZONTAL; p.add(t, c);
        c.gridx=1+w; c.gridwidth=e; c.weightx=0; c.fill=GridBagConstraints.NONE; p.add(Box.createHorizontalGlue(), c);
        return ++r;
    }

    private void customizeComboBox(JComboBox<?> cb) {
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }

    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout()); h.setBackground(Color.WHITE); h.setBorder(new EmptyBorder(10,15,10,15));
        JLabel l = new JLabel("<html><center><b>Please Fill Up</b><br>Certificate of Residency</center></html>");
        l.setFont(new Font("Arial", Font.BOLD, 18)); l.setHorizontalAlignment(JLabel.CENTER);
        h.add(l, BorderLayout.CENTER);
        return h;
    }
}

@Data
class ResidencyFormData {
    private String firstName;
    private String lastName;
    private String citizenship;
    private String yearsResiding;
    private String street;
    private String purok;
    private String purpose;

    public String getFullName() { return firstName + " " + lastName; }
}