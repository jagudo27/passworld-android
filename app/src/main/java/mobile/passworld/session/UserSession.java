package mobile.passworld.session;


import javax.crypto.spec.SecretKeySpec;
import java.sql.SQLException;
import java.util.Arrays;

public class UserSession {
    private static UserSession instance;
    private String userId;
    private transient String idToken;
    private transient String refreshToken;
    private transient SecretKeySpec masterKey;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isLoggedIn() {
        return userId != null && !userId.isEmpty();
    }

    public void setLoggedIn() {
    }

    // Master key en memoria
    public SecretKeySpec getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(SecretKeySpec masterKey) {
        this.masterKey = masterKey;
    }

    // Limpia todos los datos sensibles de la sesión
    public void clearSession() throws SQLException {
        userId = null;
        if (idToken != null) {
            Arrays.fill(idToken.toCharArray(), '\0');
            idToken = null;
        }
        if (refreshToken != null) {
            Arrays.fill(refreshToken.toCharArray(), '\0');
            refreshToken = null;
        }
        clearMasterKey();

    }

    // Limpia la master key de memoria
    public void clearMasterKey() {
        if (masterKey != null) {
            byte[] keyBytes = masterKey.getEncoded();
            Arrays.fill(keyBytes, (byte) 0);
            masterKey = null;
        }
    }
}