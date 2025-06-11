package mobile.passworld.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import mobile.passworld.R;

public class PasswordEvaluator {

    // Lista de palabras comunes simplificada para reemplazar el Trie
    private static final Set<String> COMMON_WORDS = new HashSet<>(Arrays.asList(
            "password", "123456", "qwerty", "admin", "welcome", "login",
            "abc123", "letmein", "monkey", "passw0rd", "iloveyou", "trustno1",
            "dragon", "baseball", "football", "master", "sunshine", "ashley",
            "bailey", "shadow", "superman", "qazwsx", "michael", "football"
    ));

    // Calcular la fortaleza de la contraseña
    public static int calculateStrength(String password) {
        if (password == null || password.isEmpty()) return 0;

        int score = 0;

        // 1. Puntuación por longitud
        int length = password.length();
        if (length < 8) score -= 2;
        if (length >= 8) score += 2;
        if (length >= 12) score += 2;
        if (length >= 16) score += 2;

        // 2. Puntuación por diversidad de caracteres
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[^a-zA-Z0-9]").matcher(password).find();

        int diversity = 0;
        if (hasUpper) diversity++;
        if (hasLower) diversity++;
        if (hasDigit) diversity++;
        if (hasSpecial) diversity++;

        score += diversity * 2; // Incrementar peso de la diversidad

        // 3. Penalizaciones

        // 3.1 Contiene palabras comunes
        if (containsCommonWords(password)) score -= 5;

        // 3.2 Secuencias comunes
        if (hasSequentialChars(password)) score -= 2;

        // 3.3 Repetición fuerte de caracteres o patrones
        if (hasRepetition(password)) score -= 2;

        // 4. Normalizar puntuación final a rango 0–4
        if (score <= 2) return 0;            // Muy débil
        else if (score <= 5) return 1;       // Débil
        else if (score <= 7) return 2;       // Media
        else if (score <= 10) return 3;       // Fuerte
        else return 4;                       // Muy fuerte
    }

    // Verificar si la contraseña contiene palabras comunes
    public static boolean containsCommonWords(String password) {
        String lower = password.toLowerCase();
        for (String word : COMMON_WORDS) {
            if (lower.contains(word)) {
                // Penalizar si la palabra común representa más del 50% de la longitud de la contraseña
                if ((double) word.length() / password.length() > 0.5) {
                    return true;
                }
            }
        }
        return false;
    }

    // Detecta secuencias simples como "abcd", "1234"
    private static boolean hasSequentialChars(String password) {
        String lower = password.toLowerCase();
        String sequence = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";

        for (int i = 0; i < sequence.length() - 3; i++) {
            String sub = sequence.substring(i, i + 4);
            if (lower.contains(sub)) return true;
        }

        for (int i = 0; i < digits.length() - 3; i++) {
            String sub = digits.substring(i, i + 4);
            if (password.contains(sub)) return true;
        }

        return false;
    }

    // Detecta repeticiones de caracteres o patrones simples
    private static boolean hasRepetition(String password) {
        // Repetición exacta de substrings (como "abcabc", "1212")
        for (int len = 1; len <= password.length() / 2; len++) {
            String part = password.substring(0, len);
            StringBuilder repeated = new StringBuilder();
            while (repeated.length() < password.length()) {
                repeated.append(part);
            }
            if (repeated.toString().equals(password)) return true;
        }

        // Repetición de un solo carácter muchas veces
        return password.matches("(.)\\1{3,}");
    }

    // Método para actualizar la interfaz de usuario con la información de fortaleza
    @SuppressLint("ResourceAsColor")
    public static void updatePasswordStrengthInfo(int strength, TextView passwordStrengthLabel, ProgressBar passwordStrengthBar, Context context) {
        // Mapear la fortaleza a porcentajes
        int progressPercentage;
        int colorRes;

        switch (strength) {
            case 0: // Muy débil
                passwordStrengthLabel.setText(R.string.very_weak);
                progressPercentage = 10;
                colorRes = R.color.strengthVeryWeak; // Asegúrate de tener este color
                break;
            case 1: // Débil
                passwordStrengthLabel.setText(R.string.weak);
                progressPercentage = 25;
                colorRes = R.color.strengthWeak;
                break;
            case 2: // Media
                passwordStrengthLabel.setText(R.string.medium);
                progressPercentage = 50;
                colorRes = R.color.strengthModerate;
                break;
            case 3: // Fuerte
                passwordStrengthLabel.setText(R.string.strong);
                progressPercentage = 75;
                colorRes = R.color.strengthGood;
                break;
            case 4: // Muy fuerte
                passwordStrengthLabel.setText(R.string.very_strong);
                progressPercentage = 100;
                colorRes = R.color.strengthStrong;
                break;
            default:
                progressPercentage = 0;
                colorRes = R.color.strengthVeryWeak; // Por defecto a muy débil
        }

        // Establecer el progreso con el porcentaje calculado
        passwordStrengthBar.setProgress(progressPercentage);
        int color = ContextCompat.getColor(context, colorRes);
        passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(color));



        // Hacer visibles los elementos
        passwordStrengthBar.setVisibility(View.VISIBLE);
        passwordStrengthLabel.setVisibility(View.VISIBLE);
    }

    public static boolean isPasswordValid(Context context, String password, EditText passwordEditText) {
        if (password.length() < 8) {
            passwordEditText.setError(context.getString(R.string.password_too_short));
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            passwordEditText.setError(context.getString(R.string.password_needs_uppercase));
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            passwordEditText.setError(context.getString(R.string.password_needs_special));
            return false;
        }

        return true;
    }
}