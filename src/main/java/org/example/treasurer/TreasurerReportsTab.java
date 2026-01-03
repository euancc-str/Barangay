package org.example.treasurer;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;


public class TreasurerReportsTab extends JPanel {


    private FinancialService financialService;
    private JTable dailySummaryTable;
    private JTable dayPaymentsTable;
    private JTable monthDetailsTable;
    private JTable yearDetailsTable;


    private DefaultTableModel dailySummaryModel;
    private DefaultTableModel dayPaymentsModel;
    private DefaultTableModel monthDetailsModel;
    private DefaultTableModel yearDetailsModel;


    // Tabbed pane for different summaries
    private JTabbedPane summaryTabPane;


    // Labels for the cards
    private JLabel lblToday, lblWeek, lblMonth, lblYear;


    // Date selectors
    private JTextField dateTextField;
    private JButton btnCalendar;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JButton btnLoadMonth;
    private JButton btnLoadYear;


    // Summary labels
    private JLabel lblDayTotal, lblDayTransactions;
    private JLabel lblMonthTotal, lblMonthTransactions;
    private JLabel lblYearTotal, lblYearTransactions;


    // Labels for month/year names
    private JLabel lblSelectedMonthName;
    private JLabel lblSelectedYearName;


    // Calendar dialog
    private JDialog calendarDialog;
    private JLabel calendarMonthYearLabel;
    private JPanel calendarDaysPanel;
    private LocalDate selectedCalendarDate;
    private LocalDate currentCalendarDate;


    // Gradient colors
    private final Color CERULEAN_BLUE = new Color(100, 149, 237);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    private final Color VERY_LIGHT_BLUE = new Color(225, 245, 254);
    private final Color DARK_CERULEAN = new Color(70, 130, 180);
    private final Color WHITE_SMOKE = new Color(245, 245, 245);


    public TreasurerReportsTab() {
        this.financialService = new FinancialService();
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);


        // 1. Statistics Cards (Top)
        add(createStatsPanel(), BorderLayout.NORTH);


        // 2. Main Content Area with tabbed summaries
        add(createMainContentPanel(), BorderLayout.CENTER);


        // Create calendar dialog
        createCalendarDialog();


        // Initial Load
        refreshData();
        loadDailySummary();


        // Set current month/year in selectors
        LocalDate now = LocalDate.now();
        monthComboBox.setSelectedIndex(now.getMonthValue() - 1);
        yearComboBox.setSelectedItem(now.getYear());


        // Set initial date in text field
        dateTextField.setText(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        selectedCalendarDate = now;
        currentCalendarDate = now;


        // Load current day payments
        loadDayPayments(Date.from(now.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));


        // Load current month details initially
        loadMonthDetails(now.getYear(), now.getMonthValue());


        // Load current year details
        loadYearDetails(now.getYear());
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        GradientPaint gradient = new GradientPaint(
                0, 0, LIGHT_BLUE,
                getWidth(), getHeight(), VERY_LIGHT_BLUE
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }


    private void refreshData() {
        // Update Cards
        lblToday.setText("₱ " + String.format("%,.2f", financialService.getTotalIncome("Today")));
        lblWeek.setText("₱ " + String.format("%,.2f", financialService.getTotalIncome("Week")));
        lblMonth.setText("₱ " + String.format("%,.2f", financialService.getTotalIncome("Month")));
        lblYear.setText("₱ " + String.format("%,.2f", financialService.getTotalIncome("Year")));
    }


    private void loadDailySummary() {
        dailySummaryModel.setRowCount(0);
        List<Object[]> rows = financialService.getDailySummary();
        for (Object[] row : rows) {
            dailySummaryModel.addRow(row);
        }
    }


    private void loadDayPayments(Date date) {
        dayPaymentsModel.setRowCount(0);
        List<Object[]> rows = financialService.getTransactionsByDate(
                new java.sql.Date(date.getTime()).toLocalDate()
        );


        double dayTotal = 0;
        int transactionCount = 0;


        for (Object[] row : rows) {
            dayPaymentsModel.addRow(new Object[]{
                    row[0], row[1], row[2], row[3], row[4]
            });
            dayTotal += (Double) row[3];
            transactionCount++;
        }


        // Update summary labels
        lblDayTotal.setText("₱ " + String.format("%,.2f", dayTotal));
        lblDayTransactions.setText(String.valueOf(transactionCount));


        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        dayPaymentsTable.getTableHeader().setToolTipText("Payments for " + sdf.format(date));
    }


    private void loadMonthDetails(int year, int month) {
        monthDetailsModel.setRowCount(0);
        List<Object[]> rows = financialService.getTransactionsByMonthYear(year, month);


        double totalAmount = 0;
        int transactionCount = 0;


        for (Object[] row : rows) {
            monthDetailsModel.addRow(new Object[]{
                    row[0], row[1], row[2], row[3], row[4], row[5]
            });
            totalAmount += (Double) row[3];
            transactionCount++;
        }


        // Update the month summary panel with selected month's data
        updateMonthSummaryPanel(year, month, totalAmount, transactionCount);
    }


    private void updateMonthSummaryPanel(int year, int month, double totalAmount, int transactionCount) {
        // Update the month name label
        String monthName = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        lblSelectedMonthName.setText(monthName);


        // Update summary labels
        lblMonthTotal.setText("₱ " + String.format("%,.2f", totalAmount));
        lblMonthTransactions.setText(String.valueOf(transactionCount));


        // Update table title
        monthDetailsTable.getTableHeader().setToolTipText("Transactions for " + monthName);
    }


    private void loadYearDetails(int year) {
        yearDetailsModel.setRowCount(0);


        // Load monthly breakdown for the year
        double yearlyTotal = 0;
        int yearlyTransactions = 0;


        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};


        for (int month = 1; month <= 12; month++) {
            double monthTotal = financialService.getTotalIncomeForMonth(year, month);
            if (monthTotal > 0) {
                List<Object[]> monthTransactions = financialService.getTransactionsByMonthYear(year, month);
                int monthTransactionCount = monthTransactions.size();


                yearDetailsModel.addRow(new Object[]{
                        months[month - 1],
                        monthTransactionCount,
                        monthTotal
                });


                yearlyTotal += monthTotal;
                yearlyTransactions += monthTransactionCount;
            }
        }


        // Update the year summary panel
        updateYearSummaryPanel(year, yearlyTotal, yearlyTransactions);
    }


