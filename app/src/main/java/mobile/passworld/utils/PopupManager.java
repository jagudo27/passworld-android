package mobile.passworld.utils;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;

import java.sql.SQLException;
import java.util.Locale;

import mobile.passworld.R;
import mobile.passworld.activity.SignInActivity;
import mobile.passworld.data.session.UserSession;

public class PopupManager {

    public interface MenuCallback {
        void onLanguageChanged(Locale locale);
        void onDarkModeChanged(boolean isDarkMode);
        void onLogout();
    }

    public static void showOptionsMenu(Activity activity, View anchor, boolean isUserLoggedIn, MenuCallback callback) {
        // Usar un contexto envuelto con el tema de la app para garantizar que se aplique el estilo
        Context contextWrapper = new ContextThemeWrapper(activity, R.style.PopupMenu_PassworldAndroid);
        PopupMenu popupMenu = new PopupMenu(contextWrapper, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

        // Mostrar opción de cerrar sesión solo si el usuario está autenticado
        popupMenu.getMenu().findItem(R.id.menu_logout).setVisible(isUserLoggedIn);

        // Cambiar el texto del menú de modo oscuro/claro según el tema actual
        int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        // Cambiar el título del menú según el modo actual
        if (isDarkMode) {
            popupMenu.getMenu().findItem(R.id.menu_dark_mode).setTitle(R.string.light_mode);
        } else {
            popupMenu.getMenu().findItem(R.id.menu_dark_mode).setTitle(R.string.dark_mode);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_change_language) {
                popupMenu.dismiss(); // Cerrar el menú explícitamente
                showLanguageDialog(activity, callback);
                return true;
            } else if (itemId == R.id.menu_dark_mode) {
                popupMenu.dismiss(); // Cerrar el menú explícitamente
                toggleDarkMode(activity, callback);
                return true;
            } else if (itemId == R.id.menu_logout) {
                popupMenu.dismiss(); // Cerrar el menú explícitamente
                logout(activity, callback);
                return true;
            } else if (itemId == R.id.menu_goto_help) {
                popupMenu.dismiss();
                openHelpPage(activity, callback);
                return true; // Añadir return true para consistencia
            }

            return false;
        });

        popupMenu.show();
    }

    public static void showSortMenu(Activity activity, View anchor, SortMenuCallback callback) {
        // Usar un contexto envuelto con el tema de la app para garantizar que se aplique el estilo
        Context contextWrapper = new ContextThemeWrapper(activity, R.style.Theme_PassworldAndroid);
        PopupMenu popupMenu = new PopupMenu(contextWrapper, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_sort_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.sort_name_asc) {
                callback.onSortSelected(SortType.NAME_ASC);
                return true;
            } else if (itemId == R.id.sort_name_desc) {
                callback.onSortSelected(SortType.NAME_DESC);
                return true;
            } else if (itemId == R.id.sort_date_newest) {
                callback.onSortSelected(SortType.DATE_NEWEST);
                return true;
            } else if (itemId == R.id.sort_date_oldest) {
                callback.onSortSelected(SortType.DATE_OLDEST);
                return true;
            }

            return false;
        });
        
        popupMenu.show();
    }
    
    public enum SortType {
        NAME_ASC, NAME_DESC, DATE_NEWEST, DATE_OLDEST
    }
    
    public interface SortMenuCallback {
        void onSortSelected(SortType sortType);
    }

    private static void showLanguageDialog(Activity activity, MenuCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.select_language));
        String[] languages = {"Español", "English", "Deutsch"};

        builder.setItems(languages, (dialog, which) -> {
            String languageCode;
            Locale locale;
            switch (which) {
                case 0:
                    languageCode = "es";
                    locale = new Locale(languageCode);
                    break;
                case 1:
                    languageCode = "en";
                    locale = new Locale(languageCode);
                    break;
                case 2:
                    languageCode = "de";
                    locale = new Locale(languageCode);
                    break;
                default:
                    languageCode = Locale.getDefault().getLanguage();
                    locale = Locale.getDefault();
            }

            // Guardar preferencia de idioma
            UserPreferences.saveLanguagePreference(activity, languageCode);
            updateLocale(activity, locale);

            if (callback != null) {
                callback.onLanguageChanged(locale);
            }
        });

        builder.show();
    }

    private static void updateLocale(Activity activity, Locale locale) {
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        activity.recreate();
    }

    private static void toggleDarkMode(Activity activity, MenuCallback callback) {
        int currentNightMode = activity.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        
        boolean isDarkMode = currentNightMode != Configuration.UI_MODE_NIGHT_YES;
        
        AppCompatDelegate.setDefaultNightMode(isDarkMode 
                ? AppCompatDelegate.MODE_NIGHT_YES 
                : AppCompatDelegate.MODE_NIGHT_NO);

        // Guardar preferencia de modo oscuro
        UserPreferences.saveDarkModePreference(activity, isDarkMode);

        if (callback != null) {
            callback.onDarkModeChanged(isDarkMode);
        }
        
        activity.recreate();
    }

    private static void logout(Activity activity, MenuCallback callback) {
        FirebaseAuth.getInstance().signOut();
        try {
            UserSession.getInstance().clearSession();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (callback != null) {
            callback.onLogout();
        }
        
        Intent intent = new Intent(activity, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    public static void openHelpPage(Context context, MenuCallback callback) {
        String url = "https://g4vr3.github.io/passworld-web/user-guide.html";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No se pudo abrir la página de ayuda", Toast.LENGTH_SHORT).show();
        }
        if (callback != null) {
            callback.onLogout(); // Llamar al callback si es necesario
        }
    }
}

