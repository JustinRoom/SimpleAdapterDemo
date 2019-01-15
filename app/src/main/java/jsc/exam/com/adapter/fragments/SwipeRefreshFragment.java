package jsc.exam.com.adapter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jsc.exam.com.adapter.R;
import jsc.exam.com.adapter.bean.ClassItem;
import jsc.exam.com.adapter.utils.CompatResourceUtils;
import jsc.kit.adapter.SimpleAdapter3;
import jsc.kit.adapter.SimpleItemClickListener3;
import jsc.kit.adapter.SimpleItemLongClickListener3;
import jsc.kit.adapter.SpaceItemDecoration;
import jsc.kit.adapter.refresh.SwipeRefreshRecyclerView;


/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/22 23:24 Saturday
 *
 * @author jsc
 */
public class SwipeRefreshFragment extends BaseFragment {

    SwipeRefreshRecyclerView swipeRefreshRecyclerView;
    SimpleAdapter3<ClassItem> adapter3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_swipe_refresh, container, false);
        swipeRefreshRecyclerView = root.findViewById(R.id.swipe_refresh_view);
        swipeRefreshRecyclerView.setPageSize(12);
        swipeRefreshRecyclerView.getRecyclerView().setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        swipeRefreshRecyclerView.getRecyclerView().addItemDecoration(new SpaceItemDecoration(
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_16),
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_2)
        ));
        swipeRefreshRecyclerView.setOnRefreshListener(new SwipeRefreshRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh(int currentPageNumber, int pageSize) {
                itemIndex = -1;
                loadData();
            }

            @Override
            public void onLoadMore(int currentPageNumber, int pageSize) {
                loadData();
            }
        });
        adapter3 = new SimpleAdapter3<ClassItem>() {
            @Override
            protected void onBindDataViewHolder(@NonNull BaseViewHolder holder, int position, ClassItem dataBean) {
                holder.setText(R.id.tv_label, dataBean.getLabel());
                //给child添加点击事件
//                addOnChildClickListener(holder, R.id.tv_label);
                //给child添加长按事件
//                addOnChildLongClickListener(holder, R.id.tv_label);
            }
        };
        adapter3.setOnItemClickListener(new SimpleItemClickListener3<ClassItem>() {
            @Override
            public void onDataItemClick(@NonNull View dataItemView, int position, ClassItem dataBean) {
                Toast.makeText(dataItemView.getContext(), "clicked: " + dataBean.getLabel(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEmptyItemClick(@NonNull View emptyItemView, int position, Object emptyBean) {
                swipeRefreshRecyclerView.refresh();
            }
        });
        adapter3.setOnItemLongClickListener(new SimpleItemLongClickListener3<ClassItem>() {
            @Override
            public boolean onDataItemLongClick(@NonNull View dataItemView, int position, ClassItem dataBean) {
                Toast.makeText(dataItemView.getContext(), "long clicked:" + dataBean.getLabel(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        adapter3.setDataLayoutId(R.layout.main_list_item_layout);
        adapter3.bindRecyclerView(swipeRefreshRecyclerView.getRecyclerView());

        adapter3.addEmpty(new Object());
        return root;
    }

    @Override
    void onLoadData(Context context) {
//        swipeRefreshRecyclerView.refresh();
    }

    private void loadData() {
        swipeRefreshRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshRecyclerView.loadComplete();
                List<ClassItem> items = loadRandomData();
                if (swipeRefreshRecyclerView.isFirstPage())
                    adapter3.setData(items);
                else
                    adapter3.addData(items);
                swipeRefreshRecyclerView.setHasMorePage(items.size() >= 12);
            }
        }, new Random().nextInt(3000));
    }

    private int itemIndex = -1;

    private List<ClassItem> loadRandomData() {
        List<ClassItem> items = new ArrayList<>();
        int count = 8 + new Random().nextInt(16);
        for (int i = 0; i < count; i++) {
            itemIndex++;
            ClassItem item = new ClassItem();
            item.setLabel("list item " + itemIndex);
            items.add(item);
        }
        return items;
    }
}
