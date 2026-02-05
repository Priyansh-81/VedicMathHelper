package com.priyansh.vedicMaths;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Button;
import android.widget.LinearLayout;
import android.content.Intent;
import android.widget.Toast;


public class CareerActivity extends AppCompatActivity {
    LinearLayout sutraContainer;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_career);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                });

        sutraContainer=findViewById(R.id.sutraContainer);
        db=FirebaseFirestore.getInstance();

        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
               Long currentSutraObj=documentSnapshot.getLong("currentSutra");
               int currentSutra=(currentSutraObj!=null)?currentSutraObj.intValue():1;

               loadSutras(currentSutra);
            });
        }
    }

    private void loadSutras(int unlockedTill){
        for(int i=1;i<=16;i++){
            Button btn=new Button(this);
            btn.setText("Sutra " + i);

            if(i<=unlockedTill){
                btn.setEnabled(true);
                int sutraNumber=i;

                btn.setOnClickListener(v->{
                    Intent intent= new Intent(CareerActivity.this,SutraActivity.class);
                    intent.putExtra("sutraNumber", sutraNumber);
                    startActivity(intent);
                });
            }else{
                btn.setText("Sutra "+i+" locked");
                btn.setEnabled(false);
            }
            sutraContainer.addView(btn);
        }
    }
}