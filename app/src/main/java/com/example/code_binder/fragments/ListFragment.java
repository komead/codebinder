package com.example.code_binder.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.code_binder.Application;
import com.example.code_binder.CompletedTask;
import com.example.code_binder.DataSender;
import com.example.code_binder.HttpRequests;
import com.example.code_binder.R;
import com.example.code_binder.adapters.ListViewAdapter;
import com.example.code_binder.enums.MessageCode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

public class ListFragment extends Fragment {
    private RecyclerView codeData_rv;
    private Button send_btn;
    private FloatingActionButton back_btn;
    private TextView task_tv;
    private ListViewAdapter adapter;

    private Application application;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);


        Gson gson = new Gson();
        application = gson.fromJson(getArguments().getString("task"), Application.class);

        send_btn = view.findViewById(R.id.send_btn);
        back_btn = view.findViewById(R.id.btn_back);
        task_tv = view.findViewById(R.id.task_tv);
        codeData_rv = view.findViewById(R.id.codeData_rv);
        codeData_rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ListViewAdapter(application.getProducts(), getArguments().getStringArrayList("scannedCodes"));
        codeData_rv.setAdapter(adapter);

        task_tv.setText(application.toString());

        setListeners();

        return view;
    }
    private void setListeners() {
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpRequests httpRequests = new HttpRequests();

                        Gson gson = new Gson();
                        CompletedTask completedTask = new CompletedTask(application.getId(), getArguments().getStringArrayList("scannedCodes"));
                        requireActivity().runOnUiThread(() -> {
                            task_tv.setText(gson.toJson(completedTask));
                        });


                        //httpRequests.Post("http://10.162.0.68:9090/application/json", null);

                        DataSender dataSender = new ViewModelProvider(requireActivity()).get(DataSender.class);
                        dataSender.sendData(MessageCode.JOB_DONE.getCode(), "");
                    }
                }).start();
            }
        });
    }
}