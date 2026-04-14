package com.priyansh.vedicMaths;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChallengeResultActivity extends AppCompatActivity {

    TextView scoreText, xpText, rankText;
    ImageView resultBaba;
    MaterialButton playAgainBtn, homeBtn;

    FirebaseFirestore db;
    FirebaseAuth auth;

    int score;
    int xpEarned;
    int bestStreak;   // NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_result);

        scoreText = findViewById(R.id.scoreText);
        xpText = findViewById(R.id.xpText);
        rankText = findViewById(R.id.rankText);
        resultBaba = findViewById(R.id.resultBaba);
        playAgainBtn = findViewById(R.id.playAgainBtn);
        homeBtn = findViewById(R.id.homeBtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get values from ChallengeActivity
        score = getIntent().getIntExtra("score", 0);
        xpEarned = getIntent().getIntExtra("xpEarned", 0);
        bestStreak = getIntent().getIntExtra("bestStreak", 0);

        // Update user data
        updateUserStats(xpEarned, bestStreak);

        scoreText.setText("Score: " + score);
        xpText.setText("+" + xpEarned + " XP");

        String rank = getRankTitle(score);
        rankText.setText(rank);

        if (score >= 15) {
            resultBaba.setImageResource(R.drawable.baba_happy);
        } else if (score >= 8) {
            resultBaba.setImageResource(R.drawable.baba_neutral);
        } else {
            resultBaba.setImageResource(R.drawable.baba_sad);
        }

        playAgainBtn.setOnClickListener(v -> {
            performHaptic(v);
            performSound();
            startActivity(new Intent(this, ChallengeActivity.class));
            finish();
        });

        homeBtn.setOnClickListener(v -> {
            performHaptic(v);
            performSound();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    // ---------------- FIRESTORE UPDATE ----------------

    private void updateUserStats(int xpEarned, int newBestStreak) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {

            if (documentSnapshot.exists()) {

                Long currentXP = documentSnapshot.getLong("xp");
                Long storedStreak = documentSnapshot.getLong("streak");

                if (currentXP == null) currentXP = 0L;
                if (storedStreak == null) storedStreak = 0L;

                long updatedXP = currentXP + xpEarned;

                // Only update streak if new one is better
                long updatedStreak = storedStreak;
                if (newBestStreak > storedStreak) {
                    updatedStreak = newBestStreak;
                }

                userRef.update(
                        "xp", updatedXP,
                        "streak", updatedStreak
                );
            }
        });
    }

    // ---------------- FEEDBACK ----------------

    private void performHaptic(View view) {
        if (AppSettings.isHapticsEnabled(this)) {
            view.performHapticFeedback(
                    android.view.HapticFeedbackConstants.KEYBOARD_TAP
            );
        }
    }

    private void performSound() {
        if (AppSettings.isSoundEnabled(this)) {
            SoundManager.playButtonClick();
        }
    }

    private String getRankTitle(int score) {

        if (score < 10) {
            return "Novice";
        } else if (score < 25) {
            return "Apprentice";
        } else if (score < 50) {
            return "Scholar";
        } else if (score < 80) {
            return "Acharya";
        } else if (score < 120) {
            return "Master";
        } else {
            return "Vedic Grandmaster";
        }
    }
}