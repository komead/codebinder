package com.example.code_binder.fragments;

import android.annotation.SuppressLint;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
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
import com.example.code_binder.DataMatrixParser;
import com.example.code_binder.HttpRequests;
import com.example.code_binder.Preferences;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Application application;
    private HttpRequests httpRequests;
    private Gson gson;
    private Preferences preferences;
    private DataMatrixParser dataMatrixParser;

    private boolean torchState;
    private String task;
    private ArrayList<String> scannedBoxes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new Preferences(requireContext());
        httpRequests = new HttpRequests();
        dataMatrixParser = new DataMatrixParser();
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

        if (application == null) {
            application = gson.fromJson(task, Application.class);
            application.fillAllGtins();
        }

        if (scannedBoxes == null) {
            scannedBoxes = new ArrayList<>();
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
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
                                log(dataFromBarcode);
                                HashMap<String, String> parts = dataMatrixParser.parseDataMatrix(dataFromBarcode);
                                Product product = application.getProductByGtin(parts.get("01"));

                                if (dataFromBarcode != null && product != null) {
                                    if (dataFromBarcode.length() <= 40) {
                                        productBarcode(parts.get("01"), parts.get("21"), product);
                                    } else {
                                        boxBarcode(dataFromBarcode, product);
                                    }
                                }
                            }
                        })
                        .addOnCompleteListener(task -> imageProxy.close());
            }
        });

        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void productBarcode(String gtin, String packageNumber, Product product) {
        if (!product.isScanned(packageNumber) && !delete_s.isChecked()) {
            if (product.getAlreadyScanned() < product.getCount()) {
                product.addPackage(packageNumber);
                product.setAlreadyScanned(product.getAlreadyScanned() + 1);
                setMessage(product.getTitle() + "\t" + product.getAlreadyScanned() + "\\" + product.getCount());
            } else {
                //добавить вывод информации о том, что необходимое кол-во продуктов отсканированно и при превышении остальные нужно удалить
            }
        } else if (product.isScanned(packageNumber) && delete_s.isChecked()) {
            if (product.getAlreadyScanned() > 0) {
                product.deletePackage(packageNumber);
                product.setAlreadyScanned(product.getAlreadyScanned() - 1);
                setMessage(product.getTitle() + "\t" + product.getAlreadyScanned() + "\\" + product.getCount());
            } else {
                //можно добавить вывод информации о том, что продукты данного типа не сканировались, поэтому удалять нечего
            }
        }
    }

    private void boxBarcode(String boxBarcode, Product product) {
        if ((!scannedBoxes.contains(boxBarcode) && !delete_s.isChecked()) || (scannedBoxes.contains(boxBarcode) && delete_s.isChecked())) {
            if (!scannedBoxes.contains(boxBarcode))
                scannedBoxes.add(boxBarcode);
            else
                scannedBoxes.remove(boxBarcode);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String json = httpRequests.Get("http://"
                            + preferences.get("serverIP") + ":"
                            + preferences.get("serverPort") + "/box/"
                            + boxBarcode);

                    //!!!!!!!!!!!!!!!!!!не забыть удалить логирование
                    requireActivity().runOnUiThread(() -> {
                        Log.d("scanned", json);
                    });

                    JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                    Type listType = new TypeToken<List<String>>() {}.getType();
                    List<String> list = gson.fromJson(jsonObject.get("items"), listType);

                    int count = 0;
                    String message = "изменено ";

                    for (String string : list) {
                        if (!delete_s.isChecked() && !product.isScanned(string)) {
                            product.addPackage(string);
                            message = "добавлено ";
                        } else if (delete_s.isChecked() && product.isScanned(string)) {
                            product.deletePackage(string);
                            message = "удалено ";
                        }
                        count++;
                    }

                    setMessage(message + count + " элементов");
                }
            }).start();
        } else {

        }
    }

    private void log(String string) {
        requireActivity().runOnUiThread(() -> {
            Log.d("mmmeeesssaaagggeee", string);
        });
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

                ArrayList<String> scannedProducts = new ArrayList<>();
                for (Product product : application.getProducts()) {
                    for (String string : product.getScannedPackages())
                        scannedProducts.add(product.getGtin() + string);
                }

//                bundle.putStringArrayList("scannedCodes", scannedProducts);
                bundle.putSerializable("application", application);

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
                application = null;
                executor.shutdown();

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