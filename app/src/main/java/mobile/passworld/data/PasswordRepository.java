package mobile.passworld.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import mobile.passworld.exception.EncryptionException;
import mobile.passworld.session.UserSession;
import mobile.passworld.utils.EncryptionUtil;
import mobile.passworld.utils.LogUtils;
import mobile.passworld.utils.SecurityFilterUtils;

public class PasswordRepository {

    private final DatabaseReference passwordsRef;
    private ValueEventListener passwordsListener;

    public PasswordRepository() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        passwordsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("passwords");

        passwordsRef.keepSynced(true);
    }

    // Interfaces de callbacks...
    public interface PasswordsCallback {
        void onPasswordsLoaded(List<PasswordDTO> passwords);
        void onError(DatabaseError error);
    }

    public interface PasswordCallback {
        void onPasswordLoaded(PasswordDTO password);
        void onError(DatabaseError error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Método para leer todas las contraseñas en tiempo real
    public void getAllPasswords(final PasswordsCallback callback) {
        SecretKeySpec masterKey = UserSession.getInstance().getMasterKey();

        // Primero removemos cualquier listener previo
        if (passwordsListener != null) {
            passwordsRef.removeEventListener(passwordsListener);
        }

        // Creamos y agregamos un nuevo listener en tiempo real
        passwordsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PasswordDTO> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PasswordDTO dto = new PasswordDTO();

                    // ID Firebase (clave)
                    dto.setIdFb(child.getKey());

                    // Campos String
                    dto.setDescription(child.child("description").getValue(String.class));
                    dto.setPassword(child.child("password").getValue(String.class));
                    dto.setUrl(child.child("url").getValue(String.class));
                    dto.setUsername(child.child("username").getValue(String.class));
                    // Recuperar el campo lastModified
                    String lastModified = child.child("lastModified").getValue(String.class);
                    dto.setLastModified(lastModified);

                    // Campos booleanos
                    Boolean weak = child.child("isWeak").getValue(Boolean.class);
                    dto.setWeak(weak != null && weak);

                    Boolean duplicate = child.child("isDuplicate").getValue(Boolean.class);
                    dto.setDuplicate(duplicate != null && duplicate);

                    Boolean urlUnsafe = child.child("isUrlUnsafe").getValue(Boolean.class);
                    dto.setUrlUnsafe(urlUnsafe != null && urlUnsafe);

                    Boolean compromised = child.child("isCompromised").getValue(Boolean.class);
                    dto.setCompromised(compromised != null && compromised);

                    // Desencriptar datos
                    try {
                        dto.setUrl(EncryptionUtil.decryptData(dto.getUrl(), masterKey));
                        dto.setUsername(EncryptionUtil.decryptData(dto.getUsername(), masterKey));
                        dto.setDescription(EncryptionUtil.decryptData(dto.getDescription(), masterKey));
                        dto.setPassword(EncryptionUtil.decryptData(dto.getPassword(), masterKey));
                    } catch (EncryptionException e) {
                        LogUtils.LOGGER.severe("Error descifrando contraseña: " + e.getMessage());
                    }
                    list.add(dto);
                }

                // Analizar seguridad de todas las contraseñas
                updateSecurityStatus(list);

                callback.onPasswordsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                LogUtils.LOGGER.severe("Error cargando contraseñas: " + error.getMessage());
                callback.onError(error);
            }
        };

        passwordsRef.addValueEventListener(passwordsListener);
    }

    // Método para detener la escucha (importante durante logout)
    public void removeListeners() {
        if (passwordsListener != null) {
            passwordsRef.removeEventListener(passwordsListener);
            passwordsListener = null;
        }
    }

    // Método para leer una contraseña por id y devolverla por callback
    public void getPasswordById(String idFb, final PasswordCallback callback) {
        SecretKeySpec masterKey = UserSession.getInstance().getMasterKey();

        passwordsRef.child(idFb).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onPasswordLoaded(null);
                    return;
                }

                PasswordDTO dto = new PasswordDTO();

                // ID Firebase (clave)
                dto.setIdFb(snapshot.getKey());

                // Campos String
                dto.setDescription(snapshot.child("description").getValue(String.class));
                dto.setPassword(snapshot.child("password").getValue(String.class));
                dto.setUrl(snapshot.child("url").getValue(String.class));
                dto.setUsername(snapshot.child("username").getValue(String.class));
                dto.setLastModified(snapshot.child("lastModified").getValue(String.class));

                // Campos booleanos
                Boolean weak = snapshot.child("isWeak").getValue(Boolean.class);
                dto.setWeak(weak != null && weak);

                Boolean duplicate = snapshot.child("isDuplicate").getValue(Boolean.class);
                dto.setDuplicate(duplicate != null && duplicate);

                Boolean urlUnsafe = snapshot.child("isUrlUnsafe").getValue(Boolean.class);
                dto.setUrlUnsafe(urlUnsafe != null && urlUnsafe);

                Boolean compromised = snapshot.child("isCompromised").getValue(Boolean.class);
                dto.setCompromised(compromised != null && compromised);

                // Desencriptar datos
                try {
                    dto.setUrl(EncryptionUtil.decryptData(dto.getUrl(), masterKey));
                    dto.setUsername(EncryptionUtil.decryptData(dto.getUsername(), masterKey));
                    dto.setDescription(EncryptionUtil.decryptData(dto.getDescription(), masterKey));
                    dto.setPassword(EncryptionUtil.decryptData(dto.getPassword(), masterKey));
                } catch (EncryptionException e) {
                    LogUtils.LOGGER.severe("Error descifrando contraseña: " + e.getMessage());
                }

                callback.onPasswordLoaded(dto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                LogUtils.LOGGER.severe("Error cargando contraseña por id: " + error.getMessage());
                callback.onError(error);
            }
        });
    }

    // Método para guardar una nueva contraseña
    public void savePassword(PasswordDTO password, OperationCallback callback) {
        try {
            SecretKeySpec masterKey = UserSession.getInstance().getMasterKey();

            // Analizar seguridad de la contraseña
            SecurityFilterUtils.analyzePasswordSecurity(password);

            // Generar ID si no existe
            if (password.getIdFb() == null || password.getIdFb().isEmpty()) {
                password.setIdFb(passwordsRef.push().getKey());
            }


            // Crear mapa con datos cifrados
            Map<String, Object> updates = new HashMap<>();
            updates.put("description", EncryptionUtil.encryptData(password.getDescription(), masterKey));

            String username = password.getUsername() == null ? "null" : password.getUsername();
            String url = password.getUrl() == null ? "null" : password.getUrl();

            if (username.equals("null") || username.isEmpty()) {
                updates.put("username", "null");
            } else {
                updates.put("username", EncryptionUtil.encryptData(username, masterKey));
            }

            if (url.equals("null") || url.isEmpty()) {
                updates.put("url", "null");
            } else {
                updates.put("url", EncryptionUtil.encryptData(url, masterKey));
            }

            updates.put("password", EncryptionUtil.encryptData(password.getPassword(), masterKey));
            updates.put("isWeak", password.isWeak());
            updates.put("isDuplicate", password.isDuplicate());
            updates.put("isCompromised", password.isCompromised());
            updates.put("isUrlUnsafe", password.isUrlUnsafe());
            updates.put("lastModified", password.getLastModified());

            // Guardar en Firebase
            passwordsRef.child(password.getIdFb()).setValue(updates)
                    .addOnSuccessListener(aVoid -> {
                        SecurityFilterUtils.addUniquePassword(password.getPassword());
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> callback.onError(e));

        } catch (Exception e) {
            LogUtils.LOGGER.severe("Error guardando contraseña: " + e.getMessage());
            callback.onError(e);
        }
    }

    // Método para actualizar contraseña
    public void updatePassword(PasswordDTO password, OperationCallback callback) {
        try {
            SecretKeySpec masterKey = UserSession.getInstance().getMasterKey();

            // Crear mapa con datos cifrados
            Map<String, Object> updates = new HashMap<>();
            updates.put("description", EncryptionUtil.encryptData(password.getDescription(), masterKey));

            String username = password.getUsername() == null ? "null" : password.getUsername();
            String url = password.getUrl() == null ? "null" : password.getUrl();

            if (username.equals("null") || username.isEmpty()) {
                updates.put("username", "null");
            } else {
                updates.put("username", EncryptionUtil.encryptData(username, masterKey));
            }

            if (url.equals("null") || url.isEmpty()) {
                updates.put("url", "null");
            } else {
                updates.put("url", EncryptionUtil.encryptData(url, masterKey));
            }

            updates.put("password", EncryptionUtil.encryptData(password.getPassword(), masterKey));
            updates.put("isWeak", password.isWeak());
            updates.put("isDuplicate", password.isDuplicate());
            updates.put("isCompromised", password.isCompromised());
            updates.put("isUrlUnsafe", password.isUrlUnsafe());
            updates.put("lastModified", password.getLastModified());

            // Actualizar en Firebase
            passwordsRef.child(password.getIdFb()).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError(e));

        } catch (EncryptionException e) {
            callback.onError(e);
        }
    }

    // Método para eliminar contraseña
    public void deletePassword(String passwordId, OperationCallback callback) {
        passwordsRef.child(passwordId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e));
    }

    private void updateSecurityStatus(List<PasswordDTO> passwords) {
        SecurityFilterUtils.clearUniquePasswords();

        // Analizar cada contraseña
        for (PasswordDTO password : passwords) {
            SecurityFilterUtils.analyzePasswordSecurity(password);
        }
    }

    // Método para actualizar la seguridad de todas las contraseñas
    private void updateAllPasswordsSecurity() {
        getAllPasswords(new PasswordsCallback() {
            @Override
            public void onPasswordsLoaded(List<PasswordDTO> passwords) {
                SecurityFilterUtils.clearUniquePasswords();

                // Analizar y actualizar cada contraseña
                for (PasswordDTO password : passwords) {
                    SecurityFilterUtils.analyzePasswordSecurity(password);

                    // Actualizar en Firebase sin callback (operación silenciosa)
                    try {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("isWeak", password.isWeak());
                        updates.put("isDuplicate", password.isDuplicate());
                        updates.put("isCompromised", password.isCompromised());
                        updates.put("isUrlUnsafe", password.isUrlUnsafe());

                        passwordsRef.child(password.getIdFb())
                                .updateChildren(updates);
                    } catch (Exception e) {
                        LogUtils.LOGGER.severe("Error actualizando seguridad de contraseña: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(DatabaseError error) {
                LogUtils.LOGGER.severe("Error actualizando seguridad de contraseñas: " + error.getMessage());
            }
        });
    }
}