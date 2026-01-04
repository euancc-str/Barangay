package org.example.Admin.AdminSettings;

import org.example.Admin.SystemLogDAO;
import org.example.BorrowingDAO;
import org.example.BarangayAssetDAO;
import org.example.ResidentDAO;
import org.example.Users.BarangayAsset;
import org.example.Users.BorrowRecord;
import org.example.Users.Resident;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.awt.print.*;
import java.text.MessageFormat;
import java.util.*; // Add this import for java.util.Date


public class AdminAssetBorrowingTab extends JPanel {

    // Enhanced Color scheme
    private final Color PRIMARY_COLOR = new Color(0, 102, 204);      // Blue
    private final Color SECONDARY_COLOR = new Color(240, 147, 43);   // Orange
    private final Color ACCENT_COLOR = new Color(39, 174, 96);       // Green
    private final Color WARNING_COLOR = new Color(231, 76, 60);      // Red
    private final Color INFO_COLOR = new Color(52, 152, 219);        // Light Blue
    private final Color BG_COLOR = new Color(248, 249, 250);         // Light Gray
    private final Color SECTION_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(206, 212, 218);
    private final Color TABLE_HEADER_COLOR = new Color(52, 73, 94);  // Dark Blue
    private final Color SHADOW_COLOR = new Color(0, 0, 0, 20);

    // Calendar color variables (copied from SecretaryPrintDocument)
    private final Color MODERN_BLUE = new Color(66, 133, 244);
    private final Color LIGHT_GREY = new Color(248, 249, 250);
    private final Color DARK_GREY = new Color(52, 58, 64);

