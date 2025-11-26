package org.example.Admin;

import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class AdminLogsTab extends JPanel {

    private JTable logsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblRecordCount;


    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94);
    private final Color BTN_PRINT_COLOR = new Color(60, 60, 60); // Dark Gray

    public AdminLogsTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        loadLogData();
    }
    private JComboBox<String> dateFilterBox;

    // =========================================================================
    // DATA SIMULATION (The Audit Trail)
    // =========================================================================
    public void loadLogData() {
        String filter = (dateFilterBox != null) ? (String) dateFilterBox.getSelectedItem() : "All Time";

        tableModel.setRowCount(0);
        SystemLogDAO logDao = new SystemLogDAO();
        java.util.List<Object[]> logs = logDao.getLogsByFilter(filter);

        for (Object[] row : logs) {
            tableModel.addRow(row);
        }

        updateRecordCount();
        if (logsTable != null) {
            logsTable.repaint();
        }
    }

    // =========================================================================
    // 1. PRINT FUNCTIONALITY
    // =========================================================================
    private void handlePrint() {
        MessageFormat header = new MessageFormat("System Audit Logs Report");
        MessageFormat footer = new MessageFormat("Page {0,number,integer}");

        try {
            boolean complete = logsTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) {
                JOptionPane.showMessageDialog(this, "Report saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception pe) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + pe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); // Added gap
        leftPanel.setBackground(BG_COLOR);

        // A. Search
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField searchField = new JTextField(15);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 5, 5, 5)));

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
        leftPanel.add(filterLabel); // Add Label
        leftPanel.add(dateFilterBox); // Add Box

        // Right: Print Button (Same as before)
        JPanel rightPrint = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPrint.setBackground(BG_COLOR);
        JButton btnPrint = createRoundedButton("🖨 Print Report", new Color(60, 60, 60));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setPreferredSize(new Dimension(160, 40));
        btnPrint.addActionListener(e -> handlePrint());
        rightPrint.add(btnPrint);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPrint, BorderLayout.EAST);

        contentPanel.add(topPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // --- TABLE ---
        String[] columnNames = {"Log ID", "Action Type", "Target", "Performed By (Staff)", "Date & Time"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; } // Read-only logs
        };

        logsTable = new JTable(tableModel);
        logsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        logsTable.setRowHeight(45);
        logsTable.setGridColor(new Color(200, 200, 200));
        logsTable.setSelectionBackground(new Color(220, 220, 220)); // Grey selection for read-only feel
        logsTable.setSelectionForeground(Color.BLACK);
        logsTable.setShowVerticalLines(true);
        logsTable.setShowHorizontalLines(true);

        sorter = new TableRowSorter<>(tableModel);
        logsTable.setRowSorter(sorter);

        JTableHeader header = logsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < logsTable.getColumnCount(); i++) {
            logsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(logsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(TABLE_HEADER_BG, 2));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

        contentPanel.add(tableScrollPane);

        // --- FOOTER COUNT ---
        contentPanel.add(Box.createVerticalStrut(10));
        lblRecordCount = new JLabel("Total Logs: 0");
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        lblRecordCount.setForeground(Color.GRAY);
        contentPanel.add(lblRecordCount);

        return contentPanel;
    }

    private void updateRecordCount() {
        int count = logsTable.getRowCount();
        lblRecordCount.setText("Total Logs: " + count);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
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