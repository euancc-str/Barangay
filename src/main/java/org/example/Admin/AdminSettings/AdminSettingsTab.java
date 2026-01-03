package org.example.Admin.AdminSettings;


import org.example.Admin.AdminSystemSettings;


import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;


public class AdminSettingsTab extends JPanel {


    private AdminSystemSettings dao;


    // Tables
    private JTable docTable, posTable;
    private DefaultTableModel docModel, posModel;


    // Modern Color Scheme
    private final Color PRIMARY_COLOR = new Color(59, 130, 246); // Modern blue
    private final Color SECONDARY_COLOR = new Color(107, 114, 128); // Cool gray
    private final Color BACKGROUND_COLOR = new Color(249, 250, 251); // Light gray
    private final Color CARD_COLOR = Color.WHITE;
    private final Color SUCCESS_COLOR = new Color(16, 185, 129); // Modern green
    private final Color DANGER_COLOR = new Color(239, 68, 68); // Modern red
    private final Color HEADER_BG = new Color(30, 41, 59); // Dark blue-gray
    private final Color BORDER_COLOR = new Color(229, 231, 235); // Light border
    private final Color TABLE_HEADER_BG = new Color(59, 130, 246); // Blue header


    public AdminSettingsTab() {
        this.dao = new AdminSystemSettings();
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);


        add(createHeaderPanel(), BorderLayout.NORTH);


        // --- THE MERGE: JTabbedPane ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(PRIMARY_COLOR);


        // Tab 1: Documents
        tabbedPane.addTab("Document Fees", createDocumentsPanel());


        // Tab 2: Positions
        tabbedPane.addTab("Positions & Roles", createPositionsPanel());


