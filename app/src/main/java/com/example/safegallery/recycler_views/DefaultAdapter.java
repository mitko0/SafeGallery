package com.example.safegallery.recycler_views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.dialogs.ErrorDialog;
import com.example.safegallery.dialogs.ProgressDialog;
import com.example.safegallery.dialogs.RenameDialog;
import com.example.safegallery.dialogs.interfaces.ProgressListener;
import com.example.safegallery.recycler_views.interfaces.BottomSheetListener;
import com.example.safegallery.recycler_views.interfaces.ClickListener;
import com.example.safegallery.recycler_views.interfaces.ViewModelListener;
import com.example.safegallery.tabs.data.DataEncryptorTask;
import com.example.safegallery.tabs.data.DataPath;
import com.example.safegallery.tabs.data.DataType;
import com.example.safegallery.tabs.data.StorageData;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class DefaultAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>
        implements BottomSheetListener {

    protected boolean safe;
    protected DataType dataType;
    protected Map<String, List<DataPath>> dataMap = new HashMap<>();
    protected List<DataPath> selectedItems;
    protected boolean selecting = false;

    protected ClickListener clickListener;
    protected ViewModelListener viewModelListener;

    protected BottomSheetBehavior<View> bsSelectTools;
    protected Context context;

    protected boolean handleOnLongClick() {
        this.selecting = !this.selecting;
        this.notifyDataSetChanged();

        if (this.selecting) {
            this.selectedItems = new ArrayList<>();
            this.bsSelectTools.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else
            this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);

        return true;
    }

    protected void cancelSelecting() {
        this.selecting = false;
        this.notifyDataSetChanged();
        this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    protected void delete() {
        @SuppressLint("DefaultLocale")
        String message = String.format("Do you want to delete selected item/s?\nIf deleted, item/s cannot be recovered!\n(Delete: %d)", this.selectedItems.size());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
        builder.setTitle("Delete items!")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    for (DataPath dataPath : this.selectedItems) {
                        File file = new File(dataPath.getPath());
                        this.deleteFile(file);
                    }
                    this.cancelSelecting();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteFile(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File childFile : children) {
                this.deleteFile(childFile);
            }
        }

        try {
            Files.delete(file.toPath());

            List<DataPath> values = this.dataMap.get(file.getParent());
            values.removeIf(dataPath -> dataPath.getPath().equals(file.getAbsolutePath()));
            if (values.size() == 0) {
                this.dataMap.remove(file.getParent());
                //noinspection ConstantConditions
                Files.delete(file.getParentFile().toPath());
            }
        } catch (Exception e) {
            Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void rename(FragmentManager fragmentManager, boolean renameParent) {
        Set<DataPath> renameItems = new HashSet<>(this.selectedItems);
        if (renameParent) {
            renameItems = new HashSet<>();
            for (DataPath path : this.selectedItems) {
                File file = new File(path.getPath());
                DataPath parent = new DataPath();
                parent.setPath(file.getParent());
                renameItems.add(parent);
            }
        }

        for (DataPath currentSelectedItem : renameItems) {
            RenameDialog dialog = new RenameDialog(FilenameUtils.getBaseName(currentSelectedItem.getPath()));
            dialog.setRenameDialogListener(value -> {
                String resultPath = this.renameFile(currentSelectedItem.getPath(), value);
                if (resultPath != null) {
                    if (renameParent) {
                        List<DataPath> values = this.dataMap.remove(currentSelectedItem.getPath());
                        this.dataMap.put(resultPath, values);
                    } else
                        currentSelectedItem.setPath(resultPath);
                    Toast.makeText(this.context, "Rename successful", Toast.LENGTH_SHORT).show();
                }
                this.cancelSelecting();
            });
            dialog.show(fragmentManager, currentSelectedItem.getPath());
        }
        this.cancelSelecting();
    }

    private String renameFile(String filePathToRename, String newName) {
        File fileToRename = new File(filePathToRename);
        String parentPath = fileToRename.getParent();
        String extension = FilenameUtils.getExtension(filePathToRename);
        if (!extension.equals(""))
            extension = "." + extension;

        String newPath = String.format("%s/%s%s", parentPath, newName, extension);
        File newFile = new File(newPath);
        List<DataPath> children = this.dataMap.getOrDefault(filePathToRename, new ArrayList<>());
        for (DataPath child : children) {
            File file = new File(child.getPath());
            Path path = Paths.get(newPath, file.getName());
            child.setPath(path.toString());
        }

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


    protected void lock(FragmentManager fragmentManager) {
        String lock = this.safe
                ? "unlock"
                : "lock";

        @SuppressLint("DefaultLocale")
        String message = String.format("Do you want to %1$s selected item/s?\nDo not stop the process while %1$sing the item/s.\n(Selected: %2$d)", lock, this.selectedItems.size());

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

//        String[] items = new String[children.length + 1];
        List<String> items = new ArrayList<>();
        items.add("Keep parent folder");

        for (File child : children) {
            if (child.isDirectory()
                    && Objects.requireNonNull(child.listFiles()).length != 0)

                items.add(child.getName());
        }

        AtomicInteger selected = new AtomicInteger();
        builder.setTitle("Select destination")
                .setSingleChoiceItems(items.toArray(new String[0]), 0, (dialog, which) -> selected.set(which))
                .setPositiveButton("Select", (dialog, which) -> this.finishEncryption(selected.get() == 0, items.get(selected.get()), fragmentManager))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();

    }

    private void finishEncryption(boolean keepParentFolder, String destination, FragmentManager fragmentManager) {
        ProgressDialog progressDialog = new ProgressDialog(this.selectedItems.size());
        progressDialog.setCancelable(false);
        progressDialog.show(fragmentManager, "progress dialog");

        ProgressListener progressListener = new ProgressListener() {
            List<DataEncryptorTask.DataHolder> errorHolders;
            List<DataEncryptorTask.DataHolder> successfulFiles;

            @SuppressLint("DefaultLocale")
            @Override
            public void onTaskFinish(List<DataEncryptorTask.DataHolder> successfulFiles, List<DataEncryptorTask.DataHolder> errorHolders) {
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
                updateData(this.successfulFiles);
                progressDialog.dismiss();
                cancelSelecting();
            }

            @Override
            public void onNegativeButtonClick() {
                updateData(this.successfulFiles);
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

    private void updateData(List<DataEncryptorTask.DataHolder> data) {
        List<String> keys = new ArrayList<>();
        List<DataPath> dataPaths = data.stream().map(DataEncryptorTask.DataHolder::getDataPath).collect(Collectors.toList());

        this.dataMap.forEach((key, valueList) -> {
            valueList.removeAll(dataPaths);
            if (valueList.isEmpty())
                keys.add(key);
        });

        keys.forEach(key -> {
            this.dataMap.remove(key);
            File file = new File(key);
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        });

        this.viewModelListener.updateData(data);
    }
}
