package com.example.code_binder.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.code_binder.Application;
import com.example.code_binder.DataSender;
import com.example.code_binder.Preferences;
import com.example.code_binder.Product;
import com.example.code_binder.R;
import com.example.code_binder.enums.MessageCode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

public class MainFragment extends Fragment {
    private Button start_btn;
    private Button load_btn;
    private FloatingActionButton settings_btn;
    private TextView task_tv;
    private TextView taskId_tv;

    private Application task;
    private DataSender dataSender;
    private Gson gson;
    private String message;

    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new Preferences(requireContext());
        preferences.save("hostPort", "11000");
        preferences.save("hostIP", "192.168.100.4");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        settings_btn = view.findViewById(R.id.btn_settings);
        start_btn = view.findViewById(R.id.btn_start);
        load_btn = view.findViewById(R.id.btn_load);
        task_tv = view.findViewById(R.id.tv_task);
        taskId_tv = view.findViewById(R.id.tv_id);

        setListeners();

        //start_btn.setVisibility(View.INVISIBLE);
        //start_btn.setEnabled(false);



        return view;
    }

    private void setListeners() {
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment settingsFragment = new SettingsFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, settingsFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment secondFragment = new ScanFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, secondFragment)
                        .addToBackStack(null)
                        .commit();
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

                            requireActivity().runOnUiThread(() -> {
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