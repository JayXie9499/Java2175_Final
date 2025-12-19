package com.mybank.model;

import com.mybank.service.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Currency {
    public final String name;
    public final double exchangeRate;

    public static List<Currency> getCurrencies() throws SQLException {
        final List<Currency> currencies = new ArrayList<>();
        final Connection conn = Database.getConnection();
        final Statement stmt = conn.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT * FROM banks;");
        while (rs.next()) {
            final String name = rs.getString("name");
            final double exchangeRate = rs.getDouble("exchangeRate");
            final Currency currency = new Currency(name, exchangeRate);
            currencies.add(currency);
        }
        rs.close();
        stmt.close();
        conn.close();
        return currencies;
    }

    private Currency(String name, double exchangeRate) {
        this.name = name;
        this.exchangeRate = exchangeRate;
    }
}
