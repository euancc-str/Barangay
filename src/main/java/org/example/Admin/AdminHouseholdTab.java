package org.example.Admin;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.sql.Timestamp;


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

    // --- ADD DATE FILTER VARIABLES (COPIED FROM SecretaryPrintDocument) ---
    private JButton dateFilterBtn; // Changed from JComboBox to JButton
    private Date selectedDate; // Added for calendar selection

    // --- ADD CALENDAR COLOR VARIABLES (COPIED FROM SecretaryPrintDocument) ---
    private final Color MODERN_BLUE = new Color(66, 133, 244);
    private final Color LIGHT_GREY = new Color(248, 249, 250);
    private final Color DARK_GREY = new Color(52, 58, 64);
    private JComboBox<String> periodFilterBox;
    private JComboBox<String> yearFilterBox;

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
                            "Error saving household. Please try again.",
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
        p.setBorder(new EmptyBorder(30, 50, 30, 50)); // Keeping the nice padding from Old Code

        // --- 1. ACTION BUTTONS (From Old Code) ---
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btns.setBackground(BG_COLOR);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        btns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

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

        // --- 2. SEARCH PANEL (From Old Code) ---
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setMaximumSize(new Dimension(300, 35));
        searchPanel.setPreferredSize(new Dimension(300, 35));
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchLabel.setForeground(new Color(60, 60, 60));

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
            applyFilters(); // Changed to use new filter logic
        });

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters(); // Changed to use new filter logic
            }
        });

        fieldWrapper.add(searchField, BorderLayout.CENTER);
        fieldWrapper.add(btnClear, BorderLayout.EAST);
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(fieldWrapper, BorderLayout.CENTER);

        p.add(searchPanel);
        p.add(Box.createVerticalStrut(10));

        // --- 3. FILTER PANEL (INJECTED FROM NEW CODE) ---
        JPanel dateFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dateFilterPanel.setBackground(BG_COLOR);
        // Important: Match alignment to the rest of the layout
        dateFilterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateFilterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel lblPeriod = new JLabel("Filter:");
        lblPeriod.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateFilterPanel.add(lblPeriod);

        String[] periods = {"All Periods", "This Week", "This Month"};
        periodFilterBox = new JComboBox<>(periods);
        periodFilterBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        periodFilterBox.setBackground(Color.WHITE);
        periodFilterBox.setFocusable(false);
        periodFilterBox.addActionListener(e -> {
            if (!periodFilterBox.getSelectedItem().equals("All Periods")) {
                yearFilterBox.setSelectedIndex(0);
                selectedDate = null;
                dateFilterBtn.setText("📅 Select Date");
                dateFilterBtn.setForeground(Color.DARK_GRAY);
            }
            applyFilters();
        });
        dateFilterPanel.add(periodFilterBox);

        dateFilterPanel.add(Box.createHorizontalStrut(10));

        JLabel lblYear = new JLabel("Year:");
        lblYear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateFilterPanel.add(lblYear);

        yearFilterBox = new JComboBox<>();
        yearFilterBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yearFilterBox.setBackground(Color.WHITE);
        yearFilterBox.setFocusable(false);
        yearFilterBox.addItem("All Years");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i <= 5; i++) {
            yearFilterBox.addItem(String.valueOf(currentYear - i));
        }
        yearFilterBox.addActionListener(e -> {
            if (!yearFilterBox.getSelectedItem().equals("All Years")) {
                periodFilterBox.setSelectedIndex(0);
                selectedDate = null;
                dateFilterBtn.setText("📅 Select Date");
                dateFilterBtn.setForeground(Color.DARK_GRAY);
            }
            applyFilters();
        });
        dateFilterPanel.add(yearFilterBox);

        dateFilterPanel.add(Box.createHorizontalStrut(10));

        // Custom Date Button
        dateFilterBtn = new JButton("📅 Select Date") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selectedDate == null) {
                    g2d.setColor(new Color(233, 236, 239));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    GradientPaint gradient = new GradientPaint(0, 0, MODERN_BLUE, getWidth(), getHeight(), new Color(26, 115, 232));
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                super.paintComponent(g2d);
            }
        };
        dateFilterBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateFilterBtn.setForeground(selectedDate == null ? Color.DARK_GRAY : Color.WHITE);
        dateFilterBtn.setFocusPainted(false);
        dateFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dateFilterBtn.setBorder(new RoundBorder(8, new Color(206, 212, 218)));
        dateFilterBtn.setContentAreaFilled(false);
        dateFilterBtn.setOpaque(false);
        dateFilterBtn.setPreferredSize(new Dimension(140, 35));
        dateFilterBtn.addActionListener(e -> showModernDatePicker());

        JButton clearDateBtn = new JButton("Clear") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(233, 236, 239));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2d);
            }
        };
        clearDateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearDateBtn.setForeground(Color.DARK_GRAY);
        clearDateBtn.setFocusPainted(false);
        clearDateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearDateBtn.setBorder(new RoundBorder(8, new Color(206, 212, 218)));
        clearDateBtn.setContentAreaFilled(false);
        clearDateBtn.setOpaque(false);
        clearDateBtn.setPreferredSize(new Dimension(80, 35));
        clearDateBtn.addActionListener(e -> {
            selectedDate = null;
            dateFilterBtn.setText("📅 Select Date");
            dateFilterBtn.setForeground(Color.DARK_GRAY);
            applyFilters();
        });

        dateFilterPanel.add(dateFilterBtn);
        dateFilterPanel.add(Box.createHorizontalStrut(5));
        dateFilterPanel.add(clearDateBtn);

        p.add(dateFilterPanel);
        p.add(Box.createVerticalStrut(10));

        // --- 4. DATA TABLE (From Old Code - Keeps the better layout) ---
        String[] cols = { "ID", "Household No", "Purok", "Street", "Address", "Head ID",
                "Members", "Notes", "4Ps", "Ownership", "Income", "Family Type",
                "Electricity", "Created", "Updated" };

        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 5 || c == 6) return Integer.class;
                if (c == 8) return Boolean.class;
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Ensure table fills the width (Old Code logic implies this via ScrollPane layout)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);

        // Column widths (From Old Code)
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
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
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
                if (e.getClickCount() == 2) handleUpdate();
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(tableScroll);

        JPanel recordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recordPanel.setBackground(BG_COLOR);
        recordPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblRecordCount = new JLabel("Total Households: 0");
        lblRecordCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRecordCount.setForeground(PRIMARY_COLOR);
        recordPanel.add(lblRecordCount);
        p.add(recordPanel);

        return p;
    }
    // =========================================================================
    //  FILTER LOGIC (ADAPTED FROM SecretaryPrintDocument)
    // =========================================================================
    private void applyFilters() {
        if (sorter == null) return;

        String text = searchField.getText();
        String selectedPeriod = (String) periodFilterBox.getSelectedItem();
        String selectedYear = (String) yearFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Text Search Filter
        if (text != null && !text.trim().isEmpty()) {
            String regexPattern = "(?i)" + java.util.regex.Pattern.quote(text.trim());
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object value = entry.getValue(i);
                        if (value != null && value.toString().toLowerCase().contains(text.trim().toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        // 2. Specific Date Filter (Calendar Picker - Column 13)
        if (selectedDate != null) {
            final Date finalSelectedDate = selectedDate;
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(13); // Created At Column
                        if (val == null || !(val instanceof String)) return false;

                        String dateStr = (String) val; // Format: yyyy-MM-dd HH:mm:ss
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        // Compare string prefixes (yyyy-MM-dd)
                        return dateStr.startsWith(sdf.format(finalSelectedDate));

                    } catch (Exception e) { return false; }
                }
            });
        }

        // 3. Period Filter (This Week / This Month)
        if (selectedPeriod != null && !selectedPeriod.equals("All Periods") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(13); // Created At Column
                        if (val == null || !(val instanceof String)) return false;

                        // Parse String to LocalDate
                        String dateStr = ((String) val).substring(0, 10);
                        java.time.LocalDate rowDate = java.time.LocalDate.parse(dateStr);
                        java.time.LocalDate now = java.time.LocalDate.now();

                        if (selectedPeriod.equals("This Week")) {
                            java.time.temporal.TemporalField fieldISO = java.time.temporal.WeekFields.of(java.util.Locale.FRANCE).dayOfWeek();
                            java.time.LocalDate startOfWeek = now.with(fieldISO, 1);
                            java.time.LocalDate endOfWeek = now.with(fieldISO, 7);
                            return !rowDate.isBefore(startOfWeek) && !rowDate.isAfter(endOfWeek);
                        } else if (selectedPeriod.equals("This Month")) {
                            return rowDate.getMonth() == now.getMonth() && rowDate.getYear() == now.getYear();
                        }
                        return true;
                    } catch (Exception e) { return false; }
                }
            });
        }

        // 4. Year Filter (2025, 2024...)
        if (selectedYear != null && !selectedYear.equals("All Years") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(13); // Created At Column
                        if (val == null || !(val instanceof String)) return false;

                        String dateStr = ((String) val).substring(0, 10);
                        java.time.LocalDate rowDate = java.time.LocalDate.parse(dateStr);
                        int targetYear = Integer.parseInt(selectedYear);

                        return rowDate.getYear() == targetYear;
                    } catch (Exception e) { return false; }
                }
            });
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        if (lblRecordCount != null)
            lblRecordCount.setText("Total Households: " + sorter.getViewRowCount());
    }
    // =========================================================================
    //  COPIED CALENDAR/COMPONENT STYLING CLASSES
    // =========================================================================

    private static class RoundBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(2, 8, 2, 8);
        }
    }

    // =========================================================================
    //  COPIED CALENDAR IMPLEMENTATION (EXACT COPY FROM SecretaryPrintDocument)
    // =========================================================================
    private void showModernDatePicker() {
        // Create a modern popup window for calendar
        JDialog dateDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dateDialog.setLayout(new BorderLayout());
        dateDialog.setResizable(false);
        dateDialog.getContentPane().setBackground(LIGHT_GREY);

        // Main container panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(LIGHT_GREY);

        // Create modern calendar panel with shadow effect
        JPanel calendarPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 15, 15);

                // Draw main panel
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, 15, 15);
            }
        };
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        calendarPanel.setOpaque(false);
        calendarPanel.setPreferredSize(new Dimension(350, 400));

        // Create month navigation with compact styling
        JPanel monthPanel = new JPanel(new BorderLayout());
        monthPanel.setOpaque(false);
        monthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Navigation buttons with modern icons
        JButton prevMonthBtn = createCompactNavButton("◀");
        JButton nextMonthBtn = createCompactNavButton("▶");

        // Year and Month Selection Panel
        JPanel yearMonthPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        yearMonthPanel.setOpaque(false);

        // Month ComboBox
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        JComboBox<String> monthComboBox = new JComboBox<>(months);
        monthComboBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        monthComboBox.setBackground(Color.WHITE);
        monthComboBox.setBorder(new RoundBorder(4, new Color(206, 212, 218)));
        monthComboBox.setFocusable(false);
        monthComboBox.setPreferredSize(new Dimension(70, 25));

        // Year ComboBox
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        yearComboBox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        yearComboBox.setBackground(Color.WHITE);
        yearComboBox.setBorder(new RoundBorder(4, new Color(206, 212, 218)));
        yearComboBox.setFocusable(false);
        yearComboBox.setPreferredSize(new Dimension(65, 25));

        // Populate years (from 2000 to current year + 10)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = 2000; year <= currentYear + 10; year++) {
            yearComboBox.addItem(year);
        }

        // Initialize calendar
        final Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
        }

        // Set initial values for comboboxes
        monthComboBox.setSelectedIndex(calendar.get(Calendar.MONTH));
        yearComboBox.setSelectedItem(calendar.get(Calendar.YEAR));

        // Create days panel with compact grid
        JPanel daysPanel = new JPanel(new GridLayout(6, 7, 2, 2));
        daysPanel.setOpaque(false);
        daysPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        daysPanel.setPreferredSize(new Dimension(300, 180));

        // Function to rebuild calendar days
        java.util.function.Consumer<Void> rebuildCalendarDays = v -> {
            daysPanel.removeAll();

            // Get current date for comparison
            final Calendar today = Calendar.getInstance();
            Calendar tempCal = (Calendar) calendar.clone();
            tempCal.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
            int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            // Add empty cells for days before first day of month
            for (int i = 1; i < firstDayOfWeek; i++) {
                daysPanel.add(createEmptyDayLabel());
            }

            // Add day buttons
            for (int dayNum = 1; dayNum <= daysInMonth; dayNum++) {
                final int day = dayNum;
                JButton dayBtn = createDayButton(day, calendar, today, dateDialog);
                daysPanel.add(dayBtn);
            }

            // Add empty cells for remaining spots
            int totalCells = daysInMonth + (firstDayOfWeek - 1);
            int remainingCells = 42 - totalCells;
            for (int i = 0; i < remainingCells; i++) {
                daysPanel.add(createEmptyDayLabel());
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        };

        // Build initial calendar
        rebuildCalendarDays.accept(null);

        // Add action listeners to comboboxes
        monthComboBox.addActionListener(e -> {
            int selectedMonth = monthComboBox.getSelectedIndex();
            calendar.set(Calendar.MONTH, selectedMonth);
            rebuildCalendarDays.accept(null);
        });

        yearComboBox.addActionListener(e -> {
            int selectedYear = (int) yearComboBox.getSelectedItem();
            calendar.set(Calendar.YEAR, selectedYear);
            rebuildCalendarDays.accept(null);
        });

        prevMonthBtn.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            monthComboBox.setSelectedIndex(calendar.get(Calendar.MONTH));
            yearComboBox.setSelectedItem(calendar.get(Calendar.YEAR));
            rebuildCalendarDays.accept(null);
        });

        nextMonthBtn.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            monthComboBox.setSelectedIndex(calendar.get(Calendar.MONTH));
            yearComboBox.setSelectedItem(calendar.get(Calendar.YEAR));
            rebuildCalendarDays.accept(null);
        });

        yearMonthPanel.add(monthComboBox);
        yearMonthPanel.add(yearComboBox);

        // Today button
        JButton todayBtn = new JButton("Today") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(233, 236, 239));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                super.paintComponent(g2d);
            }
        };
        todayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        todayBtn.setForeground(MODERN_BLUE);
        todayBtn.setFocusPainted(false);
        todayBtn.setBorder(new RoundBorder(4, new Color(206, 212, 218)));
        todayBtn.setContentAreaFilled(false);
        todayBtn.setOpaque(false);
        todayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        todayBtn.setPreferredSize(new Dimension(60, 25));
        todayBtn.addActionListener(e -> {
            calendar.setTime(new Date());
            monthComboBox.setSelectedIndex(calendar.get(Calendar.MONTH));
            yearComboBox.setSelectedItem(calendar.get(Calendar.YEAR));
            rebuildCalendarDays.accept(null);
        });

        // Navigation panel
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setOpaque(false);

        JPanel leftNavPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        leftNavPanel.setOpaque(false);
        leftNavPanel.add(prevMonthBtn);
        leftNavPanel.add(nextMonthBtn);

        JPanel rightNavPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        rightNavPanel.setOpaque(false);
        rightNavPanel.add(todayBtn);

        navPanel.add(leftNavPanel, BorderLayout.WEST);
        navPanel.add(yearMonthPanel, BorderLayout.CENTER);
        navPanel.add(rightNavPanel, BorderLayout.EAST);

        monthPanel.add(navPanel, BorderLayout.CENTER);

        // Create compact day headers panel
        JPanel dayHeaderPanel = new JPanel(new GridLayout(1, 7, 2, 2));
        dayHeaderPanel.setOpaque(false);
        dayHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        dayHeaderPanel.setPreferredSize(new Dimension(300, 25));

        String[] dayNames = {"S", "M", "T", "W", "T", "F", "S"};
        for (String day : dayNames) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            dayLabel.setForeground(new Color(108, 117, 125));
            dayHeaderPanel.add(dayLabel);
        }

        // Add footer with action buttons
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = createCompactDialogButton("Cancel", new Color(108, 117, 125));
        cancelBtn.addActionListener(e -> dateDialog.dispose());

        JButton selectBtn = createCompactDialogButton("Select", MODERN_BLUE);
        selectBtn.addActionListener(e -> {
            if (selectedDate == null) {
                // If no date selected, use current date
                selectedDate = calendar.getTime();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateFilterBtn.setText("📅 " + sdf.format(selectedDate));
            dateFilterBtn.setForeground(Color.WHITE);
            dateFilterBtn.repaint();
            dateDialog.dispose();
            applyFilters();
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(selectBtn);
        footerPanel.add(buttonPanel, BorderLayout.EAST);

        calendarPanel.add(monthPanel, BorderLayout.NORTH);
        calendarPanel.add(dayHeaderPanel, BorderLayout.CENTER);
        calendarPanel.add(daysPanel, BorderLayout.SOUTH);

        mainPanel.add(calendarPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        dateDialog.add(mainPanel, BorderLayout.CENTER);
        dateDialog.pack();

        // Center the dialog on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension dialogSize = dateDialog.getSize();
        int x = (screenSize.width - dialogSize.width) / 2;
        int y = (screenSize.height - dialogSize.height) / 2;
        dateDialog.setLocation(x, y);

        dateDialog.setVisible(true);
    }

    private JLabel createEmptyDayLabel() {
        JLabel label = new JLabel("");
        label.setOpaque(false);
        return label;
    }

    private JButton createDayButton(int day, Calendar calendar, Calendar today, JDialog dateDialog) {
        final boolean isSelectedForThisButton;
        if (selectedDate != null) {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
            isSelectedForThisButton = calendar.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH) &&
                    day == selectedCal.get(Calendar.DAY_OF_MONTH);
        } else {
            isSelectedForThisButton = false;
        }

        final boolean isTodayForThisButton = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                day == today.get(Calendar.DAY_OF_MONTH);

        JButton dayBtn = new JButton(String.valueOf(day)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isTodayForThisButton) {
                    g2d.setColor(new Color(66, 133, 244, 30));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    g2d.setColor(MODERN_BLUE);
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                }

                if (isSelectedForThisButton) {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, MODERN_BLUE,
                            getWidth(), getHeight(), new Color(26, 115, 232)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                }

                super.paintComponent(g2d);
            }
        };

        dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dayBtn.setFocusPainted(false);
        dayBtn.setBorderPainted(false);
        dayBtn.setContentAreaFilled(false);
        dayBtn.setOpaque(false);
        dayBtn.setBorder(new EmptyBorder(1, 1, 1, 1));
        dayBtn.setPreferredSize(new Dimension(40, 30));

        // Set foreground color
        if (isSelectedForThisButton) {
            dayBtn.setForeground(Color.WHITE);
        } else if (isTodayForThisButton) {
            dayBtn.setForeground(MODERN_BLUE);
        } else {
            dayBtn.setForeground(DARK_GREY);
        }

        // Add hover effect
        final JButton finalDayBtn = dayBtn;
        dayBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isSelectedForThisButton) {
                    finalDayBtn.setBackground(new Color(66, 133, 244, 15));
                    finalDayBtn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                finalDayBtn.setBackground(null);
                finalDayBtn.repaint();
            }
        });

        dayBtn.addActionListener(e -> {
            calendar.set(Calendar.DAY_OF_MONTH, day);
            selectedDate = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateFilterBtn.setText("📅 " + sdf.format(selectedDate));
            dateFilterBtn.setForeground(Color.WHITE);
            dateFilterBtn.repaint();
            dateDialog.dispose();
            applyFilters();
        });

        return dayBtn;
    }

    private JButton createCompactNavButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(66, 133, 244, 150));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(66, 133, 244, 30));
                } else {
                    g2d.setColor(new Color(233, 236, 239));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2d);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(MODERN_BLUE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(30, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createCompactDialogButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, color,
                            getWidth(), getHeight(), color.darker()
                    );
                    g2d.setPaint(gradient);
                } else {
                    g2d.setColor(color);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                super.paintComponent(g2d);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(70, 28));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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

