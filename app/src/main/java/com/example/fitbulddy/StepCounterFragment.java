package com.example.fitbulddy;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StepCounterFragment extends Fragment implements SensorEventListener {

    private TextView textStepCount;

    private SensorManager sensorManager;

    private Sensor stepSensor;

    private int stepCount = 0;

    private FirebaseFirestore db;

    private static final double CALORIES_PER_STEP = 0.04;

    private static final double AVERAGE_STRIDE_LENGTH_CM = 70.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_counter, container, false);

        textStepCount = view.findViewById(R.id.textStepCount);

        db = FirebaseFirestore.getInstance();
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor == null) {
            // Step counter sensor is not available on this device
            Toast.makeText(requireContext(), "Step counter sensor not available", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == stepSensor) {
            stepCount = (int) event.values[0];
            textStepCount.setText("Step Count: " + stepCount);

            // Calculate calories burned and distance traveled
            double caloriesBurned = stepCount * CALORIES_PER_STEP;
            double distanceTraveled = stepCount * (AVERAGE_STRIDE_LENGTH_CM / 100000); // Convert cm to km

            // Save step count, calories burned, and distance traveled data to Firestore
            saveStepDataToFirestore(caloriesBurned, distanceTraveled);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy change if needed
    }

    private void saveStepDataToFirestore(double caloriesBurned, double distanceTraveled) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.e("StepCounterFragment", "User not authenticated");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String dateId = getCurrentDateId();

        DocumentReference stepDataRef = db.collection("user_steps")
                .document(userId)
                .collection("daily_steps")
                .document(dateId);

        Map<String, Object> data = new HashMap<>();
        data.put("steps", stepCount);
        data.put("caloriesBurned", caloriesBurned);
        data.put("distanceTraveled", distanceTraveled);
        data.put("timestamp", FieldValue.serverTimestamp());

        stepDataRef.set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("StepCounterFragment", "Step data saved successfully");
                        Toast.makeText(requireContext(), "Step data saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("StepCounterFragment", "Failed to save step data", task.getException());
                        Toast.makeText(requireContext(), "Failed to save step data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getCurrentDateId() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month starts from 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return String.format("%04d%02d%02d", year, month, day);
    }
}
