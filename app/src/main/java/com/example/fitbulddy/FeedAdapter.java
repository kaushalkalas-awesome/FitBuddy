package com.example.fitbulddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<FeedItem> feedItems;

    public FeedAdapter(Context context, List<FeedItem> feedItems) {
        this.context = context;
        this.feedItems = feedItems;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem feedItem = feedItems.get(position);

        holder.imageView.setImageResource(feedItem.getImageResource());
        holder.textViewTitle.setText(feedItem.getTitle());
        holder.textViewDescription.setText(feedItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewTitle;
        TextView textViewDescription;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }
    }
}
