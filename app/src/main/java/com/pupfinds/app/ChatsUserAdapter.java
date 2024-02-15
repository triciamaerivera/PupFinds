package com.pupfinds.app;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsUserAdapter extends RecyclerView.Adapter<ChatsUserAdapter.MyViewHolder> {
    private Context context;
    private List<UserModel> userModelList;

    private TextView name, program;
    private CircleImageView profilePic;

    public ChatsUserAdapter(Context context) {
        this.context = context;
        this.userModelList = new ArrayList<>();
    }

    public void add(UserModel userModel) {
        userModelList.add(userModel);
        // notifyDataSetChanged();
    }

    public void addAll(List<UserModel> userModels) {
        userModelList.addAll(userModels);
        notifyDataSetChanged();
    }

    public void clear() {
        userModelList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatsUserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsUserAdapter.MyViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);
        String FirstName = userModel.getFirstName();
        String LastName = userModel.getLastName();
        String DisplayName = FirstName + " " + LastName;
        String uid = userModel.getUserUid();


        if (userModel.getProfileImageUrl() != null && !userModel.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(userModel.getProfileImageUrl()).into(profilePic);
        }

        name.setText(DisplayName);
        program.setText(userModel.getProgram());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ConvoActivity.class);
                intent.putExtra("current_user_uid", FirebaseAuth.getInstance().getUid());
                intent.putExtra("receiver_uid", userModel.getUserUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public List<UserModel> getUserModelList(){
        return userModelList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.imageViewReceiverPic);
            name = itemView.findViewById(R.id.textviewUserName);
            program = itemView.findViewById(R.id.textviewProgram);
        }
    }

        public void sortByLastMessageTimestamp() {
        Collections.sort(userModelList, new Comparator<UserModel>() {
            @Override
            public int compare(UserModel user1, UserModel user2) {
                long timestamp1 = user1.getLastMessageTimestamp();
                long timestamp2 = user2.getLastMessageTimestamp();
                return Long.compare(timestamp2, timestamp1);
            }
        });
    }
}