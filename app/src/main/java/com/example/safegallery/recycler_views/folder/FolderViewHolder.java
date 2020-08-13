package com.example.safegallery.recycler_views.folder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;

public class FolderViewHolder extends RecyclerView.ViewHolder {

    ImageView ivItemImage;
    TextView tvItemName;
    CheckBox cbSelect;

    public FolderViewHolder(@NonNull View itemView) {
        super(itemView);

        this.ivItemImage = itemView.findViewById(R.id.ivItemImage);
        this.tvItemName = itemView.findViewById(R.id.tvItemName);
        this.cbSelect = itemView.findViewById(R.id.cbSelect);
    }
}
