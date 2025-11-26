package org.example;

import org.example.Documents.DocumentRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentRequestDao {
    public List<DocumentRequest> getAllResidentsDocument(String status) {
        List<DocumentRequest> list = new ArrayList<>();

        // 1. The SQL Query (Notice the '?' at the end)
        String sql = "SELECT " +
                "    CONCAT(resident.firstName, ' ', resident.lastName) AS fullName, " +
                "    document_request.status, document_request.requestId, document_type.name AS documentName " +
                "FROM document_request " +
                "JOIN resident ON document_request.residentId = resident.residentId " +
                "JOIN document_type ON document_request.docTypeId = document_type.docTypeId " +
                "WHERE document_request.status = ?ORDER BY `document_request`.`requestId` DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 2. >>> THIS IS THE MISSING LINE <<<
            // This tells MySQL: "Replace '?' with 'Approved' or 'Pending'"
            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DocumentRequest doc = new DocumentRequest();
                    // Map your results
                    doc.setFullName(rs.getString("fullName"));
                    doc.setName(rs.getString("documentName"));
                    doc.setStatus(rs.getString("status"));
                    doc.setRequestId(rs.getInt("requestId"));

                    list.add(doc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
