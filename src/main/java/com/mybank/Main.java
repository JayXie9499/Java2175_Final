package com.mybank;

import com.mybank.model.Account;
import com.mybank.model.Bank;
import com.mybank.service.Database;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static List<Bank> banks;
    private static List<Account> accounts;
    private static Account account;
    private static boolean isAdmin = false;

    public static void main(String[] args) throws Exception {
        Database.init();
        banks = Bank.getBanks();
        accounts = Account.getAccounts();

        while (true) {
            if (isAdmin) {
                adminMenu();
            } else if (account == null) {
                entryMenu();
            }
        }
    }

    private static void adminMenu() {
        while (isAdmin) {
            System.out.println("1. 創建銀行");
            System.out.println("2. 設定換匯費率");
            System.out.println("3. 設定轉帳費率");
            System.out.println("4. 設定帳戶金額");
            System.out.println("5. 登出\n");
            System.out.print("選擇動作: ");

            final int action = scanner.nextInt();
            if (action < 1 || action > 5) {
                System.out.println("無效的選擇");
                continue;
            }

            switch (action) {
                case 1: {
                    System.out.print("請註冊銀行ID: ");
                    final String bankID = scanner.next();
                    if (banks.stream().anyMatch(bank -> bankID.equals(bank.id))) {
                        System.out.println("此ID已有銀行註冊");
                        continue;
                    }

                    System.out.print("請註冊銀行名稱: ");
                    final String bankName = scanner.next();
                    if (banks.stream().anyMatch(bank -> bankName.equals(bank.name))) {
                        System.out.println("此名稱已有銀行註冊");
                        continue;
                    }

                    System.out.print("請輸入轉帳費率: ");
                    final float transferFee = scanner.nextFloat();
                    if (transferFee < 0) {
                        System.out.println("無效的轉帳費率。");
                        continue;
                    }

                    System.out.print("請輸入換匯費率: ");
                    final float exchangeFee = scanner.nextFloat();
                    if (exchangeFee < 0) {
                        System.out.println("無效的換匯費率。");
                        continue;
                    }

                    final Bank newBank = Bank.createBank(bankName, bankID, transferFee, exchangeFee);
                    if (newBank == null) {
                        System.out.println("創建失敗");
                        continue;
                    }
                    banks.add(newBank);
                    break;
                }
                case 2: {
                    for (final Bank bank : banks) {
                        System.out.printf("(%s) %s\n", bank.id, bank.name);
                    }
                    System.out.print("請選擇銀行: ");
                    final String bankId = scanner.next();
                    final Bank selectedBank = banks.stream()
                            .filter(b -> bankId.equals(b.id))
                            .findFirst()
                            .orElse(null);
                    if (selectedBank == null) {
                        System.out.println("無效的選擇。");
                        continue;
                    }

                    System.out.println("請輸入換匯費率: ");
                    final float exchangeFee = scanner.nextFloat();
                    if (exchangeFee < 0) {
                        System.out.println("無效的費率。");
                        continue;
                    }

                    selectedBank.setExchangeFee(exchangeFee);
                    System.out.println("設定完成");
                    break;
                }
                case 3: {
                    for (final Bank bank : banks) {
                        System.out.printf("(%s) %s\n", bank.id, bank.name);
                    }
                    System.out.print("請選擇銀行: ");
                    final String bankId = scanner.next();
                    final Bank selectedBank = banks.stream()
                            .filter(b -> bankId.equals(b.id))
                            .findFirst()
                            .orElse(null);
                    if (selectedBank == null) {
                        System.out.println("無效的選擇。");
                        continue;
                    }

                    System.out.println("請輸入換匯費率: ");
                    final float transferFee = scanner.nextFloat();
                    if (transferFee < 0) {
                        System.out.println("無效的費率。");
                        continue;
                    }

                    selectedBank.setTransferFee(transferFee);
                    System.out.println("設定完成");
                    break;
                }
                case 4: {
                    for (final Bank bank : banks) {
                        System.out.printf("(%s) %s\n", bank.id, bank.name);
                    }
                    System.out.print("請選擇銀行: ");
                    final String bankId = scanner.next();
                    if (banks.stream().noneMatch(bank -> bankId.equals(bank.id))) {
                        System.out.println("無效的選擇。");
                        continue;
                    }

                    final List<Account> accList = accounts.stream()
                            .filter(a -> bankId.equals(a.bankId))
                            .toList();
                    if (accList.isEmpty()) {
                        System.out.println("這個銀行下沒有用戶。");
                        continue;
                    }

                    for (int i = 0; i < accList.size(); i++) {
                        final Account acc = accList.get(i);
                        System.out.printf("%d. %s - %s\n", i + 1, acc.id, acc.name);
                    }
                    System.out.print("請選擇帳號: ");
                    final int index = scanner.nextInt();
                    if (index < 1 || index > accList.size()) {
                        System.out.println("無效的選擇。");
                        continue;
                    }

                    System.out.print("請輸入金額: ");
                    final int balance = scanner.nextInt();
                    if (balance < 0) {
                        System.out.println("無效的金額。");
                        continue;
                    }

                    final Account acc = accList.get(index - 1);
                    final boolean result = acc.setBalance(balance);
                    if (!result) {
                        System.out.println("帳戶金額設定失敗。");
                        continue;
                    }

                    System.out.println("帳戶金額設定成功。");
                    break;
                }
                case 5:
                    isAdmin = false;
                    break;
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
                final Account acc = accounts.stream()
                        .filter(a -> bankId.equals(a.bankId) && id.equals(a.id))
                        .findFirst()
                        .orElse(null);
                if (acc == null) {
                    System.out.println("帳號不存在。");
                    continue;
                }

                final boolean loggedIn = Account.verifyPassword(pwd, acc.hashedPwd);
                if (!loggedIn) {
                    System.out.println("登入失敗。");
                    continue;
                }

                account = acc;
            } else {
                isAdmin = true;
            }
            break;
        }
    }
}
