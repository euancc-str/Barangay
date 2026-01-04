// AdminRequestTab.java - Updated with exact calendar/date filter from SecretaryPrintDocument
package org.example.Admin;


import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.utils.AutoRefresher;


// OpenPDF Imports
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


import java.io.File;
import java.io.FileOutputStream;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;


public class AdminRequestTab extends JPanel {


    private JTable requestTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;


    // New Fields for Filtering
    private JTextField searchField;
    private JComboBox<String> statusFilterBox;
    private JButton dateFilterBtn;
    private Date selectedDate;


    // --- UPDATED VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color BTN_UPDATE_COLOR = new Color(41, 128, 185);
    private final Color BTN_DELETE_COLOR = new Color(231, 76, 60);
    private final Color MODERN_BLUE = new Color(66, 133, 244);
    private final Color LIGHT_GREY = new Color(248, 249, 250);
    private final Color DARK_GREY = new Color(52, 58, 64);
    private final Color CERULEAN_BLUE = new Color(100, 149, 237);


    private JLabel lblRecordCount;


    public AdminRequestTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        // Load data AFTER components are created
        SwingUtilities.invokeLater(() -> {
            loadRequestData();
            updateRecordCount();
        });

        // Ensure table text is black for readability
        if (requestTable != null) {
            requestTable.setForeground(Color.BLACK);
            JTableHeader rh = requestTable.getTableHeader();
            if (rh != null)
                rh.setForeground(Color.BLACK);
        }
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                if (refresher != null) {
                    refresher.stop();
                }
                refresher = new AutoRefresher("Document", AdminRequestTab.this::loadRequestData);
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
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }


    private AutoRefresher refresher;
    private javax.swing.Timer lightTimer;
    private static volatile long lastGlobalUpdate = System.currentTimeMillis();
    private long lastKnownCount = -1;


    public void loadRequestData() {
        System.out.println("[" + new Date() + "] Loading request data...");
        new SwingWorker<List<DocumentRequest>, Void>() {
            @Override
            protected List<DocumentRequest> doInBackground() throws Exception {
                ResidentDAO residentDAO = new ResidentDAO();
                return residentDAO.getAllResidentsDocument();
            }


            @Override
            protected void done() {
                try {
                    List<DocumentRequest> documentRequestList = get();


                    if (tableModel != null) {
                        // Clear existing data
                        tableModel.setRowCount(0);

                        // Add new data
                        for (DocumentRequest document : documentRequestList) {
                            if (document != null) {
                                String id = "" + document.getRequestId();
                                tableModel.addRow(new Object[] {
                                        id,
                                        document.getFullName(),
                                        document.getName(),
                                        document.getPurpose(),
                                        document.getStatus(),
                                        document.getRequestDate()
                                });
                            }
                        }

                        // Re-apply filters after loading new data
                        applyFilters();

                        // Update record count
                        updateRecordCount();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    private void applyFilters() {
        if (sorter == null) return;

        String text = searchField.getText();
        String status = (String) statusFilterBox.getSelectedItem();
        String selectedPeriod = (String) periodFilterBox.getSelectedItem();
        String selectedYear = (String) yearFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Text Search
        if (text != null && !text.trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + text.trim()));
        }

        // 2. Status Filter (Column 4)
        if (status != null && !status.equals("All Status")) {
            filters.add(RowFilter.regexFilter("(?i)^" + status + "$", 4));
        }

        // 3. Calendar Date Filter (Column 5)
        if (selectedDate != null) {
            final Date finalSelectedDate = selectedDate;
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(5); // Date Column
                        if (!(val instanceof LocalDateTime)) return false;

                        LocalDate rowDate = ((LocalDateTime) val).toLocalDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String selectedDateStr = sdf.format(finalSelectedDate);
                        LocalDate selectedLocalDate = LocalDate.parse(selectedDateStr);

                        return rowDate.equals(selectedLocalDate);
                    } catch (Exception e) { return false; }
                }
            });
        }

        // 4. Period Filter (This Week / This Month)
        if (selectedPeriod != null && !selectedPeriod.equals("All Periods") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(5); // Date Column
                        if (!(val instanceof LocalDateTime)) return false;

                        LocalDate rowDate = ((LocalDateTime) val).toLocalDate();
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

        // 5. Year Filter (2025, 2024...)
        if (selectedYear != null && !selectedYear.equals("All Years") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(5); // Date Column
                        if (!(val instanceof LocalDateTime)) return false;

                        LocalDate rowDate = ((LocalDateTime) val).toLocalDate();
                        int targetYear = Integer.parseInt(selectedYear);

                        return rowDate.getYear() == targetYear;
                    } catch (Exception e) { return false; }
                }
            });
        }

        if (sorter != null) {
            sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        }
        updateRecordCount();
    }
    // =========================================================================
    //  CALENDAR IMPLEMENTATION (EXACT COPY FROM SecretaryPrintDocument)
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

        // Initialize calendar (EXACT COPY FROM SecretaryPrintDocument)
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

        // Today button (EXACT COPY FROM SecretaryPrintDocument)
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
                // If no date selected, use current date (EXACT COPY FROM SecretaryPrintDocument)
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

    // =========================================================================
    //  CREATE DAY BUTTON (EXACT COPY FROM SecretaryPrintDocument)
    // =========================================================================
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

        // DAY BUTTON ACTION LISTENER (EXACT COPY FROM SecretaryPrintDocument)
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
    //  DELETE FUNCTIONALITY
    // =========================================================================
    private void handleDelete() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }


        int modelRow = requestTable.convertRowIndexToModel(selectedRow);
        String reqId = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);


        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><body style='width: 250px;'>" +
                        "<b>WARNING: Irreversible Action</b><br><br>" +
                        "Are you sure you want to DELETE Request <b>#" + reqId + "</b><br>" +
                        "for <b>" + name + "</b>?<br><br>" +
                        "This cannot be undone.</body></html>",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);


        if (confirm == JOptionPane.YES_OPTION) {
            new StaffDAO().deleteRequest(Integer.parseInt(reqId));
            tableModel.removeRow(modelRow);
            JOptionPane.showMessageDialog(this, "Record deleted successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            new SystemLogDAO().addLog("Deleted a Document", name, Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));

            loadRequestData();
        }
    }


    // =========================================================================
    //  UPDATE FUNCTIONALITY
    // =========================================================================
    private void handleUpdate() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to update.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
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
        String[] statuses = { "Pending", "Approved", "Released" };
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setSelectedItem(currentStatus);
        cbStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        cbStatus.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Status:", cbStatus);


        JTextArea txtPurpose = new JTextArea(currentPurpose);
        txtPurpose.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPurpose.setLineWrap(true);
        txtPurpose.setEditable(false);
        txtPurpose.setBackground(new Color(250, 250, 250));
        txtPurpose.setWrapStyleWord(true);
        txtPurpose.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));


        JScrollPane scrollPurpose = new JScrollPane(txtPurpose);
        scrollPurpose.setPreferredSize(new Dimension(200, 80));
        addStyledRow(detailsPanel, "Purpose:", scrollPurpose);


        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);


        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);


        JButton btnCancel = createRoundedButton("Cancel", new Color(149, 165, 166));
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());


        JButton btnSave = createRoundedButton("Save Changes", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(200, 45));


        // SAVE ACTION
        btnSave.addActionListener(e -> {
            String newPurpose = txtPurpose.getText().trim();


            if (newPurpose.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Purpose cannot be empty!", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }


            int confirmUpdate = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to update this record?",
                    "Confirm Update", JOptionPane.YES_NO_OPTION);


            if (confirmUpdate == JOptionPane.YES_OPTION) {
                // Update Table Model
                tableModel.setValueAt(cbStatus.getSelectedItem(), modelRow, 4);
                tableModel.setValueAt(newPurpose, modelRow, 3);


                JOptionPane.showMessageDialog(dialog, "Request Updated Successfully!");
                dialog.dispose();
                loadRequestData();
            }
        });


        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
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
        if (field instanceof JScrollPane)
            label.setVerticalAlignment(SwingConstants.TOP);


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
                new EmptyBorder(5, 10, 5, 10)));
        return field;
    }


    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }


    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));


        // --- 1. ACTION BUTTONS ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));


        JButton btnDelete = createRoundedButton("Delete Record", BTN_DELETE_COLOR);
        btnDelete.setPreferredSize(new Dimension(150, 40));
        btnDelete.addActionListener(e -> handleDelete());


        JButton btnPrint = new JButton("🖨 Print Report") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, CERULEAN_BLUE,
                        getWidth(), getHeight(), new Color(70, 130, 180));
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2d);
            }
        };
        JButton btnRefresh = createRoundedButton("↻ Refresh", new Color(52, 152, 219));
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.addActionListener(e -> loadRequestData());


        buttonPanel.add(btnRefresh);


        btnPrint.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setPreferredSize(new Dimension(140, 40));
        btnPrint.setBorderPainted(false);
        btnPrint.setContentAreaFilled(false);
        btnPrint.setOpaque(false);
        btnPrint.addActionListener(e -> handlePrint());


        buttonPanel.add(btnDelete);
        buttonPanel.add(btnPrint);


        contentPanel.add(buttonPanel);


        contentPanel.add(Box.createVerticalStrut(10));


        // --- 2. SEARCH & FILTER PANEL (Updated with Calendar) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));


        // Label
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));


        // Search Field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true), new EmptyBorder(4, 4, 4, 4)));


        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });


        // Status Filter Box
        String[] filters = { "All Status", "Pending", "Approved","Released" };
        statusFilterBox = new JComboBox<>(filters);
        statusFilterBox.setFont(new Font("Arial", Font.PLAIN, 13));
        statusFilterBox.setBackground(Color.WHITE);
        statusFilterBox.setPreferredSize(new Dimension(130, 30));


        statusFilterBox.addActionListener(e -> applyFilters());
        searchPanel.add(Box.createHorizontalStrut(10));

        // 1. Period Filter
        String[] periods = {"All Periods", "This Week", "This Month"};
        periodFilterBox = new JComboBox<>(periods);
        periodFilterBox.setFont(new Font("Arial", Font.PLAIN, 13));
        periodFilterBox.setBackground(Color.WHITE);
        periodFilterBox.setPreferredSize(new Dimension(100, 30));
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

        searchPanel.add(Box.createHorizontalStrut(5));

        // 2. Year Filter
        yearFilterBox = new JComboBox<>();
        yearFilterBox.setFont(new Font("Arial", Font.PLAIN, 13));
        yearFilterBox.setBackground(Color.WHITE);
        yearFilterBox.setPreferredSize(new Dimension(80, 30));

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

        searchPanel.add(Box.createHorizontalStrut(5));

        // Date Filter Label
        JLabel dateLabel = new JLabel("  Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Modern date filter button (EXACT SAME AS SecretaryPrintDocument)
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

        // Clear date button (EXACT SAME AS SecretaryPrintDocument)
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
        contentPanel.add(Box.createVerticalStrut(8));


        // --- 3. TABLE SETUP ---
        String[] columnNames = { "Request ID", "Resident Name", "Document Type", "Purpose", "Status", "Date" };


        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };


        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 13));


        requestTable.setRowHeight(30);
        requestTable.setGridColor(new Color(200, 200, 200));
        requestTable.setSelectionBackground(new Color(220, 237, 250));
        requestTable.setShowVerticalLines(true);
        requestTable.setShowHorizontalLines(true);


        sorter = new TableRowSorter<>(tableModel);
        requestTable.setRowSorter(sorter);


        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(5, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.addRowSorterListener(e -> updateRecordCount());


        // Comparator for Request ID column
        sorter.setComparator(0, (s1, s2) -> {
            try {
                return Integer.parseInt((String) s1) - Integer.parseInt((String) s2);
            } catch (Exception e) {
                return ((String) s1).compareTo((String) s2);
            }
        });


        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));


        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(BG_COLOR);


        lblRecordCount = new JLabel("Total Records: " + tableModel.getRowCount());
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 12));
        footerPanel.add(lblRecordCount);


        contentPanel.add(tableScrollPane);
        contentPanel.add(footerPanel);


        return contentPanel;
    }


    private void updateRecordCount() {
        if (lblRecordCount != null && requestTable != null) {
            int count = requestTable.getRowCount();
            lblRecordCount.setText("Total Records: " + count);
        }
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
                Document doc = new Document(PageSize.A4.rotate());
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


                // 4. Add Headers
                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);
                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);


                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(requestTable.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor);
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8);
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
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(HEADER_BG);
                        g2.fillRoundRect(x, y, width, height, 15, 15);
                    }
                }, new EmptyBorder(15, 20, 15, 20)));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);


        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 20));
        lblSystem.setForeground(Color.WHITE);


        JLabel lblModule = new JLabel("Admin Dashboard");
        lblModule.setFont(new Font("Arial", Font.BOLD, 16));
        lblModule.setForeground(Color.WHITE);


        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
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


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
            }
            JFrame frame = new JFrame("Admin Request Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new AdminRequestTab());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
    private JComboBox<String> periodFilterBox;
    private JComboBox<String> yearFilterBox;
}

