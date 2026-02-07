package com.priyansh.vedicMaths;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CareerActivity extends AppCompatActivity {

    View dialContainer;

    ImageView teachingBaba;
    MaterialButton[] sutraButtons = new MaterialButton[16];
    MaterialButton startBtn;

    TextView sutraTitle, sutraDesc;

    FirebaseFirestore db;

    float lastAngle = 0f;
    float currentRotation = 0f;

    int selectedIndex = 0;
    int unlockedTill = 1;

    final float STEP_ANGLE = 22.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career);

        db = FirebaseFirestore.getInstance();

        dialContainer = findViewById(R.id.dialContainer);
        sutraTitle = findViewById(R.id.sutraTitle);
        sutraDesc = findViewById(R.id.sutraDesc);
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
                    Long obj = doc.getLong("currentSutra");
                    unlockedTill = (obj != null) ? obj.intValue() : 1;
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
        sutraDesc.setText("Short description here");
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