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

    // Define a folder relative to the project for portability
    // "user.dir" gets the project root folder
    private static final String IMAGE_DIR = System.getProperty("user.dir") + File.separator + "resident_photos" + File.separator;

    // Use a classpath resource for the default icon so it works inside the JAR
    // Make sure you have a default image at src/main/resources/profile.png
    private static final String DEFAULT_ICON_PATH = "/org/example/resources/profile.png";
    // In ImageUtils.java
    public String getImageDir() {
        return IMAGE_DIR;
    }
    // 1. SAVE IMAGE
    public static String saveImage(File originalFile, String residentId) {
        try {
            File folder = new File(IMAGE_DIR);
            if (!folder.exists()) {
                folder.mkdirs();
            }

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

    // 2. DISPLAY IMAGE (Updated with explicit dimensions)
    public static void displayImage(JLabel label, String imageName, int width, int height) {
        // 1. Construct the file path
        String path = (imageName != null && !imageName.isEmpty()) ? IMAGE_DIR + imageName : "";
        File imgFile = new File(path);
        BufferedImage img = null;

        try {
            // 2. Try to read the specific resident photo
            if (imgFile.exists()) {
                img = ImageIO.read(imgFile);
            }

            // 3. If failed or missing, try to read the default icon from resources
            if (img == null) {
                // Using getResource allows loading from inside a JAR file
                java.net.URL defaultUrl = ImageUtils.class.getResource(DEFAULT_ICON_PATH);
                if (defaultUrl != null) {
                    img = ImageIO.read(defaultUrl);
                }
            }

            // 4. If we have an image (resident or default), scale and show it
            if (img != null) {
                // Use provided width/height, or fallback to label preferred size, or hard fallback 100
                int w = (width > 0) ? width : (label.getWidth() > 0 ? label.getWidth() : 100);
                int h = (height > 0) ? height : (label.getHeight() > 0 ? label.getHeight() : 100);

                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
                label.setText("");
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            } else {
                // 5. Absolute fail-safe (text only)
                throw new IOException("No image found");
            }

        } catch (IOException e) {
            label.setIcon(null);
            label.setText("<html><center>No Photo</center></html>");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int last = name.lastIndexOf(".");
        return (last == -1) ? ".jpg" : name.substring(last);
    }
}