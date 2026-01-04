package org.example.Admin;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.DatabaseConnection;


public class SystemLogDAO {
    public boolean addLog(String actionType, String userType, int userId) {
        String sql = "INSERT INTO system_logs (actionType, targetResident, staffId, logDate) VALUES (?, ?, ?, NOW())";



        if (actionType.length() > 255) {
            actionType = actionType.substring(0, 255);
        }


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setString(1, actionType);
            stmt.setString(2, userType);
            stmt.setInt(3, userId);


            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    private String truncateLogMessage(String message, int maxLength) {
        if (message.length() > maxLength) {
            return message.substring(0, maxLength);
        }
        return message;
    }
    public List<Object[]> getLogsByFilter(String filterType) {
        List<Object[]> logs = new ArrayList<>();


        StringBuilder sql = new StringBuilder("""
           SELECT s.logId, s.actionType, s.targetResident,
                  CONCAT(b.firstName, ' ', b.lastName) AS staffName,
                  s.logDate
           FROM system_logs s
           LEFT JOIN barangay_staff b ON s.staffId = b.staffId
       """);


        switch (filterType) {
            case "Today":
                sql.append(" WHERE DATE(s.logDate) = CURDATE() ");
                break;
            case "This Week":
                sql.append(" WHERE YEARWEEK(s.logDate, 1) = YEARWEEK(CURDATE(), 1) ");
                break;
            case "This Month":
                sql.append(" WHERE YEAR(s.logDate) = YEAR(CURDATE()) AND MONTH(s.logDate) = MONTH(CURDATE()) ");
                break;
            case "All Time":
            default:
                // No WHERE clause needed
                break;
        }


        sql.append(" ORDER BY s.logDate DESC");


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {


            while (rs.next()) {
                String id = "LOG-" + String.format("%04d", rs.getInt("logId"));
                String action = rs.getString("actionType");
                String target = rs.getString("targetResident");
                String staff = rs.getString("staffName");
                if (staff == null) staff = "System / Deleted User";
                Timestamp dateStamp = rs.getTimestamp("logDate");
                String date = dateStamp.toLocalDateTime().toString();
                logs.add(new Object[]{id, action, target, staff, date});
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    public boolean deleteLog(int logId) {
        String sql = "DELETE FROM system_logs WHERE logId = ?";
        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setInt(1, logId);
            int rows = stmt.executeUpdate();
            return rows > 0; // Returns true if deleted


        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Inside SystemLogDAO class
    public int getLatestLogId(String actionFilter) {
        String sql = "SELECT MAX(logId) FROM system_logs";
        if (actionFilter != null && !actionFilter.isEmpty()) {
            sql += " WHERE actionType LIKE ?";
        }

        System.out.println("🔎 Executing: " + sql);
        System.out.println("🔎 Filter pattern: %" + actionFilter + "%");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (actionFilter != null && !actionFilter.isEmpty()) {
                pstmt.setString(1, "%" + actionFilter + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int maxId = rs.getInt(1);
                    System.out.println("✅ Found MAX logId: " + maxId);
                    return maxId;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error:");
            e.printStackTrace();
        }

        System.out.println("⚠️ No matching logs found, returning 0");
        return 0;
    }


}
