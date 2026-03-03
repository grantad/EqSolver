package com.eqsolver.ml;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

/**
 * Wrapper class for Google ML Kit Text Recognition.
 * Extracts text from images containing mathematical equations.
 */
public class TextRecognizer {

    private static final String TAG = "TextRecognizer";
    private final com.google.mlkit.vision.text.TextRecognizer recognizer;

    public TextRecognizer() {
        // Initialize ML Kit text recognizer with Latin script
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    /**
     * Recognizes text from a bitmap image.
     * Applies preprocessing to improve OCR accuracy.
     *
     * @param bitmap The image containing text to recognize
     * @return Task that will contain the recognized text
     */
    public Task<Text> recognizeText(Bitmap bitmap) {
        // Preprocess the image for better OCR results
        Bitmap preprocessed = preprocessImage(bitmap);

        InputImage image = InputImage.fromBitmap(preprocessed, 0);
        return recognizer.process(image);
    }

    /**
     * Preprocesses the image to improve OCR accuracy.
     * Increases contrast and sharpness for better text detection.
     *
     * @param original The original bitmap
     * @return Preprocessed bitmap
     */
    private Bitmap preprocessImage(Bitmap original) {
        try {
            // Create a mutable copy
            Bitmap processed = original.copy(Bitmap.Config.ARGB_8888, true);

            // Increase contrast using ColorMatrix
            Canvas canvas = new Canvas(processed);
            Paint paint = new Paint();

            // Contrast adjustment: values > 1 increase contrast
            float contrast = 1.5f;
            float translate = (-.5f * contrast + .5f) * 255.f;

            ColorMatrix colorMatrix = new ColorMatrix(new float[] {
                contrast, 0, 0, 0, translate,
                0, contrast, 0, 0, translate,
                0, 0, contrast, 0, translate,
                0, 0, 0, 1, 0
            });

            paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            canvas.drawBitmap(processed, 0, 0, paint);

            Log.d(TAG, "Image preprocessing applied (contrast enhancement)");
            return processed;
        } catch (Exception e) {
            Log.e(TAG, "Error preprocessing image, using original", e);
            return original;
        }
    }

    /**
     * Recognizes text from an image URI.
     *
     * @param imageUri The URI of the image containing text
     * @param context Context needed to access the image
     * @return Task that will contain the recognized text
     */
    public Task<Text> recognizeText(Uri imageUri, android.content.Context context) throws IOException {
        InputImage image = InputImage.fromFilePath(context, imageUri);
        return recognizer.process(image);
    }

    /**
     * Extracts and cleans the text from ML Kit recognition result.
     * Attempts to identify and extract the mathematical equation.
     *
     * @param result The recognition result from ML Kit
     * @return Cleaned equation text
     */
    public String extractEquation(Text result) {
        String fullText = result.getText();

        Log.d(TAG, "Raw text from ML Kit: '" + fullText + "'");

        if (fullText.isEmpty()) {
            Log.w(TAG, "No text detected in image");
            return "";
        }

        // Try to extract just the equation from all detected text
        String equation = findEquation(fullText);

        Log.d(TAG, "Extracted equation: '" + equation + "'");

        return equation;
    }

    /**
     * Finds and extracts the mathematical equation from text.
     * Filters out UI elements and non-mathematical text.
     *
     * @param text The full text detected by ML Kit
     * @return The extracted equation, or cleaned full text if no equation found
     */
    private String findEquation(String text) {
        // Split into lines to process individually
        String[] lines = text.split("\\n");

        String bestMatch = "";
        int highestScore = 0;

        for (String line : lines) {
            String cleaned = line.trim();
            int score = scoreEquationLikelihood(cleaned);

            Log.d(TAG, "Line: '" + cleaned + "' score: " + score);

            if (score > highestScore) {
                highestScore = score;
                bestMatch = cleaned;
            }
        }

        // If we found a good equation candidate, return it
        if (highestScore > 0) {
            return bestMatch;
        }

        // Otherwise, clean up the full text
        return text.trim().replaceAll("\\s+", " ").replaceAll("\\n+", " ");
    }

    /**
     * Scores how likely a line of text is to be a mathematical equation.
     *
     * @param line The line of text to score
     * @return Score (higher = more likely to be an equation)
     */
    private int scoreEquationLikelihood(String line) {
        int score = 0;

        // Filter out common UI patterns
        if (line.matches(".*(?i)(notes|days|table|PM|AM|previous|new note).*")) {
            return 0; // Definitely not an equation
        }

        // Must contain at least one variable or number
        if (!line.matches(".*[a-zA-Z0-9].*")) {
            return 0;
        }

        // High score for mathematical operators
        if (line.contains("=")) score += 10;
        if (line.contains("+")) score += 5;
        if (line.contains("-")) score += 5;
        if (line.contains("*") || line.contains("×")) score += 5;
        if (line.contains("/") || line.contains("÷")) score += 5;
        if (line.matches(".*[0-9].*")) score += 3; // Contains numbers

        // Contains common math variables
        if (line.matches(".*[xyzXYZ].*")) score += 3;

        // Penalize if it's too long (likely contains UI text)
        if (line.length() > 50) score -= 5;

        // Penalize if it contains too many words (not equation-like)
        int wordCount = line.split("\\s+").length;
        if (wordCount > 5) score -= wordCount;

        return Math.max(0, score);
    }

    /**
     * Closes the text recognizer and releases resources.
     */
    public void close() {
        recognizer.close();
    }
}
