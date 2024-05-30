package com.example.code_binder.fragments;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.code_binder.Application;
import com.example.code_binder.HttpRequests;
import com.example.code_binder.Product;
import com.example.code_binder.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanFragment extends Fragment {
    private PreviewView camera_pv;
    private FloatingActionButton back_btn;
    private FloatingActionButton list_btn;
    private FloatingActionButton torch_btn;
    private TextView text_tv;
    private Switch delete_s;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Camera camera;
    private ArrayList<String> scannedCodes;
    private Application application;
    private ArrayList<String> gtins;
    private ArrayList<String> boxGtin;
    private HttpRequests httpRequests;
    private Gson gson;

    private boolean torchState;
    private String task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        httpRequests = new HttpRequests();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        back_btn = view.findViewById(R.id.btn_back);
        list_btn = view.findViewById(R.id.btn_list);
        torch_btn = view.findViewById(R.id.btn_torch);
        text_tv = view.findViewById(R.id.tv_text);
        delete_s = view.findViewById(R.id.switch1);

        task = getArguments().getString("task");
        gson = new Gson();
        application = gson.fromJson(task, Application.class);
        gtins = new ArrayList<>();

        for (Product product : application.getProducts())
            gtins.add(product.getGtin());

        if (scannedCodes == null) {
            scannedCodes = new ArrayList<>();
            boxGtin = new ArrayList<>();
        }
        torchState = false;

        text_tv.setText("Ничего не сканировалось");

        setListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        camera_pv = view.findViewById(R.id.viewFinder);
        startCamera();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        executor.shutdown();
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
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
                                String dataFromBarcode = barcode.getRawValue();

                                /////не забыть удалить!!!!!!!!!!!!!!!
//                                if (dataFromBarcode != null)
//                                    requireActivity().runOnUiThread(() -> {
//                                        Log.d("scanned", dataFromBarcode + "  длина:  " + dataFromBarcode.length());
//                                        Log.d("scanned", dataFromBarcode.substring(4, 14));
//                                    });

                                if (dataFromBarcode != null && gtins.contains(dataFromBarcode.substring(4, 14))) {
                                    if (!delete_s.isChecked()) {
                                        if (dataFromBarcode.length() <= 40) {
                                            if (!scannedCodes.contains(dataFromBarcode)) {
                                                scannedCodes.add(dataFromBarcode);
                                                setMessage("added");
                                            }
                                        } else if (!boxGtin.contains(dataFromBarcode)) {
                                            boxGtin.add(dataFromBarcode);
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String json = httpRequests.Get("http://192.168.100.4:9090/box/" + dataFromBarcode);
                                                    requireActivity().runOnUiThread(() -> {
                                                        Log.d("scanned", json);
                                                    });
                                                    JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                                                    Type listType = new TypeToken<List<String>>() {
                                                    }.getType();
                                                    List<String> list = gson.fromJson(jsonObject.get("items"), listType);

                                                    for (String string : list)
                                                        requireActivity().runOnUiThread(() -> {
                                                            Log.d("scanned", string);
                                                        });

                                                    for (String string : list) {
                                                        if (!scannedCodes.contains(string)) {
                                                            scannedCodes.add(dataFromBarcode);
                                                        }
                                                    }
                                                }
                                            }).start();
                                        }
                                    } else {
                                        if (dataFromBarcode.length() <= 40 && scannedCodes.contains(dataFromBarcode)) {
                                            scannedCodes.remove(dataFromBarcode);
                                            setMessage("deleted");
                                        }
                                    }
                                }
                            }
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            }
        });

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void setMessage(String message) {
        requireActivity().runOnUiThread(() -> {
            text_tv.setText(message);
        });
    }

    private void setListeners() {
        list_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("task", task);
                bundle.putStringArrayList("scannedCodes", scannedCodes);

                Fragment listFragment = new ListFragment();
                listFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, listFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
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