# Arc
**LatestVersion**

[ ![Download](https://api.bintray.com/packages/justinquote/maven/adapter-component/images/download.svg) ](https://bintray.com/justinquote/maven/adapter-component/_latestVersion)

<a href='https://bintray.com/justinquote/maven/adapter-component?source=watch' alt='Get automatic notifications about new "adapter-component" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_greyscale.png'></a>

一个adapter就够了。强大好用的RecyclerView适配器，一个adapter搞定所有。

Scan QRCode to download demo application below:

![](/app/src/main/res/drawable/simple_adapter_qr_code.png)

### 1、implementation
+ 1.1、Gradle
```
compile 'jsc.kit.adapter:adapter-component:_latestVersion'
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

### 3、usage

+ 1、示例：
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
+ 2、BaseHeaderFooterAdapter

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




### 4、Screenshots

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
