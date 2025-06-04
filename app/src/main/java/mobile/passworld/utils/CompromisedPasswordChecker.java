package mobile.passworld.utils;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CompromisedPasswordChecker {
    private static final String TAG = "CompromisedPwdChecker";
    private static final String API_URL = "https://api.pwnedpasswords.com/range/";
    private static final Executor executor = Executors.newCachedThreadPool();

    public interface CompromisedCheckCallback {
        void onResult(boolean isCompromised);
    }

    // Método asíncrono con callback
    public static void checkIfCompromised(String password, CompromisedCheckCallback callback) {
        executor.execute(() -> {
            boolean result = checkPasswordInBackground(password);
            callback.onResult(result);
        });
    }

    // Método sincrónico (solo para uso en hilos secundarios)
    public static boolean isCompromisedPassword(String password) {
        // Advierte sobre el uso sincrónico
        if (Thread.currentThread().getName().equals("main")) {
            Log.w(TAG, "¡ADVERTENCIA! Llamando a isCompromisedPassword desde el hilo principal. " +
                   "Esto puede bloquear la UI, usa checkIfCompromised con callback en su lugar.");
        }

        return checkPasswordInBackground(password);
    }

    private static boolean checkPasswordInBackground(String password) {
        try {
            String hash = sha1(password);
            String prefix = hash.substring(0, 5);
            String suffix = hash.substring(5);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_URL + prefix)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                String[] lines = responseBody.split("\n");
                for (String line : lines) {
                    String[] parts = line.split(":");
                    if (parts.length > 0 && parts[0].equalsIgnoreCase(suffix)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error al comprobar contraseña: " + e.getMessage());
            return false;
        }
    }

    private static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }
}

