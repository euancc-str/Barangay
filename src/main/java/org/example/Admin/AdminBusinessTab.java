package org.example.Admin;




import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.BusinessDAO;
import org.example.ResidentDAO;
import org.example.Users.BusinessEstablishment;
import org.example.Users.Resident;




import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
// Add these imports at the top:
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.RowFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;




public class AdminBusinessTab extends JPanel {




    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTotal;
    private JTextField txtSearch;




    // --- COLORS (Solid colors for Performance) ---
    private final Color BG_COLOR = new Color(225, 245, 254); // Very Light Blue
    private final Color HEADER_BG = new Color(0, 123, 167); // Cerulean
    private final Color BTN_ADD = new Color(46, 139, 87);   // Green
    private final Color BTN_UPDATE = new Color(100, 149, 237); // Blue
    private final Color BTN_DELETE = new Color(220, 20, 60);   // Red




    public AdminBusinessTab() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));




        // 1. TOP HEADER (Search & Add)
        add(createTopPanel(), BorderLayout.NORTH);




        // 2. CENTER TABLE
        add(createTablePanel(), BorderLayout.CENTER);




        // 3. BOTTOM PANEL (Count & Refresh)
        add(createBottomPanel(), BorderLayout.SOUTH);


        setupSearch();
        refreshData();
    }
    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }




    // =========================================================================
    //  UI BUILDERS
    // =========================================================================
// Replace the createTopPanel() method with this improved version:
    private JPanel createTopPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(0, 0, 10, 0));


        // ===== LEFT SIDE: SEARCH AND FILTER CONTROLS =====
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        leftPanel.setOpaque(false);


        // Search panel with icon
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));


        // Search field with icon
        JPanel searchFieldPanel = new JPanel(new BorderLayout(5, 0));
        searchFieldPanel.setOpaque(false);


        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Arial", Font.PLAIN, 14));


        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search businesses...");


        searchFieldPanel.add(searchIcon, BorderLayout.WEST);
        searchFieldPanel.add(txtSearch, BorderLayout.CENTER);


        // Filter dropdown
        String[] columns = {"All Columns", "Business Name", "Owner", "Nature", "Status", "Permit #", "Address", "Energy Source"};
        filterColumnCombo = new JComboBox<>(columns);
        filterColumnCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        filterColumnCombo.setPreferredSize(new Dimension(150, 30));


        // Status filter
        String[] statuses = {"All Status", "Active", "Pending", "Expired", "Revoked"};
        JComboBox<String> statusFilter = new JComboBox<>(statuses);
        statusFilter.setFont(new Font("Arial", Font.PLAIN, 12));
        statusFilter.setPreferredSize(new Dimension(120, 30));
        statusFilter.addActionListener(e -> filterByStatus((String)statusFilter.getSelectedItem()));


        // Quick filter buttons in a separate panel
        JPanel quickFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quickFilterPanel.setOpaque(false);
        quickFilterPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));


        JButton btnActiveOnly = createQuickFilterButton("✓ Active", new Color(46, 139, 87));
        JButton btnPendingOnly = createQuickFilterButton("⏳ Pending", new Color(255, 165, 0));
        JButton btnExpiredOnly = createQuickFilterButton("✗ Expired", new Color(220, 20, 60));
        JButton btnAdvancedFilter = createQuickFilterButton("⚙ Advanced", new Color(70, 130, 180));


        JButton searchButton = new JButton("🔍");
        searchButton.setFont(new Font("Arial", Font.PLAIN, 14));
        searchButton.addActionListener(e -> performSearch()); // Add this line
        searchButton.setToolTipText("Search");


        JButton btnClearSearch = new JButton("Clear");
        btnClearSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        btnClearSearch.setBackground(new Color(220, 220, 220));
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            performSearch();
        });
        btnActiveOnly.addActionListener(e -> filterByStatus("Active"));
        btnPendingOnly.addActionListener(e -> filterByStatus("Pending"));
        btnExpiredOnly.addActionListener(e -> filterByStatus("Expired"));
        btnAdvancedFilter.addActionListener(e -> showAdvancedFilterDialog());


        quickFilterPanel.add(new JLabel("Quick:"));
        quickFilterPanel.add(btnActiveOnly);
        quickFilterPanel.add(btnPendingOnly);
        quickFilterPanel.add(btnExpiredOnly);
        quickFilterPanel.add(btnAdvancedFilter);
        quickFilterPanel.add(searchButton);
        quickFilterPanel.add(btnClearSearch);


        // Add components to search panel
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topRow.setOpaque(false);
        topRow.add(new JLabel("Search in:"));
        topRow.add(filterColumnCombo);
        topRow.add(new JLabel("Status:"));
        topRow.add(statusFilter);
        topRow.add(searchFieldPanel);


        JButton btnClearFilters = new JButton("Clear Filters");
        btnClearFilters.setFont(new Font("Arial", Font.PLAIN, 12));
        btnClearFilters.setBackground(new Color(200, 200, 200));
        btnClearFilters.setPreferredSize(new Dimension(100, 30));
        btnClearFilters.addActionListener(e -> clearFilters());


        topRow.add(btnClearFilters);


        searchPanel.add(topRow, BorderLayout.NORTH);
        searchPanel.add(quickFilterPanel, BorderLayout.SOUTH);


        leftPanel.add(searchPanel);


        // ===== RIGHT SIDE: ACTION BUTTONS =====
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(BorderFactory.createTitledBorder("Actions"));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        // Create styled buttons
        JButton btnAdd = createStyledButton("+ Add Business", BTN_ADD);
        JButton btnEdit = createStyledButton("✎ Edit", BTN_UPDATE);
        JButton btnDelete = createStyledButton("🗑 Delete", BTN_DELETE);
        JButton btnExport = createStyledButton("📤 Export", new Color(128, 0, 128));
        JButton btnRefresh = createStyledButton("↻ Refresh", new Color(70, 130, 180));


        // Button actions
        btnAdd.addActionListener(e -> showFormDialog(null));
        btnEdit.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnExport.addActionListener(e -> exportToCSV());
        btnRefresh.addActionListener(e -> refreshData());


        // Add buttons in two rows for better organization
        JPanel buttonRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonRow1.setOpaque(false);
        buttonRow1.add(btnAdd);
        buttonRow1.add(btnEdit);
        buttonRow1.add(btnDelete);


        JPanel buttonRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonRow2.setOpaque(false);
        buttonRow2.add(btnExport);
        buttonRow2.add(btnRefresh);


        gbc.gridy = 0;
        rightPanel.add(buttonRow1, gbc);


        gbc.gridy = 1;
        rightPanel.add(buttonRow2, gbc);


        // ===== ADD TO MAIN PANEL =====
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);


        return mainPanel;
    }


