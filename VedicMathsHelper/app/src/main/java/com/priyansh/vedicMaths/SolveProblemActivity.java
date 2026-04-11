package com.priyansh.vedicMaths;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SolveProblemActivity extends AppCompatActivity {

    private TextView sutraTitle, questionText, solutionText;
    private EditText answerInput;
    private Button checkBtn, backBtn;
    private int a, b, correctAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solve_problem);

        sutraTitle = findViewById(R.id.sutraTitle);
        questionText = findViewById(R.id.questionText);
        solutionText = findViewById(R.id.solutionText);
        answerInput = findViewById(R.id.answerInput);
        checkBtn = findViewById(R.id.checkBtn);
        backBtn = findViewById(R.id.backBtn);

        int sutraNumber = getIntent().getIntExtra("sutraNumber", 1);
        a = getIntent().getIntExtra("a", 0);
        b = getIntent().getIntExtra("b", 0);
        correctAnswer = a * b;

        sutraTitle.setText(getSutraName(sutraNumber));
        questionText.setText(a + " × " + b);

        checkBtn.setOnClickListener(v -> checkAnswer());
        backBtn.setOnClickListener(v -> finish());
    }

    private void checkAnswer() {
        String input = answerInput.getText().toString().trim();
        if (input.isEmpty()) return;

        try {
            int userVal = Integer.parseInt(input);
            if (userVal == correctAnswer) {
                solutionText.setText("✅ Correct!");
                solutionText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            } else {
                solutionText.setText("❌ Try again! Correct is " + correctAnswer);
                solutionText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private String getSutraName(int id) {
        switch (id) {
            case 1: return "Ekadhikena Purvena";
            case 2: return "Nikhilam Navatashcaramam Dashatah";
            case 3: return "Urdhva-Tiryagbhyam";
            default: return "Vedic Sutra";
        }
    }
}