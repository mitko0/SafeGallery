package com.example.safegallery.recycler_views.def;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.interfaces.ClickListener;
import com.example.safegallery.tabs.data.DataPath;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultAdapter extends RecyclerView.Adapter<DefaultViewHolder> {

    private Context context;
    private ClickListener clickListener;
    private List<String> viewDataPaths = new ArrayList<>();
    private List<DataPath> dataPaths = new ArrayList<>();

    @NonNull
    @Override
    public DefaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DefaultViewHolder(
                LayoutInflater.from(context).inflate(R.layout.default_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull DefaultViewHolder holder, int position) {
        String path = this.viewDataPaths.get(position);
        File fileForPath = new File(path);

        holder.img.setOnClickListener(v -> clickListener.onClick(fileForPath));
        holder.icon.setVisibility(View.GONE);
        holder.fileName.setVisibility(View.GONE);
        holder.folderName.setVisibility(View.VISIBLE);
        holder.folderName.setText(fileForPath.getName());

        Glide.with(context).load(R.drawable.ic_folder_open_black).into(holder.img);

        /*if (fileForPath.isDirectory()) {
            Glide.with(context).load(R.drawable.ic_folder_open_black).into(holder.img);
            holder.folderName.setVisibility(View.VISIBLE);
            holder.folderName.setText(fileForPath.getName());
        } else {
            holder.fileName.setVisibility(View.VISIBLE);
            holder.fileName.setText(fileForPath.getName());

            if (path.getMimeType().contains("audio")){
                Glide.with(context).load(R.drawable.ic_music).into(holder.img);
            } else {
                Glide.with(context).load(fileForPath).into(holder.img);


                if (path.getMimeType().contains("image")) {
                    holder.icon.setVisibility(View.GONE);
                } else {
                    holder.icon.setVisibility(View.VISIBLE);
                }
            }
        }*/
    }

    @Override
    public int getItemCount() {
        return this.viewDataPaths.size();
    }
}
