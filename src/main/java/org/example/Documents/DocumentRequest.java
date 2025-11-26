package org.example.Documents;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.DatabaseConnection;
import org.example.Users.Person;
import org.example.Users.Resident;

import javax.print.Doc;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class DocumentRequest extends Person {
    private int requestId;         // PK
    private int residentId;        // FK
    private int docTypeId;         // FK
    private int staffId;           // FK
    private String purpose;
    private String name;
    private String fullName;
    private LocalDateTime requestDate;
    private String status;         // Pending, Processing, Released, Denied
    private LocalDateTime releasedDate;
    private String referenceNo;
    private String remarks;
    private double totalFee;
    private boolean paid;
    private String paymentStatus;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public List<DocumentRequest> displayStatusData() {
        List<DocumentRequest> status = new ArrayList<>();

        String sql = "SELECT document_request.status FROM document_request";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DocumentRequest r = new DocumentRequest();
                r.setStatus(rs.getString("status"));
                status.add(r);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching residents:");
            e.printStackTrace();
        }
        return status;
    }
}