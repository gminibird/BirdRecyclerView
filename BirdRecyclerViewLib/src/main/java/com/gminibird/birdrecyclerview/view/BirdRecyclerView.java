package com.gminibird.birdrecyclerview.view;


import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.gminibird.birdrecyclerview.R;
import com.gminibird.birdrecyclerview.adapter.RecyclerAdapter;
import com.gminibird.birdrecyclerview.item.IRecyclerItem;
import com.gminibird.birdrecyclerview.adapter.RecyclerViewHolder;
import com.gminibird.birdrecyclerview.adapter.WrapAdapter;
import com.gminibird.birdrecyclerview.item.DownRefreshItem;
import com.gminibird.birdrecyclerview.item.RecyclerItem;
import com.gminibird.birdrecyclerview.item.UpRefreshItem;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.view.ViewCompat.TYPE_TOUCH;

/**
 * Created by a on 2018/3/22.
 */

public class BirdRecyclerView extends RecyclerView {

    //下拉view状态改变监听器
    private OnChangeListener mDownChangeListener;
    //上拉view状态改变监听器
    private OnChangeListener mUpChangeListener;
    //正在下拉刷新监听器
    private OnRefreshListener mDownRefreshListener;
    //正在上拉加载监听器
    private OnRefreshListener mUpRefreshListener;
    /**
     * 用户主数据Adapter，此 adapter 用于被 {@link WrapAdapter}包裹，
     * {@link #getAdapter()} 返回的是此对象
     */
    private RecyclerAdapter mAdapter;
    /**
     * 此 RecyclerView 的真正 adapter，内部包裹了{@link #mAdapter},
     * 并且封装了相关逻辑，不需要操作此adapter，只需调用{@link #mAdapter}
     * 的相关方法即可
     */
    private WrapAdapter mWrapAdapter = new WrapAdapter();
    //下拉刷新view
    private View mDownRefreshView;
    //上拉加载view
    private View mUpRefreshView;
    //下拉刷新view的LayoutParams
    private MarginLayoutParams mParams;
    /**
     * 列表Header，如果有下拉刷新View，那么此View将在mHeaders.get(0)中
     */
    private List<IRecyclerItem> mHeaders = new ArrayList<>();
    /**
     * 列表Footer
     **/
    private List<IRecyclerItem> mFooters = new ArrayList<>();
    //刷新类型
    public static final int TYPE_DOWN_PULL = 0; //下拉刷新
    public static final int TYPE_UP_PULL = 1;  //下拉加载
    public static final int TYPE_NORMAL = 2;  //普通模式（没有任何刷新）
    //当前刷新类型
    private int mRefreshType = TYPE_NORMAL;
    //用来判断第一次刷新类型的flag
    private boolean mRefreshFlag = false;
    //默认下拉刷新距离(dip),当下拉距离大于此才会触发刷新
    private final int REFRESH_TOP_OFFSET = 60;
    /**
     * 下拉刷新距离(px),当下拉距离大于此才会触发刷新
     * 可以使用{@link #setRefreshOffsetEnd(int)}来自定义此属性
     */
    private int mRefreshOffsetEnd;

    //拖动阻力系数
    private final float DRAG_RATIO = 0.4f;
    //下拉view初始TopMargin
    private int mOriginTopMargin = 0;
    //动画持续时间
    private final int ANIMATE_TO_TRIGGER_DURATION = 200;
    private final int ANIMATE_TO_START_DURATION = 200;
    /**
     * 是否可以下拉刷新
     */
    private boolean isDownRefreshable = false;
    /**
     * 是否可以上拉刷新
     */
    private boolean isUpRefreshable = false;
    //上拉view是否可见
    private boolean isUpRefreshViewVisible = false;

