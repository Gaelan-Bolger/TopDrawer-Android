package day.cloudy.apps.assistant.recycler.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import day.cloudy.apps.assistant.R;
import day.cloudy.apps.assistant.model.ApplicationItem;
import day.cloudy.apps.assistant.model.FrequentItem;
import day.cloudy.apps.assistant.recycler.OnItemClickListener;
import day.cloudy.apps.assistant.recycler.OnItemLongClickListener;
import day.cloudy.apps.assistant.util.IconUtils;

import static butterknife.ButterKnife.bind;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 * App drawer adapter
 */
public class ApplicationItemAdapter extends RecyclerView.Adapter<ApplicationItemAdapter.Holder> implements Filterable {

    private static final String TAG = ApplicationItemAdapter.class.getSimpleName();
    private static final float MIN_ALPHA = 0.3f;
    private static final float MAX_ALPHA = 1.0f;

    private final Context mContext;
    private OnItemClickListener<ApplicationItem> mOnItemClickListener;
    private OnItemLongClickListener<ApplicationItem> mOnItemLongClickListener;
    private List<ApplicationItem> mItems;
    private ApplicationItemFilter mFilter;
    private ApplicationItem mHighlightedItem;

    public ApplicationItemAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_application, parent, false));
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        ApplicationItem item = getItem(position);
        holder.bindView(IconUtils.normalize(item.icon), item.label);

        if (null != mOnItemClickListener) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(holder, getItem(holder.getAdapterPosition()));
                }
            });
        }
        if (null != mOnItemLongClickListener) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return mOnItemLongClickListener.onItemLongClick(holder, getItem(holder.getAdapterPosition()));
                }
            });
        }

        if (null != mHighlightedItem) {
            if (item.equals(mHighlightedItem)) {
                if (holder.itemView.getAlpha() != MAX_ALPHA)
                    ObjectAnimator.ofFloat(holder.itemView, "alpha", MIN_ALPHA, MAX_ALPHA).start();
            } else {
                ObjectAnimator.ofFloat(holder.itemView, "alpha", MAX_ALPHA, MIN_ALPHA).start();
            }
        } else if (holder.itemView.getAlpha() != MAX_ALPHA) {
            ObjectAnimator.ofFloat(holder.itemView, "alpha", MIN_ALPHA, MAX_ALPHA).start();
        }
    }

    @Override
    public int getItemCount() {
        return null != mItems ? mItems.size() : 0;
    }

    private ApplicationItem getItem(int position) {
        return mItems.get(position);
    }

    public boolean containsFrequentItems() {
        return getItemCount() > 0 && getItem(0) instanceof FrequentItem;
    }

    public void setApplications(List<ApplicationItem> applications) {
        mItems = applications;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<ApplicationItem> listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<ApplicationItem> listener) {
        mOnItemLongClickListener = listener;
    }

    @Override
    public ApplicationItemFilter getFilter() {
        if (null == mFilter) {
            mFilter = new ApplicationItemFilter(this, mItems);
        }
        return mFilter;
    }

    public void setFilterText(CharSequence text) {
        getFilter().filter(text);
    }

    public CharSequence getFilterText() {
        return getFilter().getText();
    }

    public void setHighlightedItem(ApplicationItem item) {
        mHighlightedItem = item;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_view_application_icon)
        ImageView icon;
        @BindView(R.id.text_view_application_label)
        TextView label;

        Holder(View itemView) {
            super(itemView);
            bind(this, itemView);
        }

        void bindView(Drawable icon, CharSequence label) {
            this.icon.setImageDrawable(icon);
            this.label.setText(label);
        }
    }

}
