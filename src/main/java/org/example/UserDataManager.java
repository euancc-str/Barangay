package org.example;

import lombok.Getter;
import lombok.Setter;
import org.example.Documents.DocumentRequest;
import org.example.Documents.DocumentType;
import org.example.Documents.Payment;
import org.example.Users.BarangayStaff;
import org.example.Users.Resident;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class to manage user data across the application
 * This simulates a database in memory
 */
public class UserDataManager {
    private static UserDataManager instance;
    private List<Resident> residents;
    private List<BarangayStaff> staff;

    private UserDataManager() {
        residents = new ArrayList<>();
        staff = new ArrayList<>();
    }

    public static UserDataManager getInstance() {
        if (instance == null) {
            instance = new UserDataManager();
        }
        return instance;
    }

    public void addStaff(BarangayStaff staff) {
        String sql = "INSERT INTO barangay_staff (" +
                "firstName, lastName, position, contactNo, email, username, password, " +
                "role, status, department, lastLogin, createdAt, updatedAt, address, idNumber, birthDate" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staff.getFirstName());
            pstmt.setString(2, staff.getLastName());
            pstmt.setString(3, staff.getPosition());
            pstmt.setString(4, staff.getContactNo());
            pstmt.setString(5, staff.getEmail());
            pstmt.setString(6, staff.getUsername());
            pstmt.setString(7, staff.getPassword());
            pstmt.setString(8, staff.getRole());
            pstmt.setString(9, staff.getStatus());
            pstmt.setString(10, staff.getDepartment());

