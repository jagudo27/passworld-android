package mobile.passworld.activity.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import mobile.passworld.R;
import mobile.passworld.data.PasswordDTO;
import mobile.passworld.utils.PasswordEvaluator;

public class SavePasswordDialogFragment extends DialogFragment {

    private TextInputEditText dialogDescription;
    private TextInputEditText dialogUsername;
    private TextInputEditText dialogUrl;
    private TextInputEditText dialogPassword;
    private TextView passwordStrengthText;
    private ProgressBar passwordStrengthBar;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;

    private String generatedPassword;
    private OnPasswordSaveListener listener;

    public interface OnPasswordSaveListener {
        void onPasswordSave(PasswordDTO password);
    }

    public static SavePasswordDialogFragment newInstance(String generatedPassword) {
        SavePasswordDialogFragment fragment = new SavePasswordDialogFragment();
        Bundle args = new Bundle();
        args.putString("password", generatedPassword);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            generatedPassword = getArguments().getString("password");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_save_password, container, false);

        // Inicializar vistas
        dialogDescription = view.findViewById(R.id.dialogDescription);
        dialogUsername = view.findViewById(R.id.dialogUsername);
        dialogUrl = view.findViewById(R.id.dialogUrl);
        dialogPassword = view.findViewById(R.id.dialogPassword);
        passwordStrengthBar = view.findViewById(R.id.passwordStrengthBar);

        saveButton = view.findViewById(R.id.savePasswordButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        // Establecer la contraseña generada
        dialogPassword.setText(generatedPassword);

        // Configurar los botones
        saveButton.setOnClickListener(v -> savePassword());
        cancelButton.setOnClickListener(v -> dismiss());

        // Mostrar la fortaleza inicial
        updatePasswordStrength(generatedPassword);

        // Monitorear cambios en la contraseña para actualizar la evaluación
        dialogPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Solo actualizamos la visualización para mejor UX
                updatePasswordStrength(s.toString());
            }
        });

        return view;
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrengthText != null && passwordStrengthBar != null) {
            // Solo mostrar la evaluación visual, sin guardar el resultado
            int strength = PasswordEvaluator.calculateStrength(password);
            PasswordEvaluator.updatePasswordStrengthInfo(strength, passwordStrengthText, passwordStrengthBar);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void savePassword() {
        String description = dialogDescription.getText().toString().trim();
        String username = dialogUsername.getText().toString().trim();
        String url = dialogUrl.getText().toString().trim();
        String password = dialogPassword.getText().toString();

        // Validar la descripción (obligatoria)
        if (description.isEmpty()) {
            return;
        }

        // Solo crear el DTO básico sin analizar
        PasswordDTO passwordDTO = new PasswordDTO(description, username, url, password);
        passwordDTO.setLastModifiedToNow();

        // El análisis se hará en la actividad
        if (listener != null) {
            listener.onPasswordSave(passwordDTO);
        }

        dismiss();
    }

    public void setOnPasswordSaveListener(OnPasswordSaveListener listener) {
        this.listener = listener;
    }
}