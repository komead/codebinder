package com.example.code_binder;

import android.content.Context;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String sendData(int messageCode, String body) {
        // Отправка данных на сервер
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            OutputStream output = socket.getOutputStream();
            ByteBuffer byteBuffer = ByteBuffer.allocate(2);
            byteBuffer.putShort((short)(1 + body.length()));

            byte[] arr = byteBuffer.array();
            byte a = arr[0];
            arr[0] = arr[1];
            arr[1] = a;

//            for (byte b : arr)
//            Log.d("buffer", Byte.toString(b));

            byteArrayOutputStream.write(arr);
            byteArrayOutputStream.write(messageCode);
            byteArrayOutputStream.write(body.getBytes(StandardCharsets.UTF_8));

            byte[] array = byteArrayOutputStream.toByteArray();
            output.write(array);

            output.flush();
            output.close();
        } catch (IOException e) {
            System.err.println("ошибка: " + e);
        }

        return "success";
    }

    public String getData() {
        String receivedString = "";

        try {
            InputStream input = socket.getInputStream();

            // Читаем полученную строку
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                System.out.write(buffer, 0, bytesRead); // Выводим принятые байты на консоль
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivedString;
    }
}
