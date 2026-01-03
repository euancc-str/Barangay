package org.example.Admin;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
    // SEARCH FUNCTIONALITY
    // =========================================================================
    private void performSearch() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
            lblTotal.setText("Active Borrows: " + tableModel.getRowCount());
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            lblTotal.setText("Showing " + sorter.getViewRowCount() + " of " + tableModel.getRowCount());
        }
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

        // Search
        JPanel searchP = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchP.setBackground(BG_COLOR);
        searchField = new JTextField(20);
        searchField.setFont(MAIN_FONT);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { performSearch(); }
        });

        searchP.add(new JLabel("Search Records: "));
        searchP.add(searchField);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { searchField.setText(""); performSearch(); });
        searchP.add(clearBtn);

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
    // CALENDAR DIALOG
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
}