import javax.crypto.SecretKey;
import java.util.Scanner;

public class Main {
    private static VaultManager vault;

    public static void main(String[] args) {
        System.out.println("Welcome to Samuel's Password Manager");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter master password: ");
        String masterPassword = scanner.nextLine();

        try {

            byte[] salt = VaultManager.loadOrCreateSalt();
            SecretKey key = CryptoUtils.deriveKey(masterPassword, salt);
            VaultManager vault = new VaultManager("vault.dat", key, salt); // Load or initialize vault
            
            new Thread(() -> { //for auto locking
                while (true) {
                    long now = System.currentTimeMillis();
                    long inactiveTime = now - vault.getLastActivityTime();
                    if (inactiveTime > 2 * 60 * 1000) { // 2 minutes
                        System.out.println("\nAuto-locked due to inactivity.");
                        vault.lock(); // Clear sensitive data
                        System.out.println("\n🔒 Vault auto-locked due to inactivity.");
                        reAuthenticate(); //redirect to login loop
                    }
                    try {
                        Thread.sleep(5000); // every 5 seconds
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();
            
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
                        System.exit(0);
                        return;

                    default:
                        System.out.println("Unknown command. \nCommands: [add, list, get, exit]");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }    
    }

    private static void reAuthenticate() { //for auto-locking
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter master password to unlock: ");
            String attempt = scanner.nextLine();
            if (vault.unlock(attempt)) {
                System.out.println("Password Vault unlocked.");
                break;
            } else {
                System.out.println("Incorrect password. Try again.");
            }
        }
    }
   
    
}
