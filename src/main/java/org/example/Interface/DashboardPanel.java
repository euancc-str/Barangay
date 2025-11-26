package org.example.Interface;

import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

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
    private JComboBox<String> dateFilterBox;
    private JLabel lblRecordCount;
    private JButton btnPrint;

    private JLabel hiLabel;
    private JLabel dashboardProfilePicture;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // ===== HEADER =====
        add(createHeader(), BorderLayout.NORTH);

        // ===== CENTER CONTENT (Buttons + Filters + Table) =====
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setBackground(new Color(240, 240, 240));
        centerContent.setBorder(new EmptyBorder(20, 50, 20, 50)); // Padding

        // 1. Status Buttons
        centerContent.add(createStatusButtonPanel());
        centerContent.add(Box.createVerticalStrut(20));

        // 2. Filter & Action Bar (NEW)
        centerContent.add(createFilterActionPanel());
        centerContent.add(Box.createVerticalStrut(10));

        // 3. Table
        centerContent.add(createTablePanel());

        // 4. Record Count Footer (NEW)
        centerContent.add(Box.createVerticalStrut(10));
        lblRecordCount = new JLabel("Total Records: 0");
        lblRecordCount.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Wrap label in panel for alignment
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.add(lblRecordCount);
        centerContent.add(footerPanel);

        add(centerContent, BorderLayout.CENTER);

        // Initial Load
        loadDataFromFile();
        switchSection("Pending"); // Default view
    }

    // =========================================================================
    //  GUI CREATION METHODS
    // =========================================================================

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 20, 20));
        header.setPreferredSize(new Dimension(0, 120));
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel title = new JLabel("<html><b>Documentary<br>Request</b></html>");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        header.add(title, BorderLayout.WEST);

        // Stack greeting and profile vertically
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        hiLabel = new JLabel(getGreetingFromProperties());
        hiLabel.setForeground(Color.WHITE);
        hiLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        hiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dashboardProfilePicture = new JLabel();
        dashboardProfilePicture.setPreferredSize(new Dimension(50, 50));
        dashboardProfilePicture.setOpaque(true);
        dashboardProfilePicture.setBackground(Color.WHITE);
        dashboardProfilePicture.setBorder(new LineBorder(Color.GRAY, 1, true));
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
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        pendingBtn = createSectionButton("Pending Documents", new Color(255, 140, 0));
        verifiedBtn = createSectionButton("Verified Documents", new Color(46, 139, 87));
        rejectBtn = createSectionButton("Rejected Documents", new Color(220, 20, 60));

        // Actions
        pendingBtn.addActionListener(e -> switchSection("Pending"));
        verifiedBtn.addActionListener(e -> switchSection("Approved"));
        rejectBtn.addActionListener(e -> switchSection("Rejected"));

        buttonPanel.add(pendingBtn);
        buttonPanel.add(verifiedBtn);
        buttonPanel.add(rejectBtn);

        return buttonPanel;
    }

    private JPanel createFilterActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // LEFT: Filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(new Color(240, 240, 240));

        // Doc Type Filter
        JLabel lblDoc = new JLabel("Document:");
        lblDoc.setFont(new Font("SansSerif", Font.BOLD, 14));
        String[] docTypes = {"All Documents", "Barangay Clearance", "Business Clearance", "Certificate of Indigency", "Certificate of Residency"};
        docTypeFilterBox = new JComboBox<>(docTypes);
        docTypeFilterBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        docTypeFilterBox.addActionListener(e -> applyFilters());

        // Date Filter
        JLabel lblDate = new JLabel("Date:");
        lblDate.setFont(new Font("SansSerif", Font.BOLD, 14));
        String[] dateOptions = {"All Time", "Today", "This Week", "Last Month", "This Year"};
        dateFilterBox = new JComboBox<>(dateOptions);
        dateFilterBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dateFilterBox.addActionListener(e -> applyFilters());

        filterPanel.add(lblDoc);
        filterPanel.add(docTypeFilterBox);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(lblDate);
        filterPanel.add(dateFilterBox);

        // RIGHT: Print Button
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionPanel.setBackground(new Color(240, 240, 240));

        btnPrint = new JButton("🖨 Print Report");
        btnPrint.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnPrint.setBackground(new Color(60, 60, 60));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setPreferredSize(new Dimension(150, 35));
        btnPrint.addActionListener(e -> handlePrint());

        actionPanel.add(btnPrint);

        panel.add(filterPanel, BorderLayout.WEST);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());

        // Added "Date" column
        String[] columns = {"Request ID", "Resident Name", "Document Type", "Status", "Date"};

        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        documentTable = new JTable(model);
        documentTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        documentTable.setRowHeight(45);
        documentTable.setShowGrid(false);
        documentTable.setIntercellSpacing(new Dimension(0, 0));
        JTableHeaderStyle(documentTable);

        // Double Click Action
        documentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = documentTable.getSelectedRow();
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
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    // =========================================================================
    //  LOGIC METHODS
    // =========================================================================

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

    // Switch tabs (Pending, Approved, Rejected)
    private void switchSection(String status) {
        currentFilter = status;

        // Highlight buttons
        highlightButton(status.equals("Pending") ? pendingBtn : (status.equals("Approved") ? verifiedBtn : rejectBtn));

        model.setRowCount(0);

        for (Object[] row : allData) {
            // row[3] is Status, row[4] is Date
            String rowStatus = (String) row[3];

            boolean matchesStatus = false;
            if (rowStatus != null) {
                if (status.equals("Approved")) {
                    matchesStatus = rowStatus.equalsIgnoreCase("Paid") || rowStatus.equalsIgnoreCase("Released") || rowStatus.equalsIgnoreCase("Approved");
                } else {
                    matchesStatus = rowStatus.equalsIgnoreCase(status);
                }
            }

            if (matchesStatus) {
                // >>> 5-MINUTE RULE LOGIC <<<
                if (status.equals("Pending")) {
                    if (isRecent(row[4])) {
                        model.addRow(row);
                    }
                } else {
                    // Approved/Rejected don't have time limits
                    model.addRow(row);
                }
            }
        }

        applyFilters();
    }
    private boolean isRecent(Object dateObj) {
        if (dateObj == null) return false;
        try {
            String dateStr = dateObj.toString();
            // Normalize format: remove millis if present, ensure space between date/time
            if (dateStr.contains(".")) dateStr = dateStr.split("\\.")[0];
            dateStr = dateStr.replace("T", " ");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime reqTime = LocalDateTime.parse(dateStr, formatter);
            LocalDateTime now = LocalDateTime.now();

            // Calculate difference in minutes
            long diff = ChronoUnit.MINUTES.between(reqTime, now);

            // Allow if difference is between 0 and 5 minutes
            return diff >= 0 && diff <= 5;

        } catch (Exception e) {
            e.printStackTrace();
            return false; // If parsing fails, assume it's old/invalid
        }
    }

    // Apply Dropdown Filters
    private void applyFilters() {
        if (sorter == null) return;

        String selectedDoc = (String) docTypeFilterBox.getSelectedItem();
        String dateFilter = (String) dateFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Document Type Filter (Column 2)
        if (selectedDoc != null && !selectedDoc.equals("All Documents")) {
            filters.add(RowFilter.regexFilter("(?i)^" + selectedDoc + "$", 2));
        }

        // 2. Date Filter (Column 4)
        if (dateFilter != null && !dateFilter.equals("All Time")) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        String dateStr = entry.getStringValue(4);
                        if(dateStr.contains("T")) dateStr = dateStr.split("T")[0];
                        else if(dateStr.contains(" ")) dateStr = dateStr.split(" ")[0];

                        LocalDate rowDate = LocalDate.parse(dateStr);
                        LocalDate today = LocalDate.now();

                        if (dateFilter.equals("Today")) {
                            return rowDate.equals(today);
                        } else if (dateFilter.equals("This Week")) {
                            return !rowDate.isBefore(today.minusDays(7)) && !rowDate.isAfter(today);
                        } else if (dateFilter.equals("Last Month")) {
                            return !rowDate.isBefore(today.minusDays(30));
                        } else if (dateFilter.equals("This Year")) {
                            return rowDate.getYear() == today.getYear();
                        }
                    } catch (Exception e) { return true; } // Show if parse fails (safe)
                    return true;
                }
            });
        }

        if (filters.isEmpty()) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.andFilter(filters));

        updateRecordCount();
    }

    private void updateRecordCount() {
        int count = documentTable.getRowCount();
        lblRecordCount.setText("Total Records: " + count);
    }

    private void handlePrint() {
        MessageFormat header = new MessageFormat("Document Requests Report - " + currentFilter);
        MessageFormat footer = new MessageFormat("Page {0,number,integer}");
        try {
            boolean complete = documentTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) JOptionPane.showMessageDialog(this, "Printing Complete");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Printing Failed: " + ex.getMessage());
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
    private String getGreetingFromProperties() { return "Hi User"; }
    private void setProfilePicture(JLabel l) { l.setText("👤"); }

    private void JTableHeaderStyle(JTable t) {
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        t.getTableHeader().setBackground(new Color(245, 245, 245));
        t.getTableHeader().setForeground(Color.BLACK);
        t.getTableHeader().setReorderingAllowed(false);
        ((DefaultTableCellRenderer) t.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }

    private JButton createSectionButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 48));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new RoundedBorder(20, color));
        Color inactiveColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 130);
        btn.setBackground(inactiveColor);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(color); }
            public void mouseExited(MouseEvent e) {
                if (btn != pendingBtn && btn != verifiedBtn && btn != rejectBtn) btn.setBackground(inactiveColor);
                else if (!currentFilter.equals(text.split(" ")[0])) btn.setBackground(inactiveColor); // Simple check
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
}