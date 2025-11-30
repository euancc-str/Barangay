package org.example.Captain;

import org.example.Admin.AdminDashboard;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.StaffDAO;
import org.example.Users.BarangayStaff;
import org.example.treasurer.TreasurerReportsTab;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.lang.reflect.Method;

public class CaptainDashboard extends JFrame {

    private JPanel contentContainer;
    private CardLayout cardLayout;
    private JPanel sidebar;
    private TreasurerReportsTab treasurerReportsTab;

    public CaptainDashboard() {
        setTitle("Documentary Request - Captain Dashboard");
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
        contentArea.setBackground(new Color(229, 231, 235));
        contentArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(new Color(229, 231, 235));

        // Add different panels
        art = new ApprovedRequestTab();
        contentContainer.add(new PersonalInformation(), "personal_info");
        contentContainer.add(art, "approved");
        treasurerReportsTab = new TreasurerReportsTab();
        contentContainer.add(treasurerReportsTab, "total");
        contentContainer.add(new BarangayOfficialProfile(), "profile");
        String population = ""+new StaffDAO().countPopulationFromDb();
        contentContainer.add(createPlaceholderPanel("Barangay Population: " + population), "population");

        contentArea.add(contentContainer, BorderLayout.CENTER);
        mainPanel.add(contentArea, BorderLayout.CENTER);

        add(mainPanel);

        // Show personal info by default
        cardLayout.show(contentContainer, "total");
    }
    private SystemConfigDAO dao;

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.BLACK);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo and Title
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 25));
        logoPanel.setBackground(Color.BLACK);
        logoPanel.setMaximumSize(new Dimension(260, 90));

        dao = new SystemConfigDAO();
        String logoPath = dao.getConfig("logoPath");
        JPanel logoCircle = new JPanel() {

            private Image logoImage = new ImageIcon("resident_photos/"+logoPath).getImage(); // 🔹 path to your logo image

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int diameter = Math.min(getWidth(), getHeight());

                // 🟢 Draw circular clipping area
                g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));

                // 🖼️ Draw the logo image scaled to the panel size
                g2.drawImage(logoImage, 0, 0, diameter, diameter, this);

                // ⚪ Optional: Add a white circular border
                g2.setClip(null);
                g2.setColor(Color.WHITE);
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
        sidebar.add(createMenuItem("approved", "Approved Request", false));
        sidebar.add(createMenuItem("total", "Total Request", true));
        sidebar.add(createMenuItem("profile", "Barangay Official Profile", false));
        sidebar.add(createMenuItem("population", "Barangay Population", false));

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
                int choice = JOptionPane.showConfirmDialog(
                        CaptainDashboard.this,  // Use 'this' to reference the current frame
                        "Are you sure you want to log out?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    // Logout and return to Main
                    org.example.UserDataManager.getInstance().logout();
                    dispose(); // Close current window
                    openMainWindow();
                }
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
    private ApprovedRequestTab art;
    private JPanel createMenuItem(String type, String text, boolean selected) {
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
                    case "approved":
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
                        g2.setStroke(new BasicStroke(3));
                        g2.drawPolyline(new int[]{5, 15, 35}, new int[]{20, 30, 10}, 3);
                        break;
                    case "population":
                        g2.fillOval(5, 5, 12, 12);
                        g2.fillOval(23, 5, 12, 12);
                        g2.fillArc(0, 18, 22, 20, 0, 180);
                        g2.fillArc(18, 18, 22, 20, 0, 180);
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
                art.loadData();
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

    private JPanel createPlaceholderPanel(String title) {
        JPanel containerPanel = new JPanel(new BorderLayout(0, 0));
        containerPanel.setBackground(new Color(229, 231, 235));

        JPanel headerPanel = createHeaderPanel();
        containerPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(new Color(100, 100, 100));
        contentPanel.add(label);

        containerPanel.add(contentPanel, BorderLayout.CENTER);

        return containerPanel;
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(30, true, false),
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
        BarangayStaff staff = new StaffDAO().findStaffByPosition("Brgy.Captain");

        JLabel lblUser = new JLabel("Hi Mr. "+staff.getFirstName());
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

    // Custom rounded border class
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

            CaptainDashboard dashboard = new CaptainDashboard();
            dashboard.setVisible(true);
        });
    }
}