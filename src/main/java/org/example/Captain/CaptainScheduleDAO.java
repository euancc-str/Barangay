package org.example.Captain;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.example.DatabaseConnection;
import org.example.StaffDAO;
import org.example.Users.BarangayStaff;

public class CaptainScheduleDAO {
    private int currentCaptainId; // Store current captain's ID

    // Constructor with captain ID
    public CaptainScheduleDAO() {
        // Get current captain ID from logged-in user
        this.currentCaptainId = getCurrentCaptainId();
    }

    private int getCurrentCaptainId() {

        try {

            BarangayStaff staff = new StaffDAO().findStaffByPosition("Captain");
            return staff != null ? Integer.parseInt(staff.getStaffId()) : 1;
        } catch (Exception e) {
            return 1; // Default captain ID
        }
    }

    // Check if EXACT schedule already exists
    public boolean isExactScheduleExists(LocalDate date, LocalTime startTime, LocalTime endTime) {
        String sql = "SELECT COUNT(*) FROM captain_schedule WHERE " +
                "captain_id = ? AND schedule_date = ? AND " +
                "start_time = ? AND end_time = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentCaptainId);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setTime(3, Time.valueOf(startTime));
            pstmt.setTime(4, Time.valueOf(endTime));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update the isTimeSlotAvailable method to be more accurate
    public boolean isTimeSlotAvailable(LocalDate date, LocalTime startTime, LocalTime endTime) {
        String sql = "SELECT COUNT(*) FROM captain_schedule WHERE captain_id = ? AND schedule_date = ? AND " +
                "is_available = TRUE AND " +
                "((start_time < ? AND end_time > ?) OR " +  // Overlap condition
                "(start_time <= ? AND end_time >= ?))";      // Exact match or contains

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentCaptainId);
            pstmt.setDate(2, Date.valueOf(date));
            pstmt.setTime(3, Time.valueOf(endTime));    // start_time < endTime (new)
            pstmt.setTime(4, Time.valueOf(startTime));  // end_time > startTime (new)
            pstmt.setTime(5, Time.valueOf(startTime));  // start_time <= startTime (existing)
            pstmt.setTime(6, Time.valueOf(endTime));    // end_time >= endTime (existing)

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<CaptainSchedule> getSchedulesByDate(LocalDate date) {
        List<CaptainSchedule> list = new ArrayList<>();
        String sql = "SELECT schedule_id,captain_id,schedule_date,start_time,end_time,is_available,day_of_week,CONCAT(staff.firstName,' ',staff.middleName,' ',staff.lastName) AS full FROM captain_schedule JOIN barangay_staff staff " +
                "ON staff.staffId = captain_id" +
                " WHERE schedule_date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CaptainSchedule s = new CaptainSchedule();
                s.setCaptainName(rs.getString("full"));
                s.setScheduleId(rs.getInt("schedule_id"));
                s.setCaptainId(rs.getInt("captain_id"));
                s.setScheduleDate(rs.getDate("schedule_date").toLocalDate());

                Time start = rs.getTime("start_time");
                if(start != null) s.setStartTime(start.toLocalTime());

                Time end = rs.getTime("end_time");
                if(end != null) s.setEndTime(end.toLocalTime());

                s.setAvailable(rs.getBoolean("is_available"));
                s.setDayOfWeek(rs.getString("day_of_week"));

                // Note: 'activity' column is missing in your DB, so we skip it.
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    // Add schedule
    public boolean addSchedule(CaptainSchedule schedule) {
        String sql = "INSERT INTO captain_schedule (captain_id, schedule_date, start_time, end_time, is_available, day_of_week) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentCaptainId); // Use current captain ID
            pstmt.setDate(2, Date.valueOf(schedule.getScheduleDate()));
            pstmt.setTime(3, Time.valueOf(schedule.getStartTime()));
            pstmt.setTime(4, Time.valueOf(schedule.getEndTime()));
            pstmt.setBoolean(5, schedule.isAvailable());
            pstmt.setString(6, schedule.getDayOfWeek());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Update schedule
    public boolean updateSchedule(CaptainSchedule schedule) {
        String sql = "UPDATE captain_schedule SET schedule_date = ?, start_time = ?, end_time = ?, " +
                "is_available = ?, day_of_week = ? WHERE schedule_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(schedule.getScheduleDate()));
            pstmt.setTime(2, Time.valueOf(schedule.getStartTime()));
            pstmt.setTime(3, Time.valueOf(schedule.getEndTime()));
            pstmt.setBoolean(4, schedule.isAvailable());
            pstmt.setString(5, schedule.getDayOfWeek());
            pstmt.setInt(6, schedule.getScheduleId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete schedule
    public boolean deleteSchedule(int scheduleId) {
        String sql = "DELETE FROM captain_schedule WHERE schedule_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, scheduleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all schedules
    public List<CaptainSchedule> getAllSchedules() {
        List<CaptainSchedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM captain_schedule WHERE captain_id = ? ORDER BY schedule_date, start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentCaptainId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CaptainSchedule schedule = CaptainSchedule.builder()
                        .scheduleId(rs.getInt("schedule_id"))
                        .captainId(rs.getInt("captain_id"))
                        .scheduleDate(rs.getDate("schedule_date").toLocalDate())
                        .startTime(rs.getTime("start_time").toLocalTime())
                        .endTime(rs.getTime("end_time").toLocalTime())
                        .isAvailable(rs.getBoolean("is_available"))
                        .dayOfWeek(rs.getString("day_of_week"))
                        .build();

                schedules.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    public List<CaptainSchedule> getAvailableSchedulesByDate(LocalDate date) {
        List<CaptainSchedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM captain_schedule WHERE schedule_date = ? AND is_available = TRUE " +
                "ORDER BY start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CaptainSchedule schedule = CaptainSchedule.builder()
                        .scheduleId(rs.getInt("schedule_id"))
                        .scheduleDate(rs.getDate("schedule_date").toLocalDate())
                        .startTime(rs.getTime("start_time").toLocalTime())
                        .endTime(rs.getTime("end_time").toLocalTime())
                        .isAvailable(rs.getBoolean("is_available"))
                        .dayOfWeek(rs.getString("day_of_week"))
                        .build();

                schedules.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    // Get schedules within a date range
    public List<CaptainSchedule> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<CaptainSchedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM captain_schedule WHERE schedule_date BETWEEN ? AND ? " +
                "ORDER BY schedule_date, start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CaptainSchedule schedule = CaptainSchedule.builder()
                        .scheduleId(rs.getInt("schedule_id"))
                        .scheduleDate(rs.getDate("schedule_date").toLocalDate())
                        .startTime(rs.getTime("start_time").toLocalTime())
                        .endTime(rs.getTime("end_time").toLocalTime())
                        .isAvailable(rs.getBoolean("is_available"))
                        .dayOfWeek(rs.getString("day_of_week"))
                        .build();

                schedules.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }


    // Get next available date
    public LocalDate getNextAvailableDate() {
        String sql = "SELECT MIN(schedule_date) FROM captain_schedule " +
                "WHERE is_available = TRUE AND schedule_date >= CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next() && rs.getDate(1) != null) {
                return rs.getDate(1).toLocalDate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return LocalDate.now();
    }
}