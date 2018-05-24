package com.gminibird.birdrecyclerview.item;

import com.gminibird.birdrecyclerview.adapter.RecyclerViewHolder;

public abstract class RecyclerItem implements IRecyclerItem {

    private OnConvertListener mListener;

    @Override
    public int getType() {
        return getClass().hashCode();
    }

    @Override
    public void convert(RecyclerViewHolder holder) {
        if (mListener!=null){
            mListener.onConvert(holder);
        }
    }

    public void addOnConvertListener(OnConvertListener l){
        mListener = l;
    }

    public interface OnConvertListener{
        void onConvert(RecyclerViewHolder holder);
    }
}
