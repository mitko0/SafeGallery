package com.example.safegallery.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.safegallery.R;
import com.example.safegallery.dialogs.interfaces.RenameDialogListener;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class RenameDialog extends DialogFragment {

    private RenameDialogListener renameDialogListener;

    Context context;
    EditText etEdit;
    String oldName;

    public RenameDialog(String oldName) {
        this.oldName = oldName;
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
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_rename_dialog, null);

        builder.setView(view)
                .setTitle("Rename item/s?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Rename", (dialog, which) -> this.renameDialogListener.onRename(this.etEdit.getText().toString()));

        this.etEdit = view.findViewById(R.id.etEdit);
        this.etEdit.setText(this.oldName);

        return builder.create();
    }
}
