package com.example.code_binder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class DataSender {
    public Void sendData(ArrayList<String> data) {
        // Отправка данных на сервер
        if (data != null && !data.isEmpty()) {
            ApiService apiService = ApiClient.getApiService();
            Call<Void> call = apiService.addDataToServer(data);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d("DataSyncTask", "Данные успешно отправлены на сервер");
                    } else {
                        Log.e("DataSyncTask", "Ошибка при отправке данных на сервер");
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("DataSyncTask", "Ошибка при отправке запроса на сервер", t);
                }
            });
        }

        return null;
    }
}
