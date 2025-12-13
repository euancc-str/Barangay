// AdminResidentTab.java - Updated color scheme
package org.example.Admin;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.Resident;

import java.awt.*;
import java.awt.event.*;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import java.util.List;
import javax.swing.table.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.Month;

public class AdminResidentTab extends JPanel {

    private JTable residentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // Components
    private JLabel lblRecordCount;
    private JTextField searchField;

    // --- UPDATED VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color BTN_ADD_COLOR = new Color(39, 174, 96);
    private final Color BTN_UPDATE_COLOR = new Color(41, 128, 185);
    private final Color BTN_DEACTIVATE_COLOR = new Color(231, 76, 60);

    public AdminResidentTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        loadResidentData();
        // Ensure resident table text and header use black for clarity
        if (residentTable != null) {
            residentTable.setForeground(Color.BLACK);
            JTableHeader rh = residentTable.getTableHeader();
            if (rh != null) rh.setForeground(Color.BLACK);
        }
    }

    public void loadResidentData() {
        new SwingWorker<List<Resident>, Void>() {
            @Override
            protected List<Resident> doInBackground() throws Exception {
                // Background: Heavy DB Fetch
                return new ResidentDAO().getAllResidents();
            }

            @Override
            protected void done() {
                try {
                    List<Resident> residents = get();

                    // UI Update: Instant & Safe
                    if (tableModel != null) {
                        tableModel.setRowCount(0); // Clear

                        for (Resident r : residents) {
                            tableModel.addRow(new Object[]{
                                   ""+ r.getResidentId(),
                                    r.getLastName() + ", " + r.getFirstName(),
                                    r.getGender(),
                                    ""+ r.getAge(),
                                    r.getAddress()
                            });
                        }

                        // Update Count Label
                        if (lblRecordCount != null) {
                            lblRecordCount.setText("Total Residents: " + residents.size());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    private void updateRecordCount() {
        if (lblRecordCount != null && residentTable != null) {
            int count = residentTable.getRowCount();
            lblRecordCount.setText("Total Records: " + count);
        }
    }

    // =========================================================================
    // 1. DEACTIVATE LOGIC
    // =========================================================================
    private void handleDeactivate() {
        int selectedRow = residentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a resident.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = residentTable.convertRowIndexToModel(selectedRow);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 5);

        if("Inactive".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This resident account is already inactive.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><body style='width: 300px;'>" +
                        "<b>CONFIRM DEACTIVATION</b><br><br>" +
                        "Are you sure you want to deactivate <b>" + name + "</b>?<br>" +
                        "</body></html>",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.setValueAt("Inactive", modelRow, 5);
            JOptionPane.showMessageDialog(this, "Resident access deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    // 2. UPDATE LOGIC (With Pre-filled Email)
    // =========================================================================
    private void handleUpdate() {
        int selectedRow = residentTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = residentTable.convertRowIndexToModel(selectedRow);

        // Retrieve values from table
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String gender = (String) tableModel.getValueAt(modelRow, 2);
        String age = (String) tableModel.getValueAt(modelRow, 3);
        String address = (String) tableModel.getValueAt(modelRow, 4);
        ResidentDAO residentDAO = new ResidentDAO();
        String user = residentDAO.findResidentByFullName(Integer.parseInt(id));


        showUpdateResidentDialog(id, name, gender, age, address,  user, modelRow);
    }

    private void showUpdateResidentDialog(String id, String name, String gender, String age, String address,  String email, int modelRow) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Resident", true);
        dialog.setSize(550, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());


        Resident residentData = new ResidentDAO().findResidentById(Integer.parseInt(id));


        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Edit Resident Info", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(HEADER_BG);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- FIELDS ---
        JTextField txtId = createStyledTextField(id);
        txtId.setEditable(false);
        txtId.setBackground(new Color(250, 250, 250));
        addStyledRow(detailsPanel, "Resident ID:", txtId);

        JTextField txtName = createStyledTextField(name);
        addStyledRow(detailsPanel, "Full Name:", txtName);
        txtName.setEditable(false);
        String[] genders = new SystemConfigDAO().getOptionsNature("sex");
        JComboBox<String> cbGender = new JComboBox<>(genders);
        cbGender.setSelectedItem(gender);
        cbGender.setBackground(Color.WHITE);
        cbGender.setEnabled(false);
        addStyledRow(detailsPanel, "Gender:", cbGender);

        JTextField txtAge = createStyledTextField(age);
        addStyledRow(detailsPanel, "Age:", txtAge);

        JTextField txtAddress = createStyledTextField(address);
        addStyledRow(detailsPanel, "Address:", txtAddress);
        txtAge.setEditable(false);
        String [] dao = new SystemConfigDAO().getOptionsNature("purok");

        JComboBox<String> purok = new JComboBox<>(dao);
        addStyledRow(detailsPanel, "Purok:", purok);
        purok.setSelectedItem(residentData.getPurok());
        JTextField street = createStyledTextField("");
        addStyledRow(detailsPanel, "Street:", street);
        street.setText(residentData.getStreet());
        // --- 2. DATE OF BIRTH & AUTO AGE LOGIC ---

        // Age Field (Read Only)
        JTextField txtContact = createStyledTextField("");
        ((javax.swing.text.AbstractDocument) txtContact.getDocument()).setDocumentFilter(new PhoneDocumentFilter());
        // --- SEPARATOR ---
        addStyledRow(detailsPanel,"Contact: ", txtContact);
        txtContact.setText(residentData.getContactNo());

        String[] civilStatus = new SystemConfigDAO().getOptionsNature("civilStatus");
        JComboBox<String> cbcivilStatus = new JComboBox<>(civilStatus);
        cbcivilStatus.setBackground(Color.WHITE);
        addStyledRow(detailsPanel,"Civil status:",cbcivilStatus);
        cbcivilStatus.setSelectedItem(residentData.getCivilStatus());
        detailsPanel.add(Box.createVerticalStrut(15));
        detailsPanel.add(Box.createVerticalStrut(10));


        // Add Hint for Password

        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = createRoundedButton("Cancel", new Color(149, 165, 166));
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = createRoundedButton("Save Changes", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(200, 45));

        btnSave.addActionListener(e -> {

            if (txtName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if(address.length() < 10){
                JOptionPane.showMessageDialog(dialog, "Address Length should not be less than 10", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }



            int confirm = JOptionPane.showConfirmDialog(dialog, "Save changes?", "Confirm", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                // Update Table Model
                tableModel.setValueAt(txtName.getText(), modelRow, 1);
                tableModel.setValueAt(cbGender.getSelectedItem(), modelRow, 2);
                tableModel.setValueAt(txtAge.getText(), modelRow, 3);
                tableModel.setValueAt(txtAddress.getText(), modelRow, 4);
                JOptionPane.showMessageDialog(dialog, "Resident updated successfully!");
                Resident resident = Resident.builder()
                        .address(txtAddress.getText())
                        .residentId(Integer.parseInt(id))
                        .street(street.getText())
                        .purok(purok.getSelectedItem().toString())
                        .contactNo(txtContact.getText())
                        .civilStatus(cbcivilStatus.getSelectedItem().toString())
                        .build();

               new StaffDAO().updateResident(resident,
                       Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));

                   dialog.dispose();
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // =========================================================================
    // 3. ADD RESIDENT LOGIC
    // =========================================================================
    private void handleAddResident() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Resident", true);
        dialog.setSize(500, 750); // Increased height slightly
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // --- 1. CREATE FIELDS ---
        JTextField txtName = createStyledTextField("");
        JTextField lastName = createStyledTextField("");
        JTextField middleName = createStyledTextField("");

        String[] genders = new SystemConfigDAO().getOptionsNature("sex");
        JComboBox<String> cbGender = new JComboBox<>(genders);
        cbGender.setBackground(Color.WHITE);

        JTextField txtAddress = createStyledTextField("");
        String [] dao = new SystemConfigDAO().getOptionsNature("purok");

        JComboBox<String> purok = new JComboBox<>(dao);
        JTextField street = createStyledTextField("");

        // --- 2. DATE OF BIRTH & AUTO AGE LOGIC ---

        // Age Field (Read Only)
        JTextField txtContact = createStyledTextField("");
        ((javax.swing.text.AbstractDocument) txtContact.getDocument()).setDocumentFilter(new PhoneDocumentFilter());
        JTextField txtAge = createStyledTextField("");
        txtAge.setEditable(false); // Prevent manual typing
        txtAge.setBackground(new Color(245, 245, 245)); // Grey out slightly


        // Date Components
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        JComboBox<String> cbMonth = new JComboBox<>(months);

        // Days 1-31
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        JComboBox<String> cbDay = new JComboBox<>(days);

        // Years (Current Year down to 1900)
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[125];
        for (int i = 0; i < 125; i++) years[i] = String.valueOf(currentYear - i);
        JComboBox<String> cbYear = new JComboBox<>(years);

        // Styling Dropdowns
        cbMonth.setBackground(Color.WHITE);
        cbDay.setBackground(Color.WHITE);
        cbYear.setBackground(Color.WHITE);

        // Panel to hold the 3 dropdowns in one row
        JPanel datePanel = new JPanel(new GridLayout(1, 3, 5, 0));
        datePanel.setBackground(Color.WHITE);
        datePanel.add(cbMonth);
        datePanel.add(cbDay);
        datePanel.add(cbYear);

        // --- AUTO-CALCULATION LOGIC ---
        ActionListener dateListener = e -> {
            try {
                String mStr = (String) cbMonth.getSelectedItem();
                int d = Integer.parseInt((String) cbDay.getSelectedItem());
                int y = Integer.parseInt((String) cbYear.getSelectedItem());

                // Convert Month Name to Number (e.g., January -> 1)
                int m = Month.valueOf(mStr.toUpperCase()).getValue();

                LocalDate birthDate = LocalDate.of(y, m, d);
                LocalDate now = LocalDate.now();

                // Calculate Age
                int age = Period.between(birthDate, now).getYears();

                // Update Field
                if (age < 0) {
                    txtAge.setText("Invalid");
                } else {
                    txtAge.setText(String.valueOf(age));
                }
            } catch (Exception ex) {
                // Handles invalid dates (e.g., Feb 31)
                txtAge.setText("Invalid Date");
            }
        };

        // Attach listener to all date boxes
        cbMonth.addActionListener(dateListener);

        cbDay.addActionListener(dateListener);
        cbYear.addActionListener(dateListener);

        // Trigger once to set initial age based on defaults
        dateListener.actionPerformed(null);
        String[] civilStatus = new SystemConfigDAO().getOptionsNature("civilStatus");
        JComboBox<String> cbcivilStatus = new JComboBox<>(civilStatus);
        cbcivilStatus.setBackground(Color.WHITE);

        // --- 3. ADD TO PANEL ---
        addStyledRow(formPanel, "First Name:", txtName);
        addStyledRow(formPanel, "Middle Initial:", middleName);
        addStyledRow(formPanel, "Last Name:", lastName);
        addStyledRow(formPanel, "Gender:", cbGender);
        addStyledRow(formPanel,"Contact Number:", txtContact);
        // Add Date Picker + Age
        addStyledRow(formPanel, "Date of Birth:", datePanel);
        addStyledRow(formPanel, "Age:", txtAge);

        addStyledRow(formPanel, "Address:", txtAddress);
        addStyledRow(formPanel,"Purok",purok);
        addStyledRow(formPanel,"Street",street);
        addStyledRow(formPanel, "Civil status:",cbcivilStatus);


        // --- 4. SAVE BUTTON ---
        JButton btnSave = createRoundedButton("Register", BTN_ADD_COLOR);
        btnSave.addActionListener(e -> {
            String contact = txtContact.getText().trim();
            String address = txtAddress.getText().trim();
            String street1 = street.getText().trim();
            String fName = txtName.getText().trim();
            String lName = lastName.getText().trim();
            String mName = middleName.getText().trim();
            ResidentDAO rDao = new ResidentDAO();
            if (rDao.isResidentExists(fName, lName,mName)) {
                JOptionPane.showMessageDialog(dialog,
                        "Resident '" + fName + " " + lName + "' is already registered!",
                        "Duplicate Entry",
                        JOptionPane.WARNING_MESSAGE);
                return; // Stop here, do not save!
            }
            if (!mName.isEmpty()) {
                // Regex: Matches exactly ONE letter (a-z or A-Z), optionally followed by a dot
                if (!mName.matches("^[a-zA-Z]\\.?$")) {
                    JOptionPane.showMessageDialog(dialog,
                            "Middle Initial must be a single letter (e.g., 'A' or 'A.').",
                            "Invalid Format",
                            JOptionPane.WARNING_MESSAGE);
                    return; // Stop here
                }

                // Format: Remove any existing dot, uppercase the letter, then append a dot
                mName = mName.replace(".", "").toUpperCase() + ".";
            }
            if(address.length() < 5){
                JOptionPane.showMessageDialog(dialog, "Address Length should not be less than 10", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 1. Check if Empty
            if (contact.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Contact Number is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Check exact length (Must be 11 digits)
            if (contact.length() != 11) {
                JOptionPane.showMessageDialog(dialog, "Phone number must be exactly 11 digits (09...)", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 1. VALIDATION (Name Check)
            if (txtName.getText().trim().isEmpty() || lastName.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Names cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            String ageText = txtAge.getText();
            if (ageText.equals("Invalid Date") || ageText.equals("0")) {
                JOptionPane.showMessageDialog(dialog, "Please select a valid birth date.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // 3. GET DATE FROM DROPDOWNS
                String mStr = (String) cbMonth.getSelectedItem();
                int d = Integer.parseInt((String) cbDay.getSelectedItem());
                int y = Integer.parseInt((String) cbYear.getSelectedItem());
                int m = java.time.Month.valueOf(mStr.toUpperCase()).getValue();

                // Create the LocalDate object for the database
                java.time.LocalDate birthDate = java.time.LocalDate.of(y, m, d);

                // 4. PREPARE TABLE DATA (Mock ID)
                int newId = residentTable.getRowCount() + 1;
                String fullName = txtName.getText() + " " + lastName.getText();

                // Update GUI Table
                tableModel.addRow(new Object[]{
                        String.valueOf(newId),
                        fullName,
                        cbGender.getSelectedItem(),
                        ageText, // The calculated age
                        txtAddress.getText(),
                        "Active"
                });
                updateRecordCount();

                Resident resident = Resident.builder()
                        .firstName(txtName.getText())
                        .lastName(lastName.getText())
                        .name(txtName.getText() + " " + lastName.getText())
                        .gender((String) cbGender.getSelectedItem())
                        .age(Integer.parseInt(ageText))
                        .dob(birthDate)
                        .address(txtAddress.getText())

                        .contactNo(txtContact.getText())
                        .civilStatus(cbcivilStatus.getSelectedItem().toString())
                        .position("Resident")
                        .status("Active")
                        .middleName(mName)
                        .purok(purok.getSelectedItem().toString())
                        .street(street.getText())
                        .createdAt(java.time.LocalDateTime.now())
                        .updatedAt(java.time.LocalDateTime.now())
                        .build();

                // 6. SAVE TO DB
                SystemLogDAO logDAO = new SystemLogDAO();
                logDAO.addLog("Added a resident", resident.getName(), Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
                UserDataManager.getInstance().addResident(resident);

                JOptionPane.showMessageDialog(dialog, "Resident Added Successfully!");
                dialog.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnSave);

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Add this class at the bottom of your file
    static class PhoneDocumentFilter extends javax.swing.text.DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (string == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);

            if (isValidPhone(newText)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
            if (text == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

            if (isValidPhone(newText)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        // YOUR LOGIC HERE
        private boolean isValidPhone(String text) {
            if (text.isEmpty()) return true;
            if (!text.matches("\\d*")) return false; // Digits only
            if (text.length() > 11) return false;    // Max 11
            // Strict check: Must start with 09 if length is 2 or more
            if (text.length() >= 2 && !text.startsWith("09")) return false;
            return true;
        }
    }
    // =========================================================================
    // GUI SETUP
    // =========================================================================

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnAdd = createRoundedButton("+ Add Resident", BTN_ADD_COLOR);
        btnAdd.setPreferredSize(new Dimension(160, 45));
        btnAdd.addActionListener(e -> handleAddResident());

        JButton btnUpdate = createRoundedButton("Edit Details", BTN_UPDATE_COLOR);
        btnUpdate.setPreferredSize(new Dimension(150, 45));
        btnUpdate.addActionListener(e -> handleUpdate());


        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true), new EmptyBorder(5, 5, 5, 5)));

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                updateRecordCount();
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- TABLE SETUP ---
        String[] columnNames = {"ID", "Full Name", "Gender", "Age", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        residentTable = new JTable(tableModel);
        residentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        residentTable.setRowHeight(50);
        residentTable.setGridColor(new Color(200, 200, 200));
        residentTable.setSelectionBackground(new Color(220, 237, 250));
        residentTable.setShowVerticalLines(true);
        residentTable.setShowHorizontalLines(true);

        residentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleUpdate();
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(0, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Integer i1 = Integer.parseInt(s1);
                    Integer i2 = Integer.parseInt(s2);
                    return i1.compareTo(i2);
                } catch (NumberFormatException e) {
                    return s1.compareTo(s2); // Fallback
                }
            }
        });

        sorter.setComparator(3, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Integer i1 = Integer.parseInt(s1);
                    Integer i2 = Integer.parseInt(s2);
                    return i1.compareTo(i2);
                } catch (Exception e) {
                    return s1.compareTo(s2);
                }
            }
        });

        residentTable.setRowSorter(sorter);

        sorter.addRowSorterListener(new RowSorterListener() {
            @Override
            public void sorterChanged(RowSorterEvent e) {
                updateRecordCount();
            }
        });

        JTableHeader header = residentTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < residentTable.getColumnCount(); i++) {
            residentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(residentTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        contentPanel.add(tableScrollPane);

        // Footer Count
        contentPanel.add(Box.createVerticalStrut(10));
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(BG_COLOR);

        lblRecordCount = new JLabel("Total Records: 0");
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        lblRecordCount.setForeground(new Color(100, 100, 100));

        footerPanel.add(lblRecordCount);
        contentPanel.add(footerPanel);

        return contentPanel;
    }

    // Visual Helpers
    private void addStyledRow(JPanel panel, String labelText, JComponent field) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
        label.setPreferredSize(new Dimension(150, 35));

        if (field instanceof JPanel) label.setVerticalAlignment(SwingConstants.TOP);

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(Color.WHITE);
        fieldWrapper.setBorder(new EmptyBorder(5, 0, 15, 0));
        fieldWrapper.add(field, BorderLayout.CENTER);

        rowPanel.add(label, BorderLayout.WEST);
        rowPanel.add(fieldWrapper, BorderLayout.CENTER);
        panel.add(rowPanel);
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(HEADER_BG);
                        g2.fillRoundRect(x, y, width, height, 30, 30);
                    }
                }, new EmptyBorder(25, 40, 25, 40)));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);
        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 26));
        lblSystem.setForeground(Color.WHITE);
        JLabel lblModule = new JLabel("Resident Management");
        lblModule.setFont(new Font("Arial", Font.BOLD, 22));
        lblModule.setForeground(Color.WHITE);
        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            JFrame frame = new JFrame("Admin Resident Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new AdminResidentTab());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}