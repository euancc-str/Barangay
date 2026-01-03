package org.example.Captain;

import org.example.StaffDAO;
import org.example.Users.BarangayStaff;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class BarangayOfficialProfile extends JPanel {

    private static final Color HEADER_BG = new Color(21, 101, 192);

    public BarangayOfficialProfile() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(229, 231, 235));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = createContentPanel();
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(229, 231, 235));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);

        JLabel lblMain = new JLabel("Barangay Officials Directory");
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblMain.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Meet Your Barangay Leadership Team");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(new Color(200, 220, 255));

        titlePanel.add(lblMain);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSub);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(HEADER_BG);

        BarangayStaff staff = new StaffDAO().findStaffByPosition("Captain");

        JLabel lblUser = new JLabel("Hi Mr. " + staff.getFirstName());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.WHITE);

        JPanel userIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillOval(0, 0, 45, 45);
                g2.setColor(HEADER_BG);
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

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                new EmptyBorder(50, 60, 50, 60)
        ));

        // Top Captain (centered)
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        topRow.setBackground(Color.WHITE);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        BarangayStaff staff = new StaffDAO().findStaffByPosition("Captain");
        String fullName = staff.getFirstName() + " " + staff.getMiddleName() + " "+staff.getLastName();
        topRow.add(createOfficialCard(fullName, "Brgy. Captain", new Color(34, 197, 94), true));
        contentPanel.add(topRow);
        contentPanel.add(Box.createVerticalStrut(40));

        BarangayStaff secretary = new StaffDAO().findStaffByPosition("Secretary");
        String secFullName = secretary.getFirstName() + " " + secretary.getMiddleName() + " "+secretary.getLastName();
        // Second row (2 officials)
        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 120, 0));
        secondRow.setBackground(Color.WHITE);
        secondRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        secondRow.add(createOfficialCard(secFullName, "Barangay Secretary", null, false));
        BarangayStaff treasurer = new StaffDAO().findStaffByPosition("Treasurer");
        String treasurerFullName = treasurer.getFirstName() + " " + treasurer.getMiddleName() + " "+treasurer.getLastName();

        secondRow.add(createOfficialCard(treasurerFullName, "Brgy. Treasurer", null, false));
        contentPanel.add(secondRow);
        contentPanel.add(Box.createVerticalStrut(40));

        // Third row (4 officials)
        JPanel thirdRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 0));
        thirdRow.setBackground(Color.WHITE);
        thirdRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        thirdRow.add(createOfficialCard(" ", "Brgy. Councilor", null, false));
        thirdRow.add(createOfficialCard(" ", "Brgy. Councilor", null, false));
        thirdRow.add(createOfficialCard(" ", "Brgy. Councilor", null, false));
        thirdRow.add(createOfficialCard(" ", "Brgy. Councilor", null, false));
        contentPanel.add(thirdRow);

        contentPanel.add(Box.createVerticalGlue());

        return contentPanel;
    }

    private JPanel createOfficialCard(String name, String position, Color borderColor, boolean isCaptain) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(150, 190));

        // Profile picture placeholder
        int picSize = isCaptain ? 130 : 110;
        JPanel profilePic = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw border
                if (borderColor != null) {
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(4));
                    g2.drawOval(2, 2, getWidth() - 4, getHeight() - 4);
                }

                // Draw placeholder circle
                g2.setColor(new Color(200, 200, 200));
                g2.fillOval(8, 8, getWidth() - 16, getHeight() - 16);

                // Draw simple face icon
                g2.setColor(new Color(150, 150, 150));
                // Head
                g2.fillOval(getWidth()/2 - 15, getHeight()/3 - 10, 30, 30);
                // Body
                int bodyY = getHeight()/3 + 18;
                g2.fillArc(getWidth()/2 - 25, bodyY, 50, 40, 0, 180);
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(picSize, picSize);
            }
        };
        profilePic.setOpaque(false);
        profilePic.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(profilePic);
        card.add(Box.createVerticalStrut(15));

        // Name
        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Arial", Font.BOLD, isCaptain ? 16 : 14));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblName.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblName);

        card.add(Box.createVerticalStrut(5));

        // Position
        JLabel lblPosition = new JLabel(position);
        lblPosition.setFont(new Font("Arial", Font.PLAIN, isCaptain ? 13 : 12));
        lblPosition.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPosition.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblPosition);

        return card;
    }
}