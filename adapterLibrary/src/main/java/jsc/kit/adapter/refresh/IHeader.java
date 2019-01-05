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
public interface IHeader {

    void initChildren(@NonNull View headerView);
    void updateLastRefreshTime(long lastRefreshTimeStamp);
    void onUpdateState(@PullToRefreshRecyclerView.State int state, CharSequence txt);
    void onScroll(@PullToRefreshRecyclerView.State int state, boolean refreshEnable, boolean isRefreshing, int scrollY, int headerHeight, int refreshThresholdValue);
}
