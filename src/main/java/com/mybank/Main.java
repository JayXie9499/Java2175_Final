package com.mybank;

import com.mybank.model.Account;
import com.mybank.model.Bank;
import com.mybank.service.Database;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static List<Bank> banks;
    private static List<Account> accounts;
    private static Account account;
    private static boolean is_admin = false;

    public static void main(String[] args) throws Exception {
        Database.init();
        banks = Bank.getBanks();
        accounts = Account.getAccounts();

        while (true) {
            if (is_admin) {
                while(is_admin){
                    System.out.println("1.創建銀行");
                    System.out.println("2.設定換匯費率");
                    System.out.println("3.設定轉帳費率");
                    System.out.println("4.設定帳戶金額");
                    System.out.println("5.登出\n");
                    System.out.print("請輸入操作模式: ");
                    final int active = scanner.nextInt();
                    if(active<1 || active > 5){
                        System.out.println("無效的選擇");
                        continue;
                    }
                    switch (active){
                        case 1:
                            System.out.print("請註冊銀行ID: ");
                            final String bankID = scanner.next();
                            if(banks.stream().anyMatch(bank -> bankID.equals(bank.id))){
                                System.out.println("此ID已有銀行註冊");
                                continue;
                            }
                            System.out.print("請註冊銀行名稱: ");
                            final String bankName = scanner.next();
                            if(banks.stream().anyMatch(bank -> bankName.equals(bank.name))){
                                System.out.println("此名稱已有銀行註冊");
                                continue;
                            }
                            System.out.print("請輸入轉帳費率: ");
                            final float transferFee = scanner.nextFloat();
                            System.out.print("請輸入換匯費率: ");
                            final float exchangeFee = scanner.nextFloat();
                            final Bank newbank = Bank.createBank(bankName,bankID,transferFee,exchangeFee);
                            if(newbank == null){
                                System.out.println("創建失敗");
                                continue;
                            }
                            banks.add(newbank);
                            break;
                        case 2:
                            for (final Bank bank : banks) {
                                System.out.printf("(%s) %s\n", bank.id, bank.name);
                            }
                            System.out.print("請選擇銀行: ");
                            final String bankId = scanner.next();
                            if (banks.stream().noneMatch(bank -> bankId.equals(bank.id))) {
                                System.out.println("無效的選擇。");
                                continue;
                            }
                            break;
                        case 3:
                            break;
                        case 4:
                            break;
                        case 5:
                            is_admin = false;
                            break;
                    }
                }
            } else if (account == null) {
                entryMenu();
            }
        }
    }

    private static void entryMenu() {
        while (true) {
            System.out.println("1. 開戶");
            System.out.println("2. 登入");
            System.out.println("3. 管理員模式\n");
            System.out.print("選擇動作: ");

            final int action = scanner.nextInt();
            if (action <= 0 || action > 3) {
                System.out.println("無效的選擇。");
                continue;
            }

            if (action == 1) {
                for (final Bank bank : banks) {
                    System.out.printf("(%s) %s\n", bank.id, bank.name);
                }
                System.out.print("\n選擇銀行: ");
                final String bankId = scanner.next();
                if (banks.stream().noneMatch(bank -> bankId.equals(bank.id))) {
                    System.out.println("無效的選擇。");
                    continue;
                }

                System.out.print("輸入身分證字號: ");
                final String id = scanner.next();
                if (accounts.stream().anyMatch(acc -> bankId.equals(acc.bankId) && id.equals(acc.id))) {
                    System.out.println("你在該銀行已經擁有戶頭。");
                    continue;
                }

                System.out.print("輸入姓名: ");
                final String name = scanner.next();
                System.out.print("輸入密碼: ");
                final String pwd = scanner.next();
                System.out.print("確認密碼: ");
                final String pwdConfirm = scanner.next();
                if (!Objects.equals(pwd, pwdConfirm)) {
                    System.out.println("輸入密碼不相同。");
                    continue;
                }

                final Account acc = Account.createAccount(name, id, pwd, bankId);
                if (acc == null) {
                    System.out.println("開戶失敗。");
                    continue;
                }

                accounts.add(acc);
                account = acc;
            } else if (action == 2) {
                for (final Bank bank : banks) {
                    System.out.printf("(%s) %s\n", bank.id, bank.name);
                }
                System.out.print("\n選擇銀行: ");
                final String bankId = scanner.next();
                if (banks.stream().noneMatch(bank -> bankId.equals(bank.id))) {
                    System.out.println("無效的選擇。");
                    continue;
                }

                System.out.print("輸入身分證字號: ");
                final String id = scanner.next();
                System.out.print("輸入密碼: ");
                final String pwd = scanner.next();
                final Optional<Account> existingAcc = accounts.stream().filter(acc -> bankId.equals(acc.bankId) && id.equals(acc.id)).findFirst();
                if (existingAcc.isEmpty()) {
                    System.out.println("帳號不存在。");
                    continue;
                }

                final Account acc = existingAcc.get();
                final boolean loggedIn = Account.verifyPassword(pwd, acc.hashedPwd);
                if (!loggedIn) {
                    System.out.println("登入失敗。");
                    continue;
                }

                account = acc;
            } else {
                is_admin = true;
            }
            break;
        }
    }
}
