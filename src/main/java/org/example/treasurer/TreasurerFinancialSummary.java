package org.example.treasurer;


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;




public class TreasurerFinancialSummary extends JPanel {


    // Color scheme matching TreasurerDashboard
    private final Color CERULEAN_BLUE = new Color(100, 149, 237);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    private final Color VERY_LIGHT_BLUE = new Color(225, 245, 254);
    private final Color DARK_CERULEAN = new Color(70, 130, 180);
    private final Color SUCCESS_GREEN = new Color(46, 204, 113);
    private final Color WARNING_ORANGE = new Color(255, 165, 0);
    private final Color INFO_PURPLE = new Color(155, 89, 182);


    // Fonts
    private final Font HEADER_FONT = new Font("Arial", Font.BOLD, 26);
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 22);
    private final Font SUBTITLE_FONT = new Font("Arial", Font.PLAIN, 14);
    private final Font STAT_FONT = new Font("Arial", Font.BOLD, 32);
    private final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 13);
    private final Font TABLE_FONT = new Font("Arial", Font.PLAIN, 14);
    private final Font TABLE_HEADER_FONT = new Font("Arial", Font.BOLD, 15);


    // DAO instances
    private FinancialDAO financialDAO = new FinancialDAO();


    // UI Components
    private JLabel totalAssetsLabel;
    private JLabel totalPaidLabel;
    private JLabel todayIncomeLabel;
    private JLabel thisMonthIncomeLabel;
    private JTable recentTransactionsTable;
    private DefaultTableModel recentTransactionsModel;
    private JComboBox<String> timeFrameCombo;


    // Date filtering components
    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton applyDateFilterButton;
    private JButton clearDateFilterButton;
    private LocalDate fromDate = null;
    private LocalDate toDate = null;


    public TreasurerFinancialSummary() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(15, 15, 15, 15));


        initializeUI();
        loadData();
        startAutoRefresh();
    }


    private void initializeUI() {
        // Create header panel
        JPanel headerPanel = createHeaderPanel();


        // Create main content panel
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), VERY_LIGHT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));


        // Filter panel with time frame and date range
        JPanel filterPanel = createFilterPanel();


        // Stats cards panel
        JPanel statsPanel = createStatsCardsPanel();


        // Recent transactions panel
        JPanel transactionsPanel = createRecentTransactionsPanel();


        // Add components to content panel
        contentPanel.add(filterPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(statsPanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(transactionsPanel);


        // Add to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }


    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));


        // Time frame selector row
        JPanel timeFramePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timeFramePanel.setOpaque(false);
        timeFramePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));


        JLabel timeFrameLabel = new JLabel("Time Frame:");
        timeFrameLabel.setFont(LABEL_FONT);
        timeFrameLabel.setForeground(Color.DARK_GRAY);


        timeFrameCombo = new JComboBox<>(new String[]{
                "Today", "This Week", "This Month", "This Year", "All Time", "Custom Range"
        });
        timeFrameCombo.setFont(LABEL_FONT);
        timeFrameCombo.setBackground(Color.WHITE);
        timeFrameCombo.setPreferredSize(new Dimension(150, 30));
        timeFrameCombo.addActionListener(e -> {
            if (timeFrameCombo.getSelectedItem().equals("Custom Range")) {
                enableDateRangeFields(true);
            } else {
                enableDateRangeFields(false);
                loadData();
            }
        });


        timeFramePanel.add(timeFrameLabel);
        timeFramePanel.add(timeFrameCombo);
        timeFramePanel.add(Box.createHorizontalGlue());


        // Date range filter row
        JPanel dateRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dateRangePanel.setOpaque(false);
        dateRangePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        dateRangePanel.setBorder(new EmptyBorder(5, 0, 0, 0));


        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(LABEL_FONT);
        fromLabel.setForeground(Color.DARK_GRAY);


        fromDateField = new JTextField(10);
        fromDateField.setFont(LABEL_FONT);
        fromDateField.setBackground(Color.WHITE);
        fromDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CERULEAN_BLUE, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        fromDateField.setEditable(false);


        JButton fromDateButton = new JButton("📅");
        fromDateButton.setFont(new Font("Arial", Font.PLAIN, 14));
        fromDateButton.setBackground(Color.WHITE);
        fromDateButton.setBorder(BorderFactory.createLineBorder(CERULEAN_BLUE, 1));
        fromDateButton.setFocusPainted(false);
        fromDateButton.addActionListener(e -> showDatePicker(fromDateField, "Select Start Date"));


        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(LABEL_FONT);
        toLabel.setForeground(Color.DARK_GRAY);


        toDateField = new JTextField(10);
        toDateField.setFont(LABEL_FONT);
        toDateField.setBackground(Color.WHITE);
        toDateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CERULEAN_BLUE, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        toDateField.setEditable(false);


        JButton toDateButton = new JButton("📅");
        toDateButton.setFont(new Font("Arial", Font.PLAIN, 14));
        toDateButton.setBackground(Color.WHITE);
        toDateButton.setBorder(BorderFactory.createLineBorder(CERULEAN_BLUE, 1));
        toDateButton.setFocusPainted(false);
        toDateButton.addActionListener(e -> showDatePicker(toDateField, "Select End Date"));


        applyDateFilterButton = new JButton("Apply Filter");
        applyDateFilterButton.setFont(LABEL_FONT);
        applyDateFilterButton.setBackground(CERULEAN_BLUE);
        applyDateFilterButton.setForeground(Color.WHITE);
        applyDateFilterButton.setFocusPainted(false);
        applyDateFilterButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        applyDateFilterButton.addActionListener(e -> applyDateRangeFilter());


        clearDateFilterButton = new JButton("Clear");
        clearDateFilterButton.setFont(LABEL_FONT);
        clearDateFilterButton.setBackground(new Color(150, 150, 150));
        clearDateFilterButton.setForeground(Color.WHITE);
        clearDateFilterButton.setFocusPainted(false);
        clearDateFilterButton.setBorder(new EmptyBorder(5, 15, 5, 15));
        clearDateFilterButton.addActionListener(e -> clearDateRangeFilter());


        dateRangePanel.add(fromLabel);
        dateRangePanel.add(fromDateField);
        dateRangePanel.add(fromDateButton);
        dateRangePanel.add(Box.createHorizontalStrut(10));
        dateRangePanel.add(toLabel);
        dateRangePanel.add(toDateField);
        dateRangePanel.add(toDateButton);
        dateRangePanel.add(Box.createHorizontalStrut(20));
        dateRangePanel.add(applyDateFilterButton);
        dateRangePanel.add(clearDateFilterButton);


        // Initially disable date range fields
        enableDateRangeFields(false);


        panel.add(timeFramePanel);
        panel.add(dateRangePanel);


        return panel;
    }


    private void enableDateRangeFields(boolean enabled) {
        fromDateField.setEnabled(enabled);
        toDateField.setEnabled(enabled);
        applyDateFilterButton.setEnabled(enabled);
        clearDateFilterButton.setEnabled(enabled);


        if (!enabled) {
            fromDateField.setText("");
            toDateField.setText("");
            fromDate = null;
            toDate = null;
        }
    }


    private void showDatePicker(JTextField targetField, String title) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        CalendarDialog calendar = new CalendarDialog(parentWindow, targetField, title);
        calendar.setVisible(true);
    }


    private void applyDateRangeFilter() {
        String fromText = fromDateField.getText().trim();
        String toText = toDateField.getText().trim();


        if (fromText.isEmpty() || toText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select both start and end dates.",
                    "Incomplete Date Range",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }


        try {
            fromDate = LocalDate.parse(fromText);
            toDate = LocalDate.parse(toText);


            if (fromDate.isAfter(toDate)) {
                JOptionPane.showMessageDialog(this,
                        "Start date cannot be after end date.",
                        "Invalid Date Range",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }


            // Calculate days between dates
            long daysBetween = ChronoUnit.DAYS.between(fromDate, toDate);
            if (daysBetween > 365) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "You've selected a date range of " + daysBetween + " days.\n" +
                                "This might take longer to process.\n" +
                                "Do you want to continue?",
                        "Large Date Range Warning",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);


                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }


            loadData();


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD format.",
                    "Date Format Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private void clearDateRangeFilter() {
        fromDateField.setText("");
        toDateField.setText("");
        fromDate = null;
        toDate = null;
        timeFrameCombo.setSelectedItem("All Time");
        loadData();
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, CERULEAN_BLUE,
                        getWidth(), 0, DARK_CERULEAN
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(CERULEAN_BLUE, 2, true),
                new EmptyBorder(25, 40, 25, 40)
        ));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);


        JLabel lblTitle = new JLabel("Financial Summary");
        lblTitle.setFont(HEADER_FONT);
        lblTitle.setForeground(Color.WHITE);


        JLabel lblSubtitle = new JLabel("Overview of barangay assets and document payments");
        lblSubtitle.setFont(SUBTITLE_FONT);
        lblSubtitle.setForeground(Color.WHITE);


        titlePanel.add(lblTitle);
        titlePanel.add(lblSubtitle);


        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        datePanel.setOpaque(false);


        JLabel lblDate = new JLabel("📅 " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        lblDate.setFont(SUBTITLE_FONT);
        lblDate.setForeground(Color.WHITE);


        datePanel.add(lblDate);


        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(datePanel, BorderLayout.EAST);


        return headerPanel;
    }


    private JPanel createStatsCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(1200, 400));


        // Card 1: Total Assets Value
        JPanel assetsCard = createStatCard(
                "📦 Total Assets Value",
                "Total value of all barangay assets",
                "₱0.00",
                SUCCESS_GREEN,
                "fa:cube"
        );
        totalAssetsLabel = (JLabel) assetsCard.getClientProperty("valueLabel");


        // Card 2: Total Paid Documents
        JPanel paidCard = createStatCard(
                "💰 Total Paid Documents",
                "Total revenue from document payments",
                "₱0.00",
                CERULEAN_BLUE,
                "fa:money-bill-wave"
        );
        totalPaidLabel = (JLabel) paidCard.getClientProperty("valueLabel");


        // Card 3: Today's Income
        JPanel todayCard = createStatCard(
                "📈 Today's Income",
                "Total payments received today",
                "₱0.00",
                WARNING_ORANGE,
                "fa:calendar-day"
        );
        todayIncomeLabel = (JLabel) todayCard.getClientProperty("valueLabel");


        // Card 4: This Month's Income
        JPanel monthCard = createStatCard(
                "📊 This Month's Income",
                "Total payments this month",
                "₱0.00",
                INFO_PURPLE,
                "fa:calendar-alt"
        );
        thisMonthIncomeLabel = (JLabel) monthCard.getClientProperty("valueLabel");


        panel.add(assetsCard);
        panel.add(paidCard);
        panel.add(todayCard);
        panel.add(monthCard);


        return panel;
    }


    private JPanel createStatCard(String title, String description, String value, Color accentColor, String icon) {
        JPanel card = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;


                // Draw rounded rectangle background
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));


                // Draw accent line at top
                g2d.setColor(accentColor);
                g2d.fillRect(0, 0, getWidth(), 5);


                // Draw subtle shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 20, 20));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(280, 180));


        // Top section with icon and title
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);


        JLabel iconLabel = new JLabel(getIconForStat(icon));
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        iconLabel.setForeground(accentColor);


        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);


        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.DARK_GRAY);


        JLabel descLabel = new JLabel(description);
        descLabel.setFont(SUBTITLE_FONT);
        descLabel.setForeground(new Color(100, 100, 100));


        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(descLabel, BorderLayout.SOUTH);


        topPanel.add(iconLabel, BorderLayout.WEST);
        topPanel.add(titlePanel, BorderLayout.CENTER);


        // Value display
        JPanel valuePanel = new JPanel(new BorderLayout());
        valuePanel.setOpaque(false);


        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(STAT_FONT);
        valueLabel.setForeground(Color.BLACK);


        valuePanel.add(valueLabel, BorderLayout.CENTER);


        // Store reference to value label
        card.putClientProperty("valueLabel", valueLabel);


        card.add(topPanel, BorderLayout.NORTH);
        card.add(valuePanel, BorderLayout.CENTER);


        return card;
    }


    private String getIconForStat(String iconType) {
        switch(iconType) {
            case "fa:cube": return "📦";
            case "fa:money-bill-wave": return "💰";
            case "fa:calendar-day": return "📈";
            case "fa:calendar-alt": return "📊";
            default: return "📊";
        }
    }


    private JPanel createRecentTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(1200, 400));


        // Panel header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);


        JLabel titleLabel = new JLabel("📝 Recent Transactions");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.DARK_GRAY);


        JButton refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(LABEL_FONT);
        refreshButton.setBackground(CERULEAN_BLUE);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(new EmptyBorder(8, 15, 8, 15));
        refreshButton.addActionListener(e -> loadRecentTransactions());


        JButton exportButton = new JButton("📤 Export");
        exportButton.setFont(LABEL_FONT);
        exportButton.setBackground(new Color(46, 204, 113));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.setBorder(new EmptyBorder(8, 15, 8, 15));
        exportButton.addActionListener(e -> exportTransactions());


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportButton);


        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);


        // Table
        String[] columns = {"Date", "Transaction ID", "Resident", "Document Type", "Amount", "Status"};
        recentTransactionsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };


        recentTransactionsTable = new JTable(recentTransactionsModel);
        styleTransactionsTable();


        JScrollPane tableScroll = new JScrollPane(recentTransactionsTable);
        tableScroll.setBorder(new LineBorder(CERULEAN_BLUE, 1));
        tableScroll.setBackground(Color.WHITE);


        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);


        return panel;
    }


    private void styleTransactionsTable() {
        recentTransactionsTable.setFont(TABLE_FONT);
        recentTransactionsTable.setRowHeight(35);
        recentTransactionsTable.setGridColor(new Color(220, 220, 220));
        recentTransactionsTable.setSelectionBackground(LIGHT_BLUE);
        recentTransactionsTable.setSelectionForeground(Color.BLACK);
        recentTransactionsTable.setShowVerticalLines(true);
        recentTransactionsTable.setShowHorizontalLines(true);
        recentTransactionsTable.setIntercellSpacing(new Dimension(1, 1));
        recentTransactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentTransactionsTable.getTableHeader().setReorderingAllowed(false);


        JTableHeader header = recentTransactionsTable.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(DARK_CERULEAN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));


        // Custom cell renderer for amount column
        recentTransactionsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            private DecimalFormat formatter = new DecimalFormat("₱#,##0.00");


            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);


                if (value instanceof Double) {
                    setText(formatter.format(value));
                    setForeground(new Color(0, 100, 0));
                }
                return c;
            }
        });


        // Custom cell renderer for status column
        recentTransactionsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);


                String status = value.toString();
                if (status.equals("Paid") || status.equals("Approved")) {
                    setBackground(new Color(220, 255, 220));
                    setForeground(new Color(0, 100, 0));
                } else if (status.equals("Pending")) {
                    setBackground(new Color(255, 255, 200));
                    setForeground(new Color(204, 153, 0));
                } else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }


                if (isSelected) {
                    setBackground(LIGHT_BLUE);
                }


                return c;
            }
        });


        // Set column widths
        recentTransactionsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        recentTransactionsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        recentTransactionsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        recentTransactionsTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        recentTransactionsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        recentTransactionsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
    }


    private void loadData() {
        new SwingWorker<Void, Void>() {
            private double totalAssetsValue = 0;
            private double totalPaidAmount = 0;
            private double todayIncome = 0;
            private double monthIncome = 0;


            @Override
            protected Void doInBackground() throws Exception {
                // Get total assets value from barangay_asset table
                String assetsSql = "SELECT SUM(value) FROM barangay_asset WHERE status NOT IN ('Lost', 'Disposed')";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery(assetsSql)) {
                    if (rs.next()) {
                        totalAssetsValue = rs.getDouble(1);
                    }
                }


                // Get total paid amount based on selected timeframe or date range
                String timeframe = (String) timeFrameCombo.getSelectedItem();


                if (timeframe.equals("Custom Range") && fromDate != null && toDate != null) {
                    // Use date range
                    totalPaidAmount = financialDAO.getTotalIncomeByDateRange(
                            java.sql.Date.valueOf(fromDate),
                            java.sql.Date.valueOf(toDate)
                    );
                } else {
                    // Use predefined timeframe
                    totalPaidAmount = financialDAO.getTotalIncome(timeframe);
                }


                // Get today's income
                todayIncome = financialDAO.getTotalIncome("Today");


                // Get this month's income
                monthIncome = financialDAO.getTotalIncome("Month");


                return null;
            }


            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions


                    // Update labels with formatted currency
                    DecimalFormat formatter = new DecimalFormat("₱#,##0.00");


                    if (totalAssetsLabel != null) {
                        totalAssetsLabel.setText(formatter.format(totalAssetsValue));
                    }


                    if (totalPaidLabel != null) {
                        totalPaidLabel.setText(formatter.format(totalPaidAmount));
                    }


                    if (todayIncomeLabel != null) {
                        todayIncomeLabel.setText(formatter.format(todayIncome));
                    }


                    if (thisMonthIncomeLabel != null) {
                        thisMonthIncomeLabel.setText(formatter.format(monthIncome));
                    }


                    // Load recent transactions
                    loadRecentTransactions();


                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TreasurerFinancialSummary.this,
                            "Error loading financial data: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }


    private void loadRecentTransactions() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Clear existing data
                recentTransactionsModel.setRowCount(0);


                // Build SQL query based on filter
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("SELECT r.updatedAt, r.requestId, ")
                        .append("CONCAT(res.firstName, ' ', res.lastName) as residentName, ")
                        .append("dt.name as docType, r.totalFee, r.paymentStatus ")
                        .append("FROM document_request r ")
                        .append("JOIN resident res ON r.residentId = res.residentId ")
                        .append("JOIN document_type dt ON r.docTypeId = dt.docTypeId ")
                        .append("WHERE r.paymentStatus = 'Paid' ");


                // Add date filter if custom range is selected
                String timeframe = (String) timeFrameCombo.getSelectedItem();
                if (timeframe.equals("Custom Range") && fromDate != null && toDate != null) {
                    sqlBuilder.append("AND DATE(r.updatedAt) BETWEEN ? AND ? ");
                }


                sqlBuilder.append("ORDER BY r.updatedAt DESC LIMIT 50");


                String sql = sqlBuilder.toString();


                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {


                    // Set parameters if custom range
                    int paramIndex = 1;
                    if (timeframe.equals("Custom Range") && fromDate != null && toDate != null) {
                        pstmt.setDate(paramIndex++, java.sql.Date.valueOf(fromDate));
                        pstmt.setDate(paramIndex, java.sql.Date.valueOf(toDate));
                    }


                    java.sql.ResultSet rs = pstmt.executeQuery();


                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");


                    while (rs.next()) {
                        java.util.Date date = rs.getTimestamp("updatedAt");
                        String dateStr = sdf.format(date);
                        int requestId = rs.getInt("requestId");
                        String residentName = rs.getString("residentName");
                        String docType = rs.getString("docType");
                        double amount = rs.getDouble("totalFee");
                        String status = rs.getString("paymentStatus");


                        recentTransactionsModel.addRow(new Object[]{
                                dateStr,
                                "REQ-" + requestId,
                                residentName,
                                docType,
                                amount,
                                status
                        });
                    }
                }
                return null;
            }


            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    recentTransactionsTable.repaint();


                    // Update table title with filter info
                    String timeframe = (String) timeFrameCombo.getSelectedItem();
                    if (timeframe.equals("Custom Range") && fromDate != null && toDate != null) {
                        recentTransactionsTable.getTableHeader().setToolTipText(
                                "Showing transactions from " + fromDate + " to " + toDate
                        );
                    } else {
                        recentTransactionsTable.getTableHeader().setToolTipText(
                                "Showing " + timeframe.toLowerCase() + " transactions"
                        );
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    private void exportTransactions() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Transactions");
        fileChooser.setSelectedFile(new File("transactions_" + LocalDate.now() + ".csv"));


        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                for (int i = 0; i < recentTransactionsTable.getColumnCount(); i++) {
                    writer.print("\"" + recentTransactionsTable.getColumnName(i) + "\"");
                    if (i < recentTransactionsTable.getColumnCount() - 1) writer.print(",");
                }
                writer.println();


                // Write data
                for (int row = 0; row < recentTransactionsTable.getRowCount(); row++) {
                    for (int col = 0; col < recentTransactionsTable.getColumnCount(); col++) {
                        Object value = recentTransactionsTable.getValueAt(row, col);
                        writer.print("\"" + (value != null ? value.toString().replace("\"", "\"\"") : "") + "\"");
                        if (col < recentTransactionsTable.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                }


                JOptionPane.showMessageDialog(this,
                        "Transactions exported successfully!\nLocation: " + file.getAbsolutePath(),
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


    private void startAutoRefresh() {
        javax.swing.Timer refreshTimer = new javax.swing.Timer(30000, e -> loadData());
        refreshTimer.start();
    }


    // Public method to manually refresh data
    public void refreshData() {
        loadData();
    }


    // Calendar Dialog Class
    class CalendarDialog extends JDialog {
        private LocalDate currentDate;
        private JTextField targetField;
        private String title;
        private JLabel lblMonthYear;
        private JPanel daysPanel;
        private DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        public CalendarDialog(Window parent, JTextField targetField, String title) {
            super(parent, title, ModalityType.APPLICATION_MODAL);
            this.targetField = targetField;
            this.title = title;


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


            JButton btnPrev = new JButton("◀");
            JButton btnNext = new JButton("▶");
            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Arial", Font.BOLD, 16));
            lblMonthYear.setForeground(DARK_CERULEAN);


            btnPrev.setFont(new Font("Arial", Font.BOLD, 14));
            btnPrev.setBackground(CERULEAN_BLUE);
            btnPrev.setForeground(Color.WHITE);
            btnPrev.setFocusPainted(false);
            btnPrev.setBorder(new EmptyBorder(5, 10, 5, 10));


            btnNext.setFont(new Font("Arial", Font.BOLD, 14));
            btnNext.setBackground(CERULEAN_BLUE);
            btnNext.setForeground(Color.WHITE);
            btnNext.setFocusPainted(false);
            btnNext.setBorder(new EmptyBorder(5, 10, 5, 10));


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
            buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


            JButton btnToday = new JButton("Today");
            btnToday.setFont(LABEL_FONT);
            btnToday.setBackground(CERULEAN_BLUE);
            btnToday.setForeground(Color.WHITE);
            btnToday.setFocusPainted(false);
            btnToday.addActionListener(e -> {
                currentDate = LocalDate.now();
                targetField.setText(currentDate.format(dateFormatter));
                dispose();
            });


            JButton btnCancel = new JButton("Cancel");
            btnCancel.setFont(LABEL_FONT);
            btnCancel.setBackground(new Color(150, 150, 150));
            btnCancel.setForeground(Color.WHITE);
            btnCancel.setFocusPainted(false);
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
                dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
                dayLabel.setForeground(DARK_CERULEAN);
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
                dayButton.setFont(new Font("Arial", Font.PLAIN, 12));
                dayButton.setFocusPainted(false);
                dayButton.setBackground(Color.WHITE);
                dayButton.setBorder(BorderFactory.createLineBorder(LIGHT_BLUE, 1));


                if (dayDate.equals(LocalDate.now())) {
                    dayButton.setBackground(CERULEAN_BLUE);
                    dayButton.setForeground(Color.WHITE);
                } else if (dayDate.getDayOfWeek().getValue() == 7) { // Sunday
                    dayButton.setForeground(Color.RED);
                } else if (dayDate.getDayOfWeek().getValue() == 6) { // Saturday
                    dayButton.setForeground(new Color(0, 100, 0));
                }


                try {
                    if (!targetField.getText().isEmpty()) {
                        LocalDate selectedDate = LocalDate.parse(targetField.getText(), dateFormatter);
                        if (dayDate.equals(selectedDate)) {
                            dayButton.setBackground(new Color(100, 149, 237, 150));
                            dayButton.setForeground(Color.BLACK);
                        }
                    }
                } catch (Exception e) {
                    // Ignore parse errors
                }


                final LocalDate selectedDay = dayDate;
                dayButton.addActionListener(e -> {
                    targetField.setText(selectedDay.format(dateFormatter));
                    dispose();
                });


                dayButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!dayDate.equals(LocalDate.now())) {
                            dayButton.setBackground(LIGHT_BLUE);
                        }
                    }


                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!dayDate.equals(LocalDate.now())) {
                            dayButton.setBackground(Color.WHITE);
                        }
                    }
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


    // Need to add missing imports


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }


            JFrame frame = new JFrame("Financial Summary");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);


            frame.add(new TreasurerFinancialSummary());
            frame.setVisible(true);
        });
    }
}

