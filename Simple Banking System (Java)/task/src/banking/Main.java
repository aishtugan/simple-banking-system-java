
package banking;

import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws SQLException {

        Scanner sc = new Scanner(System.in);

        BankService bankService = new BankService();

        BankingSystem defaultBS = new BankingSystem("empty", "");

        String command1 = "1. Create an account";
        String command2 = "2. Log into account";
        String command0 = "0. Exit";

        Account currentAccount = null;

        boolean continueLoop = true;

        String dbFileName = null;
        if (args.length > 0) {
            for (int i = 0; i < args.length - 1; i++) {
                if ("-fileName".equals(args[i])) {
                    dbFileName = args[i + 1];
                    break;
                }
            }
        }

        Connection con = DatabaseManager.getConnection(dbFileName);

        bankService.createAccountTable(con);

        while(continueLoop){

            System.out.println(command1);
            System.out.println(command2);
            System.out.println(command0);

            int choice = sc.nextInt();
            System.out.println();

            switch (choice){
                case 1:
                    switch (command1){
                        case "1. Create an account":

                            Account account = new Account(null, null, 0);
                            account.generateCardNumber(defaultBS, bankService.bankBIN, bankService.cardNumberOfDigits);
                            account.generatePinCode(bankService.pinNumberOfDigits);
                            System.out.println("Your card has been created");
                            System.out.println("Your card number:");
                            System.out.println(account.cardNumber);
                            System.out.println("Your card PIN:");
                            System.out.println(account.pin);
                            System.out.println();
                            bankService.accounts.add(account);
                            bankService.saveAccountToDB(con, account);
                            break;

                        case "1. Balance":
                            System.out.println("Balance: " + currentAccount.balance + "\n");
                            break;
                    }

                    break;

                case 2:
                    switch (command2){
                        case "2. Log into account":

                            System.out.println("Enter your card number:");
                            String inputCardNumber = sc.next();
                            Account foundAccount = bankService.findAccount(con, inputCardNumber);
                            System.out.println("Enter your PIN:");
                            String inputPIN = sc.next();
                            System.out.println();
                            if (foundAccount == null){
                                System.out.println("Wrong card number or PIN!\n");
                                break;
                            }
                            if (!bankService.pinIsCorrect(foundAccount, inputPIN)) {
                                System.out.println("Wrong card number or PIN!\n");
                                break;
                            }

                            currentAccount = foundAccount;
                            command1 = "1. Balance";
                            command2 = "2. Log out";

                            System.out.println("You have successfully logged in!\n");
                            break;

                        case "2. Log out":

                            currentAccount = null;
                            command1 = "1. Create an account";
                            command2 = "2. Log into account";
                            System.out.println("You have successfully logged out!\n");
                            break;
                    }
                    break;

                case 0:
                    continueLoop = false;
                    System.out.println("Bye!");
                    break;

                default:
                    System.out.println("Wrong choice!");
            }

        }

        DatabaseManager.closeConnection(con);
    }
}

class DatabaseManager {
    private static final String dbPath = "Simple Banking System (Java)/task/db/simplebanksystem.s3db";
    private static final String dbUrl = "jdbc:sqlite:";

    public static Connection getConnection(String dbFileName) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl + (dbFileName == null ? dbPath : dbFileName));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void createTable(Connection conn, String tableName, Map columns) {

        String sqlCommand = "CREATE TABLE IF NOT EXISTS " + tableName + " (";
        for (Object key : columns.keySet()) {
            sqlCommand += "     " + key + " " + columns.get(key) + ",";
        }
        sqlCommand = sqlCommand.substring(0, sqlCommand.length() - 1).concat(");");

        try {
            Statement stmt = conn.createStatement();
            stmt.execute(sqlCommand);
            closeStatement(stmt);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void insertRow(Connection conn, String table, List<String> columns, List<Object> values) throws SQLException {
        if (columns.size() != values.size()) {
            throw new IllegalArgumentException("Columns and values must have the same size");
        }

        String columnPart = String.join(", ", columns);
        String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
        String sqlString = "INSERT INTO " + table + " (" + columnPart + ") VALUES (" + placeholders + ")";

        try (PreparedStatement stmt = conn.prepareStatement(sqlString)) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        }
    }

    public static List<Account> findAccountsByParameters(Connection conn, String table, Map parameters) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        List<Object>  values = new ArrayList<>();
        String sql = "SELECT * FROM " + table + " WHERE ";
        int i = 1;
        for (Object key : parameters.keySet()) {
            sql += key + " = " + parameters.get(key);
            i++;
            if (i < parameters.size()) {
                sql.concat(" AND ");
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Account account = new Account(rs.getString("number"), rs.getString("pin"), rs.getInt("balance"));
                accounts.add(account);
            }
        }

        return accounts;
    }

    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class BankingSystem {
    String name;
    String cardCode;

    BankingSystem(String name, String cardCode) {
        this.name = name;
        this.cardCode = cardCode;
    }
}

