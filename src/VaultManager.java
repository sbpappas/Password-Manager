import javax.crypto.SecretKey;
import java.io.*;
import java.util.*;
import java.nio.file.Files; //for file output stream
import java.nio.file.Paths;

import java.nio.charset.StandardCharsets; //for bytearray to string decryption

public class VaultManager {
    private Map<String, PasswordEntry> entries = new HashMap<>();
    private final String vaultFile;
    private SecretKey key;
    private byte[] salt;
    private static final String saltFile = "salt.bin";
    private long lastActivityTime = System.currentTimeMillis(); //to do auto logout after inactivity


    public VaultManager(String vaultFile, SecretKey key, byte[] salt) throws Exception {
        this.vaultFile = vaultFile;
        this.key = key;
        this.salt = salt;
    
        File file = new File(vaultFile);
        if (file.exists()) {
            loadVault();
        } else {
            System.out.println("No vault found — starting fresh.");
        }
    }

    public static byte[] loadOrCreateSalt() throws IOException {
        File saltFileObj = new File(saltFile);
        if (saltFileObj.exists()) {
            return Files.readAllBytes(saltFileObj.toPath());
        } else {
            byte[] newSalt = CryptoUtils.generateSalt();
            Files.write(saltFileObj.toPath(), newSalt);
            return newSalt;
        }
    }

    private void loadFromString(String decrypted) {
        entries.clear(); // Clear old entries before loading new ones
        for (String line : decrypted.split("\n")) {
            String[] parts = line.split(",", 3);
            if (parts.length == 3) {
                PasswordEntry entry = new PasswordEntry(parts[0], parts[1], parts[2]);
                entries.put(parts[0], entry);
            }
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

    public Set<String> getSiteNames() {
        return entries.keySet();
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
    
    public void setKey(SecretKey key) {
        this.key = key;
    }
    
    public void lock() { // called on auto-lock
        key = null;
    }

    public boolean unlock(String password) { //trying to relogin if autologged out
        try {
            SecretKey newKey = CryptoUtils.deriveKey(password, salt);
            byte[] encryptedData = Files.readAllBytes(Paths.get(vaultFile));
            String decrypted = new String(CryptoUtils.decrypt(encryptedData, newKey), StandardCharsets.UTF_8);
            loadFromString(decrypted);
            this.key = newKey;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void loadVault() throws Exception {
        byte[] encryptedData = Files.readAllBytes(Paths.get(vaultFile));
        String decrypted = new String(CryptoUtils.decrypt(encryptedData, key), StandardCharsets.UTF_8);
        loadFromString(decrypted);
        System.out.println("Vault loaded with " + entries.size() + " entries.");
    }
}
