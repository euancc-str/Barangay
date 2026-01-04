package org.example.treasurer;




import org.example.DatabaseConnection;
import org.example.Admin.SystemLogDAO;
import org.example.UserDataManager;
import org.example.Users.BarangayStaff;




import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




public class FinancialDAO {




    private SystemLogDAO systemLogDAO = new SystemLogDAO();




    public double getTotalIncome(String timeframe) {
        String sql = "SELECT SUM(amount) FROM payment WHERE status = 'Approved'";




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


    public double getTotalAssetsValue() {
        String sql = "SELECT SUM(value) FROM barangay_asset WHERE status NOT IN ('Lost', 'Disposed')";


        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {


            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    // Get total paid amount for all time
    public double getTotalPaidAmount() {
        String sql = "SELECT SUM(totalFee) FROM document_request WHERE paymentStatus = 'Paid'";


        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {


            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }


    // Add this method to FinancialDAO.java
    public double getTotalIncomeByDateRange(Date fromDate, Date toDate) {
        String sql = "SELECT SUM(totalFee) FROM document_request " +
                "WHERE paymentStatus = 'Paid' " +
                "AND DATE(updatedAt) BETWEEN ? AND ?";


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setDate(1, new java.sql.Date(fromDate.getTime()));
            stmt.setDate(2, new java.sql.Date(toDate.getTime()));


            ResultSet rs = stmt.executeQuery();


            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    // Get daily summary for last 30 days
    public List<Object[]> getDailySummary() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT DATE(r.updatedAt) as payment_date, " +
                "COUNT(r.requestId) as transaction_count, " +
                "SUM(r.totalFee) as daily_total " +
                "FROM document_request r " +
                "WHERE r.paymentStatus = 'Paid' " +
                "AND r.updatedAt >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                "GROUP BY DATE(r.updatedAt) " +
                "ORDER BY payment_date DESC";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {




            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");




            while (rs.next()) {
                Date paymentDate = rs.getDate("payment_date");
                int transactionCount = rs.getInt("transaction_count");
                double dailyTotal = rs.getDouble("daily_total");




                list.add(new Object[]{
                        sdf.format(paymentDate),
                        transactionCount,
                        dailyTotal
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }




    public List<Object[]> getPaymentsByDate(Date date) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT r.requestId, " +
                "CONCAT(res.firstName, ' ', res.lastName) as fullname, " +
                "dt.name as docName, " +
                "r.totalFee, " +
                "DATE_FORMAT(r.updatedAt, '%h:%i %p') as payment_time " +
                "FROM document_request r " +
                "JOIN resident res ON r.residentId = res.residentId " +
                "JOIN document_type dt ON r.docTypeId = dt.docTypeId " +
                "WHERE r.paymentStatus = 'Paid' " +
                "AND DATE(r.updatedAt) = ? " +
                "ORDER BY r.updatedAt DESC";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {




            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            stmt.setDate(1, sqlDate);




            ResultSet rs = stmt.executeQuery();




            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("requestId"),
                        rs.getString("fullname"),
                        rs.getString("docName"),
                        rs.getDouble("totalFee"),
                        rs.getString("payment_time")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public List<Object[]> getTodaysPayments() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT r.requestId, " +
                "CONCAT(res.firstName, ' ', res.lastName) as fullname, " +
                "dt.name as docName, " +
                "r.totalFee, " +
                "DATE_FORMAT(r.updatedAt, '%h:%i %p') as payment_time " +
                "FROM document_request r " +
                "JOIN resident res ON r.residentId = res.residentId " +
                "JOIN document_type dt ON r.docTypeId = dt.docTypeId " +
                "WHERE r.paymentStatus = 'Paid' " +
                "AND DATE(r.updatedAt) = CURDATE() " +
                "ORDER BY r.updatedAt DESC";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {




            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("requestId"),
                        rs.getString("fullname"),
                        rs.getString("docName"),
                        rs.getDouble("totalFee"),
                        rs.getString("payment_time")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }




    // Get monthly summary
    public List<Object[]> getMonthlySummary() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(updatedAt, '%M %Y') as month_year, " +
                "COUNT(requestId) as transaction_count, " +
                "SUM(totalFee) as monthly_total " +
                "FROM document_request " +
                "WHERE paymentStatus = 'Paid' " +
                "GROUP BY DATE_FORMAT(updatedAt, '%Y-%m') " +
                "ORDER BY YEAR(updatedAt) DESC, MONTH(updatedAt) DESC";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {




            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("month_year"),
                        rs.getInt("transaction_count"),
                        rs.getDouble("monthly_total")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }




    // Get yearly summary
    public List<Object[]> getYearlySummary() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT YEAR(updatedAt) as year, " +
                "COUNT(requestId) as transaction_count, " +
                "SUM(totalFee) as yearly_total " +
                "FROM document_request " +
                "WHERE paymentStatus = 'Paid' " +
                "GROUP BY YEAR(updatedAt) " +
                "ORDER BY year DESC";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {




            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("year"),
                        rs.getInt("transaction_count"),
                        rs.getDouble("yearly_total")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public List<Object[]> getTransactionsByMonthYear(int year, int month) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT r.requestId, " +
                "CONCAT(res.firstName, ' ', res.lastName) as fullname, " +
                "dt.name as docName, " +
                "r.totalFee, " +
                "DATE(r.updatedAt) as payment_date, " +
                "DATE_FORMAT(r.updatedAt, '%h:%i %p') as payment_time " +
                "FROM document_request r " +
                "JOIN resident res ON r.residentId = res.residentId " +
                "JOIN document_type dt ON r.docTypeId = dt.docTypeId " +
                "WHERE r.paymentStatus = 'Paid' " +
                "AND YEAR(r.updatedAt) = ? " +
                "AND MONTH(r.updatedAt) = ? " +
                "ORDER BY r.updatedAt DESC";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {




            stmt.setInt(1, year);
            stmt.setInt(2, month);




            ResultSet rs = stmt.executeQuery();




            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("requestId"),
                        rs.getString("fullname"),
                        rs.getString("docName"),
                        rs.getDouble("totalFee"),
                        rs.getDate("payment_date"),
                        rs.getString("payment_time")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }




    // Get total income for specific month
    public double getTotalIncomeForMonth(int year, int month) {
        String sql = "SELECT SUM(totalFee) FROM document_request " +
                "WHERE paymentStatus = 'Paid' " +
                "AND YEAR(updatedAt) = ? " +
                "AND MONTH(updatedAt) = ?";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {




            stmt.setInt(1, year);
            stmt.setInt(2, month);




            ResultSet rs = stmt.executeQuery();




            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }




    // Delete a document request
    public boolean deleteRequest(int requestId) {
        Connection conn = null;
        PreparedStatement stmt = null;




        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction




            // First, delete from payments table if exists
            String deletePaymentsSQL = "DELETE FROM payments WHERE requestId = ?";
            stmt = conn.prepareStatement(deletePaymentsSQL);
            stmt.setInt(1, requestId);
            stmt.executeUpdate();
            stmt.close();




            // Then delete from document_request table
            String deleteRequestSQL = "DELETE FROM document_request WHERE requestId = ?";
            stmt = conn.prepareStatement(deleteRequestSQL);
            stmt.setInt(1, requestId);
            int rowsAffected = stmt.executeUpdate();




            conn.commit(); // Commit transaction




            // Log the deletion
            if (rowsAffected > 0) {
                try {
                    BarangayStaff currentStaff = UserDataManager.getInstance().getCurrentStaff();
                    if (currentStaff != null) {
                        int staffId = Integer.parseInt(currentStaff.getStaffId());
                        systemLogDAO.addLog("Deleted Document Request", "Request ID: " + requestId, staffId);
                    }
                } catch (Exception e) {
                    System.err.println("Error logging deletion: " + e.getMessage());
                }
                return true;
            }




            return false;




        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




    // Get request details before deletion (for confirmation)
    public String getRequestDetails(int requestId) {
        String sql = "SELECT CONCAT(r.firstName, ' ', r.lastName) as resident_name, " +
                "d.name as document_type, dr.totalFee, dr.purpose " +
                "FROM document_request dr " +
                "JOIN resident r ON dr.residentId = r.residentId " +
                "JOIN document_type d ON dr.docTypeId = d.docTypeId " +
                "WHERE dr.requestId = ?";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {




            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();




            if (rs.next()) {
                String residentName = rs.getString("resident_name");
                String documentType = rs.getString("document_type");
                double totalFee = rs.getDouble("totalFee");
                String purpose = rs.getString("purpose");




                return String.format("Resident: %s\nDocument: %s\nAmount: ₱%.2f\nPurpose: %s",
                        residentName, documentType, totalFee, purpose);
            }




        } catch (SQLException e) {
            e.printStackTrace();
        }




        return "Request details not found";
    }




    // Check if request exists
    public boolean requestExists(int requestId) {
        String sql = "SELECT COUNT(*) FROM document_request WHERE requestId = ?";




        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {




            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();




            if (rs.next()) {
                return rs.getInt(1) > 0;
            }




        } catch (SQLException e) {
            e.printStackTrace();
        }




        return false;
    }
}



