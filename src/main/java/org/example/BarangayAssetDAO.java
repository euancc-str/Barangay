package org.example;

import org.example.Users.BarangayAsset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BarangayAssetDAO {

    // --- READ ---
    public List<BarangayAsset> getAllAssets() {
        List<BarangayAsset> list = new ArrayList<>();
        String sql = "SELECT * FROM barangay_asset ORDER BY assetId DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new BarangayAsset(
                        rs.getInt("assetId"),
                        rs.getString("itemName"),
                        rs.getString("propertyNumber"),
                        rs.getDate("dateAcquired"), // Use dateAcquired based on your SQL
                        rs.getString("status"),
                        rs.getDouble("value") // Use value based on your SQL
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- CREATE ---
    public boolean addAsset(BarangayAsset asset) {
        String sql = "INSERT INTO barangay_asset (itemName, propertyNumber, dateAcquired, status, value) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asset.getItemName());
            stmt.setString(2, asset.getPropertyNumber());
            stmt.setDate(3, asset.getDateAcquired());
            stmt.setString(4, asset.getStatus());
            stmt.setDouble(5, asset.getValue());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- UPDATE ---
    public boolean updateAsset(BarangayAsset asset) {
        String sql = "UPDATE barangay_asset SET itemName=?, propertyNumber=?, dateAcquired=?, status=?, value=? WHERE assetId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asset.getItemName());
            stmt.setString(2, asset.getPropertyNumber());
            stmt.setDate(3, asset.getDateAcquired());
            stmt.setString(4, asset.getStatus());
            stmt.setDouble(5, asset.getValue());
            stmt.setInt(6, asset.getAssetId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- DELETE ---
    public boolean deleteAsset(int id) {
        String sql = "DELETE FROM barangay_asset WHERE assetId=?";
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