package mobile.passworld.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import mobile.passworld.R;
import mobile.passworld.activity.adapter.PasswordAdapter;
import mobile.passworld.activity.fragment.PasswordDetailDialogFragment;
import mobile.passworld.data.PasswordDTO;
import mobile.passworld.data.PasswordRepository;
import mobile.passworld.session.UserSession;
import mobile.passworld.utils.PopupManager;

public class PasswordListActivity extends AppCompatActivity {
    private RecyclerView passwordRecyclerView;
    private PasswordAdapter adapter;
    private TextView emptyView;
    private PasswordRepository repository;
    private EditText searchEditText;
    private ImageView clearSearchButton;
    private ImageView sortButton;
    private ImageView optionsButton;

    // Variables para controlar la lista
    private List<PasswordDTO> allPasswords = new ArrayList<>();
    private List<PasswordDTO> filteredPasswords = new ArrayList<>();

    private MaterialCardView generatePasswordButton;
    private static final int REQUEST_GENERATE_PASSWORD = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_list);

        // Referencias a vistas
        passwordRecyclerView = findViewById(R.id.passwordRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        searchEditText = findViewById(R.id.searchEditText);
        clearSearchButton = findViewById(R.id.clearSearchButton);


        sortButton = findViewById(R.id.sortButton);
        optionsButton = findViewById(R.id.menuButton);

        optionsButton.setOnClickListener(v -> {
            PopupManager.showOptionsMenu(this, optionsButton, UserSession.getInstance().isLoggedIn(), new PopupManager.MenuCallback() {
                @Override
                public void onLanguageChanged(Locale locale) {
                    // Código adicional específico de la actividad si es necesario
                }

                @Override
                public void onDarkModeChanged(boolean isDarkMode) {
                    // Código adicional específico de la actividad si es necesario
                }

                @Override
                public void onLogout() {
                    // Código adicional específico de la actividad si es necesario
                }
            });
        });
        // Añade este código en el método onCreate(), después de inicializar los demás elementos
        generatePasswordButton = findViewById(R.id.generatePasswordButton);
        generatePasswordButton.setOnClickListener(v -> {
            // Abre la actividad de generación de contraseñas
            Intent intent = new Intent(PasswordListActivity.this, GeneratePasswordActivity.class);
            startActivityForResult(intent, REQUEST_GENERATE_PASSWORD);
        });

        // Configurar RecyclerView
        adapter = new PasswordAdapter();
        passwordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        passwordRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(password -> showPasswordDialog(password));

        // Configurar búsqueda
        setupSearchFunctionality();

        // Configurar ordenamiento
        setupSortFunctionality();

        // Cargar contraseñas
        repository = new PasswordRepository();
        loadPasswords();
    }

    private void setupSearchFunctionality() {
        // Mostrar/ocultar botón de limpiar
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterPasswords(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Botón para limpiar búsqueda
        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            filterPasswords("");
        });
    }

    private void setupSortFunctionality() {
        sortButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, sortButton);
            popupMenu.getMenuInflater().inflate(R.menu.menu_sort_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.sort_name_asc) {
                    sortPasswordsByName(true);
                    return true;
                } else if (itemId == R.id.sort_name_desc) {
                    sortPasswordsByName(false);
                    return true;
                } else if (itemId == R.id.sort_date_newest) {
                    sortPasswordsByDate(true);
                    return true;
                } else if (itemId == R.id.sort_date_oldest) {
                    sortPasswordsByDate(false);
                    return true;
                }

                return false;
            });

            popupMenu.show();
        });
    }

    private void loadPasswords() {
        repository.getAllPasswords(new PasswordRepository.PasswordsCallback() {
            @Override
            public void onPasswordsLoaded(List<PasswordDTO> passwords) {
                allPasswords = new ArrayList<>(passwords);
                filteredPasswords = new ArrayList<>(passwords);

                if (passwords.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    passwordRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    passwordRecyclerView.setVisibility(View.VISIBLE);

                    // Ordenar y mostrar contraseñas
                    sortPasswordsByName(true); // Orden inicial A-Z
                }
            }

            @Override
            public void onError(com.google.firebase.database.DatabaseError error) {
                emptyView.setText("Error al cargar contraseñas");
                emptyView.setVisibility(View.VISIBLE);
                passwordRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void filterPasswords(String query) {
        filteredPasswords.clear();

        if (query.isEmpty()) {
            filteredPasswords.addAll(allPasswords);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (PasswordDTO password : allPasswords) {
                // Comprobamos que ningún campo sea null antes de llamar a toLowerCase()
                String description = password.getDescription();
                String username = password.getUsername();
                String url = password.getUrl();

                if ((description != null && description.toLowerCase().contains(lowerCaseQuery)) ||
                        (username != null && username.toLowerCase().contains(lowerCaseQuery)) ||
                        (url != null && url.toLowerCase().contains(lowerCaseQuery))) {
                    filteredPasswords.add(password);
                }
            }
        }

        // Aplicar el ordenamiento actual
        updateAdapterData();
    }

    private void sortPasswordsByName(boolean ascending) {
        Collections.sort(filteredPasswords, (p1, p2) -> {
            int result = p1.getDescription().compareToIgnoreCase(p2.getDescription());
            return ascending ? result : -result;
        });
        updateAdapterData();
    }

    private void sortPasswordsByDate(boolean newestFirst) {
        Collections.sort(filteredPasswords, (p1, p2) -> {
            // Obtener LocalDateTime de cada contraseña
            LocalDateTime date1 = p1.getLastModifiedAsDate(); // Usa el método existente con 'l' minúscula
            LocalDateTime date2 = p2.getLastModifiedAsDate();

            // Manejar valores nulos (contraseñas sin fecha)
            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return newestFirst ? 1 : -1;
            if (date2 == null) return newestFirst ? -1 : 1;

            // Comparar fechas (LocalDateTime tiene su propio compareTo)
            int comparison = date1.compareTo(date2);

            // Invertir si queremos las más recientes primero
            return newestFirst ? -comparison : comparison;
        });
        updateAdapterData();
    }

    private void sortPasswordsBySecurity(boolean mostVulnerableFirst) {
        Collections.sort(filteredPasswords, (p1, p2) -> {
            int p1Issues = getSecurityScore(p1);
            int p2Issues = getSecurityScore(p2);
            return mostVulnerableFirst ? p2Issues - p1Issues : p1Issues - p2Issues;
        });
        updateAdapterData();
    }

    private int getSecurityScore(PasswordDTO password) {
        int score = 0;
        if (password.isCompromised()) score += 4;
        if (password.isWeak()) score += 2;
        if (password.isDuplicate()) score += 1;
        if (password.isUrlUnsafe()) score += 1;
        return score;
    }

    private void updateAdapterData() {
        adapter.setPasswords(filteredPasswords);

        // Mostrar mensaje cuando no hay resultados
        if (filteredPasswords.isEmpty()) {
            emptyView.setText("No se encontraron contraseñas");
            emptyView.setVisibility(View.VISIBLE);
            passwordRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            passwordRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Mostrar diálogo al hacer clic en un elemento de la lista
    private void showPasswordDialog(PasswordDTO password) {
        PasswordDetailDialogFragment dialog = PasswordDetailDialogFragment.newInstance(password);
        dialog.setListener(new PasswordDetailDialogFragment.OnPasswordActionListener() {
            @Override
            public void onPasswordUpdated() {
                // La contraseña se actualizó, refrescar la lista si es necesario
                // (no debería ser necesario si usas el listener en tiempo real)
            }

            @Override
            public void onPasswordDeleted() {
                // La contraseña se eliminó, refrescar la lista si es necesario
                // (no debería ser necesario si usas el listener en tiempo real)
            }
        });
        dialog.show(getSupportFragmentManager(), "password_detail");
    }
}