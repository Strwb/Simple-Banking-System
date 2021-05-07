package banking;

public class CurrentUser {

    private long balance = 0L;
    private final String pin;
    private final String cardNumber;

    public CurrentUser(String pin, String cardNumber, long balance) {
        this.pin = pin;
        this.cardNumber = cardNumber;
        this.balance = balance;
    }

    public String getPin() {
        return this.pin;
    }

    public String getCardNumber() {
        return this.cardNumber;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public void earn(long balance) {
        this.balance += balance;
    }

    public void spend(long balance) {
        this.balance -= balance;
    }

    public long getBalance() {
        return this.balance;
    }
}
