package org.example.Admin.AdminSettings;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageUtils {

    // NOTE: When you build the JAR, 'src' won't exist.
    // Use a folder next to your app like "resident_photos/" instead.
    private static final String IMAGE_DIR = "example/ImagePath/";
    private static final String DEFAULT_ICON = "example/resources/profile.png";

    // 1. SAVE IMAGE
    public static String saveImage(File originalFile, String residentId) {
        try {
            File folder = new File(IMAGE_DIR);
            if (!folder.exists()) folder.mkdirs();

            String extension = getFileExtension(originalFile);
            String newFileName = "resident_" + residentId + extension;
            File destination = new File(IMAGE_DIR + newFileName);

            Files.copy(originalFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 2. DISPLAY IMAGE (Clean Version)
    public static void displayImage(JLabel label, String imageName) {
        String path = (imageName != null && !imageName.isEmpty()) ? IMAGE_DIR + imageName : DEFAULT_ICON;
        File imgFile = new File(path);

        // If file missing, force default
        if (!imgFile.exists()) {
            imgFile = new File(DEFAULT_ICON);
        }

        try {
            BufferedImage img = ImageIO.read(imgFile);
            if (img != null) {
                // Resize to fit the label
                Image scaled = img.getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
                label.setText(""); // Clear text
            }
        } catch (IOException e) {
            // If even default fails, show text
            label.setIcon(null);
            label.setText("<html><center>No Photo<br>Click to Upload</center></html>");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        }
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int last = name.lastIndexOf(".");
        return (last == -1) ? ".jpg" : name.substring(last);
    }
}