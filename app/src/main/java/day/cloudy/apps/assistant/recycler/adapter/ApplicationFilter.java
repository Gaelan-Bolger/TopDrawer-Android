package day.cloudy.apps.assistant.recycler.adapter;

import android.text.TextUtils;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import day.cloudy.apps.assistant.model.ApplicationItem;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 */
class ApplicationFilter extends Filter {

    private ApplicationAdapter applicationAdapter;
    private final List<ApplicationItem> sOriginal;
    private final List<ApplicationItem> sFiltered;
    private CharSequence sText;

    ApplicationFilter(ApplicationAdapter applicationAdapter, List<ApplicationItem> applications) {
        this.applicationAdapter = applicationAdapter;
        sOriginal = applications;
        sFiltered = new ArrayList<>();
    }

    @Override
    protected FilterResults performFiltering(CharSequence text) {
        sFiltered.clear();
        if (text.length() > 0)
            for (ApplicationItem item : sOriginal) {
                String label = item.label;
                String filter = text.toString();
                if (label.toLowerCase().startsWith(filter.toLowerCase()))
                    sFiltered.add(item);
            }
        FilterResults results = new FilterResults();
        results.values = sFiltered;
        results.count = sFiltered.size();
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence text, FilterResults filterResults) {
        sText = text;
        if (TextUtils.isEmpty(text) && filterResults.count == 0)
            applicationAdapter.setApplications(sOriginal);
        else
            applicationAdapter.setApplications((List<ApplicationItem>) filterResults.values);
    }

    public CharSequence getText() {
        return sText;
    }
}
