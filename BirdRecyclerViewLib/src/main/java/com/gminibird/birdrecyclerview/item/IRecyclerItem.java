package com.gminibird.birdrecyclerview.item;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.gminibird.birdrecyclerview.adapter.RecyclerAdapter;
import com.gminibird.birdrecyclerview.adapter.RecyclerViewHolder;


/**
 *{@link android.support.v7.widget.RecyclerView} 的子item,
 * 此接口需要配合{@link RecyclerAdapter}使用。
 * 如果需要使用多item类型，你应该为每一个item都创建一个实现该接口的类。
 */
public interface IRecyclerItem {

    /**
     *@return 返回item的type,当使用多item时必须保证每个item返回不同的type
     */
    int getType();

    /**
     * 此方法在{@link RecyclerAdapter#onCreateViewHolder(ViewGroup, int)}
     * 内调用，你应该在此方法的实现中返回item的view对象
     * @return item view
     */
    View getView(Context context, ViewGroup parent);

    /**
     * 此方法在{@link RecyclerAdapter#onBindViewHolder(RecyclerViewHolder, int)}
     * 内调用,你应该在此方法的实现中进行item的view与数据的绑定
     * @param holder 当前item的{@link RecyclerViewHolder}
     */
    void convert(RecyclerViewHolder holder);

}
