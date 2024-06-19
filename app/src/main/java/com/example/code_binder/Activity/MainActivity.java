package com.example.code_binder.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.code_binder.*;
import com.example.code_binder.fragments.MainFragment;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private DataSender dataSender;
    private Preferences preferences;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        preferences = new Preferences(this);
        if (preferences.isEmpty()) {
            preferences.save("hostPort", "11000");
            preferences.save("hostIP", "192.168.100.4");
        }

        dataSender = new ViewModelProvider(this).get(DataSender.class);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new MainFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (dataSender.isConnected()) {
                            HashMap<String, String> receivedData = dataSender.getData();
                            if (receivedData != null) {
                                dataSender.setLastMessage(receivedData);
                            }
                        }
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}