package com.pupfinds.app;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.pupfinds.app.LostItemAdapter;


public class LostItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView itemImageView;
    private final TextView itemNameTextView;
    private final TextView itemLocationTextView;
    private LostItemAdapter.AdapterOnItemClickListener listener;

    private LostItem lostItem;

    public LostItemViewHolder(@NonNull View itemView, LostItemAdapter.AdapterOnItemClickListener listener) {
        super(itemView);
        this.listener = listener;
        itemImageView = itemView.findViewById(R.id.itemImageView);
        itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
        itemLocationTextView = itemView.findViewById(R.id.itemLocationTextView);

        itemView.setOnClickListener(this);
    }

    public void bind(LostItem lostItem) {
        this.lostItem = lostItem;

        if (lostItem.getImageUrl() != null && !lostItem.getImageUrl().isEmpty()) {
            Picasso.get().load(lostItem.getImageUrl()).into(itemImageView);
        } else {
            // Set a placeholder image or handle the case where the image URL is empty.
            itemImageView.setImageResource(R.drawable.placeholder_image);
        }


        itemNameTextView.setText(lostItem.getName());
        itemLocationTextView.setText(lostItem.getLocation());
    }

    @Override
    public void onClick(View v) {
        if (listener != null && lostItem != null) {
            listener.onItemClick(lostItem);
        }
    }
}