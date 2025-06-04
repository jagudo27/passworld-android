package mobile.passworld.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mobile.passworld.R;
import mobile.passworld.activity.adapter.PasswordAdapter;
import mobile.passworld.activity.fragment.PasswordDetailDialogFragment;
import mobile.passworld.activity.fragment.SavePasswordDialogFragment;
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

    // Variables para estadísticas
    private TextView allPasswordsCount;
    private TextView compromisedPasswordsCount;
    private MaterialCardView allPasswordsCard;
    private MaterialCardView compromisedPasswordsCard;

    // Enumeración para el filtro actual
    private enum PasswordFilter {
        ALL,
        COMPROMISED
    }

    // Variables para controlar la lista
    private List<PasswordDTO> allPasswords = new ArrayList<>();
    private List<PasswordDTO> filteredPasswords = new ArrayList<>();
    private PasswordFilter currentFilter = PasswordFilter.ALL;

    private FloatingActionButton generatePasswordButton;
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

        // Referencias a los componentes de estadísticas
        allPasswordsCount = findViewById(R.id.allPasswordsCount);
        compromisedPasswordsCount = findViewById(R.id.compromisedPasswordsCount);
        allPasswordsCard = findViewById(R.id.allPasswordsCard);
        compromisedPasswordsCard = findViewById(R.id.compromisedPasswordsCard);

        // Configurar listeners para las tarjetas de filtro
        setupFilterCards();

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

                // Actualizar estadísticas
                updatePasswordStats();
            }

            @Override
            public void onError(DatabaseError error) {
                emptyView.setText("Error al cargar contraseñas");
                emptyView.setVisibility(View.VISIBLE);
                passwordRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void filterPasswords(String query) {
        // Crear una nueva lista para los resultados filtrados
        List<PasswordDTO> newFilteredList = new ArrayList<>();

        if (query.isEmpty()) {
            newFilteredList.addAll(allPasswords);
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
                    newFilteredList.add(password);
                }
            }
        }

        // Actualizar la lista filtrada y aplicar el ordenamiento actual
        filteredPasswords = newFilteredList;
        updateAdapterData();
    }

    private void sortPasswordsByName(boolean ascending) {
        // Crear una nueva lista para ordenar (importante para ListAdapter)
        List<PasswordDTO> sortedList = new ArrayList<>(filteredPasswords);

        sortedList.sort((p1, p2) -> {
            // Manejar casos en los que description pueda ser null
            String desc1 = p1.getDescription();
            String desc2 = p2.getDescription();

            // Si ambos son null, son "iguales"
            if (desc1 == null && desc2 == null) return 0;
            // Si solo desc1 es null, debería ir después (o antes si orden descendente)
            if (desc1 == null) return ascending ? 1 : -1;
            // Si solo desc2 es null, debería ir antes (o después si orden descendente)
            if (desc2 == null) return ascending ? -1 : 1;

            // Si ninguno es null, comparar normalmente
            int result = desc1.compareToIgnoreCase(desc2);
            return ascending ? result : -result;
        });

        // Actualizar la lista filtrada y notificar al adaptador
        filteredPasswords = sortedList;
        updateAdapterData();
    }

    private void sortPasswordsByDate(boolean newestFirst) {
        // Crear una nueva lista para ordenar
        List<PasswordDTO> sortedList = new ArrayList<>(filteredPasswords);

        sortedList.sort((p1, p2) -> {
            // Obtener LocalDateTime de cada contraseña
            LocalDateTime date1 = p1.getLastModifiedAsDate();
            Log.d("PasswordListActivity", "Comparando: " + p1.getDescription() + " - Fecha: " + date1);
            LocalDateTime date2 = p2.getLastModifiedAsDate();

            // Manejar valores nulos (contraseñas sin fecha)
            if (date1 == null && date2 == null) return 0;
            if (date1 == null) return newestFirst ? 1 : -1;
            if (date2 == null) return newestFirst ? -1 : 1;

            // Comparar fechas
            int comparison = date1.compareTo(date2);

            // Invertir si queremos las más recientes primero
            return newestFirst ? -comparison : comparison;
        });

        // Actualizar la lista filtrada y notificar al adaptador
        filteredPasswords = sortedList;
        updateAdapterData();
    }

    private void sortPasswordsBySecurity(boolean mostVulnerableFirst) {
        // Crear una nueva lista para ordenar
        List<PasswordDTO> sortedList = new ArrayList<>(filteredPasswords);

        sortedList.sort((p1, p2) -> {
            int p1Issues = getSecurityScore(p1);
            int p2Issues = getSecurityScore(p2);
            return mostVulnerableFirst ? p2Issues - p1Issues : p1Issues - p2Issues;
        });

        // Actualizar la lista filtrada y notificar al adaptador
        filteredPasswords = sortedList;
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
        // Crear una nueva instancia de la lista para que ListAdapter detecte el cambio
        List<PasswordDTO> listToSubmit = new ArrayList<>(filteredPasswords);
        adapter.submitList(listToSubmit);

        // Mostrar mensaje cuando no hay resultados
        if (filteredPasswords.isEmpty()) {
            emptyView.setText(R.string.no_passwords_found);
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
                // La contraseña se actualizó, refrescar la lista
                loadPasswords();
            }

            @Override
            public void onPasswordDeleted() {
                // La contraseña se eliminó, refrescar la lista
                loadPasswords();
            }
        });
        dialog.show(getSupportFragmentManager(), "password_detail");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GENERATE_PASSWORD && resultCode == RESULT_OK && data != null) {
            String generatedPassword = data.getStringExtra("generatedPassword");
            if (generatedPassword != null && !generatedPassword.isEmpty()) {
                // Mostrar el diálogo para guardar la contraseña
                showSavePasswordDialog(generatedPassword);
            }
        }
    }

    private void showSavePasswordDialog(String password) {
        SavePasswordDialogFragment dialog = SavePasswordDialogFragment.newInstance(password);
        dialog.setOnPasswordSaveListener(passwordDTO -> {
            // Guardar la nueva contraseña
            repository.savePassword(passwordDTO, new PasswordRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    // Recargar lista de contraseñas
                    loadPasswords();
                }

                @Override
                public void onError(Exception e) {
                    // Manejar error
                    // Aquí podrías mostrar un mensaje al usuario
                }
            });
        });
        dialog.show(getSupportFragmentManager(), "save_password_dialog");
    }

    /**
     * Configura los listeners para las tarjetas de filtro de contraseñas
     */
    private void setupFilterCards() {
        // Configurar tarjeta de todas las contraseñas
        allPasswordsCard.setOnClickListener(v -> {
            currentFilter = PasswordFilter.ALL;
            applyCurrentFilter();
            updateCardSelection();
        });

        // Configurar tarjeta de contraseñas comprometidas
        compromisedPasswordsCard.setOnClickListener(v -> {
            currentFilter = PasswordFilter.COMPROMISED;
            applyCurrentFilter();
            updateCardSelection();
        });
    }

    /**
     * Actualiza la selección visual de las tarjetas según el filtro actual
     */
    private void updateCardSelection() {
        // Resetear todas las tarjetas
        allPasswordsCard.setStrokeWidth(0);
        compromisedPasswordsCard.setStrokeWidth(0);

        // Destacar la tarjeta seleccionada
        switch (currentFilter) {
            case ALL:
                allPasswordsCard.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.card_selected_stroke_width));
                break;
            case COMPROMISED:
                compromisedPasswordsCard.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.card_selected_stroke_width));
                break;
        }
    }

    /**
     * Aplica el filtro actual a la lista de contraseñas
     */
    private void applyCurrentFilter() {
        // Si no hay contraseñas, no hay nada que filtrar
        if (allPasswords.isEmpty()) {
            return;
        }

        // Texto de búsqueda actual
        String searchQuery = searchEditText.getText().toString().trim();

        // Aplicar filtro según la categoría seleccionada
        switch (currentFilter) {
            case ALL:
                // Sin filtro específico, solo aplicar búsqueda si existe
                if (!searchQuery.isEmpty()) {
                    filterPasswords(searchQuery);
                } else {
                    filteredPasswords = new ArrayList<>(allPasswords);
                    updateAdapterData();
                }
                break;
            case COMPROMISED:
                // Filtrar solo contraseñas comprometidas
                List<PasswordDTO> compromisedList = new ArrayList<>();
                for (PasswordDTO password : allPasswords) {
                    if (password.isCompromised() || password.isWeak() || password.isDuplicate() || password.isUrlUnsafe()) {
                        // Si hay búsqueda, también filtrar por texto
                        if (searchQuery.isEmpty() || matchesSearchQuery(password, searchQuery)) {
                            compromisedList.add(password);
                        }
                    }
                }
                filteredPasswords = compromisedList;
                updateAdapterData();
                break;
        }
    }

    /**
     * Verifica si una contraseña coincide con la consulta de búsqueda
     */
    private boolean matchesSearchQuery(PasswordDTO password, String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        String description = password.getDescription();
        String username = password.getUsername();
        String url = password.getUrl();

        return (description != null && description.toLowerCase().contains(lowerCaseQuery)) ||
                (username != null && username.toLowerCase().contains(lowerCaseQuery)) ||
                (url != null && url.toLowerCase().contains(lowerCaseQuery));
    }

    /**
     * Actualiza las estadísticas mostradas en las tarjetas
     */
    private void updatePasswordStats() {
        int totalPasswords = allPasswords.size();
        int compromisedCount = 0;

        // Contar contraseñas comprometidas
        for (PasswordDTO password : allPasswords) {
            if (password.isCompromised() || password.isWeak() || password.isDuplicate() || password.isUrlUnsafe()) {
                compromisedCount++;
            }
        }

        // Actualizar contadores en UI
        allPasswordsCount.setText(String.valueOf(totalPasswords));
        compromisedPasswordsCount.setText(String.valueOf(compromisedCount));
    }
}
