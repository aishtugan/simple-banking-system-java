
package banking;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        BankService bankService = new BankService();

        BankingSystem defaultBS = new BankingSystem("empty", "");

        String command1 = "1. Create an account";
        String command2 = "2. Log into account";
        String command0 = "0. Exit";

        Account currentAccount = null;

        boolean continueLoop = true;

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
                            Account foundAccount = bankService.findAccount(inputCardNumber);
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

        int checksum = rand.nextInt(10); //for the first step
//        for (int i = 0; i < bankingSystem.cardCode.length() - bankBIN.length() - 1; i++) {
//
//        }

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

    List<Account> accounts = new ArrayList<>();
    List<BankingSystem> bankingSystems = new ArrayList<>();

    Account findAccount(String cardNumber) {
        for (Account account : accounts) {
            if (account.cardNumber.equals(cardNumber)) {
                return account;
            }
        }
        return null;
    }

    boolean pinIsCorrect(Account account, String pin) {
        return account.pin.equals(pin);
    }
}