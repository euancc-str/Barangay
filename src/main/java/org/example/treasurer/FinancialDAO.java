package org.example.treasurer;

import org.example.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinancialDAO {

    // --- GET TOTAL INCOME (Dynamic Timeframe) ---
    public double getTotalIncome(String timeframe) {
        String sql = "SELECT SUM(totalFee) FROM document_request WHERE paymentStatus = 'Paid'";

        // Append Date Filter
        switch (timeframe) {
            case "Today":
                sql += " AND DATE(updatedAt) = CURDATE()";
                break;
            case "Week":
                sql += " AND YEARWEEK(updatedAt, 1) = YEARWEEK(CURDATE(), 1)";
                break;
            case "Month":
                sql += " AND MONTH(updatedAt) = MONTH(CURDATE()) AND YEAR(updatedAt) = YEAR(CURDATE())";
                break;
            case "Year":
                sql += " AND YEAR(updatedAt) = YEAR(CURDATE())";
                break;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // --- GET TRANSACTION HISTORY ---
    public List<Object[]> getTransactionHistory(String filter) {
        List<Object[]> list = new ArrayList<>();
        // Join with Resident to get names
        StringBuilder sql = new StringBuilder(
                "SELECT r.requestId, CONCAT(res.firstName, ' ', res.lastName) as fullname, " +
                        "dt.name as docName, r.totalFee, r.updatedAt " +
                        "FROM document_request r " +
                        "JOIN resident res ON r.residentId = res.residentId " +
                        "JOIN document_type dt ON r.docTypeId = dt.docTypeId " +
                        "WHERE r.paymentStatus = 'Paid'"
        );

        // Reuse logic for filter
        if (filter.equals("Today")) sql.append(" AND DATE(r.updatedAt) = CURDATE()");
        else if (filter.equals("This Week")) sql.append(" AND YEARWEEK(r.updatedAt, 1) = YEARWEEK(CURDATE(), 1)");
        else if (filter.equals("This Month")) sql.append(" AND MONTH(r.updatedAt) = MONTH(CURDATE()) AND YEAR(r.updatedAt) = YEAR(CURDATE())");

        sql.append(" ORDER BY r.updatedAt DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("requestId"),
                        rs.getString("fullname"),
                        rs.getString("docName"),
                        rs.getDouble("totalFee"),
                        rs.getTimestamp("updatedAt")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}