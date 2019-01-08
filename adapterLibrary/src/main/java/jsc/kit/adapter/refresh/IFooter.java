package jsc.kit.adapter.refresh;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2019/1/4 17:05 Friday
 *
 * @author jsc
 */
public interface IFooter {

    void initChildren(@NonNull View footerView);

    /**
     * The call back of pulling up to load more.
     * @param state See {@link PullToRefreshRecyclerView.State}.
     * @param txt the tips of pulling up.
     */
    void onUpdateState(@PullToRefreshRecyclerView.State int state, CharSequence txt);

    /**
     * The call back of pulling up.
     * @param state See {@link PullToRefreshRecyclerView.State}.
     * @param loadMoreEnable load more enable or disable
     * @param isLoadingMore true, loading more, else false.
     * @param scrollY the scroll of y coordination
     * @param footerHeight the height of the load more view.
     */
    void onScroll(@PullToRefreshRecyclerView.State int state, boolean loadMoreEnable, boolean isLoadingMore, int scrollY, int footerHeight, int loadMoreThresholdValue);
}
