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
import android.util.Log;
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
    //回弹动画
    private ObjectAnimator reboundAnimator = null;
    //下拉刷新视图
    private View refreshView;
    private RecyclerView recyclerView;
    //上滑加载更多视图
    private View loadMoreView;
    //下拉刷新监听（方便用户实现自定义下拉刷新动画效果）
    private IRefresh refresh = null;
    //上滑加载更多监听（方便用户实现自定义上滑加载更多动画效果）
    private ILoadMore footer = null;

    //滑动速度跟踪器
    private VelocityTracker velocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int scaledTouchSlop;

    //下拉刷新视图之高度
    private int refreshViewHeight;
    //上滑加载更多视图之高度
    private int loadMoreViewHeight;

    //是否启动下拉刷新功能
    private boolean refreshEnable = true;
    //是否启动上滑加载更多功能
    private boolean loadMoreEnable = true;
    //分页加载时起始页码
    private int startPage = 1;
    //分页加载时单页最大数据量
    private int pageSize = 12;
    //分页加载时当前加载到第几页
    private int currentPage;
    //是否有下一页待加载的数据。如果没有下一页待加载的数据，则不能继续上滑。
    private boolean haveMore;
    //刷新、加载更多之监听
    private OnRefreshListener onRefreshListener = null;

    //进入滑动模式
    private boolean intoScrollingModel;
    private int touchedPointerId = -1;
    private float lastTouchY = 0.0f;

    //上一次刷新时间戳
    private long lastRefreshTimeStamp = 0L;
    //提示文案："下拉刷新"
    private CharSequence pullDownToRefreshText;
    //提示文案："释放刷新"
    private CharSequence releaseToRefreshText;
    //提示文案："正在刷新"
    private CharSequence refreshingText;
    //提示文案："刷新完成"
    private CharSequence refreshCompletedText;
    //提示文案："上滑加载更多"
    private CharSequence pullUpToLoadMoreText;
    //提示文案："释放加载更多"
    private CharSequence releaseToLoadMoreText;
    //提示文案："正在加载"
    private CharSequence loadingMoreText;
    //提示文案："加载更多完成"
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

        setPadding(getPaddingStart(), getPaddingTop(), getPaddingEnd(), getPaddingBottom());
        setHaveMore(false);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        recyclerView.setPadding(left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        refreshViewHeight = refreshView.getMeasuredHeight();
        loadMoreViewHeight = loadMoreView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        refreshView.layout(0, 0 - refreshView.getMeasuredHeight(), getMeasuredWidth(), 0);
        recyclerView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
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
                if (!intoScrollingModel) {
                    //这里使用PointerId防止多手指touch混乱问题
                    touchedPointerId = ev.getPointerId(0);
                    stopReboundAnim();
                    recyclerView.stopScroll();
                    lastTouchY = ev.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!intoScrollingModel && touchedPointerId == ev.getPointerId(0)) {
                    float curTouchY = ev.getY();
                    float dy = curTouchY - lastTouchY;
                    dy = dy > 0 ? dy + 0.5f : dy - 0.5f;
                    lastTouchY = curTouchY;
                    //如果滑动距离小于scaledTouchSlop，则把事件交给子View消耗；
                    //否则此事件交由自己的onTouchEvent(MotionEvent event)方法消耗。
                    if (Math.abs((int) dy) >= scaledTouchSlop / 2){
                        intoScrollingModel = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!intoScrollingModel) {
                    executeUpOrCancelMotionEventWithoutVelocity();
                }
                break;
        }
        return intoScrollingModel;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        enSureVelocityTrackerNonNull();
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (touchedPointerId == ev.getPointerId(0)) {
                    trackMotionEvent(ev);
                    lastTouchY = ev.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchedPointerId == ev.getPointerId(0)) {
                    trackMotionEvent(ev);
                    float curTouchY = ev.getY();
                    float dy = curTouchY - lastTouchY;
                    if (dy != 0) {
                        dy = dy > 0 ? dy + 0.5f : dy - 0.5f;
                        lastTouchY = curTouchY;
                        executeMove((int) -dy);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (touchedPointerId == ev.getPointerId(0)) {
                    final VelocityTracker tracker = velocityTracker;
                    tracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocity = (int) tracker.getYVelocity();
                    recycleVelocityTracker();
                    executeUpOrCancelMotionEvent(velocity);
                    intoScrollingModel = false;
                }
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
                refresh.onScroll(getState(), isRefreshEnable(), isRefreshing(), getScrollY(), refreshViewHeight, getRefreshThresholdValue());
                return;
            }

            if (getScrollY() < getRefreshThresholdValue()) {
                //release to refresh
                setState(RELEASE_TO_REFRESH);
            } else {
                //pull down to refresh
                setState(PULL_DOWN_TO_REFRESH);
            }
            refresh.onScroll(getState(), isRefreshEnable(), isRefreshing(), getScrollY(), refreshViewHeight, getRefreshThresholdValue());
        } else if (getScrollY() > 0) {
            if (!isLoadMoreEnable() || isLoadingMore() || !isHaveMore()) {
                footer.onScroll(getState(), isLoadMoreEnable(), isLoadingMore(), getScrollY(), loadMoreViewHeight, getLoadMoreThresholdValue());
                return;
            }

            if (getScrollY() > getLoadMoreThresholdValue()) {
                //release to load more
                setState(RELEASE_TO_LOAD_MORE);
            } else {
                //pull up to load more
                setState(PULL_UP_TO_LOAD_MORE);
            }
            footer.onScroll(getState(), isLoadMoreEnable(), isLoadingMore(), getScrollY(), loadMoreViewHeight, getLoadMoreThresholdValue());
        } else {
            refresh.onScroll(getState(), isRefreshEnable(), isRefreshing(), getScrollY(), refreshViewHeight, getRefreshThresholdValue());
            footer.onScroll(getState(), isLoadMoreEnable(), isLoadingMore(), getScrollY(), loadMoreViewHeight, getLoadMoreThresholdValue());
        }
    }

    private void executeUpOrCancelMotionEvent(int velocity) {
        switch (getState()) {
            case REFRESHING:
                executeRebound(0 - refreshViewHeight);
                recyclerView.fling(0, 0 - velocity);
                break;
            case LOADING_MORE:
                executeRebound(loadMoreViewHeight);
                recyclerView.fling(0, 0 - velocity);
                break;
            case RELEASE_TO_REFRESH:
                executeRebound(0 - refreshViewHeight);
                break;
            case RELEASE_TO_LOAD_MORE:
                executeRebound(isHaveMore() ? loadMoreViewHeight : 0);
                break;
            default:
                executeRebound(0);
                recyclerView.fling(0, 0 - velocity);
                break;
        }
    }

    private void executeUpOrCancelMotionEventWithoutVelocity() {
        switch (getState()) {
            case REFRESHING:
                executeRebound(0 - refreshViewHeight);
                break;
            case LOADING_MORE:
                executeRebound(loadMoreViewHeight);
                break;
            case RELEASE_TO_REFRESH:
                executeRebound(0 - refreshViewHeight);
                break;
            case RELEASE_TO_LOAD_MORE:
                executeRebound(isHaveMore() ? loadMoreViewHeight : 0);
                break;
            default:
                executeRebound(0);
                break;
        }
    }

    private void executeRebound(int destinationScrollY) {
        int scrollYDistance = destinationScrollY - getScrollY();
        if (scrollYDistance == 0)
            return;

        int duration = Math.abs(scrollYDistance);
        duration = Math.max(200, duration);
        duration = Math.min(500, duration);
        if (reboundAnimator == null) {
            reboundAnimator = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofInt(SCROLL_Y, getScrollY(), destinationScrollY));
            reboundAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            reboundAnimator.addListener(new SimpleAnimatorListener() {
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
            reboundAnimator.setIntValues(getScrollY(), destinationScrollY);
        }
        reboundAnimator.setDuration(duration);
        reboundAnimator.start();
    }

    private void stopReboundAnim() {
        if (reboundAnimator != null && reboundAnimator.isRunning()) {
            setState(INIT);
            reboundAnimator.cancel();
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
        Log.i(TAG, "setState: " + Integer.toHexString(state));
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
        return 0 - refreshViewHeight / 2;
    }

    private int getLoadMoreThresholdValue() {
        return loadMoreViewHeight / 2;
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
            executeRebound(0 - refreshViewHeight);
            return;
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                //refresh
                setState(RELEASE_TO_REFRESH);
                executeRebound(0 - refreshViewHeight);
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
