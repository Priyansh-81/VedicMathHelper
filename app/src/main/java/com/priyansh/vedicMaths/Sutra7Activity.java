// Sutra7Activity.java
package com.priyansh.vedicMaths;

import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class Sutra7Activity extends AppCompatActivity {

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
            "✨ Sankalana-Vyavakalanabhyam = By Addition and Subtraction",
            "🧠 Used to solve equations quickly using sum & difference",
            "📘 Example: x + y = 10 and x - y = 2",
            "Add equations: 2x = 12",
            "x = 6",
            "Now y = 10 - 6 = 4",
            "🎉 Answer: x=6, y=4",
            "🚀 Ready for Practice!"
    };

    int x,y,sum,diff;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_sutra7);

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
                    startActivity(new Intent(this, Sutra8Activity.class));
                    finish();
                    return;
                } else if (btnText.equalsIgnoreCase("Home")) {
                    finish();
                    return;
                }

                questionText.setText("🏆 Sutra Mastered!");
                hintText.setText("You solved 3 systems!");
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

        progressBar.setProgress((teachStep+1)*12);
        questionText.setText("Sutra 7 Learning");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);
        fade(solutionText);

        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);

        if(teachStep==2){
            animate(new String[]{
                    "x+y=10",
                    "x-y=2",
                    "Add → 2x=12",
                    "x=6, y=4"
            },800);
        }
    }

    void generateQ(){
        x=r.nextInt(9)+1;
        y=r.nextInt(9)+1;
        sum=x+y;
        diff=x-y;

        questionText.setText("x+y="+sum+" , x-y="+diff+"\nFind x");
        hintText.setText("Use addition");
        solutionText.setText("");
        answerInput.setText("");
        progressBar.setProgress(100);
        nextBtn.setText("Next ➜");
    }

    void check(){
        if(nextBtn.getText().toString().equalsIgnoreCase("Next Sutra")) return;

        if(answerInput.getText().toString().isEmpty()) return;

        int user=Integer.parseInt(answerInput.getText().toString());

        if(user==x){
            if (practice < 3) {
                score+=10;
                practice++;
            }
            solutionText.setText("✅ Correct! y = "+y);
            solutionText.setTextColor(Color.GREEN);
        } else {
            solutionText.setText("❌ Correct: x = "+x+", y = "+y);
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