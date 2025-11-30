package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {


    public static Connection getConnection() {
        Connection connection = null;

        try {
            // 1. THE ZOMBIE CHECK: Check if null OR closed
            if (connection == null || connection.isClosed()) {

                // 2. LOAD CONFIGURATION (The "Flexible" Part)
                try {
                    Properties props = new Properties();
                    // This looks for the file in the main project folder
                    FileInputStream fis = new FileInputStream("config.properties");
                    props.load(fis);

                    String url = props.getProperty("db.url");
                    String user = props.getProperty("db.username");
                    String pass = props.getProperty("db.password");

                    // 3. CONNECT
                    connection = DriverManager.getConnection(url, user, pass);
                    fis.close();

                } catch (IOException e) {
                    System.err.println("❌ ERROR: config.properties file missing!");
                    System.err.println("   Please create 'config.properties' in the project folder.");

                    // FALLBACK (Optional): If file is missing, try default settings so app doesn't crash completely
                    connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/barangay_db", "root", "");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Connection Error: " + e.getMessage());
            e.printStackTrace();
        }

        return connection;
    }
}