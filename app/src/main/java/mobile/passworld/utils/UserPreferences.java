package mobile.passworld.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

/**
 * Clase para gestionar las preferencias del usuario (modo oscuro e idioma)
 */
public class UserPreferences {
    private static final String PREFS_NAME = "passworld_user_preferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_LANGUAGE = "language";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Guarda la preferencia de modo oscuro del usuario
     * @param context Contexto de la aplicación
     * @param isDarkMode true si el modo oscuro está activado, false en caso contrario
     */
    public static void saveDarkModePreference(Context context, boolean isDarkMode) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.apply();
        Log.d("UserPreferences", "Modo oscuro guardado: " + isDarkMode);
    }

    /**
     * Obtiene la preferencia de modo oscuro del usuario
     * @param context Contexto de la aplicación
     * @return true si el modo oscuro está activado, false en caso contrario
     */
    public static boolean getDarkModePreference(Context context) {
        // Por defecto, seguir la configuración del sistema
        boolean systemDarkMode = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        return getPrefs(context).getBoolean(KEY_DARK_MODE, systemDarkMode);
    }

    /**
     * Guarda el idioma preferido por el usuario
     * @param context Contexto de la aplicación
     * @param languageCode Código del idioma (ej: "es", "en")
     */
    public static void saveLanguagePreference(Context context, String languageCode) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();
        Log.d("UserPreferences", "Idioma guardado: " + languageCode);
    }

    /**
     * Obtiene el idioma preferido por el usuario
     * @param context Contexto de la aplicación
     * @return Código del idioma (ej: "es", "en")
     */
    public static String getLanguagePreference(Context context) {
        // Por defecto, usar el idioma del sistema
        String systemLanguage = Locale.getDefault().getLanguage();
        return getPrefs(context).getString(KEY_LANGUAGE, systemLanguage);
    }

    /**
     * Aplica el idioma guardado en las preferencias
     * @param context Contexto de la aplicación
     */
    public static void applyLanguage(Context context) {
        String languageCode = getLanguagePreference(context);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        resources.updateConfiguration(config, resources.getDisplayMetrics());
        Log.d("UserPreferences", "Idioma aplicado: " + languageCode);
    }
}
