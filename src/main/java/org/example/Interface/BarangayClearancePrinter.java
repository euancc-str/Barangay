package org.example.Interface;

import org.example.Admin.AdminSettings.ImageUtils;
import org.example.Admin.AdminSettings.PhotoDAO;
import org.example.Admin.AdminSettings.SystemConfigDAO;
import org.example.StaffDAO;
import org.example.Users.BarangayStaff;

import java.awt.*;
import java.awt.print.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.ImageIcon;

public class BarangayClearancePrinter implements Printable {

    private String name;
    private String address;
    private String gender;
    private String dob;
    private String age;
    private String civilStatus;
    private String purpose;
    private String ctcNo, ctcDate, ctcPlace;
    private int residentId;
    private String captainName;

    // Fonts
    private final Font FONT_HEADER = new Font("Times New Roman", Font.PLAIN, 12);
    private final Font FONT_OFFICE = new Font("Arial", Font.BOLD, 12);
    private final Font FONT_TITLE = new Font("Arial", Font.BOLD, 24);
    private final Font FONT_LABEL = new Font("Arial", Font.BOLD, 11);
    private final Font FONT_VALUE = new Font("Arial", Font.PLAIN, 12);
    private final Font FONT_BODY = new Font("Times New Roman", Font.PLAIN, 12);

