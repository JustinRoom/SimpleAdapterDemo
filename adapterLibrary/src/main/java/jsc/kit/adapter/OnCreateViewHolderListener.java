package jsc.kit.adapter;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2019/3/2 17:44 Saturday
 *
 * @author jsc
 */
public interface OnCreateViewHolderListener {
    void onCreateHeaderViewHolder(@NonNull BaseHeaderFooterAdapter.BaseViewHolder holder);
    void onCreateDataViewHolder(@NonNull BaseHeaderFooterAdapter.BaseViewHolder holder);
    void onCreateFooterViewHolder(@NonNull BaseHeaderFooterAdapter.BaseViewHolder holder);
    void onCreateEmptyViewHolder(@NonNull BaseHeaderFooterAdapter.BaseViewHolder holder);
}
