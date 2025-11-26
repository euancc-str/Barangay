package org.example.Interface;

import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Documents.DocumentRequest;
import org.example.Documents.DocumentType;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.treasurer.ReceiptPrinter;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.print.PageFormat;

public class SecretaryPrintDocument extends JPanel {

    private JTable requestTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    // New Fields for Filtering
    private JTextField searchField;
    private JComboBox<String> statusFilterBox;

    // --- VISUAL STYLE VARIABLES ---
    private final Color BG_COLOR = new Color(229, 231, 235);
    private final Color HEADER_BG = new Color(40, 40, 40);
    private final Color TABLE_HEADER_BG = new Color(34, 197, 94);
    private final Color BTN_UPDATE_COLOR = new Color(100, 149, 237); // Cornflower Blue
    private final Color BTN_DELETE_COLOR = new Color(255, 77, 77);   // Red

    public SecretaryPrintDocument() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(new JScrollPane(createContentPanel()), BorderLayout.CENTER);
        updateRecordCount();
        loadRequestData();
    }

    public void loadRequestData() {
        // Clear existing data to prevent duplicates if called again
        if (tableModel != null) {
            tableModel.setRowCount(0);
        }

        ResidentDAO residentDAO = new ResidentDAO();
        List<DocumentRequest> documentRequestList = residentDAO.getAllResidentsDocument();

        for(DocumentRequest document : documentRequestList){
            if(document != null) {
                String id = "" + document.getRequestId();
                tableModel.addRow(new Object[]{
                        id,
                        document.getFullName(),
                        document.getName(),
                        document.getPurpose(),
                        document.getRemarks(),
                        document.getRequestDate()
                });
            }
        }
        if (requestTable!=null){
            requestTable.repaint();
        }
    }

    // =========================================================================
    // NEW: FILTER LOGIC
    // =========================================================================
    private void applyFilters() {
        String text = searchField.getText();
        String status = (String) statusFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // 1. Text Search Filter (Across all columns)
        if (text != null && text.trim().length() > 0) {
            filters.add(RowFilter.regexFilter("(?i)" + text));
        }

        // 2. Status Filter (Specific to Status Column, index 4)
        if (status != null && !status.equals("All Status")) {
            // The "^...$" ensures exact match (e.g., "Pending" won't match "Spending")
            filters.add(RowFilter.regexFilter("(?i)^" + status + "$", 4));
        }

        // Apply combined filters
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
        updateRecordCount();
    }

    // =========================================================================
    // 1. DELETE FUNCTIONALITY (With Error Prevention)
    // =========================================================================
    private void handleDelete() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = requestTable.convertRowIndexToModel(selectedRow);
        String reqId = (String) tableModel.getValueAt(modelRow, 0);
        String name = (String) tableModel.getValueAt(modelRow, 1);

        // ERROR PREVENTION: Warning Dialog
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html><body style='width: 250px;'>" +
                        "<b>WARNING: Irreversible Action</b><br><br>" +
                        "Are you sure you want to DELETE Request <b>#" + reqId + "</b><br>" +
                        "for <b>" + name + "</b>?<br><br>" +
                        "This cannot be undone.</body></html>",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // TODO: Call DAO delete method here (e.g., residentDAO.deleteRequest(reqId))
            tableModel.removeRow(modelRow);
            JOptionPane.showMessageDialog(this, "Record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    // 2. UPDATE FUNCTIONALITY
    // =========================================================================
    private void handleUpdate() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = requestTable.convertRowIndexToModel(selectedRow);

        // Retrieve Data
        String currentId = (String) tableModel.getValueAt(modelRow, 0);
        String currentName = (String) tableModel.getValueAt(modelRow, 1);
        String currentDoc = (String) tableModel.getValueAt(modelRow, 2);
        String currentPurpose = (String) tableModel.getValueAt(modelRow, 3);
        String currentStatus = (String) tableModel.getValueAt(modelRow, 4);

        // --- BUILD THE "NICE GUI" DIALOG ---
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Request", true);
        dialog.setSize(600, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(0, 40));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 40, 70, 40));

        // Header
        JLabel titleLabel = new JLabel("Update Request #" + currentId, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(HEADER_BG);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Content
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);

        // 1. Read-Only Fields (Context)
        JTextField txtName = createStyledTextField(currentName);
        txtName.setEditable(false);
        txtName.setBackground(new Color(250, 250, 250));
        addStyledRow(detailsPanel, "Resident Name:", txtName);

        JTextField txtDoc = createStyledTextField(currentDoc);
        txtDoc.setEditable(false);
        txtDoc.setBackground(new Color(250, 250, 250));
        addStyledRow(detailsPanel, "Document Type:", txtDoc);

        // 2. Editable Fields
        String[] statuses = {"Awaiting approval", "confirmed payment", "Rejected"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setSelectedItem(currentStatus);
        cbStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        cbStatus.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Status:", cbStatus);

        JTextArea txtPurpose = new JTextArea(currentPurpose);
        txtPurpose.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPurpose.setLineWrap(true);
        txtPurpose.setEditable(false);
        txtPurpose.setBackground(new Color(250, 250, 250));
        txtPurpose.setWrapStyleWord(true);
        txtPurpose.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));

        JScrollPane scrollPurpose = new JScrollPane(txtPurpose);
        scrollPurpose.setPreferredSize(new Dimension(200, 80));
        addStyledRow(detailsPanel, "Purpose:", scrollPurpose);

        mainPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = createRoundedButton("Cancel", Color.GRAY);
        btnCancel.setPreferredSize(new Dimension(150, 45));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = createRoundedButton("Print document?", BTN_UPDATE_COLOR);
        btnSave.setPreferredSize(new Dimension(200, 45));

        // SAVE ACTION
        if(currentStatus.equals("confirmed payment")){

            btnSave.addActionListener(e -> {

                // Update Table Model
                showDocumentPreview(currentName,currentDoc,currentPurpose, Integer.parseInt(currentId));
                dialog.dispose();
            });
        } else {
            btnSave.setVisible(false);
            btnSave.addActionListener(e -> {
                JOptionPane.showMessageDialog(this,"Unable to print! Payment is not confirmed");

            });
        }


        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void addStyledRow(JPanel panel, String labelText, JComponent field) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (field instanceof JScrollPane) ? 90 : 50));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(new Color(80, 80, 80));
        label.setPreferredSize(new Dimension(150, 35));
        if (field instanceof JScrollPane) label.setVerticalAlignment(SwingConstants.TOP);

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
    private void printPanel(JPanel panelToPrint) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Barangay Document Print");

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics pg, PageFormat pf, int pageNum) {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;

                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());

                // Scale the panel to fit the page
                double scaleX = pf.getImageableWidth() / panelToPrint.getWidth();
                double scaleY = pf.getImageableHeight() / panelToPrint.getHeight();
                double scale = Math.min(scaleX, scaleY); // Maintain aspect ratio
                g2.scale(scale, scale);

                panelToPrint.printAll(g2); // Print the component
                return Printable.PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Printing Failed: " + e.getMessage());
            }
        }
    }
    private static void printReceipt(String residentName, String docType) {
        // 1. Setup the print job
        PrinterJob job = PrinterJob.getPrinterJob();

        // 2. Create your receipt object with real data
       DocumentType documentData = UserDataManager.getInstance().getDocumentTypeByName(docType);
        // (You can fetch the fee amount from your DB/DAO if needed)

        String amount = String.valueOf(documentData.getFee());;
        String orNum = "OR-" + System.currentTimeMillis(); // Generate or fetch
        BarangayStaff staff = UserDataManager.getInstance().getCurrentStaff();
        String cashier = staff.getFirstName() +" " + staff.getMiddleName() + " "+ staff.getLastName(); // Or UserDataManager.getCurrentStaff().getName()

        // 3. Pass it to the printer
        job.setPrintable(new ReceiptPrinter(residentName, docType, amount, orNum, cashier));

        // 4. Show Print Dialog
        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print(); // This opens the "Save as PDF" or selects printer
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }
    private void markAsReleased(int requestId) {
        // SQL to update status and timestamp
        String sql = "UPDATE document_request SET status = 'Released', releasedDate = NOW() WHERE requestId = ?";

        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);
            stmt.executeUpdate();

            // Optional: Log it
            // new SystemLogDAO().addLog("Released Document #" + requestId, "", staffId);

            JOptionPane.showMessageDialog(this, "Document Status updated to Released!");

            // REFRESH YOUR TABLE HERE
           loadRequestData();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDocumentPreview(String name, String docType, String purpose, int requestId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Document Preview", true);
        dialog.setSize(600, 750);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        // --- A. THE DOCUMENT PAPER ---
        JPanel paper = new JPanel();
        paper.setLayout(new BoxLayout(paper, BoxLayout.Y_AXIS));
        paper.setBackground(Color.WHITE);
        paper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(50, 50, 50, 50)
        ));

        // 1. Header
        JLabel header = new JLabel("REPUBLIC OF THE PHILIPPINES");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setFont(new Font("Serif", Font.PLAIN, 16));

        JLabel subHeader = new JLabel("OFFICE OF THE BARANGAY CAPTAIN");
        subHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        subHeader.setFont(new Font("Serif", Font.BOLD, 14));

        // 2. Title
        JLabel title = new JLabel(docType.toUpperCase());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        // 3. Body
        JTextArea body = new JTextArea();
        body.setText("TO WHOM IT MAY CONCERN:\n\n" +
                "This is to certify that " + name.toUpperCase() + ", of legal age, " +
                "is a resident of this Barangay.\n\n" +
                "This certification is issued upon request for the purpose of: \n" +
                purpose.toUpperCase() + ".\n\n" +
                "Issued this " + java.time.LocalDate.now() + ".");

        body.setFont(new Font("Serif", Font.PLAIN, 14));
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Photo Section (The Logic You Requested)
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        photoPanel.setBackground(Color.WHITE);
        photoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel lblPhoto = new JLabel();
        lblPhoto.setPreferredSize(new Dimension(100, 100)); // Passport size-ish
        lblPhoto.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        lblPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Show hand cursor so they know it's clickable
        lblPhoto.setToolTipText("Click to Update Photo");

        // FETCH & DISPLAY PHOTO
        // We need the residentId. Since you only passed requestId/name, let's fetch it first.
        PhotoDAO resDao = new PhotoDAO();
        // Assuming you have a method to find ID by Name (or pass ResidentID to this method directly)
        // Ideally, pass 'residentId' into showDocumentPreview to be safe.
        // For now, assuming UserDataManager holds the current resident context from the main table click:
        int residentId = UserDataManager.getInstance().getResidentId();

        String currentPhotoPath = resDao.getPhotoPath(residentId);
        ImageUtils.displayImage(lblPhoto, currentPhotoPath);

        // CLICK TO EDIT PHOTO
        lblPhoto.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));

                int res = chooser.showOpenDialog(dialog);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();

                    // 1. Save file to folder
                    String newFileName = ImageUtils.saveImage(selectedFile, String.valueOf(residentId));

                    // 2. Update Database
                    resDao.updateResidentPhoto(residentId, newFileName);

                    // 3. Refresh Label immediately
                    ImageUtils.displayImage(lblPhoto, newFileName);

                    JOptionPane.showMessageDialog(dialog, "Photo Updated Successfully!");
                }
            }
        });

        photoPanel.add(lblPhoto);

        // 5. Signatories
        JPanel sigPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sigPanel.setBackground(Color.WHITE);

        // Fetch Captain from DB if possible
        BarangayStaff staffDAO = new StaffDAO().findStaffByPosition("Brgy.Captain");
        String firstName = staffDAO.getFirstName();
        String middleName = staffDAO.getMiddleName();
        String lastName = staffDAO.getLastName();
        String fullName = firstName + " "+middleName + " " + lastName;
        JLabel sig = new JLabel("<html><center>"+fullName+"<br>Barangay Captain</center></html>");
        sig.setFont(new Font("Serif", Font.BOLD, 14));
        sigPanel.add(sig);

        // Assemble Paper
        paper.add(header);
        paper.add(subHeader);
        paper.add(title);
        paper.add(photoPanel); // Added Photo here
        paper.add(body);
        paper.add(sigPanel);

        // --- B. CONTROLS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrint = new JButton("Print & Release");
        btnPrint.setBackground(new Color(76, 175, 80));
        btnPrint.setForeground(Color.WHITE);

        btnPrint.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();

            job.setJobName(docType+"- " + name);
            job.setPrintable(new DocumentPrinter(
                    name,
                    docType,
                    purpose,
                   fullName// Replace with: UserDataManager.getInstance().getCaptainName()
            ));

            if (job.printDialog()) {
                try {
                    job.print();
                    // Then call your database update logic
                    markAsReleased(requestId);
                } catch (PrinterException ex) {
                    ex.printStackTrace();
                }
            }
            dialog.dispose();
        });

        btnPanel.add(btnPrint);

        dialog.add(new JScrollPane(paper), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));

        // --- 1. ACTION BUTTONS ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));


        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // --- 2. SEARCH & FILTER PANEL (Updated) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(BG_COLOR);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Label
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Search Field
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true), new EmptyBorder(5, 5, 5, 5)));

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                applyFilters(); // Call helper method
            }
        });

        // Status Filter Box (New)
        String[] filters = {"All Status", "Awaiting approval", "confirmed payment", "Rejected"};
        statusFilterBox = new JComboBox<>(filters);
        statusFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        statusFilterBox.setBackground(Color.WHITE);
        statusFilterBox.setPreferredSize(new Dimension(150, 30));

        // Add Listener for Status Box
        statusFilterBox.addActionListener(e -> applyFilters());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("  Status:")); // Spacer label
        searchPanel.add(statusFilterBox);

        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));

        // --- 3. TABLE SETUP ---
        // Added "Date" column at the end (Index 5)
        String[] columnNames = {"Request ID", "Resident Name", "Document Type", "Purpose", "Status", "Date"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 14));
        requestTable.setRowHeight(50);
        requestTable.setGridColor(new Color(200, 200, 200));
        requestTable.setSelectionBackground(new Color(200, 240, 240));
        requestTable.setShowVerticalLines(true);
        requestTable.setShowHorizontalLines(true);

        requestTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleUpdate();
            }
        });

        sorter = new TableRowSorter<>(tableModel);

        requestTable.setRowSorter(sorter);

        // SORT BY DATE (Index 5) DEFAULT
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(5, SortOrder.DESCENDING)); // Sort by Date, Descending (Newest First)
        sorter.setSortKeys(sortKeys);
        sorter.addRowSorterListener(e -> updateRecordCount());
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
        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(TABLE_HEADER_BG, 2));
        tableScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setBackground(new Color(229, 231, 235));

        lblRecordCount = new JLabel("Total Records: " + tableModel.getRowCount());
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 13));
        footerPanel.add(lblRecordCount);

        contentPanel.add(footerPanel);
        contentPanel.add(tableScrollPane);
        return contentPanel;
    }

    private JLabel lblRecordCount;
    private void updateRecordCount() {
        int count = requestTable.getRowCount(); // Gets filtered count
        lblRecordCount.setText("Total Records: " + count);
    }


    private JButton createRoundedButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
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
        JLabel lblModule = new JLabel("Admin Dashboard");
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
            JFrame frame = new JFrame("Admin Request Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.add(new SecretaryPrintDocument());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}