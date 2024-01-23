package com.pupfinds.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

        // Check if the user is already authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already authenticated, redirect to the dashboard
            redirectToDashboard();
        }

        // Initialize the AuthStateListener
        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                // User is authenticated, redirect to the dashboard
                redirectToDashboard();
            }
        };

        // Add the AuthStateListener
        mAuth.addAuthStateListener(authStateListener);

        imageButtonProfile = findViewById(R.id.imageButtonProfile);
        imageButtonProfile.setOnClickListener(v -> openFileChooser());

        Button registerButton = findViewById(R.id.buttonCreateAccount);
        registerButton.setOnClickListener(v -> registerUser());
    }


    private void registerUser() {
        String email = ((EditText) findViewById(R.id.editTextTextEmailAddress2)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.editTextTextPassword)).getText().toString().trim();
        String firstName = ((EditText) findViewById(R.id.editTextFirstName)).getText().toString().trim();
        String lastName = ((EditText) findViewById(R.id.editTextLastName)).getText().toString().trim();
        String middleName = ((EditText) findViewById(R.id.editTextMiddleName)).getText().toString().trim();
        String suffix = ((EditText) findViewById(R.id.editTextSuffix)).getText().toString().trim();
        String contactNumber = ((EditText) findViewById(R.id.editTextContactNumber)).getText().toString().trim();
        String studentId = ((EditText) findViewById(R.id.editTextStudentID)).getText().toString().trim();
        String college = ((EditText) findViewById(R.id.editTextCollege)).getText().toString().trim();
        String program = ((EditText) findViewById(R.id.editTextProgram)).getText().toString().trim();

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




