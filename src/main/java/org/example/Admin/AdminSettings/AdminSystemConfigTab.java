package org.example.Admin.AdminSettings;


import org.example.Admin.AdminSettings.ImageUtils;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;


public class AdminSystemConfigTab extends JPanel {


    private SystemConfigDAO dao;


    // Tab 1 Components
    private JTextField txtBrgyName;
    private JLabel lblLogoPreview; // Left Logo (Barangay)
    private JLabel lblBigLogoPreview; // Center/Watermark Logo
    private JLabel lblDaetLogoPreview; // Right Logo (Daet)


    // Tab 2 Components
    private JComboBox<String> cbCategory;
    private DefaultTableModel optionsModel;
    private JTable optionsTable;


    // Buttons promoted to class level to control visibility
    private JButton btnAddOption;
    private JButton btnDelOption;


    private JTextField txtfullBrgyName;
    private AdminSettingsTab tab;


    // Modern Color Scheme
    private final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private final Color SECONDARY_COLOR = new Color(107, 114, 128);
    private final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);
    private final Color HEADER_BG = new Color(30, 41, 59);
    private final Color BORDER_COLOR = new Color(229, 231, 235);


    public AdminSystemConfigTab() {
        this.dao = new SystemConfigDAO();
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        this.tab = new AdminSettingsTab();


        add(createHeader(), BorderLayout.NORTH);


        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(PRIMARY_COLOR);


        tabbedPane.addTab("General Settings", createGeneralPanel());
        tabbedPane.addTab("Manage Dropdowns", createOptionsPanel());
        tabbedPane.addTab("Document Fees", tab.createDocumentsPanel());
        tabbedPane.addTab("Positions & Roles", tab.createPositionsPanel());


        add(tabbedPane, BorderLayout.CENTER);


        // Load data on startup
        loadSettings();
    }


    // =======================================================
    // TAB 1: GENERAL SETTINGS
    // =======================================================
    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(30, 50, 30, 50));


        // Container for Logos
        JPanel logosContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        logosContainer.setBackground(BACKGROUND_COLOR);
        logosContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));


        // --- 1. BARANGAY LOGO (Left) ---
        logosContainer.add(createLogoUploader("Barangay Logo", "logoPath", lblLogoPreview = createLogoLabel()));


        // --- 2. DAET LOGO (Right) ---
        logosContainer
                .add(createLogoUploader("Daet Logo (Right)", "daetLogoPath", lblDaetLogoPreview = createLogoLabel()));


        // --- 3. BIG LOGO (Center/Watermark) ---
        logosContainer
                .add(createLogoUploader("Main Interface Logo", "bigLogoPath", lblBigLogoPreview = createLogoLabel()));


        panel.add(logosContainer);
        panel.add(Box.createVerticalStrut(20));


        // --- TEXT FIELDS ---
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        fieldsPanel.setBackground(CARD_COLOR);
        fieldsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_COLOR), "Information"),
                new EmptyBorder(10, 10, 10, 10)));


        txtBrgyName = createStyledField("");
        fieldsPanel.add(createStyledLabel("Barangay Name:"));
        fieldsPanel.add(txtBrgyName);


        // --- full barangay name ---
        JPanel fieldsPanel1 = new JPanel(new GridLayout(0, 1, 0, 5));
        fieldsPanel1.setBackground(CARD_COLOR);
        txtfullBrgyName = createStyledField("");
        fieldsPanel1.add(createStyledLabel("Barangay Hall Location:"));
        fieldsPanel1.add(txtfullBrgyName);


        // Save Button
        JButton btnSave = createModernButton("Save General Settings", SUCCESS_COLOR);
        btnSave.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to save these general settings?",
                    "Confirm Save",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );


            if (confirm == JOptionPane.YES_OPTION) {
                dao.updateConfig("barangay_name", txtBrgyName.getText());
                dao.updateConfig("defaultCtcPlace", txtfullBrgyName.getText());

                JOptionPane.showMessageDialog(this, "Settings Saved Successfully!");
            }
        });


        panel.add(fieldsPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(fieldsPanel1);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnSave);


        return panel;
    }


    // --- Helper to Create a Logo Box ---
    private JPanel createLogoUploader(String title, String configKey, JLabel previewLabel) {
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        logoPanel.setBackground(CARD_COLOR);
        logoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER_COLOR), title),
                new EmptyBorder(10, 10, 10, 10)));


        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_COLOR);


        previewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(previewLabel);
        content.add(Box.createVerticalStrut(10));


        JButton btnUpload = createModernButton("Change", PRIMARY_COLOR);
        btnUpload.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUpload.addActionListener(e -> handleLogoUpload(previewLabel, configKey));


        content.add(btnUpload);
        logoPanel.add(content);


        return logoPanel;
    }


    private JLabel createLogoLabel() {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(120, 120));
        label.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground(new Color(243, 244, 246));
        label.setOpaque(true);
        return label;
    }


    private void handleLogoUpload(JLabel label, String configKey) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));


        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String fileIdentifier = "system_" + configKey;
            String newPath = ImageUtils.saveImage(file, fileIdentifier);


            if (newPath != null) {
                dao.updateConfig(configKey, newPath);
                ImageUtils.displayImage(label, newPath, 120, 120);
                JOptionPane.showMessageDialog(this, "Image Updated Successfully!");
            }
        }

    }


    private void loadSettings() {
        new SwingWorker<Void, Void>() {
            String brgyName, locName;
            String logoPath, bigLogoPath, daetLogoPath;


            @Override
            protected Void doInBackground() throws Exception {
                brgyName = dao.getConfig("barangay_name");
                locName = dao.getConfig("defaultCtcPlace");
                logoPath = dao.getConfig("logoPath");
                bigLogoPath = dao.getConfig("bigLogoPath");
                daetLogoPath = dao.getConfig("daetLogoPath");
                return null;
            }


            @Override
            protected void done() {
                if (txtBrgyName != null)
                    txtBrgyName.setText(brgyName);
                if (txtfullBrgyName != null)
                    txtfullBrgyName.setText(locName);
                if (lblLogoPreview != null)
                    ImageUtils.displayImage(lblLogoPreview, logoPath, 120, 120);
                if (lblBigLogoPreview != null)
                    ImageUtils.displayImage(lblBigLogoPreview, bigLogoPath, 120, 120);
                if (lblDaetLogoPreview != null)
                    ImageUtils.displayImage(lblDaetLogoPreview, daetLogoPath, 120, 120);
            }
        }.execute();
    }


    // =======================================================
    // TAB 2: OPTIONS MANAGER
    // =======================================================
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));


        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(BACKGROUND_COLOR);


        cbCategory = new JComboBox<>(dao.getOptionsCategory());
        cbCategory.setBackground(CARD_COLOR);
        cbCategory.setFont(new Font("Arial", Font.PLAIN, 14));


        // Listener: When category changes, check visibility and load data
        cbCategory.addActionListener(e -> {
            updateButtonVisibility();
            loadOptions();
        });


        // Initialize Buttons
        btnAddOption = createModernButton("Add Option", SUCCESS_COLOR);
        btnAddOption.addActionListener(e -> handleAddOption());


        btnDelOption = createModernButton("Delete Selected", DANGER_COLOR);
        btnDelOption.addActionListener(e -> handleDeleteOption());


        toolbar.add(createStyledLabel("Category: "));
        toolbar.add(cbCategory);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnAddOption);
        toolbar.add(btnDelOption);


        optionsModel = new DefaultTableModel(new String[] { "Value" }, 0);
        optionsTable = new JTable(optionsModel);
        optionsTable.setRowHeight(35);
        optionsTable.setBackground(CARD_COLOR);
        optionsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        optionsTable.setGridColor(BORDER_COLOR);
        optionsTable.getTableHeader().setBackground(PRIMARY_COLOR);
        optionsTable.getTableHeader().setForeground(Color.BLACK);
        optionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));


        // Initial setup
        updateButtonVisibility();
        loadOptions();


        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(optionsTable), BorderLayout.CENTER);
        return panel;
    }


    // --- BUTTON VISIBILITY LOGIC ---
    private void updateButtonVisibility() {
        Object selected = cbCategory.getSelectedItem();
        if (selected == null)
            return;


        // Convert to lowercase for safer comparison
        String cat = selected.toString().trim().toLowerCase();


        // LOGIC: Hide buttons if "Civil Status" (or "Sex") is selected
        // Using "contains" covers "Civil Status", "civil status", etc.
        boolean isRestricted = cat.contains("civil status") || cat.equals("sex");


        if (btnAddOption != null)
            btnAddOption.setVisible(!isRestricted);
        if (btnDelOption != null)
            btnDelOption.setVisible(!isRestricted);
    }


    private void loadOptions() {
        String category = (String) cbCategory.getSelectedItem();
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return dao.getOptions(category);
            }


            @Override
            protected void done() {
                try {
                    List<String> options = get();
                    if (optionsModel != null) {
                        optionsModel.setRowCount(0);
                        for (String opt : options)
                            optionsModel.addRow(new Object[] { opt });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    // =======================================================
    // ADD OPTION
    // =======================================================
    private void handleAddOption() {
        String cat = (String) cbCategory.getSelectedItem();
        if (cat == null)
            return;


        String catLower = cat.trim().toLowerCase();
        String val = JOptionPane.showInputDialog(this, "Add new " + cat + ":");


        // 1. Validation: Block Null/Empty
        if (val == null || val.trim().isEmpty()) {
            if (val != null) {
                JOptionPane.showMessageDialog(this, "Input cannot be empty.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
            }
            return;
        }


        String input = val.trim();


        // 2. Validation for Incident Type
        if (catLower.contains("incident")) {
            if (!input.matches("^[a-zA-Z\\sñÑ]+$")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Incident Type.\nInput must contain LETTERS ONLY.\n(No numbers or special characters allowed)",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }


        // 3. Validation for Purok
        else if (catLower.contains("purok")) {
            if (!input.matches("^[a-zA-Z0-9\\sñÑ]+$")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Purok Name.\nSpecial characters are not allowed.\n(Only Letters, Numbers, and Spaces)",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }


        dao.addOption(cat, input);
        loadOptions();
    }


    private void handleDeleteOption() {
        int row = optionsTable.getSelectedRow();
        if (row != -1) {
            String val = (String) optionsModel.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(this, "Delete '" + val + "'?", "Confirm",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dao.deleteOption(val);
                loadOptions();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
        }
    }


    // --- Visual Helpers ---
    private JTextField createStyledField(String text) {
        JTextField f = new JTextField(text);
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(8, 10, 8, 10)));
        f.setBackground(CARD_COLOR);
        return f;
    }


    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(SECONDARY_COLOR);
        return label;
    }


    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE); // Better contrast
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }


            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        return button;
    }


    private JPanel createHeader() {
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT));
        h.setBackground(HEADER_BG);
        JLabel l = new JLabel("System Configuration");
        l.setFont(new Font("Arial", Font.BOLD, 24));
        l.setForeground(Color.WHITE);
        l.setBorder(new EmptyBorder(10, 20, 10, 20));
        h.add(l);
        return h;
    }
}

