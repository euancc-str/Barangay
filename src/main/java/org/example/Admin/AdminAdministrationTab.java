package org.example.Admin;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.Admin.AdminSettings.AdminAssetTab;
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
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;

// =========================================================================
// ADD THESE IMPORTS
// =========================================================================
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.border.Border;
import javax.swing.RowFilter;
import java.util.ArrayList;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.BasicStroke;

public class AdminAdministrationTab extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JLabel lblTotal;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color BTN_LEND_COLOR = new Color(46, 204, 113);   // Green
    private final Color BTN_RETURN_COLOR = new Color(230, 126, 34); // Orange
    private final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 14);

    // =========================================================================
    // ADDED: Date Filter Variables (EXACT COPY from SecretaryPrintDocument)
    // =========================================================================
    private JButton dateFilterBtn; // Changed from JComboBox to JButton
    private java.util.Date selectedDate;
    private JComboBox<String> periodFilterBox;
    private JComboBox<String> yearFilterBox;
    private final Color MODERN_BLUE = new Color(66, 133, 244);
    private final Color LIGHT_GREY = new Color(248, 249, 250);
    private final Color DARK_GREY = new Color(52, 58, 64);

    public AdminAdministrationTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> loadData());
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {

                if (refresher != null) {
                    refresher.stop();
                }
                refresher = new AutoRefresher("Asset", AdminAdministrationTab.this::loadData);
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
                        "UNIX_TIMESTAMP(MAX(GREATEST(" +
                        "COALESCE(dateReturned, '1970-01-01'), " +  // Using dateReturned as updated timestamp
                        "COALESCE(dateBorrowed, '1970-01-01')" +    // Using dateBorrowed as created timestamp
                        "))) as last_ts " +
                        "FROM asset_borrowing";                     // Your table name

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
                        System.out.println("Change detected in asset borrowing! Refreshing...");
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

    // =========================================================================
    // DATA LOADING
    // =========================================================================
    public void loadData() {
        new SwingWorker<List<BorrowRecord>, Void>() {
            @Override
            protected List<BorrowRecord> doInBackground() throws Exception {
                return new BorrowingDAO().getAllBorrows();
            }

            @Override
            protected void done() {
                try {
                    List<BorrowRecord> list = get();
                    tableModel.setRowCount(0);

                    for (BorrowRecord r : list) {
                        tableModel.addRow(new Object[]{
                                r.getBorrowId(),
                                r.getAssetId(),
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
            String remarks = JOptionPane.showInputDialog(this, "Remarks (Optional - e.g. 'Good Condition'):");
            if (remarks == null) remarks = "Returned in Good Condition";

            boolean success = new BorrowingDAO().returnItem(borrowId, assetId, Date.valueOf(LocalDate.now()), remarks);

            if (success) {
                JOptionPane.showMessageDialog(this, "Item returned successfully.");
                int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
                try { new SystemLogDAO().addLog("Asset Returned: " + assetName, borrower, staffId); } catch (Exception e){}
                loadData();
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
        d.setSize(500, 480);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);

        // --- FIELDS ---
        JTextField txtAsset = createStyledTextField("");
        txtAsset.setEditable(false);
        JTextField txtAssetId = new JTextField(); // Hidden ID

        JTextField txtResident = createStyledTextField("");
        txtResident.setEditable(false);
        JTextField txtResidentId = new JTextField(); // Hidden ID

        JTextField txtBorrowDate = createStyledTextField(LocalDate.now().toString());
        JTextField txtReturnDate = createStyledTextField(LocalDate.now().plusDays(3).toString());

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

                boolean success = new BorrowingDAO().lendItem(aId, rId, bDate, rDate);

                if (success) {
                    JOptionPane.showMessageDialog(d, "Transaction Saved!");
                    int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
                    Resident resident = new ResidentDAO().findResidentById(rId);
                    try { new SystemLogDAO().addLog("Lent Asset ID " + aId, resident.getFirstName() + " " + resident.getLastName(), staffId); } catch (Exception ex){}
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

        JPanel searchP = new JPanel(new FlowLayout());
        JTextField txtSearch = new JTextField(20);
        txtSearch.setFont(MAIN_FONT);
        searchP.add(new JLabel("Search Asset:"));
        searchP.add(txtSearch);

        JButton searchBtn = new JButton("🔍");
        searchP.add(searchBtn);

        String[] cols = {"ID", "Item Name", "Prop No", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(25);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(m);
        t.setRowSorter(sorter);

        // Load Only AVAILABLE Assets
        List<BarangayAsset> list = new BarangayAssetDAO().getAllAssets();
        for(BarangayAsset a : list) {
            m.addRow(new Object[]{ a.getAssetId(), a.getItemName(), a.getPropertyNumber(), a.getStatus() });

        }

        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterAssetTable(txtSearch.getText(), sorter); }
        });
        searchBtn.addActionListener(e -> filterAssetTable(txtSearch.getText(), sorter));

        JButton btnSelect = new JButton("Select Asset");
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

    private void filterAssetTable(String searchText, TableRowSorter<DefaultTableModel> sorter) {
        if (searchText.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
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
                Document doc = new Document(PageSize.A4.rotate()); // ✅ Fixed: lowercase 'rotate()'
                PdfWriter.getInstance(doc, new FileOutputStream(file));
                doc.open();

                // 2. Add Title
                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                Paragraph title = new Paragraph("Borrowing List", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);

                // 3. Create Table
                int colCount = table.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);

                // 4. Add Headers (✅ MATCHING YOUR TABLE COLORS)
                // We use the same color: new Color(52, 152, 219)
                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);

                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);

                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(table.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor); // ✅ Blue Background
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8); // More padding like your table
                    pdfTable.addCell(cell);
                }

                // 5. Add Rows
                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = table.getValueAt(i, j);
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

    private void showResidentPicker(JTextField nameField, JTextField idField) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Borrower", true);
        d.setSize(600, 400);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);

        JPanel searchP = new JPanel(new FlowLayout());
        JTextField txtSearch = new JTextField(20);
        searchP.add(new JLabel("Search Name:"));
        searchP.add(txtSearch);

        String[] cols = {"ID", "Name", "Purok"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(25);
        TableRowSorter<DefaultTableModel> sort = new TableRowSorter<>(m);
        t.setRowSorter(sort);

        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                sort.setRowFilter(RowFilter.regexFilter("(?i)" + txtSearch.getText()));
            }
        });

        List<Resident> list = new ResidentDAO().getAllResidents();
        for(Resident r : list) {
            m.addRow(new Object[]{   r.getResidentId(), r.getFirstName() +" " + r.getMiddleName()+" " + r.getLastName(), r.getPurok() });
        }

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
    // SEARCH FUNCTIONALITY (Modified to include date filter)
    // =========================================================================
    private void performSearch() {
        applyFilters(); // Call the new filter method that includes date
    }

    // =========================================================================
    // FILTER LOGIC with Date Filter (EXACT COPY from SecretaryPrintDocument, adapted)
    // =========================================================================
    private void applyFilters() {
        if (sorter == null) return;

        String text = searchField.getText().trim();
        String selectedPeriod = (String) periodFilterBox.getSelectedItem();
        String selectedYear = (String) yearFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Text Search Filter
        if (text != null && !text.isEmpty()) {
            String regexPattern = "(?i)" + java.util.regex.Pattern.quote(text);
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        Object value = entry.getValue(i);
                        if (value != null && value.toString().toLowerCase().contains(text.toLowerCase())) {
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
                        Object val = entry.getValue(4); // Column 4: Date Borrowed
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
        // Only runs if "All Periods" is NOT selected AND Calendar is empty
        if (selectedPeriod != null && !selectedPeriod.equals("All Periods") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(4); // Column 4
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
        // Only runs if "All Years" is NOT selected AND Calendar is empty
        if (selectedYear != null && !selectedYear.equals("All Years") && selectedDate == null) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        Object val = entry.getValue(4); // Column 4
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
        if (lblTotal != null) lblTotal.setText("Active Borrows: " + sorter.getViewRowCount());
    }

    // =========================================================================
    // CALENDAR IMPLEMENTATION (EXACT COPY from SecretaryPrintDocument)
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
    // UI SETUP
    // =========================================================================
    private JPanel createContentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));

        // Toolbar
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        btns.setBackground(BG_COLOR);

        JButton btnLend = createRoundedButton("➕ New Transaction", BTN_LEND_COLOR);
        btnLend.addActionListener(e -> handleLend());

        JButton btnReturn = createRoundedButton("✅ Mark Returned", BTN_RETURN_COLOR);
        btnReturn.addActionListener(e -> handleReturn());
        JButton btnPrint = createRoundedButton("🖨 Print Report", new Color(44, 62, 80));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(160, 40));
        btnPrint.addActionListener(e -> handlePrint());

        btns.add(btnLend);
        btns.add(btnReturn);
        btns.add(btnPrint);
        p.add(btns);
        p.add(Box.createVerticalStrut(20));

        // =========================================================================
        // MODIFIED: Search Panel with Date Filter (EXACT COPY from SecretaryPrintDocument)
        // =========================================================================
        JPanel searchP = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchP.setBackground(BG_COLOR);
        searchP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Label
        JLabel searchLabel = new JLabel("Search Records:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));

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
        searchP.add(Box.createHorizontalStrut(10));

        // 1. Period Filter (This Week/Month)
        JLabel lblPeriod = new JLabel("Filter:");
        lblPeriod.setFont(new Font("Arial", Font.BOLD, 14));
        searchP.add(lblPeriod);

        String[] periods = {"All Periods", "This Week", "This Month"};
        periodFilterBox = new JComboBox<>(periods);
        periodFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        periodFilterBox.setBackground(Color.WHITE);
        periodFilterBox.setFocusable(false);
        periodFilterBox.addActionListener(e -> {
            if (!periodFilterBox.getSelectedItem().equals("All Periods")) {
                // If user selects "This Week", Reset Year and Calendar
                yearFilterBox.setSelectedIndex(0); // Reset Year
                selectedDate = null;
                dateFilterBtn.setText("📅 Select Date");
                dateFilterBtn.setForeground(Color.DARK_GRAY);
            }
            applyFilters();
        });
        searchP.add(periodFilterBox);

        searchP.add(Box.createHorizontalStrut(10));

        // 2. Year Filter (2026, 2025...)
        JLabel lblYear = new JLabel("Year:");
        lblYear.setFont(new Font("Arial", Font.BOLD, 14));
        searchP.add(lblYear);

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
                // If user selects a Year, Reset Period and Calendar
                periodFilterBox.setSelectedIndex(0); // Reset Period
                selectedDate = null;
                dateFilterBtn.setText("📅 Select Date");
                dateFilterBtn.setForeground(Color.DARK_GRAY);
            }
            applyFilters();
        });
        searchP.add(yearFilterBox);

        searchP.add(Box.createHorizontalStrut(10));
        // Date Filter Button (Modern date filter button from SecretaryPrintDocument)
        JLabel dateLabel = new JLabel("  Date Borrowed:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));

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

        // Clear date button (from SecretaryPrintDocument)
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

        // Clear search button
        JButton clearBtn = new JButton("Clear Search");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            selectedDate = null;
            dateFilterBtn.setText("📅 Select Date");
            dateFilterBtn.setForeground(Color.DARK_GRAY);
            applyFilters();
        });

        searchP.add(searchLabel);
        searchP.add(searchField);
        searchP.add(clearBtn);
        searchP.add(Box.createHorizontalStrut(15));
        searchP.add(dateLabel);
        searchP.add(dateFilterBtn);
        searchP.add(Box.createHorizontalStrut(5));
        searchP.add(clearDateBtn);
        searchP.add(Box.createHorizontalStrut(15));


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

        // Hide ID Columns
        table.getColumnModel().getColumn(0).setMinWidth(0); table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(1).setMinWidth(0); table.getColumnModel().getColumn(1).setMaxWidth(0);

        p.add(new JScrollPane(table));

        lblTotal = new JLabel("Total Active: 0");
        p.add(lblTotal);

        return p;
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

        JLabel lblModule = new JLabel("Asset Borrowing & Monitoring");
        lblModule.setFont(new Font("Arial", Font.BOLD, 22));
        lblModule.setForeground(Color.WHITE);

        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
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
        btn.addActionListener(e -> showDatePicker(field));
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }

    // =========================================================================
    // CALENDAR DIALOG (Keep original for other date pickers)
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
            btnToday.addActionListener(e -> { currentDate = LocalDate.now(); targetField.setText(currentDate.format(dateFormatter)); dispose(); });
            JButton btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(e -> dispose());
            buttonPanel.add(btnToday);
            buttonPanel.add(btnCancel);
            add(buttonPanel, BorderLayout.SOUTH);

            updateCalendar();
        }

        private void updateCalendar() {
            lblMonthYear.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            daysPanel.removeAll();

            String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            for (String dayName : dayNames) {
                JLabel l = new JLabel(dayName, SwingConstants.CENTER);
                l.setFont(new Font("Arial", Font.BOLD, 12));
                daysPanel.add(l);
            }

            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue();
            int startOffset = (dayOfWeekValue % 7);

            for (int i = 0; i < startOffset; i++) daysPanel.add(new JLabel(""));

            int daysInMonth = currentDate.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate dayDate = currentDate.withDayOfMonth(day);
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setFocusPainted(false);
                dayButton.setBackground(dayDate.equals(LocalDate.now()) ? new Color(220, 240, 255) : Color.WHITE);
                if (dayDate.equals(LocalDate.now())) dayButton.setForeground(Color.BLUE);

                dayButton.addActionListener(e -> {
                    targetField.setText(dayDate.format(dateFormatter));
                    dispose();
                });
                daysPanel.add(dayButton);
            }
            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }

    // =========================================================================
    // HELPER CLASSES FOR STYLING (EXACT COPY from SecretaryPrintDocument)
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
            JFrame f = new JFrame();
            f.setSize(1000, 700);
            f.add(new AdminAdministrationTab());
            f.setVisible(true);
        });
    }
}
