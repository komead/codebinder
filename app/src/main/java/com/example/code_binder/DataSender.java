package com.example.code_binder;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class DataSender extends AndroidViewModel {
    private Socket socket;

    private OutputStream output;
    private InputStream input;

    private MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    private boolean isAuthorized = false;

    public DataSender(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disconnect();
    }

    public String connect(int hostPort, String hostIP) {
        try {
            socket = new Socket(hostIP, hostPort);
            output = socket.getOutputStream();
            input = socket.getInputStream();
//            isConnected.postValue(true);
            return "success";
        } catch (IOException e) {
            e.printStackTrace();
//            isConnected.postValue(false);
            return e.getMessage();
        }
    }

    public void disconnect() {
        try {
            if (socket != null)
                socket.close();

            if (output != null)
                output.close();

            if (input != null)
                input.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            socket = null;
            output = null;
            input = null;
//            isConnected.postValue(false);
        }
    }

    public boolean isConnected() {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            isAuthorized = false;
//            isConnected.postValue(false);
            return false;
        }
        return true;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public boolean testConnection() {
        try {
            socket.sendUrgentData(0xFF);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String sendData(int messageCode, @NonNull String body) {
        try {
            if (isConnected()) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(2);
                byteBuffer.putShort((short) (1 + body.length()));

                byte[] arr = byteBuffer.array();
                byte a = arr[0];
                arr[0] = arr[1];
                arr[1] = a;

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                byteArrayOutputStream.write(arr);
                byteArrayOutputStream.write(messageCode);
                byteArrayOutputStream.write(body.getBytes(StandardCharsets.UTF_8));

                byte[] array = byteArrayOutputStream.toByteArray();
                output.write(array);

                output.flush();
                return "success";
            } else {
                return "ошибка: Соединение отсутствует";
            }
        } catch (IOException e) {
//            isConnected.postValue(false);
            return "ошибка: " + e;
        }
    }

    public HashMap<String, String> getData() {
        HashMap<String, String> map = new HashMap<>();
        try {
            if (isConnected()) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];

                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    if (input.available() == 0) {
                        break;
                    }
                }
                buffer = byteArrayOutputStream.toByteArray();

                String receivedString = new String(buffer);
                receivedString = receivedString.substring(3);

                map.put("code", Byte.toString(buffer[2]));
                map.put("body", receivedString);
                return map;
            } else {
                map.put("code", null);
                map.put("body", "Socket is not connected");
                return map;
            }
        } catch (IOException e) {
            e.printStackTrace();
//            isConnected.postValue(false);
            map.put("code", null);
            map.put("body", e.getMessage());
            return map;
        }
    }
}
