// AdminDashboard.java - Updated color scheme
package org.example.Admin;

import org.example.Admin.AdminSettings.AdminAssetTab;
import org.example.Admin.AdminSettings.AdminServerConfigTab;
import org.example.Admin.AdminSettings.AdminSystemConfigTab;
import org.example.Admin.AdminSettings.SuperAdminBlotterTab;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import javax.swing.*;
import javax.swing.border.*;

public class AdminDashboard extends JFrame {

    private JPanel contentContainer;
    private CardLayout cardLayout;
    private JPanel sidebar;
    private AdminLogsTab adminLogsTab;
    private AdminRequestTab adminRequestTab;
    private AdminResidentTab adminResidentTab;
    private AdminStaffTab adminStaffTab;
    private AdminSystemConfigTab tab;

    private AdminHouseholdTab adminHouseholdTab;
    private AdminAdministrationTab adminAdministrationTab;
    private AdminAssetTab assetTab;
    private DashboardSwitcher dashboardSwitcher;
    // UPDATED Visual Colors
    private final Color SIDEBAR_BG = new Color(44, 62, 80); // Modern dark blue-gray
    private final Color MENU_HOVER = new Color(52, 73, 94); // Slightly lighter blue-gray for hover
    private final Color CONTENT_BG = new Color(245, 247, 250); // Clean light gray background
    private SuperAdminBlotterTab superAdminBlotterTab;
    public AdminDashboard() {
        setTitle("Barangay System - Super Admin Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);


        setUndecorated(true);
        setLocationRelativeTo(null);

        // Main Container (Sidebar + Content)
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(CONTENT_BG);

        // 1. Initialize Content Area (CardLayout)
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(CONTENT_BG);

        adminResidentTab = new AdminResidentTab();
        contentContainer.add(adminResidentTab, "resident");
        adminStaffTab = new AdminStaffTab();
        contentContainer.add(adminStaffTab, "staff");
        adminRequestTab = new AdminRequestTab();
        contentContainer.add(adminRequestTab, "request");
        adminLogsTab = new AdminLogsTab();
        contentContainer.add(adminLogsTab, "logs");

        adminHouseholdTab = new AdminHouseholdTab();
        contentContainer.add(adminHouseholdTab,"house");
         // 2. Create Sidebar
        tab = new AdminSystemConfigTab();
        contentContainer.add(tab,"settings");
        assetTab = new AdminAssetTab();
        contentContainer.add(assetTab,"asset");
        superAdminBlotterTab = new SuperAdminBlotterTab();
        contentContainer.add(superAdminBlotterTab,"blotter");
        dashboardSwitcher = new DashboardSwitcher();
        contentContainer.add(dashboardSwitcher,"switch");
        adminAdministrationTab = new AdminAdministrationTab();
        contentContainer.add(adminAdministrationTab,"admin");

        sidebar = createSidebar();

        // 3. Add to Main Panel
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(contentContainer, BorderLayout.CENTER);
        JTabbedPane settingsTabs = new JTabbedPane();
        contentContainer.add(new AdminServerConfigTab(), "Server & Database");
        add(mainPanel);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        // Show default tab
        cardLayout.show(contentContainer, "resident");

    }

    private JPanel createSidebar() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(260, 0));

        // --- LOGO HEADER ---
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 30));
        logoPanel.setBackground(SIDEBAR_BG);
        logoPanel.setMaximumSize(new Dimension(260, 100));

        // Draw a simple logo circle
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(52, 152, 219)); // Modern blue brand color
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                g2.drawString("B", 13, 27); // Simple "B" logo
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(40, 40); }
        };
        logoIcon.setOpaque(false);

        JLabel titleLabel = new JLabel("<html><b>Barangay</b><br>Super Admin Portal</html>");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        logoPanel.add(logoIcon);
        logoPanel.add(titleLabel);
        sidebarPanel.add(logoPanel);
        sidebarPanel.add(Box.createVerticalStrut(20));

        // --- MENU ITEMS ---
        // Params: (CardName, Display Text, isSelected)
        sidebarPanel.add(createMenuItem("resident", "Resident Management", true));
        sidebarPanel.add(createMenuItem("staff", "Staff Management", false));
        sidebarPanel.add(createMenuItem("request", "Document Requests", false));
        sidebarPanel.add(createMenuItem("logs", "System Audit Logs", false));
         sidebarPanel.add(createMenuItem("house", "Household Management", false));
        sidebarPanel.add(createMenuItem("asset","Barangay Assets",false));
        sidebarPanel.add(createMenuItem("blotter","Barangay Blotter",false));
        sidebarPanel.add(createMenuItem("admin", "Borrowing records", false));

        sidebarPanel.add(createMenuItem("switch", "View staff interface", false));
        sidebarPanel.add(createMenuItem("settings", "System Settings", false));
        sidebarPanel.add(createMenuItem("Server & Database","Config",false));

        // Spacer to push Logout to bottom
        sidebarPanel.add(Box.createVerticalGlue());

        // --- LOGOUT BUTTON ---
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 20));
        logoutPanel.setBackground(SIDEBAR_BG);
        logoutPanel.setMaximumSize(new Dimension(260, 80));
        logoutPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblLogout = new JLabel("Log Out");
        lblLogout.setForeground(new Color(231, 76, 60));
        lblLogout.setFont(new Font("Arial", Font.BOLD, 15));

        logoutPanel.add(lblLogout);
        logoutPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
                if(confirm == JOptionPane.YES_OPTION) {

                    openMainWindow();
                    dispose();
                }
            }
        });

        sidebarPanel.add(logoutPanel);

        return sidebarPanel;
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

    // Helper to create consistent Menu Buttons
    private JPanel createMenuItem(String cardName, String text, boolean isSelected) {
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15));
        itemPanel.setMaximumSize(new Dimension(260, 55));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Background Color Logic
        if (isSelected) itemPanel.setBackground(MENU_HOVER);
        else itemPanel.setBackground(SIDEBAR_BG);

        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Arial", Font.PLAIN, 14));
        lblText.setForeground(Color.WHITE);
        itemPanel.add(lblText);

        // Hover & Click Events
        itemPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // 1. Switch Content
                cardLayout.show(contentContainer, cardName);


                resetSidebarStyles();
                itemPanel.setBackground(MENU_HOVER);
            }

            public void mouseEntered(MouseEvent e) {
                if (!itemPanel.getBackground().equals(MENU_HOVER)) {
                    itemPanel.setBackground(new Color(52, 73, 94)); // Slight hover effect
                }
            }

            public void mouseExited(MouseEvent e) {
                if (!itemPanel.getBackground().equals(MENU_HOVER)) {
                    itemPanel.setBackground(SIDEBAR_BG);
                }
            }
        });

        return itemPanel;
    }

    // Helper to reset all button colors before highlighting the new one
    private void resetSidebarStyles() {
        for (Component comp : sidebar.getComponents()) {
            if (comp instanceof JPanel && comp.getMaximumSize().height == 55) { // Check by size/type
                comp.setBackground(SIDEBAR_BG);
            }
        }
    }
    private void maximizeFrame() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            new AdminDashboard().setVisible(true);
        });
    }
}