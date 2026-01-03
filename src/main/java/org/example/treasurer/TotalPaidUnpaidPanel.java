package org.example.treasurer;


import org.example.UserDataManager;
import org.example.Users.BarangayStaff;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.Arc2D;


public class TotalPaidUnpaidPanel extends JPanel {
    private int paidCount;
    private int unpaidCount;
    private JLabel paidPercentageLabel, unpaidPercentageLabel, totalLabel;
    private JLabel paidCountLabel, unpaidCountLabel;
    private JPanel pieChartPanel;


    // Gradient colors
    private final Color CERULEAN_BLUE = new Color(100, 149, 237);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);
    private final Color VERY_LIGHT_BLUE = new Color(225, 245, 254);
    private final Color DARK_CERULEAN = new Color(70, 130, 180);


    public TotalPaidUnpaidPanel(int paidCount, int unpaidCount) {
        this.paidCount = paidCount;
        this.unpaidCount = unpaidCount;
        initializeUI();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // Create gradient from light blue to very light blue
        GradientPaint gradient = new GradientPaint(
                0, 0, LIGHT_BLUE,
                getWidth(), getHeight(), VERY_LIGHT_BLUE
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }


    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);


        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);


        // Main Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(40, 60, 40, 60));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);


        // Left side - Pie Chart
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        JPanel chartPanel = createChartPanel();
        contentPanel.add(chartPanel, gbc);


        // Right side - Statistics
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        JPanel statsPanel = createStatsPanel();
        contentPanel.add(statsPanel, gbc);


        add(contentPanel, BorderLayout.CENTER);
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Create gradient for header
                GradientPaint gradient = new GradientPaint(
                        0, 0, CERULEAN_BLUE,
                        getWidth(), 0, DARK_CERULEAN
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new CompoundBorder(
                new LineBorder(CERULEAN_BLUE, 2, true),
                new EmptyBorder(25, 40, 25, 40)
        ));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);


        JLabel lblDocumentary = new JLabel("Documentary");
        lblDocumentary.setFont(new Font("Arial", Font.BOLD, 26));
        lblDocumentary.setForeground(Color.WHITE);


        JLabel lblRequest = new JLabel("Request");
        lblRequest.setFont(new Font("Arial", Font.BOLD, 22));
        lblRequest.setForeground(Color.WHITE);


        titlePanel.add(lblDocumentary);
        titlePanel.add(lblRequest);


        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
        String name = staff.getFirstName();
        String data = "";
        if(staff.getSex().equals("Male")){
            data = "Mr.";
        }else{
            data = "Ms.";
        }
        JLabel lblUser = new JLabel("Hi "+data+" "+name);
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));
        lblUser.setForeground(Color.WHITE);


        JPanel userIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                // Gradient for user icon
                GradientPaint gradient = new GradientPaint(
                        0, 0, LIGHT_BLUE,
                        getWidth(), getHeight(), CERULEAN_BLUE
                );
                g2.setPaint(gradient);
                g2.fillOval(0, 0, 45, 45);


                g2.setColor(Color.WHITE);
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


    private JPanel createChartPanel() {
        JPanel chartPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, Color.WHITE,
                        getWidth(), getHeight(), VERY_LIGHT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        chartPanel.setOpaque(false);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CERULEAN_BLUE, 2),
                new EmptyBorder(20, 20, 20, 20)
        ));


        // Create a fixed-size panel for the pie chart to ensure it's a circle
        JPanel pieChartContainer = new JPanel(new GridBagLayout());
        pieChartContainer.setOpaque(false);


        pieChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // draw using the pie panel's own size to guarantee a circle
                drawPieChart(g, getWidth(), getHeight());
            }


            @Override
            public Dimension getPreferredSize() {
                // Fixed size to ensure perfect circle
                return new Dimension(300, 300);
            }
        };
        pieChartPanel.setOpaque(false);
        pieChartPanel.setPreferredSize(new Dimension(300, 300));


        pieChartContainer.add(pieChartPanel);
        chartPanel.add(pieChartContainer, BorderLayout.CENTER);


        return chartPanel;
    }


    private void drawPieChart(Graphics g, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // Use the smaller dimension of the pie panel so we draw a perfect circle
        int size = Math.min(width, height) - 8; // small padding
        int x = (width - size) / 2;
        int y = (height - size) / 2;


        int total = paidCount + unpaidCount;
        if (total == 0) {
            // Draw gradient background when no data
            GradientPaint gradient = new GradientPaint(
                    x, y, LIGHT_BLUE,
                    x + size, y + size, VERY_LIGHT_BLUE
            );
            g2.setPaint(gradient);
            g2.fillOval(x, y, size, size);
            return;
        }


        int paidAngle = (int) Math.round(360 * paidCount / (double) total);
        int unpaidAngle = 360 - paidAngle;


        // Use cerulean color scheme for pie chart
        Color paidColor = DARK_CERULEAN;
        Color unpaidColor = CERULEAN_BLUE;


        int start = 90; // start at top
        g2.setColor(paidColor);
        g2.fill(new Arc2D.Double(x, y, size, size, start, -paidAngle, Arc2D.PIE));


        g2.setColor(unpaidColor);
        g2.fill(new Arc2D.Double(x, y, size, size, start - paidAngle, -unpaidAngle, Arc2D.PIE));


        // draw inner white circle (donut)
        int inner = size / 2;
        int ix = x + (size - inner) / 2;
        int iy = y + (size - inner) / 2;


        // Use gradient for inner circle
        GradientPaint innerGradient = new GradientPaint(
                ix, iy, Color.WHITE,
                ix + inner, iy + inner, VERY_LIGHT_BLUE
        );
        g2.setPaint(innerGradient);
        g2.fillOval(ix, iy, inner, inner);


        // center left empty (no text) — the donut shows data via the stats panel
    }


    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, Color.WHITE,
                        getWidth(), getHeight(), VERY_LIGHT_BLUE
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CERULEAN_BLUE, 2),
                new EmptyBorder(30, 30, 30, 30)
        ));


        // Title
        JLabel titleLabel = new JLabel("Payment Summary");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.add(titleLabel);
        statsPanel.add(Box.createVerticalStrut(30));


        // Calculate percentages
        int total = paidCount + unpaidCount;
        double paidPercentage = total == 0 ? 0 : (paidCount * 100.0) / total;
        double unpaidPercentage = total == 0 ? 0 : (unpaidCount * 100.0) / total;


        // Color indicators (larger size) - store percentage labels in fields so updateData can change them
        Color paidColor = DARK_CERULEAN;
        Color unpaidColor = CERULEAN_BLUE;


        // Paid indicator
        JPanel paidIndicator = new JPanel(new BorderLayout(15, 0));
        paidIndicator.setOpaque(false);
        paidIndicator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        paidIndicator.setBorder(new EmptyBorder(10, 0, 10, 0));


        JPanel paidCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(paidColor);
                g2.fillOval(0,0,40,40);
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(40,40);
            }
        };
        paidCircle.setOpaque(false);


        JPanel paidTextPanel = new JPanel();
        paidTextPanel.setLayout(new BoxLayout(paidTextPanel, BoxLayout.Y_AXIS));
        paidTextPanel.setOpaque(false);


        JLabel paidLabel = new JLabel("Total Paid (Verified)");
        paidLabel.setFont(new Font("Arial", Font.BOLD, 18));
        paidLabel.setForeground(Color.DARK_GRAY);


        paidPercentageLabel = new JLabel(String.format("%.1f%%", paidPercentage));
        paidPercentageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        paidPercentageLabel.setForeground(paidColor);


        paidCountLabel = new JLabel(String.format("%d", paidCount));
        paidCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        paidCountLabel.setForeground(paidColor);


        paidTextPanel.add(paidLabel);
        paidTextPanel.add(Box.createVerticalStrut(4));
        paidTextPanel.add(paidPercentageLabel);
        paidTextPanel.add(Box.createVerticalStrut(4));
        paidTextPanel.add(paidCountLabel);


        paidIndicator.add(paidCircle, BorderLayout.WEST);
        paidIndicator.add(paidTextPanel, BorderLayout.CENTER);
        statsPanel.add(paidIndicator);
        statsPanel.add(Box.createVerticalStrut(20));


        // Unpaid indicator
        JPanel unpaidIndicator = new JPanel(new BorderLayout(15,0));
        unpaidIndicator.setOpaque(false);
        unpaidIndicator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        unpaidIndicator.setBorder(new EmptyBorder(10,0,10,0));


        JPanel unpaidCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(unpaidColor);
                g2.fillOval(0,0,40,40);
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(40,40);
            }
        };
        unpaidCircle.setOpaque(false);


        JPanel unpaidTextPanel = new JPanel();
        unpaidTextPanel.setLayout(new BoxLayout(unpaidTextPanel, BoxLayout.Y_AXIS));
        unpaidTextPanel.setOpaque(false);


        JLabel unpaidLabel = new JLabel("Total Voided (Pending)");
        unpaidLabel.setFont(new Font("Arial", Font.BOLD, 18));
        unpaidLabel.setForeground(Color.DARK_GRAY);


        unpaidPercentageLabel = new JLabel(String.format("%.1f%%", unpaidPercentage));
        unpaidPercentageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        unpaidPercentageLabel.setForeground(unpaidColor);


        unpaidCountLabel = new JLabel(String.format("%d", unpaidCount));
        unpaidCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        unpaidCountLabel.setForeground(unpaidColor);


        unpaidTextPanel.add(unpaidLabel);
        unpaidTextPanel.add(Box.createVerticalStrut(4));
        unpaidTextPanel.add(unpaidPercentageLabel);
        unpaidTextPanel.add(Box.createVerticalStrut(4));
        unpaidTextPanel.add(unpaidCountLabel);


        unpaidIndicator.add(unpaidCircle, BorderLayout.WEST);
        unpaidIndicator.add(unpaidTextPanel, BorderLayout.CENTER);
        statsPanel.add(unpaidIndicator);
        statsPanel.add(Box.createVerticalStrut(30));


        // Separator
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(CERULEAN_BLUE);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        statsPanel.add(separator);
        statsPanel.add(Box.createVerticalStrut(30));


        // Total Count Section
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        totalPanel.setBorder(new EmptyBorder(10, 0, 10, 0));


        JLabel totalTextLabel = new JLabel("Total Documents");
        totalTextLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalTextLabel.setForeground(Color.DARK_GRAY);


        totalLabel = new JLabel(String.valueOf(total));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 24));
        totalLabel.setForeground(DARK_CERULEAN);
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);


        totalPanel.add(totalTextLabel, BorderLayout.WEST);
        totalPanel.add(totalLabel, BorderLayout.EAST);
        statsPanel.add(totalPanel);


        statsPanel.add(Box.createVerticalGlue());


        return statsPanel;
    }


    private JPanel createColorIndicator(String label, double percentage, Color color) {
        JPanel indicatorPanel = new JPanel(new BorderLayout(15, 0));
        indicatorPanel.setOpaque(false);
        indicatorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        indicatorPanel.setBorder(new EmptyBorder(10, 0, 10, 0));


        // Color circle
        JPanel colorCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, 40, 40); // Larger color circle
            }


            @Override
            public Dimension getPreferredSize() {
                return new Dimension(40, 40);
            }
        };
        colorCircle.setOpaque(false);


        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);


        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Arial", Font.BOLD, 18));
        labelLabel.setForeground(Color.DARK_GRAY);
        labelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);


        JLabel percentageLabel = new JLabel(String.format("%.1f%%", percentage));
        percentageLabel.setFont(new Font("Arial", Font.BOLD, 20));
        percentageLabel.setForeground(color);
        percentageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);


        textPanel.add(labelLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(percentageLabel);


        indicatorPanel.add(colorCircle, BorderLayout.WEST);
        indicatorPanel.add(textPanel, BorderLayout.CENTER);


        return indicatorPanel;
    }


    public void updateData(int newPaidCount, int newUnpaidCount) {
        this.paidCount = newPaidCount;
        this.unpaidCount = newUnpaidCount;


        int total = paidCount + unpaidCount;
        double paidPercentage = total == 0 ? 0 : (paidCount * 100.0) / total;
        double unpaidPercentage = total == 0 ? 0 : (unpaidCount * 100.0) / total;


        // Update labels
        paidPercentageLabel.setText(String.format("%.1f%%", paidPercentage));
        unpaidPercentageLabel.setText(String.format("%.1f%%", unpaidPercentage));
        if (paidCountLabel != null) paidCountLabel.setText(String.valueOf(paidCount));
        if (unpaidCountLabel != null) unpaidCountLabel.setText(String.valueOf(unpaidCount));
        totalLabel.setText(String.valueOf(total));


        // Repaint the chart
        if (pieChartPanel != null) pieChartPanel.repaint(); else repaint();
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
}

