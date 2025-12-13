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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ApprovedRequestTab extends JPanel {

    // Color Palette - Professional Barangay Theme
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);
    private static final Color SECONDARY_COLOR = new Color(13, 71, 161);
    private static final Color ACCENT_COLOR = new Color(76, 175, 80);
    private static final Color DANGER_COLOR = new Color(244, 67, 54);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    private static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color HEADER_BG = new Color(21, 101, 192);

    private JTable requestTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JLabel lblRecordCount;
    private JComboBox<String> statusFilterBox;
    private JComboBox<String> dateFilterBox;
    private JTextField searchField;


    public ApprovedRequestTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BACKGROUND_COLOR);
        add(scrollPane, BorderLayout.CENTER);

        loadRequestData();

    }
    private boolean isLoading = false;

    public void loadRequestData() {
        if (isLoading) return; // Prevent stacking requests
        isLoading = true;
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                ResidentDAO residentDAO = new ResidentDAO();
                List<DocumentRequest> rawList = residentDAO.getAllResidentsDocument();
                List<Object[]> processedRows = new ArrayList<>();

                for (DocumentRequest doc : rawList) {
                    // Filter: ONLY Approved
                    if (doc != null && "Approved".equalsIgnoreCase(doc.getStatus()) || "Released".equalsIgnoreCase(doc.getStatus())) {

                        Object dateObj = doc.getRequestDate();
                        LocalDateTime finalDate = null;
                        try {
                            if (dateObj instanceof java.sql.Timestamp) {
                                finalDate = ((java.sql.Timestamp) dateObj).toLocalDateTime();
                            } else if (dateObj != null) {
                                String s = dateObj.toString().replace("T", " ");
                                if (s.length() >= 19) finalDate = LocalDateTime.parse(s.substring(0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                else if (s.length() >= 16) finalDate = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                else if (s.length() >= 10) finalDate = LocalDate.parse(s.substring(0, 10)).atStartOfDay();
                            }
                        } catch (Exception ignored) {}

                        processedRows.add(new Object[]{
                                doc.getRequestId(),      // Col 0: ID (int)
                                doc.getFullName(),       // Col 1: Name (String)
                                doc.getName(),           // Col 2: Doc Type (String)
                                doc.getStatus() ,        // Col 3: Purpose (String)
                                finalDate,               // Col 4: Date (LocalDateTime Object)
                                doc.getStatus()          // Col 5: Status (String)
                        });
                    }
                }
                return processedRows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    if (tableModel != null) {
                        // Safely clear table
                        RowFilter<? super DefaultTableModel, ? super Integer> currentFilter = null;
                        if (sorter != null) {
                            currentFilter = sorter.getRowFilter();
                            sorter.setRowFilter(null);
                        }

                        tableModel.setRowCount(0);

                        for (Object[] row : rows) {
                            tableModel.addRow(row);
                        }

                        if (sorter != null) sorter.setRowFilter(currentFilter);
                        applyFilters();
                    }
                    updateRecordCount();
                    if (requestTable != null) requestTable.repaint();

                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    isLoading = false; // Release lock
                }
            }
        }.execute();
    }


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

        // 3. Date Filter (ROBUST VERSION)
        if (dateFilter != null && !dateFilter.equals("All Time")) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    try {
                        // Get the date string from column 4
                        Object dateObj = entry.getValue(4);
                        if (dateObj == null) return false;

                        String dateStr = dateObj.toString();

                        // Handle "T" separator (Java LocalDateTime) or Space (SQL DateTime)
                        if (dateStr.contains("T")) {
                            dateStr = dateStr.split("T")[0];
                        } else if (dateStr.contains(" ")) {
                            dateStr = dateStr.split(" ")[0];
                        }

                        // Parse Date
                        LocalDate rowDate;
                        try {
                            rowDate = LocalDate.parse(dateStr); // Expects yyyy-MM-dd
                        } catch (Exception e) {
                            // If format is wrong, try another or just return false to hide row
                            return false;
                        }

                        LocalDate today = LocalDate.now();

                        if (dateFilter.equals("Today")) {
                            return rowDate.isEqual(today);
                        } else if (dateFilter.equals("This Week")) {
                            // Logic: Is it after 7 days ago AND before/equal to today?
                            return !rowDate.isBefore(today.minusDays(7)) && !rowDate.isAfter(today);
                        } else if (dateFilter.equals("This Month")) {
                            return rowDate.getMonth() == today.getMonth() &&
                                    rowDate.getYear() == today.getYear();
                        }

                    } catch (Exception e) {
                        // Catch-all to prevent crash. Return true to show row, or false to hide.
                        // False is safer for filtering.
                        return false;
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
        MessageFormat header = new MessageFormat("Document Requests Report - " + statusFilterBox.getSelectedItem());
        MessageFormat footer = new MessageFormat("Page {0,number,integer}");

        try {
            boolean complete = requestTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Report Printed Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(this, "Printing Failed: " + pe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        // Title and Filters in one clean bar
        JPanel topBar = createTopBar();
        contentPanel.add(topBar);
        contentPanel.add(Box.createVerticalStrut(25));

        // Table Card
        JPanel tableCard = createTableCard();
        contentPanel.add(tableCard);

        return contentPanel;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(20, 0));
        topBar.setBackground(BACKGROUND_COLOR);
        topBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Left: Title
        JLabel titleLabel = new JLabel("Document Requests");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        // Center: Filters
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        filtersPanel.setBackground(BACKGROUND_COLOR);

        // Search Field
        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_COLOR),
                new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.setToolTipText("Search by name, ID, or document type");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters();
            }
        });

        // Status Filter
        statusFilterBox = new JComboBox<>(new String[]{"All Status", "Approved","Released"});
        statusFilterBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusFilterBox.setBackground(CARD_COLOR);
        statusFilterBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_COLOR),
                new EmptyBorder(8, 12, 8, 12)
        ));
        statusFilterBox.addActionListener(e -> applyFilters());

        // Date Filter
        dateFilterBox = new JComboBox<>(new String[]{ "Today","This Week", "This Month", "All Time"});
        dateFilterBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateFilterBox.setBackground(CARD_COLOR);
        dateFilterBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_COLOR),
                new EmptyBorder(8, 12, 8, 12)
        ));
        dateFilterBox.addActionListener(e -> applyFilters());

        filtersPanel.add(searchField);
        filtersPanel.add(statusFilterBox);
        filtersPanel.add(dateFilterBox);

        // Right: Print Button
        JButton btnPrint = createModernButton("Print Report", PRIMARY_COLOR, Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(130, 38));
        btnPrint.addActionListener(e -> handlePrintTable());

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(filtersPanel, BorderLayout.CENTER);
        topBar.add(btnPrint, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(12, BORDER_COLOR),
                new EmptyBorder(25, 25, 25, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

        String[] columnNames = {"Request ID", "Name", "Document Type", "Status", "Request Date"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        requestTable.setRowHeight(50);
        requestTable.setGridColor(BORDER_COLOR);
        requestTable.setSelectionBackground(new Color(227, 242, 253));
        requestTable.setSelectionForeground(TEXT_PRIMARY);
        requestTable.setShowVerticalLines(true);
        requestTable.setShowHorizontalLines(true);
        requestTable.setIntercellSpacing(new Dimension(8, 8));

        sorter = new TableRowSorter<>(tableModel);
        requestTable.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.addRowSorterListener(e -> updateRecordCount());

        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createEmptyBorder());

        // Custom Cell Renderer with Status Badges
        requestTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(JLabel.CENTER);

                if (column == 3 && value != null) {
                    String status = value.toString();
                    JLabel label = new JLabel(status);
                    label.setOpaque(true);
                    label.setHorizontalAlignment(JLabel.CENTER);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    label.setBorder(new EmptyBorder(5, 15, 5, 15));

                    if (status.equalsIgnoreCase("Approved")) {
                        label.setBackground(new Color(200, 230, 201));
                        label.setForeground(new Color(27, 94, 32));
                    } else if (status.equalsIgnoreCase("Pending")) {
                        label.setBackground(new Color(255, 224, 178));
                        label.setForeground(new Color(230, 81, 0));
                    } else if (status.equalsIgnoreCase("Rejected")) {
                        label.setBackground(new Color(255, 205, 210));
                        label.setForeground(new Color(183, 28, 28));
                    }

                    if (isSelected) {
                        label.setOpaque(false);
                    }

                    return label;
                }

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                }

                return c;
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        tableScrollPane.setBorder(new RoundedBorder(8, BORDER_COLOR));

        // Footer with record count
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(CARD_COLOR);
        lblRecordCount = new JLabel("Total Records: 0");
        lblRecordCount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblRecordCount.setForeground(TEXT_SECONDARY);
        footerPanel.add(lblRecordCount);

        card.add(tableScrollPane, BorderLayout.CENTER);
        card.add(footerPanel, BorderLayout.SOUTH);

        return card;
    }



    private JButton createModernButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));

        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);

        JLabel lblMain = new JLabel("Documentary Request Management");
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblMain.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Barangay Document Processing System");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(200, 220, 255));

        titlePanel.add(lblMain);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSub);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(HEADER_BG);

        BarangayStaff staff = new StaffDAO().findStaffByPosition("Brgy.Captain");

        JLabel lblUser = new JLabel("Hi Mr. " + staff.getFirstName());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);

        JPanel userIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 45, 45);
                g2.setColor(PRIMARY_COLOR);
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

    static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color borderColor;

        RoundedBorder(int radius, Color borderColor) {
            this.radius = radius;
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("Barangay Document System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 900);
            frame.add(new ApprovedRequestTab());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}