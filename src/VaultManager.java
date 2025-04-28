import javax.crypto.SecretKey;
import java.io.*;
import java.util.*;
import java.nio.file.Files; //for file output stream
import java.nio.file.Paths;

import java.nio.charset.StandardCharsets; //for bytearray to string decryption

public class VaultManager {
    private Map<String, PasswordEntry> entries = new HashMap<>();
    private final String vaultFile;
    private final SecretKey key;
    private long lastActivityTime = System.currentTimeMillis(); //to do auto logout after inactivity


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
        updateLastActivityTime();
        entries.put(entry.getSite(), entry); //call pwEntry class
    }

    public PasswordEntry getEntry(String site) {
        updateLastActivityTime();
        return entries.get(site);
    }

    public void listSites() {
        updateLastActivityTime();
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

        byte[] encrypted = CryptoUtils.encrypt(builder.toString().getBytes(StandardCharsets.UTF_8), key); //have to pass in bytearray
        FileOutputStream fos = new FileOutputStream(vaultFile);
            fos.write(encrypted);//write to file
            fos.close();
    }

    public void updateLastActivityTime() {
        lastActivityTime = System.currentTimeMillis();
    }

    public long getLastActivityTime(){
        return lastActivityTime;
    }

    private void loadVault() throws Exception {
        byte[] encryptedData = Files.readAllBytes(Paths.get(vaultFile));
        String decrypted = new String(CryptoUtils.decrypt(encryptedData, key), StandardCharsets.UTF_8);

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
