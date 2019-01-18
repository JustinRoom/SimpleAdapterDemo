package jsc.exam.com.adapter;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import jsc.exam.com.adapter.bean.ClassItem;
import jsc.exam.com.adapter.fragments.AboutFragment;
import jsc.exam.com.adapter.fragments.CheckableFragment;
import jsc.exam.com.adapter.fragments.OptionalFragment;
import jsc.exam.com.adapter.fragments.PullToRefreshFragment;
import jsc.exam.com.adapter.fragments.SwipeRefreshFragment;
import jsc.exam.com.adapter.retrofit.ApiService;
import jsc.exam.com.adapter.retrofit.CustomHttpClient;
import jsc.exam.com.adapter.retrofit.CustomRetrofit;
import jsc.exam.com.adapter.utils.CompatResourceUtils;
import jsc.kit.adapter.SimpleAdapter3;
import jsc.kit.adapter.SimpleItemClickListener3;
import jsc.kit.adapter.SpaceItemDecoration;
import jsc.kit.adapter.refresh.PullToRefreshRecyclerView;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class MainActivity extends BaseActivity {

    SharedPreferences sharedPreferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PullToRefreshRecyclerView pullToRefreshRecyclerView = new PullToRefreshRecyclerView(this);
        pullToRefreshRecyclerView.setRefreshEnable(false);
        pullToRefreshRecyclerView.setLoadMoreEnable(false);
        pullToRefreshRecyclerView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(pullToRefreshRecyclerView);

        RecyclerView recyclerView = pullToRefreshRecyclerView.getRecyclerView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpaceItemDecoration(
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_16),
                CompatResourceUtils.getDimensionPixelSize(this, R.dimen.space_2)
        ));
        setTitleBarTitle(getClass().getSimpleName().replace("Activity", ""));
        showTitleBarBackView(false);

        SimpleAdapter3<ClassItem> adapter3 = new SimpleAdapter3<ClassItem>() {
            @Override
            protected void onBindDataViewHolder(@NonNull BaseViewHolder holder, int position, ClassItem dataBean) {
                holder.setText(R.id.tv_label, dataBean.getLabel());
            }
        };
        adapter3.setOnItemClickListener(new SimpleItemClickListener3<ClassItem>() {
            @Override
            public void onDataItemClick(@NonNull View dataItemView, int position, ClassItem dataBean) {
                toNewActivity(dataBean);
            }
        });
        adapter3.setDataLayoutId(R.layout.main_list_item_layout);
        adapter3.bindRecyclerView(recyclerView);
        adapter3.setData(getClassItems());

        //check upgrade if the latest checking time is 2 hours ago
        sharedPreferences = getSharedPreferences("share_arc", MODE_PRIVATE);
        long lastCheckUpdateTimeStamp = sharedPreferences.getLong("lastCheckUpdateTimeStamp", 0);
        long curTime = new Date().getTime();
        if (curTime - lastCheckUpdateTimeStamp > 2 * 60 * 60_000) {
            checkUpdate();
        }
    }

    private void toNewActivity(ClassItem item) {
        switch (item.getType()) {
            case ClassItem.TYPE_ACTIVITY:
                startActivity(new Intent(this, item.getClazz()));
                break;
            case ClassItem.TYPE_FRAGMENT:
                Bundle bundle = new Bundle();
                bundle.putString(EmptyFragmentActivity.EXTRA_TITLE, item.getLabel());
                bundle.putBoolean(EmptyFragmentActivity.EXTRA_SHOW_ACTION_BAR, true);
                bundle.putBoolean(EmptyFragmentActivity.EXTRA_LANDSCAPE, item.isLandscape());
                bundle.putString(EmptyFragmentActivity.EXTRA_FRAGMENT_CLASS_NAME, item.getClazz().getName());
                EmptyFragmentActivity.launch(this, bundle);
                break;
        }
    }

    private List<ClassItem> getClassItems() {
        List<ClassItem> classItems = new ArrayList<>();
        classItems.add(new ClassItem(ClassItem.TYPE_FRAGMENT, "SwipeRefresh", SwipeRefreshFragment.class, false));
        classItems.add(new ClassItem(ClassItem.TYPE_FRAGMENT, "PullToRefresh", PullToRefreshFragment.class, false));
        classItems.add(new ClassItem(ClassItem.TYPE_FRAGMENT, "Optional", OptionalFragment.class, false));
        classItems.add(new ClassItem(ClassItem.TYPE_FRAGMENT, "Checkable", CheckableFragment.class, false));
        classItems.add(new ClassItem(ClassItem.TYPE_FRAGMENT, "About", AboutFragment.class, false));
        return classItems;
    }

    private void checkUpdate() {
        OkHttpClient client = new CustomHttpClient()
                .addHeader(new Pair<>("token", ""))
                .setConnectTimeout(5_000)
                .setShowLog(true)
                .createOkHttpClient();
        Retrofit retrofit = new CustomRetrofit()
                //我在app的build.gradle文件的defaultConfig标签里定义了BASE_URL
                .setBaseUrl(BuildConfig.BASE_URL)
                .setOkHttpClient(client)
                .createRetrofit();
        Disposable disposable = retrofit.create(ApiService.class)
                .getVersionInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        explainVersionInfoJson(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        showToast(throwable.getLocalizedMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {

                    }
                });
    }

    private void explainVersionInfoJson(String json) {
        json = json.substring(1, json.length() - 1);
        try {
            JSONObject object = new JSONObject(json).getJSONObject("apkInfo");
            int versionCode = object.getInt("versionCode");
            String versionName = object.getString("versionName");
            String fileName = object.getString("outputFile");
            String content = object.getString("content");

            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_GIDS);
            long curVersionCode = packageInfo.versionCode;
            String curVersionName = packageInfo.versionName;

            Log.i("MainActivity", "explainVersionInfoJson: {versionCod" + versionCode + ", curVersionCode:" + curVersionCode);
            //a new version
            if (versionCode > curVersionCode) {
                sharedPreferences.edit().putLong("lastCheckUpdateTimeStamp", new Date().getTime()).apply();
                showNewVersionDialog(String.format(
                        Locale.CHINA,
                        "当前版本:\u2000%1s\n"
                                + "最新版本:\u2000%2s\n\n"
                                + "更新内容:\n%3s"
                                + "\n\n立即更新？",
                        curVersionName,
                        versionName,
                        content
                ), fileName);
            }
        } catch (JSONException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showNewVersionDialog(String content, final String fileName) {
        new AlertDialog.Builder(this)
                .setTitle("新版本提示")
                .setMessage(content)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = BuildConfig.BASE_URL + BuildConfig.DOWNLOAD_URL;
                        Uri uri = Uri.parse(String.format(Locale.CHINA, url, fileName));
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);

                    }
                })
                .setNegativeButton("知道了", null)
                .show();
    }
}
