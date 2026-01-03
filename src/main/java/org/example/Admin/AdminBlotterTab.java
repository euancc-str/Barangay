package org.example.Admin;




import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Timer;


import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;


import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.BlotterCaseDAO;
import org.example.Captain.CaptainSchedule;
import org.example.Captain.CaptainScheduleDAO;
import org.example.ResidentDAO;
import org.example.UserDataManager;
import org.example.Users.BlotterCase;
import org.example.Users.Resident;
import org.example.utils.AutoRefresher;


public class AdminBlotterTab extends JPanel implements Printable {




    // Fields
    private JTextField txtCaseNo;
    private JTextField txtDateRecorded;
    private JTextField txtTimeRecorded;




    // Complainant Section
    private JTextField txtComplainantName;
    private JButton btnPickComplainant;
    private JTextField txtVictim;




    // Respondent Section
    private JTextField txtRespondentName;
    private JButton btnPickRespondent;
    private JButton btnPickVictim;




    // Incident Details
    private JTextField txtIncidentDate;
    private JTextArea txtOfficer;
    private JTextField txtIncidentTime;
    private JTextField txtIncidentPlace;
    private JComboBox<String> cmbIncidentType;




    // Text Areas
    private JTextArea txtNarrative;
    private JTextArea txtResolution;
    private JTextArea txtWitnesses;




    // Table for blotter reports
    private JTable blotterTable;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JComboBox<String> cmbIncidentFilter;
    private JComboBox<String> cmbYearFilter;




    // Hidden ID
    private JTextField txtCaseId;




    // Summon Date
    private JTextField txtSummonDate;
    private JComboBox<String> cmbHearingDate;




    // Colors for modern UI
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BG_COLOR = new Color(245, 247, 250);
    private final Color SECTION_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(220, 226, 234);
    private final Color TABLE_HEADER_COLOR = new Color(41, 128, 185);
    private final Color TABLE_ROW_COLOR = new Color(248, 249, 250);
    private final Color OVERDUE_COLOR = new Color(230, 126, 34);
    private final Color SUMMON_COLOR = new Color(155, 89, 182);




    // Cerulean and Light Blue gradient colors
    private final Color CERULEAN = new Color(0, 123, 167);
    private final Color LIGHT_BLUE = new Color(173, 216, 230);




