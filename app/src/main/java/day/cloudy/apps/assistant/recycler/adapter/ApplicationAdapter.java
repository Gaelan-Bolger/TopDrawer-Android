package day.cloudy.apps.assistant.recycler.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import day.cloudy.apps.assistant.model.ApplicationItem;
import day.cloudy.apps.assistant.R;
import day.cloudy.apps.assistant.recycler.OnItemClickListener;
import day.cloudy.apps.assistant.recycler.OnItemLongClickListener;
import day.cloudy.apps.assistant.util.IconUtil;

import static butterknife.ButterKnife.bind;

/**
 * Created by Gaelan Bolger on 12/16/2016.
 * App drawer adapter
 */
public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.Holder> implements Filterable {

    private static final String TAG = ApplicationAdapter.class.getSimpleName();

    private final Context sContext;
    private OnItemClickListener<ApplicationItem> sOnItemClickListener;
    private OnItemLongClickListener<ApplicationItem> sOnItemLongClickListener;
    private List<ApplicationItem> sApplications;
    private ApplicationFilter sFilter;

    public ApplicationAdapter(Context context) {
        sContext = context;
    }

    public void setOnItemClickListener(OnItemClickListener<ApplicationItem> listener) {
        sOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<ApplicationItem> listener) {
        sOnItemLongClickListener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(sContext).inflate(R.layout.item_application, parent, false));
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        ApplicationItem applicationItem = getItem(position);
        holder.bindView(IconUtil.normalize(applicationItem.icon), applicationItem.label);
        if (null != sOnItemClickListener) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sOnItemClickListener.onItemClick(holder, getItem(holder.getAdapterPosition()));
                }
            });
        }
        if (null != sOnItemLongClickListener) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return sOnItemLongClickListener.onItemLongClick(holder, getItem(holder.getAdapterPosition()));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return null != sApplications ? sApplications.size() : 0;
    }

    private ApplicationItem getItem(int position) {
        return sApplications.get(position);
    }

    public void setApplications(List<ApplicationItem> applications) {
        sApplications = applications;
        notifyDataSetChanged();
    }

    @Override
    public ApplicationFilter getFilter() {
        if (null == sFilter) {
            sFilter = new ApplicationFilter(this, sApplications);
        }
        return sFilter;
    }

    public void setFilterText(CharSequence text) {
        sFilter.filter(text);
    }

    public CharSequence getFilterText() {
        return getFilter().getText();
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
