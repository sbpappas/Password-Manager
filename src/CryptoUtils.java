import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class CryptoUtils {

    private static final int KEY_SIZE = 256; //final is immutable
    private static final int ITERATIONS = 65536;
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 16;

    // derives an AES key from the master password (new salt generated per session) - need switch
    public static SecretKey deriveKey(String password) throws Exception {
        // should make it so salt can be stored and reused
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    // Encrypts plain text using AES-CBC and returns Base64(salt + IV + ciphertext)
    public static String encrypt(String plainText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());

        byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedWithIv);
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
