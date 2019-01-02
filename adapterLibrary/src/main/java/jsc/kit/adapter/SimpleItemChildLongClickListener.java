package jsc.kit.adapter;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/24 09:38 Monday
 *
 * @author jsc
 */
public class SimpleItemChildLongClickListener<H, D, F, E> implements OnItemChildLongClickListener<H, D, F, E> {

    @Override
    public boolean onHeaderItemChildLongClick(@NonNull View headerChild, int position, H headerBean) {
        return false;
    }

    @Override
    public boolean onDataItemChildLongClick(@NonNull View dataItemChild, int position, D dataBean) {
        return false;
    }

    @Override
    public boolean onFooterItemChildLongClick(@NonNull View footerChild, int position, F footerBean) {
        return false;
    }

    @Override
    public boolean onEmptyItemChildLongClick(@NonNull View emptyChild, int position, E emptyBean) {
        return false;
    }
}
