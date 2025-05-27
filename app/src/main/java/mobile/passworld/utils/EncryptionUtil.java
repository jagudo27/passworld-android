package mobile.passworld.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import mobile.passworld.exception.EncryptionException;
import mobile.passworld.session.UserSession;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
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
            return new SecretKeySpec(derivedKey, ALGORITHM);
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
            cipher.init(Cipher.ENCRYPT_MODE, masterKey);
            byte[] encrypted = cipher.doFinal(plainPassword.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
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
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, masterKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decrypted);
        } catch (Exception e) {
            throw new EncryptionException("Error decrypting data" + e.getMessage(), e);
        }
    }
}
