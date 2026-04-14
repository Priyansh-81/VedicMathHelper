package com.priyansh.vedicMaths;

import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class sutra2Activity extends AppCompatActivity {
    TextView questionText, hintText, solutionText, scoreText, sutraTitle;
    EditText answerInput;
    Button checkBtn, nextBtn;
    ImageButton homeBtn;
    ProgressBar progressBar;

    int a, correctAnswer, teachStep = 0, score = 0, practice = 0;
    boolean learning = true;
    Handler h = new Handler();
    Random r = new Random();
    
    FirebaseFirestore db;

    String[] teach = {
            "✨ Nikhilam means: All from 9 and last from 10",
            "🧠 Used for fast subtraction from 10, 100, 1000...",
            "📘 Example: 100 - 37",
            "Step 1: 9 - 3 = 6",
            "Step 2: 10 - 7 = 3",
            "🎉 Answer = 63",
            "🚀 Ready for Practice!"
    };

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.activity_sutra2);
        
        db = FirebaseFirestore.getInstance();

        sutraTitle = findViewById(R.id.sutraTitle);
        questionText = findViewById(R.id.questionText);
        hintText = findViewById(R.id.hintText);
        solutionText = findViewById(R.id.solutionText);
        answerInput = findViewById(R.id.answerInput);
        checkBtn = findViewById(R.id.checkBtn);
        nextBtn = findViewById(R.id.nextBtn);
        homeBtn = findViewById(R.id.homeBtn);
        scoreText = findViewById(R.id.scoreText);
        progressBar = findViewById(R.id.progressBar);

        sutraTitle.setText("Nikhilam Navatashcaramam");
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
                String btnText = nextBtn.getText().toString();
                if (btnText.equalsIgnoreCase("Next Sutra")) {
                    startActivity(new Intent(this, sutra3.class));
                    finish();
                    return;
                }
                
                updateProgress(3);
                
                questionText.setText("🏆 Sutra Mastered!");
                hintText.setText("You've mastered Nikhilam!");
                solutionText.setText("What would you like to do next?");
                
                answerInput.setVisibility(View.GONE);
                checkBtn.setVisibility(View.VISIBLE);
                checkBtn.setText("Home");
                nextBtn.setText("Next Sutra");
                
                checkBtn.setOnClickListener(v -> finish());
                return;
            }
            generateQ();
        }
    }

    private void updateProgress(int nextSutra) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    long current = doc.contains("currentSutra") ? doc.getLong("currentSutra") : 1;
                    if (nextSutra > current) {
                        db.collection("users").document(user.getUid())
                                .update("currentSutra", nextSutra);
                    }
                });
    }

    void showTeach() {
        if (teachStep >= teach.length) {
            learning = false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            nextBtn.setText("Next Question");
            generateQ();
            return;
        }

        progressBar.setProgress((teachStep + 1) * 14);
        questionText.setText("Sutra 2 Learning Mode");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);
        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if (teachStep == 2) animate(new String[]{"100 - 37", "9 from first digit", "10 from last digit"}, 800);
        if (teachStep == 5) animate(new String[]{"63", "✅ Fast subtraction!"}, 900);
    }

    void generateQ() {
        a = r.nextInt(89) + 10;
        correctAnswer = 100 - a;
        questionText.setText("100 - " + a);
        hintText.setText("All from 9, last from 10");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
        nextBtn.setText("Next ➜");
    }

    void check() {
        if (answerInput.getText().toString().isEmpty()) return;

        try {
            int user = Integer.parseInt(answerInput.getText().toString().trim());
            if (user == correctAnswer) {
                if (practice < 3) {
                    score += 10;
                    practice++;
                }
                solutionText.setText("✅ Correct!");
                solutionText.setTextColor(Color.GREEN);
            } else {
                solutionText.setText("❌ Correct: " + correctAnswer);
                solutionText.setTextColor(Color.RED);
            }
            scoreText.setText("⭐ Score: " + score + " | ✔ " + practice + "/3");
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    void animate(String[] arr, int d) {
        for (int i = 0; i < arr.length; i++) {
            final String t = arr[i];
            h.postDelayed(() -> {
                solutionText.setText(t);
                fade(solutionText);
            }, (long) i * d);
        }
    }

    void fade(View v) {
        AlphaAnimation a = new AlphaAnimation(0f, 1f);
        a.setDuration(500);
        v.startAnimation(a);
    }
}
