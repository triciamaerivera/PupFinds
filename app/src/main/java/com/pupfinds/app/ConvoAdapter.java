package com.pupfinds.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ConvoAdapter extends RecyclerView.Adapter<ConvoAdapter.MyViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private Context context;
    private List<MessageModel> messageModelList;

    public ConvoAdapter(Context context) {
        this.context = context;
        this.messageModelList = new ArrayList<>();
    }

    public void add(MessageModel messageModel) {
        messageModelList.add(messageModel);
        notifyItemInserted(messageModelList.size() - 1);
    }

    public void addAll(List<MessageModel> messages) {
        messageModelList.addAll(messages);
        notifyDataSetChanged();
    }

    public void clear() {
        messageModelList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConvoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_sent, parent, false);
            return new MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row_recieved, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ConvoAdapter.MyViewHolder holder, int position) {
        MessageModel messageModel = messageModelList.get(position);

        if (messageModel.getSenderID().equals(FirebaseAuth.getInstance().getUid())) {
            holder.textViewSentMessage.setText(messageModel.getMessage());
        } else {
            holder.textViewReceivedMessage.setText(messageModel.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModelList.get(position).getSenderID().equals(FirebaseAuth.getInstance().getUid())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSentMessage, textViewReceivedMessage;

        public MyViewHolder(View itemView) {
            super(itemView);
            textViewSentMessage = itemView.findViewById(R.id.TextViewSentMessage);
            textViewReceivedMessage = itemView.findViewById(R.id.TextViewReceivedMessage);
        }
    }
}
