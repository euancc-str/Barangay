package org.example.SerbisyongBarangay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Admin.SystemLogDAO;
import org.example.Documents.DocumentType;
import org.example.ResidentDAO;
import org.example.UserDataManager;
import org.example.Users.Resident;

import lombok.Data;

public class BClearanceForm extends JDialog {

    // Fields
    private JTextField txtName, txtLastName, txtMiddleName, txtSuffix;
    private JComboBox<String> cmbSex;
    private JTextField txtAge;
    private JComboBox<String> cmbBirthDate;
    private JTextField txtCivilStatus, txtDate, txtPerYearIncome, txtCurrentAddress;
    private JTextArea txtPurpose;
    private JTextField txtCtcNumber;
    private JTextField txtCtcPlaceIssued;
    private JTextField street;
    private JComboBox<String> txtPurok;
    private JComboBox<String> cmbCtcMonth, cmbCtcDay, cmbCtcYear;

    private Resident currentResident;
    private BClearanceFormData formData;

    public BClearanceForm() {
        super((Frame) null, "Barangay Clearance Form", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);

        currentResident = UserDataManager.getInstance().getCurrentResident();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 248, 250));
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(null);

        loadResidentData();
    }

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
            txtCtcNumber.setText(currentResident.getCtcNumber() != null ? currentResident.getCtcNumber() : "");

            try {
                Object ctcDateObj = currentResident.getCtcDateIssued();
                if (ctcDateObj != null) {
                    String dateStr = ctcDateObj.toString();
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
                System.err.println("Error loading CTC Date: " + e.getMessage());
            }

            SystemConfigDAO config = new SystemConfigDAO();
            String defaultPlace = config.getConfig("defaultCtcPlace");
            txtCtcPlaceIssued.setText(defaultPlace);
            txtDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            txtAge.setEditable(false);
            txtDate.setEditable(false);
            cmbBirthDate.setEditable(false);
        }
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 248, 250));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 15, 10, 15),
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 11);
        Color labelColor = new Color(60, 60, 60);

        // Row 0: Name
        txtName = createStyledTextField(15);
        row = addField(formPanel, gbc, "Name:", txtName, row, 1, 3, labelFont, labelColor);

        // Row 1: Last Name & Sex
        txtLastName = createStyledTextField(15);
        row = addField(formPanel, gbc, "Last Name:", txtLastName, row, 1, 1, labelFont, labelColor);

        cmbSex = new JComboBox<>(new String[] { "Male", "Female" });
        customizeComboBox(cmbSex);
        gbc.gridx = 2; gbc.gridy = row-1; gbc.gridheight = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(cmbSex, gbc);
        gbc.gridheight = 1; gbc.gridwidth = 1;

        // Row 2: Middle Name
        txtMiddleName = createStyledTextField(15);
        row = addField(formPanel, gbc, "Middle Initial:", txtMiddleName, row, 1, 3, labelFont, labelColor);

        // Row 3: Suffix
        txtSuffix = createStyledTextField(15);
        row = addField(formPanel, gbc, "Suffix:", txtSuffix, row, 1, 3, labelFont, labelColor);

        // Row 4: Age & Birthdate
        txtAge = createStyledTextField(6);
        String dob = (currentResident != null && currentResident.getDob() != null) ? currentResident.getDob().toString() : "";
        cmbBirthDate = new JComboBox<>(new String[] { dob });
        customizeComboBox(cmbBirthDate);

        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(labelFont);
        ageLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(ageLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtAge, gbc);

        JLabel birthDateLabel = new JLabel("Birth Date:");
        birthDateLabel.setFont(labelFont);
        birthDateLabel.setForeground(labelColor);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(birthDateLabel, gbc);
        gbc.gridx = 3; gbc.weightx = 0.7; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbBirthDate, gbc);
        row++;

        // Row 5: Civil Status & Date
        txtCivilStatus = createStyledTextField(8);
        txtDate = createStyledTextField(8);

        JLabel civilLabel = new JLabel("Civil Status:");
        civilLabel.setFont(labelFont);
        civilLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(civilLabel, gbc);
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtCivilStatus, gbc);

        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(labelFont);
        dateLabel.setForeground(labelColor);
        gbc.gridx = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(dateLabel, gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtDate, gbc);
        row++;

        // Row 6: Street
        street = createStyledTextField(15);
        row = addField(formPanel, gbc, "Street:", street, row, 1, 3, labelFont, labelColor);

        // Row 7: Purok
        String[] puroks = new SystemConfigDAO().getOptionsNature("purok");
        txtPurok = new JComboBox<>(puroks);
        customizeComboBox(txtPurok);

        JLabel purokLabel = new JLabel("Purok:");
        purokLabel.setFont(labelFont);
        purokLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(purokLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPurok, gbc);
        row++;

        // Row 8: Current Address
        txtCurrentAddress = createStyledTextField(15);
        row = addField(formPanel, gbc, "Current Address:", txtCurrentAddress, row, 1, 3, labelFont, labelColor);

        // Row 9: Purpose
        JLabel purposeLabel = new JLabel("Purpose:");
        purposeLabel.setFont(labelFont);
        purposeLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(purposeLabel, gbc);

        txtPurpose = new JTextArea(2, 15);
        txtPurpose.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtPurpose.setLineWrap(true);
        txtPurpose.setWrapStyleWord(true);
        txtPurpose.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        txtPurpose.setBackground(Color.WHITE);

        JScrollPane purposeScrollPane = new JScrollPane(txtPurpose);
        purposeScrollPane.setBorder(null);
        purposeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        purposeScrollPane.setPreferredSize(new Dimension(0, 45));

        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gbc.weighty = 0.1; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(purposeScrollPane, gbc);
        gbc.weighty = 0.0;
        row++;

        // Row 10: CTC Separator
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4; gbc.weighty = 0;
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(70, 130, 180));
        formPanel.add(separator, gbc);
        row++;

        // Row 11: CTC Title
        JLabel lblCtc = new JLabel("Community Tax Certificate (Cedula) Details");
        lblCtc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCtc.setForeground(new Color(70, 130, 180));
        gbc.gridy = row;
        formPanel.add(lblCtc, gbc);
        row++;

        // Row 12: CTC Number
        txtCtcNumber = createStyledTextField(15);
        row = addField(formPanel, gbc, "CTC Number:", txtCtcNumber, row, 1, 3, labelFont, labelColor);

        txtCtcNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();

                if (!Character.isDigit(c) && c != ' ') {
                    e.consume();
                }

                // 2. LIMIT LENGTH (Max 15 characters)
                if (txtCtcNumber.getText().length() >= 15) {
                    e.consume();
                }
            }
        });
        // Row 13: CTC Date
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        cmbCtcMonth = new JComboBox<>(months);

        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        cmbCtcDay = new JComboBox<>(days);

        int currentYear = LocalDate.now().getYear();
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) years[i] = String.valueOf(currentYear - i);
        cmbCtcYear = new JComboBox<>(years);

        customizeComboBox(cmbCtcMonth);
        customizeComboBox(cmbCtcDay);
        customizeComboBox(cmbCtcYear);

        JPanel ctcDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        ctcDatePanel.setBackground(new Color(245, 248, 250));
        ctcDatePanel.add(cmbCtcMonth);
        ctcDatePanel.add(cmbCtcDay);
        ctcDatePanel.add(cmbCtcYear);

        JLabel ctcDateLabel = new JLabel("Date Issued:");
        ctcDateLabel.setFont(labelFont);
        ctcDateLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        formPanel.add(ctcDateLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(ctcDatePanel, gbc);
        row++;

        // Row 14: Place Issued
        txtCtcPlaceIssued = createStyledTextField(15);
        row = addField(formPanel, gbc, "Place Issued:", txtCtcPlaceIssued, row, 1, 3, labelFont, labelColor);

        // Row 15: Photo Section
        txtAge.setEditable(false);
        txtDate.setEditable(false);
        txtLastName.setEditable(false);
        txtSuffix.setEditable(false);
        cmbBirthDate.setEnabled(false);
        txtCurrentAddress.setEditable(false);
        cmbSex.setEnabled(false);
        street.setEditable(false);
        txtPurok.setEnabled(false);
        txtCivilStatus.setEditable(false);
        txtMiddleName.setEditable(false);
        txtName.setEditable(false);
        addPhotoSection(formPanel, gbc, row);

        return formPanel;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        textField.setBackground(Color.WHITE);
        return textField;
    }

    private void addPhotoSection(JPanel formPanel, GridBagConstraints gbc, int row) {
        JPanel stackPanel = new JPanel();
        stackPanel.setLayout(new BoxLayout(stackPanel, BoxLayout.Y_AXIS));
        stackPanel.setBackground(new Color(245, 248, 250));

        JLabel lblPhoto = new JLabel();
        lblPhoto.setPreferredSize(new Dimension(80, 80));
        lblPhoto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
                BorderFactory.createLineBorder(Color.WHITE, 1)
        ));
        lblPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);

        PhotoDAO resDao = new PhotoDAO();
        int residentId = currentResident.getResidentId();
        String currentPhotoPath = resDao.getPhotoPath(residentId);
        ImageUtils.displayImage(lblPhoto, currentPhotoPath, 80, 80);

        JButton btnUpload = new JButton("Add/Edit Photo");
        btnUpload.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnUpload.setBackground(new Color(70, 130, 180));
        btnUpload.setForeground(Color.WHITE);
        btnUpload.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 110, 160), 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        btnUpload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpload.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnUpload.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnUpload.setBackground(new Color(90, 150, 200));
            }
            public void mouseExited(MouseEvent e) {
                btnUpload.setBackground(new Color(70, 130, 180));
            }
        });

        Runnable uploadAction = () -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png"));
            if (chooser.showOpenDialog(formPanel) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                String newFileName = ImageUtils.saveImage(selectedFile, String.valueOf(residentId));
                resDao.updateResidentPhoto(residentId, newFileName);
                ImageUtils.displayImage(lblPhoto, newFileName, 80, 80);
                JOptionPane.showMessageDialog(formPanel, "Photo Updated!");
            }
        };

        lblPhoto.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { uploadAction.run(); }
        });
        btnUpload.addActionListener(e -> uploadAction.run());

        stackPanel.add(lblPhoto);
        stackPanel.add(Box.createVerticalStrut(4));
        stackPanel.add(btnUpload);

        JPanel photoWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        photoWrapper.setBackground(new Color(245, 248, 250));
        photoWrapper.add(stackPanel);

        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(photoWrapper, gbc);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(new Color(245, 248, 250));
        buttonPanel.setBorder(new EmptyBorder(8, 15, 10, 15));

        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        cancelButton.setBackground(new Color(220, 80, 80));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 60, 60), 1),
                BorderFactory.createEmptyBorder(6, 0, 6, 0)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        cancelButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                cancelButton.setBackground(new Color(230, 100, 100));
            }
            public void mouseExited(MouseEvent e) {
                cancelButton.setBackground(new Color(220, 80, 80));
            }
        });

        JButton proceedButton = new JButton("PROCEED");
        proceedButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        proceedButton.setBackground(new Color(70, 130, 180));
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 110, 160), 1),
                BorderFactory.createEmptyBorder(6, 0, 6, 0)
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
            if(txtPurpose.getText().length() > 250){
                JOptionPane.showMessageDialog(this, "Error! Purpose is too long amd should not exceed 250 \nPurpose length:"+ txtPurpose.getText().length());
                return;
            }

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
                    ctcDate = null;
                }
            }

            try {
                ResidentDAO dao = new ResidentDAO();
                String fullDetails = txtPurpose.getText();

                dao.updateResidentCedula(currentResident.getResidentId(), ctcNum.isEmpty() ? null : "CC"+ctcNum, ctcDate);

                DocumentType docType = UserDataManager.getInstance().getDocumentTypeByName("Barangay Clearance");

                if (docType == null) {
                    JOptionPane.showMessageDialog(this, "Error: Document Type 'Barangay Clearance' not found in database settings.");
                    return;
                }

                UserDataManager.getInstance().residentRequestsDocument(currentResident, null, docType, fullDetails);
                String name = currentResident.getFirstName() + " " + currentResident.getLastName();
                int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
                new SystemLogDAO().addLog("Request Document",name,staffId);
                JOptionPane.showMessageDialog(this, "Barangay Clearance request submitted!");
                this.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
            }
        });

        proceedButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                proceedButton.setBackground(new Color(90, 150, 200));
            }
            public void mouseExited(MouseEvent e) {
                proceedButton.setBackground(new Color(70, 130, 180));
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
    }

    private int addField(JPanel p, GridBagConstraints c, String labelText, JTextField t, int r, int w, int e, Font labelFont, Color labelColor) {
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        label.setForeground(labelColor);
        c.gridx = 0; c.gridy = r; c.gridwidth = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        p.add(label, c);
        c.gridx = 1; c.gridwidth = w; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        p.add(t, c);
        c.gridx = 1 + w; c.gridwidth = e; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        p.add(Box.createHorizontalGlue(), c);
        return ++r;
    }

    private void customizeComboBox(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
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

        header.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        header.setPreferredSize(new Dimension(0, 45));

        JLabel title = new JLabel("<html><center><b>BARANGAY CLEARANCE FORM</b><br>" +
                "<span style='font-size:9px; font-weight:normal;'>Official Document Request</span></center></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(JLabel.CENTER);

        header.add(title, BorderLayout.CENTER);

        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(50, 110, 160)),
                BorderFactory.createEmptyBorder(0, 0, 1, 0)
        ));

        return header;
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
