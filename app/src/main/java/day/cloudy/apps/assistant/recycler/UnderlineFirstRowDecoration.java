package day.cloudy.apps.assistant.recycler;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Gaelan Bolger on 12/19/2016.
 */
public class UnderlineFirstRowDecoration extends RecyclerView.ItemDecoration {

    private final LinearLayoutManager mLayoutManager;
    private final int mBottomPadding;
    private Paint mPaint;

    public UnderlineFirstRowDecoration(LinearLayoutManager layoutManager, int bottomPadding) {
        mLayoutManager = layoutManager;
        mBottomPadding = bottomPadding;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#AAAAAA"));
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildCount() > 0 && mLayoutManager.findFirstVisibleItemPosition() == 0) {
            View view = parent.getChildAt(0);
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();
            int bottom = view.getBottom() + mBottomPadding;
            c.drawLine(left, bottom, right, bottom, mPaint);
        }
    }
}
