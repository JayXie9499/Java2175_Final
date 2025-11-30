package com.mybank.model;

import com.mybank.service.Database;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Account {
    public final String name;
    public final String id;
    public final String bankId;
    public final String hashedPwd;
    private int balance;

    public static List<Account> getAccounts() throws SQLException {
        final List<Account> accounts = new ArrayList<>();
        final Connection conn = Database.getConnection();
        final Statement stmt = conn.createStatement();
        final ResultSet rs = stmt.executeQuery("SELECT * FROM accounts;");
        while (rs.next()) {
            final String name = rs.getString("name");
            final String id = rs.getString("id");
            final String bankId = rs.getString("bankId");
            final String hashedPwd = rs.getString("hashedPwd");
            final int balance = rs.getInt("balance");
            final Account acc = new Account(name, id, bankId, hashedPwd);
            acc.setBalance(balance);
            accounts.add(acc);
        }
        rs.close();
        stmt.close();
        conn.close();
        return accounts;
    }

    public static Account createAccount(String name, String id, String pwd, String bankId) {
        try {
            final Connection conn = Database.getConnection();
            final Statement stmt = conn.createStatement();
            final String hashedPwd = hashPassword(pwd);
            stmt.executeUpdate(String.format("""
                    INSERT INTO accounts (name, id, bankId, hashedPwd)
                    VALUES ('%s', '%s', '%s', '%s');
                    """, name, id, bankId, hashedPwd));
            stmt.close();
            conn.close();

            return new Account(name, id, bankId, hashedPwd);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    private static String hashPassword(String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final SecureRandom random = new SecureRandom();
        final byte[] salt = new byte[16];
        random.nextBytes(salt);

        final KeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, 65536, 256);
        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final byte[] hash = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String pwd, String hashedPwd) {
        try {
            final String[] parts = hashedPwd.split(":");
            final byte[] salt = Base64.getDecoder().decode(parts[0]);
            final byte[] hash = Base64.getDecoder().decode(parts[1]);
            final PBEKeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, 65536, 256);
            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            final byte[] computedHash = factory.generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(hash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }

    private Account(String name, String id, String bankId, String hashedPwd) {
        this.name = name;
        this.id = id;
        this.bankId = bankId;
        this.hashedPwd = hashedPwd;
    }

    public boolean setBalance(int newBalance) {
        try {
            final Connection conn = Database.getConnection();
            final Statement stmt = conn.createStatement();
            stmt.executeUpdate(String.format("""
                    UPDATE accounts
                    SET balance = %d
                    WHERE id = %s, bankId = %s;
                    """, newBalance, id, bankId));
            this.balance = newBalance;
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace(System.err);
            return false;
        }
    }

    public int getBalance() {
        return balance;
    }

    public boolean withdraw(int amount) {
        if (balance < amount) {
            return false;
        }

        return setBalance(balance - amount);
    }
}
