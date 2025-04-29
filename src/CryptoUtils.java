import javax.crypto.*;
import java.security.*;
import java.util.Base64;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

public class CryptoUtils {

    private static final int KEY_LENGTH = 256; //final is immutable
    private static final String SALT_FILE = "salt.bin";
    private static final int ITERATIONS = 65536;
    private static final int SALT_LENGTH = 16;

    public static SecretKey deriveKey(String password) throws Exception {
        byte[] salt = getOrCreateSalt();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); //password based key derivation function with SHA256
        // i read that this hashes the password and salt together over 65,536 iterations. neat.
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES"); //converts raw 256 bit key to AES SecretKey object which can be used by Cipher to encrypt/decrypt
    }

    public static SecretKey deriveKey(String password, byte[] salt) throws Exception { //overload for when we have salt
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    // Encrypts plain text using AES-CBC and returns Base64 -salt + IV + ciphertext
    public static byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); //cbc = cipher block chaining
        byte[] iv = new byte[16]; //random secret key to encrypt our data
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
    
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
    
        byte[] encrypted = cipher.doFinal(data);
    
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv); // prepend IV to ciphertext
        outputStream.write(encrypted); // ciphertext
        return outputStream.toByteArray();
    }
    
    // Decrypts Base64(salt + IV + ciphertext) using AES-CBC
    public static byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedData);
        byte[] iv = new byte[16];
        inputStream.read(iv);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        byte[] ciphertext = inputStream.readAllBytes();

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(ciphertext);
    }

    //used in deriveKey() if we already made a salt, get it, if not make one
    private static byte[] getOrCreateSalt() throws IOException {
        File saltFile = new File(SALT_FILE);

        if (saltFile.exists()) {
            return Files.readAllBytes(saltFile.toPath());
        } else {
            byte[] salt = new byte[SALT_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt); //randomly make a salt
            Files.write(saltFile.toPath(), salt);
            return salt;
        }
    }

    public static byte[] generateSalt() { //used in vaultmanager loadorcreatesalt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

}
