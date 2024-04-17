package com.example.code_binder;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.*;
import java.net.Socket;

import static android.content.Context.MODE_PRIVATE;

public class DataSender {
    private Context context;

    private final int hostPort;
    private final String hostIP;
    private Socket socket;

    public DataSender(int hostPort, String hostIP) {
        this.hostPort = hostPort;
        this.hostIP = hostIP;
    }

    public void connect() {
        try {
            socket = new Socket(hostIP, hostPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendData(String data) {
        // Отправка данных на сервер
        try {
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
        String receivedString = "";

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Читаем строку, полученную от сервера
            receivedString = reader.readLine();

            // Закрываем соединение
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedString;
    }
}
