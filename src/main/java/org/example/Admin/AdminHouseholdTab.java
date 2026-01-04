package org.example.Admin;




import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.DatabaseConnection;
import org.example.HouseholdDAO;
import org.example.Interface.SecretaryPerformSearch;
import org.example.ResidentDAO;
import org.example.UserDataManager;
import org.example.Users.Household;
import org.example.Users.Resident;
import org.example.Admin.SystemLogDAO;
import org.example.utils.AutoRefresher;




import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;




public class AdminHouseholdTab extends JPanel {




    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblRecordCount;
    private JTextField searchField;




    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color BTN_ADD_COLOR = new Color(39, 174, 96);
    private final Color BTN_UPDATE_COLOR = new Color(41, 128, 185);
    private final Color BTN_DELETE_COLOR = new Color(231, 76, 60);
    private final Color FORM_BG = new Color(248, 249, 251);
    private final Color FIELD_BG = new Color(255, 255, 255);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);
    private final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);




    public AdminHouseholdTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);




        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);




        loadData();
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                if (refresher != null) {
                    refresher.stop();
                }
                loadData();
                refresher = new AutoRefresher("Household", AdminHouseholdTab.this::loadData);
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
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }




    private AutoRefresher refresher;




    private javax.swing.Timer lightTimer;
    private static volatile long lastGlobalUpdate = System.currentTimeMillis();




    private void startLightPolling() {
        lightTimer = new javax.swing.Timer(3000, e -> {
            if (table != null && table.getSelectedRow() == -1) {
                checkLightUpdate();
            }
        });
        lightTimer.start();
    }




    private void checkLightUpdate() {
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                String sql = "SELECT UNIX_TIMESTAMP(MAX(GREATEST(" +
                        "COALESCE(updatedAt, '1970-01-01'), " +
                        "COALESCE(createdAt, '1970-01-01')" +
                        "))) as last_ts FROM household";




                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {




                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        return rs.getLong("last_ts") * 1000L;
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
                        loadData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }




    // =========================================================================
    // DATA LOADING (Background Thread)
    // =========================================================================
    public void loadData() {
        new SwingWorker<List<Household>, Void>() {
            @Override
            protected List<Household> doInBackground() throws Exception {
                return new HouseholdDAO().getAllHouseholds();
            }




            @Override
            protected void done() {
                try {
                    List<Household> list = get();
                    if (tableModel != null)
                        tableModel.setRowCount(0);




                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");




                    for (Household h : list) {
                        String createdAtStr = "";
                        String updatedAtStr = "";




                        try {
                            if (h.getCreatedAt() != null) {
                                createdAtStr = dateFormat.format(h.getCreatedAt());
                            }
                            if (h.getUpdatedAt() != null) {
                                updatedAtStr = dateFormat.format(h.getUpdatedAt());
                            }
                        } catch (Exception e) {
                            createdAtStr = "N/A";
                            updatedAtStr = "N/A";
                        }




                        tableModel.addRow(new Object[] {
                                h.getHouseholdId(),
                                h.getHouseholdNo(),
                                h.getPurok(),
                                h.getStreet(),
                                h.getAddress(),
                                h.getHouseholdHeadId(),
                                h.getTotalMembers(),
                                h.getNotes(),
                                h.is4PsBeneficiary(),
                                h.getOwnershipType(),
                                h.getMonthlyIncome(),
                                h.getFamilyType(),
                                h.getElectricitySource(),
                                createdAtStr,
                                updatedAtStr
                        });
                    }
                    if (lblRecordCount != null)
                        lblRecordCount.setText("Total Households: " + tableModel.getRowCount());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }




    // =========================================================================
    // CRUD ACTIONS
    // =========================================================================
    private void handleAdd() {
        showDialog(null, "Register New Household");
    }




    private void handleUpdate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a household to update.");
            return;
        }




        int modelRow = table.convertRowIndexToModel(selectedRow);
        int householdId = (int) tableModel.getValueAt(modelRow, 0);
        Household h = new HouseholdDAO().findHouseholdById(householdId);




        if (h != null) {
            showDialog(h, "Update Household Details");
        } else {
            JOptionPane.showMessageDialog(this, "Error: Could not fetch household details.");
        }
    }




    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1)
            return;




        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String num = (String) tableModel.getValueAt(modelRow, 1);




        int confirm = JOptionPane.showConfirmDialog(this, "Delete Household " + num + "?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new HouseholdDAO().deleteHousehold(id);
            log.addLog("Deleted Household", "Household id:" + id,
                    Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
            loadData();
            JOptionPane.showMessageDialog(this, "Household deleted.");
        }
    }




    SystemLogDAO log = new SystemLogDAO();




    // =========================================================================
    // DIALOG - COMPLETELY REDESIGNED PROFESSIONAL FORM
    // =========================================================================
    private void showDialog(Household existing, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(950, 850);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setResizable(true); // Makes it resizable
        dialog.setModal(true); // Makes it modal (blocks parent window)




        // Set default close operation
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);




        // This ensures native window decorations
        dialog.setUndecorated(false);




        // Main container with gradient background
        JPanel mainContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);




                Color color1 = new Color(248, 250, 252);
                Color color2 = new Color(241, 245, 249);
                GradientPaint gradient = new GradientPaint(
                        0, 0, color1,
                        getWidth(), getHeight(), color2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainContainer.setOpaque(true);




        // HEADER SECTION
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(37, 99, 235)),
                new EmptyBorder(20, 30, 20, 30)));




        JLabel titleLabel = new JLabel("<html><div style='font-size:24px; font-weight:bold; color:white;'>" +
                title + "</div></html>");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);




        JLabel requiredLabel = new JLabel(
                "<html><div style='font-size:12px; color:rgba(255,255,255,0.9);'>* Required fields</div></html>");
        headerPanel.add(requiredLabel, BorderLayout.EAST);




        // FORM CONTENT
        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBorder(new EmptyBorder(25, 30, 25, 30));
        formContent.setBackground(FORM_BG);




        // Create fields with modern styling
        JTextField txtHouseNo = createModernTextField(existing != null ? existing.getHouseholdNo() : "",
                "Household No",
                "Enter unique household number (2-20 characters)");




        JTextField txtPurok = createModernTextField(existing != null ? existing.getPurok() : "",
                "Purok",
                "Enter purok/zone name (letters only)");




        JTextField txtStreet = createModernTextField(existing != null ? existing.getStreet() : "",
                "Street",
                "Enter street name (optional)");




        JTextField txtAddress = createModernTextField(existing != null ? existing.getAddress() : "",
                "Full Address",
                "Enter complete address");




        // Combo boxes
        String[] ownershipTypes = { "Owned", "Rented", "Free Use", "CareTaker", "Mortgaged" };
        JComboBox<String> cbOwnershipType = createModernComboBox(ownershipTypes, "Ownership Type");
        if (existing != null && existing.getOwnershipType() != null) {
            cbOwnershipType.setSelectedItem(existing.getOwnershipType());
        }




        String[] familyTypes = { "Nuclear", "Extended", "Single Parent", "Child-Headed", "Couple Only", "Other" };
        JComboBox<String> cbFamilyType = createModernComboBox(familyTypes, "Family Type");
        if (existing != null && existing.getFamilyType() != null) {
            cbFamilyType.setSelectedItem(existing.getFamilyType());
        }




        JTextField txtTotalMembers = createModernTextField(
                existing != null ? String.valueOf(existing.getTotalMembers()) : "1",
                "Total Members",
                "Number of household members");




        String[] electricitySources = { "Canoreco", "Electric Coop", "Solar", "Generator", "None", "Other" };
        JComboBox<String> cbElectricitySource = createModernComboBox(electricitySources, "Electricity Source");
        if (existing != null && existing.getElectricitySource() != null) {
            cbElectricitySource.setSelectedItem(existing.getElectricitySource());
        }




        JTextField txtMonthlyIncome = createModernTextField(existing != null ? existing.getMonthlyIncome() : "0.00",
                "Monthly Income (₱)",
                "Total monthly household income");




        // Head of Family Section
        JTextField txtHeadId = createModernTextField(
                existing != null ? String.valueOf(existing.getHouseholdHeadId()) : "",
                "Head ID",
                "Auto-filled when selecting head");
        txtHeadId.setEditable(false);
        txtHeadId.setBackground(new Color(249, 250, 251));




        JTextField txtHeadName = createModernTextField("", "Head Name", "Click Select to choose head of family");
        txtHeadName.setEditable(false);
        txtHeadName.setBackground(new Color(249, 250, 251));




        JButton btnSelectHead = createModernIconButton("Select Head", PRIMARY_COLOR);
        btnSelectHead.addActionListener(e -> openResidentSelector(dialog, txtHeadId, txtHeadName,
                txtHouseNo, txtPurok, txtStreet, txtAddress, txtTotalMembers));




        // Status Checkboxes
        JCheckBox chk4PsBeneficiary = createModernCheckbox("4Ps Beneficiary Household",
                existing != null && existing.is4PsBeneficiary(),
                "Check if household receives 4Ps benefits");




        // Date fields
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createdAt = existing != null && existing.getCreatedAt() != null
                ? dateFormat.format(existing.getCreatedAt())
                : "Will be set on save";
        String updatedAt = existing != null && existing.getUpdatedAt() != null
                ? dateFormat.format(existing.getUpdatedAt())
                : "Will be set on save";




        JTextField txtCreatedAt = createModernTextField(createdAt, "Date Created", "Auto-generated timestamp");
        txtCreatedAt.setEditable(false);
        txtCreatedAt.setBackground(new Color(249, 250, 251));




        JTextField txtUpdatedAt = createModernTextField(updatedAt, "Last Updated", "Auto-updated timestamp");
        txtUpdatedAt.setEditable(false);
        txtUpdatedAt.setBackground(new Color(249, 250, 251));




        // Notes
        JTextArea txtNotes = createModernTextArea(existing != null ? existing.getNotes() : "",
                "Additional notes about the household (max 500 chars)");




        // LAYOUT ORGANIZATION
        // Section 1: Basic Information
        formContent.add(createFormSection("Basic Information",
                new JComponent[] { txtHouseNo, txtPurok, txtStreet, txtAddress }));




        // Section 2: Family Details
        formContent.add(Box.createVerticalStrut(15));
        formContent.add(createFormSection("Family Details",
                new JComponent[] { cbOwnershipType, cbFamilyType, txtTotalMembers }));




        // Section 3: Utilities & Income
        formContent.add(Box.createVerticalStrut(15));
        formContent.add(createFormSection("Utilities & Income",
                new JComponent[] { cbElectricitySource, txtMonthlyIncome }));




        // Section 4: Head of Family
        formContent.add(Box.createVerticalStrut(15));
        JPanel headSection = createFormSection("Head of Family", null);
        JPanel headContent = new JPanel(new GridBagLayout());
        headContent.setBackground(Color.WHITE);
        headContent.setBorder(new EmptyBorder(15, 15, 15, 15));




        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);




        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        headContent.add(createFieldWithLabel("Head Name:", txtHeadName), gbc);




        gbc.gridx = 2;
        gbc.gridwidth = 1;
        headContent.add(btnSelectHead, gbc);




        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        headContent.add(createFieldWithLabel("Head ID:", txtHeadId), gbc);




        headSection.add(headContent, BorderLayout.CENTER);
        formContent.add(headSection);




        // Section 5: Status & Dates
        formContent.add(Box.createVerticalStrut(15));
        JPanel statusSection = createFormSection("Status & Timestamps", null);
        JPanel statusContent = new JPanel(new GridLayout(3, 1, 10, 10));
        statusContent.setBackground(Color.WHITE);
        statusContent.setBorder(new EmptyBorder(15, 15, 15, 15));




        statusContent.add(createCheckboxPanel(chk4PsBeneficiary));
        statusContent.add(createFieldWithLabel("Date Created:", txtCreatedAt));
        statusContent.add(createFieldWithLabel("Last Updated:", txtUpdatedAt));




        statusSection.add(statusContent, BorderLayout.CENTER);
        formContent.add(statusSection);




        // Section 6: Notes
        formContent.add(Box.createVerticalStrut(15));
        JPanel notesSection = createFormSection("Additional Notes", null);
        JPanel notesContent = new JPanel(new BorderLayout());
        notesContent.setBackground(Color.WHITE);
        notesContent.setBorder(new EmptyBorder(15, 15, 15, 15));




        JPanel notesHeader = new JPanel(new BorderLayout());
        notesHeader.setBackground(Color.WHITE);
        JLabel notesLabel = new JLabel("Household Notes:");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        notesLabel.setForeground(new Color(75, 85, 99));
        notesHeader.add(notesLabel, BorderLayout.WEST);
        notesHeader.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);




        notesContent.add(notesHeader, BorderLayout.NORTH);
        notesContent.add(new JScrollPane(txtNotes), BorderLayout.CENTER);




        notesSection.add(notesContent, BorderLayout.CENTER);
        formContent.add(notesSection);




        // Add vertical glue
        formContent.add(Box.createVerticalGlue());




        // Create scrollable form
        JScrollPane formScrollPane = new JScrollPane(formContent);
        formScrollPane.setBorder(null);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.getViewport().setBackground(FORM_BG);




        // BUTTON PANEL
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(FORM_BG);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
                new EmptyBorder(15, 30, 15, 30)));




        JButton btnCancel = createModernButton("Cancel", new Color(107, 114, 128));
        btnCancel.addActionListener(e -> dialog.dispose());




        JButton btnSave = createModernButton(existing == null ? "Create Household" : "Update Household",
                SUCCESS_COLOR);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));




