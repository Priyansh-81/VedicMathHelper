package com.priyansh.vedicMaths;

import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;
import java.util.Random;

public class SutraActivity extends AppCompatActivity {

    TextView sutraTitle, questionText, hintText, solutionText, progressText;
    EditText answerInput;
    Button checkBtn, nextBtn;
    ProgressBar levelProgressBar;

    int sutraNumber;
    int a, b, correctAnswer;
    int currentStep = 0;
    int maxSteps = 1;
    int intermediateTarget = 0;

    boolean isDemo = true;
    Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sutra);

        sutraNumber = getIntent().getIntExtra("sutraNumber", 1);

        sutraTitle = findViewById(R.id.sutraTitle);
        questionText = findViewById(R.id.questionText);
        hintText = findViewById(R.id.hintText);
        solutionText = findViewById(R.id.solutionText);
        progressText = findViewById(R.id.progressText);
        answerInput = findViewById(R.id.answerInput);
        checkBtn = findViewById(R.id.checkBtn);
        nextBtn = findViewById(R.id.nextBtn);
        levelProgressBar = findViewById(R.id.levelProgressBar);

        sutraTitle.setText(getSutraName(sutraNumber));

        loadInitialProgress();
        generateQuestion();
        startDemo();

        checkBtn.setOnClickListener(v -> {
            performHaptic(v);
            checkAnswer();
        });

        nextBtn.setOnClickListener(v -> {
            performHaptic(v);
            if (isDemo) {
                isDemo = false;
                answerInput.setVisibility(View.VISIBLE);
                checkBtn.setVisibility(View.VISIBLE);
                nextBtn.setText("Next Level");
                nextBtn.setVisibility(View.GONE);
                generateQuestion();
                currentStep = 0;
                loadStep();
            } else {
                generateQuestion();
                currentStep = 0;
                loadStep();
                nextBtn.setVisibility(View.GONE);
                checkBtn.setVisibility(View.VISIBLE);
                answerInput.setVisibility(View.VISIBLE);
            }
        });
    }

    private void performHaptic(View view) {
        if (AppSettings.isHapticsEnabled(this)) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
        }
    }

    private String getSutraName(int id) {
        switch (id) {
            case 1: return "Ekadhikena Purvena";
            case 2: return "Nikhilam Navatashcaramam Dashatah";
            case 3: return "Urdhva-Tiryagbhyam";
            case 4: return "Paravartya Yojayet";
            case 5: return "Shunyam Saamyasamuccaye";
            case 6: return "Anurupyena";
            case 7: return "Sankalana-Vyavakalanabhyam";
            case 8: return "Puranapuranabhyam";
            case 9: return "Chalana-Kalanabhyam";
            case 10: return "Yavadunam";
            case 11: return "Vyashtisamanstih";
            case 12: return "Shesanyankena Charamena";
            case 13: return "Sopantyadvayamantyam";
            case 14: return "Ekanyunena Purvena";
            case 15: return "Gunakasamuccayah";
            case 16: return "Gunita Samuccayah";
            default: return "Sutra " + id;
        }
    }

    private void generateQuestion() {
        answerInput.setText("");
        solutionText.setText("");
        
        switch (sutraNumber) {
            case 1: // Ekadhikena (square ending 5)
                int tens = random.nextInt(9) + 1;
                a = tens * 10 + 5;
                b = a;
                maxSteps = 2;
                break;
            case 2: // Nikhilam (near 100)
                a = 100 - (random.nextInt(12) + 1);
                b = 100 - (random.nextInt(12) + 1);
                maxSteps = 3;
                break;
            case 3: // Urdhva (2x2)
                a = random.nextInt(40) + 11;
                b = random.nextInt(40) + 11;
                maxSteps = 3;
                break;
            case 14: // Ekanyunena (x99)
                a = random.nextInt(89) + 10;
                b = 99;
                maxSteps = 2;
                break;
            default:
                a = random.nextInt(20) + 5;
                b = random.nextInt(20) + 5;
                maxSteps = 1;
        }
        correctAnswer = a * b;
    }

    private void startDemo() {
        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setText("Start Practice");
        playDemo();
    }

    private void playDemo() {
        solutionText.setAlpha(0f);
        solutionText.animate().alpha(1f).setDuration(600).start();

        switch (sutraNumber) {
            case 1:
                solutionText.setText("Multiply the first digit by 'one more than itself' and append 25.\n\nExample: 35²\n3 × 4 = 12\nResult: 1225");
                break;
            case 2:
                solutionText.setText("Find the deficiency from 100.\n\nExample: 98 × 97\nDeficiencies: -2, -3\n98-3 = 95\n2×3 = 06\nResult: 9506");
                break;
            case 3:
                solutionText.setText("Vertical and Crosswise multiplication.\n\n1. Multiply units\n2. Cross multiply and add\n3. Multiply tens");
                break;
            case 14:
                solutionText.setText("Subtract 1 from the number, then find the complement of the new number.\n\nExample: 42 × 99\n42-1 = 41\n99-41 = 58\nResult: 4158");
                break;
            default:
                solutionText.setText("Vedic Method practice for Sutra " + sutraNumber);
        }
    }

    private void loadStep() {
        answerInput.setText("");
        questionText.setText(a + ( (a==b && sutraNumber==1) ? "²" : " × " + b));
        
        switch (sutraNumber) {
            case 1: // Ekadhikena
                if (currentStep == 0) {
                    int tens = a / 10;
                    hintText.setText("Step 1: Multiply tens digit (" + tens + ") by " + (tens + 1));
                    intermediateTarget = tens * (tens + 1);
                } else {
                    hintText.setText("Step 2: Append 25 to " + intermediateTarget);
                    intermediateTarget = correctAnswer;
                }
                break;
            case 2: // Nikhilam
                int defA = 100 - a;
                int defB = 100 - b;
                if (currentStep == 0) {
                    hintText.setText("Step 1: Multiply deficiencies (" + defA + " × " + defB + ")");
                    intermediateTarget = defA * defB;
                } else if (currentStep == 1) {
                    hintText.setText("Step 2: Cross subtract (" + a + " - " + defB + ")");
                    intermediateTarget = a - defB;
                } else {
                    hintText.setText("Step 3: Combine parts (" + (a - defB) + " | " + String.format("%02d", (defA * defB)) + ")");
                    intermediateTarget = correctAnswer;
                }
                break;
            case 14: // Ekanyunena
                if (currentStep == 0) {
                    hintText.setText("Step 1: Subtract 1 from " + a);
                    intermediateTarget = a - 1;
                } else {
                    hintText.setText("Step 2: Find complement of " + (a-1) + " from 99");
                    intermediateTarget = correctAnswer;
                }
                break;
            default:
                hintText.setText("Calculate the final product");
                intermediateTarget = correctAnswer;
        }
    }

    private void checkAnswer() {
        String input = answerInput.getText().toString().trim();
        if (input.isEmpty()) return;

        try {
            int userAns = Integer.parseInt(input);
            if (userAns == intermediateTarget) {
                hideKeyboard();
                if (currentStep < maxSteps - 1) {
                    solutionText.setText("✅ Correct! Now for the next step.");
                    solutionText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    currentStep++;
                    loadStep();
                } else {
                    solutionText.setText("✨ Brilliant! Final Answer Correct.");
                    solutionText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    checkBtn.setVisibility(View.GONE);
                    answerInput.setVisibility(View.GONE);
                    nextBtn.setVisibility(View.VISIBLE);
                    nextBtn.setText("Next Problem");
                    
                    updateUserProgress();
                }
            } else {
                solutionText.setText("❌ Try again! Focus on the hint.");
                solutionText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        } catch (NumberFormatException e) {
            solutionText.setText("Please enter a valid number");
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void loadInitialProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        long currentSutra = doc.getLong("currentSutra") != null ? doc.getLong("currentSutra") : 1;
                        if (sutraNumber == currentSutra) {
                            int progress = doc.getLong("levelProgress") != null ? doc.getLong("levelProgress").intValue() : 0;
                            updateProgressUI(progress);
                        } else if (sutraNumber < currentSutra) {
                            updateProgressUI(100);
                        } else {
                            updateProgressUI(0);
                        }
                    }
                });
    }

    private void updateProgressUI(int progress) {
        levelProgressBar.setProgress(progress);
        progressText.setText(progress + "% complete");
    }

    private void showHappyToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_happy, findViewById(android.R.id.content), false);

        TextView text = layout.findViewById(R.id.toastMessage);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    private void updateUserProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        long currentSutra = doc.getLong("currentSutra") != null ? doc.getLong("currentSutra") : 1;
                        
                        // Only update progress if playing the currently active (highest) level
                        if (sutraNumber == currentSutra) {
                            int progress = doc.getLong("levelProgress") != null ? doc.getLong("levelProgress").intValue() : 0;
                            progress += 20; // 5 problems to complete level

                            if (progress >= 100) {
                                db.collection("users").document(user.getUid())
                                        .update("currentSutra", FieldValue.increment(1),
                                                "levelProgress", 0,
                                                "xp", FieldValue.increment(50));
                                updateProgressUI(100);
                                showHappyToast("Sutra Mastered! +50 XP");
                            } else {
                                db.collection("users").document(user.getUid())
                                        .update("levelProgress", progress,
                                                "xp", FieldValue.increment(10));
                                updateProgressUI(progress);
                                // Small toast for problem completion
                                Toast.makeText(this, "Great job! +10 XP", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
