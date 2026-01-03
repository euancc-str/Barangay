// AdminRequestTab.java - Updated color scheme
package org.example.Admin;


import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;
import org.example.StaffDAO;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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


    // --- UPDATED VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250); // Lighter, cleaner background
    private final Color HEADER_BG = new Color(44, 62, 80); // Dark blue-gray for header
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219); // Modern blue for table header
    private final Color BTN_UPDATE_COLOR = new Color(41, 128, 185); // Deeper blue for update
    private final Color BTN_DELETE_COLOR = new Color(231, 76, 60); // Coral red for delete


    public AdminRequestTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);
        updateRecordCount();
        loadRequestData();
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


    private void startLightPolling() {
        lightTimer = new javax.swing.Timer(3000, e -> { // Every 3 seconds
            if (requestTable != null && requestTable.getSelectedRow() == -1) {
                // Just check a simple "last updated" flag
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
                        "COALESCE(updatedAt, '1970-01-01'), " +
                        "COALESCE(createdAt, '1970-01-01')" +
                        "))) as last_ts " +
                        "FROM document_request";


                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {


                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        long count = rs.getLong("total_count");
                        long timestamp = rs.getLong("last_ts") * 1000L;
                        return new Object[] { count, timestamp };
                    }
                }
                return new Object[] { 0L, 0L };
            }


            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    long currentCount = (Long) result[0];
                    long dbTimestamp = (Long) result[1];


                    // Store last known count as a class variable
                    if (lastKnownCount == -1) {
                        lastKnownCount = currentCount;
                    }


                    // Check BOTH timestamp AND count
                    if (dbTimestamp > lastGlobalUpdate || currentCount != lastKnownCount) {
                        System.out.println("Change detected! Refreshing data...");
                        lastGlobalUpdate = dbTimestamp;
                        lastKnownCount = currentCount;
                        loadRequestData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    // Add this class variable:
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
                        tableModel.setRowCount(0);
                    }


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


                    // 3. Refresh Filters if needed
                    if (searchField != null && !searchField.getText().isEmpty()) {
                        applyFilters();
                    } else {
                        updateRecordCount();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    private void applyFilters() {
        try {
            String text = searchField != null ? searchField.getText() : "";
            String status = statusFilterBox != null ? (String) statusFilterBox.getSelectedItem() : "All Status";


            List<RowFilter<Object, Object>> filters = new ArrayList<>();


            // 1. Text Search Filter (Across all columns except ID)
            if (text != null && !text.trim().isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + text.trim()));
            }


            // 2. Status Filter (Specific to Status Column, index 4)
            if (status != null && !status.equals("All Status")) {
                filters.add(RowFilter.regexFilter("(?i)^" + status + "$", 4));
            }


            // Apply combined filters safely
            if (filters.isEmpty()) {
                if (sorter != null) {
                    sorter.setRowFilter(null);
                }
            } else {
                if (sorter != null) {
                    sorter.setRowFilter(RowFilter.andFilter(filters));
                }
            }


            // Update count safely
            SwingUtilities.invokeLater(() -> {
                if (lblRecordCount != null) {
                    updateRecordCount();
                }
            });


        } catch (Exception e) {
            System.err.println("[" + new Date() + "] Error in applyFilters: " + e.getMessage());
            e.printStackTrace();


            // Reset filters on error
            if (sorter != null) {
                sorter.setRowFilter(null);
            }
        }
        updateRecordCount();
    }


    // =========================================================================
    // 1. DELETE FUNCTIONALITY (With Error Prevention)
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


        // ERROR PREVENTION: Warning Dialog
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
            // TODO: Call DAO delete method here (e.g., residentDAO.deleteRequest(reqId))
            new StaffDAO().deleteRequest(Integer.parseInt(reqId));
            tableModel.removeRow(modelRow);
            JOptionPane.showMessageDialog(this, "Record deleted successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // =========================================================================
    // 2. UPDATE FUNCTIONALITY
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // Less rounded corner
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
        // FIX 8: Reduced button internal padding (Was 10, 20 -> Now 8, 15)
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
        // FIX 1: Reduced outer margin (Was 35, 60 -> Now 15, 20)
        contentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));


        // --- 1. ACTION BUTTONS ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Reduced gap between buttons
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Reduced height slightly


        JButton btnDelete = createRoundedButton("Delete Record", BTN_DELETE_COLOR);
        btnDelete.setPreferredSize(new Dimension(150, 40)); // Made smaller
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
        btnRefresh.setPreferredSize(new Dimension(120, 40)); // Made smaller
        btnRefresh.addActionListener(e -> loadRequestData());


        buttonPanel.add(btnRefresh);


        btnPrint.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setPreferredSize(new Dimension(140, 40)); // Made smaller
        btnPrint.setBorderPainted(false);
        btnPrint.setContentAreaFilled(false);
        btnPrint.setOpaque(false);
        btnPrint.addActionListener(e -> handlePrint());


        buttonPanel.add(btnDelete);
        buttonPanel.add(btnPrint);


        contentPanel.add(buttonPanel);


        // FIX 2: Reduced vertical gap (Was 30 -> Now 10)
        contentPanel.add(Box.createVerticalStrut(10));


        // --- 2. SEARCH & FILTER PANEL (Updated) ---
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


        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("  Status:"));
        searchPanel.add(statusFilterBox);


        contentPanel.add(searchPanel);
        // FIX 3: Reduced gap before table (Was 10 -> Now 8)
        contentPanel.add(Box.createVerticalStrut(8));


        // --- 3. TABLE SETUP ---
        String[] columnNames = { "Request ID", "Resident Name", "Document Type", "Purpose", "Status", "Date" };


        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };


        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 13)); // Slightly smaller font


        // FIX 4: Reduced Row Height significantly (Was 50 -> Now 30)
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


        // Comparator setup... (kept same)
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
        // FIX 5: Reduced Header Height (Was 50 -> Now 35)
        header.setPreferredSize(new Dimension(header.getWidth(), 35));


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));


        // FIX 6: REMOVED setMaximumSize so table fills the screen
        // tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500)); <--
        // DELETED


        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(BG_COLOR);


        lblRecordCount = new JLabel("Total Records: " + tableModel.getRowCount());
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 12));
        footerPanel.add(lblRecordCount);


        contentPanel.add(tableScrollPane); // Added directly to fill space
        contentPanel.add(footerPanel);


        return contentPanel;
    }


    private JLabel lblRecordCount;


    private void updateRecordCount() {
        int count = requestTable.getRowCount(); // Gets filtered count
        lblRecordCount.setText("Total Records: " + count);
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

    private final Color CERULEAN_BLUE = new Color(100, 149, 237);


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);


        // FIX 7: Reduced Header Padding (Was 25, 40 -> Now 15, 20)
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(HEADER_BG);
                        g2.fillRoundRect(x, y, width, height, 15, 15); // Smaller rounding
                    }
                }, new EmptyBorder(15, 20, 15, 20)));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);


        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 20)); // Slightly smaller
        lblSystem.setForeground(Color.WHITE);


        JLabel lblModule = new JLabel("Admin Dashboard");
        lblModule.setFont(new Font("Arial", Font.BOLD, 16)); // Slightly smaller
        lblModule.setForeground(Color.WHITE);


        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
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
}



