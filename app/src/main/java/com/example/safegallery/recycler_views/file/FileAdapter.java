package com.example.safegallery.recycler_views.file;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.safegallery.R;
import com.example.safegallery.dialogs.ErrorDialog;
import com.example.safegallery.dialogs.ProgressDialog;
import com.example.safegallery.dialogs.RenameDialog;
import com.example.safegallery.dialogs.interfaces.ProgressListener;
import com.example.safegallery.recycler_views.interfaces.ClickListener;
import com.example.safegallery.recycler_views.interfaces.BottomSheetListener;
import com.example.safegallery.tabs.data.*;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileAdapter extends RecyclerView.Adapter<FileViewHolder>
        implements BottomSheetListener {

    private boolean safe;
    private DataType dataType;
    private List<DataPath> dataPaths = new ArrayList<>();
    private List<DataPath> selectedItems;
    private boolean selecting = false;

    private ClickListener clickListener;

    BottomSheetBehavior<View> bsSelectTools;
    Context context;

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
        this.selecting = false;
        this.notifyDataSetChanged();
        this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void lock(FragmentManager fragmentManager) {
        String lock = this.safe
                ? "unlock"
                : "lock";

        String message = String.format("Do you want to %s selected item/s?\nDo not stop the process while locking the item/s.\n(Selected: %d)", lock, this.selectedItems.size());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
        builder.setTitle("Lock items!")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (this.safe)
                        this.finishEncryption(false, null, fragmentManager);
                    else
                        this.lockConfirmed(fragmentManager);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void lockConfirmed(FragmentManager fragmentManager) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);

        String path = StorageData.APP_SAFE_DATA_PATH + "Safe" + this.dataType.name();
        File file = new File(path);
        File[] children = Objects.requireNonNull(file.listFiles());

        String[] items = new String[children.length + 1];
        items[0] = "Keep parent folder";

        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirectory())
                items[i + 1] = children[i].getName();
        }

        AtomicInteger selected = new AtomicInteger();
        builder.setTitle("Select destination")
                .setSingleChoiceItems(items, 0, (dialog, which) -> selected.set(which))
                .setPositiveButton("Select", (dialog, which) -> this.finishEncryption(selected.get() == 0, items[selected.get()], fragmentManager))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();

    }

    private void finishEncryption(boolean keepParentFolder, String destination, FragmentManager fragmentManager) {
        ProgressDialog progressDialog = new ProgressDialog(this.selectedItems.size());
        progressDialog.setCancelable(false);
        progressDialog.show(fragmentManager, "progress dialog");

        ProgressListener progressListener = new ProgressListener() {
            List<DataEncryptorTask.ErrorHolder> errorHolders;
            List<DataPath> successfulFiles;

            @Override
            public void onTaskFinish(List<DataPath> successfulFiles, List<DataEncryptorTask.ErrorHolder> errorHolders) {
                this.errorHolders = errorHolders;
                this.successfulFiles = successfulFiles;

                progressDialog.getTvEndMessage().setText(String.format("Success: %d / Error: %d", successfulFiles.size(), errorHolders.size()));
                progressDialog.setButtonVisibility(DialogInterface.BUTTON_POSITIVE, View.VISIBLE);
                if (errorHolders.size() > 0)
                    progressDialog.setButtonVisibility(DialogInterface.BUTTON_NEGATIVE, View.VISIBLE);
            }

            @Override
            public void onProgressUpdate(Integer... values) {
                progressDialog.updateProgress(values[0]);
            }

            @Override
            public void onPositiveButtonClick() {
                dataPaths.removeAll(this.successfulFiles);
                progressDialog.dismiss();
                cancelSelecting();
            }

            @Override
            public void onNegativeButtonClick() {
                dataPaths.removeAll(this.successfulFiles);
                progressDialog.dismiss();
                cancelSelecting();

                ErrorDialog errorDialog = new ErrorDialog(this.errorHolders);
                errorDialog.show(fragmentManager, "error dialog");
            }
        };

        progressDialog.setProgressListener(progressListener);

        DataPath[] dataPaths = this.selectedItems.toArray(new DataPath[0]);
        new DataEncryptorTask(this.safe, this.dataType, keepParentFolder, destination, progressListener).execute(dataPaths);
    }

    private void delete() {
        String message = String.format("Do you want to delete selected item/s?\nIf deleted, item/s cannot be recovered!\n(Delete: %d)", this.selectedItems.size());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
        builder.setTitle("Delete items!")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    for (DataPath dataPath : this.selectedItems) {
                        File file = new File(dataPath.getPath());
                        if (file.delete()) {
                            this.dataPaths.remove(dataPath);
                            Toast.makeText(this.context, "Item successfully deleted", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(this.context, "Item cannot be deleted", Toast.LENGTH_SHORT).show();
                    }
                    this.cancelSelecting();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void rename(FragmentManager fragmentManager) {
        for (DataPath currentSelectedItem : this.selectedItems) {
            RenameDialog dialog = new RenameDialog(FilenameUtils.getBaseName(currentSelectedItem.getPath()));
            dialog.setRenameDialogListener(value -> {
                String resultPath = this.renameFile(currentSelectedItem.getPath(), value);
                if (resultPath != null) {
                    currentSelectedItem.setPath(resultPath);
                    Toast.makeText(this.context, "Rename successful", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this.context, "Rename failed", Toast.LENGTH_SHORT).show();
                cancelSelecting();
            });
            dialog.show(fragmentManager, currentSelectedItem.getPath());
        }
    }

    private String renameFile(String filePathToRename, String newName) {
        File fileToRename = new File(filePathToRename);
        String parent = fileToRename.getParent();
        String extension = FilenameUtils.getExtension(filePathToRename);
        if (!extension.equals(""))
            extension = "." + extension;

        String newPath = String.format("%s/%s%s", parent, newName, extension);
        File newFile = new File(newPath);

        if (newFile.exists()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
            builder.setTitle("Invalid input!!!")
                    .setMessage("Name already exists.")
                    .setPositiveButton("Confirm", (dialog, which) -> dialog.dismiss())
                    .show();
            return null;
        }

        try {
            return Files.move(fileToRename.toPath(), newFile.toPath(), StandardCopyOption.ATOMIC_MOVE).toString();
        } catch (IOException e) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
            builder.setTitle("Error!!!")
                    .setMessage(e.getMessage())
                    .setPositiveButton("Confirm", (dialog, which) -> dialog.dismiss())
                    .show();
            return null;
        }
    }
}
