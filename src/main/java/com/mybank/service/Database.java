package com.mybank.service;

import java.sql.*;

public class Database {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:data.db");
    }

    public static void init() {
        try {
            final Connection conn = getConnection();
            final Statement stmt = conn.createStatement();
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS banks (
                        id TEXT UNIQUE NOT NULL,
                        name TEXT UNIQUE NOT NULL,
                        transferFee REAL NOT NULL,
                        exchangeFee REAL NOT NULL
                    );
                    """);
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        id TEXT PRIMARY KEY,
                        userId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        hashedPwd TEXT NOT NULL,
                        balance INTEGER DEFAULT 0,
                        bankId TEXT NOT NULL,
                    
                        FOREIGN KEY (bankId) REFERENCES banks(id)
                    );
                    """);
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
}
