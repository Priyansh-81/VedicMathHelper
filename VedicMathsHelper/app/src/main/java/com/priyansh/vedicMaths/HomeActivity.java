package com.priyansh.vedicMaths;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class HomeActivity extends AppCompatActivity {

    Button logoutBtn,careerBtn;
    TextView userInfoText;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        logoutBtn=findViewById(R.id.logoutBtn);
        careerBtn=findViewById(R.id.careerBtn);
        userInfoText=findViewById(R.id.userInfoText);

        db=FirebaseFirestore.getInstance();

        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            String uid=user.getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if(documentSnapshot.exists()){
                    Long xpObj=documentSnapshot.getLong("xp");
                    Long streakObj=documentSnapshot.getLong("streak");
                    long xp= (xpObj!=null)?xpObj:0;
                    long streak= (streakObj!=null)?streakObj:0;

                    userInfoText.setText("XP: "+xp+"\nStreak: "+streak+"\n");
                }
            });
        }

        careerBtn.setOnClickListener(v -> {
            Intent intent=new Intent(this,CareerActivity.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        });

    }
}