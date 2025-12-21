package com.mybank;

import com.mybank.model.Account;
import com.mybank.model.Bank;
import com.mybank.model.Currency;
import com.mybank.service.Database;

import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static List<Currency> currencies;
    private static List<Bank> banks;
    private static List<Account> accounts;
    private static Account account;
    private static boolean isAdmin = false;

    public static void main(String[] args) throws Exception {
        Database.init();
        currencies = Currency.getCurrencies();
        banks = Bank.getBanks();
        accounts = new ArrayList<>();
        for (Bank bank : banks) {
            accounts.addAll(Account.getAccounts(bank));
        }

        while (true) {
            try {
                if (isAdmin) {
                    adminMenu();
                } else if (account == null) {
                    entryMenu();
                } else {
                    userMenu();
                }
            } catch (InputMismatchException ignored) {
                System.out.println("無效的輸入。");
                pause();
            } catch (NoSuchElementException ignored) {
            }
        }
    }

    private static void pause() {
        System.out.print("按 Enter 鍵繼續...");
        scanner.nextLine();
        scanner.nextLine();
    }

    private static void resetCursor() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void userMenu() {
        while (account != null) {
            resetCursor();
            System.out.println("1. 轉帳");
            System.out.println("2. 換匯");
            System.out.println("3. 登出\n");
            System.out.print("選擇動作: ");

            final int action = scanner.nextInt();
            if (action < 1 || action > 3) {
                System.out.println("無效的選擇。");
                pause();
                continue;
            }

            switch (action) {
                case 1: {
                    for (final Bank bank : banks) {
                        System.out.printf("(%s) %s\n", bank.id, bank.name);
                    }
                    System.out.print("請選擇銀行: ");
                    final String bankId = scanner.next();
                    if (banks.stream().noneMatch(b -> bankId.equals(b.id))) {
                        System.out.println("無效的選擇。");
                        pause();
                        continue;
                    }

                    System.out.print("請輸入轉帳對象帳戶: ");
                    final String targetId = scanner.next();
                    final Account target = accounts.stream()
                            .filter(a -> targetId.equals(a.id))
                            .findFirst()
                            .orElse(null);
                    if (target == null) {
                        System.out.println("輸入的帳戶不存在。");
                        pause();
                        continue;
                    }

                    System.out.print("請輸入轉帳金額: ");
                    final int amount = scanner.nextInt();
                    if (amount < 1) {
                        System.out.println("無效的金額。");
                        pause();
                        continue;
                    }

                    final double transferFeeRate = account.bank.id.equals(target.bank.id) ? 0 : account.bank.getTransferFeeRate();
                    final int transferFee = (int) Math.ceil(amount * transferFeeRate);
                    if (amount + transferFee > account.getBalance()) {
                        System.out.println("你的餘額不足。");
                        pause();
                        continue;
                    }

                    final boolean transaction = account.transfer(target, amount);
                    if (!transaction) {
                        System.out.println("轉帳失敗。");
                        pause();
                        continue;
                    }

                    System.out.println("轉帳成功。");
                    pause();
                    break;
                }
                case 2: {
                    for (int i = 0; i < currencies.size(); i++) {
                        final Currency currency = currencies.get(i);
                        System.out.printf("%d. %s (%f)\n", i + 1, currency.name, currency.exchangeRate);
                    }
                    System.out.print("請選擇貨幣: ");
                    final int index = scanner.nextInt();
                    if (index < 1 || index > currencies.size()) {
                        System.out.println("無效的選擇。");
                        pause();
                        continue;
                    }

                    System.out.print("請輸入欲換取金額: ");
                    final int amount = scanner.nextInt();
                    if (amount < 1) {
                        System.out.println("無效的金額。");
                        pause();
                        continue;
                    }

                    final int exchangeFee = (int) Math.ceil(amount * account.bank.getExchangeFeeRate());
                    if (amount + exchangeFee > account.getBalance()) {
                        System.out.println("你的餘額不足。");
                        pause();
                        continue;
                    }

                    final Currency currency = currencies.get(index - 1);
                    final boolean transaction = account.exchange(currency, amount);
                    if (!transaction) {
                        System.out.println("換匯失敗。");
                        pause();
                        continue;
                    }

                    System.out.println("換匯成功。");
                    pause();
                    break;
                }
                case 3:
                    account = null;
                    break;
            }
        }
    }

    private static void adminMenu() {
        while (isAdmin) {
            resetCursor();
            System.out.println("1. 創建銀行");
            System.out.println("2. 設定換匯費率");
            System.out.println("3. 設定轉帳費率");
            System.out.println("4. 設定帳戶金額");
            System.out.println("5. 登出\n");
            System.out.print("選擇動作: ");

            final int action = scanner.nextInt();
            if (action < 1 || action > 5) {
                System.out.println("無效的選擇。");
                pause();
                continue;
            }

            switch (action) {
                case 1: {
                    System.out.print("請註冊銀行ID: ");
                    final String bankID = scanner.next();
                    if (banks.stream().anyMatch(bank -> bankID.equals(bank.id))) {
                        System.out.println("此ID已有銀行註冊。");
                        pause();
                        continue;
                    }

                    System.out.print("請註冊銀行名稱: ");
                    final String bankName = scanner.next();
                    if (banks.stream().anyMatch(bank -> bankName.equals(bank.name))) {
                        System.out.println("此名稱已有銀行註冊。");
                        pause();
                        continue;
                    }

                    System.out.print("請輸入轉帳費率: ");
                    final float transferFee = scanner.nextFloat();
                    if (transferFee < 0) {
                        System.out.println("無效的轉帳費率。");
                        pause();
                        continue;
                    }

                    System.out.print("請輸入換匯費率: ");
                    final float exchangeFee = scanner.nextFloat();
                    if (exchangeFee < 0) {
                        System.out.println("無效的換匯費率。");
                        pause();
                        continue;
                    }

                    final Bank newBank = Bank.createBank(bankName, bankID, transferFee, exchangeFee);
                    if (newBank == null) {
                        System.out.println("創建失敗。");
                        pause();
                        continue;
                    }
                    banks.add(newBank);
                    pause();
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
                        pause();
                        continue;
                    }

                    System.out.println("請輸入換匯費率: ");
                    final float exchangeFeeRate = scanner.nextFloat();
                    if (exchangeFeeRate < 0) {
                        System.out.println("無效的費率。");
                        pause();
                        continue;
                    }

                    selectedBank.setExchangeFeeRate(exchangeFeeRate);
                    System.out.println("設定完成。");
                    pause();
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
                        pause();
                        continue;
                    }

                    System.out.println("請輸入轉帳費率: ");
                    final float transferFeeRate = scanner.nextFloat();
                    if (transferFeeRate < 0) {
                        System.out.println("無效的費率。");
                        pause();
                        continue;
                    }

                    selectedBank.setTransferFeeRate(transferFeeRate);
                    System.out.println("設定完成。");
                    pause();
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
                        pause();
                        continue;
                    }

                    final List<Account> accList = accounts.stream()
                            .filter(a -> bankId.equals(a.bank.id))
                            .toList();
                    if (accList.isEmpty()) {
                        System.out.println("這個銀行下沒有用戶。");
                        pause();
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
                        pause();
                        continue;
                    }

                    System.out.print("請輸入金額: ");
                    final int balance = scanner.nextInt();
                    if (balance < 0) {
                        System.out.println("無效的金額。");
                        pause();
                        continue;
                    }

                    final Account acc = accList.get(index - 1);
                    final boolean result = acc.setBalance(balance);
                    if (!result) {
                        System.out.println("帳戶金額設定失敗。");
                        pause();
                        continue;
                    }

                    System.out.println("帳戶金額設定成功。");
                    pause();
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
            resetCursor();
            System.out.println("1. 開戶");
            System.out.println("2. 登入");
            System.out.println("3. 管理員模式\n");
            System.out.print("選擇動作: ");

            final int action = scanner.nextInt();
            if (action <= 0 || action > 3) {
                System.out.println("無效的選擇。");
                pause();
                continue;
            }

            if (action == 1) {
                for (final Bank bank : banks) {
                    System.out.printf("(%s) %s\n", bank.id, bank.name);
                }
                System.out.print("\n選擇銀行: ");
                final String bankId = scanner.next();
                final Bank selectedBank = banks.stream()
                        .filter(b -> bankId.equals(b.id))
                        .findFirst()
                        .orElse(null);
                if (selectedBank == null) {
                    System.out.println("無效的選擇。");
                    pause();
                    continue;
                }

                System.out.print("輸入身分證字號: ");
                final String userId = scanner.next();
                if (accounts.stream().anyMatch(acc -> bankId.equals(acc.bank.id) && userId.equals(acc.userId))) {
                    System.out.println("你在該銀行已經擁有戶頭。");
                    pause();
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
                    pause();
                    continue;
                }

                final Account acc = Account.createAccount(userId, name, pwd, selectedBank);
                if (acc == null) {
                    System.out.println("開戶失敗。");
                    pause();
                    continue;
                }

                accounts.add(acc);
                account = acc;
                System.out.println("成功開戶。");
                pause();
            } else if (action == 2) {
                for (final Bank bank : banks) {
                    System.out.printf("(%s) %s\n", bank.id, bank.name);
                }
                System.out.print("\n選擇銀行: ");
                final String bankId = scanner.next();
                if (banks.stream().noneMatch(bank -> bankId.equals(bank.id))) {
                    System.out.println("無效的選擇。");
                    pause();
                    continue;
                }

                System.out.print("輸入身分證字號: ");
                final String id = scanner.next();
                System.out.print("輸入密碼: ");
                final String pwd = scanner.next();
                final Account acc = accounts.stream()
                        .filter(a -> bankId.equals(a.bank.id) && id.equals(a.userId))
                        .findFirst()
                        .orElse(null);
                if (acc == null) {
                    System.out.println("帳號不存在。");
                    pause();
                    continue;
                }

                final boolean loggedIn = Account.verifyPassword(pwd, acc.hashedPwd);
                if (!loggedIn) {
                    System.out.println("登入失敗。");
                    pause();
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
