package mobile.passworld.activity;// SignInActivity.java


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import mobile.passworld.R;
import mobile.passworld.activity.VaultUnlockActivity;


public class SignInActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private SignInButton signInButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // Ya logeado
            startActivity(new Intent(this, VaultUnlockActivity.class));
            finish();
            return;
        }

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.googleSignInButton);
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);

        signInButton.setSize(SignInButton.SIZE_ICON_ONLY);

        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Correo requerido");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Contraseña requerida");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignInActivity.this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            startActivity(new Intent(SignInActivity.this, VaultUnlockActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this, "Error de autenticación.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}