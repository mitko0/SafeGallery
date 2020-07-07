package com.example.safegallery.recycler_views.defaultt;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;

public class DefaultViewHolder extends RecyclerView.ViewHolder {

    ImageView img;
    ImageView icon;
    TextView folderName;
    TextView fileName;

    public DefaultViewHolder(@NonNull View itemView) {
        super(itemView);
        img = itemView.findViewById(R.id.defImage);
        icon = itemView.findViewById(R.id.imgVideo);
        folderName = itemView.findViewById(R.id.textView2);
        fileName = itemView.findViewById(R.id.textView);
    }
}
