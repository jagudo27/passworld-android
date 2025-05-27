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
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import mobile.passworld.exception.EncryptionException;
import mobile.passworld.session.UserSession;
import mobile.passworld.utils.EncryptionUtil;
import mobile.passworld.utils.LogUtils;

public class PasswordRepository {

    private final DatabaseReference passwordsRef;

    public PasswordRepository() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        passwordsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("passwords");

        passwordsRef.keepSynced(true);
    }

    // Callback interface para devolver lista de PasswordDTO
    public interface PasswordsCallback {
        void onPasswordsLoaded(List<PasswordDTO> passwords);
        void onError(DatabaseError error);
    }

    // Callback interface para devolver una sola PasswordDTO
    public interface PasswordCallback {
        void onPasswordLoaded(PasswordDTO password);
        void onError(DatabaseError error);
    }

    // Método para leer todas las contraseñas y devolverlas por callback
    public void getAllPasswords(final PasswordsCallback callback) {
        SecretKeySpec masterKey = UserSession.getInstance().getMasterKey();
        passwordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PasswordDTO> list = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PasswordDTO dto = child.getValue(PasswordDTO.class);
                    if (dto != null) {
                        dto.setIdFb(child.getKey());
                        try {
                            Log.d("PasswordRepository", "Decrypting password for ID: " + dto.getIdFb() + "password: " + dto.getPassword());
                            Log.d("PasswordRepository", "Decrypting URL for ID: " + dto.getIdFb() + "url: " + dto.getUrl());
                            Log.d("PasswordRepository", "Decrypting username for ID: " + dto.getIdFb() + "username: " + dto.getUsername());
                            Log.d("PasswordRepository", "Decrypting description for ID: " + dto.getIdFb() + "description: " + dto.getDescription());
                            dto.setUrl(EncryptionUtil.decryptData(dto.getUrl(), masterKey));
                            dto.setUsername(EncryptionUtil.decryptData(dto.getUsername(), masterKey));
                            dto.setDescription(EncryptionUtil.decryptData(dto.getDescription(), masterKey));
                            dto.setPassword(EncryptionUtil.decryptData(dto.getPassword(), masterKey));
                        } catch (EncryptionException e) {
                            throw new RuntimeException(e);
                        }
                        list.add(dto);
                    }
                }
                callback.onPasswordsLoaded(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                LogUtils.LOGGER.severe("Error loading passwords: " + error.getMessage());
                callback.onError(error);
            }
        });
    }

    // Método para leer una contraseña por id y devolverla por callback
    public void getPasswordById(String idFb, final PasswordCallback callback) {
        passwordsRef.child(idFb).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PasswordDTO dto = snapshot.getValue(PasswordDTO.class);
                if (dto != null) {
                    dto.setIdFb(snapshot.getKey());
                }
                callback.onPasswordLoaded(dto);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                LogUtils.LOGGER.severe("Error loading password by id: " + error.getMessage());
                callback.onError(error);
            }
        });
    }
}
