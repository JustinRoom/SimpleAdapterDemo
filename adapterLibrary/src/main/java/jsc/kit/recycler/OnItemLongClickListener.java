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
public interface OnItemLongClickListener<H, D, F, E> {
    boolean onHeaderItemLongClick(@NonNull View headerItemView, int position, H headerBean);
    boolean onDataItemLongClick(@NonNull View dataItemView, int position, D dataBean);
    boolean onFooterItemLongClick(@NonNull View footerItemView, int position, F footerBean);
    boolean onEmptyItemLongClick(@NonNull View emptyItemView, int position, E emptyBean);
}
