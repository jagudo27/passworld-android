package mobile.passworld.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mobile.passworld.R;
import mobile.passworld.exception.EncryptionException;
import mobile.passworld.session.UserSession;
import mobile.passworld.utils.EncryptionUtil;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    private TextView loginTextView;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        loginTextView = findViewById(R.id.loginTextView);

        // Evitar que se pegue texto en el campo de confirmación de contraseña
        confirmPasswordEditText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });

        confirmPasswordEditText.setLongClickable(false);
        confirmPasswordEditText.setTextIsSelectable(false);

        // Botón de registro
        signUpButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            // Validación de campos
            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email requerido");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Contraseña requerida");
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Las contraseñas no coinciden");
                return;
            }

            // Registrar usuario sin contraseña maestra inicialmente
            registerUser(email, password);
        });

        // Enlace para ir a inicio de sesión
        loginTextView.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish();
        });

        // Configuración de Google Sign-In con One Tap
        // En lugar del código One Tap, usa esto:
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("757458993089-q9d53gkrrvo412h3opl7n4249jp7on13.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

// Asegúrate que tienes un botón del tipo SignInButton
        SignInButton googleSignUpButton = findViewById(R.id.googleSignUpButton);
        googleSignUpButton.setOnClickListener(v -> signInWithGoogle());
        googleSignUpButton.setSize(SignInButton.SIZE_ICON_ONLY);

        // Botón de registro con Google
        findViewById(R.id.googleSignUpButton).setOnClickListener(v -> signInWithGoogle());
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Usuario registrado exitosamente
                        String userId = auth.getCurrentUser().getUid();
                        UserSession.getInstance().setUserId(userId);

                        // Solicitar contraseña maestra después del registro
                        showMasterPasswordDialog(userId);
                    } else {
                        // Error al registrar
                        Toast.makeText(SignUpActivity.this,
                                "Error al registrar: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveMasterPassword(String userId, String masterPasswordHash) {
        DatabaseReference userRef = dbRef.child("users").child(userId);
        userRef.child("masterPassword").setValue(masterPasswordHash)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, VaultUnlockActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this,
                                "Error al guardar la contraseña maestra",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private static final int REQ_ONE_TAP = 2;  // Puede ser cualquier número entero

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
                        String userId = auth.getCurrentUser().getUid();
                        UserSession.getInstance().setUserId(userId);

                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            // Primero pedir una contraseña para linkear con email/password
                            showLinkPasswordDialog(acct.getEmail(), userId);
                        } else {
                            // Usuario ya existente
                            startActivity(new Intent(this, VaultUnlockActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Error con Firebase: " + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMasterPasswordDialog(String userId) {
        // Crear diálogo sin título ni botones estándar
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);

        // Inflar layout personalizado
        View view = getLayoutInflater().inflate(R.layout.dialog_master_password, null);

        // Encontrar las vistas en el layout
        TextInputLayout masterPassLayout = view.findViewById(R.id.masterPassLayout);
        TextInputLayout confirmPassLayout = view.findViewById(R.id.confirmPassLayout);
        TextInputEditText masterPassInput = view.findViewById(R.id.masterPassInput);
        TextInputEditText confirmPassInput = view.findViewById(R.id.confirmPassInput);
        Button savePasswordButton = view.findViewById(R.id.savePasswordButton);

        // Establecer la vista del diálogo
        builder.setView(view);

        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Fondo transparente
        dialog.setCancelable(false);
        dialog.show();

        // Configurar el botón de guardar desde el layout
        savePasswordButton.setOnClickListener(v -> {
            String masterPass = masterPassInput.getText().toString();
            String confirmPass = confirmPassInput.getText().toString();

            boolean hasError = false;

            if (TextUtils.isEmpty(masterPass)) {
                masterPassLayout.setError("La contraseña maestra es obligatoria");
                hasError = true;
            } else {
                masterPassLayout.setError(null);
            }

            if (!masterPass.equals(confirmPass)) {
                confirmPassLayout.setError("Las contraseñas no coinciden");
                hasError = true;
            } else {
                confirmPassLayout.setError(null);
            }

            if (hasError) {
                return;
            }

            try {
                String hashedMasterPassword = EncryptionUtil.hashMasterPassword(masterPass);
                dialog.dismiss();
                saveMasterPassword(userId, hashedMasterPassword);
            } catch (EncryptionException e) {
                Toast.makeText(SignUpActivity.this, "Error al procesar la contraseña", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showLinkPasswordDialog(String email, String userId) {
        // Crear diálogo sin título ni botones estándar
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);

        // Inflar layout personalizado
        View view = getLayoutInflater().inflate(R.layout.dialog_link_password, null);

        // Encontrar las vistas en el layout
        TextInputLayout passwordLayout = view.findViewById(R.id.passwordLayout);
        TextInputLayout confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout);
        TextInputEditText passwordInput = view.findViewById(R.id.passwordInput);
        TextInputEditText confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        Button saveLinkPasswordButton = view.findViewById(R.id.saveLinkPasswordButton);

        // Establecer la vista del diálogo
        builder.setView(view);

        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Fondo transparente
        dialog.setCancelable(false);
        dialog.show();

        // Configurar el botón de guardar desde el layout
        saveLinkPasswordButton.setOnClickListener(v -> {
            String password = passwordInput.getText().toString();
            String confirm = confirmPasswordInput.getText().toString();

            boolean hasError = false;

            if (TextUtils.isEmpty(password)) {
                passwordLayout.setError("La contraseña es obligatoria");
                hasError = true;
            } else {
                passwordLayout.setError(null);
            }

            if (!password.equals(confirm)) {
                confirmPasswordLayout.setError("Las contraseñas no coinciden");
                hasError = true;
            } else {
                confirmPasswordLayout.setError(null);
            }

            if (hasError) {
                return;
            }

            // Vincular cuenta de Google con email/password
            AuthCredential emailCredential = EmailAuthProvider.getCredential(email, password);
            auth.getCurrentUser().linkWithCredential(emailCredential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            // Ahora sí pedir la contraseña maestra
                            showMasterPasswordDialog(userId);
                        } else {
                            Toast.makeText(SignUpActivity.this, "Error al vincular cuenta: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

}
