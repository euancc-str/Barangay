package org.example.Admin.AdminSettings;

import org.example.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SystemConfigDAO {

    public String getConfig(String key) {
        String sql = "SELECT configValue FROM system_config WHERE configKey = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("configValue");
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    public void updateConfig(String key, String value) {
        // This query updates if exists, inserts if not (Safety)
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

    // =================================================
    // PART 2: SYSTEM OPTIONS (Dropdowns)
    // =================================================

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
    public String[] getOptionsNature(String category) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT displayValue FROM system_options WHERE category = ? ORDER BY sortOrder ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(rs.getString("displayValue"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list.toArray(new String[0]);
    }

    // Add a new option (e.g., New Purok)
    public void addOption(String category, String value) {
        String sql = "INSERT INTO system_options (category, displayValue, sortOrder) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setString(2, value);
            int len = 7;
            if(value.length() == 7){
                String num = String.valueOf(value.charAt(len-1));
                int order = Integer.parseInt(num);
                stmt.setInt(3,order);
            } else if (value.length() == 8){
                String num = value.substring(len - 1, len + 1);
                int order = Integer.parseInt(num);
                stmt.setInt(3,order);
            }else{
                stmt.setInt(3,0);
            }
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Delete an option
    public void deleteOption(String value) {
        String sql = "DELETE FROM system_options WHERE displayValue = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}