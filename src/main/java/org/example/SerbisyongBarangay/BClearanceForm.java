package org.example.SerbisyongBarangay;

import lombok.Data;
import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Documents.DocumentType;
import org.example.ResidentDAO;
import org.example.UserDataManager;
import org.example.Users.Resident;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BClearanceForm extends JDialog {

    // Fields
    private JTextField txtName, txtLastName, txtMiddleName, txtSuffix;
    private JComboBox<String> cmbSex;
    private JTextField txtAge;
    private JComboBox<String> cmbBirthDate;
    private JTextField txtCivilStatus, txtDate, txtPerYearIncome, txtCurrentAddress, txtPurpose;
    private JTextField txtCtcNumber;

    private JTextField txtCtcPlaceIssued;
    // --- NEW FIELDS ---
    private JTextField street;
    private JComboBox<String> txtPurok;
    private JComboBox<String> cmbCtcMonth, cmbCtcDay, cmbCtcYear;

    private Resident currentResident;
    private BClearanceFormData formData;

    public BClearanceForm() {
        // Use null owner for generic usage
        super((Frame) null, "Barangay Clearance Form", true);
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
        setLocationRelativeTo(null);


        loadResidentData();
    }




    // --- DATA RETRIEVAL ---
    private void loadResidentData() {
        if (currentResident != null) {
            txtName.setText(currentResident.getFirstName() != null ? currentResident.getFirstName() : "");
            txtLastName.setText(currentResident.getLastName() != null ? currentResident.getLastName() : "");
            txtMiddleName.setText(currentResident.getMiddleName() != null ? currentResident.getMiddleName() : "");
            txtAge.setText(String.valueOf(currentResident.getAge()));
            if (currentResident.getGender() != null) cmbSex.setSelectedItem(currentResident.getGender());
            if (currentResident.getCivilStatus() != null) txtCivilStatus.setText(currentResident.getCivilStatus());
            if (currentResident.getSuffix() != null) txtSuffix.setText(currentResident.getSuffix());
            street.setText(currentResident.getStreet());
            txtCurrentAddress.setText(currentResident.getAddress() != null ? currentResident.getAddress() : "");
            txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            txtPurok.setSelectedItem(currentResident.getPurok());
            // Load CTC Info
            txtCtcNumber.setText(currentResident.getCtcNumber() != null ? currentResident.getCtcNumber() : "CC");

            // --- LOAD CTC DATE INTO DROPDOWNS ---
            try {
                Object ctcDateObj = currentResident.getCtcDateIssued();
                if (ctcDateObj != null) {
                    String dateStr = ctcDateObj.toString();
                    // Check if it's not "null" string and not empty
                    if (!dateStr.equalsIgnoreCase("null") && !dateStr.trim().isEmpty()) {
                        LocalDate ctcDate;
                        if (ctcDateObj instanceof java.sql.Date) {
                            ctcDate = ((java.sql.Date) ctcDateObj).toLocalDate();
                        } else {
                            ctcDate = LocalDate.parse(dateStr);
                        }

                        if (cmbCtcMonth.getItemCount() > 0) cmbCtcMonth.setSelectedIndex(ctcDate.getMonthValue() - 1);
                        cmbCtcDay.setSelectedItem(String.valueOf(ctcDate.getDayOfMonth()));
                        cmbCtcYear.setSelectedItem(String.valueOf(ctcDate.getYear()));
                    }
                }
            } catch (Exception e) {
                // Ignore parsing errors, leave dropdowns as default
                System.err.println("Error loading CTC Date: " + e.getMessage());
            }
            // Set Date
            SystemConfigDAO config = new SystemConfigDAO();
            String defaultPlace = config.getConfig("defaultCtcPlace");
            txtCtcPlaceIssued.setText(defaultPlace);
            txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        }
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        // 1. Name Fields
        txtName = new JTextField(20);
        row = addField(formPanel, gbc, "Name:", txtName, row, 1, 3);

        txtLastName = new JTextField(20);
        row = addField(formPanel, gbc, "Last Name:", txtLastName, row, 1, 1);

        // Sex
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

        // Age & Birthdate
        txtAge = new JTextField(10);
        String dob = (currentResident != null && currentResident.getDob() != null) ? currentResident.getDob().toString() : "";
        cmbBirthDate = new JComboBox<>(new String[] { dob });
        customizeComboBox(cmbBirthDate);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtAge, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Birth Date:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.7; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbBirthDate, gbc);
        row++;

        // Civil Status & Date
        txtCivilStatus = new JTextField(10);
        txtDate = new JTextField(10);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Civil Status:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtCivilStatus, gbc);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtDate, gbc);
        row++;

        // --- NEW FIELDS: STREET & PUROK ---
        street = new JTextField(20);
        row = addField(formPanel, gbc, "Street:", street, row, 1, 3);

        String[] puroks = new SystemConfigDAO().getOptionsNature("purok");
        txtPurok = new JComboBox<>(puroks);
        customizeComboBox(txtPurok);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Purok:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPurok, gbc);
        row++;

        // Address & Purpose
        txtCurrentAddress = new JTextField(20);
        row = addField(formPanel, gbc, "Current Address:", txtCurrentAddress, row, 1, 3);

        txtPurpose = new JTextField(20);
        row = addField(formPanel, gbc, "Purpose:", txtPurpose, row, 1, 3);

        // Filler
        gbc.gridx = 0; gbc.gridy = row; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.VERTICAL;
        formPanel.add(Box.createVerticalGlue(), gbc);

        // --- CEDULA SECTION (UPDATED) ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        formPanel.add(new JSeparator(), gbc);
        row++;

        JLabel lblCtc = new JLabel("Community Tax Certificate (Cedula) Details");
        lblCtc.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridy = row;
        formPanel.add(lblCtc, gbc);
        row++;

        txtCtcNumber = new JTextField(20);
        row = addField(formPanel, gbc, "CTC Number:", txtCtcNumber, row, 1, 3);
        txtCtcNumber.setText("CC");
        // --- DATE DROPDOWNS ---
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        cmbCtcMonth = new JComboBox<>(months);

        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        cmbCtcDay = new JComboBox<>(days);

        int currentYear = LocalDate.now().getYear();
        String[] years = new String[10];
        for (int i = 0; i < 10; i++) years[i] = String.valueOf(currentYear - i);
        cmbCtcYear = new JComboBox<>(years);

        customizeComboBox(cmbCtcMonth);
        customizeComboBox(cmbCtcDay);
        customizeComboBox(cmbCtcYear);

        JPanel ctcDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ctcDatePanel.setBackground(Color.WHITE);
        ctcDatePanel.add(cmbCtcMonth);
        ctcDatePanel.add(cmbCtcDay);
        ctcDatePanel.add(cmbCtcYear);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Date Issued:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(ctcDatePanel, gbc);
        row++;

        txtCtcPlaceIssued = new JTextField(20);
        row = addField(formPanel, gbc, "Place Issued:", txtCtcPlaceIssued, row, 1, 3);

        // --- PHOTO SECTION ---
        addPhotoSection(formPanel, gbc, row);

        return formPanel;
    }

    private void addPhotoSection(JPanel formPanel, GridBagConstraints gbc, int row) {
        JPanel stackPanel = new JPanel();
        stackPanel.setLayout(new BoxLayout(stackPanel, BoxLayout.Y_AXIS));
        stackPanel.setBackground(Color.WHITE);

        JLabel lblPhoto = new JLabel();
        lblPhoto.setPreferredSize(new Dimension(100, 100));
        lblPhoto.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        lblPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);

        PhotoDAO resDao = new PhotoDAO();
        int residentId = currentResident.getResidentId();
        String currentPhotoPath = resDao.getPhotoPath(residentId);
        ImageUtils.displayImage(lblPhoto, currentPhotoPath, 100, 100);

        JButton btnUpload = new JButton("Add / Edit Photo");
        btnUpload.setFont(new Font("Arial", Font.PLAIN, 11));
        btnUpload.setAlignmentX(Component.CENTER_ALIGNMENT);

        Runnable uploadAction = () -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png"));
            if (chooser.showOpenDialog(formPanel) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                String newFileName = ImageUtils.saveImage(selectedFile, String.valueOf(residentId));
                resDao.updateResidentPhoto(residentId, newFileName);
                ImageUtils.displayImage(lblPhoto, newFileName, 100, 100);
                JOptionPane.showMessageDialog(formPanel, "Photo Updated!");
            }
        };

        lblPhoto.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { uploadAction.run(); } });
        btnUpload.addActionListener(e -> uploadAction.run());

        stackPanel.add(lblPhoto);
        stackPanel.add(Box.createVerticalStrut(5));
        stackPanel.add(btnUpload);

        JPanel photoWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        photoWrapper.setBackground(Color.WHITE);
        photoWrapper.add(stackPanel);

        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(photoWrapper, gbc);
    }

    private SystemConfigDAO dao;
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setBackground(new Color(220, 50, 50));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> dispose());

        JButton proceedButton = new JButton("PROCEED");
        proceedButton.setBackground(new Color(90, 180, 90));
        proceedButton.setForeground(Color.WHITE);
        proceedButton.addActionListener(e -> {
            if (validateForm()) {
                collectFormData();
                String ctcNum = txtCtcNumber.getText().trim();
                String ctcDate = null;
                if (cmbCtcMonth.isEnabled() && !ctcNum.isEmpty()) {
                    try {
                        String mStr = (String) cmbCtcMonth.getSelectedItem();
                        int m = java.time.Month.valueOf(mStr.toUpperCase()).getValue();
                        String d = (String) cmbCtcDay.getSelectedItem();
                        String y = (String) cmbCtcYear.getSelectedItem();
                        ctcDate = y + "-" + String.format("%02d", m) + "-" + String.format("%02d", Integer.parseInt(d));
                    } catch (Exception ex) {
                        ctcDate = null; // Fallback if date parsing fails
                    }
                }
                ResidentDAO dao = new ResidentDAO();
                String fullDetails = txtPurpose.getText();
                dao.updateResidentCedula(currentResident.getResidentId(), ctcNum.isEmpty() ? null : ctcNum, ctcDate);
                DocumentType docType = UserDataManager.getInstance().getDocumentTypeByName("Barangay Clearance");UserDataManager.getInstance().residentRequestsDocument(currentResident, null, docType, fullDetails);

                JOptionPane.showMessageDialog(this, "Barangay Clearance request submitted!");
                dispose();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(proceedButton);
        return buttonPanel;
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty() || street.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void collectFormData() {
        formData = new BClearanceFormData();
        formData.setStreet(street.getText().trim());
        formData.setPurok(txtPurok.getSelectedItem().toString());
        // ... collect other data if needed for local storage
    }

    // Helpers
    private int addField(JPanel p, GridBagConstraints c, String l, JTextField t, int r, int w, int e) {
        c.gridx = 0; c.gridy = r; c.gridwidth = 1; c.weightx = 0; p.add(new JLabel(l), c);
        c.gridx = 1; c.gridwidth = w; c.weightx = 1.0; p.add(t, c);
        c.gridx = 1 + w; c.gridwidth = e; c.weightx = 0; p.add(Box.createHorizontalGlue(), c);
        return ++r;
    }

    private void customizeComboBox(JComboBox<?> cb) {
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    }

    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout()); h.setBackground(Color.WHITE); h.setBorder(new EmptyBorder(10,15,10,15));
        JLabel l = new JLabel("<html><center><b>Barangay Clearance Form</b></center></html>");
        l.setFont(new Font("Arial", Font.BOLD, 18)); l.setHorizontalAlignment(JLabel.CENTER);
        h.add(l, BorderLayout.CENTER);
        return h;
    }
}

@Data
class BClearanceFormData {
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
    private String purok;
    private String yearsResiding;
    private String street;
    private String citizenship;
}