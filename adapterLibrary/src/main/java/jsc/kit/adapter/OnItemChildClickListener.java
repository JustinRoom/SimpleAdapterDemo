package jsc.kit.adapter;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/23 01:38 Sunday
 *
 * @author jsc
 */
public interface OnItemChildClickListener<H, D, F, E> {
    void onHeaderItemChildClick(@NonNull View headerChild, int position, H headerBean);
    void onDataItemChildClick(@NonNull View dataItemChild, int position, D dataBean);
    void onFooterItemChildClick(@NonNull View footerChild, int position, F footerBean);
    void onEmptyItemChildClick(@NonNull View emptyChild, int position, E emptyBean);
}
