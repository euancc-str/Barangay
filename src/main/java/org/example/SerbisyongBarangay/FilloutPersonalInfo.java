package org.example.SerbisyongBarangay;

import org.example.UserDataManager;
import org.example.Users.Resident;
import org.example.utils.ResourceUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class FilloutPersonalInfo extends JFrame {

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 750;

    private JTextField txtFirstName;
    private JTextField txtMiddleName;
    private JTextField txtLastName;
    private JTextField txtSuffix;
    private JComboBox<String> cmbSex;
    private JComboBox<String> cmbCivilStatus;
    private JTextField txtCitizenship;
    private JComboBox<String> cmbMonth;
    private JComboBox<String> cmbDay;
    private JComboBox<String> cmbYear;
    private JTextField txtAge;
    private JTextField txtAddress;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtIdNo;
    private JComboBox<String> cmbIdType;

    private Resident currentResident;

    FilloutPersonalInfo() {
        currentResident = UserDataManager.getInstance().getCurrentResident();

        if (currentResident == null) {
            JOptionPane.showMessageDialog(this,
                    "No user session found. Please login first.",
                    "Session Error",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
            return;
        }

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setLocationRelativeTo(null);
        setTitle("Serbisyong Barangay");
        setLayout(new BorderLayout());

        JPanel headerPanel = headerPanel();
        JPanel mainPanel = mainPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);


        loadUserDataIntoForm();

        setVisible(true);
    }

    // ✅ Load existing data into form fields
    private void loadUserDataIntoForm() {
        if (currentResident != null) {
            txtFirstName.setText(currentResident.getFirstName() != null ? currentResident.getFirstName() : "");
            txtMiddleName.setText(currentResident.getMiddleName() != null ? currentResident.getMiddleName() : "");
            txtLastName.setText(currentResident.getLastName() != null ? currentResident.getLastName() : "");
            txtSuffix.setText(currentResident.getSuffix() != null ? currentResident.getSuffix() : "N/A");

            if (currentResident.getGender() != null) {
                cmbSex.setSelectedItem(currentResident.getGender());
            }

            if (currentResident.getCivilStatus() != null) {
                cmbCivilStatus.setSelectedItem(currentResident.getCivilStatus());
            }

            txtCitizenship.setText("Filipino");
            txtAge.setText(String.valueOf(currentResident.getAge()));
            txtAddress.setText(currentResident.getAddress() != null ? currentResident.getAddress() : "");
            txtEmail.setText(currentResident.getEmail() != null ? currentResident.getEmail() : "");
            txtIdNo.setText(currentResident.getNationalId() != null ? currentResident.getNationalId() : "");
            LocalDate dob = currentResident.getDob();
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
            System.out.println("✅ Loaded data for: " + currentResident.getFirstName());
        }
    }

    private JLabel createRequiredLabel(String text) {
        return new JLabel("<html>" + text + " <font color='red'>*</font></html>");
    }

    protected JPanel mainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.white);
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 650));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel innerCard = new JPanel(new GridBagLayout());
        innerCard.setBackground(Color.white);
        innerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.black, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 0;

        // Header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.white);
        JLabel headerLabel = new JLabel("Personal Information", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel line = new JPanel();
        line.setPreferredSize(new Dimension(1, 1));
        line.setBackground(Color.black);

        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(line, BorderLayout.SOUTH);
        innerCard.add(headerPanel, gbc);

        // Row 1: Name Labels
        gbc.gridwidth = 1;
        gbc.weightx = 0.33;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 10, 3, 10);
        gbc.gridx = 0;
        innerCard.add(createRequiredLabel("First Name"), gbc);
        gbc.gridx = 1;
        innerCard.add(createRequiredLabel("Middle Name"), gbc);
        gbc.gridx = 2;
        innerCard.add(createRequiredLabel("Last Name"), gbc);

        // Row 2: Name Fields - ✅ USE INSTANCE VARIABLES
        gbc.gridy = 2;
        gbc.insets = new Insets(3, 10, 10, 10);
        gbc.gridx = 0;
        txtFirstName = new JTextField(15);
        innerCard.add(txtFirstName, gbc);

        gbc.gridx = 1;
        txtMiddleName = new JTextField(15);
        innerCard.add(txtMiddleName, gbc);

        gbc.gridx = 2;
        txtLastName = new JTextField(15);
        innerCard.add(txtLastName, gbc);

        // Row 3: Suffix, Sex, Civil Status, Citizenship Labels
        gbc.weightx = 0.25;
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 3, 10);
        gbc.gridx = 0;
        innerCard.add(new JLabel("Suffix"), gbc);
        gbc.gridx = 1;
        innerCard.add(createRequiredLabel("Sex"), gbc);
        gbc.gridx = 2;
        innerCard.add(createRequiredLabel("Civil Status"), gbc);
        gbc.gridx = 3;
        innerCard.add(createRequiredLabel("Citizenships"), gbc);

        // Row 4: Fields - ✅ USE INSTANCE VARIABLES
        gbc.gridy = 4;
        gbc.insets = new Insets(3, 10, 10, 10);
        gbc.gridx = 0;
        txtSuffix = new JTextField("N/A", 10);
        innerCard.add(txtSuffix, gbc);

        gbc.gridx = 1;
        cmbSex = new JComboBox<>(new String[] { "Male", "Female" });
        innerCard.add(cmbSex, gbc);

        gbc.gridx = 2;
        cmbCivilStatus = new JComboBox<>(new String[] { "Single", "Married", "Widowed", "Separated", "Divorced" });
        innerCard.add(cmbCivilStatus, gbc);

        gbc.gridx = 3;
        txtCitizenship = new JTextField("Filipino", 10);
        innerCard.add(txtCitizenship, gbc);

        // Birth Date and Age
        gbc.weightx = 0.25;
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 10, 3, 10);
        gbc.gridx = 0;
        innerCard.add(createRequiredLabel("Birth Date"), gbc);
        gbc.gridx = 3;
        innerCard.add(new JLabel("Age"), gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(3, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridwidth = 3;

        String[] days = new String[31];
        for (int i = 1, j = 0; j < days.length; i++, j++) {
            days[j] = String.valueOf(i);
        }

        String[] year = new String[126];
        for (int i = 2025, j = 0; j < year.length; i--, j++) {
            year[j] = String.valueOf(i);
        }

        JPanel datePanel = new JPanel(new GridLayout(1, 3, 5, 0));
        cmbMonth = new JComboBox<>(new String[] { "January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December" });
        cmbDay = new JComboBox<>(days);
        cmbYear = new JComboBox<>(year);
        datePanel.add(cmbMonth);
        datePanel.add(cmbDay);
        datePanel.add(cmbYear);
        innerCard.add(datePanel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 3;
        txtAge = new JTextField(10);
        innerCard.add(txtAge, gbc);

        // Contact Information Header
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(20, 0, 10, 0);

        JPanel contactHeaderPanel = new JPanel(new BorderLayout());
        contactHeaderPanel.setBackground(Color.white);
        JLabel contactHeaderLabel = new JLabel("Contact Information", SwingConstants.CENTER);
        contactHeaderLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel line2 = new JPanel();
        line2.setPreferredSize(new Dimension(1, 1));
        line2.setBackground(Color.black);

        contactHeaderPanel.add(contactHeaderLabel, BorderLayout.CENTER);
        contactHeaderPanel.add(line2, BorderLayout.SOUTH);
        innerCard.add(contactHeaderPanel, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 4;
        gbc.weighty = 0;
        gbc.insets = new Insets(10, 10, 3, 10);
        innerCard.add(createRequiredLabel("Street/Barangay/Municipality"), gbc);

        gbc.gridy = 10;
        gbc.insets = new Insets(3, 10, 10, 10);
        txtAddress = new JTextField(40);
        innerCard.add(txtAddress, gbc);

        // Phone, Email, ID Labels
        gbc.gridwidth = 1;
        gbc.weightx = 0.33;
        gbc.gridy = 11;
        gbc.insets = new Insets(10, 10, 3, 10);
        gbc.gridx = 0;
        innerCard.add(createRequiredLabel("Phone Number"), gbc);
        gbc.gridx = 1;
        innerCard.add(new JLabel("Email Address"), gbc);
        gbc.gridx = 2;
        innerCard.add(new JLabel("ID NO."), gbc);

        // Phone, Email, ID Fields
        gbc.gridy = 12;
        gbc.insets = new Insets(3, 10, 10, 10);
        gbc.gridx = 0;
        txtPhone = new JTextField(15);
        innerCard.add(txtPhone, gbc);

        gbc.gridx = 1;
        txtEmail = new JTextField(15);
        innerCard.add(txtEmail, gbc);

        gbc.gridx = 2;
        txtIdNo = new JTextField(15);
        innerCard.add(txtIdNo, gbc);

        // ID Type and Photo ID
        gbc.gridy = 13;
        gbc.insets = new Insets(10, 10, 3, 10);
        gbc.gridx = 0;
        innerCard.add(new JLabel("ID TYPE"), gbc);
        gbc.gridx = 1;
        innerCard.add(new JLabel("Photo ID."), gbc);

        gbc.gridy = 14;
        gbc.insets = new Insets(3, 10, 10, 10);

        String[] validPhilippineIDs = {
                "Philippine Identification (PhilID) Card / ePhilID",
                "Philippine Passport",
                "Driver's License (LTO)",
                "Unified Multi-Purpose ID (UMID) Card",
                "Professional Regulation Commission (PRC) ID",
                "Government Service Insurance System (GSIS) e-Card",
                "Social Security System (SSS) Card",
                "Postal ID (PVC Plastic Card)",
                "Integrated Bar of the Philippines (IBP) ID",
                "Alien Certificate of Registration (ACR) / ICR",
                "Voter's ID / Voter's Certification (COMELEC)",
                "Taxpayer Identification Number (TIN) Card",
                "National Bureau of Investigation (NBI) Clearance",
                "Police Clearance / Police ID",
                "Overseas Workers Welfare Administration (OWWA) ID / OFW ID",
                "Senior Citizen ID (OSCA/LGU)",
                "Person With Disability (PWD) ID (NCDA/LGU)",
                "PhilHealth ID",
                "Pag-IBIG Loyalty Card / MID Card",
                "Barangay Certification / Barangay ID",
                "Company ID (Current Employer)",
                "School ID (Current Student)",
                "Seaman's Book / Seafarer's Record Book (SIRB)"
        };

        gbc.gridx = 0;
        cmbIdType = new JComboBox<>(validPhilippineIDs);
        innerCard.add(cmbIdType, gbc);

        gbc.gridx = 1;
        JButton uploadButton = new JButton("↑");
        uploadButton.setFont(new Font("Arial", Font.BOLD, 18));
        uploadButton.setBackground(Color.lightGray);
        uploadButton.setFocusPainted(false);
        innerCard.add(uploadButton, gbc);

        // ✅ DONE BUTTON - COLLECT DATA FROM FORM FIELDS
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;

        JButton doneButton = new JButton("Done");
        doneButton.setPreferredSize(new Dimension(100, 35));
        doneButton.setBackground(new Color(0, 0, 200));
        doneButton.setForeground(Color.white);
        doneButton.setFont(new Font("Arial", Font.BOLD, 14));
        doneButton.setFocusPainted(false);

        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String firstName = txtFirstName.getText().trim();
                String middleName = txtMiddleName.getText().trim();
                String lastName = txtLastName.getText().trim();
                String suffix = txtSuffix.getText().trim();
                String sex = (String) cmbSex.getSelectedItem();
                String civilStatus = (String) cmbCivilStatus.getSelectedItem();
                String citizenship = txtCitizenship.getText().trim();
                String age = txtAge.getText().trim();
                String address = txtAddress.getText().trim();
                String phone = txtPhone.getText().trim();
                String email = txtEmail.getText().trim();
                String idNo = txtIdNo.getText().trim();
                String idType = (String) cmbIdType.getSelectedItem();
                //
                if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(FilloutPersonalInfo.this,
                            "Please fill all required fields (marked with *)!",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                //aa
                Resident updatedResident = currentResident.toBuilder()
                        .firstName(firstName)
                        .middleName(middleName)
                        .lastName(lastName)
                        .suffix(suffix)
                        .gender(sex)
                        .civilStatus(civilStatus)
                        .age(Integer.parseInt(age))
                        .address(address)
                        .contactNo(phone)
                        .email(email)
                        .nationalId(idNo)
                        .updatedAt(java.time.LocalDateTime.now())
                        .suffix(suffix)
                        .build();

                // save to db
                UserDataManager.getInstance().updateResident(updatedResident);

                //UPDATE SESSION
                UserDataManager.getInstance().setCurrentResident(updatedResident);

                System.out.println("✅ Updated resident: " + firstName + " " + lastName);

                // Show confirmation
                ensureallinformationarecorrectframe ensure = new ensureallinformationarecorrectframe(
                        FilloutPersonalInfo.this);
                ensure.setVisible(true);
            }
        });

        innerCard.add(doneButton, gbc);

        panel.add(innerCard, BorderLayout.NORTH);
        return panel;
    }

    protected JPanel headerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.black);
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel();
        try {
            Image logoImage = new ImageIcon(ResourceUtils.getResourceAsBytes("serbisyongBarangayLogo.jpg")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(logoImage));
        } catch (Exception e) {
            logoLabel.setText("LOGO");
            logoLabel.setForeground(Color.WHITE);
        }
        leftPanel.add(logoLabel);

        JLabel titleLabel = new JLabel("Serbisyong Barangay");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 30));
        titleLabel.setForeground(Color.white);
        leftPanel.add(titleLabel);

        panel.add(leftPanel, BorderLayout.WEST);

        JButton backButton = new JButton("\u2B05");
        backButton.setFont(new Font("Inter", Font.BOLD, 30));
        backButton.setBackground(Color.black);
        backButton.setForeground(Color.white);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5, true));
        backButton.setPreferredSize(new Dimension(50, 50));
        backButton.addActionListener(e -> {
            dispose();
            userMain user = new userMain();
            user.setVisible(true);
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(backButton);

        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }
}