package com.example.code_binder;

import android.content.Context;
import android.widget.Toast;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

public class DataSender {
    private Context context;

    private final int port = 11000;
    private final String hostIp = "10.162.0.133";

    public DataSender(Context context) {
        this.context = context;
    }

    public String sendData(String data) {
        // Отправка данных на сервер
        try {
            Socket socket = new Socket(hostIp, port);

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            bufferedWriter.write(data);
            bufferedWriter.flush();
            socket.close();
        } catch (IOException e) {
            System.err.println("ошибка: " + e);
        }

        return "success";
    }

    public String getData() {
        Gson gson = new Gson();

        try {
            Socket socket = new Socket(hostIp, port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Читаем строку, полученную от сервера
            String receivedString = reader.readLine();
            Toast.makeText(context, receivedString, Toast.LENGTH_SHORT).show();

            Type listType = new TypeToken<List<Application>>(){}.getType();
            List<Application> requirements = gson.fromJson(receivedString, listType);

            // Закрываем соединение
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "success";
    }
}
