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
    private JLabel lblLogoPreview;
    private JLabel lblBigLogoPreview;

    // Tab 2 Components
    private JComboBox<String> cbCategory;
    private DefaultTableModel optionsModel;
    private JTable optionsTable;
    private JTextField txtfullBrgyName;
    private AdminSettingsTab tab;
    public AdminSystemConfigTab() {
        this.dao = new SystemConfigDAO();
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));
        this.tab = new AdminSettingsTab();
        // Header
        add(createHeader(), BorderLayout.NORTH);

        // Main Content
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab("General Settings", createGeneralPanel());
        tabbedPane.addTab("Manage Dropdowns", createOptionsPanel());
        tabbedPane.addTab("Document Fees",tab.createDocumentsPanel());
        tabbedPane.addTab("Positions & Roles",tab.createPositionsPanel());

        add(tabbedPane, BorderLayout.CENTER);
        loadSettings();
    }

    // =======================================================
    // TAB 1: GENERAL SETTINGS
    // =======================================================
    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 50, 30, 50));

        // --- 1. SMALL LOGO (Top Left Icon) ---
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setBorder(BorderFactory.createTitledBorder("Barangay Logo:"));

        lblLogoPreview = new JLabel();
        lblLogoPreview.setPreferredSize(new Dimension(120, 120));
        lblLogoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblLogoPreview.setHorizontalAlignment(SwingConstants.CENTER);

        // Load current path from DB
        String logoPath = dao.getConfig("logoPath");
        ImageUtils.displayImage(lblLogoPreview, logoPath, 120, 120);

        JButton btnUpload = new JButton("Change Logo");
        // FIX: Pass the STRING KEY "logoPath", not the variable logoPath
        btnUpload.addActionListener(e -> handleLogoUpload(lblLogoPreview, "logoPath"));

        logoPanel.add(lblLogoPreview);
        logoPanel.add(btnUpload);

        // --- 2. BIG LOGO (For Reports/Certificates) ---
        JPanel bigLogoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        bigLogoPanel.setBackground(Color.WHITE);
        bigLogoPanel.setBorder(BorderFactory.createTitledBorder("Main interface logo:"));

        lblBigLogoPreview = new JLabel();
        lblBigLogoPreview.setPreferredSize(new Dimension(120, 120));
        lblBigLogoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        lblBigLogoPreview.setHorizontalAlignment(SwingConstants.CENTER);

        String bigLogoPath = dao.getConfig("bigLogoPath");
        ImageUtils.displayImage(lblBigLogoPreview, bigLogoPath, 120, 120);

        JButton btnBigUpload = new JButton("Change Big Logo");
        // FIX: Pass the STRING KEY "bigLogoPath"
        btnBigUpload.addActionListener(e -> handleLogoUpload(lblBigLogoPreview, "bigLogoPath"));

        bigLogoPanel.add(lblBigLogoPreview);
        bigLogoPanel.add(btnBigUpload);

        // --- TEXT FIELDS ---
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createTitledBorder("Information"));

        txtBrgyName = createStyledField(dao.getConfig("barangay_name"));
        fieldsPanel.add(new JLabel("Barangay Name:"));
        fieldsPanel.add(txtBrgyName);
        // --- full barangay name ---
        JPanel fieldsPanel1 = new JPanel(new GridLayout(0, 1, 0, 5));
        fieldsPanel1.setBackground(Color.WHITE);
        txtfullBrgyName = createStyledField(dao.getConfig("defaultCtcPlace"));
        fieldsPanel1.add(new JLabel("Barangay hall Location:"));
        fieldsPanel1.add(txtfullBrgyName);

        // Save Button
        JButton btnSave = new JButton("Save General Settings");
        btnSave.setBackground(new Color(46, 204, 113)); // Green
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            dao.updateConfig("barangay_name", txtBrgyName.getText());
            dao.updateConfig("defaultCtcPlace",txtfullBrgyName.getText());
            JOptionPane.showMessageDialog(this, "Settings Saved!");
        });

        panel.add(logoPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(bigLogoPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(fieldsPanel);
        panel.add(fieldsPanel1);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnSave);


        return panel;
    }

    private void handleLogoUpload(JLabel label, String configKey) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            // 1. Create a unique filename based on the key so they don't overwrite each other
            // e.g., "system_logoPath" vs "system_bigLogoPath"
            String fileIdentifier = "system_" + configKey;
            String newPath = ImageUtils.saveImage(file, fileIdentifier);

            // 2. Update DB with the KEY (e.g., "logoPath")
            dao.updateConfig(configKey, newPath);

            // 3. Update UI
            ImageUtils.displayImage(label, newPath, 120, 120);
            JOptionPane.showMessageDialog(this, "Image Updated!");
        }
    }

    // =======================================================
    // TAB 2: OPTIONS MANAGER
    // =======================================================
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.WHITE);

        cbCategory = new JComboBox<>(new String[]{"purok", "civilStatus","natureOfBusiness"});
        cbCategory.addActionListener(e -> loadOptions());

        JButton btnAdd = new JButton("Add Option");
        btnAdd.addActionListener(e -> handleAddOption());


        JButton btnDelete = new JButton("Delete Selected");
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> handleDeleteOption());

        toolbar.add(new JLabel("Category: "));
        toolbar.add(cbCategory);
        toolbar.add(Box.createHorizontalStrut(20));

        toolbar.add(btnAdd);
        toolbar.add(btnDelete);

        optionsModel = new DefaultTableModel(new String[]{"Value"}, 0);
        optionsTable = new JTable(optionsModel);
        optionsTable.setRowHeight(30);
        optionsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = optionsTable.getSelectedRow();
                    if (row != -1) {

                    }
                }
            }
        });

        loadOptions();

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(optionsTable), BorderLayout.CENTER);
        return panel;
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
                        for (String opt : options) {
                            optionsModel.addRow(new Object[]{opt});
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }
    private void loadSettings() {
        new SwingWorker<Void, Void>() {
            String brgyName, logoPath;

            @Override
            protected Void doInBackground() throws Exception {
                // HEAVY TASK: Database + Image I/O
                brgyName = dao.getConfig("barangay_name");
                logoPath = dao.getConfig("logoPath");
                return null;
            }

            @Override
            protected void done() {
                // UPDATE UI
                if (txtBrgyName != null) txtBrgyName.setText(brgyName);

                // Loading image might still be slightly heavy, but safer here
                if (lblLogoPreview != null) {
                    ImageUtils.displayImage(lblLogoPreview, logoPath, 120, 120);
                }
            }
        }.execute();
    }
    private void handleAddOption() {

        String cat = (String) cbCategory.getSelectedItem();
        String dis = "";
        if(cat.equals("purok")){
            dis="Purok ";
        }
        String val = JOptionPane.showInputDialog(this, "add a "+cat,dis);

        if (val != null && !val.trim().isEmpty()) {
            dao.addOption(cat, val.trim());
            loadOptions();
        }
    }

    private void handleDeleteOption() {
        int row = optionsTable.getSelectedRow();
        if (row != -1) {
            String val = (String) optionsModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete '" + val + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION){
                dao.deleteOption(val);
                loadOptions();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
        }
    }

    private JTextField createStyledField(String text) {
        JTextField f = new JTextField(text);
        f.setFont(new Font("Arial", Font.PLAIN, 14));
        return f;
    }

    private JPanel createHeader() {
        JPanel h = new JPanel(new FlowLayout(FlowLayout.LEFT));
        h.setBackground(new Color(40, 40, 40));
        JLabel l = new JLabel("System Configuration");
        l.setFont(new Font("Arial", Font.BOLD, 24));
        l.setForeground(Color.WHITE);
        l.setBorder(new EmptyBorder(10, 20, 10, 20));
        h.add(l);
        return h;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Admin Settings");
            f.setSize(800, 800);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.add(new AdminSystemConfigTab());
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}