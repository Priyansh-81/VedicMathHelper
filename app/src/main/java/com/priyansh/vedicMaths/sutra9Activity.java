// Sutra9Activity.java
package com.priyansh.vedicMaths;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.Random;

public class sutra9Activity extends AppCompatActivity {

    TextView questionText, hintText, solutionText, scoreText;
    EditText answerInput;
    MaterialButton checkBtn, nextBtn;
    ImageButton homeBtn;
    ProgressBar progressBar;

    int teachStep = 0, score = 0, practice = 0;
    boolean learning = true;

    double currentNumber;
    double correctAnswer;

    Handler handler = new Handler();
    Random random = new Random();

    String[] teach = {
            "✨ Chalana Kalanabhyam = By Motion & Calculation",
            "🧠 Used for better approximations step by step",
            "📘 Example: Find √5",
            "Start with guess = 2",
            "Improve guess → 2.25",
            "Improve again → 2.236",
            "🎉 Final Answer ≈ 2.236",
            "🚀 Ready for Practice!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sutra9);

        questionText = findViewById(R.id.questionText);
        hintText = findViewById(R.id.hintText);
        solutionText = findViewById(R.id.solutionText);
        scoreText = findViewById(R.id.scoreText);

        answerInput = findViewById(R.id.answerInput);

        checkBtn = findViewById(R.id.checkBtn);
        nextBtn = findViewById(R.id.nextBtn);
        homeBtn = findViewById(R.id.homeBtn);

        progressBar = findViewById(R.id.progressBar);

        showTeach();

        nextBtn.setOnClickListener(v -> nextFlow());
        checkBtn.setOnClickListener(v -> check());
        homeBtn.setOnClickListener(v -> finish());
    }

    void nextFlow() {
        if (learning) {
            teachStep++;
            showTeach();
        } else {
            if (practice >= 3) {
                showCompletionOptions();
                return;
            }
            generateQuestion();
        }
    }

    void showCompletionOptions() {
        questionText.setText("🏆 Sutra 9 Complete!");
        hintText.setText("You solved 3 approximation questions!");
        solutionText.setText("Great numerical skills!");
        
        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.VISIBLE);
        checkBtn.setText("Go Home");
        checkBtn.setOnClickListener(v -> finish());

        nextBtn.setText("Next Sutra");
        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Sutra10Activity.class);
            startActivity(intent);
            finish();
        });
    }

    void showTeach() {
        if (teachStep >= teach.length) {
            learning = false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            checkBtn.setText("Check");
            generateQuestion();
            return;
        }

        progressBar.setProgress((teachStep + 1) * 12);

        questionText.setText("Sutra 9 Learning");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);

        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if (teachStep == 2) {
            animate(new String[]{
                    "√5",
                    "Guess = 2",
                    "Better = 2.25",
                    "Best ≈ 2.236"
            }, 900);
        }
    }

    void generateQuestion() {
        int[] nums = {2, 3, 5, 7, 10, 11};
        currentNumber = nums[random.nextInt(nums.length)];
        correctAnswer = Math.sqrt(currentNumber);

        questionText.setText("Approximate √" + (int) currentNumber);
        hintText.setText("Round to 2 decimal places");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
    }

    void check() {
        String input = answerInput.getText().toString().trim();

        if (input.isEmpty()) return;

        double user;

        try {
            user = Double.parseDouble(input);
        } catch (Exception e) {
            solutionText.setText("❌ Enter a valid decimal number");
            solutionText.setTextColor(Color.RED);
            return;
        }

        if (Math.abs(user - correctAnswer) <= 0.05) {
            score += 10;
            practice++;
            solutionText.setText("✅ Correct!\nAnswer ≈ " +
                    String.format(Locale.getDefault(),"%.2f", correctAnswer));
            solutionText.setTextColor(Color.GREEN);
        } else {
            solutionText.setText("❌ Correct ≈ " +
                    String.format(Locale.getDefault(),"%.2f", correctAnswer));
            solutionText.setTextColor(Color.RED);
        }

        scoreText.setText("⭐ Score: " + score + " | ✔ " + practice + "/3");
    }

    void animate(String[] arr, int delay) {
        for (int i = 0; i < arr.length; i++) {
            final String text = arr[i];
            handler.postDelayed(() -> {
                solutionText.setText(text);
                fade(solutionText);
            }, (long) i * delay);
        }
    }

    void fade(View view) {
        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
        animation.setDuration(500);
        view.startAnimation(animation);
    }
}