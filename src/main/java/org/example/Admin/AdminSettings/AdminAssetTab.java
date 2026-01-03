package org.example.Admin.AdminSettings;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.Admin.AdminResidentTab;
import org.example.BarangayAssetDAO; // Ensure this import matches your package
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
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
public class AdminAssetTab extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblRecordCount;
    private JTextField searchField;

    // --- VISUAL STYLE VARIABLES (Same as others) ---
// --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);       // Dark Blue/Grey
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219); // Blue
    private final Color BTN_ADD_COLOR = new Color(46, 204, 113);   // Green
    private final Color BTN_PRINT_COLOR = new Color(155, 89, 182); // Purple
    private final Color BTN_UPDATE_COLOR = new Color(52, 152, 219);// Blue
    private final Color BTN_DELETE_COLOR = new Color(231, 76, 60); // Red
    private final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 14);

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
                // Using dateAcquired as the timestamp since it's the most relevant date field
                String sql = "SELECT " +
                        "COUNT(*) as total_count, " +
                        "UNIX_TIMESTAMP(MAX(dateAcquired)) as last_ts " +  // Using dateAcquired as timestamp
                        "FROM barangay_asset";                              // Your table name

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
                performSearch();
            }
        });

        // Optional: Add search button for manual trigger
        JButton searchButton = new JButton("🔍");
        searchButton.addActionListener(e -> performSearch());
        searchButton.setToolTipText("Search");

        // Add this button to your search panel in createContentPanel()
    }

    private void performSearch() {
        String searchText = searchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            // Show all rows if search is empty
            sorter.setRowFilter(null);
            lblRecordCount.setText("Total Assets: " + tableModel.getRowCount());
            return;
        }

        // Create a RowFilter that searches across multiple columns
        RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                // Check each column for a match
                for (int i = 0; i < entry.getValueCount(); i++) {
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
        lblRecordCount.setText("Showing " + visibleRowCount + " of " + tableModel.getRowCount() + " assets");
    }

// =========================================================================
// IMPLEMENTATION STEPS
// =========================================================================

// 1. Add this method call in your constructor after initializing components:
// setupSearchFunctionality();

// 2. Modify your createContentPanel() method to include the search button:
// In the searchPanel section, add:
/*
    JButton searchButton = new JButton("🔍");
    searchButton.setFont(new Font("Arial", Font.PLAIN, 14));
    searchButton.addActionListener(e -> performSearch());
    searchButton.setToolTipText("Search");
    searchPanel.add(searchButton);
*/

// 3. For better UX, add a clear search button:
/*
    JButton clearButton = new JButton("Clear");
    clearButton.setFont(new Font("Arial", Font.PLAIN, 12));
    clearButton.addActionListener(e -> {
        searchField.setText("");
        performSearch();
    });
    searchPanel.add(clearButton);
*/

// 4. Call setupSearchFunctionality() in your constructor:
// Add this line at the end of your constructor:
// setupSearchFunctionality();

// =========================================================================
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

        // --- ADD ROWS ---
        addStyledRow(formPanel, "Item Name:", txtName);
        addStyledRow(formPanel, "Property Code:", txtPropCode);
        addStyledRow(formPanel, "Property Number:", txtPropNo);
        addStyledRow(formPanel, "Serial Number:", txtSerial);
        addStyledRow(formPanel, "Brand:", txtBrand);
        addStyledRow(formPanel, "Model:", txtModel);

        // *** USE THE NEW VISUAL PICKER HERE ***
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

    // --- DATE PICKER HELPERS (Updated to use Calendar Grid) ---

    private void showDatePicker(JTextField targetField) {
        // Open the Custom Calendar Dialog
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        AssetCalendarDialog calendar = new AssetCalendarDialog(parentWindow, targetField);
        calendar.setVisible(true);
    }

    private JPanel createDatePickerPanel(JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.WHITE);

        // Calendar Icon Button
        JButton btn = new JButton("📅");
        btn.setPreferredSize(new Dimension(45, 25));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showDatePicker(field));

        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
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

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BG_COLOR);
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (sorter != null) sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
            }
        });
        searchPanel.add(new JLabel("Search Asset: "));
        searchPanel.add(searchField);
        p.add(searchPanel);
        p.add(Box.createVerticalStrut(10));


// Add search button
        JButton searchButton = new JButton("🔍");
        searchButton.setFont(new Font("Arial", Font.PLAIN, 14));
        searchButton.addActionListener(e -> performSearch());
        searchButton.setToolTipText("Search");
        searchPanel.add(searchButton);

// Add clear button (optional)
        JButton clearButton = createRoundedButton("Clear", new Color(150, 150, 150));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            performSearch();
        });
        searchPanel.add(clearButton);

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
                Paragraph title = new Paragraph("Asset Report", titleFont);
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

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        // Creates the rounded corner background effect
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
        titlePanel.setBackground(HEADER_BG); // Matches the rounded background

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
class AssetCalendarDialog extends JDialog {
    private LocalDate currentDate;
    private JTextField targetField;
    private JLabel lblMonthYear;
    private JPanel daysPanel;

    public AssetCalendarDialog(Window parent, JTextField targetField) {
        super(parent, "Select Date", ModalityType.APPLICATION_MODAL);
        this.targetField = targetField;

        // Parse current date from field or use today
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

        // --- HEADER (Month Navigation) ---
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

        // --- BODY (Days Grid) ---
        daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        daysPanel.setBackground(Color.WHITE);
        daysPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(headerPanel, BorderLayout.NORTH);
        add(daysPanel, BorderLayout.CENTER);

        // Add Today and Cancel buttons
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

        // Update Label
        lblMonthYear.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // Add Weekday Headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Arial", Font.BOLD, 12));
            daysPanel.add(l);
        }

        // Calculate Blank Spaces
        YearMonth yearMonth = YearMonth.of(currentDate.getYear(), currentDate.getMonth());
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Mon, 7=Sun

        // Adjust for Sunday start (Java uses Monday=1)
        int emptySlots = (dayOfWeek == 7) ? 0 : dayOfWeek;

        for (int i = 0; i < emptySlots; i++) {
            daysPanel.add(new JLabel(""));
        }

        // Add Day Buttons
        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            int finalDay = day;
            JButton btn = new JButton(String.valueOf(day));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);

            // Highlight today if relevant
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