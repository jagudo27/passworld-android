package mobile.passworld.utils;

import mobile.passworld.data.PasswordDTO;

import java.util.Set;
import java.util.HashSet;

public class SecurityFilterUtils {

    // Conjunto que mantiene las contraseñas únicas activas
    private static final Set<String> uniquePasswords = new HashSet<>();

    // Analizar la seguridad de una contraseña, usuario y URL
    public static void analyzePasswordSecurity(PasswordDTO passwordDTO) {
        boolean isWeak = isWeakPassword(passwordDTO.getPassword());
        boolean isDuplicate = isDuplicatePassword(passwordDTO.getPassword());
        boolean isCompromised = isCompromisedPassword(passwordDTO.getPassword());
        boolean isUrlUnsafe = isUrlUnsafe(passwordDTO.getUrl());

        passwordDTO.setWeak(isWeak);
        passwordDTO.setDuplicate(isDuplicate);
        passwordDTO.setCompromised(isCompromised);
        passwordDTO.setUrlUnsafe(isUrlUnsafe);
    }

    // Verificar si la contraseña es débil
    private static boolean isWeakPassword(String password) {
        return PasswordEvaluator.calculateStrength(password) < 3;
    }

    // Verificar si la contraseña está duplicada
    private static boolean isDuplicatePassword(String password) {
        // Si ya está en el conjunto de contraseñas únicas, se marca como duplicada
        return !uniquePasswords.add(password);  // Si no se puede agregar, es duplicada
    }


    // Verificar si la contraseña ha sido comprometida
    private static boolean isCompromisedPassword(String password) {
        try {
            return CompromisedPasswordChecker.isCompromisedPassword(password);
        } catch (Exception e) {
            System.err.println("Error checking compromised password: " + e.getMessage());
            LogUtils.LOGGER.warning("Error checking compromised password: " + e.getMessage());
            return false;
        }
    }

    // Eliminar una contraseña de la lista de contraseñas únicas
    public void removeUniquePassword(String password) {
        uniquePasswords.remove(password);
    }

    // Agregar una contraseña a la lista de contraseñas únicas
    public static void addUniquePassword(String password) {
        uniquePasswords.add(password);
    }

    // Limpiar la lista de contraseñas únicas
    public static void clearUniquePasswords() {
        uniquePasswords.clear();
    }

    // Verificar si la URL es insegura
    public static boolean isUrlUnsafe(String url) {
        return UnsafeUrlChecker.isUnsafe(url);
    }

    public static boolean hasPasswordSecurityIssues(PasswordDTO passwordDTO) {
        return passwordDTO.isWeak() || passwordDTO.isDuplicate() || passwordDTO.isCompromised() || passwordDTO.isUrlUnsafe();
    }
}
