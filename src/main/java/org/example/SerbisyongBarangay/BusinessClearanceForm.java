package org.example.SerbisyongBarangay;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;


import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Admin.SystemLogDAO;
import org.example.Documents.DocumentType;
import org.example.ResidentDAO;
import org.example.UserDataManager;
import org.example.Users.Resident;


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


    private JComboBox<String> cmbCtcMonth, cmbCtcDay, cmbCtcYear;
    private JTextField cbSex;
    private JComboBox<String> cbOwnership;
    private JComboBox<String> cbBirthYear;
    private JTextField txtCtcNumber;
    private JTextField txtCtcPlaceIssued;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BusinessClearanceForm().createAndShowGUI());
    }


    public void createAndShowGUI() {
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.getRootPane().setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));


        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
        mainPanel.setBackground(new Color(245, 248, 250));
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));


        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createFormFieldsPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);


        frame.add(mainPanel);
        frame.pack();
        frame.setSize(800, 700); // Set a reasonable fixed size
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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
        header.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        header.setPreferredSize(new Dimension(0, 55));


        JLabel title = new JLabel("<html><center><b>BUSINESS CLEARANCE FORM</b><br>" +
                "<span style='font-size:11px; font-weight:normal;'>Please Fill Up the Form</span></center></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(JLabel.CENTER);
        header.add(title, BorderLayout.CENTER);


        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(50, 110, 160)),
                BorderFactory.createEmptyBorder(0, 0, 1, 0)
        ));


        return header;
    }


    private SystemConfigDAO dao;
    private JPanel createFormFieldsPanel() {
        // Use JScrollPane to ensure content fits
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(245, 248, 250));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(15, 30, 15, 30));


        // 1. Get Data
        Resident resident = UserDataManager.getInstance().getCurrentResident();


        // 2. Initialize Variables with Data - make fields smaller
        txtFirstName = createStyledTextField(resident.getFirstName(), 15);
        txtLastName = createStyledTextField(resident.getLastName(), 15);
        String middleName = resident.getMiddleName() != null ? resident.getMiddleName() : "";
        String suffix = resident.getSuffix() != null ? resident.getSuffix() : "";
        txtMiddleName = createStyledTextField(middleName, 10);
        txtSuffix = createStyledTextField(suffix, 5);
        txtAge = createStyledTextField(String.valueOf(resident.getAge()), 5);


        // 3. Compact Name Panel Layout
        JPanel namePanel = new JPanel(new GridLayout(3, 2, 10, 10)); // Changed to 3x2 grid
        namePanel.setBackground(new Color(245, 248, 250));
        namePanel.setMaximumSize(new Dimension(700, 120));


        namePanel.add(wrapFieldWithLabel("Name:", txtFirstName));
        namePanel.add(wrapFieldWithLabel("Last Name:", txtLastName));
        namePanel.add(wrapFieldWithLabel("Middle initial:", txtMiddleName));
        namePanel.add(wrapFieldWithLabel("Suffix:", txtSuffix));
        namePanel.add(wrapFieldWithLabel("Age:", txtAge));

        // Add Sex field to name panel
        cbSex = createStyledTextField(resident.getGender(), 8);
        namePanel.add(wrapFieldWithLabel("Sex:", cbSex));


        formPanel.add(namePanel);
        formPanel.add(Box.createVerticalStrut(10));


        // 4. Civil Status & Date Row - make more compact
        JPanel statusRow = new JPanel(new GridLayout(1, 2, 10, 0));
        statusRow.setBackground(new Color(245, 248, 250));
        statusRow.setMaximumSize(new Dimension(700, 40));


        txtCivilStatus = createStyledTextField("", 10);
        txtCivilStatus.setText(resident.getCivilStatus());
        statusRow.add(wrapFieldWithLabel("Civil Status:", txtCivilStatus));


        txtBirthDate = createStyledTextField("", 10);
        txtBirthDate.setText(String.valueOf(resident.getDob()));
        cbBirthYear = createDropdown(new String[]{String.valueOf(LocalDate.now())});
        cbBirthYear.setPreferredSize(new Dimension(80, 25));

        JPanel birthDatePanel = new JPanel(new BorderLayout(5, 0));
        birthDatePanel.setBackground(new Color(245, 248, 250));
        birthDatePanel.add(new JLabel("Birth Date:"), BorderLayout.WEST);
        birthDatePanel.add(txtBirthDate, BorderLayout.CENTER);
        birthDatePanel.add(cbBirthYear, BorderLayout.EAST);

        statusRow.add(birthDatePanel);


        formPanel.add(statusRow);
        formPanel.add(Box.createVerticalStrut(10));


        // 5. Ownership - make more compact
        JPanel ownershipRow = new JPanel(new BorderLayout());
        ownershipRow.setBackground(new Color(245, 248, 250));
        ownershipRow.setMaximumSize(new Dimension(700, 40));

        dao = new SystemConfigDAO();
        String[] nature = dao.getOptionsNature("natureOfBusiness");
        cbOwnership = createDropdown(nature);
        customizeComboBox(cbOwnership);
        cbOwnership.setPreferredSize(new Dimension(150, 25));


        JLabel lblOwn = new JLabel("Nature of Business ownerships:");
        lblOwn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblOwn.setForeground(new Color(60, 60, 60));

        ownershipRow.add(lblOwn, BorderLayout.WEST);
        ownershipRow.add(cbOwnership, BorderLayout.EAST);


        formPanel.add(ownershipRow);
        formPanel.add(Box.createVerticalStrut(10));


        // 6. Nature of Business & Address - make fields smaller
        txtBusinessNature = createStyledTextField("", 20);
        JPanel businessNaturePanel = wrapFieldWithLabel("Nature of Business Details:", txtBusinessNature);
        businessNaturePanel.setMaximumSize(new Dimension(700, 40));
        formPanel.add(businessNaturePanel);
        formPanel.add(Box.createVerticalStrut(10));


        txtCurrentAddress = createStyledTextField(resident.getAddress(), 20);
        JPanel currentAddressPanel = wrapFieldWithLabel("Current Address:", txtCurrentAddress);
        currentAddressPanel.setMaximumSize(new Dimension(700, 40));
        formPanel.add(currentAddressPanel);
        formPanel.add(Box.createVerticalStrut(10));


        String[] puroks = dao.getOptionsNature("purok");
        txtPurok = createDropdown(puroks);
        customizeComboBox(txtPurok);
        txtPurok.setSelectedItem(resident.getPurok());
        txtPurok.setPreferredSize(new Dimension(100, 25));

        JPanel purokPanel = new JPanel(new BorderLayout(5, 0));
        purokPanel.setBackground(new Color(245, 248, 250));
        purokPanel.setMaximumSize(new Dimension(700, 40));
        purokPanel.add(new JLabel("Purok:"), BorderLayout.WEST);
        purokPanel.add(txtPurok, BorderLayout.CENTER);
        formPanel.add(purokPanel);
        formPanel.add(Box.createVerticalStrut(10));


        street = createStyledTextField(resident.getStreet(), 20);
        JPanel streetPanel = wrapFieldWithLabel("Street:", street);
        streetPanel.setMaximumSize(new Dimension(700, 40));
        formPanel.add(streetPanel);
        formPanel.add(Box.createVerticalStrut(10));


        txtBusinessAddress = createStyledTextField("", 20);
        JPanel businessAddressPanel = wrapFieldWithLabel("Business Address:", txtBusinessAddress);
        businessAddressPanel.setMaximumSize(new Dimension(700, 40));
        formPanel.add(businessAddressPanel);
        formPanel.add(Box.createVerticalStrut(20));


        // 7. Separator and CTC Title
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(70, 130, 180));
        separator.setBackground(new Color(70, 130, 180));
        formPanel.add(separator);
        formPanel.add(Box.createVerticalStrut(10));


        JLabel ctcTitle = new JLabel("Community Tax Certificate (Cedula) Details");
        ctcTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ctcTitle.setForeground(new Color(70, 130, 180));
        ctcTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(ctcTitle);
        formPanel.add(Box.createVerticalStrut(10));


        // 8. CTC Details - make more compact
        txtCtcNumber = createStyledTextField(resident.getCtcNumber() != null ? resident.getCtcNumber() : "", 15);
        JPanel ctcNumberPanel = wrapFieldWithLabel("CTC Number:", txtCtcNumber);
        // ✅ UPDATED LISTENER: Allows Digits AND Spaces (e.g., "2025 01234")
        txtCtcNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != ' ') {
                    e.consume();
                }
                if (txtCtcNumber.getText().length() >= 15) {
                    e.consume();
                }
            }
        });
        ctcNumberPanel.setMaximumSize(new Dimension(700, 40));
        formPanel.add(ctcNumberPanel);
        formPanel.add(Box.createVerticalStrut(5));


        // DATE DROPDOWNS - make compact
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        cmbCtcMonth = new JComboBox<>(months);
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        cmbCtcDay = new JComboBox<>(days);
        int curYear = LocalDate.now().getYear();
        String[] years = new String[5]; // Reduced from 10 to 5 years
        for (int i = 0; i < 5; i++) years[i] = String.valueOf(curYear - i);
        cmbCtcYear = new JComboBox<>(years);


        customizeComboBox(cmbCtcMonth);
        customizeComboBox(cmbCtcDay);
        customizeComboBox(cmbCtcYear);

        cmbCtcMonth.setPreferredSize(new Dimension(70, 25));
        cmbCtcDay.setPreferredSize(new Dimension(50, 25));
        cmbCtcYear.setPreferredSize(new Dimension(70, 25));


        // Load existing date
        try {
            Object ctcDateObj = resident.getCtcDateIssued();
            if (ctcDateObj != null) {
                String dateStr = ctcDateObj.toString();
                if (!dateStr.equalsIgnoreCase("null") && !dateStr.trim().isEmpty()) {
                    LocalDate ctcDate;
                    if (ctcDateObj instanceof java.sql.Date) {
                        ctcDate = ((java.sql.Date) ctcDateObj).toLocalDate();
                    } else {
                        ctcDate = LocalDate.parse(dateStr);
                    }


                    if (cmbCtcMonth.getItemCount() > 0) {
                        String monthName = ctcDate.getMonth().toString();
                        monthName = monthName.charAt(0) + monthName.substring(1, 3).toLowerCase();
                        cmbCtcMonth.setSelectedItem(monthName);
                    }
                    cmbCtcDay.setSelectedItem(String.valueOf(ctcDate.getDayOfMonth()));
                    cmbCtcYear.setSelectedItem(String.valueOf(ctcDate.getYear()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading CTC Date: " + e.getMessage());
        }


        JPanel ctcDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ctcDatePanel.setBackground(new Color(245, 248, 250));
        ctcDatePanel.setMaximumSize(new Dimension(700, 40));
        ctcDatePanel.add(new JLabel("Date Issued:"));
        ctcDatePanel.add(cmbCtcMonth);
        ctcDatePanel.add(cmbCtcDay);
        ctcDatePanel.add(cmbCtcYear);

        formPanel.add(ctcDatePanel);
        formPanel.add(Box.createVerticalStrut(10));


        // Load Default Place
        SystemConfigDAO config = new SystemConfigDAO();
        String defPlace = config.getConfig("defaultCtcPlace");
        txtCtcPlaceIssued = createStyledTextField(defPlace, 20);
        JPanel ctcPlacePanel = wrapFieldWithLabel("Place Issued:", txtCtcPlaceIssued);
        ctcPlacePanel.setMaximumSize(new Dimension(700, 40));
        formPanel.add(ctcPlacePanel);


        // Set fields as non-editable
        cbSex.setEditable(false);
        txtPurok.setEnabled(false);
        txtAge.setEditable(false);
        txtBirthDate.setEditable(false);
        txtFirstName.setEditable(false);
        street.setEditable(false);
        txtSuffix.setEditable(false);
        txtLastName.setEditable(false);
        txtCivilStatus.setEditable(false);
        txtMiddleName.setEditable(false);
        txtCurrentAddress.setEditable(false);


        // Add scroll pane for form panel
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return formPanel;
    }


    // --- VISUAL HELPERS ---


    private JTextField createStyledTextField(String text, int columns) {
        JTextField textField = new JTextField(text, columns);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        textField.setBackground(Color.WHITE);
        return textField;
    }


    private JComboBox<String> createDropdown(String[] options) {
        JComboBox<String> cb = new JComboBox<>(options);
        customizeComboBox(cb);
        return cb;
    }


    private void customizeComboBox(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cb.setBackground(Color.WHITE);
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }


    private JPanel wrapFieldWithLabel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(new Color(245, 248, 250));


        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(60, 60, 60));
        panel.add(label, BorderLayout.WEST);


        panel.add(field, BorderLayout.CENTER);
        return panel;
    }


    private JPanel createComplexDateField(String labelText, JTextField txt, JComboBox<String> cb) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(new Color(245, 248, 250));


        JPanel left = new JPanel(new BorderLayout(5, 0));
        left.setBackground(new Color(245, 248, 250));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(60, 60, 60));
        left.add(label, BorderLayout.WEST);
        left.add(txt, BorderLayout.CENTER);


        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(new Color(245, 248, 250));
        customizeComboBox(cb);
        cb.setPreferredSize(new Dimension(80, 25));
        right.add(cb, BorderLayout.EAST);


        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }


    // -----------------------------------------------------------------------
    // --- DATA RETRIEVAL ON BUTTON CLICK ---
    // -----------------------------------------------------------------------


    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBackground(new Color(245, 248, 250));
        buttonPanel.setBorder(new EmptyBorder(15, 30, 20, 30));


        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setBackground(new Color(220, 80, 80));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 60, 60), 1),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(cancelButton);
            parentFrame.dispose();

        });


        cancelButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                cancelButton.setBackground(new Color(230, 100, 100));
            }
            public void mouseExited(MouseEvent e) {
                cancelButton.setBackground(new Color(220, 80, 80));
            }
        });


        JButton proceedButton = new JButton("PROCEED");
        proceedButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        proceedButton.setBackground(new Color(70, 130, 180));
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 110, 160), 1),
                BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        proceedButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        proceedButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                proceedButton.setBackground(new Color(90, 150, 200));
            }
            public void mouseExited(MouseEvent e) {
                proceedButton.setBackground(new Color(70, 130, 180));
            }
        });

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
            if (busAddr.isEmpty() || busNature.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill in all Business Details.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String ctcDate = null;


            // 3. "Send" to System (Print to console for now)
            System.out.println("--- Submitting Request ---");
            System.out.println("Applicant: " + fName + " " + lName + " (" + age + ")");
            System.out.println("Sex: " + sex);
            System.out.println("Business: " + busNature + " (" + ownership + ")");
            System.out.println("Location: " + busAddr);
            String purpose = ownership + " - " + busNature +
                    " located at " + busAddr + ".";
            if(purpose.length() > 250){
                JOptionPane.showMessageDialog(this, "Purpose is too long");
                return;
            }
            String ctcNum = txtCtcNumber.getText().trim();
            if (!ctcNum.isEmpty()) {
                try {
                    String mStr = (String) cmbCtcMonth.getSelectedItem();
                    // Convert short month name to full month name for Month enum
                    String[] fullMonths = {"January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"};
                    String[] shortMonths = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    int monthIndex = 0;
                    for (int i = 0; i < shortMonths.length; i++) {
                        if (shortMonths[i].equalsIgnoreCase(mStr)) {
                            monthIndex = i;
                            break;
                        }
                    }
                    int m = monthIndex + 1;
                    ctcDate = cmbCtcYear.getSelectedItem() + "-" + String.format("%02d", m) + "-" + String.format("%02d", Integer.parseInt((String) cmbCtcDay.getSelectedItem()));
                } catch (Exception ex) {
                    ctcDate = null;
                }
            }
            String ctcPlace = txtCtcPlaceIssued.getText().trim();
            ResidentDAO dao = new ResidentDAO();
            if ( purpose.length() < 5) {
                JOptionPane.showMessageDialog(this, "empty purpose");
                return;
            }
            dao.updateResidentCedula(UserDataManager.getInstance().getCurrentResident().getResidentId(), ctcNum.isEmpty() ? null :"CC"+ ctcNum, ctcDate);
            DocumentType certificateOfIndigency = UserDataManager.getInstance().getDocumentTypeByName("Business Clearance");

            Resident currentResident = UserDataManager.getInstance().getCurrentResident();
            UserDataManager.getInstance().residentRequestsDocument( currentResident,null, certificateOfIndigency, purpose);
            String name = currentResident.getFirstName() + " " + currentResident.getLastName();
            int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
            new SystemLogDAO().addLog("Request Document",name,staffId);
            JOptionPane.showMessageDialog(null, "Request Submitted Successfully!");
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(proceedButton);
            parentFrame.dispose();
        });


        buttonPanel.add(cancelButton);
        buttonPanel.add(proceedButton);


        return buttonPanel;
    }
}

