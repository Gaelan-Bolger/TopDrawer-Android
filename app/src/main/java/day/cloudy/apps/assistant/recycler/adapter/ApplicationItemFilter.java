package day.cloudy.apps.assistant.recycler.adapter;

import android.text.TextUtils;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import day.cloudy.apps.assistant.model.ApplicationItem;
import day.cloudy.apps.assistant.model.FrequentItem;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 */
class ApplicationItemFilter extends Filter {

    private final ApplicationItemAdapter mAdapter;
    private final List<ApplicationItem> mOriginal;
    private final List<ApplicationItem> mFiltered;
    private CharSequence mText;

    ApplicationItemFilter(ApplicationItemAdapter adapter, List<ApplicationItem> items) {
        mAdapter = adapter;
        mOriginal = items;
        mFiltered = new ArrayList<>();
    }

    @Override
    protected FilterResults performFiltering(CharSequence text) {
        mFiltered.clear();
        if (text.length() > 0)
            for (ApplicationItem item : mOriginal) {
                if (!(item instanceof FrequentItem)) {
                    String label = item.label;
                    String filter = text.toString();
                    if (label.toLowerCase().startsWith(filter.toLowerCase())) {
                        mFiltered.add(item);
                    }
                }
            }
        FilterResults results = new FilterResults();
        results.values = mFiltered;
        results.count = mFiltered.size();
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence text, FilterResults filterResults) {
        mText = text;
        if (TextUtils.isEmpty(text) && filterResults.count == 0)
            mAdapter.setApplications(mOriginal);
        else
            mAdapter.setApplications((List<ApplicationItem>) filterResults.values);
    }

    public CharSequence getText() {
        return mText;
    }
}
