package day.cloudy.apps.assistant.recycler;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Gaelan Bolger on 9/11/2016.
 * Generic item click listener for RecyclerView
 */
public interface OnItemLongClickListener<T> {

    boolean onItemLongClick(RecyclerView.ViewHolder holder, T item);
}