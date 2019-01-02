package jsc.kit.recycler;

import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/22 23:34 Saturday
 *
 * @author jsc
 */
public class SimpleAdapter<H, I, F, E> extends BaseHeaderFooterAdapter<H, I, F, E, BaseHeaderFooterAdapter.BaseViewHolder> {

    public SimpleAdapter() {
    }

    public SimpleAdapter(@LayoutRes int itemLayoutId) {
        super(itemLayoutId);
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_HEADER:
                if (getHeaderLayoutId() == -1)
                    throw new IllegalArgumentException(getClass().getSimpleName() + " : please set header layout first.");
                return new BaseViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getHeaderLayoutId(), viewGroup, false));
            case TYPE_DATA:
                if (getDataLayoutId() == -1)
                    throw new IllegalArgumentException(getClass().getSimpleName() + " : please set footer layout first.");
                return new BaseViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getDataLayoutId(), viewGroup, false));
            case TYPE_FOOTER:
                if (getFooterLayoutId() == -1)
                    throw new IllegalArgumentException(getClass().getSimpleName() + " : please set footer layout first.");
                return new BaseViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getFooterLayoutId(), viewGroup, false));
            case TYPE_EMPTY:
                return new BaseViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(getEmptyLayoutId() == -1 ? R.layout.recycler_default_empty_list_layout : getEmptyLayoutId(), viewGroup, false));
            default:
                TextView textView = new TextView(viewGroup.getContext());
                textView.setTextColor(Color.RED);
                textView.setGravity(Gravity.CENTER);
                textView.setText("Unknown view type");
                textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return new BaseViewHolder(textView);
        }
    }
}
