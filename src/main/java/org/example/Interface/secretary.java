package org.example.Interface;


import org.example.Admin.AdminAdministrationTab;
import org.example.Admin.AdminBlotterTab;
import org.example.Admin.AdminBusinessTab;
import org.example.Admin.AdminHouseholdTab;
import org.example.Admin.AdminSettings.AdminAssetBorrowingTab;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Captain.PersonalInformation;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;
import org.example.utils.ResourceUtils;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;


public class secretary extends JFrame {


    private JPanel contentContainer;
    private CardLayout cardLayout;
    private JPanel sidebar;
    private Map<String, JPanel> sidebarMenuItems = new ConcurrentHashMap<>();
    private Map<String, Boolean> notificationBadges = new ConcurrentHashMap<>();
    private javax.swing.Timer notificationTimer;
    private int lastPaidCount = -1;

    private JLabel profilePictureLabel;
    private JLabel headerProfilePicture;
    private String profilePictureBase64 = "";
    private AdminHouseholdTab tab;
    private AdminBlotterTab adminBlotterTab;

    // ===== AUTO-SAVE FIELDS =====
    private Properties profileProperties = new Properties();
    private static final String PROPERTIES_FILE = "profiles/secretary.properties";


    // Form field references for auto-save
    private JTextField txtFirstName, txtMiddleName, txtLastName, txtSuffix, txtCitizenship;
    private JTextField txtPosition, txtAddress, txtPhone, txtEmail, txtUniqueId, txtIdType, txtAge;
    private JTextField dayField, yearField;
    private JComboBox<String> cmbSex, cmbStatus, monthBox;


    private DashboardPanel dashboardPanel;
    private TotalRequestPanel totalRequestPanel;
    private SecretaryPerformSearch secretaryPerformSearch;
    private JLabel personalInfoGreetingLabel; // Add this field
    private AdminAssetBorrowingTab borrowingTab;
    private final Color CONTENT_BG = new Color(245, 247, 250); // Clean light gray background

    private SecretaryPrintDocument secretaryPrintDocument;
    private AdminBusinessTab adminBusinessTab;

