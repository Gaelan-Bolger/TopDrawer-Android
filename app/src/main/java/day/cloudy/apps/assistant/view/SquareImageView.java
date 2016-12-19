package day.cloudy.apps.assistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 */

public class SquareImageView extends ImageView {

    private final int mOrientation;

    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;

    public SquareImageView(Context context) {
        this(context, null);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.orientation});
        mOrientation = ta.getInt(0, 0);
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mOrientation == HORIZONTAL)
            setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
        else
            setMeasuredDimension(getMeasuredHeight(), getMeasuredHeight());
    }
}
