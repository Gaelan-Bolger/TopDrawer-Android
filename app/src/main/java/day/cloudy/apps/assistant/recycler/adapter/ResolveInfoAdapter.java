package day.cloudy.apps.assistant.recycler.adapter;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import day.cloudy.apps.assistant.recycler.OnItemClickListener;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 */
public class ResolveInfoAdapter extends RecyclerView.Adapter<ResolveInfoAdapter.Holder> {

    private Context mContext;
    private List<ResolveInfo> mInfoList;
    private OnItemClickListener<ResolveInfo> mListener;

    public ResolveInfoAdapter(Context context, List<ResolveInfo> infoList, OnItemClickListener<ResolveInfo> listener) {
        mContext = context;
        mInfoList = infoList;
        mListener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false));
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        holder.bind(getItem(position));
        if (null != mListener)
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemClick(holder, getItem(holder.getAdapterPosition()));
                }
            });
    }

    @Override
    public int getItemCount() {
        return null != mInfoList ? mInfoList.size() : 0;
    }

    private ResolveInfo getItem(int position) {
        return mInfoList.get(position);
    }

    class Holder extends RecyclerView.ViewHolder {

        private TextView text1;
        private TextView text2;

        Holder(View itemView) {
            super(itemView);
            text1 = ButterKnife.findById(itemView, android.R.id.text1);
            text2 = ButterKnife.findById(itemView, android.R.id.text2);
        }

        void bind(ResolveInfo resolveInfo) {
            text1.setText(resolveInfo.activityInfo.packageName);
            text2.setText(resolveInfo.activityInfo.name);
        }
    }
}
