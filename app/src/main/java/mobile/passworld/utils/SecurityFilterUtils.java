package mobile.passworld.utils;

import mobile.passworld.data.PasswordDTO;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SecurityFilterUtils {

    // Conjunto que mantiene las contraseñas únicas activas
    private static final Set<String> uniquePasswords = new HashSet<>();

    public interface SecurityAnalysisCallback {
        void onAnalysisComplete(PasswordDTO password);
    }

    // Analizar la seguridad de una contraseña de manera asíncrona
    public static void analyzePasswordSecurityAsync(PasswordDTO passwordDTO, SecurityAnalysisCallback callback) {
        boolean isWeak = isWeakPassword(passwordDTO.getPassword());
        boolean isDuplicate = isDuplicatePassword(passwordDTO.getPassword());
        boolean isUrlUnsafe = isUrlUnsafe(passwordDTO.getUrl());

        passwordDTO.setWeak(isWeak);
        passwordDTO.setDuplicate(isDuplicate);
        passwordDTO.setUrlUnsafe(isUrlUnsafe);

        // Comprobar si está comprometida de forma asíncrona
        CompromisedPasswordChecker.checkIfCompromised(passwordDTO.getPassword(), isCompromised -> {
            passwordDTO.setCompromised(isCompromised);
            callback.onAnalysisComplete(passwordDTO);
        });
    }

    // Método sincrónico original (mantener para compatibilidad)
    public static void analyzePasswordSecurity(PasswordDTO passwordDTO) {
        boolean isWeak = isWeakPassword(passwordDTO.getPassword());
        boolean isDuplicate = isDuplicatePassword(passwordDTO.getPassword());
        boolean isUrlUnsafe = isUrlUnsafe(passwordDTO.getUrl());

        passwordDTO.setWeak(isWeak);
        passwordDTO.setDuplicate(isDuplicate);
        passwordDTO.setUrlUnsafe(isUrlUnsafe);

        // Nota: esto podría bloquear el hilo principal si se llama desde UI
        passwordDTO.setCompromised(isCompromisedPassword(passwordDTO.getPassword()));
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

    // Método para analizar múltiples contraseñas de forma asíncrona
    public static void analyzePasswordsSecurityAsync(List<PasswordDTO> passwords, Runnable onCompleteCallback) {
        if (passwords.isEmpty()) {
            onCompleteCallback.run();
            return;
        }

        AtomicInteger pendingChecks = new AtomicInteger(passwords.size());

        for (PasswordDTO password : passwords) {
            analyzePasswordSecurityAsync(password, updatedPassword -> {
                if (pendingChecks.decrementAndGet() == 0) {
                    onCompleteCallback.run();
                }
            });
        }
    }

    // Eliminar una contraseña de la lista de contraseñas únicas
    public static void removeUniquePassword(String password) {
        uniquePasswords.remove(password);
    }

    // Método para verificar si una URL es insegura
    private static boolean isUrlUnsafe(String url) {
        if (url == null || url.isEmpty() || url.equals("null")) {
            return false; // URL vacía o nula no es insegura
        }

        // Verificar si la URL comienza con http:// (no segura)
        if (url.startsWith("http://")) {
            return true; // URL insegura
        }

        // Comprobar con safe browsing API
        return UnsafeUrlChecker.isUnsafe(url);
    }

    // Limpiar el conjunto de contraseñas únicas
    public static void clearUniquePasswords() {
        uniquePasswords.clear();
    }

    // Agregar una contraseña al conjunto de contraseñas únicas
    public static void addUniquePassword(String password) {
        if (password != null && !password.isEmpty()) {
            uniquePasswords.add(password);
        }
    }
}
