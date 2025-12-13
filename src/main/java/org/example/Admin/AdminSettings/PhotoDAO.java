package org.example.Admin.AdminSettings; // Use your package

public class PhotoDAO {

    public void updateResidentPhoto(int residentId, String newPhotoPath) {
        String sql = "UPDATE resident SET photoPath = ? WHERE residentId = ?";

        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPhotoPath);
            stmt.setInt(2, residentId);
            stmt.executeUpdate();

            System.out.println("Photo updated for Resident ID: " + residentId + " path:" + newPhotoPath);

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public String getPhotoPath(int residentId) {
        String sql = "SELECT photoPath FROM resident WHERE residentId = ?";
        try (java.sql.Connection conn = org.example.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, residentId);
            java.sql.ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                return rs.getString("photoPath");
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return null;
    }
}