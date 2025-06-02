package mobile.passworld.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import mobile.passworld.session.UserSession;

public class UserRepository {

    public interface MasterPasswordCallback {
        void onSuccess(String masterPasswordHash);
        void onFailure(Exception e);
    }

    public static void getMasterPassword(MasterPasswordCallback callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("masterPassword");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String hash = snapshot.getValue(String.class);
                    callback.onSuccess(hash);
                } else {
                    callback.onFailure(new Exception("No masterPassword found for user " + userId));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }
}

