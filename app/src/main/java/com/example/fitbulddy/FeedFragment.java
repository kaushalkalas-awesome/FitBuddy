package com.example.fitbulddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerViewFeed;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedItems;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        recyclerViewFeed = view.findViewById(R.id.recyclerViewFeed);
        recyclerViewFeed.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Create sample feed items
        feedItems = new ArrayList<>();
        feedItems.add(new FeedItem(R.drawable.img_7, "Boost Your Cardio Endurance", "Improve your cardiovascular endurance by incorporating interval training into your workouts. Alternate between periods of high intensity and recovery to challenge your heart and lungs. Try sprint intervals on the treadmill or cycling at varying speeds. Consistent cardio workouts will enhance your stamina and overall fitness level."));
        feedItems.add(new FeedItem(R.drawable.img_8, "Strength Training Essentials", "Build strength and muscle tone with compound exercises like squats, deadlifts, and bench presses. Focus on major muscle groups and use proper form to prevent injury. Incorporate resistance training 2-3 times per week to see improvements in strength, metabolism, and bone health."));
        feedItems.add(new FeedItem(R.drawable.img_9, "Hydration and Exercise Performance", "Stay hydrated before, during, and after workouts to optimize exercise performance. Dehydration can lead to fatigue, cramps, and reduced endurance. Aim to drink water consistently throughout the day and replenish electrolytes during intense exercise sessions."));
        feedItems.add(new FeedItem(R.drawable.img_10, "Rest and Recovery Importance", "Allow your body adequate time to recover between workouts. Rest days are essential for muscle repair and growth. Listen to your body's signals and prioritize quality sleep, as it plays a crucial role in recovery and overall well-being."));
        feedItems.add(new FeedItem(R.drawable.img_11, "Master the Plank for Core Strength", "The plank is a highly effective exercise for strengthening your core muscles. Hold a plank position for 30-60 seconds, engaging your abs, glutes, and back. Gradually increase duration and experiment with side planks and variations to target different areas of your core."));

        // Initialize the adapter with the list of feed items
        feedAdapter = new FeedAdapter(getContext(), feedItems);
        recyclerViewFeed.setAdapter(feedAdapter);

        return view;
    }
}
