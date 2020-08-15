package com.example.safegallery.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.safegallery.R;
import com.example.safegallery.tabs.data.DataEncryptorTask;
import org.apache.commons.io.FilenameUtils;

import java.util.List;
import java.util.Objects;

public class ErrorDialog extends DialogFragment {

    private final List<DataEncryptorTask.DataHolder> errorHolders;

    Context context;
    TextView tvErrorMessage;
    ListView lvItemList;

    public ErrorDialog(List<DataEncryptorTask.DataHolder> errorHolders) {
        this.errorHolders = errorHolders;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        LayoutInflater inflater = Objects.requireNonNull(this.getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_error_dialog, null);

        this.tvErrorMessage = view.findViewById(R.id.tvErrorMessage);
        this.tvErrorMessage.setMovementMethod(new ScrollingMovementMethod());


        builder.setView(view)
                .setTitle("Unencrypted files")
                .setPositiveButton("Confirm", (dialog, which) -> {
                });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.context, android.R.layout.select_dialog_singlechoice);

        errorHolders.stream()
                .map(errorHolder -> FilenameUtils.getBaseName(errorHolder.getDataPath().getPath()))
                .forEach(arrayAdapter::add);

        this.lvItemList = view.findViewById(R.id.lvItemList);
        this.lvItemList.setAdapter(arrayAdapter);
        this.lvItemList.setOnItemClickListener((parent, view1, position, id) -> this.tvErrorMessage.setText(errorHolders.get(position).getValue()));

        if (errorHolders.size() > 5) {
            ViewGroup.LayoutParams lvParams = this.lvItemList.getLayoutParams();
            lvParams.height = 500;
            this.lvItemList.setLayoutParams(lvParams);
            this.lvItemList.requestLayout();
        }

        return builder.create();
    }
}
