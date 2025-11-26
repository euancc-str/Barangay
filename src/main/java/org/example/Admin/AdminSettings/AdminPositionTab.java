package org.example.Admin.AdminSettings;

import org.example.Admin.AdminSystemSettings;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class AdminPositionTab extends JPanel {

    private JTable posTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private AdminSystemSettings dao;

    // Style Variables
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94);
    private final Color BTN_ADD_COLOR = new Color(76, 175, 80);
    private final Color BTN_EDIT_COLOR = new Color(100, 149, 237);
    private final Color BTN_DELETE_COLOR = new Color(255, 77, 77);

    public AdminPositionTab() {
        this.dao = new AdminSystemSettings();

        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        refreshTableData();
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        List<Object[]> data = dao.getAllPositions();
        for (Object[] row : data) {
            // Handle null uniqueIds for display
            if (row[2] == null) row[2] = "N/A";
            tableModel.addRow(row);
        }
    }

    // --- ACTION: ADD ---
    private void handleAdd() {
        showPositionDialog(-1, "", ""); // -1 indicates NEW
    }

    // --- ACTION: EDIT ---
    private void handleEdit() {
        int selectedRow = posTable.getSelectedRow();
        if (selectedRow == -1) return;

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String uid = (String) tableModel.getValueAt(selectedRow, 2);
        if (uid.equals("N/A")) uid = "";

        showPositionDialog(id, name, uid);
    }

    // --- DIALOG: SHARED FOR ADD & EDIT ---
    private void showPositionDialog(int id, String currentName, String currentUid) {
        String title = (id == -1) ? "Add New Position" : "Edit Position";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JTextField txtName = createStyledTextField(currentName);
        JTextField txtUid = createStyledTextField(currentUid);

        addStyledRow(panel, "Position Name:", txtName);
        addStyledRow(panel, "Unique ID (Optional):", txtUid);

        // Hint Label
        JLabel lblHint = new JLabel("<html><small><i>Note: Unique ID is used for auto-selecting<br>roles during registration.</i></small></html>");
        lblHint.setForeground(Color.GRAY);
        panel.add(lblHint);

        JButton btnSave = createRoundedButton("Save", BTN_ADD_COLOR);
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String uid = txtUid.getText().trim();

            if(name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Position Name is required.");
                return;
            }

            boolean success;
            if (id == -1) {
                success = dao.addPosition(name, uid);
            } else {
                success = dao.updatePosition(id, name, uid);
            }

            if(success) {
                JOptionPane.showMessageDialog(dialog, "Saved Successfully!");
                refreshTableData();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error saving. Unique ID might be duplicate.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnSave);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // --- ACTION: DELETE ---
    private void handleDelete() {
        int selectedRow = posTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a position to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete position '" + name + "'?\nThis might affect user registration!",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if(confirm == JOptionPane.YES_OPTION) {
            if(dao.deletePosition(id)) {
                refreshTableData();
                JOptionPane.showMessageDialog(this, "Deleted Successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Could not delete. It might be in use.");
            }
        }
    }

    // --- GUI COMPONENTS ---
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // 1. Buttons & Search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setBackground(BG_COLOR);
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton btnAdd = createRoundedButton("Add Position", BTN_ADD_COLOR);
        btnAdd.addActionListener(e -> handleAdd());

        JButton btnEdit = createRoundedButton("Edit", BTN_EDIT_COLOR);
        btnEdit.addActionListener(e -> handleEdit());

        JButton btnDelete = createRoundedButton("Delete", BTN_DELETE_COLOR);
        btnDelete.addActionListener(e -> handleDelete());

        JTextField txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 5, 5, 5)));

        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        topPanel.add(btnAdd);
        topPanel.add(btnEdit);
        topPanel.add(btnDelete);
        topPanel.add(new JLabel("Search:"));
        topPanel.add(txtSearch);

        contentPanel.add(topPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        String[] columns = {"ID", "Position Name", "Unique ID "};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        posTable = new JTable(tableModel);
        posTable.setFont(new Font("Arial", Font.PLAIN, 14));
        posTable.setRowHeight(40);
        posTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Double click to edit
        posTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleEdit();
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        posTable.setRowSorter(sorter);

        JTableHeader header = posTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        JScrollPane tableScrollPane = new JScrollPane(posTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(TABLE_HEADER_BG, 2));

        contentPanel.add(tableScrollPane);

        return contentPanel;
    }

    // --- HELPERS ---
    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)));
        return field;
    }

    private void addStyledRow(JPanel panel, String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setPreferredSize(new Dimension(150, 35));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(5, 0, 15, 0));
        wrapper.add(field, BorderLayout.CENTER);

        row.add(label, BorderLayout.WEST);
        row.add(wrapper, BorderLayout.CENTER);
        panel.add(row);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(25, 40, 25, 40));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);

        JLabel lblTitle = new JLabel("System Configuration");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Manage Roles & Unique IDs");
        lblSub.setFont(new Font("Arial", Font.BOLD, 18));
        lblSub.setForeground(Color.LIGHT_GRAY);

        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    private JButton createRoundedButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(getForeground());
                g2.drawString(getText(), (getWidth() - g2.getFontMetrics().stringWidth(getText()))/2, (getHeight()/2)+5);
                g2.dispose();
            }
        };
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Position Settings");
            f.setSize(1000, 700);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new AdminPositionTab());
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}