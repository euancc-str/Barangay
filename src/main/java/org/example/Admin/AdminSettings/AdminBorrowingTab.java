package org.example.Admin.AdminSettings;

import org.example.Admin.AdminBlotterTab;
import org.example.Admin.SystemLogDAO;
import org.example.BorrowingDAO;
import org.example.BarangayAssetDAO;
import org.example.ResidentDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayAsset;
import org.example.Users.BorrowRecord;
import org.example.Users.Resident;
import org.example.utils.AutoRefresher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminBorrowingTab extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JLabel lblTotal;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94); // Green Header
    private final Color BTN_LEND_COLOR = new Color(76, 175, 80);  // Green
    private final Color BTN_RETURN_COLOR = new Color(100, 149, 237); // Blue
    private final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 14);

    public AdminBorrowingTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {

                if (refresher != null) {
                    refresher.stop();
                }
                refresher = new AutoRefresher("Asset", AdminBorrowingTab.this::loadData);
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

    public void loadData() {
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() throws Exception {
                return new BorrowingDAO().getActiveBorrows();
            }

            @Override
            protected void done() {
                try {
                    List<BorrowRecord> list = get();
                    tableModel.setRowCount(0);

                    for (BorrowRecord r : list) {
                        tableModel.addRow(new Object[]{
                                r.getBorrowId(),
                                r.getAssetId(), // Hidden Column usually, but good for reference
                                r.getAssetName(),
                                r.getBorrowerName(),
                                r.getDateBorrowed(),
                                r.getExpectedReturnDate(),
                                r.getStatus()
                        });
                    }
                    lblTotal.setText("Active Borrows: " + list.size());
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // =========================================================================
    // ACTIONS (LEND & RETURN)
    // =========================================================================

    private void handleLend() {
        showLendDialog();
    }

    private void handleReturn() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to mark as returned.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int borrowId = (int) tableModel.getValueAt(modelRow, 0);
        int assetId = (int) tableModel.getValueAt(modelRow, 1);
        String assetName = (String) tableModel.getValueAt(modelRow, 2);
        String borrower = (String) tableModel.getValueAt(modelRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark '" + assetName + "' as Returned?\nBorrowed by: " + borrower,
                "Confirm Return", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Ask for remarks (Optional)
            String remarks = JOptionPane.showInputDialog(this, "Remarks (Optional - e.g. 'Good Condition'):");
            if (remarks == null) remarks = "Returned in Good Condition";

            boolean success = new BorrowingDAO().returnItem(borrowId, assetId, Date.valueOf(LocalDate.now()), remarks);

            if (success) {
                JOptionPane.showMessageDialog(this, "Item returned successfully.");
                int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
                try { new SystemLogDAO().addLog("Asset Returned: " + assetName, borrower, staffId); } catch (Exception e){}
                loadData(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Database Error during return.");
            }
        }
    }

    // =========================================================================
    // LEND DIALOG
    // =========================================================================
    private void showLendDialog() {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lend Asset (Hiram)", true);
        d.setSize(500, 450);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);

        // --- FIELDS ---
        // Asset
        JTextField txtAsset = createStyledTextField("");
        txtAsset.setEditable(false);
        JTextField txtAssetId = new JTextField(); // Hidden ID

        // Resident
        JTextField txtResident = createStyledTextField("");
        txtResident.setEditable(false);
        JTextField txtResidentId = new JTextField(); // Hidden ID

        // Dates
        JTextField txtBorrowDate = createStyledTextField(LocalDate.now().toString());
        JTextField txtReturnDate = createStyledTextField(LocalDate.now().plusDays(3).toString()); // Default 3 days

        // --- PICKER BUTTONS ---
        JButton btnPickAsset = new JButton("Select Asset");
        btnPickAsset.addActionListener(e -> showAssetPicker(txtAsset, txtAssetId));

        JButton btnPickResident = new JButton("Select Borrower");
        btnPickResident.addActionListener(e -> showResidentPicker(txtResident, txtResidentId));

        // --- LAYOUT ---
        addStyledRow(form, "1. Select Asset:", createPickerPanel(txtAsset, btnPickAsset));
        addStyledRow(form, "2. Select Borrower:", createPickerPanel(txtResident, btnPickResident));
        addStyledRow(form, "3. Date Borrowed:", createDatePickerPanel(txtBorrowDate));
        addStyledRow(form, "4. Expected Return:", createDatePickerPanel(txtReturnDate));

        // --- SAVE BUTTON ---
        JButton btnSave = createRoundedButton("Confirm Transaction", BTN_LEND_COLOR);
        btnSave.addActionListener(e -> {
            try {
                if(txtAssetId.getText().isEmpty() || txtResidentId.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(d, "Please select both an Asset and a Borrower.");
                    return;
                }

                int aId = Integer.parseInt(txtAssetId.getText());
                int rId = Integer.parseInt(txtResidentId.getText());
                Date bDate = Date.valueOf(txtBorrowDate.getText());
                Date rDate = Date.valueOf(txtReturnDate.getText());

                // DAO Call
                boolean success = new BorrowingDAO().lendItem(aId, rId, bDate, rDate);

                if (success) {
                    JOptionPane.showMessageDialog(d, "Transaction Saved!");
                    int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
                    Resident residentName = new ResidentDAO().findResidentById(rId);

                    try {    new SystemLogDAO().addLog("Lent Asset ID " + aId,residentName.getFirstName() + " " + residentName.getLastName(), staffId);
                    } catch (Exception ex){}
                    loadData();
                    d.dispose();
                } else {
                    JOptionPane.showMessageDialog(d, "Error saving transaction.");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Invalid Data. Please check dates.");
            }
        });

        JPanel btnP = new JPanel();
        btnP.setBackground(Color.WHITE);
        btnP.add(btnSave);

        d.add(form, BorderLayout.CENTER);
        d.add(btnP, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // =========================================================================
    // PICKERS (Asset & Resident)
    // =========================================================================
    private void showAssetPicker(JTextField nameField, JTextField idField) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Available Asset", true);
        d.setSize(600, 400);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);

        // Search Panel
        JPanel searchP = new JPanel(new FlowLayout());
        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(MAIN_FONT);
        searchP.add(new JLabel("Search Asset:"));
        searchP.add(txtSearch);

        // Add search button
        JButton searchBtn = new JButton("🔍");
        searchBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        searchBtn.setToolTipText("Search");
        searchP.add(searchBtn);

        // Table
        String[] cols = {"ID", "Item Name", "Prop No", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(25);
        t.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        t.setRowSorter(sorter);

        // Load Only GOOD/AVAILABLE Assets
        List<BarangayAsset> list = new BarangayAssetDAO().getAllAssets();
        for(BarangayAsset a : list) {
            // Filter: Only show if NOT currently borrowed and status is Good
            if(!"Borrowed".equalsIgnoreCase(a.getStatus()) && !"Lost".equalsIgnoreCase(a.getStatus()) && !"Disposed".equalsIgnoreCase(a.getStatus())) {
                m.addRow(new Object[]{ a.getAssetId(), a.getItemName(), a.getPropertyNumber(), a.getStatus() });
            }
        }

        // Search Filter Logic
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterAssetTable(txtSearch.getText(), sorter);
            }
        });

        searchBtn.addActionListener(e -> {
            filterAssetTable(txtSearch.getText(), sorter);
        });

        // Selection Logic
        JButton btnSelect = new JButton("Select Asset");
        btnSelect.addActionListener(e -> {
            int row = t.getSelectedRow();
            if(row != -1) {
                int modelRow = t.convertRowIndexToModel(row);
                idField.setText(m.getValueAt(modelRow, 0).toString());
                nameField.setText(m.getValueAt(modelRow, 1).toString());
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, "Please select an asset first.");
            }
        });

        d.add(searchP, BorderLayout.NORTH);
        d.add(new JScrollPane(t), BorderLayout.CENTER);
        d.add(btnSelect, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // Helper method for filtering the asset picker table
    private void filterAssetTable(String searchText, TableRowSorter<DefaultTableModel> sorter) {
        if (searchText.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Search across all columns except ID (column 0)
            RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    for (int i = 1; i < entry.getValueCount(); i++) { // Start from column 1 (Item Name)
                        Object value = entry.getValue(i);
                        if (value != null) {
                            String stringValue = value.toString().toLowerCase();
                            if (stringValue.contains(searchText.toLowerCase())) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            };
            sorter.setRowFilter(filter);
        }
    }

    private void showResidentPicker(JTextField nameField, JTextField idField) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Borrower", true);
        d.setSize(600, 400);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);

        // Search Bar
        JPanel searchP = new JPanel(new FlowLayout());
        JTextField txtSearch = new JTextField(20);
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
        JButton btnSelect = new JButton("Select Resident");
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
    // UI HELPERS (Structure & Components)
    // =========================================================================
// =========================================================================
// SEARCH FUNCTIONALITY
// =========================================================================

    private void performSearch() {
        String searchText = searchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            // Show all rows if search is empty
            sorter.setRowFilter(null);
            lblTotal.setText("Active Borrows: " + tableModel.getRowCount());
            return;
        }

        // Create a RowFilter that searches across visible columns
        RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Search only visible columns (skip hidden ID columns 0 and 1)
                for (int i = 2; i < entry.getValueCount(); i++) { // Start from column 2 (Asset Name)
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

        sorter.setRowFilter(filter);

        // Update record count to show filtered results
        int visibleRowCount = sorter.getViewRowCount();
        lblTotal.setText("Showing " + visibleRowCount + " of " + tableModel.getRowCount() + " active borrows");
    }
    private JPanel createContentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));

        // Top Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btns.setBackground(BG_COLOR);

        JButton btnLend = createRoundedButton("➕ New Transaction", BTN_LEND_COLOR);
        btnLend.addActionListener(e -> handleLend());

        JButton btnReturn = createRoundedButton("✅ Mark Returned", BTN_RETURN_COLOR);
        btnReturn.addActionListener(e -> handleReturn());

        btns.add(btnLend);
        btns.add(btnReturn);
        p.add(btns);
        p.add(Box.createVerticalStrut(20));

        // Search Panel - UPDATED
        JPanel searchP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchP.setBackground(BG_COLOR);
        searchField = new JTextField(20);
        searchField.setFont(MAIN_FONT);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                performSearch(); // Changed to call our new method
            }
        });

        searchP.add(new JLabel("Search Records: "));
        searchP.add(searchField);

        // Add search button
        JButton searchButton = new JButton("🔍");
        searchButton.setFont(new Font("Arial", Font.PLAIN, 14));
        searchButton.addActionListener(e -> performSearch());
        searchButton.setToolTipText("Search");
        searchP.add(searchButton);

        // Add clear button
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.PLAIN, 12));
        clearButton.setBackground(new Color(220, 220, 220));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            performSearch();
        });
        searchP.add(clearButton);

        p.add(searchP);
        p.add(Box.createVerticalStrut(10));

        // Table
        String[] cols = {"Borrow ID", "Asset ID", "Asset Name", "Borrower", "Date Borrowed", "Due Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Hide IDs columns (Optional, but usually cleaner)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setWidth(0);

        p.add(new JScrollPane(table));

        lblTotal = new JLabel("Total Active: 0");
        p.add(lblTotal);

        return p;
    }

    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HEADER_BG);
        h.setBorder(new EmptyBorder(25, 40, 25, 40));
        JLabel l = new JLabel("Logistics & Borrowing (Hiram)");
        l.setFont(new Font("Arial", Font.BOLD, 26));
        l.setForeground(Color.WHITE);
        h.add(l, BorderLayout.WEST);
        return h;
    }

    private JTextField createStyledTextField(String text) {
        JTextField t = new JTextField(text);
        t.setFont(MAIN_FONT);
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
        p.add(Box.createVerticalStrut(15));
    }

    // Combine Textfield + Button for Pickers
    private JPanel createPickerPanel(JTextField field, JButton btn) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.WHITE);
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    // Combine Textfield + Calendar Button
    private JPanel createDatePickerPanel(JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.WHITE);
        JButton btn = new JButton("📅");
        btn.setPreferredSize(new Dimension(45, 25));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> showDatePicker(field));
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    // =========================================================================
    // CALENDAR DIALOG (Completed)
    // =========================================================================

    private void showDatePicker(JTextField targetField) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        CalendarDialog calendar = new CalendarDialog(parentWindow, targetField);
        calendar.setVisible(true);
    }

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

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(Color.WHITE);
            headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JButton btnPrev = new JButton(" < ");
            JButton btnNext = new JButton(" > ");
            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Arial", Font.BOLD, 16));

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
            // Update month/year label
            lblMonthYear.setText(currentDate.format(monthYearFormatter));

            // Clear the days panel
            daysPanel.removeAll();

            // Add day headers
            String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String dayName : dayNames) {
                JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
                dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
                dayLabel.setForeground(new Color(100, 100, 100));
                daysPanel.add(dayLabel);
            }

            // Get first day of month and number of days
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            int daysInMonth = currentDate.lengthOfMonth();

            // Get day of week for first day (1 = Monday, 7 = Sunday in Java)
            int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue();
            // Convert to 0 = Sunday, 6 = Saturday for our calendar
            int startOffset = (dayOfWeekValue % 7);

            // Add empty cells for days before the first day
            for (int i = 0; i < startOffset; i++) {
                daysPanel.add(new JLabel(""));
            }

            // Add day buttons
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate dayDate = currentDate.withDayOfMonth(day);
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setFont(new Font("Arial", Font.PLAIN, 12));
                dayButton.setFocusPainted(false);

                // Style for today
                if (dayDate.equals(LocalDate.now())) {
                    dayButton.setBackground(new Color(220, 240, 255));
                    dayButton.setForeground(Color.BLUE);
                } else if (dayDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    dayButton.setForeground(Color.RED);
                } else {
                    dayButton.setBackground(Color.WHITE);
                }

                // Style for selected date
                try {
                    if (!targetField.getText().isEmpty()) {
                        LocalDate selectedDate = LocalDate.parse(targetField.getText(), dateFormatter);
                        if (dayDate.equals(selectedDate)) {
                            dayButton.setBackground(new Color(180, 220, 240));
                            dayButton.setForeground(Color.BLACK);
                        }
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }

                // Add action listener
                final LocalDate selectedDay = dayDate;
                dayButton.addActionListener(e -> {
                    targetField.setText(selectedDay.format(dateFormatter));
                    dispose();
                });

                daysPanel.add(dayButton);
            }

            // Fill remaining cells to complete the grid
            int totalCells = 42; // 6 rows * 7 columns
            int currentCells = startOffset + daysInMonth;
            for (int i = currentCells; i < totalCells; i++) {
                daysPanel.add(new JLabel(""));
            }

            // Refresh the panel
            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.setProperty("sun.java2d.opengl", "true");
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Barangay Blotter System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 900);
            frame.setLocationRelativeTo(null);

            frame.add(new AdminBorrowingTab());
            frame.setVisible(true);
        });
    }
}