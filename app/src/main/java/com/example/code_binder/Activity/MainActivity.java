package com.example.code_binder.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.code_binder.*;
import com.example.code_binder.enums.MessageCode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button start_btn;
    private Button load_btn;
    private FloatingActionButton settings_btn;
    private TextView task_tv;
    private TextView taskId_tv;

    private ArrayList<String> savedCodes;
    private Application task;
    private DataSender dataSender;
    private Gson gson;
    private Preferences preferences;
    private String message;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = new Preferences(this);
        preferences.save("hostPort", "11000");
        preferences.save("hostIP", "192.168.5.108");

        settings_btn = findViewById(R.id.btn_settings);
        start_btn = findViewById(R.id.btn_start);
        load_btn = findViewById(R.id.btn_load);
        task_tv = findViewById(R.id.tv_task);
        taskId_tv = findViewById(R.id.tv_id);

        //start_btn.setVisibility(View.INVISIBLE);
        //start_btn.setEnabled(false);

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        loadDB();

        if (!savedCodes.isEmpty()) {
            Toast.makeText(this, "Есть незавершённый сеанс", Toast.LENGTH_SHORT).show();
        }

        setListeners();
    }

    private void loadDB () {
        CodeDataSource codeDataSource = new CodeDataSource(MainActivity.this);
        codeDataSource.open();

        savedCodes = codeDataSource.getAllData();

        codeDataSource.close();
    }

    private void setListeners() {
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataSender.disconnect();
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                intent.putExtra("Message", message);
                startActivity(intent);
            }
        });

        load_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gson = new Gson();
                        dataSender = new DataSender(Integer.parseInt(preferences.get("hostPort")), preferences.get("hostIP"));
                        dataSender.connect();
                        dataSender.sendData(MessageCode.START_AUTH.getCode(), "login=test_login=password=test_pass");
                        //message = dataSender.getData();

                        task = gson.fromJson(message, Application.class);

                        if (task != null) {
                            StringBuilder stringBuilder = new StringBuilder();

                            for (Product product : task.getProducts()) {
                                stringBuilder.append(product.getTitle());
                                stringBuilder.append(" - ");
                                stringBuilder.append(product.getCount());
                                stringBuilder.append(" шт.\n");
                            }

                            runOnUiThread(() -> {
                                taskId_tv.setText("Для выполнения доступна заявка №" + task.getId());
                                task_tv.setText(stringBuilder);

                                //load_btn.setVisibility(View.INVISIBLE);
                                //start_btn.setVisibility(View.VISIBLE);
                                start_btn.setEnabled(true);
                            });
                        }
                    }
                }).start();
            }
        });
    }
}
