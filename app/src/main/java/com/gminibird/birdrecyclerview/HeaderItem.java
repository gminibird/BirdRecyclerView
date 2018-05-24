package com.gminibird.birdrecyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gminibird.birdrecyclerview.adapter.RecyclerViewHolder;
import com.gminibird.birdrecyclerview.item.RecyclerItem;

public class HeaderItem extends RecyclerItem {

    private String mText;


    public HeaderItem(String text) {
        mText = text;
    }

    @Override
    public View getView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.header_item, parent, false);
    }

    @Override
    public void convert(RecyclerViewHolder holder) {
        super.convert(holder);
        TextView textView = holder.getViewById(R.id.text);
        textView.setText(mText);

    }


}