// Add these helper methods to your class:


    /**
     * Creates a styled button with consistent appearance
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));


        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }


            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });


        return button;
    }


    /**
     * Creates a quick filter button
     */
    private JButton createQuickFilterButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));


        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }


            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });


        return button;
    }


    // Also update the clearFilters() method to clear the search field:
    private void clearFilters() {
        txtSearch.setText("");
        if (filterColumnCombo != null) {
            filterColumnCombo.setSelectedIndex(0);
        }
        sorter.setRowFilter(null);


        // Find and reset the status filter combo if it exists
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JComboBox) {
                JComboBox<?> combo = (JComboBox<?>) comp;
                if (combo.getItemAt(0).toString().contains("All Status")) {
                    combo.setSelectedIndex(0);
                }
            }
        }
    }
    private void filterByStatus(String status) {
        if ("All Statuses".equals(status)) {
            sorter.setRowFilter(null);
            return;
        }


        try {
            // Status is in column 4 (0-based index)
            RowFilter<DefaultTableModel, Object> rf =
                    RowFilter.regexFilter("(?i)" + status, 4);
            sorter.setRowFilter(rf);
        } catch (java.util.regex.PatternSyntaxException e) {
            // Do nothing or show error
        }
    }
    private JPanel createBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(5, 5, 5, 5));


        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);


        lblTotal = new JLabel("Total Businesses: 0 | Filtered: 0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 12));


        // Update label when filter changes
        sorter.addRowSorterListener(e -> updateCountLabel());


        leftPanel.add(lblTotal);


        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);


        JButton btnRefresh = new JButton("↻ Refresh List");
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setForeground(new Color(0, 100, 200));
        btnRefresh.addActionListener(e -> refreshData());


        rightPanel.add(btnRefresh);


        p.add(leftPanel, BorderLayout.WEST);
        p.add(rightPanel, BorderLayout.EAST);


        return p;
    }


    private void updateCountLabel() {
        int total = tableModel.getRowCount();
        int filtered = sorter.getViewRowCount();


        if (sorter.getRowFilter() == null) {
            lblTotal.setText(String.format("Total Businesses: %d", total));
        } else {
            lblTotal.setText(String.format("Total Businesses: %d | Showing: %d", total, filtered));
        }
    }


    private int getColumnIndex(String columnName) {
        switch(columnName) {
            case "Business Name": return 1;
            case "Owner": return 2;
            case "Nature": return 3;
            case "Status": return 4;
            case "Permit #": return 5;
            case "Address": return 6;
            case "Energy Source": return 7;
            default: return -1; // All columns
        }
    }


    private JComboBox<String> filterColumnCombo;


    private JButton btnClearFilters;
    private void exportToCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("businesses_" + LocalDate.now() + ".csv"));


        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(file)) {
                // Write headers
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    pw.print(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) pw.print(",");
                }
                pw.println();


                // Write data
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        pw.print(value != null ? value.toString() : "");
                        if (col < tableModel.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                }


                JOptionPane.showMessageDialog(this, "Data exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting: " + e.getMessage());
            }
        }
    }


    private void setupSearch() {
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                performSearch();
            }
        });


        // Add search button action
        Component[] components = txtSearch.getParent().getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton && ((JButton)comp).getText().contains("🔍")) {
                ((JButton)comp).addActionListener(e -> performSearch());
            }
        }
    }


    private void performSearch() {
        String searchText = txtSearch.getText().trim().toLowerCase();
        String selectedColumn = (String) filterColumnCombo.getSelectedItem();


        if (searchText.isEmpty()) {
            // Show all rows if search is empty
            sorter.setRowFilter(null);
            updateCountLabel();
            return;
        }


        // Create a RowFilter based on selected column
        RowFilter<DefaultTableModel, Integer> filter;


        if ("All Columns".equals(selectedColumn)) {
            // Search across all visible columns except ID (column 0)
            filter = new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    for (int i = 1; i < entry.getValueCount(); i++) { // Start from column 1
                        Object value = entry.getValue(i);
                        if (value != null) {
                            String stringValue = value.toString().toLowerCase();
                            if (stringValue.contains(searchText)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };
        } else {
            // Search in specific column
            int columnIndex = getColumnIndex(selectedColumn);
            try {
                filter = RowFilter.regexFilter("(?i)" + searchText, columnIndex);
            } catch (java.util.regex.PatternSyntaxException e) {
                filter = null; // Invalid regex
            }
        }


        if (filter != null) {
            sorter.setRowFilter(filter);
        }


        updateCountLabel();
    }


    // Update your getColumnIndex method to handle all columns:






    private JPanel createTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);




        // Column Names (Visible in Table)
        String[] cols = {"ID", "Business Name", "Owner", "Nature", "Status", "Permit #", "Address", "Energy Source"};




        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };




        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setSelectionForeground(Color.BLACK);




        // Header Styling
        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 14));
        th.setBackground(HEADER_BG);
        th.setForeground(Color.WHITE);
        th.setReorderingAllowed(false);
        // In createTablePanel() method, after creating the table:
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);


                if (!isSelected) {
                    String status = table.getValueAt(row, 4).toString(); // Status column
                    switch(status) {
                        case "Active":
                            c.setBackground(new Color(220, 255, 220)); // Light green
                            break;
                        case "Pending":
                            c.setBackground(new Color(255, 255, 200)); // Light yellow
                            break;
                        case "Expired":
                        case "Revoked":
                            c.setBackground(new Color(255, 220, 220)); // Light red
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });




        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);




        // Double Click to Edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleUpdate();
            }
        });




        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }




    // =========================================================================
    //  DATA LOADING (SWING WORKER = NO LAG)
    // =========================================================================
    private void refreshData() {
        // Disable controls while loading
        if(txtSearch != null) txtSearch.setEnabled(false);
        if(btnClearFilters != null) btnClearFilters.setEnabled(false);


        // Save current filter if any
        RowFilter<?, ?> currentFilter = sorter.getRowFilter();


        lblTotal.setText("Loading data...");


        new SwingWorker<List<BusinessEstablishment>, Void>() {
            @Override
            protected List<BusinessEstablishment> doInBackground() throws Exception {
                return new BusinessDAO().getAllBusinesses();
            }


            @Override
            protected void done() {
                try {
                    List<BusinessEstablishment> list = get();
                    tableModel.setRowCount(0);


                    for (BusinessEstablishment b : list) {
                        tableModel.addRow(new Object[]{
                                b.getBusinessId(),
                                b.getBusinessName(),
                                b.getOwnerName(),
                                b.getBusinessNature(),
                                b.getPermitStatus(),
                                b.getPermitNumber(),
                                b.getPurok(),
                                b.getElectricitySource()
                        });
                    }


                    // Re-apply filter if there was one
                    if (currentFilter != null) {
                        sorter.setRowFilter((RowFilter<DefaultTableModel, Object>) currentFilter);
                    }


                    updateCountLabel();


                } catch (Exception e) {
                    lblTotal.setText("Error loading data");
                    JOptionPane.showMessageDialog(AdminBusinessTab.this,
                            "Error loading data: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    if(txtSearch != null) txtSearch.setEnabled(true);
                    if(btnClearFilters != null) btnClearFilters.setEnabled(true);
                }
            }
        }.execute();
    }
    private void showAdvancedFilterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Advanced Filters", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);


        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);


        // Business Name filter
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Business Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtNameFilter = new JTextField(15);
        form.add(txtNameFilter, gbc);


        // Owner filter
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Owner Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtOwnerFilter = new JTextField(15);
        form.add(txtOwnerFilter, gbc);


        // Status filter
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        String[] statuses = {"Any", "Active", "Pending", "Expired", "Revoked"};
        JComboBox<String> cmbStatusFilter = new JComboBox<>(statuses);
        form.add(cmbStatusFilter, gbc);


        // Energy Source filter
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("Energy Source:"), gbc);
        gbc.gridx = 1;
        String[] sources = {"Any", "Grid/Coop", "Solar", "Generator", "None"};
        JComboBox<String> cmbEnergyFilter = new JComboBox<>(sources);
        form.add(cmbEnergyFilter, gbc);


        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApply = new JButton("Apply Filters");
        JButton btnClear = new JButton("Clear All");


        btnApply.setBackground(new Color(0, 123, 167));
        btnApply.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(220, 220, 220));


        btnApply.addActionListener(e -> {
            applyAdvancedFilters(
                    txtNameFilter.getText().trim(),
                    txtOwnerFilter.getText().trim(),
                    (String) cmbStatusFilter.getSelectedItem(),
                    (String) cmbEnergyFilter.getSelectedItem()
            );
            dialog.dispose();
        });


        btnClear.addActionListener(e -> {
            clearFilters();
            dialog.dispose();
        });


        buttonPanel.add(btnClear);
        buttonPanel.add(btnApply);


        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }


    private void applyAdvancedFilters(String name, String owner, String status, String energySource) {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();


        if (!name.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + name, 1)); // Business Name column
        }


        if (!owner.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + owner, 2)); // Owner column
        }


        if (!"Any".equals(status)) {
            filters.add(RowFilter.regexFilter("(?i)" + status, 4)); // Status column
        }


        if (!"Any".equals(energySource)) {
            filters.add(RowFilter.regexFilter("(?i)" + energySource, 7)); // Energy Source column
        }


        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }


    }
    // Add these validation methods to your AdminBusinessTab class:


    /**
     * Validates business name using regex
     * - Allows letters, numbers, spaces, apostrophes, hyphens
     * - Minimum 2 characters, maximum 100
     */
    private boolean isValidBusinessName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Allows letters, numbers, spaces, apostrophes, hyphens, and common business name characters
        String regex = "^[A-Za-z0-9\\s&.'\"()-]{2,100}$";
        return name.matches(regex);
    }


    /**
     * Validates business nature/description
     * - Allows letters, numbers, spaces, commas, periods, hyphens
     */
    private boolean isValidBusinessNature(String nature) {
        if (nature == null || nature.trim().isEmpty()) {
            return true; // Optional field
        }
        String regex = "^[A-Za-z0-9\\s,.-]{2,200}$";
        return nature.matches(regex);
    }


    /**
     * Validates permit number format
     * - Format: BPL-YYYY-XXXXX (where X are digits)
     */
    private boolean isValidPermitNumber(String permitNo) {
        if (permitNo == null || permitNo.trim().isEmpty()) {
            return true; // Optional field
        }
        String regex = "^BPL-\\d{4}-\\d{5}$";
        return permitNo.matches(regex);
    }


    /**
     * Validates purok/zone format
     * - Should be like: Purok 1, Zone 5, etc.
     */
    private boolean isValidPurok(String purok) {
        if (purok == null || purok.trim().isEmpty()) {
            return true; // Optional field
        }
        String regex = "^(Purok|Zone|Barangay|Sitio|Precinct)\\s*\\d+[A-Z]?$";
        return purok.trim().matches(regex);
    }


    /**
     * Validates street address
     * - Allows letters, numbers, spaces, common address characters
     */
    private boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return true; // Optional field
        }
        String regex = "^[A-Za-z0-9\\s,#.-]{5,200}$";
        return address.matches(regex);
    }


    /**
     * Validates capital investment format
     * - Allows numbers with optional decimal point
     * - No negative values
     */
    private boolean isValidCapital(String capitalStr) {
        if (capitalStr == null || capitalStr.trim().isEmpty()) {
            return false;
        }
        String regex = "^\\d+(\\.\\d{1,2})?$";
        if (!capitalStr.matches(regex)) {
            return false;
        }


        try {
            double capital = Double.parseDouble(capitalStr);
            return capital >= 0; // No negative values
        } catch (NumberFormatException e) {
            return false;
        }
    }


    /**
     * Validates employee count
     * - Must be a positive integer
     */
    private boolean isValidEmployeeCount(String countStr) {
        if (countStr == null || countStr.trim().isEmpty()) {
            return false;
        }


        // Allow only digits (no letters or special characters)
        String regex = "^\\d+$";
        if (!countStr.matches(regex)) {
            return false;
        }


        try {
            int count = Integer.parseInt(countStr);
            // Check if within 1-999 range (3 digits maximum)
            return count > 0 && count <= 999;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    /**
     * Validates phone number format (if you add phone field later)
     * - Supports various formats: +639XXXXXXXXX, 09XXXXXXXXX, (02) XXXX-XXXX
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Optional
        }
        String regex = "^(\\+?63|0)?[9]\\d{9}$|^\\(\\d{2}\\)\\s?\\d{3,4}-\\d{4}$";
        return phone.replaceAll("\\s", "").matches(regex);
    }


    /**
     * Validates email format (if you add email field later)
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Optional
        }
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }


    /**
     * Validates date string format
     * - Must be YYYY-MM-DD
     */
    private boolean isValidDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return true; // Optional
        }
        String regex = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";
        return dateStr.matches(regex);
    }
    // =========================================================================
    //  CRUD LOGIC
    // =========================================================================




    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) return;




        int modelRow = table.convertRowIndexToModel(row);
        int busId = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());




        // In a real app, you might want to fetch full details by ID here
        // For now, we pass the ID to the form
        BusinessEstablishment dummy = new BusinessEstablishment();
        dummy.setBusinessId(busId);
        dummy.setBusinessName((String) tableModel.getValueAt(modelRow, 1));
        dummy.setBusinessNature((String) tableModel.getValueAt(modelRow, 3));
        // ... populate others if needed, or fetch from DB inside dialog




        showFormDialog(dummy);
    }




    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a business to delete.");
            return;
        }




        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this business?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = table.convertRowIndexToModel(row);
            int id = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());




            if (new BusinessDAO().deleteBusiness(id)) {
                JOptionPane.showMessageDialog(this, "Deleted successfully.");
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting.");
            }
        }
    }




    // =========================================================================
    //  FORM DIALOG (Add/Update)
    // =========================================================================




    // =========================================================================
