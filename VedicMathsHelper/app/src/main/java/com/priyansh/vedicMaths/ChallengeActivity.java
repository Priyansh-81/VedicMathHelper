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


    private static class VedicQuestion {
        String text;
        int answer;

        VedicQuestion(String text, int answer) {
            this.text = text;
            this.answer = answer;
        }
    }


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
        int sutra = random.nextInt(16) + 1; // 1 to 16
        
        switch (sutra) {
            case 1: // Ekadhikena Purvena - Squaring numbers ending in 5
                int base1 = (tier.xp * 2) + random.nextInt(10);
                int n1 = base1 * 10 + 5;
                return new VedicQuestion(n1 + "²", n1 * n1);
                
            case 2: // Nikhilam - Multiplication near base
                int base2 = (tier.xp > 2) ? 100 : 10;
                int off2a = random.nextInt(tier.xp + 2) + 1;
                int off2b = random.nextInt(tier.xp + 2) + 1;
                int n2a = base2 - off2a;
                int n2b = base2 - off2b;
                return new VedicQuestion(n2a + " × " + n2b, n2a * n2b);
                
            case 3: // Urdhva-Tiryagbhyam - General multiplication
                int n3a = random.nextInt(tier.xp * 20) + 11;
                int n3b = random.nextInt(tier.xp * 20) + 11;
                return new VedicQuestion(n3a + " × " + n3b, n3a * n3b);
                
            case 4: // Paravartya - Division (simplified to quotient)
                int div4 = 11 + random.nextInt(3);
                int quot4 = random.nextInt(tier.xp * 5) + 5;
                int n4 = div4 * quot4;
                return new VedicQuestion(n4 + " ÷ " + div4, quot4);
                
            case 5: // Shunyam - Equation constants (simplified)
                int n5 = random.nextInt(tier.xp * 10) + 1;
                return new VedicQuestion("If x + " + n5 + " = 0, x?", -n5);
                
            case 6: // Anurupyena - Squaring near sub-base
                int subBase6 = (random.nextInt(4) + 2) * 10;
                int n6 = subBase6 + (random.nextInt(3) + 1);
                return new VedicQuestion(n6 + "²", n6 * n6);
                
            case 7: // Sankalana-Vyavakalanabhyam - Addition/Subtraction
                int n7a = random.nextInt(tier.xp * 50) + 20;
                int n7b = random.nextInt(tier.xp * 50) + 20;
                if (random.nextBoolean()) return new VedicQuestion(n7a + " + " + n7b, n7a + n7b);
                else return new VedicQuestion(Math.max(n7a, n7b) + " - " + Math.min(n7a, n7b), Math.abs(n7a - n7b));
                
            case 8: // Puranapuranabhyam - Completion
                int n8a = (random.nextInt(9) + 1) * 10 - random.nextInt(3) - 1;
                int n8b = random.nextInt(15) + 5;
                return new VedicQuestion(n8a + " + " + n8b, n8a + n8b);
                
            case 9: // Chalana-Kalanabhyam - Root (simplified)
                int root9 = random.nextInt(tier.xp * 4) + 2;
                return new VedicQuestion("√" + (root9 * root9), root9);
                
            case 10: // Yavadunam - Squaring near base
                int base10 = 100;
                int off10 = random.nextInt(10) + 1;
                int n10 = base10 - off10;
                return new VedicQuestion(n10 + "²", n10 * n10);
                
            case 11: // Vyashtisamanstih - Sum of digits (simplified)
                int n11 = random.nextInt(tier.xp * 100) + 100;
                int sum11 = 0;
                int temp11 = n11;
                while (temp11 > 0) { sum11 += temp11 % 10; temp11 /= 10; }
                return new VedicQuestion("Digit sum: " + n11, sum11);
                
            case 12: // Shesanyankena - Division by 9s (simplified)
                int n12 = (random.nextInt(tier.xp * 10) + 5) * 9;
                return new VedicQuestion(n12 + " ÷ 9", n12 / 9);
                
            case 13: // Sopantyadvayamantyam - Simple Linear Equation
                int n13 = random.nextInt(tier.xp * 5) + 1;
                return new VedicQuestion("2x + " + (n13 * 2) + " = 0, x?", -n13);
                
            case 14: // Ekanyunena Purvena - Multiplication by 99
                int n14 = random.nextInt(89) + 10;
                return new VedicQuestion(n14 + " × 99", n14 * 99);
                
            case 15: // Gunakasamuccayah - Digit sum check (simplified)
                int n15a = random.nextInt(20) + 2;
                int n15b = random.nextInt(20) + 2;
                return new VedicQuestion("Digit sum of (" + n15a + "×" + n15b + ")", getDigitSum(n15a * n15b));
                
            case 16: // Gunita Samuccayah - Sc of product (simplified)
                int n16 = random.nextInt(tier.xp * 3) + 1;
                return new VedicQuestion("Sum of coeff: (x+" + n16 + ")²", (1 + n16) * (1 + n16));
                
            default:
                return generateEasy();
        }
    }

    private int getDigitSum(int n) {
        int sum = 0;
        while (n > 0) { sum += n % 10; n /= 10; }
        if (sum > 9) return getDigitSum(sum);
        return sum;
    }


    @Override
    protected void onResume() {
        super.onResume();
        SoundManager.stopBackgroundMusic();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

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


    private void checkAnswer(TextView option) {

        performHaptic(option);
        performSound();

        int selected;
        try {
            selected = Integer.parseInt(option.getText().toString());
        } catch (NumberFormatException e) {
            // Handle cases where the text might not be a simple integer (e.g., negative signs or non-numeric if question text leaked)
            return;
        }

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

    private void endGame() {
        if (timer != null) timer.cancel();

        Intent intent = new Intent(this, ChallengeResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("bestStreak", bestStreak);
        intent.putExtra("xpEarned", totalXpEarned);
        startActivity(intent);
        finish();
    }

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