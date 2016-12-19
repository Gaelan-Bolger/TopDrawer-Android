package day.cloudy.apps.assistant.shortcut;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * This is for the custom Popup windows used in GrooveMobile.
 * The app allow anyone to listen to music from Grooveshark on there Android-device for free.
 * All code is copywrited to the authors! The main-library is writen by scilor!
 * Non of the authors are related to Grooveshark in any way!
 *
 * @author Pontus Holmberg (EndLessMind)
 *         Email: the_mr_hb@hotmail.com
 **/
public class PopupWindows {
    protected Context mContext;
    public static PopupWindow mWindow;
    protected View mRootView;
    protected Drawable mBackground = null;
    protected WindowManager mWindowManager;

    /**
     * Constructor.
     *
     * @param context Context
     */
    public PopupWindows(Context context) {
        mContext = context;
        mWindow = new PopupWindow(context);
        mWindow.setBackgroundDrawable(new BitmapDrawable());
        mWindow.setTouchInterceptor(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    mWindow.dismiss();

                    return true;
                }
                return false;
            }
        });
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * On dismiss
     */
    protected void onDismiss() {
    }

    /**
     * On show
     */
    protected void onShow() {
    }


    /**
     * On pre show
     */
    protected void preShow() {
        if (mRootView == null)
            throw new IllegalStateException("setContentView was not called with a view to display.");

        onShow();

        //	if (mBackground == null)
        //		mWindow.setBackgroundDrawable(new BitmapDrawable());
        //else
        //		mWindow.setBackgroundDrawable(new BitmapDrawable());

        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//		mWindow.setTouchable(true);
        //	mWindow.setFocusable(true);
        //	mWindow.setOutsideTouchable(true);

        mWindow.setContentView(mRootView);
    }

    /**
     * Set background drawable.
     *
     * @param background Background drawable
     */
    public void setBackgroundDrawable(Drawable background) {
        mWindow.setBackgroundDrawable(background);
    }

    /**
     * Set focusable.
     *
     * @param focusable Focusable
     */
    public void setFocusable(boolean focusable) {
        mWindow.setFocusable(focusable);
    }

    /**
     * Set content view.
     *
     * @param root Root view
     */
    public void setContentView(View root) {
        mRootView = root;

        mWindow.setContentView(root);
    }

    /**
     * Set content view.
     *
     * @param layoutResID Resource id
     */
    public void setContentView(int layoutResID) {
        LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(layoutResID, null));
    }

    /**
     * Set listener on window dismissed.
     *
     * @param listener
     */
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener);
    }

    /**
     * Dismiss the popup window.
     */
    public static void dismiss() {
        if (null != mWindow && mWindow.isShowing())
            mWindow.dismiss();
    }
}