package com.example.safegallery.recycler_views.file;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;

public class FileViewHolder extends RecyclerView.ViewHolder {

    ImageView ivItemImage;
    ImageView ivVideoIcon;
    TextView tvItemName;
    CheckBox cbSelect;

    public FileViewHolder(@NonNull View itemView) {
        super(itemView);

        this.ivItemImage = itemView.findViewById(R.id.ivItemImage);
        this.ivVideoIcon = itemView.findViewById(R.id.ivVideoIcon);
        this.tvItemName = itemView.findViewById(R.id.tvItemName);
        this.cbSelect = itemView.findViewById(R.id.cbSelect);
    }
}
