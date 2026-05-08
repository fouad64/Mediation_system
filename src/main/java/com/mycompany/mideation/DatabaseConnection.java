package com.mycompany.mideation;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    private static Connection connection;

    public static Connection get() {
        if (connection != null) return connection;

        try {
            String url  = ConfigLoader.get("db.url");
            String user = ConfigLoader.get("db.username");
            String pass = ConfigLoader.get("db.password");

            connection = DriverManager.getConnection(url, user, pass);
            System.out.println("Database connected.");
            return connection;

        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
