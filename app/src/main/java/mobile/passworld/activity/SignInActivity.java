package mobile.passworld.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

import mobile.passworld.R;
import mobile.passworld.data.session.UserSession;
import mobile.passworld.utils.PasswordEvaluator;

public class SignInActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private Button signInButton;
    private SignInButton signInButtonGoogle;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        signInButtonGoogle = findViewById(R.id.googleSignInButton);
        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        TextView registerTextView = findViewById(R.id.registerTextView);
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Configuración de Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("757458993089-q9d53gkrrvo412h3opl7n4249jp7on13.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButtonGoogle.setSize(SignInButton.SIZE_ICON_ONLY);
        signInButtonGoogle.setOnClickListener(v -> signInWithGoogle());

        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            // Validar campo de email
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError(getString(R.string.required_email_field));
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError(getString(R.string.invalid_email_format));
                return;
            }

            // Validar campo de contraseña
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError(getString(R.string.required_password_field));
                return;
            }
            if (!PasswordEvaluator.isPasswordValid(this, password, passwordEditText)) {
                return; // La contraseña no es válida, no continuar
            }
            progressBar.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignInActivity.this, task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            UserSession.getInstance().setUserId(auth.getCurrentUser().getUid());
                            startActivity(new Intent(SignInActivity.this, VaultUnlockActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Inicio de sesión con Google fallido",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void signInWithGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error con Google SignIn: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential googleCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(googleCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                        UserSession.getInstance().setUserId(userId);

                        // Ir directamente a la actividad de desbloqueo
                        startActivity(new Intent(SignInActivity.this, VaultUnlockActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Error con Firebase: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


}