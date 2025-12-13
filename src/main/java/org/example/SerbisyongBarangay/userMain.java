package org.example.SerbisyongBarangay;

import org.example.UserDataManager;
import org.example.Users.Resident;
import org.example.utils.ResourceUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.border.*;

public class userMain extends JFrame {
    public static int logoHeight = 50;
    public static int logoWidth = 50;

    protected JButton logoutBtn;
    protected JButton requestBtn;
    protected JButton checkBtn;

    public userMain() {
        initComponents();
    }

    private void initComponents() {
        // ================= MAIN FRAME =================
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Serbisyong Barangay");
        setSize(1280, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(15, 15, 15));

        // ================= HEADER =================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.BLACK);
        header.setPreferredSize(new Dimension(getWidth(), 70));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Serbisyong Barangay");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Poppins", Font.BOLD, 30));

        logoutBtn = new JButton("LOGOUT →");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFont(new Font("Poppins", Font.BOLD, 14));
        logoutBtn.setBorder(new RoundedBorder(25));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Optional logo placeholder
        JLabel logo = new JLabel();
        logo.setPreferredSize(new Dimension(40, 40)); // 60
        logo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        logo.setHorizontalAlignment(SwingConstants.CENTER);


        // Image scaledImage=logoIcon.getImage();

        Image logoImg = new ImageIcon(ResourceUtils.getResourceAsBytes("serbisyongBarangayLogo.jpg")).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        logo.setIcon(new ImageIcon(logoImg));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        leftHeader.setOpaque(false);
        leftHeader.add(logo);
        leftHeader.add(title);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ================= MAIN CONTENT PANEL =================
        JPanel mainPanel = new JPanel(new GridBagLayout());
        // mainPanel.setBackground(new Color(20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.BOTH;

        // ================= LEFT PANEL =================
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new RoundedBorder(20));
        GridBagConstraints lgbc = new GridBagConstraints();
        lgbc.insets = new Insets(20, 20, 20, 20);
        lgbc.fill = GridBagConstraints.HORIZONTAL;

        requestBtn = new JButton("REQUEST A DOCUMENT");
        styleButton(requestBtn, new Color(66, 133, 244));

        requestBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nopersonalinfowarningframe hehe = new nopersonalinfowarningframe(null);
                hehe.setVisible(true);

            }

        });

        checkBtn = new JButton("CHECK DOCUMENT STATUS");
        styleButton(checkBtn, new Color(255, 105, 180));

        checkBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // CheckDocumentStatusFrame check=new CheckDocumentStatusFrame();
                // check.setVisible(true);

                screenshotofpayment hhjsd=new screenshotofpayment(null);
                hhjsd.setVisible(true);
            }

        });

        // Icons (you can replace with real ones using ImageIcon)
        JLabel docIcon = new JLabel();

        Image docimage = new ImageIcon(ResourceUtils.getResourceAsBytes("documentLogo.png")).getImage().getScaledInstance(logoWidth, logoWidth, Image.SCALE_SMOOTH);
        docIcon.setIcon(new ImageIcon(docimage));

        JLabel checkIcon = new JLabel();
        Image checkimg = new ImageIcon(ResourceUtils.getResourceAsBytes("checkDocLogo.png")).getImage().getScaledInstance(logoWidth, logoHeight,
                Image.SCALE_SMOOTH);
        checkIcon.setIcon(new ImageIcon(checkimg));

        // Add components
        lgbc.gridx = 0;
        lgbc.gridy = 0;
        leftPanel.add(docIcon, lgbc);
        lgbc.gridx = 1;
        lgbc.gridy = 0;
        leftPanel.add(requestBtn, lgbc);

        lgbc.gridx = 0;
        lgbc.gridy = 1;
        leftPanel.add(checkIcon, lgbc);
        lgbc.gridx = 1;
        lgbc.gridy = 1;
        leftPanel.add(checkBtn, lgbc);

        gbc.gridx = 0;
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        mainPanel.add(leftPanel, gbc);

        // ================= RIGHT PANEL =================
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new RoundedBorder(20));
        GridBagConstraints rgbc = new GridBagConstraints();
        rgbc.insets = new Insets(10, 10, 10, 10);
        rgbc.fill = GridBagConstraints.NONE;

        JLabel profileIcon = new JLabel();

        Image profileImage = new ImageIcon(ResourceUtils.getResourceAsBytes("userProfile.png")).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        profileIcon.setIcon(new ImageIcon(profileImage));

        JLabel nameLabel = new JLabel("NAME");
        nameLabel.setFont(new Font("Poppins", Font.BOLD, 20));
        JButton detailsBtn = new JButton("PERSONAL DETAILS");
        styleButton(detailsBtn, new Color(255, 153, 0));

        detailsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FilloutPersonalInfo fp = new FilloutPersonalInfo();
            }

        });

        rgbc.gridy = 0;
        rightPanel.add(profileIcon, rgbc);
        rgbc.gridy = 1;
        rightPanel.add(nameLabel, rgbc);
        rgbc.gridy = 2;
        rightPanel.add(detailsBtn, rgbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        mainPanel.add(rightPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // ================= UTILITY STYLES =================
    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Poppins", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(20));
        btn.setPreferredSize(new Dimension(280, 60));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // ================= CUSTOM ROUNDED BORDER =================
    class RoundedBorder implements Border {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(radius + 1, radius + 1, radius + 2, radius);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(Color.LIGHT_GRAY);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }
    private void loadCurrentResident() {
        currentResident = UserDataManager.getInstance().getCurrentResident();
        if (currentResident == null) {
            System.out.println("⚠️ No resident logged in!");
            JOptionPane.showMessageDialog(null,
                    "No user session found. Please login first.",
                    "Session Error",
                    JOptionPane.WARNING_MESSAGE);
            // Redirect to login
            openMainWindow();
        } else {
            System.out.println("✅ Loaded resident: " + currentResident.getFirstName() + " " + currentResident.getLastName());
        }
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
    private Resident currentResident;
    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new userMain());
    }

    private void noPersonaldetails() {
        JOptionPane.showMessageDialog(null, "Hi");
    }
}
