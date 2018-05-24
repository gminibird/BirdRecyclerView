package com.gminibird.birdrecyclerview.item;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gminibird.birdrecyclerview.R;
import com.gminibird.birdrecyclerview.adapter.RecyclerViewHolder;

public class SimpleItem extends RecyclerItem {

    private String mText;

    public SimpleItem(String text){
        mText = text;
    }

    @Override
    public View getView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.simple_item,parent,false);
    }

    @Override
    public void convert(RecyclerViewHolder holder) {
        TextView textView = holder.getViewById(R.id.text);
        textView.setText(mText);

    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }
}
