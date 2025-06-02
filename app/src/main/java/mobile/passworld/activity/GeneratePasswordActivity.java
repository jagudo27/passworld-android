package mobile.passworld.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mobile.passworld.R;
import mobile.passworld.activity.fragment.SavePasswordDialogFragment;
import mobile.passworld.data.PasswordRepository;
import mobile.passworld.utils.PasswordEvaluator;
import mobile.passworld.utils.SecurityFilterUtils;

public class GeneratePasswordActivity extends AppCompatActivity {

    private TextView generatedPasswordText;
    private TextView passwordStrengthLabel;
    private TextView strengthIndicatorText;
    private ProgressBar passwordStrengthBar;
    private CheckBox upperLowerCaseCheckbox;
    private CheckBox numbersCheckbox;
    private CheckBox specialCharsCheckbox;
    private TextView lengthLabel;
    private SeekBar lengthSeekBar;
    private MaterialButton generateButton;
    private MaterialButton saveButton;
    private ImageButton copyPasswordButton;
    private ImageButton btnBack;

    private PasswordRepository passwordRepository = new PasswordRepository();

    // Constantes para la generación de contraseñas
    private static final String UPPERCASE = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnñopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_+=<>?/{}[]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_password);

        // Inicializar vistas
        initViews();
        
        // Configurar listeners
        setupListeners();
        
        // Generar contraseña inicial
        generatePassword();
    }

    private void initViews() {
        generatedPasswordText = findViewById(R.id.generatedPasswordText);
        passwordStrengthLabel = findViewById(R.id.passwordStrengthLabel);
        strengthIndicatorText = findViewById(R.id.strengthIndicatorText);
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar);
        upperLowerCaseCheckbox = findViewById(R.id.upperLowerCaseCheckbox);
        numbersCheckbox = findViewById(R.id.numbersCheckbox);
        specialCharsCheckbox = findViewById(R.id.specialCharsCheckbox);
        lengthLabel = findViewById(R.id.lengthLabel);
        lengthSeekBar = findViewById(R.id.lengthSeekBar);
        generateButton = findViewById(R.id.generateButton);
        saveButton = findViewById(R.id.saveButton);
        copyPasswordButton = findViewById(R.id.copyPasswordButton);
        btnBack = findViewById(R.id.btnBack);

        // Configurar valor inicial de la etiqueta de longitud
        updateLengthLabel(lengthSeekBar.getProgress());
    }

    private void setupListeners() {
        // Botón de retroceso
        btnBack.setOnClickListener(v -> finish());

        // Botón generar contraseña
        generateButton.setOnClickListener(v -> generatePassword());

        // Botón copiar contraseña
        copyPasswordButton.setOnClickListener(v -> copyPasswordToClipboard());

        // Botón guardar contraseña
        saveButton.setOnClickListener(v -> savePassword());

        // Cambio en la barra de longitud
        lengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateLengthLabel(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateLengthLabel(int length) {
        lengthLabel.setText(getString(R.string.password_length_format, length));
    }

    private void generatePassword() {
        boolean upperAndLowerCase = upperLowerCaseCheckbox.isChecked();
        boolean numbers = numbersCheckbox.isChecked();
        boolean specialChars = specialCharsCheckbox.isChecked();
        int length = lengthSeekBar.getProgress();

        String password = generatePasswordString(upperAndLowerCase, numbers, specialChars, length);
        generatedPasswordText.setText(password);

        // Actualizar indicador de fortaleza usando PasswordEvaluator
        int strength = PasswordEvaluator.calculateStrength(password);
        PasswordEvaluator.updatePasswordStrengthInfo(strength, passwordStrengthLabel, passwordStrengthBar);

        // Actualizar el texto del indicador adicional de fortaleza
        updateStrengthIndicatorText(strength);
    }

    // Método para actualizar el texto del indicador de fortaleza
    private void updateStrengthIndicatorText(int strength) {
        String strengthText;
        int colorRes;

        switch (strength) {
            case 0:
                strengthText = getString(R.string.very_weak);
                colorRes = android.R.color.holo_red_dark;
                break;
            case 1:
                strengthText = getString(R.string.weak);
                colorRes = R.color.strengthWeak; // Naranja
                break;
            case 2:
                strengthText = getString(R.string.medium);
                colorRes = R.color.strengthModerate; // Amarillo
                break;
            case 3:
                strengthText = getString(R.string.strong);
                colorRes = R.color.strengthGood; // Verde claro
                break;
            case 4:
                strengthText = getString(R.string.very_strong);
                colorRes = R.color.strengthStrong; // Verde
                break;
            default:
                strengthText = getString(R.string.weak);
                colorRes = R.color.strengthWeak;
                break;
        }

        strengthIndicatorText.setText(getString(R.string.password_strength) + ": " + strengthText);
        int color = ContextCompat.getColor(this, colorRes);
        passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(color));
    }



    private float calculatePasswordStrength(String password) {
        float strength = 0.0f;
        
        if (password.length() >= 8) strength += 0.25f;
        if (password.matches(".*[A-Z].*") && password.matches(".*[a-z].*")) strength += 0.25f;
        if (password.matches(".*\\d.*")) strength += 0.25f;
        if (password.matches(".*[^a-zA-Z0-9].*")) strength += 0.25f;
        
        return Math.min(1.0f, strength);
    }

    private void copyPasswordToClipboard() {
        String password = generatedPasswordText.getText().toString();
        
        if (!password.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Contraseña", password);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(this, R.string.password_copied, Toast.LENGTH_SHORT).show();
        }
    }

    private void savePassword() {
        String password = generatedPasswordText.getText().toString();

        if (!password.isEmpty()) {
            // Crear y configurar el diálogo de guardar contraseña
            SavePasswordDialogFragment dialogFragment = SavePasswordDialogFragment.newInstance(password);

            // Configurar el listener para manejar cuando se guarda la contraseña
            dialogFragment.setOnPasswordSaveListener(passwordDTO -> {
                // Analizar seguridad de la contraseña antes de guardarla
                SecurityFilterUtils.analyzePasswordSecurity(passwordDTO);

                // Llamar al repository usando el patrón de callback
                passwordRepository.savePassword(passwordDTO, new PasswordRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(GeneratePasswordActivity.this, R.string.password_saved, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(GeneratePasswordActivity.this, R.string.error_saving_password, Toast.LENGTH_SHORT).show();
                            Log.e("GeneratePassword", "Error al guardar contraseña", e);
                        });
                    }
                });
            });

            dialogFragment.show(getSupportFragmentManager(), "SavePasswordDialog");
        }
    }

    // Método para generar la contraseña
    private String generatePasswordString(boolean upperAndLowerCase, boolean numbers, boolean specialChars, int length) {
        String characterPool = LOWERCASE;  // Contiene minúsculas por defecto
        List<Character> passwordChars = new ArrayList<>();

        SecureRandom random = new SecureRandom();

        // Agrega al menos un carácter de cada tipo requerido
        if (upperAndLowerCase) {
            passwordChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
            characterPool += UPPERCASE;
            passwordChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (numbers) {
            characterPool += NUMBERS;
            passwordChars.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (specialChars) {
            characterPool += SPECIAL_CHARACTERS;
            passwordChars.add(SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length())));
        }

        // Completar la contraseña hasta la longitud especificada
        for (int i = passwordChars.size(); i < length; i++) {
            passwordChars.add(characterPool.charAt(random.nextInt(characterPool.length())));
        }

        // Mezcla aleatoria de caracteres para evitar patrones predecibles
        Collections.shuffle(passwordChars);

        // Construir la contraseña final
        StringBuilder password = new StringBuilder();
        for (Character c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}