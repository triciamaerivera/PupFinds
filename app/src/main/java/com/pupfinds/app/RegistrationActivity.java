package com.pupfinds.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class RegistrationActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth mAuth;
    private CircleImageView imageButtonProfile;
    private Uri imageUri;
    private StorageReference storageReference;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        imageButtonProfile = findViewById(R.id.imageButtonProfile);
        imageButtonProfile.setOnClickListener(v -> openFileChooser());

        Button registerButton = findViewById(R.id.buttonCreateAccount);
        registerButton.setOnClickListener(v -> registerUser());

        // Add focus change listeners for password and confirm password fields
        EditText passwordEditText = findViewById(R.id.editTextTextPassword);
        EditText confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword);
        EditText firstNameEditText = findViewById(R.id.editTextFirstName);
        EditText lastNameEditText = findViewById(R.id.editTextLastName);
        EditText studentIdEditText = findViewById(R.id.editTextStudentID);
        EditText emailEditText = findViewById(R.id.editTextTextEmailAddress2);
        EditText contactEditText = findViewById(R.id.editTextContactNumber);
        EditText collegeEditText = findViewById(R.id.editTextCollege);
        EditText programEditText = findViewById(R.id.editTextProgram);

        // Set OnFocusChangeListener for email field
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail(emailEditText);
            }
        });

        // Set OnFocusChangeListener for contact field
        contactEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateField(contactEditText);
            }
        });

        // Set OnFocusChangeListener for college field
        collegeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateField(collegeEditText);
            }
        });

        // Set OnFocusChangeListener for program field
        programEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateField(programEditText);
            }
        });

        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePassword(passwordEditText, confirmPasswordEditText);
            }
        });

        confirmPasswordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // Check if the user has attempted to input something into the confirm password field
                if (confirmPasswordEditText.getTag() != null && (boolean) confirmPasswordEditText.getTag()) {
                    validatePassword(passwordEditText, confirmPasswordEditText);
                }
            }
        });

        // Add text change listener for confirm password field
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Set a flag to indicate that the user has attempted to input something into the confirm password field
                confirmPasswordEditText.setTag(true);
            }
        });

        // Add focus change listeners for first name, last name, contact, and student ID fields
        firstNameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateField(firstNameEditText);
            }
        });

        lastNameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateField(lastNameEditText);
            }
        });


        studentIdEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateField(studentIdEditText);
                validateStudentIDFormat(studentIdEditText);
            }
        });
    }
    // Method to validate email format
    private void validateEmail(EditText emailEditText) {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email address");
        } else {
            emailEditText.setError(null); // Clear the error if present
        }
    }

    // Method to validate first name, last name, contact, and student ID fields
    private void validateField(EditText editText) {
        String fieldValue = editText.getText().toString().trim();

        if (TextUtils.isEmpty(fieldValue)) {
            editText.setError("This field is required");
        } else {
            editText.setError(null); // Clear the error if present
        }
    }


    // Method to validate password and confirm password
    private void validatePassword(EditText passwordEditText, EditText confirmPasswordEditText) {
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters long");
        } else {
            passwordEditText.setError(null); // Clear the error if present
        }

        if (!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError("Passwords do not match");
        } else {
            confirmPasswordEditText.setError(null);
        }
    }

    // Method to validate the format of the Student ID
    private void validateStudentIDFormat(EditText studentIdEditText) {
        String studentId = studentIdEditText.getText().toString().trim();

        // Define the pattern for the Student ID format
        String pattern = "\\d{4}-\\d{5}-[A-Z]{2}-\\d";

        // Check if the provided Student ID matches the pattern
        if (!studentId.matches(pattern)) {
            // Display an error if the format is invalid
            studentIdEditText.setError("Invalid Student ID format");
        } else {
            // Clear any previous error if the format is valid
            studentIdEditText.setError(null);
        }
    }



    private void registerUser() {
        String email = ((EditText) findViewById(R.id.editTextTextEmailAddress2)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.editTextTextPassword)).getText().toString().trim();
        String confirmPassword = ((EditText) findViewById(R.id.editTextConfirmPassword)).getText().toString().trim();
        String firstName = ((EditText) findViewById(R.id.editTextFirstName)).getText().toString().trim();
        String lastName = ((EditText) findViewById(R.id.editTextLastName)).getText().toString().trim();
        String middleName = ((EditText) findViewById(R.id.editTextMiddleName)).getText().toString().trim();
        String suffix = ((EditText) findViewById(R.id.editTextSuffix)).getText().toString().trim();
        String contactNumber = ((EditText) findViewById(R.id.editTextContactNumber)).getText().toString().trim();
        String studentId = ((EditText) findViewById(R.id.editTextStudentID)).getText().toString().trim();
        String college = ((EditText) findViewById(R.id.editTextCollege)).getText().toString().trim();
        String program = ((EditText) findViewById(R.id.editTextProgram)).getText().toString().trim();


        // Check if the password is at least 6 characters long
        if (password.length() < 6) {
            // Display an error message if the password is not six characters long
            Toast.makeText(RegistrationActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the password and confirm password match
        if (!password.equals(confirmPassword)) {
            // Display an error message if the passwords do not match
            Toast.makeText(RegistrationActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (imageUri != null) {
                            uploadImageToFirebase(email, firstName, lastName, middleName, suffix, contactNumber, studentId, college, program);
                        } else {
                            saveUserInfo(email, null, firstName, lastName, middleName, suffix, contactNumber, studentId, college, program);
                        }

                        // Registration is successful, redirect to the login page
                        Toast.makeText(RegistrationActivity.this, "Registration successful. Redirecting to login.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void uploadImageToFirebase(String email, String firstName, String lastName, String middleName, String suffix, String contactNumber, String studentId, String college, String program) {
        StorageReference fileRef = storageReference.child("profile_images").child(email + "_profile.jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveUserInfo(email, uri.toString(), firstName, lastName, middleName, suffix, contactNumber, studentId, college, program)))
                .addOnFailureListener(e -> {
                    Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveUserInfo(email, null, firstName, lastName, middleName, suffix, contactNumber, studentId, college, program);
                });
    }

    private void saveUserInfo(String email, String imageUrl, String firstName, String lastName, String middleName, String suffix, String contactNumber, String studentId, String college, String program) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("email", email);
            userInfo.put("firstName", firstName);
            userInfo.put("lastName", lastName);
            userInfo.put("middleName", middleName);
            userInfo.put("suffix", suffix);
            userInfo.put("contactNumber", contactNumber);
            userInfo.put("studentId", studentId);
            userInfo.put("college", college);
            userInfo.put("program", program);
            if (imageUrl != null) {
                userInfo.put("profileImageUrl", imageUrl);
            }

            databaseReference.setValue(userInfo);

        }
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageButtonProfile.setImageURI(imageUri);
        }
    }


    private void redirectToDashboard() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(RegistrationActivity.this, DashboardActivityFound.class));
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the AuthStateListener when the activity is destroyed to avoid memory leaks
        if (mAuth != null && authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

}




