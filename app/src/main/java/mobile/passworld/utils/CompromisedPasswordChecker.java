package mobile.passworld.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CompromisedPasswordChecker {
    private static final String TAG = "CompromisedPwdChecker";
    private static final String API_URL = "https://api.pwnedpasswords.com/range/";

    public static boolean isCompromisedPassword(String password) {
        try {
            return new CheckPasswordTask().execute(password).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error al comprobar contraseña: " + e.getMessage());
            return false;
        }
    }

    private static class CheckPasswordTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... passwords) {
            if (passwords.length == 0) return false;

            String password = passwords[0];
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
            } catch (NoSuchAlgorithmException | IOException e) {
                Log.e(TAG, "Error al comprobar si la contraseña está comprometida: " + e.getMessage());
            }
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