package com.mybank.model;

import com.mybank.service.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Bank {
    public final String name;
    public final String id;
    private double transferFee;
    private double exchangeFee;

    public static List<Bank> getBanks() throws SQLException {
        final List<Bank> banks = new ArrayList<>();
        final Connection conn = Database.getConnection();
        final Statement stmt = conn.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT * FROM banks;");
        while (rs.next()) {
            final String name = rs.getString("name");
            final String id = rs.getString("id");
            final double transferFee = rs.getDouble("transferFee");
            final double exchangeFee = rs.getDouble("exchangeFee");
            final Bank bank = new Bank(name, id, transferFee, exchangeFee);
            banks.add(bank);
        }
        rs.close();
        stmt.close();
        conn.close();
        return banks;
    }

    public Bank(String name, String id, double transferFee, double exchangeFee) {
        this.name = name;
        this.id = id;
        this.transferFee = transferFee;
        this.exchangeFee = exchangeFee;
    }

    public static Bank createBank(String name, String id, double transferFee, double exchangeFee) {
        try {
            final Connection conn = Database.getConnection();
            final Statement stmt = conn.createStatement();
            stmt.executeUpdate(String.format("""
                    INSERT INTO banks (name, id, transferFee, exchangeFee)
                    VALUES ('%s', '%s', %f, %f)
                    """, name, id, transferFee, exchangeFee));
            stmt.close();
            conn.close();

            return new Bank(name,id,transferFee,exchangeFee);
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            return null;
        }
    }
    public void setTransferFee(double newFee) {
        this.transferFee = newFee;
    }

    public void setExchangeFee(double newFee) {
        this.exchangeFee = newFee;
    }

    public double getTransferFee() {
        return transferFee;
    }

    public double getExchangeFee() {
        return exchangeFee;
    }
}
