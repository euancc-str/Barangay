package org.example.Interface;

import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

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
import java.util.Properties;
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

     // ===== ADD THESE FOR PROFILE PICTURE =====
    private JLabel profilePictureLabel; // For personal panel form
    private JLabel headerProfilePicture; // For header in all panels
    private String profilePictureBase64 = "";

     // ===== AUTO-SAVE FIELDS =====
    private Properties profileProperties = new Properties();
    private static final String PROPERTIES_FILE = "profileData.properties";
    
    // Form field references for auto-save
    private JTextField txtFirstName, txtMiddleName, txtLastName, txtSuffix, txtCitizenship;
    private JTextField txtPosition, txtAddress, txtPhone, txtEmail, txtUniqueId, txtIdType, txtAge;
    private JTextField dayField, yearField;
    private JComboBox<String> cmbSex, cmbStatus, monthBox;

    private DashboardPanel dashboardPanel;
    private TotalRequestPanel totalRequestPanel;
    private SecretaryPerformSearch secretaryPerformSearch;
    private JLabel personalInfoGreetingLabel; // Add this field

    private SecretaryPrintDocument secretaryPrintDocument;
    public secretary() {

         // Load properties on startup
        loadProperties();
        loadProfilePicture();

        setTitle("Serbisyong Barangay - Secretary Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        dashboardPanel = new DashboardPanel();
        totalRequestPanel = new TotalRequestPanel();
        secretaryPerformSearch = new SecretaryPerformSearch();
        secretaryPrintDocument = new SecretaryPrintDocument();
        contentContainer.add(createPersonalPanel(), "personal_info");
        contentContainer.add(dashboardPanel, "dashboard");
        contentContainer.add(totalRequestPanel, "total");
        contentContainer.add(secretaryPerformSearch, "secretary");
        contentContainer.add(secretaryPrintDocument,"document");


        //contentContainer.add(createPlaceholderPanel("Barangay Official Profile"), "profile");

        contentArea.add(contentContainer, BorderLayout.CENTER);
        mainPanel.add(contentArea, BorderLayout.CENTER);

        add(mainPanel);

        // Show personal info by default
        cardLayout.show(contentContainer, "dashboard");
        SwingUtilities.invokeLater(() -> {
            loadUserDataIntoForm();
        });
        addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            confirmExit();
        }
        });

         // Save properties when window closes
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveProperties();
            }
        });
    }
    private void printPanel(JPanel panelToPrint) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Barangay Document Print");

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics pg, PageFormat pf, int pageNum) {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;

                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());

                // Scale the panel to fit the page
                double scaleX = pf.getImageableWidth() / panelToPrint.getWidth();
                double scaleY = pf.getImageableHeight() / panelToPrint.getHeight();
                double scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
                g2.scale(scale, scale);

                panelToPrint.printAll(g2); // Print the component
                return Printable.PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Printing Failed: " + e.getMessage());
            }
        }
    }

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

            JPanel logoCircle = new JPanel() {
            private Image logoImage = new ImageIcon("src/main/java/org/example/resources/logo.jpg").getImage(); // 🔹 path to your logo image

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
        sidebar.add(createMenuItem("dashboard", "Dashboard", true));
        sidebar.add(createMenuItem("total", "Total Request", false));
        sidebar.add(createMenuItem("profile", "Barangay Official Profile", false));
        sidebar.add(createMenuItem("secretary","Search",false));
        sidebar.add(createMenuItem("document","Print",false));

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

        // Hover effect + confirmation dialog
        logoutButton.addMouseListener(new MouseAdapter() {
            Color originalColor = Color.BLACK;
            Color hoverColor = new Color(200, 0, 0); // red on hover

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
                // Show confirmation dialog centered on screen
                int choice = JOptionPane.showConfirmDialog(
                        secretary.this,  // Reference to the current frame
                        "Are you sure you want to log out?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    // Save data before logout
                    saveProperties();

                    // Clear current user session
                    UserDataManager.getInstance().logout();

                    // Close secretary window
                    dispose();

                    // Open login window
                    openMainWindow(secretary.this);
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
                        g2.setStroke(new BasicStroke(3));
                        g2.drawPolyline(new int[]{5, 15, 35}, new int[]{20, 30, 10}, 3);
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
        headerPanel.setBackground(new Color(40, 40, 40));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(30, true, false),
            new EmptyBorder(25, 40, 25, 40)
        ));

        JLabel lblHeader = new JLabel("Personal Information");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 26));
        lblHeader.setForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(new Color(40, 40, 40));

        // Dynamic greeting based on user's last name and gender
        personalInfoGreetingLabel = new JLabel(getGreeting()); // Use field, not local variable
        personalInfoGreetingLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        personalInfoGreetingLabel.setForeground(Color.WHITE);

        // Create profile picture label for header
        headerProfilePicture = new JLabel();
        headerProfilePicture.setPreferredSize(new Dimension( 60, 60));
        headerProfilePicture.setOpaque(true);
        headerProfilePicture.setBackground(Color.WHITE);
        headerProfilePicture.setBorder(new LineBorder(Color.GRAY, 1, true));
        headerProfilePicture.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Load existing profile picture if available
        if (!profilePictureBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(profilePictureBase64);
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                BufferedImage image = ImageIO.read(bais);
                ImageIcon icon = new ImageIcon(image.getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                headerProfilePicture.setIcon(icon);
            } catch (IOException e) {
                // If there's an error loading, show default icon
                headerProfilePicture.setText("👤");
                headerProfilePicture.setFont(new Font("Arial", Font.PLAIN, 20));
            }
        } else {
            // Show default icon if no profile picture
            headerProfilePicture.setText("👤");
            headerProfilePicture.setFont(new Font("Arial", Font.PLAIN, 20));
        }

        userPanel.add(personalInfoGreetingLabel); // Use field here
        userPanel.add(headerProfilePicture);

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
        try (FileInputStream in = new FileInputStream(PROPERTIES_FILE)) {
            profileProperties.load(in);
            System.out.println("Profile data loaded successfully.");
        } catch (IOException ex) {
            System.out.println("No existing profile data file found. Starting fresh.");
        }
    }

    private void saveProperties() {
        try (FileOutputStream out = new FileOutputStream(PROPERTIES_FILE)) {
            profileProperties.store(out, "User Profile Data Saved on " + new Date());
            System.out.println("Profile data saved successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving profile data: " + ex.getMessage(), 
                "Save Error", JOptionPane.ERROR_MESSAGE);
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
                    saveProperties();
                }
            });
        }
    }

    private <T> void addComboBoxAutoSaveListener(JComboBox<T> comboBox, String propertyKey) {
        if (comboBox != null) {
            comboBox.addActionListener(e -> {
                if (comboBox.getSelectedItem() != null) {
                    profileProperties.setProperty(propertyKey, comboBox.getSelectedItem().toString());
                    saveProperties();
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
    private JPanel createPersonalPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout(0, 0));
        containerPanel.setBackground(new Color(229, 231, 235));

        JPanel headerPanel = createHeaderPanel();
        containerPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font fieldFont = new Font("Arial", Font.PLAIN, 15);

        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblFirstName = new JLabel("First Name");
        lblFirstName.setFont(labelFont);
        formPanel.add(lblFirstName, gbc);
        gbc.gridx = 1;
        txtFirstName = new JTextField(getSavedValue("firstName"), 12);
        txtFirstName.setFont(fieldFont);
        txtFirstName.setPreferredSize(new Dimension(160, 28));
        formPanel.add(txtFirstName, gbc);

        // Middle Name
        gbc.gridx = 2;
        JLabel lblMiddleName = new JLabel("Middle Name");
        lblMiddleName.setFont(labelFont);
        formPanel.add(lblMiddleName, gbc);
        gbc.gridx = 3;
        txtMiddleName = new JTextField(getSavedValue("middleName"), 12);
        txtMiddleName.setFont(fieldFont);
        txtMiddleName.setPreferredSize(new Dimension(160, 28));
        formPanel.add(txtMiddleName, gbc);

        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblLastName = new JLabel("Last Name");
        lblLastName.setFont(labelFont);
        formPanel.add(lblLastName, gbc);
        gbc.gridx = 1;
        txtLastName = new JTextField(getSavedValue("lastName"), 12);
        txtLastName.setFont(fieldFont);
        txtLastName.setPreferredSize(new Dimension(160, 28));
        formPanel.add(txtLastName, gbc);

        // Suffix
        gbc.gridx = 2;
        JLabel lblSuffix = new JLabel("Suffix");
        lblSuffix.setFont(labelFont);
        formPanel.add(lblSuffix, gbc);
        gbc.gridx = 3;
        txtSuffix = new JTextField(getSavedValue("suffix"), 8);
        txtSuffix.setFont(fieldFont);
        txtSuffix.setPreferredSize(new Dimension(80, 28));
        formPanel.add(txtSuffix, gbc);

        // Sex
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblSex = new JLabel("Sex");
        lblSex.setFont(labelFont);
        formPanel.add(lblSex, gbc);
        gbc.gridx = 1;
        cmbSex = new JComboBox<>(new String[]{"Female", "Male"});
        setComboBoxValue(cmbSex, getSavedValue("sex"));
        
        cmbSex.setFont(fieldFont);
        cmbSex.setPreferredSize(new Dimension(100, 28));
        cmbSex.setEditable(false); // interactable dropdown
        formPanel.add(cmbSex, gbc);

        // Civil Status
        gbc.gridx = 2;
        JLabel lblStatus = new JLabel("Civil Status");
        lblStatus.setFont(labelFont);
        formPanel.add(lblStatus, gbc);
        gbc.gridx = 3;
        cmbStatus = new JComboBox<>(new String[]{"Single", "Married"});
        setComboBoxValue(cmbStatus, getSavedValue("status"));
        cmbStatus.setFont(fieldFont);
        cmbStatus.setPreferredSize(new Dimension(100, 28));
        cmbStatus.setEditable(false); // interactable dropdown
        formPanel.add(cmbStatus, gbc);

        // Citizenship
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblCitizenship = new JLabel("Citizenship");
        lblCitizenship.setFont(labelFont);
        formPanel.add(lblCitizenship, gbc);
        gbc.gridx = 1;
        txtCitizenship = new JTextField(getSavedValue("citizenship"), 10);
        txtCitizenship.setFont(fieldFont);
        txtCitizenship.setPreferredSize(new Dimension(100, 28));
        formPanel.add(txtCitizenship, gbc);

        // Birth Date
        gbc.gridx = 2;
        JLabel lblBirthDate = new JLabel("Birth Date");
        lblBirthDate.setFont(labelFont);
        formPanel.add(lblBirthDate, gbc);
        gbc.gridx = 3;
        JPanel birthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
         monthBox = new JComboBox<>(new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        });
        monthBox.setFont(fieldFont);
        monthBox.setEditable(false); // interactable dropdown
        setComboBoxValue(monthBox, getSavedValue("birthMonth"));
        
        dayField = new JTextField(getSavedValue("birthDay"), 2);
        dayField = new JTextField(2);
        dayField.setFont(fieldFont);
        dayField.setPreferredSize(new Dimension(32, 28));
        yearField = new JTextField(getSavedValue("birthYear"), 4);
        yearField = new JTextField(4);
        yearField.setFont(fieldFont);
        yearField.setPreferredSize(new Dimension(60, 28));
        birthPanel.add(monthBox);
        birthPanel.add(Box.createHorizontalStrut(6));
        birthPanel.add(dayField);
        birthPanel.add(Box.createHorizontalStrut(6));
        birthPanel.add(yearField);
        birthPanel.setBackground(Color.WHITE);
        formPanel.add(birthPanel, gbc);

        // Age
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblAge = new JLabel("Age");
        lblAge.setFont(labelFont);
        formPanel.add(lblAge, gbc);
        gbc.gridx = 1;
        txtAge = new JTextField(getSavedValue("age"), 3);
        txtAge.setFont(fieldFont);
        txtAge.setPreferredSize(new Dimension(60, 28));
        formPanel.add(txtAge, gbc);

        // Position
        gbc.gridx = 2;
        JLabel lblPosition = new JLabel("Position");
        lblPosition.setFont(labelFont);
        formPanel.add(lblPosition, gbc);
        gbc.gridx = 3;
        txtPosition = new JTextField(getSavedValue("position"), 12);
        txtPosition.setFont(fieldFont);
        txtPosition.setPreferredSize(new Dimension(160, 28));
        formPanel.add(txtPosition, gbc);

                // Profile Picture
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblProfilePic = new JLabel("Profile Picture");
        lblProfilePic.setFont(new Font("Arial", Font.BOLD, 16));
        formPanel.add(lblProfilePic, gbc);
        gbc.gridx = 1;
        profilePictureLabel = new JLabel("Add a profile picture", SwingConstants.CENTER);
        profilePictureLabel.setOpaque(true);
        profilePictureLabel.setBackground(new Color(230, 230, 230));
        profilePictureLabel.setBorder(new LineBorder(Color.GRAY, 1, true));
        profilePictureLabel.setPreferredSize(new Dimension(60, 60));
        profilePictureLabel.setFont(fieldFont);
        
        // Load existing profile picture if available
        if (!profilePictureBase64.isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(profilePictureBase64);
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                BufferedImage image = ImageIO.read(bais);
                ImageIcon icon = new ImageIcon(image.getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                profilePictureLabel.setIcon(icon);
                profilePictureLabel.setText("");
            } catch (IOException e) {
                // Keep the default text if error loading
            }
        }
        
        

        gbc.gridx = 1;
        JButton btnAddPhoto = new JButton("Add a profile picture");
        btnAddPhoto.setFont(fieldFont);
        btnAddPhoto.setFocusPainted(false);
        btnAddPhoto.setPreferredSize(new Dimension(140, 28));
        btnAddPhoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select a Profile Photo");
            chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                saveProfilePicture(selectedFile);
            }
        });
        formPanel.add(btnAddPhoto, gbc);

        // Contact Info Section
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 4;
        formPanel.add(new JSeparator(), gbc);

        // Address
        gbc.gridy = 7; gbc.gridwidth = 1;
        JLabel lblAddress = new JLabel("Address");
        lblAddress.setFont(labelFont);
        formPanel.add(lblAddress, gbc);
        gbc.gridx = 1;
        txtAddress = new JTextField(getSavedValue("address"), 18);
        txtAddress.setFont(fieldFont);
        txtAddress.setPreferredSize(new Dimension(220, 28));
        formPanel.add(txtAddress, gbc);

        // Phone Number
        gbc.gridx = 2;
        JLabel lblPhone = new JLabel("Phone Number");
        lblPhone.setFont(labelFont);
        formPanel.add(lblPhone, gbc);
        gbc.gridx = 3;
        txtPhone = new JTextField(getSavedValue("phone"), 10);
        txtPhone.setFont(fieldFont);
        txtPhone.setPreferredSize(new Dimension(120, 28));
        formPanel.add(txtPhone, gbc);

        // Email Address
        gbc.gridx = 0; gbc.gridy = 8;
        JLabel lblEmail = new JLabel("Email Address");
        lblEmail.setFont(labelFont);
        formPanel.add(lblEmail, gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(getSavedValue("email"), 14);
        txtEmail.setFont(fieldFont);
        txtEmail.setPreferredSize(new Dimension(160, 28));
        formPanel.add(txtEmail, gbc);

        // Unique ID NO.
        gbc.gridx = 2;
        JLabel lblUniqueId = new JLabel("Unique ID NO.");
        lblUniqueId.setFont(labelFont);
        formPanel.add(lblUniqueId, gbc);
        gbc.gridx = 3;
        txtUniqueId = new JTextField(getSavedValue("uniqueId"), 12);
        txtUniqueId.setFont(fieldFont);
        txtUniqueId.setPreferredSize(new Dimension(160, 28));
        formPanel.add(txtUniqueId, gbc);

        // ID TYPE
        gbc.gridx = 0; gbc.gridy = 9;
        JLabel lblIdType = new JLabel("ID TYPE");
        lblIdType.setFont(labelFont);
        formPanel.add(lblIdType, gbc);
        gbc.gridx = 1;
        txtIdType = new JTextField(getSavedValue("idType"), 10);
        txtIdType.setFont(fieldFont);
        txtIdType.setPreferredSize(new Dimension(120, 28));
        formPanel.add(txtIdType, gbc);

        // Photo ID
        gbc.gridx = 2;
        JLabel lblPhotoId = new JLabel("Photo ID");
        lblPhotoId.setFont(labelFont);
        formPanel.add(lblPhotoId, gbc);
        gbc.gridx = 3;
        JLabel photoIdPic = new JLabel("Upload ID photo", SwingConstants.CENTER);
        photoIdPic.setOpaque(true);
        photoIdPic.setBackground(new Color(230, 230, 230));
        photoIdPic.setBorder(new LineBorder(Color.GRAY, 1, true));
        photoIdPic.setPreferredSize(new Dimension(60, 60)); // Square
        photoIdPic.setFont(fieldFont);
        formPanel.add(photoIdPic, gbc);

        gbc.gridx = 2;
        gbc.gridy = 10;
        JButton btnAddPhotoId = new JButton("Upload ID photo");
        btnAddPhotoId.setFont(fieldFont);
        btnAddPhotoId.setFocusPainted(false);
        btnAddPhotoId.setPreferredSize(new Dimension(140, 28));
        btnAddPhotoId.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select an ID Photo");
            chooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                try {
                    Image img = ImageIO.read(selectedFile);
                    if (img != null) {
                        ImageIcon icon = new ImageIcon(img.getScaledInstance(60, 60, Image.SCALE_SMOOTH)); // Square
                        photoIdPic.setText("");
                        photoIdPic.setIcon(icon);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        formPanel.add(btnAddPhotoId, gbc);

        // Done Button
        gbc.gridx = 3; gbc.gridy = 11; gbc.anchor = GridBagConstraints.EAST;
        JButton btnDone = new JButton("Done");
        btnDone.setFont(new Font("Arial", Font.BOLD, 18));
        btnDone.setForeground(Color.WHITE);
        btnDone.setBackground(new Color(0, 102, 255));
        btnDone.setFocusPainted(false);
        btnDone.setPreferredSize(new Dimension(120, 36));
        btnDone.addActionListener(e -> {
            // Save all properties before showing confirmation
            profileProperties.setProperty("firstName", txtFirstName.getText());
            profileProperties.setProperty("middleName", txtMiddleName.getText());
            profileProperties.setProperty("lastName", txtLastName.getText());
            profileProperties.setProperty("suffix", txtSuffix.getText());
            profileProperties.setProperty("sex", cmbSex.getSelectedItem().toString());
            profileProperties.setProperty("status", cmbStatus.getSelectedItem().toString());
            profileProperties.setProperty("citizenship", txtCitizenship.getText());
            profileProperties.setProperty("birthMonth", monthBox.getSelectedItem().toString());
            profileProperties.setProperty("birthDay", dayField.getText());
            profileProperties.setProperty("birthYear", yearField.getText());
            profileProperties.setProperty("age", txtAge.getText());
            profileProperties.setProperty("position", txtPosition.getText());
            profileProperties.setProperty("address", txtAddress.getText());
            profileProperties.setProperty("phone", txtPhone.getText());
            profileProperties.setProperty("email", txtEmail.getText());
            profileProperties.setProperty("uniqueId", txtUniqueId.getText());
            profileProperties.setProperty("idType", txtIdType.getText());

            BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();
            if (currentStaff != null) {
                // Update the staff object with form data
                currentStaff = currentStaff.toBuilder()
                        .firstName(txtFirstName.getText())
                        .lastName(txtLastName.getText())
                        .position(txtPosition.getText())
                        .email(txtEmail.getText())
                        .contactNo(txtPhone.getText())
                        .updatedAt(LocalDateTime.now())
                        .build();

                currentStaff.setAddress(txtAddress.getText());
                currentStaff.setMiddleName(txtMiddleName.getText());

                UserDataManager.getInstance().setCurrentStaff(currentStaff);

                System.out.println("Staff data updated successfully!");
            }

            saveProperties();

            // Update greeting in all panels
            updateGreetingInAllPanels();
            
            StringBuilder info = new StringBuilder();
            info.append("First Name: ").append(txtFirstName.getText()).append("\n");
            info.append("Middle Name: ").append(txtMiddleName.getText()).append("\n");
            info.append("Last Name: ").append(txtLastName.getText()).append("\n");
            info.append("Suffix: ").append(txtSuffix.getText()).append("\n");
            info.append("Sex: ").append(cmbSex.getSelectedItem()).append("\n");
            info.append("Civil Status: ").append(cmbStatus.getSelectedItem()).append("\n");
            info.append("Citizenship: ").append(txtCitizenship.getText()).append("\n");
            info.append("Birth Date: ").append(monthBox.getSelectedItem()).append(" ")
                .append(dayField.getText()).append(" ").append(yearField.getText()).append("\n");
            info.append("Age: ").append(txtAge.getText()).append("\n");
            info.append("Position: ").append(txtPosition.getText()).append("\n");
            info.append("Address: ").append(txtAddress.getText()).append("\n");
            info.append("Phone Number: ").append(txtPhone.getText()).append("\n");
            info.append("Email Address: ").append(txtEmail.getText()).append("\n");
            info.append("Unique ID NO.: ").append(txtUniqueId.getText()).append("\n");
            info.append("ID TYPE: ").append(txtIdType.getText()).append("\n");
            
            JOptionPane.showMessageDialog(secretary.this, info.toString(), "Submitted Information", JOptionPane.INFORMATION_MESSAGE);
            String firstName = txtFirstName.getText().trim();
            String middleName = txtMiddleName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String suffix = txtSuffix.getText().trim();
            String sex = (String) cmbSex.getSelectedItem();
            String citizenship = txtCitizenship.getText().trim();
            String age = txtAge.getText().trim();
            String address = txtAddress.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            BarangayStaff updatedStaff = currentStaff.toBuilder()
                    .firstName(firstName)
                    .middleName(middleName)
                    .lastName(lastName)
                    .suffix(suffix)
                    .gender(sex)
                    .age(Integer.parseInt(age))
                    .address(address)
                    .contactNo(phone)
                    .email(email)
                    .updatedAt(LocalDateTime.now())
                    .build();
            UserDataManager.getInstance().updateStaff(updatedStaff);
            UserDataManager.getInstance().setCurrentStaff(updatedStaff);
            System.out.println("✅ Updated staff: " + firstName + " " + lastName);
        });

        setupAutoSave();
        formPanel.add(btnDone, gbc);

        // Make the form scrollable
        JScrollPane scrollPane = new JScrollPane(formPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        containerPanel.add(scrollPane, BorderLayout.CENTER);
        return containerPanel;
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
            saveProperties();
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
            saveProperties();
            updateGreetingInAllPanels(); // <-- Add this line!
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving profile picture: " + e.getMessage());
        }
    }
    
    private void loadProfilePicture() {
        profilePictureBase64 = profileProperties.getProperty("profilePicture", "");
        if (!profilePictureBase64.isEmpty()) {
            updateProfilePictureInAllPanels();
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
            txtPosition.setText(currentStaff.getPosition() != null ? currentStaff.getPosition() : "");
            txtEmail.setText(currentStaff.getEmail() != null ? currentStaff.getEmail() : "");
            txtPhone.setText(currentStaff.getContactNo() != null ? currentStaff.getContactNo() : "");
            String age = "";
            if(age == null){
                age =""+currentStaff.getAge();
            }else{
                age = "" +(LocalDate.now().getYear() - currentStaff.getDob().getYear());
            }
            txtAge.setText(age);

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
            txtPhone.setText(currentResident.getPhoneNumber() != null ? currentResident.getPhoneNumber() : "");
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