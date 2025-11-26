package org.example;

import org.example.Documents.DocumentRequest;
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
                r.setPhoneNumber(rs.getString("phoneNumber"));
                r.setVoterStatus(rs.getString("voterStatus"));
                r.setHouseholdNo(rs.getString("householdNo"));
                r.setNationalId(rs.getString("nationalId"));
                r.setPosition(rs.getString("position"));
                r.setStatus(rs.getString("status"));
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
    public List<DocumentRequest> getAllResidentsDocument() {
        List<DocumentRequest> residents = new ArrayList<>();

        String sql = "SELECT \n" +
                "    CONCAT(resident.firstName, ' ', resident.lastName) AS fullName,\n" +
                "    document_request.status,document_request.requestId,document_request.purpose,\n" +
                "    document_request.requestDate,document_request.remarks,document_type.name  AS documentType\n" +
                "FROM resident\n" +
                "JOIN document_request \n" +
                "    ON resident.residentId = document_request.residentId\n" +
                "JOIN document_type\n" +
                "    ON document_request.docTypeId = document_type.docTypeId;";
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
                r.setRequestDate(requestDate.toLocalDateTime());
                r.setRemarks(rs.getString("remarks"));
                residents.add(r);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching residents:");
            e.printStackTrace();
        }
        return residents;
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
                    Date stamp = rs.getDate("birthDate");
                   resident = Resident.builder()
                           .firstName(rs.getString("firstName"))
                           .lastName(rs.getString("lastName"))
                           .residentId(rs.getInt("residentId"))
                           .age(rs.getInt("age"))
                           .address(rs.getString("address"))
                           .dob(stamp.toLocalDate())
                           .civilStatus(rs.getString("civilStatus") != null ? rs.getString("civilStatus"):"Empty")
                           .gender(rs.getString("gender"))
                           .middleName(rs.getString("middleName")!= null ? rs.getString("middleName"):"Empty")
                           .suffix(rs.getString("suffix"))

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

}
