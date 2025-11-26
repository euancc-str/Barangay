package org.example.treasurer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.print.*;
import java.text.MessageFormat;
import java.util.List;

public class TreasurerReportsTab extends JPanel {

    private FinancialDAO dao;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    // Labels for the cards
    private JLabel lblToday, lblWeek, lblMonth, lblYear;
    private JComboBox<String> filterBox;

    public TreasurerReportsTab() {
        this.dao = new FinancialDAO();
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(240, 240, 240));

        // 1. Statistics Cards (Top)
        add(createStatsPanel(), BorderLayout.NORTH);

        // 2. Table Area (Center)
        add(createTablePanel(), BorderLayout.CENTER);

        // Initial Load
        refreshData();
    }

    private void refreshData() {
        // Update Cards
        lblToday.setText("₱ " + String.format("%,.2f", dao.getTotalIncome("Today")));
        lblWeek.setText("₱ " + String.format("%,.2f", dao.getTotalIncome("Week")));
        lblMonth.setText("₱ " + String.format("%,.2f", dao.getTotalIncome("Month")));
        lblYear.setText("₱ " + String.format("%,.2f", dao.getTotalIncome("Year")));

        // Update Table
        String filter = (String) filterBox.getSelectedItem();
        loadTableData(filter);
    }

    private void loadTableData(String filter) {
        tableModel.setRowCount(0);
        List<Object[]> rows = dao.getTransactionHistory(filter);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
    }

    // --- UI: STATS CARDS ---
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(0, 150));

        // Create 4 Cards
        lblToday = new JLabel("₱ 0.00");
        lblWeek = new JLabel("₱ 0.00");
        lblMonth = new JLabel("₱ 0.00");
        lblYear = new JLabel("₱ 0.00");

        panel.add(createCard("Collected Today", lblToday, new Color(46, 204, 113))); // Green
        panel.add(createCard("This Week", lblWeek, new Color(52, 152, 219)));       // Blue
        panel.add(createCard("This Month", lblMonth, new Color(155, 89, 182)));     // Purple
        panel.add(createCard("This Year", lblYear, new Color(241, 196, 15)));       // Orange

        return panel;
    }

    private JPanel createCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, color), // Colored left strip
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.DARK_GRAY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        // Shadow effect (optional, simple border for now)
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        return card;
    }

    // --- UI: TABLE & CONTROLS ---
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(new Color(240, 240, 240));

        JLabel lblFilter = new JLabel("Filter History: ");
        lblFilter.setFont(new Font("Arial", Font.BOLD, 14));

        String[] filters = {"All Time", "Today", "This Week", "This Month"};
        filterBox = new JComboBox<>(filters);
        filterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        filterBox.addActionListener(e -> loadTableData((String) filterBox.getSelectedItem()));

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        JButton btnPrint = new JButton("🖨 Print Report");
        btnPrint.setBackground(new Color(60, 60, 60));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(e -> handlePrint());

        toolbar.add(lblFilter);
        toolbar.add(filterBox);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnRefresh);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnPrint);

        // Table
        String[] cols = {"Request ID", "Payer Name", "Document", "Amount", "Date Paid"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(40);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 14));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void handlePrint() {
        MessageFormat header = new MessageFormat("Financial Report - " + filterBox.getSelectedItem());
        MessageFormat footer = new MessageFormat("Generated by: Treasurer | Page {0}");
        try {
            boolean complete = historyTable.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (complete) JOptionPane.showMessageDialog(this, "Done!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}