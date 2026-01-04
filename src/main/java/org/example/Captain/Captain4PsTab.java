package org.example.Captain;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;


import org.example.Admin.AdminSettings.AdminAssetTab;
import org.example.HouseholdDAO;
import org.example.Users.Household;
import org.example.utils.AutoRefresher;


public class Captain4PsTab extends JPanel {


    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;


    // Filters
    private JComboBox<String> cmbPurok;
    private JTextField txtSearch;


    // Data Cache
    private List<Household> all4Ps = new ArrayList<>();
    private HouseholdDAO householdDAO;


    // --- COLORS (4Ps Theme - Blue theme) ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(41, 128, 185);  // Blue theme for 4Ps
    private final Color CARD_BG = Color.WHITE;
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color DARK_TEXT = new Color(60, 60, 60);
    private final Color LIGHT_TEXT = new Color(100, 100, 100);

    // Enhanced Table Colors - Vibrant but eye-friendly
    private final Color TABLE_HEADER_BG = new Color(41, 128, 185); // Blue header
    private final Color TABLE_HEADER_TEXT = Color.WHITE;
    private final Color TABLE_EVEN_ROW = new Color(255, 255, 255);
    private final Color TABLE_ODD_ROW = new Color(240, 248, 255); // Very light blue tint
    private final Color TABLE_GRID_COLOR = new Color(220, 235, 250); // Soft blue grid
    private final Color TABLE_SELECTION_BG = new Color(200, 230, 255); // Light blue selection

    // Column-specific vibrant colors - All blue variations
    private final Color COLUMN_HOUSEHOLD_BG = new Color(225, 240, 255); // Light blue
    private final Color COLUMN_NAME_BG = new Color(235, 245, 255); // Very light blue
    private final Color COLUMN_PUROK_BG = new Color(210, 230, 255); // Medium light blue
    private final Color COLUMN_MEMBERS_BG = new Color(220, 235, 250); // Soft blue
    private final Color COLUMN_INCOME_BG = new Color(230, 240, 255); // Pale blue
    private final Color COLUMN_STATUS_BG = new Color(240, 245, 255); // Whitish blue

    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font COUNT_FONT = new Font("Segoe UI", Font.BOLD, 15);


