package com.example.code_binder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class ScanActivity extends AppCompatActivity {
    private PreviewView camera_pv;
    private FloatingActionButton back_btn;
    private FloatingActionButton list_btn;
    private FloatingActionButton torch_btn;
    private TextView codeCounter_tv;

    private HashSet<String> scannedCodes;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Camera camera;

    private boolean torchState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        camera_pv = findViewById(R.id.viewFinder);
        back_btn = findViewById(R.id.btn_back);
        list_btn = findViewById(R.id.btn_list);
        torch_btn = findViewById(R.id.btn_torch);
        codeCounter_tv = findViewById(R.id.counter);

        scannedCodes = new HashSet<>();
        torchState = false;

        codeCounter_tv.setText("Отсканировано " + scannedCodes.size());

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
                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();
                                if (rawValue != null) {
                                    scannedCodes.add(rawValue);
                                    runOnUiThread(() -> {
                                        codeCounter_tv.setText("Отсканировано: " + scannedCodes.size());
                                    });
                                }
                            }
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
                CodeDataSource codeDataSource = new CodeDataSource(ScanActivity.this);
                codeDataSource.open();
                codeDataSource.clear();

                for (String code : scannedCodes)
                    codeDataSource.addData(code);

                codeDataSource.close();

                Intent intent = new Intent(ScanActivity.this, ListActivity.class);
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
                    torch_btn.setImageResource(R.drawable.icon_flash_off);
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