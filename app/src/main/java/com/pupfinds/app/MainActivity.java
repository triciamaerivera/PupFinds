package com.pupfinds.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);

        checkLoginState();
    }

    private void checkLoginState() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in, redirect to DashboardActivity
            startActivity(new Intent(this, DashboardActivityLost.class));
        } else {
            // No user is signed in, redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }

        // Finish MainActivity
        finish();
    }
}



