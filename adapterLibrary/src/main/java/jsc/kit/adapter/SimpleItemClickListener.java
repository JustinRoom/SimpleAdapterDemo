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
public class SimpleItemClickListener<H, D, F, E> implements OnItemClickListener<H, D, F, E> {

    @Override
    public void onHeaderItemClick(@NonNull View headerItemView, int position, H headerBean) {

    }

    @Override
    public void onDataItemClick(@NonNull View dataItemView, int position, D dataBean) {

    }

    @Override
    public void onFooterItemClick(@NonNull View footerItemView, int position, F footerBean) {

    }

    @Override
    public void onEmptyItemClick(@NonNull View emptyItemView, int position, E emptyBean) {

    }
}
