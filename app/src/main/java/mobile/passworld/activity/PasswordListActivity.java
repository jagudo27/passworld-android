package mobile.passworld.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mobile.passworld.R;
import mobile.passworld.activity.adapter.PasswordAdapter;
import mobile.passworld.data.PasswordDTO;
import mobile.passworld.data.PasswordRepository;

public class PasswordListActivity extends AppCompatActivity {
    private RecyclerView passwordRecyclerView;
    private PasswordAdapter adapter;
    private TextView emptyView;
    private PasswordRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_list);

        passwordRecyclerView = findViewById(R.id.passwordRecyclerView);
        emptyView = findViewById(R.id.emptyView);

        adapter = new PasswordAdapter();
        passwordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        passwordRecyclerView.setAdapter(adapter);

        repository = new PasswordRepository();
        loadPasswords();
        adapter.setOnItemClickListener(password -> showPasswordDialog(password));
    }

    private void loadPasswords() {
        repository.getAllPasswords(new PasswordRepository.PasswordsCallback() {
            @Override
            public void onPasswordsLoaded(List<PasswordDTO> passwords) {
                if (passwords.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    passwordRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    passwordRecyclerView.setVisibility(View.VISIBLE);
                    adapter.setPasswords(passwords);
                }
            }

            @Override
            public void onError(com.google.firebase.database.DatabaseError error) {
                emptyView.setText("Error al cargar contraseñas");
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }
    private void showPasswordDialog(PasswordDTO password) {
        Dialog dialog = new Dialog(this); // Puedes personalizar este estilo
        dialog.setContentView(R.layout.dialog_password_detail);

        ((TextView) dialog.findViewById(R.id.dialogDescription)).setText(password.getDescription());
        ((TextView) dialog.findViewById(R.id.dialogUsername)).setText("Usuario: " + password.getUsername());
        ((TextView) dialog.findViewById(R.id.dialogUrl)).setText("URL: " + password.getUrl());
        ((TextView) dialog.findViewById(R.id.dialogPassword)).setText("Contraseña: " + password.getPassword());

        dialog.findViewById(R.id.dialogCloseButton).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}