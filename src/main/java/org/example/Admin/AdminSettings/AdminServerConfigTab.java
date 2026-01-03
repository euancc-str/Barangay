package org.example.Admin.AdminSettings;

import org.example.DatabaseConnection; // Ensure you have this or standard JDBC
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class AdminServerConfigTab extends JPanel {

    // Properties File Path (Adjust if necessary for your project structure)
    private static final String CONFIG_FILE = "src/main/resources/application.properties";

    // UI Components
    private JTextField txtHost, txtPort, txtDbName, txtUsername, txtImagePath;
    private JPasswordField txtPassword;
    private JTextArea txtUrlPreview;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color HEADER_BG = new Color(44, 62, 80);
    private final Color BTN_SAVE_COLOR = new Color(46, 204, 113);   // Green
    private final Color BTN_TEST_COLOR = new Color(52, 152, 219);   // Blue
    private final Font MAIN_FONT = new Font("Arial", Font.PLAIN, 14);

    public AdminServerConfigTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        loadCurrentSettings();
    }

    private JPanel createContentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(30, 100, 30, 100)); // Centered look

        // --- DATABASE SECTION ---
        JPanel dbPanel = createSectionPanel("Database Connection");

        txtHost = createStyledTextField("localhost");
        txtPort = createStyledTextField("3306");
        txtDbName = createStyledTextField("barangay_db");
        txtUsername = createStyledTextField("root");
        txtPassword = new JPasswordField(20);
        styleField(txtPassword);

        addLabelAndField(dbPanel, "Host (IP Address):", txtHost);
        addLabelAndField(dbPanel, "Port:", txtPort);
        addLabelAndField(dbPanel, "Database Name:", txtDbName);
        addLabelAndField(dbPanel, "Username:", txtUsername);
        addLabelAndField(dbPanel, "Password:", txtPassword);

        // Preview of URL
        txtUrlPreview = new JTextArea(2, 40);
        txtUrlPreview.setEditable(false);
        txtUrlPreview.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtUrlPreview.setBackground(new Color(230, 230, 230));
        txtUrlPreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JPanel previewP = new JPanel(new BorderLayout());
        previewP.setBackground(Color.WHITE);
        previewP.add(new JLabel("Generated URL Preview:"), BorderLayout.NORTH);
        previewP.add(txtUrlPreview, BorderLayout.CENTER);
        previewP.setBorder(new EmptyBorder(10, 0, 0, 0));
        dbPanel.add(previewP);

        p.add(dbPanel);
        p.add(Box.createVerticalStrut(20));

        // --- FILE PATH SECTION ---
        JPanel pathPanel = createSectionPanel("File Storage Configuration");

        txtImagePath = createStyledTextField("");
        JPanel filePicker = new JPanel(new BorderLayout(5, 0));
        filePicker.setBackground(Color.WHITE);
        JButton btnBrowse = new JButton("📂 Browse");
        btnBrowse.addActionListener(e -> chooseDirectory());

        filePicker.add(txtImagePath, BorderLayout.CENTER);
        filePicker.add(btnBrowse, BorderLayout.EAST);

        addLabelAndField(pathPanel, "Asset/Photo Base Path:", filePicker);

        p.add(pathPanel);
        p.add(Box.createVerticalStrut(30));

        // --- BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setBackground(BG_COLOR);

        JButton btnTest = createRoundedButton("Test Connection", BTN_TEST_COLOR);
        btnTest.addActionListener(e -> testConnection());

        JButton btnSave = createRoundedButton("Save Configuration", BTN_SAVE_COLOR);
        btnSave.addActionListener(e -> saveSettings());

        btnPanel.add(btnTest);
        btnPanel.add(btnSave);
        p.add(btnPanel);

        return p;
    }

    // =========================================================================
    // LOGIC
    // =========================================================================

    private void loadCurrentSettings() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);

            String url = props.getProperty("db.url", "");
            // Parse URL: jdbc:mysql://localhost:3306/barangay_db?serverTimezone...
            try {
                String cleanUrl = url.replace("jdbc:mysql://", "");
                int slashIndex = cleanUrl.indexOf("/");
                String hostPort = cleanUrl.substring(0, slashIndex);
                String params = cleanUrl.substring(slashIndex + 1);

                String[] hp = hostPort.split(":");
                txtHost.setText(hp[0]);
                txtPort.setText(hp.length > 1 ? hp[1] : "3306");

                int qIndex = params.indexOf("?");
                txtDbName.setText(qIndex > -1 ? params.substring(0, qIndex) : params);

            } catch (Exception e) {
                // Fallback if parsing fails
                txtUrlPreview.setText(url);
            }

            txtUsername.setText(props.getProperty("db.username", "root"));
            txtPassword.setText(props.getProperty("db.password", ""));
            txtImagePath.setText(props.getProperty("asset.image.base-path", ""));

            updateUrlPreview();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load " + CONFIG_FILE + "\nUsing defaults.");
        }
    }

    private void saveSettings() {
        Properties props = new Properties();
        String fullUrl = getGeneratedUrl();

        props.setProperty("db.url", fullUrl);
        props.setProperty("db.username", txtUsername.getText());
        props.setProperty("db.password", new String(txtPassword.getPassword()));

        // Escape backslashes for properties file
        // FIXED: Do NOT manually escape. Properties.store() does it automatically.
        String path = txtImagePath.getText();
        props.setProperty("asset.image.base-path", path);
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Database Configuration - Updated by Admin");
            JOptionPane.showMessageDialog(this, "Settings Saved!\nPlease restart the application for changes to take effect.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void testConnection() {
        String url = getGeneratedUrl();
        String user = txtUsername.getText();
        String pass = new String(txtPassword.getPassword());

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                    return conn.isValid(2);
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) JOptionPane.showMessageDialog(AdminServerConfigTab.this, "Connection Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    else JOptionPane.showMessageDialog(AdminServerConfigTab.this, "Connection Failed.", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminServerConfigTab.this, "Connection Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private String getGeneratedUrl() {
        return "jdbc:mysql://" + txtHost.getText() + ":" + txtPort.getText() + "/" + txtDbName.getText() + "?serverTimezone=Asia/Manila";
    }

    private void updateUrlPreview() {
        txtUrlPreview.setText(getGeneratedUrl());
    }

    private void chooseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtImagePath.setText(chooser.getSelectedFile().getAbsolutePath() + File.separator);
        }
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(HEADER_BG);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lblTitle);
        p.add(Box.createVerticalStrut(10));
        p.add(new JSeparator());
        p.add(Box.createVerticalStrut(15));

        return p;
    }

    private void addLabelAndField(JPanel p, String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(labelText);
        lbl.setPreferredSize(new Dimension(150, 30));
        lbl.setFont(MAIN_FONT);

        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        p.add(row);
        p.add(Box.createVerticalStrut(10));
    }

    private JTextField createStyledTextField(String text) {
        JTextField t = new JTextField(text);
        styleField(t);
        // Add listener to update URL preview on type
        t.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) { updateUrlPreview(); }
        });
        return t;
    }

    private void styleField(JTextField t) {
        t.setFont(MAIN_FONT);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private JButton createRoundedButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(25, 40, 25, 40));

        JLabel lblTitle = new JLabel("Server & System Configuration");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        return headerPanel;
    }
}