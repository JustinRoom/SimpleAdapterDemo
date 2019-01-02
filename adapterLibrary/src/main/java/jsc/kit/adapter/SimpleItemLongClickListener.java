package jsc.kit.adapter;


import android.support.annotation.NonNull;
import android.view.View;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/24 09:36 Monday
 *
 * @author jsc
 */
public class SimpleItemLongClickListener<H, D, F, E> implements OnItemLongClickListener<H, D, F, E> {

    @Override
    public boolean onHeaderItemLongClick(@NonNull View headerItemView, int position, H headerBean) {
        return false;
    }

    @Override
    public boolean onDataItemLongClick(@NonNull View dataItemView, int position, D dataBean) {
        return false;
    }

    @Override
    public boolean onFooterItemLongClick(@NonNull View footerItemView, int position, F footerBean) {
        return false;
    }

    @Override
    public boolean onEmptyItemLongClick(@NonNull View emptyItemView, int position, E emptyBean) {
        return false;
    }
}
