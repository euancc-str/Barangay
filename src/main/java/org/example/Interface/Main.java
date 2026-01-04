package org.example.Interface;




import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Properties;


import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;


import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;
import org.example.utils.ResourceUtils;


import lombok.SneakyThrows;




public class Main {
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
    static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth();
            int h = getHeight();




            // Cerulean to Light Sky Blue gradient
            Color color1 = new Color(0, 123, 167); // Cerulean
            Color color2 = new Color(135, 206, 250); // Light Sky Blue
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }

    static class PlaceholderTextField extends JTextField {
        private String placeholder;
        private boolean showingPlaceholder;
        public PlaceholderTextField(String placeholder) {
            super();
            this.placeholder = placeholder;
            this.showingPlaceholder = true;
            setText(placeholder);
            setForeground(Color.GRAY);


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
            ImageIcon originalIcon = new ImageIcon(ResourceUtils.getResourceAsBytes(path));
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








    private static void showServerSettings() {


        JDialog dialog = new JDialog((Frame) null, "Server Configuration", true);
        dialog.setSize(900, 900);


        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout());
        org.example.Admin.AdminSettings.AdminServerConfigTab configPanel =
                new org.example.Admin.AdminSettings.AdminServerConfigTab();


        dialog.add(configPanel, BorderLayout.CENTER);


        // 3. Show it
        dialog.setVisible(true);
    }


    private static SystemConfigDAO dao;


    @SneakyThrows
    private static void loadPropertiesToSystem(){
        Properties props = new Properties();
        props.load(ResourceUtils.getResourceAsStream("application.properties"));
        props.forEach((k, v) ->
                System.setProperty(k.toString(), v.toString())
        );
    }




    public static void main(String[] args) {
        loadPropertiesToSystem();

        // 1. CHECK CONNECTION FIRST (From your previous fix)
        performStartupConnectionCheck();

        JFrame frame = new JFrame("Serbisyong Barangay\n");
        frame.setUndecorated(true);
        frame.setSize(800, 500);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 2. Load Logo (From your previous fix)
        dao = new SystemConfigDAO();
        try {
            String logoPath = dao.getLogoPath();
            ImageIcon icon = new ImageIcon(logoPath);
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.out.println("Icon not found");
        }

        // 3. MAIN LAYOUT SETUP
        frame.setContentPane(new GradientPanel());
        frame.setLayout(new BorderLayout()); // ✅ Changed to BorderLayout

        // --- ✅ NEW: TOP RIGHT EXIT BUTTON ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false); // Make transparent
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 20)); // Padding

        JButton btnExit = new JButton("Exit System");
        btnExit.setPreferredSize(new Dimension(120, 35));
        btnExit.setFont(new Font("Arial", Font.BOLD, 12));
        btnExit.setForeground(Color.WHITE);
        btnExit.setBackground(new Color(231, 76, 60)); // Red Color
        btnExit.setBorder(new RoundedBorder(15, new Color(192, 57, 43))); // Darker Red Border
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.setOpaque(true);
        btnExit.setContentAreaFilled(true);

        // Hover Effect
        btnExit.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnExit.setBackground(new Color(192, 57, 43)); }
            public void mouseExited(MouseEvent e) { btnExit.setBackground(new Color(231, 76, 60)); }
        });

        // Action: Exit
        btnExit.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to exit the application?",
                    "Exit System",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        topPanel.add(btnExit);
        frame.add(topPanel, BorderLayout.NORTH);
        // -------------------------------------

        // 4. CENTER CONTAINER (Logo & Login Form)
        // We keep the internal GridBagLayout so the form stays centered in the middle of the screen
        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new GridBagLayout());

        GridBagConstraints containerGbc = new GridBagConstraints();
        containerGbc.gridx = 0;
        containerGbc.anchor = GridBagConstraints.CENTER;
        containerGbc.insets = new Insets(0, 0, 0, 0);

        // --- (EXISTING LOGO & TITLE CODE) ---
        containerGbc.gridy = 0;
        containerGbc.insets = new Insets(20, 0, 20, 0);

        try {
            String bigLogo = dao.getBigLogoPath();
            ImageIcon originalIcon = new ImageIcon(bigLogo);
            Image originalImage = originalIcon.getImage();
            Image resizedImage = originalImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            ImageIcon resizedIcon = new ImageIcon(resizedImage);
            JLabel logoLabel = new JLabel(resizedIcon);
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            centerContainer.add(logoLabel, containerGbc);
        } catch (Exception e) {
            JLabel placeholder = new JLabel("[LOGO]");
            placeholder.setFont(new Font("Arial", Font.BOLD, 16));
            placeholder.setForeground(Color.WHITE);
            centerContainer.add(placeholder, containerGbc);
        }

        containerGbc.gridy = 1;
        containerGbc.insets = new Insets(0, 0, 5, 0);

        JLabel mainTitle = new JLabel("Serbisyong Barangay");

        mainTitle.setFont(new Font("Arial", Font.BOLD, 28));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        centerContainer.add(mainTitle, containerGbc);

        containerGbc.gridy = 2;
        containerGbc.insets = new Insets(0, 0, 30, 0);
        String brgyNameFullPath = new SystemConfigDAO().getConfig("defaultCtcPlace");
        JLabel subTitle = new JLabel(brgyNameFullPath);
        subTitle.setFont(new Font("Arial", Font.PLAIN, 16));
        subTitle.setForeground(Color.WHITE);
        subTitle.setHorizontalAlignment(SwingConstants.CENTER);
        centerContainer.add(subTitle, containerGbc);

        // --- (EXISTING LOGIN FORM CODE) ---
        containerGbc.gridy = 3;
        containerGbc.fill = GridBagConstraints.NONE;
        containerGbc.insets = new Insets(0, 0, 0, 0);

        JPanel loginFormPanel = new JPanel();
        loginFormPanel.setOpaque(false);
        loginFormPanel.setLayout(new GridBagLayout());
        loginFormPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        loginFormPanel.setBackground(new Color(255, 255, 255, 30));

        GridBagConstraints formGbc = new GridBagConstraints();
        formGbc.gridx = 0;
        formGbc.fill = GridBagConstraints.HORIZONTAL;
        formGbc.insets = new Insets(5, 20, 5, 20);

        // Welcome Label
        formGbc.gridy = 0;
        formGbc.insets = new Insets(0, 20, 20, 20);
        JLabel welcomeLabel = new JLabel("WELCOME!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginFormPanel.add(welcomeLabel, formGbc);

        // Username
        formGbc.gridy = 1;
        formGbc.insets = new Insets(5, 20, 5, 20);
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        usernameLabel.setForeground(Color.WHITE);
        loginFormPanel.add(usernameLabel, formGbc);

        formGbc.gridy = 2;
        formGbc.insets = new Insets(0, 20, 15, 20);
        JPanel usernamePanel = new JPanel(new BorderLayout(5, 0));
        usernamePanel.setOpaque(false);
        JLabel usernameIcon = new JLabel(createIcon("profile.png", 32, 32));
        usernamePanel.add(usernameIcon, BorderLayout.WEST);
        PlaceholderTextField usernameField = new PlaceholderTextField("Enter your username");
        usernameField.setPreferredSize(new Dimension(200, 35));
        usernameField.setBorder(new RoundedBorder(15, Color.WHITE));
        usernameField.setOpaque(true);
        usernameField.setBackground(Color.WHITE);
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        loginFormPanel.add(usernamePanel, formGbc);

        // Password
        formGbc.gridy = 3;
        formGbc.insets = new Insets(5, 20, 5, 20);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordLabel.setForeground(Color.WHITE);
        loginFormPanel.add(passwordLabel, formGbc);

        formGbc.gridy = 4;
        formGbc.insets = new Insets(0, 20, 20, 20);
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setOpaque(false);
        JLabel passwordIcon = new JLabel(createIcon("password.png", 32, 32));
        passwordPanel.add(passwordIcon, BorderLayout.WEST);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 35));
        passwordField.setBorder(new RoundedBorder(15, Color.WHITE));
        passwordField.setOpaque(true);
        passwordField.setBackground(Color.WHITE);
        passwordField.setEchoChar((char) 0);
        passwordField.setText("Enter your password");
        passwordField.setForeground(Color.GRAY);
        passwordField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(passwordField.getPassword()).equals("Enter your password")) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.BLACK);
                    passwordField.setEchoChar('•');
                }
            }
            public void focusLost(FocusEvent e) {
                if (passwordField.getPassword().length == 0) {
                    passwordField.setEchoChar((char) 0);
                    passwordField.setText("Enter your password");
                    passwordField.setForeground(Color.GRAY);
                }
            }
        });
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        loginFormPanel.add(passwordPanel, formGbc);

        // Login Button
        formGbc.gridy = 5;
        formGbc.insets = new Insets(10, 20, 15, 20);
        JButton loginButton = new JButton("Log In");
        loginButton.setPreferredSize(new Dimension(200, 35));
        loginButton.setBackground(new Color(50, 50, 50));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBorder(new RoundedBorder(10, new Color(50, 50, 50)));
        loginButton.setOpaque(true);
        loginButton.setContentAreaFilled(true);
        loginButton.setFocusPainted(false);
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { loginButton.setBackground(new Color(80, 80, 80)); }
            public void mouseExited(MouseEvent e) { loginButton.setBackground(new Color(50, 50, 50)); }
        });

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter your credentials.", "Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BarangayStaff staff = UserDataManager.getInstance().validateStaffLogin(username, password);
            if (staff != null && staff.getStatus().equals("Active")) {
                JDialog loader = createLoadingDialog(frame);
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    protected Void doInBackground() throws Exception { Thread.sleep(1750); return null; }
                    protected void done() {
                        try {
                            UserDataManager.getInstance().setCurrentStaff(staff);
                            if (staff.getRole().equals("Brgy.Secretary")) openSecretaryWithLoader(frame, staff);
                            else if (staff.getRole().equals("Brgy.Captain")) openCaptainDashboard(staff);
                            else if (staff.getRole().equals("Brgy.Treasurer")) openTreasurerDashboard(staff);
                            else new Main().openAdminDashboard(staff);
                            loader.dispose();
                            frame.dispose();
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                };
                worker.execute();
                loader.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        loginFormPanel.add(loginButton, formGbc);

        // Settings Button
        formGbc.gridy = 6;
        formGbc.insets = new Insets(10, 20, 10, 20);
        JButton btnSettings = new JButton("⚙ Server Settings");
        btnSettings.setFont(new Font("Arial", Font.PLAIN, 12));
        btnSettings.setBackground(new Color(240, 240, 240));
        btnSettings.setForeground(Color.BLACK);
        btnSettings.setBorder(new RoundedBorder(10, Color.LIGHT_GRAY));
        btnSettings.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSettings.setFocusPainted(false);
        btnSettings.addActionListener(e -> showServerSettings());
        loginFormPanel.add(btnSettings, formGbc);

        centerContainer.add(loginFormPanel, containerGbc);

        // ✅ Add Center Container to BORDER LAYOUT CENTER
        frame.add(centerContainer, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private static void openSecretaryWithLoader(JFrame loginFrame, BarangayStaff staff) {
        // 1. Create a transparent, undecorated dialog
        JDialog loader = new JDialog(loginFrame, true);
        loader.setUndecorated(true);
        loader.setBackground(new Color(0, 0, 0, 0)); // Crucial for rounded corners!
        loader.setSize(380, 100);
        loader.setLocationRelativeTo(loginFrame);


        // 2. Create a Custom Rounded Panel
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Draw clean white rounded rectangle
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);


                // Draw subtle gray border
                g2.setColor(new Color(220, 220, 220));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setOpaque(false); // Let the rounded paintComponent show through


        // 3. Add Content
        // LEFT: Icon (optional, looks nice)
        JLabel iconLabel = new JLabel("⚡"); // You can swap this for an ImageIcon
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        iconLabel.setForeground(new Color(0, 123, 167)); // Brand Blue


        // CENTER: Text Info
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);


        JLabel titleLabel = new JLabel("Logging in...");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.DARK_GRAY);


        JLabel subtitleLabel = new JLabel("Preparing Secretary Dashboard");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);


        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);


        // BOTTOM: Custom Flat Progress Bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(100, 6)); // Thin and sleek
        progressBar.setBorderPainted(false);
        progressBar.setBackground(new Color(230, 230, 230)); // Light gray track
        progressBar.setForeground(new Color(0, 123, 167));   // Brand blue fill


        // Remove the ugly standard Java UI styling
        progressBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                int width = progressBar.getWidth();
                int height = progressBar.getHeight();
                int fillWidth = (int) ((width * progressBar.getPercentComplete()));


                // Draw Background
                g2d.setColor(progressBar.getBackground());
                g2d.fillRoundRect(0, 0, width, height, height, height);


                // Draw Fill
                g2d.setColor(progressBar.getForeground());
                g2d.fillRoundRect(0, 0, fillWidth, height, height, height);
            }
        });


        // Assembly
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        loader.add(panel);


        // 4. Background Worker (Timer)
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i <= 100; i += 5) { // Faster increments for smoother animation
                    Thread.sleep(80); // 30ms * 20 steps = ~0.6 seconds (adjust as needed)
                    publish(i);
                }
                Thread.sleep(200); // Tiny pause at 100% to let user see it finished
                return null;
            }


            @Override
            protected void process(java.util.List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }


            @Override
            protected void done() {
                try {
                    loader.dispose();
                    openSecretaryDashboard(staff);
                    loginFrame.dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };


        worker.execute();
        loader.setVisible(true);
    }
    private static JDialog createLoadingDialog(JFrame owner) {
        JDialog dialog = new JDialog(owner, "Loading", true);
        dialog.setUndecorated(true);
        dialog.setSize(300, 80);
        dialog.setLocationRelativeTo(owner);
        dialog.setLayout(new BorderLayout());


        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        content.setBorder(new javax.swing.border.EmptyBorder(20, 20, 20, 20));


        JLabel lblMessage = new JLabel("Accessing Dashboard...", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Arial", Font.PLAIN, 14));
        lblMessage.setForeground(Color.DARK_GRAY);


        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 8));
        progressBar.setForeground(new Color(0, 123, 167));
        progressBar.setBorder(BorderFactory.createEmptyBorder());


        content.add(lblMessage, BorderLayout.CENTER);
        content.add(progressBar, BorderLayout.SOUTH);


        dialog.add(content);
        return dialog;
    }
    private static void performStartupConnectionCheck() {
        // Loop continuously until connected or user quits
        while (true) {
            try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection()) {
                // If this line succeeds, we are connected!
                if (conn != null && !conn.isClosed()) {
                    System.out.println("✅ Startup Connection Check: SUCCESS");
                    break; // Exit the loop and start the app
                }
            } catch (Exception e) {
                System.out.println("❌ Startup Connection Check: FAILED (" + e.getMessage() + ")");


                // Show Error Dialog with Options
                String message = "Database Connection Failed!\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "The application cannot start without a database connection.\n" +
                        "Please configure the Server Settings.";


                Object[] options = {"⚙ Open Server Settings", "Exit"};
                int choice = JOptionPane.showOptionDialog(null, message,
                        "Startup Error",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null, options, options[0]);


                if (choice == 0) {
                    // User clicked "Open Server Settings"
                    showServerSettings();
                    // After this method returns (dialog closed), the loop runs again to RETRY the connection.
                    loadPropertiesToSystem(); // Reload properties in case they were changed
                } else {
                    // User clicked "Exit" or closed window
                    System.exit(0);
                }
            }
        }
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
    public void openAdminDashboard(BarangayStaff staff) {
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
}





