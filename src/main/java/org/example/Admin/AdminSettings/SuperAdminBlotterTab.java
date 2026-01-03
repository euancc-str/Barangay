package org.example.Admin.AdminSettings;


import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.Admin.AdminAdministrationTab;
import org.example.Admin.SystemLogDAO;
import org.example.BlotterCaseDAO;
import org.example.Captain.CaptainSchedule;
import org.example.Captain.CaptainScheduleDAO;
import org.example.UserDataManager;
import org.example.Users.BlotterCase;
import org.example.utils.AutoRefresher;


import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class SuperAdminBlotterTab extends JPanel {


    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblRecordCount;
    private JTextField searchField;


    // Fields for validation
    private JTextField txtCaseNo;
    private JTextField txtDate;
    private JTextField txtTime;
    private JTextField txtComplainant;
    private JTextField txtRespondent;
    private JTextField txtVictim;
    private JTextField txtLocation;
    private JTextArea txtNarrative;
    private JTextField txtWitness;
    private JTextField txtOfficer;
    private JTextField txtResolution;
    private JTextField txtHearing;
    private JComboBox<String> cbType;
    private JComboBox<String> cbStatus;
    private JTextField txtIncidentDate;
    private JTextField txtIncidentTime;
    private JTextField txtSummonDate;


    // Colors matching AdminBlotterTab
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color SECTION_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(220, 226, 234);
    private final Color TABLE_HEADER_BG = new Color(52, 152, 219);
    private final Color BTN_ADD_COLOR = new Color(46, 204, 113);
    private final Color BTN_PRINT_COLOR = new Color(155, 89, 182);
    private final Color BTN_UPDATE_COLOR = new Color(52, 152, 219);
    private final Color BTN_DELETE_COLOR = new Color(231, 76, 60);
    private final Color VALIDATION_ERROR_COLOR = new Color(255, 230, 230);


    // Fonts matching AdminBlotterTab
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font SECTION_FONT = new Font("Segoe UI Semibold", Font.BOLD, 14);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    private final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 11);


    private javax.swing.Timer smartTimer;
    private int lastBlotterId = 0;


    public SuperAdminBlotterTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_COLOR);


        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);


        loadData();
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {

                if (refresher != null) {
                    refresher.stop();
                }
                refresher = new AutoRefresher("Case", SuperAdminBlotterTab.this::loadData);
                System.out.println("Tab opened/active. Auto-refresh started.");
            }

            @Override
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) {

                if (refresher != null) {
                    refresher.stop();
                    refresher = null;
                }
                System.out.println("Tab hidden/closed. Auto-refresh stopped.");
            }

            @Override
            public void ancestorMoved(javax.swing.event.AncestorEvent event) { }
        });
    }
    private AutoRefresher refresher;

    // =========================================================================
    // VALIDATION METHODS
    // =========================================================================


    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String regex = "^[A-Za-zÀ-ÿñÑ\\s.'-]{2,100}$";
        return name.trim().matches(regex);
    }


    private boolean isValidLocation(String location) {
        if (location == null || location.trim().isEmpty()) return false;
        String regex = "^[A-Za-zÀ-ÿñÑ0-9\\s.,#'()-]{2,200}$";
        return location.trim().matches(regex);
    }


    private boolean isValidOfficerName(String officer) {
        if (officer == null || officer.trim().isEmpty()) return false;
        String regex = "^[A-Za-zÀ-ÿñÑ\\s.'-]{2,100}$";
        return officer.trim().matches(regex);
    }


    private boolean isValidTextArea(String text, int minLength, int maxLength) {
        if (text == null || text.trim().isEmpty()) return true;
        text = text.trim();
        if (text.length() > maxLength) return false;
        String regex = "^[A-Za-zÀ-ÿñÑ0-9\\s.,'\"!?\\-():;]*$";
        return text.matches(regex);
    }


    private boolean isValidTime(String time) {
        if (time == null || time.trim().isEmpty()) return false;
        String regex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        return time.trim().matches(regex);
    }


    private JTextField createTimeTextField(String initialTime) {
        JTextField timeField = new JTextField(initialTime);
        timeField.setFont(FIELD_FONT);
        timeField.setToolTipText("Format: HH:MM (24-hour) e.g., 14:30");


        ((AbstractDocument) timeField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string == null) return;


                StringBuilder sb = new StringBuilder();
                for (char c : string.toCharArray()) {
                    if (Character.isDigit(c) || c == ':') {
                        sb.append(c);
                    }
                }
                super.insertString(fb, offset, sb.toString(), attr);
            }


            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text == null) return;


                StringBuilder sb = new StringBuilder();
                for (char c : text.toCharArray()) {
                    if (Character.isDigit(c) || c == ':') {
                        sb.append(c);
                    }
                }
                super.replace(fb, offset, length, sb.toString(), attrs);
            }
        });


        timeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String time = timeField.getText().trim();
                if (!time.isEmpty()) {
                    if (time.matches("^\\d{1,2}$")) {
                        int hour = Integer.parseInt(time);
                        if (hour < 0 || hour > 23) {
                            timeField.setBackground(VALIDATION_ERROR_COLOR);
                            timeField.setToolTipText("Invalid hour! Must be 00-23");
                            return;
                        }
                        time = String.format("%02d:00", hour);
                        timeField.setText(time);
                    } else if (time.matches("^\\d{1,2}:\\d{1,2}$")) {
                        String[] parts = time.split(":");
                        int hour = Integer.parseInt(parts[0]);
                        if (hour < 0 || hour > 23) {
                            timeField.setBackground(VALIDATION_ERROR_COLOR);
                            timeField.setToolTipText("Invalid hour! Must be 00-23");
                            return;
                        }


                        String minuteStr = parts[1];
                        int minute = Integer.parseInt(minuteStr.length() > 2 ? minuteStr.substring(0, 2) : minuteStr);
                        if (minute < 0 || minute > 59) {
                            timeField.setBackground(VALIDATION_ERROR_COLOR);
                            timeField.setToolTipText("Invalid minute! Must be 00-59");
                            return;
                        }


                        String hourStr = String.format("%02d", hour);
                        String minuteFormatted = String.format("%02d", minute);
                        timeField.setText(hourStr + ":" + minuteFormatted);
                    }


                    if (!isValidTime(timeField.getText())) {
                        timeField.setBackground(VALIDATION_ERROR_COLOR);
                        timeField.setToolTipText("Invalid time! Use HH:MM format (00:00 - 23:59)");
                    } else {
                        timeField.setBackground(Color.WHITE);
                        timeField.setToolTipText("Format: HH:MM (24-hour)");
                    }
                }
            }
        });


        return timeField;
    }


    private void addInputFilter(JTextField field, boolean allowNumbers) {
        if (field.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                        throws BadLocationException {
                    if (string == null) return;


                    StringBuilder sb = new StringBuilder();
                    for (char c : string.toCharArray()) {
                        if (Character.isLetter(c) ||
                                c == ' ' ||
                                c == '\'' ||
                                c == '-' ||
                                c == '.' ||
                                (allowNumbers && Character.isDigit(c)) ||
                                (allowNumbers && (c == ',' || c == '#' || c == '(' || c == ')'))) {
                            sb.append(c);
                        }
                    }
                    super.insertString(fb, offset, sb.toString(), attr);
                }


                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                        throws BadLocationException {
                    if (text == null) return;


                    StringBuilder sb = new StringBuilder();
                    for (char c : text.toCharArray()) {
                        if (Character.isLetter(c) ||
                                c == ' ' ||
                                c == '\'' ||
                                c == '-' ||
                                c == '.' ||
                                (allowNumbers && Character.isDigit(c)) ||
                                (allowNumbers && (c == ',' || c == '#' || c == '(' || c == ')'))) {
                            sb.append(c);
                        }
                    }
                    super.replace(fb, offset, length, sb.toString(), attrs);
                }
            });
        }
    }


    private void addInputFilterToTextArea(JTextArea textArea, boolean allowNumbers) {
        if (textArea.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) textArea.getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                        throws BadLocationException {
                    if (string == null) return;


                    StringBuilder sb = new StringBuilder();
                    for (char c : string.toCharArray()) {
                        if (Character.isLetter(c) ||
                                c == ' ' ||
                                c == '\'' ||
                                c == '-' ||
                                c == '.' ||
                                c == ',' ||
                                c == '!' ||
                                c == '?' ||
                                c == '\"' ||
                                c == ':' ||
                                c == ';' ||
                                c == '(' ||
                                c == ')' ||
                                (allowNumbers && Character.isDigit(c))) {
                            sb.append(c);
                        }
                    }
                    super.insertString(fb, offset, sb.toString(), attr);
                }


                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                        throws BadLocationException {
                    if (text == null) return;


                    StringBuilder sb = new StringBuilder();
                    for (char c : text.toCharArray()) {
                        if (Character.isLetter(c) ||
                                c == ' ' ||
                                c == '\'' ||
                                c == '-' ||
                                c == '.' ||
                                c == ',' ||
                                c == '!' ||
                                c == '?' ||
                                c == '\"' ||
                                c == ':' ||
                                c == ';' ||
                                c == '(' ||
                                c == ')' ||
                                (allowNumbers && Character.isDigit(c))) {
                            sb.append(c);
                        }
                    }
                    super.replace(fb, offset, length, sb.toString(), attrs);
                }
            });
        }
    }


    private void addValidationListener(JTextField field, java.util.function.Function<String, Boolean> validator) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateField(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateField(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateField(); }


            private void validateField() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String text = field.getText().trim();
                        if (!text.isEmpty() && !validator.apply(text)) {
                            field.setBackground(VALIDATION_ERROR_COLOR);
                            field.setToolTipText("Invalid characters detected!");
                        } else {
                            field.setBackground(Color.WHITE);
                            field.setToolTipText(null);
                        }
                    } catch (Exception ex) {
                        // Ignore
                    }
                });
            }
        });
    }


    private boolean hasDuplicatePersons() {
        String complainant = txtComplainant.getText().trim().toLowerCase();
        String respondent = txtRespondent.getText().trim().toLowerCase();
        String victim = txtVictim.getText().trim().toLowerCase();


        if (complainant.isEmpty() || respondent.isEmpty()) {
            return false;
        }


        boolean complainantRespondentSame = complainant.equals(respondent);
        boolean complainantVictimSame = !victim.isEmpty() && complainant.equals(victim);
        boolean respondentVictimSame = !victim.isEmpty() && respondent.equals(victim);


        if (complainantRespondentSame || complainantVictimSame || respondentVictimSame) {
            StringBuilder errorMsg = new StringBuilder("Duplicate persons found:\n\n");


            if (complainantRespondentSame) {
                errorMsg.append("• Complainant and Respondent are the same: ").append(txtComplainant.getText()).append("\n");
            }
            if (complainantVictimSame) {
                errorMsg.append("• Complainant and Victim are the same: ").append(txtComplainant.getText()).append("\n");
            }
            if (respondentVictimSame) {
                errorMsg.append("• Respondent and Victim are the same: ").append(txtRespondent.getText()).append("\n");
            }


            errorMsg.append("\nPlease ensure all persons are different individuals.");


            JOptionPane.showMessageDialog(this,
                    errorMsg.toString(),
                    "Duplicate Persons Detected",
                    JOptionPane.ERROR_MESSAGE);
            return true;
        }


        return false;
    }


    // =========================================================================
    // DUPLICATE CASE DETECTION
    // =========================================================================


    private boolean isDuplicateCase(BlotterCase newCase, boolean isUpdate, int existingCaseId) {
        BlotterCaseDAO dao = new BlotterCaseDAO();
        List<BlotterCase> allCases = dao.getAllBlotterCases();


        for (BlotterCase existingCase : allCases) {
            if (isUpdate && existingCase.getCaseId() == existingCaseId) {
                continue;
            }


            boolean sameComplainant = existingCase.getComplainant().equalsIgnoreCase(newCase.getComplainant());
            boolean sameRespondent = existingCase.getRespondent().equalsIgnoreCase(newCase.getRespondent());
            boolean sameVictim = existingCase.getVictim().equalsIgnoreCase(newCase.getVictim());
            boolean sameLocation = existingCase.getLocation().equalsIgnoreCase(newCase.getLocation());
            boolean sameIncidentType = existingCase.getIncidentType().equalsIgnoreCase(newCase.getIncidentType());
            boolean sameTime = existingCase.getTimeRecorded().equals(newCase.getTimeRecorded());


            String existingNarrative = existingCase.getNarrative().toLowerCase().trim();
            String newNarrative = newCase.getNarrative().toLowerCase().trim();
            boolean similarNarrative = existingNarrative.contains(newNarrative) ||
                    newNarrative.contains(existingNarrative) ||
                    existingNarrative.equals(newNarrative);


            if (sameComplainant && sameRespondent && sameVictim &&
                    sameLocation && sameIncidentType && sameTime && similarNarrative) {


                StringBuilder message = new StringBuilder();
                message.append("⚠️ DUPLICATE CASE DETECTED!\n\n");
                message.append("A similar case already exists in the system:\n\n");
                message.append("• Case #: ").append(existingCase.getCaseNumber()).append("\n");
                message.append("• Date Recorded: ").append(existingCase.getDateRecorded()).append("\n");
                message.append("• Time: ").append(existingCase.getTimeRecorded()).append("\n");
                message.append("• Incident Type: ").append(existingCase.getIncidentType()).append("\n");
                message.append("• Complainant: ").append(existingCase.getComplainant()).append("\n");
                message.append("• Respondent: ").append(existingCase.getRespondent()).append("\n");
                message.append("• Victim: ").append(existingCase.getVictim()).append("\n");
                message.append("• Location: ").append(existingCase.getLocation()).append("\n\n");
                message.append("Are you sure you want to save this as a new case?\n");
                message.append("(Click 'Yes' to proceed anyway, 'No' to cancel)");


                int option = JOptionPane.showConfirmDialog(
                        this,
                        message.toString(),
                        "Duplicate Case Warning",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );


                return option != JOptionPane.YES_OPTION;
            }
        }


        return false;
    }


    // =========================================================================
    // SMART POLLING
    // =========================================================================


    private void startSmartPolling() {
        initializelastBlotterId();


        smartTimer = new javax.swing.Timer(2000, e -> {
            if (table == null || table.getSelectedRow() == -1) {
                checkForBlotterUpdates();
            }
        });
        smartTimer.start();
    }


    private void initializelastBlotterId() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                String sql = "SELECT MAX(caseId) as max_id FROM blotter_case";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {


                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    return rs.next() ? rs.getInt("max_id") : 0;
                }
            }


            @Override
            protected void done() {
                try {
                    lastBlotterId = get();
                    System.out.println("Initialized lastBlotterId to: " + lastBlotterId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    private void checkForBlotterUpdates() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String sql = "SELECT MAX(caseId) as max_id FROM blotter_case";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {


                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    if (rs.next()) {
                        int currentMaxId = rs.getInt("max_id");
                        return currentMaxId > lastBlotterId;
                    }
                }
                return false;
            }


            @Override
            protected void done() {
                try {
                    boolean hasUpdates = get();
                    if (hasUpdates) {
                        System.out.println("New blotter data detected! Refreshing...");
                        loadData();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    private void updatelastBlotterId() {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                String sql = "SELECT MAX(caseId) as max_id FROM blotter_case";
                try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
                     java.sql.Statement stmt = conn.createStatement()) {


                    java.sql.ResultSet rs = stmt.executeQuery(sql);
                    return rs.next() ? rs.getInt("max_id") : 0;
                }
            }


            @Override
            protected void done() {
                try {
                    lastBlotterId = get();
                    System.out.println("Updated lastBlotterId to: " + lastBlotterId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    // =========================================================================
    // DATA LOADING
    // =========================================================================


    public void loadData() {
        new SwingWorker<List<BlotterCase>, Void>() {
            @Override
            protected List<BlotterCase> doInBackground() throws Exception {
                return new BlotterCaseDAO().getAllBlotterCases();
            }


            @Override
            protected void done() {
                try {
                    List<BlotterCase> list = get();
                    tableModel.setRowCount(0);


                    for (BlotterCase b : list) {
                        tableModel.addRow(new Object[]{
                                b.getCaseId(),
                                b.getCaseNumber(),
                                b.getDateRecorded(),
                                b.getIncidentType(),
                                b.getComplainant(),
                                b.getRespondent(),
                                b.getStatus(),
                                b.getHearingDate(),
                                b.getLocation(),
                                b.getOfficerInCharge()
                        });
                    }
                    lblRecordCount.setText("Total Cases: " + list.size());
                    updatelastBlotterId();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error loading data. Check Console.");
                }
            }
        }.execute();
    }


    // =========================================================================
    // ACTIONS
    // =========================================================================


    private void handleAdd() {
        showDialog(null, "New Blotter Case");
    }


    private void handleUpdate() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a case to edit.");
            return;
        }


        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);


        BlotterCase existing = new BlotterCaseDAO().getCaseById(id);


        if (existing != null) {
            showDialog(existing, "Update Case Details");
        } else {
            JOptionPane.showMessageDialog(this, "Error fetching case details.");
        }
    }


    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a case to delete.");
            return;
        }


        int modelRow = table.convertRowIndexToModel(selectedRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        String caseNo = (String) tableModel.getValueAt(modelRow, 1);


        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Case " + caseNo + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);


        if (confirm == JOptionPane.YES_OPTION) {
            new BlotterCaseDAO().deleteBlotterCase(id);
            int staffId = Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId());
            try { new SystemLogDAO().addLog("Deleted Case " + caseNo, "Admin",staffId ); } catch(Exception e){}
            loadData();
            JOptionPane.showMessageDialog(this, "Case deleted successfully.");
        }
    }


    private void handlePrint() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as PDF");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getAbsolutePath().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            try {
                // 1. Create Document (Landscape)
                Document doc = new Document(PageSize.A4.rotate()); // ✅ Fixed: lowercase 'rotate()'
                PdfWriter.getInstance(doc, new FileOutputStream(file));
                doc.open();

                // 2. Add Title
                com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
                Paragraph title = new Paragraph("Blotter Report", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);

                // 3. Create Table
                int colCount = table.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);

                // 4. Add Headers (✅ MATCHING YOUR TABLE COLORS)
                // We use the same color: new Color(52, 152, 219)
                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);

                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);

                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(table.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor); // ✅ Blue Background
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8); // More padding like your table
                    pdfTable.addCell(cell);
                }

                // 5. Add Rows
                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

                for (int i = 0; i < table.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = table.getValueAt(i, j);
                        String text = (val != null) ? val.toString() : "";

                        PdfPCell cell = new PdfPCell(new Paragraph(text, rowFont));
                        cell.setPadding(6);
                        cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);

                        // ✅ Zebra Striping (Light Blue tint to match theme)
                        if (i % 2 == 1) {
                            cell.setBackgroundColor(new java.awt.Color(235, 245, 250));
                        }

                        pdfTable.addCell(cell);
                    }
                }

                doc.add(pdfTable);
                doc.close();

                JOptionPane.showMessageDialog(this, "Export Success!\nFile: " + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Export Error: " + e.getMessage());
            }
        }
    }


    // =========================================================================
    // DIALOG (ADD / EDIT) - UPDATED TO MATCH ADMINBLOTTERTAB
    // =========================================================================


    private void showDialog(BlotterCase existing, String title) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(1000, 900);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());


        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);


        // Header
        JPanel headerPanel = createModernHeader(PRIMARY_COLOR);
        mainPanel.add(headerPanel, BorderLayout.NORTH);


        // Create form content
        JPanel contentPanel = createFormContentPanel(existing, SECTION_BG, BORDER_COLOR,
                LABEL_FONT, FIELD_FONT, SECTION_FONT);


        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);


        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 20, 0));


        JButton btnCancel = createModernButton("Cancel", WARNING_COLOR, "✖");
        JButton btnSave = createModernButton("Save Case", ACCENT_COLOR, "💾");


        btnCancel.addActionListener(e -> dialog.dispose());


        BlotterCaseDAO dao = new BlotterCaseDAO();
        btnSave.addActionListener(e -> {
            if (saveCaseData(dialog, existing, dao)) {
                dialog.dispose();
            }
        });


        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);


        dialog.add(mainPanel);
        dialog.setVisible(true);
    }


    private JPanel createModernHeader(Color primaryColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(primaryColor);
        panel.setBorder(new EmptyBorder(15, 0, 15, 0));


        JLabel title = new JLabel("BLOTTER CASE FORM", SwingConstants.CENTER);
        title.setFont(HEADER_FONT);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel subtitle = new JLabel("Super Administrator Mode", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(220, 220, 220));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);


        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);


        return panel;
    }


    private JPanel createFormContentPanel(BlotterCase existing, Color sectionBg, Color borderColor,
                                          Font labelFont, Font fieldFont, Font sectionFont) {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 30, 30, 30));


        // Initialize fields
        initializeFields(existing);


        // I. BLOTTER INFORMATION
        contentPanel.add(createSectionPanel("I. BLOTTER INFORMATION",
                createBlotterInfoPanel(sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // II. COMPLAINANT
        contentPanel.add(createSectionPanel("II. COMPLAINANT (NAGREREKLAMO)",
                createPersonPanel(txtComplainant, "complainant", sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // III. RESPONDENT
        contentPanel.add(createSectionPanel("III. RESPONDENT (INIREREKLAMO)",
                createPersonPanel(txtRespondent, "respondent", sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // IV. VICTIM
        contentPanel.add(createSectionPanel("IV. VICTIM (BIKTIMA)",
                createPersonPanel(txtVictim, "victim", sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // V. INCIDENT DETAILS
        contentPanel.add(createSectionPanel("V. INCIDENT DETAILS",
                createIncidentPanel(sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // VI. NARRATIVE
        contentPanel.add(createSectionPanel("VI. COMPLAINT/NARRATIVE (SALAYSAY)",
                createNarrativePanel(sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // VII. WITNESSES
        contentPanel.add(createSectionPanel("VII. WITNESSES (SAKSI)",
                createWitnessesPanel(sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // VIII. RESOLUTION
        contentPanel.add(createSectionPanel("VIII. RESOLUTION (PAGKAKASUNDO)",
                createResolutionPanel(sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // IX. OFFICER
        contentPanel.add(createSectionPanel("IX. OFFICER IN-CHARGE",
                createOfficerPanel(sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));
        contentPanel.add(Box.createVerticalStrut(15));


        // X. HEARING & SUMMON
        contentPanel.add(createSectionPanel("X. HEARING & SUMMON DATES",
                createHearingPanel(sectionBg, borderColor, labelFont, fieldFont),
                sectionFont, PRIMARY_COLOR));


        return contentPanel;
    }


    private void initializeFields(BlotterCase existing) {
        // Case number
        txtCaseNo = createStyledTextField(existing != null ? existing.getCaseNumber() :
                "BC-" + System.currentTimeMillis(), false);


        // Dates
        String today = LocalDate.now().toString();
        txtDate = createStyledTextField(existing != null && existing.getDateRecorded() != null ?
                existing.getDateRecorded().toString() : today, false);


        // Time
        String initialTime = existing != null && existing.getTimeRecorded() != null ?
                existing.getTimeRecorded().format(DateTimeFormatter.ofPattern("HH:mm")) : "12:00";
        txtTime = createTimeTextField(initialTime);


        // Incident date
        txtIncidentDate = createStyledTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), true);


        // Incident time
        txtIncidentTime = createTimeTextField("12:00");


        // People
        txtComplainant = createStyledTextField(existing != null ? existing.getComplainant() : "", true);
        txtRespondent = createStyledTextField(existing != null ? existing.getRespondent() : "", true);
        txtVictim = createStyledTextField(existing != null ? existing.getVictim() : "N/A", true);


        // Incident Type
        String[] types = new SystemConfigDAO().getOptionsNature("incidentType");
        cbType = new JComboBox<>(types);
        cbType.setFont(FIELD_FONT);
        if (existing != null) cbType.setSelectedItem(existing.getIncidentType());


        // Location
        txtLocation = createStyledTextField(existing != null ? existing.getLocation() : "Purok ", true);


        // Narrative
        txtNarrative = new JTextArea(existing != null ? existing.getNarrative() : "", 3, 20);
        txtNarrative.setFont(FIELD_FONT);
        txtNarrative.setLineWrap(true);
        txtNarrative.setWrapStyleWord(true);


        // Witnesses
        txtWitness = createStyledTextField(existing != null ? existing.getWitnesses() : "None", true);


        // Status
        String[] statuses = {"Pending", "Resolved", "Dismissed", "Certified to File Action", "Amicably Settled"};
        cbStatus = new JComboBox<>(statuses);
        cbStatus.setFont(FIELD_FONT);
        if (existing != null) cbStatus.setSelectedItem(existing.getStatus());


        // Hearing
        txtHearing = createStyledTextField(existing != null && existing.getHearingDate() != null ?
                existing.getHearingDate().toString() : "", true);


        // Summon date
        txtSummonDate = createStyledTextField("", false);
        txtSummonDate.setEditable(false);


        // Officer
        txtOfficer = createStyledTextField(existing != null ? existing.getOfficerInCharge() : "Desk Officer", true);


        // Resolution
        txtResolution = createStyledTextField(existing != null ? existing.getResolution() : "", true);


        // Add input filtering
        addInputFilter(txtComplainant, false);
        addInputFilter(txtRespondent, false);
        addInputFilter(txtVictim, false);
        addInputFilter(txtLocation, true);
        addInputFilter(txtOfficer, false);
        addInputFilter(txtWitness, true);
        addInputFilter(txtResolution, true);
        addInputFilterToTextArea(txtNarrative, true);


        // Add validation listeners
        addValidationListener(txtComplainant, this::isValidName);
        addValidationListener(txtRespondent, this::isValidName);
        addValidationListener(txtVictim, this::isValidName);
        addValidationListener(txtLocation, this::isValidLocation);
        addValidationListener(txtOfficer, this::isValidOfficerName);
    }


    private JPanel createSectionPanel(String title, JComponent content, Font titleFont, Color titleColor) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));


        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(titleColor);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));


        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(SECTION_BG);
        contentWrapper.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        contentWrapper.add(content, BorderLayout.CENTER);


        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentWrapper, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createBlotterInfoPanel(Color sectionBg, Color borderColor, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 15));
        panel.setBackground(sectionBg);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));


        panel.add(createLabeledField("Blotter No:", txtCaseNo, labelFont, fieldFont));
        panel.add(createLabeledField("Date Recorded:", txtDate, labelFont, fieldFont));
        panel.add(createLabeledField("Time Recorded:", txtTime, labelFont, fieldFont));
        panel.add(createLabeledField("Status:", cbStatus, labelFont, fieldFont));
        panel.add(new JLabel()); // Empty cell
        panel.add(new JLabel()); // Empty cell


        return panel;
    }


    private JPanel createPersonPanel(JTextField field, String type, Color sectionBg,
                                     Color borderColor, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(sectionBg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        JButton pickerBtn = createModernPickerButton();
        pickerBtn.addActionListener(e -> showResidentPicker(field));


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel label = new JLabel("Full Name(s):");
        label.setFont(labelFont);
        panel.add(label, gbc);


        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);


        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(pickerBtn, gbc);


        return panel;
    }


    private JPanel createIncidentPanel(Color sectionBg, Color borderColor, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 20, 15));
        panel.setBackground(sectionBg);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));


        // Incident Type
        panel.add(createLabeledField("Incident Type:", cbType, labelFont, fieldFont));


        // Location with picker button
        JPanel locationPanel = new JPanel(new BorderLayout(5, 0));
        locationPanel.setBackground(sectionBg);
        locationPanel.add(txtLocation, BorderLayout.CENTER);
        JButton btnLocationPicker = new JButton("📍");
        btnLocationPicker.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocationPicker.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        btnLocationPicker.setBackground(new Color(240, 240, 240));
        btnLocationPicker.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Location picker would open here");
        });
        locationPanel.add(btnLocationPicker, BorderLayout.EAST);
        panel.add(createLabeledField("Location:", locationPanel, labelFont, fieldFont));


        // Incident Date
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.setBackground(sectionBg);
        datePanel.add(txtIncidentDate, BorderLayout.CENTER);
        JButton btnDatePicker = new JButton("📅");
        btnDatePicker.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDatePicker.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        btnDatePicker.setBackground(new Color(240, 240, 240));
        btnDatePicker.addActionListener(e -> showDatePicker(txtIncidentDate));
        datePanel.add(btnDatePicker, BorderLayout.EAST);
        panel.add(createLabeledField("Incident Date:", datePanel, labelFont, fieldFont));


        // Incident Time
        panel.add(createLabeledField("Incident Time:", txtIncidentTime, labelFont, fieldFont));


        // Empty cells for grid alignment
        panel.add(new JPanel());
        panel.add(new JPanel());
        panel.add(new JPanel());
        panel.add(new JPanel());


        return panel;
    }


    private JPanel createNarrativePanel(Color sectionBg, Color borderColor, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(sectionBg);


        JLabel areaLabel = new JLabel("Narrative Details:");
        areaLabel.setFont(labelFont);
        areaLabel.setForeground(new Color(80, 80, 80));


        JScrollPane scrollPane = new JScrollPane(txtNarrative);
        scrollPane.setBorder(new LineBorder(borderColor, 1));
        scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 150));


        panel.add(areaLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createWitnessesPanel(Color sectionBg, Color borderColor, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(sectionBg);


        JLabel areaLabel = new JLabel("Witness Information:");
        areaLabel.setFont(labelFont);
        areaLabel.setForeground(new Color(80, 80, 80));


        JTextField witnessField = new JTextField();
        witnessField.setFont(fieldFont);
        witnessField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        witnessField.setText(txtWitness.getText());


        witnessField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateWitnessField(); }
            @Override public void removeUpdate(DocumentEvent e) { updateWitnessField(); }
            @Override public void changedUpdate(DocumentEvent e) { updateWitnessField(); }


            private void updateWitnessField() {
                txtWitness.setText(witnessField.getText());
            }
        });


        panel.add(areaLabel, BorderLayout.NORTH);
        panel.add(witnessField, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createResolutionPanel(Color sectionBg, Color borderColor, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(sectionBg);


        JLabel areaLabel = new JLabel("Resolution Details:");
        areaLabel.setFont(labelFont);
        areaLabel.setForeground(new Color(80, 80, 80));


        JTextArea resolutionArea = new JTextArea(4, 20);
        resolutionArea.setFont(fieldFont);
        resolutionArea.setLineWrap(true);
        resolutionArea.setWrapStyleWord(true);
        resolutionArea.setText(txtResolution.getText());


        resolutionArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateResolutionField(); }
            @Override public void removeUpdate(DocumentEvent e) { updateResolutionField(); }
            @Override public void changedUpdate(DocumentEvent e) { updateResolutionField(); }


            private void updateResolutionField() {
                txtResolution.setText(resolutionArea.getText());
            }
        });


        JScrollPane scrollPane = new JScrollPane(resolutionArea);
        scrollPane.setBorder(new LineBorder(borderColor, 1));


        panel.add(areaLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);


        return panel;
    }


    private JPanel createOfficerPanel(Color sectionBg, Color borderColor, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(sectionBg);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        JLabel label = new JLabel("Officer Name:");
        label.setFont(labelFont);


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(label, gbc);


        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtOfficer, gbc);


        return panel;
    }


    private JPanel createHearingPanel(Color sectionBg, Color borderColor, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 15));
        panel.setBackground(sectionBg);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));


        // Hearing Date with captain schedule check
        JPanel hearingPanel = new JPanel(new BorderLayout(5, 0));
        hearingPanel.setBackground(sectionBg);


        // Get available dates from captain's schedule
        List<String> availableDates = getCaptainAvailableDates();
        JComboBox<String> hearingDateCombo = new JComboBox<>(availableDates.toArray(new String[0]));
        hearingDateCombo.setFont(fieldFont);
        if (!txtHearing.getText().isEmpty()) {
            hearingDateCombo.setSelectedItem(txtHearing.getText());
        }


        // Update the original field when combo changes
        hearingDateCombo.addActionListener(e -> {
            if (hearingDateCombo.getSelectedItem() != null &&
                    !hearingDateCombo.getSelectedItem().toString().equals("Select Date")) {
                txtHearing.setText(hearingDateCombo.getSelectedItem().toString().split(" ")[0]);
            }
        });


        // Refresh button for captain's schedule
        JButton btnRefreshSchedule = new JButton("🔄 Refresh");
        btnRefreshSchedule.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnRefreshSchedule.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnRefreshSchedule.setBackground(new Color(240, 240, 240));
        btnRefreshSchedule.addActionListener(e -> refreshHearingDateComboBox(hearingDateCombo));


        hearingPanel.add(hearingDateCombo, BorderLayout.CENTER);
        hearingPanel.add(btnRefreshSchedule, BorderLayout.EAST);


        panel.add(createLabeledField("Hearing Date (Captain's Schedule):", hearingPanel, labelFont, fieldFont));


        // Auto-calculated summon date (1 week before hearing)
        txtSummonDate.setBackground(new Color(248, 249, 250));


        // Calculate summon date when hearing date changes
        hearingDateCombo.addActionListener(e -> {
            Object selected = hearingDateCombo.getSelectedItem();
            if (selected != null && !selected.toString().equals("Select Date")) {
                try {
                    String dateStr = selected.toString().split(" ")[0];
                    LocalDate hearingDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalDate summonDate = hearingDate.minusWeeks(1);
                    txtSummonDate.setText(summonDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } catch (Exception ex) {
                    txtSummonDate.setText("");
                }
            }
        });


        panel.add(createLabeledField("Letter of Summon Date (Auto-calculated):", txtSummonDate, labelFont, fieldFont));


        return panel;
    }

    SystemLogDAO log = new SystemLogDAO();
    private List<String> getCaptainAvailableDates() {
        List<String> availableDates = new ArrayList<>();
        availableDates.add("Select Date");


        try {
            CaptainScheduleDAO scheduleDAO = new CaptainScheduleDAO();
            List<CaptainSchedule> schedules = scheduleDAO.getAllSchedules();


            for (CaptainSchedule schedule : schedules) {
                if (schedule.isAvailable()) {
                    String dateStr = schedule.getScheduleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String day = schedule.getDayOfWeek();
                    String timeSlot = schedule.getStartTime() + " - " + schedule.getEndTime();
                    availableDates.add(dateStr + " (" + day + ") " + timeSlot);
                }
            }


            if (availableDates.size() == 1) {
                availableDates.add("No available dates - Contact Captain");
            }
        } catch (Exception e) {
            availableDates.add("Error loading schedule");
        }


        return availableDates;
    }


    private void refreshHearingDateComboBox(JComboBox<String> comboBox) {
        List<String> availableDates = getCaptainAvailableDates();
        comboBox.setModel(new DefaultComboBoxModel<>(availableDates.toArray(new String[0])));


        if (availableDates.size() > 1) {
            comboBox.setSelectedIndex(1);
        }
    }


    private JPanel createLabeledField(String label, JComponent field, Font labelFont, Font fieldFont) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(SECTION_BG);


        JLabel lbl = new JLabel(label);
        lbl.setFont(labelFont);
        lbl.setForeground(new Color(60, 60, 60));


        panel.add(lbl, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);


        return panel;
    }


    private JTextField createStyledTextField(String text, boolean editable) {
        JTextField field = new JTextField(text);
        field.setFont(FIELD_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(editable ? Color.WHITE : new Color(248, 249, 250));
        field.setEditable(editable);
        return field;
    }


    private JButton createModernPickerButton() {
        JButton button = new JButton("👤 Pick");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }


    private JButton createModernButton(String text, Color bgColor, String icon) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));


        button.addMouseListener(new MouseAdapter() {
            Color original = bgColor;
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(original.darker());
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(original);
            }
        });


        return button;
    }


    private boolean saveCaseData(JDialog dialog, BlotterCase existing, BlotterCaseDAO dao) {
        List<String> missingFields = new ArrayList<>();
        List<String> invalidFields = new ArrayList<>();


        // Required field validations
        if (txtComplainant.getText().trim().isEmpty()) missingFields.add("Complainant");
        if (txtRespondent.getText().trim().isEmpty()) missingFields.add("Respondent");
        if (cbType.getSelectedIndex() == -1 || cbType.getSelectedItem() == null ||
                cbType.getSelectedItem().toString().isEmpty()) missingFields.add("Incident Type");
        if (txtLocation.getText().trim().isEmpty()) missingFields.add("Location");
        if (txtTime.getText().trim().isEmpty()) missingFields.add("Time");
        if (txtOfficer.getText().trim().isEmpty()) missingFields.add("Officer In-Charge");
        if (txtNarrative.getText().trim().isEmpty()) missingFields.add("Narrative");
        if (txtResolution.getText().trim().isEmpty()) missingFields.add("Resolution");


        // Format validations
        if (!txtTime.getText().trim().isEmpty() && !isValidTime(txtTime.getText().trim())) {
            invalidFields.add("Time - Invalid format (use HH:MM, 00:00-23:59)");
        }


        // Duplicate person check
        if (hasDuplicatePersons()) {
            return false;
        }


        // Show validation errors
        if (!missingFields.isEmpty()) {
            StringBuilder message = new StringBuilder("Please fill in the following required fields:\n\n");
            for (String field : missingFields) {
                message.append("• ").append(field).append("\n");
            }
            JOptionPane.showMessageDialog(dialog,
                    message.toString(),
                    "Missing Required Fields",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }


        if (!invalidFields.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Please correct the following fields:\n\n");
            for (String field : invalidFields) {
                errorMessage.append("• ").append(field).append("\n");
            }
            JOptionPane.showMessageDialog(dialog,
                    errorMessage.toString(),
                    "Invalid Field Entries",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }


        try {
            BlotterCase b = new BlotterCase();
            b.setCaseNumber(txtCaseNo.getText().trim());
            b.setDateRecorded(Date.valueOf(txtDate.getText()).toLocalDate());
            b.setTimeRecorded(LocalTime.parse(txtTime.getText()));
            b.setIncidentType(cbType.getSelectedItem().toString());
            b.setStatus(cbStatus.getSelectedItem().toString());
            b.setComplainant(txtComplainant.getText().trim());
            b.setRespondent(txtRespondent.getText().trim());
            b.setVictim(txtVictim.getText().trim());
            b.setLocation(txtLocation.getText().trim());
            b.setNarrative(txtNarrative.getText().trim());
            b.setWitnesses(txtWitness.getText().trim());
            b.setOfficerInCharge(txtOfficer.getText().trim());
            b.setResolution(txtResolution.getText().trim());


            // Check for duplicate case number
            boolean isEditMode = existing != null;
            if (!isEditMode && dao.isCaseNumberExists(txtCaseNo.getText().trim())) {
                JOptionPane.showMessageDialog(dialog,
                        "Case number already exists! Please use a different case number.",
                        "Duplicate Case", JOptionPane.ERROR_MESSAGE);
                return false;
            }


            // Check for duplicate case details
            int existingCaseId = (existing != null) ? existing.getCaseId() : 0;
            if (isDuplicateCase(b, isEditMode, existingCaseId)) {
                return false;
            }


            // Handle hearing date
            LocalDate hearingDate = null;
            String dateText = txtHearing.getText().trim();
            if (!dateText.isEmpty()) {
                try {
                    hearingDate = java.sql.Date.valueOf(dateText).toLocalDate();
                    b.setHearingDate(hearingDate);


                    // Check hearing conflict
                    if (!checkHearingConflict(hearingDate)) {
                        return false;
                    }
                } catch (Exception dateEx) {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid Hearing Date Format!\nPlease use yyyy-mm-dd (e.g., 2024-12-30).",
                            "Date Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                b.setHearingDate(null);
            }


            if (existing == null) {
                dao.addBlotterCase(b);
                try { new SystemLogDAO().addLog("Added Case " + b.getCaseNumber(), "Admin", 1); } catch(Exception ex){}
                JOptionPane.showMessageDialog(dialog, "Case saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                b.setCaseId(existing.getCaseId());
                dao.updateBlotterCase(b);
                try { new SystemLogDAO().addLog("Updated Case " + b.getCaseNumber(), "Admin", 1); } catch(Exception ex){}
                JOptionPane.showMessageDialog(dialog, "Case updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }


            loadData();
            return true;


        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }


    // =========================================================================
    // UI HELPERS
    // =========================================================================


    private JPanel createPickerPanel(JTextField field, String type) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.WHITE);
        JButton btn = new JButton("...");
        btn.addActionListener(e -> showResidentPicker(field));
        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }


    private JPanel createSimpleDatePicker(JTextField field) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setBackground(Color.WHITE);
        JButton btn = new JButton("📅");
        btn.setFocusPainted(false);
        btn.addActionListener(e -> field.setText(LocalDate.now().toString()));


        p.add(field, BorderLayout.CENTER);
        p.add(btn, BorderLayout.EAST);
        return p;
    }


    private void showResidentPicker(JTextField targetField) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Resident", true);
        d.setSize(600, 450);
        d.setLayout(new BorderLayout());
        d.setLocationRelativeTo(this);


        // Simple Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        searchPanel.add(new JLabel("Find Name:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);


        // Table Setup
        String[] cols = {"ID", "Name", "Purok"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(25);


        // Load Data from ResidentDAO
        List<org.example.Users.Resident> list = new org.example.ResidentDAO().getAllResidents();
        for (org.example.Users.Resident r : list) {
            String middle = r.getMiddleName() != null ? r.getMiddleName() : "";
            m.addRow(new Object[]{r.getResidentId(), r.getFirstName() + " " + middle + " " + r.getLastName(), r.getPurok()});
        }


        // Select Button Logic
        JButton btnSelect = new JButton("Select");
        btnSelect.addActionListener(e -> {
            int row = t.getSelectedRow();
            if (row != -1) {
                targetField.setText(m.getValueAt(row, 1).toString());
                d.dispose();
            }
        });


        d.add(searchPanel, BorderLayout.NORTH);
        d.add(new JScrollPane(t), BorderLayout.CENTER);
        d.add(btnSelect, BorderLayout.SOUTH);
        d.setVisible(true);
    }


    private JPanel createContentPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));


        // 1. TOOLBAR
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(BG_COLOR);


        JButton btnAdd = createModernButton("+ New Case", BTN_ADD_COLOR, "➕");
        btnAdd.addActionListener(e -> handleAdd());


        JButton btnEdit = createModernButton("Edit", BTN_UPDATE_COLOR, "✏️");
        btnEdit.addActionListener(e -> handleUpdate());


        JButton btnDelete = createModernButton("Delete", BTN_DELETE_COLOR, "🗑️");
        btnDelete.addActionListener(e -> handleDelete());


        JButton btnPrint = createModernButton("Print Report", BTN_PRINT_COLOR, "🖨️");
        btnPrint.addActionListener(e -> handlePrint());


        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnPrint);


        // 2. SEARCH
        JPanel searchP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchP.setBackground(BG_COLOR);
        searchField = new JTextField(20);
        searchField.setFont(FIELD_FONT);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if(sorter != null) sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
            }
        });
        searchP.add(new JLabel("Search: "));
        searchP.add(searchField);


        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(BG_COLOR);
        topContainer.add(toolbar, BorderLayout.WEST);
        topContainer.add(searchP, BorderLayout.EAST);


        p.add(topContainer, BorderLayout.NORTH);


        // 3. TABLE
        String[] cols = {"ID", "Case #", "Date", "Type", "Complainant", "Respondent", "Status", "Hearing", "Location", "Officer"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));


        // Hide ID Column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);


        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);


        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        p.add(scroll, BorderLayout.CENTER);


        // 4. FOOTER
        lblRecordCount = new JLabel("Total Cases: 0");
        lblRecordCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRecordCount.setBorder(new EmptyBorder(10, 5, 0, 0));
        p.add(lblRecordCount, BorderLayout.SOUTH);


        return p;
    }


    private boolean checkHearingConflict(LocalDate hearingDate) {
        CaptainScheduleDAO dao = new CaptainScheduleDAO();
        List<CaptainSchedule> schedules = dao.getSchedulesByDate(hearingDate);


        if (schedules.isEmpty()) {
            return true;
        }


        for (CaptainSchedule sched : schedules) {
            String timeRange = sched.getStartTime() + " - " + sched.getEndTime();


            int choice = JOptionPane.showConfirmDialog(this,
                    "ALERT: The Captain:"+sched.getCaptainName()+" already has a schedule on " + hearingDate + ".\n" +
                            "Time: " + timeRange + "\n" +
                            "Status: " + (sched.isAvailable() ? "Available" : "Busy") + "\n\n" +
                            "Do you want to double-book this slot?",
                    "Schedule Conflict Found",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);


            if (choice == JOptionPane.NO_OPTION) {
                return false;
            }
        }


        return true;
    }


    private JPanel createHeaderPanel() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(PRIMARY_COLOR);
        h.setBorder(new EmptyBorder(25, 40, 25, 40));


        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(PRIMARY_COLOR);


        JLabel l1 = new JLabel("Barangay System");
        l1.setFont(HEADER_FONT);
        l1.setForeground(Color.WHITE);


        JLabel l2 = new JLabel("Blotter & Incident Management - Super Admin");
        l2.setFont(new Font("Arial", Font.PLAIN, 18));
        l2.setForeground(Color.LIGHT_GRAY);


        titleBox.add(l1);
        titleBox.add(l2);
        h.add(titleBox, BorderLayout.WEST);
        return h;
    }


    private void showDatePicker(JTextField targetField) {
        JDialog dateDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dateDialog.setSize(400, 350);
        dateDialog.setLocationRelativeTo(this);
        dateDialog.setLayout(new BorderLayout());


        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        JButton btnPrev = new JButton(" < ");
        JButton btnNext = new JButton(" > ");
        JLabel lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Arial", Font.BOLD, 16));


        final LocalDate[] currentDate = {LocalDate.now()};
        try {
            if (!targetField.getText().isEmpty()) {
                currentDate[0] = LocalDate.parse(targetField.getText());
            }
        } catch (Exception e) {
            currentDate[0] = LocalDate.now();
        }


        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        updateCalendar(calendarPanel, lblMonthYear, currentDate[0], targetField, dateDialog);


        btnPrev.addActionListener(e -> {
            currentDate[0] = currentDate[0].minusMonths(1);
            updateCalendar(calendarPanel, lblMonthYear, currentDate[0], targetField, dateDialog);
        });


        btnNext.addActionListener(e -> {
            currentDate[0] = currentDate[0].plusMonths(1);
            updateCalendar(calendarPanel, lblMonthYear, currentDate[0], targetField, dateDialog);
        });


        headerPanel.add(btnPrev, BorderLayout.WEST);
        headerPanel.add(lblMonthYear, BorderLayout.CENTER);
        headerPanel.add(btnNext, BorderLayout.EAST);


        dateDialog.add(headerPanel, BorderLayout.NORTH);
        dateDialog.add(calendarPanel, BorderLayout.CENTER);
        dateDialog.setVisible(true);
    }


    private void updateCalendar(JPanel calendarPanel, JLabel lblMonthYear, LocalDate currentDate,
                                JTextField targetField, JDialog dateDialog) {
        calendarPanel.removeAll();
        lblMonthYear.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));


        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Arial", Font.BOLD, 12));
            calendarPanel.add(l);
        }


        YearMonth yearMonth = YearMonth.of(currentDate.getYear(), currentDate.getMonth());
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int emptySlots = (dayOfWeek == 7) ? 0 : dayOfWeek;


        for (int i = 0; i < emptySlots; i++) calendarPanel.add(new JLabel(""));


        int daysInMonth = yearMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;
            JButton btn = new JButton(String.valueOf(day));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setMargin(new Insets(2, 2, 2, 2));


            LocalDate buttonDate = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), day);
            if (buttonDate.equals(LocalDate.now())) {
                btn.setForeground(Color.BLUE);
                btn.setFont(new Font("Arial", Font.BOLD, 12));
            }


            btn.addActionListener(e -> {
                targetField.setText(buttonDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                dateDialog.dispose();
            });
            calendarPanel.add(btn);
        }
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setSize(1200, 800);
            f.add(new SuperAdminBlotterTab());
            f.setVisible(true);
        });
    }
}

