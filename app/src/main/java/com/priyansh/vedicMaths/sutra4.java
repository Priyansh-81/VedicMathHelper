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

public class sutra4 extends AppCompatActivity {

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
            "✨ Paravartya Yojayet means Transpose & Adjust",
            "🧠 Used in fast division and algebra",
            "📘 Example: 144 ÷ 12",
            "Think: 12 × ? = 144",
            "12 × 12 = 144",
            "🎉 Answer = 12",
            "🚀 Ready for Practice!"
    };

    @Override
    protected void onCreate(Bundle b1){
        super.onCreate(b1);
        setContentView(R.layout.activity_sutra4);
        
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
                if (btnText.equalsIgnoreCase("Challenge")) {
                    startActivity(new Intent(this, ChallengeActivity.class));
                    finish();
                    return;
                }
                
                updateProgress(5);
                
                questionText.setText("🏆 Sutra Mastered!");
                hintText.setText("You solved 3 questions!");
                solutionText.setText("What would you like to do next?");
                
                answerInput.setVisibility(View.GONE);
                checkBtn.setVisibility(View.VISIBLE);
                checkBtn.setText("Home");
                nextBtn.setText("Challenge");
                
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
            generateQ();
            return;
        }

        progressBar.setProgress((teachStep+1)*14);
        questionText.setText("Sutra 4 Learning");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);
        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if(teachStep==2)
            animate(new String[]{"144 ÷ 12","12×10=120","Need 24 more","12×2"},900);
    }

    void generateQ(){
        b=(r.nextInt(8)+2);
        correctAnswer=r.nextInt(10)+2;
        a=b*correctAnswer;

        questionText.setText(a+" ÷ "+b);
        hintText.setText("Find the quotient");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
    }

    void check(){
        if(nextBtn.getText().toString().equalsIgnoreCase("Challenge")) return;

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
