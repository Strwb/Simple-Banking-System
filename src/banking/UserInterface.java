package banking;

public class UserInterface {

    public static void display(boolean logged) {
        if (!logged) {
            System.out.println("1. Create an account\n2. Log into account\n0. Exit");
        } else {
            System.out.println("1. Balance\n" +
                    "2. Add income\n" +
                    "3. Do transfer\n" +
                    "4. Close account\n" +
                    "5. Log out\n" +
                    "0. Exit");
        }
    }

    public static void farewell(){
        System.out.println("Bye!");
    }

}
