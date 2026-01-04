package org.example.Admin;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;
import org.example.utils.AutoRefresher;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.sql.Date;
import java.time.*;
import java.util.List;

public class AdminStaffTab extends JPanel {

    private JTable staffTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color BTN_ADD_COLOR = new Color(39, 174, 96);      // Green
    private final Color BTN_UPDATE_COLOR = new Color(100, 149, 237); // Blue
    private final Color BTN_DEACTIVATE_COLOR = new Color(231, 76, 60); // Red

    public AdminStaffTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);
        loadStaffData();
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {

                if (refresher != null) {
                    refresher.stop();
                }
                loadStaffData();
                refresher = new AutoRefresher("Staff", AdminStaffTab.this::loadStaffData);
                System.out.println("Tab opened/active. Auto-refresh started.");
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {

                if (refresher != null) {
                    refresher.stop();
                    refresher = null;
                }
                System.out.println("Tab hidden/closed. Auto-refresh stopped.");
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) { }
        });
    }
    private AutoRefresher refresher;
    private javax.swing.Timer lightTimer;
    private static volatile long lastGlobalUpdate = System.currentTimeMillis();
    private void startLightPolling() {
        lightTimer = new javax.swing.Timer(3000, e -> { // Every 3 seconds
            if (staffTable != null && staffTable.getSelectedRow() == -1) {
                // Just check a simple "last updated" flag
                checkLightUpdate();
            }
        });
        lightTimer.start();
    }

    private void checkLightUpdate() {
        // Quick query - just get the latest timestamp
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                String sql = "SELECT UNIX_TIMESTAMP(MAX(GREATEST(" +
                        "COALESCE(updatedAt, '1970-01-01'), " +
                        "COALESCE(createdAt, '1970-01-01')" +
                        "))) as last_ts FROM barangay_staff";

                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {

                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        return rs.getLong("last_ts") * 1000L; // Convert to milliseconds
                    }
                }
                return 0L;
            }

            @Override
            protected void done() {
                try {
                    long dbTimestamp = get();
                    if (dbTimestamp > lastGlobalUpdate) {
                        lastGlobalUpdate = dbTimestamp;
                       loadStaffData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
    // =========================================================================
    // DATA LOADING
    // =========================================================================
    public void loadStaffData() {
        new SwingWorker<List<BarangayStaff>, Void>() {
            @Override
            protected List<BarangayStaff> doInBackground() throws Exception {
                return new StaffDAO().getAllStaff();
            }

            @Override
            protected void done() {
                try {
                    List<BarangayStaff> staffList = get();

                    if (tableModel != null) {
                        tableModel.setRowCount(0);

                        for (BarangayStaff s : staffList) {
                            tableModel.addRow(new Object[]{
                                    s.getStaffId(),
                                    s.getName(),
                                    s.getPosition(),
                                    s.getContactNo(),
                                    s.getStatus(),
                                    s.getLastLogin()
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // =========================================================================
    // 1. UPDATE LOGIC (Full Implementation)
    // =========================================================================
    private void handleUpdate() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member to edit.");
            return;
        }

        int modelRow = staffTable.convertRowIndexToModel(selectedRow);

        String idStr = String.valueOf(tableModel.getValueAt(modelRow, 0));
        int staffId = Integer.parseInt(idStr);

        // 2. Fetch FULL object from DB to get split names (First, Middle, Last)
        // The table only has "Full Name", so we need the fresh object from DB.
        BarangayStaff currentStaff = new StaffDAO().findStaffById(staffId);

        if (currentStaff != null) {
            showUpdateStaffDialog(currentStaff);
        } else {
            JOptionPane.showMessageDialog(this, "Error fetching staff details from database.");
        }
    }

    private void showUpdateStaffDialog(BarangayStaff staff) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Staff Profile", true);
        dialog.setSize(600, 800);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Edit Staff Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(HEADER_BG);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- 1. ID ---
        JTextField txtId = createStyledTextField(staff.getStaffId());
        txtId.setEditable(false);
        txtId.setBackground(new Color(250, 250, 250));
        JTextField txtResId = createStyledTextField(String.valueOf(staff.getResidentId()));
        txtResId.setEditable(false);
        txtResId.setBackground(new Color(250, 250, 250));
        addStyledRow(detailsPanel, "Staff ID:", txtId);
        addStyledRow(detailsPanel, "Resident ID:", txtResId);
        // --- 2. SPLIT NAMES & RESIDENT LINK ---
        JTextField txtFirst = createStyledTextField(staff.getFirstName());
        JTextField txtMiddle = createStyledTextField(staff.getMiddleName() != null ? staff.getMiddleName() : "");
        JTextField txtLast = createStyledTextField(staff.getLastName());
        JTextField txtSuffix = createStyledTextField(staff.getSuffix() != null ? staff.getSuffix() : "");
        JTextField txtContact = createStyledTextField(staff.getContactNo());
        JTextField txtAddress = createStyledTextField(staff.getAddress() != null ? staff.getAddress() : "");
        JTextField txtEmail = createStyledTextField(staff.getEmail() != null ? staff.getEmail() : "");
        JTextField txtSex = createStyledTextField(staff.getSex() != null ? staff.getSex() : "");
        JTextField txtCivilStatus = createStyledTextField(staff.getCivilStatus() != null ? staff.getCivilStatus() : "");
        JButton btnLink = new JButton("<html><center>Select from<br>Resident List</center></html>");
        btnLink.setFont(new Font("Arial", Font.PLAIN, 10));
        btnLink.setFocusPainted(false);
        btnLink.setBackground(new Color(240, 240, 240));


        btnLink.addActionListener(e -> openResidentSelector(dialog, txtFirst, txtMiddle, txtLast, txtSuffix, txtContact, txtAddress, txtEmail,txtSex,txtCivilStatus,txtResId));

        JPanel nameHeader = new JPanel(new BorderLayout());
        nameHeader.setBackground(Color.WHITE);
        nameHeader.add(new JLabel("Personal Information"), BorderLayout.CENTER);
        nameHeader.add(btnLink, BorderLayout.EAST);

        addStyledPanelRow(detailsPanel, "", nameHeader);
        addStyledRow(detailsPanel, "First Name:", txtFirst);
        addStyledRow(detailsPanel, "Middle Initial:", txtMiddle);
        addStyledRow(detailsPanel, "Last Name:", txtLast);
        addStyledRow(detailsPanel, "Suffix:", txtSuffix);
        addStyledRow(detailsPanel, "Address:", txtAddress);
        addStyledRow(detailsPanel, "Sex:", txtSex);
        addStyledRow(detailsPanel, "Civil status:", txtCivilStatus);
        txtSex.setEditable(false);
        txtCivilStatus.setEditable(false);
        // --- 3. POSITION & TERM LIMIT ---
        String[] positions = {"Brgy.Secretary", "Brgy.Treasurer", "Brgy.Captain", "Admin"};
        JComboBox<String> cbPosition = new JComboBox<>(positions);
        cbPosition.setSelectedItem(staff.getRole());
        cbPosition.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Position:", cbPosition);

        // Term Fields
        JTextField txtTermStart = createStyledTextField(LocalDate.now().toString());
        JTextField txtTermEnd = createStyledTextField(LocalDate.now().plusYears(4).toString());
        txtTermEnd.setEditable(false);

        JPanel termPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        termPanel.setBackground(Color.WHITE);
        termPanel.add(wrapField("Term Start:", txtTermStart));
        termPanel.add(wrapField("Term End (4yrs):", txtTermEnd));

        JPanel termWrapper = new JPanel(new BorderLayout());
        termWrapper.setBackground(Color.WHITE);
        termWrapper.setBorder(BorderFactory.createTitledBorder("Term of Office"));
        termWrapper.add(termPanel, BorderLayout.CENTER);

        // Only show term for Captain
        termWrapper.setVisible("Brgy.Captain".equals(staff.getPosition()));

        // Listeners for Term
        txtTermStart.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                try {
                    LocalDate start = LocalDate.parse(txtTermStart.getText());
                    txtTermEnd.setText(start.plusYears(4).toString());
                } catch(Exception ex) {}
            }
        });

        cbPosition.addActionListener(e -> {
            termWrapper.setVisible("Brgy.Captain".equals(cbPosition.getSelectedItem()));
            dialog.revalidate();
        });

        detailsPanel.add(termWrapper);
        detailsPanel.add(Box.createVerticalStrut(10));


        addStyledRow(detailsPanel, "Contact No:", txtContact);
        addStyledRow(detailsPanel, "Email:", txtEmail);

        String[] statuses = {"Active", "Inactive", "Suspended"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setSelectedItem(staff.getStatus());
        cbStatus.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Account Status:", cbStatus);

        // --- 5. CREDENTIALS ---
        JTextField txtUser = createStyledTextField("*****");
        addStyledRow(detailsPanel, "New Username:", txtUser);

        JTextField txtPass = createStyledTextField("*****");
        addStyledRow(detailsPanel, "New Password:", txtPass);

        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);
        cbPosition.setEnabled(false);
        JButton btnCancel = createRoundedButton("Cancel", Color.GRAY);
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = createRoundedButton("Save Changes", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(200, 45));
        enforceLetterOnlyOnNames(txtFirst, txtMiddle, txtLast);
// Inside showUpdateStaffDialog method...

        btnSave.addActionListener(e -> {
            // A. Get Inputs
            String newFirst = txtFirst.getText().trim();
            String newLast = txtLast.getText().trim();
            String newMiddle = txtMiddle.getText().trim();

            String inputUser = txtUser.getText().trim();
            String inputPass = txtPass.getText().trim();


            if (newFirst.isEmpty() || newLast.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "First and Last names are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (!newMiddle.isEmpty()) {
                if (!newMiddle.matches("^[a-zA-Z]\\.?$")) {
                    JOptionPane.showMessageDialog(dialog,
                            "Middle Initial must be a single letter (e.g., 'A' or 'A.').",
                            "Invalid Format",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                newMiddle = newMiddle.replace(".", "").toUpperCase() + ".";
            }


            boolean nameChanged = !newFirst.equalsIgnoreCase(staff.getFirstName()) ||
                    !newLast.equalsIgnoreCase(staff.getLastName());

            // Prepare Credentials

            String finalUser = (!inputUser.equals("*****") && !inputUser.isEmpty()) ? inputUser : staff.getUsername();
            String finalPass = (!inputPass.equals("*****") && !inputPass.isEmpty()) ? inputPass : staff.getPassword();

            Object selectedPos = cbPosition.getSelectedItem();
            String posStr = (selectedPos != null) ? selectedPos.toString() : "";

            // Captain Change Warning
            if ("Brgy.Captain".equals(posStr) && nameChanged) {
                int override = JOptionPane.showConfirmDialog(dialog,
                        "You are replacing the BARANGAY CAPTAIN.\n" +
                                "Current: " + staff.getFirstName() + " " + staff.getLastName() + "\n" +
                                "New: " + newFirst + " " + newLast + "\n\nProceed?",
                        "Confirm Leadership Change", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (override != JOptionPane.YES_OPTION) return;
            }

            Object selectedStatus = cbStatus.getSelectedItem();
            String statusStr = (selectedStatus != null) ? selectedStatus.toString() : "Active";

            // D. Build the Updated Object
            BarangayStaff updatedStaff = BarangayStaff.builder()
                    .staffId(staff.getStaffId())
                    .firstName(newFirst)
                    .residentId(Integer.parseInt(txtResId.getText()))
                    .middleName(newMiddle)
                    .lastName(newLast)
                    .suffix(txtSuffix.getText().trim())
                    .position(posStr)
                    .sex(txtSex.getText())
                    .contactNo(txtContact.getText().trim())
                    .email(txtEmail.getText().trim())
                    .username(finalUser)
                    .password(finalPass)
                    .status(statusStr)
                    .address(txtAddress.getText().trim())
                    .dob(staff.getDob())
                    .citizenship(staff.getCitizenship())
                    .civilStatus(txtCivilStatus.getText())
                    .department(staff.getDepartment())
                    .lastLogin(staff.getLastLogin())
                    .idNumber(staff.getIdNumber())
                    .build();


            UserDataManager.getInstance().updateStaffUsindId(updatedStaff);


            if (nameChanged) {
                int choice = JOptionPane.showConfirmDialog(dialog,
                        "The staff member has changed to " + newFirst + " " + newLast + ".\n" +
                                "Do you want to register them as a Resident also?",
                        "Add to Residents?", JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    addStaffAsResident(updatedStaff);
                }
            }
            logDAO.addLog("Updated staff",staff.getFirstName() + " " + staff.getLastName(), Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
            JOptionPane.showMessageDialog(dialog, "Staff updated successfully!");
            loadStaffData(); // Refresh Table
            dialog.dispose();
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    private void addNameValidation(JTextField textField) {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String currentText = textField.getText();

                // Allow letters (including international), spaces, hyphens, and apostrophes
                // Also allow backspace, delete, tab, and enter
                if (!isValidNameChar(c) &&
                        c != KeyEvent.VK_BACK_SPACE &&
                        c != KeyEvent.VK_DELETE &&
                        c != KeyEvent.VK_TAB &&
                        c != KeyEvent.VK_ENTER) {
                    e.consume(); // Ignore the key press
                    Toolkit.getDefaultToolkit().beep(); // Optional: sound feedback
                }
            }

            private boolean isValidNameChar(char c) {
                // Allow letters (including international characters), spaces, hyphens, and apostrophes
                return Character.isLetter(c) ||
                        c == ' ' ||
                        c == '-' ||
                        c == '\'' ||
                        c == '.'; // Allow dot for initials
            }
        });
    }

    private void enforceLetterOnlyOnNames(JTextField firstNameField, JTextField middleNameField, JTextField lastNameField) {
        // Add validation to each field
        addNameValidation(firstNameField);
        addNameValidation(middleNameField);
        addNameValidation(lastNameField);
    }
    public void addStaffAsResident(BarangayStaff staff){
        java.sql.Date staffDob = Date.valueOf(staff.getDob());
        java.sql.Date finalDob = (staffDob != null) ? staffDob : null;

         Resident staffToResident = Resident.builder()
                .firstName(staff.getFirstName())
                .middleName(staff.getMiddleName())
                .lastName(staff.getLastName())
                .suffix(staff.getSuffix())
                .position("Resident")
                 .age(staff.getAge())
                .contactNo(staff.getContactNo())
                .email(staff.getEmail())
                .status("Active")
                 .purok("Purok 1")
                 .civilStatus(staff.getCivilStatus())
                 .street("Dasmarinas street")
                .gender(staff.getSex())
                .address(staff.getAddress())
                .dob(finalDob.toLocalDate())
                .civilStatus(staff.getCivilStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ResidentDAO rDao = new ResidentDAO();
        if (rDao.isResidentExists(staffToResident.getFirstName(), staff.getLastName(),staff.getMiddleName())) {
            JOptionPane.showMessageDialog(this,
                    "Resident '" + staffToResident.getFirstName() + " " + staff.getLastName() + "' is already registered in resident database!",
                    "Duplicate Entry",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        UserDataManager.getInstance().addResident(staffToResident);
    }

    // =========================================================================
    // RESIDENT SELECTOR (Auto-Fill Fields)
    // =========================================================================
    private void openResidentSelector(JDialog parent, JTextField first, JTextField middle, JTextField last, JTextField suffix, JTextField contact, JTextField address, JTextField email,JTextField gender,JTextField cvStatus,JTextField residentId) {
        JDialog resDialog = new JDialog(parent, "Select Resident", true);
        resDialog.setSize(600, 500);
        resDialog.setLocationRelativeTo(parent);

        ResidentDAO rDao = new ResidentDAO();
        java.util.List<Resident> residents = rDao.getAllResidents();

        String[] cols = {"ID", "Name", "Address","Gender"};
        DefaultTableModel resModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for(Resident r : residents) {
            resModel.addRow(new Object[]{r.getResidentId(), r.getFirstName() + " " + r.getLastName(), r.getAddress(),r.getGender()});
        }

        JTable resTable = new JTable(resModel);
        resTable.setRowHeight(35);
        resTable.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField search = new JTextField();
        TableRowSorter<DefaultTableModel> resSorter = new TableRowSorter<>(resModel);
        resTable.setRowSorter(resSorter);
        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                resSorter.setRowFilter(RowFilter.regexFilter("(?i)" + search.getText()));
            }
        });

        JButton btnSelect = new JButton("Use Selected Resident");
        btnSelect.setBackground(BTN_ADD_COLOR);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.addActionListener(e -> {
            int row = resTable.getSelectedRow();
            if(row != -1) {
                int modelRow = resTable.convertRowIndexToModel(row);
                int resId = (int) resModel.getValueAt(modelRow, 0);

                // Fetch Full Resident
                Resident r = rDao.findResidentById(resId);
                if (r != null) {
                    residentId.setText(String.valueOf(resId));
                    first.setText(r.getFirstName());
                    middle.setText(r.getMiddleName() != null ? r.getMiddleName() : "");
                    last.setText(r.getLastName());
                    suffix.setText(r.getSuffix() != null ? r.getSuffix() : "");
                    address.setText(r.getAddress());
                    contact.setText(r.getContactNo() != null ? r.getContactNo() : "");
                    email.setText(r.getEmail() != null ? r.getEmail() : "");
                    gender.setText(r.getGender() != null ? r.getGender():"");
                    cvStatus.setText(r.getCivilStatus()!=null?r.getCivilStatus():"");
                    resDialog.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(resDialog, "Please select a resident.");
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10,10,10,10));
        p.add(new JLabel("Search:"), BorderLayout.NORTH);
        p.add(search, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(search, BorderLayout.NORTH);
        center.add(new JScrollPane(resTable), BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        p.add(btnSelect, BorderLayout.SOUTH);

        resDialog.add(p);
        resDialog.setVisible(true);
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private void handleDeactivate() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member.");
            return;
        }

        int modelRow = staffTable.convertRowIndexToModel(selectedRow);
        String idStr = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 4);

        if ("Inactive".equals(currentStatus)) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Deactivate " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // Assuming your deactivate method takes an ID
            new StaffDAO().deactivateStaff("Inactive", name, Integer.parseInt(idStr));
            loadStaffData();
        }
    }

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void addStyledRow(JPanel panel, String labelText, JComponent field) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setPreferredSize(new Dimension(150, 35));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(5, 0, 15, 0));
        wrapper.add(field, BorderLayout.CENTER);
        rowPanel.add(label, BorderLayout.WEST);
        rowPanel.add(wrapper, BorderLayout.CENTER);
        panel.add(rowPanel);
    }

    private void addStyledPanelRow(JPanel panel, String labelText, JPanel fieldPanel) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setPreferredSize(new Dimension(150, 35));
        rowPanel.add(label, BorderLayout.WEST);

        rowPanel.add(fieldPanel, BorderLayout.CENTER);
        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(10));
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
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
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HEADER_BG);
        h.setBorder(new EmptyBorder(25, 40, 25, 40));
        JLabel l = new JLabel("Staff Management");
        l.setFont(new Font("Arial", Font.BOLD, 26));
        l.setForeground(Color.WHITE);
        h.add(l, BorderLayout.WEST);
        return h;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // --- BUTTONS ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnAdd = createRoundedButton("+ Add Staff", BTN_ADD_COLOR);
        btnAdd.setPreferredSize(new Dimension(150, 45));
        // Simple placeholder logic for Add - typically opens a blank version of the dialog
        btnAdd.addActionListener(e -> handleAddStaff());

        JButton btnUpdate = createRoundedButton("Edit Details", BTN_UPDATE_COLOR);
        btnUpdate.setPreferredSize(new Dimension(150, 45));
        btnUpdate.addActionListener(e -> handleUpdate());

        JButton btnDeactivate = createRoundedButton("Deactivate", BTN_DEACTIVATE_COLOR);
        btnDeactivate.setPreferredSize(new Dimension(150, 45));
        btnDeactivate.addActionListener(e -> handleDeactivate());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // --- SEARCH ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel searchLabel = new JLabel("Search Staff: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true), new EmptyBorder(5, 5, 5, 5)));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- TABLE ---
        String[] columnNames = {"Staff ID", "Full Name", "Position", "Contact No.", "Status", "Last Login"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        staffTable = new JTable(tableModel);
        staffTable.setFont(new Font("Arial", Font.PLAIN, 14));
        staffTable.setRowHeight(50);
        staffTable.setGridColor(new Color(200, 200, 200));
        staffTable.setSelectionBackground(new Color(220, 237, 250));
        staffTable.setShowVerticalLines(true);
        staffTable.setShowHorizontalLines(true);

        // Double Click Listener
        staffTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleUpdate();
            }
        });

        sorter = new TableRowSorter<>(tableModel);

        staffTable.setRowSorter(sorter);

        JTableHeader header = staffTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < staffTable.getColumnCount(); i++) {
            staffTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(staffTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        contentPanel.add(tableScrollPane);
        return contentPanel;
    }
    private void handleAddStaff() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Staff", true);
        dialog.setSize(600, 850);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Register New Staff", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(HEADER_BG);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- 1. HIDDEN ID FIELD (To capture Resident ID) ---
        JTextField txtResId = new JTextField("0"); // Default to 0
        txtResId.setVisible(false);
        detailsPanel.add(txtResId);

        // --- 2. PERSONAL INFORMATION ---
        JTextField txtFirst = createStyledTextField("");
        JTextField txtMiddle = createStyledTextField("");
        JTextField txtLast = createStyledTextField("");
        JTextField txtSuffix = createStyledTextField("");
        JTextField txtContact = createStyledTextField("");
        // Apply Phone Filter
        ((javax.swing.text.AbstractDocument) txtContact.getDocument()).setDocumentFilter(new PhoneDocumentFilter());

        JTextField txtEmail = createStyledTextField("");
        String brgy = new SystemConfigDAO().getConfig("barangay_name");
        JTextField txtAddress = createStyledTextField(brgy);

        // --- RESTORED DROPDOWNS ---
        // Sex
        String [] sexData = new SystemConfigDAO().getOptionsNature("sex");
        JComboBox<String> cbSex = new JComboBox<>(sexData);
        cbSex.setBackground(Color.WHITE);

        // Civil Status
        String [] civilStatus = new SystemConfigDAO().getOptionsNature("civilStatus");
        JComboBox<String> cbCivil = new JComboBox<>(civilStatus);
        cbCivil.setBackground(Color.WHITE);

        // Position
        String[] positions = {"Brgy.Secretary", "Brgy.Treasurer", "Brgy.Captain", "Admin"};
        JComboBox<String> cbPosition = new JComboBox<>(positions);
        cbPosition.setSelectedIndex(0); // Default to Secretary
        cbPosition.setBackground(Color.WHITE);

        // --- RESIDENT LINK BUTTON ---
        JButton btnLink = new JButton("<html><center>Select from<br>Resident List</center></html>");
        btnLink.setFont(new Font("Arial", Font.PLAIN, 10));
        btnLink.setFocusPainted(false);
        btnLink.setBackground(new Color(240, 240, 240));

        // Listener for Link Button (With Proxy Fields for Dropdowns)
        btnLink.addActionListener(e -> {
            // 1. Create temporary text fields because openResidentSelector requires JTextFields
            JTextField proxySex = new JTextField();
            JTextField proxyCivil = new JTextField();

            // 2. Open the selector passing the text fields and the hidden ID field
            openResidentSelector(dialog, txtFirst, txtMiddle, txtLast, txtSuffix,
                    txtContact, txtAddress, txtEmail, proxySex, proxyCivil, txtResId);

            // 3. After selector closes (it's modal), sync the Dropdowns with the data returned
            if (!proxySex.getText().isEmpty()) {
                cbSex.setSelectedItem(proxySex.getText());
            }
            if (!proxyCivil.getText().isEmpty()) {
                cbCivil.setSelectedItem(proxyCivil.getText());
            }
        });

        JPanel nameHeader = new JPanel(new BorderLayout());
        nameHeader.setBackground(Color.WHITE);
        nameHeader.add(new JLabel("Personal Information"), BorderLayout.CENTER);
        nameHeader.add(btnLink, BorderLayout.EAST);

        addStyledPanelRow(detailsPanel, "", nameHeader);
        addStyledRow(detailsPanel, "First Name:", txtFirst);
        addStyledRow(detailsPanel, "Middle Initial:", txtMiddle);
        addStyledRow(detailsPanel, "Last Name:", txtLast);
        addStyledRow(detailsPanel, "Suffix:", txtSuffix);
        addStyledRow(detailsPanel, "Sex:", cbSex);           // Dropdown
        addStyledRow(detailsPanel, "Civil Status:", cbCivil); // Dropdown
        addStyledRow(detailsPanel, "Address:", txtAddress);

        // --- 3. DATE OF BIRTH & AGE ---
        JTextField txtAge = createStyledTextField("0");
        txtAge.setEditable(false);
        txtAge.setBackground(new Color(245, 245, 245));

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        JComboBox<String> cbMonth = new JComboBox<>(months);
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        JComboBox<String> cbDay = new JComboBox<>(days);
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[100];
        for (int i = 0; i < 100; i++) years[i] = String.valueOf(currentYear - 18 - i);
        JComboBox<String> cbYear = new JComboBox<>(years);

        JPanel datePanel = new JPanel(new GridLayout(1, 3, 5, 0));
        datePanel.setBackground(Color.WHITE);
        datePanel.add(cbMonth);
        datePanel.add(cbDay);
        datePanel.add(cbYear);

        ActionListener dateListener = e -> {
            try {
                String mStr = (String) cbMonth.getSelectedItem();
                int d = Integer.parseInt((String) cbDay.getSelectedItem());
                int y = Integer.parseInt((String) cbYear.getSelectedItem());
                int m = Month.valueOf(mStr.toUpperCase()).getValue();
                LocalDate birthDate = LocalDate.of(y, m, d);
                LocalDate now = LocalDate.now();
                if (birthDate.isAfter(now)) {
                    txtAge.setText("Invalid");
                } else {
                    int age = Period.between(birthDate, now).getYears();
                    txtAge.setText(String.valueOf(age));
                }
            } catch (Exception ex) {
                txtAge.setText("Invalid");
            }
        };
        cbMonth.addActionListener(dateListener);
        cbDay.addActionListener(dateListener);
        cbYear.addActionListener(dateListener);
        dateListener.actionPerformed(null); // Init Age

        addStyledRow(detailsPanel, "Date of Birth:", datePanel);
        addStyledRow(detailsPanel, "Age (Auto):", txtAge);

        addStyledRow(detailsPanel, "Position:", cbPosition); // Dropdown
        addStyledRow(detailsPanel, "Contact No:", txtContact);
        addStyledRow(detailsPanel, "Email:", txtEmail);

        // --- 4. CREDENTIALS ---
        detailsPanel.add(Box.createVerticalStrut(10));
        JLabel lblCreds = new JLabel("Login Credentials");
        lblCreds.setFont(new Font("Arial", Font.BOLD, 14));
        lblCreds.setForeground(HEADER_BG);
        detailsPanel.add(lblCreds);

        JTextField txtUsername = createStyledTextField("");
        JPasswordField txtPassword = new JPasswordField("");
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));

        addStyledRow(detailsPanel, "Username:", txtUsername);
        addStyledRow(detailsPanel, "Password:", txtPassword);

        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = createRoundedButton("Cancel", Color.GRAY);
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = createRoundedButton("Create Account", BTN_ADD_COLOR);
        btnSave.setPreferredSize(new Dimension(200, 45));
        enforceLetterOnlyOnNames(txtFirst, txtMiddle, txtLast);
        cbPosition.setSelectedIndex(3);
        cbPosition.setEnabled(false);
        btnSave.addActionListener(e -> {
            // A. VALIDATION
            if (txtFirst.getText().trim().isEmpty() || txtLast.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "First and Last names are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtUsername.getText().trim().isEmpty() || new String(txtPassword.getPassword()).trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and Password are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtContact.getText().length() != 11) {
                JOptionPane.showMessageDialog(dialog, "Contact number must be 11 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Middle Initial Validation
            String mName = txtMiddle.getText().trim();
            if (!mName.isEmpty()) {
                if (!mName.matches("^[a-zA-Z]\\.?$")) {
                    JOptionPane.showMessageDialog(dialog, "Middle Initial must be a single letter.", "Invalid Format", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                mName = mName.replace(".", "").toUpperCase() + ".";
            }

            try {
                // B. GATHER DATA
                String mStr = (String) cbMonth.getSelectedItem();
                int m = Month.valueOf(mStr.toUpperCase()).getValue();
                int d = Integer.parseInt((String) cbDay.getSelectedItem());
                int y = Integer.parseInt((String) cbYear.getSelectedItem());
                LocalDate birthDate = LocalDate.of(y, m, d);

                int age = 0;
                try { age = Integer.parseInt(txtAge.getText()); } catch(NumberFormatException nfe) {}

                // Retrieve the Resident ID from the hidden field
                int resId = 0;
                try {
                    String idText = txtResId.getText().trim();
                    if (!idText.isEmpty()) {
                        resId = Integer.parseInt(idText);
                    }
                } catch(NumberFormatException nfe) {
                    resId = 0;
                }

                // C. BUILD OBJECT
                BarangayStaff staff = BarangayStaff.builder()
                        .residentId(resId) // Pass the captured ID here
                        .firstName(txtFirst.getText().trim())
                        .middleName(mName)
                        .lastName(txtLast.getText().trim())
                        .suffix(txtSuffix.getText().trim())
                        .position(cbPosition.getSelectedItem().toString())
                        .role(mapPositionToRole(cbPosition.getSelectedItem().toString()))
                        .sex(cbSex.getSelectedItem().toString())
                        .civilStatus(cbCivil.getSelectedItem().toString())
                        .dob(birthDate)
                        .age(age)
                        .contactNo(txtContact.getText().trim())
                        .email(txtEmail.getText().trim())
                        .address(txtAddress.getText().trim())
                        .citizenship("Filipino")
                        .username(txtUsername.getText().trim())
                        .password(new String(txtPassword.getPassword()))
                        .status("Active")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .lastLogin(null)
                        .build();

                // D. SAVE
                UserDataManager.getInstance().addStaff(staff);

                // Log and Close
                logDAO.addLog("Added staff", staff.getFirstName() + " " + staff.getLastName(),
                        Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));

                JOptionPane.showMessageDialog(dialog, "Staff Account Created Successfully!");
                loadStaffData();
                dialog.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    SystemLogDAO logDAO = new SystemLogDAO();
    private static String mapPositionToRole(String position) {
        switch (position) {
            case "Brgy.Captain":
                return "Brgy.Captain";
            case "Brgy.Secretary":
                return "Brgy.Secretary";
            case "Brgy.Treasurer":
                return "Brgy.Treasurer";
            case "Admin":
                return "Admin";
            default:
                return "Resident";
        }
    }

    static class PlaceholderTextField extends JTextField {
        private final String placeholder;
        private boolean showingPlaceholder = true;

        PlaceholderTextField(String placeholder) {
            super(placeholder);
            this.placeholder = placeholder;
            setForeground(Color.GRAY);
            setOpaque(false);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.BLACK);
                        showingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        public String getText() {
            return showingPlaceholder ? "" : super.getText();
        }
    }

    static class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;
        private boolean showing = true;

        PlaceholderPasswordField(String placeholder) {
            super(placeholder);
            this.placeholder = placeholder;
            setForeground(Color.GRAY);
            setEchoChar((char) 0);
            setOpaque(false);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showing) {
                        setText("");
                        setForeground(Color.BLACK);
                        setEchoChar('•');
                        showing = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getPassword().length == 0) {
                        setEchoChar((char) 0);
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showing = true;
                    }
                }
            });
        }

        @Override
        public char[] getPassword() {
            return showing ? new char[0] : super.getPassword();
        }
    }
    // Copy this inside AdminStaffTab class, at the bottom
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


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setSize(1200, 800);
            f.add(new AdminStaffTab());
            f.setVisible(true);
        });
    }
}