    // Fonts
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font SECTION_FONT = new Font("Segoe UI Semibold", Font.BOLD, 14);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 13);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    private final Font TABLE_HEADER_FONT = new Font("Segoe UI", Font.BOLD, 11);




    // Statistics
    private int totalCases = 0;
    private int resolvedCases = 0;
    private int pendingCases = 0;
    private int summonedCases = 0;
    private int ongoingCases = 0;
    private int todayCases = 0;
    private int overdueCases = 0;




    public AdminBlotterTab() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        SwingUtilities.invokeLater(this::initializeUI);
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override
            public void ancestorAdded(javax.swing.event.AncestorEvent event) {
                if (refresher != null) {
                    refresher.stop();
                }
                refresher = new AutoRefresher("Case", AdminBlotterTab.this::loadBlotterData);
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




    private void initializeUI() {
        // Create tabbed pane for form and reports
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));




        // Tab 1: Blotter Form
        JPanel formPanel = createFormPanel();
        tabbedPane.addTab("📝 New Blotter", formPanel);




        // Tab 2: Blotter Reports
        JPanel reportsPanel = createReportsPanel();
        tabbedPane.addTab("📊 Blotter Reports", reportsPanel);




        // Tab 3: Print Preview
        JPanel printPanel = createPrintPanel();
        tabbedPane.addTab("🖨️ Print Preview", printPanel);




        add(tabbedPane, BorderLayout.CENTER);
        initializeData();
    }
    private int monthCases = 0;
    private int dayCases = 0;


    private JComboBox<String> cmbMonthFilter;
    private JComboBox<String> cmbDayFilter;


    private JPanel createFormPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_COLOR);




        JPanel headerPanel = createModernHeader();
        mainPanel.add(headerPanel, BorderLayout.NORTH);




        JScrollPane scrollPane = new JScrollPane(createFormContentPanel());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);




        return mainPanel;
    }




    private JPanel createFormContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 30, 30, 30));




        // Case Info
        contentPanel.add(createSectionPanel("I. BLOTTER INFORMATION", createCaseInfoPanel()));
        contentPanel.add(Box.createVerticalStrut(15));




        // Complainant
        contentPanel.add(createSectionPanel("II. COMPLAINANT (NAGREREKLAMO)", createComplainantPanel()));
        contentPanel.add(Box.createVerticalStrut(15));




        // Respondent
        contentPanel.add(createSectionPanel("III. RESPONDENT (INIREREKLAMO)", createRespondentPanel()));
        contentPanel.add(Box.createVerticalStrut(15));




        // Victim
        contentPanel.add(createSectionPanel("IV. VICTIM (BIKTIMA)", createVictimPanel()));
        contentPanel.add(Box.createVerticalStrut(15));




        // Incident Details
        contentPanel.add(createSectionPanel("V. INCIDENT DETAILS", createIncidentPanel()));
        contentPanel.add(Box.createVerticalStrut(15));




        // Narrative with proper spacing
        contentPanel.add(createSectionPanel("VI. COMPLAINT/NARRATIVE (SALAYSAY)",
                createTextAreaPanel("Narrative Details:", txtNarrative = createStyledTextArea(5))));
        contentPanel.add(Box.createVerticalStrut(15));




        // Witnesses
        contentPanel.add(createSectionPanel("VII. WITNESSES (SAKSI)",
                createTextAreaPanel("Witness Information:", txtWitnesses = createStyledTextArea(3))));
        contentPanel.add(Box.createVerticalStrut(15));




        // Resolution
        contentPanel.add(createSectionPanel("VIII. RESOLUTION (PAGKAKASUNDO)",
                createTextAreaPanel("Resolution Details:", txtResolution = createStyledTextArea(4))));
        contentPanel.add(Box.createVerticalStrut(15));




        contentPanel.add(createSectionPanel("IX. OFFICER",
                createTextAreaPanel("Officer*:", txtOfficer = createStyledTextArea(2))));




        // Hearing & Summon
        contentPanel.add(createSectionPanel("X. HEARING & SUMMON DATES", createHearingSummonPanel()));
        contentPanel.add(Box.createVerticalStrut(15));




        // Signatures
        contentPanel.add(createSignatureSection());
        contentPanel.add(Box.createVerticalStrut(25));




        // Buttons
        contentPanel.add(createActionButtonsPanel());




        return contentPanel;
    }






    private JPanel createSectionPanel(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));




        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SECTION_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
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




    private JPanel createCaseInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 15));
        panel.setBackground(SECTION_BG);
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));




        txtCaseNo = createStyledTextField(generateCaseNumber(), false);
        txtDateRecorded = createStyledTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")), false);
        txtTimeRecorded = createStyledTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")), false);




        panel.add(createLabeledField("Blotter No:", txtCaseNo));
        panel.add(createLabeledField("Date Recorded:", txtDateRecorded));
        panel.add(createLabeledField("Time Recorded:", txtTimeRecorded));




        return panel;
    }






    private JPanel createComplainantPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SECTION_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;




        txtComplainantName = createStyledTextField("", true);
        btnPickComplainant = createModernPickerButton();




        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Full Name(s):"), gbc);




        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtComplainantName, gbc);




        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(btnPickComplainant, gbc);




        return panel;
    }




    private JPanel createRespondentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SECTION_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;




        txtRespondentName = createStyledTextField("", true);
        btnPickRespondent = createModernPickerButton();




        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Full Name(s):"), gbc);




        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtRespondentName, gbc);




        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(btnPickRespondent, gbc);




        return panel;
    }




    private JPanel createVictimPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SECTION_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;




        txtVictim = createStyledTextField("", true);
        btnPickVictim = createModernPickerButton();




        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Full Name(s):"), gbc);




        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtVictim, gbc);




        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(btnPickVictim, gbc);




        return panel;
    }




    private JPanel createIncidentPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 20, 15));
        panel.setBackground(SECTION_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));


        String [] dao = new SystemConfigDAO().getOptionsNature("incidentType");
        String[] incidentTypes = dao;
        cmbIncidentType = new JComboBox<>(incidentTypes);
        styleComboBox(cmbIncidentType);


        txtIncidentDate = createStyledTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), true);
        txtIncidentPlace = createStyledTextField("", true);


        // Create time field with 24-hour format validation
        txtIncidentTime = createTimeTextField();


        // Create AM/PM dropdown
        JComboBox<String> cmbAmPm = new JComboBox<>(new String[]{"AM", "PM"});
        styleComboBox(cmbAmPm);
        cmbAmPm.setSelectedIndex(0);


        // Create panel for time with AM/PM selector
        JPanel timePanel = new JPanel(new BorderLayout(5, 0));
        timePanel.setBackground(SECTION_BG);
        timePanel.add(txtIncidentTime, BorderLayout.CENTER);
        timePanel.add(cmbAmPm, BorderLayout.EAST);


        // Date picker button
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.setBackground(SECTION_BG);
        datePanel.add(txtIncidentDate, BorderLayout.CENTER);
        JButton btnDatePicker = new JButton("📅");
        btnDatePicker.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnDatePicker.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        btnDatePicker.setBackground(new Color(240, 240, 240));
        btnDatePicker.addActionListener(e -> showDatePicker(txtIncidentDate));
        datePanel.add(btnDatePicker, BorderLayout.EAST);


        panel.add(createLabeledField("Incident Type:", cmbIncidentType));
        panel.add(createLabeledField("Date (Petsa):", datePanel));
        panel.add(createLabeledField("Time (Oras):", timePanel));
        panel.add(createLabeledField("Location (Lugar):", txtIncidentPlace));


        // Add empty cells to fill the grid
        panel.add(new JPanel()); // Empty cell
        panel.add(new JPanel()); // Empty cell


        return panel;
    }
    private JTextField createTimeTextField() {
        JTextField timeField = new JTextField("12:00");
        timeField.setFont(FIELD_FONT);
        timeField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        timeField.setBackground(Color.WHITE);
        timeField.setToolTipText("Format: HH:MM (24-hour) e.g., 14:30");


        // Add document filter to allow only numbers and colon
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


        // Add focus listener for validation and auto-format
        timeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String time = timeField.getText().trim();
                if (!time.isEmpty()) {
                    // Auto-format to HH:MM
                    if (time.matches("^\\d{1,2}$")) {
                        // Single or double digit hour
                        int hour = Integer.parseInt(time);
                        if (hour < 0 || hour > 23) {
                            timeField.setBackground(new Color(255, 200, 200));
                            timeField.setToolTipText("Invalid hour! Must be 00-23");
                            return;
                        }
                        time = String.format("%02d:00", hour);
                        timeField.setText(time);
                    } else if (time.matches("^\\d{1,2}:\\d{1,2}$")) {
                        // Already has colon
                        String[] parts = time.split(":");
                        int hour = Integer.parseInt(parts[0]);
                        if (hour < 0 || hour > 23) {
                            timeField.setBackground(new Color(255, 200, 200));
                            timeField.setToolTipText("Invalid hour! Must be 00-23");
                            return;
                        }


                        String minuteStr = parts[1];
                        int minute = Integer.parseInt(minuteStr.length() > 2 ? minuteStr.substring(0, 2) : minuteStr);
                        if (minute < 0 || minute > 59) {
                            timeField.setBackground(new Color(255, 200, 200));
                            timeField.setToolTipText("Invalid minute! Must be 00-59");
                            return;
                        }


                        String hourStr = String.format("%02d", hour);
                        String minuteFormatted = String.format("%02d", minute);
                        timeField.setText(hourStr + ":" + minuteFormatted);
                    }


                    // Validate
                    if (!isValidTime(timeField.getText())) {
                        timeField.setBackground(new Color(255, 200, 200));
                        timeField.setToolTipText("Invalid time! Use HH:MM format (00:00 - 23:59)");
                    } else {
                        timeField.setBackground(Color.WHITE);
                        timeField.setToolTipText("Format: HH:MM (24-hour)");
                    }
                }
            }
        });


        // Add key listener for real-time validation
        timeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String time = timeField.getText();
                if (!time.isEmpty() && !isValidTime(time)) {
                    timeField.setBackground(new Color(255, 230, 230));
                } else {
                    timeField.setBackground(Color.WHITE);
                }
            }
        });


        return timeField;
    }
    private JPanel createHearingSummonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 15));
        panel.setBackground(SECTION_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));


        // Hearing Date with combo box and button
        JPanel hearingPanel = new JPanel(new BorderLayout(5, 0));
        hearingPanel.setBackground(SECTION_BG);


        String[] dates = getNext60Days();
        cmbHearingDate = new JComboBox<>(dates);
        styleComboBox(cmbHearingDate);
        cmbHearingDate.setEditable(true);


        hearingPanel.add(cmbHearingDate, BorderLayout.CENTER);


        JButton btnHearingDatePicker = new JButton("📅 Check");
        btnHearingDatePicker.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnHearingDatePicker.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnHearingDatePicker.setBackground(new Color(240, 240, 240));


        // Create a custom action for the combo box date picker
        btnHearingDatePicker.addActionListener(e -> {
            // Create a text field to pass to the date picker
            JTextField tempField = new JTextField();
            if (cmbHearingDate.getSelectedItem() != null) {
                tempField.setText(cmbHearingDate.getSelectedItem().toString());
            }


            // Show date picker
            showDatePickerForHearing(tempField);


            // Update combo box with selected date
            if (!tempField.getText().isEmpty()) {
                cmbHearingDate.setSelectedItem(tempField.getText());
            }
        });


        hearingPanel.add(btnHearingDatePicker, BorderLayout.EAST);


        // Summon Date
        txtSummonDate = createStyledTextField(LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), true);
        JPanel summonPanel = new JPanel(new BorderLayout(5, 0));
        summonPanel.setBackground(SECTION_BG);
        summonPanel.add(txtSummonDate, BorderLayout.CENTER);


        JButton btnSummonDatePicker = new JButton("📅");
        btnSummonDatePicker.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        btnSummonDatePicker.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        btnSummonDatePicker.setBackground(new Color(240, 240, 240));
        btnSummonDatePicker.addActionListener(e -> showDatePicker(txtSummonDate));
        summonPanel.add(btnSummonDatePicker, BorderLayout.EAST);


        panel.add(createLabeledField("Hearing Date (Petsa):", hearingPanel));
        panel.add(createLabeledField("Summon Date (Tawag sa Barangay):", summonPanel));


        // Add empty cells for the grid (2x2 grid but we only have 2 items)
        panel.add(new JPanel()); // Empty cell
        panel.add(new JPanel()); // Empty cell


        return panel;
    }


    private JPanel createTextAreaPanel(String label, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(SECTION_BG);

        JLabel areaLabel = new JLabel(label);
        areaLabel.setFont(LABEL_FONT);
        areaLabel.setForeground(new Color(80, 80, 80));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR, 1));
        scrollPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, textArea.getRows() * 50));

        // =================================================================
        // ✅ FIX: Stop Text Area from trapping the scroll
        // =================================================================
        scrollPane.setWheelScrollingEnabled(false); // Disable inner scrolling via wheel

        // Manually pass the scroll signal to the Main Form
        scrollPane.addMouseWheelListener(e -> {
            JScrollPane parentScroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, panel);
            if (parentScroll != null) {
                parentScroll.dispatchEvent(e);
            }
        });
        // =================================================================

        panel.add(areaLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }



    private JPanel createSignatureSection() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 30, 25));
        panel.setBackground(SECTION_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_COLOR, 2, true),
                new EmptyBorder(25, 25, 25, 25)
        ));




        panel.add(createSignatureField("COMPLAINANT'S SIGNATURE", ""));
        panel.add(createSignatureField("RESPONDENT'S SIGNATURE", ""));
        panel.add(createSignatureField("WITNESS SIGNATURE", ""));
        panel.add(createSignatureField("BARANGAY OFFICIAL", ""));




        return panel;
    }




    private JPanel createSignatureField(String title, String name) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SECTION_BG);




        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(70, 70, 70));




        JSeparator line = new JSeparator(SwingConstants.HORIZONTAL);
        line.setForeground(new Color(100, 100, 100));




        JLabel nameLabel = new JLabel(name.isEmpty() ? "" : "(" + name + ")", SwingConstants.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        nameLabel.setForeground(new Color(120, 120, 120));




        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(line, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);




        return panel;
    }


    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));


        JButton btnClear = createModernButton("Clear Form", WARNING_COLOR, "🗑️");
        JButton btnSave = createModernButton("Save Blotter", ACCENT_COLOR, "💾");
        JButton btnPrint = createModernButton("Print Form", PRIMARY_COLOR, "🖨️");
        JButton btnExport = createModernButton("Export PDF", SUMMON_COLOR, "📄");
        JButton btnUpdateStatus = createModernButton("Update Status", DANGER_COLOR, "🔄");


        // Add tooltips for better UX
        btnClear.setToolTipText("Clear all form fields (checks for unsaved data first)");
        btnSave.setToolTipText("Save the current blotter case");
        btnPrint.setToolTipText("Print the current form");
        btnExport.setToolTipText("Export to PDF format");
        btnUpdateStatus.setToolTipText("Update case status");


        btnClear.addActionListener(e -> clearForm());
        btnSave.addActionListener(e -> saveBlotter());
        btnPrint.addActionListener(e -> printBlotter());
        btnExport.addActionListener(e -> exportToPDF());
        btnUpdateStatus.addActionListener(e -> updateCaseStatusDialog());


        panel.add(btnClear);
        panel.add(btnSave);
        panel.add(btnPrint);
        panel.add(btnExport);
        panel.add(btnUpdateStatus);


        return panel;
    }
    // Optional: Method to check if all required fields are filled
    private boolean isFormCompletelyFilled() {
        List<String> missingFields = new ArrayList<>();


        // Check required fields
        if (txtComplainantName.getText().trim().isEmpty()) {
            missingFields.add("Complainant Name");
        }
        if (txtRespondentName.getText().trim().isEmpty()) {
            missingFields.add("Respondent Name");
        }
        if (txtIncidentPlace.getText().trim().isEmpty()) {
            missingFields.add("Incident Location");
        }
        if (cmbIncidentType.getSelectedItem() == null ||
                cmbIncidentType.getSelectedItem().toString().isEmpty()) {
            missingFields.add("Incident Type");
        }
        if (txtIncidentTime.getText().trim().isEmpty()) {
            missingFields.add("Incident Time");
        }


        // Validate time format
        if (!txtIncidentTime.getText().trim().isEmpty() &&
                !isValidTime(txtIncidentTime.getText().trim())) {
            missingFields.add("Incident Time (invalid format)");
        }


        if (missingFields.isEmpty()) {
            return true;
        } else {
            // Show which fields are missing
            StringBuilder message = new StringBuilder("The following required fields are empty:\n\n");
            for (String field : missingFields) {
                message.append("• ").append(field).append("\n");
            }
            message.append("\nPlease fill these fields before clearing the form.");


            JOptionPane.showMessageDialog(this,
                    message.toString(),
                    "Incomplete Form",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }


    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(BG_COLOR);




        // Header with search
        JPanel headerPanel = createReportsHeaderPanel();




        // Create table with gradient background
        String[] columns = {
                "Case No", "Date", "Complainant", "Respondent",
                "Incident Type", "Location", "Status", "Summon Date", "Hearing Date", "View/Edit", "Print"
        };




        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9 || column == 10; // Both View/Edit and Print columns are editable
            }




            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 9 || columnIndex == 10) {
                    return JButton.class;
                } else {
                    return String.class;
                }
            }
        };




        blotterTable = new JTable(tableModel);
        styleTable();




        // Create a custom scroll pane with gradient background
        JScrollPane tableScroll = new JScrollPane(blotterTable) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getViewport().getView() == blotterTable) {
                    Graphics2D g2d = (Graphics2D) g.create();




                    // Create gradient from Cerulean to Light Blue
                    GradientPaint gradient = new GradientPaint(
                            0, 0, CERULEAN,
                            getWidth(), getHeight(), LIGHT_BLUE
                    );




                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.dispose();
                }
            }
        };




        tableScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setOpaque(false);
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);
        tableScroll.getHorizontalScrollBar().setUnitIncrement(16);




        // Dashboard on left with gradient
        JPanel dashboardPanel = createDashboardPanel();
        JScrollPane dashboardScroll = new JScrollPane(dashboardPanel) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                GradientPaint gradient = new GradientPaint(
                        0, 0, CERULEAN.brighter(),
                        getWidth(), getHeight(), LIGHT_BLUE.brighter()
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        dashboardScroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        dashboardScroll.setPreferredSize(new Dimension(320, 0));
        dashboardScroll.getVerticalScrollBar().setUnitIncrement(16);
        dashboardScroll.setOpaque(false);
        dashboardScroll.getViewport().setOpaque(false);




        // Statistics at bottom with gradient
        JPanel statsPanel = createStatisticsPanel();




        // Main layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(statsPanel, BorderLayout.SOUTH);




        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(dashboardScroll, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);




        // Add listeners
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTable(); }
        });




        cmbStatusFilter.addActionListener(e -> filterTable());
        cmbIncidentFilter.addActionListener(e -> filterTable());
        cmbYearFilter.addActionListener(e -> filterTable());




        loadBlotterData();




        return panel;
    }
    private javax.swing.Timer smartTimer;  //
    private void startSmartPolling() {
        // Initialize blotter ID
        initializelastBlotterId();


        // Then start checking every 2 seconds
        smartTimer = new javax.swing.Timer(2000, e -> {
            // Only check if user isn't selecting a row in the blotter table
            if (blotterTable == null || blotterTable.getSelectedRow() == -1) {
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
                        loadBlotterData();// This will trigger updatelastBlotterId()
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }
    private int lastBlotterId = 0;


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


    private JPanel createReportsHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(CERULEAN);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));






        // Search and filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);


        // Year filter
        JPanel yearPanel = createFilterPanel("Year:", cmbYearFilter = new JComboBox<>(getYears()));
        yearPanel.setOpaque(false);


        // Month filter
        JPanel monthPanel = createFilterPanel("Month:",
                cmbMonthFilter = new JComboBox<>(new String[]{"All", "January", "February", "March",
                        "April", "May", "June", "July", "August", "September",
                        "October", "November", "December"}));
        monthPanel.setOpaque(false);


        // Day filter
        JPanel dayPanel = createFilterPanel("Day:",
                cmbDayFilter = new JComboBox<>(new String[]{"All", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                        "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                        "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"}));
        dayPanel.setOpaque(false);


        // Status filter
        JPanel statusPanel = createFilterPanel("Status:",
                cmbStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Resolved", "Hearing Scheduled", "Dismissed", "Certified to File Action","Amicably Settled"}));
        statusPanel.setOpaque(false);
// In createReportsHeaderPanel() method:
        String [] dao = new SystemConfigDAO().getOptionsNature("incidentType");


        JPanel incidentPanel = createFilterPanel("Incident:",
                cmbIncidentFilter = new JComboBox<>(dao));
        incidentPanel.setOpaque(false);


        // Search field
        txtSearch = new JTextField(20);
        txtSearch.setFont(FIELD_FONT);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search cases...");
        txtSearch.setBackground(new Color(255, 255, 255, 200));


        // Refresh button




        // Add filter change listeners
        cmbYearFilter.addActionListener(e -> filterTable());
        cmbMonthFilter.addActionListener(e -> filterTable());
        cmbDayFilter.addActionListener(e -> filterTable());


        filterPanel.add(yearPanel);
        filterPanel.add(monthPanel);
        filterPanel.add(dayPanel);
        filterPanel.add(statusPanel);
        filterPanel.add(incidentPanel);
        filterPanel.add(txtSearch);


        panel.add(filterPanel, BorderLayout.EAST);


        return panel;
    }
    private void checkDerogatoryRecord() {
        String names = JOptionPane.showInputDialog(this,
                "Enter comma-separated names to check:",
                "Check Derogatory Records",
                JOptionPane.QUESTION_MESSAGE);


        if (names != null && !names.trim().isEmpty()) {
            BlotterCaseDAO dao = new BlotterCaseDAO();
            String result = dao.checkMultiplePeople(names);


            if (result.equals("CLEAN")) {
                JOptionPane.showMessageDialog(this,
                        "No derogatory records found for: " + names,
                        "Clean Record", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Derogatory records found:\n" + result,
                        "Records Found", JOptionPane.WARNING_MESSAGE);
            }
        }
    }


    private JPanel createPrintPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));




        JLabel titleLabel = new JLabel("🖨️ PRINT BLOTTER FORM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));




        // Create print preview panel
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(Color.WHITE);
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PRIMARY_COLOR, 2),
                new EmptyBorder(20, 20, 20, 20)
        ));




        // Add print preview content
        JTextArea previewText = new JTextArea();
        previewText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        previewText.setEditable(false);
        previewText.setLineWrap(true);
        previewText.setWrapStyleWord(true);
        previewText.setText(generatePrintPreviewText());




        JScrollPane scrollPane = new JScrollPane(previewText);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));




        previewPanel.add(new JLabel("Print Preview:", SwingConstants.LEFT), BorderLayout.NORTH);
        previewPanel.add(scrollPane, BorderLayout.CENTER);




        // Print options panel
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBackground(SECTION_BG);
        optionsPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(15, 0, 0, 0)
        ));




        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;




        JCheckBox chkHeader = new JCheckBox(" Include Official Header");
        chkHeader.setSelected(true);
        chkHeader.setFont(new Font("Segoe UI", Font.PLAIN, 12));




        JCheckBox chkSignatures = new JCheckBox(" Include Signature Lines");
        chkSignatures.setSelected(true);
        chkSignatures.setFont(new Font("Segoe UI", Font.PLAIN, 12));




        JCheckBox chkWatermark = new JCheckBox(" Add Confidential Watermark");
        chkWatermark.setFont(new Font("Segoe UI", Font.PLAIN, 12));




        JLabel copiesLabel = new JLabel("Copies:");
        copiesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));




        JSpinner copiesSpinner = new JSpinner();
        ((SpinnerNumberModel) copiesSpinner.getModel()).setMinimum(1);
        ((SpinnerNumberModel) copiesSpinner.getModel()).setMaximum(10);
        copiesSpinner.setValue(1);




        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        optionsPanel.add(chkHeader, gbc);




        gbc.gridy = 1;
        optionsPanel.add(chkSignatures, gbc);




        gbc.gridy = 2;
        optionsPanel.add(chkWatermark, gbc);




        gbc.gridwidth = 1;
        gbc.gridy = 3; gbc.gridx = 0;
        optionsPanel.add(copiesLabel, gbc);




        gbc.gridx = 1;
        optionsPanel.add(copiesSpinner, gbc);




        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(SECTION_BG);




        JButton btnPreview = createModernButton("Refresh Preview", PRIMARY_COLOR, "👁️");
        JButton btnPrint = createModernButton("Print Document", ACCENT_COLOR, "🖨️");
        JButton btnSavePDF = createModernButton("Save as PDF", SUMMON_COLOR, "💾");




        btnPreview.addActionListener(e -> previewText.setText(generatePrintPreviewText()));
        btnPrint.addActionListener(e -> printBlotter());
        btnSavePDF.addActionListener(e -> exportToPDF());




        buttonPanel.add(btnPreview);
        buttonPanel.add(btnPrint);
        buttonPanel.add(btnSavePDF);




        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(SECTION_BG);
        southPanel.add(optionsPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);




        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(previewPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);




        return panel;
    }




    private String generatePrintPreviewText() {
        StringBuilder sb = new StringBuilder();




        // Official Header
        sb.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                         REPUBLIC OF THE PHILIPPINES                          ║\n");
        sb.append("║                       PROVINCE OF CAMARINES NORTE                            ║\n");
        sb.append("║                          CITY OF DAET                                        ║\n");
        sb.append("║                       BARANGAY ALAWIHAO                                      ║\n");
        sb.append("║                  OFFICE OF THE PUNONG BARANGAY                               ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
        sb.append("║                          OFFICIAL BLOTTER FORM                               ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════════════════════╝\n\n");




        // Case Information
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                          CASE INFORMATION\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("Case No:           %-60s\n", txtCaseNo.getText()));
        sb.append(String.format("Date Recorded:     %-60s\n", txtDateRecorded.getText()));
        sb.append(String.format("Time Recorded:     %-60s\n", txtTimeRecorded.getText()));
        sb.append("\n");




        // Parties Involved
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                          PARTIES INVOLVED\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("COMPLAINANT:       %-60s\n", txtComplainantName.getText()));
        sb.append(String.format("RESPONDENT:        %-60s\n", txtRespondentName.getText()));
        sb.append(String.format("VICTIM:            %-60s\n", txtVictim.getText()));
        sb.append("\n");




        // Incident Details
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                          INCIDENT DETAILS\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("Incident Type:     %-60s\n", cmbIncidentType.getSelectedItem()));
        sb.append(String.format("Date:              %-60s\n", txtIncidentDate.getText()));
        sb.append(String.format("Time:              %-60s\n", txtIncidentTime.getText()));
        sb.append(String.format("Location:          %-60s\n", txtIncidentPlace.getText()));
        sb.append("\n");




        // Narrative
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                          NARRATIVE OF EVENTS\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        String narrative = txtNarrative.getText();
        if (narrative.isEmpty()) narrative = "[Narrative not provided]";
        sb.append(wrapText(narrative, 70)).append("\n\n");




        // Resolution
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                          RESOLUTION\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        String resolution = txtResolution.getText();
        if (resolution.isEmpty()) resolution = "[Resolution not provided]";
        sb.append(wrapText(resolution, 70)).append("\n\n");




        // Witnesses
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                          WITNESSES\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        String witnesses = txtWitnesses.getText();
        if (witnesses.isEmpty()) witnesses = "[No witnesses listed]";
        sb.append(wrapText(witnesses, 70)).append("\n\n");




        // Officer and Dates
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                          OFFICIAL DETAILS\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append(String.format("Officer:           %-60s\n", txtOfficer.getText()));
        sb.append(String.format("Hearing Date:      %-60s\n", cmbHearingDate.getSelectedItem()));
        sb.append(String.format("Summon Date:       %-60s\n", txtSummonDate.getText()));
        sb.append("\n");




        // Signatures
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                          SIGNATURES\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("\n");
        sb.append("Complainant: ___________________________________ Date: _______________\n");
        sb.append("\n");
        sb.append("Respondent:  ___________________________________ Date: _______________\n");
        sb.append("\n");
        sb.append("Witness:     ___________________________________ Date: _______________\n");
        sb.append("\n");
        sb.append("Officer:     ___________________________________ Date: _______________\n");
        sb.append("\n");




        // Footer
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("NOTE: This is an official document of Barangay Central. Any falsification of\n");
        sb.append("information is punishable under the Revised Penal Code of the Philippines.\n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("Generated on: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        sb.append(" at ").append(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))).append("\n");




        return sb.toString();
    }




    private String wrapText(String text, int width) {
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();




        for (String word : words) {
            if (line.length() + word.length() + 1 > width) {
                wrapped.append(line).append("\n");
                line = new StringBuilder(word);
            } else {
                if (line.length() > 0) {
                    line.append(" ");
                }
                line.append(word);
            }
        }
        wrapped.append(line);




        return wrapped.toString();
    }




    private JPanel createModernHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(new EmptyBorder(15, 0, 15, 0));




        JLabel title = new JLabel("BARANGAY BLOTTER SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.black);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);




        JLabel subtitle = new JLabel("Administrative Dashboard", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(220, 220, 220));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);




        panel.add(title);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitle);




        return panel;
    }




    private JPanel createFilterPanel(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(3, 0));
        panel.setOpaque(false);




        JLabel lbl = new JLabel(label);
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(Color.black);




        styleComboBox(comboBox);
        comboBox.setBackground(new Color(255, 255, 255, 220));
        comboBox.setPreferredSize(new Dimension(120, 30));




        panel.add(lbl, BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.CENTER);




        return panel;
    }






    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));


        // Quick Stats Header
        JLabel statsLabel = new JLabel("📊 QUICK STATISTICS");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsLabel.setForeground(Color.WHITE);
        statsLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        panel.add(statsLabel);


        // Stats cards with glass effect
        JPanel statsGrid = new JPanel();
        statsGrid.setLayout(new BoxLayout(statsGrid, BoxLayout.Y_AXIS));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(280, 550));


        // Add vertical glue at top
        statsGrid.add(Box.createVerticalGlue());


        statsGrid.add(createGlassStatCard("Total Cases", String.valueOf(totalCases), Color.WHITE, "📋"));
        statsGrid.add(Box.createVerticalStrut(8));
        statsGrid.add(createGlassStatCard("Resolved", String.valueOf(resolvedCases), ACCENT_COLOR, "✅"));
        statsGrid.add(Box.createVerticalStrut(8));
        statsGrid.add(createGlassStatCard("Pending", String.valueOf(pendingCases), WARNING_COLOR, "⏳"));
        statsGrid.add(Box.createVerticalStrut(8));
        statsGrid.add(createGlassStatCard("Summoned", String.valueOf(summonedCases), DANGER_COLOR, "⚖️"));
        statsGrid.add(Box.createVerticalStrut(8));
        statsGrid.add(createGlassStatCard("Ongoing", String.valueOf(ongoingCases), SUMMON_COLOR, "🔄"));
        statsGrid.add(Box.createVerticalStrut(8));
        statsGrid.add(createGlassStatCard("Today's Cases", String.valueOf(todayCases), new Color(52, 152, 219), "📅"));
        statsGrid.add(Box.createVerticalStrut(8));
        statsGrid.add(createGlassStatCard("Overdue Cases", String.valueOf(overdueCases), OVERDUE_COLOR, "⏰"));
        statsGrid.add(Box.createVerticalStrut(8));
        statsGrid.add(createGlassStatCard("This Month", String.valueOf(monthCases), new Color(142, 68, 173), "📆"));
        statsGrid.add(Box.createVerticalStrut(8));
        statsGrid.add(createGlassStatCard("Today", String.valueOf(dayCases), new Color(39, 174, 96), "📅"));
