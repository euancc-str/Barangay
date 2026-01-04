package org.example.Captain;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;


import org.example.Admin.AdminSettings.AdminAssetTab;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.ResidentDAO;
import org.example.Users.Resident;
import org.example.utils.AutoRefresher;


public class CaptainPWDTab extends JPanel {


    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTotal;


    // Filters
    private JComboBox<String> cmbPurok;
    private JTextField txtSearch; // For Address/Name
    private JTextField txtMinAge, txtMaxAge;


    // Data Cache
    private List<Resident> allPWDs = new ArrayList<>();


    // --- COLORS (Captain Theme - Enhanced with more vibrant colors) ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(41, 128, 185); // Primary blue
    private final Color CARD_BG = Color.WHITE;
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color DARK_TEXT = new Color(60, 60, 60);
    private final Color LIGHT_TEXT = new Color(100, 100, 100);


    // Enhanced Table Colors - More vibrant but still eye-friendly
    private final Color TABLE_HEADER_BG = new Color(41, 128, 185); // Bright blue header
    private final Color TABLE_HEADER_TEXT = Color.WHITE;
    private final Color TABLE_EVEN_ROW = new Color(255, 255, 255); // Pure white
    private final Color TABLE_ODD_ROW = new Color(248, 252, 255); // Very light blue tint
    private final Color TABLE_GRID_COLOR = new Color(230, 240, 250); // Soft blue grid
    private final Color TABLE_SELECTION_BG = new Color(220, 238, 255); // Soft blue selection
    private final Color TABLE_HIGHLIGHT_AGE = new Color(255, 245, 230); // Light orange for age
    private final Color TABLE_HIGHLIGHT_GENDER = new Color(245, 255, 245); // Light green for gender
    private final Color TABLE_HIGHLIGHT_PUROK = new Color(245, 245, 255); // Light purple for purok
    private final Color TABLE_HOVER = new Color(245, 250, 255); // Slightly blue for hover effect


    // New vibrant column colors
    private final Color COLUMN_AGE_BG = new Color(255, 245, 230); // Soft orange
    private final Color COLUMN_GENDER_BG = new Color(230, 245, 255); // Soft blue
    private final Color COLUMN_PUROK_BG = new Color(235, 230, 255); // Soft purple
    private final Color COLUMN_CONTACT_BG = new Color(230, 255, 245); // Soft green


    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font COUNT_FONT = new Font("Segoe UI", Font.BOLD, 15);


