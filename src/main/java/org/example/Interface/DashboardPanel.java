package org.example.Interface;


import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;


import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;



import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;




public class DashboardPanel extends JPanel {
    private JTable documentTable;
    private JButton pendingBtn, verifiedBtn, rejectBtn;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;




    // ===== DATA PERSISTENCE =====
    private List<Object[]> allData = new ArrayList<>();
    private String currentFilter = "Pending";




    // ===== NEW COMPONENTS =====
    private JComboBox<String> docTypeFilterBox;
    private JLabel lblRecordCount;
    private JButton btnPrint;
    private JButton dateFilterBtn;
    private Date selectedDate;




    private JLabel hiLabel;
    private JLabel dashboardProfilePicture;




    // Gradient colors
    private final Color CERULEAN_BLUE = new Color(100, 149, 237);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    private final Color VERY_LIGHT_BLUE = new Color(225, 245, 254);
    private final Color MODERN_BLUE = new Color(66, 133, 244);
    private final Color LIGHT_GREY = new Color(248, 249, 250);
    private final Color DARK_GREY = new Color(52, 58, 64);




    private void maximizeFrame() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof JFrame) {
            ((JFrame) parentWindow).setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    public DashboardPanel() {
        setLayout(new BorderLayout());




        // ✅ Set solid background FIRST
        setBackground(VERY_LIGHT_BLUE);
        setOpaque(true);




        // ===== HEADER =====
        add(createHeader(), BorderLayout.NORTH);




        // ===== CENTER CONTENT =====
        JPanel centerContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // ✅ Only add this one check
                if (!isShowing()) return;




                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED); // ✅ Speed over quality
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), VERY_LIGHT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(VERY_LIGHT_BLUE); // ✅ Add fallback solid color
        centerContent.setBorder(new EmptyBorder(20, 50, 20, 50));




        centerContent.add(createStatusButtonPanel());
        centerContent.add(Box.createVerticalStrut(20));
        centerContent.add(createFilterActionPanel());
        centerContent.add(Box.createVerticalStrut(10));




        centerContent.add(createTablePanel());




        centerContent.add(Box.createVerticalStrut(10));
        lblRecordCount = new JLabel("Total Records: 0");
        lblRecordCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRecordCount.setForeground(Color.DARK_GRAY);




        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setOpaque(false);
        footerPanel.add(lblRecordCount);
        centerContent.add(footerPanel);




        add(centerContent, BorderLayout.CENTER);




        // Initial Load
        SwingUtilities.invokeLater(() -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    loadDataFromFile();
                    return null;
                }




                @Override
                protected void done() {
                    SwingUtilities.invokeLater(() -> {
                        switchSection("Pending");
                    });
                }
            }.execute();




            startAutoRefresh(); // ✅ Your original auto-refresh stays the same
        });
    }




    // =========================================================================
    //  GUI CREATION METHODS
    // =========================================================================




    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // ✅ Only add this check
                if (!isShowing()) return;




                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED); // ✅ Faster rendering
                GradientPaint gradient = new GradientPaint(
                        0, 0, MODERN_BLUE,
                        getWidth(), 0, new Color(26, 115, 232)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setBackground(MODERN_BLUE); // ✅ Add fallback solid color
        header.setPreferredSize(new Dimension(0, 120));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));
        JLabel title = new JLabel("<html><b>Document<br>Request</b></html>");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        header.add(title, BorderLayout.WEST);




        // Stack greeting and profile vertically
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));




        hiLabel = new JLabel(getGreetingFromProperties());
        hiLabel.setForeground(Color.WHITE);
        hiLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);




        dashboardProfilePicture = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), MODERN_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                super.paintComponent(g2d);
            }
        };
        dashboardProfilePicture.setPreferredSize(new Dimension(50, 50));
        dashboardProfilePicture.setOpaque(false);
        dashboardProfilePicture.setBorder(new LineBorder(Color.WHITE, 2, true));
        dashboardProfilePicture.setHorizontalAlignment(SwingConstants.CENTER);
        dashboardProfilePicture.setAlignmentX(Component.CENTER_ALIGNMENT);
        setProfilePicture(dashboardProfilePicture);




        rightPanel.add(hiLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(dashboardProfilePicture);




        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }




    private JPanel createStatusButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);




        pendingBtn = createSectionButton("Pending Documents", new Color(255, 140, 0));
        verifiedBtn = createSectionButton("Verified Documents", new Color(46, 139, 87));
        rejectBtn = createSectionButton("Voided Documents", new Color(220, 20, 60));




        // Actions
        pendingBtn.addActionListener(e -> {
            switchSection("Pending");
        });




        verifiedBtn.addActionListener(e -> {
            switchSection("Approved");
        });




        rejectBtn.addActionListener(e -> {
            switchSection("Rejected");
        });




        buttonPanel.add(pendingBtn);
        buttonPanel.add(verifiedBtn);
        buttonPanel.add(rejectBtn);




        return buttonPanel;
    }




    private JPanel createFilterActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));




        // LEFT: Filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);




        // Doc Type Filter
        JLabel lblDoc = new JLabel("Document:");
        lblDoc.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDoc.setForeground(Color.DARK_GRAY);
        String[] docTypes = {"All Documents", "Barangay Clearance", "Business Clearance", "Certificate of Indigency", "Certificate of Residency"};
        docTypeFilterBox = new JComboBox<>(docTypes);
        docTypeFilterBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        docTypeFilterBox.setBackground(Color.WHITE);
        docTypeFilterBox.setBorder(new RoundBorder(8, new Color(206, 212, 218)));
        docTypeFilterBox.addActionListener(e -> applyFilters());




        // Date Filter with Calendar Button
        JLabel lblDate = new JLabel("Date:");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDate.setForeground(Color.DARK_GRAY);

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




        filterPanel.add(lblDoc);
        filterPanel.add(docTypeFilterBox);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(lblDate);
        filterPanel.add(dateFilterBtn);
        filterPanel.add(Box.createHorizontalStrut(5));
        filterPanel.add(clearDateBtn);




        // RIGHT: Print Button
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setOpaque(false);




        btnPrint = new JButton("🖨 Print Report") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                        0, 0, MODERN_BLUE,
                        getWidth(), getHeight(), new Color(26, 115, 232)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2d);
            }
        };
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setPreferredSize(new Dimension(150, 35));
        btnPrint.setBorderPainted(false);
        btnPrint.setContentAreaFilled(false);
        btnPrint.setOpaque(false);
        btnPrint.setBorder(new RoundBorder(8, MODERN_BLUE));
        btnPrint.addActionListener(e -> handlePrint());




        actionPanel.add(btnPrint);




        panel.add(filterPanel, BorderLayout.WEST);
        panel.add(actionPanel, BorderLayout.EAST);




        return panel;
    }




    private void showModernDatePicker() {
        // Create a modern popup window for calendar
        JDialog dateDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dateDialog.setLayout(new BorderLayout());
        dateDialog.setResizable(false); // Prevent resizing
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
        calendarPanel.setPreferredSize(new Dimension(350, 400)); // Fixed size

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

        // Create days panel with compact grid - using BoxLayout for better control
        JPanel daysPanel = new JPanel(new GridLayout(6, 7, 2, 2)); // Fixed 6x7 grid
        daysPanel.setOpaque(false);
        daysPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        daysPanel.setPreferredSize(new Dimension(300, 180)); // Fixed size

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

            // Add empty cells for remaining spots (to maintain 6x7 grid)
            int totalCells = daysInMonth + (firstDayOfWeek - 1);
            int remainingCells = 42 - totalCells; // 6 weeks * 7 days = 42 cells
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
        dateDialog.pack(); // Pack to fit all components

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
        // Calculate selection status outside of inner classes
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
        dayBtn.setPreferredSize(new Dimension(40, 30)); // Fixed size for all buttons

        // Set foreground color
        if (isSelectedForThisButton) {
            dayBtn.setForeground(Color.WHITE);
        } else if (isTodayForThisButton) {
            dayBtn.setForeground(MODERN_BLUE);
        } else {
            dayBtn.setForeground(DARK_GREY);
        }

        // Add hover effect - using a final reference to the button for the inner class
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




    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBackground(VERY_LIGHT_BLUE);




        // Added "Date" column
        String[] columns = {"Request ID", "Resident Name", "Document Type", "Status", "Date"};




        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };




        documentTable = new JTable(model);
        documentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        documentTable.setRowHeight(45);
        documentTable.setShowGrid(false);
        documentTable.setIntercellSpacing(new Dimension(0, 0));
        documentTable.setBackground(VERY_LIGHT_BLUE);
        documentTable.setSelectionBackground(new Color(66, 133, 244, 30));
        documentTable.setSelectionForeground(Color.BLACK);
        documentTable.setOpaque(true);
        JTableHeaderStyle(documentTable);




        // Double Click Action
        documentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = documentTable.getSelectedRow();
                    if (viewRow == -1 || model == null || model.getRowCount() == 0) return;
                    if (viewRow != -1) {
                        int modelRow = documentTable.convertRowIndexToModel(viewRow);




                        int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
                        String name = (String) model.getValueAt(modelRow, 1);
                        String doc = (String) model.getValueAt(modelRow, 2);
                        String status = (String) model.getValueAt(modelRow, 3);




                        showRequestDetails(id, name, doc, status);
                    }
                }
            }
        });




        // Sorter for Filtering
        sorter = new TableRowSorter<>(model);
        documentTable.setRowSorter(sorter);




        // Listener to update count when filter changes results
        sorter.addRowSorterListener(e -> updateRecordCount());




        JScrollPane scrollPane = new JScrollPane(documentTable);
        scrollPane.setBorder(new RoundBorder(8, MODERN_BLUE));
        scrollPane.getViewport().setBackground(VERY_LIGHT_BLUE);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(true);




        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }




    private void startAutoRefresh() {
        javax.swing.Timer autoRefreshTimer = new javax.swing.Timer(5000, e -> {
            if (documentTable != null && documentTable.getSelectedRow() == -1) {
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        ResidentDAO rd = new ResidentDAO();
                        return rd.hasNewUpdates(); // Lightweight check
                    }




                    @Override
                    protected void done() {
                        try {
                            boolean hasChanges = get();
                            if (hasChanges) {




                                System.out.println("🔄 Changes detected, refreshing data...");
                                new SwingWorker<Void, Void>() {
                                    @Override
                                    protected Void doInBackground() throws Exception {
                                        loadDataFromFile(); // Heavy query
                                        return null;
                                    }
                                    @Override
                                    protected void done() {
                                        SwingUtilities.invokeLater(() -> {
                                            switchSection(currentFilter);
                                        });
                                    }
                                }.execute();
                            } else {




                                System.out.println("✓ No changes detected, skipping refresh");
                            }
                        } catch (Exception ex) {
                            System.err.println("Error checking for updates: " + ex.getMessage());
                        }
                    }
                }.execute();
            }
        });
        autoRefreshTimer.start();
    }




    // =========================================================================
    //  LOGIC METHODS
    // =========================================================================
    private boolean isExpired(Object dateObj) {
        if (dateObj == null) return false;
        try {
            String dateStr = dateObj.toString();
            // Clean up string formats
            if (dateStr.contains(".")) dateStr = dateStr.split("\\.")[0];
            dateStr = dateStr.replace("T", " ");




            DateTimeFormatter formatter;
            if (dateStr.length() == 19) {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            } else if (dateStr.length() == 16) {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            } else {
                return false;
            }




            LocalDateTime reqTime = LocalDateTime.parse(dateStr, formatter);
            LocalDateTime now = LocalDateTime.now();




            // Check if 24 hours have passed
            long hoursDiff = ChronoUnit.HOURS.between(reqTime, now);
            return hoursDiff >= 24;




        } catch (Exception e) {
            return false;
        }
    }




    private void loadDataFromFile() {
        allData.clear();
        ResidentDAO rd = new ResidentDAO();
        List<DocumentRequest> residents = rd.getAllResidentsDocument();




        for (DocumentRequest res : residents) {
            allData.add(new Object[]{
                    res.getRequestId(),
                    res.getFullName(),
                    res.getName(), // Document Type
                    res.getStatus(),
                    res.getRequestDate() // Date Object (Timestamp or Date)
            });
        }
        System.out.println("✅ Loaded " + allData.size() + " records.");
    }




    private void switchSection(String status) {
        currentFilter = status;




        // 1. Highlight the correct button
        JButton activeBtn = pendingBtn;
        if (status.equals("Approved")) activeBtn = verifiedBtn;
        else if (status.equals("Rejected")) activeBtn = rejectBtn;
        highlightButton(activeBtn);




        if (model == null) return;




        // SAVE CURRENT FILTER SELECTIONS BEFORE CLEARING
        String savedDocFilter = (String) docTypeFilterBox.getSelectedItem();




        // Clear table safely
        model.setRowCount(0);




        // CRITICAL FIX: Remove action listeners temporarily
        ActionListener[] docListeners = docTypeFilterBox.getActionListeners();
        for (ActionListener al : docListeners) {
            docTypeFilterBox.removeActionListener(al);
        }




        // Clear any existing filter
        if (sorter != null) {
            sorter.setRowFilter(null);
        }




        int rowCount = 0;




        for (Object[] row : allData) {
            String dbStatus = (String) row[3];
            Object dateObj = row[4];




            String effectiveStatus = dbStatus;




            if ("Pending".equalsIgnoreCase(dbStatus) && isExpired(dateObj)) {
                effectiveStatus = "Rejected";
            }




            boolean matchesStatus = false;
            if (status.equals("Approved")) {
                matchesStatus = effectiveStatus.equalsIgnoreCase("Paid") ||
                        effectiveStatus.equalsIgnoreCase("Released") ||
                        effectiveStatus.equalsIgnoreCase("Approved") ||
                        effectiveStatus.equalsIgnoreCase("Confirmed Payment");
            } else {
                matchesStatus = effectiveStatus.equalsIgnoreCase(status);
            }




            if (matchesStatus) {
                if (status.equals("Pending")) {
                    if (isRecent(row[4])) {
                        model.addRow(row);
                        rowCount++;
                    }
                } else {
                    model.addRow(row);
                    rowCount++;
                }
            }
        }




        // RESTORE SAVED FILTER SELECTIONS (don't reset to "All")
        docTypeFilterBox.setSelectedItem(savedDocFilter);




        // Re-add listeners
        for (ActionListener al : docListeners) {
            docTypeFilterBox.addActionListener(al);
        }




        // Re-apply the filters to match saved selections
        applyFilters();




        updateRecordCount();
    }




    private boolean isRecent(Object dateObj) {
        if (dateObj == null) return false;
        try {
            String dateStr = dateObj.toString();
            // Clean up string
            if (dateStr.contains(".")) dateStr = dateStr.split("\\.")[0];
            dateStr = dateStr.replace("T", " ");




            DateTimeFormatter formatter;




            // Check format based on length
            if (dateStr.length() == 19) {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            } else if (dateStr.length() == 16) {
                // THIS FIXES YOUR CRASH: Handle missing seconds
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            } else {
                return false; // Invalid format
            }




            LocalDateTime reqTime = LocalDateTime.parse(dateStr, formatter);
            LocalDateTime now = LocalDateTime.now();




            long diff = ChronoUnit.MINUTES.between(reqTime, now);
            return diff >= 0 && diff <= 5; // 5 Minute Rule




        } catch (Exception e) {
            // e.printStackTrace(); // Hide parsing errors
            return false;
        }
    }




    private void applyFilters() {
        try {
            // Initial null checks
            if (sorter == null || model == null || documentTable == null) {
                updateRecordCount();
                return;
            }




            String selectedDoc = (String) docTypeFilterBox.getSelectedItem();




            if (selectedDoc == null) {
                updateRecordCount();
                return;
            }




            // If no filters are active, just clear and return
            if (selectedDoc.equals("All Documents") && selectedDate == null) {
                sorter.setRowFilter(null);
                updateRecordCount();
                return;
            }




            // CRITICAL: Don't filter if model is empty
            if (model.getRowCount() == 0) {
                updateRecordCount();
                return;
            }




            List<RowFilter<Object, Object>> filters = new ArrayList<>();




            // 1. Document Type Filter (Column 2)
            if (!selectedDoc.equals("All Documents")) {
                filters.add(RowFilter.regexFilter("(?i)^" + selectedDoc + "$", 2));
            }




            // 2. Date Filter (Column 4) - Using selectedDate
            if (selectedDate != null) {
                final Date finalSelectedDate = selectedDate; // Make effectively final
                filters.add(new RowFilter<Object, Object>() {
                    @Override
                    public boolean include(Entry<?, ?> entry) {
                        try {
                            String dateStr = entry.getStringValue(4);
                            if (dateStr == null || dateStr.isEmpty()) {
                                return false;
                            }




                            // Clean the date string
                            if(dateStr.contains("T")) dateStr = dateStr.split("T")[0];
                            else if(dateStr.contains(" ")) dateStr = dateStr.split(" ")[0];




                            // Parse the row date
                            LocalDate rowDate = LocalDate.parse(dateStr);

                            // Convert selectedDate to LocalDate
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String selectedDateStr = sdf.format(finalSelectedDate);
                            LocalDate selectedLocalDate = LocalDate.parse(selectedDateStr);

                            // Compare dates
                            return rowDate.equals(selectedLocalDate);

                        } catch (Exception e) {
                            return false;
                        }
                    }
                });
            }




            if (filters.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                RowFilter<Object, Object> combinedFilter = RowFilter.andFilter(filters);
                sorter.setRowFilter(combinedFilter);
            }




            updateRecordCount();




        } catch (Exception e) {
            System.err.println("Filter error: " + e.getMessage());
            e.printStackTrace(); // ADD THIS to see the actual error
            if (sorter != null) {
                sorter.setRowFilter(null);
            }
            updateRecordCount();
        }
    }




    private void updateRecordCount() {
        SwingUtilities.invokeLater(() -> {
            if (lblRecordCount == null) {
                return;
            }




            int count = 0;
            if (documentTable != null && model != null) {
                count = documentTable.getRowCount();
            }




            lblRecordCount.setText("Total Records: " + count);
        });
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
                Paragraph title = new Paragraph("Document Request Master List", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);

                // 3. Create Table
                int colCount = documentTable.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);

                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);

                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);

                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(documentTable.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor); // ✅ Blue Background
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8); // More padding like your table
                    pdfTable.addCell(cell);
                }

                // 5. Add Rows
                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

                for (int i = 0; i < documentTable.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = documentTable.getValueAt(i, j);
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
    //  HELPER / STYLE METHODS
    // =========================================================================




    private void highlightButton(JButton activeBtn) {
        JButton[] buttons = {pendingBtn, verifiedBtn, rejectBtn};
        Color[] colors = { new Color(255, 140, 0), new Color(46, 139, 87), new Color(220, 20, 60) };
        for (int i = 0; i < buttons.length; i++) {
            JButton btn = buttons[i];
            if (btn == activeBtn) btn.setBackground(colors[i]);
            else btn.setBackground(new Color(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue(), 130));
        }
    }




    private void showRequestDetails(int requestId, String name, String docType, String status) {
        JOptionPane.showMessageDialog(this,
                "Details for Request #" + requestId + "\nName: " + name + "\nDoc: " + docType + "\nStatus: " + status);
    }




    // Placeholder methods from your original code
    private String getGreetingFromProperties() { return "Hi Secretary"; }
    private void setProfilePicture(JLabel l) { l.setText("👤"); }




    private void JTableHeaderStyle(JTable t) {
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.getTableHeader().setDefaultRenderer(new GradientHeaderRenderer());
        t.getTableHeader().setReorderingAllowed(false);
    }




    // Custom Table Header Renderer with Cerulean Gradient
    private class GradientHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setOpaque(false);
            return this;
        }




        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();




            // Cerulean gradient colors
            GradientPaint gradient = new GradientPaint(
                    0, 0, MODERN_BLUE,
                    getWidth(), getHeight(), new Color(26, 115, 232)
            );




            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());




            // Paint the text
            super.paintComponent(g2d);
            g2d.dispose();
        }
    }




    private JButton createSectionButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color currentColor = getBackground();
                GradientPaint gradient = new GradientPaint(
                        0, 0, currentColor,
                        getWidth(), getHeight(), currentColor.brighter()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g2d);
            }
        };
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 48));
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new RoundedBorder(20, color));
        Color inactiveColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 130);
        btn.setBackground(inactiveColor);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color);
                btn.repaint();
            }
            public void mouseExited(MouseEvent e) {
                if (btn != pendingBtn && btn != verifiedBtn && btn != rejectBtn) btn.setBackground(inactiveColor);
                else if (!currentFilter.equals(text.split(" ")[0])) btn.setBackground(inactiveColor);
                btn.repaint();
            }
        });
        return btn;
    }




    private static class RoundedBorder extends AbstractBorder {
        private final int radius; private final Color color;
        RoundedBorder(int radius, Color color) { this.radius = radius; this.color = color; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color.darker());
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        public Insets getBorderInsets(Component c) { return new Insets(10, 10, 10, 10); }
    }




    private static class RoundBorder extends AbstractBorder {
        private final int radius; private final Color color;
        RoundBorder(int radius, Color color) { this.radius = radius; this.color = color; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w-1, h-1, radius, radius);
            g2.dispose();
        }
        public Insets getBorderInsets(Component c) { return new Insets(2, 8, 2, 8); }
    }
}


