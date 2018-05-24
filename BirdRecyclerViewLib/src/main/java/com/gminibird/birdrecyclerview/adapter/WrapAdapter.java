package com.gminibird.birdrecyclerview.adapter;


import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.gminibird.birdrecyclerview.item.IRecyclerItem;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by a on 2018/3/24.
 */

public class WrapAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

    private final List<IRecyclerItem> HEADER_EMPTY_ITEM_LIST = new LinkedList<>();
    private final List<IRecyclerItem> FOOTER_EMPTY_ITEM_LIST = new LinkedList<>();
    private RecyclerAdapter mAdapter;
    private List<IRecyclerItem> mHeaders = HEADER_EMPTY_ITEM_LIST;
    private List<IRecyclerItem> mFooters = FOOTER_EMPTY_ITEM_LIST;
    private IRecyclerItem mDownRefresh;
    private IRecyclerItem mUpRefresh;
    private boolean isDownRefreshable = false;
    private boolean isUpRefreshable = false;
    private int mPosition;

    public WrapAdapter() {
    }

    public WrapAdapter(RecyclerAdapter adapter) {
        mAdapter = adapter;
    }

    public void setWrappedAdapter(RecyclerAdapter adapter) {
        mAdapter = adapter;
    }

    public void addDownRefresh(IRecyclerItem item) {
        mDownRefresh = item;
        if (mDownRefresh != null) {
            isDownRefreshable = true;
            notifyItemInserted(0);
        }
    }

    public void addUpRefresh(IRecyclerItem item) {
        mUpRefresh = item;
        if (mDownRefresh != null) {
            isUpRefreshable = true;
            notifyItemInserted(getItemCount() - 1);
        }
    }

    public void addHeaders(List<IRecyclerItem> headers) {
        if (headers == null) {
            mHeaders = HEADER_EMPTY_ITEM_LIST;
        } else {
            mHeaders = headers;
        }
        notifyHeaderItemsChanged();
    }

    public void addHeader(IRecyclerItem item) {
        if (item != null) {
            mHeaders.add(item);
            notifyHeaderItemRangeInserted(mHeaders.size() - 1, 1);
        }
    }

    public void removeHeaders(int positionStart, int itemCount) {
        for (int i = positionStart; i < positionStart + itemCount; i++) {
            mHeaders.remove(i);
        }
        notifyHeaderItemRangeRemoved(positionStart,itemCount);
    }

    public void addFooters(List<IRecyclerItem> footers) {
        if (footers == null) {
            mFooters = FOOTER_EMPTY_ITEM_LIST;
        } else {
            mFooters = footers;
        }
        notifyFooterItemChanged();
    }

    public void addFooter(IRecyclerItem item) {
        if (item != null) {
            mFooters.add(item);
            notifyFooterItemRangeInserted(mFooters.size(), 1);
        }
    }
    public void removeFooters(int positionStart, int itemCount) {
        for (int i = positionStart; i < positionStart + itemCount; i++) {
            mFooters.remove(i);
        }
        notifyFooterItemRangeRemoved(positionStart,itemCount);
    }

    private boolean isDownRefreshView(int position) {
        return isDownRefreshable && position == 0;
    }

    private boolean isUpRefreshView(int position) {
        if (isUpRefreshable) {
            return position == getItemCount() - 1;
        }
        return false;
    }

    private boolean isHeaderView(int position) {
        return position < mHeaders.size() + getDownRefreshSize()
                && position >= getDownRefreshSize();
    }

    private boolean isFooterView(int position) {
        return position >= mHeaders.size() + mAdapter.getItemCount() + getDownRefreshSize()
                && position < getItemCount() - getUpRefreshSize();

    }

    private int getDownRefreshSize() {
        return isDownRefreshable ? 1 : 0;
    }

    private int getUpRefreshSize() {
        return isUpRefreshable ? 1 : 0;
    }

    private int getFooterPosition(int globalPosition) {
        return globalPosition - mHeaders.size() - mAdapter.getItemCount()
                - getDownRefreshSize();
    }

    private int getHeaderPosition(int globalPosition) {
        return globalPosition - getDownRefreshSize();
    }

    private int getWrappedAdapterPosition(int globalPosition) {
        return globalPosition - mHeaders.size() - getDownRefreshSize();
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isDownRefreshView(mPosition)) {
            return new RecyclerViewHolder(mAdapter.getContext(), mDownRefresh, parent);
        }
        if (isUpRefreshView(mPosition)) {
            return new RecyclerViewHolder(mAdapter.getContext(), mUpRefresh, parent);
        }
        if (isHeaderView(mPosition)) {
            return new RecyclerViewHolder(mAdapter.getContext(), mHeaders.get(getHeaderPosition(mPosition)), parent);
        }
        if (isFooterView(mPosition)) {
            return new RecyclerViewHolder(mAdapter.getContext(), mFooters.get(getFooterPosition(mPosition)), parent);
        }
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        if (isDownRefreshView(position)) {
            mDownRefresh.convert(holder);
        } else if (isUpRefreshView(position)) {
            mUpRefresh.convert(holder);
        } else if (isHeaderView(position)) {
            mHeaders.get(getHeaderPosition(position)).convert(holder);
        } else if (isFooterView(position)) {
            mFooters.get(getFooterPosition(position)).convert(holder);
        } else {
            mAdapter.onBindViewHolder(holder, getWrappedAdapterPosition(position));
        }
    }


    @Override
    public int getItemCount() {
        return mAdapter.getItemCount() + mHeaders.size() + mFooters.size()
                + getUpRefreshSize() + getDownRefreshSize();
    }

    @Override
    public int getItemViewType(int position) {
        mPosition = position;
        if (isDownRefreshView(position)) {
            return mDownRefresh.getType();
        }
        if (isUpRefreshView(position)) {
            return mUpRefresh.getType();
        }
        if (isHeaderView(position)) {
            return mHeaders.get(getHeaderPosition(position)).getType();
        }
        if (isFooterView(position)) {
            return mFooters.get(getFooterPosition(position)).getType();
        }
        return mAdapter.getItemViewType(getWrappedAdapterPosition(position));
    }

    public int getHeaderStart() {
        return getDownRefreshSize();
    }

    public int getHeaderSize() {
        return mHeaders.size();
    }

    public int getWrappedDataStart() {
        return getHeaderStart() + mHeaders.size();
    }

    public int getWrappedDataSize() {
        return mAdapter.getItemCount();
    }

    public int getFooterStart() {
        return getWrappedDataStart() + getWrappedDataSize();
    }

    public int getFooterSize() {
        return mFooters.size();
    }


    public void notifyHeaderItemsChanged() {
        int headerStart = getDownRefreshSize();
        notifyItemRangeChanged(headerStart, mHeaders.size());
    }

    public void notifyHeaderItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(getHeaderStart() + positionStart, itemCount);
    }

    public void notifyHeaderItemRangeRemoved(int positionStart, int itemCount) {
        notifyItemRangeRemoved(getHeaderStart() + positionStart, itemCount);
    }

    public void notifyFooterItemChanged() {
        int footerStart = getDownRefreshSize() + mHeaders.size() + mAdapter.getItemCount();
        notifyItemRangeChanged(footerStart, mFooters.size());
    }

    public void notifyFooterItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(getFooterStart() + positionStart, itemCount);
    }

    public void notifyFooterItemRangeRemoved(int positionStart, int itemCount) {
        notifyItemRangeRemoved(getFooterStart() + positionStart, itemCount);
    }

    public void notifyWrappedDataSetChanged() {
        notifyItemRangeChanged(getWrappedDataStart(), getWrappedDataSize());
    }

    public void notifyWrappedItemRangeChanged(int positionStart, int itemCount) {
        notifyItemRangeChanged(getWrappedDataStart() + positionStart, itemCount);
    }

    public void notifyWrappedItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        notifyItemRangeChanged(positionStart, itemCount, payload);
    }

    public void notifyWrappedItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(getWrappedDataStart() + positionStart, itemCount);
    }

    public void notifyWrappedItemInserted(int position) {
        notifyItemInserted(getWrappedDataStart() + position);
    }

    public void notifyWrappedItemRemoved(int position) {
        notifyItemRemoved(getWrappedDataStart() + position);
    }

    public void notifyWrappedItemRangedRemoved(int positionStart, int itemCount) {
        notifyItemRangeRemoved(getWrappedDataSize() + positionStart, itemCount);
    }
}
