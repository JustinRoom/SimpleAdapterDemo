package jsc.kit.adapter.refresh;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jsc.kit.adapter.R;
import jsc.kit.adapter.SimpleAnimatorListener;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/23 15:51
 *
 * @author jsc
 */
public class SwipeRefreshRecyclerView extends FrameLayout {
    public static final String TAG = "SwipeRefresh";
    public static final int DEFAULT_START_PAGE_NUMBER = 1;
    public static final int DEFAULT_PAGE_SIZE = 12;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View loadMoreView;

    private int startPageNumber = DEFAULT_START_PAGE_NUMBER;
    private int currentPageNumber = startPageNumber;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private boolean isLoading = false;
    private boolean hasMorePage = false;
    private boolean refreshEnable = true;
    private boolean loadMoreEnable = true;
    private Rect visibleRect = new Rect();
    private OnRefreshListener onRefreshListener = null;
    private int translationDistance = 0;
    private Animator defaultLoadingAnimator = null;
    private Animator defaultLoadedAnimator = null;
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState != RecyclerView.SCROLL_STATE_IDLE)
                return;

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (layoutManager == null || adapter == null) {
//                swipeRefreshLayout.setEnabled(false);
                return;
            }

            if (isLoading() || !isLoadMoreEnable() || !isHasMorePage() || !isScrollToBottom())
                return;

            //如果第一条和最后一条同时可见
            if (swipeRefreshLayout.isEnabled()) {
                //如果SwipeRefreshLayout中的CircleImageView可见，则走刷新逻辑
                if (isCircleImageViewVisible()) {
                    return;
                }
            }

            Log.i(TAG, "onScrollStateChanged: ");
            if (onRefreshListener != null) {
                showScrollUpAnim();
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    public SwipeRefreshRecyclerView(Context context) {
        super(context);
        initView(context);
        initAttrs(context, null, 0);
    }

    public SwipeRefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initAttrs(context, attrs, 0);
    }

    public SwipeRefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initAttrs(context, attrs, defStyleAttr);
    }

    private void initView(Context context) {
        inflate(context, R.layout.recycler_default_swipe_refresh_recycler_view_layout, this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.recycler_view);

        swipeRefreshLayout.setColorSchemeColors(0xFF3F51B5, 0xFF303F9F);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setLoading(true);
                currentPageNumber = startPageNumber;
                onRefreshListener.onRefresh(currentPageNumber, pageSize);
            }
        });
        recyclerView.addOnScrollListener(scrollListener);
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeRefreshRecyclerView, defStyleAttr, 0);
        int layoutId = a.getResourceId(R.styleable.SwipeRefreshRecyclerView_srrvLoadMoreLayout, R.layout.recycler_default_load_more_layout);
        a.recycle();

        if (loadMoreView != null) {
            removeView(loadMoreView);
            loadMoreView = null;
        }

        if (layoutId != -1) {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            loadMoreView = LayoutInflater.from(context).inflate(layoutId, this, false);
            addView(loadMoreView, params);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        translationDistance = loadMoreView.getMeasuredHeight();
        loadMoreView.setTranslationY(translationDistance);
    }

    private boolean isCircleImageViewVisible() {
        for (int i = 0; i < swipeRefreshLayout.getChildCount(); i++) {
            View child = swipeRefreshLayout.getChildAt(i);
            if (child instanceof ImageView) {
                child.getLocalVisibleRect(visibleRect);
                if (visibleRect.bottom > 0)
                    return true;
            }
        }
        return false;
    }

    public void reset() {
        setHasMorePage(false);
        setLoading(false);
        if (defaultLoadingAnimator != null && defaultLoadingAnimator.isRunning())
            defaultLoadingAnimator.cancel();
        if (defaultLoadedAnimator != null && defaultLoadedAnimator.isRunning())
            defaultLoadedAnimator.cancel();
        loadMoreView.setTranslationY(translationDistance);
    }

    private void showScrollUpAnim() {
        if (isLoading())
            return;

        setLoading(true);
        swipeRefreshLayout.setEnabled(false);
        if (defaultLoadingAnimator == null)
            defaultLoadingAnimator = createDefaultLoadingAnimator();
        defaultLoadingAnimator.start();
    }

    private void showScrollDownAnim() {
        if (defaultLoadedAnimator == null)
            defaultLoadedAnimator = createDefaultLoadedAnimator();
        defaultLoadedAnimator.start();
    }

    /**
     * Whether scrolled to the bottom of list.
     * @return true, scrolled to the bottom of list, else false.
     */
    private boolean isScrollToBottom() {
        return recyclerView.computeVerticalScrollRange() <=
                recyclerView.computeVerticalScrollOffset() +
                recyclerView.computeVerticalScrollExtent();
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public View getLoadMoreView() {
        return loadMoreView;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void loadComplete() {
        if (currentPageNumber == startPageNumber)
            refreshComplete();
        else
            loadMoreComplete();
    }

    public void refreshComplete() {
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);
    }

    public void loadMoreComplete() {
        showScrollDownAnim();
    }

    public void refresh() {
        if (isLoading())
            return;
        refreshDelay(0);
    }

    public void refreshDelay(long delay) {
        if (isLoading())
            return;

        swipeRefreshLayout.setRefreshing(true);
        if (delay <= 0) {
            startRefreshing();
            return;
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                startRefreshing();
            }
        }, delay);
    }

    private void startRefreshing() {
        isLoading = true;
        if (onRefreshListener != null) {
            currentPageNumber = startPageNumber;
            onRefreshListener.onRefresh(currentPageNumber, pageSize);
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isRefreshEnable() {
        return refreshEnable;
    }

    public void setRefreshEnable(boolean refreshEnable) {
        this.refreshEnable = refreshEnable;
        swipeRefreshLayout.setEnabled(refreshEnable);
    }

    public boolean isLoadMoreEnable() {
        return loadMoreEnable;
    }

    public void setLoadMoreEnable(boolean loadMoreEnable) {
        this.loadMoreEnable = loadMoreEnable;
    }

    public boolean isHasMorePage() {
        return hasMorePage;
    }

    public void setHasMorePage(boolean hasMorePage) {
        this.hasMorePage = hasMorePage;
    }

    public boolean isFirstPage() {
        return currentPageNumber == startPageNumber;
    }

    public int getStartPageNumber() {
        return startPageNumber;
    }

    public void setStartPageNumber(int startPageNumber) {
        this.startPageNumber = startPageNumber;
    }

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    private Animator createDefaultLoadingAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(loadMoreView, TRANSLATION_Y, loadMoreView.getTranslationY(), 0);
        animator.setDuration(300);
        animator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onRefreshListener != null) {
                    currentPageNumber++;
                    onRefreshListener.onLoadMore(currentPageNumber, pageSize);
                }
            }
        });
        return animator;
    }

    private Animator createDefaultLoadedAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(loadMoreView, TRANSLATION_Y, loadMoreView.getTranslationY(), translationDistance);
        animator.setDuration(300);
        animator.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setLoading(false);
                swipeRefreshLayout.setEnabled(true
                );
            }
        });
        return animator;
    }

    @Override
    protected void onDetachedFromWindow() {
        getHandler().removeCallbacksAndMessages(null);
        reset();
        super.onDetachedFromWindow();
    }

    public interface OnRefreshListener {
        /**
         * Do refreshing.
         */
        void onRefresh(int currentPageNumber, int pageSize);

        /**
         * Do loading more.
         */
        void onLoadMore(int currentPageNumber, int pageSize);
    }
}
