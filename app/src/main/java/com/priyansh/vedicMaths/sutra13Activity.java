// Sutra13Activity.java
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

import java.util.Random;

public class sutra13Activity extends AppCompatActivity {

    TextView questionText, hintText, solutionText, scoreText;
    EditText answerInput;
    MaterialButton checkBtn, nextBtn;
    ImageButton homeBtn;
    ProgressBar progressBar;

    int teachStep = 0, score = 0, practice = 0;
    boolean learning = true;

    int num, correctAnswer;

    Handler handler = new Handler();
    Random random = new Random();

    String[] teach = {
            "✨ Sopantyadvayamantyam",
            "🧠 Twice the penultimate and the last",
            "📘 Example: 23²",
            "2² = 4",
            "2×2×3 = 12",
            "3² = 9",
            "🎉 Answer = 529",
            "🚀 Ready for Practice!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sutra13);

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
        questionText.setText("🏆 Sutra 13 Complete!");
        hintText.setText("You solved 3 square questions!");
        solutionText.setText("Great pattern skills!");
        
        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.VISIBLE);
        checkBtn.setText("Go Home");
        checkBtn.setOnClickListener(v -> finish());

        nextBtn.setText("Next Sutra");
        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, sutra14Activity.class);
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

        questionText.setText("Sutra 13 Learning");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);

        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if (teachStep == 2) {
            animate(new String[]{
                    "23²",
                    "4 | 12 | 9",
                    "Combine = 529"
            }, 900);
        }
    }

    void generateQuestion() {
        int tens = random.nextInt(8) + 1;
        int ones = random.nextInt(9) + 1;

        num = tens * 10 + ones;
        correctAnswer = num * num;

        questionText.setText("Find " + num + "²");
        hintText.setText("Use a² + 2ab + b²");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
    }

    void check() {
        String input = answerInput.getText().toString().trim();

        if (input.isEmpty()) return;

        int user;

        try {
            user = Integer.parseInt(input);
        } catch (Exception e) {
            solutionText.setText("❌ Enter a valid number");
            solutionText.setTextColor(Color.RED);
            return;
        }

        if (user == correctAnswer) {
            score += 10;
            practice++;
            solutionText.setText("✅ Correct!");
            solutionText.setTextColor(Color.GREEN);
        } else {
            solutionText.setText("❌ Correct: " + correctAnswer);
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