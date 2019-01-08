# SimpleAdapter
**LatestVersion**

[ ![Download](https://api.bintray.com/packages/justinquote/maven/adapter-component/images/download.svg) ](https://bintray.com/justinquote/maven/adapter-component/_latestVersion)

<a href='https://bintray.com/justinquote/maven/adapter-component?source=watch' alt='Get automatic notifications about new "adapter-component" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_greyscale.png'></a>

一个adapter就够了。强大好用的RecyclerView适配器，一个adapter搞定所有。

Scan QRCode to download demo application below:

![](/app/src/main/res/drawable/simple_adapter_qr_code.png)

### 1、implementation
You may implementation recycler view support package first.
```
implementation 'com.android.support:recyclerview-v7:xxx'
implementation 'com.android.support:appcompat-v7:xxx'
```
+ 1.1、Gradle
```
implementation 'jsc.kit.adapter:adapter-component:_latestVersion'
```
+ 1.2、Maven
```
<dependency>
  <groupId>jsc.kit.adapter</groupId>
  <artifactId>adapter-component</artifactId>
  <version>_latestVersion</version>
  <type>pom</type>
</dependency>
```

### 2、attrs
+ 2.1、[SwipeRefreshRecyclerView](/adapterLibrary/src/main/java/jsc/kit/adapter/SwipeRefreshRecyclerView.java)

| 名称 | 类型 | 描述 |
|:---|:---|:---|
|`srrvLoadMoreLayout`|reference|加载更多layout|

+ 2.2、[PullToRefreshRecyclerView](/adapterLibrary/src/main/java/jsc/kit/adapter/refresh/PullToRefreshRecyclerView.java)【**推荐使用**】
    
| 名称 | 类型 | 描述 |
|:---|:---|:---|
|`srrvLoadMoreLayout`|reference|加载更多layout|
|`prvHeaderLayout`|reference|下拉刷新头部layout|
|`prvFooterLayout`|reference|上拉加载更多底部layout|
|`prvPullDownToRefreshText`|string|下拉刷新提示|
|`prvReleaseToRefreshText`|string|释放刷新提示|
|`prvRefreshingText`|string|正在刷新提示|
|`prvRefreshCompletedText`|string|刷新完成提示|
|`prvPullUpToLoadMoreText`|string|上拉加载更多提示|
|`prvReleaseToLoadMoreText`|string|释放加载更多提示|
|`prvLoadingMoreText`|string|正在加载更多提示|
|`prvLoadMoreCompletedText`|string|加载更多完成提示|

### 3、usage

##### 3.1、SimpleAdapter
+ 3.1.1、示例：
```
        RecyclerView recyclerView;

        adapter2 = new SimpleAdapter2<MaterielOrderDetail, MaterielDetail, Object>() {
            @Override
            protected void onBindHeaderViewHolder(@NonNull BaseViewHolder holder, int position, MaterielOrderDetail headerBean) {
                holder.setText(R.id.tv_company_name, formatValue("公司名称：", headerBean.getOrgName()));
                holder.setText(R.id.tv_type, formatValue("\u3000类型：", headerBean.getEventTypeName()));
                holder.setText(R.id.tv_warehouse_name, formatValue("仓库名称：", headerBean.getWarehouseName()));
                holder.setText(R.id.tv_partner_name, formatValue("供应商：", headerBean.getPartnerName()));
                if (headerBean.getCreateTime() > 0) {
                    holder.setText(R.id.tv_create_time, formatValue("创建时间：", format.format(new Date(headerBean.getCreateTime()))));
                } else {
                    holder.setText(R.id.tv_create_time, "");
                }
                holder.setText(R.id.tv_creator, formatValue("创建人：", headerBean.getCreatePersonName()));
                //已经审核通过
                if (MaterielMenu.orderStatus.get(2).getKey().equals(headerBean.getStatus())) {
                    holder.setText(R.id.tv_auditing_time, formatValue("审核时间：", format.format(new Date(headerBean.getApproveTime()))));
                    holder.setText(R.id.tv_auditor, formatValue("审核人：", headerBean.getApprovePersonName()));
                } else {
                    holder.setText(R.id.tv_auditing_time, formatValue("审核时间：", "--"));
                    holder.setText(R.id.tv_auditor, formatValue("审核人：", "--"));
                }

                holder.setText(R.id.tv_storage_type_label, String.format(Locale.CHINA, "%s物料：", opAction1 == IMateriel.OP_ACTION_IN ? "入库" : "出库"));
            }

            @Override
            protected void onBindDataViewHolder(@NonNull BaseViewHolder holder, int position, MaterielDetail dataBean) {
                holder.setText(R.id.tv_materiel_name, dataBean.getMaterialName());
                if (dataBean.getBatchManage()) {
                    holder.setText(R.id.tv_materiel_batch, String.format(Locale.CHINA, "（%s）", dataBean.getBatch()));
                } else {
                    holder.setText(R.id.tv_materiel_batch, "");
                }
                holder.setVisibility(R.id.btn_materiel_delete, View.GONE);
                holder.setText(R.id.tv_business_unit, formatValue("业务单位：", dataBean.getMultiUnitName()));
                holder.setText(R.id.tv_business_number, formatValue("业务数量：", String.valueOf(dataBean.getQuantity())));
                holder.setText(R.id.tv_accounting_unit, formatValue("核算单位：", dataBean.getAccountUnitName()));
                holder.setText(R.id.tv_accounting_number, formatValue("核算数量：", String.valueOf(dataBean.getAccountQuantity())));
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd\u2000HH:mm", Locale.CHINA);

            CharSequence formatValue(String label, String value) {
                if (value == null || value.trim().length() == 0) {
                    return label;
                }
                SpannableString spannableString = new SpannableString(label + value);
                spannableString.setSpan(new ForegroundColorSpan(0xFF90FFBE), label.length(), label.length() + value.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            }
        };
        adapter2.setHeaderLayoutId(R.layout.breed_fragment_materiel_detail_header);
        adapter2.setDataLayoutId(R.layout.breed_list_item_materiel);
        adapter2.bindRecyclerView(recyclerView);
        adapter2.addHeader(new MaterielOrderDetail());
```
+ 3.1.2、[BaseHeaderFooterAdapter](/adapterLibrary/src/main/java/jsc/kit/adapter/BaseHeaderFooterAdapter.java)

> 头部header  
设置header视图布局文件`public void setHeaderLayoutId(@LayoutRes int layoutId)`。  
绑定数据到header视图`protected void onBindHeaderViewHolder(@NonNull BaseViewHolder holder, int position, H headerBean) {}`。

> 数据data  
设置data视图布局文件`public void setDataLayoutId(@LayoutRes int layoutId)`。  
绑定数据到data视图`protected void onBindDataViewHolder(@NonNull BaseViewHolder holder, int position, D dataBean) {}`。

> 底部footer  
设置footer视图布局文件`public void setFooterLayoutId(@LayoutRes int layoutId)`。  
绑定数据到footer视图`protected void onBindFooterViewHolder(@NonNull VH holder, int position, F footerBean) {}`。

> 空列表empty
设置empty视图布局文件`public void setEmptyLayoutId(@LayoutRes int layoutId)`。  
绑定数据到empty视图`protected void onBindEmptyViewHolder(@NonNull VH holder, int position, E emptyBean) {}`。

> 给child view添加点击事件:`addOnChildClickListener(@NonNull VH holder, @IdRes int id)`。  
给child view添加长按事件:`addOnChildLongClickListener(@NonNull VH holder, @IdRes int id)`。

[BaseViewHolder](/adapterLibrary/src/main/java/jsc/kit/adapter/BaseHeaderFooterAdapter.java)通用方法：  
`<V extends View> V findViewById(@IdRes int id)`  
`void setText(@IdRes int id, CharSequence txt)`  
`void setTextColor(@IdRes int id, @ColorInt int color)`  
`void setTextSize(@IdRes int id, float size)`  
`void setTextSize(@IdRes int id, int unit, float size)`  
`void setVisibility(@IdRes int id, int visibility)`  
`void setSelected(@IdRes int id, boolean selected)`  
`void setEnabled(@IdRes int id, boolean enable)`  
`void setBackgroundColor(@IdRes int id, @ColorInt int color)`  
`void setBackgroundResource(@IdRes int id, @DrawableRes int resId)`  
`void setBackground(@IdRes int id, Drawable background)`  
`void setTag(@IdRes int id, final Object tag)`  
`void setTag(@IdRes int id, int key, final Object tag)`  

+ 3.1.3、各种事件监听
`itemView`的点击事件监听：
```
public interface OnItemClickListener<H, D, F, E> {
    //每一条header的点击监听
    void onHeaderItemClick(@NonNull View headerItemView, int position, H headerBean);
    //每一条data的点击监听
    void onDataItemClick(@NonNull View dataItemView, int position, D dataBean);
    //每一条footer的点击监听
    void onFooterItemClick(@NonNull View footerItemView, int position, F footerBean);
    //每一条empty的点击监听
    void onEmptyItemClick(@NonNull View emptyItemView, int position, E emptyBean);
}
```

`itemView`之`child view`的点击事件监听
```
public interface OnItemChildClickListener<H, D, F, E> {
    //header child view点击监听
    void onHeaderItemChildClick(@NonNull View headerChild, int position, H headerBean);
    //data child view点击监听
    void onDataItemChildClick(@NonNull View dataItemChild, int position, D dataBean);
    //footer child view点击监听
    void onFooterItemChildClick(@NonNull View footerChild, int position, F footerBean);
    //empty child view点击监听
    void onEmptyItemChildClick(@NonNull View emptyChild, int position, E emptyBean);
}
```

`itemView`的长按事件监听：
```
public interface OnItemLongClickListener<H, D, F, E> {
    //每一条header的长按监听
    boolean onHeaderItemLongClick(@NonNull View headerItemView, int position, H headerBean);
    //每一条data的长按监听
    boolean onDataItemLongClick(@NonNull View dataItemView, int position, D dataBean);
    //每一条footer的长按监听
    boolean onFooterItemLongClick(@NonNull View footerItemView, int position, F footerBean);
    //每一条empty的长按监听
    boolean onEmptyItemLongClick(@NonNull View emptyItemView, int position, E emptyBean);
}
```

`itemView`之`child view`的长按事件监听
```
public interface OnItemChildLongClickListener<H, D, F, E> {
    //header child view长按监听
    boolean onHeaderItemChildLongClick(@NonNull View headerChild, int position, H headerBean);
    //data child view长按监听
    boolean onDataItemChildLongClick(@NonNull View dataItemChild, int position, D dataBean);
    //footer child view长按监听
    boolean onFooterItemChildLongClick(@NonNull View footerChild, int position, F footerBean);
    //empty child view长按监听
    boolean onEmptyItemChildLongClick(@NonNull View emptyChild, int position, E emptyBean);
}

```

##### 3.2、PullToRefreshRecyclerView
**以手势监听方式实现，非Adapter外部包裹方式。**  
**此方式实现优点**：不影响`RecyclerView.Adapter`各种原生操作

+ 3.2.1、示例：
```
        PullToRefreshRecyclerView pullToRefreshRecyclerView;
    
        //设置分页加载的起始页序号以及每页数据数量
        pullToRefreshRecyclerView.initializeParameters(1, 10);
        //关闭下拉刷新
//        pullToRefreshRecyclerView.setRefreshEnable(false);
        //关闭加载更多
//        pullToRefreshRecyclerView.setLoadMoreEnable(false);
        //设置下拉刷新和上拉加载更多监听
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
        
        
//模拟加载网络数据
    private int index = -1;
    private Random random = new Random();
    private void loadNetData(){
        pullToRefreshRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //刷新（或加载更多）完成
                pullToRefreshRecyclerView.completed();
                List<ClassItem> items = new ArrayList<>();
                int count = 7 + random.nextInt(12);
                for (int i = 0; i < count; i++) {
                    index ++;
                    ClassItem item = new ClassItem();
                    item.setLabel("this is " + index);
                    items.add(item);
                }

                //判定是否是第一页数据
                if (pullToRefreshRecyclerView.isFirstPage()) {
                    adapter3.setData(items);
                } else {
                    adapter3.addData(items);
                }
                //设置是否还有下一页数据
                pullToRefreshRecyclerView.setHaveMore(items.size() >= pullToRefreshRecyclerView.getPageSize());
            }
        }, 50 + random.nextInt(2000));
    }
```

+ 3.2.2、自定义下拉刷新：
> 设置刷新（头部）
```
<jsc.kit.adapter.refresh.PullToRefreshRecyclerView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    app:prvHeaderLayout="@layout/xxx"
    android:id="@+id/pull_to_refresh_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

> 设置刷新逻辑监听
```
public <H extends IHeader> void setHeader(@NonNull H header)
```

> 实现刷新逻辑
```
        IHeader header =  new IHeader() {

            @Override
            public void initChildren(@NonNull View headerView) {
                //这里初始化下拉刷新view
                //也就是app:prvHeaderLayout="@layout/xxx"属性对应的布局文件
            }

            @Override
            public void updateLastRefreshTime(long lastRefreshTimeStamp) {
                //这里是上次刷新时间更新监听
            }

            @Override
            public void onUpdateState(int state, CharSequence txt) {
                //这里是监听下拉刷新的各种状态
                //监听到的状态有：PULL_DOWN_TO_REFRESH、RELEASE_TO_REFRESH、REFRESHING、REFRESH_COMPLETED
                switch (state) {
                    case PullToRefreshRecyclerView.REFRESHING:
                        //正在刷新，我们可以正在这里启动正在刷新的动画
                        
                        break;
                    case PullToRefreshRecyclerView.REFRESH_COMPLETED:
                        //刷新完成，我们可以在这里关闭正在刷新的动画以及头部复位
                        
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(int state, boolean refreshEnable, boolean isRefreshing, int scrollY, int headerHeight, int refreshThresholdValue) {
                //这里是监听下拉刷新动作
                //监听到的状态有：INIT、PULL_DOWN_TO_REFRESH、RELEASE_TO_REFRESH、REFRESHING、REFRESH_COMPLETED
            }
        };
```

+ 3.2.3、自定义上拉加载更多：
> 设置加载更多（底部）
```
<jsc.kit.adapter.refresh.PullToRefreshRecyclerView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    app:prvFooterLayout="@layout/xxx"
    android:id="@+id/pull_to_refresh_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

> 设置加载更多逻辑监听
```
public <H extends IHeader> void setHeader(@NonNull H header)
```

> 实现加载更多逻辑
```
        IFooter footer = new IFooter() {

            @Override
            public void initChildren(@NonNull View footerView) {
                //这里初始化上拉加载更多view
                //也就是app:prvFooterLayout="@layout/xxx"属性对应的布局文件
            }

            @Override
            public void onUpdateState(@State int state, CharSequence txt) {
                //这里是监听上拉加载更多的各种状态
                //监听到的状态有：PULL_UP_TO_LOAD_MORE、RELEASE_TO_LOAD_MORE、LOADING_MORE、LOAD_MORE_COMPLETED
                switch (state) {
                    case PullToRefreshRecyclerView.LOADING_MORE:
                        //正在加载更多，我们可以正在这里启动正在加载更多的动画
                        
                        break;
                    case PullToRefreshRecyclerView.LOAD_MORE_COMPLETED:
                        //加载更多完成，我们可以在这里关闭正在加载更多的动画以及底部复位
                        
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(int state, boolean loadMoreEnable, boolean isLoadingMore, int scrollY, int footerHeight) {
                //这里是监听上拉加载更多动作
                //监听到的状态有：INIT、PULL_UP_TO_LOAD_MORE、RELEASE_TO_LOAD_MORE、LOADING_MORE、LOAD_MORE_COMPLETED
            }
        };
```


### 4、Screenshots

![SimpleAdapter and PullToRefreshRecyclerView](/output/shots/pull_to_refresh_recycler_view.png)

### 5、release log

### LICENSE
```
   Copyright 2019 JustinRoom

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
