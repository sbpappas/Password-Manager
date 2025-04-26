import javax.crypto.SecretKey;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to Samuel's Password Manager");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter master password: ");
        String masterPassword = scanner.nextLine();

        try {
            // Derive AES key
            SecretKey key = CryptoUtils.deriveKey(masterPassword);

            // Load or initialize vault
            VaultManager vault = new VaultManager("vault.dat", key);

            while (true) {
                System.out.print("\n[Commands: add, list, get, exit]\n> ");
                String command = scanner.nextLine().trim();

                switch (command) {
                    case "add":
                        System.out.print("Site: ");
                        String site = scanner.nextLine();
                        System.out.print("Username: ");
                        String username = scanner.nextLine();
                        System.out.print("Password: ");
                        String password = scanner.nextLine();
                        vault.addEntry(new PasswordEntry(site, username, password));
                        System.out.println("Entry added.");
                        break;

                    case "list":
                        vault.listSites();
                        break;

                    case "get":
                        System.out.print("Site to retrieve: ");
                        String siteToGet = scanner.nextLine();
                        PasswordEntry entry = vault.getEntry(siteToGet);
                        if (entry != null) {
                            System.out.println("Username: " + entry.getUsername());
                            System.out.println("Password: " + entry.getPassword());
                        } else {
                            System.out.println("entry NOT found.");
                        }
                        break;

                    case "exit":
                        vault.saveVault();
                        System.out.println("Vault saved. Goodbye!");
                        return;

                    default:
                        System.out.println("Unknown command. \nCommands: [add, list, get, exit]");
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