    private void updateYearSummaryPanel(int year, double totalAmount, int transactionCount) {
        // Update the year label
        lblSelectedYearName.setText(String.valueOf(year));


        // Update summary labels
        lblYearTotal.setText("₱ " + String.format("%,.2f", totalAmount));
        lblYearTransactions.setText(String.valueOf(transactionCount));


        // Update table title
        yearDetailsTable.getTableHeader().setToolTipText("Monthly Breakdown for " + year);
    }


    private void createCalendarDialog() {
        calendarDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        calendarDialog.setSize(400, 350);
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setResizable(false);


        // Header panel with month/year navigation
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CERULEAN_BLUE);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        // Previous month button
        JButton btnPrevMonth = new JButton("◀");
        btnPrevMonth.setFont(new Font("Arial", Font.BOLD, 16));
        btnPrevMonth.setBackground(Color.WHITE);
        btnPrevMonth.setForeground(CERULEAN_BLUE);
        btnPrevMonth.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnPrevMonth.addActionListener(e -> {
            currentCalendarDate = currentCalendarDate.minusMonths(1);
            updateCalendar();
        });


        // Next month button
        JButton btnNextMonth = new JButton("▶");
        btnNextMonth.setFont(new Font("Arial", Font.BOLD, 16));
        btnNextMonth.setBackground(Color.WHITE);
        btnNextMonth.setForeground(CERULEAN_BLUE);
        btnNextMonth.setBorder(new EmptyBorder(5, 10, 5, 10));
        btnNextMonth.addActionListener(e -> {
            currentCalendarDate = currentCalendarDate.plusMonths(1);
            updateCalendar();
        });


