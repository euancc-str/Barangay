package org.example.treasurer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ResidencyCertificate extends JDialog {

    public ResidencyCertificate(Frame owner, String name, String age, String status, String citizenship, String address, String purpose) {
        super(owner, "Print Preview - Certificate of Residency", true);
        setSize(650, 800);
        setLayout(new BorderLayout());
        setLocationRelativeTo(owner);

        // --- THE PAPER ---
        JPanel paper = new JPanel();
        paper.setLayout(new BoxLayout(paper, BoxLayout.Y_AXIS));
        paper.setBackground(Color.WHITE);
        paper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(50, 60, 50, 60) // Official Margins
        ));

        // 1. HEADER
        JLabel h1 = centerLabel("REPUBLIC OF THE PHILIPPINES");
        h1.setFont(new Font("Serif", Font.PLAIN, 12));
        JLabel h2 = centerLabel("Province of Camarines Norte");
        h2.setFont(new Font("Serif", Font.PLAIN, 12));
        JLabel h3 = centerLabel("Municipality of Daet");
        h3.setFont(new Font("Serif", Font.PLAIN, 12));
        JLabel h4 = centerLabel("BARANGAY ALAWIHAO");
        h4.setFont(new Font("Serif", Font.BOLD, 14));

        // 2. OFFICE TITLE
        JLabel off = centerLabel("OFFICE OF THE PUNONG BARANGAY");
        off.setFont(new Font("SansSerif", Font.BOLD, 16));
        off.setBorder(new EmptyBorder(20, 0, 0, 0));

        // 3. DOCUMENT TITLE
        JLabel title = centerLabel("CERTIFICATE OF RESIDENCY");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setBorder(new EmptyBorder(30, 0, 30, 0));
        // Underline the title visually (optional)
        title.setText("<html><u>CERTIFICATE OF RESIDENCY</u></html>");

        // 4. BODY TEXT (The Legal Part)
        JTextArea body = new JTextArea();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        body.setText(
                "\nTO WHOM IT MAY CONCERN:\n\n" +
                        "      THIS IS TO CERTIFY that " + name.toUpperCase() + ", " + age + " years old, " +
                        status + ", " + citizenship + ", is a bonafide resident of " + address + ", " +
                        "Barangay Alawihao, Daet, Camarines Norte.\n\n" +
                        "      THIS IS TO CERTIFY FURTHER that he/she is a law-abiding citizen of this " +
                        "community and has no derogatory record on file as of this date.\n\n" +
                        "      This certification is issued upon the request of the interested party for " +
                        "the requirement of: " + purpose.toUpperCase() + ".\n\n" +
                        "      ISSUED this " + dateStr + " at Barangay Alawihao, Daet, Camarines Norte."
        );

        body.setFont(new Font("Serif", Font.PLAIN, 14));
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setOpaque(false); // Transparent to match paper
        body.setBorder(new EmptyBorder(0, 10, 0, 10));

        // 5. SIGNATORY
        JPanel sigPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sigPanel.setBackground(Color.WHITE);
        sigPanel.setBorder(new EmptyBorder(60, 0, 0, 0));

        JLabel sigName = new JLabel("<html><center><b>HON. CARDO DALISAY</b><br>Punong Barangay</center></html>");
        sigName.setFont(new Font("Serif", Font.PLAIN, 14));
        sigPanel.add(sigName);

        // Add to Paper
        paper.add(h1); paper.add(h2); paper.add(h3); paper.add(h4);
        paper.add(new JSeparator()); // Line under header
        paper.add(off);
        paper.add(title);
        paper.add(body);
        paper.add(sigPanel);

        // --- PRINT BUTTON ---
        JButton btnPrint = new JButton("Print Document");
        btnPrint.setBackground(new Color(40, 40, 40));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Arial", Font.BOLD, 14));
        btnPrint.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Residency - " + name);
            job.setPrintable((pg, pf, pageNum) -> {
                if (pageNum > 0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) pg;
                g2.translate(pf.getImageableX(), pf.getImageableY());
                g2.scale(0.85, 0.85); // Fit to page
                paper.printAll(g2);
                return Printable.PAGE_EXISTS;
            });
            if(job.printDialog()) {
                try { job.print(); dispose(); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        add(new JScrollPane(paper), BorderLayout.CENTER);
        add(btnPrint, BorderLayout.SOUTH);
    }

    private JLabel centerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
}