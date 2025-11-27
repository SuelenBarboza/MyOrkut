package myorkut.com;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnection {

    // Suelen private static final String BASE_URL = "http://172.19.160.1:3000/api/";
    private static final String BASE_URL = "http://192.168.1.104:3000/api/"; // Eliseu
    private static final String PREFS = "app_prefs";
    private static final String TOKEN_KEY = "jwt_token";

    public static String getBaseUrl() {
        return BASE_URL;
    }

    // Salva token JWT
    public static void saveToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    // Pega token para rotas protegidas
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    // Requisição POST
    public static String post(String endpoint, String jsonBody, Context ctx) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");


        // Se for rota protegida
        String token = getToken(ctx);
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setDoOutput(true);
        OutputStream os = conn.getOutputStream();
        os.write(jsonBody.getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        conn.disconnect();

        return sb.toString();
    }


    public static String get(String endpoint, Context ctx) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");

        // Token JWT (se existir)
        String token = getToken(ctx);
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }


        conn.setDoInput(true);

        int responseCode = conn.getResponseCode();
        System.out.println("📌 Response Code: " + responseCode);

        InputStream is = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();
        conn.disconnect();

        System.out.println("📌 Resposta da API: " + sb.toString());

        return sb.toString();
    }

}