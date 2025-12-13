package org.example.Admin.AdminSettings;

import org.example.BarangayAssetDAO; // Ensure this import matches your package
import org.example.Users.BarangayAsset;
import org.example.Admin.SystemLogDAO;

import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class AdminAssetTab extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblRecordCount;
    private JTextField searchField;

    // --- VISUAL STYLE VARIABLES (Same as others) ---
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94);
    private final Color BTN_ADD_COLOR = new Color(76, 175, 80);
    private final Color BTN_UPDATE_COLOR = new Color(100, 149, 237);
    private final Color BTN_DELETE_COLOR = new Color(255, 77, 77);

    public AdminAssetTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        loadData();
    }

    // =========================================================================
    // DATA LOADING (SwingWorker)
    // =========================================================================
    public void loadData() {
        new SwingWorker<List<BarangayAsset>, Void>() {
            @Override
            protected List<BarangayAsset> doInBackground() throws Exception {
                return new BarangayAssetDAO().getAllAssets();
            }

            @Override
            protected void done() {
                try {
                    List<BarangayAsset> list = get();
                    if (tableModel != null) tableModel.setRowCount(0);

                    for (BarangayAsset a : list) {
                        tableModel.addRow(new Object[]{
                                a.getAssetId(),
                                a.getItemName(),
                                a.getPropertyNumber(),
                                a.getDateAcquired(),
                                a.getStatus(),
                                a.getValue()
                        });
                    }
                    if (lblRecordCount != null) lblRecordCount.setText("Total Assets: " + tableModel.getRowCount());
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // =========================================================================
    // CRUD ACTIONS
    // =========================================================================

    private void handleAdd() {
        showDialog(null, "Register New Asset");
    }

    private void handleUpdate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an asset.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        // Reconstruct object from table data
        BarangayAsset asset = new BarangayAsset(
                (int) tableModel.getValueAt(modelRow, 0),
                (String) tableModel.getValueAt(modelRow, 1),
                (String) tableModel.getValueAt(modelRow, 2),
                (Date) tableModel.getValueAt(modelRow, 3),
                (String) tableModel.getValueAt(modelRow, 4),
                (double) tableModel.getValueAt(modelRow, 5)
        );

        showDialog(asset, "Update Asset Details");
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete asset: " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new BarangayAssetDAO().deleteAsset(id);
            loadData();
            JOptionPane.showMessageDialog(this, "Asset deleted.");
        }
    }

    // =========================================================================
    // DIALOG
    // =========================================================================
    private void showDialog(BarangayAsset existing, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Fields
        JTextField txtName = createStyledTextField(existing != null ? existing.getItemName() : "");
        JTextField txtPropNo = createStyledTextField(existing != null ? existing.getPropertyNumber() : "PROP-" + System.currentTimeMillis());
        JTextField txtDate = createStyledTextField(existing != null ? existing.getDateAcquired().toString() : LocalDate.now().toString());
        JTextField txtValue = createStyledTextField(existing != null ? String.valueOf(existing.getValue()) : "0.00");

        String[] statuses = {"Good", "Damaged", "Lost", "For Repair", "Disposed"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setBackground(Color.WHITE);
        if(existing != null) cbStatus.setSelectedItem(existing.getStatus());

        addStyledRow(formPanel, "Item Name:", txtName);
        addStyledRow(formPanel, "Property No:", txtPropNo);
        addStyledRow(formPanel, "Date Acquired (YYYY-MM-DD):", txtDate);
        addStyledRow(formPanel, "Status:", cbStatus);
        addStyledRow(formPanel, "Value / Cost:", txtValue);

        JButton btnSave = createRoundedButton("Save Asset", BTN_ADD_COLOR);
        btnSave.addActionListener(e -> {
            try {
                BarangayAsset asset = new BarangayAsset();
                asset.setItemName(txtName.getText());
                asset.setPropertyNumber(txtPropNo.getText());
                asset.setDateAcquired(Date.valueOf(txtDate.getText()));
                asset.setStatus(cbStatus.getSelectedItem().toString());
                asset.setValue(Double.parseDouble(txtValue.getText()));

                if (existing == null) {
                    new BarangayAssetDAO().addAsset(asset);
                    try { new SystemLogDAO().addLog("Added Asset " + asset.getItemName(), "Admin", 1); } catch(Exception ex){}
                } else {
                    asset.setAssetId(existing.getAssetId());
                    new BarangayAssetDAO().updateAsset(asset);
                }

                JOptionPane.showMessageDialog(dialog, "Saved Successfully!");
                loadData();
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: Check date format or number format.");
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
    // UI HELPERS
    // =========================================================================

    private JPanel createContentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btns.setBackground(BG_COLOR);

        JButton btnAdd = createRoundedButton("+ New Asset", BTN_ADD_COLOR);
        btnAdd.addActionListener(e -> handleAdd());

        JButton btnEdit = createRoundedButton("Edit Details", BTN_UPDATE_COLOR);
        btnEdit.addActionListener(e -> handleUpdate());

        JButton btnDelete = createRoundedButton("Delete", BTN_DELETE_COLOR);
        btnDelete.addActionListener(e -> handleDelete());

        btns.add(btnAdd); btns.add(btnEdit); btns.add(btnDelete);
        p.add(btns);
        p.add(Box.createVerticalStrut(20));

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BG_COLOR);
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (sorter != null) sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
            }
        });
        searchPanel.add(new JLabel("Search Asset: "));
        searchPanel.add(searchField);
        p.add(searchPanel);
        p.add(Box.createVerticalStrut(10));

        String[] cols = {"ID", "Item Name", "Property No", "Date Acquired", "Status", "Value"};
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

        lblRecordCount = new JLabel("Total: 0");
        p.add(lblRecordCount);

        return p;
    }

    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HEADER_BG);
        h.setBorder(new EmptyBorder(25, 40, 25, 40));
        JLabel l = new JLabel("Barangay Assets & Inventory");
        l.setFont(new Font("Arial", Font.BOLD, 26));
        l.setForeground(Color.WHITE);
        h.add(l, BorderLayout.WEST);
        return h;
    }

    private JTextField createStyledTextField(String text) {
        JTextField t = new JTextField(text);
        t.setFont(new Font("Arial", Font.PLAIN, 14));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setSize(1000, 700);
            f.add(new AdminAssetTab());
            f.setVisible(true);
        });
    }
}