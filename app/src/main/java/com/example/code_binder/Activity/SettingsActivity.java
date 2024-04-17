package com.example.code_binder.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.code_binder.Preferences;
import com.example.code_binder.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private FloatingActionButton back_btn;
    private Button save_btn;
    private EditText ip_et;
    private EditText port_et;

    private Preferences preferences;
    private Map<String, String> settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        back_btn = findViewById(R.id.btn_back);
        ip_et = findViewById(R.id.et_ip);
        port_et = findViewById(R.id.et_port);
        save_btn = findViewById(R.id.btn_save);

        preferences = new Preferences(this);
        settings = new HashMap<>();

        setListeners();
        getSettings();
    }

    private void getSettings() {
        Map<String, String> oldSettings = preferences.getAll();

        ip_et.setText(oldSettings.get("hostIP"));
        port_et.setText(oldSettings.get("hostPort"));
    }

    private void setListeners() {
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.put("hostIP", ip_et.getText().toString());
                settings.put("hostPort", port_et.getText().toString());
                preferences.saveAll(settings);
            }
        });
    }
}
