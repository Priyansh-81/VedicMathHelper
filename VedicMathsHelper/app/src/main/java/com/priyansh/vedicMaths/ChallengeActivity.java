package com.priyansh.vedicMaths;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ChallengeActivity extends AppCompatActivity {

    TextView timerText, scoreText, questionText, floatingText, difficultyText, streakText;
    ImageView babaReaction;
    View edgeGlow;
    View difficultyRing;

    TextView optionTop, optionRight, optionBottom, optionLeft;
    View joystick;

    int score = 0;
    int streak = 0;
    int bestStreak = 0;   // NEW
    int timeLeft = 60;
    int correctAnswer = 0;
    int currentXP = 1;
    int totalXpEarned = 0;

    CountDownTimer timer;
    Random random = new Random();

    float centerX, centerY;
    String lastTierLabel = "";

    // ---------------- TIER SYSTEM ----------------

    private static class Tier {
        int xp;
        String label;

        Tier(int xp, String label) {
            this.xp = xp;
            this.label = label;
        }
    }

    private Tier getCurrentTier() {
        if (score < 5) {
            return new Tier(1, "Easy");
        } else if (score < 10) {
            return new Tier(2, "Medium");
        } else if (score < 20) {
            return new Tier(3, "Hard");
        } else if (score < 35) {
            return new Tier(4, "Expert");
        } else {
            return new Tier(5, "Master");
        }
    }

    // ---------------- VEDIC QUESTION CLASS ----------------

    private static class VedicQuestion {
        String text;
        int answer;

        VedicQuestion(String text, int answer) {
            this.text = text;
            this.answer = answer;
        }
    }

    // ---------------- VEDIC GENERATORS ----------------

    private VedicQuestion generateEasy() {
        int a = 9 - random.nextInt(4);
        int b = 9 - random.nextInt(4);
        return new VedicQuestion(a + " × " + b, a * b);
    }

    private VedicQuestion generateMedium() {
        int a = 100 - (random.nextInt(20) + 1);
        int b = 100 - (random.nextInt(20) + 1);
        return new VedicQuestion(a + " × " + b, a * b);
    }

    private VedicQuestion generateHard() {
        int a = random.nextInt(80) + 20;
        int b = random.nextInt(80) + 20;
        return new VedicQuestion(a + " × " + b, a * b);
    }

    private VedicQuestion generateExpert() {
        int a = 1000 - (random.nextInt(80) + 10);
        int b = 1000 - (random.nextInt(80) + 10);
        return new VedicQuestion(a + " × " + b, a * b);
    }

    private VedicQuestion generateMaster() {
        int tens = random.nextInt(8) + 2;
        int unit1 = random.nextInt(9);
        int unit2 = 10 - unit1;

        int a = tens * 10 + unit1;
        int b = tens * 10 + unit2;

        return new VedicQuestion(a + " × " + b, a * b);
    }

    private VedicQuestion generateVedicQuestion(Tier tier) {
        switch (tier.label) {
            case "Easy":
                return generateEasy();
            case "Medium":
                return generateMedium();
            case "Hard":
                return generateHard();
            case "Expert":
                return generateExpert();
            default:
                return generateMaster();
        }
    }

    // ------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        questionText = findViewById(R.id.questionText);
        floatingText = findViewById(R.id.floatingText);
        difficultyText = findViewById(R.id.difficultyText);
        streakText = findViewById(R.id.streakText);

        babaReaction = findViewById(R.id.babaReaction);
        edgeGlow = findViewById(R.id.edgeGlow);
        difficultyRing = findViewById(R.id.difficultyRing);

        optionTop = findViewById(R.id.optionTop);
        optionRight = findViewById(R.id.optionRight);
        optionBottom = findViewById(R.id.optionBottom);
        optionLeft = findViewById(R.id.optionLeft);

        joystick = findViewById(R.id.joystick);

        SoundManager.init(this);

        // back gesture exits activity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (timer != null) timer.cancel();
                finish();
            }
        });

        setupJoystick();
        generateQuestion();
        startTimer();
    }

    // ---------------- TIMER ----------------

    private void startTimer() {
        timer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                timerText.setText("Time: " + timeLeft);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();
    }

    // ---------------- QUESTION GENERATION ----------------

    private void generateQuestion() {
        hideBaba();

        Tier tier = getCurrentTier();
        currentXP = tier.xp;

        difficultyText.setText(tier.label);
        updateDifficultyRing(tier.label);

        VedicQuestion q = generateVedicQuestion(tier);
        correctAnswer = q.answer;
        questionText.setText(q.text);

        generateSmartOptions();
    }

    // ---------------- SMART OPTIONS ----------------

    private void generateSmartOptions() {

        ArrayList<Integer> options = new ArrayList<>();
        options.add(correctAnswer);

        int magnitude = (int) Math.pow(10,
                String.valueOf(correctAnswer).length() - 1);

        while (options.size() < 4) {

            int variationType = random.nextInt(3);
            int wrong;

            switch (variationType) {

                case 0:
                    wrong = correctAnswer + (random.nextBoolean() ? 10 : -10);
                    break;

                case 1:
                    int lastDigit = correctAnswer % 10;
                    int newLast = (lastDigit + random.nextInt(8) + 1) % 10;
                    wrong = (correctAnswer / 10) * 10 + newLast;
                    break;

                default:
                    wrong = correctAnswer +
                            (random.nextBoolean()
                                    ? magnitude / 10
                                    : -magnitude / 10);
                    break;
            }

            if (wrong > 0 && !options.contains(wrong)) {
                options.add(wrong);
            }
        }

        Collections.shuffle(options);

        optionTop.setText(String.valueOf(options.get(0)));
        optionRight.setText(String.valueOf(options.get(1)));
        optionBottom.setText(String.valueOf(options.get(2)));
        optionLeft.setText(String.valueOf(options.get(3)));
    }

    // ---------------- DIFFICULTY RING ----------------

    private void updateDifficultyRing(String label) {

        if (label.equals(lastTierLabel)) return;

        int color;

        switch (label) {
            case "Easy":
                color = getColor(R.color.soft_green);
                break;
            case "Medium":
                color = getColor(R.color.saffron);
                break;
            case "Hard":
                color = getColor(R.color.muted_red);
                break;
            case "Expert":
                color = getColor(R.color.temple_maroon);
                break;
            default:
                color = getColor(R.color.temple_maroon_dark);
                break;
        }

        difficultyRing.setBackgroundTintList(ColorStateList.valueOf(color));

        difficultyRing.setScaleX(0.9f);
        difficultyRing.setScaleY(0.9f);

        difficultyRing.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(250)
                .start();

        lastTierLabel = label;
    }

    // ---------------- JOYSTICK ----------------

    private void setupJoystick() {

        joystick.post(() -> {
            int[] location = new int[2];
            joystick.getLocationOnScreen(location);

            centerX = location[0] + joystick.getWidth() / 2f;
            centerY = location[1] + joystick.getHeight() / 2f;
        });

        joystick.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {

                case MotionEvent.ACTION_MOVE:

                    float dx = event.getRawX() - centerX;
                    float dy = event.getRawY() - centerY;

                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    float max = 140f;

                    if (distance > max) {
                        dx = dx * max / distance;
                        dy = dy * max / distance;
                    }

                    v.setTranslationX(dx);
                    v.setTranslationY(dy);
                    return true;

                case MotionEvent.ACTION_UP:

                    evaluateDirection(v.getTranslationX(), v.getTranslationY());

                    v.animate()
                            .translationX(0)
                            .translationY(0)
                            .setDuration(200)
                            .start();

                    return true;
            }
            return true;
        });
    }

    private void evaluateDirection(float dx, float dy) {

        float threshold = 40f;

        if (Math.abs(dx) < threshold && Math.abs(dy) < threshold) {
            return;
        }

        TextView selectedOption;

        if (Math.abs(dx) > Math.abs(dy)) {
            selectedOption = (dx > 0) ? optionRight : optionLeft;
        } else {
            selectedOption = (dy > 0) ? optionBottom : optionTop;
        }

        checkAnswer(selectedOption);
    }

    // ---------------- ANSWER CHECK ----------------

    private void checkAnswer(TextView option) {

        performHaptic(option);
        performSound();

        int selected = Integer.parseInt(option.getText().toString());

        if (selected == correctAnswer) {
            score += currentXP;
            totalXpEarned += currentXP;
            streak++;

            if (streak > bestStreak) {
                bestStreak = streak;
            }

            scoreText.setText(String.valueOf(score));
            streakText.setText("🔥 " + streak);

            showEncouragedBaba();
            showFloatingText(streak >= 3 ? "Perfect!" : "+" + currentXP + " XP");
            showEdgeGlow(true);

        } else {
            streak = 0;

            score -= currentXP;
            totalXpEarned -= currentXP;

            if (score < 0) score = 0;
            if (totalXpEarned < 0) totalXpEarned = 0;

            scoreText.setText(String.valueOf(score));
            streakText.setText("🔥 0");

            showSadBaba();
            showFloatingText("-" + currentXP + " XP");
            showEdgeGlow(false);
        }

        option.postDelayed(this::generateQuestion, 800);
    }

    // ---------------- BABA ----------------

    private void showEncouragedBaba() {
        babaReaction.setVisibility(View.VISIBLE);
        babaReaction.setImageResource(R.drawable.baba_encouraged);
    }

    private void showSadBaba() {
        babaReaction.setVisibility(View.VISIBLE);
        babaReaction.setImageResource(R.drawable.baba_sad);
    }

    private void hideBaba() {
        babaReaction.setVisibility(View.INVISIBLE);
    }

    // ---------------- VISUAL FEEDBACK ----------------

    private void showFloatingText(String text) {
        floatingText.setText(text);
        floatingText.setAlpha(1f);
        floatingText.setTranslationY(0f);

        floatingText.animate()
                .translationY(-60f)
                .alpha(0f)
                .setDuration(700)
                .start();
    }

    private void showEdgeGlow(boolean correct) {

        int color = correct
                ? getColor(R.color.soft_green)
                : getColor(R.color.muted_red);

        edgeGlow.setBackgroundColor(color);

        edgeGlow.setAlpha(0f);
        edgeGlow.setVisibility(View.VISIBLE);

        edgeGlow.animate()
                .alpha(1f)
                .setDuration(120)
                .withEndAction(() ->
                        edgeGlow.animate()
                                .alpha(0f)
                                .setDuration(400)
                                .start()
                )
                .start();
    }

    // ---------------- END GAME ----------------

    private void endGame() {
        if (timer != null) timer.cancel();

        Intent intent = new Intent(this, ChallengeResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("bestStreak", bestStreak);
        intent.putExtra("xpEarned", totalXpEarned);
        startActivity(intent);
        finish();
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
}