        add(tabbedPane, BorderLayout.CENTER);
    }


    // =================================================================
    // PANEL 1: DOCUMENTS LOGIC
    // =================================================================
    public JPanel createDocumentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));


        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(BACKGROUND_COLOR);


        JButton btnAdd = createModernButton("Add Document", SUCCESS_COLOR);
        btnAdd.addActionListener(e -> showDocumentDialog());


        JButton btnEdit = createModernButton("Edit Fee", PRIMARY_COLOR);
        btnEdit.addActionListener(e -> editDocumentFee());


        JButton btnDel = createModernButton("Delete", DANGER_COLOR);
        btnDel.addActionListener(e -> deleteDocument());


        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDel);


        // Table
        String[] cols = { "ID", "Document Name", "Fee (₱)" };
        docModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        docTable = createStyledTable(docModel);


        refreshDocuments(); // Load Data


        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(docTable), BorderLayout.CENTER);
        return panel;
    }


    private void refreshDocuments() {
        docModel.setRowCount(0);
        for (Object[] row : dao.getAllDocumentTypes())
            docModel.addRow(row);
    }


    private void showDocumentDialog() {
        // Pop-up for adding new document
        JTextField txtName = createStyledTextField("");
        JTextField txtFee = createStyledTextField("");


        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.setBackground(CARD_COLOR);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(createStyledLabel("Name:"));
        p.add(txtName);
        p.add(createStyledLabel("Fee:"));
        p.add(txtFee);


        int res = JOptionPane.showConfirmDialog(this, p, "New Document", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                dao.addDocumentType(txtName.getText(), Double.parseDouble(txtFee.getText()));
                refreshDocuments();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid Fee");
            }
        }
    }


    private void editDocumentFee() {
        int row = docTable.getSelectedRow();
        if (row == -1)
            return;
        int id = (int) docModel.getValueAt(row, 0);
        String val = JOptionPane.showInputDialog(this, "New Fee:");
        if (val != null) {
            dao.updateDocumentFee(id, Double.parseDouble(val));
            refreshDocuments();
        }
    }


    private void deleteDocument() {
        int row = docTable.getSelectedRow();
        if (row != -1 && JOptionPane.showConfirmDialog(this, "Delete?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.deleteDocument((int) docModel.getValueAt(row, 0));
            refreshDocuments();
        }
    }


    // =================================================================
    // PANEL 2: POSITIONS LOGIC
    // =================================================================
    public JPanel createPositionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));


        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(BACKGROUND_COLOR);


        JButton btnAdd = createModernButton("Add Position", SUCCESS_COLOR);
        btnAdd.addActionListener(e -> showPositionDialog(-1, "", ""));


        JButton btnEdit = createModernButton("Edit", PRIMARY_COLOR);
        btnEdit.addActionListener(e -> editPosition());


        JButton btnDel = createModernButton("Delete", DANGER_COLOR);
        btnDel.addActionListener(e -> deletePosition());


        btnPanel.add(btnAdd);
        btnPanel.add(btnEdit);
        btnPanel.add(btnDel);


        // Table
        String[] cols = { "ID", "Position Name", "Unique ID" };
        posModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        posTable = createStyledTable(posModel);


        refreshPositions(); // Load Data


        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(posTable), BorderLayout.CENTER);
        return panel;
    }


    private void refreshPositions() {
        posModel.setRowCount(0);
        for (Object[] row : dao.getAllPositions()) {
            if (row[2] == null)
                row[2] = "N/A";
            posModel.addRow(row);
        }
    }


    private void editPosition() {
        int row = posTable.getSelectedRow();
        if (row == -1)
            return;
        int id = (int) posModel.getValueAt(row, 0);
        String name = (String) posModel.getValueAt(row, 1);
        String uid = (String) posModel.getValueAt(row, 2);
        showPositionDialog(id, name, uid.equals("N/A") ? "" : uid);
    }


    private void deletePosition() {
        int row = posTable.getSelectedRow();
        if (row != -1 && JOptionPane.showConfirmDialog(this, "Delete?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.deletePosition((int) posModel.getValueAt(row, 0));
            refreshPositions();
        }
    }


    // =======================================================
    // UPDATED: POSITION DIALOG WITH STRICT VALIDATION
    // =======================================================
    private void showPositionDialog(int id, String currentName, String currentUid) {
        JTextField txtName = createStyledTextField(currentName);
        JTextField txtUid = createStyledTextField(currentUid);


        JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
        p.setBackground(CARD_COLOR);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(createStyledLabel("Position Name:"));
        p.add(txtName);
        p.add(createStyledLabel("Unique ID (Optional):"));
        p.add(txtUid);


        // Loop keeps dialog open until valid input or cancel
        while (true) {
            int res = JOptionPane.showConfirmDialog(this, p, id == -1 ? "Add Position" : "Edit Position",
                    JOptionPane.OK_CANCEL_OPTION);


            if (res != JOptionPane.OK_OPTION) {
                return; // User clicked Cancel or Close
            }


            String inputName = txtName.getText().trim();


            // 1. Validation: Check if Blank
            if (inputName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Position Name cannot be empty.", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                continue; // Re-show the dialog
            }


            // 2. Validation: Check Regex (Letters Only, No Numbers, No Special Chars)
            // Regex: ^[a-zA-Z\sñÑ]+$ -> Starts and ends with letters, spaces, or ñ
            if (!inputName.matches("^[a-zA-Z\\sñÑ]+$")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Position Name.\nNumbers and special characters are not allowed.\n(Letters only)",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                continue; // Re-show the dialog
            }


            // If Valid: Save and Break Loop
            if (id == -1)
                dao.addPosition(inputName, txtUid.getText().trim());
            else
                dao.updatePosition(id, inputName, txtUid.getText().trim());


            refreshPositions();
            break;
        }
    }


    // =================================================================
    // SHARED VISUAL HELPERS
    // =================================================================
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setBackground(CARD_COLOR);
        table.setGridColor(BORDER_COLOR);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }


    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 25));
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(800, 100));
        JLabel title = new JLabel("System Configuration");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title);
        return header;
    }


    private JButton createModernButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));


        // Add hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }


            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });


        return btn;
    }


    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)));
        field.setBackground(CARD_COLOR);
        return field;
    }


    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(SECONDARY_COLOR);
        return label;
    }
}

