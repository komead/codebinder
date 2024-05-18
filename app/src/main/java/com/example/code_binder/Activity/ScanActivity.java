package com.example.code_binder.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.example.code_binder.Application;
import com.example.code_binder.R;
import com.example.code_binder.adapters.ScannedCodesStorage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.google.gson.Gson;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class ScanActivity extends AppCompatActivity {
    private PreviewView camera_pv;
    private FloatingActionButton back_btn;
    private FloatingActionButton list_btn;
    private FloatingActionButton torch_btn;
    private TextView text_tv;
    private Switch delete_s;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Camera camera;
    private ScannedCodesStorage codesStorage;
    private Application application;

    private boolean torchState;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        camera_pv = findViewById(R.id.viewFinder);
        back_btn = findViewById(R.id.btn_back);
        list_btn = findViewById(R.id.btn_list);
        torch_btn = findViewById(R.id.btn_torch);
        text_tv = findViewById(R.id.tv_text);
        delete_s = findViewById(R.id.switch1);

        message = getIntent().getStringExtra("Message");
        Gson gson = new Gson();
        application = gson.fromJson(message, Application.class);

        codesStorage = new ScannedCodesStorage();
        torchState = false;

        text_tv.setText("Ничего не сканировалось");

        setListeners();
        startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Закрыть пул потоков когда Activity уничтожается
        executor.shutdown();
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // Создание предпросмотра
        Preview preview = new Preview.Builder().build();

        // Выбор камеры (задняя)
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // вывод картинки в предпросмотр
        preview.setSurfaceProvider(camera_pv.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // Обработка каждого кадра с камеры
        imageAnalysis.setAnalyzer(executor, imageProxy -> {
            @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                // Инициализируем сканер штрих-кодов
                BarcodeScanner scanner = BarcodeScanning.getClient();
                // Процесс распознавания
                scanner.process(inputImage)
                        .addOnSuccessListener(barcodes -> {
                            boolean scannings = false;

                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();

                                if (rawValue != null && !codesStorage.isScanned(rawValue)) {
                                    if (!delete_s.isChecked()) {
                                        scannings = true;

                                        codesStorage.addCode(rawValue);
                                    } else {
                                        scannings = true;

                                        codesStorage.deleteCode(rawValue);
                                    }
                                }
                            }

                            boolean finalScannings = scannings;
                            runOnUiThread(() -> {
                                //text_tv.setText("Сканирований: " + codesStorage.quantity());

                                if (finalScannings)
                                    Toast.makeText(this, "Отсканировано", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            }
        });

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void setListeners() {
        list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScanActivity.this, ListActivity.class);
                intent.putExtra("Message", message);
                intent.putStringArrayListExtra("codesForAdd", new ArrayList<>(codesStorage.getCodesForAdd()));
                intent.putStringArrayListExtra("codesForDelete", new ArrayList<>(codesStorage.getCodesForDelete()));
                startActivity(intent);
                //finish();
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        torch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (torchState) {
                    torch_btn.setImageResource(com.example.code_binder.R.drawable.icon_flash_off);
                    camera.getCameraControl().enableTorch(false);
                    torchState = false;
                }
                else {
                    torch_btn.setImageResource(R.drawable.icon_flash_on);
                    camera.getCameraControl().enableTorch(true);
                    torchState = true;
                }
            }
        });
    }

}