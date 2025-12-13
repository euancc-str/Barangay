package org.example.Interface;

import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.StaffDAO;
import org.example.Users.BarangayStaff;

import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;

public class DocumentPrinter implements Printable {

    private final String residentName;
    private final String docType;
    private final String purpose;
    private final String captainName;
    private final String purok;
    private final int residentAge;

    // Logo Configuration
    private static SystemConfigDAO dao = new SystemConfigDAO();
    String logoPath = dao.getConfig("logoPath");
    private final String LOGO_PATH = dao.getLogoPath(); // Ensure this folder exists

    public DocumentPrinter(String residentName, String docType, String purpose, String purok, int residentAge) {
        this.residentName = residentName;
        this.docType = docType;
        this.purpose = purpose;
        this.purok = purok;
        this.residentAge = residentAge;

        BarangayStaff captain = new StaffDAO().findStaffByPosition("Brgy.Captain");
        String cap = (captain != null) ? captain.getFirstName() + " " + captain.getMiddleName() + " " + captain.getLastName() : "";
        this.captainName = (!cap.isEmpty()) ? cap : "HON. Robert E. Palencia";
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;

        // 1. Set Coordinate System
        // This makes (0,0) start at the printable area, not the physical paper edge
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // 2. Prepare Dimensions
        int pageWidth = (int) pageFormat.getImageableWidth();
        int centerX = pageWidth / 2;
        int y = 50; // Starting vertical position

        // ---------------------------------------------------------
        // LOGO LOGIC (LEFT & RIGHT)
        // ---------------------------------------------------------
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
        // ---------------------------------------------------------

        // 3. HEADER TEXT
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Serif", Font.PLAIN, 12));
        drawCenteredText(g2d, "Republic of the Philippines", new Font("Serif", Font.PLAIN, 12), centerX, y); y += 15;
        drawCenteredText(g2d, "Province of Camarines Norte", new Font("Serif", Font.PLAIN, 12), centerX, y); y += 15;
        drawCenteredText(g2d, "Municipality of Daet", new Font("Serif", Font.PLAIN, 12), centerX, y); y += 15;

        g2d.setFont(new Font("Serif", Font.BOLD, 16));
        drawCenteredText(g2d, "BARANGAY ALAWIHAO", new Font("Serif", Font.BOLD, 16), centerX, y); y += 20;

        g2d.setFont(new Font("Serif", Font.BOLD, 24));
        drawCenteredText(g2d, "OFFICE OF THE PUNONG BARANGAY", new Font("Serif", Font.BOLD, 14), centerX, y); y += 20;
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(50, y, 600 - 50, y); // Horizontal Line
        y += 40;
        // 4. DOCUMENT TITLE
        g2d.setFont(new Font("Serif", Font.BOLD, 28));
        g2d.setColor(new Color(0, 51, 102)); // Dark Blue official look
        drawCenteredText(g2d, docType.toUpperCase(), new Font("Serif", Font.BOLD, 28), centerX, y); y += 60;
        g2d.setColor(Color.BLACK);

        // 5. BODY TEXT (Paragraphs)
        String dateStr = new SimpleDateFormat("MMMM dd, yyyy").format(new Date());

        // Logic for Indigency vs Clearance Text
        String ageStatus = (residentAge >= 18) ? "of legal age" : "a minor";
        String data = "Income:";
        String cleanPurpose ="";
        int s = 0;
        int amount =0;
        if(purpose.contains(data)){
            s = purpose.indexOf(data);
            cleanPurpose = purpose.substring(0,s);
            try {
            amount = Integer.parseInt(purpose.substring(s + data.length()).trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: The income part is not a valid number.");
                amount = 0;
            }
        } else{
            cleanPurpose = purpose;
        }
        String amountDataStore = amount > 0? "with annual income of("+amount+").":"and has no fixed source of income.";
        String p1 = "TO WHOM IT MAY CONCERN:";

        String p2 = "This is to certify that " + residentName.toUpperCase() + ", " + ageStatus + ", " +
                "is a bonafide resident of " + purok.toUpperCase() + ", Brgy. Alawihao, Daet, Camarines Norte.";

        String p3;

        if (docType.equalsIgnoreCase("Certificate of Indigency")) {
            p3 = "Further certifies that the above-named person belongs to the indigent families of this Barangay "+amountDataStore+"\n\n" +
                    "This certification is issued upon request for the purpose of: " + cleanPurpose.toUpperCase() + ".";
        } else {
            p3 = "This certification is issued upon the request of the above-named person for the purpose of: " + purpose.toUpperCase() + ".";
        }

        String p4 = "Given this " + dateStr + " at Barangay Alawihao, Daet, Camarines Norte.";

        // Draw Paragraphs
        int leftMargin = 50;
        int rightMargin = pageWidth - 50;

        g2d.setFont(new Font("Serif", Font.BOLD, 14));
        g2d.drawString(p1, leftMargin, y); y += 30;

        g2d.setFont(new Font("Serif", Font.PLAIN, 14));
        y = drawParagraph(g2d, p2, leftMargin, rightMargin, y); y += 20;
        y = drawParagraph(g2d, p3, leftMargin, rightMargin, y); y += 30;
        y = drawParagraph(g2d, p4, leftMargin, rightMargin, y); y += 80;

        // 6. SIGNATURE
        int sigX = pageWidth - 150;

        g2d.setFont(new Font("Serif", Font.BOLD, 14));
        drawCenteredText(g2d, captainName.toUpperCase(), new Font("Serif", Font.BOLD, 16), sigX, y); y += 15;
        drawCenteredText(g2d, "Punong Barangay", new Font("Serif", Font.PLAIN, 14), sigX, y); y += 80;
        g2d.setFont(new Font("Serif", Font.PLAIN, 12));
        drawCenteredText(g2d, "\"Not valid without", new Font("Serif", Font.BOLD, 9), sigX, y); y+= 10;
        drawCenteredText(g2d, "barangay dry seal\"", new Font("Serif", Font.BOLD, 9), sigX, y);

        return PAGE_EXISTS;
    }

    // --- HELPER METHODS ---

    private void drawCenteredText(Graphics2D g, String text, Font font, int centerX, int y) {
        g.setFont(font);
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - (textWidth / 2), y);
    }

    private int drawParagraph(Graphics2D g, String text, int left, int right, int y) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int width = right - left;

        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            String[] words = para.split(" ");
            StringBuilder line = new StringBuilder("     "); // Indent first line

            for (String word : words) {
                if (fm.stringWidth(line + word) < width) {
                    line.append(word).append(" ");
                } else {
                    g.drawString(line.toString(), left, y);
                    y += lineHeight;
                    line = new StringBuilder(word).append(" ");
                }
            }
            g.drawString(line.toString(), left, y);
            y += lineHeight;
        }
        return y;
    }
}