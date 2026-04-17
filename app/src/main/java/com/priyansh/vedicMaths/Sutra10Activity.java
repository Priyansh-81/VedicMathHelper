// Sutra10Activity.java
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

public class Sutra10Activity extends AppCompatActivity {

    TextView questionText, hintText, solutionText, scoreText;
    EditText answerInput;
    MaterialButton checkBtn, nextBtn;
    ImageButton homeBtn;
    ProgressBar progressBar;

    int teachStep = 0, score = 0, practice = 0;
    boolean learning = true;

    int a, b, correctAnswer;

    Handler handler = new Handler();
    Random random = new Random();

    String[] teach = {
            "✨ Yavadunam = Whatever the deficiency",
            "🧠 Used for multiplying numbers near a base like 10, 100, 1000",
            "📘 Example: 97 × 96",
            "Deficiencies: -3 and -4",
            "Cross subtract: 97 - 4 = 93",
            "Multiply deficits: 3 × 4 = 12",
            "🎉 Answer = 9312",
            "🚀 Ready for Practice!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sutra10);

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
        questionText.setText("🏆 Sutra 10 Complete!");
        hintText.setText("You solved 3 base questions!");
        solutionText.setText("Excellent speed maths!");
        
        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.VISIBLE);
        checkBtn.setText("Go Home");
        checkBtn.setOnClickListener(v -> finish());

        nextBtn.setText("Next Sutra");
        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, Sutra11Activity.class);
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

        questionText.setText("Sutra 10 Learning");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);

        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if (teachStep == 2) {
            animate(new String[]{
                    "97 × 96",
                    "-3 and -4",
                    "Cross = 93",
                    "3×4 = 12",
                    "9312"
            }, 850);
        }
    }

    void generateQuestion() {
        a = 90 + random.nextInt(9); // 90-98
        b = 90 + random.nextInt(9);

        int d1 = 100 - a;
        int d2 = 100 - b;

        correctAnswer = (a - d2) * 100 + (d1 * d2);

        questionText.setText(a + " × " + b);
        hintText.setText("Use base 100");
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