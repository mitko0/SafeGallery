package com.example.safegallery.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.safegallery.R;
import com.example.safegallery.dialogs.interfaces.ProgressListener;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ProgressDialog extends DialogFragment {

    private ProgressListener progressListener;
    private int max;

    Context context;
    AlertDialog dialog;
    ProgressBar progressBar;
    TextView tvCount;
    TextView tvEndMessage;

    public ProgressDialog(int max) {
        if (max <= 0)
            max = 1;
        this.max = max;
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
        View view = inflater.inflate(R.layout.layout_progress_dialog, null);

        builder.setView(view)
                .setMessage("Encrypting files...\nDo not cancel the process until all files are encrypted.")
                .setPositiveButton("Confirm", (dialog1, which) -> this.progressListener.onPositiveButtonClick())
                .setNegativeButton("Show errors", (dialog1, which) -> this.progressListener.onNegativeButtonClick());

        this.progressBar = view.findViewById(R.id.pbProgress);
        this.progressBar.setMin(0);
        this.progressBar.setMax(this.max);

        this.tvEndMessage = view.findViewById(R.id.tvEndMessage);
        this.tvCount = view.findViewById(R.id.tvCount);
        this.tvCount.setText(String.format(("(0/%d)"), this.max));

        this.dialog = builder.create();
        this.dialog.setOnShowListener(dia -> {
            this.dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
            this.dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);
        });

        return this.dialog;
    }

    public void updateProgress(int value) {
        this.progressBar.setProgress(value, true);
        this.tvCount.setText(String.format("(%d/%d)", value, max));
    }

    public void setButtonVisibility(int which, int visibility) {
        this.dialog.getButton(which).setVisibility(visibility);
    }
}
