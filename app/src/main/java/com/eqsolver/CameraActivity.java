package com.eqsolver;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.eqsolver.api.ClaudeApiClient;
import com.eqsolver.ml.TextRecognizer;
import com.eqsolver.models.Solution;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.text.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

/**
 * Camera activity for capturing images of mathematical equations.
 * Integrates CameraX, ML Kit text recognition, and Claude API.
 */
public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private PreviewView cameraPreview;
    private FloatingActionButton btnCapture;
    private FrameLayout loadingOverlay;
    private TextView loadingText;

    private ImageCapture imageCapture;
    private TextRecognizer textRecognizer;
    private ClaudeApiClient claudeApiClient;

    private String currentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initializeViews();
        initializeComponents();
        startCamera();
    }

    /**
     * Initialize UI components.
     */
    private void initializeViews() {
        cameraPreview = findViewById(R.id.cameraPreview);
        btnCapture = findViewById(R.id.btnCapture);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        loadingText = findViewById(R.id.loadingText);

        btnCapture.setOnClickListener(v -> captureImage());
    }

    /**
     * Initialize ML and API components.
     */
    private void initializeComponents() {
        textRecognizer = new TextRecognizer();
        claudeApiClient = new ClaudeApiClient();

        // Check if API key is configured
        if (BuildConfig.CLAUDE_API_KEY == null || BuildConfig.CLAUDE_API_KEY.isEmpty()) {
            Toast.makeText(this, R.string.error_no_api_key, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Starts the camera using CameraX.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(this, R.string.error_camera_failed, Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Binds camera use cases (preview and image capture).
     */
    private void bindCameraUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build();

        // Select back camera
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            // Unbind all use cases before rebinding
            cameraProvider.unbindAll();

            // Bind use cases to camera
            Camera camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture);

        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            Toast.makeText(this, R.string.error_camera_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Captures an image from the camera.
     */
    private void captureImage() {
        if (imageCapture == null) {
            return;
        }

        showLoading(getString(R.string.processing_image));
        btnCapture.setEnabled(false);

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        processImage(imageProxy);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Image capture failed", exception);
                        hideLoading();
                        btnCapture.setEnabled(true);
                        Toast.makeText(CameraActivity.this,
                                R.string.error_capture_failed, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Processes the captured image: saves it, extracts text, and solves equation.
     */
    private void processImage(@NonNull ImageProxy imageProxy) {
        // Convert ImageProxy to Bitmap
        Bitmap bitmap = imageProxyToBitmap(imageProxy);
        imageProxy.close();

        if (bitmap == null) {
            hideLoading();
            btnCapture.setEnabled(true);
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            return;
        }

        // Save bitmap to file
        currentImagePath = saveBitmapToFile(bitmap);

        // Extract text from image
        showLoading(getString(R.string.recognizing_text));
        textRecognizer.recognizeText(bitmap)
                .addOnSuccessListener(this::onTextRecognized)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed", e);
                    hideLoading();
                    btnCapture.setEnabled(true);
                    Toast.makeText(this, R.string.error_no_text_found, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Called when text recognition succeeds.
     */
    private void onTextRecognized(Text text) {
        String extractedEquation = textRecognizer.extractEquation(text);

        if (extractedEquation.isEmpty()) {
            hideLoading();
            btnCapture.setEnabled(true);
            Toast.makeText(this, R.string.error_no_text_found, Toast.LENGTH_SHORT).show();
            return;
        }

        // Solve equation using Claude API
        showLoading(getString(R.string.solving_equation));
        claudeApiClient.solveEquation(extractedEquation, new ClaudeApiClient.SolutionCallback() {
            @Override
            public void onSuccess(String solution) {
                hideLoading();
                btnCapture.setEnabled(true);
                showResult(extractedEquation, solution);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "API error: " + error);
                hideLoading();
                btnCapture.setEnabled(true);
                Toast.makeText(CameraActivity.this, R.string.error_api_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Shows the result activity with the solution.
     */
    private void showResult(String equation, String solution) {
        Solution solutionObj = new Solution(equation, solution, currentImagePath);

        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("solution", solutionObj);
        startActivity(intent);
    }

    /**
     * Converts ImageProxy to Bitmap.
     */
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Saves a bitmap to internal storage and returns the file path.
     */
    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(getCacheDir(), "captured_equation_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            return null;
        }
    }

    /**
     * Shows loading overlay with message.
     */
    private void showLoading(String message) {
        loadingText.setText(message);
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    /**
     * Hides loading overlay.
     */
    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textRecognizer != null) {
            textRecognizer.close();
        }
        if (claudeApiClient != null) {
            claudeApiClient.shutdown();
        }
    }
}
