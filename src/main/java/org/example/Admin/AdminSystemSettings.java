package org.example.Admin;

import org.example.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminSystemSettings {

    public List<Object[]> getAllDocumentTypes() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT docTypeId, name, fee FROM document_type";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{ rs.getInt("docTypeId"), rs.getString("name"), rs.getDouble("fee") });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public boolean addDocumentType(String name, double fee) {
        String sql = "INSERT INTO document_type (name, fee, processingTime, validityPeriod, renewable, copiesAllowed) VALUES (?, ?, 1, 1, 1, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, fee);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateDocumentFee(int docId, double newFee) {
        String sql = "UPDATE document_type SET fee = ? WHERE docTypeId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newFee);
            stmt.setInt(2, docId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- 4. DELETE DOCUMENT ---
    public void deleteDocument(int docId) {
        String sql = "DELETE FROM document_type WHERE docTypeId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, docId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public String[] getLoginOptions() {

        java.util.List<String> tempOptions = new java.util.ArrayList<>();

        // Your SQL
        String sql = "SELECT positionName FROM setting_position ORDER BY id ASC";

        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
             java.sql.ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tempOptions.add(rs.getString("positionName"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback in case DB fails so the dropdown isn't empty
            return new String[]{"Resident", "Admin"};
        }

        // CONVERT LIST TO 1D ARRAY (The Magic Part)
        return tempOptions.toArray(new String[0]);
    }
    public int getPositionIndexByUniqueId(String uniqueId) {
        String sql = "SELECT id FROM setting_position WHERE uniqueId = ?";
        int index = -1;

        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, uniqueId);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    index = rs.getInt("id") - 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }
    public List<Object[]> getAllPositions() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT id, positionName, uniqueId FROM setting_position ORDER BY id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("positionName"),
                        rs.getString("uniqueId") // Can be null
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addPosition(String name, String uniqueId) {
        String sql = "INSERT INTO setting_position (positionName, uniqueId) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            if (uniqueId == null || uniqueId.trim().isEmpty()) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, uniqueId);
            }
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updatePosition(int id, String name, String uniqueId) {
        String sql = "UPDATE setting_position SET positionName = ?, uniqueId = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            if (uniqueId == null || uniqueId.trim().isEmpty()) {
                stmt.setNull(2, Types.VARCHAR);
            } else {
                stmt.setString(2, uniqueId);
            }
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 4. DELETE POSITION ---
    public boolean deletePosition(int id) {
        String sql = "DELETE FROM setting_position WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}