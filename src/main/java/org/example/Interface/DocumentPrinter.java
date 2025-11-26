package org.example.Interface; // Change to your package

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

    // Adjust this path to your actual logo
    private final String LOGO_PATH = "src/main/java/org/example/resources/logo.jpg";

    public DocumentPrinter(String residentName, String docType, String purpose, String captainName) {
        this.residentName = residentName;
        this.docType = docType;
        this.purpose = purpose;
        this.captainName = captainName;
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        // --- CONFIGURATION ---
        int width = (int) pf.getImageableWidth();
        int centerX = width / 2;
        int y = 50; // Start Y position

        // --- 1. HEADER ---
        // Try to draw logo (Left side)
        try {
            ImageIcon logo = new ImageIcon(LOGO_PATH);
            g2d.drawImage(logo.getImage(), 60, 30, 80, 80, null);
        } catch (Exception e) {
            // If no logo found, just ignore
        }

        // Header Text
        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, "Republic of the Philippines", new Font("Serif", Font.PLAIN, 12), centerX, y); y += 15;
        drawCenteredText(g2d, "Province of Camarines Norte", new Font("Serif", Font.PLAIN, 12), centerX, y); y += 15;
        drawCenteredText(g2d, "Municipality of Daet", new Font("Serif", Font.PLAIN, 12), centerX, y); y += 20;
        drawCenteredText(g2d, "BARANGAY ALAWIHAO", new Font("Serif", Font.BOLD, 16), centerX, y); y += 20;

        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(50, y, width - 50, y); // Horizontal Line
        y += 40;

        // --- 2. TITLE ---
        drawCenteredText(g2d, "OFFICE OF THE PUNONG BARANGAY", new Font("SansSerif", Font.BOLD, 14), centerX, y); y += 40;
        drawCenteredText(g2d, docType.toUpperCase(), new Font("Serif", Font.BOLD, 28), centerX, y); y += 60;

        // --- 3. BODY ---
        Font bodyFont = new Font("Serif", Font.PLAIN, 14);
        g2d.setFont(bodyFont);
        int leftMargin = 70;
        int rightMargin = width - 70;

        String dateStr = new SimpleDateFormat("MMMM dd, yyyy").format(new Date());

        // Dynamic Text Construction
        String paragraph1 = "TO WHOM IT MAY CONCERN:";
        String paragraph2 = "This is to certify that " + residentName.toUpperCase() + ", of legal age, " +
                "is a bonafide resident of Barangay Alawihao, Daet, Camarines Norte.";
        String paragraph3 = "This certification is issued upon the request of the above-named person " +
                "for the purpose of: " + purpose.toUpperCase() + ".";
        String paragraph4 = "Issued this " + dateStr + " at Barangay Alawihao, Daet, Camarines Norte.";

        g2d.drawString(paragraph1, leftMargin, y); y += 30;
        y = drawParagraph(g2d, paragraph2, leftMargin, rightMargin, y); y += 20;
        y = drawParagraph(g2d, paragraph3, leftMargin, rightMargin, y); y += 20;
        y = drawParagraph(g2d, paragraph4, leftMargin, rightMargin, y); y += 80;

        // --- 4. SIGNATORY ---
        int sigX = width - 200;
        g2d.drawString("Approved by:", sigX, y); y += 50;

        g2d.setFont(new Font("Serif", Font.BOLD, 14));
        drawCenteredText(g2d, captainName.toUpperCase(), new Font("Serif", Font.BOLD, 14), sigX + 40, y); y += 15;

        g2d.setFont(new Font("Serif", Font.PLAIN, 12));
        drawCenteredText(g2d, "Punong Barangay", new Font("Serif", Font.PLAIN, 12), sigX + 40, y);

        // --- 5. THUMB & SEAL (Optional Visuals) ---
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(50, y, 100, 100); // Box for Thumbprint
        g2d.drawString("Thumbmark", 65, y + 55);

        return PAGE_EXISTS;
    }

    // --- HELPERS ---

    private void drawCenteredText(Graphics2D g, String text, Font font, int centerX, int y) {
        g.setFont(font);
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - (textWidth / 2), y);
    }

    // Draws a paragraph that wraps automatically
    private int drawParagraph(Graphics2D g, String text, int left, int right, int y) {
        FontMetrics fm = g.getFontMetrics();
        int lineHeight = fm.getHeight();
        int curX = left;
        int width = right - left;

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (fm.stringWidth(line + word) < width) {
                line.append(word).append(" ");
            } else {
                g.drawString(line.toString(), left, y);
                y += lineHeight;
                line = new StringBuilder(word).append(" ");
            }
        }
        g.drawString(line.toString(), left, y); // Draw last line
        return y + lineHeight;
    }
}