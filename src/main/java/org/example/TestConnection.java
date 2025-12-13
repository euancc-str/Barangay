package org.example;

import org.example.treasurer.ReceiptPrinter;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn != null) {
            System.out.println("🎉 Connection test successful!");
        } else {
            System.out.println("⚠️ Connection test failed.");
        }
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
}
