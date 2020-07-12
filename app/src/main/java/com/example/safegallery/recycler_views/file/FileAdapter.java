package com.example.safegallery.recycler_views.file;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.safegallery.R;
import com.example.safegallery.dialogs.RenameDialog;
import com.example.safegallery.recycler_views.interfaces.ClickListener;
import com.example.safegallery.recycler_views.interfaces.BottomSheetListener;
import com.example.safegallery.tabs.data.DataPath;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import lombok.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileAdapter extends RecyclerView.Adapter<FileViewHolder>
        implements BottomSheetListener {

    private BottomSheetBehavior<View> bsSelectTools;
    private Context context;
    private boolean selecting = false;
    private List<DataPath> dataPaths = new ArrayList<>();
    private List<DataPath> selectedItems;

    private ClickListener clickListener;

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        DataPath itemPath = this.dataPaths.get(position);
        File itemFile = new File(itemPath.getPath());

        holder.tvItemName.setContentDescription(itemPath.getPath());
        holder.tvItemName.setText(itemFile.getName());
        holder.ivVideoIcon.setVisibility(View.GONE);
        holder.cbSelect.setVisibility(View.GONE);

        if (this.selecting)
            holder.cbSelect.setVisibility(View.VISIBLE);

        if (itemPath.getMimeType().contains("audio"))
            Glide.with(context).load(R.drawable.ic_music).into(holder.ivItemImage);
        else if (itemPath.getMimeType().contains("file"))
            Glide.with(context).load(R.drawable.ic_pdf).into(holder.ivItemImage);
        else {
            if (itemPath.getMimeType().contains("video"))
                holder.ivVideoIcon.setVisibility(View.VISIBLE);
            Glide.with(context).load(itemFile).into(holder.ivItemImage);
        }

        holder.ivItemImage.setOnLongClickListener(v -> this.handleOnLongClick());
        holder.ivItemImage.setOnClickListener(v -> this.handleClick(holder, itemPath));
    }

    @Override
    public int getItemCount() {
        return this.dataPaths.size();
    }

    private boolean handleOnLongClick() {
        this.selecting = !this.selecting;
        this.notifyDataSetChanged();

        if (this.selecting) {
            this.selectedItems = new ArrayList<>();
            this.bsSelectTools.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else
            this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);

        return true;
    }

    private void handleClick(FileViewHolder holder, DataPath itemPath) {
        if (this.selecting) {
            boolean checked = holder.cbSelect.isChecked();
            holder.cbSelect.setChecked(!checked);

            this.selectedItems.remove(itemPath);
            if (!checked)
                this.selectedItems.add(itemPath);
        } else {
            File file = new File(itemPath.getPath());
            this.clickListener.onClick(file, itemPath.getMimeType());
        }
    }

    @Override
    public void onChildClick(int id, FragmentManager fragmentManager) {
        switch (id) {
            case R.id.lock:
                Toast.makeText(context, "lock", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                this.delete();
                break;
            case R.id.rename:
                this.rename(fragmentManager);
                break;
            case R.id.move:
                Toast.makeText(context, "move", Toast.LENGTH_SHORT).show();
                break;
            case R.id.close:
                this.cancelSelecting();
                break;
        }
    }

    private void cancelSelecting() {
        this.setSelecting(false);
        this.notifyDataSetChanged();
        this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void delete() {
        String message = String.format("Do you want to delete selected item/s?\nIf deleted, item/s cannot be recovered!\n(Delete: %d)", this.selectedItems.size());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
        builder
                .setTitle("Delete items!")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    for (DataPath dataPath : this.selectedItems) {
                        File file = new File(dataPath.getPath());
                        if (file.delete())
                            this.dataPaths.remove(dataPath);
                    }
                    this.notifyDataSetChanged();
                    this.cancelSelecting();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {})
                .show();

    }

    private void rename(FragmentManager fragmentManager) {
        if (this.selectedItems.size() == 0) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
            builder
                    .setMessage("There is nothing selected.")
                    .setPositiveButton("Confirm", (dialog, which) -> {})
                    .show();
            return;
        }

        for (DataPath item : this.selectedItems) {
            RenameDialog dialog = new RenameDialog(FilenameUtils.getBaseName(item.getPath()));
            dialog.setRenameDialogListener(value -> {
                String res = this.renameFile(item.getPath(), value);
                if (res != null) {
                    Toast.makeText(this.context, FilenameUtils.getExtension(item.getPath()), Toast.LENGTH_SHORT).show();
                    item.setPath(res);
                }
                cancelSelecting();
            });
            dialog.show(fragmentManager, item.getPath());
        }
    }

    private String renameFile(String path, String newName) {
        File file = new File(path);
        String parent = file.getParent();
        String extension = FilenameUtils.getExtension(file.getAbsolutePath());
        if (!extension.equals(""))
            extension = "." + extension;

        String newPath = String.format("%s/%s%s", parent, newName, extension);
        File newFile = new File(newPath);

        if (newFile.exists()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
            builder
                    .setTitle("Invalid input!!!")
                    .setMessage("Name already exists.")
                    .setPositiveButton("Confirm", (dialog, which) -> {})
                    .show();
            return null;
        }

        try {
            return Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.ATOMIC_MOVE).toString();
        } catch (IOException e) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
            builder
                    .setTitle("Error!!!")
                    .setMessage(e.getMessage())
                    .setPositiveButton("Confirm", (dialog, which) -> {})
                    .show();
            return null;
        }
    }
}
