package org.example.Admin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.example.DatabaseConnection;

public class SystemLogDAO {
    public void addLog(String action, String target, int staffId) {
        String sql = "INSERT INTO system_logs (actionType, targetResident, staffId, logDate) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, action);
            stmt.setString(2, target);
            stmt.setInt(3, staffId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- RETRIEVE LOGS WITH DATE FILTER ---
    public List<Object[]> getLogsByFilter(String filterType) {
        List<Object[]> logs = new ArrayList<>();

        // Base SQL Query
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

}