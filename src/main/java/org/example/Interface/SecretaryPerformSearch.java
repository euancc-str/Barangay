package org.example.Interface;


import org.example.Admin.AdminSettings.SystemConfigDAO;

import org.example.BlotterCaseDAO;
import org.example.ResidentDAO;
import org.example.SerbisyongBarangay.requestaDocumentFrame;

import org.example.UserDataManager;
import org.example.Users.Resident;


import java.awt.*;
import java.awt.event.*;

import java.util.Comparator;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import java.util.List;
import javax.swing.table.*;



public class SecretaryPerformSearch extends JPanel {


    private JTable residentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;


    // Components
    private JLabel lblRecordCount;
    private JTextField searchField;


    // --- NEW GRADIENT CERULEAN AND LIGHT BLUE COLOR SCHEME ---
    private final Color CERULEAN = new Color(0, 123, 167);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    private final Color DARK_CERULEAN = new Color(0, 90, 140);
    private final Color HEADER_BG = DARK_CERULEAN;
    private final Color TABLE_HEADER_BG = new Color(70, 130, 180); // Steel blue
    private final Color BTN_ADD_COLOR = new Color(0, 150, 199);
    private final Color BTN_UPDATE_COLOR = new Color(100, 149, 237); // Cornflower blue
    private final Color BTN_DEACTIVATE_COLOR = new Color(220, 100, 100);


