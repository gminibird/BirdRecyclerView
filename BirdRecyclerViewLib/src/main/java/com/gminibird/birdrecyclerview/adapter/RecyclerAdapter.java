package com.gminibird.birdrecyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.gminibird.birdrecyclerview.item.IRecyclerItem;

import java.util.List;

/**
 * Created by a on 2018/4/25.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private List<IRecyclerItem> mItemList;
    private Context mContext;
    //保存当前 ViewHolder 在 Adapter 中的位置
    private int mPosition;
    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;

    public RecyclerAdapter(Context context, List<IRecyclerItem> itemList) {
        mContext = context;
        mItemList = itemList;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(mContext, mItemList.get(mPosition), parent);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.getView().setTag(position);
        setListener(holder);
        mItemList.get(position).convert(holder);
    }

    @Override
    public int getItemViewType(int position) {
        mPosition = position;
        return mItemList.get(position).getType();
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public Context getContext() {
        return mContext;
    }

    protected void setListener(final RecyclerViewHolder holder) {
        if (mClickListener != null) {
            holder.getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onClick(v,(int)v.getTag());
                }
            });
        }
        if (mLongClickListener != null) {
            holder.getView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mLongClickListener.onLongClick(v,(int)v.getTag());
                }
            });
        }
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mClickListener = l;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) {
        mLongClickListener = l;
    }

    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(View view, int position);
    }
}

