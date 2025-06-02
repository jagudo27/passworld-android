package mobile.passworld.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

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
    public static void updatePasswordStrengthInfo(int strength, TextView passwordStrengthLabel, ProgressBar passwordStrengthProgressBar) {
        passwordStrengthProgressBar.setProgress(strength + 1);

        switch (strength) {
            case 0:
                passwordStrengthLabel.setText("Muy débil");
                passwordStrengthLabel.setTextColor(Color.RED);
                passwordStrengthProgressBar.setProgressDrawable(passwordStrengthProgressBar.getContext().getResources().getDrawable(android.R.drawable.progress_horizontal, null));
                break;
            case 1:
                passwordStrengthLabel.setText("Débil");
                passwordStrengthLabel.setTextColor(Color.rgb(255, 165, 0)); // Naranja
                passwordStrengthProgressBar.setProgressDrawable(passwordStrengthProgressBar.getContext().getResources().getDrawable(android.R.drawable.progress_horizontal, null));
                break;
            case 2:
                passwordStrengthLabel.setText("Media");
                passwordStrengthLabel.setTextColor(Color.YELLOW);
                passwordStrengthProgressBar.setProgressDrawable(passwordStrengthProgressBar.getContext().getResources().getDrawable(android.R.drawable.progress_horizontal, null));
                break;
            case 3:
                passwordStrengthLabel.setText("Fuerte");
                passwordStrengthLabel.setTextColor(Color.rgb(154, 205, 50)); // YellowGreen
                passwordStrengthProgressBar.setProgressDrawable(passwordStrengthProgressBar.getContext().getResources().getDrawable(android.R.drawable.progress_horizontal, null));
                break;
            case 4:
                passwordStrengthLabel.setText("Muy fuerte");
                passwordStrengthLabel.setTextColor(Color.GREEN);
                passwordStrengthProgressBar.setProgressDrawable(passwordStrengthProgressBar.getContext().getResources().getDrawable(android.R.drawable.progress_horizontal, null));
                break;
        }

        passwordStrengthProgressBar.setVisibility(View.VISIBLE);
        passwordStrengthLabel.setVisibility(View.VISIBLE);
    }

    public static void updateStrengthLabelOnLanguageChange(String password, TextView passwordStrengthLabel, ProgressBar passwordStrengthProgressBar) {
        if (password.isEmpty()) {
            return; // No actualiza si no hay una contraseña generada
        }

        int strength = calculateStrength(password); // Calcular la fortaleza de la contraseña
        updatePasswordStrengthInfo(strength, passwordStrengthLabel, passwordStrengthProgressBar);
    }
}