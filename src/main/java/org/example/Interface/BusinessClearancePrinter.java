package org.example.Interface;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.Documents.Payment;
import org.example.ResidentDAO;
import org.example.StaffDAO;
import org.example.Users.BarangayStaff;
import java.awt.*;

import java.awt.print.*;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.print.*;

public class BusinessClearancePrinter implements Printable {

    private String ownerName;
    private String address;
    private String businessDetails; // The "Purpose" string containing business info
    private String ctcNo, ctcDate, ctcPlace;
    private String captainName;
    private int requestId;

    // Fonts
    private final Font FONT_HEADER = new Font("Times New Roman", Font.PLAIN, 12);
    private final Font FONT_OFFICE = new Font("Arial", Font.BOLD, 12);
    private final Font FONT_TITLE = new Font("Arial", Font.BOLD, 24);
    private final Font FONT_LABEL = new Font("Arial", Font.BOLD, 11);
    private final Font FONT_VALUE = new Font("Arial", Font.PLAIN, 12);
    private final Font FONT_BODY = new Font("Times New Roman", Font.PLAIN, 12);

    public BusinessClearancePrinter(String ownerName, String address, String businessDetails,
                                    String ctcNo, String ctcDate, String ctcPlace,int requestId) {
        this.ownerName = ownerName;
        this.address = address;
        this.businessDetails = businessDetails;
        this.ctcNo =  (ctcNo != null && !ctcNo.equals("CC")) ? ctcNo : "";
        this.ctcDate = (ctcDate != null && !ctcDate.equalsIgnoreCase("null")) ? ctcDate : "";
        this.ctcPlace =ctcPlace;
        this.requestId = requestId;
        BarangayStaff captain = new StaffDAO().findStaffByPosition("Captain");
        String cap =  captain.getFirstName() + " " + captain.getMiddleName() + " "+ captain.getLastName();
        this.captainName = (cap != null && !cap.isEmpty()) ? cap : "HON. Robert E. Palencia";
    }
    private static SystemConfigDAO dao = new SystemConfigDAO();

    private final String LOGO_PATH = dao.getLogoPath();
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int width = (int) pf.getImageableWidth();
        int height = (int) pf.getImageableHeight();
        int y = 50;
        int pageWidth = (int) pf.getImageableWidth();
        // ==========================================================
        // 1. HEADER (Same as Barangay Clearance)
        // ==========================================================
        Image logo = null;
        try {
            logo = new ImageIcon(LOGO_PATH).getImage();
        } catch (Exception e) {
            System.err.println("Logo not found: " + e.getMessage());
        }

        if (logo != null) {
            // Resize logo to be uniform
            int logoSize = 90;

            // Draw LEFT Logo
            g2d.drawImage(logo, 40, 30, logoSize, logoSize, null);

            // Draw RIGHT Logo
            // Calculation: Page Width - Right Margin (40) - Logo Width (90)
            int rightLogoX = pageWidth - 40 - logoSize;
            logo = new ImageIcon(dao.getDaetLogoPath()).getImage();
            g2d.drawImage(logo, rightLogoX, 30, logoSize, logoSize, null);
        }

        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, "Republic of the Philippines", FONT_HEADER, width / 2, y); y += 15;
        drawCenteredText(g2d, "Province of Camarines Norte", FONT_HEADER, width / 2, y); y += 15;
        drawCenteredText(g2d, "Municipality of Daet", FONT_HEADER, width / 2, y); y += 20;
        String brgyName = new SystemConfigDAO().getConfig("barangay_name");
        drawCenteredText(g2d, brgyName, new Font("Arial", Font.BOLD, 16), width / 2, y); y += 30;
        drawCenteredText(g2d, "OFFICE OF THE PUNONG BARANGAY", FONT_OFFICE, width / 2, y); y += 10;

        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(40, y, width - 40, y);
        y += 50;

        // ==========================================================
        // 2. TITLE
        // ==========================================================
        drawCenteredText(g2d, "BARANGAY BUSINESS CLEARANCE", FONT_TITLE, width / 2, y);
        y += 60;

        // ==========================================================
        // 3. DETAILS (Full Width, Centered Focus)
        // ==========================================================
        int labelX = 70;
        int valueX = 220;
        int lineLength = width - valueX - 70;

        g2d.setFont(FONT_LABEL);
        g2d.drawString("TO WHOM IT MAY CONCERN:", labelX, y);
        y += 30;

        g2d.setFont(FONT_BODY);
        String intro = "      This is to certify that specific clearance is hereby granted to the " +
                "following business entity / proprietor for the operation of their business within this Barangay:";

        y = drawParagraph(g2d, intro, labelX, y, width - 140, 15);
        y += 30;

        // BUSINESS FIELDS
        // If your 'businessDetails' string is messy, we display it as is,
        // or you can parse it if you separated it with "|"
        y = drawField(g2d, "PROPRIETOR / OWNER:", ownerName.toUpperCase(), labelX, valueX, y, lineLength);
        y = drawField(g2d, "ADDRESS:", address, labelX, valueX, y, lineLength);

        // Cleaning up the purpose/business string for display
        String ownership = "";
        String nature = "";
        String location = "";

