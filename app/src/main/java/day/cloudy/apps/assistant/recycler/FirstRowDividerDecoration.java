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
public class FirstRowDividerDecoration extends RecyclerView.ItemDecoration {

    private final LinearLayoutManager mLayoutManager;
    private Paint mPaint;

    public FirstRowDividerDecoration(LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#AAAAAA"));
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildCount() > 0 && mLayoutManager.findFirstVisibleItemPosition() == 0) {
            View child = parent.getChildAt(0);
            int left = parent.getLeft();
            int right = parent.getRight();
            int bottom = child.getBottom();
            c.drawLine(left, bottom, right, bottom, mPaint);
        }
    }
}
