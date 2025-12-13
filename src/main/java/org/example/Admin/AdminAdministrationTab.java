package org.example.Admin;

import org.example.AdministrationDAO;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.Users.Administration;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class AdminAdministrationTab extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color BTN_ADD_COLOR = new Color(39, 174, 96);
    private final Color BTN_UPDATE_COLOR = new Color(100, 149, 237);
    private final Color BTN_DELETE_COLOR = new Color(231, 76, 60);

    public AdminAdministrationTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);
        loadData();
    }

    // =========================================================================
    // DATA LOADING
    // =========================================================================
    public void loadData() {
        new SwingWorker<List<Administration>, Void>() {
            @Override
            protected List<Administration> doInBackground() throws Exception {
                return new AdministrationDAO().getAllAdministrations();
            }

            @Override
            protected void done() {
                try {
                    List<Administration> list = get();
                    if (tableModel != null) tableModel.setRowCount(0);

                    for (Administration a : list) {
                        tableModel.addRow(new Object[]{
                                a.getAdminId(),
                                a.getTermName(),
                                a.getCaptainName(),
                                a.getTermStart(),
                                a.getTermEnd(),
                                a.getStatus(),
                                a.getVision()
                        });
                    }
                    if (table != null) table.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // =========================================================================
    // CRUD LOGIC
    // =========================================================================
    private void handleAdd() {
        showDialog(null, "Register New Administration");
    }

    private void handleUpdate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a term to edit.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        Administration admin = new Administration(
                id,
                (String) tableModel.getValueAt(modelRow, 1),
                (Date) tableModel.getValueAt(modelRow, 3),
                (Date) tableModel.getValueAt(modelRow, 4),
                (String) tableModel.getValueAt(modelRow, 2),
                (String) tableModel.getValueAt(modelRow, 5),
                (String) tableModel.getValueAt(modelRow, 6)
        );

        showDialog(admin, "Update Administration Term");
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a term to delete.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String term = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete: " + term + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            new AdministrationDAO().deleteAdministration(id);
            loadData();
            JOptionPane.showMessageDialog(this, "Term deleted successfully.");
        }
    }

    // =========================================================================
    // DIALOG
    // =========================================================================
    private void showDialog(Administration existing, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Auto-fetch current captain logic
        BarangayStaff staffDAO = new StaffDAO().findStaffByPosition("Brgy.Captain");
        String currentCapName = "";
        if (staffDAO != null && staffDAO.getFirstName() != null) {
            currentCapName = staffDAO.getFirstName() +" "+ staffDAO.getMiddleName() +" " + staffDAO.getLastName();
        }

        JTextField txtTermName = createStyledTextField(existing != null ? existing.getTermName() : "");

        // Captain Name Field
        String initialCapName = (existing != null) ? existing.getCaptainName() : currentCapName;
        JTextField txtCaptain = createStyledTextField(initialCapName);

        // Select Button
        JButton btnSelectHead = new JButton("Select Staff");
        btnSelectHead.setBackground(new Color(240, 240, 240));
        btnSelectHead.setFocusPainted(false);
        // FIX: Pass the text field to the selector so it can be updated
        btnSelectHead.addActionListener(e -> openStaffSelector(dialog, txtCaptain));

        JTextField txtStart = createStyledTextField(existing != null ? existing.getTermStart().toString() : "2023-01-01");
        JTextField txtEnd = createStyledTextField(existing != null && existing.getTermEnd() != null ? existing.getTermEnd().toString() : "");

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Active", "Ended", "Upcoming"});
        cbStatus.setBackground(Color.WHITE);
        if (existing != null) cbStatus.setSelectedItem(existing.getStatus());

        JTextArea txtVision = new JTextArea(existing != null ? existing.getVision() : "", 3, 20);
        txtVision.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        txtVision.setLineWrap(true);

        // Layout
        addStyledRow(formPanel, "Term Name:", txtTermName);

        // Captain Row with Button
        JPanel capPanel = new JPanel(new BorderLayout(5, 0));
        capPanel.setBackground(Color.WHITE);
        capPanel.add(txtCaptain, BorderLayout.CENTER);
        capPanel.add(btnSelectHead, BorderLayout.EAST);

        JPanel capWrapper = new JPanel(new BorderLayout(10, 0));
        capWrapper.setBackground(Color.WHITE);
        JLabel lblCap = new JLabel("Barangay Captain:");
        lblCap.setPreferredSize(new Dimension(150, 30));
        lblCap.setFont(new Font("Arial", Font.BOLD, 14));
        capWrapper.add(lblCap, BorderLayout.WEST);
        capWrapper.add(capPanel, BorderLayout.CENTER);

        formPanel.add(capWrapper);
        formPanel.add(Box.createVerticalStrut(10));

        // Date Row
        JPanel datePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        datePanel.setBackground(Color.WHITE);
        datePanel.add(wrapField("Start (YYYY-MM-DD):", txtStart));
        datePanel.add(wrapField("End (YYYY-MM-DD):", txtEnd));
        formPanel.add(datePanel);
        formPanel.add(Box.createVerticalStrut(10));

        addStyledRow(formPanel, "Status:", cbStatus);

        formPanel.add(new JLabel("Vision / Mission:"));
        formPanel.add(new JScrollPane(txtVision));
        formPanel.add(Box.createVerticalStrut(10));

        // Save Button
        JButton btnSave = createRoundedButton("Save Record", BTN_ADD_COLOR);
        btnSave.addActionListener(e -> {
            try {
                String end = txtEnd.getText();
                String name = txtTermName.getText();
                if(name.isEmpty()){
                    JOptionPane.showMessageDialog(dialog,
                            "Error! Empty term name field ",
                            "Empty field",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if(end.isEmpty()){
                    JOptionPane.showMessageDialog(dialog,
                            "Error! Empty term end field ",
                            "Empty field",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Administration admin = new Administration();
                admin.setTermName(txtTermName.getText());
                admin.setCaptainName(txtCaptain.getText());
                admin.setTermStart(Date.valueOf(txtStart.getText()));
                if (!txtEnd.getText().isEmpty()) admin.setTermEnd(Date.valueOf(txtEnd.getText()));
                admin.setStatus(cbStatus.getSelectedItem().toString());
                admin.setVision(txtVision.getText());
                AdministrationDAO dao = new AdministrationDAO();
                if (!dao.staffMatchName(staffDAO.getPosition(),txtCaptain.getText())) {
                    JOptionPane.showMessageDialog(dialog,
                            "Error! Current captain doesnt match with New Captain",
                            "Duplicate Entry",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (existing == null) {
                    new AdministrationDAO().addAdministration(admin);
                } else {
                    admin.setAdminId(existing.getAdminId());
                    new AdministrationDAO().updateAdministration(admin);
                }

                JOptionPane.showMessageDialog(dialog, "Saved Successfully!");
                loadData();
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: Check date format (YYYY-MM-DD) or inputs.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnSave);

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // =========================================================================
    // UI HELPERS (Resident Selector)
    // =========================================================================
    // UPDATED: Now takes the target TextField to fill
    private void openStaffSelector(JDialog parent, JTextField nameField) {
        JDialog resDialog = new JDialog(parent, "Select Captain from staffList", true);
        resDialog.setSize(500, 400);
        resDialog.setLocationRelativeTo(parent);

        StaffDAO rDao = new StaffDAO();
        java.util.List<BarangayStaff> residents = rDao.getAllStaff();

        String[] cols = {"ID", "Name", "Address"};
        DefaultTableModel resModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for (BarangayStaff r : residents) {
            resModel.addRow(new Object[]{r.getStaffId(), r.getName(), r.getPosition()});
        }

        JTable resTable = new JTable(resModel);
        resTable.setRowHeight(30);

        JTextField search = new JTextField();
        TableRowSorter<DefaultTableModel> resSorter = new TableRowSorter<>(resModel);
        resTable.setRowSorter(resSorter);
        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                resSorter.setRowFilter(RowFilter.regexFilter("(?i)" + search.getText()));
            }
        });

        JButton btnSelect = new JButton("Select This Staff");
        btnSelect.setBackground(BTN_ADD_COLOR);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.addActionListener(e -> {
            int row = resTable.getSelectedRow();
            if (row != -1) {
                int modelRow = resTable.convertRowIndexToModel(row);
                // Get Name
                String name = (String) resModel.getValueAt(modelRow, 1);

                // UPDATE FIELD
                nameField.setText(name);

                resDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(resDialog, "Please select a Staff.");
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.add(new JLabel("Search Staff:"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(search, BorderLayout.NORTH);
        center.add(new JScrollPane(resTable), BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        p.add(btnSelect, BorderLayout.SOUTH);

        resDialog.add(p);
        resDialog.setVisible(true);
    }

    private JPanel createContentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btns.setBackground(BG_COLOR);

        JButton btnAdd = createRoundedButton("+ New Term", BTN_ADD_COLOR);
        btnAdd.addActionListener(e -> handleAdd());

        JButton btnEdit = createRoundedButton("Edit Details", BTN_UPDATE_COLOR);
        btnEdit.addActionListener(e -> handleUpdate());

        JButton btnDelete = createRoundedButton("Delete", BTN_DELETE_COLOR);
        btnDelete.addActionListener(e -> handleDelete());

        btns.add(btnAdd);
        btns.add(btnEdit);
        btns.add(btnDelete);
        p.add(btns);
        p.add(Box.createVerticalStrut(20));

        String[] cols = {"ID", "Term Name", "Captain", "Start Date", "End Date", "Status", "Vision"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        p.add(new JScrollPane(table));
        return p;
    }

    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HEADER_BG);
        h.setBorder(new EmptyBorder(25, 40, 25, 40));
        JLabel l = new JLabel("Administration Management");
        l.setFont(new Font("Arial", Font.BOLD, 26));
        l.setForeground(Color.WHITE);
        h.add(l, BorderLayout.WEST);
        return h;
    }

    private JTextField createStyledTextField(String text) {
        JTextField t = new JTextField(text);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), new EmptyBorder(5, 5, 5, 5)));
        return t;
    }

    private JButton createRoundedButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void addStyledRow(JPanel p, String label, JComponent c) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(150, 30));
        l.setFont(new Font("Arial", Font.BOLD, 14));
        row.add(l, BorderLayout.WEST);
        row.add(c, BorderLayout.CENTER);
        p.add(row);
        p.add(Box.createVerticalStrut(10));
    }

    private JPanel wrapField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.add(new JLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setSize(1000, 700);
            f.add(new AdminAdministrationTab());
            f.setVisible(true);
        });
    }
}