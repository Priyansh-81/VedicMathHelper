package com.priyansh.vedicMaths;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    TextView userInfoText, titleText;
    ImageView profileBtn, homeBaba;
    MaterialButton careerBtn, challengeBtn;

    FrameLayout homeRoot;
    View sunRays;

    FirebaseFirestore db;

    float babaReturnX, babaReturnY;
    float touchOffsetX, touchOffsetY;
    boolean isDragging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SoundManager.init(this);

        careerBtn = findViewById(R.id.careerBtn);
        challengeBtn = findViewById(R.id.challengeBtn);
        profileBtn = findViewById(R.id.profileIcon);
        homeBaba = findViewById(R.id.homeBaba);
        userInfoText = findViewById(R.id.userInfoText);
        titleText = findViewById(R.id.titleText);

        homeRoot = findViewById(R.id.homeRoot);
        sunRays = findViewById(R.id.sunRays);

        db = FirebaseFirestore.getInstance();

        startBreathing(homeRoot);
        startSunAnimation();

        homeBaba.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setStartDelay(200)
                .withEndAction(() -> startBabaBreathing(homeBaba))
                .start();

        titleText.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setStartDelay(400)
                .start();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long xpObj = documentSnapshot.getLong("xp");
                            Long streakObj = documentSnapshot.getLong("streak");

                            long xp = (xpObj != null) ? xpObj : 0;
                            long streak = (streakObj != null) ? streakObj : 0;

                            userInfoText.setText("XP: " + xp + " • Best Streak: " + streak);
                        }
                    });
        }

        homeBaba.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    isDragging = true;
                    babaReturnX = v.getX();
                    babaReturnY = v.getY();

                    performHaptic(v);
                    performSound(v);

                    touchOffsetX = event.getRawX() - v.getX();
                    touchOffsetY = event.getRawY() - v.getY();

                    v.animate()
                            .scaleX(1.08f)
                            .scaleY(1.08f)
                            .setDuration(120)
                            .start();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (isDragging) {
                        float newX = event.getRawX() - touchOffsetX;
                        float newY = event.getRawY() - touchOffsetY;

                        v.setX(newX);
                        v.setY(newY);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isDragging = false;

                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .start();

                    v.animate()
                            .x(babaReturnX)
                            .y(babaReturnY)
                            .setDuration(450)
                            .setInterpolator(new OvershootInterpolator(0.6f))
                            .start();

                    return true;
            }
            return false;
        });

        profileBtn.setOnClickListener(v -> {
            animatePress(v);
            performSound(v);
            performHaptic(v);
            startActivity(new Intent(this, ProfileActivity.class));
        });

        careerBtn.setOnClickListener(v -> {
            animatePress(v);
            performSound(v);
            performHaptic(v);
            startActivity(new Intent(this, CareerActivity.class));
        });

        challengeBtn.setOnClickListener(v -> {
            animatePress(v);
            performHaptic(v);
            performSound(v);
            startActivity(new Intent(this,ChallengeActivity.class));
        });
    }

    private void startBreathing(FrameLayout view) {
        view.animate()
                .alpha(0.97f)
                .setDuration(4000)
                .withEndAction(() ->
                        view.animate()
                                .alpha(1f)
                                .setDuration(4000)
                                .withEndAction(() -> startBreathing(view))
                                .start()
                )
                .start();
    }

    private void startSunAnimation() {
        sunRays.setTranslationX(-80f);

        sunRays.animate()
                .translationX(80f)
                .setDuration(9000)
                .withEndAction(this::startSunAnimation)
                .start();
    }

    private void startBabaBreathing(ImageView baba) {
        baba.animate()
                .translationYBy(-25f)
                .setDuration(1600)
                .withEndAction(() ->
                        baba.animate()
                                .translationYBy(25f)
                                .setDuration(1600)
                                .withEndAction(() -> startBabaBreathing(baba))
                                .start()
                )
                .start();
    }

    private void animatePress(View v) {
        v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction(() ->
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                )
                .start();
    }

    private void performHaptic(View view) {
        if (AppSettings.isHapticsEnabled(this)) {
            view.performHapticFeedback(
                    android.view.HapticFeedbackConstants.KEYBOARD_TAP
            );
        }
    }
    private void performSound(View view) {
        if (AppSettings.isSoundEnabled(this)) {
            SoundManager.playButtonClick();
        }
    }

}