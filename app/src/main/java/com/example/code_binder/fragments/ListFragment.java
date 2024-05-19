package com.example.code_binder.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.example.code_binder.Application;
import com.example.code_binder.R;
import com.example.code_binder.adapters.ListViewAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

public class ListFragment extends Fragment {
    private ListView codeData_lv;
    private Button send_btn;
    private FloatingActionButton back_btn;
    private ListViewAdapter adapter;

    private Application application;
    private Set<String> codesForAdd;
    private Set<String> codesForDelete;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        adapter = new ListViewAdapter(requireContext());

        send_btn = view.findViewById(R.id.send_btn);
        back_btn = view.findViewById(R.id.btn_back);
        codeData_lv = view.findViewById(R.id.codeData_lv);
        codeData_lv.setAdapter(adapter);

        Gson gson = new Gson();
        //application = gson.fromJson(getIntent().getStringExtra("Message"), Application.class);

        //codesForAdd = new HashSet<>(getIntent().getStringArrayListExtra("codesForAdd"));
        //codesForDelete = new HashSet<>(getIntent().getStringArrayListExtra("codesForDelete"));
        //adapter.addAllProducts(application.getProducts());
        //adapter.notifyDataSetChanged();

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

                    }
                }).start();
            }
        });
    }
}