//  FORM DIALOG (Add/Update) - COMPLETED VERSION
// =========================================================================
// Add these imports at the top:




    // Update the showFormDialog method with date pickers:
    private void showFormDialog(BusinessEstablishment existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add Business" : "Edit Business",
                true);
        d.setSize(500, 750);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);


        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);


        // Load full business details if editing
        BusinessEstablishment fullBusiness = null;
        if (existing != null) {
            List<BusinessEstablishment> allBusinesses = new BusinessDAO().getAllBusinesses();
            for (BusinessEstablishment b : allBusinesses) {
                if (b.getBusinessId() == existing.getBusinessId()) {
                    fullBusiness = b;
                    break;
                }
            }
        }


        // ========== FORM FIELDS ==========


        // 1. Owner Selection
        JPanel ownerPanel = new JPanel(new BorderLayout(5, 0));
        ownerPanel.setBorder(BorderFactory.createTitledBorder("Owner Information"));
        JTextField txtOwnerName = new JTextField();
        txtOwnerName.setEditable(false);
        JTextField txtOwnerId = new JTextField();
        txtOwnerId.setEditable(false);
        txtOwnerId.setVisible(false);


        JButton btnPickOwner = new JButton("Select Owner");
        btnPickOwner.addActionListener(e -> showResidentPicker(txtOwnerName, txtOwnerId));


        JPanel ownerFieldPanel = new JPanel(new BorderLayout(5, 0));
        ownerFieldPanel.add(txtOwnerName, BorderLayout.CENTER);
        ownerFieldPanel.add(btnPickOwner, BorderLayout.EAST);
        ownerPanel.add(new JLabel("Owner:"), BorderLayout.WEST);
        ownerPanel.add(ownerFieldPanel, BorderLayout.CENTER);


        // 2. Basic Business Info
        JPanel basicPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        basicPanel.setBorder(BorderFactory.createTitledBorder("Basic Information"));


        JTextField txtName = new JTextField();
        JTextField txtNature = new JTextField();
        String[] types = {"Sole Proprietorship", "Partnership", "Corporation"};
        JComboBox<String> cmbType = new JComboBox<>(types);


        basicPanel.add(new JLabel("Business Name*:"));
        basicPanel.add(txtName);
        basicPanel.add(new JLabel("Nature of Business:"));
        basicPanel.add(txtNature);
        basicPanel.add(new JLabel("Ownership Type:"));
        basicPanel.add(cmbType);


        // 3. Location Info
        JPanel locationPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        locationPanel.setBorder(BorderFactory.createTitledBorder("Location Information"));



        JTextField txtAddress = new JTextField();


        locationPanel.add(new JLabel("Purok:"));


        String [] puroks = new SystemConfigDAO().getOptionsNature("purok");
        JComboBox<String> cbPurok = new JComboBox<>(puroks);
        locationPanel.add(cbPurok);
        locationPanel.add(new JLabel("Street Address:"));
        locationPanel.add(txtAddress);


        // 4. Permit & Status Info with DATE PICKERS
        JPanel permitPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        permitPanel.setBorder(BorderFactory.createTitledBorder("Permit Information"));


        String[] statuses = {"Active", "Pending", "Expired", "Revoked"};
        JComboBox<String> cmbStatus = new JComboBox<>(statuses);
        JTextField txtPermitNo = new JTextField();
        JTextField txtCapital = new JTextField("0.00");


        // DATE PICKERS instead of text fields
        JLabel lblDateEstablished = new JLabel("Date Established:");
        JDateChooser dateChooserEstablished = new JDateChooser();
        dateChooserEstablished.setDateFormatString("yyyy-MM-dd");
        dateChooserEstablished.getJCalendar().setTodayButtonVisible(true);
        dateChooserEstablished.getJCalendar().setNullDateButtonVisible(true);
        dateChooserEstablished.getJCalendar().setTodayButtonText("Today");
        dateChooserEstablished.getJCalendar().setNullDateButtonText("Clear");


        JLabel lblLastRenewal = new JLabel("Last Renewal Date:");
        JDateChooser dateChooserRenewal = new JDateChooser();
        dateChooserRenewal.setDateFormatString("yyyy-MM-dd");
        dateChooserRenewal.getJCalendar().setTodayButtonVisible(true);
        dateChooserRenewal.getJCalendar().setNullDateButtonVisible(true);
        dateChooserRenewal.getJCalendar().setTodayButtonText("Today");
        dateChooserRenewal.getJCalendar().setNullDateButtonText("Clear");


        permitPanel.add(new JLabel("Permit Status:"));
        permitPanel.add(cmbStatus);
        permitPanel.add(new JLabel("Permit Number:"));
        permitPanel.add(txtPermitNo);
        permitPanel.add(new JLabel("Capital Investment:"));
        permitPanel.add(txtCapital);
        permitPanel.add(lblDateEstablished);
        permitPanel.add(dateChooserEstablished);
        permitPanel.add(lblLastRenewal);
        permitPanel.add(dateChooserRenewal);


        // 5. Additional Info
        JPanel additionalPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        additionalPanel.setBorder(BorderFactory.createTitledBorder("Additional Information"));


        String[] powerSources = {"Grid/Coop", "Solar", "Generator", "None"};
        JComboBox<String> cmbPower = new JComboBox<>(powerSources);


        String[] buildingTypes = {"Concrete", "Semi-Concrete", "Wood", "Mixed Materials"};
        JComboBox<String> cmbBuildingType = new JComboBox<>(buildingTypes);


        JTextField txtEmployeeCount = createEmployeeCountField();


        additionalPanel.add(new JLabel("Electricity Source:"));
        additionalPanel.add(cmbPower);
        additionalPanel.add(new JLabel("Building Type:"));
        additionalPanel.add(cmbBuildingType);
        additionalPanel.add(new JLabel("Employee Count:"));
        additionalPanel.add(txtEmployeeCount);


        // ========== PRE-FILL IF EDITING ==========
        if (fullBusiness != null) {
            // Owner info
            if (fullBusiness.getOwnerId() > 0) {
                txtOwnerId.setText(String.valueOf(fullBusiness.getOwnerId()));
                txtOwnerName.setText(fullBusiness.getOwnerName() != null ?
                        fullBusiness.getOwnerName() : "Owner #" + fullBusiness.getOwnerId());
            }


            // Basic info
            txtName.setText(fullBusiness.getBusinessName());
            txtNature.setText(fullBusiness.getBusinessNature() != null ? fullBusiness.getBusinessNature() : "");


            // Set ownership type
            if (fullBusiness.getOwnershipType() != null) {
                cmbType.setSelectedItem(fullBusiness.getOwnershipType());
            }


            // Location info
            if(fullBusiness.getPurok() != null){
                cbPurok.setSelectedItem(fullBusiness.getPurok());
            }else{
                cbPurok.setSelectedIndex(0);
            }

            txtAddress.setText(fullBusiness.getStreetAddress() != null ? fullBusiness.getStreetAddress() : "");


            // Permit info
            if (fullBusiness.getPermitStatus() != null) {
                cmbStatus.setSelectedItem(fullBusiness.getPermitStatus());
            }
            txtPermitNo.setText(fullBusiness.getPermitNumber() != null ? fullBusiness.getPermitNumber() : "");
            txtCapital.setText(String.valueOf(fullBusiness.getCapitalInvestment()));


            // Dates - Set date picker values
            if (fullBusiness.getDateEstablished() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date establishedDate = sdf.parse(fullBusiness.getDateEstablished().toString());
                    dateChooserEstablished.setDate(establishedDate);
                } catch (Exception ex) {
                    // Handle parse error
                }
            }


            if (fullBusiness.getLastRenewalDate() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date renewalDate = sdf.parse(fullBusiness.getLastRenewalDate().toString());
                    dateChooserRenewal.setDate(renewalDate);
                } catch (Exception ex) {
                    // Handle parse error
                }
            }


            // Additional info
            if (fullBusiness.getElectricitySource() != null) {
                cmbPower.setSelectedItem(fullBusiness.getElectricitySource());
            }
            if (fullBusiness.getBuildingType() != null) {
                cmbBuildingType.setSelectedItem(fullBusiness.getBuildingType());
            }
            txtEmployeeCount.setText(String.valueOf(fullBusiness.getEmployeeCount()));
        }


        // ========== ADD ALL PANELS TO FORM ==========
        form.add(ownerPanel);
        form.add(Box.createVerticalStrut(10));
        form.add(basicPanel);
        form.add(Box.createVerticalStrut(10));
        form.add(locationPanel);
        form.add(Box.createVerticalStrut(10));
        form.add(permitPanel);
        form.add(Box.createVerticalStrut(10));
        form.add(additionalPanel);


        // Wrap form in scroll pane
        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);


        // ========== SAVE BUTTON ==========
        JButton btnSave = new JButton(existing == null ? "Add Business" : "Update Business");
        btnSave.setBackground(existing == null ? BTN_ADD : BTN_UPDATE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));
        btnSave.setFocusPainted(false);


        btnSave.addActionListener(e -> {
            // Get all field values
            String businessName = txtName.getText().trim();
            String ownerIdStr = txtOwnerId.getText().trim();
            String businessNature = txtNature.getText().trim();
            String permitNo = txtPermitNo.getText().trim();
            String purok = cbPurok.getSelectedItem().toString();
            String address = txtAddress.getText().trim();
            String capitalStr = txtCapital.getText().trim();
            String employeeCountStr = txtEmployeeCount.getText().trim();


            // ========== VALIDATION ==========
            List<String> errors = new ArrayList<>();


            // 1. Owner validation
            if (ownerIdStr.isEmpty()) {
                errors.add("Please select a business owner.");
            }


            // 2. Business name validation
            if (businessName.isEmpty()) {
                errors.add("Business Name is required.");
            } else if (!isValidBusinessName(businessName)) {
                errors.add("Business Name should be 2-100 characters and can only contain letters, numbers, spaces, &, ., ', \", (, ), -");
            }


            // 3. Business nature validation
            if (!businessNature.isEmpty() && !isValidBusinessNature(businessNature)) {
                errors.add("Business Nature can only contain letters, numbers, spaces, commas, periods, and hyphens (2-200 chars).");
            }


            // 4. Permit number validation
            if (!permitNo.isEmpty() && !isValidPermitNumber(permitNo)) {
                errors.add("Permit Number should follow format: BPL-YYYY-XXXXX (e.g., BPL-2023-12345)");
            }


            // 5. Purok validation
            if (!purok.isEmpty() && !isValidPurok(purok)) {
                errors.add("Purok should be in format: Purok 1, Zone 5, Barangay 3, etc.");
            }


            // 6. Address validation
            if (!address.isEmpty() && !isValidAddress(address)) {
                errors.add("Address should be 5-200 characters and can only contain letters, numbers, spaces, #, ,, ., -");
            }


            // 7. Capital validation
            if (!isValidCapital(capitalStr)) {
                errors.add("Capital Investment must be a positive number (e.g., 1000 or 1000.50)");
            }


            // 8. Employee count validation
            // 8. Employee count validation
            if (!isValidEmployeeCount(employeeCountStr)) {
                errors.add("Employee Count must be a number between 1-999 (maximum 3 digits).");
            }


            // Display errors if any
            if (!errors.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder("Please fix the following errors:\n\n");
                for (String error : errors) {
                    errorMsg.append("• ").append(error).append("\n");
                }
                JOptionPane.showMessageDialog(d, errorMsg.toString(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            // ========== PARSE VALUES ==========
            int ownerId = Integer.parseInt(ownerIdStr);
            int employeeCount = Integer.parseInt(employeeCountStr);
            double capitalInvestment = Double.parseDouble(capitalStr);


            // Get dates from date pickers
            LocalDate dateEstablished = null;
            LocalDate lastRenewalDate = null;


            try {
                // Date Established
                if (dateChooserEstablished.getDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dateStr = sdf.format(dateChooserEstablished.getDate());
                    dateEstablished = LocalDate.parse(dateStr);
                }


                // Last Renewal Date
                if (dateChooserRenewal.getDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String dateStr = sdf.format(dateChooserRenewal.getDate());
                    lastRenewalDate = LocalDate.parse(dateStr);
                }


                // Validate date logic
                if (dateEstablished != null && lastRenewalDate != null && lastRenewalDate.isBefore(dateEstablished)) {
                    JOptionPane.showMessageDialog(d, "Last Renewal Date cannot be before Date Established.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }


            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Invalid date format. Please select valid dates.", "Date Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            // ========== CREATE BUSINESS OBJECT ==========
            BusinessEstablishment b = new BusinessEstablishment();
            b.setOwnerId(ownerId);
            b.setBusinessName(businessName);
            b.setBusinessNature(businessNature);
            b.setOwnershipType(cmbType.getSelectedItem().toString());
            b.setPurok(purok);
            b.setStreetAddress(address);
            b.setDateEstablished(dateEstablished);
            b.setEmployeeCount(employeeCount);
            b.setBuildingType(cmbBuildingType.getSelectedItem().toString());
            b.setPermitStatus(cmbStatus.getSelectedItem().toString());
            b.setPermitNumber(permitNo);
            b.setLastRenewalDate(lastRenewalDate);
            b.setCapitalInvestment(capitalInvestment);
            b.setElectricitySource(cmbPower.getSelectedItem().toString());


            // ========== SAVE TO DATABASE ==========
            boolean success;
            if (existing == null) {
                success = new BusinessDAO().addBusiness(b);

            } else {
                b.setBusinessId(existing.getBusinessId());
                success = new BusinessDAO().updateBusiness(b);
            }


            if (success) {
                JOptionPane.showMessageDialog(d,
                        existing == null ? "Business added successfully!" : "Business updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
                refreshData();
            } else {
                JOptionPane.showMessageDialog(d,
                        "Database error. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });


        // ========== DIALOG LAYOUT ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnSave);


        d.add(scrollPane, BorderLayout.CENTER);
        d.add(buttonPanel, BorderLayout.SOUTH);


        // Set dialog visible
        d.setVisible(true);
    }
    /**
     * Creates a text field that only accepts numeric input up to 3 digits
     */
    private JTextField createEmployeeCountField() {
        JTextField field = new JTextField("1");
        field.setFont(new Font("Arial", Font.PLAIN, 12));


        // Add input verification
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                // Allow only digits
                if (string.matches("\\d+")) {
                    String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + string;
                    // Check if total length <= 3 and value <= 999
                    if (newText.length() <= 3) {
                        try {
                            int value = Integer.parseInt(newText);
                            if (value >= 0 && value <= 999) {
                                super.insertString(fb, offset, string, attr);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                }
            }


            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text.matches("\\d+")) {
                    String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String newText = currentText.substring(0, offset) + text +
                            currentText.substring(offset + length);


                    // Check if total length <= 3 and value <= 999
                    if (newText.length() <= 3) {
                        try {
                            int value = Integer.parseInt(newText);
                            if (value >= 0 && value <= 999) {
                                super.replace(fb, offset, length, text, attrs);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                } else if (text.isEmpty()) {
                    // Allow deletion
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });


        return field;
    }


    // Optional: You can add a helper method to create styled date pickers
    private JDateChooser createStyledDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setFont(new Font("Arial", Font.PLAIN, 12));
        dateChooser.setPreferredSize(new Dimension(150, 25));


        // Customize the calendar appearance
        dateChooser.getJCalendar().setTodayButtonVisible(true);
        dateChooser.getJCalendar().setNullDateButtonVisible(true);
        dateChooser.getJCalendar().setTodayButtonText("Today");
        dateChooser.getJCalendar().setNullDateButtonText("Clear");


        return dateChooser;
    }
    // =========================================================================
    //  RESIDENT PICKER (Reused Logic) - UPDATED WITH SEARCH FUNCTIONALITY
    // =========================================================================
    private void showResidentPicker(JTextField nameField, JTextField idField) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Owner", true);
        dialog.setSize(600, 500); // Increased width for better search layout
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);




        String[] cols = {"ID", "Name", "Purok"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };




        JTable t = new JTable(m);
        t.setRowHeight(25);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));




        // Create TableRowSorter for search functionality
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        t.setRowSorter(sorter);




        // Load Residents
        List<Resident> list = new ResidentDAO().getAllResidents();
        for(Resident r : list) {
            m.addRow(new Object[]{
                    r.getResidentId(),
                    r.getFirstName() + " " + r.getMiddleName() + " " + r.getLastName(),
                    r.getPurok()
            });
        }




        // ========== SEARCH PANEL ==========
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));




        JLabel lblSearch = new JLabel("Search Resident:");
        JTextField txtSearchResident = new JTextField(20);
        txtSearchResident.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearchResident.putClientProperty("JTextField.placeholderText", "Search by name or purok...");




        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(0, 123, 167)); // Cerulean blue
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Arial", Font.BOLD, 12));
        btnSearch.setFocusPainted(false);




        // Add search button action listener
        btnSearch.addActionListener(e -> {
            String searchText = txtSearchResident.getText().trim();
            if (searchText.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                // Search in both Name and Purok columns
                RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + searchText, 1, 2);
                sorter.setRowFilter(rf);
            }
        });




        // Add key listener for real-time search as user types
        txtSearchResident.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = txtSearchResident.getText().trim();
                if (searchText.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    RowFilter<DefaultTableModel, Object> rf = RowFilter.regexFilter("(?i)" + searchText, 1, 2);
                    sorter.setRowFilter(rf);
                }
            }
        });




        searchPanel.add(lblSearch);
        searchPanel.add(txtSearchResident);
        searchPanel.add(btnSearch);




        // ========== MAIN TABLE PANEL ==========
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        tablePanel.add(new JScrollPane(t), BorderLayout.CENTER);




        // ========== SELECT BUTTON PANEL ==========
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));




        JButton btnSelect = new JButton("Select");
        btnSelect.setBackground(new Color(46, 139, 87)); // Green
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFont(new Font("Arial", Font.BOLD, 12));
        btnSelect.setFocusPainted(false);




        btnSelect.addActionListener(e -> {
            int row = t.getSelectedRow();
            if(row != -1) {
                int modelRow = t.convertRowIndexToModel(row);
                idField.setText(m.getValueAt(modelRow, 0).toString());
                nameField.setText(m.getValueAt(modelRow, 1).toString());
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a resident first.");
            }
        });




        // Double-click to select
        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    int row = t.getSelectedRow();
                    if(row != -1) {
                        int modelRow = t.convertRowIndexToModel(row);
                        idField.setText(m.getValueAt(modelRow, 0).toString());
                        nameField.setText(m.getValueAt(modelRow, 1).toString());
                        dialog.dispose();
                    }
                }
            }
        });




        buttonPanel.add(btnSelect);




        // ========== ADD ALL COMPONENTS TO DIALOG ==========
        dialog.add(searchPanel, BorderLayout.NORTH);
        dialog.add(tablePanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);




        dialog.setVisible(true);
    }




    private JButton createBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setSize(1000, 700);
            f.add(new AdminBusinessTab());
            f.setVisible(true);
        });
    }
}



