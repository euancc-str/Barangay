package org.example;

import org.example.Documents.DocumentRequest;
import org.example.Documents.Payment;
import org.example.Users.Resident;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResidentDAO {
    public List<Resident> getAllResidents() {
        List<Resident> residents = new ArrayList<>();

        String sql = "SELECT * FROM resident";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Resident r = new Resident();
                r.setResidentId(rs.getInt("residentId"));
                r.setFirstName(rs.getString("firstName"));
                r.setLastName(rs.getString("lastName"));
                r.setGender(rs.getString("gender"));
                r.setUsername(rs.getString("username"));
                r.setContactNo(rs.getString("contactNo"));
                r.setEmail(rs.getString("email"));
                r.setPassword(rs.getString("password"));
                r.setAge(rs.getInt("age"));
                r.setPurok(rs.getString("purok"));
                r.setStreet(rs.getString("street"));
                r.setVoterStatus(rs.getString("voterStatus"));
                r.setHouseholdNo(rs.getString("householdNo"));
                r.setNationalId(rs.getString("nationalId"));
                r.setPosition(rs.getString("position"));
                r.setStatus(rs.getString("status"));
                r.setMiddleName(rs.getString("middleName") != null ? rs.getString("middleName"): "");
                r.setAddress(rs.getString("address"));
                // Handle potential NULL timestamps safely
                Timestamp createdAt = rs.getTimestamp("createdAt");
                Timestamp updatedAt = rs.getTimestamp("updatedAt");

                if (createdAt != null) r.setCreatedAt(createdAt.toLocalDateTime());
                if (updatedAt != null) r.setUpdatedAt(updatedAt.toLocalDateTime());

                residents.add(r);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching residents:");
            e.printStackTrace();
        }
        return residents;
    }
    // Add this to org.example.ResidentDAO

    public List<Resident> getPWDResidents() {
        List<Resident> list = new ArrayList<>();
        // Assuming 'isPwd' is the column name and it stores 1 for true
        String sql = "SELECT * FROM resident WHERE isPwd = 1 ORDER BY lastName";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Resident r = new Resident();
                r.setResidentId(rs.getInt("residentId"));
                r.setFirstName(rs.getString("firstName"));
                r.setMiddleName(rs.getString("middleName"));
                r.setLastName(rs.getString("lastName"));
                r.setGender(rs.getString("gender"));
                r.setAge(rs.getInt("age"));
                r.setPurok(rs.getString("purok"));
                r.setAddress(rs.getString("address")); // or street
                r.setContactNo(rs.getString("contactNo"));

                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public String [] returnAllResidentsToArray(){
         List<Resident> list = getAllResidents();
         return list.toArray(new String[0]);
    }
    public int getMaxRequestId() {
        int maxId = 0;
        String sql = "SELECT MAX(requestId) FROM document_request";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                maxId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maxId;
    }

    public List<DocumentRequest> getAllResidentsDocument() {
        List<DocumentRequest> residents = new ArrayList<>();

        String sql = "SELECT \n" +
                "    CONCAT(resident.firstName, ' ',resident.middleName, ' ', resident.lastName) AS fullName,\n" +
                "    photoPath,document_request.status,document_request.requestId,document_request.purpose,\n" +
                "    document_request.requestDate,document_request.updatedAt ,document_request.remarks,document_type.name AS documentType\n" +
                "FROM resident\n" +
                "JOIN document_request \n" +
                "    ON resident.residentId = document_request.residentId\n" +
                "JOIN document_type\n" +
                "    ON document_request.docTypeId = document_type.docTypeId" +
                " ORDER BY document_request.requestId DESC;";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DocumentRequest r = new DocumentRequest();
                r.setFullName(rs.getString("fullName"));
                r.setName(rs.getString("documentType"));
                r.setStatus(rs.getString("status"));
                r.setRequestId(Integer.parseInt(rs.getString("requestId")));
                r.setPurpose(rs.getString("purpose"));
                Timestamp requestDate = rs.getTimestamp("requestDate");
                r.setPhotoPath(rs.getString("photoPath"));
                r.setRequestDate(requestDate.toLocalDateTime());
                r.setRemarks(rs.getString("remarks"));
                Timestamp updatedAt = rs.getTimestamp("updatedAt");
                r.setUpdatedAt(updatedAt.toLocalDateTime());
                residents.add(r);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching residents:");
            e.printStackTrace();
        }
        return residents;
    }
    // Add to ResidentDAO
    private static LocalDateTime lastMaxUpdatedAt = LocalDateTime.MIN;

    public boolean hasNewUpdates() {
        String sql = "SELECT MAX(updatedAt) as latest_update FROM document_request";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Timestamp latest = rs.getTimestamp("latest_update");
                if (latest != null) {
                    LocalDateTime latestTime = latest.toLocalDateTime();
                    if (latestTime.isAfter(lastMaxUpdatedAt)) {
                        lastMaxUpdatedAt = latestTime;
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public DocumentRequest findDocumentRequestById(int requestId) {
        String sql = """
        SELECT dr.requestId,dr.updatedAt,dr.status,dr.remarks,dr.referenceNo,dr.docTypeId,dt.fee
        FROM document_request dr
        JOIN document_type dt
        ON dr.docTypeId = dt.docTypeId
        WHERE requestId = ?
        
    """;

        DocumentRequest purpose = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, requestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp reqDate = rs.getTimestamp("updatedAt");
                    purpose = DocumentRequest.builder()
                            .requestId(rs.getInt("requestId"))
                            .updatedAt(reqDate.toLocalDateTime())
                            .status(rs.getString("status"))
                            .remarks(rs.getString("remarks"))
                            .referenceNo(rs.getString("referenceNo"))
                            .build();
                    System.out.println("request id found!");

                } else {
                    System.out.println("request id not found: "+ requestId);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error finding id: " + e.getMessage());
            e.printStackTrace();
        }

        return purpose;
    }
    public DocumentRequest findPurposeByFullName(String fullName,int requestId) {
        String sql = """
        SELECT dr.purpose, dr.residentId, dr.requestDate,r.address,r.age,r.username
        FROM document_request dr
        JOIN resident r ON dr.residentId = r.residentId
        WHERE CONCAT(r.firstName, ' ', r.lastName) = ? AND requestId = ?
    """;

        DocumentRequest purpose = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setInt(2,requestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp reqDate = rs.getTimestamp("requestDate");
                    purpose = DocumentRequest.builder()
                            .purpose(rs.getString("purpose"))
                            .residentId(rs.getInt("residentId"))
                            .requestDate(reqDate.toLocalDateTime())
                            .address(rs.getString("address"))
                            .age(rs.getInt("age"))
                            .username(rs.getString("username"))
                            .build();
                    System.out.println("✅ Found purpose for " + fullName + ": " + purpose.getPurpose());
                } else {
                    System.out.println("⚠️ No purpose found for " + fullName);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving purpose: " + e.getMessage());
            e.printStackTrace();
        }

        return purpose;
    }
    public String findResidentByFullName(int id) {
        String sql = """
        SELECT username
        FROM resident
        WHERE residentId = ?
    """;

        String name = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1,id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("username");

                } else {
                    System.out.println("⚠️ No id found: " + id);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving purpose: " + e.getMessage());
            e.printStackTrace();
        }

        return name;
    }
    // Add this to ResidentDAO.java
    public boolean isResidentExists(String firstName, String lastName,String middleName) {
        String sql = "SELECT residentId FROM resident WHERE firstName = ? AND lastName = ? AND middleName = ?";
        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3,middleName);
            java.sql.ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns TRUE if a record was found

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Resident findResidentById(int id) {
        String sql = """
        SELECT *
        FROM resident
        WHERE residentId = ?
    """;

        Resident resident = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1,id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date birthDateSql = rs.getDate("birthDate");
                    LocalDate dob = (birthDateSql != null) ? birthDateSql.toLocalDate() : null;

                    // 2. Handle CTC Date safely
                    java.sql.Date ctcDateSql = rs.getDate("ctcDateIssued");
                    LocalDate ctcDate = (ctcDateSql != null) ? ctcDateSql.toLocalDate() : null;
                    Date stamp = rs.getDate("birthDate");
                   resident = Resident.builder()
                           .firstName(rs.getString("firstName"))
                           .lastName(rs.getString("lastName"))
                           .residentId(rs.getInt("residentId"))
                           .age(rs.getInt("age"))
                           .address(rs.getString("address"))
                           .dob(stamp.toLocalDate())
                           .contactNo(rs.getString("contactNo"))
                           .purok(rs.getString("purok"))
                           .isPwd(rs.getInt("isPwd"))
                           .householdNo(rs.getString("householdNo"))
                           .email(rs.getString("email"))
                           .street(rs.getString("street"))
                           .civilStatus(rs.getString("civilStatus") != null ? rs.getString("civilStatus"):"")
                           .gender(rs.getString("gender"))
                           .middleName(rs.getString("middleName")!= null ? rs.getString("middleName"):"")
                           .suffix(rs.getString("suffix"))
                           .ctcNumber(rs.getString("ctcNumber") != null ? rs.getString("ctcNumber") : "")
                           .ctcDateIssued(String.valueOf(ctcDate)) // Pass the safe LocalDate or null
                           .build();


                } else {
                    System.out.println("⚠️ No id found: " + id);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving purpose: " + e.getMessage());
            e.printStackTrace();
        }

        return resident;
    }
    public int findResidentsIdByFullName(String fullName) {
        String sql = """
        SELECT residentId
        FROM resident
        WHERE CONCAT(firstName, ' ',middleName, ' ',lastName) = ?
    """;

        int name = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1,fullName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    name = rs.getInt("residentId");

                } else {
                    System.out.println("⚠️ No id found: " + fullName);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving purpose: " + e.getMessage());
            e.printStackTrace();
        }

        return name;
    }
    public Payment findResidentReceiptById(int requestId){
        String sql = "SELECT amount,orNumber,p.referenceNo " +
                "FROM payment p " +
                "JOIN document_request dr " +
                "ON p.requestId = dr.requestId WHERE p.requestId =? ";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1,requestId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                return Payment.builder()
                        .amount(rs.getDouble("amount"))
                        .orNumber(rs.getString("orNumber"))
                        .referenceNo(rs.getString("referenceNo"))
                        .build();
            }
        }catch (SQLException e){
            System.out.println("error finding payment for request id: "+requestId +"\n" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public void updateResidentCedula(int residentId, String ctcNum, String ctcDate) {
        String sql = "UPDATE resident SET ctcNumber=?, ctcDateIssued=? WHERE residentId=?";
        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Handle ctcNum (optional safety check)
            if (ctcNum == null) {
                stmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, ctcNum);
            }

            // FIX IS HERE: Check for 'null' OR 'empty'
            if (ctcDate == null || ctcDate.trim().isEmpty()) {
                stmt.setNull(2, java.sql.Types.DATE);
            } else {
                // Only try to parse if we are sure it's not null/empty
                stmt.setDate(2, java.sql.Date.valueOf(ctcDate));
            }

            stmt.setInt(3, residentId);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateResidentHouseHold(int residentId, String household){
        String sql = "UPDATE resident SET householdNo=? WHERE residentId=?";
        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, household);
            stmt.setInt(2, residentId);

            stmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

}
