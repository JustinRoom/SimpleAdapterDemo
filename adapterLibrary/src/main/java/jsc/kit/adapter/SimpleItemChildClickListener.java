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
public class SimpleItemChildClickListener<H, D, F, E> implements OnItemChildClickListener<H, D, F, E> {

    @Override
    public void onHeaderItemChildClick(@NonNull View headerChild, int position, H headerBean) {

    }

    @Override
    public void onDataItemChildClick(@NonNull View dataItemChild, int position, D dataBean) {

    }

    @Override
    public void onFooterItemChildClick(@NonNull View footerChild, int position, F footerBean) {

    }

    @Override
    public void onEmptyItemChildClick(@NonNull View emptyChild, int position, E emptyBean) {

    }
}
