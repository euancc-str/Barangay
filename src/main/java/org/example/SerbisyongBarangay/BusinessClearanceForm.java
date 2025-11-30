package org.example.SerbisyongBarangay;

import java.util.List;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Documents.DocumentType;
import org.example.ResidentDAO;
import org.example.UserDataManager;
import org.example.Users.Resident;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;

public class BusinessClearanceForm extends JPanel {

    private JTextField txtFirstName;
    private JTextField txtLastName;
    private JTextField txtMiddleName;
    private JTextField txtSuffix;
    private JTextField txtAge;
    private JTextField txtCivilStatus;
    private JTextField txtBirthDate;
    private JTextField txtBusinessNature;
    private JTextField txtCurrentAddress;
    private JTextField txtBusinessAddress;
    private JTextField street;
    private JComboBox<String> txtPurok;


    private JTextField cbSex;
    private JComboBox<String> cbOwnership;
    private JComboBox<String> cbBirthYear;
    private JTextField txtCtcNumber;
    private JTextField txtCtcDateIssued;
    private JTextField txtCtcPlaceIssued;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BusinessClearanceForm().createAndShowGUI());
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormFieldsPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("Please Fill Up the Form", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel subtitleLabel = new JLabel("Business Clearance", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        JButton closeButton = new JButton("\u2715");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setForeground(Color.BLACK);
        closeButton.setBackground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true));

        closeButton.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(closeButton);
            parentFrame.dispose();
        });

        headerPanel.add(titlePanel, BorderLayout.CENTER);

        JPanel closeButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeButtonWrapper.setBackground(Color.WHITE);
        closeButtonWrapper.add(closeButton);
        headerPanel.add(closeButtonWrapper, BorderLayout.EAST);

        return headerPanel;
    }

    // -----------------------------------------------------------------------
    // --- FORM FIELDS CONSTRUCTION ---
    // -----------------------------------------------------------------------
    private SystemConfigDAO dao;
    private JPanel createFormFieldsPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // 1. Get Data
        Resident resident = UserDataManager.getInstance().getCurrentResident();

        // 2. Initialize Variables with Data
        txtFirstName = createStyledTextField(resident.getFirstName());
        txtLastName = createStyledTextField(resident.getLastName());
        String middleName = resident.getMiddleName() != null ?resident.getMiddleName() : "";
        String suffix = resident.getSuffix() != null? resident.getSuffix():"";
        txtMiddleName = createStyledTextField(middleName); // Default empty if not available
        txtSuffix = createStyledTextField(suffix);
        txtAge = createStyledTextField(String.valueOf(resident.getAge()));


        // 3. Name Panel Layout
        JPanel namePanel = new JPanel(new GridLayout(5, 1, 0, 10));
        namePanel.setBackground(Color.WHITE);

        namePanel.add(wrapFieldWithLabel("Name:", txtFirstName));
        namePanel.add(wrapFieldWithLabel("Last Name:", txtLastName));
        namePanel.add(wrapFieldWithLabel("Middle Name:", txtMiddleName));
        namePanel.add(wrapFieldWithLabel("Suffix:", txtSuffix));
        namePanel.add(wrapFieldWithLabel("Age:", txtAge));

        // 4. Top Wrapper (Name + Sex Dropdown)
        GridBagLayout gbl = new GridBagLayout();
        JPanel topWrapper = new JPanel(gbl);
        topWrapper.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.gridheight = 5;
        topWrapper.add(namePanel, gbc);

        // Initialize Sex Dropdown
        cbSex =  createStyledTextField(resident.getGender());
        namePanel.add(wrapFieldWithLabel("Sex:", cbSex));


        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.01; gbc.weighty = 0.01;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;

        formPanel.add(topWrapper);
        formPanel.add(Box.createVerticalStrut(15));

        // 5. Civil Status & Date Row
        JPanel statusRow = new JPanel(new GridLayout(1, 2, 10, 0));
        statusRow.setBackground(Color.WHITE);

        txtCivilStatus = createStyledTextField("");
        statusRow.add(wrapFieldWithLabel("Civil Status:", txtCivilStatus));

        // Date Field Complex Logic
        txtBirthDate = new JTextField();
        txtBirthDate.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        txtBirthDate.setText(String.valueOf(resident.getDob()));
        cbBirthYear = createDropdown(new String[]{String.valueOf(LocalDate.now())}); // Mock data
        txtCivilStatus.setText(resident.getCivilStatus());
        statusRow.add(createComplexDateField("Birth Date:", txtBirthDate, cbBirthYear));

        formPanel.add(statusRow);
        formPanel.add(Box.createVerticalStrut(10));

        // 6. Ownership
        JPanel ownershipRow = new JPanel(new GridLayout(1, 1));
        ownershipRow.setBackground(Color.WHITE);
        dao = new SystemConfigDAO();
        String [] nature =  dao.getOptionsNature("natureOfBusiness");

        cbOwnership = createDropdown(nature);
        cbOwnership.setPreferredSize(new Dimension(150, 25));

        // Custom wrapping for ownership line
        JPanel ownPanel = new JPanel(new BorderLayout(5, 0));
        ownPanel.setBackground(Color.WHITE);
        JLabel lblOwn = new JLabel("Nature of Business ownerships:");
        lblOwn.setFont(new Font("Arial", Font.PLAIN, 14));
        ownPanel.add(lblOwn, BorderLayout.WEST);

        JPanel dropWrap = new JPanel(new BorderLayout());
        dropWrap.setBackground(Color.WHITE);
        dropWrap.add(cbOwnership, BorderLayout.EAST);
        ownPanel.add(dropWrap, BorderLayout.CENTER);

        ownershipRow.add(ownPanel);
        formPanel.add(ownershipRow);
        formPanel.add(Box.createVerticalStrut(10));

        // 7. Nature of Business & Address
        txtBusinessNature = createStyledTextField("");
        formPanel.add(wrapFieldWithLabel("Nature of Business:", txtBusinessNature));
        formPanel.add(Box.createVerticalStrut(10));

        txtCurrentAddress = createStyledTextField(resident.getAddress()); // Pre-fill address
        formPanel.add(wrapFieldWithLabel("Current Address:", txtCurrentAddress));
        formPanel.add(Box.createVerticalStrut(10));

        String [] puroks = dao.getOptionsNature("purok");
        txtPurok = createDropdown(puroks);
        txtPurok.setSelectedItem(resident.getPurok());
        formPanel.add(wrapFieldWithLabel("Purok:", txtPurok));
        formPanel.add(Box.createVerticalStrut(10));

        street = createStyledTextField(resident.getStreet());
        formPanel.add(wrapFieldWithLabel("Street:", street));
        formPanel.add(Box.createVerticalStrut(10));

        txtBusinessAddress = createStyledTextField("");
        formPanel.add(wrapFieldWithLabel("Business Address:", txtBusinessAddress));
        formPanel.add(Box.createVerticalStrut(20)); JPanel stackPanel = new JPanel();
        stackPanel.setLayout(new BoxLayout(stackPanel, BoxLayout.Y_AXIS));
        stackPanel.setBackground(Color.WHITE);
        JPanel ctcPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 2 Columns
        ctcPanel.setBackground(Color.WHITE);
        ctcPanel.setBorder(BorderFactory.createTitledBorder("Community Tax Certificate (Cedula)"));

        // 1. CTC Number
        txtCtcNumber = new JTextField();
        // Retrieve existing data if available
        txtCtcNumber.setText(resident != null && resident.getCtcNumber() != null ? resident.getCtcNumber() : "");
        ctcPanel.add(new JLabel("CTC Number:"));
        ctcPanel.add(txtCtcNumber);

        txtCtcDateIssued = new JTextField();
        String ctcDate = (resident != null && resident.getCtcDateIssued() != null)
                ? resident.getCtcDateIssued().toString() : "";
        txtCtcDateIssued.setText(ctcDate);
        ctcPanel.add(new JLabel("Date Issued (yyyy-MM-dd):"));
        ctcPanel.add(txtCtcDateIssued);

        // 3. Place Issued
        txtCtcPlaceIssued = new JTextField();
        String ctcPlace = new SystemConfigDAO().getConfig("defaultCtcPlace");
        txtCtcPlaceIssued.setText(ctcPlace);
        ctcPanel.add(new JLabel("Place Issued:"));
        ctcPanel.add(txtCtcPlaceIssued);

        // Add this new panel to your main form layout
        gbc.gridx = 0;

        gbc.gridwidth = 4; // Span full width
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(ctcPanel, gbc);
        cbSex.setEditable(false);
        txtPurok.setEditable(false);
        txtAge.setEditable(false);
        txtBirthDate.setEditable(false);
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtMiddleName.setEditable(false);

        return formPanel;
    }

    // --- VISUAL HELPERS ---

    private JTextField createStyledTextField(String text) {
        JTextField textField = new JTextField(text);
        textField.setPreferredSize(new Dimension(200, 20));
        textField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        // Add internal padding so text doesn't touch label area logic
        return textField;
    }

    private JComboBox<String> createDropdown(String[] options) {
        JComboBox<String> cb = new JComboBox<>(options);
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        return cb;
    }

    // Combines a Label and a PRE-CREATED Component into a Panel
    private JPanel wrapFieldWithLabel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, BorderLayout.WEST);

        // Add some padding to the field container if needed
        if(field instanceof JTextField) {
            Border padding = BorderFactory.createEmptyBorder(0, 10, 0, 0);
            field.setBorder(BorderFactory.createCompoundBorder(field.getBorder(), padding));
        }

        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel wrapDropdownWithLabel(String labelText, JComboBox<String> box) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(120, 25));

        panel.add(new JLabel(labelText), BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createComplexDateField(String labelText, JTextField txt, JComboBox<String> cb) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);

        JPanel left = new JPanel(new BorderLayout(5, 0));
        left.setBackground(Color.WHITE);
        left.add(new JLabel(labelText), BorderLayout.WEST);
        left.add(txt, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(100, 25));
        right.add(cb, BorderLayout.EAST);

        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // -----------------------------------------------------------------------
    // --- DATA RETRIEVAL ON BUTTON CLICK ---
    // -----------------------------------------------------------------------

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setBackground(new Color(223, 50, 50));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 20));
        cancelButton.setPreferredSize(new Dimension(0, 50));
        cancelButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        cancelButton.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(cancelButton);
            parentFrame.dispose();
            requestaDocumentFrame frame = new requestaDocumentFrame();
            frame.setVisible(true);
        });

        JButton proceedButton = new JButton("PROCEED");
        proceedButton.setBackground(new Color(100, 180, 50));
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setFont(new Font("Arial", Font.BOLD, 20));
        proceedButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        // --- HERE IS THE RETRIEVAL LOGIC ---
        proceedButton.addActionListener(e -> {
            // 1. Retrieve Data from Class-Level Variables
            String fName = txtFirstName.getText();
            String lName = txtLastName.getText();
            String age = txtAge.getText();
            String busNature = txtBusinessNature.getText();
            String busAddr = txtBusinessAddress.getText();
            String sex = cbSex.getText();
            String ownership = (String) cbOwnership.getSelectedItem();

            // 2. Basic Validation
            if(busAddr.isEmpty() || busNature.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill in all Business Details.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. "Send" to System (Print to console for now)
            System.out.println("--- Submitting Request ---");
            System.out.println("Applicant: " + fName + " " + lName + " (" + age + ")");
            System.out.println("Sex: " + sex);
            System.out.println("Business: " + busNature + " (" + ownership + ")");
            System.out.println("Location: " + busAddr);
            String purpose = busNature +
                    " Ownership Type: " + ownership;
            String ctcNum = txtCtcNumber.getText().trim();
            String ctcDate = txtCtcDateIssued.getText().trim();
            String ctcPlace = txtCtcPlaceIssued.getText().trim();
            ResidentDAO dao = new ResidentDAO();

            dao.updateResidentCedula(UserDataManager.getInstance().getCurrentResident().getResidentId(), ctcNum, ctcDate, ctcPlace);
            DocumentType certificateOfIndigency = UserDataManager.getInstance().getDocumentTypeByName("Business Clearance");

            UserDataManager.getInstance().residentRequestsDocument(UserDataManager.getInstance().getCurrentResident(), null,certificateOfIndigency,purpose);
            JOptionPane.showMessageDialog(null, "Request Submitted Successfully!");

            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(proceedButton);
            parentFrame.dispose();
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(proceedButton);

        return buttonPanel;
    }
}