class Account {

    String cardNumber;
    String pin;
    Integer balance;

    Account(String cardNumber, String pin, int balance) {
        this.cardNumber = cardNumber;
        this.pin = pin;
        this.balance = balance;
    }

    void deposit(Integer amount) {
        balance += amount;
    }

    void withdraw(Integer amount) {
        balance -= amount;
    }

    void generateCardNumber(BankingSystem bankingSystem, String bankBIN, int lengthOfNumber) {

        if (null != this.cardNumber) {
            return;
        }
        Random rand = new Random();
        StringBuilder sb =  new StringBuilder();

        sb.append(bankingSystem.cardCode).append(bankBIN);

        for (int i = 0; i < lengthOfNumber - bankingSystem.cardCode.length() - bankBIN.length() - 1; i++) {
            int digit = rand.nextInt(10);
            sb.append(digit);
        }

//       int checksum = rand.nextInt(10); //for the first step
        int checksum = getCardControlSumByLuhn(sb.toString());
        this.cardNumber = sb.append(checksum).toString();
    }

    void generatePinCode(int lengthOfPin) {
        if (null != this.pin) {
            return;
        }
        Random rand = new Random();
        StringBuilder sb =  new StringBuilder();

        for (int i = 0; i < lengthOfPin; i++) {
            int digit = rand.nextInt(10);
            sb.append(digit);
        }
        this.pin = sb.toString();
    }

    static int getCardControlSumByLuhn(String cardCutNumber) {
        String[] digitsArray = cardCutNumber.split("");
        int[] interimArray = new int[digitsArray.length];

        //multiply odd digits by 2
        for (int i = 0; i < digitsArray.length; i++) {
            if(i % 2 == 0){
                interimArray[i] = 2 * Integer.parseInt(digitsArray[i]);
            } else{
                interimArray[i] = Integer.parseInt(digitsArray[i]);
            }
        }

        int sumOfDigits = 0;
        //subtract 9 to numbers over 9
        for (int i = 0; i < interimArray.length; i++) {
            if(interimArray[i] > 9) interimArray[i] = interimArray[i] - 9;
            sumOfDigits += interimArray[i];
        }

        return sumOfDigits % 10 == 0 ? 0 : 10 - sumOfDigits % 10;
    }
}

class BankService {

    public final String bankBIN = "400000";
    public final int cardNumberOfDigits = 16;
    public final int pinNumberOfDigits = 4;
    public final String accountDBName = "card";

    List<Account> accounts = new ArrayList<>();
    List<BankingSystem> bankingSystems = new ArrayList<>();

    Account findAccount(Connection conn, String cardNumber) throws SQLException {

        Map parameters = new HashMap();
        parameters.put("number", cardNumber);

        List<Account> foundAccounts = DatabaseManager.findAccountsByParameters(conn, accountDBName, parameters);
        for (Account account : foundAccounts) {
            return account;
        }
        return null;
    }

    void saveAccountToDB(Connection conn, Account account) throws SQLException {

        Map parameters = new HashMap();
        parameters.put("number", account.cardNumber);

        List<Account> foundAccounts = DatabaseManager.findAccountsByParameters(conn, accountDBName, parameters);

        if (!foundAccounts.isEmpty()) {return;}

        DatabaseManager.insertRow(conn, accountDBName, List.of("number", "pin", "balance"), List.of(account.cardNumber, account.pin, account.balance));

    }

    void createAccountTable(Connection conn) throws SQLException {

        Map columns = new HashMap();

        columns.put("id", "INTEGER PRIMARY KEY");
        columns.put("number", "TEXT UNIQUE NOT NULL");
        columns.put("pin", "TEXT NOT NULL");
        columns.put("balance", "INTEGER NOT NULL DEFAULT 0");

        DatabaseManager.createTable(conn, accountDBName, columns);
    }

    boolean pinIsCorrect(Account account, String pin) {
        return account.pin.equals(pin);
    }
}