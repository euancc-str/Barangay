package org.example;




import org.example.Users.BorrowRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;




public class BorrowingDAO {




    public BorrowRecord getBorrowRecordById(int borrowId) {
        BorrowRecord record = null;
        String sql = "SELECT b.*, a.itemName, CONCAT(r.firstName, ' ', r.lastName) AS borrowerName " +
                "FROM asset_borrowing b " +
                "JOIN barangay_asset a ON b.assetId = a.assetId " +
                "JOIN resident r ON b.residentId = r.residentId " +
                "WHERE b.borrowId = ?";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {




            stmt.setInt(1, borrowId);




            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    record = new BorrowRecord();
                    record.setBorrowId(rs.getInt("borrowId"));
                    record.setAssetId(rs.getInt("assetId"));
                    record.setAssetName(rs.getString("itemName"));
                    record.setResidentId(rs.getInt("residentId"));
                    record.setBorrowerName(rs.getString("borrowerName"));
                    record.setDateBorrowed(rs.getDate("dateBorrowed"));
                    record.setExpectedReturnDate(rs.getDate("expectedReturnDate"));
                    record.setDateReturned(rs.getDate("dateReturned"));
                    record.setRemarks(rs.getString("remarks"));
                    record.setStatus(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return record;
    }
    // 1. GET ALL ACTIVE BORROWS (For the Table)
    public List<BorrowRecord> getActiveBorrows() {
        List<BorrowRecord> list = new ArrayList<>();
        // Join with Asset and Resident tables to get names instead of just IDs
        String sql = "SELECT b.*, a.itemName, CONCAT(r.firstName, ' ', r.lastName) AS borrowerName " +
                "FROM asset_borrowing b " +
                "JOIN barangay_asset a ON b.assetId = a.assetId " +
                "JOIN resident r ON b.residentId = r.residentId " +
                "WHERE b.status = 'Active' " +
                "ORDER BY b.dateBorrowed DESC";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {




            while (rs.next()) {
                BorrowRecord rec = new BorrowRecord();
                rec.setBorrowId(rs.getInt("borrowId"));
                rec.setAssetId(rs.getInt("assetId"));
                rec.setAssetName(rs.getString("itemName"));
                rec.setResidentId(rs.getInt("residentId"));
                rec.setBorrowerName(rs.getString("borrowerName"));
                rec.setDateBorrowed(rs.getDate("dateBorrowed"));
                rec.setExpectedReturnDate(rs.getDate("expectedReturnDate"));
                rec.setStatus(rs.getString("status"));
                list.add(rec);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public List<BorrowRecord> getAllBorrows() {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT b.*, a.itemName, CONCAT(r.firstName, ' ', r.lastName) AS borrowerName " +
                "FROM asset_borrowing b " +
                "JOIN barangay_asset a ON b.assetId = a.assetId " +
                "JOIN resident r ON b.residentId = r.residentId " +
                "  " +
                "ORDER BY b.dateBorrowed DESC";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {




            while (rs.next()) {
                BorrowRecord rec = new BorrowRecord();
                rec.setBorrowId(rs.getInt("borrowId"));
                rec.setAssetId(rs.getInt("assetId"));
                rec.setAssetName(rs.getString("itemName"));
                rec.setResidentId(rs.getInt("residentId"));
                rec.setBorrowerName(rs.getString("borrowerName"));
                rec.setDateBorrowed(rs.getDate("dateBorrowed"));
                rec.setExpectedReturnDate(rs.getDate("expectedReturnDate"));
                rec.setStatus(rs.getString("status"));
                list.add(rec);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public List<BorrowRecord> getAllBorrowHistory() {
        List<BorrowRecord> history = new ArrayList<>();
        String sql = "SELECT b.borrowId, b.assetId, a.itemName, " +
                "CONCAT(r.firstName, ' ', r.lastName) as borrowerName, " +
                "b.dateBorrowed, b.expectedReturnDate, b.dateReturned, " +
                "b.status, b.remarks " +
                "FROM asset_borrowing b " +  // Changed from 'borrowing' to 'asset_borrowing'
                "JOIN barangay_asset a ON b.assetId = a.assetId " +
                "JOIN resident r ON b.residentId = r.residentId " +
                "ORDER BY b.dateBorrowed DESC";


        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {


            while (rs.next()) {
                BorrowRecord record = new BorrowRecord();
                record.setBorrowId(rs.getInt("borrowId"));
                record.setAssetId(rs.getInt("assetId"));
                record.setAssetName(rs.getString("itemName"));
                record.setBorrowerName(rs.getString("borrowerName"));
                record.setDateBorrowed(rs.getDate("dateBorrowed"));
                record.setExpectedReturnDate(rs.getDate("expectedReturnDate"));
                record.setDateReturned(rs.getDate("dateReturned"));
                record.setStatus(rs.getString("status"));
                record.setRemarks(rs.getString("remarks"));
                history.add(record);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error in getAllBorrowHistory: " + e.getMessage());
        }
        return history;
    }


    public List<BorrowRecord> testGetHistory() {
        List<BorrowRecord> history = new ArrayList<>();


        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {


            // First, show what tables exist
            String showTables = "SHOW TABLES";
            ResultSet tables = stmt.executeQuery(showTables);
            System.out.println("Available tables:");
            while (tables.next()) {
                System.out.println("  - " + tables.getString(1));
            }


            // Try to query the borrowing table
            String sql = "SELECT * FROM asset_borrowing LIMIT 5";
            ResultSet rs = stmt.executeQuery(sql);


            System.out.println("Borrowing table structure:");
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("  " + i + ". " + meta.getColumnName(i) + " - " + meta.getColumnTypeName(i));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


        return history;
    }
    // 2. LEND ITEM (Create New Record)
    public boolean lendItem(int assetId, int residentId, Date borrowDate, Date returnDate) {
        String sql = "INSERT INTO asset_borrowing (assetId, residentId, dateBorrowed, expectedReturnDate, status) VALUES (?, ?, ?, ?, 'Active')";
        String updateAsset = "UPDATE barangay_asset SET status = 'Borrowed' WHERE assetId = ?";




        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaction Mode




            try (PreparedStatement ps1 = conn.prepareStatement(sql);
                 PreparedStatement ps2 = conn.prepareStatement(updateAsset)) {




                // Insert Log
                ps1.setInt(1, assetId);
                ps1.setInt(2, residentId);
                ps1.setDate(3, borrowDate);
                ps1.setDate(4, returnDate);
                ps1.executeUpdate();




                // Update Asset Status
                ps2.setInt(1, assetId);
                ps2.executeUpdate();




                conn.commit(); // Save both
                return true;
            } catch (SQLException e) {
                conn.rollback(); // Undo if error
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }








    // 3. RETURN ITEM (Close Record)
    public boolean returnItem(int borrowId, int assetId, Date returnedDate, String remarks) {
        String sql = "UPDATE asset_borrowing SET dateReturned=?, status='Returned', remarks=? WHERE borrowId=?";
        String updateAsset = "UPDATE barangay_asset SET status = 'Good' WHERE assetId = ?";




        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);




            try (PreparedStatement ps1 = conn.prepareStatement(sql);
                 PreparedStatement ps2 = conn.prepareStatement(updateAsset)) {




                ps1.setDate(1, returnedDate);
                ps1.setString(2, remarks);
                ps1.setInt(3, borrowId);
                ps1.executeUpdate();




                ps2.setInt(1, assetId);
                ps2.executeUpdate();




                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) { return false; }
    }
}



