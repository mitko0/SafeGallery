package com.example.safegallery.recycler_views.folder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.DefaultAdapter;
import com.example.safegallery.tabs.data.DataPath;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.ArrayList;

@NoArgsConstructor
public class FolderAdapter extends DefaultAdapter<FolderViewHolder> {

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        String itemPath = new ArrayList<>(this.dataMap.keySet()).get(position);
        File itemFile = new File(itemPath);
        int childCount = this.dataMap.get(itemPath).size();

        holder.tvItemName.setText(String.format("%s (%d)", itemFile.getName(), childCount));
        holder.tvItemName.setContentDescription(itemPath);
        holder.cbSelect.setVisibility(View.GONE);

        if (this.selecting)
            holder.cbSelect.setVisibility(View.VISIBLE);

        holder.ivItemImage.setOnLongClickListener(v -> this.handleOnLongClick());
        holder.ivItemImage.setOnClickListener(v -> this.handleClick(holder, itemPath));
    }

    @Override
    public int getItemCount() {
        if (dataMap == null)
            return 0;
        return this.dataMap.size();
    }

    protected void handleClick(FolderViewHolder holder, String folderItemPath) {
        if (this.selecting) {
            boolean checked = holder.cbSelect.isChecked();
            holder.cbSelect.setChecked(!checked);

            this.selectedItems.removeAll(this.dataMap.get(folderItemPath));
            if (!checked)
               this.selectedItems.addAll(this.dataMap.get(folderItemPath));
        } else {
            DataPath dataPath = new DataPath(folderItemPath, null);
            this.clickListener.onClick(dataPath);
        }
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
                this.rename(fragmentManager, true);
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
