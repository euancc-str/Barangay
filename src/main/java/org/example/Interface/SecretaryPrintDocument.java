package org.example.Interface;


import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Documents.DocumentRequest;
import org.example.Documents.DocumentType;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;
// Assuming this is where your printer class is
// import org.example.treasurer.ReceiptPrinter; // Keep if used elsewhere


import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.time.format.DateTimeFormatter;


public class SecretaryPrintDocument extends JPanel {


    private JTable requestTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;


    // New Fields for Filtering
    private JTextField searchField;
    private JComboBox<String> statusFilterBox;
    private JComboBox<String> dateFilterBox; // Date Filter


    private JLabel lblRecordCount;


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


        loadRequestData();
    }

    // =========================================================================
    //  FIXED DATA LOADING (Safe Threading)
    // =========================================================================
    public void loadRequestData() {
        new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                ResidentDAO residentDAO = new ResidentDAO();
                List<DocumentRequest> rawList = residentDAO.getAllResidentsDocument();
                List<Object[]> processedRows = new ArrayList<>();

                for (DocumentRequest doc : rawList) {
                    if (doc != null) {
                        Object dateObj = doc.getRequestDate();
                        LocalDateTime finalDate = null;
                        try {
                            if (dateObj instanceof java.sql.Timestamp) {
                                finalDate = ((java.sql.Timestamp) dateObj).toLocalDateTime();
                            } else if (dateObj != null) {
                                String s = dateObj.toString().replace("T", " ");
                                if (s.length() >= 19) finalDate = LocalDateTime.parse(s.substring(0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                else if (s.length() >= 16) finalDate = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                                else if (s.length() >= 10) finalDate = LocalDate.parse(s.substring(0, 10)).atStartOfDay();
                            }
                        } catch (Exception ignored) {}

                        processedRows.add(new Object[]{
                                "" + doc.getRequestId(),
                                doc.getFullName(),
                                doc.getName(),
                                doc.getPurpose(),
                                doc.getStatus(),
                                finalDate
                        });
                    }
                }
                return processedRows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    if (tableModel != null) {
                        // Safely clear and reload
                        RowFilter<? super DefaultTableModel, ? super Integer> currentFilter = null;
                        if (sorter != null) {
                            currentFilter = sorter.getRowFilter();
                            sorter.setRowFilter(null);
                        }

                        tableModel.setRowCount(0);
                        for (Object[] row : rows) {
                            tableModel.addRow(row);
                        }

                        if (sorter != null) sorter.setRowFilter(currentFilter);
                        if (statusFilterBox != null) applyFilters();
                    }
                    updateRecordCount();
                    if (requestTable != null) requestTable.repaint();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.execute();
    }

    // =========================================================================
    //  FILTER LOGIC
    // =========================================================================
    private void applyFilters() {
        if (sorter == null) return;

        String text = searchField.getText();
        String status = (String) statusFilterBox.getSelectedItem();
        String dateFilter = (String) dateFilterBox.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (text != null && !text.trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + text));
        }

        if (status != null && !status.equals("All Status")) {
            filters.add(RowFilter.regexFilter("(?i)^" + status + "$", 4));
        }

        if (dateFilter != null && !dateFilter.equals("All Time")) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<?, ?> entry) {
                    Object val = entry.getValue(5);
                    if (!(val instanceof LocalDateTime)) return false;
                    LocalDate rowDate = ((LocalDateTime) val).toLocalDate();
                    LocalDate today = LocalDate.now();

                    if (dateFilter.equals("Today")) return rowDate.isEqual(today);
                    if (dateFilter.equals("This Week")) return !rowDate.isBefore(today.minusDays(7)) && !rowDate.isAfter(today);
                    if (dateFilter.equals("This Month")) return rowDate.getMonth() == today.getMonth() && rowDate.getYear() == today.getYear();
                    return true;
                }
            });
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        updateRecordCount();
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
        String[] statuses = {"Awaiting approval", "confirmed payment", "Rejected", "Paid", "Approved", "Released"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setSelectedItem(currentStatus);
        cbStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        cbStatus.setBackground(Color.WHITE);
        addStyledRow(detailsPanel, "Status:", cbStatus);


        JTextArea txtPurposeArea = new JTextArea(currentPurpose);
        txtPurposeArea.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPurposeArea.setLineWrap(true);
        txtPurposeArea.setEditable(false);
        txtPurposeArea.setBackground(new Color(250, 250, 250));
        txtPurposeArea.setWrapStyleWord(true);
        txtPurposeArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 5, 5, 5)));


        JScrollPane scrollPurpose = new JScrollPane(txtPurposeArea);
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
        JButton btnDelete = createRoundedButton("Delete", Color.RED);
        btnDelete.setPreferredSize(new Dimension(150, 45));
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "<html><body><b style='color:red'>WARNING:</b><br>" +
                            "Are you sure you want to delete this request?<br>" +
                            "This action cannot be undone.</body></html>",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                new StaffDAO().deleteRequest(Integer.parseInt(currentId));

                JOptionPane.showMessageDialog(dialog, "Request deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                loadRequestData(); // Refresh the list
            }
        });


        // CHECK STATUS FOR PRINTING
        // Allow printing if Paid, Approved, or Released (basically anything after payment)
        if(currentStatus.equalsIgnoreCase("confirmed payment") ||
                currentStatus.equalsIgnoreCase("Paid") ||
                currentStatus.equalsIgnoreCase("Approved") ||
                currentStatus.equalsIgnoreCase("Released")){


            btnSave.addActionListener(e -> {
                // Finding resident ID by name (Ensure this method exists in your DAO)
                ResidentDAO resDAO = new ResidentDAO();
                int residentId = resDAO.findResidentsIdByFullName(currentName);


                showDocumentPreview(currentName, currentDoc, currentPurpose, Integer.parseInt(currentId), residentId);
                dialog.dispose();
            });
        } else {
            btnSave.setVisible(false);
            JLabel lblStatusInfo = new JLabel("Payment not confirmed yet");
            lblStatusInfo.setForeground(Color.RED);
            btnPanel.add(lblStatusInfo);
        }


        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        btnPanel.add(btnDelete);
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
    // =========================================================================
    // PRINT PREVIEW & PHOTO LOGIC
    // =========================================================================
    private void showDocumentPreview(String name, String docType, String purpose, int requestId, int residentId) {
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


        // 4. Photo Section (Only for Barangay Clearance)
        if (docType.trim().equalsIgnoreCase("Barangay Clearance")) {
            JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            photoPanel.setBackground(Color.WHITE);
            photoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            photoPanel.setPreferredSize(new Dimension(600, 110));
            photoPanel.setMaximumSize(new Dimension(600, 110));


            JLabel lblPhoto = new JLabel();
            lblPhoto.setPreferredSize(new Dimension(100, 100)); // Passport size-ish
            lblPhoto.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            lblPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Show hand cursor so they know it's clickable
            lblPhoto.setToolTipText("Click to Update Photo");


            // FETCH & DISPLAY PHOTO
            PhotoDAO resDao = new PhotoDAO();
            String currentPhotoPath = resDao.getPhotoPath(residentId);
            // *** FIXED: Pass explicit dimensions to avoid error ***
            ImageUtils.displayImage(lblPhoto, currentPhotoPath, 100, 100);


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
                        ImageUtils.displayImage(lblPhoto, newFileName, 100, 100);


                        JOptionPane.showMessageDialog(dialog, "Photo Updated Successfully!");
                    }
                }
            });


            photoPanel.add(lblPhoto);
            paper.add(photoPanel);
        }


        // 5. Signatories
        JPanel sigPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sigPanel.setBackground(Color.WHITE);


        // Fetch Captain from DB if possible
        BarangayStaff staffDAO = new StaffDAO().findStaffByPosition("Brgy.Captain");
        String fullName = "";
        if (staffDAO != null) {
            String fName = staffDAO.getFirstName() != null ? staffDAO.getFirstName() : "";
            String mName = staffDAO.getMiddleName() != null ? staffDAO.getMiddleName() : "";
            String lName = staffDAO.getLastName() != null ? staffDAO.getLastName() : "";
            fullName = fName + " " + mName + " " + lName;
        } else {
            fullName = "HON. CARDO DALISAY"; // Fallback
        }


        JLabel sig = new JLabel("<html><center>"+fullName+"<br>Barangay Captain</center></html>");
        sig.setFont(new Font("Serif", Font.BOLD, 14));
        sigPanel.add(sig);


        // Assemble Paper
        paper.add(header);
        paper.add(subHeader);
        paper.add(title);
        // Photo added above if condition met
        paper.add(body);
        paper.add(sigPanel);


        // --- B. CONTROLS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPrint = new JButton("Print & Release");
        btnPrint.setBackground(new Color(76, 175, 80));
        btnPrint.setForeground(Color.WHITE);


        // Inside showDocumentPreview(...)


        btnPrint.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName(docType + " - " + name);


            // 1. SET PAPER SIZE (Standard A4 or Letter)
            PageFormat pf = job.defaultPage();
            Paper paperSettings = pf.getPaper();
            paperSettings.setImageableArea(0, 0, paperSettings.getWidth(), paperSettings.getHeight()); // No margins, let printer handle it
            pf.setPaper(paperSettings);
            String ctcPlace = new SystemConfigDAO().getConfig("defaultCtcPlace");
            // 2. SELECT PRINTER BASED ON DOCUMENT TYPE
            ResidentDAO dao = new ResidentDAO();
            org.example.Users.Resident res = dao.findResidentById(residentId);


            if (docType.equalsIgnoreCase("Barangay Clearance")) {


                // A. Fetch Full Resident Data (We need Address, DOB, CTC, etc.)


                if (res != null) {


                    // B. Pass all data to your new Printer Class
                    job.setPrintable(new org.example.Interface.BarangayClearancePrinter(
                            res.getFirstName() + " "+res.getMiddleName()+ " " + res.getLastName(),
                            res.getAddress(),       // Address
                            res.getGender(),        // Gender
                            res.getDob() != null ? res.getDob().toString() : "N/A", // DOB
                            String.valueOf(res.getAge()), // Age
                            res.getCivilStatus(),   // Civil Status
                            purpose,                // Purpose
                            res.getCtcNumber(),     // CTC No
                            res.getCtcDateIssued() != null ? res.getCtcDateIssued().toString() : "", // CTC Date
                            ctcPlace, // CTC Place
                            residentId, // ID (for Photo lookup)
                            requestId
                    ), pf);
                }
            }else if (docType.equalsIgnoreCase("Business Clearance")) {
                // Fetch Resident info for CTC details
                job.setPrintable(new BusinessClearancePrinter(
                        name, // Proprietor Name
                        res.getAddress(),
                        purpose, // Contains Business Details/Nature
                        res.getCtcNumber(),
                        res.getCtcDateIssued() != null ? res.getCtcDateIssued().toString() : "",
                        ctcPlace,requestId
                ), pf);
            }
            else {
                // Fallback for other documents (Indigency, etc.) - Use the Panel Print
                // Or create specific printer classes for them later
                Resident resident = new ResidentDAO().findResidentById(residentId);
                String purok;
                purok = resident.getPurok();
                int age = resident.getAge();
                job.setPrintable(new org.example.Interface.DocumentPrinter(name, docType, purpose, purok,age), pf);
            }


            // 3. SHOW PRINT DIALOG
            if (job.printDialog()) {
                try {
                    job.print();
                    // 4. Update Status in DB
                    markAsReleased(requestId);
                    dialog.dispose();
                } catch (PrinterException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Printing Error: " + ex.getMessage());
                }
            }
        });


        btnPanel.add(btnPrint);


        dialog.add(new JScrollPane(paper), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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


    private void markAsReleased(int requestId) {
        // SQL to update status and timestamp
        String sql = "UPDATE document_request SET status = 'Released', releasedDate = NOW() WHERE requestId = ?";


        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setInt(1, requestId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Document Status updated to Released!");


            // REFRESH YOUR TABLE HERE
            loadRequestData();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(35, 60, 35, 60));


        // --- 1. ACTION BUTTONS ---
        JButton btnRefresh = createRoundedButton("Refresh", new Color(100, 100, 100));
        btnRefresh.setPreferredSize(new Dimension(80, 30));
        btnRefresh.addActionListener(e -> loadRequestData());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        contentPanel.add(buttonPanel);
        contentPanel.add(btnRefresh);
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


        // Status Filter Box (Existing)
        String[] filters = {"All Status", "Pending", "Approved", "Released"};
        statusFilterBox = new JComboBox<>(filters);
        statusFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        statusFilterBox.setBackground(Color.WHITE);
        statusFilterBox.setPreferredSize(new Dimension(150, 30));
        statusFilterBox.addActionListener(e -> applyFilters());


        // [ADDED] Date Filter Box
        JLabel dateLabel = new JLabel("  Date:");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        String[] dateFilters =  {"Today", "This Week", "This Month","All Time"};
        dateFilterBox = new JComboBox<>(dateFilters);
        dateFilterBox.setFont(new Font("Arial", Font.PLAIN, 14));
        dateFilterBox.setBackground(Color.WHITE);
        dateFilterBox.setPreferredSize(new Dimension(120, 30));
        dateFilterBox.addActionListener(e -> applyFilters());


        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("  Status:"));
        searchPanel.add(statusFilterBox);
        searchPanel.add(dateLabel); // Add Date Label
        searchPanel.add(dateFilterBox); // Add Date Box


        contentPanel.add(searchPanel);
        contentPanel.add(Box.createVerticalStrut(10));


        // --- 3. TABLE SETUP ---
        String[] columnNames = {"Request ID", "Resident Name", "Document Type", "Purpose", "Status", "Date"};


        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };


        requestTable = new JTable(tableModel);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 14));
        requestTable.setRowHeight(50);
        requestTable.setGridColor(new Color(173, 216, 230));
        requestTable.setSelectionBackground(new Color(200, 240, 240));
        requestTable.setBackground(new Color(144,213,255));
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
        sortKeys.add(new RowSorter.SortKey(5, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.addRowSorterListener(e -> updateRecordCount());


        // Ensure ID sorting treats IDs as numbers
        sorter.setComparator(0, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                try {
                    Integer i1 = Integer.parseInt(s1);
                    Integer i2 = Integer.parseInt(s2);
                    return i1.compareTo(i2);
                } catch (NumberFormatException e) {
                    return s1.compareTo(s2);
                }
            }
        });


        JTableHeader header = requestTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(header.getWidth(), 50));


        // Create custom header renderer with cerulean gradient light blue
        header.setDefaultRenderer(new GradientHeaderRenderer());


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < requestTable.getColumnCount(); i++) {
            requestTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        JScrollPane tableScrollPane = new JScrollPane(requestTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
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


    // Custom Table Header Renderer with Cerulean Gradient Light Blue
    private class GradientHeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Arial", Font.BOLD, 15));
            setForeground(Color.BLACK);
            setOpaque(true);
            return this;
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();


            // Cerulean gradient light blue colors
            Color startColor = new Color(173, 216, 230); // Light blue
            Color endColor = new Color(70, 130, 180);   // Cerulean blue


            GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    getWidth(), getHeight(), endColor
            );


            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());


            // Paint the text
            super.paintComponent(g2d);
            g2d.dispose();
        }
    }


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
        headerPanel.setBackground(new Color(70, 130, 180));


        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new AbstractBorder() {
                    @Override
                    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(70, 130, 180));
                        g2.fillRoundRect(x, y, width, height, 30, 30);
                    }
                }, new EmptyBorder(25, 40, 25, 40)));


        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(70, 130, 180));
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

