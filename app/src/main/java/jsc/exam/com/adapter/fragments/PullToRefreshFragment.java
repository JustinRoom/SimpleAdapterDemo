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
import java.util.Random;

import jsc.exam.com.adapter.R;
import jsc.exam.com.adapter.bean.ClassItem;
import jsc.exam.com.adapter.utils.CompatResourceUtils;
import jsc.kit.adapter.refresh.PullToRefreshRecyclerView;
import jsc.kit.adapter.SimpleAdapter3;
import jsc.kit.adapter.SimpleItemClickListener3;
import jsc.kit.adapter.SpaceItemDecoration;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2019/1/3 10:31 Thursday
 *
 * @author jsc
 */
public class PullToRefreshFragment extends BaseFragment {

    PullToRefreshRecyclerView pullToRefreshRecyclerView;
    SimpleAdapter3<ClassItem> adapter3 = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pull_to_refresh, container, false);
        pullToRefreshRecyclerView = root.findViewById(R.id.pull_to_refresh_view);
        pullToRefreshRecyclerView.initializeParameters(1, 10);
//        pullToRefreshRecyclerView.setRefreshEnable(false);
//        pullToRefreshRecyclerView.setLoadMoreEnable(false);
        pullToRefreshRecyclerView.setOnRefreshListener(new PullToRefreshRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull Context context, int currentPage, int pageSize) {
                index = -1;
                loadNetData();
            }

            @Override
            public void onLoadMore(@NonNull Context context, int currentPage, int pageSize) {
                loadNetData();
            }
        });
        RecyclerView recyclerView = pullToRefreshRecyclerView.getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_16),
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_2)
        ));
        adapter3 = new SimpleAdapter3<ClassItem>(R.layout.main_list_item_layout) {
            @Override
            protected void onBindDataViewHolder(@NonNull BaseViewHolder holder, int position, ClassItem dataBean) {
                holder.setText(R.id.tv_label, dataBean.getLabel());
                holder.setVisibility(R.id.red_dot_view, View.GONE);
            }
        };
        adapter3.setOnItemClickListener(new SimpleItemClickListener3<ClassItem>() {
            @Override
            public void onDataItemClick(@NonNull View dataItemView, int position, ClassItem dataBean) {
                Toast.makeText(dataItemView.getContext(), "clicked:" + dataBean.getLabel(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEmptyItemClick(@NonNull View emptyItemView, int position, Object emptyBean) {
                pullToRefreshRecyclerView.refresh();
            }
        });
        adapter3.bindRecyclerView(recyclerView);
        adapter3.addEmpty(new Object());
        return root;
    }

    @Override
    void onLoadData(Context context) {
//        pullToRefreshRecyclerView.refreshDelay(300);
//        loadNetData();
    }

    private int index = -1;
    private Random random = new Random();
    private void loadNetData(){
        pullToRefreshRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshRecyclerView.completed();
                List<ClassItem> items = new ArrayList<>();
                int count = 7 + random.nextInt(12);
                for (int i = 0; i < count; i++) {
                    index ++;
                    ClassItem item = new ClassItem();
                    item.setLabel("this is " + index);
                    items.add(item);
                }

                if (pullToRefreshRecyclerView.isFirstPage()) {
                    adapter3.setData(items);
                } else {
                    adapter3.addData(items);
                }
                pullToRefreshRecyclerView.setHaveMore(items.size() >= pullToRefreshRecyclerView.getPageSize());
            }
        }, 50 + random.nextInt(2000));
    }
}
