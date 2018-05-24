package com.gminibird.birdrecyclerview.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.gminibird.birdrecyclerview.item.IRecyclerItem;

/**
 * Created by a on 2018/3/11.
 */

public class RecyclerViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    private SparseArray<View> mViewMap = new SparseArray<>();


    public <T extends IRecyclerItem> RecyclerViewHolder(Context context, T item, ViewGroup parent) {
        super(item.getView(context, parent));
        mView = itemView;
        mView.requestFocusFromTouch();
    }

    public View getView() {
        return mView;
    }

    public <T extends View> T getViewById(int ResId) {
        View view = mViewMap.get(ResId);
        if (view == null) {
            view = mView.findViewById(ResId);
            mViewMap.put(ResId, view);
        }
        return (T) view;
    }

}