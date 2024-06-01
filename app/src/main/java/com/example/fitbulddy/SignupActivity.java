package com.example.fitbulddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonSignUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUsername = findViewById(R.id.signup_username);
        editTextEmail = findViewById(R.id.signup_email);
        editTextPassword = findViewById(R.id.signup_password);
        editTextConfirmPassword = findViewById(R.id.signup_confirm_password);
        buttonSignUp = findViewById(R.id.signup_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username is required.");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required.");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address.");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required.");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password should be at least 6 characters.");
            editTextPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match.");
            editTextConfirmPassword.requestFocus();
            return;
        }

        // Register user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up successful, send email verification
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Create a map to store user data in Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("username", username);
                                userData.put("email", email);

                                // Get the document reference for the user
                                DocumentReference userRef = db.collection("users").document(user.getUid());

                                // Set the user data in Firestore
                                userRef.set(userData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> firestoreTask) {
                                                if (firestoreTask.isSuccessful()) {
                                                    // User data saved successfully
                                                    sendEmailVerification();
                                                } else {
                                                    // Error saving user data to Firestore
                                                    Toast.makeText(SignupActivity.this, "Failed to save user data: " + firestoreTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Sign up failed
                            Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> emailTask) {
                            if (emailTask.isSuccessful()) {
                                // Email verification sent
                                Toast.makeText(SignupActivity.this, "Signup successful. Verification email sent.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SignupActivity.this, WaitingActivity.class));
                                finish(); // Finish current activity
                            } else {
                                // Email verification failed
                                Toast.makeText(SignupActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}
