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

public class sutra3 extends AppCompatActivity {

    TextView questionText,hintText,solutionText,scoreText;
    EditText answerInput;
    Button checkBtn,nextBtn,homeBtn;
    ProgressBar progressBar;

    int a,b,correctAnswer,teachStep=0,score=0,practice=0;
    boolean learning=true;
    Handler h=new Handler();
    Random r=new Random();
    
    FirebaseFirestore db;

    String[] teach={
            "✨ Urdhva-Tiryagbhyam means Vertically & Crosswise",
            "🧠 A universal multiplication trick",
            "📘 Example: 12 × 13",
            "Step 1: Vertical → 2×3 = 6",
            "Step 2: Crosswise → (1×3)+(2×1)=5",
            "Step 3: Vertical → 1×1 = 1",
            "🎉 Answer = 156",
            "🚀 Ready for Practice!"
    };

    @Override
    protected void onCreate(Bundle b1){
        super.onCreate(b1);
        setContentView(R.layout.activity_sutra3);
        
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
        }else{
            if(practice>=3){
                String btnText = nextBtn.getText().toString();
                if (btnText.equalsIgnoreCase("Next Sutra")) {
                    startActivity(new Intent(this, sutra4.class));
                    finish();
                    return;
                }
                
                updateProgress(4);
                
                questionText.setText("🏆 Sutra Mastered!");
                hintText.setText("You solved 3 questions!");
                solutionText.setText("What would you like to do next?");
                
                answerInput.setVisibility(View.GONE);
                checkBtn.setVisibility(View.VISIBLE);
                checkBtn.setText("Home");
                nextBtn.setText("Next Sutra");
                
                checkBtn.setOnClickListener(v -> finish());
                return;
            }
            generateQ();
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
        if(teachStep>=teach.length){
            learning=false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            nextBtn.setText("Next Question");
            generateQ();
            return;
        }

        progressBar.setProgress((teachStep+1)*12);
        questionText.setText("Sutra 3 Learning");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);
        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if(teachStep==2)
            animate(new String[]{"12×13","2×3=6","Cross=5","1×1=1"},800);
    }

    void generateQ(){
        a=r.nextInt(90)+10;
        b=r.nextInt(90)+10;
        correctAnswer=a*b;

        questionText.setText(a+" × "+b);
        hintText.setText("Use vertical & crosswise");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
    }

    void check(){
        if(nextBtn.getText().toString().equalsIgnoreCase("Next Sutra")) return;

        if(answerInput.getText().toString().isEmpty()) return;

        try {
            int user=Integer.parseInt(answerInput.getText().toString());

            if(user==correctAnswer){
                if (practice < 3) {
                    score+=10;
                    practice++;
                }
                solutionText.setText("✅ Correct!");
                solutionText.setTextColor(Color.GREEN);
            } else {
                solutionText.setText("❌ Correct: "+correctAnswer);
                solutionText.setTextColor(Color.RED);
            }

            scoreText.setText("⭐ Score: "+score+" | ✔ "+practice+"/3");
        } catch (NumberFormatException e) {
             Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    void animate(String[] arr,int d){
        for(int i=0;i<arr.length;i++){
            final String t=arr[i];
            h.postDelayed(() -> {
                solutionText.setText(t);
                fade(solutionText);
            },(long)i*d);
        }
    }

    void fade(View v){
        AlphaAnimation a=new AlphaAnimation(0f,1f);
        a.setDuration(500);
        v.startAnimation(a);
    }
}
