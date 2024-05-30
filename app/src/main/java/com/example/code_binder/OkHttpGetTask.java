package com.example.code_binder;

import android.os.AsyncTask;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpGetTask extends AsyncTask<String, Void, String> {

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "GET request not worked";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // Здесь вы можете обновить UI с результатом
        System.out.println(result);
    }
}
