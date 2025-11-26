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

    // Visual Style
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94);
    private final Color BTN_ADD_COLOR = new Color(76, 175, 80);
    private final Color BTN_EDIT_COLOR = new Color(100, 149, 237);
    private final Color BTN_DELETE_COLOR = new Color(255, 77, 77);

    public AdminSettingsTab() {
        this.dao = new AdminSystemSettings();
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);

        // --- THE MERGE: JTabbedPane ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Tab 1: Documents
        tabbedPane.addTab("Document Fees", createDocumentsPanel());

        // Tab 2: Positions
        tabbedPane.addTab("Positions & Roles", createPositionsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =================================================================
    // PANEL 1: DOCUMENTS LOGIC
    // =================================================================
    private JPanel createDocumentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(BG_COLOR);

        JButton btnAdd = createRoundedButton("Add Document", BTN_ADD_COLOR);
        btnAdd.addActionListener(e -> showDocumentDialog());

        JButton btnEdit = createRoundedButton("Edit Fee", BTN_EDIT_COLOR);
        btnEdit.addActionListener(e -> editDocumentFee());

        JButton btnDel = createRoundedButton("Delete", BTN_DELETE_COLOR);
        btnDel.addActionListener(e -> deleteDocument());

        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDel);

        // Table
        String[] cols = {"ID", "Document Name", "Fee (₱)"};
        docModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        docTable = createStyledTable(docModel);

        refreshDocuments(); // Load Data

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(docTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshDocuments() {
        docModel.setRowCount(0);
        for (Object[] row : dao.getAllDocumentTypes()) docModel.addRow(row);
    }

    private void showDocumentDialog() {
        // Pop-up for adding new document
        JTextField txtName = createStyledTextField("");
        JTextField txtFee = createStyledTextField("");

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("Name:")); p.add(txtName);
        p.add(new JLabel("Fee:")); p.add(txtFee);

        int res = JOptionPane.showConfirmDialog(this, p, "New Document", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                dao.addDocumentType(txtName.getText(), Double.parseDouble(txtFee.getText()));
                refreshDocuments();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid Fee"); }
        }
    }

    private void editDocumentFee() {
        int row = docTable.getSelectedRow();
        if(row == -1) return;
        int id = (int) docModel.getValueAt(row, 0);
        String val = JOptionPane.showInputDialog(this, "New Fee:");
        if(val != null) {
            dao.updateDocumentFee(id, Double.parseDouble(val));
            refreshDocuments();
        }
    }

    private void deleteDocument() {
        int row = docTable.getSelectedRow();
        if(row != -1 && JOptionPane.showConfirmDialog(this, "Delete?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.deleteDocument((int) docModel.getValueAt(row, 0));
            refreshDocuments();
        }
    }

    // =================================================================
    // PANEL 2: POSITIONS LOGIC
    // =================================================================
    private JPanel createPositionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setBackground(BG_COLOR);

        JButton btnAdd = createRoundedButton("Add Position", BTN_ADD_COLOR);
        btnAdd.addActionListener(e -> showPositionDialog(-1, "", ""));

        JButton btnEdit = createRoundedButton("Edit", BTN_EDIT_COLOR);
        btnEdit.addActionListener(e -> editPosition());

        JButton btnDel = createRoundedButton("Delete", BTN_DELETE_COLOR);
        btnDel.addActionListener(e -> deletePosition());

        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDel);

        // Table
        String[] cols = {"ID", "Position Name", "Unique ID"};
        posModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        posTable = createStyledTable(posModel);

        refreshPositions(); // Load Data

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(posTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshPositions() {
        posModel.setRowCount(0);
        for (Object[] row : dao.getAllPositions()) {
            if(row[2] == null) row[2] = "N/A";
            posModel.addRow(row);
        }
    }

    private void editPosition() {
        int row = posTable.getSelectedRow();
        if(row == -1) return;
        int id = (int) posModel.getValueAt(row, 0);
        String name = (String) posModel.getValueAt(row, 1);
        String uid = (String) posModel.getValueAt(row, 2);
        showPositionDialog(id, name, uid.equals("N/A") ? "" : uid);
    }

    private void deletePosition() {
        int row = posTable.getSelectedRow();
        if(row != -1 && JOptionPane.showConfirmDialog(this, "Delete?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.deletePosition((int) posModel.getValueAt(row, 0));
            refreshPositions();
        }
    }

    private void showPositionDialog(int id, String currentName, String currentUid) {
        JTextField txtName = createStyledTextField(currentName);
        JTextField txtUid = createStyledTextField(currentUid);

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("Position Name:")); p.add(txtName);
        p.add(new JLabel("Unique ID (Optional):")); p.add(txtUid);

        int res = JOptionPane.showConfirmDialog(this, p, id == -1 ? "Add" : "Edit", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            if (id == -1) dao.addPosition(txtName.getText(), txtUid.getText());
            else dao.updatePosition(id, txtName.getText(), txtUid.getText());
            refreshPositions();
        }
    }

    // =================================================================
    // SHARED VISUAL HELPERS
    // =================================================================
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
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

    private JButton createRoundedButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private JTextField createStyledTextField(String text) {
        return new JTextField(text); // Simplified for brevity
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setSize(1000, 700);
            f.add(new AdminSettingsTab());
            f.setVisible(true);
        });
    }
}