// In createDashboardPanel() or createGlassButton() section:
        JButton btnCheckRecords = createGlassButton("Check Records", "🔍");
        btnCheckRecords.addActionListener(e -> checkDerogatoryRecord());
        // Add vertical glue at bottom
        statsGrid.add(Box.createVerticalGlue());


        panel.add(statsGrid);
        panel.add(Box.createVerticalStrut(25));


        // Quick Actions Header
        JLabel actionsLabel = new JLabel("⚡ QUICK ACTIONS");
        actionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        actionsLabel.setForeground(Color.WHITE);
        actionsLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        actionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        panel.add(actionsLabel);


        // Action buttons grid
        JPanel actionsGrid = new JPanel();
        actionsGrid.setLayout(new BoxLayout(actionsGrid, BoxLayout.Y_AXIS));
        actionsGrid.setOpaque(false);
        actionsGrid.setMaximumSize(new Dimension(280, 220));


        // Add vertical glue at top
        actionsGrid.add(Box.createVerticalGlue());


        JButton btnHistory = createGlassButton("Case History", "📜");
        JButton btnCalendar = createGlassButton("Hearing Calendar", "📅");
        JButton btnAnalytics = createGlassButton("Analytics", "📈");
        JButton btnExportAll = createGlassButton("Export All", "📄");

        btnExportAll.addActionListener(e->exportAllToPDF());
        actionsGrid.add(btnExportAll);


        // Add vertical glue at bottom
        actionsGrid.add(Box.createVerticalGlue());


        panel.add(actionsGrid);


        return panel;
    }
    private void resetStatistics() {
        totalCases = 0;
        resolvedCases = 0;
        pendingCases = 0;
        summonedCases = 0;
        ongoingCases = 0;
        todayCases = 0;
        overdueCases = 0;
        monthCases = 0;
        dayCases = 0;
    }
    private void filterTable() {
        String searchText = txtSearch.getText().toLowerCase();
        String statusFilter = (String) cmbStatusFilter.getSelectedItem();
        String incidentFilter = (String) cmbIncidentFilter.getSelectedItem();
        String yearFilter = (String) cmbYearFilter.getSelectedItem();
        String monthFilter = (String) cmbMonthFilter.getSelectedItem();
        String dayFilter = (String) cmbDayFilter.getSelectedItem();


        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        blotterTable.setRowSorter(sorter);


        if (searchText.isEmpty() && "All".equals(statusFilter) &&
                "All".equals(incidentFilter) && "All".equals(yearFilter) &&
                "All".equals(monthFilter) && "All".equals(dayFilter)) {
            sorter.setRowFilter(null);
        } else {
            java.util.List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();


            if (!searchText.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + searchText, 0, 2, 3, 4, 5));
            }


            if (!"All".equals(statusFilter)) {
                filters.add(RowFilter.regexFilter("^" + statusFilter + "$", 6));
            }


            if (!"All".equals(incidentFilter)) {
                filters.add(RowFilter.regexFilter("(?i)" + incidentFilter, 4));
            }


            if (yearFilter != null && !yearFilter.equals("All")) {
                filters.add(RowFilter.regexFilter(".*" + yearFilter + ".*", 1));
            }


            // Apply month filter
            if (!"All".equals(monthFilter)) {
                int monthNumber = getMonthNumber(monthFilter);
                String monthPattern = String.format(".*%02d.*", monthNumber);
                filters.add(RowFilter.regexFilter(monthPattern, 1));
            }


            // Apply day filter
            if (!"All".equals(dayFilter)) {
                String dayPattern = String.format(".*-%02d-.*", Integer.parseInt(dayFilter));
                filters.add(RowFilter.regexFilter(dayPattern, 1));
            }


            sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        }
    }


    private int getMonthNumber(String monthName) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return i + 1;
            }
        }
        return -1;
    }
    private void updateCaseStatusDialog() {
        // Check if a row is selected in the table
        int selectedRow = blotterTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a case from the table first!",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }


        // Get case number from selected row
        int modelRow = blotterTable.convertRowIndexToModel(selectedRow);
        String caseNumber = (String) tableModel.getValueAt(modelRow, 0);


        // Load case data from database
        BlotterCaseDAO dao = new BlotterCaseDAO();
        BlotterCase currentCase = dao.findCaseByNumber(caseNumber);


        if (currentCase == null) {
            JOptionPane.showMessageDialog(this,
                    "Case not found in database: " + caseNumber,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // Create dialog
        JDialog statusDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Update Case Status - Case: " + caseNumber, true);
        statusDialog.setSize(450, 400); // Increased size for better layout
        statusDialog.setLayout(new BorderLayout());
        statusDialog.setLocationRelativeTo(this);


        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        // Case number field (read-only)
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        contentPanel.add(new JLabel("Case Number:"), gbc);


        JTextField txtCaseNumber = new JTextField(caseNumber);
        txtCaseNumber.setEditable(false);
        txtCaseNumber.setBackground(new Color(240, 240, 240));
        gbc.gridx = 1; gbc.weightx = 1.0;
        contentPanel.add(txtCaseNumber, gbc);


        // Status combo
        gbc.gridy = 1; gbc.gridx = 0;
        contentPanel.add(new JLabel("New Status:"), gbc);


        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{
                "Pending", "Ongoing", "Resolved", "Summoned", "Closed",
                "Amicably Settled", "Certified to File Action", "Dismissed"
        });


        String currentStatus = currentCase.getStatus();
        if (currentStatus != null && !currentStatus.isEmpty()) {
            cmbStatus.setSelectedItem(currentStatus);
        }
        gbc.gridx = 1;
        contentPanel.add(cmbStatus, gbc);


        // Resolution text area
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        contentPanel.add(new JLabel("Resolution:"), gbc);


        JTextArea txtResolution = new JTextArea(4, 35);
        txtResolution.setLineWrap(true);
        txtResolution.setWrapStyleWord(true);


        String existingResolution = currentCase.getResolution();
        if (existingResolution != null && !existingResolution.isEmpty()) {
            txtResolution.setText(existingResolution);
        }


        JScrollPane scrollPane = new JScrollPane(txtResolution);
        gbc.gridy = 3;
        contentPanel.add(scrollPane, gbc);


        // Hearing date WITH BUTTON
        gbc.gridy = 4; gbc.gridwidth = 1;
        gbc.gridx = 0;
        contentPanel.add(new JLabel("Hearing Date:"), gbc);


        String hearingDateStr = "";
        try {
            // Try to get hearing date from BlotterCase
            java.lang.reflect.Method getHearingDateMethod = currentCase.getClass().getMethod("getHearingDate");
            Object hearingDateObj = getHearingDateMethod.invoke(currentCase);
            if (hearingDateObj instanceof LocalDate) {
                hearingDateStr = ((LocalDate) hearingDateObj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if (hearingDateObj != null) {
                hearingDateStr = hearingDateObj.toString();
            }
        } catch (Exception e) {
            // If method doesn't exist or error, check table column
            try {
                Object tableHearingDate = tableModel.getValueAt(modelRow, 8); // Column 8 is Hearing Date
                if (tableHearingDate != null && !tableHearingDate.toString().equals("Not Set")) {
                    hearingDateStr = tableHearingDate.toString();
                }
            } catch (Exception ex) {
                // Ignore
            }
        }


        if (hearingDateStr == null || hearingDateStr.isEmpty() || hearingDateStr.equals("Not Set")) {
            hearingDateStr = LocalDate.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }


        JTextField txtHearingDate = new JTextField(hearingDateStr);


        // Hearing date picker button
        JButton btnHearingDatePicker = new JButton("📅 Check Availability");
        btnHearingDatePicker.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnHearingDatePicker.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnHearingDatePicker.setBackground(new Color(240, 240, 240));
        btnHearingDatePicker.addActionListener(e -> showDatePickerForHearing(txtHearingDate));


        // Create panel for hearing date with button
        JPanel hearingDatePanel = new JPanel(new BorderLayout(5, 0));
        hearingDatePanel.add(txtHearingDate, BorderLayout.CENTER);
        hearingDatePanel.add(btnHearingDatePicker, BorderLayout.EAST);


        gbc.gridx = 1;
        contentPanel.add(hearingDatePanel, gbc);


        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnUpdate = new JButton("Update Status");
        JButton btnCancel = new JButton("Cancel");


        btnUpdate.addActionListener(e -> {
            String newStatus = (String) cmbStatus.getSelectedItem();
            String resolution = txtResolution.getText().trim();
            String hearingDate = txtHearingDate.getText().trim();


            // Validate hearing date format
            try {
                LocalDate.parse(hearingDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(statusDialog,
                        "Invalid date format! Please use yyyy-MM-dd format.",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (dao.updateCaseStatus(caseNumber, newStatus, resolution, hearingDate)) {
                JOptionPane.showMessageDialog(statusDialog,
                        "Status updated successfully!\n" +
                                "Case: " + caseNumber + "\n" +
                                "New Status: " + newStatus + "\n" +
                                "Hearing Date: " + hearingDate,
                        "Success", JOptionPane.INFORMATION_MESSAGE);


                loadBlotterData(); // Refresh table
                statusDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(statusDialog,
                        "Failed to update status!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        btnCancel.addActionListener(e -> statusDialog.dispose());


        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnCancel);


        statusDialog.add(contentPanel, BorderLayout.CENTER);
        statusDialog.add(buttonPanel, BorderLayout.SOUTH);
        statusDialog.setVisible(true);
    }
    private void updateStatistics(BlotterCase c, LocalDate today) {
        totalCases++;


        String status = c.getStatus();
        if (status != null) {
            switch (status) {
                case "Resolved": resolvedCases++; break;
                case "Pending": pendingCases++; break;
                case "Summoned": summonedCases++; break;
                case "Ongoing": ongoingCases++; break;
            }
        }


        // Check if case was recorded today
        LocalDate dateRecorded = c.getDateRecorded();
        if (dateRecorded != null) {
            if (dateRecorded.equals(today)) {
                todayCases++;
                dayCases++;
                System.out.println("Today's case: " + c.getCaseNumber());
            }


            // Check if case is from current month
            if (dateRecorded.getMonthValue() == today.getMonthValue() &&
                    dateRecorded.getYear() == today.getYear()) {
                monthCases++;
            }
        }


        // Check if hearing date is overdue
        try {
            LocalDate hearingDate = c.getHearingDate();
            if (hearingDate != null) {
                if (hearingDate.isBefore(today) &&
                        !"Resolved".equals(c.getStatus()) &&
                        !"Closed".equals(c.getStatus())) {
                    overdueCases++;


                }
            }
        } catch (Exception e) {
            // Method doesn't exist or error
        }
    }


    private JPanel createGlassStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(new Color(255, 255, 255, 30));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(255, 255, 255, 60), 1),
                new EmptyBorder(12, 15, 12, 15)
        ));
        card.setMaximumSize(new Dimension(280, 60));


        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(150, 30));


        JLabel titleLabel = new JLabel(icon + " " + title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(240, 240, 240));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        leftPanel.add(titleLabel);


        // Create value label and store reference
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);


        // Store references based on title
        switch(title) {
            case "Total Cases": lblTotalCases = valueLabel; break;
            case "Resolved": lblResolved = valueLabel; break;
            case "Pending": lblPending = valueLabel; break;
            case "Summoned": lblSummoned = valueLabel; break;
            case "Ongoing": lblOngoing = valueLabel; break;
            case "Today's Cases": lblToday = valueLabel; break;
            case "This Month": lblMonth = valueLabel; break;
            case "Today": lblDay = valueLabel; break;
            case "Overdue Cases": lblOverdue = valueLabel; break;
        }


        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(valueLabel, gbc);


        card.add(leftPanel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.CENTER);


        return card;
    }private void updateAllStatistics() {
        // Update dashboard statistics
        if (lblTotalCases != null) {
            lblTotalCases.setText(String.valueOf(totalCases));
            lblResolved.setText(String.valueOf(resolvedCases));
            lblPending.setText(String.valueOf(pendingCases));
            lblSummoned.setText(String.valueOf(summonedCases));
            lblOngoing.setText(String.valueOf(ongoingCases));
            lblToday.setText(String.valueOf(todayCases));
            lblMonth.setText(String.valueOf(monthCases));
            lblDay.setText(String.valueOf(dayCases));
            if (lblOverdue != null) {
                lblOverdue.setText(String.valueOf(overdueCases));
            }
        }


        // Update statistics panel labels
        if (statLabels != null && statLabels.length >= 7) {
            statLabels[0].setText("📊 Total: " + totalCases);
            statLabels[1].setText("✅ Resolved: " + resolvedCases);
            statLabels[2].setText("⏳ Pending: " + pendingCases);
            statLabels[3].setText("⚖️ Summoned: " + summonedCases);
            statLabels[4].setText("🔄 Ongoing: " + ongoingCases);
            statLabels[5].setText("📅 Today: " + dayCases);
            statLabels[6].setText("📆 Month: " + monthCases);
        }
    }


    private void updateStatisticsPanel() {
        // Find the statistics panel and update it
        for (Component comp : this.getComponents()) {
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) comp;
                Component reportsTab = tabbedPane.getComponentAt(1);
                if (reportsTab instanceof JPanel) {
                    updateStatisticsPanelInReports((JPanel) reportsTab);
                    break;
                }
            }
        }
    }




    private JButton createGlassButton(String text, String icon) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(255, 255, 255, 40));
        button.setForeground(Color.WHITE); // FIX: Change to white for better visibility
        button.setFocusPainted(false);
        button.setBorderPainted(false);




        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);




        // FIX: Set fixed size to prevent layout issues
        button.setPreferredSize(new Dimension(250, 45));
        button.setMaximumSize(new Dimension(250, 45));
        button.setMinimumSize(new Dimension(250, 45));




        // FIX: Set content area filled to false for cleaner look
        button.setContentAreaFilled(true);




        // FIX: Add padding to icon and text
        button.setIconTextGap(10);




        // Glass hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(255, 255, 255, 80));
                button.setForeground(Color.BLACK); // Change text color on hover
                button.repaint();
            }
            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(255, 255, 255, 40));
                button.setForeground(Color.WHITE);
                button.repaint();
            }
        });




        return button;
    }private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panel.setBackground(new Color(255, 255, 255, 180));
        panel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(255, 255, 255, 100)),
                new EmptyBorder(10, 15, 10, 15)
        ));


        // Create array to store labels
        statLabels = new JLabel[7];


        // Add statistics labels
        statLabels[0] = createGlassStatLabel("📊 Total: " + totalCases, Color.WHITE);
        statLabels[1] = createGlassStatLabel("✅ Resolved: " + resolvedCases, ACCENT_COLOR);
        statLabels[2] = createGlassStatLabel("⏳ Pending: " + pendingCases, WARNING_COLOR);
        statLabels[3] = createGlassStatLabel("⚖️ Summoned: " + summonedCases, DANGER_COLOR);
        statLabels[4] = createGlassStatLabel("🔄 Ongoing: " + ongoingCases, SUMMON_COLOR);
        statLabels[5] = createGlassStatLabel("📅 Today: " + dayCases, new Color(39, 174, 96));
        statLabels[6] = createGlassStatLabel("📆 Month: " + monthCases, new Color(142, 68, 173));


        for (JLabel label : statLabels) {
            panel.add(label);
        }


        return panel;
    }




    private JLabel createGlassStatLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Semibold", Font.BOLD, 11));
        label.setForeground(color);
        label.setBorder(new CompoundBorder(
                new LineBorder(new Color(255, 255, 255, 60), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
        label.setBackground(new Color(255, 255, 255, 30));
        label.setOpaque(true);
        return label;
    }
    private String[] getYears() {
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[6];
        years[0] = "All";
        for (int i = 1; i <= 5; i++) {
            years[i] = String.valueOf(currentYear - i + 1);
        }
        return years;
    }




    private void styleTable() {
        blotterTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        blotterTable.setRowHeight(35);
        blotterTable.setShowGrid(true);
        blotterTable.setGridColor(new Color(255, 255, 255, 50));
        blotterTable.setSelectionBackground(new Color(255, 255, 255, 100));
        blotterTable.setSelectionForeground(Color.BLACK);
        blotterTable.setOpaque(false);
        blotterTable.setFillsViewportHeight(true);




        // Custom cell renderer for text columns
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);




                // Make background transparent
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 0));




                // Set text color
                if (isSelected) {
                    setForeground(Color.BLACK);
                } else {
                    setForeground(Color.WHITE);
                }




                // Center alignment for certain columns
                if (column == 0 || column == 1 || column == 6 || column == 7 || column == 8) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }




                return c;
            }
        };




        // Apply renderer to text columns
        for (int i = 0; i < blotterTable.getColumnCount(); i++) {
            if (i != 9 && i != 10) { // Skip button columns
                blotterTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        }




        // Header styling with glass effect
        JTableHeader header = blotterTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(255, 255, 255, 150));
        header.setForeground(CERULEAN.darker());
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));




        // Column widths
        TableColumnModel columnModel = blotterTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(90);
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(3).setPreferredWidth(120);
        columnModel.getColumn(4).setPreferredWidth(110);
        columnModel.getColumn(5).setPreferredWidth(110);
        columnModel.getColumn(6).setPreferredWidth(80);
        columnModel.getColumn(7).setPreferredWidth(100);
        columnModel.getColumn(8).setPreferredWidth(100);
        columnModel.getColumn(9).setPreferredWidth(80);  // View/Edit button
        columnModel.getColumn(10).setPreferredWidth(70); // Print button




        // Status renderer with colors
        columnModel.getColumn(6).setCellRenderer(new StatusRenderer());




        // Action buttons renderers and editors
        // In styleTable() method:
        columnModel.getColumn(9).setCellRenderer(new ViewEditButtonRenderer());
        columnModel.getColumn(9).setCellEditor(new ViewEditButtonEditor(new JCheckBox()));


        columnModel.getColumn(10).setCellRenderer(new PrintButtonRenderer());
        columnModel.getColumn(10).setCellEditor(new PrintButtonEditor(new JCheckBox()));
    }
    private boolean isCaptainAvailable(LocalDate date, LocalTime time) {
        try {
            CaptainScheduleDAO scheduleDAO = new CaptainScheduleDAO();
            return scheduleDAO.isTimeSlotAvailable(date, time, time.plusHours(1)); // Assuming 1-hour hearings
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void populateHearingDateComboBox() {
        CaptainScheduleDAO scheduleDAO = new CaptainScheduleDAO();
        List<CaptainSchedule> schedules = scheduleDAO.getAllSchedules();


        // Clear existing items
        cmbHearingDate.removeAllItems();
        cmbHearingDate.addItem("Select Date");


        // Add available dates
        for (CaptainSchedule schedule : schedules) {
            if (schedule.isAvailable()) {
                String dateStr = schedule.getScheduleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String day = schedule.getDayOfWeek();
                String timeSlot = schedule.getStartTime() + " - " + schedule.getEndTime();
                cmbHearingDate.addItem(dateStr + " (" + day + ") " + timeSlot);
            }
        }
    }
    private void showDatePickerForHearing(JTextField targetField) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Select Hearing Date (Captain's Available Schedule)", true);
        dialog.setSize(500, 400);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);


        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        // Calendar panel
        JPanel calendarPanel = new JPanel(new BorderLayout());


        // Create calendar
        JSpinner monthSpinner = new JSpinner(new SpinnerNumberModel(
                LocalDate.now().getMonthValue(), 1, 12, 1));
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(
                LocalDate.now().getYear(), 2020, 2030, 1));


        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.add(new JLabel("Month:"));
        controlPanel.add(monthSpinner);
        controlPanel.add(new JLabel("Year:"));
        controlPanel.add(yearSpinner);


        // Calendar grid
        JPanel gridPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        gridPanel.setBorder(new EmptyBorder(10, 0, 10, 0));


        // Day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            dayLabel.setForeground(Color.BLUE);
            gridPanel.add(dayLabel);
        }


        // Load captain's available dates
        CaptainScheduleDAO scheduleDAO = new CaptainScheduleDAO();
        List<CaptainSchedule> availableSchedules = scheduleDAO.getAllSchedules();
        List<LocalDate> availableDates = new java.util.ArrayList<>();


        for (CaptainSchedule schedule : availableSchedules) {
            if (schedule.isAvailable()) {
                availableDates.add(schedule.getScheduleDate());
            }
        }


        // Create day buttons
        LocalDate currentDate = LocalDate.now();
        LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0 = Sunday


        // Fill empty cells for days before the first day of month
        for (int i = 0; i < dayOfWeek; i++) {
            gridPanel.add(new JLabel(""));
        }


        // Create buttons for each day of the month
        int daysInMonth = currentDate.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 12));


            // Check if date is available
            if (availableDates.contains(date)) {
                dayButton.setBackground(new Color(46, 204, 113));
                dayButton.setForeground(Color.WHITE);
                dayButton.setEnabled(true);
                dayButton.setToolTipText("Captain is available on " + date);


                dayButton.addActionListener(e -> {
                    targetField.setText(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    dialog.dispose();
                });
            } else {
                dayButton.setBackground(Color.LIGHT_GRAY);
                dayButton.setForeground(Color.DARK_GRAY);
                dayButton.setEnabled(false);
                dayButton.setToolTipText("Captain is not available on " + date);
            }


            gridPanel.add(dayButton);
        }


        calendarPanel.add(controlPanel, BorderLayout.NORTH);
        calendarPanel.add(gridPanel, BorderLayout.CENTER);


        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new CompoundBorder(
                new LineBorder(Color.GRAY, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));


        JLabel infoLabel = new JLabel("<html><b>Legend:</b><br>" +
                "<font color='#2ecc71'>● Available</font> - Captain is available for hearing<br>" +
                "<font color='#95a5a6'>● Unavailable</font> - Captain is not available</html>");
        infoPanel.add(infoLabel, BorderLayout.CENTER);


        mainPanel.add(calendarPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);


        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    private void refreshCalendarGrid(JPanel gridPanel, LocalDate date, List<LocalDate> availableDates,
                                     JTextField targetField, JDialog dialog) {
        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7; // 0 = Sunday


        // Fill empty cells for days before the first day of month
        for (int i = 0; i < dayOfWeek; i++) {
            gridPanel.add(new JLabel(""));
        }


        // Create buttons for each day of the month
        int daysInMonth = date.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = LocalDate.of(date.getYear(), date.getMonthValue(), day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 12));


            // Check if date is available
            if (availableDates.contains(currentDate)) {
                dayButton.setBackground(new Color(46, 204, 113));
                dayButton.setForeground(Color.WHITE);
                dayButton.setEnabled(true);
                dayButton.setToolTipText("Captain is available on " + currentDate);


                dayButton.addActionListener(e -> {
                    targetField.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    dialog.dispose();
                });
            } else {
                dayButton.setBackground(Color.LIGHT_GRAY);
                dayButton.setForeground(Color.DARK_GRAY);
                dayButton.setEnabled(false);
                dayButton.setToolTipText("Captain is not available on " + currentDate);
            }


            gridPanel.add(dayButton);
        }
    }


    // =================== FUNCTIONALITY METHODS ===================
    private void loadBlotterData() {
        SwingWorker<List<BlotterCase>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BlotterCase> doInBackground() throws Exception {
                return new BlotterCaseDAO().getAllBlotterCases();
            }


            @Override
            protected void done() {
                try {
                    List<BlotterCase> list = get();
                    tableModel.setRowCount(0);


                    resetStatistics();
                    LocalDate today = LocalDate.now();


                    for (BlotterCase c : list) {
                        String summonDate = "Not Set";
                        String hearingDate = "Not Set";




                        try {
                            java.lang.reflect.Method getSummonDateMethod = c.getClass().getMethod("getSummonDate");
                            Object summonDateObj = getSummonDateMethod.invoke(c);
                            if (summonDateObj != null) {
                                summonDate = summonDateObj.toString();
                            }
                        } catch (Exception e) {
                            // Method doesn't exist
                        }




                        try {
                            java.lang.reflect.Method getHearingDateMethod = c.getClass().getMethod("getHearingDate");
                            Object hearingDateObj = getHearingDateMethod.invoke(c);
                            if (hearingDateObj != null) {
                                hearingDate = hearingDateObj.toString();
                            }
                        } catch (Exception e) {
                            // Method doesn't exist
                        }




                        tableModel.addRow(new Object[]{
                                c.getCaseNumber(),
                                c.getDateRecorded() != null ? c.getDateRecorded().toString() : "N/A",
                                c.getComplainant(),
                                c.getRespondent(),
                                c.getIncidentType(),
                                c.getLocation(),
                                c.getStatus(),
                                summonDate,
                                hearingDate,
                                "👁️ Edit",  // View/Edit button
                                "🖨️"       // Print button
                        });
                        updateStatistics(c, today);


                    }
                    updatelastBlotterId();
                    // Update statistics display
                    updateAllStatistics();
                    refreshDashboard();


                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AdminBlotterTab.this,
                            "Error loading data: " + e.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    private void refreshDashboard() {
        // Force UI to repaint the dashboard area
        Container parent = this.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
    private void printStatistics() {
        System.out.println("=== STATISTICS ===");
        System.out.println("Total: " + totalCases);
        System.out.println("Resolved: " + resolvedCases);
        System.out.println("Pending: " + pendingCases);
        System.out.println("Summoned: " + summonedCases);
        System.out.println("Ongoing: " + ongoingCases);
        System.out.println("Today: " + todayCases);
        System.out.println("Month: " + monthCases);
        System.out.println("Day: " + dayCases);
        System.out.println("Overdue: " + overdueCases);
        System.out.println("==================");
    }
    private void refreshDashboardDisplay() {
        // Remove and re-add the dashboard panel
        JTabbedPane tabbedPane = findParentTabbedPane();
        if (tabbedPane != null && tabbedPane.getTabCount() > 1) {
            Component reportsTab = tabbedPane.getComponentAt(1); // Reports tab
            if (reportsTab instanceof JPanel) {
                JPanel reportsPanel = (JPanel) reportsTab;


                // Find and update the dashboard panel
                updateDashboardPanelInReportsPanel(reportsPanel);
            }
        }
    }
    // Add these fields to store dashboard labels
    private JLabel lblTotalCases, lblResolved, lblPending, lblSummoned, lblOngoing, lblToday, lblMonth, lblDay, lblOverdue;




    private void updateDashboardPanelInReportsPanel(JPanel reportsPanel) {
        // Find the dashboard scroll pane in the reports panel
        for (Component comp : reportsPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JPanel) {
                    JPanel containerPanel = (JPanel) view;
                    // Recreate dashboard panel
                    JPanel newDashboard = createDashboardPanel();
                    // Replace the old dashboard
                    scrollPane.setViewportView(newDashboard);
                    break;
                }
            }
        }
    }



    SystemLogDAO log = new SystemLogDAO();
    private void saveBlotter() {
        try {
            List<String> missingFields = new ArrayList<>();
            List<String> invalidFields = new ArrayList<>();


            // =================== VALIDATION ===================


            // Complainant validation
            if (txtComplainantName.getText().trim().isEmpty()) {
                missingFields.add("Complainant Name*");
            } else if (!isValidName(txtComplainantName.getText().trim())) {
                invalidFields.add("Complainant Name - Only letters, spaces, hyphens, apostrophes, periods allowed");
            }


            // Respondent validation
            if (txtRespondentName.getText().trim().isEmpty()) {
                missingFields.add("Respondent Name*");
            } else if (!isValidName(txtRespondentName.getText().trim())) {
                invalidFields.add("Respondent Name - Only letters, spaces, hyphens, apostrophes, periods allowed");
            }


            // Victim validation (optional)
            if (!txtVictim.getText().trim().isEmpty() && !isValidName(txtVictim.getText().trim())) {
                invalidFields.add("Victim Name - Only letters, spaces, hyphens, apostrophes, periods allowed");
            }


            // Incident Type validation
            if (cmbIncidentType.getSelectedIndex() == -1 ||
                    cmbIncidentType.getSelectedItem() == null ||
                    cmbIncidentType.getSelectedItem().toString().isEmpty()) {
                missingFields.add("Incident Type*");
            }


            // Incident Location validation
            if (txtIncidentPlace.getText().trim().isEmpty()) {
                missingFields.add("Incident Location*");
            } else if (!isValidLocation(txtIncidentPlace.getText().trim())) {
                invalidFields.add("Incident Location - Invalid characters (allows letters, numbers, spaces, ., , # ' ( ) -)");
            }


            // Incident Date validation
            try {
                LocalDate.parse(txtIncidentDate.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                invalidFields.add("Incident Date - Invalid date format (use yyyy-mm-dd)");
            }


            // Incident Time validation
            if (txtIncidentTime.getText().trim().isEmpty()) {
                missingFields.add("Incident Time*");
            } else if (!isValidTime(txtIncidentTime.getText().trim())) {
                invalidFields.add("Incident Time - Invalid time format (use HH:MM, 00:00-23:59)");
            }


            if (txtOfficer.getText().trim().isEmpty()) {
                missingFields.add("Officer Name*");
            } else if (!isValidOfficerName(txtOfficer.getText().trim())) {
                invalidFields.add("Officer Name - Only letters, spaces, hyphens, apostrophes allowed");
            }


            // Narrative validation
            String narrative = txtNarrative.getText().trim();
            if (!narrative.isEmpty() && narrative.length() > 1000) {
                invalidFields.add("Narrative - Too long (max 1000 characters)");
            }


            // Resolution validation
            String resolution = txtResolution.getText().trim();
            if (!resolution.isEmpty() && resolution.length() > 1000) {
                invalidFields.add("Resolution - Too long (max 1000 characters)");
            }


            // Witnesses validation
            String witnesses = txtWitnesses.getText().trim();
            if (!witnesses.isEmpty() && witnesses.length() > 500) {
                invalidFields.add("Witnesses - Too long (max 500 characters)");
            }


            // Summon Date validation
            try {
                LocalDate summonDate = LocalDate.parse(txtSummonDate.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (summonDate.isBefore(LocalDate.now())) {
                    invalidFields.add("Summon Date - Cannot be in the past");
                }
            } catch (Exception e) {
                invalidFields.add("Summon Date - Invalid date format (use yyyy-mm-dd)");
            }


            // =================== DUPLICATE PERSON VALIDATION ===================
            String complainant = txtComplainantName.getText().trim();
            String respondent = txtRespondentName.getText().trim();
            String victim = txtVictim.getText().trim();


            // Check if complainant and respondent are the same
            if (!complainant.isEmpty() && !respondent.isEmpty() &&
                    complainant.equalsIgnoreCase(respondent)) {
                JOptionPane.showMessageDialog(this,
                        "Complainant and Respondent cannot be the same person!\n" +
                                "• Complainant: " + complainant + "\n" +
                                "• Respondent: " + respondent + "\n\n" +
                                "Please enter different individuals for each role.",
                        "Duplicate Persons Detected",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }


            // Check if victim is the same as complainant or respondent (if victim is provided)
            if (!victim.isEmpty()) {
                if (!complainant.isEmpty() && victim.equalsIgnoreCase(complainant)) {
                    JOptionPane.showMessageDialog(this,
                            "Victim cannot be the same as the Complainant!\n" +
                                    "• Complainant/Victim: " + complainant + "\n\n" +
                                    "A person cannot be both the complainant and the victim.",
                            "Duplicate Persons Detected",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }


                if (!respondent.isEmpty() && victim.equalsIgnoreCase(respondent)) {
                    JOptionPane.showMessageDialog(this,
                            "Victim cannot be the same as the Respondent!\n" +
                                    "• Respondent/Victim: " + respondent + "\n\n" +
                                    "A person cannot be both the respondent and the victim.",
                            "Duplicate Persons Detected",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }


            // =================== SHOW VALIDATION ERRORS ===================


            // First show missing required fields
            if (!missingFields.isEmpty()) {
                showUnfilledFieldsDialog(missingFields);
                return;
            }


            // Then show invalid fields
            if (!invalidFields.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Please correct the following fields:\n\n");
                for (String field : invalidFields) {
                    errorMessage.append("• ").append(field).append("\n");
                }
                errorMessage.append("\nAllowed characters:\n");
                errorMessage.append("- Names: Letters, spaces, hyphens (-), apostrophes ('), periods (.)\n");
                errorMessage.append("- Location: Letters, numbers, spaces, ., , # ' ( ) -\n");
                errorMessage.append("- Time: HH:MM format (00:00-23:59)\n");


                JOptionPane.showMessageDialog(this,
                        errorMessage.toString(),
                        "Invalid Field Entries",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }


            // =================== HEARING DATE CONFLICT CHECK ===================
            LocalDate hearingDate = null;
            Object selectedItem = cmbHearingDate.getSelectedItem();
            String dateText = (selectedItem != null) ? selectedItem.toString().trim() : "";


            if (!dateText.isEmpty() && !dateText.equals("Select Date")) {
                try {
                    String cleanDateStr = dateText.split(" ")[0];
                    hearingDate = java.sql.Date.valueOf(cleanDateStr).toLocalDate();


                    if (!checkHearingConflict(hearingDate)) {
                        return;
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid Hearing Date Format: '" + dateText + "'\nPlease use yyyy-mm-dd (e.g., 2024-12-30).",
                            "Date Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }


            // =================== SAVE TO DATABASE ===================
            BlotterCaseDAO dao = new BlotterCaseDAO();
            String caseNumber = txtCaseNo.getText().trim();
            boolean isEditMode = !txtCaseId.getText().trim().isEmpty();


            if (!isEditMode && dao.isCaseNumberExists(caseNumber)) {
                JOptionPane.showMessageDialog(this,
                        "Case number already exists! Please use a different case number.",
                        "Duplicate Case", JOptionPane.ERROR_MESSAGE);
                txtCaseNo.setText(generateCaseNumber());
                return;
            }


            // Create BlotterCase object
            BlotterCase blotterCase = createBlotterCaseFromForm();
            blotterCase.setHearingDate(hearingDate);


            if (isEditMode) {
                updateBlotterCaseInDatabase(blotterCase);
                JOptionPane.showMessageDialog(this,
                        "Case updated successfully!\nCase Number: " + caseNumber,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                log.addLog("Added Case","Case number: " + blotterCase.getCaseNumber(),Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
            } else {
                dao.addBlotterCase(blotterCase);
                log.addLog("Updated Case","Case number: " + blotterCase.getCaseNumber(),Integer.parseInt(UserDataManager.getInstance().getCurrentStaff().getStaffId()));
                JOptionPane.showMessageDialog(this,
                        "Case saved successfully!\nCase Number: " + caseNumber,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }


            loadBlotterData();
            clearForm();


        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving case: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private boolean handleValidationErrors(List<String> missingFields, List<String> invalidFields) {
        // First show missing required fields
        if (!missingFields.isEmpty()) {
            showUnfilledFieldsDialog(missingFields);
            return false;
        }


        // Then show invalid fields
        if (!invalidFields.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Please correct the following fields:\n\n");
            for (String field : invalidFields) {
                errorMessage.append("• ").append(field).append("\n");
            }
            JOptionPane.showMessageDialog(this,
                    errorMessage.toString(),
                    "Invalid Field Entries",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }


        return true;
    }


    private boolean validateNoDuplicatePersons() {
        String complainant = txtComplainantName.getText().trim();
        String respondent = txtRespondentName.getText().trim();
        String victim = txtVictim.getText().trim();


        // Check if complainant and respondent are the same
        if (!complainant.isEmpty() && !respondent.isEmpty() &&
                complainant.equalsIgnoreCase(respondent)) {
            JOptionPane.showMessageDialog(this,
                    "Complainant and Respondent cannot be the same person!\n" +
                            "• Complainant: " + complainant + "\n" +
                            "• Respondent: " + respondent + "\n\n" +
                            "Please enter different individuals for each role.",
                    "Duplicate Persons Detected",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }


        // Check if victim is the same as complainant or respondent (if victim is provided)
        if (!victim.isEmpty()) {
            if (!complainant.isEmpty() && victim.equalsIgnoreCase(complainant)) {
                JOptionPane.showMessageDialog(this,
                        "Victim cannot be the same as the Complainant!\n" +
                                "• Complainant/Victim: " + complainant + "\n\n" +
                                "A person cannot be both the complainant and the victim.",
                        "Duplicate Persons Detected",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }


            if (!respondent.isEmpty() && victim.equalsIgnoreCase(respondent)) {
                JOptionPane.showMessageDialog(this,
                        "Victim cannot be the same as the Respondent!\n" +
                                "• Respondent/Victim: " + respondent + "\n\n" +
                                "A person cannot be both the respondent and the victim.",
                        "Duplicate Persons Detected",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }


        return true;
    }


    private LocalDate parseHearingDate() {
        Object selectedItem = cmbHearingDate.getSelectedItem();
        String dateText = (selectedItem != null) ? selectedItem.toString().trim() : "";


        if (!dateText.isEmpty() && !dateText.equals("Select Date")) {
            try {
                String cleanDateStr = dateText.split(" ")[0];
                return java.sql.Date.valueOf(cleanDateStr).toLocalDate();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Hearing Date Format: '" + dateText + "'\nPlease use yyyy-mm-dd (e.g., 2024-12-30).",
                        "Date Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }



    private boolean updateBlotterCaseInDatabase(BlotterCase blotterCase) {
        try {
            BlotterCaseDAO dao = new BlotterCaseDAO();


            // First, check i
            // First, check if the case exists
            BlotterCase existingCase = dao.findCaseByNumber(blotterCase.getCaseNumber());
            if (existingCase == null) {
                JOptionPane.showMessageDialog(this,
                        "Case not found in database: " + blotterCase.getCaseNumber(),
                        "Update Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }


            // Use the existing case ID to ensure we're updating the right record
            Integer existingCaseId = existingCase.getCaseId();
            if (existingCaseId == null) {
                JOptionPane.showMessageDialog(this,
                        "Cannot update case: No case ID found",
                        "Update Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }


            // Create a builder starting from the existing case
            BlotterCase.BlotterCaseBuilder builder = BlotterCase.builder()
                    .caseId(existingCaseId) // Use the existing ID
                    .caseNumber(blotterCase.getCaseNumber())
                    .dateRecorded(existingCase.getDateRecorded()) // Preserve original date
                    .timeRecorded(existingCase.getTimeRecorded()) // Preserve original time
                    .complainant(blotterCase.getComplainant())
                    .respondent(blotterCase.getRespondent())
                    .victim(blotterCase.getVictim())
                    .incidentType(blotterCase.getIncidentType())
                    .location(blotterCase.getLocation())
                    .narrative(blotterCase.getNarrative())
                    .witnesses(blotterCase.getWitnesses())
                    .status(existingCase.getStatus()) // Preserve existing status
                    .officerInCharge(blotterCase.getOfficerInCharge())
                    .resolution(blotterCase.getResolution());


            // Try to copy hearing date from the updated case
            try {
                java.lang.reflect.Method getHearingDateMethod = blotterCase.getClass()
                        .getMethod("getHearingDate");
                Object hearingDate = getHearingDateMethod.invoke(blotterCase);
                if (hearingDate != null) {
                    java.lang.reflect.Method hearingDateMethod = builder.getClass()
                            .getMethod("hearingDate", LocalDate.class);
                    hearingDateMethod.invoke(builder, hearingDate);
                }
            } catch (Exception e) {
                // Method doesn't exist, ignore
            }


            // Try to copy summon date from the updated case
            try {
                java.lang.reflect.Method getSummonDateMethod = blotterCase.getClass()
                        .getMethod("getSummonDate");
                Object summonDate = getSummonDateMethod.invoke(blotterCase);
                if (summonDate != null) {
                    java.lang.reflect.Method summonDateMethod = builder.getClass()
                            .getMethod("summonDate", LocalDate.class);
                    summonDateMethod.invoke(builder, summonDate);
                }
            } catch (Exception e) {
                // Method doesn't exist, ignore
            }


            // Build the updated case
            BlotterCase updatedCase = builder.build();


            // Update in database
            boolean success = dao.updateBlotterCase(updatedCase);


            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Case updated successfully!\nCase Number: " + blotterCase.getCaseNumber(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update case in database!",
                        "Update Error", JOptionPane.ERROR_MESSAGE);
            }


            return success;


        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error updating case: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private boolean hasDuplicatePersons() {
        String complainant = txtComplainantName.getText().trim().toLowerCase();
        String respondent = txtRespondentName.getText().trim().toLowerCase();
        String victim = txtVictim.getText().trim().toLowerCase();


        if (complainant.isEmpty() || respondent.isEmpty()) {
            return false; // Can't check duplicates if required fields are empty
        }


        // Check all possible duplicate combinations
        boolean complainantRespondentSame = complainant.equals(respondent);
        boolean complainantVictimSame = !victim.isEmpty() && complainant.equals(victim);
        boolean respondentVictimSame = !victim.isEmpty() && respondent.equals(victim);


        if (complainantRespondentSame || complainantVictimSame || respondentVictimSame) {
            StringBuilder errorMsg = new StringBuilder("Duplicate persons found:\n\n");


            if (complainantRespondentSame) {
                errorMsg.append("• Complainant and Respondent are the same: ")
                        .append(txtComplainantName.getText()).append("\n");
            }
            if (complainantVictimSame) {
                errorMsg.append("• Complainant and Victim are the same: ")
                        .append(txtComplainantName.getText()).append("\n");
            }
            if (respondentVictimSame) {
                errorMsg.append("• Respondent and Victim are the same: ")
                        .append(txtRespondentName.getText()).append("\n");
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


    private boolean checkHearingConflict(LocalDate hearingDate) {
        // 1. Get schedules for that specific date
        CaptainScheduleDAO dao = new CaptainScheduleDAO();
        List<CaptainSchedule> schedules = dao.getSchedulesByDate(hearingDate);


        if (schedules.isEmpty()) {
            return true; // No schedules at all, safe to proceed
        }


        // 2. If we found ANY schedule, warn the user
        for (CaptainSchedule sched : schedules) {
            // If the schedule is marked as NOT available, or if you want to warn for any event
            if (!sched.isAvailable()) {
                String timeRange = sched.getStartTime() + " - " + sched.getEndTime();


                int choice = JOptionPane.showConfirmDialog(this,
                        "ALERT: The Captain already has a schedule on " + hearingDate + ".\n" +
                                "Time: " + timeRange + "\n" +
                                "Status: Busy/Booked\n\n" +
                                "Do you want to double-book this slot?",
                        "Schedule Conflict Found",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);


                if (choice == JOptionPane.NO_OPTION) {
                    return false; // User clicked NO -> Stop saving
                }
            }
        }
        return true; // User clicked YES (or no blocking conflicts), proceed
    }


    private BlotterCase createBlotterCaseFromForm() {
        try {
            // Get basic information
            LocalDate dateRecorded = LocalDate.now();
            LocalTime timeRecorded = LocalTime.now();


            // Get case ID from hidden field
            Integer caseId = null;
            String caseIdText = txtCaseId.getText().trim();


            if (!caseIdText.isEmpty()) {
                try {
                    caseId = Integer.parseInt(caseIdText);
                } catch (NumberFormatException e) {
                    caseId = null; // New case, will get auto-generated ID
                }
            }


            // Parse incident date
            LocalDate incidentDate;
            try {
                incidentDate = LocalDate.parse(txtIncidentDate.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                incidentDate = LocalDate.now();
            }


            // Parse hearing date
            LocalDate hearingDate = null;
            Object selectedHearingItem = cmbHearingDate.getSelectedItem();
            if (selectedHearingItem != null) {
                String hearingDateStr = selectedHearingItem.toString().trim();
                if (!hearingDateStr.isEmpty() && !hearingDateStr.equals("Select Date")) {
                    try {
                        // Extract just the date part (before any space)
                        String cleanDateStr = hearingDateStr.split(" ")[0];
                        hearingDate = LocalDate.parse(cleanDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    } catch (Exception e) {
                        // If parsing fails, keep as null
                    }
                }
            }


            // Parse summon date
            LocalDate summonDate;
            try {
                summonDate = LocalDate.parse(txtSummonDate.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                summonDate = LocalDate.now().plusDays(3);
            }


            // Build the BlotterCase object
            // Use the builder pattern safely with null checks
            BlotterCase.BlotterCaseBuilder builder = BlotterCase.builder()
                    .caseId(caseId)
                    .caseNumber(txtCaseNo.getText().trim())
                    .dateRecorded(dateRecorded)
                    .timeRecorded(timeRecorded)
                    .complainant(txtComplainantName.getText().trim())
                    .respondent(txtRespondentName.getText().trim())
                    .victim(txtVictim.getText().trim())
                    .incidentType(cmbIncidentType.getSelectedItem() != null ?
                            cmbIncidentType.getSelectedItem().toString() : "")
                    .location(txtIncidentPlace.getText().trim())
                    .narrative(txtNarrative.getText().trim())
                    .witnesses(txtWitnesses.getText().trim())
                    .status("Pending")
                    .officerInCharge(txtOfficer.getText().trim())
                    .resolution(txtResolution.getText().trim());


            // Add optional incident date if the method exists
            try {
                // Try to set incident date using reflection
                java.lang.reflect.Method incidentDateMethod = builder.getClass()
                        .getMethod("incidentDate", LocalDate.class);
                incidentDateMethod.invoke(builder, incidentDate);
            } catch (Exception e) {
                // Method doesn't exist, ignore
            }


            // Add incident time if the method exists
            try {
                java.lang.reflect.Method incidentTimeMethod = builder.getClass()
                        .getMethod("incidentTime", String.class);
                incidentTimeMethod.invoke(builder, txtIncidentTime.getText().trim());
            } catch (Exception e) {
                // Method doesn't exist, ignore
            }


            // Add hearing date if the method exists
            if (hearingDate != null) {
                try {
                    java.lang.reflect.Method hearingDateMethod = builder.getClass()
                            .getMethod("hearingDate", LocalDate.class);
                    hearingDateMethod.invoke(builder, hearingDate);
                } catch (Exception e) {
                    // Method doesn't exist, ignore
                }
            }


            // Add summon date if the method exists
            try {
                java.lang.reflect.Method summonDateMethod = builder.getClass()
                        .getMethod("summonDate", LocalDate.class);
                summonDateMethod.invoke(builder, summonDate);
            } catch (Exception e) {
                // Method doesn't exist, ignore
            }


            // Build and return the object
            return builder.build();


        } catch (Exception e) {
            e.printStackTrace();
            // Return a minimal valid BlotterCase to avoid NullPointerException
            return BlotterCase.builder()
                    .caseNumber(txtCaseNo.getText().trim())
                    .dateRecorded(LocalDate.now())
                    .timeRecorded(LocalTime.now())
                    .complainant(txtComplainantName.getText().trim())
                    .respondent(txtRespondentName.getText().trim())
                    .status("Pending")
                    .build();
        }
    }






    private String getCurrentCaseStatus(String caseNumber) {
        try {
            BlotterCaseDAO dao = new BlotterCaseDAO();
            BlotterCase existingCase = dao.findCaseByNumber(caseNumber);


            return existingCase != null ? existingCase.getStatus() : "Pending";
        } catch (Exception e) {
            return "Pending";
        }
    }
    private void clearForm() {
        // Check if any required fields are filled
        boolean hasData = isFormPartiallyFilled();


        if (hasData) {
            // Ask if user wants to save before clearing
            int choice = JOptionPane.showOptionDialog(this,
                    "You have unsaved data in the form!\n\n" +
                            "Do you want to:\n" +
                            "1. Save the data first\n" +
                            "2. Clear without saving\n" +
                            "3. Cancel and continue editing",
                    "Unsaved Changes Detected",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{"Save First", "Clear Anyway", "Cancel"},
                    "Save First");


            switch (choice) {
                case 0: // Save First
                    saveBlotter();
                    if (!isFormPartiallyFilled()) { // If save was successful and cleared
                        resetFormToDefaults();
                    }
                    break;


                case 1: // Clear Anyway
                    showConfirmationAndClear();
                    break;


                case 2: // Cancel
                default:
                    return; // Do nothing, keep the data
            }
        } else {
            // No data, just clear with confirmation
            showConfirmationAndClear();
        }
    }


    // Helper method to check if form has any data
    private boolean isFormPartiallyFilled() {
        // Check required fields
        if (!txtComplainantName.getText().trim().isEmpty() ||
                !txtRespondentName.getText().trim().isEmpty() ||
                !txtVictim.getText().trim().isEmpty() ||
                !txtIncidentPlace.getText().trim().isEmpty() ||
                !txtNarrative.getText().trim().isEmpty() ||
                !txtResolution.getText().trim().isEmpty() ||
                !txtWitnesses.getText().trim().isEmpty() ||
                !txtOfficer.getText().trim().isEmpty() ||
                (cmbIncidentType.getSelectedItem() != null &&
                        !cmbIncidentType.getSelectedItem().toString().isEmpty() &&
                        !cmbIncidentType.getSelectedItem().toString().equals("Select"))) {
            return true;
        }


        // Check if time is not default
        if (!txtIncidentTime.getText().trim().equals("12:00") &&
                !txtIncidentTime.getText().trim().isEmpty()) {
            return true;
        }


        // Check if date is not today
        try {
            LocalDate today = LocalDate.now();
            LocalDate incidentDate = LocalDate.parse(txtIncidentDate.getText(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!incidentDate.equals(today)) {
                return true;
            }
        } catch (Exception e) {
            // Ignore date parsing errors
        }


        return false;
    }


    // Show confirmation before clearing
    private void showConfirmationAndClear() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear the form?\n" +
                        "All entered data will be lost.",
                "Confirm Clear Form",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);


        if (confirm == JOptionPane.YES_OPTION) {
            resetFormToDefaults();
            JOptionPane.showMessageDialog(this,
                    "Form cleared successfully!",
                    "Form Cleared",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // Method to reset form to default values
    private void resetFormToDefaults() {
        // Clear all text fields
        txtCaseId.setText(""); // Clear hidden ID


        // Clear name fields
        txtComplainantName.setText("");
        txtRespondentName.setText("");
        txtVictim.setText("");


        // Reset incident details
        txtIncidentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtIncidentTime.setText("12:00");
        txtIncidentPlace.setText("");


        // Reset text areas
        txtNarrative.setText("");
        txtWitnesses.setText("");
        txtResolution.setText("");
        txtOfficer.setText("");


        // Reset dates
        txtSummonDate.setText(LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));


        // Reset comboboxes
        if (cmbIncidentType.getItemCount() > 0) {
            cmbIncidentType.setSelectedIndex(0);
        }


        // Reset hearing date
        String[] dates = getNext60Days();
        cmbHearingDate.setModel(new DefaultComboBoxModel<>(dates));
        if (dates.length > 0) {
            cmbHearingDate.setSelectedIndex(0);
        }


        // Generate new case number
        txtCaseNo.setText(generateCaseNumber());
        txtDateRecorded.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        txtTimeRecorded.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));


        // Reset validation colors
        resetValidationColors();
    }


    // Reset all field backgrounds to default
    private void resetValidationColors() {
        txtComplainantName.setBackground(Color.WHITE);
        txtComplainantName.setToolTipText(null);


        txtRespondentName.setBackground(Color.WHITE);
        txtRespondentName.setToolTipText(null);


        txtVictim.setBackground(Color.WHITE);
        txtVictim.setToolTipText(null);


        txtIncidentPlace.setBackground(Color.WHITE);
        txtIncidentPlace.setToolTipText(null);


        txtIncidentTime.setBackground(Color.WHITE);
        txtIncidentTime.setToolTipText("Format: HH:MM (24-hour) e.g., 14:30");


        txtNarrative.setBackground(Color.WHITE);
        txtNarrative.setToolTipText(null);


        txtResolution.setBackground(Color.WHITE);
        txtResolution.setToolTipText(null);


        txtWitnesses.setBackground(Color.WHITE);
        txtWitnesses.setToolTipText(null);


        txtOfficer.setBackground(Color.WHITE);
        txtOfficer.setToolTipText(null);
    }


    private void printBlotter() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Barangay Blotter - " + txtCaseNo.getText());
        job.setPrintable(this);




        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this,
                        "Blotter printed successfully!",
                        "Print Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error printing: " + ex.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void exportAllToPDF() {
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
                Paragraph title = new Paragraph("Admin Blotter Report", titleFont);
                title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                doc.add(title);

                // 3. Create Table
                int colCount = blotterTable.getColumnCount();
                PdfPTable pdfTable = new PdfPTable(colCount);
                pdfTable.setWidthPercentage(100);

                // 4. Add Headers (✅ MATCHING YOUR TABLE COLORS)
                // We use the same color: new Color(52, 152, 219)
                java.awt.Color headerColor = new java.awt.Color(52, 152, 219);

                com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD, java.awt.Color.BLACK);

                for (int i = 0; i < colCount; i++) {
                    PdfPCell cell = new PdfPCell(new Paragraph(blotterTable.getColumnName(i), headerFont));
                    cell.setBackgroundColor(headerColor); // ✅ Blue Background
                    cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.lowagie.text.Element.ALIGN_MIDDLE);
                    cell.setPadding(8); // More padding like your table
                    pdfTable.addCell(cell);
                }

                // 5. Add Rows
                com.lowagie.text.Font rowFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);

                for (int i = 0; i < blotterTable.getRowCount(); i++) {
                    for (int j = 0; j < colCount; j++) {
                        Object val = blotterTable.getValueAt(i, j);
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



    private void exportToPDF() {
        JOptionPane.showMessageDialog(this,
                "PDF export feature would be implemented here.\n" +
                        "Would generate a formal Barangay Blotter document.",
                "Export PDF", JOptionPane.INFORMATION_MESSAGE);
    }




    private void showBlotterHistory() {
        JOptionPane.showMessageDialog(this,
                "Blotter History feature would be implemented here.",
                "Feature", JOptionPane.INFORMATION_MESSAGE);
    }




    private void showHearingCalendar() {
        JOptionPane.showMessageDialog(this,
                "Hearing Calendar feature would be implemented here.",
                "Feature", JOptionPane.INFORMATION_MESSAGE);
    }




    private void showCaseAnalytics() {
        JOptionPane.showMessageDialog(this,
                "Case Analytics feature would be implemented here.",
                "Feature", JOptionPane.INFORMATION_MESSAGE);
    }




    private void exportData() {
        JOptionPane.showMessageDialog(this,
                "Data export feature would be implemented here.",
                "Export Data", JOptionPane.INFORMATION_MESSAGE);
    }
    private void viewEditCase(int viewRow) {
        try {
            if (viewRow < 0 || viewRow >= blotterTable.getRowCount()) {
                return;
            }


            int modelRow = blotterTable.convertRowIndexToModel(viewRow);
            String caseNo = (String) tableModel.getValueAt(modelRow, 0);


            if (caseNo == null || caseNo.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "No case number found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            // Generate preview text for this case
            String previewText = generatePrintPreviewTextFromDB(viewRow);


            // You could display this preview in a dialog or use it somewhere
            // For example, show it in the print preview tab:
            updatePrintPreviewTab(previewText);


            // Show loading message
            JOptionPane.showMessageDialog(this,
                    "Loading case: " + caseNo + "\nPlease wait...",
                    "Loading", JOptionPane.INFORMATION_MESSAGE);


            // Load case data
            BlotterCaseDAO dao = new BlotterCaseDAO();
            BlotterCase caseData = dao.findCaseByNumber(caseNo);


            if (caseData == null) {
                JOptionPane.showMessageDialog(this,
                        "Case not found in database: " + caseNo,
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            // Populate form fields (simplified version - you'll need to expand this)
            txtCaseId.setText(String.valueOf(caseData.getCaseId()));
            txtCaseNo.setText(caseData.getCaseNumber());
            txtComplainantName.setText(caseData.getComplainant() != null ? caseData.getComplainant() : "");
            txtRespondentName.setText(caseData.getRespondent() != null ? caseData.getRespondent() : "");
            txtVictim.setText(caseData.getVictim() != null ? caseData.getVictim() : "");
            txtIncidentPlace.setText(caseData.getLocation() != null ? caseData.getLocation() : "");


            // Set incident type
            if (caseData.getIncidentType() != null) {
                cmbIncidentType.setSelectedItem(caseData.getIncidentType());
            }


            // Set narrative and resolution
            txtNarrative.setText(caseData.getNarrative() != null ? caseData.getNarrative() : "");
            txtResolution.setText(caseData.getResolution() != null ? caseData.getResolution() : "");
            txtWitnesses.setText(caseData.getWitnesses() != null ? caseData.getWitnesses() : "");
            txtOfficer.setText(caseData.getOfficerInCharge() != null ? caseData.getOfficerInCharge() : "");


            // Switch to the form tab
            switchToFormTab();


            // Show success message
            JOptionPane.showMessageDialog(this,
                    "Now editing Case: " + caseNo + "\nMake changes and click Save to update.",
                    "Edit Mode", JOptionPane.INFORMATION_MESSAGE);


        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading case: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void switchToFormTab() {
        System.out.println("DEBUG: Attempting to switch to form tab...");


        // Method 1: Direct parent search
        Container parent = this;
        int levels = 0;
        while (parent != null && !(parent instanceof JTabbedPane)) {
            System.out.println("DEBUG: Level " + levels + ": " + parent.getClass().getSimpleName());
            parent = parent.getParent();
            levels++;
        }


        if (parent instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) parent;
            System.out.println("DEBUG: Found JTabbedPane with " + tabbedPane.getTabCount() + " tabs");
            tabbedPane.setSelectedIndex(0);
            System.out.println("DEBUG: Switched to tab index 0");
        } else {
            System.out.println("DEBUG: Could not find JTabbedPane via parent search");


            // Method 2: Window search
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                System.out.println("DEBUG: Found window: " + window.getClass().getSimpleName());
                searchForTabbedPane(window);
            }
        }
    }


    private void searchForTabbedPane(Window window) {
        if (window instanceof JFrame) {
            searchForTabbedPaneInComponent(((JFrame) window).getContentPane());
        } else if (window instanceof JDialog) {
            searchForTabbedPaneInComponent(((JDialog) window).getContentPane());
        }
    }


    private void searchForTabbedPaneInComponent(Component comp) {
        if (comp instanceof JTabbedPane) {
            System.out.println("DEBUG: Found JTabbedPane via window search");
            ((JTabbedPane) comp).setSelectedIndex(0);
        } else if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                searchForTabbedPaneInComponent(child);
            }
        }
    }
    private void updatePrintPreviewTab(String previewText) {
        // Find the print preview tab and update it
        JTabbedPane tabbedPane = findParentTabbedPane();
        if (tabbedPane != null && tabbedPane.getTabCount() > 2) {
            Component printTab = tabbedPane.getComponentAt(2); // Print Preview tab (index 2)
            if (printTab instanceof JPanel) {
                // Find the JTextArea in the print panel and update it
                updateTextAreaInPrintPanel((JPanel) printTab, previewText);
            }
        }
    }


    private void updateTextAreaInPrintPanel(JPanel printPanel, String text) {
        // Search for the JTextArea in the print panel
        for (Component comp : printPanel.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTextArea) {
                    ((JTextArea) view).setText(text);
                    break;
                }
            }
        }
    }




    private void searchForTabbedPane(JFrame frame) {
        // Search recursively for JTabbedPane
        Component[] components = frame.getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JTabbedPane) {
                ((JTabbedPane) comp).setSelectedIndex(0);
                break;
            } else if (comp instanceof Container) {
                searchForTabbedPaneInContainer((Container) comp);
            }
        }
    }


    private void searchForTabbedPaneInContainer(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTabbedPane) {
                ((JTabbedPane) comp).setSelectedIndex(0);
                return;
            } else if (comp instanceof Container) {
                searchForTabbedPaneInContainer((Container) comp);
            }
        }
    }
    private String generatePrintPreviewTextFromDB(int row) {
        StringBuilder sb = new StringBuilder();


        try {
            // Get case data from database based on row
            int modelRow = blotterTable.convertRowIndexToModel(row);
            String caseNo = (String) tableModel.getValueAt(modelRow, 0);


            BlotterCaseDAO dao = new BlotterCaseDAO();
            BlotterCase caseData = dao.findCaseByNumber(caseNo);


            if (caseData == null) {
                return "Error: Case not found in database - " + caseNo;
            }


            // Format dates
            String dateRecorded = caseData.getDateRecorded() != null ?
                    caseData.getDateRecorded().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "N/A";


            String timeRecorded = caseData.getTimeRecorded() != null ?
                    caseData.getTimeRecorded().format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A";


            String incidentDate = "N/A";
            try {
                if (caseData.getDateRecorded() != null) {
                    incidentDate = caseData.getDateRecorded().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception e) {
                // Use default
            }


            String hearingDate = "Not Set";
            try {
                if (caseData.getHearingDate() != null) {
                    hearingDate = caseData.getHearingDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception e) {
                // Use default
            }


            // Official Header
            sb.append("╔══════════════════════════════════════════════════════════════════════════════╗\n");
            sb.append("║                         REPUBLIC OF THE PHILIPPINES                          ║\n");
            sb.append("║                       PROVINCE OF CAMARINES NORTE                            ║\n");
            sb.append("║                          CITY OF DAET                                        ║\n");
            sb.append("║                       BARANGAY ALAWIHAO                                      ║\n");
            sb.append("║                  OFFICE OF THE PUNONG BARANGAY                               ║\n");
            sb.append("╠══════════════════════════════════════════════════════════════════════════════╣\n");
            sb.append("║                          OFFICIAL BLOTTER FORM                               ║\n");
            sb.append("╚══════════════════════════════════════════════════════════════════════════════╝\n\n");


            // Case Information
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                          CASE INFORMATION\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append(String.format("Case No:           %-70s\n", caseData.getCaseNumber()));
            sb.append(String.format("Date Recorded:     %-60s\n", dateRecorded));
            sb.append(String.format("Time Recorded:     %-60s\n", timeRecorded));
            sb.append("\n");


            // Parties Involved
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                          PARTIES INVOLVED\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append(String.format("COMPLAINANT:       %-60s\n",
                    caseData.getComplainant() != null ? caseData.getComplainant() : "[Not specified]"));
            sb.append(String.format("RESPONDENT:        %-60s\n",
                    caseData.getRespondent() != null ? caseData.getRespondent() : "[Not specified]"));
            sb.append(String.format("VICTIM:            %-60s\n",
                    caseData.getVictim() != null ? caseData.getVictim() : "[Not specified]"));
            sb.append("\n");


            // Incident Details
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                          INCIDENT DETAILS\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append(String.format("Incident Type:     %-60s\n",
                    caseData.getIncidentType() != null ? caseData.getIncidentType() : "[Not specified]"));
            sb.append(String.format("Date:              %-60s\n", incidentDate));
            sb.append(String.format("Time:              %-60s\n",
                    caseData.getTimeRecorded() != null ?
                            caseData.getTimeRecorded().format(DateTimeFormatter.ofPattern("hh:mm a")) : "[Not specified]"));
            sb.append(String.format("Location:          %-60s\n",
                    caseData.getLocation() != null ? caseData.getLocation() : "[Not specified]"));
            sb.append("\n");


            // Narrative
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                          NARRATIVE OF EVENTS\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            String narrative = caseData.getNarrative();
            if (narrative == null || narrative.isEmpty()) narrative = "[Narrative not provided]";
            sb.append(wrapText(narrative, 70)).append("\n\n");


            // Resolution
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                          RESOLUTION\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            String resolution = caseData.getResolution();
            if (resolution == null || resolution.isEmpty()) resolution = "[Resolution not provided]";
            sb.append(wrapText(resolution, 70)).append("\n\n");


            // Witnesses
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                          WITNESSES\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            String witnesses = caseData.getWitnesses();
            if (witnesses == null || witnesses.isEmpty()) witnesses = "[No witnesses listed]";
            sb.append(wrapText(witnesses, 70)).append("\n\n");


            // Officer and Dates
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                          OFFICIAL DETAILS\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append(String.format("Officer:           %-60s\n",
                    caseData.getOfficerInCharge() != null ? caseData.getOfficerInCharge() : "[Not assigned]"));
            sb.append(String.format("Hearing Date:      %-60s\n", hearingDate));
            sb.append("\n");


            // Signatures
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("                          SIGNATURES\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("\n");
            sb.append("Complainant: ___________________________________ Date: _______________\n");
            sb.append("\n");
            sb.append("Respondent:  ___________________________________ Date: _______________\n");
            sb.append("\n");
            sb.append("Witness:     ___________________________________ Date: _______________\n");
            sb.append("\n");
            sb.append("Officer:     ___________________________________ Date: _______________\n");
            sb.append("\n");


            // Footer
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("NOTE: This is an official document of Barangay Central. Any falsification of\n");
            sb.append("information is punishable under the Revised Penal Code of the Philippines.\n");
            sb.append("════════════════════════════════════════════════════════════════════════════════\n");
            sb.append("Generated on: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            sb.append(" at ").append(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))).append("\n");


        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating preview: " + e.getMessage();
        }


        return sb.toString();
    }
    private void printCase(int row) {
        int modelRow = blotterTable.convertRowIndexToModel(row);
        String caseNo = (String) tableModel.getValueAt(modelRow, 0);


        try {
            BlotterCaseDAO dao = new BlotterCaseDAO();
            BlotterCase caseData = dao.findCaseByNumber(caseNo);


            if (caseData == null) {
                JOptionPane.showMessageDialog(this, "Case not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }




            String previewText = generatePrintPreviewTextFromDB(row);
            System.out.println("Preview text generated for case: " + caseNo);
            // You could show this in a dialog or use it elsewhere


            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Blotter Case: " + caseNo);


            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }


                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    g2d.setFont(new Font("Arial", Font.PLAIN, 10));


                    int y = 50;
                    int margin = 50;


                    g2d.drawString("BARANGAY BLOTTER CASE REPORT", margin, y);
                    y += 30;
                    g2d.drawString("Case Number: " + caseData.getCaseNumber(), margin, y);
                    y += 20;
                    g2d.drawString("Date: " + (caseData.getDateRecorded() != null ?
                            caseData.getDateRecorded().toString() : "N/A"), margin, y);
                    y += 20;
                    g2d.drawString("Complainant: " + caseData.getComplainant(), margin, y);
                    y += 20;
                    g2d.drawString("Respondent: " + caseData.getRespondent(), margin, y);
                    y += 20;
                    g2d.drawString("Victim: " + caseData.getVictim(), margin, y);
                    y += 20;
                    g2d.drawString("Incident Type: " + caseData.getIncidentType(), margin, y);
                    y += 20;
                    g2d.drawString("Location: " + caseData.getLocation(), margin, y);
                    y += 20;
                    g2d.drawString("Status: " + caseData.getStatus(), margin, y);
                    y += 20;
                    if (caseData.getHearingDate() != null) {
                        g2d.drawString("Hearing Date: " + caseData.getHearingDate().toString(), margin, y);
                        y += 20;
                    }
                    y += 40;
                    g2d.drawString("Printed on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), margin, y);


                    return PAGE_EXISTS;
                }
            });


            if (job.printDialog()) {
                try {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Case printed successfully!", "Print Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (PrinterException ex) {
                    JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
                }
            }


        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error retrieving case data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void searchBlotterCases() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadBlotterData(); // Load all if search is empty
            return;
        }


        SwingWorker<List<BlotterCase>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BlotterCase> doInBackground() throws Exception {
                BlotterCaseDAO dao = new BlotterCaseDAO();
                return dao.searchBlotterCases(searchText);
            }


            @Override
            protected void done() {
                try {
                    List<BlotterCase> list = get();
                    tableModel.setRowCount(0);


                    resetStatistics();
                    LocalDate today = LocalDate.now();


                    for (BlotterCase c : list) {
                        // ... same population logic as in loadBlotterData() ...
                        tableModel.addRow(new Object[]{
                                c.getCaseNumber(),
                                c.getDateRecorded() != null ? c.getDateRecorded().toString() : "N/A",
                                c.getComplainant(),
                                c.getRespondent(),
                                c.getIncidentType(),
                                c.getLocation(),
                                c.getStatus(),
                                c.getHearingDate() != null ? c.getHearingDate().toString() : "Not Set",
                                c.getHearingDate() != null ? c.getHearingDate().toString() : "Not Set",
                                "👁️ Edit",
                                "🖨️"
                        });


                        updateStatistics(c, today);
                    }


                    JOptionPane.showMessageDialog(AdminBlotterTab.this,
                            "Found " + list.size() + " case(s) matching: " + searchText,
                            "Search Results", JOptionPane.INFORMATION_MESSAGE);


                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(AdminBlotterTab.this,
                            "Error searching: " + e.getMessage(),
                            "Search Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };


        worker.execute();
    }


    // =================== UI HELPER METHODS ===================




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




    private JTextArea createStyledTextArea(int rows) {
        JTextArea area = new JTextArea(rows, 20);
        area.setFont(FIELD_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        area.setBackground(Color.WHITE);
        return area;
    }




    private JButton createModernButton(String text, Color bgColor, String icon) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.black);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));




        // Hover effect
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




    private JButton createModernPickerButton() {
        JButton button = new JButton("👤 Pick");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }




    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(FIELD_FONT);
        comboBox.setBackground(Color.WHITE);  // Explicitly set to white
        comboBox.setForeground(Color.BLACK);  // Ensure text is readable
        comboBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }




    private JPanel createLabeledField(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(SECTION_BG);




        JLabel lbl = new JLabel(label);
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(new Color(60, 60, 60));




        panel.add(lbl, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);




        return panel;
    }










    private String[] getNext60Days() {
        String[] dates = new String[61];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Calendar cal = java.util.Calendar.getInstance();




        dates[0] = "Select Date";
        for (int i = 1; i <= 60; i++) {
            dates[i] = sdf.format(cal.getTime());
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }




    private String generateCaseNumber() {
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        int randomNum = 1000 + (int)(Math.random() * 9000);
        return "BL-" + yearMonth + "-" + randomNum;
    }




    private void initializeData() {
        txtCaseId = new JTextField();
        txtCaseId.setVisible(false); // Hidden field


        txtIncidentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtSummonDate.setText(LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        txtCaseNo.setText(generateCaseNumber());
        txtDateRecorded.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        txtTimeRecorded.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));


        btnPickComplainant.addActionListener(e -> showResidentPicker(txtComplainantName));
        btnPickRespondent.addActionListener(e -> showResidentPicker(txtRespondentName));
        btnPickVictim.addActionListener(e -> showResidentPicker(txtVictim));


        // Add input filtering to block special characters
        addInputFiltering();


        // Add validation listeners
        addFieldValidationListeners();


        // Set default time
        txtIncidentTime.setText("12:00");


        // Add number blocking to name fields
        addNumberDetectionToNameFields();
    }




    private void showDatePicker(JTextField targetField) {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);




        try {
            if (!targetField.getText().isEmpty()) {
                Date current = new SimpleDateFormat("yyyy-MM-dd").parse(targetField.getText());
                dateSpinner.setValue(current);
            }
        } catch (Exception ignored) {}




        int option = JOptionPane.showOptionDialog(
                this, dateSpinner, "Select Date",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, null, null
        );




        if (option == JOptionPane.OK_OPTION) {
            Date date = (Date) dateSpinner.getValue();
            targetField.setText(new SimpleDateFormat("yyyy-MM-dd").format(date));
        }
    }




    private void showResidentPicker(JTextField targetField) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Resident", true);
        dialog.setSize(600, 450);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);




        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        searchPanel.add(new JLabel("Find Name:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);




        String[] cols = {"ID", "Last Name", "First Name", "Middle", "Purok"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(25);




        List<Resident> list = new ResidentDAO().getAllResidents();
        for (Resident r : list) {
            m.addRow(new Object[]{r.getResidentId(), r.getLastName(), r.getFirstName(), r.getMiddleName(), r.getPurok()});
        }




        JButton btnSelect = new JButton("Select");
        btnSelect.addActionListener(e -> {
            int row = t.getSelectedRow();
            if (row >= 0) {
                String lastName = m.getValueAt(row, 1).toString();
                String firstName = m.getValueAt(row, 2).toString();
                String middleName = m.getValueAt(row, 3).toString();
                String fullName = firstName + " " + (middleName != null && !middleName.equals("null") ? middleName + " " : "") + lastName;
                targetField.setText(fullName);
                dialog.dispose();
            }
        });




        dialog.add(searchPanel, BorderLayout.NORTH);
        dialog.add(new JScrollPane(t), BorderLayout.CENTER);
        dialog.add(btnSelect, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }




    // =================== PRINTABLE INTERFACE ===================


    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }


        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        // Page dimensions
        int pageWidth = (int) pageFormat.getImageableWidth();
        int pageHeight = (int) pageFormat.getImageableHeight();
        int margin = 40;
        int y = margin;


        // Fonts for printing
        Font headerFont = new Font("Arial", Font.BOLD, 16);
        Font titleFont = new Font("Arial", Font.BOLD, 14);
        Font sectionFont = new Font("Arial", Font.BOLD, 12);
        Font labelFont = new Font("Arial", Font.BOLD, 11);
        Font contentFont = new Font("Arial", Font.PLAIN, 11);
        Font signatureFont = new Font("Arial", Font.ITALIC, 10);


        // =================== HEADER ===================
        g2d.setFont(headerFont);
        String republic = "REPUBLIC OF THE PHILIPPINES";
        g2d.drawString(republic, margin + (pageWidth - margin * 2 - g2d.getFontMetrics().stringWidth(republic)) / 2, y);
        y += 20;


        String province = "PROVINCE OF CAMARINES NORTE";
        g2d.drawString(province, margin + (pageWidth - margin * 2 - g2d.getFontMetrics().stringWidth(province)) / 2, y);
        y += 20;


        String city = "CITY OF DAET";
        g2d.drawString(city, margin + (pageWidth - margin * 2 - g2d.getFontMetrics().stringWidth(city)) / 2, y);
        y += 20;


        String brgy = "BARANGAY ALAWIHAO";
        g2d.drawString(brgy, margin + (pageWidth - margin * 2 - g2d.getFontMetrics().stringWidth(brgy)) / 2, y);
        y += 20;


        String office = "OFFICE OF THE PUNONG BARANGAY";
        g2d.drawString(office, margin + (pageWidth - margin * 2 - g2d.getFontMetrics().stringWidth(office)) / 2, y);
        y += 30;


        // =================== TITLE ===================
        g2d.setFont(titleFont);
        String title = "OFFICIAL BLOTTER FORM";
        g2d.drawString(title, margin + (pageWidth - margin * 2 - g2d.getFontMetrics().stringWidth(title)) / 2, y);
        y += 25;


        // Draw line under title
        g2d.drawLine(margin, y, pageWidth - margin, y);
        y += 25;


        // =================== CASE INFORMATION ===================
        g2d.setFont(sectionFont);
        g2d.drawString("I. CASE INFORMATION", margin, y);
        y += 20;


        // Draw box for Case No
        int caseBoxWidth = 150;
        int caseBoxX = pageWidth - margin - caseBoxWidth;
        g2d.drawRect(caseBoxX, y - 15, caseBoxWidth, 25);
        g2d.drawString("CASE NO: " + txtCaseNo.getText(), caseBoxX + 10, y);


        g2d.setFont(labelFont);
        g2d.drawString("Date Recorded:", margin, y);
        g2d.setFont(contentFont);
        g2d.drawString(txtDateRecorded.getText(), margin + 100, y);
        y += 15;


        g2d.setFont(labelFont);
        g2d.drawString("Time Recorded:", margin, y);
        g2d.setFont(contentFont);
        g2d.drawString(txtTimeRecorded.getText(), margin + 100, y);
        y += 25;


        // =================== PARTIES INVOLVED ===================
        g2d.setFont(sectionFont);
        g2d.drawString("II. PARTIES INVOLVED", margin, y);
        y += 20;


        // Complainant
        g2d.setFont(labelFont);
        g2d.drawString("COMPLAINANT (Nagrereklamo):", margin, y);
        y += 15;
        g2d.setFont(contentFont);
        String complainant = txtComplainantName.getText().isEmpty() ? "[Not specified]" : txtComplainantName.getText();
        g2d.drawString(complainant, margin + 20, y);
        g2d.drawLine(margin + 15, y + 2, pageWidth - margin, y + 2);
        y += 20;


        // Respondent
        g2d.setFont(labelFont);
        g2d.drawString("RESPONDENT (Inirereklamo):", margin, y);
        y += 15;
        g2d.setFont(contentFont);
        String respondent = txtRespondentName.getText().isEmpty() ? "[Not specified]" : txtRespondentName.getText();
        g2d.drawString(respondent, margin + 20, y);
        g2d.drawLine(margin + 15, y + 2, pageWidth - margin, y + 2);
        y += 20;


        // Victim
        g2d.setFont(labelFont);
        g2d.drawString("VICTIM (Biktima):", margin, y);
        y += 15;
        g2d.setFont(contentFont);
        String victim = txtVictim.getText().isEmpty() ? "[Not specified]" : txtVictim.getText();
        g2d.drawString(victim, margin + 20, y);
        g2d.drawLine(margin + 15, y + 2, pageWidth - margin, y + 2);
        y += 25;


        // =================== INCIDENT DETAILS ===================
        g2d.setFont(sectionFont);
        g2d.drawString("III. INCIDENT DETAILS", margin, y);
        y += 20;


        g2d.setFont(labelFont);
        g2d.drawString("Incident Type:", margin, y);
        g2d.setFont(contentFont);
        String incidentType = cmbIncidentType.getSelectedItem() == null ? "[Not specified]" : cmbIncidentType.getSelectedItem().toString();
        g2d.drawString(incidentType, margin + 100, y);
        y += 15;


        g2d.setFont(labelFont);
        g2d.drawString("Date (Petsa):", margin, y);
        g2d.setFont(contentFont);
        g2d.drawString(txtIncidentDate.getText(), margin + 100, y);


        g2d.setFont(labelFont);
        g2d.drawString("Time (Oras):", margin + 250, y);
        g2d.setFont(contentFont);
        String incidentTime = txtIncidentTime.getText().isEmpty() ? "[Not specified]" : txtIncidentTime.getText();
        g2d.drawString(incidentTime, margin + 310, y);
        y += 15;


        g2d.setFont(labelFont);
        g2d.drawString("Location (Lugar):", margin, y);
        g2d.setFont(contentFont);
        String location = txtIncidentPlace.getText().isEmpty() ? "[Not specified]" : txtIncidentPlace.getText();
        g2d.drawString(location, margin + 100, y);
        y += 25;


        // =================== NARRATIVE ===================
        g2d.setFont(sectionFont);
        g2d.drawString("IV. NARRATIVE OF EVENTS (Salaysay)", margin, y);
        y += 20;


        g2d.setFont(contentFont);
        String narrative = txtNarrative.getText().isEmpty() ? "[Not provided]" : txtNarrative.getText();
        y = drawWrappedText(g2d, narrative, margin, y, pageWidth - margin * 2, contentFont);
        y += 15;


        // =================== RESOLUTION ===================
        g2d.setFont(sectionFont);
        g2d.drawString("V. RESOLUTION (Pagkakasundo)", margin, y);
        y += 20;


        g2d.setFont(contentFont);
        String resolution = txtResolution.getText().isEmpty() ? "[Not provided]" : txtResolution.getText();
        y = drawWrappedText(g2d, resolution, margin, y, pageWidth - margin * 2, contentFont);
        y += 15;


        // =================== WITNESSES ===================
        g2d.setFont(sectionFont);
        g2d.drawString("VI. WITNESSES (Saksi)", margin, y);
        y += 20;


        g2d.setFont(contentFont);
        String witnesses = txtWitnesses.getText().isEmpty() ? "[No witnesses listed]" : txtWitnesses.getText();
        y = drawWrappedText(g2d, witnesses, margin, y, pageWidth - margin * 2, contentFont);
        y += 15;


        // =================== OFFICIAL DETAILS ===================
        g2d.setFont(sectionFont);
        g2d.drawString("VII. OFFICIAL DETAILS", margin, y);
        y += 20;


        g2d.setFont(labelFont);
        g2d.drawString("Officer-in-Charge:", margin, y);
        g2d.setFont(contentFont);
        String officer = txtOfficer.getText().isEmpty() ? "[Not assigned]" : txtOfficer.getText();
        g2d.drawString(officer, margin + 120, y);
        y += 15;


        g2d.setFont(labelFont);
        g2d.drawString("Hearing Date:", margin, y);
        g2d.setFont(contentFont);
        String hearingDate = cmbHearingDate.getSelectedItem() == null ? "[Not set]" : cmbHearingDate.getSelectedItem().toString();
        g2d.drawString(hearingDate, margin + 100, y);


        g2d.setFont(labelFont);
        g2d.drawString("Summon Date:", margin + 250, y);
        g2d.setFont(contentFont);
        g2d.drawString(txtSummonDate.getText(), margin + 320, y);
        y += 25;


        // =================== SIGNATURES ===================
        g2d.setFont(sectionFont);
        g2d.drawString("VIII. SIGNATURES", margin, y);
        y += 20;


        int signatureY = y;
        int signatureWidth = 200;
        int signatureSpacing = 50;


        // Complainant signature
        g2d.drawLine(margin, signatureY, margin + signatureWidth, signatureY);
        g2d.setFont(signatureFont);
        g2d.drawString("Complainant", margin, signatureY + 15);


        // Respondent signature
        g2d.drawLine(margin + 250, signatureY, margin + 250 + signatureWidth, signatureY);
        g2d.drawString("Respondent", margin + 250, signatureY + 15);
        signatureY += signatureSpacing;


        // Witness signature
        g2d.drawLine(margin, signatureY, margin + signatureWidth, signatureY);
        g2d.drawString("Witness", margin, signatureY + 15);


        // Officer signature
        g2d.drawLine(margin + 250, signatureY, margin + 250 + signatureWidth, signatureY);
        g2d.drawString("Barangay Official", margin + 250, signatureY + 15);
        y = signatureY + 40;


        // =================== FOOTER ===================
        g2d.setFont(new Font("Arial", Font.ITALIC, 9));
        String footer = "Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) +
                " at " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        g2d.drawString(footer, margin + (pageWidth - margin * 2 - g2d.getFontMetrics().stringWidth(footer)) / 2, y);


        return PAGE_EXISTS;
    }


    private int drawWrappedText(Graphics2D g2d, String text, int x, int y, int width, Font font) {
        g2d.setFont(font);
        if (text == null || text.isEmpty()) {
            text = "[Not specified]";
        }


        String[] paragraphs = text.split("\n");
        int lineHeight = g2d.getFontMetrics().getHeight();


        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();


            for (String word : words) {
                if (g2d.getFontMetrics().stringWidth(line + word) < width) {
                    line.append(word).append(" ");
                } else {
                    g2d.drawString(line.toString(), x, y);
                    y += lineHeight;
                    line = new StringBuilder(word + " ");
                }
            }
            g2d.drawString(line.toString(), x, y);
            y += lineHeight;
        }
        return y;
    }




    private JTabbedPane findParentTabbedPane() {
        Container container = this;
        while (container != null && !(container instanceof JTabbedPane)) {
            container = container.getParent();
        }
        return (JTabbedPane) container;
    }


    private void updateStatisticsPanelInReports(JPanel reportsPanel) {
        // This method finds the statistics panel in the reports panel and updates it
        // Look for the statistics panel in the BorderLayout.SOUTH position
        if (reportsPanel.getLayout() instanceof BorderLayout) {
            Component southComponent = ((BorderLayout) reportsPanel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            if (southComponent instanceof JPanel) {
                JPanel currentStatsPanel = (JPanel) southComponent;
                JPanel newStatsPanel = createStatisticsPanel();


                // Replace the old stats panel with new one
                reportsPanel.remove(currentStatsPanel);
                reportsPanel.add(newStatsPanel, BorderLayout.SOUTH);
                reportsPanel.revalidate();
                reportsPanel.repaint();
            }
        }
    }
    // Add these fields to your class (near the other statistics fields)
    private JLabel[] statLabels;




    // =================== TABLE RENDERERS & EDITORS ===================




    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 10));
            setOpaque(true);




            // Glass effect colors
            if (status.equals("Resolved")) {
                setBackground(new Color(46, 204, 113, 180));
                setForeground(Color.WHITE);
            } else if (status.equals("Pending")) {
                setBackground(new Color(241, 196, 15, 180));
                setForeground(Color.WHITE);
            } else if (status.equals("Ongoing")) {
                setBackground(new Color(52, 152, 219, 180));
                setForeground(Color.WHITE);
            } else if (status.equals("Summoned")) {
                setBackground(new Color(231, 76, 60, 180));
                setForeground(Color.WHITE);
            } else if (status.equals("Closed")) {
                setBackground(new Color(149, 165, 166, 180));
                setForeground(Color.WHITE);
            } else {
                setBackground(new Color(255, 255, 255, 100));
                setForeground(Color.WHITE);
            }




            if (isSelected) {
                setBackground(getBackground().brighter());
            }




            return c;
        }
    }




    class ViewEditButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ViewEditButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 10));
            setBackground(new Color(52, 152, 219, 180));
            setForeground(Color.black);
            setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }




        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }




    class ViewEditButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private JTable table;
        private int clickedRow = -1; // Store the clicked row


        public ViewEditButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
            });
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            this.clickedRow = row; // Store the row being clicked
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setBackground(new Color(52, 152, 219));
            button.setForeground(Color.WHITE);
            isPushed = true;
            return button;
        }


        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Use the stored row
                if (clickedRow >= 0) {
                    SwingUtilities.invokeLater(() -> {
                        viewEditCase(clickedRow);
                    });
                }
            }
            isPushed = false;
            return label;
        }


        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }


    class PrintButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public PrintButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBackground(new Color(46, 204, 113, 180));
            setForeground(Color.black);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }




        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }


    class PrintButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private JTable table;
        private int clickedRow = -1;


        public PrintButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.table = table;
            this.clickedRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setBackground(new Color(46, 204, 113));
            button.setForeground(Color.WHITE);
            isPushed = true;
            return button;
        }


        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                if (clickedRow >= 0) {
                    SwingUtilities.invokeLater(() -> {
                        printCase(clickedRow); // This should be printCase, not viewEditCase
                    });
                }
            }
            isPushed = false;
            return label;
        }


        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
    // =================== MAIN METHOD ===================
// Add these validation methods to your class:


    // VALIDATION METHODS
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        // Allows only letters (including Filipino names with accents), spaces, hyphens, apostrophes, periods
        // DOES NOT allow numbers or special characters
        String regex = "^[A-Za-zÀ-ÿñÑ\\s.'-]{2,100}$";
        return name.trim().matches(regex);
    }


    private boolean isValidLocation(String location) {
        if (location == null || location.trim().isEmpty()) return false;
        // Allows letters, numbers, spaces, hyphens, periods, commas, parentheses, # for addresses
        // Blocks most special characters except common address symbols
        String regex = "^[A-Za-zÀ-ÿñÑ0-9\\s.,#'()-]{2,200}$";
        return location.trim().matches(regex);
    }


    private boolean isValidOfficerName(String officer) {
        if (officer == null || officer.trim().isEmpty()) return false;
        // Allows only letters, spaces, hyphens, apostrophes, periods
        // DOES NOT allow numbers or special characters
        String regex = "^[A-Za-zÀ-ÿñÑ\\s.'-]{2,100}$";
        return officer.trim().matches(regex);
    }


    private boolean isValidTextArea(String text, int minLength, int maxLength) {
        if (text == null || text.trim().isEmpty()) return true;
        text = text.trim();
        if (text.length() > maxLength) return false;


        // Allows letters, numbers, spaces, and limited punctuation
        // Blocks most special characters
        String regex = "^[A-Za-zÀ-ÿñÑ0-9\\s.,'\"!?\\-():;]*$";
        return text.matches(regex);
    }


    // Time validation method (24-hour format HH:MM)
    private boolean isValidTime(String time) {
        if (time == null || time.trim().isEmpty()) return false;
        String regex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        return time.trim().matches(regex);
    }


    private void showUnfilledFieldsDialog(List<String> missingFields) {
        if (!missingFields.isEmpty()) {
            StringBuilder message = new StringBuilder("Please fill in the following required fields:\n\n");
            for (String field : missingFields) {
                message.append("• ").append(field).append("\n");
            }
            message.append("\nInstructions:\n");
            message.append("• Names: Only letters, spaces, hyphens (-), apostrophes ('), periods (.)\n");
            message.append("• Names: NO numbers or special characters\n");
            message.append("• Location: Letters, numbers, spaces, and: . , # ' ( ) -\n");
            message.append("• Time: HH:MM format (00:00-23:59), numbers only\n");
            message.append("• Fields with * are required.");


            JOptionPane.showMessageDialog(this,
                    message.toString(),
                    "Missing Required Fields",
                    JOptionPane.WARNING_MESSAGE);
        }
    }


    // First, update the addValidationListener method to handle both JTextField and JTextArea
    private void addFieldValidationListeners() {
        // Add document listeners to text fields for real-time validation
        addValidationListener(txtComplainantName, (String text) -> isValidName(text));
        addValidationListener(txtRespondentName, (String text) -> isValidName(text));
        addValidationListener(txtVictim, (String text) -> isValidName(text));
        addValidationListener(txtIncidentPlace, (String text) -> isValidLocation(text));


        // Add validation for txtOfficer (JTextArea)
        txtOfficer.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateOfficerField(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateOfficerField(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateOfficerField(); }


            private void validateOfficerField() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String text = txtOfficer.getText().trim();
                        if (text.isEmpty()) {
                            txtOfficer.setBackground(new Color(255, 230, 200)); // Light orange for empty required field
                            txtOfficer.setToolTipText("Officer Name is required!");
                        } else if (!isValidOfficerName(text)) {
                            txtOfficer.setBackground(new Color(255, 230, 230));
                            if (text.matches(".*\\d.*")) {
                                txtOfficer.setToolTipText("Numbers are not allowed in Officer Name!");
                            } else if (text.matches(".*[#@\\$%\\^&\\*\\+=\\[\\]\\{\\}\\|;:\"<>].*")) {
                                txtOfficer.setToolTipText("Special characters are not allowed in Officer Name!");
                            } else {
                                txtOfficer.setToolTipText("Invalid characters in Officer Name!");
                            }
                        } else {
                            txtOfficer.setBackground(Color.WHITE);
                            txtOfficer.setToolTipText(null);
                        }
                    } catch (Exception ex) {
                        // Ignore
                    }
                });
            }
        });


        // Add validation for other text areas
        addTextAreaValidation(txtNarrative, "Narrative", 1000);
        addTextAreaValidation(txtResolution, "Resolution", 1000);
        addTextAreaValidation(txtWitnesses, "Witnesses", 500);
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
                            field.setBackground(new Color(255, 230, 230));
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


    private void addTextAreaValidation(JTextArea textArea, String fieldName, int maxLength) {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateField(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateField(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateField(); }


            private void validateField() {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String text = textArea.getText();
                        if (!text.isEmpty()) {
                            // Check for special characters
                            if (text.matches(".*[#@\\$%\\^&\\*\\+=\\[\\]\\{\\}\\|;:\"<>].*")) {
                                textArea.setBackground(new Color(255, 230, 230));
                                textArea.setToolTipText("Special characters are not allowed in " + fieldName + "!");
                            } else if (text.length() > maxLength) {
                                textArea.setBackground(new Color(255, 230, 230));
                                textArea.setToolTipText(fieldName + " is too long! Max " + maxLength + " characters.");
                            } else {
                                textArea.setBackground(Color.WHITE);
                                textArea.setToolTipText(null);
                            }
                        } else {
                            textArea.setBackground(Color.WHITE);
                            textArea.setToolTipText(null);
                        }
                    } catch (Exception ex) {
                        // Ignore
                    }
                });
            }
        });
    }
    private void addInputFiltering() {
        // Add input filters to prevent special characters
        addInputFilter(txtComplainantName, false); // No numbers, no special chars
        addInputFilter(txtRespondentName, false);  // No numbers, no special chars
        addInputFilter(txtVictim, false);          // No numbers, no special chars
        addInputFilter(txtIncidentPlace, true);    // Allow numbers for addresses


        // Add input filter for officer text area
        addInputFilterToTextArea(txtOfficer, false);


        // Add input filters for other text areas
        addInputFilterToTextArea(txtNarrative, true);
        addInputFilterToTextArea(txtResolution, true);
        addInputFilterToTextArea(txtWitnesses, true);
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


    private void addNumberDetectionToNameFields() {
        // Create a key listener that prevents numbers in name fields
        KeyAdapter numberBlockingAdapter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isDigit(c)) {
                    e.consume(); // Prevent the digit from being entered


                    // Show feedback
                    JTextField source = (JTextField) e.getSource();
                    source.setToolTipText("Numbers are not allowed in names!");
                    source.setBackground(new Color(255, 200, 200));


                    // Schedule to clear the tooltip after 2 seconds
                    Timer timer = new Timer(2000, ev -> {
                        source.setToolTipText(null);
                        source.setBackground(Color.WHITE);
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        };


        // Apply to all name text fields
        txtComplainantName.addKeyListener(numberBlockingAdapter);
        txtRespondentName.addKeyListener(numberBlockingAdapter);
        txtVictim.addKeyListener(numberBlockingAdapter);


        // For txtOfficer (JTextArea)
        txtOfficer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isDigit(c)) {
                    e.consume();
                    txtOfficer.setToolTipText("Numbers are not allowed in officer names!");
                    txtOfficer.setBackground(new Color(255, 200, 200));


                    Timer timer = new Timer(2000, ev -> {
                        txtOfficer.setToolTipText(null);
                        txtOfficer.setBackground(Color.WHITE);
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }




            JFrame frame = new JFrame("Barangay Blotter System - Admin");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1400, 900);
            frame.setLocationRelativeTo(null);




            frame.add(new AdminBlotterTab());
            frame.setVisible(true);
        });
    }


}



