package com.mybank.model;

public class Account {
    public final String name;
    public final String id;
    public final String bankId;
    public final String hashedPwd;
    private int balance;

    private Account(String name, String id, String bankId, String hashedPwd) {
        this.name = name;
        this.id = id;
        this.bankId = bankId;
        this.hashedPwd = hashedPwd;
    }

    public int getBalance(){
        return balance;
    }

    public void withdraw(int m){
        if(balance<m){
            System.out.println("Invalid input");
        }
        else{
            balance-=m;
        }
    }
}
