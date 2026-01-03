package org.example.Admin.AdminSettings;

import org.example.DatabaseConnection;
import org.example.utils.ResourceUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemConfigDAO {

    private static String BASE_PATH = "";

    static {
        BASE_PATH = System.getProperty("asset.image.base-path");
    }


    public String getImageDir() {
        return BASE_PATH;
    }

    // 2. Get Barangay Logo Path (Left Side)
    public String getLogoPath() {
        String filename = getConfig("logoPath");
        // Default if DB is empty
        if (filename == null || filename.isEmpty()) {
            filename = "daetlogo.png";
        }
        return BASE_PATH + filename;
    }


    public String getDaetLogoPath() {
        String filename = getConfig("daetLogoPath");

        if (filename == null || filename.isEmpty()) {
            filename = "daetlogo.png"; // Fallback to existing file
        }
        return BASE_PATH + filename;
    }


    public String getBigLogoPath() {
        String filename = getConfig("bigLogoPath");
        if (filename == null || filename.isEmpty()) {
            filename = "daetlogo.png";
        }
        return BASE_PATH + filename;
    }


    public String getPhotoPath(String photoFilename) {

        if (photoFilename == null || photoFilename.isEmpty()) {
            return null;
        }
        return BASE_PATH + photoFilename;
    }

    public String getConfig(String key) {
        String sql = "SELECT configValue FROM system_config WHERE configKey = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);

            ResultSet rs = stmt.executeQuery();
            System.out.println(key);
            if (rs.next()) return rs.getString("configValue");
        } catch (SQLException e) { e.printStackTrace(); }


        if (key.equals("barangay_name")) return "BARANGAY ALAWIHAO";
        if (key.equals("defaultCtcPlace")) return "Daet, Camarines Norte";

        return "";
    }

    public void updateConfig(String key, String value) {

        String sql = "INSERT INTO system_config (configKey, configValue) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE configValue = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, value);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }



    public List<String> getOptions(String category) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT displayValue FROM system_options WHERE category = ? ORDER BY sortOrder ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(rs.getString("displayValue"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public List<String> getCategory() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM system_options ORDER BY sortOrder ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(rs.getString("category"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public String[] getOptionsCategory() {
        List<String> list = getCategory(); // Reuse method above
        return list.toArray(new String[0]);
    }

    public String[] getOptionsNature(String category) {
        List<String> list = getOptions(category); // Reuse method above
        return list.toArray(new String[0]);
    }

    public void addOption(String category, String value) {
        String sql = "INSERT INTO system_options (category, displayValue, sortOrder) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            stmt.setString(2, value);


            int order = 0;
            try {
                if (value.toLowerCase().contains("purok")) {
                    String numStr = value.replaceAll("[^0-9]", ""); // Extract digits
                    if (!numStr.isEmpty()) order = Integer.parseInt(numStr);
                }
            } catch (Exception ignored) {}

            stmt.setInt(3, order);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteOption(String value) {
        String sql = "DELETE FROM system_options WHERE displayValue = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}