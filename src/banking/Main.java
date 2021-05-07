package banking;

//TODO the following functionalities have to be implemented:
// - Add income (DONE)
// - Do transfer: (DONE)
//      - Check if specified card number is a proper credit card number OK
//      - Check if specified card number is in the database OK
//      - Send money OK
// - Close account:
//      - Delete user's row from the database
//      - Log out
//      - Print "The account has been closed!"


public class Main {
    public static void main(String[] args) {
        String dbName = args[1];
        System.out.println("Hello World!");
        BankingSystem bank = new BankingSystem(dbName);
        bank.startService();
    }
}