    public SecretaryPerformSearch() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false); // Make transparent for gradient


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);


        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                return null;
            }

            @Override
            protected void done() {
                loadResidentData(); // Runs after the white screen is gone
            }
        }.execute();
        startLightPolling();
    }

    private javax.swing.Timer lightTimer;
    private static volatile long lastGlobalUpdate = System.currentTimeMillis();
    private void startLightPolling() {
        lightTimer = new javax.swing.Timer(3000, e -> { // Every 3 seconds
            if (residentTable != null && residentTable.getSelectedRow() == -1) {
                // Just check a simple "last updated" flag
                checkLightUpdate();
            }
        });
        lightTimer.start();
    }

    private void checkLightUpdate() {
        // Quick query - just get the latest timestamp
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                String sql = "SELECT UNIX_TIMESTAMP(MAX(GREATEST(" +
                        "COALESCE(updatedAt, '1970-01-01'), " +
                        "COALESCE(createdAt, '1970-01-01')" +
                        "))) as last_ts FROM resident";

                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {

                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        return rs.getLong("last_ts") * 1000L; // Convert to milliseconds
                    }
                }
                return 0L;
            }

            @Override
            protected void done() {
                try {
                    long dbTimestamp = get();
                    if (dbTimestamp > lastGlobalUpdate) {
                        lastGlobalUpdate = dbTimestamp;
                        loadResidentData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // Create gradient from cerulean to light blue
        GradientPaint gradient = new GradientPaint(
                0, 0, CERULEAN,
                getWidth(), getHeight(), LIGHT_BLUE
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private ResidentDAO rd = new ResidentDAO();
    public void loadResidentData() {

        System.out.println("loaded");
        if (tableModel != null) {
            tableModel.setRowCount(0);
        }

        List<Resident> residentsList = rd.getAllResidents();
        for(Resident resident : residentsList){
            String name = resident.getFirstName() + " "+resident.getMiddleName()+" "+ resident.getLastName();
            if(resident != null) {
                tableModel.addRow(new Object[]{""+resident.getResidentId(),
                        name,
                        resident.getGender(),
                        ""+resident.getAge(),
                        resident.getAddress()
                });
            }
        }
        if(residentTable!=null){
            residentTable.repaint();
        }
        updateRecordCount();
    }


    private void updateRecordCount() {
        if (lblRecordCount != null && residentTable != null) {
            int count = residentTable.getRowCount();
            lblRecordCount.setText("Total Records: " + count);
        }
    }


    private void handleUpdate() {
        int selectedRow = residentTable.getSelectedRow();
        if (selectedRow == -1) return;


        int modelRow = residentTable.convertRowIndexToModel(selectedRow);


        // Retrieve values from table
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String gender = (String) tableModel.getValueAt(modelRow, 2);
        String age = (String) tableModel.getValueAt(modelRow, 3);
        String address = (String) tableModel.getValueAt(modelRow, 4);

        Resident resident = rd.findResidentById(Integer.parseInt(id));


        UserDataManager.getInstance().setCurrentResident(resident);


        showUpdateResidentDialog(id, name, gender, age, address, modelRow);
    }


    private void showUpdateResidentDialog(String id, String name, String gender, String age, String address, int modelRow) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Request Resident a doc", true);
        dialog.setSize(550, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());


        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));


        JLabel titleLabel = new JLabel("Viewing Resident Info", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(DARK_CERULEAN);
        mainPanel.add(titleLabel, BorderLayout.NORTH);


        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));


        // --- FIELDS ---
        JTextField txtId = createStyledTextField(id);
        txtId.setEditable(false);
        txtId.setBackground(new Color(240, 248, 255)); // Alice blue
        addStyledRow(detailsPanel, "Resident ID:", txtId);


        JTextField txtName = createStyledTextField(name);
        addStyledRow(detailsPanel, "Full Name:", txtName);
        txtName.setEditable(false);
        String[] genders = new SystemConfigDAO().getOptionsNature("sex");
        JComboBox<String> cbGender = new JComboBox<>(genders);
        cbGender.setSelectedItem(gender);
        cbGender.setEnabled(false);
        cbGender.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Sex:", cbGender);


        JTextField txtAge = createStyledTextField(age);
        addStyledRow(detailsPanel, "Age:", txtAge);


        JTextField txtAddress = createStyledTextField(address);
        addStyledRow(detailsPanel, "Address:", txtAddress);


        // --- SEPARATOR ---
        detailsPanel.add(Box.createVerticalStrut(15));
        detailsPanel.add(Box.createVerticalStrut(10));




        // Add Hint for Password
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(Color.WHITE);


        JLabel lblHint = new JLabel(" (Leave blank to keep current)");
        lblHint.setFont(new Font("Arial", Font.ITALIC, 11));
        lblHint.setForeground(Color.GRAY);
        passPanel.add(lblHint, BorderLayout.SOUTH);


        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);


        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);


        JButton btnCancel = createRoundedButton("Cancel", new Color(120, 120, 120));
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());


        JButton btnSave = createRoundedButton("Request Resident a document", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(250, 55));


        btnSave.addActionListener(e -> {

            int confirm = JOptionPane.showConfirmDialog(dialog, "Open request?", "Confirm", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                BlotterCaseDAO blotterDao = new BlotterCaseDAO();
                String crimeRecord = blotterDao.checkMultiplePeople(name);
                String finalFindings = "";
                if (crimeRecord == null || crimeRecord.equals("CLEAN") || crimeRecord.isEmpty() || crimeRecord.contains("SETTLED") || crimeRecord.contains("DISMISSED")) {
                    finalFindings = "NO DEROGATORY RECORD";
                } else {
                  int continueResidentsRequest =  JOptionPane.showConfirmDialog(dialog,
                            "⚠️ WARNING: This resident has a pending case!\n" + crimeRecord,
                            "Derogatory Record Found",
                          JOptionPane.OK_CANCEL_OPTION);
                  if(continueResidentsRequest == JOptionPane.CANCEL_OPTION){
                      return;
                  }

                }
                dialog.dispose();

                Window mainInterface = SwingUtilities.getWindowAncestor(SecretaryPerformSearch.this);

                if (mainInterface != null) {
                    mainInterface.dispose();
                }
                requestaDocumentFrame frame = new requestaDocumentFrame();
                frame.setVisible(true);
            }
        });


        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);


        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }


    // =========================================================================
    // 3. ADD RESIDENT LOGIC
    // =========================================================================




    // Add this class at the bottom of your file
    static class PhoneDocumentFilter extends javax.swing.text.DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (string == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);


            if (isValidPhone(newText)) {
                super.insertString(fb, offset, string, attr);
            }
        }


        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
            if (text == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);


            if (isValidPhone(newText)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }


        // YOUR LOGIC HERE
        private boolean isValidPhone(String text) {
            if (text.isEmpty()) return true;
            if (!text.matches("\\d*")) return false; // Digits only
            if (text.length() > 11) return false;    // Max 11
            // Strict check: Must start with 09 if length is 2 or more
            if (text.length() >= 2 && !text.startsWith("09")) return false;
            return true;
        }
    }


    // Custom Table Header Renderer with Gradient
    class GradientHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


            setOpaque(false);
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Arial", Font.BOLD, 15));
            setForeground(Color.BLACK); // Black font color
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE),
                    BorderFactory.createEmptyBorder(0, 5, 0, 5)
            ));


            return this;
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            // Create gradient for header cells
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(100, 180, 255), // Light blue
                    0, getHeight(), new Color(70, 130, 180)  // Steel blue
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());


            super.paintComponent(g);
        }
    }


    // =========================================================================
    // GUI SETUP
    // =========================================================================


    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // Transparent to show gradient background
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));


        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));


        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(20));


        // Search Panel with Icon
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));


        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));


        // Create a panel to hold the search field with icon
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.setBackground(new Color(177, 208, 251));
        searchFieldPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1, true),
                new EmptyBorder(2, 5, 2, 5)
        ));
        searchFieldPanel.setPreferredSize(new Dimension(250, 35));


        searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        searchField.setBackground(new Color(177, 208, 251)); // Alice blue
        searchField.setOpaque(false);


        // Create search icon (using a scaled ImageIcon)
        JLabel searchIcon = new JLabel();
        searchIcon.setPreferredSize(new Dimension(20, 20));
        searchIcon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));


        try {
            // Try to load search icon from file
            ImageIcon originalIcon = new ImageIcon("resources/search.png");
            if (originalIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                searchIcon.setIcon(new ImageIcon(scaledImage));
            } else {
                // Fallback: create a simple search icon using graphics
                searchIcon.setIcon(createSearchIcon());
            }
        } catch (Exception e) {
            // Fallback: create a simple search icon using graphics
            searchIcon.setIcon(createSearchIcon());
        }


        searchFieldPanel.add(searchField, BorderLayout.CENTER);
        searchFieldPanel.add(searchIcon, BorderLayout.EAST);



        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                updateRecordCount();
            }
        });


        searchPanel.add(searchLabel);
        searchPanel.add(searchFieldPanel);
        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));


        // --- TABLE SETUP ---
        String[] columnNames = {"ID", "Full Name", "Sex", "Age", "Address"};


        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };


        residentTable = new JTable(tableModel);
        residentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        residentTable.setRowHeight(50);
        residentTable.setGridColor(new Color(200, 200, 200));
        residentTable.setSelectionBackground(new Color(173, 216, 230, 100)); // Light blue with transparency
        residentTable.setSelectionForeground(Color.BLACK);
        residentTable.setShowVerticalLines(true);
        residentTable.setShowHorizontalLines(true);
        residentTable.setBackground(new Color(144,213,255));
        residentTable.setForeground(Color.BLACK);


        residentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleUpdate();
            }
        });


        sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(0, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Integer i1 = Integer.parseInt(s1);
                    Integer i2 = Integer.parseInt(s2);
                    return i1.compareTo(i2);
                } catch (NumberFormatException e) {
                    return s1.compareTo(s2); // Fallback
                }
            }
        });


        sorter.setComparator(3, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Integer i1 = Integer.parseInt(s1);
                    Integer i2 = Integer.parseInt(s2);
                    return i1.compareTo(i2);
                } catch (Exception e) {
                    return s1.compareTo(s2);
                }
            }
        });


        residentTable.setRowSorter(sorter);


        sorter.addRowSorterListener(new RowSorterListener() {
            @Override
            public void sorterChanged(RowSorterEvent e) {
                updateRecordCount();
            }
        });


        JTableHeader header = residentTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(header.getWidth(), 50));


        // Apply gradient renderer to all header columns
        GradientHeaderRenderer headerRenderer = new GradientHeaderRenderer();
        for (int i = 0; i < residentTable.getColumnCount(); i++) {
            residentTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < residentTable.getColumnCount(); i++) {
            residentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        JScrollPane tableScrollPane = new JScrollPane(residentTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setOpaque(false);


        contentPanel.add(tableScrollPane);


        // Footer Count
        contentPanel.add(Box.createVerticalStrut(10));
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setOpaque(false);


        lblRecordCount = new JLabel("Total Records: 0");
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        lblRecordCount.setForeground(Color.BLACK);


        footerPanel.add(lblRecordCount);
        contentPanel.add(footerPanel);


        return contentPanel;
    }


    // Method to create a simple search icon programmatically
    private Icon createSearchIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Draw search icon (magnifying glass)
                g2d.setColor(Color.GRAY);
                g2d.setStroke(new BasicStroke(2f));


                // Draw circle
                g2d.drawOval(x + 2, y + 2, 10, 10);


                // Draw handle
                g2d.drawLine(x + 10, y + 10, x + 14, y + 14);


                g2d.dispose();
            }


            @Override
            public int getIconWidth() {
                return 16;
            }


            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }


    // Visual Helpers
    private void addStyledRow(JPanel panel, String labelText, JComponent field) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));


        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
        label.setPreferredSize(new Dimension(150, 35));


        if (field instanceof JPanel) label.setVerticalAlignment(SwingConstants.TOP);


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
                new EmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(new Color(240, 248, 255)); // Alice blue
        return field;
    }


    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Create gradient for button
                GradientPaint btnGradient = new GradientPaint(
                        0, 0, bgColor,
                        0, getHeight(), bgColor.darker()
                );
                g2.setPaint(btnGradient);
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
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Create gradient for header
                GradientPaint headerGradient = new GradientPaint(
                        0, 0, DARK_CERULEAN,
                        getWidth(), 0, CERULEAN
                );
                g2d.setPaint(headerGradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(DARK_CERULEAN);
                        g2.fillRoundRect(x, y, width, height, 30, 30);
                    }
                }, new EmptyBorder(25, 40, 25, 40)));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 26));
        lblSystem.setForeground(Color.WHITE);
        JLabel lblModule = new JLabel("Resident Management");
        lblModule.setFont(new Font("Arial", Font.BOLD, 22));
        lblModule.setForeground(Color.WHITE);
        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            JFrame frame = new JFrame("Search Resident Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);


            frame.add(new SecretaryPerformSearch());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

