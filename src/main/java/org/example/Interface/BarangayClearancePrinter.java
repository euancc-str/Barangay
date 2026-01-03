package org.example.Interface;


import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.BlotterCaseDAO;
import org.example.Documents.Payment;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.Users.BarangayStaff;


import java.awt.*;
import java.awt.print.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.ImageIcon;


public class BarangayClearancePrinter implements Printable {


    // Personal information
    private String lastName;
    private String firstName;
    private String middleName;
    private String street;
    private String purok;
    private String barangay;
    private String municipality;
    private String province;
    private String birthDate;
    private String age;
    private String birthPlace;
    private String maritalStatus;
    private String remarks;
    private String ctcNumber;
    private String dateIssued;
    private String placeIssued;
    private String amountPaid;
    private String orNumber;
    private String purpose;
    private int requestId;

    // Staff information
    private int residentId;
    private String captainName;
    private String secretaryName;


    // Fonts (INCREASED SIZES FOR READABILITY)
    private final Font FONT_HEADER = new Font("Times New Roman", Font.PLAIN, 11);
    private final Font FONT_OFFICE = new Font("Arial", Font.BOLD, 12);
    private final Font FONT_TITLE = new Font("Arial", Font.BOLD, 18);
    private final Font FONT_BODY = new Font("Times New Roman", Font.PLAIN, 10);
    private final Font FONT_LABEL = new Font("Arial", Font.BOLD, 10);
    private final Font FONT_VALUE = new Font("Arial", Font.PLAIN, 10);
    private final Font FONT_SIGNATURE = new Font("Arial", Font.BOLD, 10);


    // Main constructor with all fields
    public BarangayClearancePrinter(String lastName, String firstName, String middleName,
                                    String street, String purok, String barangay,
                                    String municipality, String province, String birthDate,
                                    String age, String birthPlace, String maritalStatus,
                                    String purpose, String ctcNumber, String dateIssued,
                                    String placeIssued, int residentId,int requestId) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.street = street;
        this.purok = purok;
        this.barangay = barangay;
        this.municipality = municipality;
        this.province = province;
        this.birthDate = birthDate;
        this.age = age;
        this.birthPlace = birthPlace;
        this.maritalStatus = maritalStatus;
        this.purpose = purpose;
        this.ctcNumber = ctcNumber;
        this.dateIssued = dateIssued;
        this.placeIssued = placeIssued;
        this.residentId = residentId;

        // Default values from image
        this.remarks = "NO DEROGATORY RECORD";
        this.amountPaid = "0.00";
        this.orNumber = "";

