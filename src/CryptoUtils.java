import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class CryptoUtils {

    private static final int KEY_LENGTH = 256; //final is immutable
    private static final String SALT_FILE = "salt.bin";
    private static final int ITERATIONS = 65536;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;

    // derives an AES key from the master password (new salt generated per session) - need switch
    /*public static SecretKey deriveKey(String password) throws Exception {
        // should make it so salt can be stored and reused
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }*/

    public static SecretKey deriveKey(String password) throws Exception {
        byte[] salt = getOrCreateSalt();

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); //password based key derivation function with SHA256
        // i read that this hashes the password and salt together over 65,536 iterations. neat.
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES"); //converts raw 256 bit key to AES SecretKey object which can be used by Cipher to encrypt/decrypt
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
    
        // Prepend IV to ciphertext
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv);
        outputStream.write(encrypted);
        return outputStream.toByteArray();
    }
    

    // Decrypts Base64(salt + IV + ciphertext) using AES-CBC
    public static String decrypt(String cipherTextBase64, SecretKey key) throws Exception {
        byte[] cipherText = Base64.getDecoder().decode(cipherTextBase64);

        byte[] iv = new byte[IV_LENGTH];
        byte[] encrypted = new byte[cipherText.length - IV_LENGTH];

        System.arraycopy(cipherText, 0, iv, 0, IV_LENGTH);
        System.arraycopy(cipherText, IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted);
    }
}
