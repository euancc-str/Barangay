package org.example.Interface;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Admin.SystemLogDAO;
import org.example.BlotterCaseDAO;
import org.example.Captain.Captain4PsTab;
import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;
import org.example.utils.AutoRefresher;
// Assuming this is where your printer class is
// import org.example.treasurer.ReceiptPrinter; // Keep if used elsewhere

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.time.format.DateTimeFormatter;

public class SecretaryPrintDocument extends JPanel {
    private JTable requestTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // New Fields for Filtering
    private JTextField searchField;
    private JComboBox<String> statusFilterBox;
    private JButton dateFilterBtn; // Changed from JComboBox to JButton
    private Date selectedDate; // Added for calendar selection

    private JLabel lblRecordCount;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94);
    private final Color BTN_UPDATE_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private final Color BTN_DELETE_COLOR = new Color(255, 77, 77);   // Red
    private final Color MODERN_BLUE = new Color(66, 133, 244);
    private final Color LIGHT_GREY = new Color(248, 249, 250);
    private final Color DARK_GREY = new Color(52, 58, 64);

    public SecretaryPrintDocument() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        loadRequestData();
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                if (refresher != null) {
                    refresher.stop();
                }
                refresher = new AutoRefresher("Document", SecretaryPrintDocument.this::loadRequestData);
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
    private javax.swing.Timer lightTimer;

    private void startLightPolling() {
        lightTimer = new javax.swing.Timer(3000, e -> { // Every 3 seconds
            if (requestTable != null && requestTable.getSelectedRow() == -1) {
                checkLightUpdate();
            }
        });
        lightTimer.start();
    }

    private static volatile long lastGlobalUpdate = System.currentTimeMillis();
    private volatile long lastCheckedTime = 0;

    private void checkLightUpdate() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Store the time BEFORE checking
                long checkStartTime = System.currentTimeMillis();

                String sql = "SELECT COUNT(*) as change_count FROM document_request " +
                        "WHERE updatedAt > FROM_UNIXTIME(?)";

                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setLong(1, lastCheckedTime / 1000L);

                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        int changes = rs.getInt("change_count");

                        // Only update the timestamp AFTER we've checked
                        if (changes > 0) {
                            lastCheckedTime = checkStartTime;
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadRequestData();
                        // Move this AFTER the check, not before
                        lastGlobalUpdate = System.currentTimeMillis();
                        System.out.println("✅ Changes detected - refreshing!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // =========================================================================
    //  FIXED DATA LOADING (Safe Threading)
    // =========================================================================
    public void loadRequestData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                ResidentDAO residentDAO = new ResidentDAO();
                List<DocumentRequest> rawList = residentDAO.getAllResidentsDocument();
                List<Object[]> processedRows = new ArrayList<>();

                for (DocumentRequest doc : rawList) {
                    if (doc != null) {
                        Object dateObj = doc.getRequestDate();
                        LocalDateTime finalDate = null;
                        try {
                            if (dateObj instanceof java.sql.Timestamp) {
                                finalDate = ((java.sql.Timestamp) dateObj).toLocalDateTime();
                            } else if (dateObj != null) {
                                String s = dateObj.toString().replace("T", " ");
                                if (s.length() >= 19) finalDate = LocalDateTime.parse(s.substring(0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                else if (s.length() >= 16) finalDate = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                else if (s.length() >= 10) finalDate = LocalDate.parse(s.substring(0, 10)).atStartOfDay();
                            }
                        } catch (Exception ignored) {}

                        processedRows.add(new Object[]{
                                "" + doc.getRequestId(),
                                doc.getFullName(),
                                doc.getName(),
                                doc.getPurpose(),
                                doc.getStatus(),
                                finalDate
                        });
                    }
                }
                return processedRows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    if (tableModel != null) {
                        // Safely clear and reload
                        RowFilter<? super DefaultTableModel, ? super Integer> currentFilter = null;
                        if (sorter != null) {
                            currentFilter = sorter.getRowFilter();
                            sorter.setRowFilter(null);
                        }

                        tableModel.setRowCount(0);
                        for (Object[] row : rows) {
                            tableModel.addRow(row);
                        }

                        if (sorter != null) sorter.setRowFilter(currentFilter);
                        if (statusFilterBox != null) applyFilters();
                    }
                    updateRecordCount();
                    if (requestTable != null) requestTable.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // =========================================================================
    //  FILTER LOGIC with Regex Enhancement
    // =========================================================================
    private void applyFilters() {
        if (sorter == null) return;

        String text = searchField.getText();
        String status = (String) statusFilterBox.getSelectedItem();

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

        // Status filter with exact match regex
        if (status != null && !status.equals("All Status")) {
            filters.add(RowFilter.regexFilter("(?i)^" + java.util.regex.Pattern.quote(status) + "$", 4));
        }

        // Date filter using selectedDate
        if (selectedDate != null) {
            final Date finalSelectedDate = selectedDate;
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(5);
                        if (!(val instanceof LocalDateTime)) return false;

                        LocalDate rowDate = ((LocalDateTime) val).toLocalDate();
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

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        updateRecordCount();
    }

    // =========================================================================
    //  CALENDAR IMPLEMENTATION (Similar to DashboardPanel)
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
    //  2. UPDATE FUNCTIONALITY
    // =========================================================================
    private void handleUpdate() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = requestTable.convertRowIndexToModel(selectedRow);

        // Retrieve Data
        String currentId = (String) tableModel.getValueAt(modelRow, 0);
        String currentName = (String) tableModel.getValueAt(modelRow, 1);
        String currentDoc = (String) tableModel.getValueAt(modelRow, 2);
        String currentPurpose = (String) tableModel.getValueAt(modelRow, 3);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 4);

        // --- BUILD THE "NICE GUI" DIALOG ---
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Request", true);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 40));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 40, 70, 40));

        // Header
        JLabel titleLabel = new JLabel("Update Request #" + currentId, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(HEADER_BG);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Content
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);

        // 1. Read-Only Fields (Context)
        JTextField txtName = createStyledTextField(currentName);
        txtName.setEditable(false);
        txtName.setBackground(new Color(250, 250, 250));
        addStyledRow(detailsPanel, "Resident Name:", txtName);

        JTextField txtDoc = createStyledTextField(currentDoc);
        txtDoc.setEditable(false);
        txtDoc.setBackground(new Color(250, 250, 250));
        addStyledRow(detailsPanel, "Document Type:", txtDoc);

        // 2. Editable Fields
        String[] statuses = {"Awaiting approval", "confirmed payment", "Rejected", "Paid", "Approved", "Released"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setSelectedItem(currentStatus);
        cbStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        cbStatus.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Status:", cbStatus);

        JTextArea txtPurposeArea = new JTextArea(currentPurpose);
        txtPurposeArea.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPurposeArea.setLineWrap(true);
        txtPurposeArea.setEditable(false);
        txtPurposeArea.setBackground(new Color(250, 250, 250));
        txtPurposeArea.setWrapStyleWord(true);
        txtPurposeArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));

        JScrollPane scrollPurpose = new JScrollPane(txtPurposeArea);
        scrollPurpose.setPreferredSize(new Dimension(200, 80));
        addStyledRow(detailsPanel, "Purpose:", scrollPurpose);

        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = createRoundedButton("Cancel", Color.GRAY);
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = createRoundedButton("Print document?", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(200, 45));
        JButton btnDelete = createRoundedButton("Delete", Color.RED);
        btnDelete.setPreferredSize(new Dimension(150, 45));
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "<html><body><b style='color:red'>WARNING:</b><br>" +
                            "Are you sure you want to delete this request?<br>" +
                            "This action cannot be undone.</body></html>",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                new StaffDAO().deleteRequest(Integer.parseInt(currentId));

                JOptionPane.showMessageDialog(dialog, "Request deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                loadRequestData(); // Refresh the list
            }
        });

        // CHECK STATUS FOR PRINTING
        // Allow printing if Paid, Approved, or Released (basically anything after payment)
        if(currentStatus.equalsIgnoreCase("confirmed payment") ||
                currentStatus.equalsIgnoreCase("Paid") ||
                currentStatus.equalsIgnoreCase("Approved") ||
                currentStatus.equalsIgnoreCase("Released")){

            btnSave.addActionListener(e -> {
                // Finding resident ID by name (Ensure this method exists in your DAO)
                ResidentDAO resDAO = new ResidentDAO();
                int residentId = resDAO.findResidentsIdByFullName(currentName);

                showDocumentPreview(currentName, currentDoc, currentPurpose, Integer.parseInt(currentId), residentId);
                dialog.dispose();
            });
        } else {
            btnSave.setVisible(false);
            JLabel lblStatusInfo = new JLabel("Payment not confirmed yet");
            lblStatusInfo.setForeground(Color.RED);
            btnPanel.add(lblStatusInfo);
        }

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        btnPanel.add(btnDelete);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void addStyledRow(JPanel panel, String labelText, JComponent field) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (field instanceof JScrollPane) ? 90 : 50));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(new Color(80, 80, 80));
        label.setPreferredSize(new Dimension(150, 35));
        if (field instanceof JScrollPane) label.setVerticalAlignment(SwingConstants.TOP);

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(Color.WHITE);
        fieldWrapper.setBorder(new EmptyBorder(5, 0, 15, 0));
        fieldWrapper.add(field, BorderLayout.CENTER);

        rowPanel.add(label, BorderLayout.WEST);
        rowPanel.add(fieldWrapper, BorderLayout.CENTER);

        panel.add(rowPanel);
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    // =========================================================================
    // PRINT PREVIEW & PHOTO LOGIC
    // =========================================================================
    private void showDocumentPreview(String name, String docType, String purpose, int requestId, int residentId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Document Preview", true);
        dialog.setSize(600, 750);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        // --- A. THE DOCUMENT PAPER ---
        JPanel paper = new JPanel();
        paper.setLayout(new BoxLayout(paper, BoxLayout.Y_AXIS));
        paper.setBackground(Color.WHITE);
        paper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(50, 50, 50, 50)
        ));

        // 1. Header
        JLabel header = new JLabel("REPUBLIC OF THE PHILIPPINES");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setFont(new Font("Serif", Font.PLAIN, 16));

        JLabel subHeader = new JLabel("OFFICE OF THE BARANGAY CAPTAIN");
        subHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        subHeader.setFont(new Font("Serif", Font.BOLD, 14));

        // 2. Title
        JLabel title = new JLabel(docType.toUpperCase());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        // 3. Body
        JTextArea body = new JTextArea();
        body.setText("TO WHOM IT MAY CONCERN:\n\n" +
                "This is to certify that " + name.toUpperCase() + ", of legal age, " +
                "is a resident of this Barangay.\n\n" +
                "This certification is issued upon request for the purpose of: \n" +
                purpose.toUpperCase() + ".\n\n" +
                "Issued this " + java.time.LocalDate.now() + ".");

        body.setFont(new Font("Serif", Font.PLAIN, 14));
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Photo Section (Only for Barangay Clearance)
        if (docType.trim().equalsIgnoreCase("Barangay Clearance")) {
            JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            photoPanel.setBackground(Color.WHITE);
            photoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            photoPanel.setPreferredSize(new Dimension(600, 110));
            photoPanel.setMaximumSize(new Dimension(600, 110));

            JLabel lblPhoto = new JLabel();
            lblPhoto.setPreferredSize(new Dimension(100, 100)); // Passport size-ish
            lblPhoto.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            lblPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Show hand cursor so they know it's clickable
            lblPhoto.setToolTipText("Click to Update Photo");

            // FETCH & DISPLAY PHOTO
            PhotoDAO resDao = new PhotoDAO();
            String currentPhotoPath = resDao.getPhotoPath(residentId);
            // *** FIXED: Pass explicit dimensions to avoid error ***
            ImageUtils.displayImage(lblPhoto, currentPhotoPath, 100, 100);

            // CLICK TO EDIT PHOTO
            lblPhoto.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));

                    int res = chooser.showOpenDialog(dialog);
                    if (res == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = chooser.getSelectedFile();

                        // 1. Save file to folder
                        String newFileName = ImageUtils.saveImage(selectedFile, String.valueOf(residentId));

                        // 2. Update Database
                        resDao.updateResidentPhoto(residentId, newFileName);

                        // 3. Refresh Label immediately
                        ImageUtils.displayImage(lblPhoto, newFileName, 100, 100);

                        JOptionPane.showMessageDialog(dialog, "Photo Updated Successfully!");
                    }
                }
            });

            photoPanel.add(lblPhoto);
            paper.add(photoPanel);
        }

        // 5. Signatories
        JPanel sigPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sigPanel.setBackground(Color.WHITE);

        // Fetch Captain from DB if possible
        BarangayStaff staffDAO = new StaffDAO().findStaffByPosition("Brgy.Captain");
        String fullName = "";
        if (staffDAO != null) {
            String fName = staffDAO.getFirstName() != null ? staffDAO.getFirstName() : "";
            String mName = staffDAO.getMiddleName() != null ? staffDAO.getMiddleName() : "";
            String lName = staffDAO.getLastName() != null ? staffDAO.getLastName() : "";
            fullName = fName + " " + mName + " " + lName;
        } else {
            fullName = "HON. CARDO DALISAY"; // Fallback
        }

        JLabel sig = new JLabel("<html><center>"+fullName+"<br>Barangay Captain</center></html>");
        sig.setFont(new Font("Serif", Font.BOLD, 14));
        sigPanel.add(sig);

        // Assemble Paper
        paper.add(header);
        paper.add(subHeader);
        paper.add(title);
        // Photo added above if condition met
        paper.add(body);
        paper.add(sigPanel);

        // --- B. CONTROLS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrint = new JButton("Print & Release");
        btnPrint.setBackground(new Color(76, 175, 80));
        btnPrint.setForeground(Color.WHITE);

        // Inside showDocumentPreview(...)
        btnPrint.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName(docType + " - " + name);

            // 1. SET PAPER SIZE (Standard A4 or Letter)
            PageFormat pf = job.defaultPage();
            Paper paperSettings = pf.getPaper();
            paperSettings.setImageableArea(0, 0, paperSettings.getWidth(), paperSettings.getHeight()); // No margins, let printer handle it
            pf.setPaper(paperSettings);
            String ctcPlace = new SystemConfigDAO().getConfig("defaultCtcPlace");
            // 2. SELECT PRINTER BASED ON DOCUMENT TYPE
            ResidentDAO dao = new ResidentDAO();
            org.example.Users.Resident res = dao.findResidentById(residentId);

            new SystemLogDAO().addLog("Printed Document for ",name, Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
            if (docType.equalsIgnoreCase("Barangay Clearance")) {
                if (res != null) {

                    BlotterCaseDAO blotterDao = new BlotterCaseDAO();
                    String crimeRecord = blotterDao.checkMultiplePeople(name);
                    String finalFindings = "";
                    if (crimeRecord == null || crimeRecord.equals("CLEAN") || crimeRecord.isEmpty() || crimeRecord.contains("SETTLED") || crimeRecord.contains("DISMISSED")) {
                        finalFindings = "NO DEROGATORY RECORD";
                    } else if (crimeRecord.contains("PENDING")) {
                        finalFindings = "With pending barangay blotter case";
                    } else{
                        finalFindings = crimeRecord;
                    }
                    job.setPrintable(new org.example.Interface.BarangayClearancePrinter(
                            res.getFirstName() + " "+res.getMiddleName()+ " " + res.getLastName(),
                            res.getAddress(),
                            res.getGender(),
                            res.getDob() != null ? res.getDob().toString() : "N/A",
                            String.valueOf(res.getAge()),
                            res.getCivilStatus(),
                            purpose,
                            res.getCtcNumber(),
                            res.getCtcDateIssued() != null ? res.getCtcDateIssued().toString() : "", // CTC Date
                            ctcPlace,
                            residentId,
                            requestId,finalFindings
                    ), pf);
                }
            }else if (docType.equalsIgnoreCase("Business Clearance")) {

                job.setPrintable(new BusinessClearancePrinter(
                        name, // Proprietor Name
                        res.getAddress(),
                        purpose, // Contains Business Details/Nature
                        res.getCtcNumber(),
                        res.getCtcDateIssued() != null ? res.getCtcDateIssued().toString() : "",
                        ctcPlace,requestId
                ), pf);
            }
            else {

                Resident resident = new ResidentDAO().findResidentById(residentId);
                String purok= resident.getPurok() != null? resident.getPurok():"";
                int age = resident.getAge();
                job.setPrintable(new org.example.Interface.DocumentPrinter(name, docType, purpose, purok,age), pf);
            }

            // 3. SHOW PRINT DIALOG
            if (job.printDialog()) {
                try {
                    job.print();
                    // 4. Update Status in DB
                    markAsReleased(requestId);
                    dialog.dispose();
                } catch (PrinterException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Printing Error: " + ex.getMessage());
                }
            }
        });

        btnPanel.add(btnPrint);

        dialog.add(new JScrollPane(paper), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void printPanel(JPanel panelToPrint) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Barangay Document Print");

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics pg, PageFormat pf, int pageNum) {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;

                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());

                // Scale the panel to fit the page
                double scaleX = pf.getImageableWidth() / panelToPrint.getWidth();
                double scaleY = pf.getImageableHeight() / panelToPrint.getHeight();
                double scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
                g2.scale(scale, scale);

                panelToPrint.printAll(g2); // Print the component
                return Printable.PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Printing Failed: " + e.getMessage());
            }
        }
    }

    private void markAsReleased(int requestId) {
        // SQL to update status and timestamp
        String sql = "UPDATE document_request SET status = 'Released', releasedDate = NOW() WHERE requestId = ?";

        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Document Status updated to Released!");

            // REFRESH YOUR TABLE HERE
            loadRequestData();
            startLightPolling();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Color CERULEAN_BLUE = new Color(100, 149, 237);

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // --- 1. ACTION BUTTONS ---
        JButton btnRefresh = createRoundedButton("Refresh", new Color(100, 100, 100));
        btnRefresh.setPreferredSize(new Dimension(80, 30));
        btnRefresh.addActionListener(e -> loadRequestData());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        contentPanel.add(buttonPanel);
        contentPanel.add(btnRefresh);
        contentPanel.add(Box.createVerticalStrut(30));

        // --- 2. SEARCH & FILTER PANEL (Updated with Calendar) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Label
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JButton btnPrint = new JButton("🖨 Print Report") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, CERULEAN_BLUE,
                        getWidth(), getHeight(), new Color(70, 130, 180)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2d);
            }
        };
        btnPrint.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setPreferredSize(new Dimension(150, 35));
        btnPrint.setBorderPainted(false);
        btnPrint.setContentAreaFilled(false);
        btnPrint.setOpaque(false);
        btnPrint.addActionListener(e -> handlePrint());
        buttonPanel.add(btnPrint);

        // Search Field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 5, 5, 5)));

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters(); // Call helper method
            }
        });

        // Status Filter Box (Existing)
        String[] filters = {"All Status", "Pending", "Approved", "Released"};
        statusFilterBox = new JComboBox<>(filters);
        statusFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        statusFilterBox.setBackground(Color.WHITE);
        statusFilterBox.setPreferredSize(new Dimension(150, 30));
        statusFilterBox.addActionListener(e -> applyFilters());

        // [REPLACED] Date Filter with Calendar Button
        JLabel dateLabel = new JLabel("  Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));

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
        searchPanel.add(new JLabel("  Status:"));
        searchPanel.add(statusFilterBox);
        searchPanel.add(dateLabel);
        searchPanel.add(dateFilterBtn);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(clearDateBtn);

        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- 3. TABLE SETUP ---
        String[] columnNames = {"Request ID", "Resident Name", "Document Type", "Purpose", "Status", "Date"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 14));
        requestTable.setRowHeight(50);
        requestTable.setGridColor(new Color(173, 216, 230));
        requestTable.setSelectionBackground(new Color(200, 240, 240));
        requestTable.setBackground(new Color(144,213,255));
        requestTable.setShowVerticalLines(true);
        requestTable.setShowHorizontalLines(true);

        requestTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleUpdate();
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        requestTable.setRowSorter(sorter);

        // SORT BY DATE (Index 5) DEFAULT
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(5, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.addRowSorterListener(e -> updateRecordCount());

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

        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        // Create custom header renderer with cerulean gradient light blue
        header.setDefaultRenderer(new GradientHeaderRenderer());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(new Color(229, 231, 235));

        lblRecordCount = new JLabel("Total Records: " + tableModel.getRowCount());
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        footerPanel.add(lblRecordCount);

        contentPanel.add(footerPanel);
        contentPanel.add(tableScrollPane);
        return contentPanel;
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
                // 1. Create Document (Landscape)
                Document doc = new Document(PageSize.A4.rotate()); // ✅ Fixed: lowercase 'rotate()'
                PdfWriter.getInstance(doc, new FileOutputStream(file));
                doc.open();

                // 2. Add Title
                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                Paragraph title = new Paragraph("Document Request Master List", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);

                // 3. Create Table
                int colCount = requestTable.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);

                // 4. Add Headers (✅ MATCHING YOUR TABLE COLORS)
                // We use the same color: new Color(52, 152, 219)
                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);

                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);

                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(requestTable.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor); // ✅ Blue Background
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8); // More padding like your table
                    pdfTable.addCell(cell);
                }

                // 5. Add Rows
                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

                for (int i = 0; i < requestTable.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = requestTable.getValueAt(i, j);
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

    // Custom Table Header Renderer with Cerulean Gradient Light Blue
    private class GradientHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Arial", Font.BOLD, 15));
            setForeground(Color.BLACK);
            setOpaque(true);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Cerulean gradient light blue colors
            Color startColor = new Color(173, 216, 230); // Light blue
            Color endColor = new Color(70, 130, 180);   // Cerulean blue

            GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    getWidth(), getHeight(), endColor
            );

            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Paint the text
            super.paintComponent(g2d);
            g2d.dispose();
        }
    }

    private void updateRecordCount() {
        int count = requestTable.getRowCount(); // Gets filtered count
        lblRecordCount.setText("Total Records: " + count);
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
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));

        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(70, 130, 180));
                        g2.fillRoundRect(x, y, width, height, 30, 30);
                    }
                }, new EmptyBorder(25, 40, 25, 40)));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 26));
        lblSystem.setForeground(Color.WHITE);
        JLabel lblModule = new JLabel("Print Documents");
        lblModule.setFont(new Font("Arial", Font.BOLD, 22));
        lblModule.setForeground(Color.WHITE);
        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            JFrame frame = new JFrame("Print Request Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new SecretaryPrintDocument());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    // =========================================================================
    //  HELPER CLASSES FOR STYLING
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

