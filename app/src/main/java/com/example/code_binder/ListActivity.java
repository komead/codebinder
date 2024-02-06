package com.example.code_binder;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    private ListView codeData_lv;
    private Button send_btn;
    private FloatingActionButton back_btn;
    private ListViewAdapter adapter;

    private ArrayList<String> scannedCodes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        adapter = new ListViewAdapter(this);

        send_btn = findViewById(R.id.send_btn);
        back_btn = findViewById(R.id.btn_back);
        codeData_lv = findViewById(R.id.codeData_lv);
        codeData_lv.setAdapter(adapter);

        CodeDataSource codeDataSource = new CodeDataSource(ListActivity.this);
        codeDataSource.open();
        scannedCodes = codeDataSource.getAllData();
        codeDataSource.close();

        if (scannedCodes != null)
            for (String str : scannedCodes) {
                adapter.addCode(str);
                adapter.notifyDataSetChanged();
            }

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
