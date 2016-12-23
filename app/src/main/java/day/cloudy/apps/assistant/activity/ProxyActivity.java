package day.cloudy.apps.assistant.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import day.cloudy.apps.assistant.assist.AssistInteractionSession;

/**
 * Created by Gaelan Bolger on 12/22/2016.
 */
public class ProxyActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = ProxyActivity.class.getSimpleName();

    public static final String ACTION_UNINSTALL_PACKAGE = "day.cloudy.apps.assistant.action.UNINSTALL_PACKAGE";
    public static final String EXTRA_PACKAGE_NAME = "package_name";

    private static final int REQ_UNINSTALL_PACKAGE = 3211;
    public static final String ACTION_CONFIRMATION_DIALOG = "day.cloudy.apps.assistant.action.SHOW_CONFIRMATION_DIALOG";
    public static final String EXTRA_DIALOG_TITLE = "dialog_title";
    public static final String EXTRA_DIALOG_MESSAGE = "dialog_message";
    public static final String ACTION_DELIVER_RESULT = "day.cloudy.apps.assistant.result.DELIVER_RESULT";
    public static final String EXTRA_RESULT = "result";

    private boolean mAwaitingResponse = false;

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        Intent intent = getIntent();
        String action = intent.getAction();

        if (ACTION_UNINSTALL_PACKAGE.equals(action)) {
            String packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            if (TextUtils.isEmpty(packageName))
                throw new IllegalArgumentException("Must provide a package name for uninstall action");

            handleUninstallPackage(packageName);
        } else if (ACTION_CONFIRMATION_DIALOG.equals(action)) {
            String title = intent.getStringExtra(EXTRA_DIALOG_TITLE);
            String message = intent.getStringExtra(EXTRA_DIALOG_MESSAGE);
            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(message))
                throw new IllegalArgumentException("Must provide a title or message (or both) for confirmation dialog action");

            handleConfirmationDialog(title, message);
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        if (!mAwaitingResponse && !isFinishing())
            finish();
        super.onStop();
    }

    @Override
    public void finish() {
        Log.d(TAG, "finish: ");
        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAwaitingResponse = false;
        switch (requestCode) {
            case REQ_UNINSTALL_PACKAGE:
                Intent intent = new Intent(AssistInteractionSession.ACTION_SHOW);
                if (RESULT_OK == resultCode)
                    intent.putExtra(AssistInteractionSession.EXTRA_REFRESH_APPS, true);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                finish();
                break;
        }
    }

    private void handleUninstallPackage(String packageName) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.fromParts("package", packageName, null));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        mAwaitingResponse = true;
        startActivityForResult(intent, REQ_UNINSTALL_PACKAGE);
    }

    private void handleConfirmationDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        sendResultAndFinish(false);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        sendResultAndFinish(false);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendResultAndFinish(false);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendResultAndFinish(true);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void sendResultAndFinish(boolean confirmed) {
        Intent intent = new Intent(ACTION_DELIVER_RESULT);
        intent.putExtra(EXTRA_RESULT, confirmed);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }
}