// Save button action with validation
        btnSave.addActionListener(e -> {
            String houseNo = txtHouseNo.getText().trim();
            String purok = txtPurok.getText().trim();
            String street = txtStreet.getText().trim();
            String address = txtAddress.getText().trim();
            String headId = txtHeadId.getText().trim();
            String members = txtTotalMembers.getText().trim();
            String monthlyIncome = txtMonthlyIncome.getText().trim();
            String ownershipType = cbOwnershipType.getSelectedItem().toString();
            String familyType = cbFamilyType.getSelectedItem().toString();
            String electricitySource = cbElectricitySource.getSelectedItem().toString();
            String notes = txtNotes.getText().trim();
            boolean is4Ps = chk4PsBeneficiary.isSelected();


            List<String> errors = new ArrayList<>();


            // Validation (same as before)
            if (houseNo.isEmpty()) {
                errors.add("Household Number is required.");
            } else if (!isValidHouseholdNumber(houseNo)) {
                errors.add("Household Number should be 2-20 characters (letters, numbers, spaces, dash, underscore).");
            }


            if (notes.isEmpty()) {
                errors.add("Add a note");
            }
            if (purok.isEmpty()) {
                errors.add("Purok is required.");
            } else if (!isValidPurokName(purok)) {
                errors.add("Purok should contain only letters and spaces (no numbers or special characters).");
            }


            if (!street.isEmpty() && !isValidStreet(street)) {
                errors.add("Street should be 3-100 characters (letters, numbers, spaces, ., ,, #, ', -).");
            }


            if (address.isEmpty()) {
                errors.add("Full Address is required.");
            } else if (!isValidAddress(address)) {
                errors.add("Address should be 5-200 characters (letters, numbers, spaces, ., ,, #, ', -).");
            }


            if (headId.isEmpty()) {
                errors.add("Head of Family is required. Please select a resident.");
            } else if (!isValidHeadId(headId)) {
                errors.add("Invalid Head of Family ID.");
            }


            if (!isValidTotalMembers(members)) {
                errors.add("Total Members must be a positive whole number (minimum 1).");
            }
            if (members.length() > 2) {
                errors.add("Total members should not exceed a 100");
            }


            if (monthlyIncome.isEmpty()) {
                errors.add("Monthly Income is required. Enter 0.00 if none.");
            } else if (!isValidMonthlyIncome(monthlyIncome)) {
                errors.add("Monthly Income must be a positive number (e.g., 5000 or 5000.50).");
            }


            if (!isValidOwnershipType(ownershipType)) {
                errors.add("Invalid Ownership Type selected.");
            }


            if (!isValidFamilyType(familyType)) {
                errors.add("Invalid Family Type selected.");
            }


            if (!isValidElectricitySource(electricitySource)) {
                errors.add("Invalid Electricity Source selected.");
            }


            if (!isValidNotes(notes)) {
                errors.add("Notes contain invalid characters or are too long (max 500 characters).");
            }


            // NEW VALIDATION: Check if resident is already head of another household
            if (isValidHeadId(headId)) {
                int residentId = Integer.parseInt(headId);
                int currentHouseholdId = (existing != null) ? existing.getHouseholdId() : 0;


                if (isResidentAlreadyHeadOfHousehold(residentId, currentHouseholdId)) {
                    // Get resident name for better error message
                    String residentName = getResidentNameById(residentId);
                    errors.add("Resident '" + residentName + "' (ID: " + headId + ") is already the head of another household.");
                }
            }


            if (existing == null) {
                HouseholdDAO dao = new HouseholdDAO();
                if (dao.doesHouseholdExists(houseNo)) {
                    errors.add("Household Number '" + houseNo + "' already exists!");
                }
            }


            if (!errors.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder("<html><div style='width:350px;'>");
                errorMsg.append("<b>Please fix the following errors:</b><br><br>");
                for (String error : errors) {
                    errorMsg.append("• ").append(error).append("<br>");
                }
                errorMsg.append("</div></html>");


                JOptionPane.showMessageDialog(dialog, errorMsg.toString(),
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            try {
                Household h = new Household();
                h.setHouseholdNo(houseNo);
                h.setPurok(purok);
                h.setStreet(street);
                h.setAddress(address);
                h.setNotes(notes);


                try {
                    h.setHouseholdHeadId(Integer.parseInt(headId));
                } catch (NumberFormatException ex) {
                    h.setHouseholdHeadId(0);
                }


                try {
                    h.setTotalMembers(Integer.parseInt(members));
                } catch (NumberFormatException ex) {
                    h.setTotalMembers(1);
                }


                h.setOwnershipType(ownershipType);
                h.setMonthlyIncome(monthlyIncome);
                h.set4PsBeneficiary(is4Ps);
                h.setFamilyType(familyType);
                h.setElectricitySource(electricitySource);


                // Update resident's household information
                new ResidentDAO().updateResidentHouseHold(Integer.parseInt(headId), houseNo);


                HouseholdDAO dao = new HouseholdDAO();
                int actualMemberCount = dao.countMembers(houseNo);


                if (existing != null && actualMemberCount > Integer.parseInt(members)) {
                    int response = JOptionPane.showConfirmDialog(dialog,
                            "<html><div style='width:350px;'>"
                                    + "Warning: There are currently " + actualMemberCount
                                    + " members associated with this household.<br>"
                                    + "You're trying to set total members to " + members + ".<br>"
                                    + "This might remove some members from the household. Continue?"
                                    + "</div></html>",
                            "Member Count Warning",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);


                    if (response != JOptionPane.YES_OPTION) {
                        return;
                    }
                }


                boolean success;


                if (existing == null) {
                    success = dao.addHousehold(h);


                    log.addLog("Added Household", "Admin",
                            Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
                } else {
                    h.setHouseholdId(existing.getHouseholdId());
                    success = dao.updateHousehold(h);
                    log.addLog("Updated Household", "Admin",
                            Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
                }


                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            "<html><b>Household saved successfully!</b></html>",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Household head is already exist in other household. Please Try Again!!",
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }


            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Error: " + ex.getMessage(),
                        "System Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });




        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);




        // ASSEMBLE DIALOG
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(formScrollPane, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);




        dialog.add(mainContainer);
        dialog.setVisible(true);
    }




    // =========================================================================
    // MODERN UI COMPONENT CREATION METHODS
    // =========================================================================




    private JPanel createFormSection(String title, JComponent[] fields) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));




        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(249, 250, 251));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));




        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(31, 41, 55));
        header.add(titleLabel, BorderLayout.WEST);




        section.add(header, BorderLayout.NORTH);




        // Content area
        if (fields != null) {
            JPanel content = new JPanel(new GridLayout(fields.length, 1, 10, 10));
            content.setBackground(Color.WHITE);
            content.setBorder(new EmptyBorder(15, 20, 15, 20));




            for (JComponent field : fields) {
                content.add(field);
            }
            section.add(content, BorderLayout.CENTER);
        }




        return section;
    }




    private JTextField createModernTextField(String text, String placeholder, String tooltip) {
        JTextField field = new JTextField(text);




        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(31, 41, 55));
        field.setToolTipText("<html><div style='width:250px;'>" + tooltip + "</div></html>");




        // Add placeholder effect
        if (text.isEmpty()) {
            field.setText(placeholder);
            field.setForeground(new Color(156, 163, 175));
            field.setFont(new Font("Segoe UI", Font.ITALIC, 14));




            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                        field.setForeground(new Color(31, 41, 55));
                        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    }
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                            BorderFactory.createEmptyBorder(11, 14, 11, 14)));
                    field.setBackground(new Color(249, 250, 251));
                }




                @Override
                public void focusLost(FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        field.setText(placeholder);
                        field.setForeground(new Color(156, 163, 175));
                        field.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    }
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)));
                    field.setBackground(Color.WHITE);
                }
            });
        } else {
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                            BorderFactory.createEmptyBorder(11, 14, 11, 14)));
                    field.setBackground(new Color(249, 250, 251));
                }




                @Override
                public void focusLost(FocusEvent e) {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                            BorderFactory.createEmptyBorder(12, 15, 12, 15)));
                    field.setBackground(Color.WHITE);
                }
            });
        }




        return field;
    }




    private JComboBox<String> createModernComboBox(String[] items, String tooltip) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(31, 41, 55));
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (isSelected) {
                    c.setBackground(PRIMARY_COLOR);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(new Color(31, 41, 55));
                }
                return c;
            }
        });
        comboBox.setToolTipText(tooltip);




        return comboBox;
    }




    private JButton createModernButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(backgroundColor.darker(), 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));




        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(backgroundColor.darker().brighter(), 1),
                        BorderFactory.createEmptyBorder(12, 25, 12, 25)));
            }




            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(backgroundColor.darker(), 1),
                        BorderFactory.createEmptyBorder(12, 25, 12, 25)));
            }




            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }
        });




        return button;
    }




    private JButton createModernIconButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(backgroundColor.darker(), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));




        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }




            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });




        return button;
    }




    private JCheckBox createModernCheckbox(String text, boolean selected, String tooltip) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        checkbox.setBackground(Color.WHITE);
        checkbox.setForeground(new Color(31, 41, 55));
        checkbox.setSelected(selected);
        checkbox.setFocusPainted(false);
        checkbox.setToolTipText(tooltip);
        checkbox.setIcon(new BigCheckBoxIcon(24, false)); // Unchecked State
        checkbox.setSelectedIcon(new BigCheckBoxIcon(24, true)); // Checked State




        // Add some spacing between the box and the text
        checkbox.setIconTextGap(12);




        return checkbox;
    }




    private JTextArea createModernTextArea(String text, String tooltip) {
        JTextArea textArea = new JTextArea(text, 4, 20);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(new Color(31, 41, 55));
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        textArea.setToolTipText(tooltip);




        textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textArea.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        BorderFactory.createEmptyBorder(11, 14, 11, 14)));
            }




            @Override
            public void focusLost(FocusEvent e) {
                textArea.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)));
            }
        });




        return textArea;
    }




    private JPanel createFieldWithLabel(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);




        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(75, 85, 99));
        label.setPreferredSize(new Dimension(120, 30));




        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);




        return panel;
    }




    private JPanel createCheckboxPanel(JCheckBox checkbox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(Color.WHITE);
        panel.add(checkbox);
        return panel;
    }




    // =========================================================================
    // VALIDATION METHODS
    // =========================================================================




    private boolean isValidHouseholdNumber(String houseNo) {
        if (houseNo == null || houseNo.trim().isEmpty())
            return false;
        String regex = "^[A-Za-z0-9\\s_-]{2,20}$";
        return houseNo.matches(regex);
    }




    private boolean isValidPurokName(String purok) {
        if (purok == null || purok.trim().isEmpty())
            return false;
        String regex = "^[A-Za-z0-9\\s]{2,50}$";
        return purok.matches(regex);
    }




    private boolean isValidStreet(String street) {
        if (street == null || street.trim().isEmpty())
            return true;
        String regex = "^[A-Za-z0-9\\s.,#'-]{3,100}$";
        return street.matches(regex);
    }




    private boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty())
            return false;
        String regex = "^[A-Za-z0-9\\s.,#'-]{5,200}$";
        return address.matches(regex);
    }




    private boolean isValidHeadId(String headId) {
        if (headId == null || headId.trim().isEmpty())
            return false;
        String regex = "^\\d+$";
        if (!headId.matches(regex))
            return false;
        try {
            int id = Integer.parseInt(headId);
            return id > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }




    private boolean isValidTotalMembers(String members) {
        if (members == null || members.trim().isEmpty())
            return false;
        String regex = "^\\d+$";
        if (!members.matches(regex))
            return false;
        try {
            int count = Integer.parseInt(members);
            return count >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }




    private boolean isValidMonthlyIncome(String income) {
        if (income == null || income.trim().isEmpty())
            return false;
        String regex = "^\\d+(\\.\\d{1,2})?$";
        if (!income.matches(regex))
            return false;
        try {
            double amount = Double.parseDouble(income);
            return amount >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }




    private boolean isValidOwnershipType(String type) {
        if (type == null || type.trim().isEmpty())
            return false;
        String[] validTypes = { "Owned", "Rented", "Free Use", "CareTaker", "Mortgaged" };
        for (String valid : validTypes) {
            if (valid.equals(type))
                return true;
        }
        return false;
    }




    private boolean isValidFamilyType(String type) {
        if (type == null || type.trim().isEmpty())
            return false;
        String[] validTypes = { "Nuclear", "Extended", "Single Parent", "Child-Headed", "Couple Only", "Other" };
        for (String valid : validTypes) {
            if (valid.equals(type))
                return true;
        }
        return false;
    }




    private boolean isValidElectricitySource(String source) {
        if (source == null || source.trim().isEmpty())
            return false;
        String[] validSources = { "Canoreco", "Electric Coop", "Solar", "Generator", "None", "Other" };
        for (String valid : validSources) {
            if (valid.equals(source))
                return true;
        }
        return false;
    }




    private boolean isValidNotes(String notes) {
        if (notes == null)
            return true;
        if (notes.length() > 500)
            return false;
        String regex = "^[A-Za-z0-9\\s.,!?()'\"-]*$";
        return notes.matches(regex);
    }




    // =========================================================================
    // RESIDENT SELECTOR (For Head of Family)
    // =========================================================================
    private void openResidentSelector(JDialog parent, JTextField idField, JTextField nameField,
                                      JTextField householdNo, JTextField purok, JTextField street,
                                      JTextField address, JTextField memberCountField) {
        JDialog resDialog = new JDialog(parent, "Select Head of Family", true);
        resDialog.setSize(600, 500);
        resDialog.setLocationRelativeTo(parent);
        resDialog.setLayout(new BorderLayout());




        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(FORM_BG);




        JLabel titleLabel = new JLabel("Select Head of Family");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));




        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(FORM_BG);
        searchPanel.add(new JLabel("Search Resident:"), BorderLayout.WEST);




        JTextField search = createModernTextField("", "Search by name or ID", "Type to search residents");
        searchPanel.add(search, BorderLayout.CENTER);




        ResidentDAO rDao = new ResidentDAO();
        java.util.List<Resident> residents = rDao.getAllResidents();




        String[] cols = { "ID", "Full Name", "Address", "Household No", "Purok", "Street" };
        DefaultTableModel resModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };




        for (Resident r : residents) {
            resModel.addRow(new Object[] {
                    r.getResidentId(),
                    r.getFirstName() + " " + r.getLastName(),
                    r.getAddress(),
                    r.getHouseholdNo(),
                    r.getPurok(),
                    r.getStreet()
            });
        }




        JTable resTable = new JTable(resModel);
        resTable.setRowHeight(35);
        resTable.getTableHeader().setBackground(PRIMARY_COLOR);
        resTable.getTableHeader().setForeground(Color.WHITE);
        resTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));




        resTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);




                if (isSelected) {
                    c.setBackground(new Color(220, 240, 255));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : Color.WHITE);
                    c.setForeground(Color.BLACK);
                }




                return c;
            }
        });




        TableRowSorter<DefaultTableModel> resSorter = new TableRowSorter<>(resModel);
        resTable.setRowSorter(resSorter);
        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                resSorter.setRowFilter(RowFilter.regexFilter("(?i)" + search.getText()));
            }
        });




        JButton btnSelect = createModernButton("Select This Resident", BTN_ADD_COLOR);
        btnSelect.addActionListener(e -> {
            int row = resTable.getSelectedRow();
            if (row != -1) {
                int modelRow = resTable.convertRowIndexToModel(row);
                int resId = (int) resModel.getValueAt(modelRow, 0);
                String name = (String) resModel.getValueAt(modelRow, 1);
                String add = (String) resModel.getValueAt(modelRow, 2);
                String household = (String) resModel.getValueAt(modelRow, 3);
                String purokData = (String) resModel.getValueAt(modelRow, 4);
                String streetData = (String) resModel.getValueAt(modelRow, 5);




                idField.setText(String.valueOf(resId));
                nameField.setText(name);




                if (household != null && !household.isEmpty()) {
                    householdNo.setText(household);
                } else {
                    householdNo.setText("HH-" + System.currentTimeMillis() % 1000);
                }




                address.setText(add != null ? add : "");
                purok.setText(purokData != null ? purokData : "");
                street.setText(streetData != null ? streetData : "");




                if (household != null && !household.isEmpty()) {
                    int count = new HouseholdDAO().countMembers(household);
                    memberCountField.setText(String.valueOf(count > 0 ? count : 1));
                } else {
                    memberCountField.setText("1");
                }




                resDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(resDialog,
                        "Please select a resident from the list.",
                        "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });




        JButton btnCancel = createModernButton("Cancel", new Color(120, 120, 120));
        btnCancel.addActionListener(e -> resDialog.dispose());




        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(FORM_BG);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSelect);




        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(resTable), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);




        resDialog.add(mainPanel);
        resDialog.setVisible(true);
    }




    // =========================================================================
    // UI HELPERS
    // =========================================================================




    private JPanel createContentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));




        // --- 1. ACTION BUTTONS ---
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btns.setBackground(BG_COLOR);
        // (Important for BoxLayout alignment consistency)
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Limit height of button row




        JButton btnAdd = createModernButton("+ New Household", BTN_ADD_COLOR);
        btnAdd.addActionListener(e -> handleAdd());




        JButton btnEdit = createModernButton("Edit Details", BTN_UPDATE_COLOR);
        btnEdit.addActionListener(e -> handleUpdate());




        JButton btnDelete = createModernButton("Delete", BTN_DELETE_COLOR);
        btnDelete.addActionListener(e -> handleDelete());




        JButton btnPrint = createModernButton("📊 Print Report", new Color(155, 89, 182));
        btnPrint.addActionListener(e -> handlePrint());




        JButton btnRefresh = createModernButton("🔄 Refresh", new Color(52, 152, 219));
        btnRefresh.addActionListener(e -> loadData());




        btns.add(btnAdd);
        btns.add(btnEdit);
        btns.add(btnDelete);
        btns.add(btnRefresh);
        btns.add(btnPrint);




        p.add(btns);
        p.add(Box.createVerticalStrut(20));




        // --- 2. SEARCH PANEL (SHORT & COMPACT) ---
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(BG_COLOR);




        // --- FIX HERE: Set FIXED WIDTH (300px) and FIXED HEIGHT (35px) ---
        searchPanel.setMaximumSize(new Dimension(300, 35));
        searchPanel.setPreferredSize(new Dimension(300, 35));
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align to left edge




        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(new Color(60, 60, 60));




        // White box container for Field + Clear Button
        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(Color.WHITE);
        fieldWrapper.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));




        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        searchField.setBackground(Color.WHITE);




        JButton btnClear = new JButton("x");
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setForeground(new Color(150, 150, 150));
        btnClear.setBackground(Color.WHITE);
        btnClear.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        btnClear.setFocusPainted(false);
        btnClear.setContentAreaFilled(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));




        btnClear.addActionListener(e -> {
            searchField.setText("");
            if (sorter != null)
                sorter.setRowFilter(null);
        });




        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (sorter != null) {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
                }
            }
        });




        fieldWrapper.add(searchField, BorderLayout.CENTER);
        fieldWrapper.add(btnClear, BorderLayout.EAST);




        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(fieldWrapper, BorderLayout.CENTER);




        p.add(searchPanel);
        p.add(Box.createVerticalStrut(10));




        // --- 3. DATA TABLE ---
        String[] cols = { "ID", "Household No", "Purok", "Street", "Address", "Head ID",
                "Members", "Notes", "4Ps", "Ownership", "Income", "Family Type",
                "Electricity", "Created", "Updated" };




        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }




            public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 5 || c == 6)
                    return Integer.class;
                if (c == 8)
                    return Boolean.class;
                return String.class;
            }
        };




        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));




        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(70);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);
        table.getColumnModel().getColumn(8).setPreferredWidth(50);
        table.getColumnModel().getColumn(9).setPreferredWidth(90);
        table.getColumnModel().getColumn(10).setPreferredWidth(90);
        table.getColumnModel().getColumn(11).setPreferredWidth(100);
        table.getColumnModel().getColumn(12).setPreferredWidth(100);
        table.getColumnModel().getColumn(13).setPreferredWidth(120);
        table.getColumnModel().getColumn(14).setPreferredWidth(120);




        sorter = new TableRowSorter<>(tableModel);
        Comparator<Integer> intComp = (o1, o2) -> {
            if (o1 == null && o2 == null)
                return 0;
            if (o1 == null)
                return -1;
            if (o2 == null)
                return 1;
            return o1.compareTo(o2);
        };
        sorter.setComparator(0, intComp);
        sorter.setComparator(5, intComp);
        sorter.setComparator(6, intComp);
        table.setRowSorter(sorter);




        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);




        table.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Boolean) {
                    setText((Boolean) value ? "✅ Yes" : "❌ No");
                    setHorizontalAlignment(JLabel.CENTER);
                }
                if (isSelected) {
                    c.setBackground(new Color(220, 240, 255));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });




        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i == 5 || i == 6 || i == 8) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }




        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    handleUpdate();
            }
        });




        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        tableScroll.getViewport().setBackground(Color.WHITE);
        // Align table to left as well
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);




        p.add(tableScroll);




        JPanel recordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recordPanel.setBackground(BG_COLOR);
        recordPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align footer
        lblRecordCount = new JLabel("Total Households: 0");
        lblRecordCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRecordCount.setForeground(PRIMARY_COLOR);
        recordPanel.add(lblRecordCount);
        p.add(recordPanel);




        return p;
    }




    private void handlePrint() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as PDF");
        int userSelection = fileChooser.showSaveDialog(this);


        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getAbsolutePath().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }


            try {
                // 1. Create Document (Landscape)
                Document doc = new Document(PageSize.A4.rotate()); // ✅ Fixed: lowercase 'rotate()'
                PdfWriter.getInstance(doc, new FileOutputStream(file));
                doc.open();


                // 2. Add Title
                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                Paragraph title = new Paragraph("Household Report List", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);


                // 3. Create Table
                int colCount = table.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);


                // 4. Add Headers (✅ MATCHING YOUR TABLE COLORS)
                // We use the same color: new Color(52, 152, 219)
                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);


                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);


                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(table.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor); // ✅ Blue Background
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8); // More padding like your table
                    pdfTable.addCell(cell);
                }


                // 5. Add Rows
                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);


                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = table.getValueAt(i, j);
                        String text = (val != null) ? val.toString() : "";


                        PdfPCell cell = new PdfPCell(new Paragraph(text, rowFont));
                        cell.setPadding(6);
                        cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);


                        // ✅ Zebra Striping (Light Blue tint to match theme)
                        if (i % 2 == 1) {
                            cell.setBackgroundColor(new java.awt.Color(235, 245, 250));
                        }


                        pdfTable.addCell(cell);
                    }
                }


                doc.add(pdfTable);
                doc.close();


                JOptionPane.showMessageDialog(this, "Export Success!\nFile: " + file.getAbsolutePath());


            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Export Error: " + e.getMessage());
            }
        }
    }






    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);




        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(240, 245, 255),
                getWidth(), getHeight(), new Color(255, 255, 255));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }




    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);




                GradientPaint headerGradient = new GradientPaint(
                        0, 0, new Color(41, 128, 185),
                        getWidth(), 0, new Color(52, 152, 219));
                g2d.setPaint(headerGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        h.setOpaque(false);
        h.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(30, 100, 150)),
                new EmptyBorder(25, 40, 25, 40)));




        JLabel l = new JLabel("🏠 Household Management");
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(Color.WHITE);
        h.add(l, BorderLayout.WEST);




        JLabel subLabel = new JLabel("Manage household records and information");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(new Color(255, 255, 255, 200));
        h.add(subLabel, BorderLayout.SOUTH);




        return h;
    }




    // Helper class to draw a bigger, modern checkbox
    private static class BigCheckBoxIcon implements Icon {
        private final int size;
        private final boolean isSelected;




        public BigCheckBoxIcon(int size, boolean isSelected) {
            this.size = size;
            this.isSelected = isSelected;
        }




        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);




            if (isSelected) {
                // Checked: Blue Background
                g2.setColor(new Color(59, 130, 246)); // Your PRIMARY_COLOR
                g2.fillRoundRect(x, y, size, size, 6, 6); // Rounded corners




                // Checked: White Checkmark
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Draw checkmark coordinates based on size
                g2.drawLine(x + 5, y + size / 2, x + size / 2 - 2, y + size - 5); // Short leg
                g2.drawLine(x + size / 2 - 2, y + size - 5, x + size - 5, y + 5); // Long leg
            } else {
                // Unchecked: White Background with Gray Border
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, size, size, 6, 6);




                g2.setColor(new Color(209, 213, 219)); // Light Gray Border
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x, y, size - 1, size - 1, 6, 6);
            }
            g2.dispose();
        }




        @Override
        public int getIconWidth() {
            return size;
        }




        @Override
        public int getIconHeight() {
            return size;
        }
    }
    // In HouseholdDAO.java
    // Add this method to HouseholdDAO.java
    // Add this to AdminHouseholdTab.java in VALIDATION METHODS section
    private boolean isResidentAlreadyHeadOfHousehold(int residentId, int currentHouseholdId) {
        System.out.println("[DEBUG] Checking if resident " + residentId + " is head of another household (current household: " + currentHouseholdId + ")");

        if (residentId <= 0) {
            return false;
        }

        try {
            String sql;
            PreparedStatement pstmt;
            Connection conn = null;

            try {
                conn = DatabaseConnection.getConnection();

                if (currentHouseholdId > 0) {
                    sql = "SELECT COUNT(*) FROM household WHERE household_head_id = ? AND household_id != ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, residentId);
                    pstmt.setInt(2, currentHouseholdId);
                    System.out.println("[DEBUG] Checking OTHER households only");
                } else {
                    sql = "SELECT COUNT(*) FROM household WHERE household_head_id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, residentId);
                    System.out.println("[DEBUG] Checking ALL households (new household)");
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("[DEBUG] Found " + count + " household(s) where resident is head");
                    return count > 0;
                }

                return false;

            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to get resident name by ID
    private String getResidentNameById(int residentId) {
        try {
            // Get all residents and find the one with matching ID
            List<Resident> allResidents = new ResidentDAO().getAllResidents();


            for (Resident resident : allResidents) {
                if (resident.getResidentId() == residentId) {
                    return resident.getFirstName() + " " + resident.getLastName();
                }
            }
            return "Unknown Resident";
        } catch (Exception e) {
            return "Unknown Resident";
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }




            JFrame f = new JFrame("Household Management System");
            f.setSize(1200, 750);
            f.setMinimumSize(new Dimension(1000, 600));
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);




            f.add(new AdminHouseholdTab());
            f.setVisible(true);
        });
    }
}


