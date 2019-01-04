package jsc.kit.adapter.refresh;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
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
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jsc.kit.adapter.R;
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

    private View headerView;
    private RecyclerView recyclerView;
    private View footerView;
    private IHeader header = null;
    private IFooter footer = null;

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
        inflate(context, R.layout.recycler_pull_to_refresh_recycler_view, this);
        recyclerView = findViewById(R.id.recycler_view);

        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        scaledTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefreshRecyclerView, defStyleAttr, 0);
        int headerLayoutId = a.getResourceId(R.styleable.PullToRefreshRecyclerView_prvHeaderLayout, -1);
        int footerLayoutId = a.getResourceId(R.styleable.PullToRefreshRecyclerView_prvFooterLayout, -1);

        //refresh text
        pullDownToRefreshText = a.hasValue(R.styleable.PullToRefreshRecyclerView_prvPullDownToRefreshText) ?
                a.getString(R.styleable.PullToRefreshRecyclerView_prvPullDownToRefreshText) :
                getResources().getString(R.string.recycler_default_pull_down_to_refresh);
        releaseToRefreshText = a.hasValue(R.styleable.PullToRefreshRecyclerView_prvReleaseToRefreshText) ?
                a.getString(R.styleable.PullToRefreshRecyclerView_prvReleaseToRefreshText) :
                getResources().getString(R.string.recycler_default_release_to_refresh);
        refreshingText = a.hasValue(R.styleable.PullToRefreshRecyclerView_prvRefreshingText) ?
                a.getString(R.styleable.PullToRefreshRecyclerView_prvRefreshingText) :
                getResources().getString(R.string.recycler_default_refreshing);
        refreshCompletedText = a.hasValue(R.styleable.PullToRefreshRecyclerView_prvRefreshCompletedText) ?
                a.getString(R.styleable.PullToRefreshRecyclerView_prvRefreshCompletedText) :
                getResources().getString(R.string.recycler_default_refresh_completed);

        //load more text
        pullUpToLoadMoreText = a.hasValue(R.styleable.PullToRefreshRecyclerView_prvPullUpToLoadMoreText) ?
                a.getString(R.styleable.PullToRefreshRecyclerView_prvPullUpToLoadMoreText) :
                getResources().getString(R.string.recycler_default_pull_up_to_load_more);
        releaseToLoadMoreText = a.hasValue(R.styleable.PullToRefreshRecyclerView_prvReleaseToLoadMoreText) ?
                a.getString(R.styleable.PullToRefreshRecyclerView_prvReleaseToLoadMoreText) :
                getResources().getString(R.string.recycler_default_release_to_load_more);
        loadingMoreText = a.hasValue(R.styleable.PullToRefreshRecyclerView_prvLoadingMoreText) ?
                a.getString(R.styleable.PullToRefreshRecyclerView_prvLoadingMoreText) :
                getResources().getString(R.string.recycler_default_loading_more);
        loadMoreCompletedText = a.hasValue(R.styleable.PullToRefreshRecyclerView_prvLoadMoreCompletedText) ?
                a.getString(R.styleable.PullToRefreshRecyclerView_prvLoadMoreCompletedText) :
                getResources().getString(R.string.recycler_default_load_more_completed);
        a.recycle();

        if (headerLayoutId == -1) {
            headerView = LayoutInflater.from(context).inflate(R.layout.recycler_default_header_view, this, false);
            setHeader(createDefaultHeader());
        } else {
            headerView = LayoutInflater.from(context).inflate(headerLayoutId, this, false);
        }
        if (footerLayoutId == -1) {
            footerView = LayoutInflater.from(context).inflate(R.layout.recycler_default_footer_view, this, false);
            setFooter(createDefaultFooter());
        } else {
            footerView = LayoutInflater.from(context).inflate(footerLayoutId, this, false);
        }
        addView(headerView, 0);
        addView(footerView);


        setHaveMore(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        headerHeight = headerView.getMeasuredHeight();
        footerHeight = footerView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        headerView.layout(0, 0 - headerView.getMeasuredHeight(), getMeasuredWidth(), 0);
        recyclerView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
        footerView.layout(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight() + footerView.getMeasuredHeight());
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

        //release to refresh
        if (isRefreshEnable() && getScrollY() < getRefreshThresholdValue()) {
            if (!isRefreshing()) {
                setState(RELEASE_TO_REFRESH);
            }
            return;
        }
        //pull down to refresh
        if (isRefreshEnable() && getScrollY() < 0) {
            if (!isRefreshing()) {
                setState(PULL_DOWN_TO_REFRESH);
            }
            return;
        }
        //release to load more
        if (isLoadMoreEnable() && getScrollY() > getLoadMoreThresholdValue()) {
            if (!isLoadingMore()) {
                setState(RELEASE_TO_LOAD_MORE);
            }
            return;
        }
        //pull up to load more
        if (isLoadMoreEnable() && getScrollY() > 0) {
            if (!isLoadingMore()) {
                setState(PULL_UP_TO_LOAD_MORE);
            }
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
                            header.updateLastRefreshTime(lastRefreshTimeStamp);
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
        animator.setDuration(Math.abs(scrollYDistance));
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
                header.updateRefreshTips("");
                footer.updateLoadMoreTips("");
                break;

            case PULL_DOWN_TO_REFRESH:
                header.updateRefreshTips(pullDownToRefreshText);
                break;
            case RELEASE_TO_REFRESH:
                header.updateRefreshTips(releaseToRefreshText);
                break;
            case REFRESHING:
                header.updateRefreshTips(refreshingText);
                break;
            case REFRESH_COMPLETED:
                header.updateRefreshTips(refreshCompletedText);
                break;

            case PULL_UP_TO_LOAD_MORE:
                footer.updateLoadMoreTips(pullUpToLoadMoreText);
                break;
            case RELEASE_TO_LOAD_MORE:
                footer.updateLoadMoreTips(releaseToLoadMoreText);
                break;
            case LOADING_MORE:
                footer.updateLoadMoreTips(loadingMoreText);
                break;
            case LOAD_MORE_COMPLETED:
                footer.updateLoadMoreTips(loadMoreCompletedText);
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
        headerView.setVisibility(enable ? VISIBLE : INVISIBLE);
        if (!enable)
            setState(INIT);
    }

    public boolean isLoadMoreEnable() {
        return loadMoreEnable;
    }

    public void setLoadMoreEnable(boolean enable) {
        this.loadMoreEnable = enable;
        footerView.setVisibility(enable ? VISIBLE : INVISIBLE);
        if (!enable)
            setState(INIT);
    }

    public boolean isHaveMore() {
        return haveMore;
    }

    public void setHaveMore(boolean haveMore) {
        this.haveMore = haveMore;
        if (isLoadMoreEnable())
            footerView.setVisibility(haveMore ? VISIBLE : INVISIBLE);
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public <H extends IHeader> void setHeader(@NonNull H header) {
        this.header = header;
        this.header.initChildren(headerView);
    }

    public <F extends IFooter> void setFooter(@NonNull F footer) {
        this.footer = footer;
        this.footer.initChildren(footerView);
    }

    private IHeader createDefaultHeader() {
        return new IHeader() {
            TextView tvLastRefreshTime;
            TextView tvRefreshTips;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);

            @Override
            public void initChildren(@NonNull View headerView) {
                tvLastRefreshTime = headerView.findViewById(R.id.recycler_tv_last_refresh_time);
                tvRefreshTips = headerView.findViewById(R.id.recycler_tv_refresh_tips);
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
                            getResources().getString(R.string.recycler_default_last_refresh_time),
                            dateFormat.format(new Date(lastRefreshTimeStamp))
                    ));
                }
            }

            @Override
            public void updateRefreshTips(CharSequence txt) {
                tvRefreshTips.setText(txt);
            }
        };
    }

    private IFooter createDefaultFooter() {
        return new IFooter() {
            TextView tvLoadMoreTips;

            @Override
            public void initChildren(@NonNull View footerView) {
                tvLoadMoreTips = footerView.findViewById(R.id.recycler_tv_load_more_tips);
            }

            @Override
            public void updateLoadMoreTips(CharSequence txt) {
                tvLoadMoreTips.setText(txt);
            }
        };
    }

    public interface OnRefreshListener {
        void onRefresh(@NonNull Context context, int currentPage, int pageSize);

        void onLoadMore(@NonNull Context context, int currentPage, int pageSize);
    }
}
