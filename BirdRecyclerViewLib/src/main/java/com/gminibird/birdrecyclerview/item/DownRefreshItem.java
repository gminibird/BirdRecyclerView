package com.gminibird.birdrecyclerview.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gminibird.birdrecyclerview.R;
import com.gminibird.birdrecyclerview.adapter.RecyclerViewHolder;

public class DownRefreshItem extends RecyclerItem {

    @Override
    public View getView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.refresh_item,parent,false);
    }

}