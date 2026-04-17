package com.priyansh.vedicMaths;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    TextView emailText, xpText, streakText;
    MaterialButton logoutBtn, deleteAccountBtn, changePasswordBtn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    MaterialToolbar topbar;

    SwitchMaterial hapticSwitch, musicSwitch, soundSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        emailText = findViewById(R.id.emailText);
        xpText = findViewById(R.id.xpText);
        streakText = findViewById(R.id.streakText);

        logoutBtn = findViewById(R.id.logoutBtn);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        topbar = findViewById(R.id.topBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Switches
        hapticSwitch = findViewById(R.id.hapticSwitch);
        musicSwitch = findViewById(R.id.musicSwitch);
        soundSwitch = findViewById(R.id.soundSwitch);

        // Load saved states
        hapticSwitch.setChecked(AppSettings.isHapticsEnabled(this));
        musicSwitch.setChecked(AppSettings.isMusicEnabled(this));
        soundSwitch.setChecked(AppSettings.isSoundEnabled(this));

        // Listeners
        hapticSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppSettings.setHapticsEnabled(this, isChecked)
        );

        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppSettings.setMusicEnabled(this, isChecked)
        );

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppSettings.setSoundEnabled(this, isChecked)
        );

        topbar.setNavigationOnClickListener(v -> {
            performHaptic(v);
            performSound(v);
            finish();
        });

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            emailText.setText(user.getEmail());

            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long xp = documentSnapshot.getLong("xp");
                            Long streak = documentSnapshot.getLong("streak");

                            xpText.setText("XP: " + (xp != null ? xp : 0));
                            streakText.setText("Best Streak: " + (streak != null ? streak : 0));
                        }
                    });
        }

        changePasswordBtn.setOnClickListener(v -> {
            performHaptic(v);
            performSound(v);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 0);

            EditText currentPassword = new EditText(this);
            currentPassword.setHint("Current Password");
            currentPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);

            EditText newPassword = new EditText(this);
            newPassword.setHint("New Password");
            newPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);

            layout.addView(currentPassword);
            layout.addView(newPassword);

            new MaterialAlertDialogBuilder(this, R.style.VedicDialog)
                    .setTitle("Change Password")
                    .setView(layout)
                    .setPositiveButton("Change", (dialog, which) -> {

                        String currPass = currentPassword.getText().toString().trim();
                        String newPass = newPassword.getText().toString().trim();

                        if (currPass.isEmpty() || newPass.isEmpty()) {
                            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (newPass.length() < 6) {
                            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AuthCredential credential =
                                EmailAuthProvider.getCredential(currentUser.getEmail(), currPass);

                        currentUser.reauthenticate(credential)
                                .addOnSuccessListener(aVoid ->
                                        currentUser.updatePassword(newPass)
                                                .addOnSuccessListener(aVoid1 ->
                                                        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                                                )
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "Password update failed", Toast.LENGTH_SHORT).show()
                                                )
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Current password incorrect", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        deleteAccountBtn.setOnClickListener(v -> {
            performHaptic(v);
            performSound(v);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;

            EditText passwordInput = new EditText(this);
            passwordInput.setHint("Enter your password to confirm");
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordInput.setPadding(50, 30, 50, 30);

            new MaterialAlertDialogBuilder(this, R.style.VedicDialog)
                    .setTitle("Delete Account")
                    .setMessage("This action cannot be undone.")
                    .setView(passwordInput)
                    .setPositiveButton("Delete", (dialog, which) -> {

                        String password = passwordInput.getText().toString().trim();

                        if (password.isEmpty()) {
                            Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AuthCredential credential =
                                EmailAuthProvider.getCredential(currentUser.getEmail(), password);

                        currentUser.reauthenticate(credential)
                                .addOnSuccessListener(aVoid -> {

                                    String uid = currentUser.getUid();

                                    db.collection("users").document(uid)
                                            .delete()
                                            .addOnSuccessListener(aVoid1 ->
                                                    currentUser.delete()
                                                            .addOnSuccessListener(aVoid2 -> {
                                                                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                                                                startActivity(new Intent(this, LoginActivity.class));
                                                                finishAffinity();
                                                            })
                                                            .addOnFailureListener(e ->
                                                                    Toast.makeText(this, "Account deletion failed", Toast.LENGTH_SHORT).show()
                                                            )
                                            );

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        logoutBtn.setOnClickListener(v -> {
            performHaptic(v);
            performSound(v);
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void performHaptic(View view) {
        if (AppSettings.isHapticsEnabled(this)) {
            view.performHapticFeedback(
                    android.view.HapticFeedbackConstants.KEYBOARD_TAP
            );
        }
    }

    private void performSound(View view) {
        if (AppSettings.isSoundEnabled(this)) {
            SoundManager.playButtonClick();
        }
    }
}