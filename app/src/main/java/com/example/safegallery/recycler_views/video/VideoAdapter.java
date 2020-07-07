package com.example.safegallery.recycler_views.video;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.safegallery.R;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoAdapter extends RecyclerView.Adapter<VideoViewHolder> {

    private Context context;
    private List<String> paths;

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder(
                LayoutInflater.from(context).inflate(R.layout.video_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        String path = paths.get(position);
        File f = new File(path);
        holder.videoView.setVideoURI(Uri.fromFile(f));
        holder.videoView.start();
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }
}
