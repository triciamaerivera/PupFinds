package com.pupfinds.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LostItemAdapter extends RecyclerView.Adapter<LostItemViewHolder> {

    private List<LostItem> lostItems;
    private AdapterOnItemClickListener listener;

    public LostItemAdapter(List<LostItem> lostItems, AdapterOnItemClickListener listener) {
        this.lostItems = lostItems;
        this.listener = listener;
    }

    public interface AdapterOnItemClickListener {
        void onItemClick(LostItem item);
    }

    public void setLostItems(List<LostItem> lostItems) {
        this.lostItems = lostItems;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public LostItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lost_item_layout, parent, false);
        return new LostItemViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull LostItemViewHolder holder, int position) {
        holder.bind(lostItems.get(position));
    }

    @Override
    public int getItemCount() {
        return lostItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(LostItem item);
    }
}











