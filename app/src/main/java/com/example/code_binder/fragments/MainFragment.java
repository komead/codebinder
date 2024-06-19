package com.example.code_binder.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
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

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment {
    private Button start_btn;
    private Button load_btn;
    private TextView task_tv;
    private TextView taskId_tv;
    private FloatingActionButton settings_btn;

    private Application application;
    private Preferences preferences;
    private DataSender dataSender;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private String task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new Preferences(requireContext());
        dataSender = new ViewModelProvider(requireActivity()).get(DataSender.class);
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

        connect();

        dataSender.getLastMessage().observe(getViewLifecycleOwner(), new Observer<Map<String, String>>() {
            @Override
            public void onChanged(Map<String, String> map) {
                if (!map.isEmpty()) {
                    switch (Integer.parseInt(map.get("code"))) {
                        case 2:
                            Gson gson = new Gson();
                            task = map.get("body");
                            application = gson.fromJson(task, Application.class);

                            executorService.execute(() -> {
                                dataSender.sendData(MessageCode.PROPOSAL_ACCEPTED.getCode(), "");
                            });

                            if (application != null) {
                                StringBuilder stringBuilder = new StringBuilder();

                                for (Product product : application.getProducts()) {
                                    stringBuilder.append(product.getTitle());
                                    stringBuilder.append(" - ");
                                    stringBuilder.append(product.getCount());
                                    stringBuilder.append(" шт.\n");
                                }

                                requireActivity().runOnUiThread(() -> {
                                    taskId_tv.setText("Для выполнения доступна заявка №" + application.getId());
                                    task_tv.setText(stringBuilder);

                                    load_btn.setVisibility(View.INVISIBLE);
                                    load_btn.setEnabled(false);
                                    start_btn.setVisibility(View.VISIBLE);
                                    start_btn.setEnabled(true);
                                });
                            }
                            break;
                        case 3:
                            String body = map.get("body");
                            String serverIP = "";
                            int i;
                            for (i = 7; i < body.length(); i++) {
                                if (body.charAt(i) == ':')
                                    break;
                                else
                                    serverIP += body.charAt(i);
                            }
                            i++;

                            preferences.save("serverIP", serverIP);
                            preferences.save("serverPort", body.substring(i, body.length()));
//                            Log.d("server", serverIP + "  " + body.substring(i, body.length()));
                            dataSender.setAuthorized(true);
                            break;
                        case 4:
                            taskId_tv.setText("Устройство не прошло аутентификацию");
                            task_tv.setText("Проверьте порт и адрес в настройках и повторите попытку");
                            break;
                    }
                }
            }
        });


        return view;
    }

    private void connect() {
        if (!dataSender.isConnected()) {
            executorService.execute(() -> {
                String responce = dataSender.connect(Integer.parseInt(preferences.get("hostPort")), preferences.get("hostIP"));

                if (responce.equals("success")) {
                    requireActivity().runOnUiThread(() -> {
                        taskId_tv.setText("Не получена заявка");
                        task_tv.setText("Нажмите \"Загрузить\" для получения заявки");
                        load_btn.setText("Загрузить");
                    });

                    authorize();
                } else {
                    requireActivity().runOnUiThread(() -> {
                        taskId_tv.setText("Нет соединения с сервером");
                        task_tv.setText("Проверьте настройки подключения и повторите попытку");
                        load_btn.setText("Подключиться");
                    });
                }
            });
        }
    }

    private void authorize() {
        executorService.execute(() -> {
            dataSender.sendData(MessageCode.START_AUTH.getCode(), "login=test_login=password=test_pass");
        });
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
                Bundle bundle = new Bundle();
                bundle.putString("task", task);

                Fragment scanFragment = new ScanFragment();
                scanFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, scanFragment)
                        .addToBackStack("scan")
                        .commit();
            }
        });

        load_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dataSender.isConnected()) {
                    connect();
                } else if (!dataSender.isAuthorized()) {
                    authorize();
                }
            }
        });
    }
}