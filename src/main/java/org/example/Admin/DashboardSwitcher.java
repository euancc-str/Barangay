package org.example.Admin;

import org.example.UserDataManager;
import org.example.Users.BarangayStaff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DashboardSwitcher extends JPanel {

    // Colors matching your AdminBlotterTab
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color HOVER_COLOR = new Color(235, 240, 245);

    // Role Colors
    private final Color SEC_COLOR = new Color(46, 204, 113); // Green
    private final Color TRES_COLOR = new Color(241, 196, 15); // Yellow/Gold
    private final Color CAP_COLOR = new Color(52, 152, 219);  // Blue

    public DashboardSwitcher() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // 1. Header
        JLabel lblTitle = new JLabel("System View Selector", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(44, 62, 80));
        add(lblTitle, BorderLayout.NORTH);

        // 2. Button Grid
        JPanel gridPanel = new JPanel(new GridLayout(1, 3, 30, 0)); // 1 row, 3 cols
        gridPanel.setBackground(BG_COLOR);
        gridPanel.setBorder(new EmptyBorder(50, 20, 50, 20));

        // Get current staff
        BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();

        // Add the 3 Menu Cards with proper method references
        gridPanel.add(createMenuCard("Secretary View", "Manage Residents & Docs", "📝", SEC_COLOR,
                () -> openSecretaryDashboard(currentStaff)));
        gridPanel.add(createMenuCard("Treasurer View", "Payments & Collections", "💰", TRES_COLOR,
                () -> openTreasurerDashboard(currentStaff)));
        gridPanel.add(createMenuCard("Captain View", "Oversight & Analytics", "👮", CAP_COLOR,
                () -> openCaptainDashboard(currentStaff)));

        add(gridPanel, BorderLayout.CENTER);
    }

    private JPanel createMenuCard(String title, String subtitle, String icon, Color accentColor, Runnable action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 5, 0, accentColor), // Bottom color bar
                new EmptyBorder(30, 20, 30, 20)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Icon
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(50, 50, 50));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add spacing
        card.add(Box.createVerticalGlue());
        card.add(lblIcon);
        card.add(Box.createVerticalStrut(20));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(5));
        card.add(lblSub);
        card.add(Box.createVerticalGlue());

        // Hover Effect & Click Action
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        return card;
    }

    // =========================================================================
    // NAVIGATION METHODS WITH DISPOSE
    // =========================================================================

    private void openSecretaryDashboard(BarangayStaff staff) {
        try {
            // Get the current frame
            JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

            // Open your secretary class
            Class<?> secretaryClass = Class.forName("org.example.Interface.secretary");
            java.lang.reflect.Constructor<?> constructor = secretaryClass.getDeclaredConstructor();
            Object secretaryInstance = constructor.newInstance();

            // Make it visible
            javax.swing.JFrame secretaryFrame = (javax.swing.JFrame) secretaryInstance;
            secretaryFrame.setVisible(true);

            // Dispose the current frame
            if (currentFrame != null) {
                currentFrame.dispose();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error opening secretary dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTreasurerDashboard(BarangayStaff staff) {
        try {
            // Get the current frame
            JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

            // Open your TreasurerDashboard class
            Class<?> treasurerClass = Class.forName("org.example.treasurer.TreasurerDashboard");
            java.lang.reflect.Constructor<?> constructor = treasurerClass.getDeclaredConstructor();
            Object treasurerInstance = constructor.newInstance();

            // Make it visible
            javax.swing.JFrame treasurerFrame = (javax.swing.JFrame) treasurerInstance;
            treasurerFrame.setVisible(true);

            // Dispose the current frame
            if (currentFrame != null) {
                currentFrame.dispose();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error opening treasurer dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCaptainDashboard(BarangayStaff staff) {
        try {
            // Get the current frame
            JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

            // Open your CaptainDashboard class
            Class<?> captainClass = Class.forName("org.example.Captain.CaptainDashboard");
            java.lang.reflect.Constructor<?> constructor = captainClass.getDeclaredConstructor();
            Object captainInstance = constructor.newInstance();

            // Make it visible
            javax.swing.JFrame captainFrame = (javax.swing.JFrame) captainInstance;
            captainFrame.setVisible(true);

            // Dispose the current frame
            if (currentFrame != null) {
                currentFrame.dispose();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error opening captain dashboard: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}