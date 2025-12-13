package org.example.SerbisyongBarangay;

import org.example.utils.ResourceUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class CheckDocumentStatusFrame extends JFrame {
    protected String S_dateofpickup = "September 19, 2025";
    protected String S_paymentdate = "September 12, 2025 11:10pm";
    protected String S_transactiondate = "September 12, 2025";
    protected String S_status = "Paid (Waiting for approval)";
    protected String S_doctype = "Barangay Clearance";

    protected JButton backButton;
    protected String referenceNumber = "AW43SD09SD";

    CheckDocumentStatusFrame() {
        setTitle("Serbisyong Barangay");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 750);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        JPanel header = headerPanel();

        JPanel mainPanel = mainPanel();

        add(header, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
      //  setVisible(true);
    }

    private JPanel mainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel innerpanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        innerpanel.setBackground(Color.white);
        innerpanel.setPreferredSize(new Dimension(1210, 500));
        innerpanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        // innerpanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        JPanel tablepanel = new JPanel(new BorderLayout());
        tablepanel.setPreferredSize(new Dimension(950, 500));
        tablepanel.setBackground(Color.white);
        tablepanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        JPanel toppanel = new JPanel();
        toppanel.setLayout(new BorderLayout());
        toppanel.setPreferredSize(new Dimension(toppanel.getWidth(), 80));
        // toppanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        toppanel.setBorder(new EmptyBorder(10, 100, 10, 200));
        toppanel.setBackground(new Color(226, 206, 152));

        JLabel rlabel = new JLabel("Reference Number");
        rlabel.setFont(new Font("Arial", Font.BOLD, 18));
        rlabel.setForeground(Color.black);

        JLabel dlabel = new JLabel("Document Status");
        dlabel.setFont(new Font("Arial", Font.BOLD, 18));
        dlabel.setForeground(Color.black);

        toppanel.add(rlabel, BorderLayout.WEST);
        toppanel.add(dlabel, BorderLayout.EAST);

        tablepanel.add(toppanel, BorderLayout.NORTH);

        JPanel midpanel = new JPanel();
        midpanel.setBackground(Color.yellow);
        midpanel.setLayout(new BorderLayout());
        midpanel.setBackground(Color.white);

        JPanel leftmpanel = new JPanel(new BorderLayout());
        leftmpanel.setBackground(Color.white);
        leftmpanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        leftmpanel.setPreferredSize(new Dimension(350, 200));

        JLabel refnum = new JLabel();
        refnum.setForeground(new Color(172, 0, 0));
        refnum.setText(referenceNumber);
        refnum.setFont(new Font("Arial", Font.BOLD, 25));
        refnum.setHorizontalAlignment(JLabel.CENTER);
        leftmpanel.add(refnum, BorderLayout.CENTER);

        midpanel.add(leftmpanel, BorderLayout.WEST);

        GridBagConstraints gbc = new GridBagConstraints();
        JPanel rightmpanel = new JPanel(new GridBagLayout());
        rightmpanel.setBackground(Color.white);
        // rightmpanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.weightx = 1;

        JLabel docstatus = new JLabel("Status: ");
        docstatus.setForeground(Color.black);
        docstatus.setHorizontalAlignment(JLabel.CENTER);
        docstatus.setFont(new Font("Arial", Font.PLAIN, 20));
        docstatus.setText("Status: " + S_status);

        JPanel docpanel = new JPanel(new BorderLayout());
        docpanel.setBackground(Color.white);
        docpanel.setPreferredSize(new Dimension(docpanel.getWidth(), 70));
        docpanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        docpanel.add(docstatus);

        rightmpanel.add(docpanel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 1;

        JLabel doctype = new JLabel("Document: ");
        doctype.setForeground(Color.black);
        doctype.setHorizontalAlignment(JLabel.CENTER);
        doctype.setFont(new Font("Arial", Font.PLAIN, 20));
        doctype.setText("Document: " + S_doctype);

        JPanel doctypep = new JPanel(new BorderLayout());
        doctypep.setPreferredSize(new Dimension(docpanel.getWidth(), 70));
        doctypep.setBackground(Color.white);
        doctypep.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        doctypep.add(doctype);

        rightmpanel.add(doctypep, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 2;

        JLabel transactlabel = new JLabel("Transaction Date: ");
        transactlabel.setForeground(Color.black);
        transactlabel.setHorizontalAlignment(JLabel.CENTER);
        transactlabel.setFont(new Font("Arial", Font.PLAIN, 20));
        transactlabel.setText("Transaction Date: " + S_transactiondate);

        JPanel transactpanel = new JPanel(new BorderLayout());
        transactpanel.setPreferredSize(new Dimension(transactpanel.getWidth(), 70));
        transactpanel.setBackground(Color.white);
        transactpanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        transactpanel.add(transactlabel);

        rightmpanel.add(transactpanel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 3;

        JLabel paymentdatelabel = new JLabel("Payment Date: ");
        paymentdatelabel.setForeground(Color.black);
        paymentdatelabel.setHorizontalAlignment(JLabel.CENTER);
        paymentdatelabel.setFont(new Font("Arial", Font.PLAIN, 20));
        paymentdatelabel.setText("Payment Date: " + S_paymentdate);

        JPanel paymentdatepanel = new JPanel(new BorderLayout());
        paymentdatepanel.setBackground(Color.white);
        paymentdatepanel.setPreferredSize(new Dimension(paymentdatepanel.getWidth(), 70));
        paymentdatepanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        paymentdatepanel.add(paymentdatelabel);

        rightmpanel.add(paymentdatepanel, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 4;

        JLabel dateofpicuplabel = new JLabel("Date of pick up: ");
        dateofpicuplabel.setForeground(Color.black);
        dateofpicuplabel.setHorizontalAlignment(JLabel.CENTER);
        dateofpicuplabel.setFont(new Font("Arial", Font.PLAIN, 20));
        dateofpicuplabel.setText("Date of pick up: " + S_dateofpickup);

        JPanel dateofpickuppanel = new JPanel(new BorderLayout());
        dateofpickuppanel.setBackground(Color.white);
        dateofpickuppanel.setPreferredSize(new Dimension(dateofpickuppanel.getWidth(), 70));
        dateofpickuppanel.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        dateofpickuppanel.add(dateofpicuplabel);

        rightmpanel.add(dateofpickuppanel, gbc);

        // rightmpanel.setBackground(Color.blue);
        // rightmpanel.setPreferredSize(new Dimension(50,50));

        midpanel.add(rightmpanel, BorderLayout.CENTER);

        tablepanel.add(midpanel, BorderLayout.CENTER);

        innerpanel.add(tablepanel);

        panel.add(innerpanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel headerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(1280, 80));
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));
        panel.setBackground(Color.black);

        JLabel logo = new JLabel();
        Image logoimg = new ImageIcon(ResourceUtils.getResourceAsBytes("serbisyongBarangayLogo.jpg")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        logo.setIcon(new ImageIcon(logoimg));

        JPanel leftpanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 10));
        leftpanel.setOpaque(false);
        leftpanel.setBackground(Color.white);

        JLabel title = new JLabel("Serbisyong Barangay");
        title.setForeground(Color.white);
        title.setFont(new Font("Arial", Font.PLAIN, 30));

        leftpanel.add(logo);
        leftpanel.add(title);

        JPanel rightpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        rightpanel.setBackground(Color.black);

        backButton = new JButton("\u2B05");
        backButton.setFont(new Font("Inter", Font.BOLD, 30));
        backButton.setBackground(Color.black);
        backButton.setFocusPainted(false);
        backButton.setFocusable(false);
        backButton.setForeground(Color.white);
        backButton.setPreferredSize(new Dimension(50, 50));
        backButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5, true));

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new userMain();
                dispose();
              
            }
        });

        rightpanel.add(backButton);

        panel.add(leftpanel, BorderLayout.WEST);
        panel.add(rightpanel, BorderLayout.EAST);

        return panel;
    }
}
