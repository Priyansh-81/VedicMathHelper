package com.priyansh.vedicMaths;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SutraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sutra);
        int sutraNumber=getIntent().getIntExtra("sutraNumber",1);
        TextView text=findViewById(R.id.sutraText);
        text.setText("Sutra " + sutraNumber);
    }
}