    // Enhanced Fonts
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 28);
    private final Font SECTION_FONT = new Font("Segoe UI Semibold", Font.BOLD, 16);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font BUTTON_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font TABLE_HEADER_FONT = new Font("Segoe UI Semibold", Font.BOLD, 13);
    private final Font WARNING_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private JTabbedPane tabbedPane;
    private JTable assetTable;
    private JTable borrowingTable;
    private JTable historyTable;
    private DefaultTableModel assetTableModel;
    private DefaultTableModel borrowingTableModel;
    private DefaultTableModel historyTableModel;
    private JTextField assetSearchField;
    private JTextField borrowingSearchField;
    private JTextField historySearchField;
    private JLabel assetStatsLabel;
    private JLabel borrowingStatsLabel;
    private JLabel historyStatsLabel;

    // Date filter fields for each tab
    private JButton dateFilterBtn; // For history tab
    private JButton assetDateFilterBtn; // For asset tab
    private JButton borrowingDateFilterBtn; // For borrowing tab

    // Selected dates for each tab
    private java.util.Date selectedDate; // History tab
    private java.util.Date selectedAssetDate; // Asset tab
    private java.util.Date selectedBorrowingDate; // Borrowing tab

    // Add missing combo box declaration
    private JComboBox<String> historyFilterCombo;
    private JComboBox<String> assetDateTypeCombo;
    private JComboBox<String> borrowingDateTypeCombo;

    // Regex patterns for validation
    private final String NAME_REGEX = "^[a-zA-Z0-9\\s\\-\\.,&()]+$";
    private final String PROPERTY_REGEX = "^[A-Z0-9\\-\\/]+$";
    private final String SERIAL_REGEX = "^[A-Za-z0-9\\-\\s]+$";
    private final String DECIMAL_REGEX = "^\\d+(\\.\\d{1,2})?$";
    private final String INTEGER_REGEX = "^\\d+$";
    private final String DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";
    private final String CUSTODIAN_REGEX = "^[A-Za-z\\s\\.,]+$";


    public AdminAssetBorrowingTab() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        SwingUtilities.invokeLater(this::initializeUI);
        startSmartPolling();
    }


    private javax.swing.Timer smartTimer;
    private int lastAssetMaxId = 0;


    private void startSmartPolling() {
        initializeLastAssetMaxId();
        smartTimer = new javax.swing.Timer(2000, e -> {
            if (assetTable == null || assetTable.getSelectedRow() == -1) {
                checkForAssetUpdates();
            }
        });
        smartTimer.start();
    }


    private void initializeLastAssetMaxId() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                String sql = "SELECT MAX(assetId) as max_id FROM barangay_asset";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {
                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    return rs.next() ? rs.getInt("max_id") : 0;
                }
            }


            @Override
            protected void done() {
                try {
                    lastAssetMaxId = get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    private void checkForAssetUpdates() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String sql = "SELECT MAX(assetId) as max_id FROM barangay_asset";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {
                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        int currentMaxId = rs.getInt("max_id");
                        return currentMaxId > lastAssetMaxId;
                    }
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadAssetData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    private void updateLastAssetMaxId() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                String sql = "SELECT MAX(assetId) as max_id FROM barangay_asset";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {
                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    return rs.next() ? rs.getInt("max_id") : 0;
                }
            }


            @Override
            protected void done() {
                try {
                    lastAssetMaxId = get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    private void initializeUI() {
        // Create gradient header panel
        JPanel headerPanel = createHeaderPanel();

        // Create tabbed pane with modern styling
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(SECTION_FONT);
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));

        // Tab 1: Asset Management
        JPanel assetPanel = createAssetPanel();
        tabbedPane.addTab("📦 Assets & Inventory", assetPanel);


        // Tab 2: Borrowing Management
        JPanel borrowingPanel = createBorrowingPanel();
        tabbedPane.addTab("📝 Borrowing Transactions", borrowingPanel);


        // Tab 3: Borrowing History
        JPanel historyPanel = createHistoryPanel();
        tabbedPane.addTab("📊 Borrowing History", historyPanel);


        // Custom tab colors
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setBackgroundAt(i, Color.WHITE);
            tabbedPane.setForegroundAt(i, TABLE_HEADER_COLOR);
        }


        // Main container with shadow effect
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 2, 1, SHADOW_COLOR),
                new EmptyBorder(0, 0, 0, 0)
        ));
        mainContainer.add(tabbedPane, BorderLayout.CENTER);


        add(headerPanel, BorderLayout.NORTH);
        add(mainContainer, BorderLayout.CENTER);

        // Load initial data
        loadAssetData();
        loadBorrowingData();
        loadHistoryData();
    }



    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));


        JLabel titleLabel = new JLabel("🏢 Asset & Borrowing Management");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);








        JLabel subtitleLabel = new JLabel("Manage barangay assets, track borrowing, and monitor returns");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));








        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(52, 73, 94));
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);








        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setBackground(new Color(52, 73, 94));
        statsPanel.setOpaque(false);








        JLabel dateLabel = new JLabel("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(Color.WHITE);








        statsPanel.add(dateLabel);








        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);








        return headerPanel;
    }








    private JPanel createAssetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));








        // Top section with search and buttons
        JPanel topPanel = createSectionPanel("📦 ASSET INVENTORY",
                "Search assets by name, property #, location, status...");
        topPanel.setLayout(new BorderLayout(0, 15));








        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new EmptyBorder(0, 0, 0, 0));








        assetSearchField = createSearchField("Search assets...");
        JComboBox<String> searchFilterCombo = new JComboBox<>(new String[]{
                "All Fields", "Item Name", "Property No", "Location", "Status", "Value Range", "Date Acquired"
        });
        searchFilterCombo.setFont(FIELD_FONT);
        searchFilterCombo.setBackground(Color.WHITE);








        // DATE FILTER SECTION FOR ASSETS
        JLabel dateLabel = new JLabel("  Date:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Date type combo for assets
        assetDateTypeCombo = new JComboBox<>(new String[]{
                "Date Acquired", "Purchase Date"
        });
        assetDateTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        assetDateTypeCombo.setBackground(Color.WHITE);
        assetDateTypeCombo.setBorder(new RoundBorder(4, new Color(206, 212, 218)));
        assetDateTypeCombo.setPreferredSize(new Dimension(120, 35));

        // Modern date filter button for assets
        assetDateFilterBtn = new JButton("📅 Select Date") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (selectedAssetDate == null) {
                    g2d.setColor(new Color(233, 236, 239));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, MODERN_BLUE,
                            getWidth(), getHeight(), new Color(26, 115, 232)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                super.paintComponent(g2d);
            }
        };
        assetDateFilterBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        assetDateFilterBtn.setForeground(selectedAssetDate == null ? Color.DARK_GRAY : Color.WHITE);
        assetDateFilterBtn.setFocusPainted(false);
        assetDateFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        assetDateFilterBtn.setBorder(new RoundBorder(8, new Color(206, 212, 218)));
        assetDateFilterBtn.setContentAreaFilled(false);
        assetDateFilterBtn.setOpaque(false);
        assetDateFilterBtn.setPreferredSize(new Dimension(140, 35));
        assetDateFilterBtn.addActionListener(e -> showModernDatePickerForAsset());

        // Clear date button for assets
        JButton clearAssetDateBtn = new JButton("Clear") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(233, 236, 239));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2d);
            }
        };
        clearAssetDateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearAssetDateBtn.setForeground(Color.DARK_GRAY);
        clearAssetDateBtn.setFocusPainted(false);
        clearAssetDateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearAssetDateBtn.setBorder(new RoundBorder(8, new Color(206, 212, 218)));
        clearAssetDateBtn.setContentAreaFilled(false);
        clearAssetDateBtn.setOpaque(false);
        clearAssetDateBtn.setPreferredSize(new Dimension(80, 35));
        clearAssetDateBtn.addActionListener(e -> {
            selectedAssetDate = null;
            assetDateFilterBtn.setText("📅 Select Date");
            assetDateFilterBtn.setForeground(Color.DARK_GRAY);
            assetDateFilterBtn.repaint();
            clearAssetDateBtn.repaint();
            filterAssetTable();
        });








        JButton btnClearSearch = createIconButton("✕", "Clear search");
        btnClearSearch.addActionListener(e -> {
            assetSearchField.setText("");
            filterAssetTable();
        });








        JPanel searchComponents = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchComponents.setBackground(Color.WHITE);
        searchComponents.add(assetSearchField);
        searchComponents.add(searchFilterCombo);
        searchComponents.add(dateLabel);
        searchComponents.add(assetDateTypeCombo);
        searchComponents.add(assetDateFilterBtn);
        searchComponents.add(Box.createHorizontalStrut(5));
        searchComponents.add(clearAssetDateBtn);
        searchComponents.add(btnClearSearch);








        searchPanel.add(new JLabel("🔍 Search:"), BorderLayout.WEST);
        searchPanel.add(searchComponents, BorderLayout.CENTER);








        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);








        JButton btnAddAsset = createModernButton("+ New Asset", ACCENT_COLOR, "Add new barangay asset");
        btnAddAsset.addActionListener(e -> handleAddAsset());








        JButton btnEditAsset = createModernButton("✏️ Edit", PRIMARY_COLOR, "Edit selected asset");
        btnEditAsset.addActionListener(e -> handleEditAsset());








        JButton btnDeleteAsset = createModernButton("🗑️ Delete", WARNING_COLOR, "Delete selected asset");
        btnDeleteAsset.addActionListener(e -> handleDeleteAsset());








        JButton btnExport = createModernButton("📤 Export", INFO_COLOR, "Export asset data");
        btnExport.addActionListener(e -> exportAssetData());








        buttonPanel.add(btnAddAsset);
        buttonPanel.add(btnEditAsset);
        buttonPanel.add(btnDeleteAsset);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnExport);








        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);








        // Table
        String[] assetColumns = {"ID", "Item Name", "Property No", "Date Acquired", "Status", "Value", "Location", "Custodian"};
        assetTableModel = new DefaultTableModel(assetColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };








        assetTable = new JTable(assetTableModel);
        styleAssetTable();








        JScrollPane assetScroll = new JScrollPane(assetTable);
        assetScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        assetScroll.getViewport().setBackground(Color.WHITE);








        // Statistics panel
        JPanel statsPanel = createStatsPanel();
        assetStatsLabel = new JLabel("Loading assets...");
        assetStatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        assetStatsLabel.setForeground(new Color(100, 100, 100));
        statsPanel.add(assetStatsLabel, BorderLayout.WEST);








        // Add search functionality
        TableRowSorter<DefaultTableModel> assetSorter = new TableRowSorter<>(assetTableModel);
        assetTable.setRowSorter(assetSorter);








        assetSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterAssetTable(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterAssetTable(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterAssetTable(); }
        });








        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(assetScroll, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.SOUTH);








        return panel;
    }








    private JPanel createBorrowingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));








        JPanel topPanel = createSectionPanel("📝 BORROWING TRANSACTIONS",
                "Manage active borrowing transactions");
        topPanel.setLayout(new BorderLayout(0, 15));








        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);








        borrowingSearchField = createSearchField("Search borrowing records...");
        JButton btnClearBorrowingSearch = createIconButton("✕", "Clear search");
        btnClearBorrowingSearch.addActionListener(e -> {
            borrowingSearchField.setText("");
            filterBorrowingTable();
        });








        // DATE FILTER SECTION FOR BORROWING
        JLabel borrowingDateLabel = new JLabel("  Date:");
        borrowingDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Date type combo for borrowing
        borrowingDateTypeCombo = new JComboBox<>(new String[]{
                "Date Borrowed", "Due Date"
        });
        borrowingDateTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        borrowingDateTypeCombo.setBackground(Color.WHITE);
        borrowingDateTypeCombo.setBorder(new RoundBorder(4, new Color(206, 212, 218)));
        borrowingDateTypeCombo.setPreferredSize(new Dimension(120, 35));

        // Modern date filter button for borrowing
        borrowingDateFilterBtn = new JButton("📅 Select Date") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (selectedBorrowingDate == null) {
                    g2d.setColor(new Color(233, 236, 239));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, MODERN_BLUE,
                            getWidth(), getHeight(), new Color(26, 115, 232)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                super.paintComponent(g2d);
            }
        };
        borrowingDateFilterBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        borrowingDateFilterBtn.setForeground(selectedBorrowingDate == null ? Color.DARK_GRAY : Color.WHITE);
        borrowingDateFilterBtn.setFocusPainted(false);
        borrowingDateFilterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        borrowingDateFilterBtn.setBorder(new RoundBorder(8, new Color(206, 212, 218)));
        borrowingDateFilterBtn.setContentAreaFilled(false);
        borrowingDateFilterBtn.setOpaque(false);
        borrowingDateFilterBtn.setPreferredSize(new Dimension(140, 35));
        borrowingDateFilterBtn.addActionListener(e -> showModernDatePickerForBorrowing());

        // Clear date button for borrowing
        JButton clearBorrowingDateBtn = new JButton("Clear") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(233, 236, 239));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2d);
            }
        };
        clearBorrowingDateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBorrowingDateBtn.setForeground(Color.DARK_GRAY);
        clearBorrowingDateBtn.setFocusPainted(false);
        clearBorrowingDateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBorrowingDateBtn.setBorder(new RoundBorder(8, new Color(206, 212, 218)));
        clearBorrowingDateBtn.setContentAreaFilled(false);
        clearBorrowingDateBtn.setOpaque(false);
        clearBorrowingDateBtn.setPreferredSize(new Dimension(80, 35));
        clearBorrowingDateBtn.addActionListener(e -> {
            selectedBorrowingDate = null;
            borrowingDateFilterBtn.setText("📅 Select Date");
            borrowingDateFilterBtn.setForeground(Color.DARK_GRAY);
            borrowingDateFilterBtn.repaint();
            clearBorrowingDateBtn.repaint();
            filterBorrowingTable();
        });








        JPanel searchComponents = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchComponents.setBackground(Color.WHITE);
        searchComponents.add(borrowingSearchField);
        searchComponents.add(borrowingDateLabel);
        searchComponents.add(borrowingDateTypeCombo);
        searchComponents.add(borrowingDateFilterBtn);
        searchComponents.add(Box.createHorizontalStrut(5));
        searchComponents.add(clearBorrowingDateBtn);
        searchComponents.add(btnClearBorrowingSearch);








        searchPanel.add(new JLabel("🔍 Search:"), BorderLayout.WEST);
        searchPanel.add(searchComponents, BorderLayout.CENTER);








        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);








        JButton btnNewTransaction = createModernButton("➕ New Transaction", ACCENT_COLOR, "Create new borrowing transaction");
        btnNewTransaction.addActionListener(e -> handleLend());








        JButton btnMarkReturned = createModernButton("✅ Mark Returned", PRIMARY_COLOR, "Mark selected item as returned");
        btnMarkReturned.addActionListener(e -> handleReturn());








        buttonPanel.add(btnNewTransaction);
        buttonPanel.add(btnMarkReturned);








        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);








        // Table
        String[] borrowingColumns = {"Borrow ID", "Asset Name", "Borrower", "Date Borrowed", "Due Date", "Status", "Days Overdue", "Actions"};
        borrowingTableModel = new DefaultTableModel(borrowingColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only actions column is editable
            }








            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 7 ? JButton.class : String.class;
            }
        };








        borrowingTable = new JTable(borrowingTableModel);
        styleBorrowingTable();








        JScrollPane borrowingScroll = new JScrollPane(borrowingTable);
        borrowingScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        borrowingScroll.getViewport().setBackground(Color.WHITE);








        // Statistics panel
        JPanel statsPanel = createStatsPanel();
        borrowingStatsLabel = new JLabel("Loading borrowing records...");
        borrowingStatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        borrowingStatsLabel.setForeground(new Color(100, 100, 100));
        statsPanel.add(borrowingStatsLabel, BorderLayout.WEST);








        // Add search functionality
        borrowingSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterBorrowingTable(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterBorrowingTable(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterBorrowingTable(); }
        });








        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(borrowingScroll, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.SOUTH);








        return panel;
    }








    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));








        JPanel topPanel = createSectionPanel("📊 BORROWING HISTORY",
                "View complete history of all borrowing transactions");
        topPanel.setLayout(new BorderLayout(0, 15));








        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(Color.WHITE);








        historySearchField = createSearchField("Search history...");
        JButton btnClearHistorySearch = createIconButton("✕", "Clear search");
        btnClearHistorySearch.addActionListener(e -> {
            historySearchField.setText("");
            filterHistoryTable();
        });








        // FIXED: Use class field instead of local variable
        historyFilterCombo = new JComboBox<>(new String[]{
                "All", "Good Condition", "Damaged", "Lost", "Overdue Returns"
        });
        historyFilterCombo.setFont(FIELD_FONT);
        historyFilterCombo.setBackground(Color.WHITE);








        // DATE FILTER SECTION (copied from SecretaryPrintDocument)
        JLabel dateLabel = new JLabel("  Date:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Modern date filter button
        dateFilterBtn = new JButton("📅 Select Date") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (selectedDate == null) {
                    g2d.setColor(new Color(233, 236, 239));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, MODERN_BLUE,
                            getWidth(), getHeight(), new Color(26, 115, 232)
                    );
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
        dateFilterBtn.addActionListener(e -> showModernDatePickerForHistory());

        // Clear date button
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
            dateFilterBtn.repaint();
            clearDateBtn.repaint();
            filterHistoryTable();
        });








        JPanel searchComponents = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchComponents.setBackground(Color.WHITE);
        searchComponents.add(historySearchField);
        searchComponents.add(historyFilterCombo);
        searchComponents.add(dateLabel);
        searchComponents.add(dateFilterBtn);
        searchComponents.add(Box.createHorizontalStrut(5));
        searchComponents.add(clearDateBtn);
        searchComponents.add(btnClearHistorySearch);








        searchPanel.add(new JLabel("🔍 Search:"), BorderLayout.WEST);
        searchPanel.add(searchComponents, BorderLayout.CENTER);








        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);








        JButton btnRefreshHistory = createModernButton("🔄 Refresh", PRIMARY_COLOR, "Refresh history data");
        btnRefreshHistory.addActionListener(e -> loadHistoryData());








        JButton btnExportHistory = createModernButton("📤 Export History", INFO_COLOR, "Export history data");
        btnExportHistory.addActionListener(e -> exportHistoryData());








        buttonPanel.add(btnRefreshHistory);
        buttonPanel.add(btnExportHistory);








        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);








        // Table with additional columns for history
        String[] historyColumns = {
                "Borrow ID", "Asset Name", "Borrower", "Date Borrowed",
                "Due Date", "Date Returned", "Condition", "Remarks", "Penalty"
        };
        historyTableModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };








        historyTable = new JTable(historyTableModel);
        styleHistoryTable();








        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        historyScroll.getViewport().setBackground(Color.WHITE);








        // Statistics panel
        JPanel statsPanel = createStatsPanel();
        historyStatsLabel = new JLabel("Loading history...");
        historyStatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        historyStatsLabel.setForeground(new Color(100, 100, 100));
        statsPanel.add(historyStatsLabel, BorderLayout.WEST);








        // Add search functionality
        historySearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filterHistoryTable(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filterHistoryTable(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filterHistoryTable(); }
        });








        historyFilterCombo.addActionListener(e -> filterHistoryTable());








        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(historyScroll, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.SOUTH);








        return panel;
    }

    // =========================================================================
    //  CALENDAR IMPLEMENTATION FOR EACH TAB
    // =========================================================================

    private void showModernDatePickerForHistory() {
        showModernDatePicker(dateFilterBtn, (date) -> {
            selectedDate = date;
            filterHistoryTable();
        });
    }

    private void showModernDatePickerForAsset() {
        showModernDatePicker(assetDateFilterBtn, (date) -> {
            selectedAssetDate = date;
            filterAssetTable();
        });
    }

    private void showModernDatePickerForBorrowing() {
        showModernDatePicker(borrowingDateFilterBtn, (date) -> {
            selectedBorrowingDate = date;
            filterBorrowingTable();
        });
    }

    private void showModernDatePicker(JButton targetButton, java.util.function.Consumer<java.util.Date> dateSetter) {
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
        if (targetButton == dateFilterBtn && selectedDate != null) {
            calendar.setTime(selectedDate);
        } else if (targetButton == assetDateFilterBtn && selectedAssetDate != null) {
            calendar.setTime(selectedAssetDate);
        } else if (targetButton == borrowingDateFilterBtn && selectedBorrowingDate != null) {
            calendar.setTime(selectedBorrowingDate);
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
                JButton dayBtn = createDayButton(day, calendar, today, dateDialog, targetButton, dateSetter);
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

        // Today button - FIXED: Use java.util.Date
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
            calendar.setTime(new java.util.Date()); // FIXED: Use java.util.Date
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
            // Use the current calendar date if no specific day was selected
            if (targetButton == dateFilterBtn && selectedDate == null) {
                selectedDate = calendar.getTime();
            } else if (targetButton == assetDateFilterBtn && selectedAssetDate == null) {
                selectedAssetDate = calendar.getTime();
            } else if (targetButton == borrowingDateFilterBtn && selectedBorrowingDate == null) {
                selectedBorrowingDate = calendar.getTime();
            }

            // Update the button text
            java.util.Date dateToUse = targetButton == dateFilterBtn ? selectedDate :
                    targetButton == assetDateFilterBtn ? selectedAssetDate :
                            selectedBorrowingDate;

            if (dateToUse != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                targetButton.setText("📅 " + sdf.format(dateToUse));
                targetButton.setForeground(Color.WHITE);
                targetButton.repaint();
                dateDialog.dispose();

                // Call the appropriate filter method
                if (targetButton == dateFilterBtn) {
                    filterHistoryTable();
                } else if (targetButton == assetDateFilterBtn) {
                    filterAssetTable();
                } else if (targetButton == borrowingDateFilterBtn) {
                    filterBorrowingTable();
                }
            }
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

    private JButton createDayButton(int day, Calendar calendar, Calendar today, JDialog dateDialog,
                                    JButton targetButton, java.util.function.Consumer<java.util.Date> dateSetter) {
        // Determine if this day is selected
        java.util.Date currentSelectedDate = null;
        if (targetButton == dateFilterBtn) {
            currentSelectedDate = selectedDate;
        } else if (targetButton == assetDateFilterBtn) {
            currentSelectedDate = selectedAssetDate;
        } else if (targetButton == borrowingDateFilterBtn) {
            currentSelectedDate = selectedBorrowingDate;
        }

        final boolean isSelectedForThisButton;
        if (currentSelectedDate != null) {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(currentSelectedDate);
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
            // FIXED: Use java.util.Date
            java.util.Date newDate = calendar.getTime();

            // Set the date using the provided consumer
            dateSetter.accept(newDate);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            targetButton.setText("📅 " + sdf.format(newDate));
            targetButton.setForeground(Color.WHITE);
            targetButton.repaint();
            dateDialog.dispose();

            // Call the appropriate filter method
            if (targetButton == dateFilterBtn) {
                filterHistoryTable();
            } else if (targetButton == assetDateFilterBtn) {
                filterAssetTable();
            } else if (targetButton == borrowingDateFilterBtn) {
                filterBorrowingTable();
            }
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
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
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








    private JPanel createSectionPanel(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(15, 20, 15, 20)
        ));








        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SECTION_FONT);
        titleLabel.setForeground(TABLE_HEADER_COLOR);








        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        subtitleLabel.setBorder(new EmptyBorder(5, 0, 0, 0));








        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);








        panel.add(titlePanel, BorderLayout.WEST);
        return panel;
    }








    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        return panel;
    }








    private JTextField createSearchField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 35, 8, 10)
        ));
        field.setPreferredSize(new Dimension(300, 35));
        field.putClientProperty("JTextField.placeholderText", placeholder);








        // Add search icon
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchIcon.setBorder(new EmptyBorder(0, 10, 0, 0));
        searchIcon.setForeground(new Color(150, 150, 150));
        wrapper.add(searchIcon, BorderLayout.WEST);
        wrapper.add(field, BorderLayout.CENTER);








        return field;
    }








    private JButton createIconButton(String icon, String tooltip) {
        JButton button = new JButton(icon);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(35, 35));
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(new Color(100, 100, 100));
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        return button;
    }








    private JButton createModernButton(String text, Color bgColor, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setToolTipText(tooltip);








        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            Color original = bgColor;
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(original.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(original);
            }
        });








        return button;
    }








    private void styleAssetTable() {
        styleTable(assetTable);
        TableColumnModel columnModel = assetTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);  // ID
        columnModel.getColumn(1).setPreferredWidth(200); // Item Name
        columnModel.getColumn(2).setPreferredWidth(120); // Property No
        columnModel.getColumn(3).setPreferredWidth(100); // Date Acquired
        columnModel.getColumn(4).setPreferredWidth(80);  // Status
        columnModel.getColumn(5).setPreferredWidth(100); // Value
        columnModel.getColumn(6).setPreferredWidth(120); // Location
        columnModel.getColumn(7).setPreferredWidth(150); // Custodian
    }








    private void styleBorrowingTable() {
        styleTable(borrowingTable);
        TableColumnModel columnModel = borrowingTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);   // Borrow ID
        columnModel.getColumn(1).setPreferredWidth(200);  // Asset Name
        columnModel.getColumn(2).setPreferredWidth(150);  // Borrower
        columnModel.getColumn(3).setPreferredWidth(100);  // Date Borrowed
        columnModel.getColumn(4).setPreferredWidth(100);  // Due Date
        columnModel.getColumn(5).setPreferredWidth(80);   // Status
        columnModel.getColumn(6).setPreferredWidth(80);   // Days Overdue
        columnModel.getColumn(7).setPreferredWidth(100);  // Actions








        // Add button renderer and editor
        columnModel.getColumn(7).setCellRenderer(new ButtonRenderer());
        columnModel.getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));
    }








    private void styleHistoryTable() {
        styleTable(historyTable);
        TableColumnModel columnModel = historyTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);   // Borrow ID
        columnModel.getColumn(1).setPreferredWidth(180);  // Asset Name
        columnModel.getColumn(2).setPreferredWidth(150);  // Borrower
        columnModel.getColumn(3).setPreferredWidth(100);  // Date Borrowed
        columnModel.getColumn(4).setPreferredWidth(100);  // Due Date
        columnModel.getColumn(5).setPreferredWidth(100);  // Date Returned
        columnModel.getColumn(6).setPreferredWidth(80);   // Condition
        columnModel.getColumn(7).setPreferredWidth(200);  // Remarks
        columnModel.getColumn(8).setPreferredWidth(80);   // Penalty
    }








    private void styleTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(35);
        table.setShowGrid(true);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(220, 235, 247));
        table.setSelectionForeground(Color.BLACK);
        table.setIntercellSpacing(new Dimension(0, 0));








        JTableHeader header = table.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
    }








    // =========================================================================
    // DATA LOADING METHODS
    // =========================================================================
    private void loadAssetData() {
        new SwingWorker<List<BarangayAsset>, Void>() {
            @Override
            protected List<BarangayAsset> doInBackground() throws Exception {
                return new BarangayAssetDAO().getAllAssets();
            }


            @Override
            protected void done() {
                try {
                    List<BarangayAsset> list = get();
                    assetTableModel.setRowCount(0);


                    for (BarangayAsset a : list) {
                        // Format value with comma as thousands separator
                        DecimalFormat formatter = new DecimalFormat("#,###.00");
                        String formattedValue = "₱" + formatter.format(a.getValue());


                        assetTableModel.addRow(new Object[]{
                                a.getAssetId(),
                                a.getItemName(),
                                a.getPropertyNumber(),
                                a.getDateAcquired(),
                                a.getStatus(),
                                formattedValue,  // Use formatted value here
                                a.getLocation(),
                                a.getCustodian()
                        });
                    }


                    double totalValue = list.stream().mapToDouble(BarangayAsset::getValue).sum();
                    long availableAssets = list.stream().filter(a -> "Good".equals(a.getStatus())).count();


                    // Format total value in stats label too
                    DecimalFormat formatter = new DecimalFormat("#,###.00");
                    String formattedTotalValue = "₱" + formatter.format(totalValue);


                    assetStatsLabel.setText(String.format(
                            "Total Assets: %d | Available: %d | Total Value: %s",
                            list.size(), availableAssets, formattedTotalValue
                    ));
                    updateLastAssetMaxId();
                } catch (Exception e) {
                    e.printStackTrace();
                    assetStatsLabel.setText("Error loading assets");
                }
            }
        }.execute();
    }
    private double parseFormattedValue(String valueStr) {
        try {
            // Remove currency symbol, commas, and whitespace
            String cleaned = valueStr.replace("₱", "").replace(",", "").trim();
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            // Try parsing without cleaning in case it's already a plain number
            return Double.parseDouble(valueStr);
        }
    }








    private void loadBorrowingData() {
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() throws Exception {
                return new BorrowingDAO().getActiveBorrows();
            }








            @Override
            protected void done() {
                try {
                    List<BorrowRecord> list = get();
                    borrowingTableModel.setRowCount(0);








                    for (BorrowRecord r : list) {
                        // Calculate days overdue
                        LocalDate dueDate = r.getExpectedReturnDate().toLocalDate();
                        LocalDate today = LocalDate.now();
                        int daysOverdue = today.isAfter(dueDate) ?
                                (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, today) : 0;








                        borrowingTableModel.addRow(new Object[]{
                                r.getBorrowId(),
                                r.getAssetName(),
                                r.getBorrowerName(),
                                r.getDateBorrowed(),
                                r.getExpectedReturnDate(),
                                r.getStatus(),
                                daysOverdue > 0 ? daysOverdue + " days" : "On time",
                                "Return"
                        });
                    }








                    long overdueCount = list.stream()
                            .filter(r -> LocalDate.now().isAfter(r.getExpectedReturnDate().toLocalDate()))
                            .count();








                    borrowingStatsLabel.setText(String.format(
                            "Active Borrows: %d | Overdue: %d",
                            list.size(), overdueCount
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                    borrowingStatsLabel.setText("Error loading borrowing records");
                }
            }
        }.execute();
    }








    private void loadHistoryData() {
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() throws Exception {
                try {
                    System.out.println("Loading borrowing history...");
                    BorrowingDAO dao = new BorrowingDAO();








                    // Try to get all history
                    List<BorrowRecord> history = dao.getAllBorrowHistory();
                    System.out.println("Found " + (history != null ? history.size() : 0) + " history records");
                    return history;
                } catch (Exception e) {
                    System.err.println("Error loading history: " + e.getMessage());
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }








            @Override
            protected void done() {
                try {
                    List<BorrowRecord> list = get();
                    historyTableModel.setRowCount(0);








                    if (list == null || list.isEmpty()) {
                        historyStatsLabel.setText("No borrowing history found");
                        return;
                    }








                    System.out.println("Displaying " + list.size() + " history records");








                    for (BorrowRecord r : list) {
                        // Extract condition from remarks
                        String condition = "Good";
                        String remarks = r.getRemarks() != null ? r.getRemarks() : "";








                        if (remarks.toLowerCase().contains("damaged")) {
                            condition = "Damaged";
                        } else if (remarks.toLowerCase().contains("lost") || remarks.toLowerCase().contains("stolen")) {
                            condition = "Lost/Stolen";
                        } else if (remarks.toLowerCase().contains("minor")) {
                            condition = "Minor Damage";
                        } else if (remarks.toLowerCase().contains("major")) {
                            condition = "Major Damage";
                        }








                        // Calculate penalty if overdue
                        double penalty = 0.0;
                        if (r.getDateReturned() != null && r.getExpectedReturnDate() != null) {
                            LocalDate returnDate = r.getDateReturned().toLocalDate();
                            LocalDate dueDate = r.getExpectedReturnDate().toLocalDate();
                            if (returnDate.isAfter(dueDate)) {
                                long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
                                penalty = daysLate * 50.0; // 50 pesos per day late
                            }
                        }








                        // Format dates
                        String dateReturnedStr = r.getDateReturned() != null ?
                                r.getDateReturned().toString() : "Not Returned";
                        String dueDateStr = r.getExpectedReturnDate() != null ?
                                r.getExpectedReturnDate().toString() : "N/A";
                        String borrowedDateStr = r.getDateBorrowed() != null ?
                                r.getDateBorrowed().toString() : "N/A";








                        historyTableModel.addRow(new Object[]{
                                r.getBorrowId(),
                                r.getAssetName() != null ? r.getAssetName() : "Unknown",
                                r.getBorrowerName() != null ? r.getBorrowerName() : "Unknown",
                                borrowedDateStr,
                                dueDateStr,
                                dateReturnedStr,
                                condition,
                                remarks,
                                penalty > 0 ? String.format("₱%.2f", penalty) : "None"
                        });
                    }








                    historyStatsLabel.setText(String.format(
                            "Total History Records: %d",
                            list.size()
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                    historyStatsLabel.setText("Error loading history: " + e.getMessage());
                }
            }
        }.execute();
    }
    // =========================================================================
    // FILTER METHODS
    // =========================================================================
    private void filterAssetTable() {
        String searchText = assetSearchField.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(assetTableModel);
        assetTable.setRowSorter(sorter);

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Text search with regex for all columns
        if (searchText != null && !searchText.trim().isEmpty()) {
            String regexPattern = "(?i)" + java.util.regex.Pattern.quote(searchText.trim());
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object value = entry.getValue(i);
                        if (value != null && value.toString().toLowerCase().contains(searchText.trim().toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        // Date filter for assets (Date Acquired column - index 3)
        if (selectedAssetDate != null) {
            final java.util.Date finalSelectedDate = selectedAssetDate;
            final String dateType = (String) assetDateTypeCombo.getSelectedItem();

            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        // Determine which column to check based on date type
                        int dateColumnIndex = 3; // Default: Date Acquired (index 3)

                        Object val = entry.getValue(dateColumnIndex);
                        if (!(val instanceof String)) return false;

                        String dateStr = (String) val;
                        if (dateStr.equals("N/A") || dateStr.isEmpty()) return false;

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDateStr = sdf.format(finalSelectedDate);

                        // Compare date strings (format is yyyy-MM-dd)
                        return dateStr.startsWith(selectedDateStr);

                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));

        // Update stats label
        int count = assetTable.getRowCount();
        assetStatsLabel.setText("Filtered Assets: " + count);
    }








    private void filterBorrowingTable() {
        String searchText = borrowingSearchField.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(borrowingTableModel);
        borrowingTable.setRowSorter(sorter);

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Text search with regex for all columns
        if (searchText != null && !searchText.trim().isEmpty()) {
            String regexPattern = "(?i)" + java.util.regex.Pattern.quote(searchText.trim());
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object value = entry.getValue(i);
                        if (value != null && value.toString().toLowerCase().contains(searchText.trim().toLowerCase())) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        // Date filter for borrowing
        if (selectedBorrowingDate != null) {
            final java.util.Date finalSelectedDate = selectedBorrowingDate;
            final String dateType = (String) borrowingDateTypeCombo.getSelectedItem();

            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        // Determine which column to check based on date type
                        int dateColumnIndex = dateType.equals("Date Borrowed") ? 3 : 4; // 3=Date Borrowed, 4=Due Date

                        Object val = entry.getValue(dateColumnIndex);
                        if (!(val instanceof String)) return false;

                        String dateStr = (String) val;
                        if (dateStr.equals("N/A") || dateStr.isEmpty()) return false;

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDateStr = sdf.format(finalSelectedDate);

                        // Compare date strings (format is yyyy-MM-dd)
                        return dateStr.startsWith(selectedDateStr);

                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));

        // Update stats label
        int count = borrowingTable.getRowCount();
        borrowingStatsLabel.setText("Filtered Transactions: " + count);
    }








    private void filterHistoryTable() {
        String text = historySearchField.getText().toLowerCase();
        String condition = (String) historyFilterCombo.getSelectedItem(); // FIXED: Now accessible
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(historyTableModel);
        historyTable.setRowSorter(sorter);

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Text search with regex for all columns
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

        // Condition filter with exact match regex
        if (condition != null && !condition.equals("All")) {
            filters.add(RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(condition) + "$", 6));
        }

        // Date filter using selectedDate (for Date Borrowed column - index 3)
        if (selectedDate != null) {
            final java.util.Date finalSelectedDate = selectedDate;
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(3); // Date Borrowed column
                        if (!(val instanceof String)) return false;

                        String dateStr = (String) val;
                        if (dateStr.equals("N/A") || dateStr.isEmpty()) return false;

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDateStr = sdf.format(finalSelectedDate);

                        // Compare date strings (format is yyyy-MM-dd)
                        return dateStr.startsWith(selectedDateStr);

                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        updateHistoryRecordCount();
    }

    private void updateHistoryRecordCount() {
        int count = historyTable.getRowCount();
        historyStatsLabel.setText("Total Records: " + count);
    }








    // =========================================================================
    // ASSET CRUD ACTIONS WITH REGEX VALIDATION
    // =========================================================================
    private void handleAddAsset() {
        showAssetDialog(null, "Register New Asset");
    }








    private void handleEditAsset() {
        int selectedRow = assetTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an asset to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }








        int modelRow = assetTable.convertRowIndexToModel(selectedRow);
        int assetId = (int) assetTableModel.getValueAt(modelRow, 0);
        BarangayAsset asset = new BarangayAssetDAO().getAssetById(assetId);








        if (asset != null) {
            showAssetDialog(asset, "Update Asset Details");
        } else {
            JOptionPane.showMessageDialog(this, "Error: Could not fetch asset details.");
        }
    }








    private void handleDeleteAsset() {
        int selectedRow = assetTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an asset to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }








        int modelRow = assetTable.convertRowIndexToModel(selectedRow);
        int id = (int) assetTableModel.getValueAt(modelRow, 0);
        String name = (String) assetTableModel.getValueAt(modelRow, 1);








        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete asset: " + name + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);








        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = new BarangayAssetDAO().deleteAsset(id);
            if (success) {
                JOptionPane.showMessageDialog(this, "Asset deleted successfully.");
                loadAssetData();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting asset.");
            }
        }
    }








    // =========================================================================
    // BORROWING ACTIONS WITH ENHANCED RETURN PROCESS
    // =========================================================================
    private void handleLend() {
        showLendDialog();
    }








    private void handleReturn() {
        int selectedRow = borrowingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a transaction to mark as returned.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }








        int modelRow = borrowingTable.convertRowIndexToModel(selectedRow);
        int borrowId = (int) borrowingTableModel.getValueAt(modelRow, 0);








        BorrowingDAO dao = new BorrowingDAO();
        BorrowRecord record = dao.getBorrowRecordById(borrowId);








        if (record == null) {
            JOptionPane.showMessageDialog(this, "Error: Could not find borrowing record.");
            return;
        }








        // Show enhanced return dialog with condition selection
        showReturnDialog(record);
    }








    private void showReturnDialog(BorrowRecord record) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Return Asset - Condition Report", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG_COLOR);








        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);








        // Header with asset info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));








        JLabel titleLabel = new JLabel("📦 Asset Return - Condition Report");
        titleLabel.setFont(SECTION_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);








        JLabel assetInfo = new JLabel(String.format(
                "<html><b>Asset:</b> %s<br><b>Borrower:</b> %s<br><b>Due Date:</b> %s</html>",
                record.getAssetName(),
                record.getBorrowerName(),
                record.getExpectedReturnDate()
        ));
        assetInfo.setFont(LABEL_FONT);
        assetInfo.setBorder(new EmptyBorder(5, 0, 0, 0));








        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(assetInfo, BorderLayout.CENTER);








        // Condition selection
        JPanel conditionPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        conditionPanel.setBackground(Color.WHITE);
        conditionPanel.setBorder(BorderFactory.createTitledBorder("Select Condition"));








        ButtonGroup conditionGroup = new ButtonGroup();
        JRadioButton rbGood = new JRadioButton("✅ Good Condition - No issues");
        JRadioButton rbMinor = new JRadioButton("⚠️ Minor Damage - Needs minor repair");
        JRadioButton rbMajor = new JRadioButton("🔧 Major Damage - Needs major repair");
        JRadioButton rbLost = new JRadioButton("❌ Lost/Stolen - Asset cannot be recovered");
        JRadioButton rbOther = new JRadioButton("📝 Other - Specify in remarks");








        rbGood.setSelected(true);








        conditionGroup.add(rbGood);
        conditionGroup.add(rbMinor);
        conditionGroup.add(rbMajor);
        conditionGroup.add(rbLost);
        conditionGroup.add(rbOther);








        conditionPanel.add(rbGood);
        conditionPanel.add(rbMinor);
        conditionPanel.add(rbMajor);
        conditionPanel.add(rbLost);
        conditionPanel.add(rbOther);








        // Remarks field
        JPanel remarksPanel = new JPanel(new BorderLayout(5, 5));
        remarksPanel.setBackground(Color.WHITE);
        remarksPanel.setBorder(BorderFactory.createTitledBorder("Additional Remarks"));








        JTextArea remarksArea = new JTextArea(3, 30);
        remarksArea.setFont(FIELD_FONT);
        remarksArea.setBorder(new LineBorder(BORDER_COLOR, 1));
        remarksArea.setLineWrap(true);
        remarksArea.setWrapStyleWord(true);
        remarksArea.setText("Returned in good condition.");








        JScrollPane remarksScroll = new JScrollPane(remarksArea);
        remarksPanel.add(remarksScroll, BorderLayout.CENTER);








        // Penalty calculation (if overdue)
        JPanel penaltyPanel = new JPanel(new BorderLayout());
        penaltyPanel.setBackground(Color.WHITE);








        LocalDate today = LocalDate.now();
        LocalDate dueDate = record.getExpectedReturnDate().toLocalDate();
        if (today.isAfter(dueDate)) {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
            double penalty = daysLate * 50.0; // 50 pesos per day








            JLabel penaltyLabel = new JLabel(String.format(
                    "<html><font color='red'><b>⚠️ Overdue by %d days</b><br>Penalty Fee: ₱%.2f</font></html>",
                    daysLate, penalty
            ));
            penaltyLabel.setFont(WARNING_FONT);
            penaltyPanel.add(penaltyLabel, BorderLayout.CENTER);
            penaltyPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        }








        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));








        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());








        JButton btnConfirm = createModernButton("Confirm Return", ACCENT_COLOR, "Confirm return with selected condition");
        btnConfirm.addActionListener(e -> {
            String condition;
            if (rbGood.isSelected()) condition = "Good";
            else if (rbMinor.isSelected()) condition = "Minor Damage";
            else if (rbMajor.isSelected()) condition = "Major Damage";
            else if (rbLost.isSelected()) condition = "Lost/Stolen";
            else condition = "Other";








            String remarks = condition + " - " + remarksArea.getText().trim();








            boolean success = new BorrowingDAO().returnItem(
                    record.getBorrowId(),
                    record.getAssetId(),
                    Date.valueOf(LocalDate.now()),
                    remarks
            );








            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        "Asset returned successfully.\nCondition: " + condition,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);








                try {
                    // FIXED: Truncate the log message to avoid data truncation error
                    String logMessage = "Asset Returned: " + record.getAssetName() + " (Condition: " + condition + ")";
                    if (logMessage.length() > 100) {
                        logMessage = logMessage.substring(0, 100);
                    }
                    new SystemLogDAO().addLog(logMessage, "Admin", 1);
                } catch (Exception ex) {
                    System.err.println("Log error: " + ex.getMessage());
                    // Don't fail the return operation if logging fails
                }








                loadBorrowingData();
                loadAssetData();
                loadHistoryData();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Error updating database.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });








        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);








        // Add all panels
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(conditionPanel, BorderLayout.CENTER);
        mainPanel.add(remarksPanel, BorderLayout.SOUTH);








        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        contentPanel.add(penaltyPanel, BorderLayout.SOUTH);








        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }








    // =========================================================================
    // ASSET DIALOG WITH REGEX VALIDATION AND WARNING FRAME - FIXED VERSION
    // =========================================================================
    private void showAssetDialog(BarangayAsset existing, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(850, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG_COLOR);


        // Create warning panel (initially hidden)
        JPanel warningPanel = createWarningPanel();
        warningPanel.setVisible(false);


        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));


        // Create all input fields
        List<JComponent> requiredFields = new ArrayList<>();


        // Basic Information
        JTextField txtName = createStyledTextField(existing != null ? existing.getItemName() : "");
        txtName.setToolTipText("Required. Alphanumeric, spaces, hyphens, periods, commas, ampersands, parentheses only");
        requiredFields.add(txtName);


        JTextField txtPropCode = createStyledTextField(existing != null ? existing.getPropertyCode() : "");
        txtPropCode.setToolTipText("Optional. Uppercase letters, numbers, hyphens, slashes only");


        JTextField txtPropNo = createStyledTextField(existing != null ? existing.getPropertyNumber() :
                "PROP-" + (System.currentTimeMillis() % 10000));
        txtPropNo.setToolTipText("Required. Uppercase letters, numbers, hyphens, slashes only");
        requiredFields.add(txtPropNo);


        JTextField txtSerialNo = createStyledTextField(existing != null ? existing.getSerialNumber() : "");
        txtSerialNo.setToolTipText("Optional. Alphanumeric, hyphens, spaces only");


        // Brand & Model
        JTextField txtBrand = createStyledTextField(existing != null ? existing.getBrand() : "");
        txtBrand.setToolTipText("Optional. Alphanumeric, spaces, hyphens, periods, commas, ampersands, parentheses only");


        JTextField txtModel = createStyledTextField(existing != null ? existing.getModel() : "");
        txtModel.setToolTipText("Optional. Alphanumeric, spaces, hyphens, periods, commas, ampersands, parentheses only");


        // Financial Information
        String initialValue = existing != null ? formatCurrency(existing.getValue()) : "₱0.00";
        JTextField txtValue = createStyledTextField(initialValue);
        txtValue.setToolTipText("Required. Format: ₱1,000.00 or 1000.00");
        requiredFields.add(txtValue);


        JTextField txtUsefulLife = createStyledTextField(existing != null ?
                String.valueOf(existing.getUsefulLifeYears()) : "5");
        txtUsefulLife.setToolTipText("Required. Whole numbers only");
        requiredFields.add(txtUsefulLife);








        // Financial Information










        String[] fundSources = {"Barangay Fund", "National Fund", "Donation", "Provincial Fund", "Other"};
        JComboBox<String> cmbFundSource = new JComboBox<>(fundSources);
        cmbFundSource.setBackground(Color.WHITE);
        cmbFundSource.setFont(FIELD_FONT);
        if (existing != null && existing.getFundSource() != null) {
            cmbFundSource.setSelectedItem(existing.getFundSource());
        }








        // Dates
        JTextField txtDateAcquired = createStyledTextField("");
        if (existing != null && existing.getDateAcquired() != null) {
            txtDateAcquired.setText(existing.getDateAcquired().toString());
        }
        txtDateAcquired.setToolTipText("Required. Format: YYYY-MM-DD");
        requiredFields.add(txtDateAcquired);








        JTextField txtPurchaseDate = createStyledTextField("");
        if (existing != null && existing.getPurchaseDate() != null) {
            txtPurchaseDate.setText(existing.getPurchaseDate().toString());
        }
        txtPurchaseDate.setToolTipText("Optional. Format: YYYY-MM-DD");








        // Status & Location
        String[] statuses = {"Good", "Damaged", "Lost", "For Repair", "Disposed", "Borrowed"};
        JComboBox<String> cmbStatus = new JComboBox<>(statuses);
        cmbStatus.setBackground(Color.WHITE);
        cmbStatus.setFont(FIELD_FONT);
        if (existing != null) {
            cmbStatus.setSelectedItem(existing.getStatus());
        }








        JTextField txtLocation = createStyledTextField(existing != null ? existing.getLocation() : "Barangay Hall");
        txtLocation.setToolTipText("Required. Letters, spaces, periods, commas only");
        requiredFields.add(txtLocation);








        JTextField txtCustodian = createStyledTextField(existing != null ? existing.getCustodian() : "Barangay Treasurer");
        txtCustodian.setToolTipText("Required. Letters, spaces, periods, commas only");
        requiredFields.add(txtCustodian);








        // Add input validation
        addRegexValidation(txtName, NAME_REGEX, "Item Name");
        addRegexValidation(txtPropCode, PROPERTY_REGEX, "Property Code");
        addRegexValidation(txtPropNo, PROPERTY_REGEX, "Property Number");
        addRegexValidation(txtSerialNo, SERIAL_REGEX, "Serial Number");
        addRegexValidation(txtBrand, NAME_REGEX, "Brand");
        addRegexValidation(txtModel, NAME_REGEX, "Model");
        addRegexValidation(txtValue, "^₱?[0-9,]+(\\.[0-9]{2})?$", "Value");
        addRegexValidation(txtUsefulLife, INTEGER_REGEX, "Useful Life");
        addRegexValidation(txtDateAcquired, DATE_REGEX, "Date Acquired");
        addRegexValidation(txtPurchaseDate, "^$|" + DATE_REGEX, "Purchase Date");
        addRegexValidation(txtLocation, CUSTODIAN_REGEX, "Location");
        addRegexValidation(txtCustodian, CUSTODIAN_REGEX, "Custodian");








        // Create form sections
        formPanel.add(createFormSection("Basic Information", new JComponent[][]{
                {new JLabel("Item Name*:"), txtName},
                {new JLabel("Property Code:"), txtPropCode},
                {new JLabel("Property Number*:"), txtPropNo},
                {new JLabel("Serial Number:"), txtSerialNo}
        }));








        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createFormSection("Specifications", new JComponent[][]{
                {new JLabel("Brand:"), txtBrand},
                {new JLabel("Model:"), txtModel}
        }));








        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createFormSection("Financial Information", new JComponent[][]{
                {new JLabel("Value (₱)*:"), txtValue},
                {new JLabel("Useful Life (Years)*:"), txtUsefulLife},
                {new JLabel("Fund Source:"), cmbFundSource}
        }));








        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createFormSection("Date Information", new JComponent[][]{
                {new JLabel("Date Acquired*:"), createDatePanel(txtDateAcquired)},
                {new JLabel("Purchase Date:"), createDatePanel(txtPurchaseDate)}
        }));








        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(createFormSection("Status & Location", new JComponent[][]{
                {new JLabel("Status:"), cmbStatus},
                {new JLabel("Location*:"), txtLocation},
                {new JLabel("Custodian*:"), txtCustodian}
        }));








        // Save button with validation
        JButton btnSave = createModernButton(existing == null ? "Save Asset" : "Update Asset",
                ACCENT_COLOR, "Save asset details");
        btnSave.addActionListener(e -> {
            // Validate all fields
            List<String> errors = validateAssetForm(
                    txtName, txtPropNo, txtValue, txtUsefulLife,
                    txtDateAcquired, txtPurchaseDate, txtLocation, txtCustodian
            );








            if (!errors.isEmpty()) {
                showWarningFrame(errors, warningPanel);
                return;
            }








            // Hide warning if validation passes
            warningPanel.setVisible(false);








            // Save the asset - using the corrected method signature
            saveAsset(existing, dialog, txtName, txtPropCode, txtPropNo, txtSerialNo,
                    txtBrand, txtModel, txtValue, txtUsefulLife, cmbFundSource,
                    txtDateAcquired, txtPurchaseDate, cmbStatus, txtLocation, txtCustodian);
        });








        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        btnPanel.add(warningPanel, BorderLayout.NORTH);








        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonContainer.setBackground(Color.WHITE);
        buttonContainer.add(btnSave);
        btnPanel.add(buttonContainer, BorderLayout.SOUTH);








        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);








        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }




    private String formatCurrency(double value) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return "₱" + formatter.format(value);
    }


    // Fixed saveAsset method with correct parameters
    private void saveAsset(BarangayAsset existing, JDialog dialog,
                           JTextField txtName, JTextField txtPropCode, JTextField txtPropNo, JTextField txtSerialNo,
                           JTextField txtBrand, JTextField txtModel, JTextField txtValue, JTextField txtUsefulLife,
                           JComboBox<String> cmbFundSource, JTextField txtDateAcquired, JTextField txtPurchaseDate,
                           JComboBox<String> cmbStatus, JTextField txtLocation, JTextField txtCustodian) {
        try {
            // Create asset object
            BarangayAsset asset = new BarangayAsset();
            asset.setItemName(txtName.getText().trim());
            asset.setPropertyCode(txtPropCode.getText().trim());
            asset.setPropertyNumber(txtPropNo.getText().trim());
            asset.setSerialNumber(txtSerialNo.getText().trim());
            asset.setBrand(txtBrand.getText().trim());
            asset.setModel(txtModel.getText().trim());
            asset.setValue(parseFormattedValue(txtValue.getText()));
            asset.setUsefulLifeYears(Integer.parseInt(txtUsefulLife.getText()));
            asset.setFundSource(cmbFundSource.getSelectedItem().toString());








            // Parse dates
            if (!txtDateAcquired.getText().isEmpty()) {
                asset.setDateAcquired(Date.valueOf(txtDateAcquired.getText()));
            }
            if (!txtPurchaseDate.getText().isEmpty()) {
                asset.setPurchaseDate(Date.valueOf(txtPurchaseDate.getText()));
            }








            asset.setStatus(cmbStatus.getSelectedItem().toString());
            asset.setLocation(txtLocation.getText().trim());
            asset.setCustodian(txtCustodian.getText().trim());








            if (existing != null) {
                asset.setAssetId(existing.getAssetId());
            }








            boolean success;
            if (existing == null) {
                success = new BarangayAssetDAO().addAsset(asset);
                if (success) {
                    try {
                        new SystemLogDAO().addLog("Added Asset: " + asset.getItemName(), "Admin", 1);
                    } catch (Exception ex) {}
                }
            } else {
                success = new BarangayAssetDAO().updateAsset(asset);
            }








            if (success) {
                JOptionPane.showMessageDialog(dialog,
                        existing == null ? "Asset added successfully!" : "Asset updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAssetData();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Database error. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }








    private JPanel createWarningPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(255, 243, 205)); // Light yellow
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(255, 193, 7), 2), // Yellow border
                new EmptyBorder(10, 15, 10, 15)
        ));








        JLabel warningIcon = new JLabel("⚠️");
        warningIcon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        warningIcon.setForeground(new Color(255, 193, 7));








        JLabel warningLabel = new JLabel("Please fix the following errors:");
        warningLabel.setFont(WARNING_FONT);
        warningLabel.setForeground(new Color(102, 77, 3));








        JPanel textPanel = new JPanel(new BorderLayout(5, 0));
        textPanel.setBackground(new Color(255, 243, 205));
        textPanel.add(warningIcon, BorderLayout.WEST);
        textPanel.add(warningLabel, BorderLayout.CENTER);








        panel.add(textPanel, BorderLayout.NORTH);








        JTextArea errorArea = new JTextArea();
        errorArea.setEditable(false);
        errorArea.setBackground(new Color(255, 243, 205));
        errorArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorArea.setForeground(WARNING_COLOR);
        errorArea.setBorder(new EmptyBorder(5, 25, 0, 0));








        panel.add(new JScrollPane(errorArea), BorderLayout.CENTER);








        // Store error area reference
        panel.putClientProperty("errorArea", errorArea);








        return panel;
    }








    private void showWarningFrame(List<String> errors, JPanel warningPanel) {
        JTextArea errorArea = (JTextArea) warningPanel.getClientProperty("errorArea");
        if (errorArea != null) {
            StringBuilder errorText = new StringBuilder();
            for (String error : errors) {
                errorText.append("• ").append(error).append("\n");
            }
            errorArea.setText(errorText.toString());
            warningPanel.setVisible(true);








            // Scroll to top
            errorArea.setCaretPosition(0);
        }
    }








    private List<String> validateAssetForm(JTextField... fields) {
        List<String> errors = new ArrayList<>();








        // Check required fields
        if (fields[0].getText().trim().isEmpty()) errors.add("Item Name is required");
        if (fields[1].getText().trim().isEmpty()) errors.add("Property Number is required");
        if (fields[2].getText().trim().isEmpty()) errors.add("Value is required");
        if (fields[3].getText().trim().isEmpty()) errors.add("Useful Life is required");
        if (fields[4].getText().trim().isEmpty()) errors.add("Date Acquired is required");
        if (fields[6].getText().trim().isEmpty()) errors.add("Location is required");
        if (fields[7].getText().trim().isEmpty()) errors.add("Custodian is required");








        // Regex validation
        if (!fields[0].getText().matches(NAME_REGEX))
            errors.add("Item Name contains invalid characters");
        if (!fields[1].getText().matches(PROPERTY_REGEX))
            errors.add("Property Number format invalid (use A-Z, 0-9, -, / only)");
        if (!fields[2].getText().matches(DECIMAL_REGEX))
            errors.add("Value must be a valid number (e.g., 1000 or 1500.50)");
        if (!fields[3].getText().matches(INTEGER_REGEX))
            errors.add("Useful Life must be a whole number");
        if (!fields[4].getText().matches(DATE_REGEX))
            errors.add("Date Acquired must be in YYYY-MM-DD format");
        if (!fields[5].getText().isEmpty() && !fields[5].getText().matches(DATE_REGEX))
            errors.add("Purchase Date must be in YYYY-MM-DD format or empty");
        if (!fields[6].getText().matches(CUSTODIAN_REGEX))
            errors.add("Location contains invalid characters");
        if (!fields[7].getText().matches(CUSTODIAN_REGEX))
            errors.add("Custodian contains invalid characters");








        // Numeric range validation
        try {
            // Parse the formatted value
            double value = parseFormattedValue(fields[2].getText());
            if (value < 0) errors.add("Value cannot be negative");
            if (value > 10000000) errors.add("Value is too large (max: 10,000,000)");
        } catch (NumberFormatException e) {
            errors.add("Invalid value format. Use format like: ₱1,000.00 or 1000.00");
        }








        try {
            int usefulLife = Integer.parseInt(fields[3].getText());
            if (usefulLife < 1) errors.add("Useful Life must be at least 1 year");
            if (usefulLife > 50) errors.add("Useful Life cannot exceed 50 years");
        } catch (NumberFormatException e) {
            errors.add("Invalid useful life format");
        }








        return errors;
    }








    private void addRegexValidation(JTextField field, String regex, String fieldName) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = field.getText().trim();
                if (!text.isEmpty() && !text.matches(regex)) {
                    field.setBorder(BorderFactory.createLineBorder(WARNING_COLOR, 2));
                    field.setToolTipText("Invalid format for " + fieldName);
                } else {
                    field.setBorder(BorderFactory.createCompoundBorder(
                            new LineBorder(BORDER_COLOR, 1),
                            new EmptyBorder(6, 10, 6, 10)
                    ));
                }
            }
        });
    }








    private JPanel createFormSection(String title, JComponent[][] components) {
        JPanel panel = new JPanel(new GridLayout(components.length, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                SECTION_FONT,
                TABLE_HEADER_COLOR
        ));








        for (JComponent[] row : components) {
            JLabel label = (JLabel) row[0];
            label.setFont(LABEL_FONT);
            label.setForeground(new Color(60, 60, 60));
            panel.add(label);
            panel.add(row[1]);
        }








        return panel;
    }








    private JPanel createDatePanel(JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);








        JButton btnDate = new JButton("📅");
        btnDate.setPreferredSize(new Dimension(40, 30));
        btnDate.setFocusPainted(false);
        btnDate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDate.addActionListener(e -> showDatePicker(field));








        panel.add(field, BorderLayout.CENTER);
        panel.add(btnDate, BorderLayout.EAST);








        return panel;
    }








    // =========================================================================
    // EXPORT METHODS
    // =========================================================================
    private void exportAssetData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Asset Data");
        fileChooser.setSelectedFile(new File("assets_export_" + LocalDate.now() + ".csv"));








        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                for (int i = 0; i < assetTable.getColumnCount(); i++) {
                    writer.print("\"" + assetTable.getColumnName(i) + "\"");
                    if (i < assetTable.getColumnCount() - 1) writer.print(",");
                }
                writer.println();








                // Write data
                for (int row = 0; row < assetTable.getRowCount(); row++) {
                    for (int col = 0; col < assetTable.getColumnCount(); col++) {
                        Object value = assetTable.getValueAt(row, col);
                        writer.print("\"" + (value != null ? value.toString().replace("\"", "\"\"") : "") + "\"");
                        if (col < assetTable.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                }








                JOptionPane.showMessageDialog(this,
                        "Asset data exported successfully!\nLocation: " + file.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);








            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting data: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }








    private void exportHistoryData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Borrowing History");
        fileChooser.setSelectedFile(new File("borrowing_history_" + LocalDate.now() + ".csv"));








        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                for (int i = 0; i < historyTable.getColumnCount(); i++) {
                    writer.print("\"" + historyTable.getColumnName(i) + "\"");
                    if (i < historyTable.getColumnCount() - 1) writer.print(",");
                }
                writer.println();








                // Write data
                for (int row = 0; row < historyTable.getRowCount(); row++) {
                    for (int col = 0; col < historyTable.getColumnCount(); col++) {
                        Object value = historyTable.getValueAt(row, col);
                        writer.print("\"" + (value != null ? value.toString().replace("\"", "\"\"") : "") + "\"");
                        if (col < historyTable.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                }








                JOptionPane.showMessageDialog(this,
                        "Borrowing history exported successfully!",
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);








            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting data: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }








    // =========================================================================
    // UI HELPERS
    // =========================================================================
    private JTextField createStyledTextField(String text) {
        JTextField t = new JTextField(text);
        t.setFont(FIELD_FONT);
        t.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        t.setBackground(Color.WHITE);
        return t;
    }








    private void showDatePicker(JTextField targetField) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        CalendarDialog calendar = new CalendarDialog(parentWindow, targetField);
        calendar.setVisible(true);
    }












    // =========================================================================
    // RENDERER CLASSES
    // =========================================================================
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value.toString();
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI Semibold", Font.PLAIN, 11));








            if (status.equals("Good") || status.equals("Available")) {
                setBackground(new Color(39, 174, 96, 30));
                setForeground(new Color(39, 174, 96));
            } else if (status.equals("Pending") || status.equals("For Repair")) {
                setBackground(new Color(243, 156, 18, 30));
                setForeground(new Color(243, 156, 18));
            } else if (status.equals("Borrowed") || status.equals("Ongoing")) {
                setBackground(new Color(52, 152, 219, 30));
                setForeground(new Color(41, 128, 185));
            } else if (status.equals("Damaged") || status.equals("Lost") || status.equals("Disposed")) {
                setBackground(new Color(231, 76, 60, 30));
                setForeground(new Color(192, 57, 43));
            } else if (status.contains("Overdue")) {
                setBackground(new Color(231, 76, 60, 30));
                setForeground(new Color(192, 57, 43));
                setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }








            if (isSelected) {
                setBackground(getBackground().darker());
            }








            return c;
        }
    }








    // =========================================================================
    // CALENDAR DIALOG (unchanged)
    // =========================================================================
    class CalendarDialog extends JDialog {
        private LocalDate currentDate;
        private JTextField targetField;
        private JLabel lblMonthYear;
        private JPanel daysPanel;
        private DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");








        public CalendarDialog(Window parent, JTextField targetField) {
            super(parent, "Select Date", ModalityType.APPLICATION_MODAL);
            this.targetField = targetField;
            try {
                if (!targetField.getText().isEmpty()) {
                    currentDate = LocalDate.parse(targetField.getText(), dateFormatter);
                } else {
                    currentDate = LocalDate.now();
                }
            } catch (Exception e) {
                currentDate = LocalDate.now();
            }








            setSize(400, 350);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(Color.WHITE);








            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(Color.WHITE);
            headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));








            JButton btnPrev = new JButton(" < ");
            JButton btnNext = new JButton(" > ");
            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 16));








            btnPrev.addActionListener(e -> {
                currentDate = currentDate.minusMonths(1);
                updateCalendar();
            });
            btnNext.addActionListener(e -> {
                currentDate = currentDate.plusMonths(1);
                updateCalendar();
            });








            headerPanel.add(btnPrev, BorderLayout.WEST);
            headerPanel.add(lblMonthYear, BorderLayout.CENTER);
            headerPanel.add(btnNext, BorderLayout.EAST);








            daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
            daysPanel.setBackground(Color.WHITE);
            daysPanel.setBorder(new EmptyBorder(10, 10, 10, 10));








            add(headerPanel, BorderLayout.NORTH);
            add(daysPanel, BorderLayout.CENTER);








            // Add button panel at bottom
            JPanel buttonPanel = new JPanel(new FlowLayout());
            buttonPanel.setBackground(Color.WHITE);








            JButton btnToday = new JButton("Today");
            btnToday.addActionListener(e -> {
                currentDate = LocalDate.now();
                targetField.setText(currentDate.format(dateFormatter));
                dispose();
            });








            JButton btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(e -> dispose());








            buttonPanel.add(btnToday);
            buttonPanel.add(btnCancel);
            add(buttonPanel, BorderLayout.SOUTH);








            updateCalendar();
        }








        private void updateCalendar() {
            lblMonthYear.setText(currentDate.format(monthYearFormatter));
            daysPanel.removeAll();








            String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String dayName : dayNames) {
                JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
                dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dayLabel.setForeground(new Color(100, 100, 100));
                daysPanel.add(dayLabel);
            }








            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            int daysInMonth = currentDate.lengthOfMonth();
            int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue();
            int startOffset = (dayOfWeekValue % 7);








            for (int i = 0; i < startOffset; i++) {
                daysPanel.add(new JLabel(""));
            }








            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate dayDate = currentDate.withDayOfMonth(day);
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                dayButton.setFocusPainted(false);








                if (dayDate.equals(LocalDate.now())) {
                    dayButton.setBackground(new Color(220, 240, 255));
                    dayButton.setForeground(Color.BLUE);
                } else if (dayDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    dayButton.setForeground(Color.RED);
                } else {
                    dayButton.setBackground(Color.WHITE);
                }








                try {
                    if (!targetField.getText().isEmpty()) {
                        LocalDate selectedDate = LocalDate.parse(targetField.getText(), dateFormatter);
                        if (dayDate.equals(selectedDate)) {
                            dayButton.setBackground(new Color(180, 220, 240));
                            dayButton.setForeground(Color.BLACK);
                        }
                    }
                } catch (Exception e) {}








                final LocalDate selectedDay = dayDate;
                dayButton.addActionListener(e -> {
                    targetField.setText(selectedDay.format(dateFormatter));
                    dispose();
                });








                daysPanel.add(dayButton);
            }








            int totalCells = 42;
            int currentCells = startOffset + daysInMonth;
            for (int i = currentCells; i < totalCells; i++) {
                daysPanel.add(new JLabel(""));
            }








            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }








    // =========================================================================
    // LEND DIALOG (from original code - kept for compatibility)
    // =========================================================================
    private void showLendDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lend Asset", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BG_COLOR);




        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);




        // Fields
        JTextField txtAsset = createStyledTextField("");
        txtAsset.setEditable(false);
        JTextField txtAssetId = new JTextField();
        txtAssetId.setVisible(false);




        JTextField txtResident = createStyledTextField("");
        txtResident.setEditable(false);
        JTextField txtResidentId = new JTextField();
        txtResidentId.setVisible(false);




        JTextField txtBorrowDate = createStyledTextField(LocalDate.now().toString());
        JTextField txtReturnDate = createStyledTextField(LocalDate.now().plusDays(3).toString());




        // Picker buttons
        JButton btnPickAsset = createModernButton("Select Asset", PRIMARY_COLOR, "📦");
        btnPickAsset.addActionListener(e -> showAssetPicker(txtAsset, txtAssetId));




        JButton btnPickResident = createModernButton("Select Borrower", SECONDARY_COLOR, "👤");
        btnPickResident.addActionListener(e -> showResidentPicker(txtResident, txtResidentId));




        // Layout
        addStyledRow(form, "1. Select Asset:", createPickerPanel(txtAsset, btnPickAsset));
        addStyledRow(form, "2. Select Borrower:", createPickerPanel(txtResident, btnPickResident));
        addStyledRow(form, "3. Date Borrowed:", createDatePickerPanel(txtBorrowDate));
        addStyledRow(form, "4. Expected Return:", createDatePickerPanel(txtReturnDate));




        // Save button with date validation
        JButton btnSave = createModernButton("Confirm Transaction", ACCENT_COLOR, "✅");
        btnSave.addActionListener(e -> {
            try {
                if(txtAssetId.getText().isEmpty() || txtResidentId.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please select both an Asset and a Borrower.");
                    return;
                }




                // Parse dates
                LocalDate borrowDate = LocalDate.parse(txtBorrowDate.getText());
                LocalDate returnDate = LocalDate.parse(txtReturnDate.getText());




                // Validate that return date is not before borrow date
                if (returnDate.isBefore(borrowDate)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Error: Expected return date cannot be before the borrow date.\n" +
                                    "Please select a return date on or after " + borrowDate.toString(),
                            "Invalid Date",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }




                // Validate that borrow date is not in the future
                if (borrowDate.isAfter(LocalDate.now())) {
                    int confirm = JOptionPane.showConfirmDialog(dialog,
                            "Warning: Borrow date is in the future (" + borrowDate + ").\n" +
                                    "Do you want to proceed with this future date?",
                            "Future Date Warning",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);




                    if (confirm != JOptionPane.YES_OPTION) {
                        return;
                    }
                }




                int aId = Integer.parseInt(txtAssetId.getText());
                int rId = Integer.parseInt(txtResidentId.getText());
                Date bDate = Date.valueOf(borrowDate);
                Date rDate = Date.valueOf(returnDate);




                boolean success = new BorrowingDAO().lendItem(aId, rId, bDate, rDate);




                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Transaction Saved!");
                    try {
                        // FIXED: Truncate the log message to avoid data truncation error
                        String logMessage = "Asset Lent ID " + aId;
                        if (logMessage.length() > 100) {
                            logMessage = logMessage.substring(0, 100);
                        }
                        new SystemLogDAO().addLog(logMessage, "Admin", 1);
                    } catch (Exception ex) {
                        System.err.println("Log error: " + ex.getMessage());
                    }
                    loadBorrowingData();
                    loadAssetData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Error saving transaction.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Invalid Data. Please check dates (format: YYYY-MM-DD): " + ex.getMessage());
            }
        });




        JPanel btnP = new JPanel();
        btnP.setBackground(Color.WHITE);
        btnP.add(btnSave);




        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnP, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    private void addStyledRow(JPanel p, String label, JComponent c) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(150, 30));
        l.setFont(LABEL_FONT);
        l.setForeground(new Color(60, 60, 60));
        row.add(l, BorderLayout.WEST);
        row.add(c, BorderLayout.CENTER);
        p.add(row);
        p.add(Box.createVerticalStrut(15));
    }








    private JPanel createPickerPanel(JTextField field, JButton btn) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.WHITE);
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }








    private JPanel createDatePickerPanel(JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.WHITE);
        JButton btn = new JButton("📅");
        btn.setPreferredSize(new Dimension(45, 25));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showDatePicker(field));
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }








    // =========================================================================
    // PICKER METHODS (from original code - kept for compatibility)
    // =========================================================================
    private void showAssetPicker(JTextField nameField, JTextField idField) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Available Asset", true);
        d.setSize(600, 400);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_COLOR);








        // Table
        String[] cols = {"ID", "Item Name", "Prop No", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(25);








        // Load Only GOOD/AVAILABLE Assets
        List<BarangayAsset> list = new BarangayAssetDAO().getAllAssets();
        for(BarangayAsset a : list) {
            if(!"Borrowed".equalsIgnoreCase(a.getStatus()) && !"Lost".equalsIgnoreCase(a.getStatus()) && !"Disposed".equalsIgnoreCase(a.getStatus())) {
                m.addRow(new Object[]{ a.getAssetId(), a.getItemName(), a.getPropertyNumber(), a.getStatus() });
            }
        }








        // Selection Logic
        JButton btnSelect = createModernButton("Select Asset", ACCENT_COLOR, "✅");
        btnSelect.addActionListener(e -> {
            int row = t.getSelectedRow();
            if(row != -1) {
                idField.setText(t.getValueAt(row, 0).toString());
                nameField.setText(t.getValueAt(row, 1).toString());
                d.dispose();
            }
        });








        d.add(new JScrollPane(t), BorderLayout.CENTER);
        d.add(btnSelect, BorderLayout.SOUTH);








        d.setVisible(true);
    }








    private void showResidentPicker(JTextField nameField, JTextField idField) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Borrower", true);
        d.setSize(600, 400);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);
        d.getContentPane().setBackground(BG_COLOR);








        // Search Bar
        JPanel searchP = new JPanel(new FlowLayout());
        searchP.setBackground(BG_COLOR);
        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(FIELD_FONT);
        searchP.add(new JLabel("Search Name:"));
        searchP.add(txtSearch);








        // Table
        String[] cols = {"ID", "Name", "Purok"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(25);
        TableRowSorter<DefaultTableModel> sort = new TableRowSorter<>(m);
        t.setRowSorter(sort);








        // Search Filter Logic
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                sort.setRowFilter(RowFilter.regexFilter("(?i)" + txtSearch.getText()));
            }
        });








        // Load Residents
        List<Resident> list = new ResidentDAO().getAllResidents();
        for(Resident r : list) {
            m.addRow(new Object[]{ r.getResidentId(), r.getFirstName() +" " + r.getMiddleName()+" " + r.getLastName(), r.getPurok() });
        }








        // Selection
        JButton btnSelect = createModernButton("Select Resident", ACCENT_COLOR, "✅");
        btnSelect.addActionListener(e -> {
            int row = t.getSelectedRow();
            if(row != -1) {
                int modelRow = t.convertRowIndexToModel(row);
                idField.setText(m.getValueAt(modelRow, 0).toString());
                nameField.setText(m.getValueAt(modelRow, 1).toString());
                d.dispose();
            }
        });








        d.add(searchP, BorderLayout.NORTH);
        d.add(new JScrollPane(t), BorderLayout.CENTER);
        d.add(btnSelect, BorderLayout.SOUTH);
        d.setVisible(true);
    }








    // =========================================================================
    // BUTTON RENDERER AND EDITOR CLASSES (from original code)
    // =========================================================================
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setBackground(new Color(52, 152, 219));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }








        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }








    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;








        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            button.setBackground(new Color(52, 152, 219));
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));








            button.addActionListener(e -> {
                fireEditingStopped();
                // Handle return action
                int row = borrowingTable.getSelectedRow();
                if (row != -1) {
                    handleReturn();
                }
            });
        }








        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }








        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }
    }

    // =========================================================================
    //  HELPER CLASSES FOR STYLING (copied from SecretaryPrintDocument)
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
    // MAIN METHOD
    // =========================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }








            JFrame frame = new JFrame("Asset & Borrowing Management System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 900);
            frame.setLocationRelativeTo(null);








            frame.add(new AdminAssetBorrowingTab());
            frame.setVisible(true);
        });
    }
}

