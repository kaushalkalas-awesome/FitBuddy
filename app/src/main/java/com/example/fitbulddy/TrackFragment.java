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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrackFragment extends Fragment {

    private static final String TAG = "TrackFragment";

    private BarChart barChartSteps;
    private BarChart barChartCalories;
    private BarChart barChartDistance;
    private PieChart pieChart;

    private TextView textViewNoData;

    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track, container, false);

        barChartSteps = view.findViewById(R.id.barChartStepCount);
        barChartCalories = view.findViewById(R.id.barChartCaloriesBurned);
        barChartDistance = view.findViewById(R.id.barChartDistanceTraveled);
        textViewNoData = view.findViewById(R.id.textViewNoData);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        pieChart = view.findViewById(R.id.pieChart);

        fetchPieChartData();

        fetchDailyStepCounts();
        fetchDailyCaloriesBurned();
        fetchDailyDistanceTraveled();

        return view;
    }

    private void fetchPieChartData() {
        // Get the current date in the format "yyyyMMdd"
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Construct the Firestore document path for today's data
        String documentPath = "user_steps/" + userId + "/daily_steps/" + currentDate;

        db.document(documentPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        float totalSteps = 0f;
                        float totalCaloriesBurned = 0f;
                        float totalDistanceTraveled = 0f;

                        Long steps = documentSnapshot.getLong("steps");
                        Double caloriesBurned = documentSnapshot.getDouble("caloriesBurned");
                        Double distanceTraveled = documentSnapshot.getDouble("distanceTraveled");

                        if (steps != null) {
                            totalSteps = steps.floatValue();
                        }

                        if (caloriesBurned != null) {
                            totalCaloriesBurned = caloriesBurned.floatValue();
                        }

                        if (distanceTraveled != null) {
                            float distanceTraveledMeters = (float) (distanceTraveled * 1000);
                            totalDistanceTraveled += distanceTraveledMeters;
                        }

                        // Create PieEntries for each category
                        List<PieEntry> entries = new ArrayList<>();
                        entries.add(new PieEntry(totalSteps, "Steps"));
                        entries.add(new PieEntry(totalCaloriesBurned, "Calories"));
                        entries.add(new PieEntry(totalDistanceTraveled, "Distance (m)"));

                        // Create a PieDataSet with the entries
                        PieDataSet dataSet = new PieDataSet(entries, "");
                        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

                        // Create PieData and set it to the PieChart
                        PieData pieData = new PieData(dataSet);
                        pieChart.setData(pieData);
                        pieChart.getDescription().setEnabled(false);
                        pieChart.animateY(1000);

                        // Configure legend and invalidate chart
                        Legend legend = pieChart.getLegend();
                        legend.setForm(Legend.LegendForm.SQUARE);
                        legend.setFormSize(12f);
                        legend.setTextSize(12f);
                        legend.setXEntrySpace(10f);

                        pieChart.invalidate(); // Refresh the pie chart
                    } else {
                        // Handle case where no data is available
                        showNoDataMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching pie chart data: " + e.getMessage());
                    showNoDataMessage();
                });
    }



    private void showNoDataMessage() {
        // Make the "No Data" message visible and hide the chart views
        textViewNoData.setVisibility(View.VISIBLE);
        barChartSteps.setVisibility(View.GONE);
        barChartCalories.setVisibility(View.GONE);
        barChartDistance.setVisibility(View.GONE);
    }


    private void fetchDailyStepCounts() {
        // Get the current date in the format "yyyyMMdd"
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Construct the Firestore document path for daily step counts
        String documentPath = "user_steps/" + userId + "/daily_steps/";

        db.collection(documentPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<BarEntry> entries = new ArrayList<>();
                        List<String> labels = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            long stepCount = document.getLong("steps");
                            String date = document.getId();

                            // Convert date "yyyyMMdd" to day of the week
                            String dayOfWeek = convertDateToDayOfWeek(date);

                            entries.add(new BarEntry(entries.size(), stepCount));
                            labels.add(dayOfWeek);
                        }

                        if (!entries.isEmpty()) {
                            displayBarChart(barChartSteps, entries, labels, "Steps");
                        } else {
                            textViewNoData.setVisibility(View.VISIBLE);
                            barChartSteps.setVisibility(View.GONE);
                        }
                    } else {
                        textViewNoData.setVisibility(View.VISIBLE);
                        barChartSteps.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching daily step counts: " + e.getMessage());
                });
    }

    private void fetchDailyCaloriesBurned() {
        // Get the current date in the format "yyyyMMdd"
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Construct the Firestore document path for daily calories burned
        String documentPath = "user_steps/" + userId + "/daily_steps/";

        db.collection(documentPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<BarEntry> entries = new ArrayList<>();
                        List<String> labels = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Double caloriesBurned = document.getDouble("caloriesBurned");
                            String date = document.getId();

                            // Convert date "yyyyMMdd" to day of the week
                            String dayOfWeek = convertDateToDayOfWeek(date);

                            if (caloriesBurned != null) {
                                entries.add(new BarEntry(entries.size(), caloriesBurned.floatValue()));
                                labels.add(dayOfWeek);
                            }
                        }

                        if (!entries.isEmpty()) {
                            displayBarChart(barChartCalories, entries, labels, "Calories Burned");
                        } else {
                            // Handle case where no data is available
                            textViewNoData.setVisibility(View.VISIBLE);
                            barChartCalories.setVisibility(View.GONE);
                        }
                    } else {
                        // Handle case where no data is available
                        textViewNoData.setVisibility(View.VISIBLE);
                        barChartCalories.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching daily calories burned: " + e.getMessage());
                });
    }

    private void fetchDailyDistanceTraveled() {
        // Get the current date in the format "yyyyMMdd"
        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Construct the Firestore document path for daily distance traveled
        String documentPath = "user_steps/" + userId + "/daily_steps/";

        db.collection(documentPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<BarEntry> entries = new ArrayList<>();
                        List<String> labels = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Double distanceTraveled = document.getDouble("distanceTraveled");
                            String date = document.getId();

                            // Convert date "yyyyMMdd" to day of the week
                            String dayOfWeek = convertDateToDayOfWeek(date);

                            if (distanceTraveled != null) {
                                entries.add(new BarEntry(entries.size(), distanceTraveled.floatValue()));
                                labels.add(dayOfWeek);
                            }
                        }

                        if (!entries.isEmpty()) {
                            displayBarChart(barChartDistance, entries, labels, "Distance Traveled (km)");
                        } else {
                            // Handle case where no data is available
                            textViewNoData.setVisibility(View.VISIBLE);
                            barChartDistance.setVisibility(View.GONE);
                        }
                    } else {
                        // Handle case where no data is available
                        textViewNoData.setVisibility(View.VISIBLE);
                        barChartDistance.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching daily distance traveled: " + e.getMessage());
                });
    }

    private String convertDateToDayOfWeek(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            // Map day of the week value to day name
            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    return "Sun";
                case Calendar.MONDAY:
                    return "Mon";
                case Calendar.TUESDAY:
                    return "Tue";
                case Calendar.WEDNESDAY:
                    return "Wed";
                case Calendar.THURSDAY:
                    return "Thu";
                case Calendar.FRIDAY:
                    return "Fri";
                case Calendar.SATURDAY:
                    return "Sat";
                default:
                    return "";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting date to day of week: " + e.getMessage());
            return "";
        }
    }

    private void displayBarChart(BarChart barChart, List<BarEntry> entries, List<String> labels, String dataSetLabel) {
        BarDataSet dataSet = new BarDataSet(entries, dataSetLabel);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // Set colors for the bars
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Customize X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.animateY(1000);
        xAxis.setGranularity(1f); // Minimum interval between labels
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Use IndexAxisValueFormatter with labels

        barChart.invalidate(); // Refresh the chart
    }
}
