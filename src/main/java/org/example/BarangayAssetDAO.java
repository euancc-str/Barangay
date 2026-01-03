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
                BarangayAsset asset = BarangayAsset.builder()
                        .assetId(rs.getInt("assetId"))
                        .itemName( rs.getString("itemName"))
                        .propertyNumber( rs.getString("propertyNumber"))
                        .dateAcquired(   rs.getDate("dateAcquired"))
                        .status(  rs.getString("status"))
                        .value( rs.getDouble("value"))
                        .location(rs.getString("location"))
                        .custodian(rs.getString("custodian"))
                        .build();

                list.add(asset);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    // Add this inside BarangayAssetDAO class
    public BarangayAsset getAssetById(int id) {
        String sql = "SELECT * FROM barangay_asset WHERE assetId = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return BarangayAsset.builder()
                            .assetId(rs.getInt("assetId"))
                            .itemName(rs.getString("itemName"))
                            .propertyCode(rs.getString("propertyCode"))
                            .propertyNumber(rs.getString("propertyNumber"))
                            .dateAcquired(rs.getDate("dateAcquired"))
                            .status(rs.getString("status"))
                            .value(rs.getDouble("value"))
                            .brand(rs.getString("brand"))
                            .model(rs.getString("model"))
                            .fundSource(rs.getString("fundSource"))
                            .custodian(rs.getString("custodian"))
                            .usefulLifeYears(rs.getInt("usefulLifeYears"))
                            .purchaseDate(rs.getDate("purchaseDate"))
                            .location(rs.getString("location"))
                            .serialNumber(rs.getString("serialNumber"))
                            .build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- CREATE ---
    // --- CREATE (Insert all attributes) ---
    public boolean addAsset(BarangayAsset asset) {
        String sql = "INSERT INTO barangay_asset " +
                "(itemName, propertyCode, propertyNumber, dateAcquired, status, value, " +
                "brand, model, fundSource, custodian, usefulLifeYears, purchaseDate, location, serialNumber) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asset.getItemName());
            stmt.setString(2, asset.getPropertyCode());
            stmt.setString(3, asset.getPropertyNumber());
            stmt.setDate(4, asset.getDateAcquired());
            stmt.setString(5, asset.getStatus());
            stmt.setDouble(6, asset.getValue());

            stmt.setString(7, asset.getBrand());
            stmt.setString(8, asset.getModel());
            stmt.setString(9, asset.getFundSource());
            stmt.setString(10, asset.getCustodian());
            stmt.setInt(11, asset.getUsefulLifeYears());
            stmt.setDate(12, asset.getPurchaseDate());
            stmt.setString(13, asset.getLocation());
            stmt.setString(14, asset.getSerialNumber());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- UPDATE (Update all attributes based on ID) ---
    public boolean updateAsset(BarangayAsset asset) {
        String sql = "UPDATE barangay_asset SET " +
                "itemName=?, propertyCode=?, propertyNumber=?, dateAcquired=?, status=?, value=?, " +
                "brand=?, model=?, fundSource=?, custodian=?, usefulLifeYears=?, purchaseDate=?, " +
                "location=?, serialNumber=? WHERE assetId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asset.getItemName());
            stmt.setString(2, asset.getPropertyCode());
            stmt.setString(3, asset.getPropertyNumber());
            stmt.setDate(4, asset.getDateAcquired());
            stmt.setString(5, asset.getStatus());
            stmt.setDouble(6, asset.getValue());

            stmt.setString(7, asset.getBrand());
            stmt.setString(8, asset.getModel());
            stmt.setString(9, asset.getFundSource());
            stmt.setString(10, asset.getCustodian());
            stmt.setInt(11, asset.getUsefulLifeYears());
            stmt.setDate(12, asset.getPurchaseDate());
            stmt.setString(13, asset.getLocation());
            stmt.setString(14, asset.getSerialNumber());

            // The ID is the 15th parameter
            stmt.setInt(15, asset.getAssetId());

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