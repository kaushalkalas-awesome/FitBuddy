package com.example.fitbulddy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WaitingActivity extends AppCompatActivity {

    private static final int CHECK_INTERVAL_MS = 3000; // Check interval (3 seconds)
    private Handler handler;
    private Runnable checkVerificationRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting_activity);

        handler = new Handler();
        checkVerificationRunnable = new Runnable() {
            @Override
            public void run() {
                checkEmailVerification();
            }
        };

        // Start checking for email verification status
        handler.postDelayed(checkVerificationRunnable, CHECK_INTERVAL_MS);
    }

    private void checkEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if (user.isEmailVerified()) {
                            // Email is verified, proceed to PersonalInfoActivity
                            Log.d("WaitingActivity", "Email is verified. Redirecting to PersonalInfoActivity.");
                            startActivity(new Intent(WaitingActivity.this, PersonalInfoActivity.class));
                            finish(); // Finish waiting activity
                        } else {
                            // Email is not verified yet, continue waiting
                            Log.d("WaitingActivity", "Email is not verified. Continuing to wait.");
                            handler.postDelayed(checkVerificationRunnable, CHECK_INTERVAL_MS);
                        }
                    } else {
                        // Reload failed, handle error if needed
                        Log.e("WaitingActivity", "Failed to reload user data: " + task.getException().getMessage());
                        Toast.makeText(WaitingActivity.this, "Failed to check email verification status", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button press navigation
        // You can add a message or perform an action here if needed
        Toast.makeText(this, "Back button disabled", Toast.LENGTH_SHORT).show();
        // Remove the super call to disable back button navigation
        // super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the handler callbacks to prevent memory leaks
        if (handler != null && checkVerificationRunnable != null) {
            handler.removeCallbacks(checkVerificationRunnable);
        }
    }
}
