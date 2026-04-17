package com.priyansh.vedicMaths;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCaseGroup;
import androidx.camera.core.ViewPort;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;

public class ScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private TextRecognizer recognizer;
    private volatile boolean isScanning = false;
    private long scanStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        previewView = findViewById(R.id.previewView);
        Button btnScan = findViewById(R.id.btnScan);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);
        }

        btnScan.setOnClickListener(v -> {
            performHaptic(v);
            if (!isScanning) {
                isScanning = true;
                scanStartTime = System.currentTimeMillis();
                Toast.makeText(this, "Scanning... Point at a problem", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::processImageProxy);

                // Create ViewPort to ensure OCR sees what the user sees
                ViewPort viewPort = previewView.getViewPort();
                
                UseCaseGroup.Builder builder = new UseCaseGroup.Builder()
                        .addUseCase(preview)
                        .addUseCase(imageAnalysis);

                if (viewPort != null) {
                    builder.setViewPort(viewPort);
                }

                UseCaseGroup useCaseGroup = builder.build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("ScannerActivity", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void processImageProxy(ImageProxy imageProxy) {
        if (!isScanning) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        if (!isScanning) return;

                        if (processText(visionText, imageProxy)) {
                            isScanning = false;
                        } else if (System.currentTimeMillis() - scanStartTime > 4000) {
                            isScanning = false;
                            runOnUiThread(() -> Toast.makeText(this, "Align the problem inside the red frame.", Toast.LENGTH_LONG).show());
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Keep scanning
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }

    private boolean processText(Text visionText, ImageProxy imageProxy) {
        // Improved regex: No \b boundaries inside to support "999x1013" without spaces
        // Captures digits (1-9 digits) and a separator (common math symbols or 1-3 non-digit chars)
        Pattern pattern = Pattern.compile("(\\d{1,9})\\s*([xX*×÷/+-]|[^0-9\\s]{1,3})\\s*(\\d{1,9})");
        
        int bestOverallScore = -1;
        int bestA = -1, bestB = -1;

        int imgWidth = imageProxy.getWidth();
        int imgHeight = imageProxy.getHeight();

        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                
                Rect box = line.getBoundingBox();
                if (box == null) continue;

                // Guide Filtering: Ensure the line is reasonably centered
                float centerX = box.centerX() / (float) imgWidth;
                float centerY = box.centerY() / (float) imgHeight;

                // Relaxed centering a bit to ensure longer problems aren't clipped
                boolean isCentered = centerX > 0.15 && centerX < 0.85 && centerY > 0.25 && centerY < 0.75;
                if (!isCentered) continue;

                String lineText = line.getText();
                Log.d("VedicScanner", "Raw Line: " + lineText);

                Matcher matcher = pattern.matcher(lineText);
                while (matcher.find()) {
                    String g1 = matcher.group(1);
                    String sep = matcher.group(2);
                    String g2 = matcher.group(3);

                    if (g1 == null || g2 == null || sep == null) continue;

                    try {
                        long valA = Long.parseLong(g1);
                        long valB = Long.parseLong(g2);
                        
                        // Ignore very small numbers or results that overflow int
                        if (valA < 2 || valB < 2) continue; 
                        if (valA > Integer.MAX_VALUE || valB > Integer.MAX_VALUE) continue;
                        
                        int a = (int) valA;
                        int b = (int) valB;

                        // Scoring logic: prefer longer numbers and standard math operators
                        int score = 0;
                        String sepLower = sep.toLowerCase().trim();
                        if (sepLower.equals("x") || sepLower.equals("*") || sepLower.equals("×")) score += 100;
                        score += (g1.length() + g2.length()) * 20; // Increased weight for length
                        
                        if (sep.contains(":") || sep.contains(".")) score -= 50;

                        if (score > bestOverallScore) {
                            bestOverallScore = score;
                            bestA = a;
                            bestB = b;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (bestOverallScore > 20) {
            performSuccessHaptic();
            int finalA = bestA;
            int finalB = bestB;
            int sutraNumber = identifySutra(finalA, finalB);
            runOnUiThread(() -> showResultDialog(finalA, finalB, sutraNumber));
            return true;
        }
        return false;
    }

    private int identifySutra(int a, int b) {
        if (a == b && a % 10 == 5) return 1;
        if ((a > 80 && b > 80 && a < 120 && b < 120) || (a > 850 && b > 850 && a < 1150 && b < 1150)) return 2;
        return 3;
    }

    private void showResultDialog(int a, int b, int sutraNumber) {
        String sutraName = getSutraName(sutraNumber);
        new MaterialAlertDialogBuilder(this)
                .setTitle("Problem Detected")
                .setMessage("Detected " + a + " x " + b + ". Solve using " + sutraName + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(this, SolveProblemActivity.class);
                    intent.putExtra("sutraNumber", sutraNumber);
                    intent.putExtra("a", a);
                    intent.putExtra("b", b);
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private String getSutraName(int id) {
        switch (id) {
            case 1: return "Ekadhikena Purvena";
            case 2: return "Nikhilam Navatashcaramam Dashatah";
            case 3: return "Urdhva-Tiryagbhyam";
            default: return "Vedic Sutra";
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10 && allPermissionsGranted()) startCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private void performHaptic(View view) {
        if (AppSettings.isHapticsEnabled(this)) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    private void performSuccessHaptic() {
        if (AppSettings.isHapticsEnabled(this)) {
            int feedback;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                feedback = HapticFeedbackConstants.CONFIRM;
            } else {
                feedback = HapticFeedbackConstants.LONG_PRESS;
            }
            getWindow().getDecorView().performHapticFeedback(feedback);
        }
    }
}