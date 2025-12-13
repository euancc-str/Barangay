package org.example.Admin;

import org.example.HouseholdDAO;
import org.example.Interface.SecretaryPerformSearch;
import org.example.ResidentDAO;
import org.example.Users.Household;
import org.example.Users.Resident;
import org.example.Admin.SystemLogDAO;

import java.awt.*;
import java.awt.event.*;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

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

    public AdminHouseholdTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        loadData();
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
                    if (tableModel != null) tableModel.setRowCount(0);

                    for (Household h : list) {
                        tableModel.addRow(new Object[]{
                                h.getHouseholdId(),
                                h.getHouseholdNo(),
                                h.getPurok(),
                                h.getStreet(),
                                h.getAddress(),
                                h.getHouseholdHeadId(), // Column 5
                                h.getTotalMembers(),
                                h.getNotes(),
                                h.getUpdatedAt()
                        });
                    }
                    if (lblRecordCount != null) lblRecordCount.setText("Total Households: " + tableModel.getRowCount());
                } catch (Exception e) { e.printStackTrace(); }
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

        // Reconstruct Object from Table Data
        Household h = new Household();
        h.setHouseholdId((int) tableModel.getValueAt(modelRow, 0));
        h.setHouseholdNo((String) tableModel.getValueAt(modelRow, 1));
        h.setPurok((String) tableModel.getValueAt(modelRow, 2));
        h.setStreet((String) tableModel.getValueAt(modelRow, 3));
        h.setAddress((String) tableModel.getValueAt(modelRow, 4));
        h.setHouseholdHeadId((int) tableModel.getValueAt(modelRow, 5));
        h.setTotalMembers((int) tableModel.getValueAt(modelRow, 6));
        h.setNotes((String) tableModel.getValueAt(modelRow, 7));

        showDialog(h, "Update Household Details");
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String num = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete Household " + num + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new HouseholdDAO().deleteHousehold(id);
            loadData();
            JOptionPane.showMessageDialog(this, "Household deleted.");
        }
    }


    // =========================================================================
    // DIALOG (Shared for Add & Update)
    // =========================================================================
    private void showDialog(Household existing, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(550, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(Color.WHITE);



        // Fields
        JTextField txtHouseNo = createStyledTextField(existing != null ? existing.getHouseholdNo() : "");
        JTextField txtPurok = createStyledTextField(existing != null ? existing.getPurok() : "");
        JTextField txtStreet = createStyledTextField(existing != null ? existing.getStreet() : "");
        JTextField txtAddress = createStyledTextField(existing != null ? existing.getAddress() : "");
        JTextField txtMembers = createStyledTextField(existing != null ? String.valueOf(existing.getTotalMembers()) : "1");

        JTextArea txtNotes = new JTextArea(existing != null ? existing.getNotes() : "", 3, 20);
        txtNotes.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // --- HEAD OF FAMILY SELECTOR ---
        JTextField txtHeadId = createStyledTextField(existing != null ? String.valueOf(existing.getHouseholdHeadId()) : "");
        txtHeadId.setEditable(false); // Read-only, set by button
        txtHeadId.setBackground(new Color(250, 250, 250));

        JTextField txtHeadName = createStyledTextField(""); // Display Name
        txtHeadName.setEditable(false);

        // If editing, try to fetch current head name for display
        if (existing != null && existing.getHouseholdHeadId() > 0) {
            Resident r = new ResidentDAO().findResidentById(existing.getHouseholdHeadId());
            if (r != null) txtHeadName.setText(r.getFirstName() + " " + r.getLastName());
        }

        JButton btnSelectHead = new JButton("Select Resident");
        btnSelectHead.setBackground(new Color(240, 240, 240));
        btnSelectHead.setFocusPainted(false);
        btnSelectHead.addActionListener(e -> openResidentSelector(dialog, txtHeadId, txtHeadName,txtHouseNo,txtPurok,txtStreet,txtAddress,txtMembers));

        // Layout Rows
        addStyledRow(formPanel, "Household No:", txtHouseNo);
        addStyledRow(formPanel, "Purok:", txtPurok);
        addStyledRow(formPanel, "Street:", txtStreet);
        addStyledRow(formPanel, "Full Address:", txtAddress);

        // Head Selection Row
        JPanel headPanel = new JPanel(new BorderLayout(5, 0));
        headPanel.setBackground(Color.WHITE);
        headPanel.add(txtHeadName, BorderLayout.CENTER);
        headPanel.add(btnSelectHead, BorderLayout.EAST);

        addStyledPanelRow(formPanel, "Head of Family:", headPanel);
        addStyledRow(formPanel, "Head ID (Auto):", txtHeadId);

        addStyledRow(formPanel, "Total Members:", txtMembers);

        formPanel.add(new JLabel("Notes:"));
        formPanel.add(new JScrollPane(txtNotes));

        // Save Button
        JButton btnSave = createRoundedButton("Save Household", BTN_ADD_COLOR);
        btnSave.addActionListener(e -> {
            if (txtHouseNo.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Household No. is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Household h = new Household();

                h.setHouseholdNo(txtHouseNo.getText());
                h.setPurok(txtPurok.getText());
                h.setStreet(txtStreet.getText());
                h.setAddress(txtAddress.getText());
                h.setNotes(txtNotes.getText());
                if(txtMembers.getText().equals("0")){
                    JOptionPane.showMessageDialog(this,"Error! total member is 0");
                    return;
                }
                new ResidentDAO().updateResidentHouseHold(Integer.parseInt(txtHeadId.getText()),txtHouseNo.getText());


                // Parse Integers
                try { h.setHouseholdHeadId(Integer.parseInt(txtHeadId.getText())); } catch(Exception ex) { h.setHouseholdHeadId(0); }
                try { h.setTotalMembers(Integer.parseInt(txtMembers.getText())); } catch(Exception ex) { h.setTotalMembers(1); }

                HouseholdDAO dao = new HouseholdDAO();
                int totalMembers= dao.countMembers(h.getHouseholdNo());
                if(!title.equals("Update Household Details")){
                    if (dao.doesHouseholdExists(txtHouseNo.getText())) {
                        JOptionPane.showMessageDialog(dialog,
                                "Household Number '" + txtHouseNo.getText() + "' already exists!",
                                "Duplicate Entry",
                                JOptionPane.WARNING_MESSAGE);
                        return; // Stop here
                    }
                }

                if (existing == null) {
                    dao.addHousehold(h);
                } else {
                    h.setHouseholdId(existing.getHouseholdId());
                    dao.updateHousehold(h);
                }

                JOptionPane.showMessageDialog(dialog, "Saved Successfully!");
                loadData();
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: Check your inputs.");
                ex.printStackTrace();
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
    // RESIDENT SELECTOR (For Head of Family)
    // =========================================================================
    private void openResidentSelector(JDialog parent, JTextField idField, JTextField nameField,JTextField householdNo,JTextField purok, JTextField street,JTextField address,JTextField memberCountField) {
        JDialog resDialog = new JDialog(parent, "Select Head of Family", true);
        resDialog.setSize(500, 400);
        resDialog.setLocationRelativeTo(parent);

        ResidentDAO rDao = new ResidentDAO();
        java.util.List<Resident> residents = rDao.getAllResidents();

        String[] cols = {"ID", "Name", "Address","HouseholdNo","Purok","Street"};
        DefaultTableModel resModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        for(Resident r : residents) {
            resModel.addRow(new Object[]{r.getResidentId(), r.getFirstName() + " " + r.getLastName(), r.getAddress(),r.getHouseholdNo(),r.getPurok(),r.getStreet()});
        }

        JTable resTable = new JTable(resModel);
        resTable.setRowHeight(30);

        JTextField search = new JTextField();
        TableRowSorter<DefaultTableModel> resSorter = new TableRowSorter<>(resModel);
        resSorter.setComparator(0, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        resTable.setRowSorter(resSorter);
        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                resSorter.setRowFilter(RowFilter.regexFilter("(?i)" + search.getText()));
            }
        });

        JButton btnSelect = new JButton("Select This Resident");
        btnSelect.setBackground(BTN_ADD_COLOR);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.addActionListener(e -> {
            int row = resTable.getSelectedRow();
            if(row != -1) {
                int modelRow = resTable.convertRowIndexToModel(row);
                // Get ID and Name
                int resId = (int) resModel.getValueAt(modelRow, 0);
                String name = (String) resModel.getValueAt(modelRow, 1);
                String add = (String) resModel.getValueAt(modelRow,2);
                String household = (String) resModel.getValueAt(modelRow,3);
                String purokData = (String)resModel.getValueAt(modelRow,4);
                String streetData = (String)resModel.getValueAt(modelRow,5);
                // Set to Fields
                idField.setText(String.valueOf(resId));
                nameField.setText(name);
                if (household!= null){
                    householdNo.setText(household);
                }else{
                    householdNo.setText("HH");
                }

                address.setText(add);
                purok.setText(purokData);
                street.setText(streetData);
                if (household != null && !household.isEmpty()) {
                    int count = new HouseholdDAO().countMembers(household);
                    // If count is 0 (unlikely since we just selected a resident), default to 1
                    memberCountField.setText(String.valueOf(count > 0 ? count : 1));
                } else {
                    // If no household number, they are the first member
                    memberCountField.setText("1");
                }

                resDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(resDialog, "Please select a resident.");
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10,10,10,10));
        p.add(new JLabel("Search Resident:"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout());
        center.add(search, BorderLayout.NORTH);
        center.add(new JScrollPane(resTable), BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        p.add(btnSelect, BorderLayout.SOUTH);

        resDialog.add(p);
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

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btns.setBackground(BG_COLOR);

        JButton btnAdd = createRoundedButton("+ New Household", BTN_ADD_COLOR);
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
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        p.add(searchPanel);
        p.add(Box.createVerticalStrut(10));

        String[] cols = {"ID", "Household No", "Purok", "Street", "Address", "Head ID", "Members", "Notes", "Updated"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int c) {
                if(c == 0 || c == 5 || c == 6) return Integer.class;
                return String.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(40);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(0, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        table.setRowSorter(sorter);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        GradientHeaderRenderer headerRenderer = new GradientHeaderRenderer();
        for(int i=0;i<table.getColumnCount();i++){
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        for(int i=0;i<table.getColumnCount();i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if(e.getClickCount()==2) handleUpdate(); }
        });

        p.add(new JScrollPane(table));

        lblRecordCount = new JLabel("Total: 0");
        p.add(lblRecordCount);

        return p;
    }

    private final Color CERULEAN = new Color(0, 123, 167);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // Create gradient from cerulean to light blue
        GradientPaint gradient = new GradientPaint(
                0, 0, CERULEAN,
                getWidth(), getHeight(), LIGHT_BLUE
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private final Color DARK_CERULEAN = new Color(0, 90, 140);

    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout()){    @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            // Create gradient for header
            GradientPaint headerGradient = new GradientPaint(
                    0, 0, DARK_CERULEAN,
                    getWidth(), 0, CERULEAN
            );
            g2d.setPaint(headerGradient);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        }
        };
        h.setOpaque(false);
        h.setBorder(BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(DARK_CERULEAN);
                        g2.fillRoundRect(x, y, width, height, 30, 30);
                    }
                }, new EmptyBorder(25, 40, 25, 40)));

        JLabel l = new JLabel("Household Management");
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
    class GradientHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


            setOpaque(false);
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Arial", Font.BOLD, 15));
            setForeground(Color.BLACK); // Black font color
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE),
                    BorderFactory.createEmptyBorder(0, 5, 0, 5)
            ));


            return this;
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            // Create gradient for header cells
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(100, 180, 255), // Light blue
                    0, getHeight(), new Color(70, 130, 180)  // Steel blue
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());


            super.paintComponent(g);
        }
    }


    // =========================================================================
    // GUI SETUP
    // =========================================================================



    private void addStyledPanelRow(JPanel p, String label, JPanel fieldPanel) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(150, 30));
        l.setFont(new Font("Arial", Font.BOLD, 14));
        row.add(l, BorderLayout.WEST);
        row.add(fieldPanel, BorderLayout.CENTER);
        p.add(row);
        p.add(Box.createVerticalStrut(10));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setSize(1000, 700);
            f.add(new AdminHouseholdTab());
            f.setVisible(true);
        });
    }
}