// 1. SPLIT OWNERSHIP (Look for " - ")
        if (businessDetails.contains(" - ")) {
            String[] parts1 = businessDetails.split(" - ", 2); // Split into max 2 parts
            ownership = parts1[0].trim();

            // The rest of the string: "Sari-Sari Store located at Purok 1..."
            String remaining = parts1[1];

            // 2. SPLIT NATURE & LOCATION (Look for " located at ")
            if (remaining.contains(" located at ")) {
                String[] parts2 = remaining.split(" located at ", 2);
                nature = parts2[0].trim();
                location = parts2[1].replace(".", "").trim(); // Remove trailing dot
            } else {
                // Fallback if "located at" is missing
                nature = remaining;
            }
        } else {
            // Fallback if the format is completely different
            nature = businessDetails;
        }

        y = drawField(g2d, "BUSINESS NATURE / NAME:", nature.toUpperCase(), labelX, valueX, y, lineLength);
        y += 10;
        y = drawField(g2d, "OWNERSHIP TYPE:", ownership.toUpperCase(), labelX, valueX, y, lineLength);
        y += 10;

        y = drawField(g2d, "LOCATION:", location.toUpperCase(), labelX, valueX, y, lineLength);
        y += 10;
        g2d.setFont(FONT_BODY);
        String body = "      This clearance is issued upon the request of the subject for the application " +
                "or renewal of Mayor's Permit / Business License.\n\n" +
                "      The above-mentioned business has complied with the existing Barangay Ordinances, " +
                "Rules, and Regulations relating to its operation.";

        y = drawParagraph(g2d, body, labelX, y, width - 140, 16);
        y += 170;

        // Validity Note
        g2d.setFont(new Font("Arial", Font.ITALIC, 10));
        drawCenteredText(g2d, "This clearance is valid for one (1) year from the date of issuance unless revoked.", width/2, y);
        y += 40;

        // ==========================================================
        // 5. FOOTER
        // ==========================================================
        int footerY = height - 200;

        // Left: CTC Details
        int ctcX = labelX;
        g2d.setFont(FONT_BODY);
        int addSpace = 20;
        g2d.drawString("CTC No.   : " + (ctcNo != null ? ctcNo : "_________"), ctcX, footerY+addSpace);
        g2d.drawString("Issued On : " + (ctcDate != null ? ctcDate : "_________"), ctcX, footerY + 15+addSpace);
        g2d.drawString("Issued At : " + (ctcPlace != null ? ctcPlace : "_________"), ctcX, footerY + 30+addSpace);
        Payment documentData = new ResidentDAO().findResidentReceiptById(requestId);
        String docu = documentData.getOrNumber();
        g2d.drawString("OR No.    : " + (docu != null ? docu:"_________ (See Receipt)"), ctcX, footerY + 45+addSpace);

        // Right: Signatory
        int sigX = width - 200;
        int sigY = footerY + 50;

        g2d.setFont(FONT_LABEL);
        drawCenteredText(g2d, "Approved by:", FONT_BODY, sigX + 70, sigY - 30);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        drawCenteredText(g2d, captainName.toUpperCase(), new Font("Arial", Font.BOLD, 14), sigX + 70, sigY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        drawCenteredText(g2d, "Punong Barangay", new Font("Arial", Font.PLAIN, 12), sigX + 70, sigY + 15);

        // Seal Box (Optional)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawOval(sigX + 20, sigY - 20, 100, 100);
        g2d.drawString("Barangay Seal", sigX + 35, sigY + 35);

        return PAGE_EXISTS;
    }

    // --- HELPERS (Same as BarangayClearancePrinter) ---

    private int drawField(Graphics2D g, String label, String value, int labelX, int valueX, int y, int lineLen) {
        g.setFont(FONT_LABEL);
        g.drawString(label, labelX, y);
        g.setFont(FONT_VALUE);
        if(value != null) g.drawString(value, valueX, y);
        g.setStroke(new BasicStroke(0.5f));
        g.drawLine(valueX, y + 2, valueX + lineLen, y + 2);
        return y + 25;
    }

    private void drawCenteredText(Graphics2D g, String text, Font font, int centerX, int y) {
        g.setFont(font);
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - (w / 2), y);
    }

    private void drawCenteredText(Graphics2D g, String text, int centerX, int y) {
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - (w / 2), y);
    }

    private int drawParagraph(Graphics2D g, String text, int x, int y, int width, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (word.contains("\n")) {
                String[] split = word.split("\n");
                line.append(split[0]);
                g.drawString(line.toString(), x, y);
                y += lineHeight;
                line = new StringBuilder(split.length > 1 ? split[1] + " " : "");
                if(word.contains("\n\n")) y += lineHeight;
            } else {
                if (fm.stringWidth(line + word) < width) {
                    line.append(word).append(" ");
                } else {
                    g.drawString(line.toString(), x, y);
                    y += lineHeight;
                    line = new StringBuilder(word).append(" ");
                }
            }
        }
        g.drawString(line.toString(), x, y);
        return y + lineHeight;
    }
}