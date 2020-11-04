package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AccountHandler {

    //TODO Split this class into two separate classes, all methods below should be put in an auxiliary class:
    // - generatePin()
    // - generateCardNumber()
    // - generateChecksum()
    // - luhnNumber()


    private final Map<String, CurrentUser> registeredAccounts;
    private final Random randomizer;
    private CurrentUser currentUser;
    private final SQLiteDataSource dbSource;

    public AccountHandler(String dbName) {
        this.registeredAccounts = new HashMap<>();
        this.randomizer = new Random();
        this.dbSource = new SQLiteDataSource();
        startDb(dbName);
    }

    private void startDb(String dbName) {
        this.dbSource.setUrl("jdbc:sqlite:" + dbName);
        try (Connection con = this.dbSource.getConnection()) {
            DatabaseMetaData dbMeta = con.getMetaData();
            ResultSet result = dbMeta.getTables(null, null, "card", null);
            if (!result.next()) {
                try (Statement statement = con.createStatement()) {
                    String createTableString = "CREATE TABLE card (\n" +
                            "id INTEGER PRIMARY KEY,\n" +
                            "number TEXT,\n" +
                            "pin TEXT,\n" +
                            "balance INTEGER DEFAULT 0\n" +
                            ")";
                    statement.executeUpdate(createTableString);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String[] register() {
        String newCardNumber = generateCardNumber();
        String newPin = generatePin();
        try (Connection con = this.dbSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String insertStatement = String.format("INSERT INTO card (number, pin) VALUES\n" +
                                "(%s, %s)",
                        newCardNumber,
                        newPin
                );
                statement.executeUpdate(insertStatement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[] {newCardNumber, newPin};
    }

    public boolean attemptLoggingIn(String cardNumber, String pin) {
        try (Connection con = this.dbSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String selectStatement = String.format("SELECT * FROM card\n" +
                        "WHERE\n" +
                        "number = '%s'\n" +
                        "AND pin = %s\n" +
                        ";",
                        cardNumber,
                        pin
                );
                try (ResultSet queryResult = statement.executeQuery(selectStatement)) {
                    if (queryResult.next()) {
                        this.currentUser = new CurrentUser(pin, cardNumber, queryResult.getLong("balance"));
                        return true;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addIncome(long income) {
        try (Connection con = this.dbSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String addIncomeStatement = String.format("UPDATE card\n" +
                        "SET balance = balance + %s\n" +
                        "WHERE\n" +
                        "number = %s\n" +
                        "AND pin = %s\n" +
                        ";", income, this.currentUser.getCardNumber(), this.currentUser.getPin());
                statement.executeUpdate(addIncomeStatement);
                this.currentUser.earn(income);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int cardExists(String cardNumber) {
        //  0 - Such card exists in the database, 1 - incorrect card number, 2 - number isn't in the database
        //  3 - specified card number is the current user's card number
        if (!isCardCorrect(cardNumber)) {
            return 1;
        } else if (cardNumber.equals(this.currentUser.getCardNumber())) {
            return 3;
        }
        try (Connection con = this.dbSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String findCardHolder = String.format("SELECT * FROM card\n" +
                        "WHERE\n" +
                        "number = %s\n" +
                        ";", cardNumber);
                ResultSet cardHolderSearch = statement.executeQuery(findCardHolder);
                if (cardHolderSearch.next()) {
                    return 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 2;
    }

    public boolean transferMoney(String cardNumber, long moneyTransferred) {
        if (this.currentUser.getBalance() < moneyTransferred) {
            return false;
        }
        try (Connection con = this.dbSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String transferStatement = String.format("UPDATE card\n" +
                        "SET balance = balance + %d\n" +
                        "WHERE\n" +
                        "number = %s\n" +
                        ";", moneyTransferred, cardNumber);
                statement.executeUpdate(transferStatement);
                String spendStatement = String.format("UPDATE card\n" +
                        "SET balance = balance - %d\n" +
                        "WHERE\n" +
                        "number = %s\n" +
                        ";", moneyTransferred, this.currentUser.getCardNumber());
                statement.executeUpdate(spendStatement);
                this.currentUser.spend(moneyTransferred);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void deleteAccount() {
        try (Connection con = this.dbSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String deleteStatement = String.format("DELETE FROM card\n" +
                        "WHERE number = %s\n" +
                        ";", this.currentUser.getCardNumber());
                statement.executeUpdate(deleteStatement);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generatePin() {
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            pin.append(randomizer.nextInt(10));
        }
        return pin.toString();
    }

    private String generateCardNumber() {
        int[] cardNumberArray = new int[16];
        cardNumberArray[0] = 4;
        cardNumberArray[1] = 0;
        cardNumberArray[2] = 0;
        cardNumberArray[3] = 0;
        cardNumberArray[4] = 0;
        cardNumberArray[5] = 0;
        for (int i = 6; i < 15; i++) {
            cardNumberArray[i] = this.randomizer.nextInt(10);
        }
        cardNumberArray[15] = generateChecksum(cardNumberArray);
        StringBuilder numberBuilder = new StringBuilder();
        for (int num : cardNumberArray) {
            numberBuilder.append(num);
        }
        return numberBuilder.toString();
    }

    private boolean isCardCorrect(String cardNumber) {
        int proposedChecksum = Integer.parseInt(String.valueOf(cardNumber.charAt(cardNumber.length() - 1)));
        int[] proposedNumber = Arrays.stream(cardNumber.substring(0, cardNumber.length() - 1).split("")).mapToInt(Integer::parseInt).toArray();
        if (proposedChecksum == generateChecksum(proposedNumber)) {
            return true;
        }
        return false;
    }

    private int generateChecksum(int[] cardNumberArray) {
        int sum = 0;
        for (int i = 0; i < 15; i++) {
            int num = cardNumberArray[i];
            sum += luhnNumber(num, i + 1);
        }
        if (sum % 10 == 0) {
            return 0;
        }  else {
            return 10 - (sum % 10);
        }
    }

    private int luhnNumber(int number, int index) {
        if (index % 2 == 1) {
            number *= 2;
        }
        if (number > 9) {
            number -= 9;
        }
        return number;
    }

    public CurrentUser getCurrentUser() {
        return this.currentUser;
    }
}
