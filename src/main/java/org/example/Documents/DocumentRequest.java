package org.example.Documents;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.DatabaseConnection;
import org.example.Users.Person;

import java.sql.*;
import java.time.LocalDate;
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
        List<DocumentRequest> statusList = new ArrayList<>();

        // Logic: Check status. If Pending & Old (>30 days), count as Rejected.
        String sql = "SELECT status, requestDate, " +
                "CASE " +
                "   WHEN status = 'Pending' AND requestDate < DATE_SUB(NOW(), INTERVAL 1 DAY) THEN 'Rejected' " +
                "   ELSE status " +
                "END AS computedStatus " +
                "FROM document_request";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DocumentRequest r = new DocumentRequest();

                // Use the computed status from SQL
                r.setStatus(rs.getString("computedStatus"));

                // FIX: You must provide the column name for getDate
                r.setRequestDate(rs.getTimestamp("requestDate").toLocalDateTime());

                statusList.add(r);
            }

        } catch (SQLException e) {
            System.out.println("❌ Error fetching status data:");
            e.printStackTrace();
        }
        return statusList;
    }
}