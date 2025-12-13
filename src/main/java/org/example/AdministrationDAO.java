package org.example;

import org.example.Users.Administration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdministrationDAO {

    // --- READ ---
    public List<Administration> getAllAdministrations() {
        List<Administration> list = new ArrayList<>();
        String sql = "SELECT * FROM administration ORDER BY termStart DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Administration(
                        rs.getInt("adminId"),
                        rs.getString("termName"),
                        rs.getDate("termStart"),
                        rs.getDate("termEnd"),
                        rs.getString("captainName"),
                        rs.getString("status"),
                        rs.getString("vision")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- CREATE ---
    public boolean addAdministration(Administration admin) {
        String sql = "INSERT INTO administration (termName, termStart, termEnd, captainName, status, vision) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, admin.getTermName());
            stmt.setDate(2, admin.getTermStart());
            stmt.setDate(3, admin.getTermEnd());
            stmt.setString(4, admin.getCaptainName());
            stmt.setString(5, admin.getStatus());
            stmt.setString(6, admin.getVision());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean staffMatchName(String pos,String fullName){
        String sql = "SELECT position FROM barangay_staff WHERE position = ? AND CONCAT(firstName, ' ',middleName, ' ',lastName) = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,pos);
            pstmt.setString(2,fullName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    // --- UPDATE ---
    public boolean updateAdministration(Administration admin) {
        String sql = "UPDATE administration SET termName=?, termStart=?, termEnd=?, captainName=?, status=?, vision=? WHERE adminId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, admin.getTermName());
            stmt.setDate(2, admin.getTermStart());
            stmt.setDate(3, admin.getTermEnd());
            stmt.setString(4, admin.getCaptainName());
            stmt.setString(5, admin.getStatus());
            stmt.setString(6, admin.getVision());
            stmt.setInt(7, admin.getAdminId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- DELETE ---
    public boolean deleteAdministration(int id) {
        String sql = "DELETE FROM administration WHERE adminId=?";
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