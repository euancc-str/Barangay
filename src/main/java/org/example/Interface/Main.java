package org.example.Interface;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Admin.AdminSystemSettings;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;
import org.example.UserDataManager;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;

public class Main {
    // Custom Rounded Border Class
    static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = 8;
            insets.top = insets.bottom = 4;
            return insets;
        }
    }

    // Custom Text Field with Placeholder
    static class PlaceholderTextField extends JTextField {
        private String placeholder;
        private boolean showingPlaceholder;

        public PlaceholderTextField(String placeholder) {
            super();
            this.placeholder = placeholder;
            this.showingPlaceholder = true;
            setText(placeholder);
            setForeground(Color.GRAY);

            // Add focus listener to handle placeholder behavior
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (showingPlaceholder) {
                        setText("");
                        setForeground(Color.BLACK);
                        showingPlaceholder = false;
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        showingPlaceholder = true;
                    }
                }
            });
        }

        @Override
        public String getText() {
            return showingPlaceholder ? "" : super.getText();
        }
    }

    // Custom ComboBox UI for rounded appearance
    static class RoundedComboBoxUI extends BasicComboBoxUI {
        private int radius;

        public RoundedComboBoxUI(int radius) {
            this.radius = radius;
        }

        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton();
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setBackground(Color.WHITE);
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Do nothing to remove the default background
        }
    }

    // Method to create icon with consistent size
    private static ImageIcon createIcon(String path, int width, int height) {
        try {
            ImageIcon originalIcon = new ImageIcon(path);
            Image resizedImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImage);
        } catch (Exception e) {
            System.out.println("Icon not found: " + path);
            // Return a placeholder if icon is not found
            return createPlaceholderIcon(width, height);
        }
    }

    // Method to create a placeholder icon when image is not found
    private static ImageIcon createPlaceholderIcon(int width, int height) {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(0, 0, width-1, height-1);
        g2d.setFont(new Font("Arial", Font.PLAIN, 8));
        g2d.drawString("Icon", width/2-10, height/2+3);
        g2d.dispose();
        return new ImageIcon(image);
    }


    private static void openRegisterWindow(JFrame currentFrame) {
        try {
            // Close the current login frame
            currentFrame.dispose();

            // Using reflection to load and run the Register class
            Class<?> registerClass = Class.forName("org.example.Interface.Register");
            java.lang.reflect.Method mainMethod = registerClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[]{});

        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "Register class not found. Please create Register.java in the interfaces package.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error opening registration form: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static SystemConfigDAO dao;

    public static void main(String[] args) {

        JFrame frame = new JFrame("Serbisyong Barangay");
        frame.setSize(800, 500);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set frame icon (optional)
        dao = new SystemConfigDAO();
        String logoPath = dao.getConfig("logoPath");
        System.out.println(logoPath);

        try {
            ImageIcon icon = new ImageIcon("resident_photos/"+logoPath);
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("Icon not found");
        }
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Left panel - 65% width - For image/content
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.65;  // 65% of available width
        gbc.weighty = 1.0;   // 100% of available height
        gbc.fill = GridBagConstraints.BOTH; // Stretch to fill space

        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.BLACK); // Black background for content
        leftPanel.setLayout(new GridBagLayout());

        // Add content to left panel matching your image
        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.gridx = 0;
        leftGbc.anchor = GridBagConstraints.CENTER;
        leftGbc.insets = new Insets(10, 50, 30, 50);

        // Add Logo Image at the top
        leftGbc.gridy = 0;
        try {
            String bigLogo = dao.getConfig("bigLogoPath");
            ImageIcon originalIcon = new ImageIcon("resident_photos/"+bigLogo);
            Image originalImage = originalIcon.getImage();
            Image resizedImage = originalImage.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            JLabel logoLabel = new JLabel(resizedIcon);
            leftPanel.add(logoLabel, leftGbc);
        } catch (Exception e) {
            System.out.println("Logo not found, using placeholder");
            JLabel placeholder = new JLabel("[LOGO]");
            placeholder.setFont(new Font("Arial", Font.BOLD, 16));
            placeholder.setForeground(Color.WHITE);
            leftPanel.add(placeholder, leftGbc);
        }

        // Main Title
        leftGbc.gridy = 1;
        JLabel mainTitle = new JLabel("Serbisyong Barangay");
        mainTitle.setFont(new Font("Arial", Font.BOLD, 28));
        mainTitle.setForeground(Color.WHITE);
        leftPanel.add(mainTitle, leftGbc);

        // Subtitle
        leftGbc.gridy = 2;
        JLabel subTitle = new JLabel("Documentary Request System");
        subTitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subTitle.setForeground(Color.WHITE);
        leftPanel.add(subTitle, leftGbc);

        // Separator line
        leftGbc.gridy = 3;
        leftGbc.fill = GridBagConstraints.HORIZONTAL;
        leftGbc.insets = new Insets(20, 50, 20, 50);

        // Second separator
        leftGbc.gridy = 8;
        leftGbc.fill = GridBagConstraints.HORIZONTAL;
        leftGbc.insets = new Insets(20, 50, 20, 50);

        frame.add(leftPanel, gbc);

        // Right panel - 35% width - Login form
        gbc.gridx = 1;
        gbc.weightx = 0.35;  // 35% of available width

        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(new Color(240, 240, 240)); // Light gray background
        rightPanel.setLayout(new GridBagLayout());

        // Create constraints for the login form
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 0;
        rightGbc.fill = GridBagConstraints.HORIZONTAL;
        rightGbc.insets = new Insets(5, 20, 5, 20);

        // Welcome label at top
        rightGbc.gridy = 0;
        rightGbc.insets = new Insets(30, 20, 20, 20);
        JLabel welcomeLabel = new JLabel("WELCOME!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.BLACK);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(welcomeLabel, rightGbc);

        // Username
        rightGbc.gridy = 1;
        rightGbc.insets = new Insets(5, 20, 5, 30);
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        rightPanel.add(usernameLabel, rightGbc);

        rightGbc.gridy = 2;
        // Create a panel for icon + text field
        JPanel usernamePanel = new JPanel(new BorderLayout(5, 0)); // 5px horizontal gap
        usernamePanel.setBackground(new Color(240, 240, 240));

        // Add icon on the left
        JLabel usernameIcon = new JLabel(createIcon("src/main/java/org/example/resources/profile.png", 32, 32));
        usernamePanel.add(usernameIcon, BorderLayout.WEST);


        // Add text field on the right with placeholder
        PlaceholderTextField usernameField = new PlaceholderTextField("Enter your username");
        usernameField.setPreferredSize(new Dimension(180, 35)); // Reduced width to accommodate icon
        usernameField.setBorder(new RoundedBorder(15, Color.GRAY));
        usernameField.setOpaque(false);
        usernameField.setBackground(Color.WHITE);
        usernamePanel.add(usernameField, BorderLayout.CENTER);

        rightPanel.add(usernamePanel, rightGbc);

        // Password
        rightGbc.gridy = 3;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        rightPanel.add(passwordLabel, rightGbc);

        rightGbc.gridy = 4;
        // Create a panel for icon + password field
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setBackground(new Color(240, 240, 240));

        // Add icon on the left
        JLabel passwordIcon = new JLabel(createIcon("src/main/java/org/example/resources/password.png", 32, 32));
        passwordPanel.add(passwordIcon, BorderLayout.WEST);


        // Add password field on the right
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(180, 35));
        passwordField.setBorder(new RoundedBorder(15, Color.GRAY));
        passwordField.setOpaque(false);
        passwordField.setBackground(Color.WHITE);
        // Set placeholder for password field using echo char trick
        passwordField.setEchoChar((char) 0); // Show text normally initially
        passwordField.setText("Enter your password");
        passwordField.setForeground(Color.GRAY);

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(passwordField.getPassword()).equals("Enter your password")) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.BLACK);
                    passwordField.setEchoChar('•'); // Set to bullet character for password
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setEchoChar((char) 0);
                    passwordField.setText("Enter your password");
                    passwordField.setForeground(Color.GRAY);
                }
            }
        });

        passwordPanel.add(passwordField, BorderLayout.CENTER);

        rightPanel.add(passwordPanel, rightGbc);

        // Log In As
        rightGbc.gridy = 5;

        rightGbc.gridy = 6;
        // Create a panel for icon + combo box
        JPanel comboBoxPanel = new JPanel(new BorderLayout(5, 0));
        comboBoxPanel.setBackground(new Color(240, 240, 240));

        // Add icon on the left


        // Add combo box on the right
        AdminSystemSettings systemSettings = new AdminSystemSettings();
        String[] loginOptions = systemSettings.getLoginOptions();

        rightPanel.add(comboBoxPanel, rightGbc);

        // Log In Button
        rightGbc.gridy = 7;
        rightGbc.insets = new Insets(20, 20, 10, 20);
        JButton loginButton = new JButton("Log In");
        loginButton.setPreferredSize(new Dimension(200, 35));
        loginButton.setBackground(Color.BLUE);
        loginButton.setForeground(Color.BLUE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        // Optional: Round the button corners too
        loginButton.setBorder(new RoundedBorder(10, Color.BLUE));
        loginButton.setOpaque(false);
        loginButton.setContentAreaFilled(false);
        loginButton.setFocusPainted(false);
        rightPanel.add(loginButton, rightGbc);

        // Sign Up Link
        rightGbc.gridy = 8;
        rightGbc.insets = new Insets(10, 20, 30, 20);
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter your username.",
                        "Username Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter your password.",
                        "Password Required",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }



                BarangayStaff staff = UserDataManager.getInstance().validateStaffLogin(username, password);
                if (staff != null && staff.getStatus().equals("Active")) {
                    // ✅ SET CURRENT STAFF
                    UserDataManager.getInstance().setCurrentStaff(staff);
                    staff.setLastLogin(LocalDateTime.now());

                    JOptionPane.showMessageDialog(frame,
                            "Login Successful!\n\n" +
                                    "Welcome, " + staff.getFirstName() + " " + staff.getLastName() + "!\n" +
                                    "Role: " + staff.getRole(),
                            "Login Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    frame.dispose();

                    // Open appropriate dashboard based on role
                    if (staff.getRole().equals("Brgy.Secretary")) {
                        openSecretaryDashboard(staff);
                    } else if (staff.getRole().equals("Brgy.Captain")) {
                        openCaptainDashboard(staff);
                    } else if (staff.getRole().equals("Brgy.Treasurer")){
                        openTreasurerDashboard(staff);
                    } else {
                        openAdminDashboard(staff);
                    }
                } else {

                        JOptionPane.showMessageDialog(frame,
                                "Invalid username or password ",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);


                }

        });

        frame.add(rightPanel, gbc);

        frame.setVisible(true);
    }
    private static void createFallbackResidentDashboard(Resident resident) {
        JFrame dashboard = new JFrame("Resident Dashboard - " + resident.getFirstName());
        dashboard.setSize(800, 600);
        dashboard.setLocationRelativeTo(null);
        dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel welcomeLabel = new JLabel(
                "<html><h1>Welcome, " + resident.getFirstName() + " " + resident.getLastName() + "!</h1>" +
                        "<p>Username: " + resident.getUsername() + "</p>" +
                        "<p>Email: " + resident.getEmail() + "</p>" +
                        "<p>Phone: " + resident.getPhoneNumber() + "</p>" +
                        "<p>Status: " + resident.getStatus() + "</p></html>"
        );
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            UserDataManager.getInstance().logout();
            dashboard.dispose();
            main(new String[]{});
        });
        panel.add(logoutBtn, BorderLayout.SOUTH);

        dashboard.add(panel);
        dashboard.setVisible(true);
    }
    private static void openResidentDashboard(JFrame currentFrame, Resident resident) {
        try {
            currentFrame.dispose();

            // Try to open the userMain class for residents
            try {
                Class<?> userMainClass = Class.forName("org.example.SerbisyongBarangay.userMain");
                java.lang.reflect.Constructor<?> constructor = userMainClass.getDeclaredConstructor();
                Object userMainInstance = constructor.newInstance();

                System.out.println("✅ Resident dashboard (userMain) opened for: " + resident.getFirstName());

            } catch (ClassNotFoundException e) {
                // If userMain doesn't exist, create a simple fallback dashboard
                System.out.println("⚠️ userMain class not found, using fallback dashboard");
                createFallbackResidentDashboard(resident);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error opening resident dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private static void openCaptainDashboard(BarangayStaff staff) {
        try {
            // Open your CaptainDashboard class
            Class<?> captainClass = Class.forName("org.example.Captain.CaptainDashboard");
            java.lang.reflect.Constructor<?> constructor = captainClass.getDeclaredConstructor();
            Object captainInstance = constructor.newInstance();

            // Make it visible
            javax.swing.JFrame captainFrame = (javax.swing.JFrame) captainInstance;
            captainFrame.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error opening captain dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private static void openAdminDashboard(BarangayStaff staff) {
        // Simply open the main container
        new org.example.Admin.AdminDashboard().setVisible(true);
    }
    private static void openTreasurerDashboard(BarangayStaff staff) {
        try {
            // Open your CaptainDashboard class
            Class<?> captainClass = Class.forName("org.example.treasurer.TreasurerDashboard");
            java.lang.reflect.Constructor<?> constructor = captainClass.getDeclaredConstructor();
            Object captainInstance = constructor.newInstance();

            // Make it visible
            javax.swing.JFrame captainFrame = (javax.swing.JFrame) captainInstance;
            captainFrame.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error opening captain dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private static void openSecretaryDashboard(BarangayStaff staff) {
        try {
            // Open your secretary class
            Class<?> secretaryClass = Class.forName("org.example.Interface.secretary");
            java.lang.reflect.Constructor<?> constructor = secretaryClass.getDeclaredConstructor();
            Object secretaryInstance = constructor.newInstance();

            // Make it visible
            javax.swing.JFrame secretaryFrame = (javax.swing.JFrame) secretaryInstance;
            secretaryFrame.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error opening secretary dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}//rba