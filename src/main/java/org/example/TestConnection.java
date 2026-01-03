package org.example;

import org.example.treasurer.ReceiptPrinter;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

public class TestConnection extends Component {
    public static void main(String[] args) {

        String val = "Purok 10";
        int len = 7;
        if(val.length() == 7){
            String num = String.valueOf(val.charAt(len-1));
            System.out.println(Integer.parseInt(num));
        } else if (val.length() == 8){
            String num = val.substring(len - 1, len + 1);
            System.out.println(Integer.parseInt(num));
        }
        String data = "Income:";
        String cleanPurpose ="";
        String purpose = "scholarship Income:100";
        String cleanAmount ="";
        int s = 0;
        int amount =0;
        if(purpose.contains(data)){
            s = purpose.indexOf(data);
            cleanPurpose = purpose.substring(0,s);
            try {
                cleanAmount = purpose.substring(s + data.length()).trim();
                amount = Integer.parseInt(cleanAmount);
            } catch (NumberFormatException e) {
                System.out.println("Error: The income part is not a valid number.");
                amount = 0;
            }
        } else{
            cleanPurpose = purpose;
        }
        System.out.println(cleanPurpose);
        String amountDataStore = amount > 0? "with annual income of("+amount+")":"and has no fixed source of income.";
        System.out.println(amountDataStore);
        System.out.println(cleanAmount);
      //  TestConnection t = new TestConnection();

       // t.printIncidentReport("1234","Jonaz","TESTTTTTT","To complain","res");
        String trimmer = "Brgy.";
        String data1 = "";
        String staff = "Brgy.Sec";

        if(staff.contains(trimmer)){
            int val1 = trimmer.length();
            int e = staff.length();
            data1 = staff.substring(val1,e);
        }
        System.out.println(data1);

    }


    private static void printReceipt(String residentName, String docType) {
        // 1. Setup the print job
        PrinterJob job = PrinterJob.getPrinterJob();

        // 2. Create your receipt object with real data
        // (You can fetch the fee amount from your DB/DAO if needed)
        String amount = "50.00";
        String orNum = "OR-" + System.currentTimeMillis(); // Generate or fetch
        String cashier = "Hon. Treasurer"; // Or UserDataManager.getCurrentStaff().getName()

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
    private void printIncidentReport(String caseNo, String complainant, String respondent, String narrative, String resolution) {

        // Get the current date for the report
        String currentDate = new java.text.SimpleDateFormat("MMMM dd, yyyy").format(new java.util.Date());

        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append("  body { font-family: 'Times New Roman', serif; font-size: 12px; }");
        html.append("  .container { border: 3px double black; padding: 40px; height: 900px; }"); // The "Document Border"
        html.append("  .header { text-align: center; }");
        html.append("  .title { font-size: 18px; font-weight: bold; text-decoration: underline; margin-top: 20px; }");
        html.append("  .case-info { width: 100%; margin-top: 30px; border-collapse: collapse; }");
        html.append("  .case-info td { vertical-align: top; padding: 5px; }");
        html.append("  .narrative { text-align: justify; line-height: 1.6; margin-top: 20px; }");
        html.append("  .signatures { width: 100%; margin-top: 80px; }");
        html.append("  .signatures td { text-align: center; font-weight: bold; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        // --- MAIN CONTAINER WITH BORDER ---
        html.append("<div class='container'>");

        // --- GOVERNMENT HEADER ---
        html.append("<div class='header'>");
        html.append("Republic of the Philippines<br/>");
        html.append("Province of Camarines Norte<br/>"); // CUSTOMIZE THIS
        html.append("Municipality of Daet<br/>");       // CUSTOMIZE THIS
        html.append("<b>BARANGAY ALAWIHAO</b><br/><br/>"); // CUSTOMIZE THIS
        html.append("OFFICE OF THE LUPONG TAGAPAMAYAPA");
        html.append("</div>");

        html.append("<br/><hr/><br/>");

        // --- CASE DETAILS (Side by Side) ---
        html.append("<table class='case-info'>");
        html.append("<tr>");

        // LEFT SIDE: NAMES
        html.append("<td width='60%'>");
        html.append("<b>").append(complainant.toUpperCase()).append("</b><br/>Complainant<br/><br/>");
        html.append("- against -<br/><br/>");
        html.append("<b>").append(respondent.toUpperCase()).append("</b><br/>Respondent");
        html.append("</td>");

        // RIGHT SIDE: CASE NO
        html.append("<td width='40%' align='right'>");
        html.append("Barangay Case No: <b>").append(caseNo).append("</b><br/>");
        html.append("For: <b>Strict Compliance / Mediation</b><br/>");
        html.append("Date: ").append(currentDate);
        html.append("</td>");

        html.append("</tr>");
        html.append("</table>");

        // --- REPORT TITLE ---
        html.append("<div class='header title'>CERTIFICATE TO FILE ACTION</div>");

        // --- BODY CONTENT ---
        html.append("<div class='narrative'>");
        html.append("<b>TO WHOM IT MAY CONCERN:</b><br/><br/>");
        html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;THIS IS TO CERTIFY that the above-entitled case was filed in this office. ");
        html.append("After subsequent hearings and mediation proceedings, the following resolution was reached:<br/><br/>");
        html.append("<i>\"").append(resolution).append("\"</i><br/><br/>");
        html.append("However, no settlement was reached, and the case is hereby endorsed to the proper court of law.<br/><br/>");
        html.append("<b>INCIDENT NARRATIVE:</b><br/>");
        html.append(narrative);
        html.append("</div>");

        // --- SIGNATORIES (Bottom) ---
        html.append("<table class='signatures'>");
        html.append("<tr>");
        html.append("<td>__________________________<br/>Barangay Secretary</td>");
        html.append("<td>__________________________<br/>Punong Barangay</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append("</div>"); // End Container
        html.append("</body></html>");

        // --- PRINTING ACTION ---
        JTextPane printer = new JTextPane();
        printer.setContentType("text/html");
        printer.setText(html.toString());

        try {
            printer.print(new MessageFormat("CASE NO: " + caseNo), new MessageFormat("Page {0}"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Print Error: " + e.getMessage());
        }
    }
}
