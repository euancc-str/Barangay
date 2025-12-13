package org.example;

import org.example.Users.Household;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HouseholdDAO {

    // --- 1. GET ALL HOUSEHOLDS ---
    public List<Household> getAllHouseholds() {
        List<Household> list = new ArrayList<>();
        String sql = "SELECT * FROM household ORDER BY householdId DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Household h = new Household();
                h.setHouseholdId(rs.getInt("householdId"));
                h.setHouseholdNo(rs.getString("householdNo"));
                h.setPurok(rs.getString("purok"));
                h.setStreet(rs.getString("street"));
                h.setAddress(rs.getString("address"));
                h.setHouseholdHeadId(rs.getInt("householdHeadId")); // Use 0 or -1 if null logic needed
                h.setTotalMembers(rs.getInt("totalMembers"));
                h.setNotes(rs.getString("notes"));
                h.setCreatedAt(rs.getTimestamp("createdAt"));
                h.setUpdatedAt(rs.getTimestamp("updatedAt"));

                list.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- 2. ADD HOUSEHOLD ---
    public boolean addHousehold(Household h) {
        String sql = "INSERT INTO household (householdNo, purok, street, address, householdHeadId, totalMembers, notes, createdAt, updatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, h.getHouseholdNo());
            stmt.setString(2, h.getPurok());
            stmt.setString(3, h.getStreet());
            stmt.setString(4, h.getAddress());

            if (h.getHouseholdHeadId() > 0) stmt.setInt(5, h.getHouseholdHeadId());
            else stmt.setNull(5, Types.INTEGER);

            stmt.setInt(6, h.getTotalMembers());
            stmt.setString(7, h.getNotes());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 3. UPDATE HOUSEHOLD ---
    public boolean updateHousehold(Household h) {
        String sql = "UPDATE household SET householdNo=?, purok=?, street=?, address=?, householdHeadId=?, totalMembers=?, notes=?, updatedAt=NOW() " +
                "WHERE householdId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, h.getHouseholdNo());
            stmt.setString(2, h.getPurok());
            stmt.setString(3, h.getStreet());
            stmt.setString(4, h.getAddress());

            if (h.getHouseholdHeadId() > 0) stmt.setInt(5, h.getHouseholdHeadId());
            else stmt.setNull(5, Types.INTEGER);

            stmt.setInt(6, h.getTotalMembers());
            stmt.setString(7, h.getNotes());
            stmt.setInt(8, h.getHouseholdId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 4. DELETE HOUSEHOLD ---
    public boolean deleteHousehold(int id) {
        String sql = "DELETE FROM household WHERE householdId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- OPTIONAL: Find by ID ---
    public Household findHouseholdById(int id) {
        String sql = "SELECT * FROM household WHERE householdId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Household(
                        rs.getInt("householdId"),
                        rs.getString("householdNo"),
                        rs.getString("purok"),
                        rs.getString("street"),
                        rs.getString("address"),
                        rs.getInt("householdHeadId"),
                        rs.getInt("totalMembers"),
                        rs.getString("notes"),
                        rs.getTimestamp("createdAt"),
                        rs.getTimestamp("updatedAt")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
    public boolean doesHouseholdExists(String householdNo) {
        String sql = "SELECT householdId FROM household WHERE householdNo = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, householdNo);
            java.sql.ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns TRUE if number already exists

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public int countMembers(String householdNo) {
        String sql = "SELECT COUNT(*) FROM resident WHERE householdNo = ?";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, householdNo);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}