        // Get staff names from database
        try {
            BarangayStaff captain = new StaffDAO().findStaffByPosition("Captain");
            this.captainName = captain != null ?
                    captain.getFirstName() + " " + captain.getMiddleName() + " " + captain.getLastName() :
                    "ROBERT E. PALENCIA";

            BarangayStaff secretary = new StaffDAO().findStaffByPosition("Secretary");
            this.secretaryName = secretary != null ?
                    secretary.getFirstName() + " " + secretary.getMiddleName() + " " + secretary.getLastName() :
                    "AZEL TYINKLE V. GARCIA";
        } catch (Exception e) {
            this.captainName = "ROBERT E. PALENCIA";
            this.secretaryName = "AZEL TYINKLE V. GARCIA";
        }
    }

    private static SystemConfigDAO dao = new SystemConfigDAO();

    private final String LOGO_PATH = dao.getLogoPath(); // Ensure this folder exists
    // Additional constructor for backward compatibility
    public BarangayClearancePrinter(String fullName, String address, String gender, String dob, String age,
                                    String civilStatus, String purpose, String ctcNo, String ctcDate,
                                    String ctcPlace, int residentId, int requestId,String remarks) {
        // Parse the full name
        String[] nameParts = parseName(fullName);
        this.lastName = nameParts[0];
        this.firstName = nameParts[1];
        this.middleName = nameParts[2];

        // Parse address
        String[] addressParts = parseAddress(address);
        this.street = addressParts[0];
        this.purok = addressParts[1];


        this.barangay = "Alawihao";
        this.municipality = "Municipality of Daet";
        this.province = "Camarines Norte";

        this.birthDate = dob;
        this.age = age;
        this.birthPlace = "Daet, Camarines Norte";
        this.maritalStatus = civilStatus;
        this.purpose = purpose;
        this.ctcNumber = (ctcNo != null && !ctcNo.equals("CC")) ? ctcNo : "";
        this.dateIssued = (ctcDate != null && !ctcDate.equalsIgnoreCase("null")) ? ctcDate : "";
        this.placeIssued = ctcPlace;
        this.residentId = residentId;
        this.requestId = requestId;

        Payment documentData = new ResidentDAO().findResidentReceiptById(requestId);
        String docu = documentData.getOrNumber();
        String fee = String.valueOf(documentData.getAmount());
        // Default values from image


        this.remarks = remarks;
        this.amountPaid = fee;
        this.orNumber = docu;

        // Get staff names from database
        try {
            BarangayStaff captain = new StaffDAO().findStaffByPosition("Captain");
            this.captainName = captain != null ?
                    captain.getFirstName() + " " + captain.getMiddleName() + " " + captain.getLastName() :
                    "ROBERT E. PALENCIA";

            BarangayStaff secretary = new StaffDAO().findStaffByPosition("Secretary");
            this.secretaryName = secretary != null ?
                    secretary.getFirstName() + " " + secretary.getMiddleName() + " " + secretary.getLastName() :
                    "RAZEL V. GARCIA";
        } catch (Exception e) {
            this.captainName = "ROBERT E. PALENCIA";
            this.secretaryName = "RAZEL V. GARCIA";
        }
    }


    // Helper method to parse full name into components
    private String[] parseName(String fullName) {
        String lastName = "";
        String firstName = "";
        String middleName = "";

        if (fullName != null && !fullName.isEmpty()) {
            String[] nameParts = fullName.trim().split("\\s+");

            if (nameParts.length == 1) {
                lastName = nameParts[0];
            } else if (nameParts.length == 2) {
                firstName = nameParts[0];
                lastName = nameParts[1];
            } else if (nameParts.length >= 3) {
                firstName = nameParts[0];
                // Last element is the last name
                lastName = nameParts[nameParts.length - 1];
                // Everything in between is middle name
                StringBuilder middle = new StringBuilder();
                for (int i = 1; i < nameParts.length - 1; i++) {
                    if (middle.length() > 0) middle.append(" ");
                    middle.append(nameParts[i]);
                }
                middleName = middle.toString();
            }
        }

        return new String[]{lastName, firstName, middleName};
    }


    // Helper method to parse address
    private String[] parseAddress(String address) {
        String street = "";
        String purok = "1"; // Default to Purok 1 as shown in image

        if (address != null && !address.isEmpty()) {
            // Try to extract purok information
            if (address.toLowerCase().contains("purok")) {
                String[] parts = address.split("(?i)purok");
                if (parts.length > 1) {
                    // Extract purok number
                    String afterPurok = parts[1].trim();
                    String purokNum = afterPurok.replaceAll("^\\D*(\\d+).*", "$1");
                    if (!purokNum.isEmpty()) {
                        purok = purokNum;
                    }
                    // The part before "purok" is likely the street
                    street = parts[0].trim();
                } else {
                    street = address;
                }
            } else {
                street = address;
            }
        }

        return new String[]{street, purok};
    }


    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int width = (int) pf.getImageableWidth();
        int y = 40; // Starting Y position for Header
        int pageWidth = (int) pf.getImageableWidth();

        // ==========================================================
        // 1. HEADER (Republic, Province, Municipality, Barangay)
        // ==========================================================
        Image logo = null;
        try {
            logo = new ImageIcon(LOGO_PATH).getImage();
        } catch (Exception e) {
            System.err.println("Logo not found: " + e.getMessage());
        }

        if (logo != null) {
            int logoSize = 90;
            // Draw LEFT Logo
            g2d.drawImage(logo, 40, 30, logoSize, logoSize, null);

            // Draw RIGHT Logo
            int rightLogoX = pageWidth - 40 - logoSize;
            logo = new ImageIcon(dao.getDaetLogoPath()).getImage();
            g2d.drawImage(logo, rightLogoX, 30, logoSize, logoSize, null);
        }

        // Header text centered
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, "Republic of the Philippines", FONT_HEADER, width / 2, y + 15);
        drawCenteredText(g2d, "Province of Camarines Norte", FONT_HEADER, width / 2, y + 30);
        drawCenteredText(g2d, "Municipality of Daet", FONT_HEADER, width / 2, y + 45);
        drawCenteredText(g2d, "Barangay Alawihao", FONT_HEADER, width / 2, y + 60);
        y += 80;

        // ==========================================================
        // 2. OFFICE OF THE PUNONG BARANGAY
        // ==========================================================
        drawCenteredText(g2d, "OFFICE OF THE PUNONG BARANGAY", FONT_OFFICE, width / 2, y);
        y += 20;

        // ==========================================================
        // 3. BARANGAY CLEARANCE TITLE
        // ==========================================================
        drawCenteredText(g2d, "BARANGAY CLEARANCE", FONT_TITLE, width / 2, y);
        y += 40;

        // ==========================================================
        // 4. "TO WHOM IT MAY CONCERN" and Date
        // ==========================================================
        g2d.setFont(new Font("Times New Roman", Font.PLAIN, 12));
        g2d.drawString("TO WHOM IT MAY CONCERN", 50, y);

        // Date on the right
        String printDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString("Date Printed: " + printDate, width - 150, y);
        y += 30;

        // ==========================================================
        // 5. CERTIFICATION STATEMENT
        // ==========================================================
        g2d.setFont(FONT_BODY);
        String certification = "This is to certify that the person whose name and right thumb prints appear " +
                "hereon has requested a RECORD CLEARANCE from this office and the result(s) is/are listed below:";
        y = drawWrappedText(g2d, certification, 50, y, width - 100, 15);
        y += 20;

        // ==========================================================
        // 6. PERSONAL INFORMATION TABLE
        // ==========================================================
        int labelX = 50;
        int valueX = 180;
        int lineY = y; // Start the table at the current Y position

        // --- Photo Section (Fixed Right Side) ---
        int photoX = width - 150;
        int photoY = lineY;
        int photoSize = 100;

        try {
            PhotoDAO photoDao = new PhotoDAO();
            String path = photoDao.getPhotoPath(residentId);
            if (path != null && !path.isEmpty()) {
                String fullPath = new ImageUtils().getImageDir() + path;
                ImageIcon icon = new ImageIcon(fullPath);
                g2d.drawImage(icon.getImage(), photoX, photoY, photoSize, photoSize, null);
            }
            g2d.drawRect(photoX, photoY, photoSize, photoSize);
        } catch (Exception e) {
            g2d.drawRect(photoX, photoY, photoSize, photoSize);
        }
        drawCenteredText(g2d, "PICTURE", new Font("Arial", Font.PLAIN, 8), photoX + photoSize / 2, photoY + photoSize + 10);

        // --- Data Fields (Left Column) ---
        int dataSpacing = 22;

        drawField(g2d, "LAST NAME:", lastName.toUpperCase(), labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "FIRST NAME:", firstName.toUpperCase(), labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "MIDDLE INITIAL:", middleName.toUpperCase(), labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "STREET:", street != null ? street.toUpperCase() : "", labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "PUROK:", purok, labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "BARANGAY:", barangay.toUpperCase(), labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "MUNICIPALITY:", municipality.toUpperCase(), labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "PROVINCE:", province.toUpperCase(), labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "BIRTHDATE:", birthDate, labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "AGE:", age, labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "BIRTHPLACE:", birthPlace.toUpperCase(), labelX, valueX, lineY);
        lineY += dataSpacing;

        // "Not valid without seal" text
        g2d.setFont(new Font("Arial", Font.PLAIN, 8));
        g2d.drawString("Not valid without seal", photoX + 25, lineY - 5);

        drawField(g2d, "MARITAL STATUS:", maritalStatus.toUpperCase(), labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "REMARKS:", remarks, labelX, valueX, lineY);
        lineY += dataSpacing;

        // --- Right Thumb Mark Box ---
        int thumbY = lineY - 10;
        int thumbSize = 80;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(photoX + (photoSize / 2) - (thumbSize / 2), thumbY, thumbSize, thumbSize);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 8));
        drawCenteredText(g2d, "RIGHT THUMB MARK", new Font("Arial", Font.PLAIN, 8), photoX + photoSize / 2, thumbY + thumbSize + 10);

        drawField(g2d, "CTC NUMBER:", ctcNumber != null ? ctcNumber : "", labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "DATE ISSUED:", dateIssued != null ? dateIssued : "", labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "PLACE ISSUED:", placeIssued != null ? placeIssued.toUpperCase() : "", labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "AMOUNT PAID:", amountPaid, labelX, valueX, lineY);
        lineY += dataSpacing;

        drawField(g2d, "OR NUMBER:", orNumber, labelX, valueX, lineY);
        lineY += 40; // Add extra space before Purpose section

        // ==========================================================
        // 7. PURPOSE SECTION (FIXED WITH JTEXTAREA)
        // ==========================================================
        // We use a hidden JTextArea here because it handles line wrapping much better than manual calculation

        // 1. Prepare Text
        String displayPurpose = purpose;
        if (purpose != null && purpose.contains("|")) {
            displayPurpose = purpose.split("\\|")[0].trim();
        }

        String bodyText = "THIS CERTIFICATION IS ISSUED FOR THE PURPOSE OF: "
                + (displayPurpose != null ? displayPurpose.toUpperCase() : "N/A")
                + ".";

        // 2. Setup Invisible Text Area
        javax.swing.JTextArea textArea = new javax.swing.JTextArea();
        textArea.setText(bodyText);
        textArea.setFont(new Font("Arial", Font.BOLD, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false); // Transparent

        // 3. Set Dimensions (Width = Page Width - Margins)
        int textWidth = width - 100; // 50 left margin + 50 right margin = 100
        textArea.setSize(textWidth, 1000); // Set tall height initially to calculate need
        int neededHeight = textArea.getPreferredSize().height;
        textArea.setSize(textWidth, neededHeight);

        // 4. Draw the Text Area onto the Paper
        g2d.translate(50, lineY); // Move "pen" to position (x=50, y=lineY)
        textArea.print(g2d);      // Print the text area
        g2d.translate(-50, -lineY); // Move "pen" back

        // 5. Update lineY for next section
        lineY += neededHeight + 25;

        // ==========================================================
        // 8. VALIDITY
        // ==========================================================
        g2d.setFont(FONT_BODY);
        g2d.drawString("VALID FOR SIX MONTHS FROM DATE OF ISSUE:", 50, lineY);
        lineY += 20;

        // ==========================================================
        // 9. SIGNATURES SECTION
        // ==========================================================
        final Font SMALL_SIGNATURE_NAME = new Font("Arial", Font.BOLD, 10);
        final Font SMALL_SIGNATURE_TITLE = new Font("Arial", Font.PLAIN, 10);

        int signatureStartY = lineY + 10;

        // Secretary (Left)
        int secX = 100;
        g2d.setFont(SMALL_SIGNATURE_NAME);
        drawCenteredText(g2d, secretaryName, SMALL_SIGNATURE_NAME, secX + 100, signatureStartY);
        g2d.setFont(SMALL_SIGNATURE_TITLE);
        g2d.drawLine(secX, signatureStartY + 2, secX + 200, signatureStartY + 2);
        drawCenteredText(g2d, "Barangay Secretary", SMALL_SIGNATURE_TITLE, secX + 100, signatureStartY + 15);

        // Captain (Right)
        int capX = width - 300;
        g2d.setFont(SMALL_SIGNATURE_NAME);
        drawCenteredText(g2d, captainName, SMALL_SIGNATURE_NAME, capX + 100, signatureStartY);
        g2d.setFont(SMALL_SIGNATURE_TITLE);
        g2d.drawLine(capX, signatureStartY + 2, capX + 200, signatureStartY + 2);
        drawCenteredText(g2d, "Punong Barangay", SMALL_SIGNATURE_TITLE, capX + 100, signatureStartY + 15);

        return PAGE_EXISTS;
    }

    // ==========================================================
    // HELPER METHODS
    // ==========================================================


    private void drawCenteredText(Graphics2D g, String text, Font font, int centerX, int y) {
        g.setFont(font);
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - (w / 2), y);
    }


    // drawField handles drawing without an underline
    private void drawField(Graphics2D g, String label, String value,
                           int labelX, int valueX, int y) {
        // Draw label
        g.setFont(FONT_LABEL);
        g.drawString(label, labelX, y);

        // Draw value
        g.setFont(FONT_VALUE);
        String displayValue = value != null ? value : "";
        g.drawString(displayValue, valueX, y);
    }


    private int drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.toString() + (currentLine.length() > 0 ? " " : "") + word;
            int testWidth = fm.stringWidth(testLine);

            if (testWidth > maxWidth && currentLine.length() > 0) {
                // Draw the current line
                g.drawString(currentLine.toString(), x, y);
                y += lineHeight;
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        // Draw the last line
        if (currentLine.length() > 0) {
            g.drawString(currentLine.toString(), x, y);
            y += lineHeight;
        }

        return y;
    }
}

