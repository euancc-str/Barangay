package org.example.treasurer;

import org.example.Admin.SystemLogDAO;
import org.example.DocumentRequestDao;
import org.example.Documents.DocumentRequest;
import org.example.Documents.DocumentType;
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
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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

    // --- NEW COMPONENTS (Search & Count) ---
    private JTextField searchField;
    private JLabel lblRecordCount;
    private TableRowSorter<DefaultTableModel> sorter;

    public TreasurerDashboard() {
        setTitle("Documentary Request - Treasurer Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.BLACK);

        // Sidebar
        sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        // Content area with CardLayout
        JPanel contentArea = new JPanel(new BorderLayout(0, 0));
        contentArea.setBackground(new Color(240, 240, 240));
        contentArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(new Color(240, 240, 240));

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

    // ... [Sidebar creation code remains unchanged] ...
    private JPanel createSidebar() {
        // (Keep your existing sidebar code exactly as is)
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.BLACK);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo and Title
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 25));
        logoPanel.setBackground(Color.BLACK);
        logoPanel.setMaximumSize(new Dimension(260, 90));

        JPanel logoCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 150, 100));
                g2.fillOval(0, 0, 45, 45);
                g2.setColor(Color.WHITE);
                g2.drawOval(0, 0, 45, 45);
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
        sidebar.add(createMenuItem("dashboard", "Dashboard", false));
        sidebar.add(createMenuItem("financial_reports", "Financial Reports", true));

        sidebar.add(createMenuItem("total", "Total Paid and Unpaid", false));
        sidebar.add(createMenuItem("profile", "Barangay Official Profile", false));

        sidebar.add(Box.createVerticalGlue());

        // Logout button
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 25));
        logoutPanel.setBackground(Color.BLACK);
        logoutPanel.setMaximumSize(new Dimension(260, 70));

        JPanel logoutButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        logoutButton.setBackground(Color.BLACK);
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
                System.exit(0);
            }
        });

        logoutPanel.add(logoutButton);
        sidebar.add(logoutPanel);

        return sidebar;
    }

    private JPanel createMenuItem(String type, String text, boolean selected) {
        // (Keep your existing menu item code exactly as is)
        JPanel menuItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        menuItem.setMaximumSize(new Dimension(260, 65));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (selected) {
            menuItem.setBackground(new Color(55, 55, 55));
        } else {
            menuItem.setBackground(Color.BLACK);
        }

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
                        g2.setColor(Color.BLACK);
                        g2.drawLine(12, 12, 28, 12);
                        g2.drawLine(12, 18, 28, 18);
                        g2.drawLine(12, 24, 20, 24);
                        break;
                    case "total":
                        g2.fillRect(8, 15, 24, 20);
                        g2.setColor(new Color(150, 150, 150));
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
                if (menuItem.getBackground().equals(Color.BLACK)) {
                    menuItem.setBackground(new Color(35, 35, 35));
                }
            }

            public void mouseExited(MouseEvent e) {
                if (!menuItem.getBackground().equals(new Color(55, 55, 55))) {
                    menuItem.setBackground(Color.BLACK);
                }
            }
        });

        return menuItem;
    }

    private void updateSelectedMenuItem(JPanel selectedItem) {
        // (Keep existing code)
        Component[] components = sidebar.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getCursor().getType() == Cursor.HAND_CURSOR &&
                        panel.getMaximumSize() != null &&
                        panel.getMaximumSize().height == 65) {
                    panel.setBackground(Color.BLACK);
                }
            }
        }
        selectedItem.setBackground(new Color(55, 55, 55));
    }

    private JPanel createDashboardPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout(0, 0));
        containerPanel.setBackground(new Color(240, 240, 240));

        // Header
        JPanel headerPanel = createHeaderPanel();
        containerPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(229, 231, 235));
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setBackground(new Color(229, 231, 235));
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
        separator.setForeground(Color.BLACK);
        separator.setBackground(Color.BLACK);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        contentPanel.add(separator);

        contentPanel.add(Box.createVerticalStrut(30));

        // --- START OF NEW SEARCH SECTION ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(229, 231, 235));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 5, 5, 5)));

        // --- SEARCH LISTENER (Added) ---
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (sorter != null) {
                    if (text.trim().length() == 0) {
                        sorter.setRowFilter(null);
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                    updateRecordCount(); // Calls the counter
                }
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        // --- END OF NEW SEARCH SECTION ---

        // Create tables
        String[] columnNames = {"Request ID","Name", "Types of Documents", "Status"};
        Object[][] pendingData = getDocumentData("Pending");
        Object[][] verifiedData = getDocumentData("Approved");

        pendingTableModel = new DefaultTableModel(pendingData, columnNames) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        verifiedTableModel = new DefaultTableModel(verifiedData, columnNames) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        paymentTable = new JTable(pendingTableModel);
        styleTable(paymentTable);

        // --- ADD SORTER (For Search to work) ---
        sorter = new TableRowSorter<>(pendingTableModel);
        paymentTable.setRowSorter(sorter);

        paymentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = paymentTable.getSelectedRow();
                    if (row != -1) {
                        // Fix index for filtered views
                        handleRowDoubleClick(row);
                    }
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(paymentTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        tableScrollPane.setPreferredSize(new Dimension(1200, 400));
        tableScrollPane.setMaximumSize(new Dimension(1200, 400));

        contentPanel.add(tableScrollPane);

        // --- START OF NEW RECORD COUNTER ---
        contentPanel.add(Box.createVerticalStrut(10));
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(new Color(229, 231, 235));

        lblRecordCount = new JLabel("Total Records: " + paymentTable.getRowCount());
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));

        footerPanel.add(lblRecordCount);
        contentPanel.add(footerPanel);
        // --- END OF NEW RECORD COUNTER ---

        contentPanel.add(Box.createVerticalStrut(20));

        containerPanel.add(contentPanel, BorderLayout.CENTER);
        return containerPanel;
    }

    // --- NEW HELPER METHOD FOR UPDATING COUNT ---
    private void updateRecordCount() {
        if (lblRecordCount != null) {
            int count = paymentTable.getRowCount(); // Gets visible count
            lblRecordCount.setText("Total Records: " + count);
        }
    }

    private Object[][] getDocumentData(String status) {
        DocumentRequestDao rd = new DocumentRequestDao();
        List<DocumentRequest> residentsData = rd.getAllResidentsDocument(status);

        if (residentsData == null || residentsData.isEmpty()) {
            return new Object[0][4];
        }

        Object[][] pendingData = new Object[residentsData.size()][4];
        for (int i = 0; i < residentsData.size(); i++) {
            DocumentRequest req = residentsData.get(i);
            pendingData[i][0] = req.getRequestId();
            pendingData[i][1] = req.getFullName();
            pendingData[i][2] = req.getName();
            pendingData[i][3] = req.getStatus();
        }
        return pendingData;
    }

    private void styleTable(JTable table) {
        // (Keep existing styling)
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(50);
        table.setGridColor(new Color(200, 200, 200));
        table.setSelectionBackground(new Color(200, 240, 240));
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 16));
        header.setOpaque(true);
        header.setBackground(new Color(240, 240, 240));
        header.setForeground(Color.BLACK);
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

    private void showPendingPayments() {
        paymentTable.setModel(pendingTableModel);
        styleTable(paymentTable);
        sorter = new TableRowSorter<>(pendingTableModel);
        paymentTable.setRowSorter(sorter);
        if(searchField != null) searchField.setText(""); // Clear search

        setButtonSelected(btnPending, true);
        setButtonSelected(btnVerified, false);
        showingPending = true;
        updateTableHeader("Pending Payment");
        updateTotalPaidUnpaidPanel();
        updateRecordCount();
    }

    private void showVerifiedPayments() {
        paymentTable.setModel(verifiedTableModel);
        styleTable(paymentTable);

        // IMPORTANT: Reset Sorter for Verified Model
        sorter = new TableRowSorter<>(verifiedTableModel);
        paymentTable.setRowSorter(sorter);
        if(searchField != null) searchField.setText(""); // Clear search

        setButtonSelected(btnPending, false);
        setButtonSelected(btnVerified, true);
        showingPending = false;
        updateTableHeader("Verified Payment");
        updateTotalPaidUnpaidPanel();
        updateRecordCount();
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
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!((showingPending && button == btnPending) || (!showingPending && button == btnVerified))) {
                    button.setBackground(button == btnPending ? new Color(255, 165, 0) : new Color(0, 128, 0));
                }
            }
        });
        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.BLACK);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(30, true, false),
                new EmptyBorder(25, 40, 25, 40)
        ));
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.BLACK);
        JLabel lblDocumentary = new JLabel("Documentary");
        lblDocumentary.setFont(new Font("Arial", Font.BOLD, 26));
        lblDocumentary.setForeground(Color.WHITE);
        JLabel lblRequest = new JLabel("Request");
        lblRequest.setFont(new Font("Arial", Font.BOLD, 22));
        lblRequest.setForeground(Color.WHITE);
        titlePanel.add(lblDocumentary);
        titlePanel.add(lblRequest);
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(Color.BLACK);
        JLabel lblUser = new JLabel("Hi Ms. Anduja");
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
                g2.setColor(Color.BLACK);
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
        containerPanel.setBackground(new Color(240, 240, 240));
        JPanel headerPanel = createHeaderPanel();
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        JLabel label = new JLabel(title + " - Coming Soon");
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(new Color(100, 100, 100));
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
                    false,name,documentType
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
                showLargeMessageDialog("Success", "Payment verified successfully!",1,name,documentType);
                updateTotalPaidUnpaidPanel();
                updateRecordCount(); // Update count after move
            }
        } else {
            boolean confirmed = showLargeConfirmDialog(
                    "Unverify Payment",
                    "Unverify payment for <b>" + name + "</b>?<br/>Document: " + documentType,
                    true,name,documentType
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
                showLargeMessageDialog("Success", "Payment unverified successfully!",0," ","");
                updateTotalPaidUnpaidPanel();
                updateRecordCount(); // Update count after move
            }
        }
    }
    private static ResidentDAO rd = new ResidentDAO();

    private static SystemLogDAO systemLogDAO = new SystemLogDAO();
    private static StaffDAO staffDAO = new StaffDAO();
    BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
    private boolean showLargeConfirmDialog(String title, String htmlMessage, boolean destructiveYes, String name, String docType) {
        final boolean[] result = {false};
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel messageLabel = new JLabel("<html><div style='font-size:14px; width:420px; font-weight:bold; color:#000000;'>" + htmlMessage + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 15));
        messageLabel.setForeground(Color.BLACK);
        content.add(messageLabel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
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
            print.addActionListener(e ->   printReceipt(name,docType));
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

    private void showLargeMessageDialog(String title, String message, int id,String name, String documentType) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel messageLabel = new JLabel("<html><div style='font-size:14px; width:420px; text-align:center;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        messageLabel.setForeground(Color.BLACK);
        content.add(messageLabel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton ok = new JButton("OK");

        ok.setFont(new Font("Arial", Font.BOLD, 16));
        ok.setPreferredSize(new Dimension(160, 44));
        ok.setBackground(new Color(0, 123, 255));
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
            printReceipt(name,documentType);
        }
    }
    private static void printReceipt(String residentName, String docType) {
        // 1. Setup the print job
        PrinterJob job = PrinterJob.getPrinterJob();

        // 2. Create your receipt object with real data
        DocumentType documentData = UserDataManager.getInstance().getDocumentTypeByName(docType);
        // (You can fetch the fee amount from your DB/DAO if needed)

        String amount = String.valueOf(documentData.getFee());;
        String orNum = "OR-" + System.currentTimeMillis(); // Generate or fetch
        BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
        String cashier = staff.getFirstName() +" " + staff.getMiddleName() + " "+ staff.getLastName(); // Or UserDataManager.getCurrentStaff().getName()

        // 3. Pass it to the printer
        job.setPrintable(new ReceiptPrinter(residentName, docType, amount, orNum, cashier));

        // 4. Show Print Dialog
        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print(); // This opens the "Save as PDF" or selects printer
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }
    private void printReceipt1(String residentName, String docType) {
        // 1. Setup the print job
        JDialog receiptDialog = new JDialog(this, "Print Receipt", true);
        receiptDialog.setSize(350, 500);
        receiptDialog.setLayout(new BorderLayout());

        // 2. Create the Receipt Content (Text Area)
        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(new Font("Monospaced", Font.BOLD, 12));
        receiptArea.setEditable(false);
        receiptArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 3. Build the Receipt Text
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
        btnPanel.setBackground(Color.WHITE);

        JButton btnPrint = new JButton("🖨 Print");
        btnPrint.setBackground(new Color(40, 167, 69)); // Green
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Arial", Font.BOLD, 14));

        JButton btnClose = new JButton("Close");
        btnClose.setBackground(new Color(220, 53, 69)); // Red
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Arial", Font.BOLD, 14));

        // 5. Print Action
        btnPrint.addActionListener(e -> {
            try {
                // This opens the system print dialog
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

        // 6. Show it
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