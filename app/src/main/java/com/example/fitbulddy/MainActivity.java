package com.example.fitbulddy;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private Map<Integer, Class<? extends Fragment>> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize fragment map with menu item IDs and corresponding fragment classes
        fragmentMap.put(R.id.nav_home, HomeFragment.class);
        fragmentMap.put(R.id.nav_feed, FeedFragment.class);
        fragmentMap.put(R.id.nav_step_counter, StepCounterFragment.class);
        fragmentMap.put(R.id.nav_track, TrackFragment.class);
        fragmentMap.put(R.id.nav_settings, SettingsFragment.class);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            // Initial fragment setup (e.g., HomeFragment)
            showFragment(HomeFragment.class);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // Check if the selected item has a mapped fragment class
                    Class<? extends Fragment> fragmentClass = fragmentMap.get(item.getItemId());
                    if (fragmentClass != null) {
                        showFragment(fragmentClass);
                        return true;
                    }
                    return false;
                }
            };

    private void showFragment(Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null); // Add to back stack for state management
            transaction.commit();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
