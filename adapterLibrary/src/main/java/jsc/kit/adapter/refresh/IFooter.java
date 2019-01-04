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
    void updateLoadMoreTips(@PullToRefreshRecyclerView.State int state, CharSequence txt);
}
