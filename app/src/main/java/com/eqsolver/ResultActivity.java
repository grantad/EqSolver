package com.eqsolver;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.eqsolver.models.Solution;
import com.google.android.material.button.MaterialButton;

/**
 * Result activity for displaying the solved equation with step-by-step solution.
 */
public class ResultActivity extends AppCompatActivity {

    private ImageView capturedImage;
    private TextView extractedEquation;
    private TextView solutionText;
    private MaterialButton btnShare;
    private MaterialButton btnSolveAnother;

    private Solution solution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Enable up navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.result_title);
        }

        initializeViews();
        loadSolution();
        setupButtons();
    }

    /**
     * Initialize UI components.
     */
    private void initializeViews() {
        capturedImage = findViewById(R.id.capturedImage);
        extractedEquation = findViewById(R.id.extractedEquation);
        solutionText = findViewById(R.id.solutionText);
        btnShare = findViewById(R.id.btnShare);
        btnSolveAnother = findViewById(R.id.btnSolveAnother);
    }

    /**
     * Load and display the solution from intent extras.
     */
    private void loadSolution() {
        solution = (Solution) getIntent().getSerializableExtra("solution");

        if (solution == null) {
            finish();
            return;
        }

        // Display extracted equation
        extractedEquation.setText(solution.getExtractedEquation());

        // Display solution
        solutionText.setText(solution.getStepByStepSolution());

        // Display captured image
        if (solution.getImagePath() != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(solution.getImagePath());
            if (bitmap != null) {
                capturedImage.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * Setup button click listeners.
     */
    private void setupButtons() {
        btnShare.setOnClickListener(v -> shareSolution());
        btnSolveAnother.setOnClickListener(v -> solveAnother());
    }

    /**
     * Share the solution using Android's share intent.
     */
    private void shareSolution() {
        if (solution == null) {
            return;
        }

        String shareText = "Equation: " + solution.getExtractedEquation() + "\n\n" +
                "Solution:\n" + solution.getStepByStepSolution() + "\n\n" +
                "Solved with EqSolver";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_solution_title));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_solution_title)));
    }

    /**
     * Return to camera activity to solve another equation.
     */
    private void solveAnother() {
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
