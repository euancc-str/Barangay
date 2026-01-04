// AdminLogsTab.java - Updated color scheme
package org.example.Admin;


import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;


public class AdminLogsTab extends JPanel {


    private JTable logsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblRecordCount;


    // Date filter components
    private JButton dateFilterBtn;
    private Date selectedDate;
    private JButton clearDateBtn;
    private JTextField searchField;


    // --- UPDATED VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color MODERN_BLUE = new Color(66, 133, 244);
    private final Color LIGHT_GREY = new Color(248, 249, 250);
    private final Color DARK_GREY = new Color(52, 58, 64);
    private LocalDateTime lastLogDate = LocalDateTime.MIN; // Initialize to minimum value
    private int lastMaxId = 0;
    private javax.swing.Timer autoRefreshTimer;


    public AdminLogsTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);


        // Load data initially
        loadLogData();
        startAutoRefresh();
    }




    public void loadLogData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                // HEAVY TASK: Connects to DB
                SystemLogDAO logDao = new SystemLogDAO();
                return logDao.getLogsByFilter("All Time");
            }


            @Override
            protected void done() {
                // 3. Update UI when finished
                try {
                    List<Object[]> logs = get();


                    // Wipe the board only when data is ready
                    if (tableModel != null) {
                        RowFilter<? super DefaultTableModel, ? super Integer> currentFilter = null;
                        if (sorter != null) {
                            currentFilter = sorter.getRowFilter();
                            sorter.setRowFilter(null);
                        }


                        tableModel.setRowCount(0);


                        if (logs != null) {
                            for (Object[] row : logs) {
                                tableModel.addRow(row);
                            }
                        }


                        if (sorter != null) sorter.setRowFilter(currentFilter);
                        applyFilters();
                    }


                    updateRecordCount();


                    // Update lastMaxId after loading data
                    updateLastMaxId();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    private void applyFilters() {
        if (sorter == null) return;

        String text = searchField.getText();
        String selectedPeriod = (String) periodFilterBox.getSelectedItem();
        String selectedYear = (String) yearFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Text Search Filter
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

        // 2. Specific Date Filter (Calendar Picker - Column 4)
        if (selectedDate != null) {
            final Date finalSelectedDate = selectedDate;
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(4); // Date is at index 4
                        if (!(val instanceof String)) return false;
                        String dateStr = (String) val; // "yyyy-MM-dd HH:mm:ss"

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDateStr = sdf.format(finalSelectedDate);

                        return dateStr.startsWith(selectedDateStr);
                    } catch (Exception e) { return false; }
                }
            });
        }

        // 3. Period Filter (This Week / This Month)
        if (selectedPeriod != null && !selectedPeriod.equals("All Periods") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(4); // Date is at index 4
                        if (!(val instanceof String)) return false;

                        // Parse String to LocalDate (Taking only first 10 chars "yyyy-MM-dd")
                        String dateStr = ((String) val).substring(0, 10);
                        java.time.LocalDate rowDate = java.time.LocalDate.parse(dateStr);
                        java.time.LocalDate now = java.time.LocalDate.now();

                        if (selectedPeriod.equals("This Week")) {
                            java.time.temporal.TemporalField fieldISO = java.time.temporal.WeekFields.of(java.util.Locale.FRANCE).dayOfWeek();
                            java.time.LocalDate startOfWeek = now.with(fieldISO, 1);
                            java.time.LocalDate endOfWeek = now.with(fieldISO, 7);
                            return !rowDate.isBefore(startOfWeek) && !rowDate.isAfter(endOfWeek);
                        } else if (selectedPeriod.equals("This Month")) {
                            return rowDate.getMonth() == now.getMonth() && rowDate.getYear() == now.getYear();
                        }
                        return true;
                    } catch (Exception e) { return false; }
                }
            });
        }

        // 4. Year Filter (2025, 2024...)
        if (selectedYear != null && !selectedYear.equals("All Years") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(4); // Date is at index 4
                        if (!(val instanceof String)) return false;

                        String dateStr = ((String) val).substring(0, 10);
                        java.time.LocalDate rowDate = java.time.LocalDate.parse(dateStr);
                        int targetYear = Integer.parseInt(selectedYear);

                        return rowDate.getYear() == targetYear;
                    } catch (Exception e) { return false; }
                }
            });
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        updateRecordCount();
    }

    // =========================================================================
    //  CALENDAR IMPLEMENTATION (Copied from SecretaryPrintDocument)
    // =========================================================================
    private void showModernDatePicker() {
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
        if (selectedDate != null) {
            calendar.setTime(selectedDate);
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
                JButton dayBtn = createDayButton(day, calendar, today, dateDialog);
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

        // Today button
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
            calendar.setTime(new Date());
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
            if (selectedDate == null) {
                // If no date selected, use current date
                selectedDate = calendar.getTime();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateFilterBtn.setText("📅 " + sdf.format(selectedDate));
            dateFilterBtn.setForeground(Color.WHITE);
            dateFilterBtn.repaint();
            dateDialog.dispose();
            applyFilters();
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

    private JButton createDayButton(int day, Calendar calendar, Calendar today, JDialog dateDialog) {
        final boolean isSelectedForThisButton;
        if (selectedDate != null) {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
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
            selectedDate = calendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            dateFilterBtn.setText("📅 " + sdf.format(selectedDate));
            dateFilterBtn.setForeground(Color.WHITE);
            dateFilterBtn.repaint();
            dateDialog.dispose();
            applyFilters();
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


    // =========================================================================
    //  REST OF EXISTING CODE
    // =========================================================================

    // NEW METHOD: Update lastMaxId after loading data
    private void updateLastMaxId() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                String sql = "SELECT MAX(logId) as max_id FROM system_logs";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {


                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    return rs.next() ? rs.getInt("max_id") : 0;
                }
            }


            @Override
            protected void done() {
                try {
                    lastMaxId = get();
                    System.out.println("Updated lastMaxId to: " + lastMaxId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(2000, e -> {
            // Only refresh if user isn't selecting a row
            if (logsTable != null && logsTable.getSelectedRow() == -1) {
                checkForUpdates();
            }
        });
        autoRefreshTimer.start();
    }


    private void checkForUpdates() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Simple query - just get the latest ID
                String sql = "SELECT MAX(logId) as max_id FROM system_logs";


                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {


                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        int currentMaxId = rs.getInt("max_id");
                        return currentMaxId > lastMaxId;
                    }
                }
                return false;
            }


            @Override
            protected void done() {
                try {
                    boolean hasUpdates = get();
                    if (hasUpdates) {
                        System.out.println("New log data detected! Refreshing...");
                        loadLogData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // =========================================================================
    // 1. PRINT FUNCTIONALITY
    // =========================================================================
    private void handlePrint() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as PDF");
        int userSelection = fileChooser.showSaveDialog(this);


        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getAbsolutePath().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }


            try {
                // 1. Create Document (Landscape)
                Document doc = new Document(PageSize.A4.rotate()); // ✅ Fixed: lowercase 'rotate()'
                PdfWriter.getInstance(doc, new FileOutputStream(file));
                doc.open();


                // 2. Add Title
                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                Paragraph title = new Paragraph("Logs List", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);


                // 3. Create Table
                int colCount = logsTable.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);


                // 4. Add Headers (✅ MATCHING YOUR TABLE COLORS)
                // We use the same color: new Color(52, 152, 219)
                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);


                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);


                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(logsTable.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor); // ✅ Blue Background
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8); // More padding like your table
                    pdfTable.addCell(cell);
                }


                // 5. Add Rows
                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);


                for (int i = 0; i < logsTable.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = logsTable.getValueAt(i, j);
                        String text = (val != null) ? val.toString() : "";


                        PdfPCell cell = new PdfPCell(new Paragraph(text, rowFont));
                        cell.setPadding(6);
                        cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);


                        // ✅ Zebra Striping (Light Blue tint to match theme)
                        if (i % 2 == 1) {
                            cell.setBackgroundColor(new java.awt.Color(235, 245, 250));
                        }


                        pdfTable.addCell(cell);
                    }
                }


                doc.add(pdfTable);
                doc.close();


                JOptionPane.showMessageDialog(this, "Export Success!\nFile: " + file.getAbsolutePath());


            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Export Error: " + e.getMessage());
            }
        }
    }


    // =========================================================================
    // GUI CONSTRUCTION
    // =========================================================================
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));


        // --- TOP TOOLBAR ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));


        // Left: Search + Filter
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setBackground(BG_COLOR);


        // A. Search
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true), new EmptyBorder(5, 5, 5, 5)));


        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        leftPanel.add(Box.createHorizontalStrut(10));

        // 1. Period Filter
        JLabel lblPeriod = new JLabel("Filter:");
        lblPeriod.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(lblPeriod);

        String[] periods = {"All Periods", "This Week", "This Month"};
        periodFilterBox = new JComboBox<>(periods);
        periodFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        periodFilterBox.setBackground(Color.WHITE);
        periodFilterBox.setFocusable(false);
        periodFilterBox.addActionListener(e -> {
            if (!periodFilterBox.getSelectedItem().equals("All Periods")) {
                yearFilterBox.setSelectedIndex(0); // Reset Year
                selectedDate = null;
                dateFilterBtn.setText("📅 Select Date");
                dateFilterBtn.setForeground(Color.DARK_GRAY);
            }
            applyFilters();
        });
        leftPanel.add(periodFilterBox);

        leftPanel.add(Box.createHorizontalStrut(10));

        // 2. Year Filter
        JLabel lblYear = new JLabel("Year:");
        lblYear.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(lblYear);

        yearFilterBox = new JComboBox<>();
        yearFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        yearFilterBox.setBackground(Color.WHITE);
        yearFilterBox.setFocusable(false);

        yearFilterBox.addItem("All Years");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i <= 5; i++) {
            yearFilterBox.addItem(String.valueOf(currentYear - i));
        }

        yearFilterBox.addActionListener(e -> {
            if (!yearFilterBox.getSelectedItem().equals("All Years")) {
                periodFilterBox.setSelectedIndex(0); // Reset Period
                selectedDate = null;
                dateFilterBtn.setText("📅 Select Date");
                dateFilterBtn.setForeground(Color.DARK_GRAY);
            }
            applyFilters();
        });
        leftPanel.add(yearFilterBox);

        leftPanel.add(Box.createHorizontalStrut(10));
        // B. Date Filter - REPLACED WITH CALENDAR
        JLabel filterLabel = new JLabel("  Date:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Modern date filter button (similar to DashboardPanel)
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
        dateFilterBtn.addActionListener(e -> showModernDatePicker());

        // Clear date button
        clearDateBtn = new JButton("Clear") {
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
            applyFilters();
        });


        leftPanel.add(searchLabel);
        leftPanel.add(searchField);
        leftPanel.add(filterLabel);
        leftPanel.add(dateFilterBtn);
        leftPanel.add(Box.createHorizontalStrut(5));
        leftPanel.add(clearDateBtn);


        // Right: Print Button
        JPanel rightPrint = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPrint.setBackground(BG_COLOR);
        JButton btnPrint = createRoundedButton("🖨 Print Report", new Color(44, 62, 80));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(160, 40));
        btnPrint.addActionListener(e -> handlePrint());
        rightPrint.add(btnPrint);


        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPrint, BorderLayout.EAST);


        contentPanel.add(topPanel);
        contentPanel.add(Box.createVerticalStrut(15));


        // --- TABLE ---
        String[] columnNames = {"Log ID", "Action Type", "Target", "Performed By", "Date & Time"};


        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };


        logsTable = new JTable(tableModel);
        logsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        logsTable.setRowHeight(45);
        logsTable.setGridColor(new Color(200, 200, 200));
        logsTable.setSelectionBackground(new Color(220, 237, 250));
        logsTable.setSelectionForeground(Color.BLACK);
        logsTable.setShowVerticalLines(true);
        logsTable.setShowHorizontalLines(true);


        sorter = new TableRowSorter<>(tableModel);
        logsTable.setRowSorter(sorter);


        // Ensure ID sorting treats IDs as numbers
        sorter.setComparator(0, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Integer i1 = Integer.parseInt(s1);
                    Integer i2 = Integer.parseInt(s2);
                    return i1.compareTo(i2);
                } catch (NumberFormatException e) {
                    return s1.compareTo(s2);
                }
            }
        });


        // Default Sort: Newest Logs First (Column 4)
        List<RowSorter.SortKey> sortKeys = new java.util.ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);


        JTableHeader header = logsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));


        // Ensure table cell text is black for readability
        logsTable.setForeground(Color.BLACK);


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < logsTable.getColumnCount(); i++) {
            logsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        JScrollPane tableScrollPane = new JScrollPane(logsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));


        contentPanel.add(tableScrollPane);


        // --- FOOTER COUNT ---
        contentPanel.add(Box.createVerticalStrut(10));
        lblRecordCount = new JLabel("Total Logs: 0");
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        lblRecordCount.setForeground(new Color(100, 100, 100));
        contentPanel.add(lblRecordCount);


        return contentPanel;
    }


    private void updateRecordCount() {
        if (lblRecordCount != null && logsTable != null) {
            int count = logsTable.getRowCount();
            lblRecordCount.setText("Total Logs: " + count);
        }
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(25, 40, 25, 40));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);


        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 26));
        lblSystem.setForeground(Color.WHITE);


        JLabel lblModule = new JLabel("System Audit Logs");
        lblModule.setFont(new Font("Arial", Font.BOLD, 22));
        lblModule.setForeground(Color.WHITE);


        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }


    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            JFrame frame = new JFrame("Audit Logs");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new AdminLogsTab());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
    private JComboBox<String> periodFilterBox;
    private JComboBox<String> yearFilterBox;

    // =========================================================================
    //  HELPER CLASSES FOR STYLING (Copied from SecretaryPrintDocument)
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
}

