package com.pupfinds.app;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.pupfinds.app.FoundItemAdapter;


public class FoundItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView itemImageView;
    private final TextView itemNameTextView;
    private final TextView itemLocationTextView;
    private FoundItemAdapter.AdapterOnItemClickListener listener;

    private FoundItem foundItem;

    public FoundItemViewHolder(@NonNull View itemView, FoundItemAdapter.AdapterOnItemClickListener listener) {
        super(itemView);
        this.listener = listener;
        itemImageView = itemView.findViewById(R.id.itemImageView);
        itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
        itemLocationTextView = itemView.findViewById(R.id.itemLocationTextView);

        itemView.setOnClickListener(this);
    }

    public void bind(FoundItem foundItem) {
        this.foundItem = foundItem;

        if (foundItem.getImageUrl() != null && !foundItem.getImageUrl().isEmpty()) {
            Picasso.get().load(foundItem.getImageUrl()).into(itemImageView);
        } else {
            // Set a placeholder image or handle the case where the image URL is empty.
            itemImageView.setImageResource(R.drawable.placeholder_image);
        }


        itemNameTextView.setText(foundItem.getName());
        itemLocationTextView.setText(foundItem.getLocation());
    }

    @Override
    public void onClick(View v) {
        if (listener != null && foundItem != null) {
            listener.onItemClick(foundItem);
        }
    }
}














