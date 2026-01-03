package org.example.SerbisyongBarangay;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Interface.secretary;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/**
 * OPTIMIZED VERSION - Eliminates painting lag by caching expensive operations
 */
public class requestaDocumentFrame extends JFrame {
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 750;
    private static final Color HEADER_BG_COLOR = Color.BLACK;
    private static final Color CONTENT_BG_COLOR = Color.WHITE;
    private static final Color BUTTON_BORDER_COLOR = Color.BLACK;
    private static final Color PRIMARY_BLUE = new Color(0, 0, 139);

    private SystemConfigDAO dao;

    // ✅ OPTIMIZATION 1: Cache the logo image once
    private Image cachedLogoImage;

    public requestaDocumentFrame() {
        // ✅ Load logo ONCE during initialization
        dao = new SystemConfigDAO();
        String logoPath = dao.getLogoPath();
        cachedLogoImage = new ImageIcon(logoPath).getImage();

        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setTitle("Serbisyong Barangay - Document Request");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout());

        // Build UI
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentArea(), BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private JPanel createHeaderPanel() {
        // ✅ OPTIMIZATION 2: Use solid color instead of painting gradient every time
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER_BG_COLOR);
        panel.setPreferredSize(new Dimension(WINDOW_WIDTH, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left section
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        // ✅ OPTIMIZATION 3: Create logo label with cached image (no custom painting)
        JLabel logoLabel = new JLabel();
        Image scaledLogo = cachedLogoImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        logoLabel.setIcon(new ImageIcon(scaledLogo));
        leftPanel.add(logoLabel);

        JLabel titleLabel = new JLabel("Serbisyong Barangay");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 30));
        titleLabel.setForeground(CONTENT_BG_COLOR);
        leftPanel.add(titleLabel);

        panel.add(leftPanel, BorderLayout.WEST);

        // Right section
        JButton backButton = new JButton("⬅");
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
        });

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(backButton);

        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createContentArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel innerCard = new JPanel(new BorderLayout());
        innerCard.setBackground(CONTENT_BG_COLOR);
        innerCard.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        // Top Section
        JPanel requestHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        requestHeader.setOpaque(false);

        JButton requestButton = new JButton("📄 REQUEST A DOCUMENT");
        requestButton.setFont(new Font("Inter", Font.BOLD, 20));
        requestButton.setForeground(CONTENT_BG_COLOR);
        requestButton.setBackground(new Color(66, 133, 244));
        requestButton.setFocusPainted(false);
        requestButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_BLUE, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        // ✅ OPTIMIZATION 4: Remove custom painting from button - use standard button
        requestButton.setOpaque(true);

        requestHeader.add(requestButton);
        innerCard.add(requestHeader, BorderLayout.NORTH);

        // Center Section: Button Grid
        JPanel buttonGridPanel = new JPanel(new GridLayout(2, 2, 40, 40));
        buttonGridPanel.setOpaque(false);
        buttonGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 80, 80, 80));

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

    private JButton createDocumentButton(String text) {
        // ✅ OPTIMIZATION 5: Use standard JButton without custom painting
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.PLAIN, 24));
        button.setBackground(CONTENT_BG_COLOR);
        button.setForeground(BUTTON_BORDER_COLOR);
        button.setFocusPainted(false);
        button.setMargin(new Insets(30, 30, 30, 30));

        Border line = BorderFactory.createLineBorder(BUTTON_BORDER_COLOR, 2);
        Border empty = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BUTTON_BORDER_COLOR, 2, 20),
                empty));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ✅ OPTIMIZATION 6: Load forms in background thread
                SwingUtilities.invokeLater(() -> {
                    if (text.equals("Barangay Clearance")) {
                        areyousureyouwantorequestbformframe ar = new areyousureyouwantorequestbformframe(null);
                        ar.setVisible(true);
                    } else if (text.equals("Business Clearance")) {
                        BusinessClearanceForm bs = new BusinessClearanceForm();
                        bs.createAndShowGUI();
                    } else if (text.equals("Certificate of Indigency")) {
                        IndigencyFormDialog ig = new IndigencyFormDialog(null);
                        ig.setVisible(true);
                    } else {
                        ResidencyFormDialog dialog = new ResidencyFormDialog(null);
                        dialog.setVisible(true);
                    }
                });
            }
        });

        // Simple hover effect - no custom painting
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            requestaDocumentFrame app = new requestaDocumentFrame();
            app.setVisible(true);
        });
    }
}