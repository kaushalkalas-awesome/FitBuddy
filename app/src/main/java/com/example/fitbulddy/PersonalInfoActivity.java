package com.example.fitbulddy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PersonalInfoActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextAddress, editTextWeight, editTextHeight, editTextAge;
    private RadioGroup radioGroupGender;
    private Button buttonSave;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextHeight = findViewById(R.id.editTextHeight);
        editTextAge = findViewById(R.id.editTextAge);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        buttonSave = findViewById(R.id.buttonSave);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePersonalInfo();
            }
        });
    }

    private void savePersonalInfo() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String weight = editTextWeight.getText().toString().trim();
        String height = editTextHeight.getText().toString().trim();
        String age = editTextAge.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address) ||
                TextUtils.isEmpty(weight) || TextUtils.isEmpty(height) || TextUtils.isEmpty(age)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected gender
        String gender = getSelectedRadioButtonText();

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        // Create a map to update user document with personal info
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        userInfo.put("address", address);
        userInfo.put("weight", weight);
        userInfo.put("height", height);
        userInfo.put("age", age);
        userInfo.put("gender", gender);

        // Update the user document in Firestore
        userRef.update(userInfo)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(PersonalInfoActivity.this, "Personal info saved", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PersonalInfoActivity.this, LoginActivity.class));
                            finish(); // Finish activity and go to main screen
                        } else {
                            Toast.makeText(PersonalInfoActivity.this, "Failed to save personal info", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String getSelectedRadioButtonText() {
        RadioButton selectedRadioButton = findViewById(radioGroupGender.getCheckedRadioButtonId());
        if (selectedRadioButton != null) {
            return selectedRadioButton.getText().toString();
        } else {
            return ""; // Return empty string if no radio button is selected
        }
    }
}
