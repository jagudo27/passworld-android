package mobile.passworld.activity;

import static mobile.passworld.R.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import mobile.passworld.R;
import mobile.passworld.data.UserRepository;
import mobile.passworld.data.session.UserSession;
import mobile.passworld.utils.EncryptionUtil;


public class VaultUnlockActivity extends AppCompatActivity {
    private EditText masterPasswordEditText;
    private Button unlockButton;
    private TextView errorLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault_unlock);

        masterPasswordEditText = findViewById(R.id.masterPasswordField);
        unlockButton = findViewById(R.id.unlockButton);
        errorLabel = findViewById(R.id.errorLabel);

        unlockButton.setOnClickListener(v -> {
            String input = masterPasswordEditText.getText().toString().trim();
            if (!input.isEmpty()) {
                attemptUnlock(input);
            } else {
                showError("Please enter your master password");
            }
        });
    }

    private void attemptUnlock(String inputPassword) {
        UserRepository userRepository = new UserRepository();
        userRepository.getMasterPassword(new UserRepository.MasterPasswordCallback() {
            @Override
            public void onSuccess(String storedHash) {
                try {
                    boolean verified = EncryptionUtil.verifyMasterPassword(inputPassword, storedHash);
                    if (verified) {
                        UserSession.getInstance().setMasterKey(EncryptionUtil.deriveAESKey(inputPassword));
                        goToVault();
                    } else {
                        showError(getString(string.incorrect_master_password));
                    }
                } catch (Exception e) {
                    showError("Error verifying password");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                showError("Error loading password");
                e.printStackTrace();
            }
        });
    }

    private void goToVault() {
        startActivity(new Intent(VaultUnlockActivity.this, PasswordListActivity.class));
        finish();
    }

    private void showError(String message) {
        errorLabel.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}