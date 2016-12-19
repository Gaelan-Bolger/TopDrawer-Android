package day.cloudy.apps.assistant.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import day.cloudy.apps.assistant.util.Bundler;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class ConfirmationDialog extends DialogFragment {

    public static ConfirmationDialog newInstance(String message, DialogInterface.OnClickListener positiveButtonClickListener) {
        ConfirmationDialog dialog = new ConfirmationDialog();
        dialog.setArguments(new Bundler().with("message", message).bundle());
        dialog.setPositiveButtonClickListener(positiveButtonClickListener);
        return dialog;
    }

    private DialogInterface.OnClickListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString("message"))
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, mListener);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public void setPositiveButtonClickListener(DialogInterface.OnClickListener listener) {
        mListener = listener;
    }
}
