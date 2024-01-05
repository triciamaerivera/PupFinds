package com.pupfinds.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoundItemAdapter extends RecyclerView.Adapter<FoundItemViewHolder> {

    private List<FoundItem> foundItems;
    private AdapterOnItemClickListener listener;

    public FoundItemAdapter(List<FoundItem> foundItems, AdapterOnItemClickListener listener) {
        this.foundItems = foundItems;
        this.listener = listener;
    }

    public interface AdapterOnItemClickListener {
        void onItemClick(FoundItem item);
    }

    public void setFoundItems(List<FoundItem> foundItems) {
        this.foundItems = foundItems;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public FoundItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_item_layout, parent, false);
        return new FoundItemViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FoundItemViewHolder holder, int position) {
        holder.bind(foundItems.get(position));
    }

    @Override
    public int getItemCount() {
        return foundItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(FoundItem item);
    }
}











