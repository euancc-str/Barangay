package org.example.Captain;

import org.example.Admin.SystemLogDAO;
import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.*;

public class ApprovedRequestTab extends JPanel {

    private JTable requestTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // --- FILTER COMPONENTS ---
    private JLabel lblRecordCount;
    private JComboBox<String> statusFilterBox;
    private JComboBox<String> dateFilterBox;
    private JTextField searchField;

    // Timer
    private javax.swing.Timer autoRefreshTimer;

    public ApprovedRequestTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(229, 231, 235));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(229, 231, 235));
        add(scrollPane, BorderLayout.CENTER);

        // Initial Load
        loadData();

        // Start Timer
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new javax.swing.Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Only refresh if user is NOT interacting with the table
                if (requestTable != null && requestTable.getSelectedRow() == -1) {
                    loadData();
                }
            }
        });
        autoRefreshTimer.start();
    }

    // =========================================================================
    //  DATA LOADING
    // =========================================================================
    public void loadData() {
        // 1. Clear existing rows
        if (tableModel != null) {
            tableModel.setRowCount(0);
        }

        // 2. Fetch fresh data
        ResidentDAO rd = new ResidentDAO();
        List<DocumentRequest> residents = rd.getAllResidentsDocument();

        if (residents != null) {
            for (DocumentRequest res : residents) {
                String requestId = "" + res.getRequestId();
                String fullName = res.getFullName();
                String docTypeName = res.getName();
                String status = res.getStatus();
                Object date = res.getRequestDate(); // Keep as Object

                if (tableModel != null) {
                    // Columns: RequestID, Name, DocType, Status, Date
                    tableModel.addRow(new Object[]{requestId, fullName, docTypeName, status, date});
                }
            }
        }

        // 3. Refresh UI
        if (requestTable != null) {
            requestTable.repaint();
        }

        // 4. Re-apply filters/update count
        if (statusFilterBox != null) {
            applyFilters();
        } else {
            updateRecordCount();
        }
    }

    // =========================================================================
    // FILTER LOGIC (FIXED: TODAY, THIS WEEK, THIS MONTH)
    // =========================================================================
    private void applyFilters() {
        String text = searchField.getText();
        String status = (String) statusFilterBox.getSelectedItem();
        String dateFilter = (String) dateFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Text Search
        if (text != null && text.trim().length() > 0) {
            filters.add(RowFilter.regexFilter("(?i)" + text));
        }

        // 2. Status Filter
        if (status != null && !status.equals("All Status")) {
            filters.add(RowFilter.regexFilter("(?i)^" + status + "$", 3));
        }

        // 3. Date Filter (FIXED for 'T' format)
        if (dateFilter != null && !dateFilter.equals("All Time")) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    // Get string from Date column (index 4)
                    String dateStr = entry.getStringValue(4);
                    try {
                        if (dateStr.contains("T")) {
                            dateStr = dateStr.split("T")[0];
                        }
                        else if (dateStr.contains(" ")) {
                            dateStr = dateStr.split(" ")[0];
                        }

                        LocalDate rowDate = LocalDate.parse(dateStr);
                        LocalDate today = LocalDate.now();

                        if (dateFilter.equals("Today")) {
                            return rowDate.isEqual(today);
                        }
                        else if (dateFilter.equals("This Week")) {
                            return !rowDate.isBefore(today.minusDays(7)) && !rowDate.isAfter(today);
                        }
                        else if (dateFilter.equals("This Month")) {
                            return rowDate.getMonth() == today.getMonth() &&
                                    rowDate.getYear() == today.getYear();
                        }
                    } catch (Exception e) {
                        return true; // Show row if parse fails
                    }
                    return true;
                }
            });
        }

        if (sorter != null) {
            if (filters.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.andFilter(filters));
            }
        }

        updateRecordCount();
    }
    private void updateRecordCount() {
        if (lblRecordCount != null && requestTable != null) {
            int count = requestTable.getRowCount();
            lblRecordCount.setText("Total Records: " + count);
        }
    }
    private void handlePrintTable() {
        // 1. Create Header and Footer
        MessageFormat header = new MessageFormat("Document Requests Report - " + statusFilterBox.getSelectedItem());
        MessageFormat footer = new MessageFormat("Page {0,number,integer}");

        try {
            // 2. Print the Table
            // FIT_WIDTH mode ensures all columns fit on one page
            boolean complete = requestTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);

            if (complete) {
                JOptionPane.showMessageDialog(this, "Report Printed Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(this, "Printing Failed: " + pe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // =========================================================================
    // GUI SETUP
    // =========================================================================
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(229, 231, 235));
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setBackground(new Color(229, 231, 235));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));



        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(40));

        // --- SEARCH & FILTER PANEL ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(229, 231, 235));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JButton btnPrint = createRoundedButton("Print List", new Color(100, 100, 100)); // Grey
        btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(e -> handlePrintTable());
        buttonPanel.add(btnPrint);
        searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)
        ));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });

        JLabel lblStatus = new JLabel("  Status: ");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        String[] statusOpts = {"All Status", "Pending", "Approved", "Rejected"};
        statusFilterBox = new JComboBox<>(statusOpts);
        statusFilterBox.addActionListener(e -> applyFilters());

        // --- UPDATED DATE OPTIONS ---
        JLabel lblDate = new JLabel("  Date: ");
        lblDate.setFont(new Font("Arial", Font.BOLD, 14));
        String[] dateOpts = { "Today", "This Week", "This Month","All Time"}; // Changed "Last Month" to "This Month"
        dateFilterBox = new JComboBox<>(dateOpts);
        dateFilterBox.addActionListener(e -> applyFilters());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(lblStatus);
        searchPanel.add(statusFilterBox);
        searchPanel.add(lblDate);
        searchPanel.add(dateFilterBox);

        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- TABLE SETUP ---
        String[] columnNames = {"Request ID", "Name", "Types of Documents", "Status", "Date"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 14));
        requestTable.setRowHeight(60);
        requestTable.setGridColor(new Color(200, 200, 200));
        requestTable.setSelectionBackground(new Color(200, 240, 240));
        requestTable.setShowVerticalLines(true);
        requestTable.setShowHorizontalLines(true);

        // Mouse Listener
        requestTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {

            }
        });

        sorter = new TableRowSorter<>(tableModel);
        requestTable.setRowSorter(sorter);

        // Sort by Date (Col 4) Descending
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);

        sorter.addRowSorterListener(e -> updateRecordCount());

        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(new Color(34, 197, 94));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        header.setBorder(BorderFactory.createEmptyBorder());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(34, 197, 94), 2));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        contentPanel.add(tableScrollPane);

        // Footer
        contentPanel.add(Box.createVerticalStrut(10));
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(new Color(229, 231, 235));
        lblRecordCount = new JLabel("Total Records: 0");
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        footerPanel.add(lblRecordCount);
        contentPanel.add(footerPanel);

        return contentPanel;
    }

    // =========================================================================
    //  ACTION / VIEW LOGIC (Unchanged)
    // =========================================================================
    private void handleViewRequest() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = requestTable.convertRowIndexToModel(selectedRow);
        String requestIdStr = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String documentType = (String) tableModel.getValueAt(modelRow, 2);
        String status = (String) tableModel.getValueAt(modelRow, 3);

        int requestId = Integer.parseInt(requestIdStr);

        ResidentDAO r = new ResidentDAO();
        DocumentRequest docRequest = r.findPurposeByFullName(name, requestId);

        if (docRequest != null) {
            UserDataManager.getInstance().setResidentId(docRequest.getResidentId());
            UserDataManager.getInstance().setFullName(name);
            UserDataManager.getInstance().setPurpose(docRequest.getPurpose());
            UserDataManager.getInstance().setAddress(docRequest.getAddress());
            UserDataManager.getInstance().setAge(docRequest.getAge());
            UserDataManager.getInstance().setReqDate(docRequest.getRequestDate());
            showRequestDetailsDialog(name, documentType, modelRow, status);
        } else {
            JOptionPane.showMessageDialog(this, "Error finding details.");
        }
    }

    // ... [Dialog and Helper methods below] ...

    private JTextArea purposeArea;
    private JLabel addressLabel;

    private void showRequestDetailsDialog(String name, String documentType, int rowIndex, String currentStatus) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Approved Request", true);
        dialog.setSize(700, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Approved " + name + " Request?", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel docTypeLabel = new JLabel(documentType, SwingConstants.CENTER);
        docTypeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 5));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(docTypeLabel, BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        addDetailRow(detailsPanel, "Name:", name.split(" ")[0]);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        genderPanel.setBackground(Color.WHITE);
        JButton genderBtn = new JButton("Male");
        genderBtn.setBackground(Color.WHITE); genderBtn.setEnabled(false);
        genderBtn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 2), new EmptyBorder(8, 30, 8, 30)));
        genderPanel.add(genderBtn);
        addDetailRowCustom(detailsPanel, "", genderPanel);

        addDetailRow(detailsPanel, "Last Name:", name.split(" ")[1]);
        addDetailRow(detailsPanel, "Age:", String.valueOf(UserDataManager.getInstance().getAge()));

        JPanel birthPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        birthPanel.setBackground(Color.WHITE);
        JButton birthBtn = new JButton("");
        birthBtn.setBackground(Color.WHITE); birthBtn.setEnabled(false);
        birthBtn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 2), new EmptyBorder(8, 20, 8, 20)));
        birthPanel.add(birthBtn);
        addDetailRowCustom(detailsPanel, "", birthPanel);
        addDetailRow(detailsPanel, "Date:", String.valueOf(UserDataManager.getInstance().getReqDate()));

        detailsPanel.add(Box.createVerticalStrut(10));
        addressLabel = new JLabel("Current Address:");
        addressLabel.setFont(new Font("Arial", Font.BOLD, 13));
        addressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(addressLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        JLabel addressValue = new JLabel(UserDataManager.getInstance().getAddress());
        addressValue.setFont(new Font("Arial", Font.PLAIN, 12));
        addressValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(addressValue);
        detailsPanel.add(Box.createVerticalStrut(15));

        JLabel purposeLabel = new JLabel("Purpose:");
        purposeLabel.setFont(new Font("Arial", Font.BOLD, 13));
        purposeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(purposeLabel);
        detailsPanel.add(Box.createVerticalStrut(5));

        purposeArea = new JTextArea(UserDataManager.getInstance().getPurpose());
        purposeArea.setFont(new Font("Arial", Font.PLAIN, 12));
        purposeArea.setLineWrap(true);
        purposeArea.setWrapStyleWord(true);
        purposeArea.setEditable(false);
        purposeArea.setBackground(Color.WHITE);
        purposeArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsPanel.add(purposeArea);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton rejectBtn = createDialogButton("Reject", new Color(255, 77, 77));
        rejectBtn.addActionListener(e -> {
            dialog.dispose();
            showRejectDialog(name, documentType, rowIndex);
        });

        JButton approveBtn = createDialogButton("Approve", new Color(76, 175, 80));
        approveBtn.addActionListener(e -> {
            dialog.dispose();
            showCertificateDialog(name, documentType, rowIndex);
        });

        buttonPanel.add(rejectBtn);
        buttonPanel.add(approveBtn);

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void showCertificateDialog(String name, String documentType, int rowIndex) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Barangay Clearance", true);
        dialog.setSize(750, 850);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(240, 242, 255));

        JPanel certPanel = new JPanel();
        certPanel.setBackground(new Color(240, 242, 255));
        certPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        JScrollPane certScrollPane = new JScrollPane(certPanel);
        certScrollPane.setBorder(new EmptyBorder(30, 40, 20, 40));
        certScrollPane.setBackground(new Color(240, 242, 255));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(240, 242, 255));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 20, 0));

        JButton printBtn = createDialogButton("Print", new Color(76, 175, 80));
        printBtn.addActionListener(e -> {
            tableModel.setValueAt("Approved", rowIndex, 3);

            JOptionPane.showMessageDialog(dialog, "Document printed successfully!");
            BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
            UserDataManager.getInstance().staffOperations(staff, "Approved", UserDataManager.getInstance().getResidentId());
            SystemLogDAO systemLogDAO = new SystemLogDAO();
            systemLogDAO.addLog("Approved Request", UserDataManager.getInstance().getFullName(), Integer.parseInt(staff.getStaffId()));

            loadData();
            dialog.dispose();
        });

        buttonPanel.add(printBtn);
        mainPanel.add(certScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JTextArea reasonText;
    private void showRejectDialog(String name, String documentType, int rowIndex) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reject Request", true);
        dialog.setSize(650, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Reject " + name + " Request?", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel docTypeLabel = new JLabel(documentType, SwingConstants.CENTER);
        docTypeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 5));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(docTypeLabel, BorderLayout.CENTER);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        addDetailRow(detailsPanel, "Name:", name.split(" ")[0]);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        genderPanel.setBackground(Color.WHITE);
        JButton genderBtn = new JButton("Male");
        genderBtn.setBackground(Color.WHITE);
        genderBtn.setEnabled(false);
        genderBtn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 2), new EmptyBorder(8, 30, 8, 30)));
        genderPanel.add(genderBtn);
        addDetailRowCustom(detailsPanel, "", genderPanel);

        addDetailRow(detailsPanel, "Last Name:", name.split(" ")[1]);
        addDetailRow(detailsPanel, "Middle Name:", "");

        detailsPanel.add(Box.createVerticalStrut(20));

        JPanel reasonPanel = new JPanel(new BorderLayout(0, 15));
        reasonPanel.setBackground(Color.WHITE);
        reasonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK, 3), new EmptyBorder(20, 20, 20, 20)));

        JLabel reasonTitle = new JLabel("Please provide a reason", SwingConstants.CENTER);
        reasonTitle.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel reasonContent = new JPanel(new BorderLayout(0, 10));
        reasonContent.setBackground(Color.WHITE);
        JLabel reasonLabel = new JLabel("Reason:");
        reasonLabel.setFont(new Font("Arial", Font.BOLD, 13));

        reasonText = new JTextArea("");
        reasonText.setFont(new Font("Arial", Font.PLAIN, 13));
        reasonText.setLineWrap(true);
        reasonText.setWrapStyleWord(true);
        reasonText.setRows(4);
        reasonText.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), new EmptyBorder(8, 8, 8, 8)));

        reasonContent.add(reasonLabel, BorderLayout.NORTH);
        reasonContent.add(new JScrollPane(reasonText), BorderLayout.CENTER);
        reasonPanel.add(reasonTitle, BorderLayout.NORTH);
        reasonPanel.add(reasonContent, BorderLayout.CENTER);
        detailsPanel.add(reasonPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton sendBtn = createDialogButton("Send to Requester", new Color(76, 175, 80));
        sendBtn.addActionListener(e -> {
            String reason = reasonText.getText().trim();
            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please provide a reason.", "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String reasonOfStaff = reasonText.getText().toString();
            BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
            int staffId = Integer.parseInt(staff.getStaffId());
            StaffDAO staffDao = new StaffDAO();
            staffDao.addReasonForRejection(UserDataManager.getInstance().getResidentId(), reasonOfStaff, staffId);
            SystemLogDAO systemLogDAO = new SystemLogDAO();
            systemLogDAO.addLog("Rejected Request", UserDataManager.getInstance().getFullName(), staffId);

            tableModel.setValueAt("Rejected", rowIndex, 3);
            JOptionPane.showMessageDialog(dialog, "Rejection sent!", "Success", JOptionPane.INFORMATION_MESSAGE);

            loadData();
            dialog.dispose();
        });

        buttonPanel.add(sendBtn);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(panel.getBackground());
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel lblLabel = new JLabel(label); lblLabel.setFont(new Font("Arial", Font.BOLD, 13)); lblLabel.setPreferredSize(new Dimension(150, 25));
        JLabel lblValue = new JLabel(value); lblValue.setFont(new Font("Arial", Font.PLAIN, 13));
        rowPanel.add(lblLabel, BorderLayout.WEST); rowPanel.add(lblValue, BorderLayout.CENTER);
        panel.add(rowPanel); panel.add(Box.createVerticalStrut(5));
    }
    private void addDetailRowCustom(JPanel panel, String label, JPanel customComponent) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(panel.getBackground());
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        if (!label.isEmpty()) { JLabel lblLabel = new JLabel(label); lblLabel.setFont(new Font("Arial", Font.BOLD, 13)); lblLabel.setPreferredSize(new Dimension(150, 35)); rowPanel.add(lblLabel, BorderLayout.WEST); }
        rowPanel.add(customComponent, BorderLayout.CENTER);
        panel.add(rowPanel); panel.add(Box.createVerticalStrut(5));
    }

    private JButton createDialogButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(getForeground()); g2.drawString(getText(), (getWidth()-getFontMetrics(getFont()).stringWidth(getText()))/2, (getHeight()/2)+5); g2.dispose();
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 14)); button.setBackground(bgColor); button.setForeground(Color.WHITE);
        button.setFocusPainted(false); button.setBorder(new EmptyBorder(12, 40, 12, 40)); button.setCursor(new Cursor(Cursor.HAND_CURSOR)); button.setContentAreaFilled(false);
        return button;
    }

    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(getForeground()); g2.drawString(getText(), (getWidth()-getFontMetrics(getFont()).stringWidth(getText()))/2, (getHeight()/2)+5); g2.dispose();
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 16)); button.setBackground(bgColor); button.setForeground(Color.BLACK);
        button.setFocusPainted(false); button.setBorder(new EmptyBorder(15, 50, 15, 50)); button.setCursor(new Cursor(Cursor.HAND_CURSOR)); button.setContentAreaFilled(false); button.setPreferredSize(new Dimension(250, 50));
        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new CaptainDashboard.RoundedBorder(30, true, false),
                new EmptyBorder(25, 40, 25, 40)
        ));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(40, 40, 40));

        JLabel lblDocumentary = new JLabel("Documentary");
        lblDocumentary.setFont(new Font("Arial", Font.BOLD, 26));
        lblDocumentary.setForeground(Color.WHITE);

        JLabel lblRequest = new JLabel("Request");
        lblRequest.setFont(new Font("Arial", Font.BOLD, 22));
        lblRequest.setForeground(Color.WHITE);

        titlePanel.add(lblDocumentary);
        titlePanel.add(lblRequest);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(new Color(40, 40, 40));

        JLabel lblUser = new JLabel("Hi Mr. Dalisay");
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblUser.setForeground(Color.WHITE);

        JPanel userIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 45, 45);
                g2.setColor(new Color(40, 40, 40));
                g2.fillOval(12, 8, 20, 20);
                g2.fillArc(5, 25, 35, 30, 0, 180);
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(45, 45);
            }
        };
        userIcon.setOpaque(false);

        userPanel.add(lblUser);
        userPanel.add(userIcon);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);

        return headerPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            CaptainDashboard dashboard = new CaptainDashboard();
            dashboard.setVisible(true);
        });
    }
}