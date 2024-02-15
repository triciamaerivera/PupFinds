package com.pupfinds.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConvoActivity extends AppCompatActivity {
    DatabaseReference dbReferenceSender, dbReferenceReceiver, userReference;
    RecyclerView recyclerView;
    ConvoAdapter convoAdapter;
    String receiverID, senderID;
    ImageView buttonSend;
    EditText textMessage;
    CircleImageView imageViewProfilePic;
    TextView textViewReceiverName, textViewReceiverProgram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convo);

        senderID = FirebaseAuth.getInstance().getUid();
        receiverID = getIntent().getStringExtra("item_owner_uid");

        Toolbar toolbarConvo = findViewById(R.id.toolbarConvo);
        setSupportActionBar(toolbarConvo);

        userReference = FirebaseDatabase.getInstance().getReference("users");

        String senderRoom = senderID + receiverID;
        String receiverRoom = receiverID + senderID;

        imageViewProfilePic = findViewById(R.id.imageViewProfilePic);
        textViewReceiverName = findViewById(R.id.textViewReceiverName);
        textViewReceiverProgram = findViewById(R.id.textViewReceiverProgram);

        buttonSend = findViewById(R.id.buttonSend);
        convoAdapter = new ConvoAdapter(this);
        recyclerView = findViewById(R.id.recyclerConvo);
        textMessage = findViewById(R.id.editMessage);
        recyclerView.setAdapter(convoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbReferenceSender = FirebaseDatabase.getInstance().getReference("chats").child(senderRoom);
        dbReferenceReceiver = FirebaseDatabase.getInstance().getReference("chats").child(receiverRoom);

        dbReferenceSender.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MessageModel> messages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                    messages.add(messageModel);
                }

                messages.sort((message1, message2) -> Long.compare(message1.getTimestamp(), message2.getTimestamp()));

                convoAdapter.clear();
                convoAdapter.addAll(messages);
                convoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });

        loadReceiverInfo(receiverID);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textMessage.getText().toString();
                if (message.trim().length() > 0) {
                    SendMessage(message);
                } else {
                    Toast.makeText(ConvoActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }

            private void SendMessage(String message) {
                String messageId = UUID.randomUUID().toString();
                long currentTime = System.currentTimeMillis();
                MessageModel messageModel = new MessageModel(messageId, senderID, receiverID, message, currentTime);

                convoAdapter.add(messageModel);
                recyclerView.scrollToPosition(convoAdapter.getItemCount() - 1);
                textMessage.setText("");

                dbReferenceSender.child(messageId).setValue(messageModel)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Handle success
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ConvoActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                            }
                        });

                dbReferenceReceiver.child(messageId).setValue(messageModel);
            }
        });
    }

    private void loadReceiverInfo(String receiverUID) {
        DatabaseReference userProfileRef = FirebaseDatabase.getInstance().getReference("users").child(receiverUID);

        userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        // Add the displayName field to the UserProfile object
                        userProfile.setDisplayName(userProfile.getFirstName() + " " + userProfile.getLastName());
                        // Update the user profile information in the views
                        updateUserProfileViews(userProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // To-do: error handling
            }
        });
    }



    private void updateUserProfileViews(UserProfile userProfile) {
        // Find views for user profile information

        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(userProfile.getProfileImageUrl()).into(imageViewProfilePic);
        }
        textViewReceiverProgram.setText(userProfile.getProgram());
        textViewReceiverName.setText(userProfile.getDisplayName());
    }

//    private void loadReceiverInfo(String receiverUID) {
//        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(receiverUID);
//        userReference.child(receiverUID).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    UserProfile receiverUser = dataSnapshot.getValue(UserProfile.class);
//                    if (receiverUser != null) {
//                        Log.d("Debug", "User Data Exists");
//                        Log.d("Debug", "Display Name: " + receiverUser.getDisplayName());
//                        Log.d("Debug", "Program: " + receiverUser.getProgram());
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                textViewReceiverName.setText(receiverUser.getDisplayName());
//                                textViewReceiverProgram.setText(receiverUser.getProgram());
//                            }
//                        });
//                    } else {
//                        Log.d("Debug", "UserProfile is null");
//                    }
//                } else {
//                    Log.d("Debug", "DataSnapshot does not exist");
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("onCancelled", "Error reading data: " + error.getMessage());
//                // Handle onCancelled
//            }
//        });
//
//    }



}
