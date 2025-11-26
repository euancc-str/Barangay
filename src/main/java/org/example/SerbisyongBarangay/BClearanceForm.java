package org.example.SerbisyongBarangay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// This is a Java Swing example for a desktop application.
// It uses GridBagLayout for a flexible/resizable (desktop "responsive") layout.

public class BClearanceForm extends JFrame {

    // Constructor to set up the GUI
    public BClearanceForm() {
        // --- Frame Setup ---
        // ================= MAIN FRAME =================
       // setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Serbisyong Barangay");
        setSize(1280, 750);
        setLocationRelativeTo(null);
        // Ensure the main layout is BorderLayout for North/South placement
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(15, 15, 15));

        // Use GridBagLayout for flexible positioning and resizing of the form fields
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Header ---
        JLabel titleLabel = new JLabel("Fill out this form with correct information");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 24));
        JLabel subtitleLabel = new JLabel("Barangay Clearance");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 18));

        // Add Header to the top center
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 5, 0); // Bottom margin
        mainPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 20, 0); // Bottom margin
        mainPanel.add(subtitleLabel, gbc);

        // --- Form Fields ---
        gbc.gridwidth = 1; // Reset to 1 column width
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow components to grow horizontally
        gbc.insets = new Insets(5, 5, 5, 5); // Padding around components

        int row = 2;

        // Name (Full width input)
        row = addField(mainPanel, gbc, row, "Fist Name:", new JTextField(20));
        row = addField(mainPanel, gbc, row, "Middle Name:", new JTextField(20));
        row = addField(mainPanel, gbc, row, "Last Name:", new JTextField(20));
        row = addField(mainPanel, gbc, row, "Suffix:", new JTextField(20));

        // Age (Small input)
        row = addField(mainPanel, gbc, row, "Age:", new JTextField(5));

        // Civil Status (Small input)
        row = addField(mainPanel, gbc, row, "Civil Status:", new JTextField(15));

        // Current Address (Full width)
        row = addField(mainPanel, gbc, row, "Current Address:", new JTextField(20));

        // Purpose (Multi-line area - using JTextArea)
        JLabel purposeLabel = new JLabel("Purpose:");
        JTextArea purposeArea = new JTextArea(4, 20); // 4 rows high
        purposeArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Add Purpose Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(purposeLabel, gbc);

        // Add Purpose Area
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0; // Allow it to grow vertically
        mainPanel.add(new JScrollPane(purposeArea), gbc); // Use JScrollPane for scrolling if needed
        gbc.weighty = 0; // Reset vertical weight
        row++;

        // --- Side Fields (Sex, Birth Date, Date) ---

        // Sex Dropdown
        JLabel sexLabel = new JLabel("Sex:");
        JComboBox<String> sexBox = new JComboBox<>(new String[] { "", "Male", "Female", "Other" });

        // Use a dedicated panel for the right-side elements
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(sexLabel);
        rightPanel.add(sexBox);

        gbc.gridx = 1;
        gbc.gridy = 2; // Position next to 'Name' row
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(rightPanel, gbc);

        // Date fields
        row = addField(mainPanel, gbc, row, "Birth Date:", new JComboBox<>(new String[] { "", "Select Date", "..." }));
        row = addField(mainPanel, gbc, row, "Date:", new JTextField(15));

        // --- BUTTONS PANEL (Added to SOUTH of the JFrame) ---

        // Create a dedicated panel for the buttons, using FlowLayout to center them
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.RED);
        cancelButton.setForeground(Color.white);
        cancelButton.setFocusable(true);
        // Add to buttonPanel
        buttonPanel.add(cancelButton);

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Action: Close the form or return to the previous screen
                int cancel = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to cancel? Your inputs will be lost.", "Confirmation",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (cancel == 0) {
                    dispose();
                }

            }
        });

        JButton proceedButton = new JButton("Proceed");
        proceedButton.setBackground(Color.GREEN);
        proceedButton.setForeground(Color.white);
        // Add to buttonPanel
        buttonPanel.add(proceedButton);

        proceedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int cancel = JOptionPane.showConfirmDialog(null,
                        "Please make sure your information are correct before clicking \"Yes\".", "Confirmation",
                        JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (cancel == 0) {
                   // JOptionPane.showMessageDialog(null, "Mabayad na next");
                   congratulationsyouhavesucreqaformframe cons=new congratulationsyouhavesucreqaformframe(null,"Barangay Clearance");
                   cons.setVisible(true);

                }
            }
        });

        // --- Finalize Frame ---
        // 1. Add the main form content to the center of the JFrame
        add(mainPanel, BorderLayout.CENTER);

        // 2. Add the button panel to the bottom (SOUTH) of the JFrame
        add(buttonPanel, BorderLayout.SOUTH);

        pack(); // Sizes the frame to fit the preferred size of its subcomponents
        setLocationRelativeTo(null); // Center the frame on the screen
        //setVisible(true);
    }

    // Helper method to add a label and component pair to the panel
    private int addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent component) {
        // Label
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);

        // Component (Input field)
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(component, gbc);

        return row + 1;
    }

    public static void main(String[] args) {
        // Run GUI construction on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new BClearanceForm());
    }
}
