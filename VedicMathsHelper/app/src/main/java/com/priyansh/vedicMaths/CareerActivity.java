package com.priyansh.vedicMaths;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class CareerActivity extends AppCompatActivity {

    View dialContainer;

    ImageView teachingBaba;
    MaterialButton[] sutraButtons = new MaterialButton[16];
    MaterialButton startBtn;

    TextView sutraTitle, sutraDesc, progressText, xpBadge;
    ProgressBar levelProgressBar;

    FirebaseFirestore db;
    ListenerRegistration progressListener;

    float lastAngle = 0f;
    float currentRotation = 0f;

    int selectedIndex = 0;
    int unlockedTill = 1;
    int currentLevelProgress = 0;

    final float STEP_ANGLE = 22.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career);

        db = FirebaseFirestore.getInstance();

        dialContainer = findViewById(R.id.dialContainer);
        sutraTitle = findViewById(R.id.sutraTitle);
        sutraDesc = findViewById(R.id.sutraDesc);
        progressText = findViewById(R.id.progressText);
        xpBadge = findViewById(R.id.xpBadge);
        levelProgressBar = findViewById(R.id.levelProgressBar);
        startBtn = findViewById(R.id.startBtn);
        teachingBaba = findViewById(R.id.teaching_baba);

        SoundManager.init(this);
        startBabaBreathing();

        bindButtons();
        loadUserProgress();

        dialContainer.post(() -> {
            dialContainer.setPivotX(dialContainer.getWidth() / 2f);
            dialContainer.setPivotY(dialContainer.getHeight() / 2f);

            // Start with Sutra 1 on LEFT
            currentRotation = 180f;
            rotateDial(currentRotation);
        });

        setupDialTouch();
        animateDialEntry();

        startBtn.setOnClickListener(v -> {
            if (selectedIndex + 1 <= unlockedTill) {
                performHaptic(v);
                performButtonSound();
                Intent intent = new Intent(this, SutraActivity.class);
                intent.putExtra("sutraNumber", selectedIndex + 1);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningProgress();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressListener != null) {
            progressListener.remove();
        }
    }

    private void startListeningProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        progressListener = db.collection("users")
                .document(user.getUid())
                .addSnapshotListener((doc, error) -> {
                    if (error != null) return;
                    if (doc != null && doc.exists()) {
                        Long xpObj = doc.getLong("xp");
                        long xp = (xpObj != null) ? xpObj : 0;
                        xpBadge.setText("XP: " + xp);

                        // Use currentSutra for unlocking if it exists
                        Long sutraObj = doc.getLong("currentSutra");
                        
                        // Otherwise fallback to XP-based unlocking (1 level per 100 XP)
                        if (sutraObj == null) {
                            unlockedTill = (int) (xp / 100) + 1;
                        } else {
                            unlockedTill = sutraObj.intValue();
                        }

                        if (unlockedTill > 16) unlockedTill = 16;
                        
                        Long progObj = doc.getLong("levelProgress");
                        currentLevelProgress = (progObj != null) ? progObj.intValue() : 0;
                        
                        updateSelection(selectedIndex);
                    }
                });
    }

    private void bindButtons() {
        for (int i = 0; i < 16; i++) {
            int id = getResources().getIdentifier(
                    "sutra" + (i + 1), "id", getPackageName());
            sutraButtons[i] = findViewById(id);
        }
    }

    private void loadUserProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    Long xpObj = doc.getLong("xp");
                    long xp = (xpObj != null) ? xpObj : 0;
                    xpBadge.setText("XP: " + xp);

                    Long sutraObj = doc.getLong("currentSutra");
                    if (sutraObj == null) {
                        unlockedTill = (int) (xp / 100) + 1;
                    } else {
                        unlockedTill = sutraObj.intValue();
                    }
                    if (unlockedTill > 16) unlockedTill = 16;
                    updateSelection(selectedIndex);
                });
    }

    private void setupDialTouch() {
        dialContainer.setOnTouchListener((v, event) -> {

            float cx = dialContainer.getX() + dialContainer.getWidth() / 2f;
            float cy = dialContainer.getY() + dialContainer.getHeight() / 2f;

            float dx = event.getRawX() - cx;
            float dy = event.getRawY() - cy;

            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.performClick();
                    lastAngle = angle;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float delta = angle - lastAngle;

                    if (delta > 180) delta -= 360;
                    if (delta < -180) delta += 360;

                    currentRotation += delta;
                    rotateDial(currentRotation);
                    lastAngle = angle;
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    snapToNearestSutra();
                    return true;
            }
            return false;
        });
    }

    private void rotateDial(float rotation) {
        dialContainer.setRotation(rotation);

        float normalized = rotation % 360f;
        if (normalized < 0) normalized += 360f;

        // LEFT side is selection zone (180°)
        float selectionAngle = (180f - normalized + 360f) % 360f;

        int index = Math.round(selectionAngle / STEP_ANGLE) % 16;

        if (index != selectedIndex) {
            performTickHaptic();
            performTickSound();
            updateSelection(index);
        }
    }

    private void snapToNearestSutra() {
        float snapped = Math.round(currentRotation / STEP_ANGLE) * STEP_ANGLE;

        dialContainer.animate()
                .rotation(snapped)
                .setDuration(200)
                .withEndAction(() -> {
                    currentRotation = snapped;
                    rotateDial(currentRotation);
                })
                .start();
    }

    private void updateSelection(int index) {
        selectedIndex = index;

        for (int i = 0; i < sutraButtons.length; i++) {
            boolean unlocked = (i + 1) <= unlockedTill;
            MaterialButton btn = sutraButtons[i];

            btn.setEnabled(unlocked);
            btn.setAlpha(unlocked ? 1f : 0.35f);

            if (i == index) {
                btn.animate().scaleX(1.35f).scaleY(1.35f).setDuration(150).start();
            } else {
                btn.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            }

            btn.setRotation(-currentRotation);
        }

        sutraTitle.setText("Sutra " + (index + 1));
        sutraDesc.setText(getSutraDescription(index + 1));

        int displayProgress = 0;
        if (index + 1 < unlockedTill) {
            displayProgress = 100;
        } else if (index + 1 == unlockedTill) {
            displayProgress = currentLevelProgress;
        }

        levelProgressBar.setProgress(displayProgress);
        progressText.setText(displayProgress + "% complete");
    }

    private String getSutraDescription(int id) {
        switch (id) {
            case 1: return "Ekadhikena Purvena\n(Squares ending in 5)";
            case 2: return "Nikhilam Navatashcaramam Dashatah\n(Numbers near base 10/100)";
            case 3: return "Urdhva-Tiryagbhyam\n(General Multiplication)";
            case 14: return "Ekanyunena Purvena\n(Multiplication by 9, 99, 999...)";
            default: return "Ancient Vedic Math Technique";
        }
    }

    private void animateDialEntry() {
        dialContainer.setScaleX(0.8f);
        dialContainer.setScaleY(0.8f);
        dialContainer.setAlpha(0f);

        dialContainer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void performTickHaptic() {
        dialContainer.performHapticFeedback(
                android.view.HapticFeedbackConstants.CLOCK_TICK
        );
    }

    private void performTickSound() {
        if (AppSettings.isSoundEnabled(this)) {
            SoundManager.playDialTick();
        }
    }

    private void performButtonSound() {
        if (AppSettings.isSoundEnabled(this)) {
            SoundManager.playButtonClick();
        }
    }

    private void performHaptic(View view) {
        if (AppSettings.isHapticsEnabled(this)) {
            view.performHapticFeedback(
                    android.view.HapticFeedbackConstants.KEYBOARD_TAP
            );
        }
    }

    private void startBabaBreathing() {
        teachingBaba.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(1800)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> teachingBaba.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(1800)
                        .setInterpolator(new DecelerateInterpolator())
                        .withEndAction(this::startBabaBreathing)
                        .start()
                )
                .start();
    }
}