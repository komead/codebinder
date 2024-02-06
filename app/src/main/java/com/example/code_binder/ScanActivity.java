package com.example.code_binder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import android.util.Size;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;

public class ScanActivity extends AppCompatActivity {
    private PreviewView camera_pv;
    private FloatingActionButton back_btn;
    private FloatingActionButton save_btn;
    private FloatingActionButton torch_btn;
    private TextView codeCounter_tv;

    private ImageCapture imageCapture;
    private HashSet<String> scannedCodes;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Camera camera;
    private int numberOfCodes;
    private boolean torchState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        camera_pv = findViewById(R.id.viewFinder);
        back_btn = findViewById(R.id.btn_back);
        save_btn = findViewById(R.id.btn_save);
        torch_btn = findViewById(R.id.btn_torch);
        codeCounter_tv = findViewById(R.id.counter);

        scannedCodes = new HashSet<>();

        numberOfCodes = getIntent().getIntExtra("Number", 1);
        codeCounter_tv.setText(scannedCodes.size() + "/" + numberOfCodes);

        save_btn.setClickable(false);
        save_btn.setVisibility(View.INVISIBLE);

        torchState = false;

        save_btn.setOnClickListener(new View.OnClickListener() {
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
                finish();
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
                .setTargetResolution(new Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();


        // Обработка каждого кадра с камеры
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                // Распознание Data Matrix кода
                for (int currentAngle = 0; currentAngle <= 45; currentAngle = currentAngle + 5) {
                    ArrayList<String> dataMatrixData = decodeDataMatrixCode(rotateBitmap(image.toBitmap(), currentAngle));
                    if (dataMatrixData != null) {
                        runOnUiThread(() -> {
                            for (String data : dataMatrixData)
                                if (scannedCodes.size() <= numberOfCodes) {
                                    scannedCodes.add(data);

                                    codeCounter_tv.setText(scannedCodes.size() + "/" + numberOfCodes);

                                    if (scannedCodes.size() == numberOfCodes)
                                        Toast.makeText(ScanActivity.this, "Остался код на коробке", Toast.LENGTH_SHORT).show();
                                    else if (scannedCodes.size() == (numberOfCodes + 1)) {
                                        image.close();
                                        cameraProvider.unbindAll();

                                        save_btn.setClickable(true);
                                        save_btn.setVisibility(View.VISIBLE);
                                    }
                                }
                        });
                        break;
                    }
                }
                image.close();
            }
        });

        // Связывание компонентов камеры. Компоненты будут работать, пока жива ScanActivity
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
    }

    private ArrayList<String> decodeDataMatrixCode(Bitmap bitmap) {
        MultiFormatReader multiFormatReader = new MultiFormatReader();
        Map<DecodeHintType,Object> hints = new HashMap<>();
        //тщательное распознание кода
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        //поиск только DataMatrix
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.DATA_MATRIX));
        multiFormatReader.setHints(hints);

        GenericMultipleBarcodeReader multipleBarcodeReader = new GenericMultipleBarcodeReader(multiFormatReader);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(width, height, pixels)));

        try {
            Result[] result = multipleBarcodeReader.decodeMultiple(binaryBitmap);

            // Распознанный текст Data Matrix кода
            ArrayList<String> dataMatrixData = new ArrayList<>();
            for (Result res : result)
                dataMatrixData.add(res.getText());

            return dataMatrixData;
        } catch (Exception e) {
            // Data Matrix код не был обнаружен
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap rotateBitmap(Bitmap original, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
    }

}