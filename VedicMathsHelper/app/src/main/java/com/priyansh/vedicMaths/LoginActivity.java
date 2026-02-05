package com.priyansh.vedicMaths;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    MaterialButton loginBtn;
    TextView registerLink;

    LinearLayout heroContainer, formContainer;
    ImageView babaView;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerLink);

        heroContainer = findViewById(R.id.heroContainer);
        formContainer = findViewById(R.id.formContainer);
        babaView = findViewById(R.id.babaView);

        mAuth = FirebaseAuth.getInstance();

        heroContainer.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setStartDelay(100)
                .start();

        formContainer.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setStartDelay(350)
                .start();

        babaView.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(700)
                .setStartDelay(800)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .withEndAction(() -> startBabaIdleMotion(babaView))
                .start();

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            hideKeyboard();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        registerLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }
    private void startBabaIdleMotion(ImageView babaView) {
        babaView.animate()
                .translationY(-50f)
                .setDuration(3500)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction(() ->
                        babaView.animate()
                                .translationY(0f)
                                .setDuration(3500)
                                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                                .withEndAction(() -> startBabaIdleMotion(babaView))
                                .start()
                )
                .start();
    }
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

