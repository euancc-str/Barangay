package org.example.Admin.AdminSettings;


import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.BarangayAssetDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayAsset;
import org.example.Admin.SystemLogDAO;
import org.example.utils.AutoRefresher;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.time.format.DateTimeFormatter;


public class AdminAssetTab extends JPanel {


    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblRecordCount;
    private JTextField searchField;


    // --- VISUAL STYLE VARIABLES (Same as others) ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color BTN_ADD_COLOR = new Color(46, 204, 113);
    private final Color BTN_PRINT_COLOR = new Color(155, 89, 182);
    private final Color BTN_UPDATE_COLOR = new Color(52, 152, 219);
    private final Color BTN_DELETE_COLOR = new Color(231, 76, 60);
    private final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 14);


    // --- CALENDAR FILTER VARIABLES (Copied from SecretaryPrintDocument) ---
    private JButton dateFilterBtn;
    private java.util.Date selectedDate;
    private final Color MODERN_BLUE = new Color(66, 133, 244);
    private final Color LIGHT_GREY = new Color(248, 249, 250);
    private final Color DARK_GREY = new Color(52, 58, 64);


    public AdminAssetTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);


        loadData();
        setupSearchFunctionality();
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                if (refresher != null) {
                    refresher.stop();
                }
                loadData();
                refresher = new AutoRefresher("Asset", AdminAssetTab.this::loadData);
                System.out.println("Tab opened/active. Auto-refresh started.");
            }


            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
                if (refresher != null) {
                    refresher.stop();
                    refresher = null;
                }
                System.out.println("Tab hidden/closed. Auto-refresh stopped.");
            }


            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) { }
        });
    }

    private AutoRefresher refresher;

    // =========================================================================
    // DATA LOADING (SwingWorker)
    // =========================================================================
    private javax.swing.Timer lightTimer;
    private static volatile long lastGlobalUpdate = System.currentTimeMillis();
    private long lastKnownCount = -1;


    private void startLightPolling() {
        lightTimer = new javax.swing.Timer(3000, e -> {
            if (table != null && table.getSelectedRow() == -1) {
                checkLightUpdate();
            }
        });
        lightTimer.start();
    }


    private void checkLightUpdate() {
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                String sql = "SELECT " +
                        "COUNT(*) as total_count, " +
                        "UNIX_TIMESTAMP(MAX(dateAcquired)) as last_ts " +
                        "FROM barangay_asset";


                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {


                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        long count = rs.getLong("total_count");
                        long timestamp = rs.getLong("last_ts") * 1000L;
                        return new Object[]{count, timestamp};
                    }
                }
                return new Object[]{0L, 0L};
            }


            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    long currentCount = (Long) result[0];
                    long dbTimestamp = (Long) result[1];


                    if (lastKnownCount == -1) {
                        lastKnownCount = currentCount;
                    }


                    if (dbTimestamp > lastGlobalUpdate || currentCount != lastKnownCount) {
                        System.out.println("Change detected in barangay assets! Refreshing...");
                        lastGlobalUpdate = dbTimestamp;
                        lastKnownCount = currentCount;
                        loadData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


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
    // FILTER LOGIC (Separated Period and Year)
    // =========================================================================
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

        // 2. Specific Date Filter (Calendar Picker)
        if (selectedDate != null) {
            final java.util.Date finalSelectedDate = selectedDate;
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(3); // Date Acquired column (Index 3)
                        if (val == null) return false;

                        LocalDate rowDate;
                        if (val instanceof java.sql.Date) {
                            rowDate = ((java.sql.Date) val).toLocalDate();
                        } else {
                            rowDate = LocalDate.parse(val.toString());
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDateStr = sdf.format(finalSelectedDate);
                        LocalDate selectedLocalDate = LocalDate.parse(selectedDateStr);

                        return rowDate.equals(selectedLocalDate);

                    } catch (Exception e) {
                        return false;
                    }
                }
            });
        }

        // 3. Logic for PERIOD (This Week / This Month)
        if (selectedPeriod != null && !selectedPeriod.equals("All Periods") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(3); // Column 3
                        if (val == null) return false;

                        LocalDate rowDate;
                        if (val instanceof java.sql.Date) rowDate = ((java.sql.Date) val).toLocalDate();
                        else rowDate = LocalDate.parse(val.toString());

                        LocalDate now = LocalDate.now();

                        if (selectedPeriod.equals("This Week")) {
                            java.time.temporal.TemporalField fieldISO = java.time.temporal.WeekFields.of(java.util.Locale.FRANCE).dayOfWeek();
                            LocalDate startOfWeek = now.with(fieldISO, 1);
                            LocalDate endOfWeek = now.with(fieldISO, 7);
                            return !rowDate.isBefore(startOfWeek) && !rowDate.isAfter(endOfWeek);
                        } else if (selectedPeriod.equals("This Month")) {
                            return rowDate.getMonth() == now.getMonth() && rowDate.getYear() == now.getYear();
                        }
                        return true;
                    } catch (Exception e) { return false; }
                }
            });
        }

        // 4. Logic for YEAR (2025, 2024, etc.)
        if (selectedYear != null && !selectedYear.equals("All Years") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(3); // Column 3
                        if (val == null) return false;

                        LocalDate rowDate;
                        if (val instanceof java.sql.Date) rowDate = ((java.sql.Date) val).toLocalDate();
                        else rowDate = LocalDate.parse(val.toString());

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
    // CALENDAR IMPLEMENTATION (Exactly copied from SecretaryPrintDocument)
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
            calendar.setTime(new java.util.Date());
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
        int assetId = (int) tableModel.getValueAt(modelRow, 0);


        BarangayAsset asset = new BarangayAssetDAO().getAssetById(assetId);


        if (asset != null) {
            showDialog(asset, "Update Asset Details");
        } else {
            JOptionPane.showMessageDialog(this, "Error: Could not fetch asset details.");
        }
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
    // SEARCH FUNCTIONALITY
    // =========================================================================


    private void setupSearchFunctionality() {
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilters(); // Changed to use applyFilters instead of performSearch
            }
        });
    }


    private void performSearch() {
        applyFilters(); // Alias to maintain compatibility
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
        JPanel rightPrint = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPrint.setBackground(BG_COLOR);
        JButton btnPrint = createRoundedButton("🖨 Print Report", new Color(44, 62, 80));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(160, 40));
        btnPrint.addActionListener(e -> handlePrint());


        btns.add(btnAdd); btns.add(btnEdit); btns.add(btnDelete); btns.add(btnPrint);
        p.add(btns);
        p.add(Box.createVerticalStrut(20));


        // Search Panel with Calendar Filter (Added from SecretaryPrintDocument)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BG_COLOR);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 5, 5, 5)));

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });


        searchPanel.add(Box.createHorizontalStrut(10));

        // 1. Period Filter (This Week/Month)



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
        searchPanel.add(periodFilterBox);

        searchPanel.add(Box.createHorizontalStrut(10));

        // 2. Year Filter (2026, 2025...)
        JLabel lblYear = new JLabel("Year Filter:");
        lblYear.setFont(new Font("Arial", Font.BOLD, 14));
        searchPanel.add(lblYear);

        yearFilterBox = new JComboBox<>();
        yearFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        yearFilterBox.setBackground(Color.WHITE);
        yearFilterBox.setFocusable(false);

        yearFilterBox.addItem("All Years");
        int currentYear = LocalDate.now().getYear();
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
        searchPanel.add(yearFilterBox);

        searchPanel.add(Box.createHorizontalStrut(10));
        JLabel dateLabel = new JLabel("  Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));

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
        dateFilterBtn.addActionListener(e -> showModernDatePicker());

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
            applyFilters();
        });


        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        // Add search button
        JButton searchButton = new JButton("🔍");
        searchButton.setFont(new Font("Arial", Font.PLAIN, 14));
        searchButton.addActionListener(e -> applyFilters());
        searchButton.setToolTipText("Search");


        // Add clear button
        JButton clearButton = createRoundedButton("Clear", new Color(150, 150, 150));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            applyFilters();
        });
        JLabel lblFilter = new JLabel("Quick Filter: ");
        lblFilter.setFont(new Font("Arial", Font.BOLD, 14));




        periodFilterBox.addActionListener(e -> {
            // Reset specific date if a period/year is chosen
            if (!periodFilterBox.getSelectedItem().equals("All Time")) {
                selectedDate = null;
                dateFilterBtn.setText("📅 Select Date");
                dateFilterBtn.setForeground(Color.DARK_GRAY);
            }
            applyFilters();
        });

        searchPanel.add(clearButton);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(lblFilter);
        searchPanel.add(periodFilterBox);
        searchPanel.add(Box.createHorizontalStrut(20)); // Add some spacing
        searchPanel.add(dateLabel);
        searchPanel.add(dateFilterBtn);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(clearDateBtn);


        p.add(searchPanel);
        p.add(Box.createVerticalStrut(10));


        String[] cols = {"ID", "Item Name", "Property No", "Date Acquired", "Status", "Value"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);

        // --- TABLE STYLING ---
        table.setRowHeight(30);
        table.setFont(MAIN_FONT);


        // Header Colors
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));


        // Optional: Remove border if you want the exact look
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);


        p.add(new JScrollPane(table));


        lblRecordCount = new JLabel("Total: 0");
        p.add(lblRecordCount);


        return p;
    }

    private JComboBox<String> periodFilterBox;
    private JComboBox<String> yearFilterBox;
    private void updateRecordCount() {
        int count = table.getRowCount(); // Gets filtered count
        lblRecordCount.setText("Total Assets: " + count);
    }


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
                Document doc = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(doc, new FileOutputStream(file));
                doc.open();


                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                Paragraph title = new Paragraph("Asset Report", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);


                int colCount = table.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);


                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);
                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);


                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(table.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor);
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8);
                    pdfTable.addCell(cell);
                }


                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);


                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = table.getValueAt(i, j);
                        String text = (val != null) ? val.toString() : "";


                        PdfPCell cell = new PdfPCell(new Paragraph(text, rowFont));
                        cell.setPadding(6);
                        cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);


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


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(HEADER_BG);
                        g2.fillRoundRect(x, y, width, height, 30, 30);
                    }
                }, new EmptyBorder(25, 40, 25, 40)));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);


        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 26));
        lblSystem.setForeground(Color.WHITE);


        JLabel lblModule = new JLabel("Property & Equipment Management");
        lblModule.setFont(new Font("Arial", Font.BOLD, 22));
        lblModule.setForeground(Color.WHITE);


        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);


        return headerPanel;
    }
    // --- NUMERIC VALIDATION METHOD ---
    private void addNumericValidation(JTextField textField, boolean allowDecimal, int maxLength) {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String currentText = textField.getText();

                // Check length limit
                if (currentText.length() >= maxLength && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    return;
                }

                if (allowDecimal) {
                    // Allow digits, one decimal point, and backspace
                    if (!Character.isDigit(c) && c != '.' && c != KeyEvent.VK_BACK_SPACE) {
                        e.consume(); // Reject invalid character
                    }

                    // Allow only one decimal point
                    if (c == '.' && currentText.contains(".")) {
                        e.consume();
                    }
                } else {
                    // Allow only digits and backspace
                    if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                        e.consume(); // Reject invalid character
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // Visual feedback for validation
                String text = textField.getText().trim();
                if (text.isEmpty() || (allowDecimal && (text.equals("0") || text.equals("0.00"))) ||
                        (!allowDecimal && text.equals("0"))) {
                    textField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                } else {
                    textField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                }
            }
        });
    }

    // --- TEXT FIELD VALIDATION WITH MIN LENGTH ---
    private void addTextValidation(JTextField textField, int minLength, int maxLength, String fieldName) {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String currentText = textField.getText();

                // Check max length limit
                if (currentText.length() >= maxLength && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String text = textField.getText().trim();

                // Visual feedback based on validation
                if (text.isEmpty()) {
                    textField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                    textField.setToolTipText(fieldName + " cannot be empty");
                } else if (text.length() < minLength) {
                    textField.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
                    textField.setToolTipText(fieldName + " must be at least " + minLength + " characters");
                } else {
                    textField.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
                    textField.setToolTipText(null);
                }
            }
        });
    }

    // --- ALPHANUMERIC VALIDATION (for codes/serial numbers) ---
    private void addAlphanumericValidation(JTextField textField, int minLength, int maxLength, String fieldName) {
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                String currentText = textField.getText();

                // Check max length
                if (currentText.length() >= maxLength && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                // Allow letters, digits, hyphens, and backspace only
                if (!Character.isLetterOrDigit(c) && c != '-' && c != '_' && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String text = textField.getText().trim();

                if (text.isEmpty()) {
                    textField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                    textField.setToolTipText(fieldName + " cannot be empty");
                } else if (text.length() < minLength) {
                    textField.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
                    textField.setToolTipText(fieldName + " must be at least " + minLength + " characters");
                } else {
                    textField.setBorder(BorderFactory.createLineBorder(Color.GREEN, 1));
                    textField.setToolTipText(null);
                }
            }
        });
    }

    // --- COMPREHENSIVE VALIDATION BEFORE SAVE ---
    private boolean validateAllFields(JTextField txtName, JTextField txtPropCode, JTextField txtPropNo,
                                      JTextField txtSerial, JTextField txtBrand, JTextField txtModel,
                                      JTextField txtValue, JTextField txtLife, JTextField txtCustodian,
                                      JTextField txtLocation, JTextField txtFund, JDialog dialog) {

        // Item Name validation
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Item Name cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return false;
        }
        if (txtName.getText().trim().length() < 3) {
            JOptionPane.showMessageDialog(dialog, "Item Name must be at least 3 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        // Property Code validation
        if (txtPropCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Property Code cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPropCode.requestFocus();
            return false;
        }
        if (txtPropCode.getText().trim().length() < 5) {
            JOptionPane.showMessageDialog(dialog, "Property Code must be at least 5 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPropCode.requestFocus();
            return false;
        }

        // Property Number validation
        if (txtPropNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Property Number cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPropNo.requestFocus();
            return false;
        }
        if (txtPropNo.getText().trim().length() < 5) {
            JOptionPane.showMessageDialog(dialog, "Property Number must be at least 5 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPropNo.requestFocus();
            return false;
        }

        // Serial Number validation
        if (txtSerial.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Serial Number cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtSerial.requestFocus();
            return false;
        }
        if (txtSerial.getText().trim().length() < 5) {
            JOptionPane.showMessageDialog(dialog, "Serial Number must be at least 5 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtSerial.requestFocus();
            return false;
        }

        // Brand validation
        if (txtBrand.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Brand cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtBrand.requestFocus();
            return false;
        }
        if (txtBrand.getText().trim().length() < 2) {
            JOptionPane.showMessageDialog(dialog, "Brand must be at least 2 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtBrand.requestFocus();
            return false;
        }

        // Model validation
        if (txtModel.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Model cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtModel.requestFocus();
            return false;
        }
        if (txtModel.getText().trim().length() < 2) {
            JOptionPane.showMessageDialog(dialog, "Model must be at least 2 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtModel.requestFocus();
            return false;
        }

        // Value validation
        if (txtValue.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Acquisition Cost/Value cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtValue.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(txtValue.getText().trim());
            if (value <= 0) {
                JOptionPane.showMessageDialog(dialog, "Acquisition Cost/Value must be greater than 0!",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtValue.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Acquisition Cost/Value must be a valid number!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtValue.requestFocus();
            return false;
        }

        // Useful Life validation
        if (txtLife.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Useful Life cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtLife.requestFocus();
            return false;
        }
        try {
            int life = Integer.parseInt(txtLife.getText().trim());
            if (life <= 0) {
                JOptionPane.showMessageDialog(dialog, "Useful Life must be at least 1 year!",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtLife.requestFocus();
                return false;
            }
            if (life > 99) {
                JOptionPane.showMessageDialog(dialog, "Useful Life cannot exceed 99 years!",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                txtLife.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Useful Life must be a valid number!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtLife.requestFocus();
            return false;
        }

        // Fund Source validation
        if (txtFund.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Fund Source cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtFund.requestFocus();
            return false;
        }
        if (txtFund.getText().trim().length() < 3) {
            JOptionPane.showMessageDialog(dialog, "Fund Source must be at least 3 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtFund.requestFocus();
            return false;
        }

        // Custodian validation
        if (txtCustodian.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Custodian cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtCustodian.requestFocus();
            return false;
        }
        if (txtCustodian.getText().trim().length() < 3) {
            JOptionPane.showMessageDialog(dialog, "Custodian must be at least 3 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtCustodian.requestFocus();
            return false;
        }

        // Location validation
        if (txtLocation.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Location cannot be empty!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtLocation.requestFocus();
            return false;
        }
        if (txtLocation.getText().trim().length() < 3) {
            JOptionPane.showMessageDialog(dialog, "Location must be at least 3 characters!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtLocation.requestFocus();
            return false;
        }

        return true; // All validations passed
    }

// Usage in showDialog method - Add these after creating the text fields:

    // For Value field - max 12 digits (e.g., 999,999,999.99)

    // In the Save button action listener, replace the validation section with:
// if (!validateAllFields(txtName, txtPropCode, txtPropNo, txtSerial, txtBrand,
//                        txtModel, txtValue, txtLife, txtCustodian, txtLocation, txtFund, dialog)) {
//     return;
// }
    private void showDialog(BarangayAsset existing, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(600, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());


        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));


        // --- FIELDS ---
        JTextField txtName = createStyledTextField(existing != null ? existing.getItemName() : "");
        JTextField txtPropCode = createStyledTextField(existing != null ? existing.getPropertyCode() : "");
        JTextField txtPropNo = createStyledTextField(existing != null ? existing.getPropertyNumber() : "PROP-" + System.currentTimeMillis());
        JTextField txtSerial = createStyledTextField(existing != null ? existing.getSerialNumber() : "");
        JTextField txtBrand = createStyledTextField(existing != null ? existing.getBrand() : "");
        JTextField txtModel = createStyledTextField(existing != null ? existing.getModel() : "");


        // Dates
        String defaultDate = LocalDate.now().toString();
        JTextField txtDateAcquired = createStyledTextField(existing != null && existing.getDateAcquired() != null ? existing.getDateAcquired().toString() : defaultDate);
        JTextField txtPurchaseDate = createStyledTextField(existing != null && existing.getPurchaseDate() != null ? existing.getPurchaseDate().toString() : defaultDate);


        JTextField txtValue = createStyledTextField(existing != null ? String.valueOf(existing.getValue()) : "0.00");
        JTextField txtFund = createStyledTextField(existing != null ? existing.getFundSource() : "Barangay Fund");
        JTextField txtLife = createStyledTextField(existing != null ? String.valueOf(existing.getUsefulLifeYears()) : "1");


        String[] statuses = {"Good", "Damaged", "Lost", "For Repair", "Disposed"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setBackground(Color.WHITE);
        if(existing != null) cbStatus.setSelectedItem(existing.getStatus());


        JTextField txtCustodian = createStyledTextField(existing != null ? existing.getCustodian() : "");
        JTextField txtLocation = createStyledTextField(existing != null ? existing.getLocation() : "Barangay Hall");
        addNumericValidation(txtValue, true, 12);

        // For Useful Life field - max 2 digits (up to 99 years)
        addNumericValidation(txtLife, false, 2);

        // Add text validations:
        addTextValidation(txtName, 3, 100, "Item Name");
        addTextValidation(txtBrand, 2, 50, "Brand");
        addTextValidation(txtModel, 2, 50, "Model");
        addTextValidation(txtCustodian, 3, 100, "Custodian");
        addTextValidation(txtLocation, 3, 100, "Location");
        addTextValidation(txtFund, 3, 100, "Fund Source");

        // Add alphanumeric validations for codes:
        addAlphanumericValidation(txtPropCode, 5, 20, "Property Code");
        addAlphanumericValidation(txtPropNo, 5, 30, "Property Number");
        addAlphanumericValidation(txtSerial, 5, 30, "Serial Number");

        // --- ADD ROWS ---
        addStyledRow(formPanel, "Item Name:", txtName);
        addStyledRow(formPanel, "Property Code:", txtPropCode);
        addStyledRow(formPanel, "Property Number:", txtPropNo);
        addStyledRow(formPanel, "Serial Number:", txtSerial);
        addStyledRow(formPanel, "Brand:", txtBrand);
        addStyledRow(formPanel, "Model:", txtModel);


        addStyledRow(formPanel, "Date Acquired:", createDatePickerPanel(txtDateAcquired));
        addStyledRow(formPanel, "Purchase Date:", createDatePickerPanel(txtPurchaseDate));


        addStyledRow(formPanel, "Acquisition Cost/Value:", txtValue);
        addStyledRow(formPanel, "Fund Source:", txtFund);
        addStyledRow(formPanel, "Useful Life (Years):", txtLife);
        addStyledRow(formPanel, "Current Status:", cbStatus);
        addStyledRow(formPanel, "Custodian:", txtCustodian);
        addStyledRow(formPanel, "Location:", txtLocation);


        // --- SAVE LOGIC ---
        JButton btnSave = createRoundedButton("Save Asset", BTN_ADD_COLOR);
        btnSave.addActionListener(e -> {
            if (!validateAllFields(txtName, txtPropCode, txtPropNo, txtSerial, txtBrand,
                    txtModel, txtValue, txtLife, txtCustodian, txtLocation, txtFund, dialog)) {
                return; // Stop if validation fails
            }
            try {
                BarangayAsset asset = new BarangayAsset();
                asset.setItemName(txtName.getText());
                asset.setPropertyCode(txtPropCode.getText());
                asset.setPropertyNumber(txtPropNo.getText());
                asset.setSerialNumber(txtSerial.getText());
                asset.setBrand(txtBrand.getText());
                asset.setModel(txtModel.getText());
                asset.setDateAcquired(Date.valueOf(txtDateAcquired.getText()));
                try { asset.setPurchaseDate(Date.valueOf(txtPurchaseDate.getText())); } catch (Exception ex) { asset.setPurchaseDate(null); }
                asset.setValue(Double.parseDouble(txtValue.getText()));
                asset.setFundSource(txtFund.getText());
                asset.setUsefulLifeYears(Integer.parseInt(txtLife.getText()));
                asset.setStatus(cbStatus.getSelectedItem().toString());
                asset.setCustodian(txtCustodian.getText());
                asset.setLocation(txtLocation.getText());
                int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
                if (existing == null) {
                    new BarangayAssetDAO().addAsset(asset);
                    try { new SystemLogDAO().addLog("Added Asset " + asset.getItemName(), "Admin", staffId); } catch(Exception ex){}
                } else {
                    asset.setAssetId(existing.getAssetId());
                    new BarangayAssetDAO().updateAsset(asset);
                    new SystemLogDAO().addLog("Updated Asset " + asset.getItemName(), "Admin", staffId);
                }
                JOptionPane.showMessageDialog(dialog, "Saved Successfully!");
                loadData();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: Check date format or numbers.");
            }
        });


        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnSave);


        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }


    // --- DATE PICKER HELPERS ---
    private void showDatePicker(JTextField targetField) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        AssetCalendarDialog calendar = new AssetCalendarDialog(parentWindow, targetField);
        calendar.setVisible(true);
    }


    private JPanel createDatePickerPanel(JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.WHITE);


        JButton btn = new JButton("📅");
        btn.setPreferredSize(new Dimension(45, 25));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showDatePicker(field));


        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
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


// =========================================================================
// ROUND BORDER CLASS (Copied from SecretaryPrintDocument)
// =========================================================================
class RoundBorder extends AbstractBorder {
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


class AssetCalendarDialog extends JDialog {
    private LocalDate currentDate;
    private JTextField targetField;
    private JLabel lblMonthYear;
    private JPanel daysPanel;


    public AssetCalendarDialog(Window parent, JTextField targetField) {
        super(parent, "Select Date", ModalityType.APPLICATION_MODAL);
        this.targetField = targetField;


        try {
            if (!targetField.getText().isEmpty()) {
                currentDate = LocalDate.parse(targetField.getText());
            } else {
                currentDate = LocalDate.now();
            }
        } catch (Exception e) {
            currentDate = LocalDate.now();
        }


        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());


        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        JButton btnPrev = new JButton(" < ");
        JButton btnNext = new JButton(" > ");
        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Arial", Font.BOLD, 16));


        btnPrev.addActionListener(e -> { currentDate = currentDate.minusMonths(1); updateCalendar(); });
        btnNext.addActionListener(e -> { currentDate = currentDate.plusMonths(1); updateCalendar(); });


        headerPanel.add(btnPrev, BorderLayout.WEST);
        headerPanel.add(lblMonthYear, BorderLayout.CENTER);
        headerPanel.add(btnNext, BorderLayout.EAST);


        daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        daysPanel.setBackground(Color.WHITE);
        daysPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        add(headerPanel, BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);


        JButton btnToday = new JButton("Today");
        btnToday.addActionListener(e -> {
            targetField.setText(LocalDate.now().toString());
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
        daysPanel.removeAll();


        lblMonthYear.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));


        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Arial", Font.BOLD, 12));
            daysPanel.add(l);
        }


        java.time.YearMonth yearMonth = java.time.YearMonth.of(currentDate.getYear(), currentDate.getMonth());
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();


        int emptySlots = (dayOfWeek == 7) ? 0 : dayOfWeek;


        for (int i = 0; i < emptySlots; i++) {
            daysPanel.add(new JLabel(""));
        }


        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            int finalDay = day;
            JButton btn = new JButton(String.valueOf(day));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);


            if (currentDate.withDayOfMonth(finalDay).equals(LocalDate.now())) {
                btn.setForeground(Color.BLUE);
                btn.setFont(new Font("Arial", Font.BOLD, 12));
            }


            btn.addActionListener(e -> {
                LocalDate selected = currentDate.withDayOfMonth(finalDay);
                targetField.setText(selected.toString());
                dispose();
            });
            daysPanel.add(btn);
        }


        daysPanel.revalidate();
        daysPanel.repaint();
    }
}

