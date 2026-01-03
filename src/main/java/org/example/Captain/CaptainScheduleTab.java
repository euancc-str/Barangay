package org.example.Captain;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Pattern;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;


public class CaptainScheduleTab extends JPanel {
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private CaptainScheduleDAO scheduleDAO;
    private JButton btnAdd, btnEdit, btnDelete, btnMarkAvailable, btnMarkUnavailable;
    private JComboBox<String> cmbDayFilter;
    private final JButton dateButton;
    private final JTextField startTimeField;
    private final JTextField endTimeField;
    private final JComboBox<String> startAmPmCombo;
    private final JComboBox<String> endAmPmCombo;


    // Colors
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color AVAILABLE_COLOR = new Color(46, 204, 113);
    private final Color UNAVAILABLE_COLOR = new Color(231, 76, 60);
    private final Color CALENDAR_BG = new Color(245, 245, 245);
    private final Color CALENDAR_HOVER = new Color(225, 240, 255);
    private final Color VALID_FIELD = new Color(220, 255, 220);
    private final Color INVALID_FIELD = new Color(255, 220, 220);


    // Calendar dialog
    private JDialog calendarDialog;
    private JButton[][] dayButtons;
    private LocalDate selectedCalendarDate;
    private JLabel monthYearLabel;


    // Time regex patterns
    private static final String TIME_12H_REGEX = "^(1[0-2]|0?[1-9]):[0-5][0-9]$";
    private static final Pattern TIME_12H_PATTERN = Pattern.compile(TIME_12H_REGEX);


    public CaptainScheduleTab() {
        scheduleDAO = new CaptainScheduleDAO();
        // Ensure table exists


        setLayout(new BorderLayout(0, 10));
        setBackground(new Color(240, 242, 245));
        setBorder(new EmptyBorder(10, 10, 10, 10));


        // Initialize date button and time fields
        dateButton = new JButton();
        startTimeField = new JTextField();
        endTimeField = new JTextField();
        startAmPmCombo = new JComboBox<>(new String[]{"AM", "PM"});
        endAmPmCombo = new JComboBox<>(new String[]{"AM", "PM"});


        initializeUI();
        loadSchedules();
        initializeCalendarDialog();
        updateDateButton();
    }


