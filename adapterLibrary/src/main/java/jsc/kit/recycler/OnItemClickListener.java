package jsc.kit.recycler;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/23 01:38 Sunday
 *
 * @author jsc
 */
public interface OnItemClickListener<H, D, F, E> {
    void onHeaderItemClick(@NonNull View headerItemView, int position, H headerBean);
    void onDataItemClick(@NonNull View dataItemView, int position, D dataBean);
    void onFooterItemClick(@NonNull View footerItemView, int position, F footerBean);
    void onEmptyItemClick(@NonNull View emptyItemView, int position, E emptyBean);
}
