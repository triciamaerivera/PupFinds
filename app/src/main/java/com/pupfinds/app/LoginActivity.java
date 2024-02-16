package com.pupfinds.app;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;
import android.text.TextUtils;


public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // References to the email and password EditText fields
        EditText emailEditText = findViewById(R.id.editTextTextEmailAddress);
        EditText passwordEditText = findViewById(R.id.editTextPassword);

        // Reference to login button
        Button loginButton = findViewById(R.id.buttonLogin);

        // Set OnClickListener for the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Perform login operation
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Login success, navigate to DashboardActivity
                                    startActivity(new Intent(LoginActivity.this, DashboardActivityLost.class));
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        // Find the "Forgot Password" TextView
        TextView forgotPasswordTextView = findViewById(R.id.forgot_password);
        // Find the "Create Account" TextView
        TextView createAccountTextView = findViewById(R.id.textViewCreateAccount);

        // Set an OnClickListener for the "Forgot Password" TextView
        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the email entered by the user
                String email = emailEditText.getText().toString().trim();

                // Check if the email is empty
                if (!TextUtils.isEmpty(email)) {
                    // Send a password reset email to the user
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Password reset email sent successfully
                                        Toast.makeText(LoginActivity.this, "Check your email for a password reset link if you're registered with us", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Failed to send password reset email
                                        Toast.makeText(LoginActivity.this, "Check your email for a password reset link if you're registered with us", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // Display a message indicating that the email field is empty
                    Toast.makeText(LoginActivity.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set an OnClickListener for the "Create Account" TextView
        createAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the RegistrationActivity when "Create Account" is clicked
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });
    }
}
