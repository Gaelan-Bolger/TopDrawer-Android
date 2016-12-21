package day.cloudy.apps.assistant.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import day.cloudy.apps.assistant.recycler.FirstRowDividerDecoration;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class FirstRowDividerRecyclerView extends RecyclerView {

    private boolean mFirstRowDividerAdded;
    private FirstRowDividerDecoration mFirstRowDividerDecoration;

    public FirstRowDividerRecyclerView(Context context) {
        this(context, null);
    }

    public FirstRowDividerRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FirstRowDividerRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        mFirstRowDividerDecoration = new FirstRowDividerDecoration((LinearLayoutManager) layout);
    }

    public void showFirstRowDivider(boolean show) {
        if (show && !mFirstRowDividerAdded) {
            addItemDecoration(mFirstRowDividerDecoration);
            mFirstRowDividerAdded = true;
        } else if (!show && mFirstRowDividerAdded) {
            removeItemDecoration(mFirstRowDividerDecoration);
            mFirstRowDividerAdded = false;
        }
    }
}
