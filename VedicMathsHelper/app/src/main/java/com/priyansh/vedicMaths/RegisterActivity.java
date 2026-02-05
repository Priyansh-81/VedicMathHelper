package com.priyansh.vedicMaths;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText emailInput, passwordInput, confirmPasswordInput;
    MaterialButton registerBtn;
    TextView loginLink;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    LinearLayout heroContainer, formContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerBtn = findViewById(R.id.registerBtn);
        loginLink = findViewById(R.id.loginLink);

        heroContainer = findViewById(R.id.heroContainer);
        formContainer = findViewById(R.id.formContainer);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        registerBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            hideKeyboard();

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) {
                                Toast.makeText(this, "User creation failed unexpectedly", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String uid = user.getUid();

                            HashMap<String, Object> userProfile = new HashMap<>();
                            userProfile.put("email", user.getEmail());
                            userProfile.put("xp", 0);
                            userProfile.put("streak", 1);
                            userProfile.put("currentSutra", 1);
                            userProfile.put("createdAt", System.currentTimeMillis());

                            db.collection("users")
                                    .document(uid)
                                    .set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, HomeActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(
                                                this,
                                                "Account created, but profile setup failed",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    });

                        } else {
                            Toast.makeText(
                                    this,
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Registration failed",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        loginLink.setOnClickListener(v -> finish());
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