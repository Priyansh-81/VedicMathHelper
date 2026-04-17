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

    TextView questionText,hintText,solutionText,scoreText;
    EditText answerInput;
    Button checkBtn,nextBtn;
    ImageButton homeBtn;
    ProgressBar progressBar;

    int base, num, correctAnswer;
    int teachStep = 0, score = 0, practice = 0;
    boolean learning = true;

    Handler h = new Handler();
    Random r = new Random();
    FirebaseFirestore db;

    String[] teach = {
            "✨ Nikhilam means: All from 9 and last from 10",
            "🧠 Use it to subtract from numbers like 10, 100, 1000, 10000...",
            "📘 Rule: Pick nearest base with only 1 and zeros",
            "Step 1: Subtract every digit from 9",
            "Step 2: Subtract last digit from 10",
            "🎯 This gives the complement from the base",
            "🚀 Ready for examples!"
    };

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_sutra2);

        db = FirebaseFirestore.getInstance();

        questionText=findViewById(R.id.questionText);
        hintText=findViewById(R.id.hintText);
        solutionText=findViewById(R.id.solutionText);
        answerInput=findViewById(R.id.answerInput);
        checkBtn=findViewById(R.id.checkBtn);
        nextBtn=findViewById(R.id.nextBtn);
        homeBtn=findViewById(R.id.homeBtn);
        progressBar=findViewById(R.id.progressBar);
        scoreText=findViewById(R.id.scoreText);

        showTeach();

        nextBtn.setOnClickListener(v -> nextFlow());
        checkBtn.setOnClickListener(v -> check());
        homeBtn.setOnClickListener(v -> finish());
    }

    void nextFlow(){
        if(learning){
            teachStep++;
            showTeach();
        } else {
            if(practice >= 3){
                String btnText = nextBtn.getText().toString();
                if (btnText.equalsIgnoreCase("Next Sutra")) {
                    startActivity(new Intent(this, sutra3.class));
                    finish();
                    return;
                } else if (btnText.equalsIgnoreCase("Home")) {
                    finish();
                    return;
                }

                updateProgress(3);
                questionText.setText("🏆 Sutra Mastered!");
                hintText.setText("You solved 3 examples!");
                solutionText.setText("What would you like to do next?");
                
                answerInput.setVisibility(View.GONE);
                checkBtn.setVisibility(View.VISIBLE);
                checkBtn.setText("Home");
                nextBtn.setText("Next Sutra");
                
                checkBtn.setOnClickListener(v -> finish());
                return;
            }
            generateQuestion();
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

    void showTeach(){
        if(teachStep >= teach.length){
            learning = false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            generateQuestion();
            return;
        }

        progressBar.setProgress((teachStep + 1) * 14);
        questionText.setText("Sutra 2 Learning Mode");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);
        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if(teachStep == 2){
            animate(new String[]{
                    "Example: 1000 - 256",
                    "9-2 = 7",
                    "9-5 = 4",
                    "10-6 = 4",
                    "Answer = 744"
            }, 900);
        }
    }

    void generateQuestion(){

        int[] bases = {10,100,1000,10000};
        base = bases[r.nextInt(bases.length)];

        num = r.nextInt(base - 1) + 1;
        correctAnswer = base - num;

        questionText.setText(base + " - " + num);
        hintText.setText("All from 9, last from 10");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
        nextBtn.setText("Next ➜");
    }

    void check(){
        if(nextBtn.getText().toString().equalsIgnoreCase("Next Sutra")) return;

        if(answerInput.getText().toString().isEmpty()) return;

        int user = Integer.parseInt(answerInput.getText().toString());

        if(user == correctAnswer){
            if (practice < 3) {
                score += 10;
                practice++;
            }
            solutionText.setText("✅ Correct!\n" + explainSteps());
            solutionText.setTextColor(Color.GREEN);

        } else {
            solutionText.setText("❌ Correct: " + correctAnswer + "\n" + explainSteps());
            solutionText.setTextColor(Color.RED);
        }

        scoreText.setText("⭐ Score: " + score + " | ✔ " + practice + "/3");
    }

    String explainSteps(){

        String n = String.format("%0" + (String.valueOf(base).length()-1) + "d", num);
        StringBuilder ans = new StringBuilder();

        for(int i=0;i<n.length();i++){
            int digit = Character.getNumericValue(n.charAt(i));

            if(i == n.length()-1){
                ans.append("10 - ").append(digit).append(" = ").append(10-digit);
            } else {
                ans.append("9 - ").append(digit).append(" = ").append(9-digit).append("\n");
            }
        }

        return ans.toString();
    }

    void animate(String[] arr,int d){
        for(int i=0;i<arr.length;i++){
            final String t = arr[i];
            h.postDelayed(() -> {
                solutionText.setText(t);
                fade(solutionText);
            }, (long)i*d);
        }
    }

    void fade(View v){
        AlphaAnimation a = new AlphaAnimation(0f,1f);
        a.setDuration(500);
        v.startAnimation(a);
    }
}
