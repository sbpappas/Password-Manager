import javax.crypto.SecretKey;
import java.io.*;
import java.util.*;

public class VaultManager {
    private Map<String, PasswordEntry> entries = new HashMap<>();
    private final String vaultFile;
    private final SecretKey key;

    public VaultManager(String vaultFile, SecretKey key) throws Exception {
        this.vaultFile = vaultFile;
        this.key = key;

        File file = new File(vaultFile);
        if (file.exists()) {
            loadVault();
        } else {
            System.out.println("No vault found — starting fresh.");
        }
    }

    public void addEntry(PasswordEntry entry) {
        entries.put(entry.getSite(), entry); //call pwEntry class
    }

    public PasswordEntry getEntry(String site) {
        return entries.get(site);
    }

    public void listSites() {
        if (entries.isEmpty()) {
            System.out.println("Vault is empty.");
            return;
        }
        System.out.println("Stored sites:");
        for (String site : entries.keySet()) {
            System.out.println(" - " + site);
        }
    }

    public void saveVault() throws Exception {
        //convert entries to a simple serialized format and write them to file
        StringBuilder builder = new StringBuilder();
        for (PasswordEntry entry : entries.values()) {
            builder.append(entry.getSite()).append(",")
                   .append(entry.getUsername()).append(",")
                   .append(entry.getPassword()).append("\n");
        }

        String encrypted = CryptoUtils.encrypt(builder.toString(), key);
        try (FileWriter writer = new FileWriter(vaultFile)) {
            writer.write(encrypted); //write to file
        }
    }

    private void loadVault() throws Exception {
        StringBuilder encryptedData = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(vaultFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                encryptedData.append(line);
            }
        }

        String decrypted = CryptoUtils.decrypt(encryptedData.toString(), key);

        for (String line : decrypted.split("\n")) {
            String[] parts = line.split(",", 3);
            if (parts.length == 3) {
                PasswordEntry entry = new PasswordEntry(parts[0], parts[1], parts[2]);
                entries.put(parts[0], entry);
            }
        }

        System.out.println("Vault loaded with " + entries.size() + " entries.");
    }
}
