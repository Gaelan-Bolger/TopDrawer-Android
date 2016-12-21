package day.cloudy.apps.assistant.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.SugarRecord;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import day.cloudy.apps.assistant.R;
import day.cloudy.apps.assistant.dialog.ConfirmationDialog;
import day.cloudy.apps.assistant.model.RecentApplication;
import day.cloudy.apps.assistant.settings.AppPrefs;
import day.cloudy.apps.assistant.util.AssistUtils;
import day.cloudy.apps.assistant.util.PackageUtils;

import static butterknife.ButterKnife.bind;
import static butterknife.ButterKnife.findById;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 */
public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.image_view_assistant_status)
    ImageView vAssistStatus;
    @BindView(R.id.image_view_home_status)
    ImageView vHomeStatus;
    @BindView(R.id.spinner_home_options)
    Spinner vHomeOptions;

    private AppPrefs mPrefs = AppPrefs.getInstance();
    private HomeApplicationsAdapter mHomePackagesAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_reset_frequents:
                showResetFrequentsConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.button_assist_settings, R.id.button_home_settings})
    public void onButtonClick(Button button) {
        switch (button.getId()) {
            case R.id.button_assist_settings:
                startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
                break;
            case R.id.button_home_settings:
                startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
                break;
        }
    }

    private void updateViews() {
        boolean isAssistPackage = AssistUtils.isDefaultAssistPackage(this);
        vAssistStatus.setImageResource(isAssistPackage ? R.drawable.ic_check_green_24dp : R.drawable.ic_cancel_red_24dp);

        boolean isHomePackage = PackageUtils.isDefaultHomePackage(this);
        vHomeStatus.setImageResource(isHomePackage ? R.drawable.ic_check_green_24dp : R.drawable.ic_cancel_red_24dp);

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> homeApps = PackageUtils.getHomeActivities(packageManager);
        for (int i = homeApps.size() - 1; i >= 0; i--) {
            ResolveInfo homeApp = homeApps.get(i);
            String homeAppPackageName = homeApp.activityInfo.packageName;
            if (homeAppPackageName.equals(getPackageName())) {
                homeApps.remove(homeApp);
                break;
            }
        }
        Collections.sort(homeApps, new ResolveInfo.DisplayNameComparator(packageManager));
        mHomePackagesAdapter = new HomeApplicationsAdapter(this);
        mHomePackagesAdapter.setsHomeApps(homeApps);
        vHomeOptions.setAdapter(mHomePackagesAdapter);

        String homePackageName = mPrefs.getHomePackageName(this);
        String homeActivityName = mPrefs.getHomeActivityName(this);
        for (int i = 0; i < mHomePackagesAdapter.getCount(); i++) {
            ResolveInfo resolveInfo = mHomePackagesAdapter.getItem(i);
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo.packageName.equals(homePackageName)
                    && activityInfo.name.equals(homeActivityName)) {
                vHomeOptions.setSelection(i);
                break;
            }
        }

        vHomeOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                ResolveInfo item = mHomePackagesAdapter.getItem(position);
                String packageName = item.activityInfo.packageName;
                String activityName = item.activityInfo.name;
                mPrefs.setHomePackageName(SettingsActivity.this, packageName);
                mPrefs.setHomeActivityName(SettingsActivity.this, activityName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void showResetFrequentsConfirmationDialog() {
        ConfirmationDialog.newInstance("Reset all most frequently used application data?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                SugarRecord.deleteAll(RecentApplication.class);
                Toast.makeText(SettingsActivity.this, "Most frequent apps reset", Toast.LENGTH_SHORT).show();
            }
        }).show(getSupportFragmentManager(), "reset_frequents");
    }

    private class HomeApplicationsAdapter extends BaseAdapter implements SpinnerAdapter {

        private final Context sContext;
        private final PackageManager sPackageManager;
        private List<ResolveInfo> sHomeApps;

        HomeApplicationsAdapter(Context context) {
            sContext = context;
            sPackageManager = context.getPackageManager();
        }

        @Override
        public int getCount() {
            return null != sHomeApps ? sHomeApps.size() : 0;
        }

        @Override
        public ResolveInfo getItem(int position) {
            return sHomeApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder h;
            if (null == convertView) {
                convertView = LayoutInflater.from(sContext).inflate(R.layout.item_home_package, parent, false);
                h = new Holder(convertView);
            } else {
                h = (Holder) convertView.getTag();
            }
            ResolveInfo item = getItem(position);
            h.icon.setImageDrawable(item.loadIcon(sPackageManager));
            h.text1.setText(item.loadLabel(sPackageManager));
            h.text2.setText(item.activityInfo.packageName);
            return convertView;
        }

        void setsHomeApps(List<ResolveInfo> homeApps) {
            this.sHomeApps = homeApps;
            notifyDataSetChanged();
        }

        class Holder {

            ImageView icon;
            TextView text1;
            TextView text2;

            Holder(View itemView) {
                itemView.setTag(this);
                icon = findById(itemView, android.R.id.icon);
                text1 = findById(itemView, android.R.id.text1);
                text2 = findById(itemView, android.R.id.text2);
            }
        }
    }
}
