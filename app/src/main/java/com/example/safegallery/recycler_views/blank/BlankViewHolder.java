package com.example.safegallery.recycler_views.blank;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;

public class BlankViewHolder extends RecyclerView.ViewHolder {

    ImageView dataHolderView;
    TextView tvItemName;
    CheckBox cbMultiple;

    public BlankViewHolder(@NonNull View itemView) {
        super(itemView);

        this.dataHolderView = itemView.findViewById(R.id.video);
        this.tvItemName = itemView.findViewById(R.id.tvItemName);
        this.cbMultiple = itemView.findViewById(R.id.cbMultiple);
    }
}