    public Captain4PsTab() {
        this.householdDAO = new HouseholdDAO();


        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(0, 0, 0, 0));


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);


        loadData();
        // ✅ CORRECTED LISTENER (Does not freeze the screen)
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                // Run this in a background thread to prevent UI freezing
                new Thread(() -> {
                    if (refresher != null) {
                        refresher.stop();
                    }
                    loadData();
                    refresher = new AutoRefresher("Household", () -> {
                        // Ensure loadData updates the UI safely
                        SwingUtilities.invokeLater(() -> loadData());
                    });

                    System.out.println("Tab active. Auto-refresh started in background.");
                }).start();
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
                if (refresher != null) {
                    refresher.stop();
                    refresher = null;
                }
                System.out.println("Tab hidden. Auto-refresh stopped.");
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
            }
        });
    }
    private AutoRefresher refresher;


    // =========================================================================
    // DATA LOADING & FILTERING - IMPROVED SEARCH
    // =========================================================================


    private void loadData() {
        new SwingWorker<List<Household>, Void>() {
            @Override
            protected List<Household> doInBackground() {
                return householdDAO.get4PsHouseholds();
            }


            @Override
            protected void done() {
                try {
                    all4Ps = get();
                    applyFilters();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(Captain4PsTab.this,
                            "Error loading data: " + e.getMessage(),
                            "Data Load Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }


    private void applyFilters() {
        String selectedPurok = (String) cmbPurok.getSelectedItem();
        String searchText = txtSearch.getText().toLowerCase().trim();


        List<Household> filtered = all4Ps.stream()
                .filter(h -> {
                    // 1. Purok Filter
                    if (!"All Purok".equals(selectedPurok) && !selectedPurok.equalsIgnoreCase(h.getPurok())) {
                        return false;
                    }

                    // 2. IMPROVED Search - Search across multiple fields
                    if (!searchText.isEmpty()) {
                        boolean found = false;
                        // Search in multiple fields
                        String householdNo = (h.getHouseholdNo() != null ? h.getHouseholdNo() : "").toLowerCase();
                        String fullName = (h.getFullName() != null ? h.getFullName() : "").toLowerCase();
                        String purok = (h.getPurok() != null ? h.getPurok() : "").toLowerCase();
                        String income = (h.getMonthlyIncome() != null ? h.getMonthlyIncome().toString() : "").toLowerCase();
                        String status = (h.getOwnershipType() != null ? h.getOwnershipType() : "").toLowerCase();

                        // Check each field
                        found = householdNo.contains(searchText) ||
                                fullName.contains(searchText) ||
                                purok.contains(searchText) ||
                                income.contains(searchText) ||
                                status.contains(searchText);

                        if (!found) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());


        // Update Table
        tableModel.setRowCount(0);
        for (Household h : filtered) {
            tableModel.addRow(new Object[]{
                    h.getHouseholdNo(),
                    h.getFullName(),
                    h.getPurok(),
                    h.getTotalMembers(),
                    h.getMonthlyIncome(),
                    h.getOwnershipType()
            });
        }
        lblTotal.setText("Total 4Ps Households: " + filtered.size());
    }


    // =========================================================================
    // UI COMPONENTS - ENHANCED WITH BETTER SEARCH AND VIBRANT BLUE COLORS
    // =========================================================================


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(HEADER_BG.darker(), 1),
                new EmptyBorder(20, 30, 20, 30)
        ));


        // Main Title
        JLabel titleLabel = new JLabel("4Ps BENEFICIARIES MASTERLIST");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);


        // Subtitle
        JLabel subtitleLabel = new JLabel("Pantawid Pamilyang Pilipino Program - Barangay Record");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(220, 220, 220));


        // Title Container
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setBackground(HEADER_BG);
        titleContainer.add(titleLabel);
        titleContainer.add(Box.createVerticalStrut(5));
        titleContainer.add(subtitleLabel);


        // Icon
        JLabel iconLabel = new JLabel("🏠");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        iconLabel.setForeground(Color.WHITE);
        iconLabel.setBorder(new EmptyBorder(0, 0, 0, 20));


        headerPanel.add(titleContainer, BorderLayout.WEST);
        headerPanel.add(iconLabel, BorderLayout.EAST);


        return headerPanel;
    }


    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));


        // --- FILTER CARD ---
        JPanel filterCard = new JPanel(new BorderLayout());
        filterCard.setBackground(CARD_BG);
        filterCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));


        // Filter Title
        JLabel filterTitle = new JLabel("Search & Filter Options");
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterTitle.setForeground(DARK_TEXT);
        filterTitle.setBorder(new EmptyBorder(0, 0, 10, 0));


        // Filter Container
        JPanel filterContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterContainer.setBackground(CARD_BG);


        // SAFE MODE: Hardcoded Puroks
        String[] puroks = {"Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5", "Purok 6", "Purok 7"};


        cmbPurok = new JComboBox<>();
        cmbPurok.setFont(TEXT_FONT);
        cmbPurok.addItem("All Purok");
        for (String s : puroks) cmbPurok.addItem(s);
        cmbPurok.addActionListener(e -> applyFilters());
        cmbPurok.setMaximumRowCount(10);
        cmbPurok.setBackground(Color.WHITE);
        cmbPurok.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        cmbPurok.setPreferredSize(new Dimension(150, 32));


        // IMPROVED Search Field with real-time search
        txtSearch = createStyledTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by household #, name, purok, income, or status...");
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters(); // Real-time filtering as user types
            }
        });
        txtSearch.setPreferredSize(new Dimension(250, 32));


        filterContainer.add(new JLabel("Filter by Purok:"));
        filterContainer.add(cmbPurok);
        filterContainer.add(Box.createHorizontalStrut(20));
        filterContainer.add(new JLabel("Search:"));
        filterContainer.add(txtSearch);


        // Refresh Button
        JButton btnRefresh = createStyledButton("Refresh");
        btnRefresh.addActionListener(e -> loadData());
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        filterContainer.add(Box.createHorizontalStrut(20));
        filterContainer.add(btnRefresh);


        // Reset Button
        JButton btnReset = createStyledButton("Reset Filters");
        btnReset.addActionListener(e -> {
            cmbPurok.setSelectedIndex(0);
            txtSearch.setText("");
            applyFilters();
        });
        btnReset.setPreferredSize(new Dimension(120, 32));
        filterContainer.add(btnReset);


        // Add components to filter card
        filterCard.add(filterTitle, BorderLayout.NORTH);
        filterCard.add(filterContainer, BorderLayout.CENTER);


        contentPanel.add(filterCard, BorderLayout.NORTH);


        // --- TABLE CARD ---
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));


        // Table Title Bar
        JPanel tableTitlePanel = new JPanel(new BorderLayout());
        tableTitlePanel.setBackground(CARD_BG);
        tableTitlePanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel tableTitle = new JLabel("4Ps Households");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(DARK_TEXT);

        //JLabel tableInfo = new JLabel("Double-click row for details • Real-time search");
        //tableInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        //tableInfo.setForeground(LIGHT_TEXT);

        tableTitlePanel.add(tableTitle, BorderLayout.WEST);
        //tableTitlePanel.add(tableInfo, BorderLayout.EAST);


        // Table
        String[] cols = {"Household #", "Family Head", "Purok", "Members", "Monthly Income", "Ownership Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Integer.class; // Members column
                if (columnIndex == 4) return Double.class;  // Income column
                return String.class;
            }
        };
        table = new JTable(tableModel);
        styleTable();


        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        scrollPane.getViewport().setBackground(TABLE_EVEN_ROW);
        scrollPane.setPreferredSize(new Dimension(900, 500));


        // Add to table card
        tableCard.add(tableTitlePanel, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);


        contentPanel.add(tableCard, BorderLayout.CENTER);


        // --- FOOTER PANEL ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(BG_COLOR);
        footerPanel.setBorder(new EmptyBorder(10, 5, 0, 5));


        lblTotal = new JLabel("Total 4Ps Households: 0");
        lblTotal.setFont(COUNT_FONT);
        lblTotal.setForeground(ACCENT_COLOR);


        // Info label
        //JLabel infoLabel = new JLabel("Real-time search enabled | Type in search field to filter instantly");
        //infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        //infoLabel.setForeground(LIGHT_TEXT);


        footerPanel.add(lblTotal, BorderLayout.WEST);
        //footerPanel.add(infoLabel, BorderLayout.EAST);


        contentPanel.add(footerPanel, BorderLayout.SOUTH);


        return contentPanel;
    }


    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(TEXT_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        textField.setBackground(Color.WHITE);
        return textField;
    }


    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(6, 15, 6, 15));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ACCENT_COLOR);
            }
        });

        return button;
    }


    private void styleTable() {
        // Table appearance
        table.setRowHeight(38);
        table.setFont(TABLE_FONT);
        table.setForeground(DARK_TEXT);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(TABLE_GRID_COLOR);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setBackground(TABLE_EVEN_ROW);


        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_TEXT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(30, 100, 160)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        header.setReorderingAllowed(false);

        // Custom column renderers with blue colors
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);

                // Add blue background colors for specific columns
                if (!isSelected) {
                    switch (column) {
                        case 0: // Household #
                            c.setBackground(COLUMN_HOUSEHOLD_BG);
                            break;
                        case 1: // Family Head
                            c.setBackground(COLUMN_NAME_BG);
                            break;
                        case 2: // Purok
                            c.setBackground(COLUMN_PUROK_BG);
                            break;
                        case 3: // Members
                            c.setBackground(COLUMN_MEMBERS_BG);
                            break;
                        case 4: // Income
                            c.setBackground(COLUMN_INCOME_BG);
                            break;
                        case 5: // Status
                            c.setBackground(COLUMN_STATUS_BG);
                            break;
                        default:
                            if (row % 2 == 0) {
                                c.setBackground(TABLE_EVEN_ROW);
                            } else {
                                c.setBackground(TABLE_ODD_ROW);
                            }
                    }
                }

                return c;
            }
        };

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                ((JLabel)c).setHorizontalAlignment(SwingConstants.LEFT);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(TABLE_EVEN_ROW);
                    } else {
                        c.setBackground(TABLE_ODD_ROW);
                    }
                }

                return c;
            }
        };

        // Apply renderers - center align most columns
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 1) { // Family Head - left aligned
                table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }


        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // Household #
        table.getColumnModel().getColumn(1).setPreferredWidth(220); // Family Head
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Purok
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Members
        table.getColumnModel().getColumn(4).setPreferredWidth(130); // Income
        table.getColumnModel().getColumn(5).setPreferredWidth(150); // Status


        // Custom renderer for the entire table with enhanced styling
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (!isSelected) {
                    // Column-specific text styling with blue shades
                    switch (column) {
                        case 0: // Household #
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                            c.setForeground(new Color(0, 80, 160)); // Dark blue
                            ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                            break;
                        case 1: // Family Head
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                            c.setForeground(new Color(0, 60, 120)); // Navy blue
                            ((JLabel)c).setHorizontalAlignment(SwingConstants.LEFT);
                            break;
                        case 2: // Purok
                            c.setFont(c.getFont().deriveFont(Font.BOLD, 13));
                            c.setForeground(new Color(30, 100, 200)); // Bright blue
                            ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                            break;
                        case 3: // Members
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                            c.setForeground(new Color(0, 100, 200)); // Medium blue
                            ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                            break;
                        case 4: // Income
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                            // Color code income levels with blue shades
                            if (value instanceof Number) {
                                double income = ((Number) value).doubleValue();
                                if (income < 10000) {
                                    c.setForeground(new Color(0, 120, 200)); // Light blue for low income
                                } else if (income < 20000) {
                                    c.setForeground(new Color(0, 80, 180)); // Medium blue for medium
                                } else {
                                    c.setForeground(new Color(0, 40, 120)); // Dark blue for high
                                }
                            }
                            ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                            break;
                        case 5: // Status
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                            String status = value != null ? value.toString() : "";
                            if (status.equalsIgnoreCase("Owned")) {
                                c.setForeground(new Color(0, 100, 180)); // Blue for Owned
                            } else if (status.equalsIgnoreCase("Rented")) {
                                c.setForeground(new Color(0, 140, 200)); // Light blue for Rented
                            } else if (status.equalsIgnoreCase("Mortgaged")) {
                                c.setForeground(new Color(0, 60, 120)); // Dark blue for Mortgaged
                            } else {
                                c.setForeground(new Color(100, 140, 200)); // Gray-blue for other
                            }
                            ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                            break;
                    }
                } else {
                    // Selected row styling
                    c.setBackground(TABLE_SELECTION_BG);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setForeground(Color.BLACK);
                }

                // Add subtle border effect to cells
                ((JLabel)c).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 240, 250)),
                        BorderFactory.createEmptyBorder(2, 8, 2, 8)
                ));

                return c;
            }
        });

        // Special renderer for header
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                c.setBackground(TABLE_HEADER_BG);
                c.setForeground(TABLE_HEADER_TEXT);
                ((JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel)c).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(60, 140, 200)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
                ((JLabel)c).setFont(((JLabel)c).getFont().deriveFont(Font.BOLD, 13));
                return c;
            }
        });

        // Set table preferred viewport size
        table.setPreferredScrollableViewportSize(new Dimension(850, 400));

        // Add hover effect listener
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0 && row < table.getRowCount()) {
                    table.setRowSelectionInterval(row, row);
                }
            }
        });

        // Double-click listener for viewing details
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        Object householdNo = tableModel.getValueAt(row, 0);
                        // TODO: Implement view household details
                        System.out.println("Viewing household with ID: " + householdNo);
                    }
                }
            }
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setSize(1200, 800);
            f.add(new Captain4PsTab());
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
}

