package org.example.Interface;


import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;
import org.example.Users.Resident;
import org.example.utils.ResourceUtils;


import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
public class TotalRequestPanel extends JPanel {
    private static final String DATA_FILE = "dashboard_data.csv";

    private JLabel lblTotal;
    private JLabel lblPending, lblVerified, lblRejected;
    private PieChart chartPanel;
    private JLabel lblUser;
    private JLabel totalRequestProfilePicture;


    public TotalRequestPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(229, 231, 235));
        setBorder(new EmptyBorder(15, 15, 15, 15));


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // We can't run updateStats() directly here because it touches UI components (lblTotal.setText).
                // Instead, we fetch the data first, then update UI in done().

                // NOTE: Ideally, you should split updateStats() into "fetchData" and "updateUI".
                // But for a quick fix, just wrapping the whole method is risky if it touches Swing components.
                // A safer quick fix is to run it after the window opens:
                return null;
            }

            @Override
            protected void done() {
                updateStats(); // Runs on UI thread, but AFTER the window has initialized
            }
        }.execute();
    }


    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 123, 167));
        headerPanel.setPreferredSize(new Dimension(0, 90));
        headerPanel.setBorder(new EmptyBorder(15, 25, 15, 25));


        JLabel title = new JLabel("Documentary Request");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));


        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        userPanel.setOpaque(false);


        lblUser = new JLabel(getGreetingFromProperties());
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Arial", Font.PLAIN, 15));



        userPanel.add(lblUser);


        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);


        return headerPanel;
    }


    private JPanel createContentPanel() {
        JPanel content = new JPanel(new BorderLayout(30, 0));
        content.setBackground(new Color(144,213,255));
        content.setBorder(new EmptyBorder(30, 50, 30, 50));


        // Left: Pie Chart
        chartPanel = new PieChart();
        chartPanel.setPreferredSize(new Dimension(500, 400));
        content.add(chartPanel, BorderLayout.WEST);


        // Right: Stats Panel
        JPanel statsPanel = new JPanel();
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setLayout(new GridBagLayout());
        statsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;


        JLabel lblTitle = new JLabel("Total Request");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        statsPanel.add(lblTitle, gbc);


        gbc.gridy++;
        lblTotal = new JLabel("", SwingConstants.CENTER);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 32));
        lblTotal.setForeground(Color.RED);
        statsPanel.add(lblTotal, gbc);


        gbc.gridy++;
        JLabel lblPercentTitle = new JLabel("Percentage of Request");
        lblPercentTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblPercentTitle.setForeground(Color.BLACK);
        statsPanel.add(lblPercentTitle, gbc);


        gbc.gridy++;
        lblPending = new JLabel();
        lblVerified = new JLabel();
        lblRejected = new JLabel();



        lblPending.setFont(new Font("Arial", Font.PLAIN, 16));
        lblVerified.setFont(new Font("Arial", Font.PLAIN, 16));
        lblRejected.setFont(new Font("Arial", Font.PLAIN, 16));



        JPanel percents = new JPanel();
        percents.setLayout(new GridLayout(4, 1, 5, 5));
        percents.setBackground(Color.WHITE);


        percents.add(lblPending);
        percents.add(lblVerified);
        percents.add(lblRejected);



        gbc.gridy++;
        statsPanel.add(percents, gbc);


        content.add(statsPanel, BorderLayout.CENTER);


        return content;
    }




    private void updateStats() {
        // Load data from database using ResidentDAO
        int pendingCount = 0;
        int verifiedCount = 0;
        int rejectedCount = 0;
        int approvedCount = 0;


        try {
            DocumentRequest dr = new DocumentRequest();
            List<DocumentRequest> res = dr.displayStatusData();


            for (DocumentRequest documentRequest : res) {




                if (documentRequest != null) {
                    String status = documentRequest.getStatus();
                    switch (status) {
                        case "Pending":
                            pendingCount++;
                            break;
                        case "Confirmed Payment":
                            verifiedCount++;
                            break;
                        case "Rejected":
                            rejectedCount++;
                            break;
                        case "Approved":
                            approvedCount++;
                            break;
                        case "Released":
                            approvedCount++;
                            break;
                    }
                }
            }
        } catch (Exception e) {
            // Use default values if database error occurs
            System.err.println("Error loading data from database: " + e.getMessage());
            e.printStackTrace();
            pendingCount = 4;
            verifiedCount = 1;
            rejectedCount = 1;
            approvedCount = 0;
        }


        int total = pendingCount + verifiedCount + rejectedCount + approvedCount;
        DecimalFormat df = new DecimalFormat("0.0");


        double pendingPercent = total > 0 ? (double) pendingCount / total * 100 : 0;
        double verifiedPercent = total > 0 ? (double) verifiedCount / total * 100 : 0;
        double rejectedPercent = total > 0 ? (double) rejectedCount / total * 100 : 0;
        double approvedPercent = total > 0 ? (double) approvedCount / total * 100 : 0;


        lblTotal.setText(String.valueOf(total));


        lblPending.setText("<html><font color='blue'>Pending Request = </font>" + df.format(pendingPercent) + "%</html>");
        lblVerified.setText("<html><font color='green'>Verified Request = </font>" + df.format(approvedPercent) + "%</html>");
        lblRejected.setText("<html><font color='red'>Voided Request = </font>" + df.format(rejectedPercent) + "%</html>");


        if (chartPanel != null) {
            chartPanel.updateData(pendingPercent, verifiedPercent, rejectedPercent, approvedPercent);
        }
    }

    // Helper: Checks if a request is older than 1 Day


    // Refresh method for external calls
    public void refreshData() {
        updateStats();
        if (chartPanel != null) {
            chartPanel.repaint();
        }
    }


    // Simple pie chart using Java2D
    class PieChart extends JPanel {
        private double pending, verified, rejected, approved;


        public void updateData(double pending, double verified, double rejected, double approved) {
            this.pending = pending;
            this.verified = verified;
            this.rejected = rejected;
            this.approved = approved;
            repaint();
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            int size = Math.min(getWidth(), getHeight()) - 100;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;


            double start = 0;
            double[] values = {pending, verified, rejected, approved};
            Color[] colors = {
                    new Color(102, 153, 255), // Pending (blue)
                    new Color(46, 139, 87),   // Verified (green)
                    new Color(220, 20, 60),   // Rejected (red)
                    new Color(102, 255, 255)  // Approved (light cyan)
            };
            String[] labels = {"Pending Request", "Verified Request", "Rejected Request", "Approved Request"};


            // Draw pie chart
            for (int i = 0; i < values.length; i++) {
                if (values[i] > 0) {
                    double angle = values[i] * 3.6;
                    g2.setColor(colors[i]);
                    g2.fillArc(x, y, size, size, (int) start, (int) Math.ceil(angle));
                    start += angle;
                }
            }


            // Labels
            int legendX = x + size + 40;
            int legendY = y + 20;
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            for (int i = 0; i < labels.length; i++) {
                g2.setColor(colors[i]);
                g2.fillRect(legendX, legendY + (i * 25), 18, 18);
                g2.setColor(Color.BLACK);
                g2.drawString(labels[i], legendX + 25, legendY + 14 + (i * 25));
            }
        }
    }


    // ===== PROFILE PICTURE LOADING =====


    // ===== DYNAMIC GREETING =====
    private String getGreetingFromProperties() {
        java.util.Properties props = new java.util.Properties();
        try {
            props.load(ResourceUtils.getResourceAsStream("profiles/secretary.properties"));
            String lastName = props.getProperty("lastName", "");
            String sex = props.getProperty("sex", "");

            if (lastName.isEmpty()) {
                return "Hi Secretary";
            }

            String title = "Mr.";
            if ("Female".equals(sex)) {
                title = "Mrs.";
            }

            return "Hi " + title + " " + lastName;
        } catch (IOException e) {
            return "Hi Secretary";
        }
    }


    public void refreshHeader() {
        lblUser.setText(getGreetingFromProperties());

    }



}

