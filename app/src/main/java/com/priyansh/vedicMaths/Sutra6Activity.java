// Sutra6Activity.java
package com.priyansh.vedicMaths;

import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class Sutra6Activity extends AppCompatActivity {

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
            "✨ Anurupyena = Proportionately",
            "🧠 Used for ratios, proportions & checking equations",
            "📘 Example: 2 : 4 = 3 : 6 ?",
            "2/4 = 1/2 and 3/6 = 1/2",
            "Both sides equal → Proportional ✅",
            "🎉 Answer = Yes",
            "🚀 Ready for Practice!"
    };

    int a,b,c,d;
    String correctAnswer;

    @Override
    protected void onCreate(Bundle b1){
        super.onCreate(b1);
        setContentView(R.layout.activity_sutra6);

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
                String btnText = nextBtn.getText().toString();
                if (btnText.equalsIgnoreCase("Next Sutra")) {
                    startActivity(new Intent(this, Sutra7Activity.class));
                    finish();
                    return;
                } else if (btnText.equalsIgnoreCase("Home")) {
                    finish();
                    return;
                }

                questionText.setText("🏆 Sutra Mastered!");
                hintText.setText("You solved 3 ratio questions!");
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

    void showTeach(){
        if(teachStep>=teach.length){
            learning=false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            generateQ();
            return;
        }

        progressBar.setProgress((teachStep+1)*14);
        questionText.setText("Sutra 6 Learning");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);
        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if(teachStep==2){
            animate(new String[]{
                    "2:4 = 3:6",
                    "2×6 = 12",
                    "4×3 = 12",
                    "Equal ✔"
            },800);
        }
    }

    void generateQ(){
        a=r.nextInt(8)+1;
        b=r.nextInt(8)+2;
        c=a*2;
        d=b*2;
        correctAnswer="yes";

        questionText.setText(a+":"+b+" = "+c+":"+d+" ?");
        hintText.setText("Type Yes or No");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
        nextBtn.setText("Next ➜");
    }

    void check(){
        if(nextBtn.getText().toString().equalsIgnoreCase("Next Sutra")) return;

        String user=answerInput.getText().toString().trim().toLowerCase();
        if(user.isEmpty()) return;

        if(user.equals(correctAnswer)){
            if (practice < 3) {
                score+=10;
                practice++;
            }
            solutionText.setText("✅ Correct!");
            solutionText.setTextColor(Color.GREEN);
        } else {
            solutionText.setText("❌ Correct: Yes");
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