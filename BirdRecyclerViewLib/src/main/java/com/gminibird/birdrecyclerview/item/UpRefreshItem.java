package com.gminibird.birdrecyclerview.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gminibird.birdrecyclerview.R;
import com.gminibird.birdrecyclerview.adapter.RecyclerViewHolder;
import com.gminibird.birdrecyclerview.view.RefreshView;

public class UpRefreshItem extends RecyclerItem {

    @Override
    public View getView(Context context, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.refresh_item,parent,false);
        RefreshView refreshView = view.findViewById(R.id.refresh_view);
        refreshView.setOriginState(RefreshView.STATE_PREPARED);
        return view;
    }

}