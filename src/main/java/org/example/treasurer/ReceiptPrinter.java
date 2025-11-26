package org.example.treasurer;

import java.awt.*;
import java.awt.print.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiptPrinter implements Printable {

    private String residentName;
    private String docType;
    private String amount;
    private String orNumber;
    private String cashierName;

    public ReceiptPrinter(String name, String doc, String amt, String or, String cashier) {
        this.residentName = name;
        this.docType = doc;
        this.amount = amt;
        this.orNumber = or;
        this.cashierName = cashier;
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) return NO_SUCH_PAGE; // We only have one page

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        // --- STYLE SETTINGS ---
        int y = 20; // Start vertical position
        int startX = 20;
        int pageWidth = (int) pf.getImageableWidth();
        int lineSpacing = 15;

        Font titleFont = new Font("Monospaced", Font.BOLD, 16);
        Font headerFont = new Font("Monospaced", Font.BOLD, 12);
        Font normalFont = new Font("Monospaced", Font.PLAIN, 10);

        // --- 1. HEADER ---
        g2d.setFont(titleFont);
        centerText(g2d, "BARANGAY ALAWIHAO", pageWidth, y);
        y += 20;

        g2d.setFont(normalFont);
        centerText(g2d, "Daet, Camarines Norte", pageWidth, y);
        y += 15;
        centerText(g2d, "OFFICIAL RECEIPT", pageWidth, y);
        y += 30;

        // --- 2. TRANSACTION DETAILS ---
        g2d.setFont(normalFont);
        String date = new SimpleDateFormat("MM/dd/yyyy hh:mm a").format(new Date());

        g2d.drawString("Date: " + date, startX, y); y += lineSpacing;
        g2d.drawString("OR No.: " + orNumber, startX, y); y += lineSpacing * 2;

        // --- 3. BILLING INFO ---
        g2d.drawString("Received from:", startX, y); y += lineSpacing;
        g2d.setFont(headerFont);
        g2d.drawString("  " + residentName, startX, y); y += lineSpacing * 2;

        g2d.setFont(normalFont);
        g2d.drawString("In payment of:", startX, y); y += lineSpacing;
        g2d.drawString("  " + docType, startX, y); y += lineSpacing * 2;

        // --- 4. AMOUNT ---
        g2d.drawLine(startX, y, pageWidth - 20, y); // Line separator
        y += 20;

        g2d.setFont(titleFont);
        g2d.drawString("TOTAL PAID:", startX, y);

        // Align amount to right
        String amtText = "P " + amount;
        int amtWidth = g2d.getFontMetrics().stringWidth(amtText);
        g2d.drawString(amtText, pageWidth - amtWidth - 20, y);
        y += 20;

        g2d.drawLine(startX, y, pageWidth - 20, y); // Line separator
        y += 40;

        // --- 5. FOOTER / SIGNATORY ---
        g2d.setFont(normalFont);
        g2d.drawString("Cashier:", startX, y); y += 25;
        g2d.setFont(headerFont);
        g2d.drawString(cashierName.toUpperCase(), startX, y);
        g2d.drawLine(startX, y + 2, startX + 150, y + 2); // Signature line
        y += lineSpacing;
        g2d.setFont(normalFont);
        g2d.drawString("Barangay Treasurer", startX, y); y += 40;

        centerText(g2d, "*** Thank You! ***", pageWidth, y);

        return PAGE_EXISTS;
    }

    // Helper to center text
    private void centerText(Graphics2D g, String text, int width, int y) {
        int textWidth = g.getFontMetrics().stringWidth(text);
        int x = (width - textWidth) / 2;
        g.drawString(text, x, y);
    }
}