package mobile.passworld.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UnsafeUrlChecker {
    private static final String TAG = "UnsafeUrlChecker";
    private static final String API_URL = "https://safebrowsing.googleapis.com/v4/threatMatches:find";
    private static final String API_KEY = ""; // API Key
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static boolean isUnsafe(String urlToCheck) {
        try {
            return new CheckUrlTask().execute(urlToCheck).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "Error al comprobar URL: " + e.getMessage());
            return false;
        }
    }

    private static class CheckUrlTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            if (urls.length == 0) return false;

            String urlToCheck = urls[0];
            OkHttpClient client = new OkHttpClient();

            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("client", new JSONObject()
                        .put("clientId", "PassworldSecurityChecker")
                        .put("clientVersion", "1.0"));

                JSONArray threatTypes = new JSONArray();
                threatTypes.put("MALWARE");
                threatTypes.put("SOCIAL_ENGINEERING");

                JSONArray platformTypes = new JSONArray();
                platformTypes.put("WINDOWS");

                JSONArray threatEntryTypes = new JSONArray();
                threatEntryTypes.put("URL");

                JSONArray threatEntries = new JSONArray();
                threatEntries.put(new JSONObject().put("url", urlToCheck));

                requestBody.put("threatInfo", new JSONObject()
                        .put("threatTypes", threatTypes)
                        .put("platformTypes", platformTypes)
                        .put("threatEntryTypes", threatEntryTypes)
                        .put("threatEntries", threatEntries));

                RequestBody body = RequestBody.create(requestBody.toString(), JSON);
                Request request = new Request.Builder()
                        .url(API_URL + "?key=" + API_KEY)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    return responseBody != null && !responseBody.trim().isEmpty();
                }
            } catch (JSONException | IOException e) {
                Log.e(TAG, "Error verificando seguridad URL: " + e.getMessage());
            }

            return false;
        }
    }
}