    public CaptainPWDTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(0, 0, 0, 0));


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);


        SwingUtilities.invokeLater(() -> loadData());
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {


                javax.swing.Timer startTimer = new javax.swing.Timer(500, e -> {
                    new Thread(() -> {
                        if (refresher != null)
                            refresher.stop();
                        loadData();

                        // Initialize the refresher safely in the background
                        refresher = new AutoRefresher("Resident", () -> {
                            SwingUtilities.invokeLater(() -> loadData());
                        });


                        System.out.println("PWD Tab: Auto-refresh connected.");
                    }).start();
                });


                startTimer.setRepeats(false); // Run only once
                startTimer.start();
            }


            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {
                if (refresher != null) {
                    refresher.stop();
                    refresher = null;
                }
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
        new SwingWorker<List<Resident>, Void>() {
            @Override
            protected List<Resident> doInBackground() {
                return new ResidentDAO().getPWDResidents();
            }


            @Override
            protected void done() {
                try {
                    allPWDs = get();
                    applyFilters(); // Initial populate
                    // Initialize sorter after data is loaded
                    sorter = new TableRowSorter<>(tableModel);
                    table.setRowSorter(sorter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    private void applyFilters() {
        String selectedPurok = (String) cmbPurok.getSelectedItem();
        String searchText = txtSearch.getText().toLowerCase().trim();


        int minAge = 0;
        int maxAge = 200;


        try {
            if (!txtMinAge.getText().isEmpty())
                minAge = Integer.parseInt(txtMinAge.getText().trim());
            if (!txtMaxAge.getText().isEmpty())
                maxAge = Integer.parseInt(txtMaxAge.getText().trim());
        } catch (NumberFormatException e) {
            // Ignore invalid number input
        }


        // Final variables for lambda
        int finalMin = minAge;
        int finalMax = maxAge;


        List<Resident> filtered = allPWDs.stream()
                .filter(r -> {
                    // 1. Purok Filter
                    if (!"All Purok".equals(selectedPurok) && !selectedPurok.equalsIgnoreCase(r.getPurok())) {
                        return false;
                    }
                    // 2. Age Filter
                    if (r.getAge() < finalMin || r.getAge() > finalMax) {
                        return false;
                    }
                    // 3. IMPROVED Search - Search across multiple fields
                    if (!searchText.isEmpty()) {
                        boolean found = false;
                        // Search in name (first, middle, last separately for better matching)
                        String fullName = (r.getFirstName() + " " + r.getMiddleName() + " " + r.getLastName())
                                .toLowerCase();
                        String firstName = r.getFirstName().toLowerCase();
                        String lastName = r.getLastName().toLowerCase();
                        String address = (r.getAddress() != null ? r.getAddress() : "").toLowerCase();
                        String street = (r.getStreet() != null ? r.getStreet() : "").toLowerCase();
                        String contact = (r.getContactNo() != null ? r.getContactNo() : "").toLowerCase();
                        String purok = (r.getPurok() != null ? r.getPurok() : "").toLowerCase();


                        // Check each field
                        found = fullName.contains(searchText) ||
                                firstName.contains(searchText) ||
                                lastName.contains(searchText) ||
                                address.contains(searchText) ||
                                street.contains(searchText) ||
                                contact.contains(searchText) ||
                                purok.contains(searchText);


                        if (!found) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());


        // Update Table
        tableModel.setRowCount(0);
        for (Resident r : filtered) {
            tableModel.addRow(new Object[] {
                    r.getResidentId(),
                    r.getFirstName() + " " + r.getMiddleName() + " " + r.getLastName(),
                    r.getAge(),
                    r.getGender(),
                    r.getPurok(),
                    r.getAddress(),
                    r.getContactNo()
            });
        }
        lblTotal.setText("Total PWDs Found: " + filtered.size());
    }


    // =========================================================================
    // UI COMPONENTS - ENHANCED WITH BETTER SEARCH
    // =========================================================================


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(HEADER_BG.darker(), 1),
                new EmptyBorder(20, 30, 20, 30)));


        // Main Title
        JLabel titleLabel = new JLabel("PWD MASTERLIST");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);


        // Subtitle
        JLabel subtitleLabel = new JLabel("Persons with Disabilities - Barangay Record");
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
        JLabel iconLabel = new JLabel("♿");
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


        // --- FILTER PANEL (Compact Card Style) ---
        JPanel filterCard = new JPanel(new BorderLayout());
        filterCard.setBackground(CARD_BG);
        filterCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(15, 15, 15, 15)));


        // Filter Title
        JLabel filterTitle = new JLabel("Search & Filter Options");
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterTitle.setForeground(DARK_TEXT);
        filterTitle.setBorder(new EmptyBorder(0, 0, 10, 0));


        // Main Filter Container - Compact layout
        JPanel filterContainer = new JPanel(new GridBagLayout());
        filterContainer.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        // Load Puroks
        String[] puroks = new SystemConfigDAO().getOptionsNature("purok");
        if (puroks.length == 0)
            puroks = new String[] { "Purok 1", "Purok 2", "Purok 3", "Purok 4", "Purok 5" };


        cmbPurok = new JComboBox<>();
        cmbPurok.setFont(TEXT_FONT);
        cmbPurok.addItem("All Purok");
        for (String s : puroks)
            cmbPurok.addItem(s);
        cmbPurok.addActionListener(e -> applyFilters());
        cmbPurok.setMaximumRowCount(10);
        cmbPurok.setBackground(Color.WHITE);
        cmbPurok.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        cmbPurok.setPreferredSize(new Dimension(150, 32));


        // Age Inputs
        // txtMinAge = createStyledTextField(3);
        // txtMinAge.setPreferredSize(new Dimension(60, 32));
        // txtMaxAge = createStyledTextField(3);
        // txtMaxAge.setPreferredSize(new Dimension(60, 32));
        // KeyAdapter enterKey = new KeyAdapter() {
        // public void keyReleased(KeyEvent e) { applyFilters(); }
        // };
        // txtMinAge.addKeyListener(enterKey);
        // txtMaxAge.addKeyListener(enterKey);
        // Age Inputs


        txtMinAge = createStyledTextField(3);
        txtMinAge.setPreferredSize(new Dimension(60, 32));
        txtMaxAge = createStyledTextField(3);
        txtMaxAge.setPreferredSize(new Dimension(60, 32));


        // --- NEW: Numeric Only Filter ---
        ((javax.swing.text.AbstractDocument) txtMinAge.getDocument()).setDocumentFilter(new NumericDocumentFilter());
        ((javax.swing.text.AbstractDocument) txtMaxAge.getDocument()).setDocumentFilter(new NumericDocumentFilter());


        // --- NEW: Validation Logic ---
        java.awt.event.FocusAdapter ageValidationListener = new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                validateAgeRange();
            }
        };


        txtMinAge.addFocusListener(ageValidationListener);
        txtMaxAge.addFocusListener(ageValidationListener);


        // Keep your existing Enter Key listener
        KeyAdapter enterKey = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                validateAgeRange(); // Validate while typing/releasing
                applyFilters();
            }
        };
        txtMinAge.addKeyListener(enterKey);
        txtMaxAge.addKeyListener(enterKey);


        // IMPROVED Search Field with real-time search
        txtSearch = createStyledTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by name, address, purok, or contact...");
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters(); // Real-time filtering as user types
            }
        });
        txtSearch.setPreferredSize(new Dimension(250, 32));


        // Row 1: Purok Filter
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel purokLabel = new JLabel("Purok:");
        purokLabel.setFont(LABEL_FONT);
        purokLabel.setForeground(DARK_TEXT);
        filterContainer.add(purokLabel, gbc);


        gbc.gridx = 1;
        gbc.weightx = 0.3;
        filterContainer.add(cmbPurok, gbc);


        // Row 1: Age Filter (same row)
        gbc.gridx = 2;
        gbc.weightx = 0;
        JLabel ageLabel = new JLabel("Age Range:");
        ageLabel.setFont(LABEL_FONT);
        ageLabel.setForeground(DARK_TEXT);
        filterContainer.add(ageLabel, gbc);


        JPanel agePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        agePanel.setBackground(CARD_BG);
        agePanel.add(txtMinAge);
        agePanel.add(new JLabel(" to "));
        agePanel.add(txtMaxAge);
        gbc.gridx = 3;
        filterContainer.add(agePanel, gbc);


        // Row 1: Search Filter (same row)
        gbc.gridx = 4;
        gbc.weightx = 0;
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(LABEL_FONT);
        searchLabel.setForeground(DARK_TEXT);
        filterContainer.add(searchLabel, gbc);


        gbc.gridx = 5;
        gbc.weightx = 0.7;
        filterContainer.add(txtSearch, gbc);


        // Reset Button (same row)
        JButton btnReset = createStyledButton("Reset");
        btnReset.addActionListener(e -> {
            cmbPurok.setSelectedIndex(0);
            txtMinAge.setText("");
            txtMaxAge.setText("");
            txtSearch.setText("");
            applyFilters();
        });
        btnReset.setPreferredSize(new Dimension(100, 32));


        gbc.gridx = 6;
        gbc.weightx = 0;
        filterContainer.add(btnReset, gbc);


        // Add components to filter card
        filterCard.add(filterTitle, BorderLayout.NORTH);
        filterCard.add(filterContainer, BorderLayout.CENTER);


        contentPanel.add(filterCard, BorderLayout.NORTH);


        // --- TABLE PANEL (Larger Card Style) ---
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 10, 10, 10)));


        // Table Title Bar
        JPanel tableTitlePanel = new JPanel(new BorderLayout());
        tableTitlePanel.setBackground(CARD_BG);
        tableTitlePanel.setBorder(new EmptyBorder(0, 0, 10, 0));


        JLabel tableTitle = new JLabel("PWD Residents");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(DARK_TEXT);


        // JLabel tableInfo = new JLabel("Double-click row for details • Real-time
        // search");
        // tableInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        // tableInfo.setForeground(LIGHT_TEXT);


        tableTitlePanel.add(tableTitle, BorderLayout.WEST);
        // tableTitlePanel.add(tableInfo, BorderLayout.EAST);


        // Table
        String[] cols = { "ID", "Full Name", "Age", "Sex", "Purok", "Address", "Contact" };
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }


            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2)
                    return Integer.class; // Age column
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


        lblTotal = new JLabel("Total PWDs Found: 0");
        lblTotal.setFont(COUNT_FONT);
        lblTotal.setForeground(ACCENT_COLOR);


        // Info label
        // JLabel infoLabel = new JLabel("Real-time search enabled | Type in search
        // field to filter instantly");
        // infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        // infoLabel.setForeground(LIGHT_TEXT);


        footerPanel.add(lblTotal, BorderLayout.WEST);
        // footerPanel.add(infoLabel, BorderLayout.EAST);


        contentPanel.add(footerPanel, BorderLayout.SOUTH);


        return contentPanel;
    }


    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(TEXT_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
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
        table.setRowHeight(38); // Slightly taller for better readability
        table.setFont(TABLE_FONT);
        table.setForeground(DARK_TEXT);
        table.setSelectionBackground(TABLE_SELECTION_BG);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(TABLE_GRID_COLOR);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setBackground(TABLE_EVEN_ROW);


        // Header styling - More vibrant
        JTableHeader header = table.getTableHeader();
        header.setFont(TABLE_HEADER_FONT);
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_TEXT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(30, 100, 160)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        header.setReorderingAllowed(false);


        // Custom column renderers with vibrant colors
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);


                // Add vibrant background colors for specific columns
                if (!isSelected) {
                    switch (table.convertColumnIndexToModel(column)) {
                        case 2: // Age column
                            c.setBackground(COLUMN_AGE_BG);
                            break;
                        case 3: // Gender column
                            c.setBackground(COLUMN_GENDER_BG);
                            break;
                        case 4: // Purok column
                            c.setBackground(COLUMN_PUROK_BG);
                            break;
                        case 6: // Contact column
                            c.setBackground(COLUMN_CONTACT_BG);
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
                ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);


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


        // Apply renderers
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer); // Name - left aligned
        table.getColumnModel().getColumn(5).setCellRenderer(leftRenderer); // Address - left aligned


        // Center-aligned columns with vibrant backgrounds
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Age
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Gender
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Purok
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Contact


        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);


        // Set column widths - optimized for larger table
        table.getColumnModel().getColumn(1).setPreferredWidth(220); // Full Name
        table.getColumnModel().getColumn(2).setPreferredWidth(70); // Age
        table.getColumnModel().getColumn(3).setPreferredWidth(90); // Gender
        table.getColumnModel().getColumn(4).setPreferredWidth(110); // Purok
        table.getColumnModel().getColumn(5).setPreferredWidth(250); // Address
        table.getColumnModel().getColumn(6).setPreferredWidth(130); // Contact


        // Custom renderer for the entire table with enhanced styling
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);


                int modelColumn = table.convertColumnIndexToModel(column);


                if (!isSelected) {
                    // Apply column-specific backgrounds (already handled by column renderers)
                    // Just ensure text styling


                    // Age column - bold and centered
                    if (modelColumn == 2) {
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                        c.setForeground(new Color(200, 100, 0)); // Orange text for age
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    }


                    // Gender column - styled text
                    else if (modelColumn == 3) {
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                        String gender = value != null ? value.toString() : "";
                        if (gender.equalsIgnoreCase("Male")) {
                            c.setForeground(new Color(0, 100, 200)); // Blue for male
                        } else if (gender.equalsIgnoreCase("Female")) {
                            c.setForeground(new Color(200, 0, 150)); // Pink for female
                        }
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    }


                    // Purok column - vibrant color
                    else if (modelColumn == 4) {
                        c.setFont(c.getFont().deriveFont(Font.BOLD, 13));
                        c.setForeground(new Color(100, 0, 200)); // Purple for purok
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    }


                    // Contact column - different style
                    else if (modelColumn == 6) {
                        c.setFont(c.getFont().deriveFont(Font.PLAIN, 12));
                        c.setForeground(new Color(0, 120, 60)); // Green for contact
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    }


                    // Name column - normal
                    else if (modelColumn == 1) {
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                        c.setForeground(new Color(30, 30, 30)); // Dark for name
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                    }


                    else {
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                    }
                } else {
                    // Selected row styling
                    c.setBackground(TABLE_SELECTION_BG);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    c.setForeground(Color.BLACK);
                }


                // Add subtle border effect to cells
                ((JLabel) c).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)),
                        BorderFactory.createEmptyBorder(2, 8, 2, 8)));


                // Add hover effect using client property
                if (table.getRowCount() > row) {
                    table.setToolTipText("Double-click to view resident details");
                }


                return c;
            }
        });


        // Special renderer for header to remove the default 3D effect
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                c.setBackground(TABLE_HEADER_BG);
                c.setForeground(TABLE_HEADER_TEXT);
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel) c).setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(60, 140, 200)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)));
                ((JLabel) c).setFont(((JLabel) c).getFont().deriveFont(Font.BOLD, 13));
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
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && row < table.getRowCount() && col >= 0) {
                    // Highlight row on hover
                    table.setRowSelectionInterval(row, row);
                }
            }
        });


        // Double-click listener for viewing details (placeholder)
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {


                        int modelRow = table.convertRowIndexToModel(row);
                        Object residentId = tableModel.getValueAt(modelRow, 0);
                        // TODO: Implement view resident details
                        System.out.println("Viewing resident with ID: " + residentId);
                    }
                }
            }
        });
    }


    // Validates if Min > Max and updates UI color
    private void validateAgeRange() {
        try {
            String minStr = txtMinAge.getText().trim();
            String maxStr = txtMaxAge.getText().trim();


            if (!minStr.isEmpty() && !maxStr.isEmpty()) {
                int min = Integer.parseInt(minStr);
                int max = Integer.parseInt(maxStr);


                if (min > max) {
                    txtMinAge.setForeground(Color.RED);
                    txtMaxAge.setForeground(Color.RED);
                    txtMinAge.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
                } else {
                    txtMinAge.setForeground(DARK_TEXT);
                    txtMaxAge.setForeground(DARK_TEXT);
                    txtMinAge.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
                }
            } else {
                txtMinAge.setForeground(DARK_TEXT);
                txtMaxAge.setForeground(DARK_TEXT);
                txtMinAge.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            }
        } catch (NumberFormatException e) {
            // Silently handle
        }
    }


    // DocumentFilter to allow only positive digits
    private static class NumericDocumentFilter extends javax.swing.text.DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr)
                throws javax.swing.text.BadLocationException {
            if (string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            }
        }


        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs)
                throws javax.swing.text.BadLocationException {
            if (text.matches("\\d+")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }
}



