package org.example.SerbisyongBarangay;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class CertificateofIndigencyForm {

    public static void main(String[] args) {
        // Ensure GUI runs on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new CertificateofIndigencyForm().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame();

        frame.setUndecorated(true); // Removes the default frame border and title bar
        frame.getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK, 4)); // Add a thick black border to the dialog

        // 1. Create the Main Panel using BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10)); // Vertical gap of 10
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15)); // Padding around the edges

        // --- TOP SECTION (Header and Close Button) ---
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // --- CENTER SECTION (Form Fields) ---
        mainPanel.add(createFormFieldsPanel(), BorderLayout.CENTER);

        // --- BOTTOM SECTION (Buttons) ---
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setVisible(true);
    }
    
// -----------------------------------------------------------------------
// --- SECTION 1: HEADER ---
// -----------------------------------------------------------------------

    private JPanel createHeaderPanel() {
        // Use BorderLayout to position the title in the CENTER and the button in the EAST
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        // Title and Subtitle container
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("Please Fill Up the Form", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JLabel subtitleLabel = new JLabel("Certificate of Residency", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        // Close Button (Using Unicode X for the icon)
        JButton closeButton = new JButton("\u2715"); // Heavy Multiplication X
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setForeground(Color.BLACK);
        closeButton.setBackground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2, true)); // Rounded border
        
        // Action to close the window
        closeButton.addActionListener(e -> {
            // Find the parent JFrame and dispose of it
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(closeButton);
            parentFrame.dispose();
        });

        // Add components to the header panel
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Wrap the button in a FlowLayout panel to prevent it from stretching
        JPanel closeButtonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeButtonWrapper.setBackground(Color.WHITE);
        closeButtonWrapper.add(closeButton);
        headerPanel.add(closeButtonWrapper, BorderLayout.EAST);

        return headerPanel;
    }

