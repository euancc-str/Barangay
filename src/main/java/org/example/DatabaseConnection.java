package org.example;

import lombok.experimental.UtilityClass;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@UtilityClass
public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        try {
            // 1. Load the properties file (where the Config Tab saves data)
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("src/main/resources/application.properties");
            props.load(in);
            in.close();

            // 2. Get the values (Updated by your Config Tab)
            String url = props.getProperty("db.url");       // e.g. jdbc:mysql://192.168.1.5:3306/...
            String user = props.getProperty("db.username");
            String pass = props.getProperty("db.password");

            // 3. Connect
            return DriverManager.getConnection(url, user, pass);

        } catch (IOException e) {
            // Fallback if file is missing
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/barangay_db", "root", "");
        }
    }
}