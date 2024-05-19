package com.example.code_binder.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.code_binder.*;
import com.example.code_binder.fragments.MainFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> savedCodes;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new MainFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        loadDB();

        if (!savedCodes.isEmpty()) {
            Toast.makeText(this, "Есть незавершённый сеанс", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDB () {
        CodeDataSource codeDataSource = new CodeDataSource(MainActivity.this);
        codeDataSource.open();

        savedCodes = codeDataSource.getAllData();

        codeDataSource.close();
    }
}
