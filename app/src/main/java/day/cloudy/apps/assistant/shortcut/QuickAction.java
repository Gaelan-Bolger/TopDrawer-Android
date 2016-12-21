package day.cloudy.apps.assistant.shortcut;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import day.cloudy.apps.assistant.R;
import day.cloudy.apps.assistant.recycler.ListSpacingDecoration;

/**
 * This is for the custom QuickAction dialogs used in GrooveMobile.
 * The app allow anyone to listen to music from Grooveshark on there Android-device for free.
 * All code is copywrited to the authors! The main-library is writen by scilor!
 * Non of the authors are related to Grooveshark in any way!
 *
 * @author Pontus Holmberg (EndLessMind)
 *         Email: the_mr_hb@hotmail.com
 **/
public class QuickAction extends PopupWindows implements OnDismissListener {

    private LayoutInflater mInflater;
    private View mRootView;
    private RecyclerView mRecycler;
    private OnActionItemClickListener mItemClickListener;
    private OnDismissListener mDismissListener;

    private List<ActionItem> actionItems = new ArrayList<>();

    private boolean mDidAction;
    private boolean isDismissed = false;

    private int mAnimStyle = ANIM_AUTO;
    private int rootWidth = 0;

    public static final int ANIM_GROW_FROM_LEFT = 1;
    public static final int ANIM_GROW_FROM_RIGHT = 2;
    public static final int ANIM_GROW_FROM_CENTER = 3;
    public static final int ANIM_REFLECT = 4;
    public static final int ANIM_AUTO = 5;

    /**
     * Constructor allowing orientation override
     *
     * @param context Context
     */
    public QuickAction(Context context) {
        super(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setRootViewId(R.layout.popup_quick_action);
    }

    /**
     * Set root view.
     *
     * @param id Layout resource id
     */
    public void setRootViewId(int id) {
        //	setOutsideTouchable(true);
        mRootView = mInflater.inflate(id, null);
        mRecycler = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mRecycler.setHasFixedSize(true);
        mRecycler.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        mRecycler.setLayoutManager(new LinearLayoutManager(mContext));
        mRecycler.addItemDecoration(new ListSpacingDecoration(mContext, R.dimen.action_item_space));
        mRecycler.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ActionHolder(mInflater.inflate(R.layout.item_quick_action, parent, false));
            }

            @Override
            public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
                ActionItem item = getActionItem(position);
                ((ActionHolder) holder).icon.setImageDrawable(item.getIcon());
                ((ActionHolder) holder).text.setText(item.getTitle());
                if (null != mItemClickListener)
                    holder.itemView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int adapterPosition = holder.getAdapterPosition();
                            ActionItem action = getActionItem(adapterPosition);
                            mItemClickListener.onItemClick(QuickAction.this, adapterPosition, action.getActionId());
                        }
                    });
            }

            @Override
            public int getItemCount() {
                return actionItems.size();
            }
        });

        //This was previously defined on show() method, moved here to prevent force close that occured
        //when tapping fastly on a view to show quickaction dialog.
        //Thanx to zammbi (github.com/zammbi)
        mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setContentView(mRootView);
    }

    private class ActionHolder extends RecyclerView.ViewHolder {

        private final ImageView icon;
        public final TextView text;

        ActionHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.iv_icon);
            text = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }

    /**
     * Set animation style
     *
     * @param mAnimStyle animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(int mAnimStyle) {
        this.mAnimStyle = mAnimStyle;
    }

    /**
     * Set listener for action item clicked.
     *
     * @param listener Listener
     */
    public void setOnActionItemClickListener(OnActionItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * Add action item
     *
     * @param action {@link ActionItem}
     */
    public void addActionItem(ActionItem action) {
        actionItems.add(action);
        mRecycler.getAdapter().notifyDataSetChanged();
    }

    /**
     * Get action item at an index
     *
     * @param index Index of item (position from callback)
     * @return Action Item at the position
     */
    public ActionItem getActionItem(int index) {
        return actionItems.get(index);
    }

    /**
     * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
     */
    public void show(View anchor) {
        preShow();
        mDidAction = false;

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1]
                + anchor.getHeight());

        //mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int rootHeight = mRootView.getMeasuredHeight();
        if (rootWidth == 0) {
            rootWidth = mRootView.getMeasuredWidth();
        }

        int xPos, yPos;
        int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        //automatically get X coord of popup (top left)
        if ((anchorRect.left + rootWidth) > screenWidth) {
            xPos = anchorRect.left - (rootWidth - anchor.getWidth()) + anchor.getPaddingRight();
            xPos = (xPos < 0) ? 0 : xPos;
        } else {
            if (anchor.getWidth() > rootWidth) {
                xPos = anchorRect.centerX() - (rootWidth / 2);
            } else {
                xPos = anchorRect.left - anchor.getPaddingLeft();
            }
        }

        int dyTop = anchorRect.top;
        int dyBottom = screenHeight - anchorRect.bottom;
        boolean onTop = (dyTop > rootHeight);
        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
                LayoutParams l = mRecycler.getLayoutParams();
                l.height = dyTop - anchor.getHeight();
            } else {
                yPos = anchorRect.top - rootHeight;
            }
        } else {
            yPos = anchorRect.bottom;
            if (rootHeight > dyBottom) {
                LayoutParams l = mRecycler.getLayoutParams();
                l.height = dyBottom;
            }
        }

        setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
    }

    /**
     * Set animation style
     *
     * @param screenWidth screen width
     * @param requestedX  distance from left edge
     * @param onTop       flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
     *                    and vice versa
     */
    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        switch (mAnimStyle) {
            case ANIM_AUTO:
            case ANIM_GROW_FROM_RIGHT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
                break;
            case ANIM_GROW_FROM_LEFT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
                break;
            case ANIM_GROW_FROM_CENTER:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
                break;
            case ANIM_REFLECT:
                mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
                break;
        }
    }

    /**
     * Set listener for window dismissed. This listener will only be fired if the quicakction dialog is dismissed
     * by clicking outside the dialog or clicking on sticky item.
     */
    public void setOnDismissListener(OnDismissListener listener) {
        setOnDismissListener(this);
        mDismissListener = listener;
    }

    @Override
    public void onDismiss() {
        if (!mDidAction && mDismissListener != null) {
            isDismissed = true;
            mDismissListener.onDismiss();
        }
    }

    /**
     * Listener for item click
     */
    public interface OnActionItemClickListener {
        void onItemClick(QuickAction source, int pos, int actionId);
    }

    /**
     * Listener for window dismiss
     */
    public interface OnDismissListener {
        void onDismiss();
    }
}