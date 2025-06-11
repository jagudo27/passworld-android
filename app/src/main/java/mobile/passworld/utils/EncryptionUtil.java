package mobile.passworld.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import mobile.passworld.data.exception.EncryptionException;
import mobile.passworld.data.session.UserSession;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private static byte[] getSalt() {
        return UserSession.getInstance().getUserId().getBytes(); // Usar el UID del usuario como sal
    }

    // === HASHING AND VERIFICATION ===
    public static String hashMasterPassword(String masterPassword) throws EncryptionException {
        try {
            PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), getSalt(), ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new EncryptionException("Error hashing master password", e);
        }
    }

    public static boolean verifyMasterPassword(String enteredPassword, String storedHashBase64) throws EncryptionException {
        try {
            String hashOfInput = hashMasterPassword(enteredPassword);
            return hashOfInput.equals(storedHashBase64);
        } catch (Exception e) {
            throw new EncryptionException("Error verifying master password", e);
        }
    }

    public static SecretKeySpec deriveAESKey(String masterPassword) throws EncryptionException {
        try {
            PBEKeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), getSalt(), ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] derivedKey = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(derivedKey, "AES");
        } catch (Exception e) {
            throw new EncryptionException("Error deriving AES key", e);
        }
    }

    // === ENCRYPTION AND DECRYPTION ===
    public static String encryptData(String plainPassword, SecretKeySpec masterKey) throws EncryptionException {
        if (plainPassword == null || masterKey == null) {
            throw new EncryptionException("Cannot encrypt data: input is null");
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, ivSpec);

            byte[] encrypted = cipher.doFinal(plainPassword.getBytes(StandardCharsets.UTF_8));

            // Concatenamos IV + cifrado y codificamos en Base64
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Error encrypting data", e);
        }
    }

    public static String decryptData(String encryptedPassword, SecretKeySpec masterKey) throws EncryptionException {
        if (encryptedPassword == null || masterKey == null || encryptedPassword.equals("null") ) {
            return null;
        }

        Log.d("EncryptionUtil", "UID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
        Log.d("EncryptionUtil", "Master Key: " + Base64.getEncoder().encodeToString(masterKey.getEncoded()));

        try {
            byte[] combined = Base64.getDecoder().decode(encryptedPassword);
            byte[] iv = new byte[16];
            byte[] encryptedBytes = new byte[combined.length - 16];

            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, masterKey, ivSpec);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Error decrypting data: " + e.getMessage(), e);
        }
    }
}