    public BarangayClearancePrinter(String name, String address, String gender, String dob, String age,
                                    String civilStatus, String purpose, String ctcNo, String ctcDate,
                                    String ctcPlace, int residentId) {
        this.name = name;
        this.address = address;
        this.gender = gender;
        this.dob = dob;
        this.age = age;
        this.civilStatus = civilStatus;
        this.purpose = purpose;
        this.ctcNo = ctcNo;
        this.ctcDate = ctcDate;
        this.ctcPlace = ctcPlace;
        this.residentId = residentId;

        BarangayStaff captain = new StaffDAO().findStaffByPosition("Brgy.Captain");
        String cap =  captain.getFirstName() + " " + captain.getMiddleName() + " "+ captain.getLastName();
        this.captainName = (cap != null && !cap.isEmpty()) ? cap: "HON. CARDO DALISAY";
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        int width = (int) pf.getImageableWidth();
        int height = (int) pf.getImageableHeight();
        int y = 50;

        // ==========================================================
        // 1. HEADER
        // ==========================================================
        try {
            String logoPath = new SystemConfigDAO().getConfig("logoPath");
            if (logoPath != null && !logoPath.isEmpty()) {
                ImageIcon logo = new ImageIcon("resident_photos/"+logoPath);
                g2d.drawImage(logo.getImage(), 50, 20, 70, 70, null);
            }
        } catch (Exception e) {}

        g2d.setColor(Color.BLACK);
        drawCenteredText(g2d, "Republic of the Philippines", FONT_HEADER, width / 2, y); y += 15;
        drawCenteredText(g2d, "Province of Camarines Norte", FONT_HEADER, width / 2, y); y += 15;
        drawCenteredText(g2d, "Municipality of Daet", FONT_HEADER, width / 2, y); y += 20;
        drawCenteredText(g2d, "BARANGAY ALAWIHAO", new Font("Arial", Font.BOLD, 16), width / 2, y); y += 30;
        drawCenteredText(g2d, "OFFICE OF THE PUNONG BARANGAY", FONT_OFFICE, width / 2, y); y += 10;

        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(40, y, width - 40, y);
        y += 50;

        // ==========================================================
        // 2. TITLE
        // ==========================================================
        drawCenteredText(g2d, "BARANGAY CLEARANCE", FONT_TITLE, width / 2, y);
        y += 50;

        // ==========================================================
        // 3. PHOTO (Top Right)
        // ==========================================================
        int photoSize = 100;
        int photoX = width - 150; // Right side margin
        int photoY = y; // Align with top of details

        try {
            PhotoDAO photoDao = new PhotoDAO();
            String path = photoDao.getPhotoPath(residentId);
            if (path != null && !path.isEmpty()) {
                String fullPath = new ImageUtils().getImageDir() + path;
                ImageIcon icon = new ImageIcon(fullPath);
                g2d.drawImage(icon.getImage(), photoX, photoY, photoSize, photoSize, null);
            }
            g2d.drawRect(photoX, photoY, photoSize, photoSize);
        } catch (Exception e) {}

        // ==========================================================
        // 4. DETAILS (Left Side)
        // ==========================================================
        int labelX = 50;
        int valueX = 180; // Strict start point for values to avoid overlap
        int lineLength = photoX - valueX - 20; // Stop before the photo

        g2d.setFont(FONT_LABEL);
        g2d.drawString("TO WHOM IT MAY CONCERN:", labelX, y);
        y += 30;

        // Draw Fields (Name, Address, etc.)
        y = drawField(g2d, "NAME:", name.toUpperCase(), labelX, valueX, y, lineLength);
        y = drawField(g2d, "ADDRESS:", address, labelX, valueX, y, lineLength);
        y = drawField(g2d, "DATE OF BIRTH:", dob, labelX, valueX, y, lineLength);
        y = drawField(g2d, "SEX / CIVIL STATUS:", gender + " / " + civilStatus, labelX, valueX, y, lineLength);

        // Purpose (Clean up string)
        String displayPurpose = purpose;
        if (displayPurpose.contains("|")) displayPurpose = displayPurpose.split("\\|")[0].trim();

        // Ensure Y is below photo before starting body text
        y = Math.max(y, photoY + photoSize + 30);

        // ==========================================================
        // 5. BODY TEXT
        // ==========================================================
        g2d.setFont(FONT_BODY);
        String body = "      This is to certify that the person whose name, signature, and thumbprint appear " +
                "hereon has requested a "+purpose+" from this office.\n\n" +
                "      This certification is issued upon request of the subject for the purpose stated above.";

        y = drawParagraph(g2d, body, labelX, y, width - 100, 16);
        y += 50;

        // ==========================================================
        // 6. FOOTER (Thumb, CTC, Captain)
        // ==========================================================

        // Left: Thumb & CTC
        int footerY = y;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(labelX, footerY, 70, 70);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 9));
        g2d.drawString("Right Thumb", labelX + 10, footerY + 85);

        int ctcX = labelX + 90;
        int ctcY = footerY + 45;
        g2d.setFont(FONT_BODY);
        g2d.drawString("CTC No.   : " + (ctcNo != null ? ctcNo : ""), ctcX, ctcY); ctcY += 15;
        g2d.drawString("Issued On : " + (ctcDate != null ? ctcDate : ""), ctcX, ctcY); ctcY += 15;
        g2d.drawString("Issued At : " + (ctcPlace != null ? ctcPlace : ""), ctcX, ctcY);

        // Right: Captain
        int sigX = width - 150;
        int sigY = footerY + 40;

        g2d.setFont(FONT_BODY);
        drawCenteredText(g2d, "Approved by:", FONT_BODY, sigX, sigY - 40);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        drawCenteredText(g2d, captainName.toUpperCase(), new Font("Arial", Font.BOLD, 14), sigX, sigY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        drawCenteredText(g2d, "Punong Barangay", new Font("Arial", Font.PLAIN, 12), sigX, sigY + 15);

        return PAGE_EXISTS;
    }

    // --- HELPERS ---

    private int drawField(Graphics2D g, String label, String value, int labelX, int valueX, int y, int lineLen) {
        g.setFont(FONT_LABEL);
        g.drawString(label, labelX, y);

        g.setFont(FONT_VALUE);
        if (value != null) g.drawString(value, valueX, y);

        g.setStroke(new BasicStroke(0.5f));
        g.drawLine(valueX, y + 2, valueX + lineLen, y + 2); // Underline

        return y + 25;
    }

    private void drawCenteredText(Graphics2D g, String text, Font font, int centerX, int y) {
        g.setFont(font);
        int w = g.getFontMetrics().stringWidth(text);
        g.drawString(text, centerX - (w / 2), y);
    }

    // Overload for simpler calls
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
                if (word.contains("\n\n")) y += lineHeight;
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