package banking;

import java.util.Scanner;

public class BankingSystem {

    private final AccountHandler handler;
    private boolean loggedIn;
    private final Scanner scanner;

    public BankingSystem(String dbName) {
        this.handler = new AccountHandler(dbName);
        this.loggedIn = false;
        this.scanner = new Scanner(System.in);
    }

    public void startService() {
        service();
    }

    private void service() {
        String userInput;
        do {
            UserInterface.display(this.loggedIn);
            userInput = this.scanner.next();
        }
        while (!processInput(userInput));
        UserInterface.farewell();
    }

    private boolean processInput(String userInput) {
        if (this.loggedIn) {
            switch (userInput) {
                case "1": // Check balance
                    System.out.printf("Balance: %d\n", handler.getCurrentUser().getBalance());
                    break;
                case "2": // Add income
                    System.out.println("Enter income:");
                    long incomeAdded = this.scanner.nextLong();
                    if (handler.addIncome(incomeAdded)) {
                        System.out.println("Income was added!");
                    }
                    break;
                case "3": // Transfer money
                    System.out.println("Transfer\n" +
                            "Enter credit card number:");
                    String transferNumber = this.scanner.next();
                    switch (this.handler.cardExists(transferNumber)) {
                        case 0: // account, other than the user was found in the database
                            System.out.println("Enter how much money you want to transfer:\n");
                            long money = this.scanner.nextLong();
                            if (this.handler.transferMoney(transferNumber, money)) {
                                System.out.println("Success!");
                            } else {
                                System.out.println("Not enough money!");
                            }
                            break;
                        case 1: // card number doesn't adhere to the luhn algorithm
                            System.out.println("Probably you made mistake in the card number\n" +
                                    "Please try again!");
                            break;
                        case 2: // there isn't such a card in the database
                            System.out.println("Such a card does not exist.");
                            break;
                        case 3:

                            break;
                        default:
                            break;
                    }
                    break;
                case "4": // Close account//TODO
                    this.handler.deleteAccount();
                    this.loggedIn = false;
                    System.out.println("The account has been closed!");
                    break;
                case "5": // Log out
                    this.loggedIn = false;
                    System.out.println("You have successfully logged out!\n");
                    break;
                case "0": // Exit
                    return true;
            }
        } else {
            switch (userInput) {
                case "1":
                    String[] registerData = handler.register();
                    System.out.printf("Your card has been created\n" +
                            "Your card number:\n" +
                            "%s\n" +
                            "Your card PIN:\n" +
                            "%s\n", registerData[0], registerData[1]);
                    break;
                case "2":
                    System.out.println("Enter your card number:");
                    String cardNumberEntered = this.scanner.next();
                    System.out.println("Enter your PIN:");
                    String pinEntered = this.scanner.next();
                    if (handler.attemptLoggingIn(cardNumberEntered, pinEntered)) {
                        this.loggedIn = true;
                        System.out.println("You have successfully logged in!\n");
                    } else {
                        System.out.println("Wrong card number or PIN!\n");
                    }
                    break;
                case "0":
                    return true;
            }
        }
        return false;
    }

}