// -----------------------------------------------------------------------
// --- SECTION 2: FORM FIELDS ---
// -----------------------------------------------------------------------
    
    private JPanel createFormFieldsPanel() {
        // This panel holds all the input fields and text, using a vertical BoxLayout for stacking
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // Create the top row container for name fields
        JPanel namePanel = new JPanel(new GridLayout(5, 1, 0, 10)); // 5 rows, 1 column, 10 vertical gap
        namePanel.setBackground(Color.WHITE);
        
        // Replicate the name fields structure
        String[] nameLabels = {"Name:", "Last Name:", "Middle Name:", "Suffix:", "Age:"};
        for (String labelText : nameLabels) {
            namePanel.add(createLabeledTextField(labelText, 0)); // No horizontal field padding
        }
        
        // --- Sex Dropdown on the side (using GridBagLayout for flexible positioning)
        GridBagLayout gbl = new GridBagLayout();
        JPanel topWrapper = new JPanel(gbl);
        topWrapper.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Add Name Panel (takes up most of the space)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = 5; // Span 5 rows
        topWrapper.add(namePanel, gbc);

        // Add Sex Dropdown (small, aligned to top right)
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.01; // Minimal width contribution
        gbc.weighty = 0.01;
        gbc.gridheight = 1; // Span 1 row
        gbc.anchor = GridBagConstraints.NORTHEAST;
        topWrapper.add(createSexDropdown(), gbc);

        formPanel.add(topWrapper);
        formPanel.add(Box.createVerticalStrut(15)); // Add vertical space after name fields

        // --- Civil Status and Birth Date Row
        JPanel statusRow = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 row, 2 columns, 10 horizontal gap
        statusRow.setBackground(Color.WHITE);
        
        statusRow.add(createLabeledTextField("Civil Status:", 10));
        statusRow.add(createDateField("Date:", "Birth Date"));

        formPanel.add(statusRow);
        formPanel.add(Box.createVerticalStrut(10)); // Add vertical space

        // --- Current Address
        formPanel.add(createLabeledTextField("Current Address:", 10));
        formPanel.add(Box.createVerticalStrut(20)); // Add vertical space before paragraph

        // --- Declaration Paragraph
        formPanel.add(createDeclarationPanel());
        formPanel.add(Box.createVerticalStrut(15)); // Add vertical space
        
        // --- Signature
        formPanel.add(createSignaturePanel());
        formPanel.add(Box.createVerticalStrut(20)); // Final space before bottom buttons

        return formPanel;
    }

    /** Helper to create a Label + Text Field with the line/border structure */
    private JPanel createLabeledTextField(String labelText, int fieldPadding) {
        JPanel panel = new JPanel(new BorderLayout(5, 0)); // Small horizontal gap
        panel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, BorderLayout.WEST);

        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(200, 20)); // Set a reasonable height
        textField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK)); // Bottom line border
        
        // Add padding around the text field to match the visual gap
        Border fieldBorder = BorderFactory.createEmptyBorder(0, fieldPadding, 0, 0); 
        textField.setBorder(BorderFactory.createCompoundBorder(textField.getBorder(), fieldBorder));
        
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    /** Helper to create the combined Date and Dropdown structure */
    private JPanel createDateField(String dateLabel, String dropdownText) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);
        
        // Left side: Date Label and Text Field
        JPanel datePanel = new JPanel(new BorderLayout(5, 0));
        datePanel.setBackground(Color.WHITE);
        datePanel.add(new JLabel(dateLabel), BorderLayout.WEST);
        
        JTextField dateField = new JTextField();
        dateField.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        datePanel.add(dateField, BorderLayout.CENTER);

        // Right side: Dropdown
        String[] options = {dropdownText, "Option 1", "Option 2"};
        JComboBox<String> dropdown = new JComboBox<>(options);
        dropdown.setBackground(Color.WHITE);
        dropdown.setPreferredSize(new Dimension(100, 25)); 
        dropdown.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        
        // Use a wrapping panel for the dropdown to limit its width
        JPanel dropdownWrapper = new JPanel(new BorderLayout());
        dropdownWrapper.setBackground(Color.WHITE);
        dropdownWrapper.add(dropdown, BorderLayout.EAST);

        panel.add(datePanel, BorderLayout.CENTER);
        panel.add(dropdownWrapper, BorderLayout.EAST);
        
        return panel;
    }
    
    /** Helper for the Sex dropdown in the top right */
    private JPanel createSexDropdown() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(150, 25)); // Set preferred size
        
        panel.add(new JLabel("Sex:"), BorderLayout.WEST);

        String[] options = {"", "Male", "Female", "Other"};
        JComboBox<String> dropdown = new JComboBox<>(options);
        dropdown.setBackground(Color.WHITE);
        dropdown.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        
        panel.add(dropdown, BorderLayout.CENTER);
        return panel;
    }
    
    /** Helper to create the legal declaration paragraph */
    private JPanel createDeclarationPanel() {
        // Use GridBagLayout to align the text with the fields
        JPanel declarationPanel = new JPanel(new GridBagLayout());
        declarationPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 5); // Add small spacing

        // Line 1: "I [FIELD] Of Legal age, with Residence and postal address at"
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; declarationPanel.add(new JLabel("I "), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; declarationPanel.add(createHorizontalLine(), gbc);
        gbc.gridx = 2; gbc.weightx = 0.6; declarationPanel.add(new JLabel(" Of Legal age, with Residence and postal address at"), gbc);

        // Line 2: "[FIELD] do hereby that [FIELD]"
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.5; gbc.gridwidth = 2; declarationPanel.add(createHorizontalLine(), gbc);
        gbc.gridx = 2; gbc.weightx = 0.0; gbc.gridwidth = 1; declarationPanel.add(new JLabel(" do hereby that "), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5; declarationPanel.add(createHorizontalLine(), gbc);

        // Line 3: "[FIELD] is presently residing at the"
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 1.0; gbc.gridwidth = 3; declarationPanel.add(createHorizontalLine(), gbc);
        gbc.gridx = 3; gbc.weightx = 0.0; gbc.gridwidth = 1; declarationPanel.add(new JLabel(" is presently residing at the"), gbc);
        
        // Line 4: "address mentioned above."
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 1.0; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.WEST;
        declarationPanel.add(new JLabel("address mentioned above."), gbc);

        return declarationPanel;
    }
    
    /** Helper to create a simple horizontal line */
    private JComponent createHorizontalLine() {
        JTextField line = new JTextField();
        line.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        line.setPreferredSize(new Dimension(100, 15)); // Give it a slight height for the line to show
        line.setEditable(false);
        line.setBackground(Color.WHITE);
        return line;
    }

    /** Helper to create the Sincerely Yours/Signature section */
    private JPanel createSignaturePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Use a wrapper panel to push signature area to the right
        JPanel rightAligned = new JPanel(new GridLayout(2, 1, 0, 5));
        rightAligned.setBackground(Color.WHITE);
        
        // Sincerely Yours
        JLabel sincerely = new JLabel("Sincerely Yours", SwingConstants.CENTER);
        sincerely.setFont(new Font("Arial", Font.BOLD, 14));
        rightAligned.add(sincerely);
        
        // Signature Line
        JTextField signatureLine = new JTextField();
        signatureLine.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        signatureLine.setPreferredSize(new Dimension(200, 40)); // The box for the signature
        signatureLine.setEditable(false);
        signatureLine.setBackground(Color.WHITE);
        
        // Combine Signature box and label
        JPanel sigPanel = new JPanel(new BorderLayout());
        sigPanel.setBackground(Color.WHITE);
        sigPanel.add(signatureLine, BorderLayout.CENTER);
        
        JLabel signatureLabel = new JLabel("Signature", SwingConstants.CENTER);
        signatureLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        sigPanel.add(signatureLabel, BorderLayout.SOUTH);
        
        rightAligned.add(sigPanel);
        
        panel.add(rightAligned, BorderLayout.EAST);
        return panel;
    }
    
// -----------------------------------------------------------------------
// --- SECTION 3: BOTTOM BUTTONS ---
// -----------------------------------------------------------------------

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 row, 2 columns, 10 horizontal gap
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // Add bottom padding

        // CANCEL Button
        JButton cancelButton = new JButton("CANCEL");
        cancelButton.setBackground(new Color(223, 50, 50)); // Red color
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 20));
        cancelButton.setPreferredSize(new Dimension(0, 50));
        cancelButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        cancelButton.addActionListener(e -> {
             JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(cancelButton);
             parentFrame.dispose();
        });

        // PROCEED Button
        JButton proceedButton = new JButton("PROCEED");
        proceedButton.setBackground(new Color(100, 180, 50)); // Green color
        proceedButton.setForeground(Color.WHITE);
        proceedButton.setFont(new Font("Arial", Font.BOLD, 20));
        proceedButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        
        // Add buttons to the panel
        buttonPanel.add(cancelButton);
        buttonPanel.add(proceedButton);

        return buttonPanel;
    }
}