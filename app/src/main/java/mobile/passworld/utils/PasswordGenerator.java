package mobile.passworld.utils;

import android.util.Log;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilidad para generar contraseñas seguras basadas en diferentes criterios.
 */
public class PasswordGenerator {

    private static final String TAG = "PasswordGenerator";

    // Constantes para la generación de contraseñas
    private static final String UPPERCASE = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnñopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_+=<>?/{}[]";

    /**
     * Genera una contraseña basada en los criterios especificados.
     *
     * @param upperAndLowerCase Si se deben incluir mayúsculas y minúsculas
     * @param numbers Si se deben incluir números
     * @param specialChars Si se deben incluir caracteres especiales
     * @param length Longitud deseada de la contraseña
     * @return Una contraseña generada que cumple con los criterios
     */
    public static String generatePassword(boolean upperAndLowerCase, boolean numbers, boolean specialChars, int length) {
        StringBuilder characterPool = new StringBuilder();
        List<Character> passwordChars = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        int mandatoryCharsCount = 0;

        Log.d(TAG, "Generando contraseña con:");
        Log.d(TAG, "Mayúsculas y minúsculas: " + upperAndLowerCase);
        Log.d(TAG, "Números: " + numbers);
        Log.d(TAG, "Símbolos: " + specialChars);
        Log.d(TAG, "Longitud solicitada: " + length);

        if (upperAndLowerCase) {
            characterPool.append(LOWERCASE).append(UPPERCASE);
            char lower = LOWERCASE.charAt(random.nextInt(LOWERCASE.length()));
            char upper = UPPERCASE.charAt(random.nextInt(UPPERCASE.length()));
            passwordChars.add(lower);
            passwordChars.add(upper);
            mandatoryCharsCount += 2;
            Log.d(TAG, "Añadido obligatorio: " + lower + ", " + upper);
        }

        if (numbers) {
            characterPool.append(NUMBERS);
            char num = NUMBERS.charAt(random.nextInt(NUMBERS.length()));
            passwordChars.add(num);
            mandatoryCharsCount += 1;
            Log.d(TAG, "Añadido obligatorio: " + num);
        }

        if (specialChars) {
            characterPool.append(SPECIAL_CHARACTERS);
            char symbol = SPECIAL_CHARACTERS.charAt(random.nextInt(SPECIAL_CHARACTERS.length()));
            passwordChars.add(symbol);
            mandatoryCharsCount += 1;
            Log.d(TAG, "Añadido obligatorio: " + symbol);
        }

        // Fallback
        if (characterPool.length() == 0) {
            characterPool.append(LOWERCASE);
            char fallbackChar = LOWERCASE.charAt(random.nextInt(LOWERCASE.length()));
            passwordChars.add(fallbackChar);
            mandatoryCharsCount = 1;
            Log.d(TAG, "Ninguna opción seleccionada. Usando minúsculas por defecto. Añadido: " + fallbackChar);
        }

        if (length < mandatoryCharsCount) {
            Log.d(TAG, "Longitud menor que los caracteres obligatorios. Ajustando longitud de " + length + " a " + mandatoryCharsCount);
            length = mandatoryCharsCount;
        }

        // Completar la contraseña
        for (int i = passwordChars.size(); i < length; i++) {
            char c = characterPool.charAt(random.nextInt(characterPool.length()));
            passwordChars.add(c);
        }

        Collections.shuffle(passwordChars);

        StringBuilder password = new StringBuilder();
        for (Character c : passwordChars) {
            password.append(c);
        }

        Log.d(TAG, "Contraseña generada: " + password.toString());
        return password.toString();
    }
}
