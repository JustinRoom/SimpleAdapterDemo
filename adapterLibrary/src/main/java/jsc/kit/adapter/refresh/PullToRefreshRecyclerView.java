package jsc.kit.adapter.refresh;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jsc.kit.adapter.SimpleAnimatorListener;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2019/1/2 09:53 Wednesday
 *
 * @author jsc
 */
public class PullToRefreshRecyclerView extends ViewGroup {

    private static final String TAG = "pullToRefresh";
    public static final Property<View, Integer> SCROLL_X = new Property<View, Integer>(Integer.class, "mScrollX") {

        @Override
        public Integer get(View object) {
            return object.getScrollX();
        }

        @Override
        public void set(View object, Integer value) {
            object.setScrollX(value);
        }
    };
    public static final Property<View, Integer> SCROLL_Y = new Property<View, Integer>(Integer.class, "mScrollY") {

        @Override
        public Integer get(View object) {
            return object.getScrollY();
        }

        @Override
        public void set(View object, Integer value) {
            object.setScrollY(value);
        }
    };
    /**
     * 状态：初始化
     */
    public static final int INIT = -1;
    /**
     * 状态：下拉刷新
     */
    public static final int PULL_DOWN_TO_REFRESH = 0x10;
    /**
     * 状态：释放刷新
     */
    public static final int RELEASE_TO_REFRESH = 0x11;
    /**
     * 状态：刷新中
     */
    public static final int REFRESHING = 0x12;
    /**
     * 状态：刷新完成
     */
    public static final int REFRESH_COMPLETED = 0x13;
    /**
     * 状态：上滑加载更多
     */
    public static final int PULL_UP_TO_LOAD_MORE = 0x20;
    /**
     * 状态：释放加载更多
     */
    public static final int RELEASE_TO_LOAD_MORE = 0x21;
    /**
     * 状态：正在加载更多
     */
    public static final int LOADING_MORE = 0x22;
    /**
     * 状态：加载更多完成
     */
    public static final int LOAD_MORE_COMPLETED = 0x23;