            pstmt.setTimestamp(11, staff.getLastLogin() != null
                    ? Timestamp.valueOf(staff.getLastLogin())
                    : Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(12, staff.getCreatedAt() != null
                    ? Timestamp.valueOf(staff.getCreatedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(13, staff.getUpdatedAt() != null
                    ? Timestamp.valueOf(staff.getUpdatedAt())
                    : Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(14, staff.getAddress());
            pstmt.setString(15, staff.getStaffId());
            pstmt.setDate(16, staff.getDob() != null
                    ? Date.valueOf(staff.getDob())
                    : null);
            pstmt.executeUpdate();
            System.out.println("✅ Staff saved successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Error saving staff: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void residentRequestsDocument(Resident resident, BarangayStaff barangayStaff, DocumentType documentType,String purpose) {
        String sql = "INSERT INTO document_request (" +
                "residentId, docTypeId, staffId, purpose, requestDate, " +
                "status, releasedDate, referenceNo, remarks, priorityLevel, " +
                "expiryDate, pickupMethod, createdAt, updatedAt, totalFee,paymentStatus" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, resident.getResidentId());
            pstmt.setInt(2, documentType.getDocTypeId());
            if (barangayStaff == null) {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(3, Integer.parseInt(barangayStaff.getStaffId()));
            }
            pstmt.setString(4, purpose);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now())); // requestDate
            pstmt.setString(6, "Pending"); // initial status
            pstmt.setTimestamp(7, null);
            pstmt.setString(8, "REF-" + System.currentTimeMillis()); // auto-generate referenceNo
            pstmt.setString(9, "Awaiting approval"); // remarks
            pstmt.setString(10, "Normal"); // priorityLevel
            pstmt.setTimestamp(11, null); // expiryDate (optional)
            pstmt.setString(12, "Pickup"); // pickupMethod
            pstmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now())); // createdAt
            pstmt.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now())); // updatedAt
            pstmt.setDouble(15,documentType.getFee());
            pstmt.setString(16,"Unpaid");
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Document request created successfully!");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error saving document request: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public boolean staffOperations(BarangayStaff barangayStaff, String status, int residentId) {
        String selectSql = "SELECT requestId, referenceNo FROM document_request " +
                "WHERE residentId = ? AND status = 'Pending' " +
                "ORDER BY createdAt DESC LIMIT 1";

        // 2. Update the request with staff assignment
        String updateSql = "UPDATE document_request " +
                "SET staffId = ?, status = ?, updatedAt = ? " +
                "WHERE requestId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            // Find the document request
            selectStmt.setInt(1, residentId); // ← Use residentId instead of name!
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int requestId = rs.getInt("requestId");
                String existingRef = rs.getString("referenceNo");

                // Parse staffId to int
                int staffId;
                try {
                    String staffIdStr = barangayStaff.getStaffId().replace("-", "");
                    staffId = Integer.parseInt(staffIdStr);
                } catch (NumberFormatException e) {
                    System.err.println("❌ Invalid staffId format: " + barangayStaff.getStaffId());
                    return false;
                }

                // Update the document request
                updateStmt.setInt(1, staffId);
                updateStmt.setString(2, status);
                updateStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                updateStmt.setInt(4, requestId);

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("✅ Request " + existingRef + " assigned to " +
                            barangayStaff.getFirstName() + ". Status: " + status);
                    return true;
                } else {
                    System.out.println("⚠️ Failed to update request.");
                    return false;
                }
            } else {
                System.out.println("⚠️ No pending request found for resident ID: " + residentId);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error in staff operations: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public void addPayment(DocumentType documentType, DocumentRequest documentRequest,BarangayStaff staff) {
        String sql = "INSERT INTO payment (" +
                "requestId, amount, paymentMethod, orNumber, datePaid, cashier, discount, " +
                "status, remarks, referenceNo, transactionId, paymentChannel, createdAt, updatedAt" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Payment payment = new Payment().generateData();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, documentRequest.getRequestId());
            pstmt.setDouble(2, documentType.getFee());
            pstmt.setString(3, "Payment Method: CASH");
            pstmt.setString(4, payment.getOrNumber());
            pstmt.setTimestamp(5, Timestamp.valueOf(documentRequest.getUpdatedAt() != null ?
                    documentRequest.getUpdatedAt() : LocalDateTime.now()));
            pstmt.setString(6, staff.getFirstName() + " " + staff.getMiddleName() + " " +staff.getLastName());
            pstmt.setDouble(7, 0);//discount
            pstmt.setString(8, documentRequest.getStatus());
            pstmt.setString(9, documentRequest.getRemarks());
            pstmt.setString(10, documentRequest.getReferenceNo());
            pstmt.setString(11, payment.getTransactionId());
            pstmt.setString(12, payment.getPaymentChannel());
            pstmt.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now())); // createdAt
            pstmt.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now())); // updatedAt

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Payment record added successfully!");
            } else {
                System.out.println("⚠️ No rows inserted into payment table.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error adding payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addResident(Resident resident) {
        String sql = "INSERT INTO resident (" +
                "firstName, lastName, gender, username, contactNo, email, password, " +
                "age, voterStatus, householdNo, nationalId, photoPath, " +
                "position, createdAt, updatedAt, status, address,birthDate,purok,street,middleName,civilStatus" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resident.getFirstName());
            pstmt.setString(2, resident.getLastName());
            pstmt.setString(3, resident.getGender());
            pstmt.setString(4, resident.getUsername());
            pstmt.setString(5, resident.getContactNo());
            pstmt.setString(6, resident.getEmail());
            pstmt.setString(7, resident.getPassword());
            pstmt.setInt(8, resident.getAge());
            pstmt.setString(9, resident.getVoterStatus());
            pstmt.setString(10, resident.getHouseholdNo());
            pstmt.setString(11, resident.getNationalId());
            pstmt.setInt(12, resident.getPhotoID());
            pstmt.setString(13, resident.getPosition());
            pstmt.setTimestamp(14, Timestamp.valueOf(resident.getCreatedAt()));
            pstmt.setTimestamp(15, Timestamp.valueOf(resident.getUpdatedAt()));
            pstmt.setString(16, resident.getStatus());
            pstmt.setString(17, resident.getAddress());
            pstmt.setDate(18, Date.valueOf(resident.getDob()));
            pstmt.setString(19,resident.getPurok());
            pstmt.setString(20,resident.getStreet());
            pstmt.setString(21,resident.getMiddleName());
            pstmt.setString(22,resident.getCivilStatus());
            pstmt.executeUpdate();
            System.out.println("✅ Resident saved successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error saving resident: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public Resident validateResidentLogin(String username, String password) {
        String sql = "SELECT * FROM resident WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password); // hash

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Resident.builder()
                        .residentId(rs.getInt("residentID"))
                        .firstName(rs.getString("firstName"))
                        .lastName(rs.getString("lastName"))
                        .gender(rs.getString("gender"))
                        .username(rs.getString("username"))
                        .contactNo(rs.getString("contactNo"))
                        .address(rs.getString("address"))
                        .email(rs.getString("email"))
                        .password(rs.getString("password"))
                        .age(rs.getInt("age"))
                        .voterStatus(rs.getString("voterStatus"))
                        .householdNo(rs.getString("householdNo"))
                        .nationalId(rs.getString("nationalId"))
                        .photoID(rs.getInt("photoID"))
                        .suffix(rs.getString("suffix"))
                        .civilStatus(rs.getString("civilStatus"))
                        .middleName(rs.getString("middleName"))
                        .dob(rs.getDate("birthDate").toLocalDate())
                        .position(rs.getString("position"))
                        .createdAt(rs.getTimestamp("createdAt").toLocalDateTime())
                        .updatedAt(rs.getTimestamp("updatedAt") != null ?
                                rs.getTimestamp("updatedAt").toLocalDateTime() : null)
                        .status(rs.getString("status"))
                        .build();
            }

        } catch (SQLException e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Login failed
    }
    private static void logIn(String username, String password, String role){
        String updateSql = "UPDATE barangay_staff SET lastLogin = ? WHERE username = ? AND password = ? AND staffId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement update = conn.prepareStatement(updateSql)){
            update.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            update.setString(2, username);
            update.setString(3, password);
            update.setInt(4, Integer.parseInt(role));
            int rowsAffected =  update.executeUpdate();
            System.out.println("updated "+rowsAffected);
        }catch (SQLException e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public BarangayStaff validateStaffLogin(String username, String password) {
        String sql = "SELECT * FROM barangay_staff WHERE username = ? AND password = ?";

        // We will store the found user here
        BarangayStaff foundStaff = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 1. EXTRACT DATA IMMEDIATELY (While connection is still open)
                    foundStaff = BarangayStaff.builder()
                            .staffId(rs.getString("staffId"))
                            .firstName(rs.getString("firstName"))
                            .lastName(rs.getString("lastName"))
                            .position(rs.getString("position"))
                            .idNumber(rs.getString("idNumber"))
                            .contactNo(rs.getString("contactNo"))
                            .email(rs.getString("email"))
                            .username(rs.getString("username"))
                            .password(rs.getString("password"))
                            .middleName(rs.getString("middleName"))
                            .role(rs.getString("role"))
                            .status(rs.getString("status"))
                            .civilStatus(rs.getString("civilStatus"))
                            .department(rs.getString("department"))
                            .suffix(rs.getString("suffix") != null ? rs.getString("suffix") : "") // Default to empty string
                            .sex(rs.getString("sex") != null ? rs.getString("sex") : "Male")      // Default to Male/Female
                            .citizenship(rs.getString("citizenship") != null ? rs.getString("citizenship") : "Filipino")
                            .dob(rs.getDate("birthDate") != null ? rs.getDate("birthDate").toLocalDate() : null)
                            .lastLogin(LocalDateTime.now())
                            .createdAt(rs.getTimestamp("createdAt") != null ? rs.getTimestamp("createdAt").toLocalDateTime() : null)
                            .updatedAt(rs.getTimestamp("updatedAt") != null ? rs.getTimestamp("updatedAt").toLocalDateTime() : null)
                            .address(rs.getString("address"))
                            .build();

                    System.out.println("✅ User found, data extracted.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Fetch error: " + e.getMessage());
            e.printStackTrace();
        }

        if (foundStaff != null) {
            logIn(username, password,foundStaff.getStaffId());
            System.out.println("✅ Login Timestamp Updated.");
        }

        return foundStaff;
    }

    // Check if username exists

    @Getter
    private Resident currentResident;
    @Getter
    private BarangayStaff currentStaff;

    public void setCurrentResident(Resident resident) {
        this.currentResident = resident;
        System.out.println("Current resident set: " + resident.getFirstName() + " " + resident.getLastName());
    }

    public void setCurrentStaff(BarangayStaff staff) {
        this.currentStaff = staff;
        System.out.println("Current staff set: " + staff.getUsername());
    }

    // Logout (clear current user)
    public void logout() {
        currentResident = null;
        currentStaff = null;

        System.out.println("User logged out");
    }


    public void updateResident(Resident resident) {
        String sql = "UPDATE resident SET firstName = ?, gender = ?, email = ?, contactNo = ?, " +
                " voterStatus = ?, householdNo = ?, nationalId = ?, " +
                "position = ?, age = ?, status = ?, updatedAt = ?, address = ?,middleName = ?,suffix = ?,civilStatus = ? WHERE residentId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resident.getFirstName());
            pstmt.setString(2, resident.getGender());
            pstmt.setString(3, resident.getEmail());
            pstmt.setString(4, resident.getContactNo());
            pstmt.setString(5, resident.getVoterStatus());
            pstmt.setString(6, resident.getHouseholdNo());
            pstmt.setString(7, resident.getNationalId());
            pstmt.setString(8, resident.getPosition());
            pstmt.setInt(9, resident.getAge());
            pstmt.setString(10, resident.getStatus());
            pstmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(12,resident.getAddress());
            pstmt.setString(13,resident.getMiddleName());
            pstmt.setString(14,resident.getSuffix());
            pstmt.setString(15,resident.getCivilStatus());
            pstmt.setInt(16, resident.getResidentId());

            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("✅ Updated " + rowsUpdated + " row(s) successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error updating resident: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void updateStaff(BarangayStaff staff) {
        String sql = "UPDATE barangay_staff SET " +
                "firstName = ?, lastName = ?, position = ?, contactNo = ?, email = ?, " +
                "username = ?, password = ?, role = ?, status = ?, department = ?, " +
                "lastLogin = ?, updatedAt = ?, address = ?, birthDate = ?,citizenship = ?,suffix = ?,civilStatus=? " +
                "WHERE idNumber = ?";
        System.out.println(staff);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staff.getFirstName());
            pstmt.setString(2, staff.getLastName());
            pstmt.setString(3, staff.getPosition());
            pstmt.setString(4, staff.getContactNo());
            pstmt.setString(5, staff.getEmail());
            pstmt.setString(6, staff.getUsername());
            pstmt.setString(7, staff.getPassword());
            pstmt.setString(8, staff.getRole());
            pstmt.setString(9, staff.getStatus());
            pstmt.setString(10, staff.getDepartment());

            // Handle nullable timestamps safely
            pstmt.setTimestamp(11, staff.getLastLogin() != null
                    ? Timestamp.valueOf(staff.getLastLogin())
                    : null);

            //updatedAt
            pstmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.setString(13, staff.getAddress());

            pstmt.setDate(14, staff.getDob() != null
                    ? Date.valueOf(staff.getDob())
                    : null);
            pstmt.setString(15,staff.getCitizenship());
            pstmt.setString(16,staff.getSuffix());
            pstmt.setString(17,staff.getCivilStatus());

            pstmt.setString(18, staff.getIdNumber());

            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("✅ Updated " + rowsUpdated + " staff record(s) successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error updating staff: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public DocumentType getDocumentTypeByName(String docName) {
        String sql = "SELECT * FROM document_type WHERE name = ?";
        DocumentType doc = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, docName); // Set the name you're searching for

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    doc = DocumentType.builder()
                            .docTypeId(rs.getInt("docTypeId"))
                            .name(rs.getString("name"))
                            .fee(rs.getDouble("fee"))
                            .processingTime(rs.getInt("processingTime"))
                            .validityPeriod(rs.getInt("validityPeriod"))
                            .category(rs.getString("category"))
                            .description(rs.getString("description"))
                            .requiresSignature(rs.getBoolean("requiresSignature"))
                            .formatTemplate(rs.getString("formatTemplate"))
                            .issuingAuthority(rs.getString("issuingAuthority"))
                            .renewable(rs.getBoolean("renewable"))
                            .copiesAllowed(rs.getInt("copiesAllowed"))
                            .documentCode(rs.getString("documentCode"))
                            .createdAt(rs.getTimestamp("createdAt").toLocalDateTime())
                            .updatedAt(rs.getTimestamp("updatedAt").toLocalDateTime())
                            .build();

                } else {
                    System.out.println("⚠️ No document found with name: " + docName);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error reading document type: " + e.getMessage());
            e.printStackTrace();
        }

        return doc;
    }
    @Getter
    @Setter
    String purpose;
    @Getter
    @Setter
    String fullName;
    @Getter
    @Setter
    int residentId;
    @Getter
    @Setter
    String address;
    @Getter
    @Setter
    int age;
    @Getter
    @Setter
    LocalDateTime reqDate;

}