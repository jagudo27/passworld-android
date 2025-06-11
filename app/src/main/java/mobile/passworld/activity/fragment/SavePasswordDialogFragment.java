package mobile.passworld.activity.fragment;

import android.app.Dialog;
import android.content.res.ColorStateList;
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

import androidx.core.content.ContextCompat;
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
        passwordStrengthText = view.findViewById(R.id.strengthIndicatorText);
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
            String safePassword = password != null ? password : "";
            int strength = PasswordEvaluator.calculateStrength(safePassword);
            PasswordEvaluator.updatePasswordStrengthInfo(
                    strength,
                    passwordStrengthText,
                    passwordStrengthBar,
                    requireContext() // Asegúrate de pasar el contexto
            );
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            float density = getResources().getDisplayMetrics().density;
            int dpWidth = (int) (screenWidth / density); // Convertimos a dp

            int width;
            if (dpWidth >= 600) {
                // Tablet: usar 60% del ancho de pantalla
                width = (int) (screenWidth * 0.6);
            } else {
                // Móvil: usar 90% del ancho de pantalla
                width = (int) (screenWidth * 0.9);
            }

            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void savePassword() {
        String description = dialogDescription.getText().toString().trim();
        String username = dialogUsername.getText().toString().trim();
        String url = dialogUrl.getText().toString().trim();
        String password = dialogPassword.getText().toString();

        // Validar descripción
        if (description.isEmpty()) {
            dialogDescription.setError(getString(R.string.required_field));
            dialogDescription.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            dialogPassword.setError(getString(R.string.required_field));
            dialogPassword.requestFocus();
            return;
        }

        PasswordDTO passwordDTO = new PasswordDTO(description, username, url, password);
        passwordDTO.setLastModifiedToNow();

        if (listener != null) {
            listener.onPasswordSave(passwordDTO);
        }

        dismiss();
    }

    public void setOnPasswordSaveListener(OnPasswordSaveListener listener) {
        this.listener = listener;
    }
}