    @IntDef({
            INIT,
            PULL_DOWN_TO_REFRESH,
            RELEASE_TO_REFRESH,
            REFRESHING,
            REFRESH_COMPLETED,
            PULL_UP_TO_LOAD_MORE,
            RELEASE_TO_LOAD_MORE,
            LOADING_MORE,
            LOAD_MORE_COMPLETED
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    private int state = INIT;
    private ObjectAnimator animator = null;

    private View refreshView;
    private RecyclerView recyclerView;
    private View loadMoreView;
    private IRefresh refresh = null;
    private ILoadMore footer = null;

    private VelocityTracker velocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int scaledTouchSlop;

    private int headerHeight;
    private int footerHeight;

    private boolean refreshEnable = true;
    private boolean loadMoreEnable = true;
    private int startPage = 1;
    private int pageSize = 12;
    private int currentPage;
    private boolean haveMore;
    private OnRefreshListener onRefreshListener = null;

    private float lastTouchY = 0.0f;

    private long lastRefreshTimeStamp = 0L;
    private CharSequence pullDownToRefreshText;
    private CharSequence releaseToRefreshText;
    private CharSequence refreshingText;
    private CharSequence refreshCompletedText;
    private CharSequence pullUpToLoadMoreText;
    private CharSequence releaseToLoadMoreText;
    private CharSequence loadingMoreText;
    private CharSequence loadMoreCompletedText;

    public PullToRefreshRecyclerView(Context context) {
        super(context);
        initView(context);
        initAttrs(context, null, 0);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        initAttrs(context, attrs, 0);
    }

    public PullToRefreshRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initAttrs(context, attrs, defStyleAttr);
    }

    private void initView(Context context) {
        inflate(context, jsc.kit.adapter.R.layout.recycler_pull_to_refresh_recycler_view, this);
        recyclerView = findViewById(jsc.kit.adapter.R.id.recycler_view);

        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        scaledTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView, defStyleAttr, 0);
        int refreshLayoutId = a.getResourceId(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvRefreshLayout, -1);
        int loadMoreLayoutId = a.getResourceId(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvLoadMoreLayout, -1);

        //refresh text
        pullDownToRefreshText = a.hasValue(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvPullDownToRefreshText) ?
                a.getString(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvPullDownToRefreshText) :
                getResources().getString(jsc.kit.adapter.R.string.recycler_default_pull_down_to_refresh);
        releaseToRefreshText = a.hasValue(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvReleaseToRefreshText) ?
                a.getString(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvReleaseToRefreshText) :
                getResources().getString(jsc.kit.adapter.R.string.recycler_default_release_to_refresh);
        refreshingText = a.hasValue(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvRefreshingText) ?
                a.getString(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvRefreshingText) :
                getResources().getString(jsc.kit.adapter.R.string.recycler_default_refreshing);
        refreshCompletedText = a.hasValue(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvRefreshCompletedText) ?
                a.getString(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvRefreshCompletedText) :
                getResources().getString(jsc.kit.adapter.R.string.recycler_default_refresh_completed);

        //load more text
        pullUpToLoadMoreText = a.hasValue(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvPullUpToLoadMoreText) ?
                a.getString(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvPullUpToLoadMoreText) :
                getResources().getString(jsc.kit.adapter.R.string.recycler_default_pull_up_to_load_more);
        releaseToLoadMoreText = a.hasValue(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvReleaseToLoadMoreText) ?
                a.getString(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvReleaseToLoadMoreText) :
                getResources().getString(jsc.kit.adapter.R.string.recycler_default_release_to_load_more);
        loadingMoreText = a.hasValue(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvLoadingMoreText) ?
                a.getString(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvLoadingMoreText) :
                getResources().getString(jsc.kit.adapter.R.string.recycler_default_loading_more);
        loadMoreCompletedText = a.hasValue(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvLoadMoreCompletedText) ?
                a.getString(jsc.kit.adapter.R.styleable.PullToRefreshRecyclerView_prvLoadMoreCompletedText) :
                getResources().getString(jsc.kit.adapter.R.string.recycler_default_load_more_completed);
        a.recycle();

        if (refreshLayoutId == -1) {
            refreshView = LayoutInflater.from(context).inflate(jsc.kit.adapter.R.layout.recycler_default_header_view, this, false);
            setRefresh(createDefaultHeader());
        } else {
            refreshView = LayoutInflater.from(context).inflate(refreshLayoutId, this, false);
        }
        if (loadMoreLayoutId == -1) {
            loadMoreView = LayoutInflater.from(context).inflate(jsc.kit.adapter.R.layout.recycler_default_footer_view, this, false);
            setLoadMore(createDefaultFooter());
        } else {
            loadMoreView = LayoutInflater.from(context).inflate(loadMoreLayoutId, this, false);
        }
        addView(refreshView, 0);
        addView(loadMoreView);


        setHaveMore(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        headerHeight = refreshView.getMeasuredHeight();
        footerHeight = loadMoreView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        refreshView.layout(0, 0 - refreshView.getMeasuredHeight(), getMeasuredWidth(), 0);
        recyclerView.layout(getPaddingLeft(), getPaddingTop(), getMeasuredWidth() - getPaddingRight(), getMeasuredHeight() - getPaddingBottom());
        loadMoreView.layout(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight() + loadMoreView.getMeasuredHeight());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getState() == REFRESH_COMPLETED
                || getState() == LOAD_MORE_COMPLETED)
            return super.onInterceptTouchEvent(ev);

        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                stopReboundAnim();
                recyclerView.stopScroll();
                lastTouchY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float curTouchY = ev.getY();
                float dy = curTouchY - lastTouchY;
                dy = dy > 0 ? dy + 0.5f : dy - 0.5f;
                lastTouchY = curTouchY;
                //如果滑动距离小于scaledTouchSlop，则把事件交给子View消耗；
                //否则此事件交由自己的onTouchEvent(MotionEvent event)方法消耗。
                if (Math.abs((int) dy) >= scaledTouchSlop / 2)
                    return true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        enSureVelocityTrackerNonNull();
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                trackMotionEvent(ev);
                lastTouchY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                trackMotionEvent(ev);
                float curTouchY = ev.getY();
                float dy = curTouchY - lastTouchY;
                if (dy != 0) {
                    dy = dy > 0 ? dy + 0.5f : dy - 0.5f;
                    lastTouchY = curTouchY;
                    executeMove((int) -dy);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                final VelocityTracker tracker = velocityTracker;
                tracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocity = (int) tracker.getYVelocity();
                recycleVelocityTracker();
                executeUpOrCancelMotionEvent(velocity);
                break;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        getHandler().removeCallbacksAndMessages(null);
        recycleVelocityTracker();
        super.onDetachedFromWindow();
    }

    private void executeMove(int distance) {
        if (distance == 0)
            return;

        int scrollY = getScrollY();
        int scrolledY = 0;
        if (distance < 0) {//向下滑动
            if (!isLoadingMore() && scrollY > 0) {
                scrolledY = Math.max(0 - scrollY, distance);
                scrollBy(0, scrolledY);
                distance = distance - scrolledY;
            }

            scrolledY = Math.max(0 - getRecyclerViewMaxCanPullDownDistance(), distance);
            if (scrolledY != 0)
                recyclerView.scrollBy(0, scrolledY);

            if (!isLoadingMore()) {
                distance = distance - scrolledY;
                distance = toScaledValue(distance);
                if (distance != 0)
                    scrollBy(0, distance);
            }
        } else {//向上滑动
            if (!isRefreshing() && scrollY < 0) {
                scrolledY = Math.min(Math.abs(scrollY), distance);
                scrollBy(0, scrolledY);
                distance = distance - scrolledY;
            }

            scrolledY = Math.min(getRecyclerViewMaxCanPullUpDistance(), distance);
            if (scrolledY != 0)
                recyclerView.scrollBy(0, scrolledY);

            if (!isRefreshing()) {
                distance = distance - scrolledY;
                distance = toScaledValue(distance);
                if (distance != 0)
                    scrollBy(0, distance);
            }
        }

        if (getScrollY() < 0) {
            if (!isRefreshEnable() || isRefreshing()) {
                refresh.onScroll(getState(), isRefreshEnable(), isRefreshing(), getScrollY(), headerHeight, getRefreshThresholdValue());
                return;
            }

            if (getScrollY() < getRefreshThresholdValue()) {
                //release to refresh
                setState(RELEASE_TO_REFRESH);
            } else {
                //pull down to refresh
                setState(PULL_DOWN_TO_REFRESH);
            }
            refresh.onScroll(getState(), isRefreshEnable(), isRefreshing(), getScrollY(), headerHeight, getRefreshThresholdValue());
        } else if (getScrollY() > 0) {
            if (!isLoadMoreEnable() || isLoadingMore()) {
                footer.onScroll(getState(), isLoadMoreEnable(), isLoadingMore(), getScrollY(), footerHeight, getLoadMoreThresholdValue());
                return;
            }

            if (getScrollY() > getLoadMoreThresholdValue()) {
                //release to load more
                setState(RELEASE_TO_LOAD_MORE);
            } else {
                //pull up to load more
                setState(PULL_UP_TO_LOAD_MORE);
            }
            footer.onScroll(getState(), isLoadMoreEnable(), isLoadingMore(), getScrollY(), footerHeight, getLoadMoreThresholdValue());
        } else {
            refresh.onScroll(getState(), isRefreshEnable(), isRefreshing(), getScrollY(), headerHeight, getRefreshThresholdValue());
            footer.onScroll(getState(), isLoadMoreEnable(), isLoadingMore(), getScrollY(), footerHeight, getLoadMoreThresholdValue());
        }


    }

    private void executeUpOrCancelMotionEvent(int velocity) {
        switch (getState()) {
            case REFRESHING:
                executeRebound(0 - headerHeight);
                recyclerView.fling(0, 0 - velocity);
                break;
            case LOADING_MORE:
                executeRebound(footerHeight);
                recyclerView.fling(0, 0 - velocity);
                break;
            case RELEASE_TO_REFRESH:
                executeRebound(0 - headerHeight);
                break;
            case RELEASE_TO_LOAD_MORE:
                executeRebound(isHaveMore() ? footerHeight : 0);
                break;
            default:
                executeRebound(0);
                recyclerView.fling(0, 0 - velocity);
                break;
        }
    }

    private void executeRebound(int destinationScrollY) {
        int scrollYDistance = destinationScrollY - getScrollY();
        int duration = Math.abs(scrollYDistance);
        duration = Math.max(200, duration);
        duration = Math.min(500, duration);
        if (animator == null) {
            animator = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofInt(SCROLL_Y, getScrollY(), destinationScrollY));
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    switch (getState()) {
                        case RELEASE_TO_REFRESH:
                            if (!isRefreshing() && onRefreshListener != null) {
                                setState(REFRESHING);
                                currentPage = startPage;
                                onRefreshListener.onRefresh(getContext(), currentPage, pageSize);
                            }
                            break;
                        case RELEASE_TO_LOAD_MORE:
                            if (isHaveMore() && !isLoadingMore() && onRefreshListener != null) {
                                setState(LOADING_MORE);
                                currentPage++;
                                onRefreshListener.onLoadMore(getContext(), currentPage, pageSize);
                            }
                            break;
                        case REFRESH_COMPLETED:
                            setState(INIT);
                            lastRefreshTimeStamp = System.currentTimeMillis();
                            refresh.updateLastRefreshTime(lastRefreshTimeStamp);
                            break;
                        case LOAD_MORE_COMPLETED:
                            setState(INIT);
                            break;
                    }
                }
            });
        } else {
            animator.setIntValues(getScrollY(), destinationScrollY);
        }
        animator.setDuration(duration);
        animator.start();
    }

    private void stopReboundAnim() {
        if (animator != null && animator.isRunning()) {
            setState(INIT);
            animator.cancel();
        }
    }

    private void enSureVelocityTrackerNonNull() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void trackMotionEvent(MotionEvent ev) {
        if (velocityTracker != null)
            velocityTracker.addMovement(ev);
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private int getState() {
        return state;
    }

    private void setState(@State int state) {
        this.state = state;
        switch (state) {
            case INIT:
                refresh.onUpdateState(state, "");
                footer.onUpdateState(state, "");
                break;

            case PULL_DOWN_TO_REFRESH:
                refresh.onUpdateState(state, pullDownToRefreshText);
                break;
            case RELEASE_TO_REFRESH:
                refresh.onUpdateState(state, releaseToRefreshText);
                break;
            case REFRESHING:
                refresh.onUpdateState(state, refreshingText);
                break;
            case REFRESH_COMPLETED:
                refresh.onUpdateState(state, refreshCompletedText);
                break;

            case PULL_UP_TO_LOAD_MORE:
                footer.onUpdateState(state, pullUpToLoadMoreText);
                break;
            case RELEASE_TO_LOAD_MORE:
                footer.onUpdateState(state, releaseToLoadMoreText);
                break;
            case LOADING_MORE:
                footer.onUpdateState(state, loadingMoreText);
                break;
            case LOAD_MORE_COMPLETED:
                footer.onUpdateState(state, loadMoreCompletedText);
                break;
        }
    }

    private int toScaledValue(int value) {
        return value * 3 / 5;
    }

    private int getRefreshThresholdValue() {
        return 0 - headerHeight / 2;
    }

    private int getLoadMoreThresholdValue() {
        return footerHeight / 2;
    }

    private int getRecyclerViewMaxCanPullDownDistance() {
        return recyclerView.computeVerticalScrollOffset();
    }

    private int getRecyclerViewMaxCanPullUpDistance() {
        return recyclerView.computeVerticalScrollRange() -
                recyclerView.computeVerticalScrollOffset() -
                recyclerView.computeVerticalScrollExtent();
    }


    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public void setLayoutManager(@Nullable RecyclerView.LayoutManager layout) {
        recyclerView.setLayoutManager(layout);
    }

    public void initializeParameters(int startPage, int pageSize) {
        this.startPage = startPage;
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void refresh() {
        refreshDelay(0L);
    }

    public void refreshDelay(long delay) {
        if (delay <= 0) {
            //refresh
            setState(RELEASE_TO_REFRESH);
            executeRebound(0 - headerHeight);
            return;
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                //refresh
                setState(RELEASE_TO_REFRESH);
                executeRebound(0 - headerHeight);
            }
        }, delay);
    }

    public void completed() {
        if (currentPage == startPage) {
            setState(REFRESH_COMPLETED);
            executeRebound(0);
        } else {
            setState(LOAD_MORE_COMPLETED);
            executeRebound(0);
        }
    }

    public boolean isRefreshing() {
        return getState() == REFRESHING;
    }

    public boolean isLoadingMore() {
        return getState() == LOADING_MORE;
    }

    public boolean isFirstPage() {
        return currentPage == startPage;
    }

    public boolean isRefreshEnable() {
        return refreshEnable;
    }

    public void setRefreshEnable(boolean enable) {
        this.refreshEnable = enable;
        refreshView.setVisibility(enable ? VISIBLE : INVISIBLE);
        if (!enable)
            setState(INIT);
    }

    public boolean isLoadMoreEnable() {
        return loadMoreEnable;
    }

    public void setLoadMoreEnable(boolean enable) {
        this.loadMoreEnable = enable;
        loadMoreView.setVisibility(enable ? VISIBLE : INVISIBLE);
        if (!enable)
            setState(INIT);
    }

    public boolean isHaveMore() {
        return haveMore;
    }

    public void setHaveMore(boolean haveMore) {
        this.haveMore = haveMore;
        if (isLoadMoreEnable())
            loadMoreView.setVisibility(haveMore ? VISIBLE : INVISIBLE);
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public <R extends IRefresh> void setRefresh(@NonNull R refresh) {
        this.refresh = refresh;
        this.refresh.initChildren(refreshView);
    }

    public <L extends ILoadMore> void setLoadMore(@NonNull L loadMore) {
        this.footer = loadMore;
        this.footer.initChildren(loadMoreView);
    }

    private IRefresh createDefaultHeader() {
        return new IRefresh() {
            ProgressBar headerProgressBar;
            ImageView ivHeaderIcon;
            TextView tvLastRefreshTime;
            TextView tvRefreshTips;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

            @Override
            public void initChildren(@NonNull View headerView) {
                headerProgressBar = headerView.findViewById(jsc.kit.adapter.R.id.recycler_header_progress_bar);
                ivHeaderIcon = headerView.findViewById(jsc.kit.adapter.R.id.recycler_iv_header_icon);
                tvLastRefreshTime = headerView.findViewById(jsc.kit.adapter.R.id.recycler_tv_last_refresh_time);
                tvRefreshTips = headerView.findViewById(jsc.kit.adapter.R.id.recycler_tv_refresh_tips);

                headerProgressBar.setVisibility(GONE);
            }

            @Override
            public void updateLastRefreshTime(long lastRefreshTimeStamp) {
                if (tvLastRefreshTime != null) {
                    if (lastRefreshTimeStamp == 0) {
                        tvLastRefreshTime.setText("");
                        return;
                    }
                    tvLastRefreshTime.setText(String.format(
                            Locale.CHINA,
                            getResources().getString(jsc.kit.adapter.R.string.recycler_default_last_refresh_time),
                            dateFormat.format(new Date(lastRefreshTimeStamp))
                    ));
                }
            }

            @Override
            public void onUpdateState(int state, CharSequence txt) {
                tvRefreshTips.setText(txt);
                switch (state) {
                    case PullToRefreshRecyclerView.REFRESHING:
                        headerProgressBar.setVisibility(VISIBLE);
                        ivHeaderIcon.setVisibility(GONE);
                        break;
                    case PullToRefreshRecyclerView.REFRESH_COMPLETED:
                        headerProgressBar.setVisibility(GONE);
                        ivHeaderIcon.setVisibility(VISIBLE);
                        ivHeaderIcon.setRotation(0);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(int state, boolean refreshEnable, boolean isRefreshing, int scrollY, int headerHeight, int refreshThresholdValue) {
                if (!refreshEnable)
                    return;
                float rotation = 180 * 1.0f * scrollY / refreshThresholdValue;
                rotation = Math.min(rotation, 180);
                ivHeaderIcon.setRotation(rotation);
            }
        };
    }

    private ILoadMore createDefaultFooter() {
        return new ILoadMore() {
            ProgressBar footerProgressBar;
            TextView tvLoadMoreTips;

            @Override
            public void initChildren(@NonNull View footerView) {
                footerProgressBar = footerView.findViewById(jsc.kit.adapter.R.id.recycler_footer_progress_bar);
                tvLoadMoreTips = footerView.findViewById(jsc.kit.adapter.R.id.recycler_tv_load_more_tips);

                footerProgressBar.setVisibility(GONE);
            }

            @Override
            public void onUpdateState(@State int state, CharSequence txt) {
                tvLoadMoreTips.setText(txt);
                switch (state) {
                    case PullToRefreshRecyclerView.LOADING_MORE:
                        footerProgressBar.setVisibility(VISIBLE);
                        break;
                    case PullToRefreshRecyclerView.LOAD_MORE_COMPLETED:
                        footerProgressBar.setVisibility(GONE);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(int state, boolean loadMoreEnable, boolean isLoadingMore, int scrollY, int footerHeight, int loadMoreThresholdValue) {

            }
        };
    }

    public interface OnRefreshListener {

        void onRefresh(@NonNull Context context, int nextPage, int pageSize);

        void onLoadMore(@NonNull Context context, int nextPage, int pageSize);
    }
}