        // Month/Year label
        calendarMonthYearLabel = new JLabel();
        calendarMonthYearLabel.setFont(new Font("Arial", Font.BOLD, 16));
        calendarMonthYearLabel.setForeground(Color.WHITE);
        calendarMonthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);


        // Today button
        JButton btnToday = new JButton("Today");
        btnToday.setFont(new Font("Arial", Font.BOLD, 12));
        btnToday.setBackground(Color.WHITE);
        btnToday.setForeground(CERULEAN_BLUE);
        btnToday.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnToday.addActionListener(e -> {
            currentCalendarDate = LocalDate.now();
            updateCalendar();
        });


        headerPanel.add(btnPrevMonth, BorderLayout.WEST);
        headerPanel.add(calendarMonthYearLabel, BorderLayout.CENTER);
        headerPanel.add(btnNextMonth, BorderLayout.EAST);
        headerPanel.add(btnToday, BorderLayout.SOUTH);


        // Day names panel
        JPanel dayNamesPanel = new JPanel(new GridLayout(1, 7));
        dayNamesPanel.setBackground(Color.LIGHT_GRAY);
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            dayLabel.setForeground(Color.DARK_GRAY);
            dayLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            dayNamesPanel.add(dayLabel);
        }


        // Days panel
        calendarDaysPanel = new JPanel(new GridLayout(6, 7, 2, 2));
        calendarDaysPanel.setBackground(Color.WHITE);
        calendarDaysPanel.setBorder(new EmptyBorder(5, 5, 5, 5));


        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> calendarDialog.setVisible(false));


        JButton btnSelect = new JButton("Select");
        btnSelect.setFont(new Font("Arial", Font.BOLD, 12));
        btnSelect.setBackground(CERULEAN_BLUE);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.addActionListener(e -> {
            if (selectedCalendarDate != null) {
                dateTextField.setText(selectedCalendarDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                loadDayPayments(Date.from(selectedCalendarDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
            }
            calendarDialog.setVisible(false);
        });


        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSelect);


        calendarDialog.add(headerPanel, BorderLayout.NORTH);
        calendarDialog.add(dayNamesPanel, BorderLayout.CENTER);
        calendarDialog.add(calendarDaysPanel, BorderLayout.CENTER);
        calendarDialog.add(buttonPanel, BorderLayout.SOUTH);
    }


    private void updateCalendar() {
        // Update month/year label
        calendarMonthYearLabel.setText(currentCalendarDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));


        // Clear days panel
        calendarDaysPanel.removeAll();


        // Get first day of month and total days
        YearMonth yearMonth = YearMonth.from(currentCalendarDate);
        LocalDate firstOfMonth = currentCalendarDate.withDayOfMonth(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // 0 = Sunday, 6 = Saturday


        // Add empty cells for days before first day of month
        for (int i = 0; i < dayOfWeek; i++) {
            calendarDaysPanel.add(new JLabel());
        }


        // Add day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            final LocalDate date = LocalDate.of(currentCalendarDate.getYear(), currentCalendarDate.getMonthValue(), day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 12));
            dayButton.setMargin(new Insets(2, 2, 2, 2));


            // Style for today
            if (date.equals(LocalDate.now())) {
                dayButton.setBackground(new Color(255, 220, 100));
                dayButton.setForeground(Color.BLACK);
                dayButton.setFont(new Font("Arial", Font.BOLD, 12));
            } else {
                dayButton.setBackground(Color.WHITE);
                dayButton.setForeground(Color.BLACK);
            }


            // Style for selected day
            if (date.equals(selectedCalendarDate)) {
                dayButton.setBackground(CERULEAN_BLUE);
                dayButton.setForeground(Color.WHITE);
                dayButton.setFont(new Font("Arial", Font.BOLD, 12));
            }


            // Style for weekends
            int dayOfWeekIndex = date.getDayOfWeek().getValue() % 7;
            if (dayOfWeekIndex == 0 || dayOfWeekIndex == 6) { // Sunday or Saturday
                dayButton.setForeground(new Color(200, 0, 0));
            }


            dayButton.addActionListener(e -> {
                selectedCalendarDate = date;
                updateCalendar(); // Refresh to show new selection
            });


            calendarDaysPanel.add(dayButton);
        }


        // Fill remaining cells
        int totalCells = 42; // 6 weeks * 7 days
        int filledCells = dayOfWeek + daysInMonth;
        for (int i = filledCells; i < totalCells; i++) {
            calendarDaysPanel.add(new JLabel());
        }


        calendarDaysPanel.revalidate();
        calendarDaysPanel.repaint();
    }


    private void showCalendarDialog() {
        currentCalendarDate = selectedCalendarDate != null ? selectedCalendarDate : LocalDate.now();
        updateCalendar();
        calendarDialog.setVisible(true);
    }


    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(0, 150));


        lblToday = new JLabel("₱ 0.00");
        lblWeek = new JLabel("₱ 0.00");
        lblMonth = new JLabel("₱ 0.00");
        lblYear = new JLabel("₱ 0.00");


        panel.add(createCard("Today", lblToday, DARK_CERULEAN, CERULEAN_BLUE));
        panel.add(createCard("This Week", lblWeek, new Color(52, 152, 219), new Color(100, 180, 255)));
        panel.add(createCard("This Month", lblMonth, new Color(155, 89, 182), new Color(185, 120, 220)));
        panel.add(createCard("This Year", lblYear, new Color(241, 196, 15), new Color(255, 220, 100)));


        return panel;
    }


    private JPanel createCard(String title, JLabel valueLabel, Color startColor, Color endColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                GradientPaint gradient = new GradientPaint(
                        0, 0, startColor,
                        getWidth(), getHeight(), endColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);


                g2d.setColor(startColor.darker());
                g2d.fillRoundRect(0, 0, 8, getHeight(), 15, 15);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 20, 15, 15));


        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);


        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);


        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel, BorderLayout.CENTER);


        card.add(lblTitle, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);


        return card;
    }


    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(10, 20, 20, 20));


        // Create tabbed pane for different summaries
        summaryTabPane = new JTabbedPane(JTabbedPane.TOP);
        summaryTabPane.setFont(new Font("Arial", Font.BOLD, 14));
        summaryTabPane.setBackground(VERY_LIGHT_BLUE);


        // Tab 1: Daily Summary
        summaryTabPane.addTab("Daily", createDailyTab());


        // Tab 2: Monthly Summary
        summaryTabPane.addTab("Monthly", createMonthlyTab());


        // Tab 3: Yearly Summary
        summaryTabPane.addTab("Yearly", createYearlyTab());


        mainPanel.add(summaryTabPane, BorderLayout.CENTER);
        return mainPanel;
    }


    private JPanel createDailyTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));


        // Top Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(0, 0, 10, 0));


        JLabel lblDate = new JLabel("Select Date: ");
        lblDate.setFont(new Font("Arial", Font.BOLD, 14));
        lblDate.setForeground(Color.DARK_GRAY);


        // Date text field with calendar button
        JPanel datePanel = new JPanel(new BorderLayout());
        datePanel.setPreferredSize(new Dimension(180, 30));


        dateTextField = new JTextField();
        dateTextField.setFont(new Font("Arial", Font.PLAIN, 14));
        dateTextField.setEditable(false);
        dateTextField.setBackground(Color.WHITE);


        btnCalendar = new JButton("📅");
        btnCalendar.setFont(new Font("Arial", Font.PLAIN, 16));
        btnCalendar.setBackground(Color.WHITE);
        btnCalendar.setBorder(new LineBorder(CERULEAN_BLUE, 1));
        btnCalendar.setPreferredSize(new Dimension(40, 30));
        btnCalendar.addActionListener(e -> showCalendarDialog());


        datePanel.add(dateTextField, BorderLayout.CENTER);
        datePanel.add(btnCalendar, BorderLayout.EAST);


        JButton btnLoadDay = new JButton("Load Day Payments");
        btnLoadDay.setFont(new Font("Arial", Font.BOLD, 14));
        btnLoadDay.setBackground(CERULEAN_BLUE);
        btnLoadDay.setForeground(Color.WHITE);
        btnLoadDay.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnLoadDay.setFocusPainted(false);
        btnLoadDay.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(dateTextField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                loadDayPayments(Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        JButton btnRefreshDaily = new JButton("🔄 Refresh");
        btnRefreshDaily.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefreshDaily.setBackground(new Color(40, 167, 69));
        btnRefreshDaily.setForeground(Color.WHITE);
        btnRefreshDaily.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnRefreshDaily.setFocusPainted(false);
        btnRefreshDaily.addActionListener(e -> {
            refreshData();
            loadDailySummary();
            try {
                LocalDate date = LocalDate.parse(dateTextField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                loadDayPayments(Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
            } catch (Exception ex) {
                // Keep current date if parsing fails
                loadDayPayments(new Date());
            }
        });


        JButton btnPrintDay = new JButton("🖨 Print Report");
        btnPrintDay.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrintDay.setBackground(DARK_CERULEAN);
        btnPrintDay.setForeground(Color.WHITE);
        btnPrintDay.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnPrintDay.setFocusPainted(false);
        btnPrintDay.addActionListener(e -> printDailyReport());


        controlPanel.add(lblDate);
        controlPanel.add(datePanel);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnLoadDay);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnRefreshDaily);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnPrintDay);


        // Use JSplitPane instead of GridLayout to allow resizing
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerLocation(350); // Set initial divider position
        splitPane.setResizeWeight(0.3); // Left panel gets 30% of space initially


        // Left panel: Daily Summary (narrower)
        JPanel dailySummaryPanel = createDailySummaryPanel();
        dailySummaryPanel.setPreferredSize(new Dimension(350, 400));


        // Right panel: Day Payments (wider)
        JPanel dayPaymentsPanel = createDayPaymentsPanel();
        dayPaymentsPanel.setPreferredSize(new Dimension(650, 400));


        splitPane.setLeftComponent(dailySummaryPanel);
        splitPane.setRightComponent(dayPaymentsPanel);


        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createDailySummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(CERULEAN_BLUE, 2),
                new EmptyBorder(10, 10, 10, 10) // Reduced padding
        ));


        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);


        JLabel titleLabel = new JLabel("📅 Daily Summary (Last 30 Days)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14)); // Smaller font
        titleLabel.setForeground(DARK_CERULEAN);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));


        // Daily summary table with fewer columns
        String[] summaryCols = {"Date", "Transactions", "Total"};
        dailySummaryModel = new DefaultTableModel(summaryCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return String.class;
                    case 1: return Integer.class;
                    case 2: return Double.class;
                    default: return Object.class;
                }
            }
        };


        dailySummaryTable = new JTable(dailySummaryModel);
        dailySummaryTable.setRowHeight(30); // Slightly smaller row height
        dailySummaryTable.setFont(new Font("Arial", Font.PLAIN, 12)); // Smaller font
        dailySummaryTable.setBackground(WHITE_SMOKE);
        dailySummaryTable.setSelectionBackground(LIGHT_BLUE);
        dailySummaryTable.setSelectionForeground(Color.BLACK);
        dailySummaryTable.setGridColor(new Color(220, 220, 220));
        dailySummaryTable.setShowGrid(true);
        dailySummaryTable.getTableHeader().setReorderingAllowed(false);
        dailySummaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);


        // Style table header
        JTableHeader header = dailySummaryTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12)); // Smaller header font
        header.setBackground(CERULEAN_BLUE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));


        // Set column widths to make table narrower
        dailySummaryTable.getColumnModel().getColumn(0).setPreferredWidth(100);  // Date
        dailySummaryTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // Transactions
        dailySummaryTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Total


        // Custom renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);


        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    setText("₱ " + String.format("%,.0f", (Double) value)); // No decimals for summary
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return this;
            }
        };


        // Apply renderers
        dailySummaryTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        dailySummaryTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        dailySummaryTable.getColumnModel().getColumn(2).setCellRenderer(amountRenderer);


        // Make table scrollable
        JScrollPane scrollPane = new JScrollPane(dailySummaryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.getViewport().setBackground(WHITE_SMOKE);
        scrollPane.setPreferredSize(new Dimension(320, 300)); // Smaller preferred size


        // Add subtitle
        JLabel subtitle = new JLabel("Last 30 Days");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 11));
        subtitle.setForeground(Color.GRAY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        subtitle.setBorder(new EmptyBorder(5, 0, 5, 0));


        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(subtitle, BorderLayout.SOUTH);


        return panel;
    }


    private JPanel createDayPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(CERULEAN_BLUE, 2),
                new EmptyBorder(10, 10, 10, 10)
        ));


        // Header with day summary - more compact
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));


        JLabel titleLabel = new JLabel("💰 Daily Transactions");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(DARK_CERULEAN);


        // Summary panel for selected day - more compact layout
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryPanel.setOpaque(false);


        JLabel totalLabel = new JLabel("Total:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
        totalLabel.setForeground(Color.DARK_GRAY);


        lblDayTotal = new JLabel("₱ 0.00");
        lblDayTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblDayTotal.setForeground(DARK_CERULEAN);


        JLabel transactionsLabel = new JLabel("Count:");
        transactionsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        transactionsLabel.setForeground(Color.DARK_GRAY);


        lblDayTransactions = new JLabel("0");
        lblDayTransactions.setFont(new Font("Arial", Font.BOLD, 14));
        lblDayTransactions.setForeground(DARK_CERULEAN);


        summaryPanel.add(totalLabel);
        summaryPanel.add(lblDayTotal);
        summaryPanel.add(transactionsLabel);
        summaryPanel.add(lblDayTransactions);


        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(summaryPanel, BorderLayout.EAST);


        // Day payments table - wider columns
        String[] paymentsCols = {"ID", "Name", "Document", "Amount", "Time"};
        dayPaymentsModel = new DefaultTableModel(paymentsCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return Integer.class;
                    case 3: return Double.class;
                    default: return String.class;
                }
            }
        };


        dayPaymentsTable = new JTable(dayPaymentsModel);
        dayPaymentsTable.setRowHeight(30);
        dayPaymentsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        dayPaymentsTable.setBackground(WHITE_SMOKE);
        dayPaymentsTable.setSelectionBackground(LIGHT_BLUE);
        dayPaymentsTable.setSelectionForeground(Color.BLACK);
        dayPaymentsTable.setGridColor(new Color(220, 220, 220));
        dayPaymentsTable.setShowGrid(true);
        dayPaymentsTable.getTableHeader().setReorderingAllowed(false);
        dayPaymentsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Allow horizontal scrolling


        // Style table header
        JTableHeader header = dayPaymentsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(CERULEAN_BLUE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));


        // Set column widths for rectangular layout
        dayPaymentsTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        dayPaymentsTable.getColumnModel().getColumn(1).setPreferredWidth(180);  // Name (wider)
        dayPaymentsTable.getColumnModel().getColumn(2).setPreferredWidth(150);  // Document (wider)
        dayPaymentsTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // Amount
        dayPaymentsTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Time


        // Custom renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);


        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    double amount = (Double) value;
                    if (amount == 0) {
                        setText("₱ 0.00");
                    } else {
                        setText("₱ " + String.format("%,.2f", amount));
                    }
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return this;
            }
        };


        // Apply renderers
        dayPaymentsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        dayPaymentsTable.getColumnModel().getColumn(3).setCellRenderer(amountRenderer);
        dayPaymentsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);


        // Make table scrollable with horizontal scrollbar
        JScrollPane scrollPane = new JScrollPane(dayPaymentsTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.getViewport().setBackground(WHITE_SMOKE);
        scrollPane.setPreferredSize(new Dimension(600, 300)); // Wider preferred size


        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createMonthlyTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));


        // Top Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(0, 0, 10, 0));


        JLabel lblMonth = new JLabel("Month: ");
        lblMonth.setFont(new Font("Arial", Font.BOLD, 14));
        lblMonth.setForeground(Color.DARK_GRAY);


        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        monthComboBox.setPreferredSize(new Dimension(120, 30));


        JLabel lblYear = new JLabel("Year: ");
        lblYear.setFont(new Font("Arial", Font.BOLD, 14));
        lblYear.setForeground(Color.DARK_GRAY);


        yearComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear; year >= currentYear - 5; year--) {
            yearComboBox.addItem(year);
        }
        yearComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        yearComboBox.setPreferredSize(new Dimension(100, 30));


        btnLoadMonth = new JButton("Load Month Details");
        btnLoadMonth.setFont(new Font("Arial", Font.BOLD, 14));
        btnLoadMonth.setBackground(CERULEAN_BLUE);
        btnLoadMonth.setForeground(Color.WHITE);
        btnLoadMonth.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnLoadMonth.setFocusPainted(false);
        btnLoadMonth.addActionListener(e -> {
            int year = (Integer) yearComboBox.getSelectedItem();
            int month = monthComboBox.getSelectedIndex() + 1;
            loadMonthDetails(year, month);
        });


        JButton btnRefreshMonth = new JButton("🔄 Refresh");
        btnRefreshMonth.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefreshMonth.setBackground(new Color(40, 167, 69));
        btnRefreshMonth.setForeground(Color.WHITE);
        btnRefreshMonth.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnRefreshMonth.setFocusPainted(false);
        btnRefreshMonth.addActionListener(e -> {
            int year = (Integer) yearComboBox.getSelectedItem();
            int month = monthComboBox.getSelectedIndex() + 1;
            loadMonthDetails(year, month);
        });


        JButton btnPrintMonth = new JButton("🖨 Print Report");
        btnPrintMonth.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrintMonth.setBackground(DARK_CERULEAN);
        btnPrintMonth.setForeground(Color.WHITE);
        btnPrintMonth.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnPrintMonth.setFocusPainted(false);
        btnPrintMonth.addActionListener(e -> printMonthlyReport());


        controlPanel.add(lblMonth);
        controlPanel.add(monthComboBox);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(lblYear);
        controlPanel.add(yearComboBox);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnLoadMonth);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnRefreshMonth);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnPrintMonth);


        // Main content panel with summary and table
        JPanel mainContentPanel = new JPanel(new BorderLayout(0, 15));
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));


        // Top: Summary Panel
        mainContentPanel.add(createMonthSummaryPanel(), BorderLayout.NORTH);


        // Bottom: Transactions Table (Bigger)
        mainContentPanel.add(createMonthDetailsPanel(), BorderLayout.CENTER);


        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(mainContentPanel, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createMonthSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(CERULEAN_BLUE, 2),
                new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(new Color(240, 248, 255));
        panel.setPreferredSize(new Dimension(0, 120));


        // Left side: Month info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        leftPanel.setOpaque(false);


        JLabel iconLabel = new JLabel("📊 ");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 24));
        iconLabel.setForeground(CERULEAN_BLUE);


        JPanel monthInfoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        monthInfoPanel.setOpaque(false);


        JLabel monthTitle = new JLabel("SELECTED MONTH");
        monthTitle.setFont(new Font("Arial", Font.BOLD, 12));
        monthTitle.setForeground(Color.DARK_GRAY);


        lblSelectedMonthName = new JLabel("January 2024");
        lblSelectedMonthName.setFont(new Font("Arial", Font.BOLD, 20));
        lblSelectedMonthName.setForeground(DARK_CERULEAN);


        monthInfoPanel.add(monthTitle);
        monthInfoPanel.add(lblSelectedMonthName);


        leftPanel.add(iconLabel);
        leftPanel.add(Box.createHorizontalStrut(15));
        leftPanel.add(monthInfoPanel);


        // Right side: Statistics
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 10));
        statsPanel.setOpaque(false);


        JLabel totalLabel = new JLabel("TOTAL CASH:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
        totalLabel.setForeground(Color.DARK_GRAY);


        lblMonthTotal = new JLabel("₱ 0.00");
        lblMonthTotal.setFont(new Font("Arial", Font.BOLD, 20));
        lblMonthTotal.setForeground(new Color(46, 204, 113));


        JLabel transactionsLabel = new JLabel("TRANSACTIONS:");
        transactionsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        transactionsLabel.setForeground(Color.DARK_GRAY);


        lblMonthTransactions = new JLabel("0");
        lblMonthTransactions.setFont(new Font("Arial", Font.BOLD, 20));
        lblMonthTransactions.setForeground(new Color(52, 152, 219));


        statsPanel.add(totalLabel);
        statsPanel.add(lblMonthTotal);
        statsPanel.add(transactionsLabel);
        statsPanel.add(lblMonthTransactions);


        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(statsPanel, BorderLayout.EAST);


        return panel;
    }


    private JPanel createMonthDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));


        // Header
        JLabel titleLabel = new JLabel("📋 Month Transactions (Detailed)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(DARK_CERULEAN);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));


        // Month details table
        String[] detailsCols = {"Request ID", "Payer Name", "Document", "Amount", "Date", "Time"};
        monthDetailsModel = new DefaultTableModel(detailsCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return Integer.class;
                    case 3: return Double.class;
                    case 4: return java.sql.Date.class;
                    default: return String.class;
                }
            }
        };


        monthDetailsTable = new JTable(monthDetailsModel);
        monthDetailsTable.setRowHeight(35);
        monthDetailsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        monthDetailsTable.setBackground(WHITE_SMOKE);
        monthDetailsTable.setSelectionBackground(LIGHT_BLUE);
        monthDetailsTable.setSelectionForeground(Color.BLACK);
        monthDetailsTable.setGridColor(new Color(220, 220, 220));
        monthDetailsTable.setShowGrid(true);
        monthDetailsTable.getTableHeader().setReorderingAllowed(false);
        monthDetailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Enable horizontal scrolling


        // Style table header
        JTableHeader header = monthDetailsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(CERULEAN_BLUE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));


        // Set column widths
        monthDetailsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Request ID
        monthDetailsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Payer Name
        monthDetailsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Document
        monthDetailsTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Amount
        monthDetailsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Date
        monthDetailsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Time


        // Custom renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);


        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    setText("₱ " + String.format("%,.2f", (Double) value));
                }
                setHorizontalAlignment(JLabel.RIGHT);
                return this;
            }
        };


        // Date renderer
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof java.sql.Date) {
                    setText(new SimpleDateFormat("yyyy-MM-dd").format((java.sql.Date) value));
                }
                setHorizontalAlignment(JLabel.CENTER);
                return this;
            }
        };


        // Apply renderers
        monthDetailsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        monthDetailsTable.getColumnModel().getColumn(3).setCellRenderer(amountRenderer);
        monthDetailsTable.getColumnModel().getColumn(4).setCellRenderer(dateRenderer);
        monthDetailsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);


        // Make table scrollable with both horizontal and vertical scrolling
        JScrollPane scrollPane = new JScrollPane(monthDetailsTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.getViewport().setBackground(WHITE_SMOKE);
        scrollPane.setPreferredSize(new Dimension(800, 400));


        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createYearlyTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));


        // Top Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(0, 0, 10, 0));


        JLabel lblYear = new JLabel("Year: ");
        lblYear.setFont(new Font("Arial", Font.BOLD, 14));
        lblYear.setForeground(Color.DARK_GRAY);


        JComboBox<Integer> yearSelectComboBox = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear; year >= currentYear - 5; year--) {
            yearSelectComboBox.addItem(year);
        }
        yearSelectComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        yearSelectComboBox.setPreferredSize(new Dimension(100, 30));


        btnLoadYear = new JButton("Load Year Details");
        btnLoadYear.setFont(new Font("Arial", Font.BOLD, 14));
        btnLoadYear.setBackground(CERULEAN_BLUE);
        btnLoadYear.setForeground(Color.WHITE);
        btnLoadYear.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnLoadYear.setFocusPainted(false);
        btnLoadYear.addActionListener(e -> {
            int year = (Integer) yearSelectComboBox.getSelectedItem();
            loadYearDetails(year);
        });


        JButton btnRefreshYear = new JButton("🔄 Refresh");
        btnRefreshYear.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefreshYear.setBackground(new Color(40, 167, 69));
        btnRefreshYear.setForeground(Color.WHITE);
        btnRefreshYear.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnRefreshYear.setFocusPainted(false);
        btnRefreshYear.addActionListener(e -> {
            int year = (Integer) yearSelectComboBox.getSelectedItem();
            loadYearDetails(year);
        });


        JButton btnPrintYear = new JButton("🖨 Print Report");
        btnPrintYear.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrintYear.setBackground(DARK_CERULEAN);
        btnPrintYear.setForeground(Color.WHITE);
        btnPrintYear.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnPrintYear.setFocusPainted(false);
        btnPrintYear.addActionListener(e -> printYearlyReport());


        controlPanel.add(lblYear);
        controlPanel.add(yearSelectComboBox);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnLoadYear);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnRefreshYear);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(btnPrintYear);


        // Main content panel with summary and table
        JPanel mainContentPanel = new JPanel(new BorderLayout(0, 15));
        mainContentPanel.setOpaque(false);
        mainContentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));


        // Top: Summary Panel
        mainContentPanel.add(createYearSummaryPanel(), BorderLayout.NORTH);


        // Bottom: Monthly Breakdown Table (Bigger)
        mainContentPanel.add(createYearDetailsPanel(), BorderLayout.CENTER);


        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(mainContentPanel, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createYearSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(241, 196, 15), 2),
                new EmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(new Color(255, 253, 231));
        panel.setPreferredSize(new Dimension(0, 120));


        // Left side: Year info
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        leftPanel.setOpaque(false);


        JLabel iconLabel = new JLabel("📈 ");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 24));
        iconLabel.setForeground(new Color(241, 196, 15));


        JPanel yearInfoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        yearInfoPanel.setOpaque(false);


        JLabel yearTitle = new JLabel("SELECTED YEAR");
        yearTitle.setFont(new Font("Arial", Font.BOLD, 12));
        yearTitle.setForeground(Color.DARK_GRAY);


        lblSelectedYearName = new JLabel("2024");
        lblSelectedYearName.setFont(new Font("Arial", Font.BOLD, 20));
        lblSelectedYearName.setForeground(new Color(241, 196, 15));


        yearInfoPanel.add(yearTitle);
        yearInfoPanel.add(lblSelectedYearName);


        leftPanel.add(iconLabel);
        leftPanel.add(Box.createHorizontalStrut(15));
        leftPanel.add(yearInfoPanel);


        // Right side: Statistics
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 10));
        statsPanel.setOpaque(false);


        JLabel totalLabel = new JLabel("TOTAL CASH:");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 12));
        totalLabel.setForeground(Color.DARK_GRAY);


        lblYearTotal = new JLabel("₱ 0.00");
        lblYearTotal.setFont(new Font("Arial", Font.BOLD, 20));
        lblYearTotal.setForeground(new Color(46, 204, 113));


        JLabel transactionsLabel = new JLabel("TRANSACTIONS:");
        transactionsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        transactionsLabel.setForeground(Color.DARK_GRAY);


        lblYearTransactions = new JLabel("0");
        lblYearTransactions.setFont(new Font("Arial", Font.BOLD, 20));
        lblYearTransactions.setForeground(new Color(52, 152, 219));


        statsPanel.add(totalLabel);
        statsPanel.add(lblYearTotal);
        statsPanel.add(transactionsLabel);
        statsPanel.add(lblYearTransactions);


        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(statsPanel, BorderLayout.EAST);


        return panel;
    }


    private JPanel createYearDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));


        // Header
        JLabel titleLabel = new JLabel("📊 Monthly Breakdown");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(DARK_CERULEAN);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));


        // Year details table (showing monthly breakdown)
        String[] detailsCols = {"Month", "Transactions", "Total Amount"};
        yearDetailsModel = new DefaultTableModel(detailsCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: return String.class;
                    case 1: return Integer.class;
                    case 2: return Double.class;
                    default: return Object.class;
                }
            }
        };


        yearDetailsTable = new JTable(yearDetailsModel);
        yearDetailsTable.setRowHeight(40);
        yearDetailsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        yearDetailsTable.setBackground(WHITE_SMOKE);
        yearDetailsTable.setSelectionBackground(LIGHT_BLUE);
        yearDetailsTable.setSelectionForeground(Color.BLACK);
        yearDetailsTable.setGridColor(new Color(220, 220, 220));
        yearDetailsTable.setShowGrid(true);
        yearDetailsTable.getTableHeader().setReorderingAllowed(false);


        // Style table header
        JTableHeader header = yearDetailsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(CERULEAN_BLUE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));


        // Set column widths
        yearDetailsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Month
        yearDetailsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Transactions
        yearDetailsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Total Amount


        // Custom renderers
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);


        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    setText("₱ " + String.format("%,.2f", (Double) value));
                }
                setHorizontalAlignment(JLabel.RIGHT);
                setFont(new Font("Arial", Font.BOLD, 14));
                return this;
            }
        };


        // Apply renderers
        yearDetailsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        yearDetailsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        yearDetailsTable.getColumnModel().getColumn(2).setCellRenderer(amountRenderer);


        // Make table scrollable
        JScrollPane scrollPane = new JScrollPane(yearDetailsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollPane.getViewport().setBackground(WHITE_SMOKE);
        scrollPane.setPreferredSize(new Dimension(800, 400));


        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);


        return panel;
    }


    private void printDailyReport() {
        if (dayPaymentsModel.getRowCount() == 0 && dailySummaryModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to print.", "Print Error", JOptionPane.WARNING_MESSAGE);
            return;
        }


        int choice = JOptionPane.showOptionDialog(this,
                "What would you like to print?",
                "Print Daily Report",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Day Payments", "Daily Summary", "Both"},
                "Day Payments");


        if (choice == 0 || choice == 2) {
            printTable(dayPaymentsTable, "Daily Payments Report");
        }
        if (choice == 1 || choice == 2) {
            printTable(dailySummaryTable, "Daily Summary Report");
        }
    }


    private void printMonthlyReport() {
        if (monthDetailsModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No monthly data to print.", "Print Error", JOptionPane.WARNING_MESSAGE);
            return;
        }


        int year = (Integer) yearComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1;
        String monthName = YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        printTable(monthDetailsTable, "Monthly Transactions - " + monthName);
    }


    private void printYearlyReport() {
        if (yearDetailsModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No yearly data to print.", "Print Error", JOptionPane.WARNING_MESSAGE);
            return;
        }


        printTable(yearDetailsTable, "Yearly Monthly Breakdown Report");
    }


    private void printTable(JTable table, String title) {
        MessageFormat header = new MessageFormat(title + " - Generated on " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        MessageFormat footer = new MessageFormat("Page {0}");


        try {
            boolean complete = table.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Print completed successfully!", "Print Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error printing: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

