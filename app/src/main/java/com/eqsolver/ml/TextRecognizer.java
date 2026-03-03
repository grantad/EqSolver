package com.eqsolver.ml;

import android.graphics.Bitmap;
import android.net.Uri;

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

    private final com.google.mlkit.vision.text.TextRecognizer recognizer;

    public TextRecognizer() {
        // Initialize ML Kit text recognizer with Latin script
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    /**
     * Recognizes text from a bitmap image.
     *
     * @param bitmap The image containing text to recognize
     * @return Task that will contain the recognized text
     */
    public Task<Text> recognizeText(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        return recognizer.process(image);
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

        if (fullText.isEmpty()) {
            return "";
        }

        // Clean up the text: remove extra whitespace, newlines
        String cleaned = fullText.trim()
                .replaceAll("\\s+", " ")  // Replace multiple spaces with single space
                .replaceAll("\\n+", " ");  // Replace newlines with space

        // If the text contains multiple lines, try to identify the equation
        // For now, we'll return the full cleaned text
        // Future enhancement: more sophisticated equation detection
        return cleaned;
    }

    /**
     * Closes the text recognizer and releases resources.
     */
    public void close() {
        recognizer.close();
    }
}
