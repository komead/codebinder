package com.example.code_binder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SettingsActivity extends AppCompatActivity {
    private FloatingActionButton back_btn;
    private Button save_btn;
    private EditText ip_et;
    private EditText port_et;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("ApplicationPrefs", MODE_PRIVATE);

        back_btn = findViewById(R.id.btn_back);
        ip_et = findViewById(R.id.et_ip);
        port_et = findViewById(R.id.et_port);
        save_btn = findViewById(R.id.btn_save);

        setListeners();
        getSettings();
    }

    private void getSettings() {
        ip_et.setText(preferences.getString("hostIP", "10.162.0.133"));
        port_et.setText(preferences.getString("hostPort", "11000"));
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
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("hostIP", ip_et.getText().toString());
                editor.putString("hostPort", port_et.getText().toString());
                editor.apply();
            }
        });
    }
}
