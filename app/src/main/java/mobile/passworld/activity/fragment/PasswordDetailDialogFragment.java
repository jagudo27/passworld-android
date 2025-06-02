package mobile.passworld.activity.fragment;

import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import mobile.passworld.R;
import mobile.passworld.data.PasswordDTO;
import mobile.passworld.data.PasswordRepository;
import mobile.passworld.utils.LogUtils;
import mobile.passworld.utils.SecurityFilterUtils;

public class PasswordDetailDialogFragment extends DialogFragment {

    private PasswordDTO password;
    private TextInputEditText descriptionField, usernameField, urlField, passwordField;
    private MaterialButton saveButton, deleteButton;
    private boolean hasChanges = false;

    public interface OnPasswordActionListener {
        void onPasswordUpdated();
        void onPasswordDeleted();
    }

    private OnPasswordActionListener listener;

    public void setListener(OnPasswordActionListener listener) {
        this.listener = listener;
    }

    public static PasswordDetailDialogFragment newInstance(PasswordDTO password) {
        PasswordDetailDialogFragment fragment = new PasswordDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("password", password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            password = (PasswordDTO) getArguments().getSerializable("password");
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            int displayWidth = getResources().getDisplayMetrics().widthPixels;
            int dialogWidth = (int) (displayWidth * 0.85);
            dialog.getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_password_detail, container, false);

        descriptionField = view.findViewById(R.id.dialogDescription);
        usernameField = view.findViewById(R.id.dialogUsername);
        urlField = view.findViewById(R.id.dialogUrl);
        passwordField = view.findViewById(R.id.dialogPassword);

        saveButton = view.findViewById(R.id.savePasswordButton2);
        deleteButton = view.findViewById(R.id.deletePasswordButton);
        MaterialButton closeButton = view.findViewById(R.id.dialogCloseButton);

        TextView securityText = view.findViewById(R.id.securityIssuesText);
        ImageView securityIcon = view.findViewById(R.id.securityStatusIcon);

        setupSecurityStatus(securityText, securityIcon);

        if (password != null) {
            descriptionField.setText(password.getDescription());
            usernameField.setText(password.getUsername());
            urlField.setText(password.getUrl());
            passwordField.setText(password.getPassword());
        }

        hasChanges = false;
        saveButton.setVisibility(View.GONE);

        TextWatcher textWatcher = createTextWatcher();
        descriptionField.addTextChangedListener(textWatcher);
        usernameField.addTextChangedListener(textWatcher);
        urlField.addTextChangedListener(textWatcher);
        passwordField.addTextChangedListener(textWatcher);

        saveButton.setOnClickListener(v -> savePassword());
        deleteButton.setOnClickListener(v -> deletePassword());
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkForChanges();
            }
        };
    }

    private void checkForChanges() {
        boolean newHasChanges = false;

        if (password != null) {
            String newDesc = descriptionField.getText() != null ? descriptionField.getText().toString() : "";
            String newUser = usernameField.getText() != null ? usernameField.getText().toString() : "";
            String newUrl = urlField.getText() != null ? urlField.getText().toString() : "";
            String newPass = passwordField.getText() != null ? passwordField.getText().toString() : "";

            String origDesc = password.getDescription() != null ? password.getDescription() : "";
            String origUser = password.getUsername() != null ? password.getUsername() : "";
            String origUrl = password.getUrl() != null ? password.getUrl() : "";
            String origPass = password.getPassword() != null ? password.getPassword() : "";

            newHasChanges = !newDesc.equals(origDesc) ||
                    !newUser.equals(origUser) ||
                    !newUrl.equals(origUrl) ||
                    !newPass.equals(origPass);
        }

        hasChanges = newHasChanges;
        saveButton.setVisibility(hasChanges ? View.VISIBLE : View.GONE);
    }

    private void setupSecurityStatus(TextView securityText, ImageView securityIcon) {
        if (password == null) return;

        StringBuilder issues = new StringBuilder();
        boolean hasIssues = false;

        if (password.isWeak()) {
            issues.append("• ").append(getString(R.string.weak_password)).append("\n");
            hasIssues = true;
        }

        if (password.isDuplicate()) {
            issues.append("• ").append(getString(R.string.duplicate_password)).append("\n");
            hasIssues = true;
        }

        if (password.isCompromised()) {
            issues.append("• ").append(getString(R.string.compromised_password)).append("\n");
            hasIssues = true;
        }

        if (password.isUrlUnsafe()) {
            issues.append("• ").append(getString(R.string.unsafe_url)).append("\n");
            hasIssues = true;
        }

        // Detectar si estamos en modo oscuro
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        if (hasIssues) {
            securityText.setText(getString(R.string.security_issues_detected, issues.toString().trim()));
            // Seleccionar icono de advertencia según el modo
            securityIcon.setImageResource(isDarkMode ?
                    R.drawable.warning_icon : R.drawable.warning_icon);
        } else {
            securityText.setText(getString(R.string.no_security_issues));
            // Seleccionar icono de protección según el modo
            securityIcon.setImageResource(isDarkMode ?
                    R.drawable.protect_icon : R.drawable.protect_icon);
        }
    }

    private void savePassword() {
        try {
            if (password == null) return;

            String newDescription = descriptionField.getText().toString();
            String newUsername = usernameField.getText().toString();
            String newUrl = urlField.getText().toString();
            String newPassword = passwordField.getText().toString();

            password.setDescription(newDescription);
            password.setUsername(newUsername);
            password.setUrl(newUrl);
            password.setPassword(newPassword);
            password.setLastModifiedToNow();

            try {
                SecurityFilterUtils.analyzePasswordSecurity(password);
            } catch (Exception e) {
                LogUtils.LOGGER.warning("Error analizando seguridad: " + e.getMessage());
            }

            PasswordRepository repository = new PasswordRepository();
            repository.updatePassword(password, new PasswordRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.password_updated), Toast.LENGTH_SHORT).show();
                    }
                    if (listener != null) {
                        listener.onPasswordUpdated();
                    }
                    dismiss();
                }

                @Override
                public void onError(Exception e) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.error_updating, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.error_saving, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deletePassword() {
        if (password == null) return;

        PasswordRepository repository = new PasswordRepository();
        repository.deletePassword(password.getIdFb(), new PasswordRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (getContext() != null) {
                    Toast.makeText(getContext(), getString(R.string.password_deleted), Toast.LENGTH_SHORT).show();
                }
                if (listener != null) {
                    listener.onPasswordDeleted();
                }
                dismiss();
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), getString(R.string.error_deleting, e.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
