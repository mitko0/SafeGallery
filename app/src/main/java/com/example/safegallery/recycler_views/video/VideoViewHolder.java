package com.example.safegallery.recycler_views.video;

import android.view.View;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;

public class VideoViewHolder extends RecyclerView.ViewHolder {

    VideoView videoView;

    public VideoViewHolder(@NonNull View itemView) {
        super(itemView);

        this.videoView = itemView.findViewById(R.id.video);
    }
}