    private void initializeUI() {
        // Header Panel
        JPanel headerPanel = createHeaderPanel();


        // Control Panel
        JPanel controlPanel = createControlPanel();


        // Table Panel
        JPanel tablePanel = createTablePanel();


        // Add panels to main layout
        add(headerPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.WEST);
        add(tablePanel, BorderLayout.CENTER);
    }


    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_COLOR.darker(), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));


        JLabel titleLabel = new JLabel("CAPTAIN'S AVAILABLE SCHEDULE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);


        JLabel subtitleLabel = new JLabel("Manage your availability for hearings and meetings");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(220, 220, 220));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(PRIMARY_COLOR);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitleLabel);


        panel.add(titlePanel, BorderLayout.WEST);


        // Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        legendPanel.setBackground(PRIMARY_COLOR);


        JLabel availableLegend = new JLabel("● Available");
        availableLegend.setForeground(AVAILABLE_COLOR);
        availableLegend.setFont(new Font("Arial", Font.BOLD, 12));


        JLabel unavailableLegend = new JLabel("● Unavailable");
        unavailableLegend.setForeground(UNAVAILABLE_COLOR);
        unavailableLegend.setFont(new Font("Arial", Font.BOLD, 12));


        legendPanel.add(availableLegend);
        legendPanel.add(Box.createHorizontalStrut(15));
        legendPanel.add(unavailableLegend);


        panel.add(legendPanel, BorderLayout.EAST);


        return panel;
    }


    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(300, 0));


        // Title
        JLabel controlTitle = new JLabel("Schedule Controls");
        controlTitle.setFont(new Font("Arial", Font.BOLD, 16));
        controlTitle.setForeground(PRIMARY_COLOR);
        controlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(controlTitle);
        panel.add(Box.createVerticalStrut(20));


        // Date Selection
        JPanel datePanel = new JPanel(new GridBagLayout());
        datePanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        datePanel.add(dateLabel, gbc);


        // Configure date button (calendar)
        dateButton.setFont(new Font("Arial", Font.PLAIN, 12));
        dateButton.setPreferredSize(new Dimension(120, 25));
        dateButton.addActionListener(e -> showCalendarDialog());
        gbc.gridx = 1; gbc.weightx = 1.0;
        datePanel.add(dateButton, gbc);


        JButton btnToday = new JButton("Today");
        btnToday.setFont(new Font("Arial", Font.PLAIN, 10));
        btnToday.addActionListener(e -> {
            selectedCalendarDate = LocalDate.now();
            updateDateButton();
        });
        gbc.gridx = 2; gbc.weightx = 0;
        datePanel.add(btnToday, gbc);


        panel.add(datePanel);
        panel.add(Box.createVerticalStrut(15));


        // Start Time Selection
        JPanel startTimePanel = new JPanel(new BorderLayout(5, 0));
        startTimePanel.setBackground(Color.WHITE);

        JLabel startTimeLabel = new JLabel("Start Time:");
        startTimeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        startTimePanel.add(startTimeLabel, BorderLayout.WEST);

        JPanel startTimeInputPanel = new JPanel(new BorderLayout(5, 0));
        startTimeInputPanel.setBackground(Color.WHITE);

        startTimeField.setText("08:00");
        configureTimeField(startTimeField);
        startTimeInputPanel.add(startTimeField, BorderLayout.CENTER);

        startAmPmCombo.setSelectedIndex(0); // Default to AM
        startTimeInputPanel.add(startAmPmCombo, BorderLayout.EAST);

        startTimePanel.add(startTimeInputPanel, BorderLayout.CENTER);

        panel.add(startTimePanel);
        panel.add(Box.createVerticalStrut(10));


        // End Time Selection
        JPanel endTimePanel = new JPanel(new BorderLayout(5, 0));
        endTimePanel.setBackground(Color.WHITE);

        JLabel endTimeLabel = new JLabel("End Time:");
        endTimeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        endTimePanel.add(endTimeLabel, BorderLayout.WEST);

        JPanel endTimeInputPanel = new JPanel(new BorderLayout(5, 0));
        endTimeInputPanel.setBackground(Color.WHITE);

        endTimeField.setText("09:00");
        configureTimeField(endTimeField);
        endTimeInputPanel.add(endTimeField, BorderLayout.CENTER);

        endAmPmCombo.setSelectedIndex(0); // Default to AM
        endTimeInputPanel.add(endAmPmCombo, BorderLayout.EAST);

        endTimePanel.add(endTimeInputPanel, BorderLayout.CENTER);

        panel.add(endTimePanel);

        // Time format hint
        JLabel timeHint = new JLabel("Format: hh:mm (e.g., 08:00, 1:30)");
        timeHint.setFont(new Font("Arial", Font.PLAIN, 10));
        timeHint.setForeground(Color.GRAY);
        timeHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(5));
        panel.add(timeHint);
        panel.add(Box.createVerticalStrut(15));


        // Day Filter
        JPanel filterPanel = new JPanel(new BorderLayout(5, 0));
        filterPanel.setBackground(Color.WHITE);


        JLabel filterLabel = new JLabel("Filter by Day:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 12));
        filterPanel.add(filterLabel, BorderLayout.WEST);


        String[] days = {"All", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY",
                "FRIDAY", "SATURDAY", "SUNDAY"};
        cmbDayFilter = new JComboBox<>(days);
        cmbDayFilter.addActionListener(e -> filterByDay());
        filterPanel.add(cmbDayFilter, BorderLayout.CENTER);


        panel.add(filterPanel);
        panel.add(Box.createVerticalStrut(20));


        // Action Buttons
        btnAdd = createControlButton("Add Schedule", "➕", PRIMARY_COLOR);
        btnEdit = createControlButton("Edit Selected", "✏️", new Color(52, 152, 219));
        btnDelete = createControlButton("Delete Selected", "🗑️", UNAVAILABLE_COLOR);
        btnMarkAvailable = createControlButton("Mark as Available", "✅", AVAILABLE_COLOR);
        btnMarkUnavailable = createControlButton("Mark as Unavailable", "❌", new Color(241, 196, 15));


        btnAdd.addActionListener(e -> addSchedule());
        btnEdit.addActionListener(e -> editSchedule());
        btnDelete.addActionListener(e -> deleteSchedule());
        btnMarkAvailable.addActionListener(e -> markAsAvailable(true));
        btnMarkUnavailable.addActionListener(e -> markAsAvailable(false));


        panel.add(btnAdd);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnEdit);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnDelete);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnMarkAvailable);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnMarkUnavailable);


        panel.add(Box.createVerticalGlue());


        // Quick Actions
        JLabel quickLabel = new JLabel("Quick Actions:");
        quickLabel.setFont(new Font("Arial", Font.BOLD, 14));
        quickLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(30));
        panel.add(quickLabel);
        panel.add(Box.createVerticalStrut(10));


        JButton btnAddWeek = createControlButton("Add Next Week", "📅", new Color(155, 89, 182));
        JButton btnClearAll = createControlButton("Clear All", "⚠️", new Color(230, 126, 34));


        btnAddWeek.addActionListener(e -> addWeeklySchedule());
        btnClearAll.addActionListener(e -> clearAllSchedules());


        panel.add(btnAddWeek);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnClearAll);


        return panel;
    }


    private void configureTimeField(JTextField timeField) {
        timeField.setFont(new Font("Arial", Font.PLAIN, 12));
        timeField.setHorizontalAlignment(JTextField.CENTER);
        timeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Add placeholder
        timeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (timeField.getText().equals("hh:mm")) {
                    timeField.setText("");
                    timeField.setForeground(Color.BLACK);
                }
                validateTimeField(timeField);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (timeField.getText().isEmpty()) {
                    timeField.setText("hh:mm");
                    timeField.setForeground(Color.GRAY);
                } else {
                    validateTimeField(timeField);
                }
            }
        });

        // Add real-time validation
        timeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validateTimeField(timeField);
            }
        });

        // Initial validation
        validateTimeField(timeField);
    }

    private void validateTimeField(JTextField timeField) {
        String text = timeField.getText();

        // Skip validation if it's placeholder text
        if (text.equals("hh:mm")) {
            timeField.setBackground(Color.WHITE);
            return;
        }

        if (text.isEmpty()) {
            timeField.setBackground(Color.WHITE);
            return;
        }

        if (isValidTimeFormat(text)) {
            timeField.setBackground(VALID_FIELD);
            timeField.setToolTipText("Valid time format");
        } else {
            timeField.setBackground(INVALID_FIELD);
            timeField.setToolTipText("Invalid time format. Use hh:mm (e.g., 08:00, 1:30)");
        }
    }

    private boolean isValidTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return false;
        }

        // Check 12-hour format (1:30, 01:30, 12:00, etc.)
        return TIME_12H_PATTERN.matcher(time).matches();
    }

    private LocalTime parse12HourTime(String timeStr, String amPm) throws DateTimeParseException {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new DateTimeParseException("Time string is empty", timeStr, 0);
        }

        timeStr = timeStr.trim();

        if (!isValidTimeFormat(timeStr)) {
            throw new DateTimeParseException("Invalid time format", timeStr, 0);
        }

        // Ensure it has leading zeros if needed
        if (timeStr.length() == 4) { // Format: "h:mm"
            timeStr = "0" + timeStr;
        }

        // Split hour and minute
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        // Convert to 24-hour format
        if (amPm.equalsIgnoreCase("PM")) {
            if (hour != 12) {
                hour += 12;
            }
        } else { // AM
            if (hour == 12) {
                hour = 0;
            }
        }

        return LocalTime.of(hour, minute);
    }


    private void initializeCalendarDialog() {
        calendarDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        calendarDialog.setSize(350, 400);
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.setResizable(false);
        calendarDialog.setLocationRelativeTo(this);


        // Initialize selected date to today
        selectedCalendarDate = LocalDate.now();


        // Main calendar panel
        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBackground(CALENDAR_BG);
        calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        // Month/year navigation panel
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(CALENDAR_BG);
        navPanel.setBorder(new EmptyBorder(0, 0, 10, 0));


        JButton prevMonthBtn = new JButton("◀");
        prevMonthBtn.setFont(new Font("Arial", Font.BOLD, 14));
        prevMonthBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        prevMonthBtn.addActionListener(e -> {
            selectedCalendarDate = selectedCalendarDate.minusMonths(1);
            updateCalendar();
        });


        monthYearLabel = new JLabel();
        monthYearLabel.setFont(new Font("Arial", Font.BOLD, 16));
        monthYearLabel.setHorizontalAlignment(SwingConstants.CENTER);


        JButton nextMonthBtn = new JButton("▶");
        nextMonthBtn.setFont(new Font("Arial", Font.BOLD, 14));
        nextMonthBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        nextMonthBtn.addActionListener(e -> {
            selectedCalendarDate = selectedCalendarDate.plusMonths(1);
            updateCalendar();
        });


        navPanel.add(prevMonthBtn, BorderLayout.WEST);
        navPanel.add(monthYearLabel, BorderLayout.CENTER);
        navPanel.add(nextMonthBtn, BorderLayout.EAST);


        // Day labels panel
        JPanel dayLabelsPanel = new JPanel(new GridLayout(1, 7));
        dayLabelsPanel.setBackground(CALENDAR_BG);
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayNames) {
            JLabel dayLabel = new JLabel(day);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dayLabelsPanel.add(dayLabel);
        }


        // Days grid panel
        JPanel daysPanel = new JPanel(new GridLayout(6, 7, 2, 2));
        daysPanel.setBackground(CALENDAR_BG);
        dayButtons = new JButton[6][7];


        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                final int r = row;
                final int c = col;
                JButton dayBtn = new JButton();
                dayBtn.setFont(new Font("Arial", Font.PLAIN, 12));
                dayBtn.setFocusPainted(false);
                dayBtn.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


                dayBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (!dayBtn.getText().isEmpty()) {
                            dayBtn.setBackground(CALENDAR_HOVER);
                        }
                    }


                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (!dayBtn.getText().isEmpty()) {
                            LocalDate buttonDate = getButtonDate(r, c);
                            if (buttonDate != null) {
                                if (buttonDate.equals(LocalDate.now())) {
                                    dayBtn.setBackground(new Color(220, 240, 255));
                                } else if (buttonDate.equals(selectedCalendarDate)) {
                                    dayBtn.setBackground(PRIMARY_COLOR);
                                } else {
                                    dayBtn.setBackground(Color.WHITE);
                                }
                            }
                        }
                    }
                });


                dayBtn.addActionListener(e -> {
                    LocalDate selectedDate = getButtonDate(r, c);
                    if (selectedDate != null) {
                        selectedCalendarDate = selectedDate;
                        updateCalendar();
                        calendarDialog.setVisible(false);
                        updateDateButton();
                    }
                });


                dayButtons[row][col] = dayBtn;
                daysPanel.add(dayBtn);
            }
        }


        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CALENDAR_BG);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));


        JButton btnTodayCal = new JButton("Today");
        btnTodayCal.addActionListener(e -> {
            selectedCalendarDate = LocalDate.now();
            updateCalendar();
            updateDateButton();
        });


        JButton btnSelect = new JButton("Select");
        btnSelect.setBackground(PRIMARY_COLOR);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.addActionListener(e -> {
            calendarDialog.setVisible(false);
            updateDateButton();
        });


        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> calendarDialog.setVisible(false));


        buttonPanel.add(btnTodayCal);
        buttonPanel.add(btnSelect);
        buttonPanel.add(btnCancel);


        // Add all panels to dialog
        calendarPanel.add(navPanel, BorderLayout.NORTH);
        calendarPanel.add(dayLabelsPanel, BorderLayout.CENTER);
        calendarPanel.add(daysPanel, BorderLayout.SOUTH);


        calendarDialog.add(calendarPanel, BorderLayout.CENTER);
        calendarDialog.add(buttonPanel, BorderLayout.SOUTH);


        updateCalendar();
    }


    private void updateCalendar() {
        // Update month/year label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(selectedCalendarDate.format(formatter));


        // Get first day of month and number of days
        LocalDate firstDayOfMonth = selectedCalendarDate.withDayOfMonth(1);
        int daysInMonth = selectedCalendarDate.lengthOfMonth();
        int startDay = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0 = Sunday, 6 = Saturday


        // Clear all buttons
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                dayButtons[row][col].setText("");
                dayButtons[row][col].setBackground(Color.WHITE);
                dayButtons[row][col].setForeground(Color.BLACK);
                dayButtons[row][col].setEnabled(false);
            }
        }


        // Fill in days
        LocalDate today = LocalDate.now();
        int dayCounter = 1;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == 0 && j < startDay) {
                    continue;
                }
                if (dayCounter > daysInMonth) {
                    break;
                }


                LocalDate currentDate = LocalDate.of(selectedCalendarDate.getYear(),
                        selectedCalendarDate.getMonth(),
                        dayCounter);
                dayButtons[i][j].setText(String.valueOf(dayCounter));
                dayButtons[i][j].setEnabled(true);


                // Highlight today
                if (currentDate.equals(today)) {
                    dayButtons[i][j].setBackground(new Color(220, 240, 255));
                }


                // Highlight selected date
                if (currentDate.equals(selectedCalendarDate)) {
                    dayButtons[i][j].setBackground(PRIMARY_COLOR);
                    dayButtons[i][j].setForeground(Color.WHITE);
                } else {
                    dayButtons[i][j].setForeground(Color.BLACK);
                }


                // Gray out weekends
                if (j == 0 || j == 6) { // Sunday or Saturday
                    if (!currentDate.equals(selectedCalendarDate) && !currentDate.equals(today)) {
                        dayButtons[i][j].setForeground(Color.GRAY);
                    }
                }


                dayCounter++;
            }
        }
    }


    private LocalDate getButtonDate(int row, int col) {
        try {
            String dayText = dayButtons[row][col].getText();
            if (dayText.isEmpty() || !dayButtons[row][col].isEnabled()) {
                return null;
            }


            int day = Integer.parseInt(dayText);
            return LocalDate.of(selectedCalendarDate.getYear(),
                    selectedCalendarDate.getMonth(),
                    day);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    private void showCalendarDialog() {
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setVisible(true);
    }


    private void updateDateButton() {
        if (selectedCalendarDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateButton.setText(selectedCalendarDate.format(formatter));
        }
    }


    private JButton createControlButton(String text, String icon, Color color) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 40));


        // Gumawa ng final copy ng color para sa lambda
        final Color buttonColor = color;


        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(buttonColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(buttonColor);
            }
        });


        return button;
    }


    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));


        // Create table model
        String[] columns = {"ID", "Date", "Day", "Start Time", "End Time", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only Actions column is editable
            }


            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) return JButton.class;
                return String.class;
            }
        };


        scheduleTable = new JTable(tableModel);
        styleTable();


        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(null);


        panel.add(scrollPane, BorderLayout.CENTER);


        return panel;
    }


    private void styleTable() {
        scheduleTable.setRowHeight(40);
        scheduleTable.setFont(new Font("Arial", Font.PLAIN, 12));
        scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        scheduleTable.getTableHeader().setBackground(PRIMARY_COLOR);
        scheduleTable.getTableHeader().setForeground(Color.WHITE);
        scheduleTable.setSelectionBackground(new Color(220, 240, 255));
        scheduleTable.setSelectionForeground(Color.BLACK);


        // Center alignment for most columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < scheduleTable.getColumnCount(); i++) {
            if (i != 6) { // Not for Actions column
                scheduleTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }


        // Custom renderer for Status column
        scheduleTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());


        // Set column widths
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        scheduleTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        scheduleTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        scheduleTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        scheduleTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        scheduleTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        scheduleTable.getColumnModel().getColumn(6).setPreferredWidth(120);


        // Add button renderer and editor for Actions column
        scheduleTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        scheduleTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
    }


    private void loadSchedules() {
        List<CaptainSchedule> schedules = scheduleDAO.getAllSchedules();
        tableModel.setRowCount(0);


        for (CaptainSchedule schedule : schedules) {
            String status = schedule.isAvailable() ? "Available" : "Unavailable";
            String dayOfWeek = schedule.getDayOfWeek();
            if (dayOfWeek == null || dayOfWeek.isEmpty()) {
                if (schedule.getScheduleDate() != null) {
                    dayOfWeek = schedule.getScheduleDate().getDayOfWeek().toString();
                } else {
                    dayOfWeek = "N/A";
                }
            }
            tableModel.addRow(new Object[]{
                    schedule.getScheduleId(),
                    schedule.getScheduleDate().toString(),
                    dayOfWeek,
                    schedule.getStartTime().toString(),
                    schedule.getEndTime().toString(),
                    status,
                    "Edit/Delete"
            });
        }
    }


    private void filterByDay() {
        String selectedDay = (String) cmbDayFilter.getSelectedItem();
        if ("All".equals(selectedDay)) {
            loadSchedules();
            return;
        }


        List<CaptainSchedule> allSchedules = scheduleDAO.getAllSchedules();
        tableModel.setRowCount(0);


        for (CaptainSchedule schedule : allSchedules) {
            // Get the day from the schedule date
            String scheduleDay;
            if (schedule.getDayOfWeek() != null && !schedule.getDayOfWeek().isEmpty()) {
                scheduleDay = schedule.getDayOfWeek();
            } else if (schedule.getScheduleDate() != null) {
                scheduleDay = schedule.getScheduleDate().getDayOfWeek().toString();
            } else {
                scheduleDay = "";
            }


            // Compare days (case-insensitive)
            if (selectedDay.equalsIgnoreCase(scheduleDay)) {
                String status = schedule.isAvailable() ? "Available" : "Unavailable";
                tableModel.addRow(new Object[]{
                        schedule.getScheduleId(),
                        schedule.getScheduleDate().toString(),
                        scheduleDay,
                        schedule.getStartTime().toString(),
                        schedule.getEndTime().toString(),
                        status,
                        "Edit/Delete"
                });
            }
        }
    }


    private void addSchedule() {
        try {
            // Get date from button
            String dateText = dateButton.getText();
            LocalDate scheduleDate = LocalDate.parse(dateText);


            // Get time from fields
            String startTimeText = startTimeField.getText().trim();
            String endTimeText = endTimeField.getText().trim();
            String startAmPm = (String) startAmPmCombo.getSelectedItem();
            String endAmPm = (String) endAmPmCombo.getSelectedItem();


            // Validate time formats
            if (!isValidTimeFormat(startTimeText)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid start time format!\nUse hh:mm (e.g., 08:00, 1:30)",
                        "Invalid Time", JOptionPane.ERROR_MESSAGE);
                startTimeField.requestFocus();
                return;
            }


            if (!isValidTimeFormat(endTimeText)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid end time format!\nUse hh:mm (e.g., 09:00, 2:30)",
                        "Invalid Time", JOptionPane.ERROR_MESSAGE);
                endTimeField.requestFocus();
                return;
            }


            LocalTime startTime = parse12HourTime(startTimeText, startAmPm);
            LocalTime endTime = parse12HourTime(endTimeText, endAmPm);


            // Validation
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                JOptionPane.showMessageDialog(this, "End time must be after start time!",
                        "Invalid Time", JOptionPane.ERROR_MESSAGE);
                return;
            }


            // Check if time slot overlaps or exists
            if (!scheduleDAO.isTimeSlotAvailable(scheduleDate, startTime, endTime)) {
                // Check if EXACT duplicate exists
                if (scheduleDAO.isExactScheduleExists(scheduleDate, startTime, endTime)) {
                    JOptionPane.showMessageDialog(this,
                            "This exact schedule already exists!\n" +
                                    "Date: " + scheduleDate + "\n" +
                                    "Time: " + startTime + " to " + endTime,
                            "Duplicate Schedule", JOptionPane.WARNING_MESSAGE);
                    return;
                } else {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Time slot overlaps with existing schedule. Still add?",
                            "Time Conflict", JOptionPane.YES_NO_OPTION);
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
            }


            CaptainSchedule schedule = CaptainSchedule.builder()
                    .scheduleDate(scheduleDate)
                    .startTime(startTime)
                    .endTime(endTime)
                    .isAvailable(true)
                    .build();


            if (scheduleDAO.addSchedule(schedule)) {
                JOptionPane.showMessageDialog(this, "Schedule added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSchedules();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add schedule!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }


        } catch (DateTimeParseException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error parsing time: " + e.getMessage() +
                            "\nPlease use format: hh:mm (e.g., 08:00, 1:30)",
                    "Time Parse Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void editSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to edit!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }


        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);


        // Create edit dialog
        JDialog editDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Schedule", true);
        editDialog.setSize(400, 400);
        editDialog.setLayout(new BorderLayout());
        editDialog.setLocationRelativeTo(this);


        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        // Get current schedule
        List<CaptainSchedule> allSchedules = scheduleDAO.getAllSchedules();
        CaptainSchedule selectedSchedule = null;
        for (CaptainSchedule schedule : allSchedules) {
            if (schedule.getScheduleId() == scheduleId) {
                selectedSchedule = schedule;
                break;
            }
        }


        if (selectedSchedule == null) {
            JOptionPane.showMessageDialog(this, "Schedule not found!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // Create a final reference for use in lambda
        final CaptainSchedule finalSchedule = selectedSchedule;

        // Date field with calendar button
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        editPanel.add(new JLabel("Date:"), gbc);


        JButton editDateButton = new JButton(selectedSchedule.getScheduleDate().toString());
        editDateButton.setFont(new Font("Arial", Font.PLAIN, 12));
        editDateButton.setPreferredSize(new Dimension(120, 25));

        // Local variable to hold date in edit dialog
        final LocalDate[] editSelectedDate = {selectedSchedule.getScheduleDate()};

        editDateButton.addActionListener(e -> {
            // Create a mini calendar for edit dialog
            JDialog miniCalendar = new JDialog(editDialog, "Select Date", true);
            miniCalendar.setSize(300, 300);
            miniCalendar.setLayout(new BorderLayout());

            // Create calendar panel similar to main calendar
            JPanel miniCalPanel = new JPanel(new BorderLayout());
            miniCalPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel miniMonthLabel = new JLabel();
            miniMonthLabel.setFont(new Font("Arial", Font.BOLD, 14));
            miniMonthLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel miniDaysPanel = new JPanel(new GridLayout(6, 7, 2, 2));

            // Function to update mini calendar
            Runnable updateMiniCalendar = () -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
                miniMonthLabel.setText(editSelectedDate[0].format(formatter));

                LocalDate firstDay = editSelectedDate[0].withDayOfMonth(1);
                int daysInMonth = editSelectedDate[0].lengthOfMonth();
                int startDay = firstDay.getDayOfWeek().getValue() % 7;

                // Clear buttons
                miniDaysPanel.removeAll();

                // Create day buttons
                int dayCounter = 1;
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 7; j++) {
                        JButton dayBtn = new JButton();
                        dayBtn.setFont(new Font("Arial", Font.PLAIN, 11));

                        if (i == 0 && j < startDay) {
                            dayBtn.setEnabled(false);
                        } else if (dayCounter > daysInMonth) {
                            dayBtn.setEnabled(false);
                        } else {
                            final int currentDay = dayCounter;
                            dayBtn.setText(String.valueOf(currentDay));

                            final LocalDate btnDate = LocalDate.of(editSelectedDate[0].getYear(),
                                    editSelectedDate[0].getMonth(), currentDay);

                            // Use finalSchedule in the lambda
                            if (btnDate.equals(finalSchedule.getScheduleDate())) {
                                dayBtn.setBackground(PRIMARY_COLOR);
                                dayBtn.setForeground(Color.WHITE);
                            } else if (btnDate.equals(LocalDate.now())) {
                                dayBtn.setBackground(new Color(220, 240, 255));
                            } else {
                                dayBtn.setBackground(Color.WHITE);
                            }

                            dayBtn.addActionListener(ev -> {
                                editSelectedDate[0] = btnDate;
                                editDateButton.setText(btnDate.toString());
                                miniCalendar.dispose();
                            });

                            dayCounter++;
                        }

                        miniDaysPanel.add(dayBtn);
                    }
                }

                miniDaysPanel.revalidate();
                miniDaysPanel.repaint();
            };

            // Navigation buttons
            JPanel miniNavPanel = new JPanel(new BorderLayout());
            JButton prevBtn = new JButton("◀");
            prevBtn.addActionListener(ev -> {
                editSelectedDate[0] = editSelectedDate[0].minusMonths(1);
                updateMiniCalendar.run();
            });

            JButton nextBtn = new JButton("▶");
            nextBtn.addActionListener(ev -> {
                editSelectedDate[0] = editSelectedDate[0].plusMonths(1);
                updateMiniCalendar.run();
            });

            miniNavPanel.add(prevBtn, BorderLayout.WEST);
            miniNavPanel.add(miniMonthLabel, BorderLayout.CENTER);
            miniNavPanel.add(nextBtn, BorderLayout.EAST);

            miniCalPanel.add(miniNavPanel, BorderLayout.NORTH);
            miniCalPanel.add(miniDaysPanel, BorderLayout.CENTER);

            // Today button
            JButton miniTodayBtn = new JButton("Today");
            miniTodayBtn.addActionListener(ev -> {
                editSelectedDate[0] = LocalDate.now();
                editDateButton.setText(editSelectedDate[0].toString());
                miniCalendar.dispose();
            });

            JPanel miniButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            miniButtonPanel.add(miniTodayBtn);

            miniCalendar.add(miniCalPanel, BorderLayout.CENTER);
            miniCalendar.add(miniButtonPanel, BorderLayout.SOUTH);

            updateMiniCalendar.run();
            miniCalendar.setLocationRelativeTo(editDialog);
            miniCalendar.setVisible(true);
        });

        gbc.gridx = 1; gbc.weightx = 1.0;
        editPanel.add(editDateButton, gbc);


        // Start time field
        gbc.gridx = 0; gbc.gridy = 1;
        editPanel.add(new JLabel("Start Time:"), gbc);

        JPanel editStartTimePanel = new JPanel(new BorderLayout(5, 0));
        JTextField editStartField = new JTextField();
        configureTimeField(editStartField);

        // Convert 24-hour time to 12-hour for display
        String startTime12Hour = convertTo12HourFormat(selectedSchedule.getStartTime());
        String[] startParts = startTime12Hour.split(" ");
        editStartField.setText(startParts[0]);

        JComboBox<String> editStartAmPm = new JComboBox<>(new String[]{"AM", "PM"});
        if (startParts.length > 1) {
            editStartAmPm.setSelectedItem(startParts[1]);
        }

        editStartTimePanel.add(editStartField, BorderLayout.CENTER);
        editStartTimePanel.add(editStartAmPm, BorderLayout.EAST);
        gbc.gridx = 1;
        editPanel.add(editStartTimePanel, gbc);


        // End time field
        gbc.gridx = 0; gbc.gridy = 2;
        editPanel.add(new JLabel("End Time:"), gbc);

        JPanel editEndTimePanel = new JPanel(new BorderLayout(5, 0));
        JTextField editEndField = new JTextField();
        configureTimeField(editEndField);

        // Convert 24-hour time to 12-hour for display
        String endTime12Hour = convertTo12HourFormat(selectedSchedule.getEndTime());
        String[] endParts = endTime12Hour.split(" ");
        editEndField.setText(endParts[0]);

        JComboBox<String> editEndAmPm = new JComboBox<>(new String[]{"AM", "PM"});
        if (endParts.length > 1) {
            editEndAmPm.setSelectedItem(endParts[1]);
        }

        editEndTimePanel.add(editEndField, BorderLayout.CENTER);
        editEndTimePanel.add(editEndAmPm, BorderLayout.EAST);
        gbc.gridx = 1;
        editPanel.add(editEndTimePanel, gbc);


        // Status checkbox
        gbc.gridx = 0; gbc.gridy = 3;
        editPanel.add(new JLabel("Available:"), gbc);


        final JCheckBox chkAvailable = new JCheckBox();
        chkAvailable.setSelected(selectedSchedule.isAvailable());
        gbc.gridx = 1;
        editPanel.add(chkAvailable, gbc);


        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> {
            try {
                LocalDate newScheduleDate = editSelectedDate[0];

                String startTimeText = editStartField.getText().trim();
                String endTimeText = editEndField.getText().trim();
                String startAmPm = (String) editStartAmPm.getSelectedItem();
                String endAmPm = (String) editEndAmPm.getSelectedItem();

                // Validate time formats
                if (!isValidTimeFormat(startTimeText)) {
                    JOptionPane.showMessageDialog(editDialog,
                            "Invalid start time format!\nUse hh:mm (e.g., 08:00, 1:30)",
                            "Invalid Time", JOptionPane.ERROR_MESSAGE);
                    editStartField.requestFocus();
                    return;
                }


                if (!isValidTimeFormat(endTimeText)) {
                    JOptionPane.showMessageDialog(editDialog,
                            "Invalid end time format!\nUse hh:mm (e.g., 09:00, 2:30)",
                            "Invalid Time", JOptionPane.ERROR_MESSAGE);
                    editEndField.requestFocus();
                    return;
                }

                LocalTime newStartTime = parse12HourTime(startTimeText, startAmPm);
                LocalTime newEndTime = parse12HourTime(endTimeText, endAmPm);


                // Time validation
                if (newEndTime.isBefore(newStartTime) || newEndTime.equals(newStartTime)) {
                    JOptionPane.showMessageDialog(editDialog, "End time must be after start time!",
                            "Invalid Time", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                // Use the finalSchedule reference
                finalSchedule.setScheduleDate(newScheduleDate);
                finalSchedule.setStartTime(newStartTime);
                finalSchedule.setEndTime(newEndTime);
                finalSchedule.setAvailable(chkAvailable.isSelected());


                if (scheduleDAO.updateSchedule(finalSchedule)) {
                    JOptionPane.showMessageDialog(editDialog, "Schedule updated!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSchedules();
                    editDialog.dispose();
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(editDialog,
                        "Error parsing time: " + ex.getMessage() +
                                "\nPlease use format: hh:mm (e.g., 08:00, 1:30)",
                        "Time Parse Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editDialog, "Error updating schedule!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        btnCancel.addActionListener(e -> editDialog.dispose());


        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);


        editDialog.add(editPanel, BorderLayout.CENTER);
        editDialog.add(buttonPanel, BorderLayout.SOUTH);
        editDialog.setVisible(true);
    }

    private String convertTo12HourFormat(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        String amPm = "AM";

        if (hour >= 12) {
            amPm = "PM";
        }

        if (hour > 12) {
            hour -= 12;
        } else if (hour == 0) {
            hour = 12;
        }

        return String.format("%d:%02d %s", hour, minute, amPm);
    }


    private void deleteSchedule() {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to delete!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }


        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);


        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this schedule?\nThis action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);


        if (confirm == JOptionPane.YES_OPTION) {
            if (scheduleDAO.deleteSchedule(scheduleId)) {
                JOptionPane.showMessageDialog(this, "Schedule deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSchedules();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete schedule!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void markAsAvailable(boolean available) {
        int selectedRow = scheduleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }


        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);


        // Get schedule from database
        List<CaptainSchedule> allSchedules = scheduleDAO.getAllSchedules();
        for (CaptainSchedule schedule : allSchedules) {
            if (schedule.getScheduleId() == scheduleId) {
                schedule.setAvailable(available);
                if (scheduleDAO.updateSchedule(schedule)) {
                    String status = available ? "Available" : "Unavailable";
                    JOptionPane.showMessageDialog(this,
                            "Schedule marked as " + status + "!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadSchedules();
                }
                break;
            }
        }
    }


    private void addWeeklySchedule() {
        try {
            // Get date from button
            String dateText = dateButton.getText();
            LocalDate startDate = LocalDate.parse(dateText);


            // Get time from fields
            String startTimeText = startTimeField.getText().trim();
            String endTimeText = endTimeField.getText().trim();
            String startAmPm = (String) startAmPmCombo.getSelectedItem();
            String endAmPm = (String) endAmPmCombo.getSelectedItem();


            // Validate time formats
            if (!isValidTimeFormat(startTimeText)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid start time format!\nUse hh:mm (e.g., 08:00, 1:30)",
                        "Invalid Time", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (!isValidTimeFormat(endTimeText)) {
                JOptionPane.showMessageDialog(this,
                        "Invalid end time format!\nUse hh:mm (e.g., 09:00, 2:30)",
                        "Invalid Time", JOptionPane.ERROR_MESSAGE);
                return;
            }


            LocalTime startTime = parse12HourTime(startTimeText, startAmPm);
            LocalTime endTime = parse12HourTime(endTimeText, endAmPm);


            // Time validation
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                JOptionPane.showMessageDialog(this, "End time must be after start time!",
                        "Invalid Time", JOptionPane.ERROR_MESSAGE);
                return;
            }


            int daysToAdd = 7; // One week
            int addedCount = 0;
            int skippedCount = 0;
            StringBuilder skippedDates = new StringBuilder();


            for (int i = 0; i < daysToAdd; i++) {
                LocalDate scheduleDate = startDate.plusDays(i);


                // Check if schedule already exists for this date and time
                if (!scheduleDAO.isTimeSlotAvailable(scheduleDate, startTime, endTime)) {
                    skippedCount++;
                    skippedDates.append("- ").append(scheduleDate).append(" (Already exists/conflict)\n");
                    continue;
                }


                CaptainSchedule schedule = CaptainSchedule.builder()
                        .scheduleDate(scheduleDate)
                        .startTime(startTime)
                        .endTime(endTime)
                        .isAvailable(true)
                        .build();


                if (scheduleDAO.addSchedule(schedule)) {
                    addedCount++;
                }
            }


            String message = "Added " + addedCount + " schedules for the next " + daysToAdd + " days!";
            if (skippedCount > 0) {
                message += "\n\nSkipped " + skippedCount + " dates (already scheduled/conflict):\n" + skippedDates.toString();
            }


            JOptionPane.showMessageDialog(this, message,
                    "Schedule Added", JOptionPane.INFORMATION_MESSAGE);
            loadSchedules();


        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Error parsing time: " + e.getMessage() +
                            "\nPlease use format: hh:mm (e.g., 08:00, 1:30)",
                    "Time Parse Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void clearAllSchedules() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete ALL schedules?\nThis action cannot be undone!",
                "Confirm Delete All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);


        if (confirm == JOptionPane.YES_OPTION) {
            List<CaptainSchedule> allSchedules = scheduleDAO.getAllSchedules();
            int deletedCount = 0;


            for (CaptainSchedule schedule : allSchedules) {
                if (scheduleDAO.deleteSchedule(schedule.getScheduleId())) {
                    deletedCount++;
                }
            }


            JOptionPane.showMessageDialog(this,
                    "Deleted " + deletedCount + " schedules!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSchedules();
        }
    }


    // Custom cell renderer for Status column
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);


            String status = (String) value;
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Arial", Font.BOLD, 11));


            if ("Available".equals(status)) {
                setBackground(AVAILABLE_COLOR);
                setForeground(Color.WHITE);
                setOpaque(true);
            } else if ("Unavailable".equals(status)) {
                setBackground(UNAVAILABLE_COLOR);
                setForeground(Color.WHITE);
                setOpaque(true);
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }


            if (isSelected) {
                setBackground(getBackground().darker());
            }


            return c;
        }
    }


    // Button renderer for Actions column
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Arial", Font.PLAIN, 11));
            setBackground(new Color(52, 152, 219));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }


        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }


    // Button editor for Actions column
    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;


        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = table.convertRowIndexToModel(row);
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setBackground(new Color(41, 128, 185));
            button.setForeground(Color.WHITE);
            isPushed = true;
            return button;
        }


        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int modelRow = scheduleTable.convertRowIndexToModel(currentRow);
                int scheduleId = (int) tableModel.getValueAt(modelRow, 0);


                // Show options for selected schedule
                showScheduleOptions(scheduleId);
            }
            isPushed = false;
            return label;
        }


        private void showScheduleOptions(int scheduleId) {
            JPopupMenu popup = new JPopupMenu();


            JMenuItem editItem = new JMenuItem("Edit Schedule");
            JMenuItem deleteItem = new JMenuItem("Delete Schedule");
            JMenuItem toggleItem = new JMenuItem("Toggle Availability");


            editItem.addActionListener(e -> {
                int row = scheduleTable.getSelectedRow();
                if (row != -1) {
                    editSchedule();
                }
            });


            deleteItem.addActionListener(e -> {
                int row = scheduleTable.getSelectedRow();
                if (row != -1) {
                    deleteSchedule();
                }
            });


            toggleItem.addActionListener(e -> {
                int row = scheduleTable.getSelectedRow();
                if (row != -1) {
                    String currentStatus = (String) tableModel.getValueAt(row, 5);
                    markAsAvailable(!"Available".equals(currentStatus));
                }
            });


            popup.add(editItem);
            popup.add(deleteItem);
            popup.addSeparator();
            popup.add(toggleItem);


            // Show popup near the clicked cell
            Rectangle cellRect = scheduleTable.getCellRect(currentRow, 6, true);
            popup.show(scheduleTable, cellRect.x, cellRect.y + cellRect.height);
        }
    }


    // Public method to get available dates for blotter hearing
    public List<LocalDate> getAvailableDates() {
        List<CaptainSchedule> schedules = scheduleDAO.getAllSchedules();
        List<LocalDate> availableDates = new java.util.ArrayList<>();


        for (CaptainSchedule schedule : schedules) {
            if (schedule.isAvailable() &&
                    !availableDates.contains(schedule.getScheduleDate())) {
                availableDates.add(schedule.getScheduleDate());
            }
        }


        return availableDates;
    }


    // Public method to get available time slots for a specific date
    public List<String> getAvailableTimeSlots(LocalDate date) {
        List<CaptainSchedule> schedules = scheduleDAO.getAvailableSchedulesByDate(date);
        List<String> timeSlots = new java.util.ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


        for (CaptainSchedule schedule : schedules) {
            String timeSlot = schedule.getStartTime().format(timeFormatter) + " - " +
                    schedule.getEndTime().format(timeFormatter);
            timeSlots.add(timeSlot);
        }


        return timeSlots;
    }


    // Method to check if captain is available on a specific date and time
    public boolean isCaptainAvailable(LocalDate date, LocalTime time) {
        List<CaptainSchedule> schedules = scheduleDAO.getAvailableSchedulesByDate(date);


        for (CaptainSchedule schedule : schedules) {
            if (!time.isBefore(schedule.getStartTime()) &&
                    time.isBefore(schedule.getEndTime())) {
                return true;
            }
        }
        return false;
    }
}

