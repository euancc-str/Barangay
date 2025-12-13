package org.example.treasurer;


import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Admin.SystemLogDAO;
import org.example.DocumentRequestDao;
import org.example.Documents.DocumentRequest;
import org.example.Documents.DocumentType;
import org.example.Documents.Payment;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class TreasurerDashboard extends JFrame {


    private JPanel contentContainer;
    private CardLayout cardLayout;
    private JPanel sidebar;
    private JTable paymentTable;
    private DefaultTableModel pendingTableModel;
    private DefaultTableModel verifiedTableModel;
    private JButton btnPending, btnVerified;
    private boolean showingPending = true;



    private JTextField searchField;
    private JLabel lblRecordCount;
    private TableRowSorter<DefaultTableModel> sorter;


    // Gradient colors
    private final Color CERULEAN_BLUE = new Color(100, 149, 237);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    private final Color VERY_LIGHT_BLUE = new Color(225, 245, 254);
    private final Color DARK_CERULEAN = new Color(70, 130, 180);


    public TreasurerDashboard() {
        setTitle("Documentary Request - Treasurer Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);


        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(DARK_CERULEAN);


        // Sidebar
        sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);


        // Content area with CardLayout
        JPanel contentArea = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), VERY_LIGHT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(15, 15, 15, 15));


        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setOpaque(false);


        // Add different panels
        contentContainer.add(new TreasurerPersonalInformation(), "personal_info");
        contentContainer.add(createDashboardPanel(), "dashboard");
        contentContainer.add(createTotalPaidUnpaidPanel(), "total");
        contentContainer.add(createPlaceholderPanel("Barangay Official Profile"), "profile");
        contentContainer.add(new TreasurerReportsTab(), "financial_reports");
        contentArea.add(contentContainer, BorderLayout.CENTER);
        mainPanel.add(contentArea, BorderLayout.CENTER);


        add(mainPanel);


        // Show dashboard by default
        cardLayout.show(contentContainer, "dashboard");
    }
    private void loadTableData(DefaultTableModel model, String status) {
        if (model == null) return;

        // USE SWINGWORKER TO PREVENT FREEZING
        new SwingWorker<List<DocumentRequest>, Void>() {
            @Override
            protected List<DocumentRequest> doInBackground() throws Exception {
                // BACKGROUND THREAD: Fetch Data
                DocumentRequestDao rd = new DocumentRequestDao();
                return rd.getAllResidentsDocument(status);
            }

            @Override
            protected void done() {
                // UI THREAD: Update Table
                try {
                    List<DocumentRequest> residentsData = get();

                    // 1. Clear Table
                    model.setRowCount(0);

                    // 2. Populate Table
                        if(status.equals("Approved")){
                            for (DocumentRequest req : residentsData) {

                                model.addRow(new Object[]{
                                        req.getRequestId(),
                                        req.getFullName(),
                                        req.getName(),
                                        req.getStatus(),
                                        req.getTotalFee()
                                });
                            }
                        }else{
                            for (DocumentRequest req : residentsData) {
                                if(!isRecent(req.getRequestDate())){
                                    continue;
                                }
                                model.addRow(new Object[]{
                                        req.getRequestId(),
                                        req.getFullName(),
                                        req.getName(),
                                        req.getStatus(),
                                        req.getRequestDate()
                                });
                            }
                        }



                    // 3. Update UI
                    if(paymentTable != null) paymentTable.repaint();

                    // 4. Re-apply filters
                    if (searchField != null && !searchField.getText().isEmpty()) {
                        // applyFilters(); // Ensure you have this method accessible or reimplemented
                        // Simple reimplementation for now if not accessible:
                        if(sorter != null) sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
                        updateRecordCount();
                    } else {
                        updateRecordCount();
                    }

                    updateTotalPaidUnpaidPanel();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private boolean isRecent(Object dateObj) {
        if (dateObj == null) return false;

        try {
            LocalDateTime reqTime = null;

            if (dateObj instanceof java.sql.Timestamp) {
                reqTime = ((java.sql.Timestamp) dateObj).toLocalDateTime();
            }
            else if (dateObj instanceof java.time.LocalDateTime) {
                reqTime = (java.time.LocalDateTime) dateObj;
            }
            else {

                String dateStr = dateObj.toString();

                if (dateStr.contains(".")) dateStr = dateStr.split("\\.")[0];
                dateStr = dateStr.replace("T", " ");

                DateTimeFormatter formatter;
                if (dateStr.length() == 19) {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                } else if (dateStr.length() == 16) {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                } else {
                    return false;
                }

                reqTime = LocalDateTime.parse(dateStr, formatter);
            }

            // 3. Calculate Difference
            if (reqTime != null) {
                long diff = java.time.temporal.ChronoUnit.MINUTES.between(reqTime, LocalDateTime.now());
                // Return true if 0 to 5 minutes old
                return diff >= 0 && diff <= 5;
            }

        } catch (Exception e) {
            // e.printStackTrace(); // Quiet fail
        }
        return false;
    }
    private SystemConfigDAO dao;


    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(20, 20, 20),
                        getWidth(), getHeight(), new Color(50, 50, 50)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));


        // Logo and Title
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 25));
        logoPanel.setOpaque(false);
        logoPanel.setMaximumSize(new Dimension(260, 90));


        dao = new SystemConfigDAO();
        String logoPath = dao.getConfig("logoPath");
        JPanel logoCircle = new JPanel() {
            private Image logoImage = new ImageIcon("resident_photos/"+logoPath).getImage();


            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                int diameter = Math.min(getWidth(), getHeight());


                // Draw circular clipping area
                g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));


                // Draw the logo image scaled to the panel size
                g2.drawImage(logoImage, 0, 0, diameter, diameter, this);


                // Optional: Add a cerulean circular border
                g2.setClip(null);
                g2.setColor(CERULEAN_BLUE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(0, 0, diameter - 1, diameter - 1);


                g2.dispose();
            }


            @Override
            public Dimension getPreferredSize() {
                return new Dimension(45, 45);
            }
        };
        logoCircle.setOpaque(false);


        JLabel titleLabel = new JLabel("Serbisyong Barangay");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));


        logoPanel.add(logoCircle);
        logoPanel.add(titleLabel);


        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(10));


        // Menu Items
        sidebar.add(createMenuItem("personal_info", "Personal Information", false));
        sidebar.add(createMenuItem("dashboard", "Dashboard", true));
        sidebar.add(createMenuItem("financial_reports", "Financial Reports", false));
        sidebar.add(createMenuItem("total", "Total Paid and Unpaid", false));
        sidebar.add(createMenuItem("profile", "Barangay Official Profile", false));


        sidebar.add(Box.createVerticalGlue());


        // Logout button
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 25));
        logoutPanel.setOpaque(false);
        logoutPanel.setMaximumSize(new Dimension(260, 70));


        JPanel logoutButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(220, 53, 69),
                        getWidth(), getHeight(), new Color(200, 35, 51)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2d);
            }
        };
        logoutButton.setOpaque(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));


        JLabel logoutIcon = new JLabel("⊗");
        logoutIcon.setForeground(Color.WHITE);
        logoutIcon.setFont(new Font("Arial", Font.BOLD, 18));


        JLabel logoutText = new JLabel("LOG OUT");
        logoutText.setForeground(Color.WHITE);
        logoutText.setFont(new Font("Arial", Font.BOLD, 13));


        logoutButton.add(logoutIcon);
        logoutButton.add(logoutText);


        logoutButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dispose();
                openMainWindow();
            }
        });


        logoutPanel.add(logoutButton);
        sidebar.add(logoutPanel);


        return sidebar;
    }


    private void openMainWindow() {
        try {
            Class<?> mainClass = Class.forName("org.example.Interface.Main");
            Method main = mainClass.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not open Login (Main).\nMake sure Main.java exists.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    private JPanel createMenuItem(String type, String text, boolean selected) {
        JPanel menuItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                if (selected) {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(80, 80, 80),
                            getWidth(), getHeight(), new Color(100, 100, 100)
                    );
                    g2d.setPaint(gradient);
                } else {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(30, 30, 30),
                            getWidth(), getHeight(), new Color(50, 50, 50)
                    );
                    g2d.setPaint(gradient);
                }
                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g2d);
            }
        };
        menuItem.setMaximumSize(new Dimension(260, 65));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        menuItem.setOpaque(false);


        // Icon panel
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);


                switch(type) {
                    case "personal_info":
                        g2.fillOval(10, 5, 20, 20);
                        g2.fillArc(2, 22, 36, 25, 0, 180);
                        break;
                    case "dashboard":
                        g2.fillRect(8, 5, 24, 30);
                        g2.setColor(new Color(30, 30, 30));
                        g2.drawLine(12, 12, 28, 12);
                        g2.drawLine(12, 18, 28, 18);
                        g2.drawLine(12, 24, 20, 24);
                        break;
                    case "total":
                        g2.fillRect(8, 15, 24, 20);
                        g2.setColor(new Color(100, 100, 100));
                        g2.fillRect(10, 10, 24, 20);
                        g2.setColor(Color.WHITE);
                        g2.fillRect(12, 5, 24, 20);
                        break;
                    case "profile":
                        g2.fillOval(10, 5, 20, 20);
                        g2.fillArc(2, 22, 36, 25, 0, 180);
                        break;
                }
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(40, 40);
            }
        };
        iconPanel.setOpaque(false);


        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 14));


        menuItem.add(iconPanel);
        menuItem.add(textLabel);


        // Click handler to switch tabs
        menuItem.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(contentContainer, type);
                updateSelectedMenuItem(menuItem);
            }


            public void mouseEntered(MouseEvent e) {
                if (!selected) {
                    menuItem.repaint();
                }
            }


            public void mouseExited(MouseEvent e) {
                if (!selected) {
                    menuItem.repaint();
                }
            }
        });


        return menuItem;
    }


    private void updateSelectedMenuItem(JPanel selectedItem) {
        Component[] components = sidebar.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getCursor().getType() == Cursor.HAND_CURSOR &&
                        panel.getMaximumSize() != null &&
                        panel.getMaximumSize().height == 65) {
                    panel.repaint();
                }
            }
        }
        selectedItem.repaint();
    }


    private JPanel createDashboardPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout(0, 0));
        containerPanel.setOpaque(false);


        // Header
        JPanel headerPanel = createHeaderPanel();
        containerPanel.add(headerPanel, BorderLayout.NORTH);


        // Main Content
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), VERY_LIGHT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));


        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));


        // Create buttons with hover effects
        btnPending = createRoundedButton("Pending Payment", new Color(255, 165, 0));
        btnPending.addActionListener(e -> showPendingPayments());


        btnVerified = createRoundedButton("Verified Payment", new Color(0, 128, 0));
        btnVerified.addActionListener(e -> showVerifiedPayments());


        // Set initial state - Pending selected
        setButtonSelected(btnPending, true);
        setButtonSelected(btnVerified, false);


        buttonPanel.add(btnPending);
        buttonPanel.add(btnVerified);


        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(30));


        // Separator Line
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(CERULEAN_BLUE);
        separator.setBackground(CERULEAN_BLUE);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        contentPanel.add(separator);


        contentPanel.add(Box.createVerticalStrut(30));


        // Search Section
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));


        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchLabel.setForeground(Color.DARK_GRAY);


        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBackground(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CERULEAN_BLUE, 1, true), new EmptyBorder(5, 5, 5, 5)));


        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (sorter != null) {
                    if (text.trim().length() == 0) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                    updateRecordCount();
                }
            }
        });


        searchPanel.add(searchLabel);
        searchPanel.add(searchField);


        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));


        // Create tables
        String[] columnNames = {"Request ID","Name", "Types of Documents", "Status","Paid:"};
        String[] columnNames1 = {"Request ID","Name", "Types of Documents", "Status","Date issued:"};
        pendingTableModel = new DefaultTableModel(columnNames1, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        verifiedTableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        loadTableData(pendingTableModel, "Pending");
        loadTableData(verifiedTableModel, "Approved");
        paymentTable = new JTable(pendingTableModel);
        styleTable(paymentTable);


        sorter = new TableRowSorter<>(pendingTableModel);
        paymentTable.setRowSorter(sorter);


        paymentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = paymentTable.getSelectedRow();
                    if (row != -1) {
                        handleRowDoubleClick(row);
                    }
                }
            }
        });


        JScrollPane tableScrollPane = new JScrollPane(paymentTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(CERULEAN_BLUE, 2));
        tableScrollPane.setPreferredSize(new Dimension(1200, 400));
        tableScrollPane.setMaximumSize(new Dimension(1200, 400));
        tableScrollPane.getViewport().setBackground(VERY_LIGHT_BLUE);


        contentPanel.add(tableScrollPane);


        // Record Counter
        contentPanel.add(Box.createVerticalStrut(10));
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setOpaque(false);


        lblRecordCount = new JLabel("Total Records: " + paymentTable.getRowCount());
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        lblRecordCount.setForeground(Color.DARK_GRAY);


        footerPanel.add(lblRecordCount);
        contentPanel.add(footerPanel);


        contentPanel.add(Box.createVerticalStrut(20));


        containerPanel.add(contentPanel, BorderLayout.CENTER);
        return containerPanel;
    }


    private void updateRecordCount() {
        if (lblRecordCount != null) {
            int count = paymentTable.getRowCount();
            lblRecordCount.setText("Total Records: " + count);
        }
    }





    private void styleTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(50);
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(LIGHT_BLUE);
        table.setSelectionForeground(Color.BLACK);
        table.setBackground(VERY_LIGHT_BLUE);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);


        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new GradientHeaderRenderer());
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        header.setBorder(BorderFactory.createEmptyBorder());


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);


        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);


        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0) {
                table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }


    // Custom Table Header Renderer with Cerulean Gradient
    private class GradientHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Arial", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setOpaque(false);
            return this;
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();


            GradientPaint gradient = new GradientPaint(
                    0, 0, CERULEAN_BLUE,
                    getWidth(), getHeight(), DARK_CERULEAN
            );


            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());


            super.paintComponent(g2d);
            g2d.dispose();
        }
    }


    private void showPendingPayments() {
        paymentTable.setModel(pendingTableModel);
        styleTable(paymentTable);
        sorter = new TableRowSorter<>(pendingTableModel);
        paymentTable.setRowSorter(sorter);
        if(searchField != null) searchField.setText("");


        setButtonSelected(btnPending, true);
        setButtonSelected(btnVerified, false);
        showingPending = true;
        updateTableHeader("Pending Payment");
        updateTotalPaidUnpaidPanel();
        updateRecordCount();
        loadTableData(pendingTableModel, "Pending");
    }


    private void showVerifiedPayments() {
        paymentTable.setModel(verifiedTableModel);
        styleTable(paymentTable);


        sorter = new TableRowSorter<>(verifiedTableModel);
        paymentTable.setRowSorter(sorter);
        if(searchField != null) searchField.setText("");


        setButtonSelected(btnPending, false);
        setButtonSelected(btnVerified, true);
        showingPending = false;
        updateTableHeader("Verified Payment");
        updateTotalPaidUnpaidPanel();
        updateRecordCount();
        loadTableData(verifiedTableModel, "Approved");
    }


    private void updateTableHeader(String title) {
        contentContainer.revalidate();
        contentContainer.repaint();
    }


    private void setButtonSelected(JButton button, boolean selected) {
        if (selected) {
            button.setBackground(button == btnPending ? new Color(255, 140, 0) : new Color(0, 100, 0));
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBorder(new EmptyBorder(18, 55, 18, 55));
        } else {
            button.setBackground(button == btnPending ? new Color(255, 165, 0) : new Color(0, 128, 0));
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.setBorder(new EmptyBorder(15, 50, 15, 50));
        }
    }


    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Create gradient for button
                GradientPaint gradient = new GradientPaint(
                        0, 0, getBackground(),
                        getWidth(), getHeight(), getBackground().brighter()
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);


                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(15, 50, 15, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(250, 50));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!((showingPending && button == btnPending) || (!showingPending && button == btnVerified))) {
                    button.setBackground(button == btnPending ? new Color(255, 185, 0) : new Color(0, 148, 0));
                    button.repaint();
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!((showingPending && button == btnPending) || (!showingPending && button == btnVerified))) {
                    button.setBackground(button == btnPending ? new Color(255, 165, 0) : new Color(0, 128, 0));
                    button.repaint();
                }
            }
        });
        return button;
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, CERULEAN_BLUE,
                        getWidth(), 0, DARK_CERULEAN
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setOpaque(false);
        // Remove the RoundedBorder that was creating white borders and replace with cerulean border
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(CERULEAN_BLUE, 2, true),
                new EmptyBorder(25, 40, 25, 40)
        ));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);


        JLabel lblDocumentary = new JLabel("Documentary");
        lblDocumentary.setFont(new Font("Arial", Font.BOLD, 26));
        lblDocumentary.setForeground(Color.WHITE);


        JLabel lblRequest = new JLabel("Request");
        lblRequest.setFont(new Font("Arial", Font.BOLD, 22));
        lblRequest.setForeground(Color.WHITE);


        titlePanel.add(lblDocumentary);
        titlePanel.add(lblRequest);


        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
        String name = staff.getFirstName();
        String data = "";
        if(staff.getSex().equals("Male")){
            data = "Mr.";
        }else{
            data = "Ms.";
        }
        JLabel lblUser = new JLabel("Hi "+data+" "+name);
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblUser.setForeground(Color.WHITE);


        JPanel userIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Gradient for user icon
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), CERULEAN_BLUE
                );
                g2.setPaint(gradient);
                g2.fillOval(0, 0, 45, 45);


                g2.setColor(Color.WHITE);
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


    private JPanel createPlaceholderPanel(String title) {
        JPanel containerPanel = new JPanel(new BorderLayout(0, 0));
        containerPanel.setOpaque(false);


        JPanel headerPanel = createHeaderPanel();
        containerPanel.add(headerPanel, BorderLayout.NORTH);


        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), VERY_LIGHT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(50, 50, 50, 50));


        JLabel label = new JLabel(title + " - Coming Soon");
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.DARK_GRAY);
        contentPanel.add(label);


        containerPanel.add(contentPanel, BorderLayout.CENTER);
        return containerPanel;
    }


    private JPanel createTotalPaidUnpaidPanel() {
        int paidCount = verifiedTableModel.getRowCount();
        int unpaidCount = pendingTableModel.getRowCount();
        return new TotalPaidUnpaidPanel(paidCount, unpaidCount);
    }


    private void updateTotalPaidUnpaidPanel() {
        Component[] components = contentContainer.getComponents();
        for (Component comp : components) {
            if (comp instanceof TotalPaidUnpaidPanel) {
                TotalPaidUnpaidPanel panel = (TotalPaidUnpaidPanel) comp;
                int paidCount = verifiedTableModel.getRowCount();
                int unpaidCount = pendingTableModel.getRowCount();
                panel.updateData(paidCount, unpaidCount);
                break;
            }
        }
    }


    // ... [Rest of the methods remain the same] ...


    private void handleRowDoubleClick(int row) {
        int modelRow = paymentTable.convertRowIndexToModel(row);
        int requestId = Integer.parseInt(paymentTable.getModel().getValueAt(modelRow, 0).toString());
        String name = (String) paymentTable.getValueAt(row, 1);
        String documentType = (String) paymentTable.getValueAt(row, 2);


        boolean isPending = paymentTable.getModel() == pendingTableModel || showingPending;


        if (isPending) {
            boolean confirmed = showLargeConfirmDialog(
                    "Verify Payment",
                    "Verify payment for <b>" + name + "</b>?<br/>Document: " + documentType,
                    false,name,documentType,requestId
            );
            if (confirmed) {
                DocumentRequest doc = rd.findPurposeByFullName(name, requestId);
                UserDataManager.getInstance().setResidentId(doc.getResidentId());
                Object[] rowData = {requestId, name, documentType, "Approved" };


                int staffId = Integer.parseInt(staff.getStaffId());
                staffDAO.documentDecisionByStatus("Approved", UserDataManager.getInstance().getResidentId(), staffId,"Paid",requestId,"confirmed payment");
                systemLogDAO.addLog("Verified Payment", name,staffId);
                DocumentType docType = UserDataManager.getInstance().getDocumentTypeByName(documentType);
                DocumentRequest dao = new ResidentDAO().findDocumentRequestById(requestId);
                UserDataManager.getInstance().addPayment(docType,dao,UserDataManager.getInstance().getCurrentStaff());
                pendingTableModel.removeRow(row);
                verifiedTableModel.addRow(rowData);
                showLargeMessageDialog("Success", "Payment verified successfully!",1,name,documentType,requestId);
                updateTotalPaidUnpaidPanel();
                updateRecordCount();
            }
        } else {
            boolean confirmed = showLargeConfirmDialog(
                    "Unverify Payment",
                    "Unverify payment for <b>" + name + "</b>?<br/>Document: " + documentType,
                    true,name,documentType,requestId
            );
            if (confirmed) {
                Object[] rowData = { requestId,name, documentType, "Pending" };
                DocumentRequest doc = rd.findPurposeByFullName(name, requestId);
                UserDataManager.getInstance().setResidentId(doc.getResidentId());
                int staffId = Integer.parseInt(staff.getStaffId());
                staffDAO.documentDecisionByStatus("Pending", UserDataManager.getInstance().getResidentId(), staffId,"Unpaid",requestId,"Awaiting approval");
                systemLogDAO.addLog("Unverified Payment", name,staffId);
                verifiedTableModel.removeRow(row);
                pendingTableModel.addRow(rowData);
                showLargeMessageDialog("Success", "Payment unverified successfully!",0," ","",requestId);
                updateTotalPaidUnpaidPanel();
                updateRecordCount();
            }
        }
    }
    private static ResidentDAO rd = new ResidentDAO();


    private static SystemLogDAO systemLogDAO = new SystemLogDAO();
    private static StaffDAO staffDAO = new StaffDAO();
    BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();


    private boolean showLargeConfirmDialog(String title, String htmlMessage, boolean destructiveYes, String name, String docType,int requestId) {
        final boolean[] result = {false};
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);


        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        content.setBackground(VERY_LIGHT_BLUE);


        JLabel messageLabel = new JLabel("<html><div style='font-size:14px; width:420px; font-weight:bold; color:#000000;'>" + htmlMessage + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 15));
        messageLabel.setForeground(Color.BLACK);
        content.add(messageLabel, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(VERY_LIGHT_BLUE);


        JButton btnYes = new JButton("Yes");
        btnYes.setFont(new Font("Arial", Font.BOLD, 16));
        btnYes.setPreferredSize(new Dimension(140, 48));
        btnYes.setBackground(destructiveYes ? new Color(220, 53, 69) : new Color(40, 167, 69));
        btnYes.setForeground(Color.BLACK);
        btnYes.setFocusPainted(false);
        btnYes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });


        if(destructiveYes){
            JButton print = new JButton("Print");
            print.setFont(new Font("Arial", Font.BOLD, 16));
            print.setPreferredSize(new Dimension(140, 48));
            print.setBackground(Color.WHITE);
            print.setForeground(Color.BLACK);
            print.setFocusPainted(false);
            buttonPanel.add(print);
            print.addActionListener(e ->   printReceipt(name,docType,requestId));
        }


        JButton btnNo = new JButton("No");
        btnNo.setFont(new Font("Arial", Font.BOLD, 16));
        btnNo.setPreferredSize(new Dimension(140, 48));
        btnNo.setBackground(destructiveYes ? new Color(40, 167, 69) : new Color(220, 53, 69));
        btnNo.setForeground(Color.BLACK);
        btnNo.setFocusPainted(false);
        btnNo.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });
        JButton btnDelete = createRoundedButton("Delete", Color.RED);
        btnDelete.setPreferredSize(new Dimension(100, 45));
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "<html><body><b style='color:red'>WARNING:</b><br>" +
                            "Are you sure you want to delete this request?<br>" +
                            "This action cannot be undone.</body></html>",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {

                new FinancialDAO().deleteRequest(requestId);

                JOptionPane.showMessageDialog(dialog, "Request deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();

            }
        });

        buttonPanel.add(btnYes);
        buttonPanel.add(btnNo);
        content.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getContentPane().add(content);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);


        return result[0];
    }


    private void showLargeMessageDialog(String title, String message, int id,String name, String documentType, int requestId) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);


        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        content.setBackground(VERY_LIGHT_BLUE);


        JLabel messageLabel = new JLabel("<html><div style='font-size:14px; width:420px; text-align:center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        messageLabel.setForeground(Color.BLACK);
        content.add(messageLabel, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(VERY_LIGHT_BLUE);


        JButton ok = new JButton("OK");
        ok.setFont(new Font("Arial", Font.BOLD, 16));
        ok.setPreferredSize(new Dimension(160, 44));
        ok.setBackground(CERULEAN_BLUE);
        ok.setForeground(Color.WHITE);
        ok.setFocusPainted(false);
        ok.addActionListener(e -> dialog.dispose());


        buttonPanel.add(ok);
        content.add(buttonPanel, BorderLayout.SOUTH);
        dialog.getContentPane().add(content);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);


        if(id == 1){

        }
    }


    private static void printReceipt(String residentName, String docType,int requestId) {
        PrinterJob job = PrinterJob.getPrinterJob();
        Payment documentData = new ResidentDAO().findResidentReceiptById(requestId);
        String amount = String.valueOf(documentData.getAmount());
        String orNum = documentData.getOrNumber();
        BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
        String cashier = staff.getFirstName() +" " + staff.getMiddleName() + " "+ staff.getLastName();
        job.setPrintable(new ReceiptPrinter(residentName, docType, amount, orNum, cashier));
        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }


    private void printReceipt1(String residentName, String docType) {
        JDialog receiptDialog = new JDialog(this, "Print Receipt", true);
        receiptDialog.setSize(350, 500);
        receiptDialog.setLayout(new BorderLayout());


        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        receiptArea.setEditable(false);
        receiptArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        receiptArea.setBackground(Color.WHITE);


        DocumentType documentType = UserDataManager.getInstance().getDocumentTypeByName(docType);
        StringBuilder sb = new StringBuilder();
        sb.append("      SERBISYONG BARANGAY      \n");
        sb.append("        OFFICIAL RECEIPT       \n");
        sb.append("-------------------------------\n");
        sb.append("Date: ").append(java.time.LocalDate.now()).append("\n");
        sb.append("Time: ").append(java.time.LocalTime.now().toString().substring(0, 5)).append("\n\n");
        sb.append("Received From:\n");
        sb.append("  ").append(residentName.toUpperCase()).append("\n\n");
        sb.append("Payment For:\n");
        sb.append("  ").append(docType).append("\n\n");
        sb.append("Payment Status:\n");
        sb.append("  PAID / VERIFIED\n");
        sb.append("-------------------------------\n\n");
        BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
        sb.append("        "+staff.getFirstName() + " "+staff.getLastName()+"\n");
        sb.append("      __________________       \n");
        sb.append("       Barangay Treasurer      \n");


        receiptArea.setText(sb.toString());
        receiptDialog.add(new JScrollPane(receiptArea), BorderLayout.CENTER);


        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(VERY_LIGHT_BLUE);


        JButton btnPrint = new JButton("🖨 Print");
        btnPrint.setBackground(CERULEAN_BLUE);
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Arial", Font.BOLD, 14));


        JButton btnClose = new JButton("Close");
        btnClose.setBackground(new Color(220, 53, 69));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Arial", Font.BOLD, 14));


        btnPrint.addActionListener(e -> {
            try {
                boolean complete = receiptArea.print();
                if (complete) {
                    JOptionPane.showMessageDialog(receiptDialog, "Printing Complete", "Success", JOptionPane.INFORMATION_MESSAGE);
                    receiptDialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(receiptDialog, "Printing Failed: " + ex.getMessage());
            }
        });


        btnClose.addActionListener(e -> receiptDialog.dispose());


        btnPanel.add(btnPrint);
        btnPanel.add(btnClose);
        receiptDialog.add(btnPanel, BorderLayout.SOUTH);


        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setVisible(true);
    }


    static class RoundedBorder extends AbstractBorder {
        private int radius;
        private boolean top;
        private boolean bottom;
        RoundedBorder(int radius, boolean top, boolean bottom) {
            this.radius = radius;
            this.top = top;
            this.bottom = bottom;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());
            if (top && bottom) {
                g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
            } else if (top) {
                g2.fillRoundRect(x, y, width - 1, height + radius, radius, radius);
            } else if (bottom) {
                g2.fillRoundRect(x, y - radius, width - 1, height + radius, radius, radius);
            }
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }


            TreasurerDashboard dashboard = new TreasurerDashboard();
            dashboard.setVisible(true);
        });
    }
}

