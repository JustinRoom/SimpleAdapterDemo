package jsc.exam.com.adapter.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jsc.exam.com.adapter.R;
import jsc.exam.com.adapter.bean.CustomBean;
import jsc.exam.com.adapter.utils.CompatResourceUtils;
import jsc.kit.adapter.BaseItemAdapter;
import jsc.kit.adapter.SimpleAdapter3;
import jsc.kit.adapter.SimpleItemClickListener3;
import jsc.kit.adapter.decoration.SpaceItemDecoration;

/**
 * 快速实现复选示例
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2019/1/3 10:31 Thursday
 *
 * @author jsc
 */
public class BaseItemAdapterFragment extends BaseFragment {

    BaseItemAdapter<CustomBean> adapter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_16),
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_2)
        ));
        adapter = new BaseItemAdapter<CustomBean>(R.layout.checkable_list_item_layout) {
            @Override
            protected void onBindItemViewHolder(@NonNull BaseItemViewHolder<CustomBean> holder, int position, CustomBean item) {
                holder.setText(R.id.tv_label, item.getLabel())
                        .setTextColor(R.id.tv_label, item.isSelected() ? Color.GREEN : 0xFF333333)
                        .setSelected(R.id.iv_selected_state, item.isSelected());
            }
        };
        adapter.setOnCreateViewHolderListener(new BaseItemAdapter.OnCreateViewHolderListener<CustomBean>() {
            @Override
            public View onCreateItemView(@NonNull ViewGroup viewGroup) {
                return null;
            }

            @Override
            public void afterCreateViewHolder(@NonNull BaseItemAdapter.BaseItemViewHolder<CustomBean> holder) {
                holder.addClickListener(R.id.iv_selected_state);
            }
        });
        adapter.setOnItemClickListener(new BaseItemAdapter.OnItemClickListener<CustomBean>() {
            @Override
            public void onItemClick(@NonNull View itemView, int position, CustomBean item) {
                Toast.makeText(itemView.getContext(), "clicked item: " + item.getLabel(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnItemChildClickListener(new BaseItemAdapter.OnItemChildClickListener<CustomBean>() {
            @Override
            public void onItemChildClick(@NonNull View child, int position, CustomBean item) {
                item.setSelected(!item.isSelected());
                adapter.notifyItemChanged(position);
            }
        });
        adapter.bindRecyclerView(recyclerView);
        return root;
    }

    @Override
    void onLoadData(Context context) {
        loadNetData();
    }

    private void loadNetData() {
        List<CustomBean> items = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            CustomBean item = new CustomBean();
            item.setLabel("this is " + i);
            items.add(item);
        }
        adapter.setItems(items);
    }
}
