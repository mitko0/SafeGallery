package com.example.safegallery.recycler_views.file;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.bumptech.glide.Glide;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.DefaultAdapter;
import com.example.safegallery.tabs.data.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import lombok.*;

import java.io.File;
import java.util.ArrayList;

@Setter
@NoArgsConstructor
public class FileAdapter extends DefaultAdapter<FileViewHolder> {

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        DataPath itemPath = this.dataMap.values()
                .stream()
                .reduce(new ArrayList<>(), (result, values) -> {
                    result.addAll(values);
                    return result;
                }).get(position);
        File itemFile = new File(itemPath.getPath());

        holder.tvItemName.setContentDescription(itemPath.getPath());
        holder.tvItemName.setText(itemFile.getName());
        holder.ivVideoIcon.setVisibility(View.GONE);
        holder.cbSelect.setVisibility(View.GONE);

        if (this.selecting)
            holder.cbSelect.setVisibility(View.VISIBLE);

        String mimeType = itemPath.getMimeType();
        if (mimeType.contains("audio"))
            Glide.with(context).load(R.drawable.ic_music).into(holder.ivItemImage);
        else if (mimeType.contains("file"))
            Glide.with(context).load(R.drawable.ic_pdf).into(holder.ivItemImage);
        else if (mimeType.contains("image") || mimeType.contains("video")) {
            if (mimeType.contains("video"))
                holder.ivVideoIcon.setVisibility(View.VISIBLE);

            if (this.safe)
                Glide.with(context).load(itemPath.getData()).into(holder.ivItemImage);
            else
                Glide.with(context).load(itemFile).into(holder.ivItemImage);
        } else
            Glide.with(context).load(R.drawable.ic_mood_bad).into(holder.ivItemImage);

        holder.ivItemImage.setOnLongClickListener(v -> this.handleOnLongClick());
        holder.ivItemImage.setOnClickListener(v -> this.handleClick(holder, itemPath));
    }

    @Override
    public int getItemCount() {
        return this.dataMap.values()
                .stream()
                .reduce(new ArrayList<>(), (result, values) -> {
                    result.addAll(values);
                    return result;
                }).size();
    }

    protected void handleClick(FileViewHolder holder, DataPath itemPath) {
        if (this.selecting) {
            boolean checked = holder.cbSelect.isChecked();
            holder.cbSelect.setChecked(!checked);

            this.selectedItems.remove(itemPath);
            if (!checked)
                this.selectedItems.add(itemPath);
        } else
            this.clickListener.onClick(itemPath);
    }

    @Override
    public void onChildClick(int id, FragmentManager fragmentManager) {
        if (id != R.id.close && this.selectedItems.size() == 0) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
            builder.setMessage("There is nothing selected.")
                    .setPositiveButton("Confirm", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        switch (id) {
            case R.id.lock:
            case R.id.unlock:
                this.lock(fragmentManager);
                break;
            case R.id.delete:
                this.delete();
                break;
            case R.id.rename:
                this.rename(fragmentManager, false);
                break;
            case R.id.move:
                Toast.makeText(context, "move", Toast.LENGTH_SHORT).show();
                break;
            case R.id.close:
                this.cancelSelecting();
                break;
        }
    }
}
