package com.example.fitbulddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView nameTextView, addressTextView, emailTextView, heightTextView, weightTextView, phoneTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameTextView = view.findViewById(R.id.nameTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        heightTextView = view.findViewById(R.id.heightTextView);
        weightTextView = view.findViewById(R.id.weightTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);

        Button logoutButton = view.findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle logout
                mAuth.signOut();
                // Navigate to login screen
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish(); // Finish the current activity
            }
        });

        // Get current user's UID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // Retrieve user data from Firestore
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // User document exists, retrieve and display user data
                        String name = documentSnapshot.getString("name");
                        String address = documentSnapshot.getString("address");
                        String email = documentSnapshot.getString("email");
                        String height = documentSnapshot.getString("height");
                        String weight = documentSnapshot.getString("weight");
                        String phone = documentSnapshot.getString("phone");

                        // Set user data to TextViews
                        nameTextView.setText("Name: " + name);
                        addressTextView.setText("Address: " + address);
                        emailTextView.setText("Email: " + email);
                        heightTextView.setText("Height: " + height);
                        weightTextView.setText("Weight: " + weight);
                        phoneTextView.setText("Phone: " + phone);
                    } else {
                        // User document does not exist
                        Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed to retrieve user data
                    Toast.makeText(getActivity(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        return view;
    }
}
