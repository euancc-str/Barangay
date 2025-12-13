package org.example.Admin.AdminSettings;

import org.example.utils.ResourceUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageUtils {


    private static final String DEFAULT_ICON_PATH = "profile.png";

    public String getImageDir() {
        return new SystemConfigDAO().getImageDir();
    }


    public static String saveImage(File originalFile, String fileNameIdentifier) {
        try {

            SystemConfigDAO config = new SystemConfigDAO();
            String sharedFolderPath = config.getImageDir();
            //   Ensure folder exists (Network permissions required)
            File folder = new File(sharedFolderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }


            String extension = getFileExtension(originalFile);

            // If the identifier is "resident_1", the file becomes "resident_1.jpg"
            // If it is "system_logoPath", it becomes "system_logoPath.png"
            String finalName;
            if (fileNameIdentifier.startsWith("resident_") || fileNameIdentifier.startsWith("system_")) {
                finalName = fileNameIdentifier + extension;
            } else {
                // Default prefix for residents if just ID is passed
                finalName = "resident_" + fileNameIdentifier + extension;
            }

            File destination = new File(sharedFolderPath + finalName);


            Files.copy(originalFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return finalName; // Return just the name to save in DB

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving to network folder: " + e.getMessage());
            return null;
        }
    }

    public static void displayImage(JLabel label, String imageName, int width, int height) {
        // A. Get Full Network Path
        SystemConfigDAO config = new SystemConfigDAO();
        // This returns: \\LAPTOP-NH6HI8IB\BarangayImages\imageName.jpg
        String fullPath = config.getPhotoPath(imageName);

        File imgFile = (fullPath != null) ? new File(fullPath) : null;
        BufferedImage img = null;

        try {

            if (imgFile != null && imgFile.exists()) {
                img = ImageIO.read(imgFile);
            }


            if (img == null) {
                img = ImageIO.read(ResourceUtils.getResourceAsStream(DEFAULT_ICON_PATH));
            }

            if (img != null) {

                int w = (width > 0) ? width : (label.getWidth() > 0 ? label.getWidth() : 100);
                int h = (height > 0) ? height : (label.getHeight() > 0 ? label.getHeight() : 100);

                Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
                label.setText("");
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            } else {
                throw new IOException("No image found");
            }

        } catch (Exception e) {

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