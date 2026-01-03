// AdminLogsTab.java - Updated color scheme
package org.example.Admin;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class AdminLogsTab extends JPanel {

    private JTable logsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblRecordCount;

    // Class-level variable for filter access
    private JComboBox<String> dateFilterBox;

    // --- UPDATED VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private LocalDateTime lastLogDate = LocalDateTime.MIN; // Initialize to minimum value
    private int lastMaxId = 0;
    private javax.swing.Timer autoRefreshTimer;

    public AdminLogsTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        // Load data initially
        loadLogData();
        startAutoRefresh();
    }


    public void loadLogData() {
        // 1. Get current filter (safely)
        String filter = (dateFilterBox != null) ? (String) dateFilterBox.getSelectedItem() : "All Time";

        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                // HEAVY TASK: Connects to DB
                SystemLogDAO logDao = new SystemLogDAO();
                return logDao.getLogsByFilter(filter);
            }

            @Override
            protected void done() {
                // 3. Update UI when finished
                try {
                    List<Object[]> logs = get();

                    // Wipe the board only when data is ready
                    if (tableModel != null) {
                        tableModel.setRowCount(0);

                        if (logs != null) {
                            for (Object[] row : logs) {
                                tableModel.addRow(row);
                            }
                        }
                    }

                    updateRecordCount();

                    // Update lastMaxId after loading data
                    updateLastMaxId();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // NEW METHOD: Update lastMaxId after loading data
    private void updateLastMaxId() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                String sql = "SELECT MAX(logId) as max_id FROM system_logs";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {

                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    return rs.next() ? rs.getInt("max_id") : 0;
                }
            }

            @Override
            protected void done() {
                try {
                    lastMaxId = get();
                    System.out.println("Updated lastMaxId to: " + lastMaxId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
    private void startAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(2000, e -> {
            // Only refresh if user isn't selecting a row
            if (logsTable != null && logsTable.getSelectedRow() == -1) {
                checkForUpdates();
            }
        });
        autoRefreshTimer.start();
    }

    private void checkForUpdates() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Simple query - just get the latest ID
                String sql = "SELECT MAX(logId) as max_id FROM system_logs";

                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {

                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        int currentMaxId = rs.getInt("max_id");
                        return currentMaxId > lastMaxId;
                    }
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    boolean hasUpdates = get();
                    if (hasUpdates) {
                        System.out.println("New log data detected! Refreshing...");
                        loadLogData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
    // =========================================================================
    // 1. PRINT FUNCTIONALITY
    // =========================================================================
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
                Paragraph title = new Paragraph("Logs List", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);

                // 3. Create Table
                int colCount = logsTable.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);

                // 4. Add Headers (✅ MATCHING YOUR TABLE COLORS)
                // We use the same color: new Color(52, 152, 219)
                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);

                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);

                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(logsTable.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor); // ✅ Blue Background
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8); // More padding like your table
                    pdfTable.addCell(cell);
                }

                // 5. Add Rows
                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

                for (int i = 0; i < logsTable.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = logsTable.getValueAt(i, j);
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
    // GUI CONSTRUCTION
    // =========================================================================
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // --- TOP TOOLBAR ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Left: Search + Filter
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setBackground(BG_COLOR);

        // A. Search
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true), new EmptyBorder(5, 5, 5, 5)));

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                updateRecordCount();
            }
        });

        // B. Date Filter
        JLabel filterLabel = new JLabel("Date:");
        filterLabel.setFont(new Font("Arial", Font.BOLD, 14));

        String[] filters = {"All Time", "Today", "This Week", "This Month"};
        dateFilterBox = new JComboBox<>(filters);
        dateFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        dateFilterBox.setBackground(Color.WHITE);
        dateFilterBox.setPreferredSize(new Dimension(120, 30));

        // Add Listener to refresh table when selection changes
        dateFilterBox.addActionListener(e -> loadLogData());

        leftPanel.add(searchLabel);
        leftPanel.add(searchField);
        leftPanel.add(filterLabel);
        leftPanel.add(dateFilterBox);

        // Right: Print Button
        JPanel rightPrint = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPrint.setBackground(BG_COLOR);
        JButton btnPrint = createRoundedButton("🖨 Print Report", new Color(44, 62, 80));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(160, 40));
        btnPrint.addActionListener(e -> handlePrint());
        rightPrint.add(btnPrint);


        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPrint, BorderLayout.EAST);

        contentPanel.add(topPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // --- TABLE ---
        String[] columnNames = {"Log ID", "Action Type", "Target", "Performed By", "Date & Time"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        logsTable = new JTable(tableModel);
        logsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        logsTable.setRowHeight(45);
        logsTable.setGridColor(new Color(200, 200, 200));
        logsTable.setSelectionBackground(new Color(220, 237, 250));
        logsTable.setSelectionForeground(Color.BLACK);
        logsTable.setShowVerticalLines(true);
        logsTable.setShowHorizontalLines(true);

        sorter = new TableRowSorter<>(tableModel);
        logsTable.setRowSorter(sorter);

        // Default Sort: Newest Logs First (Column 4)
        List<RowSorter.SortKey> sortKeys = new java.util.ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);

        JTableHeader header = logsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        // Ensure table cell text is black for readability
        logsTable.setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < logsTable.getColumnCount(); i++) {
            logsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(logsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

        contentPanel.add(tableScrollPane);

        // --- FOOTER COUNT ---
        contentPanel.add(Box.createVerticalStrut(10));
        lblRecordCount = new JLabel("Total Logs: 0");
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        lblRecordCount.setForeground(new Color(100, 100, 100));
        contentPanel.add(lblRecordCount);

        return contentPanel;
    }

    private void updateRecordCount() {
        if (lblRecordCount != null && logsTable != null) {
            int count = logsTable.getRowCount();
            lblRecordCount.setText("Total Logs: " + count);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(25, 40, 25, 40));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);

        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 26));
        lblSystem.setForeground(Color.WHITE);

        JLabel lblModule = new JLabel("System Audit Logs");
        lblModule.setFont(new Font("Arial", Font.BOLD, 22));
        lblModule.setForeground(Color.WHITE);

        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
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
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            JFrame frame = new JFrame("Audit Logs");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new AdminLogsTab());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}