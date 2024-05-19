package com.example.code_binder.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.code_binder.Preferences;
import com.example.code_binder.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    private FloatingActionButton back_btn;
    private Button save_btn;
    private EditText ip_et;
    private EditText port_et;

    private Preferences preferences;
    private Map<String, String> settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        back_btn = view.findViewById(R.id.btn_back);
        ip_et = view.findViewById(R.id.et_ip);
        port_et = view.findViewById(R.id.et_port);
        save_btn = view.findViewById(R.id.btn_save);

        preferences = new Preferences(requireContext());
        settings = new HashMap<>();

        setListeners();
        getSettings();

        return view;
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
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
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