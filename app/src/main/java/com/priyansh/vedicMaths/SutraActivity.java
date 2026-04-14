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

public class SutraActivity extends AppCompatActivity {
    TextView sutraTitle,questionText,hintText,solutionText,scoreText;
    EditText answerInput;
    Button checkBtn,nextBtn;
    ImageButton homeBtn;
    ProgressBar progressBar;

    int sutraNumber,a,b,correctAnswer,score=0,teachStep=0,practiceCount=0;
    boolean isDemo=true;
    Random random=new Random();
    Handler h=new Handler();

    FirebaseFirestore db;

    String[] teach={"✨ Meaning: By one more than the previous one","📚 This sutra finds squares of numbers ending in 5 instantly","🧠 Why it works: every answer ends with 25","🔍 Rule Step 1: Take the digits before 5","➕ Rule Step 2: Multiply by one more than itself","🎉 Rule Step 3: Attach 25 at the end","📘 Example: 35²","❓ Quiz: What comes before 5 in 35?","✖ Multiply 3 × 4","🎯 Final Answer = 1225","🚀 Now you are ready for practice!"};

    @Override protected void onCreate(Bundle saved){
        super.onCreate(saved);
        setContentView(R.layout.activity_sutra);

        db = FirebaseFirestore.getInstance();

        sutraNumber=getIntent().getIntExtra("sutraNumber",1);

        sutraTitle=findViewById(R.id.sutraTitle);
        questionText=findViewById(R.id.questionText);
        hintText=findViewById(R.id.hintText);
        solutionText=findViewById(R.id.solutionText);
        answerInput=findViewById(R.id.answerInput);
        checkBtn=findViewById(R.id.checkBtn);
        homeBtn=findViewById(R.id.homeBtn);
        nextBtn=findViewById(R.id.nextBtn);
        scoreText=findViewById(R.id.scoreText);
        progressBar=findViewById(R.id.progressBar);

        sutraTitle.setText(getSutraName(sutraNumber));
        generateQuestion();
        startDemo();

        checkBtn.setOnClickListener(v->checkAnswer());
        nextBtn.setOnClickListener(v->nextFlow());
        homeBtn.setOnClickListener(v->finish());
    }

    void nextFlow(){
        if(isDemo){
            teachStep++;
            showTeach();
        } else {
            if(practiceCount>=3){
                String btnText = nextBtn.getText().toString();
                if (btnText.equalsIgnoreCase("Next Sutra")) {
                    startActivity(new Intent(this, sutra2Activity.class));
                    finish();
                    return;
                } else if (btnText.equalsIgnoreCase("Home")) {
                    finish();
                    return;
                }
                
                updateProgress(2);
                questionText.setText("🏆 Sutra Mastered!");
                hintText.setText("You've successfully learned Sutra 1!");
                solutionText.setText("What would you like to do next?");
                
                checkBtn.setVisibility(View.VISIBLE);
                checkBtn.setText("Home");
                nextBtn.setText("Next Sutra");
                
                checkBtn.setOnClickListener(v -> finish());
                return;
            }
            generateQuestion();
            loadStep();
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

    void startDemo(){
        isDemo=true;
        teachStep=0;
        answerInput.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);
        nextBtn.setText("Next ➜");
        showTeach();
    }

    void showTeach(){
        if(sutraNumber!=1){
            isDemo=false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            nextBtn.setText("Next Question");
            loadStep();
            return;
        }
        if(teachStep>=teach.length){
            isDemo=false;
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            nextBtn.setText("Next Question");
            loadStep();
            return;
        }
        progressBar.setProgress(Math.min((teachStep+1)*10,100));
        questionText.setText("Sutra 1 Learning Mode");
        hintText.setText("");
        solutionText.setText(teach[teachStep]);
        fade(solutionText);

        if(teachStep==6) animateSeq(new String[]{"35²","Take 3","3×4=12","...25"},700);
        if(teachStep==7){
            answerInput.setVisibility(View.VISIBLE);
            checkBtn.setVisibility(View.VISIBLE);
            answerInput.setHint("Type digit");
        }
        else {
            answerInput.setVisibility(View.GONE);
            checkBtn.setVisibility(View.GONE);
        }
        if(teachStep==8) animateSeq(new String[]{"3+1=4","3×4=12"},900);
        if(teachStep==9) animateSeq(new String[]{"12 + 25","1225","✅ Answer"},900);
    }

    void animateSeq(String[] arr,int delay){
        for(int i=0;i<arr.length;i++){
            final String t=arr[i];
            h.postDelayed(()->{
                solutionText.setText(t);
                fade(solutionText);
            },(long)i*delay);
        }
    }

    void fade(View v){
        AlphaAnimation a=new AlphaAnimation(0f,1f);
        a.setDuration(500);
        v.startAnimation(a);
    }

    private String getSutraName(int id){
        String[] names={"","Ekadhikena Purvena","Nikhilam Navatashcaramam Dashatah","Urdhva-Tiryagbhyam","Paravartya Yojayet","Shunyam Saamyasamuccaye","Anurupyena","Sankalana-Vyavakalanabhyam","Puranapuranabhyam","Chalana-Kalanabhyam","Yavadunam","Vyashtisamanstih","Shesanyankena Charamena","Sopantyadvayamantyam","Ekanyunena Purvena","Gunakasamuccayah","Gunita Samuccayah"};
        return (id >= 0 && id < names.length) ? names[id] : "Vedic Sutra";
    }

    private void generateQuestion(){
        answerInput.setText("");
        solutionText.setText("");
        switch(sutraNumber){
            case 1:
                a=(random.nextInt(9)+1)*10+5;
                b=a;
                break;
            case 2:
                a=100-(random.nextInt(20)+1);
                b=100-(random.nextInt(20)+1);
                break;
            default:
                a=random.nextInt(50)+10;
                b=random.nextInt(50)+10;
        }
        correctAnswer=a*b;
    }

    private void loadStep(){
        progressBar.setProgress(100);
        answerInput.setText("");
        switch(sutraNumber){
            case 1:
                questionText.setText("Square of "+a);
                hintText.setText("Use Ekadhikena rule");
                break;
            case 2:
                questionText.setText(a+" × "+b);
                hintText.setText("Use base 100");
                break;
            default:
                questionText.setText(a+" × "+b);
                hintText.setText("Solve it!");
        }
    }

    private void checkAnswer(){
        if(answerInput.getText().toString().isEmpty()) return;
        String txt=answerInput.getText().toString().trim();

        if(isDemo && teachStep==7){
            if(txt.equals("3")){
                solutionText.setText(" Correct! Click Next");
                solutionText.setTextColor(Color.GREEN);
            } else {
                solutionText.setText("Try again. It's 3");
                solutionText.setTextColor(Color.RED);
            }
            return;
        }

        try {
            int user = Integer.parseInt(txt);
            if(user==correctAnswer){
                if (practiceCount < 3) {
                    score+=10;
                    practiceCount++;
                }
                solutionText.setText(" Correct!");
                solutionText.setTextColor(Color.GREEN);
            } else {
                solutionText.setText(" Correct: "+correctAnswer);
                solutionText.setTextColor(Color.RED);
            }
            scoreText.setText("⭐ Score: "+score+" | ✔ "+practiceCount+"/3");
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }
}
