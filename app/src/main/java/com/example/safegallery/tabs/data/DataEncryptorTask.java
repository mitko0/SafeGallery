package com.example.safegallery.tabs.data;

import android.os.AsyncTask;
import com.example.safegallery.dialogs.interfaces.ProgressListener;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DataEncryptorTask extends AsyncTask<DataPath, Integer, Void> {

    private boolean keepParentFolder;
    private String destination;
    private final boolean safe;
    private final DataType dataType;
    private final ProgressListener progressListener;

    List<DataHolder> successfulFiles = new ArrayList<>();
    List<DataHolder> errorMessages = new ArrayList<>();

    public DataEncryptorTask(boolean safe, DataType dataType, boolean keepParentFolder, String destination, ProgressListener listener) {
        this.keepParentFolder = keepParentFolder;
        this.dataType = dataType;
        this.safe = safe;
        this.progressListener = listener;

        if (!keepParentFolder && destination == null)
            this.keepParentFolder = true;
        else if (destination != null)
            this.destination = destination;
    }

    @Override
    protected Void doInBackground(DataPath... paths) {

        for (int i = 0; i < paths.length; i++) {
            File currentFile = new File(paths[i].getPath());
            String destinationPath = this.getDestinationPath(currentFile);
            File destinationFile = new File(destinationPath);
            boolean isCreated = destinationFile.mkdirs();

            try {
                byte[] data;
                byte[] currentFileData = Files.readAllBytes(currentFile.toPath());

                if (safe)
                    data = DataEncryptor.decrypt(Files.readAllBytes(currentFile.toPath()));
                else
                    data = DataEncryptor.encrypt(Files.readAllBytes(currentFile.toPath()));

                Path newPath = Files.move(currentFile.toPath(), destinationFile.toPath().resolve(currentFile.getName()), StandardCopyOption.ATOMIC_MOVE);
                Files.write(newPath, data);

                DataHolder item = new DataHolder(paths[i], newPath.toString(), currentFileData);
                this.successfulFiles.add(item);
            } catch (Exception e) {
                DataHolder errorHolder = new DataHolder(paths[i], e.getMessage());
                this.errorMessages.add(errorHolder);
                if (isCreated)
                    //noinspection ResultOfMethodCallIgnored
                    destinationFile.delete();
            }

            this.publishProgress(i + 1, this.successfulFiles.size(), this.errorMessages.size());
        }

        // if the thread lives for a short amount of time the buttons on the dialog wont show
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        this.progressListener.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void v) {
        this.progressListener.onTaskFinish(successfulFiles, errorMessages);
    }

    private String getDestinationPath(File file) {
        String destinationResult;

        if (this.safe) {
            destinationResult = StorageData.APP_DATA_PATH + Objects.requireNonNull(file.getParentFile()).getName();

        } else {
            destinationResult = StorageData.APP_SAFE_DATA_PATH + "Safe" + this.dataType.name();
            if (this.keepParentFolder)
                destinationResult = String.format("%s/%s", destinationResult, Objects.requireNonNull(file.getParentFile()).getName());
            else
                destinationResult = String.format("%s/%s", destinationResult, this.destination);
        }

        return destinationResult;
    }

    @Getter
    @AllArgsConstructor
    public static class DataHolder {
        private final DataPath dataPath;
        private final String value;
        private byte[] data;

        public DataHolder(DataPath dataPath, String value) {
            this.dataPath = dataPath;
            this.value = value;
        }
    }
}
