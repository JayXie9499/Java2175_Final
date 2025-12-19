package com.mybank.model;

import com.mybank.service.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Bank {
    public final String name;
    public final String id;
    private double transferFeeRate;
    private double exchangeFeeRate;

    public static List<Bank> getBanks() throws SQLException {
        final List<Bank> banks = new ArrayList<>();
        final Connection conn = Database.getConnection();
        final Statement stmt = conn.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT * FROM banks;");
        while (rs.next()) {
            final String name = rs.getString("name");
            final String id = rs.getString("id");
            final double transferFeeRate = rs.getDouble("transferFeeRate");
            final double exchangeFeeRate = rs.getDouble("exchangeFeeRate");
            final Bank bank = new Bank(name, id, transferFeeRate, exchangeFeeRate);
            banks.add(bank);
        }
        rs.close();
        stmt.close();
        conn.close();
        return banks;
    }

    public static Bank createBank(String name, String id, double transferFeeRate, double exchangeFeeRate) {
        try {
            final Connection conn = Database.getConnection();
            final PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO banks (name, id, transferFeeRate, exchangeFeeRate)
                    VALUES (?, ?, ?, ?);
                    """);
            stmt.setString(1, name);
            stmt.setString(2, id);
            stmt.setDouble(3, transferFeeRate);
            stmt.setDouble(4, exchangeFeeRate);
            final int rowsAffected = stmt.executeUpdate();
            stmt.close();
            conn.close();
            if (rowsAffected > 0) {
                return new Bank(name, id, transferFeeRate, exchangeFeeRate);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

    }

    public Bank(String name, String id, double transferFeeRate, double exchangeFeeRate) {
        this.name = name;
        this.id = id;
        this.transferFeeRate = transferFeeRate;
        this.exchangeFeeRate = exchangeFeeRate;
    }

    public boolean setTransferFeeRate(double newFee) {
        try {
            final Connection conn = Database.getConnection();
            final PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE banks
                    SET transferFeeRate = ?
                    WHERE id = ?;
                    """);
            stmt.setDouble(1, newFee);
            stmt.setString(2, id);
            stmt.executeUpdate();
            this.transferFeeRate = newFee;
            stmt.close();
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public boolean setExchangeFeeRate(double newFee) {
        try {
            final Connection conn = Database.getConnection();
            final PreparedStatement stmt = conn.prepareStatement("""
                    UPDATE banks
                    SET exchangeFeeRate = ?
                    WHERE id = ?;
                    """);
            stmt.setDouble(1, newFee);
            stmt.setString(2, id);
            stmt.executeUpdate();
            this.exchangeFeeRate = newFee;
            stmt.close();
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public double getTransferFeeRate() {
        return transferFeeRate;
    }

    public double getExchangeFeeRate() {
        return exchangeFeeRate;
    }
}
