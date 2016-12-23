package day.cloudy.apps.assistant.recycler;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;

/**
 * Created by Gaelan Bolger on 12/21/2016.
 */
public class LockableGridLayoutManager extends GridLayoutManager {

    private boolean mCanScrollVertically;

    public LockableGridLayoutManager(Context context, int numColumns) {
        super(context, numColumns);
    }

    @Override
    public boolean canScrollVertically() {
        return mCanScrollVertically;
    }

    public void setCanScrollVertically(boolean canScrollVertically) {
        this.mCanScrollVertically = canScrollVertically;
    }
}
