package com.priyansh.vedicMaths;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser User= FirebaseAuth.getInstance().getCurrentUser();

        if(User!=null){
            startActivity(new Intent(this, HomeActivity.class));
        }else{
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}