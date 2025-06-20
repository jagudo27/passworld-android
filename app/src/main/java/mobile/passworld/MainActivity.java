package mobile.passworld;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

import mobile.passworld.activity.SignInActivity;
import mobile.passworld.activity.VaultUnlockActivity;
import mobile.passworld.data.session.UserSession;
import mobile.passworld.utils.UserPreferences;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Aplicar el idioma guardado
        UserPreferences.applyLanguage(this);

        // Aplicar el modo oscuro guardado
        boolean isDarkMode = UserPreferences.getDarkModePreference(this);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Comprobar si el usuario está autenticado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // No hay usuario logueado, abrir SignInActivity
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();  // Opcional: cerrar MainActivity para que no vuelva con "back"
        } else {
            UserSession.getInstance().setUserId(currentUser.getUid());
            // Usuario autenticado, abrir VaultUnlockActivity
            Intent intent = new Intent(this, VaultUnlockActivity.class);
            startActivity(intent);
            finish();  // Opcional: cerrar MainActivity para que no vuelva con "back"
        }
    }
}
