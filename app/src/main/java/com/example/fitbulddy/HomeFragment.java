package com.example.fitbulddy;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView textViewWelcome;
    private TextView textViewCaloriesBurned;
    private TextView textViewStepCount;
    private TextView textViewDistance;
    private TextView textViewBMI;

    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textViewWelcome = view.findViewById(R.id.textViewWelcome);
        textViewCaloriesBurned = view.findViewById(R.id.textViewCaloriesBurned);
        textViewStepCount = view.findViewById(R.id.textViewStepCount);
        textViewDistance = view.findViewById(R.id.textViewDistance);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchUserData();
        return view;
    }

    private void fetchUserData() {
        // Retrieve the user document from the Firestore "users" collection
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        // Display welcome message
                        textViewWelcome.setText("Welcome, " + username);

                        // Fetch daily step count and calories burned
                        fetchDailyStepCount();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore query failure
                    textViewBMI.setText("Failed to retrieve user data.");
                    Log.e("Firestore", "Error fetching user data: " + e.getMessage());
                });
    }

    private void fetchDailyStepCount() {
        // Get the current date in the format "yyyyMMdd"
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Construct the Firestore document path for daily step count
        String documentPath = "user_steps/" + userId + "/daily_steps/" + currentDate;

        db.document(documentPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        // Document exists, retrieve the step count
                        Long stepCount = documentSnapshot.getLong("steps");
                        Double caloriesBurned = documentSnapshot.getDouble("caloriesBurned");
                        Double distanceTraveled = documentSnapshot.getDouble("distanceTraveled");

                        if (stepCount != null) {
                            textViewStepCount.setText("Steps: " + stepCount);

                            // Calculate calories burned (assuming 0.04 calories per step)
                            textViewCaloriesBurned.setText("Calories: " + caloriesBurned +" cal");

                            // Display distance traveled in kilometers
                            if (distanceTraveled != null) {
                                String formattedDistance = String.format(Locale.getDefault(), "%.1f km", distanceTraveled);
                                textViewDistance.setText("Distance: " + formattedDistance);
                            } else {
                                textViewDistance.setText("Distance: 0");
                            }
                        }
                    } else {
                        // Document does not exist for the current date
                        textViewStepCount.setText("Steps: 0");
                        textViewCaloriesBurned.setText("Calories: 0");
                        textViewDistance.setText("Distance: 0");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore query failure
                    textViewStepCount.setText("Failed to retrieve step count data.");
                    textViewCaloriesBurned.setText("");
                    textViewDistance.setText("Distance: N/A");
                    Log.e("Firestore", "Error fetching step count data: " + e.getMessage());
                });
    }

}
