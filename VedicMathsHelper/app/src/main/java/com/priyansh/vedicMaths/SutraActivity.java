package com.priyansh.vedicMaths;

import android.os.Bundle;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class SutraActivity extends AppCompatActivity {

    TextView sutraTitle, questionText, hintText, solutionText;
    TextView problemHeaderText, stepCounterText, progressText;
    EditText answerInput;
    Button checkBtn, nextBtn;
    ImageView feedbackBaba;
    ProgressBar sutraProgressBar;
    LinearLayout progressContainer;

    int sutraNumber;
    int a, b, correctAnswer;
    int step = 0;
    int totalSteps = 0;
    int[] stepAnswers = new int[5];
    
    int questionsSolved = 0;
    static final int QUESTIONS_TO_UNLOCK = 5;
    int userUnlockedTill = 1;

    boolean isDemo = true;
    Random random = new Random();
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sutra);

        db = FirebaseFirestore.getInstance();
        sutraNumber = getIntent().getIntExtra("sutraNumber", 1);

        sutraTitle = findViewById(R.id.sutraTitle);
        problemHeaderText = findViewById(R.id.problemHeaderText);
        stepCounterText = findViewById(R.id.stepCounterText);
        questionText = findViewById(R.id.questionText);
        hintText = findViewById(R.id.hintText);
        solutionText = findViewById(R.id.solutionText);
        answerInput = findViewById(R.id.answerInput);
        checkBtn = findViewById(R.id.checkBtn);
        nextBtn = findViewById(R.id.nextBtn);
        feedbackBaba = findViewById(R.id.feedbackBaba);
        
        progressContainer = findViewById(R.id.progressContainer);
        progressText = findViewById(R.id.progressText);
        sutraProgressBar = findViewById(R.id.sutraProgressBar);

        sutraTitle.setText(getSutraName(sutraNumber));

        loadUserProgress();

        // Check if we came from the Scanner with specific numbers
        int extraA = getIntent().getIntExtra("a", -1);
        int extraB = getIntent().getIntExtra("b", -1);

        if (extraA != -1 && extraB != -1) {
            isDemo = false;
            a = extraA;
            b = extraB;
            setupSteps();
            
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            nextBtn.setText("New Question");
            nextBtn.setVisibility(View.GONE);
            progressContainer.setVisibility(View.VISIBLE);
            loadStep();
        } else {
            generateQuestion();
            startDemo();
        }

        checkBtn.setOnClickListener(v -> {
            performHaptic(v);
            checkAnswer();
        });

        nextBtn.setOnClickListener(v -> {
            performHaptic(v);
            isDemo = false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            nextBtn.setText("New Question");
            nextBtn.setVisibility(View.GONE);
            progressContainer.setVisibility(View.VISIBLE);
            
            generateQuestion();
            step = 0;
            loadStep();
        });
    }

    private void loadUserProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Long obj = doc.getLong("currentSutra");
                        userUnlockedTill = (obj != null) ? obj.intValue() : 1;
                        
                        Long solvedObj = doc.getLong("sutraProgressCount");
                        questionsSolved = (solvedObj != null) ? solvedObj.intValue() : 0;
                        updateProgressBar();
                    }
                });
    }

    private String getSutraName(int id) {
        switch (id) {
            case 1: return "Ekadhikena Purvena";
            case 2: return "Nikhilam Navatashcaramam Dashatah";
            case 3: return "Urdhva-Tiryagbhyam";
            default: return "Sutra " + id;
        }
    }

    private void generateQuestion() {
        switch (sutraNumber) {
            case 1:
                a = (random.nextInt(9) + 1) * 10 + 5;
                b = a;
                break;
            case 2:
                a = 100 - (random.nextInt(20) + 1);
                b = 100 - (random.nextInt(20) + 1);
                break;
            case 3:
                a = random.nextInt(90) + 10;
                b = random.nextInt(90) + 10;
                break;
            default:
                a = random.nextInt(50) + 10;
                b = random.nextInt(50) + 10;
        }
        setupSteps();
    }

    private void setupSteps() {
        correctAnswer = a * b;
        switch (sutraNumber) {
            case 1:
                int x = a / 10;
                stepAnswers[0] = x * (x + 1);
                stepAnswers[1] = correctAnswer;
                totalSteps = 2;
                break;
            case 2:
                int base = (a > 500) ? 1000 : 100;
                stepAnswers[0] = (base - a) * (base - b);
                stepAnswers[1] = a - (base - b);
                stepAnswers[2] = correctAnswer;
                totalSteps = 3;
                break;
            case 3:
                stepAnswers[0] = (a % 10) * (b % 10);
                stepAnswers[1] = (a / 10) * (b % 10) + (b / 10) * (a % 10);
                stepAnswers[2] = (a / 10) * (b / 10);
                stepAnswers[3] = correctAnswer;
                totalSteps = 4;
                break;
            default:
                stepAnswers[0] = correctAnswer;
                totalSteps = 1;
        }
    }

    private void startDemo() {
        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);
        nextBtn.setText("Start Practice");
        nextBtn.setVisibility(View.VISIBLE);
        progressContainer.setVisibility(View.GONE);
        playDemo();
    }

    private void playDemo() {
        solutionText.setAlpha(0f);
        solutionText.animate().alpha(1f).setDuration(400).start();
        feedbackBaba.setImageResource(R.drawable.teaching_baba);
        problemHeaderText.setText(a + " × " + b);
        stepCounterText.setText("Introduction");

        switch (sutraNumber) {
            case 1:
                solutionText.setText("Square numbers ending with 5\n\nExample: 25²\n2 × 3 = 6\nAppend 25 → 625");
                break;
            case 2:
                solutionText.setText("Use base 100 or 1000\n\nSubtract from base\nCross subtract\nMultiply deficits");
                break;
            case 3:
                solutionText.setText("Vertical & Crosswise multiplication\n\nMultiply units\nCross multiply\nMultiply tens");
                break;
            default:
                solutionText.setText("Vedic mathematics simplifies complex calculations using ancient Indian principles.");
        }
    }

    private void loadStep() {
        answerInput.setText("");
        problemHeaderText.setText(a + " × " + b);
        stepCounterText.setText("Step " + (step + 1) + " of " + totalSteps);
        solutionText.setText("");
        feedbackBaba.setImageResource(R.drawable.baba_neutral);
        
        updateProgressBar();

        if (!isDemo) {
            nextBtn.setVisibility(View.GONE);
        }

        switch (sutraNumber) {
            case 1:
                if (step == 0) {
                    questionText.setText("Multiply tens part");
                    hintText.setText("Calculate " + (a/10) + " × " + ((a/10)+1));
                } else {
                    questionText.setText("Final Result");
                    hintText.setText("Append 25 to " + stepAnswers[0]);
                }
                break;
            case 2:
                if (step == 0) {
                    questionText.setText("Product of deficits");
                    int base = (a > 500) ? 1000 : 100;
                    hintText.setText("Multiply (" + base + "-" + a + ") and (" + base + "-" + b + ")");
                } else if (step == 1) {
                    questionText.setText("Calculate left part");
                    int base = (a > 500) ? 1000 : 100;
                    hintText.setText("Subtract " + (base - b) + " from " + a);
                } else {
                    questionText.setText("Combine results");
                    hintText.setText("Merge left and right parts");
                }
                break;
            case 3:
                if (step == 0) {
                    questionText.setText("Multiply units digits");
                    hintText.setText((a%10) + " × " + (b%10));
                } else if (step == 1) {
                    questionText.setText("Cross multiply and add");
                    hintText.setText("(" + (a/10) + "×" + (b%10) + ") + (" + (a%10) + "×" + (b/10) + ")");
                } else if (step == 2) {
                    questionText.setText("Multiply tens digits");
                    hintText.setText((a/10) + " × " + (b/10));
                } else {
                    questionText.setText("Combine with carries");
                    hintText.setText("Finalize your answer");
                }
                break;
            default:
                questionText.setText("Solve the problem");
                hintText.setText("");
        }
    }

    private void updateProgressBar() {
        if (userUnlockedTill > sutraNumber) {
            sutraProgressBar.setProgress(100);
            progressText.setText("Sutra Mastered! Next unlocked.");
            return;
        }

        int progress = (questionsSolved * 100) / QUESTIONS_TO_UNLOCK;
        sutraProgressBar.setProgress(progress);
        progressText.setText("Progress to unlock next: " + progress + "%");
    }

    private void checkAnswer() {
        String input = answerInput.getText().toString();
        if (input.isEmpty()) return;
        try {
            int userVal = Integer.parseInt(input);
            if (userVal == stepAnswers[step]) {
                performSuccessHaptic();
                if (step < totalSteps - 1) {
                    step++;
                    solutionText.setText("✅ Correct!");
                    feedbackBaba.setImageResource(R.drawable.baba_happy);
                    new Handler().postDelayed(this::loadStep, 1000);
                } else {
                    solutionText.setText("🎉 Excellent! Answer: " + correctAnswer);
                    feedbackBaba.setImageResource(R.drawable.baba_encouraged);
                    nextBtn.setVisibility(View.VISIBLE);
                    
                    if (userUnlockedTill <= sutraNumber) {
                        questionsSolved++;
                        updateProgressBar();
                        
                        if (questionsSolved >= QUESTIONS_TO_UNLOCK) {
                            unlockNextSutra();
                        } else {
                            updateSutraProgressInDb();
                        }
                    }
                }
            } else {
                performErrorHaptic();
                solutionText.setText("❌ Not quite. Try again!");
                feedbackBaba.setImageResource(R.drawable.baba_sad);
                nextBtn.setVisibility(View.VISIBLE);
                
                if (userUnlockedTill <= sutraNumber) {
                    questionsSolved = Math.max(0, questionsSolved - 1);
                    updateProgressBar();
                    updateSutraProgressInDb();
                }
            }
        } catch (NumberFormatException e) {
            solutionText.setText("Please enter a valid number");
        }
    }

    private void updateSutraProgressInDb() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("sutraProgressCount", questionsSolved);
        }
    }

    private void unlockNextSutra() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update(
                        "currentSutra", sutraNumber + 1,
                        "sutraProgressCount", 0
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Next Sutra Unlocked!", Toast.LENGTH_LONG).show();
                        userUnlockedTill = sutraNumber + 1;
                        questionsSolved = 0;
                        updateProgressBar();
                    });
        }
    }

    private void performHaptic(View view) {
        if (AppSettings.isHapticsEnabled(this)) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    private void performSuccessHaptic() {
        if (AppSettings.isHapticsEnabled(this)) {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
    }

    private void performErrorHaptic() {
        if (AppSettings.isHapticsEnabled(this)) {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.REJECT);
        }
    }
}