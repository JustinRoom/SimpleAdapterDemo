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
public interface OnItemChildLongClickListener<H, D, F, E> {
    boolean onHeaderItemChildLongClick(@NonNull View headerChild, int position, H headerBean);
    boolean onDataItemChildLongClick(@NonNull View dataItemChild, int position, D dataBean);
    boolean onFooterItemChildLongClick(@NonNull View footerChild, int position, F footerBean);
    boolean onEmptyItemChildLongClick(@NonNull View emptyChild, int position, E emptyBean);
}
