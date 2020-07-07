package com.example.safegallery.recycler_views.blank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.ClickListener;
import lombok.*;

import java.io.File;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlankAdapter extends RecyclerView.Adapter<BlankViewHolder> {

    private Context context;
    protected ClickListener clickListener;
    private List<String> itemNames;
    private Map<String, List<String>> map;

    @NonNull
    @Override
    public BlankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BlankViewHolder(
                LayoutInflater.from(context).inflate(R.layout.blank_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull BlankViewHolder holder, int position) {
        String itemName = this.itemNames.get(position);

        File f = new File(itemName);
        boolean a = f.exists();

        //Picasso.get().load("https://dummyimage.com/600x400/000/fff").into(holder.dataHolderView);
        Glide.with(context).load(itemName).into(holder.dataHolderView);
        holder.tvItemName.setText(itemName);
//        holder.itemView.setOnClickListener(v -> clickListener.onClick(itemName));
    }

    @Override
    public int getItemCount() {
        return this.itemNames.size();
    }
}
