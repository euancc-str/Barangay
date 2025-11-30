package org.example.Admin;

import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.List;
import java.time.LocalDate;
import java.time.Period;
import java.time.Month;

public class AdminStaffTab extends JPanel {

    private JTable staffTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94);
    private final Color BTN_ADD_COLOR = new Color(76, 175, 80);      // Green
    private final Color BTN_UPDATE_COLOR = new Color(100, 149, 237); // Blue
    private final Color BTN_DEACTIVATE_COLOR = new Color(255, 77, 77); // Red

    public AdminStaffTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);
        loadStaffData();
    }

    public void loadStaffData() {
        tableModel.setRowCount(0);
        StaffDAO staffDAO = new StaffDAO();
        List <BarangayStaff> staffList = staffDAO.getAllStaff();
        for(BarangayStaff staff: staffList){
            tableModel.addRow(new Object[]{staff.getStaffId(),staff.getName(),staff.getPosition(),staff.getContactNo(),staff.getStatus(),staff.getLastLogin()});
        }
        if(staffTable!=null){
            staffTable.repaint();
        }
    }

    // =========================================================================
    // 1. DEACTIVATE LOGIC (With Warning/Error Prevention)
    // =========================================================================
    private void handleDeactivate() {
        int selectedRow = staffTable.getSelectedRow();
        System.out.println(selectedRow);
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a staff member.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = staffTable.convertRowIndexToModel(selectedRow);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 4);

        if("Inactive".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This staff member is already inactive.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // ERROR PREVENTION: Warning Dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><body style='width: 300px;'>" +
                        "<b>WARNING: Account Deactivation</b><br><br>" +
                        "Are you sure you want to deactivate <b>" + name + "</b>?<br>" +
                        "They will lose access to the system immediately.<br>" +
                        "<i>(This preserves history, unlike Delete)</i>" +
                        "</body></html>",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.setValueAt("Inactive", modelRow, 4);
            StaffDAO dao = new StaffDAO();
            dao.deactivateStaff("Inactive",name, selectedRow+1);
            SystemLogDAO logDAO = new SystemLogDAO();
            logDAO.addLog("Inactivated staff",name, Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
            JOptionPane.showMessageDialog(this, "Staff access deactivated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    // 2. UPDATE LOGIC (The "Nice GUI" Dialog)
    // =========================================================================
    private void handleUpdate() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = staffTable.convertRowIndexToModel(selectedRow);

        // Retrieve current values
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String position = (String) tableModel.getValueAt(modelRow, 2);
        String contact = (String) tableModel.getValueAt(modelRow, 3);
        String status = (String) tableModel.getValueAt(modelRow, 4);

        showUpdateStaffDialog(id, name, position, contact, status, modelRow);
    }

    private void showUpdateStaffDialog(String id, String currentName, String currentPos, String currentContact, String currentStatus, int modelRow) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Staff Profile", true);
        dialog.setSize(550, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel titleLabel = new JLabel("Edit Staff Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(HEADER_BG);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Details Panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- EDITABLE FIELDS ---

        // Staff ID (Read-Only)
        JTextField txtId = createStyledTextField(id);

        txtId.setBackground(new Color(250, 250, 250));
        addStyledRow(detailsPanel, "Staff ID:", txtId);

        // Full Name (Editable)
        JTextField txtName = createStyledTextField(currentName);

        addStyledRow(detailsPanel, "Full Name:", txtName);

        // Position (ComboBox)
        String[] positions = {"Secretary", "Treasurer", "Brgy.Captain"};
        JComboBox<String> cbPosition = new JComboBox<>(positions);
        cbPosition.setSelectedItem(currentPos);
        cbPosition.setFont(new Font("Arial", Font.PLAIN, 14));
        cbPosition.setBackground(Color.WHITE);
        cbPosition.setPopupVisible(false);
        addStyledRow(detailsPanel, "Position:", cbPosition);
        // Contact (Editable)
        JTextField txtContact = createStyledTextField(currentContact);
        addStyledRow(detailsPanel, "Contact No:", txtContact);

        // Status (ComboBox)

        String[] statuses = {"Active", "Inactive", "Suspended"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setSelectedItem(currentStatus);
        cbStatus.setFont(new Font("Arial", Font.PLAIN, 14));

        cbStatus.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Account Status:", cbStatus);
        BarangayStaff staff = new StaffDAO().findStaffById(Integer.parseInt(id));
        JTextField txtUser = createStyledTextField("");

        addStyledRow(detailsPanel,"Set new username:",txtUser);

        JTextField txtPass = createStyledTextField("");

        addStyledRow(detailsPanel,"Set new password",txtPass);
        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = createRoundedButton("Cancel", Color.GRAY);
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = createRoundedButton("Save Changes", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(200, 45));

        // SAVE ACTION (With Validation & Confirm)
        btnSave.addActionListener(e -> {
            String newName = txtName.getText().trim();
            String newContact = txtContact.getText().trim();
            String pass = txtPass.getText().trim();
            String user = txtUser.getText().trim();
            if(pass.isEmpty() || user.isEmpty()){
                JOptionPane.showMessageDialog(dialog, "User or pass cannot be empty!!!!!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(pass.length() < 5 && user.length() <5){
                JOptionPane.showMessageDialog(dialog, "pass or username's length cant be less than 5", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // ERROR PREVENTION
            if (newName.isEmpty() || newContact.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Contact Number cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(newContact.length() != 11 || !newContact.matches("\\d*")){
                JOptionPane.showMessageDialog(dialog,"Contact number's length must be 11 and must only contain numbers!!!","Validation Error",JOptionPane.ERROR_MESSAGE);
                return;
            }
            // CONFIRMATION
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to update details for " + currentName + "?",
                    "Confirm Update",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Update Table Model
                tableModel.setValueAt(newName, modelRow, 1);
                tableModel.setValueAt(cbPosition.getSelectedItem(), modelRow, 2);
                tableModel.setValueAt(newContact, modelRow, 3);
                tableModel.setValueAt(cbStatus.getSelectedItem(), modelRow, 4);
                StaffDAO staffDAO = new StaffDAO();
                staffDAO.setStaffStatus(cbStatus.getSelectedItem().toString(),newName,Integer.parseInt(txtId.getText()), newContact,cbPosition.getSelectedItem().toString(),txtPass.getText(),txtUser.getText());
                SystemLogDAO logDAO = new SystemLogDAO();
                logDAO.addLog("Updated staff status",newName, Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
                JOptionPane.showMessageDialog(dialog, "Staff details updated successfully!");
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
    // VISUAL HELPERS (Matches AdminRequestTab)
    // =========================================================================

    private void addStyledRow(JPanel panel, String labelText, JComponent field) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
        label.setPreferredSize(new Dimension(150, 35));

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
                BorderFactory.createLineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 5, 5, 5)));
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
        staffTable.setSelectionBackground(new Color(200, 240, 240));
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
        tableScrollPane.setBorder(BorderFactory.createLineBorder(TABLE_HEADER_BG, 2));
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
        cbPosition.setBackground(Color.WHITE);
        cbPosition.setFont(new Font("Arial", Font.PLAIN, 14));

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
        addStyledRow(formPanel, "Last Name:", txtLastName);
        addStyledRow(formPanel, "Position:", cbPosition);
        addStyledRow(formPanel, "Date of Birth:", datePanel);
        addStyledRow(formPanel, "Age (Auto):", txtAge);
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
                        .dob(birthDate) // Use java.sql.Date for DB
                        .contactNo(txtContact.getText())
                        .email(txtEmail.getText())
                        .address(txtAddress.getText())
                        .username(txtUsername.getText())
                        .password(new String(txtPassword.getPassword()))
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
        JLabel lblModule = new JLabel("Staff Management");
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
            JFrame frame = new JFrame("Admin Staff Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new AdminStaffTab());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}