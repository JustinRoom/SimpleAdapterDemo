package jsc.exam.com.adapter.fragments;

import android.content.Context;
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
import jsc.kit.adapter.SimpleAdapter;
import jsc.kit.adapter.SimpleItemChildClickListener;
import jsc.kit.adapter.SimpleItemClickListener;
import jsc.kit.adapter.SpaceItemDecoration;

/**
 * 快速实现单选示例
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2019/1/3 10:31 Thursday
 *
 * @author jsc
 */
public class SimpleAdapterFragment extends BaseFragment implements View.OnClickListener {

    SimpleAdapter<Object, CustomBean, Object, Object> adapter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_simple_adapter, container, false);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        root.findViewById(R.id.btn_add_header).setOnClickListener(this);
        root.findViewById(R.id.btn_remove_header).setOnClickListener(this);
        root.findViewById(R.id.btn_add_data_before).setOnClickListener(this);
        root.findViewById(R.id.btn_add_data).setOnClickListener(this);
        root.findViewById(R.id.btn_remove_data).setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                0,
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_2)
        ));
        adapter = new SimpleAdapter<Object, CustomBean, Object, Object>(R.layout.checkable_list_item_layout) {
            @Override
            protected void onBindHeaderViewHolder(@NonNull BaseViewHolder holder, int position, Object headerBean) {
                holder.setImageResource(R.id.iv_header, R.drawable.picture);
                addOnChildClickListener(holder, R.id.btn_header);
            }

            @Override
            protected void onBindDataViewHolder(@NonNull BaseViewHolder holder, int position, CustomBean dataBean) {
                holder.setText(R.id.tv_label, dataBean.getLabel());
            }
        };
        adapter.setOnItemClickListener(new SimpleItemClickListener<Object, CustomBean, Object, Object>() {
            @Override
            public void onDataItemClick(@NonNull View dataItemView, int position, CustomBean dataBean) {
                Toast.makeText(dataItemView.getContext(), "clicked:" + dataBean.getLabel(), Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnItemChildClickListener(new SimpleItemChildClickListener<Object, CustomBean, Object, Object>() {
            @Override
            public void onHeaderItemChildClick(@NonNull View headerChild, int position, Object headerBean) {
                Toast.makeText(headerChild.getContext(), "clicked header:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setHeaderLayoutId(R.layout.list_header_layout);
        adapter.setFooterLayoutId(R.layout.list_footer_layout);
        adapter.bindRecyclerView(recyclerView);
        adapter.addEmpty(new Object());
        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_header:
                adapter.addHeader(new Object());
                break;
            case R.id.btn_remove_header:
                adapter.removeHeader(adapter.getHeaderSize() - 1);
                break;
            case R.id.btn_add_data_before:
                indexBefore++;
                CustomBean item1 = new CustomBean();
                item1.setLabel("data before " + indexBefore);
                adapter.addData(indexBefore, item1);
                break;
            case R.id.btn_add_data:
                index++;
                CustomBean item2 = new CustomBean();
                item2.setLabel("this is " + index);
                adapter.addData(item2);
                break;
            case R.id.btn_remove_data:
                adapter.removeData(adapter.getDataSize() - 1);
                if (index >= 0)
                    index--;
                else
                    indexBefore--;
                break;
        }
    }

    @Override
    void onLoadData(Context context) {
        loadNetData();
    }

    int index = -1;
    int indexBefore = -1;

    private void loadNetData() {
        List<CustomBean> items = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            index++;
            CustomBean item = new CustomBean();
            item.setLabel("this is " + index);
            items.add(item);
        }
        adapter.setData(items);
    }
}
