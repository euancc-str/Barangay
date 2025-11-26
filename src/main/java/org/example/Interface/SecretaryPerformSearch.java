package org.example.Interface;

import org.example.Admin.SystemLogDAO;
import org.example.Documents.DocumentRequest;
import org.example.ResidentDAO;
import org.example.SerbisyongBarangay.requestaDocumentFrame;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.Resident;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import java.util.List;
import javax.swing.table.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.Month;
import java.util.regex.Pattern;

public class SecretaryPerformSearch extends JPanel {

    private JTable residentTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // Components
    private JLabel lblRecordCount;
    private JTextField searchField;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94);
    private final Color BTN_ADD_COLOR = new Color(76, 175, 80);
    private final Color BTN_UPDATE_COLOR = new Color(100, 149, 237);
    private final Color BTN_DEACTIVATE_COLOR = new Color(255, 77, 77);

    public SecretaryPerformSearch() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);

        loadResidentData();
    }

    public void loadResidentData() {

        if (tableModel != null) {
            tableModel.setRowCount(0);
        }
        ResidentDAO rd = new ResidentDAO();
        List<Resident> residentsList = rd.getAllResidents();
        for(Resident resident : residentsList){
            String name = resident.getFirstName() + " "+ resident.getLastName();
            if(resident != null) {
                String filterAddress = "Alawihao";

                String status = "Not qualified";
                if(Pattern.compile(filterAddress,Pattern.CASE_INSENSITIVE).matcher(resident.getAddress()).find()){
                    status = "Qualified for request";
                }
                tableModel.addRow(new Object[]{""+resident.getResidentId(),
                        name,
                        resident.getGender(),
                        ""+resident.getAge(),
                        resident.getAddress(),status
                        });
            }
        }
        if(residentTable!=null){
            residentTable.repaint();
        }
        updateRecordCount();
    }

    private void updateRecordCount() {
        if (lblRecordCount != null && residentTable != null) {
            int count = residentTable.getRowCount();
            lblRecordCount.setText("Total Records: " + count);
        }
    }

    private void handleUpdate() {
        int selectedRow = residentTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = residentTable.convertRowIndexToModel(selectedRow);

        // Retrieve values from table
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);
        String gender = (String) tableModel.getValueAt(modelRow, 2);
        String age = (String) tableModel.getValueAt(modelRow, 3);
        String address = (String) tableModel.getValueAt(modelRow, 4);
        ResidentDAO dao = new ResidentDAO();
        Resident resident = dao.findResidentById(Integer.parseInt(id));

        UserDataManager.getInstance().setCurrentResident(resident);

        showUpdateResidentDialog(id, name, gender, age, address, modelRow);
    }

    private void showUpdateResidentDialog(String id, String name, String gender, String age, String address, int modelRow) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Resident", true);
        dialog.setSize(550, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Viewing Resident Info", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(HEADER_BG);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- FIELDS ---
        JTextField txtId = createStyledTextField(id);
        txtId.setEditable(false);
        txtId.setBackground(new Color(250, 250, 250));
        addStyledRow(detailsPanel, "Resident ID:", txtId);

        JTextField txtName = createStyledTextField(name);
        addStyledRow(detailsPanel, "Full Name:", txtName);
        txtName.setEditable(false);
        String[] genders = {"Male", "Female"};
        JComboBox<String> cbGender = new JComboBox<>(genders);
        cbGender.setSelectedItem(gender);
        cbGender.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Gender:", cbGender);

        JTextField txtAge = createStyledTextField(age);
        addStyledRow(detailsPanel, "Age:", txtAge);

        JTextField txtAddress = createStyledTextField(address);
        addStyledRow(detailsPanel, "Address:", txtAddress);

        // --- SEPARATOR ---
        detailsPanel.add(Box.createVerticalStrut(15));
        detailsPanel.add(Box.createVerticalStrut(10));


        // Add Hint for Password
        JPanel passPanel = new JPanel(new BorderLayout());
        passPanel.setBackground(Color.WHITE);

        JLabel lblHint = new JLabel(" (Leave blank to keep current)");
        lblHint.setFont(new Font("Arial", Font.ITALIC, 11));
        lblHint.setForeground(Color.GRAY);
        passPanel.add(lblHint, BorderLayout.SOUTH);

        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = createRoundedButton("Cancel", Color.GRAY);
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = createRoundedButton("Request Resident a document", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(250, 55));

        btnSave.addActionListener(e -> {

            int confirm = JOptionPane.showConfirmDialog(dialog, "open request", "Confirm", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                dialog.dispose();
                requestaDocumentFrame frame = new requestaDocumentFrame();
                frame.setVisible(true);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    // =========================================================================
    // 3. ADD RESIDENT LOGIC
    // =========================================================================


    // Add this class at the bottom of your file
    static class PhoneDocumentFilter extends javax.swing.text.DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (string == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + string + currentText.substring(offset);

            if (isValidPhone(newText)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs) throws javax.swing.text.BadLocationException {
            if (text == null) return;
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

            if (isValidPhone(newText)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        // YOUR LOGIC HERE
        private boolean isValidPhone(String text) {
            if (text.isEmpty()) return true;
            if (!text.matches("\\d*")) return false; // Digits only
            if (text.length() > 11) return false;    // Max 11
            // Strict check: Must start with 09 if length is 2 or more
            if (text.length() >= 2 && !text.startsWith("09")) return false;
            return true;
        }
    }
    // =========================================================================
    // GUI SETUP
    // =========================================================================

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 5, 5, 5)));

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                updateRecordCount();
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- TABLE SETUP ---
        String[] columnNames = {"ID", "Full Name", "Gender", "Age", "Address", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        residentTable = new JTable(tableModel);
        residentTable.setFont(new Font("Arial", Font.PLAIN, 14));
        residentTable.setRowHeight(50);
        residentTable.setGridColor(new Color(200, 200, 200));
        residentTable.setSelectionBackground(new Color(200, 240, 240));
        residentTable.setShowVerticalLines(true);
        residentTable.setShowHorizontalLines(true);

        residentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleUpdate();
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(0, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Integer i1 = Integer.parseInt(s1);
                    Integer i2 = Integer.parseInt(s2);
                    return i1.compareTo(i2);
                } catch (NumberFormatException e) {
                    return s1.compareTo(s2); // Fallback
                }
            }
        });

        sorter.setComparator(3, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Integer i1 = Integer.parseInt(s1);
                    Integer i2 = Integer.parseInt(s2);
                    return i1.compareTo(i2);
                } catch (Exception e) {
                    return s1.compareTo(s2);
                }
            }
        });

        residentTable.setRowSorter(sorter);

        sorter.addRowSorterListener(new RowSorterListener() {
            @Override
            public void sorterChanged(RowSorterEvent e) {
                updateRecordCount();
            }
        });

        JTableHeader header = residentTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < residentTable.getColumnCount(); i++) {
            residentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(residentTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(TABLE_HEADER_BG, 2));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        contentPanel.add(tableScrollPane);

        // Footer Count
        contentPanel.add(Box.createVerticalStrut(10));
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(BG_COLOR);

        lblRecordCount = new JLabel("Total Records: 0");
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        lblRecordCount.setForeground(new Color(80, 80, 80));

        footerPanel.add(lblRecordCount);
        contentPanel.add(footerPanel);

        return contentPanel;
    }

    // Visual Helpers
    private void addStyledRow(JPanel panel, String labelText, JComponent field) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
        label.setPreferredSize(new Dimension(150, 35));

        if (field instanceof JPanel) label.setVerticalAlignment(SwingConstants.TOP);

        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.setBackground(Color.WHITE);
        fieldWrapper.setBorder(new EmptyBorder(5, 0, 15, 0));
        fieldWrapper.add(field, BorderLayout.CENTER);

        rowPanel.add(label, BorderLayout.WEST);
        rowPanel.add(fieldWrapper, BorderLayout.CENTER);
        panel.add(rowPanel);
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(HEADER_BG);
                        g2.fillRoundRect(x, y, width, height, 30, 30);
                    }
                }, new EmptyBorder(25, 40, 25, 40)));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(HEADER_BG);
        JLabel lblSystem = new JLabel("Barangay System");
        lblSystem.setFont(new Font("Arial", Font.BOLD, 26));
        lblSystem.setForeground(Color.WHITE);
        JLabel lblModule = new JLabel("Resident Management");
        lblModule.setFont(new Font("Arial", Font.BOLD, 22));
        lblModule.setForeground(Color.WHITE);
        titlePanel.add(lblSystem);
        titlePanel.add(lblModule);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        return headerPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            JFrame frame = new JFrame("Admin Resident Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new SecretaryPerformSearch());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
