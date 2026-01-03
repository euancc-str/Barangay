package org.example;

import org.example.Users.BusinessEstablishment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BusinessDAO {


    public List<BusinessEstablishment> getAllBusinesses() {
        List<BusinessEstablishment> list = new ArrayList<>();
        String sql = "SELECT b.*, CONCAT(r.firstName, ' ', r.lastName) AS ownerName " +
                "FROM business_establishment b " +
                "LEFT JOIN resident r ON b.ownerId = r.residentId " +
                "ORDER BY b.businessId DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BusinessEstablishment b = new BusinessEstablishment();
                b.setBusinessId(rs.getInt("businessId"));
                b.setOwnerId(rs.getInt("ownerId"));
                b.setOwnerName(rs.getString("ownerName")); // From JOIN
                b.setBusinessName(rs.getString("businessName"));
                b.setBusinessNature(rs.getString("businessNature"));
                b.setOwnershipType(rs.getString("ownershipType"));
                b.setPurok(rs.getString("purok"));
                b.setStreetAddress(rs.getString("streetAddress"));

                // Handle Dates Safely
                Date estDate = rs.getDate("dateEstablished");
                if(estDate != null) b.setDateEstablished(estDate.toLocalDate());

                b.setEmployeeCount(rs.getInt("employeeCount"));
                b.setBuildingType(rs.getString("buildingType"));
                b.setPermitStatus(rs.getString("permitStatus"));
                b.setPermitNumber(rs.getString("permitNumber"));

                Date renewDate = rs.getDate("lastRenewalDate");
                if(renewDate != null) b.setLastRenewalDate(renewDate.toLocalDate());

                b.setCapitalInvestment(rs.getDouble("capitalInvestment"));
                b.setElectricitySource(rs.getString("electricitySource"));

                list.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. ADD BUSINESS
    public boolean addBusiness(BusinessEstablishment b) {
        String sql = "INSERT INTO business_establishment " +
                "(ownerId, businessName, businessNature, ownershipType, purok, streetAddress, " +
                "dateEstablished, employeeCount, buildingType, permitStatus, permitNumber, " +
                "lastRenewalDate, capitalInvestment, electricitySource) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, b.getOwnerId());
            ps.setString(2, b.getBusinessName());
            ps.setString(3, b.getBusinessNature());
            ps.setString(4, b.getOwnershipType());
            ps.setString(5, b.getPurok());
            ps.setString(6, b.getStreetAddress());
            ps.setDate(7, b.getDateEstablished() != null ? Date.valueOf(b.getDateEstablished()) : null);
            ps.setInt(8, b.getEmployeeCount());
            ps.setString(9, b.getBuildingType());
            ps.setString(10, b.getPermitStatus());
            ps.setString(11, b.getPermitNumber());
            ps.setDate(12, b.getLastRenewalDate() != null ? Date.valueOf(b.getLastRenewalDate()) : null);
            ps.setDouble(13, b.getCapitalInvestment());
            ps.setString(14, b.getElectricitySource());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. UPDATE BUSINESS
    public boolean updateBusiness(BusinessEstablishment b) {
        String sql = "UPDATE business_establishment SET " +
                "businessName=?, businessNature=?, ownershipType=?, purok=?, streetAddress=?, " +
                "employeeCount=?, buildingType=?, permitStatus=?, permitNumber=?, " +
                "capitalInvestment=?, electricitySource=? " + // Note: Usually we don't update Owner or Established Date easily
                "WHERE businessId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, b.getBusinessName());
            ps.setString(2, b.getBusinessNature());
            ps.setString(3, b.getOwnershipType());
            ps.setString(4, b.getPurok());
            ps.setString(5, b.getStreetAddress());
            ps.setInt(6, b.getEmployeeCount());
            ps.setString(7, b.getBuildingType());
            ps.setString(8, b.getPermitStatus());
            ps.setString(9, b.getPermitNumber());
            ps.setDouble(10, b.getCapitalInvestment());
            ps.setString(11, b.getElectricitySource());
            ps.setInt(12, b.getBusinessId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. DELETE BUSINESS
    public boolean deleteBusiness(int id) {
        String sql = "DELETE FROM business_establishment WHERE businessId=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}