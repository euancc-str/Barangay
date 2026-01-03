package org.example;

import org.example.Users.BlotterCase;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BlotterCaseDAO {

    // --- 1. CREATE (Add New Case) ---
    public void addBlotterCase(BlotterCase bCase) {
        String sql = "INSERT INTO blotter_case (caseNumber, dateRecorded, timeRecorded, complainant, respondent, " +
                "victim, incidentType, location, narrative, witnesses, status, hearingDate, officerInCharge, resolution) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bCase.getCaseNumber());
            ps.setDate(2, Date.valueOf(bCase.getDateRecorded())); // Convert LocalDate to SQL Date
            ps.setTime(3, Time.valueOf(bCase.getTimeRecorded())); // Convert LocalTime to SQL Time
            ps.setString(4, bCase.getComplainant());
            ps.setString(5, bCase.getRespondent());
            ps.setString(6, bCase.getVictim());
            ps.setString(7, bCase.getIncidentType());
            ps.setString(8, bCase.getLocation());
            ps.setString(9, bCase.getNarrative());
            ps.setString(10, bCase.getWitnesses());
            ps.setString(11, bCase.getStatus());

            // Handle Nullable Hearing Date
            if (bCase.getHearingDate() != null) {
                ps.setDate(12, Date.valueOf(bCase.getHearingDate()));
            } else {
                ps.setNull(12, Types.DATE);
            }

            ps.setString(13, bCase.getOfficerInCharge());
            ps.setString(14, bCase.getResolution());

            ps.executeUpdate();
            System.out.println("✅ Blotter Case Added Successfully: " + bCase.getCaseNumber());

        } catch (SQLException e) {
            System.out.println("❌ Error adding blotter case:");
            e.printStackTrace();
        }
    }


    public boolean isCaseNumberExists(String caseNumber) {
        String sql = "SELECT caseId FROM blotter_case WHERE caseNumber = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, caseNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<BlotterCase> getAllBlotterCases() {
        List<BlotterCase> cases = new ArrayList<>();
        String sql = "SELECT * FROM blotter_case ORDER BY dateRecorded DESC, timeRecorded DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BlotterCase b = mapResultSetToBlotterCase(rs);
                cases.add(b);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching blotter cases:");
            e.printStackTrace();
        }
        return cases;
    }

    // --- 3. SEARCH (Find by Name or Case No) ---
    public List<BlotterCase> searchBlotterCases(String query) {
        List<BlotterCase> cases = new ArrayList<>();
        String sql = "SELECT * FROM blotter_case WHERE caseNumber LIKE ? OR complainant LIKE ? OR respondent LIKE ? OR status LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String search = "%" + query + "%";
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cases.add(mapResultSetToBlotterCase(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Error searching blotter cases:");
            e.printStackTrace();
        }
        return cases;
    }
    // --- DELETE CASE ---
    public boolean deleteBlotterCase(int id) {
        String sql = "DELETE FROM blotter_case WHERE caseId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBlotterCase(BlotterCase bCase) {
        String sql = "UPDATE blotter_case SET caseNumber=?, dateRecorded=?, timeRecorded=?, " +
                "complainant=?, respondent=?, victim=?, incidentType=?, location=?, " +
                "narrative=?, witnesses=?, status=?, hearingDate=?, officerInCharge=?, resolution=? " +
                "WHERE caseId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bCase.getCaseNumber());
            ps.setDate(2, java.sql.Date.valueOf(bCase.getDateRecorded()));
            ps.setTime(3, java.sql.Time.valueOf(bCase.getTimeRecorded()));
            ps.setString(4, bCase.getComplainant());
            ps.setString(5, bCase.getRespondent());
            ps.setString(6, bCase.getVictim());
            ps.setString(7, bCase.getIncidentType());
            ps.setString(8, bCase.getLocation());
            ps.setString(9, bCase.getNarrative());
            ps.setString(10, bCase.getWitnesses());
            ps.setString(11, bCase.getStatus());


            if (bCase.getHearingDate() != null) {
                ps.setDate(12, java.sql.Date.valueOf(bCase.getHearingDate()));
            } else {
                ps.setNull(12, java.sql.Types.DATE);
            }

            ps.setString(13, bCase.getOfficerInCharge());
            ps.setString(14, bCase.getResolution());

            ps.setInt(15, bCase.getCaseId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateCaseStatus(String caseNumber, String newStatus, String resolution, String hearingDate) {
        String sql = "UPDATE blotter_case SET status = ?, resolution = ?, hearingDate = ? WHERE caseNumber = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, resolution);

            if (hearingDate != null && !hearingDate.isEmpty()) {
                ps.setDate(3, Date.valueOf(hearingDate)); // Assumes String is yyyy-MM-dd
            } else {
                ps.setNull(3, Types.DATE);
            }

            ps.setString(4, caseNumber);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Case Updated: " + caseNumber);
            } else {
                System.out.println("⚠️ Case not found for update: " + caseNumber);
            }
            return true;

        } catch (SQLException e) {
            System.out.println("❌ Error updating case:");
            e.printStackTrace();
        }
        return false;
    }

    // --- 5. READ SINGLE (Find by Case Number) ---
    public BlotterCase findCaseByNumber(String caseNumber) {
        String sql = "SELECT * FROM blotter_case WHERE caseNumber = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, caseNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBlotterCase(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error finding case: " + caseNumber);
            e.printStackTrace();
        }
        return null;
    }
    // Check if a resident has an Unresolved Case (Pending or Scheduled)
    public String getDerogatoryRecord(String fullName) {
        String record = "CLEAN"; // Default

        // We check if they are the RESPONDENT (Suspect) and status is NOT settled
        String sql = "SELECT incidentType, caseNumber,status FROM blotter_case " +
                "WHERE respondent = ? AND status NOT IN ('Settled', 'Closed', 'Dismissed')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fullName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    record = "Resident has " + rs.getString("status") + " case";
                    record = record.toUpperCase();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return record;
    }

    public String checkMultiplePeople(String commaSeparatedNames) {
        if (commaSeparatedNames == null || commaSeparatedNames.trim().isEmpty()) {
            return "CLEAN";
        }


        String[] names = commaSeparatedNames.split(",");

        StringBuilder report = new StringBuilder();
        boolean foundCrime = false;


        for (String rawName : names) {
            String cleanName = rawName.trim();

            if (!cleanName.isEmpty()) {

                String result = getDerogatoryRecord(cleanName);


                if (result != null && !result.equals("CLEAN") && !result.isEmpty()) {
                    foundCrime = true;
                    report.append(result);
                }
            }
        }


        if (foundCrime) {
            return report.toString();
        } else {
            return "CLEAN";
        }
    }
    public BlotterCase getCaseById(int id) {
        String sql = "SELECT * FROM blotter_case WHERE caseId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBlotterCase(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if not found
    }
    private BlotterCase mapResultSetToBlotterCase(ResultSet rs) throws SQLException {

        java.sql.Date sqlDate = rs.getDate("dateRecorded");
        LocalDate recDate = (sqlDate != null) ? sqlDate.toLocalDate() : null;

        java.sql.Time sqlTime = rs.getTime("timeRecorded");
        LocalTime recTime = (sqlTime != null) ? sqlTime.toLocalTime() : null;

        java.sql.Date hearDateSql = rs.getDate("hearingDate");
        LocalDate hearDate = (hearDateSql != null) ? hearDateSql.toLocalDate() : null;

        return BlotterCase.builder()
                .caseId(rs.getInt("caseId"))
                .caseNumber(rs.getString("caseNumber"))
                .dateRecorded(recDate)
                .timeRecorded(recTime)
                .complainant(rs.getString("complainant"))
                .respondent(rs.getString("respondent"))
                .victim(rs.getString("victim"))
                .incidentType(rs.getString("incidentType"))
                .location(rs.getString("location"))
                .narrative(rs.getString("narrative"))
                .witnesses(rs.getString("witnesses"))
                .status(rs.getString("status"))
                .hearingDate(hearDate)
                .officerInCharge(rs.getString("officerInCharge"))
                .resolution(rs.getString("resolution"))
                .build();
    }
}