package org.example.SerbisyongBarangay;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Interface.secretary;
import org.example.Users.BarangayStaff;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;

/**
 * A Java Swing application replicating the "Serbisyong Barangay" (Barangay
 * Service)
 * document request interface.
 *
 * This application creates a window with a fixed size of 1280x750 pixels,
 * featuring a header, a central "Request a Document" section, and four
 * large buttons for different document types.
 */
public class requestaDocumentFrame extends JFrame {

    // --- Configuration Constants ---
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 750;
    private static final Color HEADER_BG_COLOR = Color.BLACK;
    private static final Color CONTENT_BG_COLOR = Color.WHITE;
    private static final Color BUTTON_BORDER_COLOR = Color.BLACK;
    private static final Color PRIMARY_BLUE = new Color(0, 0, 139); // Darker Blue for accent
    private SystemConfigDAO dao;
    public requestaDocumentFrame() {
        // 1. Frame Setup
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 25));
        logoPanel.setBackground(Color.BLACK);
        logoPanel.setMaximumSize(new Dimension(260, 90));

        dao = new SystemConfigDAO();
        String logoPath = dao.getConfig("logoPath");
        JPanel logoCircle = new JPanel() {

            private Image logoImage = new ImageIcon(System.getProperty("asset.image.base-path")+logoPath).getImage(); // 🔹 path to your logo image

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

        setTitle("Serbisyong Barangay - Document Request");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true); // Keeps the required size fixed
        setLayout(new BorderLayout());

        // 2. Build the Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 3. Build the Main Content Area
        JPanel contentArea = createContentArea();
        add(contentArea, BorderLayout.CENTER);



        // Center the frame on the screen
        setLocationRelativeTo(null);
    }

    /**
     * Creates the top black header panel containing the title and the back button.
     * 
     * @return The configured header JPanel.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER_BG_COLOR);
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left section (Logo/Icon and Title)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        // Placeholder for Logo/Seal (Replaced with text/icon since image loading is
        // complex in a single file)
        JLabel logoLabel = new JLabel(); // Unicode for shield/badge icon
        String logolink = new SystemConfigDAO().getConfig("logoPath");
        Image logoImage = new ImageIcon(logolink).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(logoImage));
        leftPanel.add(logoLabel);

        // Title Label
        JLabel titleLabel = new JLabel("Serbisyong Barangay");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 30));
        titleLabel.setForeground(CONTENT_BG_COLOR);
        leftPanel.add(titleLabel);

        panel.add(leftPanel, BorderLayout.WEST);

        // Right section (Back Button)
        JButton backButton = new JButton("\u2B05"); // Unicode for back arrow
        backButton.setFont(new Font("Inter", Font.BOLD, 30));
        backButton.setBackground(HEADER_BG_COLOR);
        backButton.setForeground(CONTENT_BG_COLOR);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createLineBorder(CONTENT_BG_COLOR, 5, true));
        backButton.setPreferredSize(new Dimension(50, 50));
        backButton.addActionListener(e -> {
            secretary secretary = new secretary();
            secretary.setVisible(true);
            dispose();
            // JOptionPane.showMessageDialog(this, "Back button clicked.", "Navigation",
            // JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(backButton);

        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates the main white content panel, including the document header and the
     * button grid.
     * 
     * @return The configured content JPanel.
     */
    private JPanel createContentArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE); // Dark gray border/background
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Outer padding

        // Inner white card
        JPanel innerCard = new JPanel(new BorderLayout());
        innerCard.setBackground(CONTENT_BG_COLOR);
        innerCard.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Inner border

        // Top Section: "REQUEST A DOCUMENT" Button/Label
        JPanel requestHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        requestHeader.setOpaque(false);

        JButton requestButton = new JButton("REQUEST A DOCUMENT");
        requestButton.setFont(new Font("Inter", Font.BOLD, 20));
        requestButton.setForeground(CONTENT_BG_COLOR);
        requestButton.setBackground(new Color(66, 133, 244));
        requestButton.setFocusPainted(false);
        requestButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        requestButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Request Document process started.", "Action",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Add a document icon placeholder
        JLabel docIcon = new JLabel("\uD83D\uDCC4 "); // Unicode for a paper document icon
        docIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        docIcon.setForeground(CONTENT_BG_COLOR);

        // Use a wrapper to place the icon and text together in the button
        requestButton.setText("\uD83D\uDCC4 REQUEST A DOCUMENT");

        requestHeader.add(requestButton);
        innerCard.add(requestHeader, BorderLayout.NORTH);

        // Center Section: 2x2 Button Grid
        JPanel buttonGridPanel = new JPanel(new GridLayout(2, 2, 40, 40)); // Rows, Cols, HGap, VGap
        buttonGridPanel.setOpaque(false);
        buttonGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 80, 80)); // Inner padding

        // Create and add the four document buttons
        String[] documentNames = {
                "Barangay Clearance",
                "Business Clearance",
                "Certificate of Indigency",
                "Certificate of Residency"
        };

        for (String name : documentNames) {
            JButton docButton = createDocumentButton(name);
            buttonGridPanel.add(docButton);
        }

        innerCard.add(buttonGridPanel, BorderLayout.CENTER);

        panel.add(innerCard, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates a large, styled button for a document type.
     * 
     * @param text The text to display on the button.
     * @return The configured JButton.
     */
    private JButton createDocumentButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.PLAIN, 24));
        button.setBackground(CONTENT_BG_COLOR);
        button.setForeground(BUTTON_BORDER_COLOR);
        button.setFocusPainted(false);
        button.setMargin(new Insets(30, 30, 30, 30)); // Large internal padding

        // Custom rounded border effect
        Border line = BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 2);
        Border empty = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BUTTON_BORDER_COLOR, 2, 20), // Custom rounded line border
                empty));

        // Add action listener
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // 1. Use the captured 'text' variable (the button's label)
                if (text.equals("Barangay Clearance")) {
                    // If the user clicks the "Barangay Clearance" button
                    // BClearanceForm clearance=new BClearanceForm();
                    // clearance.setVisible(true);
                    // JOptionPane.showMessageDialog(null,
                    //         "Opening Barangay Clearance Form!",
                    //         "Document Selected",
                    //         JOptionPane.INFORMATION_MESSAGE);
                    // BClearanceForm clearance = new BClearanceForm();
                    // clearance.setVisible(true);
                    areyousureyouwantorequestbformframe ar=new areyousureyouwantorequestbformframe(null);
                    ar.setVisible(true);

                } else if (text.equals("Business Clearance")) {
                    // Logic for another document

                    JOptionPane.showMessageDialog(null,
                            "Opening Another Document Form!",
                            "Document Selected",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    BusinessClearanceForm bs = new BusinessClearanceForm();
                    bs.createAndShowGUI();
                } else if (text.equals("Certificate of Indigency")) {
                    // Logic for another document
                    JOptionPane.showMessageDialog(null,
                            "Opening Another Document Form!",
                            "Document Selected",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    IndigencyFormDialog ig = new IndigencyFormDialog(null);
                    ig.setVisible(true);

                } else {
                    // Certificate of Residency
                    // Fallback for any other button
                    dispose();
                    ResidencyFormDialog dialog = new ResidencyFormDialog(null);
                    dialog.setVisible(true);
                }
            }
        });
        // Add hover effect (simple color change)
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 240));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(CONTENT_BG_COLOR);
            }
        });

        return button;
    }

    /**
     * Custom Border implementation to draw a rounded rectangle border,
     * mimicking modern UI aesthetics.
     */
    private static class RoundedBorder implements Border {
        private final Color color;
        private final int thickness;
        private final int radius;

        RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                    width - thickness, height - thickness,
                    radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int i = thickness + radius / 2;
            return new Insets(i, i, i, i);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        // Ensure the UI is updated on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            requestaDocumentFrame app = new requestaDocumentFrame();
            app.setVisible(true);
        });
    }
}
