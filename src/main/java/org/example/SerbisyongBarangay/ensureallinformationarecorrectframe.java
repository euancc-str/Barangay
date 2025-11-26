package org.example.SerbisyongBarangay;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ensureallinformationarecorrectframe extends JDialog {
    protected final Color greencolor = new Color(86, 149, 65);
    protected final Color redcolor = new Color(255, 49, 49);

    public ensureallinformationarecorrectframe(Frame owner) {
        super(owner, "", true); // Modal dialog
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true); // Remove title bar and window decorations

        // --- Main Panel Setup ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        // Thick black border around the dialog
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));

        // --- Close Button (X) ---
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setBackground(Color.WHITE);
        closeButton.setForeground(Color.BLACK);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // No internal border
        closeButton.setFocusPainted(false);
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.addActionListener(e -> dispose());

        JPanel closeButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Top-right padding
        closeButtonWrapper.setOpaque(false);
        closeButtonWrapper.add(closeButton);
        mainPanel.add(closeButtonWrapper, BorderLayout.NORTH);

        // --- Center Content Label ---
        JLabel contentLabel = new JLabel(
                "<html><p align='center'>" +
                        "<b>Please ensure all your<br>Information are correct before <br>clicking proceed</b>"
                        +
                        "</p></html>");
        contentLabel.setFont(new Font("Arial", Font.BOLD, 22));
        contentLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentLabel.setBorder(new EmptyBorder(50, 40, 50, 40)); // Padding around text
        mainPanel.add(contentLabel, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 1 row, 2 columns, 20px gap
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 30, 30, 30)); // Padding at the bottom

        // 1. EDIT DETAILS Button (Orange)
        JButton cancelbutton = new JButton("CANCEL");
        cancelbutton.setFont(new Font("Arial", Font.BOLD, 18));
        cancelbutton.setForeground(Color.WHITE);
        cancelbutton.setBackground(redcolor); // Bright Orange

        // Simple customization for a block look
        cancelbutton.setBorderPainted(false);
        cancelbutton.setFocusPainted(false);
        cancelbutton.setPreferredSize(new Dimension(0, 50));
        cancelbutton.addActionListener(e -> {
            // JOptionPane.showMessageDialog(this, "Editing details...");
            // dispose();
            dispose();
        });

        // 2. LATER Button (Gray)
        JButton proceedbutton = new JButton("PROCEED");
        proceedbutton.setFont(new Font("Arial", Font.BOLD, 18));
        proceedbutton.setForeground(Color.white); // Dark Gray text
        proceedbutton.setBackground(greencolor); // Light Gray background

        // Simple customization for a block look
        proceedbutton.setBorderPainted(false);
        proceedbutton.setFocusPainted(false);
        proceedbutton.setPreferredSize(new Dimension(0, 50));
        proceedbutton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                dispose();
                // requestaDocumentFrame req = new requestaDocumentFrame();
                // req.setVisible(true);

                successyourinformationframe suc = new successyourinformationframe(null);
                suc.setVisible(true);

            }

        });

        buttonPanel.add(cancelbutton);
        buttonPanel.add(proceedbutton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // setVisible(true);
        setContentPane(mainPanel);
        pack(); // Adjusts size to fit components
        setLocationRelativeTo(owner);
    }

    // public static void main(String[] args) {
    // SwingUtilities.invokeLater(() -> {
    // // Create a hidden temporary frame to own the dialog
    // JFrame frame = new JFrame();
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.setVisible(false);

    // nopersonalinfowarningframe dialog = new nopersonalinfowarningframe(frame);
    // dialog.setVisible(true);

    // // Ensures the application exits when the dialog is closed

    // });
    // }
}
