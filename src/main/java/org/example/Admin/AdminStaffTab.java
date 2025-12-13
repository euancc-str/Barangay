package org.example.Admin;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

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

        // 1. Get the ID safely
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
        addStyledRow(detailsPanel, "Staff ID:", txtId);

        // --- 2. SPLIT NAMES & RESIDENT LINK ---
        JTextField txtFirst = createStyledTextField(staff.getFirstName());
        JTextField txtMiddle = createStyledTextField(staff.getMiddleName() != null ? staff.getMiddleName() : "");
        JTextField txtLast = createStyledTextField(staff.getLastName());
        JTextField txtSuffix = createStyledTextField(staff.getSuffix() != null ? staff.getSuffix() : "");

        JTextField txtContact = createStyledTextField(staff.getContactNo());
        JTextField txtAddress = createStyledTextField(staff.getAddress() != null ? staff.getAddress() : "");
        JTextField txtEmail = createStyledTextField(staff.getEmail() != null ? staff.getEmail() : "");

        // Link Button
        JButton btnLink = new JButton("<html><center>Select from<br>Resident List</center></html>");
        btnLink.setFont(new Font("Arial", Font.PLAIN, 10));
        btnLink.setFocusPainted(false);
        btnLink.setBackground(new Color(240, 240, 240));

        // LINKING LOGIC: Pass all fields to be auto-filled
        btnLink.addActionListener(e -> openResidentSelector(dialog, txtFirst, txtMiddle, txtLast, txtSuffix, txtContact, txtAddress, txtEmail));

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

        // --- 3. POSITION & TERM LIMIT ---
        String[] positions = {"Secretary", "Treasurer", "Brgy.Captain", "Admin"};
        JComboBox<String> cbPosition = new JComboBox<>(positions);
        cbPosition.setSelectedItem(staff.getPosition());
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

        // --- 4. CONTACT & STATUS ---
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

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = createRoundedButton("Cancel", Color.GRAY);
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = createRoundedButton("Save Changes", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(200, 45));

        // --- SAVE ACTION ---
        btnSave.addActionListener(e -> {
            // A. Get Inputs
            String newFirst = txtFirst.getText().trim();
            String newLast = txtLast.getText().trim();

            String inputUser = txtUser.getText().trim();
            String inputPass = txtPass.getText().trim();

            // B. Validation
            if (newFirst.isEmpty() || newLast.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "First and Last names are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String mName = txtMiddle.getText().trim();
            if (!mName.isEmpty()) {
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

            // C. Credential Logic (Keep old if *****)
            String finalUser = (!inputUser.equals("*****") && !inputUser.isEmpty()) ? inputUser : staff.getUsername();
            String finalPass = (!inputPass.equals("*****") && !inputPass.isEmpty()) ? inputPass : staff.getPassword();

            // D. Captain Override Warning
            Object selectedPos = cbPosition.getSelectedItem();
            String posStr = (selectedPos != null) ? selectedPos.toString() : "";

            if ("Brgy.Captain".equals(posStr)) {
                int override = JOptionPane.showConfirmDialog(dialog,
                        "You are updating the BARANGAY CAPTAIN.\n" +
                                "The new name '" + newFirst + " " + newLast + "' will replace the current captain.\n" +
                                "Term: " + txtTermStart.getText() + " to " + txtTermEnd.getText() + ".\n\nProceed?",
                        "Confirm Leadership Change", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (override != JOptionPane.YES_OPTION) return;
            }

            Object selectedStatus = cbStatus.getSelectedItem();
            String statusStr = (selectedStatus != null) ? selectedStatus.toString() : "Active";

            BarangayStaff updatedStaff = BarangayStaff.builder()
                    .staffId(staff.getStaffId()) // Primary Key
                    .firstName(newFirst)
                    .middleName(txtMiddle.getText().trim())
                    .lastName(newLast)
                    .suffix(txtSuffix.getText().trim())
                    .position(posStr)
                    .role(posStr)
                    .contactNo(txtContact.getText().trim())
                    .email(txtEmail.getText().trim())
                    .username(finalUser)
                    .password(finalPass)
                    .status(statusStr)
                    .address(txtAddress.getText().trim())
                    // Keep existing fields we didn't edit
                    .dob(staff.getDob())
                    .citizenship(staff.getCitizenship())
                    .civilStatus(staff.getCivilStatus())
                    .department(staff.getDepartment())
                    .lastLogin(staff.getLastLogin())
                    .idNumber(staff.getIdNumber())
                    .build();

            // F. Call Your Method (NO LOGS as requested)
            UserDataManager.getInstance().updateStaff(updatedStaff);
            addStaffAsResident(staff);
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
    private void openResidentSelector(JDialog parent, JTextField first, JTextField middle, JTextField last, JTextField suffix, JTextField contact, JTextField address, JTextField email) {
        JDialog resDialog = new JDialog(parent, "Select Resident", true);
        resDialog.setSize(600, 500);
        resDialog.setLocationRelativeTo(parent);

        ResidentDAO rDao = new ResidentDAO();
        java.util.List<Resident> residents = rDao.getAllResidents();

        String[] cols = {"ID", "Name", "Address"};
        DefaultTableModel resModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for(Resident r : residents) {
            resModel.addRow(new Object[]{r.getResidentId(), r.getFirstName() + " " + r.getLastName(), r.getAddress()});
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
                    first.setText(r.getFirstName());
                    middle.setText(r.getMiddleName() != null ? r.getMiddleName() : "");
                    last.setText(r.getLastName());
                    suffix.setText(r.getSuffix() != null ? r.getSuffix() : "");
                    address.setText(r.getAddress());

                    // Added Contact & Email as requested
                    contact.setText(r.getContactNo() != null ? r.getContactNo() : "");
                    email.setText(r.getEmail() != null ? r.getEmail() : "");

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
        buttonPanel.add(btnDeactivate);

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
        dialog.setSize(500, 800); // Slightly taller for extra fields
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // --- 1. CREATE FIELDS ---
        JTextField txtName = createStyledTextField("");
        JTextField txtLastName = createStyledTextField("");

        String[] positions = {"Secretary", "Treasurer", "Brgy.Captain","Admin"};
        JComboBox<String> cbPosition = new JComboBox<>(positions);
        cbPosition.setSelectedIndex(3);
        cbPosition.setEnabled(false);
        cbPosition.setBackground(Color.WHITE);
        cbPosition.setFont(new Font("Arial", Font.PLAIN, 14));

        String [] civilStatus = new SystemConfigDAO().getOptionsNature("civilStatus");
        JComboBox<String> cbCivil = new JComboBox<>(civilStatus);
        cbCivil.setBackground(Color.WHITE);
        cbCivil.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField txtContact = createStyledTextField("");
        // Apply Phone Filter immediately
        ((javax.swing.text.AbstractDocument) txtContact.getDocument()).setDocumentFilter(new PhoneDocumentFilter());

        JTextField txtEmail = createStyledTextField("");
        JTextField txtAddress = createStyledTextField("");

        // Credentials
        JTextField txtUsername = createStyledTextField("");
        JPasswordField txtPassword = new JPasswordField(""); // Use PasswordField for staff
        // Style the password field manually to match
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));

        // --- 2. DATE & AGE LOGIC (Reusable Logic) ---
        JTextField txtAge = createStyledTextField("");
        txtAge.setEditable(false);
        txtAge.setBackground(new Color(245, 245, 245));
        txtAge.setText("0");

        JTextField txtMiddleName = createStyledTextField("");
        txtAge.setEditable(false);
        txtAge.setBackground(new Color(245, 245, 245));

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        JComboBox<String> cbMonth = new JComboBox<>(months);

        String[] days = new String[31];
        for (int i = 0; i < 31; i++) days[i] = String.valueOf(i + 1);
        JComboBox<String> cbDay = new JComboBox<>(days);

        int currentYear = LocalDate.now().getYear();
        String[] years = new String[100]; // Staff usually aren't infants, so 100 years back is fine
        for (int i = 0; i < 100; i++) years[i] = String.valueOf(currentYear - 18 - i); // Start at 18 years ago
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
                txtAge.setText("Invalid Date");
            }
        };

        cbMonth.addActionListener(dateListener);
        cbDay.addActionListener(dateListener);
        cbYear.addActionListener(dateListener);
        dateListener.actionPerformed(null); // Init

        // --- 3. ADD TO PANEL ---
        addStyledRow(formPanel, "First Name:", txtName);
        addStyledRow(formPanel,"Middle Initial: ", txtMiddleName);
        addStyledRow(formPanel, "Last Name:", txtLastName);
        addStyledRow(formPanel, "Position:", cbPosition);
        addStyledRow(formPanel, "Date of Birth:", datePanel);
        addStyledRow(formPanel, "Age (Auto):", txtAge);
        addStyledRow(formPanel,"Civil status:",cbCivil);
        addStyledRow(formPanel, "Contact No. (09...):", txtContact);
        addStyledRow(formPanel, "Email:", txtEmail);
        addStyledRow(formPanel, "Address:", txtAddress);

        // Divider
        formPanel.add(Box.createVerticalStrut(10));
        JLabel lblCreds = new JLabel("Login Credentials");
        lblCreds.setFont(new Font("Arial", Font.BOLD, 14));
        lblCreds.setForeground(HEADER_BG);
        formPanel.add(lblCreds);
        formPanel.add(Box.createVerticalStrut(5));

        addStyledRow(formPanel, "Username:", txtUsername);
        addStyledRow(formPanel, "Password:", txtPassword);

        // --- 4. SAVE BUTTON ---
        JButton btnSave = createRoundedButton("Create Account", BTN_ADD_COLOR);
        btnSave.addActionListener(e -> {
            // Validation
            if (txtName.getText().isEmpty() || txtLastName.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Names are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtContact.getText().length() != 11) {
                JOptionPane.showMessageDialog(dialog, "Contact number must be 11 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (txtUsername.getText().isEmpty() || new String(txtPassword.getPassword()).isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Username and Password are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Date Logic
                String mStr = (String) cbMonth.getSelectedItem();
                int m = Month.valueOf(mStr.toUpperCase()).getValue();
                int d = Integer.parseInt((String) cbDay.getSelectedItem());
                int y = Integer.parseInt((String) cbYear.getSelectedItem());
                LocalDate birthDate = LocalDate.of(y, m, d);

                int age = Integer.parseInt(txtAge.getText());
                String fullName = txtName.getText() + " " + txtLastName.getText();

                // Mock ID for GUI
                int newId = staffTable.getRowCount() + 1;

                // Update UI Table
                tableModel.addRow(new Object[]{
                        String.valueOf(newId),
                        fullName,
                        cbPosition.getSelectedItem(),
                        txtContact.getText(),
                        "Active",
                        "Never"
                });

                // Build Staff Object
                BarangayStaff staff = BarangayStaff.builder()
                        .firstName(txtName.getText())
                        .lastName(txtLastName.getText())
                        .position((String) cbPosition.getSelectedItem())
                        .age(age)
                        .role((String) cbPosition.getSelectedItem())
                        .middleName(txtMiddleName.getText())
                        .dob(birthDate) // Use java.sql.Date for DB
                        .contactNo(txtContact.getText())
                        .email(txtEmail.getText())
                        .address(txtAddress.getText())
                        .username(txtUsername.getText())
                        .password(new String(txtPassword.getPassword()))
                        .civilStatus(cbCivil.getSelectedItem().toString())
                        .status("Active")
                        .lastLogin(java.time.LocalDateTime.now())
                        .createdAt(java.time.LocalDateTime.now())
                        .updatedAt(java.time.LocalDateTime.now())

                        .build();

                // Save to DB
                UserDataManager.getInstance().addStaff(staff);
                SystemLogDAO logDAO = new SystemLogDAO();
                logDAO.addLog("Added staff",staff.getFirstName() + " " + staff.getLastName(), Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
                JOptionPane.showMessageDialog(dialog, "Staff Account Created Successfully!");
                dialog.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error saving staff: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnSave);

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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