    private JPanel createLoadingPanel(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Loading spinner
        JLabel spinner = new JLabel("⟳");
        spinner.setFont(new Font("Arial", Font.BOLD, 48));
        spinner.setForeground(new Color(100, 149, 237));
        panel.add(spinner, gbc);

        // Loading message
        gbc.gridy = 1;
        JLabel loading = new JLabel(message);
        loading.setFont(new Font("Arial", Font.PLAIN, 16));
        loading.setForeground(Color.GRAY);
        panel.add(loading, gbc);

        // Animate spinner
        javax.swing.Timer timer = new javax.swing.Timer(100, e -> {
            spinner.setText(spinner.getText().equals("⟳") ? "⟲" : "⟳");
        });
        timer.start();

        return panel;
    }
    private void lazyLoadPanel(String panelName) {
        // Check if already loaded
        switch (panelName) {
            case "total":
                if (!totalRequestLoaded) {
                    loadTotalRequestPanel();
                }
                break;
            case "secretary":
                if (!secretarySearchLoaded) {
                    loadSecretarySearchPanel();
                }
                break;
            case "document":
                if (!printDocumentLoaded) {
                    loadPrintDocumentPanel();
                }
                break;
            case "tab":
                if (!householdTabLoaded) {
                    loadHouseholdPanel();
                }
                break;
            case "blotter":
                if (!blotterTabLoaded) {
                    loadBlotterPanel();
                }
                break;
            case "business":
                if (!businessTabLoaded) {
                    loadBusinessPanel();
                }
                break;
            case "borrowingTab":
                if (!borrowingTabLoaded) {
                    loadBorrowingPanel();
                }
                break;
        }
    }
    private void loadTotalRequestPanel() {
        new SwingWorker<TotalRequestPanel, Void>() {
            @Override
            protected TotalRequestPanel doInBackground() {
                return new TotalRequestPanel();
            }

            @Override
            protected void done() {
                try {
                    totalRequestPanel = get();
                    contentContainer.remove(getComponentByName("total"));
                    contentContainer.add(totalRequestPanel, "total");
                    cardLayout.show(contentContainer, "total");
                    totalRequestLoaded = true;
                    System.out.println("✅ Total Request Panel loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    private void loadDashboardPanel() {
        new SwingWorker<DashboardPanel, Void>() {
            @Override
            protected DashboardPanel doInBackground() {
                return new DashboardPanel();
            }

            @Override
            protected void done() {
                try {
                    dashboardPanel = get();
                    contentContainer.remove(getComponentByName("dashboard"));
                    contentContainer.add(dashboardPanel, "dashboard");
                    cardLayout.show(contentContainer, "dashboard");
                    dashboardLoaded = true;
                    System.out.println("✅ Secretary Dashboard Panel loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }



    private void loadSecretarySearchPanel() {
        new SwingWorker<SecretaryPerformSearch, Void>() {
            @Override
            protected SecretaryPerformSearch doInBackground() {
                return new SecretaryPerformSearch();
            }

            @Override
            protected void done() {
                try {
                    secretaryPerformSearch = get();
                    contentContainer.remove(getComponentByName("secretary"));
                    contentContainer.add(secretaryPerformSearch, "secretary");
                    cardLayout.show(contentContainer, "secretary");
                    secretarySearchLoaded = true;
                    System.out.println("✅ Secretary Search Panel loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadPrintDocumentPanel() {
        new SwingWorker<SecretaryPrintDocument, Void>() {
            @Override
            protected SecretaryPrintDocument doInBackground() {
                return new SecretaryPrintDocument();
            }

            @Override
            protected void done() {
                try {
                    secretaryPrintDocument = get();
                    contentContainer.remove(getComponentByName("document"));
                    contentContainer.add(secretaryPrintDocument, "document");
                    cardLayout.show(contentContainer, "document");
                    printDocumentLoaded = true;
                    System.out.println("✅ Print Document Panel loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadHouseholdPanel() {
        new SwingWorker<AdminHouseholdTab, Void>() {
            @Override
            protected AdminHouseholdTab doInBackground() {
                return new AdminHouseholdTab();
            }

            @Override
            protected void done() {
                try {
                    tab = get();
                    contentContainer.remove(getComponentByName("tab"));
                    contentContainer.add(tab, "tab");
                    cardLayout.show(contentContainer, "tab");
                    householdTabLoaded = true;
                    System.out.println("✅ Household Panel loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadBlotterPanel() {
        new SwingWorker<AdminBlotterTab, Void>() {
            @Override
            protected AdminBlotterTab doInBackground() {
                return new AdminBlotterTab();
            }

            @Override
            protected void done() {
                try {
                    blotter = get();
                    contentContainer.remove(getComponentByName("blotter"));
                    contentContainer.add(blotter, "blotter");
                    cardLayout.show(contentContainer, "blotter");
                    blotterTabLoaded = true;
                    System.out.println("✅ Blotter Panel loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadBusinessPanel() {
        new SwingWorker<AdminBusinessTab, Void>() {
            @Override
            protected AdminBusinessTab doInBackground() {
                return new AdminBusinessTab();
            }

            @Override
            protected void done() {
                try {
                    adminBusinessTab = get();
                    contentContainer.remove(getComponentByName("business"));
                    contentContainer.add(adminBusinessTab, "business");
                    cardLayout.show(contentContainer, "business");
                    businessTabLoaded = true;
                    System.out.println("✅ Business Panel loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadBorrowingPanel() {
        new SwingWorker<AdminAssetBorrowingTab, Void>() {
            @Override
            protected AdminAssetBorrowingTab doInBackground() {
                return new AdminAssetBorrowingTab();
            }

            @Override
            protected void done() {
                try {
                    borrowingTab = get();
                    contentContainer.remove(getComponentByName("borrowingTab"));
                    contentContainer.add(borrowingTab, "borrowingTab");
                    cardLayout.show(contentContainer, "borrowingTab");
                    borrowingTabLoaded = true;
                    System.out.println("✅ Borrowing Panel loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    private Component getComponentByName(String name) {
        for (Component comp : contentContainer.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(name)) {
                return comp;
            }
        }
        return contentContainer.getComponent(0); // Fallback
    }
    public secretary() {
        setTitle("Serbisyong Barangay - Secretary Dashboard");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(CONTENT_BG);

        // 1. Setup Layout & Sidebar (Fast)
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(CONTENT_BG);
        sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        JPanel contentArea = new JPanel(new BorderLayout(0, 0));
        contentArea.setBackground(new Color(229, 231, 235));
        contentArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(new Color(229, 231, 235));

        // ✅ FIX: Remove wrapper - add dashboard directly
        JPanel dashboardLoading = createLoadingPanel("Loading dashboard...");
        dashboardLoading.setName("dashboard");
        contentContainer.add(dashboardLoading, "dashboard");

        // Add personal info

        // ✅ Add placeholder panels for heavy content with names (7 loading panels + 2 immediate panels = 9 total)
        JPanel totalLoading = createLoadingPanel("Loading Total Requests...");
        totalLoading.setName("total");
        contentContainer.add(totalLoading, "total");

        JPanel secretaryLoading = createLoadingPanel("Loading Search...");
        secretaryLoading.setName("secretary");
        contentContainer.add(secretaryLoading, "secretary");

        JPanel documentLoading = createLoadingPanel("Loading Print...");
        documentLoading.setName("document");
        contentContainer.add(documentLoading, "document");

        JPanel tabLoading = createLoadingPanel("Loading Households...");
        tabLoading.setName("tab");
        contentContainer.add(tabLoading, "tab");

        JPanel blotterLoading = createLoadingPanel("Loading Blotter...");
        blotterLoading.setName("blotter");
        contentContainer.add(blotterLoading, "blotter");

        JPanel businessLoading = createLoadingPanel("Loading Business...");
        businessLoading.setName("business");
        contentContainer.add(businessLoading, "business");

        JPanel borrowingLoading = createLoadingPanel("Loading Borrowing...");
        borrowingLoading.setName("borrowingTab");
        contentContainer.add(borrowingLoading, "borrowingTab");

        contentArea.add(contentContainer, BorderLayout.CENTER);
        mainPanel.add(contentArea, BorderLayout.CENTER);
        add(mainPanel);

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
        startBackgroundLoading();
        startNotificationService();
        // ✅ Load user data immediately (lightweight)


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }
    // === NEW METHOD: Create a simple loading placeholder ===
    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel loading = new JLabel("Loading data, please wait...", SwingConstants.CENTER);
        loading.setFont(new Font("Arial", Font.PLAIN, 16));
        loading.setForeground(Color.GRAY);
        panel.add(loading, BorderLayout.CENTER);
        return panel;
    }
    private boolean dashboardLoaded = false;
    private boolean totalRequestLoaded = false;
    private boolean secretarySearchLoaded = false;
    private boolean printDocumentLoaded = false;
    private boolean householdTabLoaded = false;
    private boolean blotterTabLoaded = false;
    private boolean businessTabLoaded = false;
    private boolean borrowingTabLoaded = false;

    private AdminBlotterTab blotter;
    private void startBackgroundLoading() {
        // Load Dashboard FIRST (highest priority)
        new SwingWorker<DashboardPanel, Void>() {
            @Override
            protected DashboardPanel doInBackground() {
                return new DashboardPanel();
            }

            @Override
            protected void done() {
                try {
                    DashboardPanel loadedDashboard = get();
                    Component loadingPanel = getComponentByName("dashboard");
                    if (loadingPanel != null) {
                        contentContainer.remove(loadingPanel);
                    }
                    loadedDashboard.setName("dashboard");
                    contentContainer.add(loadedDashboard, "dashboard");
                    dashboardPanel = loadedDashboard;
                    dashboardLoaded = true;
                    cardLayout.show(contentContainer, "dashboard");
                    System.out.println("✅ Dashboard loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();


        loadPanelAsync("total", () -> new TotalRequestPanel());
        loadPanelAsync("secretary", () -> new SecretaryPerformSearch());
        loadPanelAsync("document", () -> new SecretaryPrintDocument());
        loadPanelAsync("tab", () -> new AdminHouseholdTab());
        loadPanelAsync("blotter", () -> new AdminBlotterTab());
        loadPanelAsync("business", () -> new AdminBusinessTab());
        loadPanelAsync("borrowingTab", () -> new AdminAssetBorrowingTab());
    }

    // Generic async loader
    private <T extends JPanel> void loadPanelAsync(String name, java.util.function.Supplier<T> panelSupplier) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return panelSupplier.get();
            }

            @Override
            protected void done() {
                try {
                    T panel = get();
                    contentContainer.add(panel, name);

                    // Update loaded flags
                    switch(name) {
                        case "total":
                            totalRequestPanel = (TotalRequestPanel) panel;
                            totalRequestLoaded = true;
                            break;
                        case "secretary":
                            secretaryPerformSearch = (SecretaryPerformSearch) panel;
                            secretarySearchLoaded = true;
                            break;
                        case "document":
                            secretaryPrintDocument = (SecretaryPrintDocument) panel;
                            printDocumentLoaded = true;
                            break;
                        case "tab":
                            tab = (AdminHouseholdTab) panel;
                            householdTabLoaded = true;
                            break;
                        case "blotter":
                            blotter = (AdminBlotterTab) panel;
                            blotterTabLoaded = true;
                            break;
                        case "business":
                            adminBusinessTab = (AdminBusinessTab) panel;
                            businessTabLoaded = true;
                            break;
                        case "borrowingTab":
                            borrowingTab = (AdminAssetBorrowingTab) panel;
                            borrowingTabLoaded = true;
                            break;
                    }
                    System.out.println("✅ " + name + " loaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    private static SystemConfigDAO dao;
    private Image logoImage;
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

        // =================================================================================
        // ✅ NEW: Background Image Loader (Replaces the blocking 'dao' lines)
        // =================================================================================
        JPanel logoCircle = new JPanel() {
            private Image logoImage = null;
            private boolean isLoading = true;

            // This block runs AUTOMATICALLY when the panel is created
            {
                new SwingWorker<Image, Void>() {
                    @Override
                    protected Image doInBackground() throws Exception {
                        // 1. WE CONNECT TO DATABASE HERE (So the main app doesn't freeze)
                        SystemConfigDAO bgDao = new SystemConfigDAO();
                        String path = bgDao.getLogoPath(); // We get the path here safely!

                        // 2. Load the image
                        ImageIcon originalIcon = new ImageIcon(path);
                        if (originalIcon.getIconWidth() > 0) {
                            return originalIcon.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            logoImage = get(); // Get the finished image
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            isLoading = false;
                            repaint(); // Show the image now that it's ready
                        }
                    }
                }.execute();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int diameter = Math.min(getWidth(), getHeight());
                g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));

                if (logoImage != null) {
                    // Draw the image we loaded in the background
                    g2.drawImage(logoImage, 0, 0, diameter, diameter, this);
                } else {
                    // Draw placeholder while waiting
                    g2.setColor(new Color(50, 50, 50));
                    g2.fillRect(0, 0, diameter, diameter);

                    if (isLoading) {
                        g2.setColor(Color.GRAY);
                        g2.setFont(new Font("Arial", Font.BOLD, 10));
                        g2.drawString("...", diameter/2 - 5, diameter/2 + 5);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                        g2.drawString("👤", 10, 30);
                    }
                }

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
        // =================================================================================

        JLabel titleLabel = new JLabel("Serbisyong Barangay");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));

        logoPanel.add(logoCircle);
        logoPanel.add(titleLabel);

        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(10));

        // Menu Items
        sidebar.add(createMenuItem("dashboard", "Dashboard", true));
        sidebar.add(createMenuItem("total", "Total Request", false));
        sidebar.add(createMenuItem("tab","Household Management",false));
        sidebar.add(createMenuItem("secretary","Search/Document Request",false));
        sidebar.add(createMenuItem("document","Print",false));
        sidebar.add(createMenuItem("blotter","Blotter tab",false));
        sidebar.add(createMenuItem("business","Business Establishments",false));
        sidebar.add(createMenuItem("borrowingTab","Property & equipment",false));

        // ✅ SAFE ADMIN CHECK (Replaces the blocking check)
        SwingUtilities.invokeLater(() -> {
            BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();
            if (currentStaff != null && "Admin".equals(currentStaff.getPosition())) {
                sidebar.add(createMenuItem("admin_view", "Admin Dashboard", false));
                sidebar.revalidate(); // Refresh sidebar to show the new button
                sidebar.repaint();
            }
        });

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
            Color originalColor = Color.BLACK;
            Color hoverColor = new Color(200, 0, 0);

            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(originalColor);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        secretary.this,
                        "Are you sure you want to log out?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    UserDataManager.getInstance().logout();
                    openMainWindow(secretary.this);
                    dispose();
                }
            }
        });

        logoutPanel.add(logoutButton);
        sidebar.add(logoutPanel);

        return sidebar;
    }
    private static void openMainWindow(Window current) {
        try {
            if (current != null) {
                current.dispose();
            }
            Class<?> mainClass = Class.forName("org.example.Interface.Main");
            Method main = mainClass.getMethod("main", String[].class);
            main.invoke(null, (Object) new String[]{});
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Could not open Login (Main).\nMake sure Main.java exists in org.example.Interface package.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void maximizeFrame() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    private JPanel createMenuItem(String type, String text, boolean selected) {
        JPanel menuItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 18));
        menuItem.setMaximumSize(new Dimension(260, 65));
        menuItem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Save reference for repainting later
        sidebarMenuItems.put(type, menuItem);

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


                switch (type) {
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
                    case "tab": // Household Management (House Icon)
                        int[] xPoints = { 20, 5, 35 };
                        int[] yPoints = { 5, 20, 20 };
                        g2.fillPolygon(xPoints, yPoints, 3);
                        g2.fillRect(10, 20, 20, 15);
                        g2.setColor(Color.BLACK);
                        g2.fillRect(17, 26, 6, 9);
                        break;
                    case "secretary": // Search/Document Request (Search Icon)
                        g2.setStroke(new BasicStroke(2));
                        g2.drawOval(8, 8, 15, 15);
                        g2.drawLine(20, 20, 30, 30);
                        break;
                    case "document": // Print (Printer Icon)
                        g2.fillRect(10, 20, 20, 12); // Printer body
                        g2.fillRect(14, 12, 12, 8); // Top paper
                        g2.setColor(new Color(200, 200, 200));
                        g2.fillRect(14, 28, 12, 8); // Bottom paper output
                        break;
                    case "blotter": // Blotter Tab (Scale/Law Icon)
                        g2.setStroke(new BasicStroke(2));
                        g2.drawLine(20, 10, 20, 30); // Center pillar
                        g2.drawLine(10, 15, 30, 15); // Balance beam
                        g2.drawArc(8, 15, 4, 10, 0, -180); // Left scale
                        g2.drawArc(28, 15, 4, 10, 0, -180); // Right scale
                        break;
                    case "business": // Business Establishments (Shop/Store Icon)
                        g2.fillRect(8, 18, 24, 14); // Storefront
                        g2.fillRect(10, 10, 20, 8); // Roof/Awning
                        g2.setColor(Color.BLACK);
                        g2.fillRect(17, 24, 6, 8); // Door
                        break;
                    case "borrowingTab": // Property & Equipment (Wrench/Tools Icon)
                        g2.setStroke(new BasicStroke(3));
                        g2.drawOval(10, 10, 12, 12); // Wrench head
                        g2.setColor(Color.BLACK);
                        g2.fillRect(14, 14, 6, 4); // Notch in head
                        g2.setColor(Color.WHITE);
                        g2.drawLine(20, 20, 32, 32); // Handle
                        break;
                }
                if (notificationBadges.getOrDefault(type, false)) {
                    g2.setColor(new Color(255, 50, 50)); // Bright Red
                    g2.fillOval(30, 0, 12, 12); // Draw circle at top-right of icon

                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawOval(30, 0, 12, 12); // White border to make it pop
                }

            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(45, 40);
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
                if (type.equals("admin_view")) {
                    JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(menuItem);
                    new Main().openAdminDashboard(UserDataManager.getInstance().getCurrentStaff());
                    if (currentFrame != null) {
                        currentFrame.dispose();
                    }
                    return;
                }


                lazyLoadPanel(type);


                if (isPanelLoaded(type)) {
                    cardLayout.show(contentContainer, type);
                }

                updateSelectedMenuItem(menuItem);
                if (notificationBadges.getOrDefault(type, false)) {
                    notificationBadges.put(type, false); // Clear notification
                    menuItem.repaint();
                }
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
    private void startNotificationService() {
        // Run every 5 seconds (5000 ms)
        notificationTimer = new javax.swing.Timer(5000, e -> checkForUpdates());
        notificationTimer.start();
    }

    private void checkForUpdates() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                String sql = "SELECT COUNT(*) FROM system_logs WHERE actionType = 'Verified Document'";

                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                     java.sql.ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
                return 0;
            }

            @Override
            protected void done() {
                try {
                    int currentCount = get();

                    if (lastPaidCount == -1) {
                        lastPaidCount = currentCount;
                        return;
                    }

                    // If the number of "Verified Document" logs increased, it means a NEW one just happened
                    if (currentCount > lastPaidCount) {


                        triggerBadge("document");

                        // 2. Show Toast Notification
                        showToastNotification("New Payment/Request Update!");

                        // 3. Play a sound (Optional, helpful for attention)
                        java.awt.Toolkit.getDefaultToolkit().beep();

                        // Update tracker so we catch the next one
                        lastPaidCount = currentCount;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
    private void showToastNotification(String message) {
        JWindow toast = new JWindow();
        toast.setBackground(new Color(0, 0, 0, 0)); // Transparent

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(40, 40, 40, 220)); // Semi-transparent black
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lbl = new JLabel("🔔 " + message);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panel.add(lbl);
        toast.add(panel);
        toast.pack();

        // Position at bottom right of screen
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        int x = scr.width - toast.getWidth() - 20;
        int y = scr.height - toast.getHeight() - 50; // Above taskbar
        toast.setLocation(x, y);
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toast.dispose();
            }
        });
        toast.setVisible(true);
        toast.setAlwaysOnTop(true);
        new javax.swing.Timer(10000, e -> {
            toast.dispose();
            ((javax.swing.Timer)e.getSource()).stop();
        }).start();
    }
    private void triggerBadge(String menuName) {
        notificationBadges.put(menuName, true);
        if (sidebarMenuItems.containsKey(menuName)) {
            sidebarMenuItems.get(menuName).repaint(); // Redraws to show red dot
        }
    }
    private boolean isPanelLoaded(String type) {
        switch (type) {
            case "total": return totalRequestLoaded;
            case "secretary": return secretarySearchLoaded;
            case "document": return printDocumentLoaded;

            case "tab": return householdTabLoaded;
            case "blotter": return blotterTabLoaded;
            case "business": return businessTabLoaded;
            case "borrowingTab": return borrowingTabLoaded;
            case "dashboard": return dashboardLoaded;
            case "personal_info":
                return true; // Always loaded
            default: return false;
        }
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






    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 123, 167));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(30, true, false),
                new EmptyBorder(25, 40, 25, 40)
        ));


        JLabel lblHeader = new JLabel("Personal Information");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 26));
        lblHeader.setForeground(Color.WHITE);


        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(new Color(0, 123, 167));


        // Dynamic greeting based on user's last name and gender
        personalInfoGreetingLabel = new JLabel(getGreeting()); // Use field, not local variable
        personalInfoGreetingLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        personalInfoGreetingLabel.setForeground(Color.WHITE);


        // Create profile picture label for header



        userPanel.add(personalInfoGreetingLabel); // Use field here


        headerPanel.add(lblHeader, BorderLayout.WEST);
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


    // ====================================================================
    // AUTO-SAVE FUNCTIONALITY
    // ====================================================================


    private void loadProperties() {
        try {
            profileProperties.load(ResourceUtils.getResourceAsStream(PROPERTIES_FILE));
            System.out.println("Profile data loaded successfully.");
        } catch (IOException ex) {
            System.out.println("No existing profile data file found. Starting fresh.");
        }
    }




    private String getSavedValue(String key) {
        return profileProperties.getProperty(key, "");
    }


    private void setupAutoSave() {
        // Add document listeners to all text fields for auto-save
        addAutoSaveListener(txtFirstName, "firstName");
        addAutoSaveListener(txtMiddleName, "middleName");
        addAutoSaveListener(txtLastName, "lastName");
        addAutoSaveListener(txtSuffix, "suffix");
        addAutoSaveListener(txtCitizenship, "citizenship");
        addAutoSaveListener(txtPosition, "position");
        addAutoSaveListener(txtAddress, "address");
        addAutoSaveListener(txtPhone, "phone");
        addAutoSaveListener(txtEmail, "email");
        addAutoSaveListener(txtUniqueId, "uniqueId");
        addAutoSaveListener(txtIdType, "idType");
        addAutoSaveListener(txtAge, "age");
        addAutoSaveListener(dayField, "birthDay");
        addAutoSaveListener(yearField, "birthYear");


        // Add action listeners to combo boxes for auto-save
        addComboBoxAutoSaveListener(cmbSex, "sex");
        addComboBoxAutoSaveListener(cmbStatus, "status");
        addComboBoxAutoSaveListener(monthBox, "birthMonth");
    }


    private void addAutoSaveListener(JTextField field, String propertyKey) {
        if (field != null) {
            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { saveField(); }
                public void removeUpdate(DocumentEvent e) { saveField(); }
                public void changedUpdate(DocumentEvent e) { saveField(); }


                private void saveField() {
                    profileProperties.setProperty(propertyKey, field.getText());

                }
            });
        }
    }


    private <T> void addComboBoxAutoSaveListener(JComboBox<T> comboBox, String propertyKey) {
        if (comboBox != null) {
            comboBox.addActionListener(e -> {
                if (comboBox.getSelectedItem() != null) {
                    profileProperties.setProperty(propertyKey, comboBox.getSelectedItem().toString());

                }
            });
        }
    }


    private <T> void setComboBoxValue(JComboBox<T> comboBox, String value) {
        if (value != null && !value.isEmpty()) {
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                if (comboBox.getItemAt(i).toString().equalsIgnoreCase(value)) {
                    comboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }


    // Personal Information Panel (example layout)

    static class PhoneDocumentFilter extends javax.swing.text.DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (string == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);
            if (isValidPhone(newText)) super.insertString(fb, offset, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
            if (text == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);
            if (isValidPhone(newText)) super.replace(fb, offset, length, text, attrs);
        }

        private boolean isValidPhone(String text) {
            if (text.isEmpty()) return true;
            if (!text.matches("\\d*")) return false;
            if (text.length() > 11) return false;
            if (text.length() >= 2 && !text.startsWith("09")) return false;
            return true;
        }
    }

    private void confirmExit() {
        // Show confirmation dialog
        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );


        if (option == JOptionPane.YES_OPTION) {
            // Save data and exit

            dispose(); // Close the window
            System.exit(0); // Exit the application
        }
        // If NO, do nothing - window stays open
    }


    // ====================================================================
    // PROFILE PICTURE METHODS
    // ====================================================================


    private void saveProfilePicture(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            profilePictureBase64 = Base64.getEncoder().encodeToString(imageBytes);
            profileProperties.setProperty("profilePicture", profilePictureBase64);

            updateGreetingInAllPanels(); // <-- Add this line!
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving profile picture: " + e.getMessage());
        }
    }





    private void updateProfilePictureInAllPanels() {
        if (!profilePictureBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(profilePictureBase64);
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                BufferedImage image = ImageIO.read(bais);
                ImageIcon icon = new ImageIcon(image.getScaledInstance(45, 45, Image.SCALE_SMOOTH));


                // Update header profile picture
                if (headerProfilePicture != null) {
                    headerProfilePicture.setIcon(icon);
                    headerProfilePicture.setText("");
                }
            } catch (IOException e) {
                System.out.println("Error loading profile picture: " + e.getMessage());
            }
        }
    }


    // ====================================================================
    // NAME AND GENDER METHODS FOR GREETING
    // ====================================================================


    private String getGreeting() {
        String lastName = profileProperties.getProperty("lastName", "");
        String sex = profileProperties.getProperty("sex", "");


        if (lastName.isEmpty()) {
            return "Hi Secretary";
        }


        String title = "Mr.";
        if ("Female".equals(sex)) {
            title = "Mrs.";
        }


        return "Hi " + title + " " + lastName;
    }


    private void updateGreetingInAllPanels() {
        // Update personal info header greeting
        if (personalInfoGreetingLabel != null) {
            personalInfoGreetingLabel.setText(getGreeting());
        }
        // Update personal info header profile picture
        if (headerProfilePicture != null) {
            if (!profilePictureBase64.isEmpty()) {
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(profilePictureBase64);
                    ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                    BufferedImage image = ImageIO.read(bais);
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(45, 45, Image.SCALE_SMOOTH));
                    headerProfilePicture.setIcon(icon);
                    headerProfilePicture.setText("");
                } catch (IOException e) {
                    headerProfilePicture.setText("👤");
                    headerProfilePicture.setIcon(null);
                }
            } else {
                headerProfilePicture.setText("👤");
                headerProfilePicture.setIcon(null);
            }
        }
        // Update dashboard and total request panels


        if (totalRequestPanel != null) totalRequestPanel.refreshHeader();
    }


    private void loadUserDataIntoForm() {


        BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();


        if (currentStaff != null) {
            // Populate fields from staff data
            txtFirstName.setText(currentStaff.getFirstName() != null ? currentStaff.getFirstName() : "");
            txtLastName.setText(currentStaff.getLastName() != null ? currentStaff.getLastName() : "");
            txtPosition.setText(currentStaff.getRole() != null ? currentStaff.getRole() : "");
            txtEmail.setText(currentStaff.getEmail() != null ? currentStaff.getEmail() : "");
            txtPhone.setText(currentStaff.getContactNo() != null ? currentStaff.getContactNo() : "");
            String age = "";
            if(age == null){
                age =""+currentStaff.getAge();
            }else{
                age = "" +(LocalDate.now().getYear() - currentStaff.getDob().getYear());
            }
            txtAge.setText(age);

            txtSuffix.setText(currentStaff.getSuffix());
            cmbSex.setSelectedItem(currentStaff.getSex());
            cmbStatus.setSelectedItem(currentStaff.getCivilStatus());
            txtCitizenship.setText(currentStaff.getCitizenship());

            String uniqueId = ""+currentStaff.getIdNumber();
            txtUniqueId.setText(uniqueId);
            LocalDate dob = currentStaff.getDob();
            if (dob != null) {
                System.out.println("Loading DOB: " + dob); // Debug


                // Set day
                dayField.setText(String.valueOf(dob.getDayOfMonth()));


                // Set year
                yearField.setText(String.valueOf(dob.getYear()));


                // Set month (ComboBox index is 0-based, Month value is 1-based)
                int monthIndex = dob.getMonthValue() - 1; // Use getMonthValue() instead of getMonth()
                if (monthIndex >= 0 && monthIndex < 12) {
                    monthBox.setSelectedIndex(monthIndex);
                }


                System.out.println("Set day: " + dob.getDayOfMonth() + ", month index: " + monthIndex + ", year: " + dob.getYear());
            } else {
                System.out.println("DOB is null!");
            }
            if (currentStaff.getAddress() != null) {
                txtAddress.setText(currentStaff.getAddress());
            }
            if (currentStaff.getMiddleName() != null) {
                txtMiddleName.setText(currentStaff.getMiddleName());
            }


            System.out.println("Loaded staff data: " + currentStaff.getFirstName() + " " + currentStaff.getLastName());
            return;
        }
        Resident currentResident = UserDataManager.getInstance().getCurrentResident();


        if (currentResident != null) {
            // Populate fields from resident data
            txtFirstName.setText(currentResident.getFirstName() != null ? currentResident.getFirstName() : "");
            txtLastName.setText(currentResident.getLastName() != null ? currentResident.getLastName() : "");
            txtEmail.setText(currentResident.getEmail() != null ? currentResident.getEmail() : "");
            txtPhone.setText(currentResident.getContactNo() != null ? currentResident.getContactNo() : "");
            txtAddress.setText(currentResident.getAddress() != null ? currentResident.getAddress() : "");
            txtAge.setText(String.valueOf(currentResident.getAge()));
            txtUniqueId.setText(currentResident.getNationalId() != null ? currentResident.getNationalId() : "");


            if (currentResident.getGender() != null) {
                setComboBoxValue(cmbSex, currentResident.getGender());
            }
            if (currentResident.getVoterStatus() != null) {
                setComboBoxValue(cmbStatus, currentResident.getVoterStatus());
            }
            if (currentResident.getMiddleName() != null) {
                txtMiddleName.setText(currentResident.getMiddleName());
            }


            System.out.println("Loaded resident data: " + currentResident.getFirstName() + " " + currentResident.getLastName());
        } else {
            System.out.println("No user logged in, loading from properties file");
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            secretary dashboard = new secretary();
            dashboard.setVisible(true);
        });
    }
}

