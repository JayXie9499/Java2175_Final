package com.mybank.service;

import com.mybank.model.SqlCommand;

import java.sql.*;

public class Database {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:data.db");
    }

    public static boolean executeMultiple(SqlCommand... commands) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            for (SqlCommand cmd : commands) {
                stmt = conn.prepareStatement(cmd.stmt);
                for (int i = 0; i < cmd.values.length; i++) {
                    final Object val = cmd.values[i];
                    if (val instanceof String) {
                        stmt.setString(i + 1, (String) val);
                    } else if (val instanceof Integer) {
                        stmt.setInt(i + 1, (int) val);
                    } else if (val instanceof Double) {
                        stmt.setDouble(i + 1, (double) val);
                    }
                }

                stmt.executeUpdate();
                stmt.close();
            }
            conn.commit();
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
            return false;
        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public static void init() {
        try {
            final Connection conn = getConnection();
            final Statement stmt = conn.createStatement();
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS banks (
                        id TEXT PRIMARY KEY,
                        name TEXT UNIQUE NOT NULL,
                        transferFeeRate REAL NOT NULL,
                        exchangeFeeRate REAL NOT NULL
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
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS currencies (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        exchangeRate REAL NOT NULL
                    )
                    """);
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS wallets (
                        id INTEGER PRIMARY KEY,
                        accountId TEXT NOT NULL,
                        currencyId INTEGER NOT NULL,
                        balance REAL DEFAULT 0,
                    
                        FOREIGN KEY (accountId) REFERENCES accounts(id),
                        FOREIGN KEY (currencyId) REFERENCES currencies(id),
                        UNIQUE(accountId, currencyId)
                    )
                    """);
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }
}