    private Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int currentTopMargin = mParams.topMargin;
            int endTopMargin = mRefreshOffsetEnd + mOriginTopMargin;
            mParams.topMargin += (int) ((endTopMargin - currentTopMargin) * interpolatedTime);
            scrollToPosition(0);
        }
    };

    private Animation mAnimateToStartPosition = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int currentTopMargin = mParams.topMargin;
            int endTopMargin = mOriginTopMargin;
            mParams.topMargin += (int) ((endTopMargin - currentTopMargin) * interpolatedTime);
            mDownRefreshView.requestLayout();
        }
    };

    private Animation.AnimationListener mAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            if (!mRefreshing) {
                if (mRefreshType == TYPE_DOWN_PULL) {
                    if (mDownChangeListener != null) {
                        mDownChangeListener.onRefreshFinished(mDownRefreshView);
                    }
                } else if (mRefreshType == TYPE_UP_PULL) {
                    if (mUpChangeListener != null) {
                        mUpChangeListener.onRefreshFinished(mUpRefreshView);
                    }
                }
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!mRefreshing) {
                mRefreshType = TYPE_NORMAL;
                return;
            }
            if (mRefreshType == TYPE_DOWN_PULL) {
                if (mDownChangeListener != null) {
                    mDownChangeListener.onRefreshing(mDownRefreshView);
                }
                if (mDownRefreshListener != null) {
                    mDownRefreshListener.onRefresh();
                }
            } else if (mRefreshType == TYPE_UP_PULL) {
                if (mUpChangeListener != null) {
                    mUpChangeListener.onRefreshing(mUpRefreshView);
                }
                if (mDownRefreshListener != null) {
                    mDownRefreshListener.onRefresh();
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };

    private boolean mRefreshing = false;
    private float mRawY = -1;
    private float mScaledTouchSlop = new ViewConfiguration().getScaledTouchSlop();


    private AdapterDataObserver mDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            mWrapAdapter.notifyWrappedDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyWrappedItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyWrappedItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyWrappedItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyWrappedItemRangedRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyWrappedItemRangedRemoved(fromPosition, itemCount);
        }

    };

    public BirdRecyclerView(Context context) {
        super(context);
        init();
    }

    public BirdRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BirdRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        ((SimpleItemAnimator) getItemAnimator()).setSupportsChangeAnimations(false);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);

    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (!(adapter instanceof RecyclerAdapter)) {
            throw new IllegalArgumentException("Adapter must instanceof RecyclerAdapter");
        }
        if (mAdapter != null) {
            mAdapter.unregisterAdapterDataObserver(mDataObserver);
            mAdapter = null;
        }
        mAdapter = (RecyclerAdapter) adapter;
        mAdapter.registerAdapterDataObserver(mDataObserver);
        mWrapAdapter.setWrappedAdapter(mAdapter);
        super.setAdapter(mWrapAdapter);
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    protected void animateMarginToCorrectPosition(Animation.AnimationListener listener) {
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        if (listener != null) {
            mAnimateToCorrectPosition.setAnimationListener(listener);
        }
        if (mDownRefreshView != null) {
            mDownRefreshView.clearAnimation();
            mDownRefreshView.startAnimation(mAnimateToCorrectPosition);
        }
    }

    protected void animateMarginToStartPosition(Animation.AnimationListener listener) {
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
        if (listener != null) {
            mAnimateToStartPosition.setAnimationListener(listener);
        }
        if (mDownRefreshView != null) {
            mDownRefreshView.clearAnimation();
            mDownRefreshView.startAnimation(mAnimateToStartPosition);
        }
    }

    public void setDownRefreshing(boolean refreshing) {

        if (refreshing) {
            smoothScrollToPosition(0);
            mRefreshing = true;
            mRefreshType = TYPE_DOWN_PULL;
            animateMarginToCorrectPosition(mAnimationListener);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRefreshing = false;
                    animateMarginToStartPosition(mAnimationListener);
                }
            }, 500);

        }
        mRefreshFlag = false;
    }

    public void setUpRefreshing(boolean refreshing) {
        if (refreshing) {
            mRefreshing = true;
            if (mUpChangeListener != null) {
                mUpChangeListener.onRefreshing(mUpRefreshView);
            }
            if (mUpRefreshListener != null) {
                mUpRefreshListener.onRefresh();
            }
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRefreshing = false;
                    if (mUpChangeListener != null) {
                        mUpChangeListener.onRefreshFinished(mUpRefreshView);
                    }
                }
            }, 500);
        }
        mRefreshFlag = false;
    }

    protected boolean canRefresh() {
        switch (mRefreshType) {
            case TYPE_DOWN_PULL:
                return canDownRefresh();
            case TYPE_UP_PULL:
                return canUpRefresh();
        }
        return false;
    }

    protected boolean canDownRefresh() {
        return isDownRefreshable && mParams.topMargin >= mRefreshOffsetEnd + mOriginTopMargin;
    }

    protected boolean canUpRefresh() {
        return isUpRefreshable && !canScrollVertically(1);
    }

    protected void dispatchRefresh(boolean refreshing) {
        switch (mRefreshType) {
            case TYPE_DOWN_PULL:
                setDownRefreshing(refreshing);
                break;
            case TYPE_UP_PULL:
                setUpRefreshing(refreshing);
                break;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRefreshType = TYPE_NORMAL;
                break;
            case MotionEvent.ACTION_MOVE:

                if (!isDownRefreshable && !isUpRefreshable) {
                    break;
                }
                boolean canDownScroll = canScrollVertically(-1);
                boolean canUpScroll = canScrollVertically(1);
                if (mRefreshType == TYPE_NORMAL && canDownScroll && canUpScroll) {
                    break;
                }
                if (!mRefreshFlag) {
                    mRawY = e.getRawY();
                    mRefreshFlag = true;
                    break;
                }
                float offsetY = e.getRawY() - mRawY;
                if (Math.abs(offsetY) < mScaledTouchSlop) {
                    break;
                }
                mRawY = e.getRawY();
                switch (mRefreshType) {
                    case TYPE_DOWN_PULL:
                        updateMargin((int) offsetY);
                        if (mDownChangeListener != null) {
                            mDownChangeListener.onPull(mDownRefreshView,
                                    (mParams.topMargin * 1f - mOriginTopMargin) / -mOriginTopMargin);
                        }
                        super.onTouchEvent(e);
                        return true;
                    case TYPE_UP_PULL:
                        if (mUpChangeListener != null) {
                            mUpChangeListener.onPull(mDownRefreshView, 1);
                        }
                        super.onTouchEvent(e);
                        return true;
                    default:
                        if (offsetY >= 0 && isDownRefreshable && !canDownScroll) {
                            mRefreshType = TYPE_DOWN_PULL;
                            setNestedScrollingEnabled(false);
                        } else if (offsetY < 0 && isUpRefreshable && !canUpScroll) {
                            if (isUpRefreshViewVisible) {
                                mRefreshType = TYPE_UP_PULL;
                            }
                        }
                        super.onTouchEvent(e);
                        return true;
                }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (canRefresh()) {
                    dispatchRefresh(true);
                } else {
                    if (mRefreshType == TYPE_DOWN_PULL) {
                        animateMarginToStartPosition(null);
                        mRefreshType = TYPE_NORMAL;
                    }
                }
                mRefreshFlag = false;
                setNestedScrollingEnabled(true);
                break;
        }
        return super.onTouchEvent(e);
    }

    protected void updateMargin(int offsetY) {
        int topMargin = mParams.topMargin;
        if (offsetY > 0) {
            topMargin += (int) (offsetY * DRAG_RATIO);
        } else {
            topMargin += offsetY;
        }
        if (topMargin <= mOriginTopMargin) {
            mParams.topMargin = mOriginTopMargin;
            mRefreshType = TYPE_NORMAL;
            restoreNestedScrolling();
        } else {
            mParams.topMargin = topMargin;
        }
        mDownRefreshView.setLayoutParams(mParams);
    }

    private void restoreNestedScrolling() {
        setNestedScrollingEnabled(true);
        int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
        nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
        startNestedScroll(nestedScrollAxis, TYPE_TOUCH);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (isDownRefreshable && mDownRefreshView == null) {
            initDownRefreshView();
        }
    }

    private void changeVisibility() {
        mUpRefreshView.setVisibility(isUpRefreshViewVisible ? VISIBLE : INVISIBLE);
    }


    private void initDownRefreshView() {
        mDownRefreshView = getChildAt(0);
        mParams = (MarginLayoutParams) mDownRefreshView.getLayoutParams();
        mOriginTopMargin = -mDownRefreshView.getBottom() + 1;
        mParams.topMargin = mOriginTopMargin;
    }


    /**
     * @see #addDownPullRefresh(IRecyclerItem, OnChangeListener, OnRefreshListener)
     */
    public void addDownPullRefresh(OnRefreshListener listener) {
        addDownPullRefresh(new DownRefreshItem(), new OnDownPullChangeListener(), listener);
    }

    /**
     * 为 BirdRecyclerView 添加下拉刷新
     *
     * @param refreshItem 自定义刷新 view，就需要传递此参数
     * @param changeL     当需要自定义View时，需要传入此参数，当下拉会调用此对象的相关方法{@link OnChangeListener},
     *                    你可以在相关方法中对view进行一些动画效果操作
     * @param refreshL    刷新监听器，你可以实现{@link OnRefreshListener#onRefresh()}方法，
     *                    在该方法中进行数据请求
     */
    public void addDownPullRefresh(IRecyclerItem refreshItem, OnChangeListener changeL, OnRefreshListener refreshL) {
        mWrapAdapter.addDownRefresh(refreshItem);
        isDownRefreshable = true;
        mDownRefreshListener = refreshL;
        setOnDownChangeListener(changeL);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mRefreshOffsetEnd = (int) (REFRESH_TOP_OFFSET * metrics.density);
    }

    /**
     * 为 BirdRecyclerView 添加上拉加载
     *
     * @param refreshItem 自定义刷新 view，就需要传递此参数
     * @param changeL     当需要自定义View时，需要传入此参数，当上拉会调用此对象的相关方法{@link OnChangeListener},
     *                    你可以在相关方法中对view进行一些动画效果操作
     * @param refreshL    刷新监听器，你可以实现{@link OnRefreshListener#onRefresh()}方法，
     *                    在该方法中进行数据请求
     */
    public void addUpPullRefresh(RecyclerItem refreshItem, OnChangeListener changeL, OnRefreshListener refreshL) {
        refreshItem.addOnConvertListener(new RecyclerItem.OnConvertListener() {
            @Override
            public void onConvert(RecyclerViewHolder holder) {
                initUpRefreshView(holder);
            }
        });
        mWrapAdapter.addUpRefresh(refreshItem);
        isUpRefreshable = true;
        mUpRefreshListener = refreshL;
        setOnUpChangeListener(changeL);
    }

    private void initUpRefreshView(RecyclerViewHolder holder) {
        mUpRefreshView = holder.getViewById(R.id.refresh_view);
        mUpRefreshView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mUpRefreshView.post(new Runnable() {
                    @Override
                    public void run() {
                        int childHeight = mUpRefreshView.getHeight();
                        for (int i = isDownRefreshable ? 1 : 0; i < getChildCount(); i++) {
                            childHeight += getChildAt(i).getHeight();
                            if (childHeight >= getHeight()) {
                                mUpRefreshView.setVisibility(VISIBLE);
                                isUpRefreshViewVisible = true;
                                return;
                            }
                        }
                        mUpRefreshView.setVisibility(INVISIBLE);
                        isUpRefreshViewVisible = false;
                    }
                });
            }
            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });
    }

    /**
     * @see #addUpPullRefresh(RecyclerItem, OnChangeListener, OnRefreshListener)
     */
    public void addUpPullRefresh(OnRefreshListener listener) {
        addUpPullRefresh(new UpRefreshItem(), new OnUpPullChangeListener(), listener);
    }


    public void setOnDownChangeListener(OnChangeListener listener) {
        mDownChangeListener = listener;
    }

    public void setOnUpChangeListener(OnChangeListener listener) {
        mUpChangeListener = listener;
    }


    public void addHeaders(List<IRecyclerItem> items) {
        mWrapAdapter.addHeaders(items);
    }

    public void addHeader(IRecyclerItem item) {
        mWrapAdapter.addHeader(item);
    }

    public void addFooters(List<IRecyclerItem> items) {
        mWrapAdapter.addFooters(items);
    }

    public void addFooter(IRecyclerItem item) {
        mWrapAdapter.addFooter(item);
    }

    public void setRefreshOffsetEnd(int offset) {
        mRefreshOffsetEnd = offset;
    }

    public class OnDownPullChangeListener implements OnChangeListener {
        RefreshView refreshView;

        @Override
        public void onPull(View view, float fraction) {
            if (refreshView == null) {
                refreshView = view.findViewById(R.id.refresh_view);
            }
            refreshView.drag(fraction);
        }

        @Override
        public void onRefreshing(View view) {
            if (refreshView == null) {
                refreshView = view.findViewById(R.id.refresh_view);
            }
            refreshView.start();
        }

        @Override
        public void onRefreshFinished(View view) {
            if (refreshView == null) {
                refreshView = view.findViewById(R.id.refresh_view);
            }
            refreshView.stop();
            refreshView.prepareToStart();
        }
    }

    public class OnUpPullChangeListener implements OnChangeListener {

        RefreshView refreshView;

        @Override
        public void onPull(View view, float fraction) {

        }

        @Override
        public void onRefreshing(View view) {
            if (refreshView == null) {
                refreshView = view.findViewById(R.id.refresh_view);
            }
            refreshView.start();
        }

        @Override
        public void onRefreshFinished(View view) {
            if (refreshView == null) {
                refreshView = view.findViewById(R.id.refresh_view);
            }
            refreshView.stop();
            refreshView.prepareToStart();
        }
    }

    public interface OnChangeListener {

        /**
         * 当刷新View被拖动时调用
         *
         * @param view     根view
         * @param fraction 拖动的进度百分比（小数 0~1）
         */
        void onPull(View view, float fraction);

        //刷新时调用，在这个方法内应该为view添加一些刷新动画
        void onRefreshing(View view);

        //刷新完成时调用，你可以在此方法内进行动画的回收还有view状态的还原
        void onRefreshFinished(View view);
    }

    public interface OnRefreshListener {
        //刷新时调用
        void onRefresh();
    }
}
