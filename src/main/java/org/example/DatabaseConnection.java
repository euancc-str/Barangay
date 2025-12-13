package org.example;

import lombok.experimental.UtilityClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@UtilityClass
public class DatabaseConnection {
    public static Connection getConnection() {
        Connection connection;
        try {
            String url = System.getProperty("db.url");
            String user = System.getProperty("db.username");
            String pass = System.getProperty("db.password");

            connection = DriverManager.getConnection(url, user, pass);
        }  catch ( SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Something went wrong while connecting to the database");
        }
        return connection;
    }
}