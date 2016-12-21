package day.cloudy.apps.assistant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.SugarRecord;

import butterknife.BindView;
import butterknife.OnClick;
import day.cloudy.apps.assistant.R;
import day.cloudy.apps.assistant.model.RecentApplication;

import static butterknife.ButterKnife.bind;
import static butterknife.ButterKnife.findById;

/**
 * Created by Gaelan Bolger on 12/20/2016.
 */

public class DialogActivity extends AppCompatActivity {

    private static final String TAG = DialogActivity.class.getSimpleName();
    public static final String ACTION_CANCELED = "day.cloudy.apps.assistant.action.CANCELED";
    public static final String ACTION_RESET_FREQUENTS = "day.cloudy.apps.assistant.action.RESET_FREQUENTS";
    public static final String EXTRA_RETURN_ACTION = "return_action";
    public static final String EXTRA_ICON_RES = "iconResId";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MESSAGE = "message";

    @BindView(R.id.image_view_dialog_icon)
    ImageView vIcon;
    @BindView(R.id.text_view_dialog_title)
    TextView vTitle;
    @BindView(R.id.layout_dialog_content)
    FrameLayout vContent;

    private boolean mActionCompleted;
    private String mAction;
    private String mReturnAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (null == intent) {
            finish();
            return;
        }
        mActionCompleted = false;
        mAction = intent.getAction();
        mReturnAction = intent.getStringExtra(EXTRA_RETURN_ACTION);
        if (TextUtils.isEmpty(mAction) || TextUtils.isEmpty(mReturnAction)) {
            Log.e(TAG, "onCreate: Must provide both an action and a 'return action' extra");
            finish();
            return;
        }

        setContentView(R.layout.activity_dialog);
        bind(this);
        int iconResId = intent.getIntExtra(EXTRA_ICON_RES, 0);
        if (iconResId > 0) {
            vIcon.setImageResource(iconResId);
            vIcon.setVisibility(View.VISIBLE);
        }
        String title = intent.getStringExtra(EXTRA_TITLE);
        vTitle.setText(title);
        findById(this, R.id.layout_dialog_title)
                .setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);

        switch (mAction) {
            case ACTION_RESET_FREQUENTS:
                getLayoutInflater().inflate(R.layout.dialog_content_message, vContent, true);
                ((TextView) findById(vContent, R.id.text_view_dialog_message))
                        .setText(intent.getStringExtra(EXTRA_MESSAGE));
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mActionCompleted)
            cancelAction();
    }

    @OnClick({R.id.button_neutral, R.id.button_negative, R.id.button_positive})
    public void onButtonClick(Button b) {
        switch (b.getId()) {
            case R.id.button_neutral:
            case R.id.button_negative:
                cancelAction();
                break;
            case R.id.button_positive:
                completeAction();
                break;
        }
    }

    private void cancelAction() {
        Intent intent = new Intent(ACTION_CANCELED);
        intent.putExtra(EXTRA_RETURN_ACTION, mReturnAction);
        sendLocalBroadcast(intent);
        finish();
    }

    private void completeAction() {
        switch (mAction) {
            case ACTION_RESET_FREQUENTS:
                SugarRecord.deleteAll(RecentApplication.class);
                Toast.makeText(this, "Frequent application data reset", Toast.LENGTH_SHORT).show();
                sendLocalBroadcast(new Intent(mReturnAction));
                mActionCompleted = true;
                finish();
                break;
        }
    }

    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
