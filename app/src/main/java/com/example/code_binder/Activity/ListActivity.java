package com.example.code_binder.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.code_binder.Application;
import com.example.code_binder.adapters.ListViewAdapter;
import com.example.code_binder.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

public class ListActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_list);

        adapter = new ListViewAdapter(this);

        send_btn = findViewById(R.id.send_btn);
        back_btn = findViewById(R.id.btn_back);
        codeData_lv = findViewById(R.id.codeData_lv);
        codeData_lv.setAdapter(adapter);

        Gson gson = new Gson();
        application = gson.fromJson(getIntent().getStringExtra("Message"), Application.class);

        codesForAdd = new HashSet<>(getIntent().getStringArrayListExtra("codesForAdd"));
        codesForDelete = new HashSet<>(getIntent().getStringArrayListExtra("codesForDelete"));
        adapter.addAllProducts(application.getProducts());
        adapter.notifyDataSetChanged();

        setListeners();
    }

    private void setListeners() {
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
