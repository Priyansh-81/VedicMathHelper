// Sutra8Activity.java
package com.priyansh.vedicMaths;

import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class Sutra8Activity extends AppCompatActivity {

    TextView questionText,hintText,solutionText,scoreText;
    EditText answerInput;
    Button checkBtn,nextBtn;
    ImageButton homeBtn;
    ProgressBar progressBar;

    int teachStep=0,score=0,practice=0;
    boolean learning=true;
    Handler h=new Handler();
    Random r=new Random();

    String[] teach={
            "✨ Puranapuranabhyam = By Completion or Non-Completion",
            "🧠 Used by completing numbers to a convenient base",
            "📘 Example: 98 + 7",
            "Complete 98 to 100 by adding 2",
            "Take 2 from 7 → remaining 5",
            "100 + 5 = 105",
            "🎉 Fast Answer = 105",
            "🚀 Ready for Practice!"
    };

    int a,b,correctAnswer;

    @Override
    protected void onCreate(Bundle b1){
        super.onCreate(b1);
        setContentView(R.layout.activity_sutra8);

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
            if(practice>=3){
                showCompletionOptions();
                return;
            }
            generateQ();
        }
    }

    void showCompletionOptions() {
        questionText.setText("🏆 Sutra 8 Complete!");
        hintText.setText("You solved 3 completion sums!");
        solutionText.setText("Excellent speed maths!");
        
        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);
        
        nextBtn.setText("Next Sutra");
        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, sutra9Activity.class);
            startActivity(intent);
            finish();
        });

        // Add a home button if needed, but we already have homeBtn in layout.
        // Let's repurpose checkBtn area for "Home" if we want it side by side, 
        // or just let the user use the top-left home button.
        // The request says "option to either go to home or next sutra".
        
        // Let's create a "Go Home" button or reuse checkBtn
        checkBtn.setVisibility(View.VISIBLE);
        checkBtn.setText("Go Home");
        checkBtn.setOnClickListener(v -> finish());
    }

    void showTeach(){
        if(teachStep>=teach.length){
            learning=false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            checkBtn.setText("Check");
            generateQ();
            return;
        }

        progressBar.setProgress((teachStep+1)*12);
        questionText.setText("Sutra 8 Learning");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);
        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if(teachStep==2){
            animate(new String[]{
                    "98 + 7",
                    "Need 2 to make 100",
                    "7 - 2 = 5",
                    "100 + 5 = 105"
            },850);
        }
    }

    void generateQ(){
        a = 90 + r.nextInt(9);   // 90 to 98
        b = r.nextInt(9) + 1;    // 1 to 9
        correctAnswer = a + b;

        questionText.setText(a+" + "+b);
        hintText.setText("Complete to nearest base");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
    }

    void check(){
        if(answerInput.getText().toString().isEmpty()) return;

        int user=Integer.parseInt(answerInput.getText().toString());

        if(user==correctAnswer){
            score+=10;
            practice++;
            solutionText.setText("✅ Correct!");
            solutionText.setTextColor(Color.GREEN);
        } else {
            solutionText.setText("❌ Correct: "+correctAnswer);
            solutionText.setTextColor(Color.RED);
        }

        scoreText.setText("⭐ Score: "+score+" | ✔ "+practice+"/3");
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