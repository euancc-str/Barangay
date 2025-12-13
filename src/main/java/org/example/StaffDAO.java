package org.example;

import com.mysql.cj.protocol.Resultset;
import org.example.Admin.SystemLogDAO;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

import javax.xml.crypto.Data;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {
    public void addReasonForRejection(int residentId, String reason, int staffId){
        String sql = """
                SELECT requestId FROM document_request WHERE residentId = ? AND status = 'Pending'
                ORDER BY createdAt DESC LIMIT 1
                """;
        String updateSQL = """
                UPDATE document_request SET remarks = ?, status = ?, staffId = ?,updatedAt =?
                WHERE requestId = ?
                """;
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement selectStmt = conn.prepareStatement(sql);
            PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
            selectStmt.setInt(1,residentId);
            ResultSet rs = selectStmt.executeQuery();
            if(rs.next()){
                int requestId = rs.getInt("requestId");
                updateStmt.setString(1,reason);
                updateStmt.setString(2,"Rejected");
                updateStmt.setInt(3,staffId);
                updateStmt.setDate(4, Date.valueOf(LocalDate.now()));
                updateStmt.setInt(5,requestId);

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Staff ID: "+staffId + ": Rejected resident's " + residentId + " document request");
                } else {
                    System.out.println("⚠️ Failed to update request.");
                }
            }else {
                System.out.println("⚠️ No pending request found for resident ID: " + residentId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error in staff operations: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    public List<BarangayStaff> getAllStaff(){
        String sql = "SELECT CONCAT(firstName, ' ',middleName,' ',lastName) AS fullName, staffId, position, contactNo, status,lastLogin,username,password,idNumber FROM barangay_staff";
        List<BarangayStaff> staffList = new ArrayList<>();
        try(Connection conn = DatabaseConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                BarangayStaff staff = new BarangayStaff();
                staff.setStaffId(String.valueOf(rs.getInt("staffId")));
                staff.setName(rs.getString("fullName"));
                staff.setPosition(rs.getString("position"));
                staff.setContactNo(rs.getString("contactNo"));
                staff.setStatus(rs.getString("status"));
                Timestamp lastLogin = rs.getTimestamp("lastLogin");
                staff.setLastLogin(lastLogin.toLocalDateTime());
                staff.setPassword(rs.getString("password"));
                staff.setIdNumber(rs.getString("idNumber"));
                staff.setUsername(rs.getString("username"));
                staffList.add(staff);

            }
        }catch (SQLException e){
            System.out.println("❌ Error fetching staff's:");
            e.printStackTrace();
        }
        return staffList;
    }
    public void setStaffStatus(String status, String fullName,int id,String contactNum,String position, String newPass,String newUser){
        String sql = "UPDATE barangay_staff SET status = ?,contactNo = ?,position = ?,password=?, username=?,updatedAt=? WHERE CONCAT(barangay_staff.firstName, ' ',barangay_staff.lastName) = ? AND staffId = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql) ){
            pstmt.setString(1,status);
            pstmt.setString(2,contactNum);
            pstmt.setString(3,position);
            pstmt.setString(4,newPass);
            pstmt.setString(5,newUser);
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(7,fullName);
            pstmt.setInt(8,id);

            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("✅ Updated " + rowsUpdated + " row(s) successfully!");
        }catch (SQLException e){
            System.err.println("❌ Error updating status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void deactivateStaff(String status, String fullName, int staffId){
        String sql = "UPDATE barangay_staff SET status = ?,updatedAt =? WHERE CONCAT(barangay_staff.firstName, ' ',barangay_staff.lastName) = ? AND staffId = ?";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql) ){
            pstmt.setString(1,status);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(3,fullName);
            pstmt.setInt(4,staffId);
            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("✅ Updated " + rowsUpdated + " row(s) successfully!");
        }catch (SQLException e){
            System.err.println("❌ Error updating status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void documentDecisionByStatus(String status, int residentId, int staffId, String paymentStatus, int requestId,String remarks) {
        // 1. FIX: Removed "CONCAT(resident...)"
        // We filter ONLY by columns that exist inside 'document_request' (residentId)
        String sql = "UPDATE document_request SET status = ?, staffId = ?, paymentStatus = ?,updatedAt=? , remarks =? WHERE residentId = ? AND requestId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, staffId);
            pstmt.setString(3, paymentStatus);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(5,remarks);
            pstmt.setInt(6, residentId);
            pstmt.setInt(7,requestId);

            int rowsUpdated = pstmt.executeUpdate();
            System.out.println(status + " resident" + residentId + " ,req id" + requestId);
            System.out.println("✅ Updated " + rowsUpdated + " row(s) successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error updating status: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void updateResident(Resident resident, int staffId){
        String sql = "UPDATE resident SET address=?,status=?,username=?,password=?,updatedAt=? WHERE residentId= ?";
        SystemLogDAO logDAO = new SystemLogDAO();
        logDAO.addLog("Updated personal info", resident.getName(),staffId);
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,resident.getAddress());
            pstmt.setString(2,resident.getStatus());
            pstmt.setString(3,resident.getUsername());
            pstmt.setString(4,resident.getPassword());
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(6,resident.getResidentId());
            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("✅ Updated " + rowsUpdated + " row(s) successfully!");
            System.out.println(resident);
        }catch(SQLException e) {

        }
    }
    public BarangayStaff findStaffByPosition(String position){
        String sql = "SELECT firstName,lastName,middleName,position,password,username,position FROM barangay_staff WHERE position = ?";
        BarangayStaff staff = null;
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,position);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                staff = BarangayStaff.builder()
                        .firstName(rs.getString("firstName"))
                        .lastName(rs.getString("lastName"))
                        .middleName(rs.getString("middleName"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .position(rs.getString("position"))
                        .role("position")
                        .build();
            }
        }catch(SQLException e) {
            System.out.printf("Error finding staff ");
            e.printStackTrace();
        }
        return staff;
    }
    public BarangayStaff findStaffById(int id){
        String sql = "SELECT * FROM barangay_staff WHERE staffId = ?";
        BarangayStaff staff = null;
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1,id);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                Timestamp lastLogin = rs.getTimestamp("lastLogin");

                staff = BarangayStaff.builder()
                        .firstName(rs.getString("firstName"))
                        .lastName(rs.getString("lastName"))
                        .middleName(rs.getString("middleName"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .idNumber(rs.getString("idNumber"))
                        .position(rs.getString("position"))
                        .staffId(rs.getString("staffId"))
                        .address(rs.getString("address"))
                        .suffix(rs.getString("suffix"))
                        .contactNo(rs.getString("contactNo"))
                        .sex(rs.getString("sex"))
                        .dob(rs.getDate("birthDate") != null ? rs.getDate("birthDate").toLocalDate() : null)
                        .role(rs.getString("role"))
                        .lastLogin(lastLogin.toLocalDateTime())
                        .civilStatus(rs.getString("civilStatus"))
                        .citizenship(rs.getString("citizenship"))
                        .build();
                staff.setAge( LocalDate.now().getYear() - staff.getDob().getYear() );
            }
        }catch(SQLException e) {
            System.out.printf("Error finding staff ");
            e.printStackTrace();
        }
        return staff;
    }
    public BarangayStaff retrieveAllDataOfStaffById(int id){
        String sql = "SELECT * FROM barangay_staff WHERE staffId = ?";
        BarangayStaff staff = null;
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1,id);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                staff = BarangayStaff.builder()
                        .firstName(rs.getString("firstName"))
                        .lastName(rs.getString("lastName"))
                        .middleName(rs.getString("middleName"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .suffix(rs.getString("suffix"))
                        .role(rs.getString("role"))
                        .contactNo(rs.getString("contactNum"))
                        .position(rs.getString("position"))
                        .email(rs.getString("email"))
                        .build();
            }
        }catch(SQLException e) {
            System.out.printf("Error finding staff ");
            e.printStackTrace();
        }
        return staff;
    }
    public String staffFullName(BarangayStaff staff){
        return staff.getFirstName() + " " + staff.getMiddleName()+  " "+ staff.getLastName();
    }
    public int countPopulationFromDb(){
        String sql = "SELECT COUNT(*) AS count FROM resident";
        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            ResultSet rs = pstmt.executeQuery();
            if(rs.next())
                return rs.getInt("count");

        }catch (SQLException e){
            System.out.println("Error");
            e.printStackTrace();
        }
        return 0;
    }
    public void handleDeleteResident(int residentId) {
        String sql = "DELETE FROM resident WHERE residentId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, residentId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Resident deleted successfully.");
            } else {
                System.out.println("No resident found with ID: " + residentId);
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }
    }
    public boolean deleteRequest(int requestId) {
        String sqlPayment = "DELETE FROM payment WHERE requestId = ?";
        String sqlRequest = "DELETE FROM document_request WHERE requestId = ?";

        java.sql.Connection conn = null;
        try {
            conn = org.example.DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            System.out.println("Deleted req id:" + requestId);
            try (java.sql.PreparedStatement stmtP = conn.prepareStatement(sqlPayment)) {
                stmtP.setInt(1, requestId);
                stmtP.executeUpdate();
            }

            // 2. Delete the Request (Parent)
            int rows;
            try (java.sql.PreparedStatement stmtR = conn.prepareStatement(sqlRequest)) {
                stmtR.setInt(1, requestId);
                rows = stmtR.executeUpdate();
            }

            conn.commit(); // Save Changes
            return rows > 0;

        } catch (java.sql.SQLException e) {
            System.out.println("could not delete req id:" + requestId);
            if (conn != null) {
                try { conn.rollback(); } catch (java.sql.SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        }
    }
}
