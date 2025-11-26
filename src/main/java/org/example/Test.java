package org.example;


import org.example.Users.Resident;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class Test {
    public static void main(String[] args) {
        ResidentDAO dao = new ResidentDAO();
        List<Resident> residents = dao.getAllResidents();

        if (residents.isEmpty()) {
            System.out.println("⚠️ No residents found in the database.");
        } else {
            System.out.println("✅ Residents found: " + residents.size());
            for (Resident r : residents) {
                System.out.println("👤 " + r.getFirstName()
                        + " | Gender: " + r.getGender()
                        + " | Email: " + r.getEmail()
                        + " | Username: " + r.getUsername());
            }
        }
    }
}
