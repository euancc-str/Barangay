package org.example.treasurer;


import java.time.LocalDate;
import java.util.*;
import java.sql.*;


public class FinancialService {
    private FinancialDAO financialDAO;


    public FinancialService() {
        this.financialDAO = new FinancialDAO();
    }


    // Daily methods
    public List<Object[]> getDailySummary() {
        return financialDAO.getDailySummary();
    }


    public List<Object[]> getTransactionsByDate(LocalDate date) {
        return financialDAO.getPaymentsByDate(java.sql.Date.valueOf(date));
    }


    public List<Object[]> getTodaysTransactions() {
        return financialDAO.getTodaysPayments();
    }


    public double getTotalIncome(String timeframe) {
        return financialDAO.getTotalIncome(timeframe);
    }


    // Monthly methods - BROKEN DOWN BY DOCUMENT TYPE
    public List<Object[]> getMonthlySummary() {
        return financialDAO.getMonthlySummary();
    }


    // NEW: Get monthly breakdown by document type
    public List<Object[]> getMonthlyBreakdownByDocumentType(int year, int month) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT " +
                "dt.name as document_type, " +
                "COUNT(r.requestId) as transaction_count, " +
                "SUM(r.totalFee) as total_amount " +
                "FROM document_request r " +
                "JOIN document_type dt ON r.docTypeId = dt.docTypeId " +
                "WHERE r.paymentStatus = 'Paid' " +
                "AND YEAR(r.updatedAt) = ? " +
                "AND MONTH(r.updatedAt) = ? " +
                "GROUP BY dt.name " +
                "ORDER BY total_amount DESC";


        try (Connection conn = org.example.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setInt(1, year);
            stmt.setInt(2, month);


            ResultSet rs = stmt.executeQuery();


            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("document_type"),
                        rs.getInt("transaction_count"),
                        rs.getDouble("total_amount")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    // NEW: Get overall monthly breakdown (all months with document type counts)
    public List<Object[]> getOverallMonthlyBreakdown() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT " +
                "DATE_FORMAT(updatedAt, '%M %Y') as month_year, " +
                "COUNT(requestId) as transaction_count, " +
                "SUM(totalFee) as monthly_total, " +
                "(SELECT COUNT(DISTINCT docTypeId) FROM document_request dr2 " +
                " WHERE dr2.paymentStatus = 'Paid' " +
                " AND DATE_FORMAT(dr2.updatedAt, '%Y-%m') = DATE_FORMAT(dr.updatedAt, '%Y-%m')) as doc_types_count " +
                "FROM document_request dr " +
                "WHERE paymentStatus = 'Paid' " +
                "GROUP BY DATE_FORMAT(updatedAt, '%Y-%m') " +
                "ORDER BY YEAR(updatedAt) DESC, MONTH(updatedAt) DESC";


        try (Connection conn = org.example.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {


            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("month_year"),
                        rs.getInt("transaction_count"),
                        rs.getDouble("monthly_total"),
                        rs.getInt("doc_types_count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public List<Object[]> getTransactionsByMonthYear(int year, int month) {
        return financialDAO.getTransactionsByMonthYear(year, month);
    }


    public double getTotalIncomeForMonth(int year, int month) {
        return financialDAO.getTotalIncomeForMonth(year, month);
    }


    // Yearly methods - BROKEN DOWN BY MONTH
    public List<Object[]> getYearlySummary() {
        return financialDAO.getYearlySummary();
    }


    // NEW: Get yearly breakdown by month
    public List<Object[]> getYearlyBreakdownByMonth(int year) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT " +
                "MONTHNAME(updatedAt) as month_name, " +
                "COUNT(requestId) as transaction_count, " +
                "SUM(totalFee) as total_amount " +
                "FROM document_request " +
                "WHERE paymentStatus = 'Paid' " +
                "AND YEAR(updatedAt) = ? " +
                "GROUP BY MONTH(updatedAt), MONTHNAME(updatedAt) " +
                "ORDER BY MONTH(updatedAt)";


        try (Connection conn = org.example.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setInt(1, year);
            ResultSet rs = stmt.executeQuery();


            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("month_name"),
                        rs.getInt("transaction_count"),
                        rs.getDouble("total_amount")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    // NEW: Get overall yearly breakdown (all years with month counts)
    public List<Object[]> getOverallYearlyBreakdown() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT " +
                "YEAR(updatedAt) as year, " +
                "COUNT(requestId) as transaction_count, " +
                "SUM(totalFee) as yearly_total, " +
                "COUNT(DISTINCT MONTH(updatedAt)) as months_active, " +
                "(SELECT COUNT(DISTINCT docTypeId) FROM document_request dr2 " +
                " WHERE dr2.paymentStatus = 'Paid' " +
                " AND YEAR(dr2.updatedAt) = YEAR(dr.updatedAt)) as doc_types_count " +
                "FROM document_request dr " +
                "WHERE paymentStatus = 'Paid' " +
                "GROUP BY YEAR(updatedAt) " +
                "ORDER BY year DESC";


        try (Connection conn = org.example.DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {


            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("year"),
                        rs.getInt("transaction_count"),
                        rs.getDouble("yearly_total"),
                        rs.getInt("months_active"),
                        rs.getInt("doc_types_count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public double getTotalIncomeForYear(int year) {
        double total = 0;
        for (int month = 1; month <= 12; month++) {
            total += financialDAO.getTotalIncomeForMonth(year, month);
        }
        return total;
    }


    // Utility methods
    public boolean deleteRequest(int requestId) {
        return financialDAO.deleteRequest(requestId);
    }


    public String getRequestDetails(int requestId) {
        return financialDAO.getRequestDetails(requestId);
    }


    public boolean requestExists(int requestId) {
        return financialDAO.requestExists(requestId);
    }
}

