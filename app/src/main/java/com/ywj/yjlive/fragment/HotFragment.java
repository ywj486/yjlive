package com.ywj.yjlive.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.google.gson.Gson;
import com.ywj.yjlive.R;
import com.ywj.yjlive.adapter.HotAdapter;
import com.ywj.yjlive.adapter.base.BaseAdapter;
import com.ywj.yjlive.adapter.decoration.DividerItemDecoration;
import com.ywj.yjlive.bean.Live;
import com.ywj.yjlive.fun_sdk.NetDbAdapter;
import com.ywj.yjlive.fun_sdk.Settings;
import com.ywj.yjlive.fun_sdk.TextureVideoActivity;
import com.ywj.yjlive.fun_sdk.TextureVodActivity;
import com.ywj.yjlive.http.Contants;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;


/**
 * Created by yj on 17/3/8.
 */

public class HotFragment extends Fragment {
    @BindView(R.id.hot_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.hot_refresh)
    MaterialRefreshLayout mRefreshLayout;
    private HotAdapter mAdapter;
    private List<Live.ResultBean.ListBean> list;
    private NetDbAdapter NetDb;
    private SharedPreferences settings;
    int page = 1;
    String path;

    public static HotFragment newInstance() {
        HotFragment fragment = new HotFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_vp2_dome, container, false);
        ButterKnife.bind(this, view);
        initListener();
        LoadData(page);
        return view;
    }

    private void initListener() {
        mRefreshLayout.setLoadMore(true);
        mRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                Log.e("TAG", "上拉刷新");
                refreshDate();
                mRefreshLayout.finishRefresh();
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                Log.e("TAG", "下拉加载");
                if (page == 0) {
                    loadMoreData();
                } else {
                    Toast.makeText(getContext(), "已经没有更多数据了", Toast.LENGTH_SHORT).show();
                }
                mRefreshLayout.finishRefreshLoadMore();
            }
        });
    }

    private void refreshDate() {
        page = 1;
        LoadData(page);
    }

    private void loadMoreData() {
        page += 1;
        LoadData(page);
    }

    void LoadData(final int page) {
        final Map<String, String> map = new HashMap<>();
        map.put("type", 0 + "");
        map.put("page", page + "");
        OkHttpUtils.post()
                .url(Contants.API.Main)
                .params(map)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Gson gson = new Gson();
                        Live live = gson.fromJson(response, Live.class);
                        if (live != null) {
                            if (live.getResult().getList() != null) {
                                list = live.getResult().getList();
                                showData();
                            }
                        }

                    }
                });
    }

    private void showData() {
        if (mAdapter == null) {
            mAdapter = new HotAdapter(getContext(), list);
        } else {
            mAdapter.clearData();
            mAdapter.addData(list);
        }
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
        mAdapter.setmOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                                MainActivity activity = (MainActivity) getActivity();
//                                activity.openActivity(LiveActivity.class);
                path = Contants.PUSH_STREAMER + list.get(position).getId();
                Log.e("TAG", "path===" + path);
                NetDb = new NetDbAdapter(getActivity());
                NetDb.open();

                if (NetDb.getData(path)) {
                    NetDb.updateData(path);
                } else {
                    NetDb.createDate(path);
                }
                NetDb.close();
                String playerType = "live";
                if (playerType.equals(Settings.VOD)) {
                    Intent intent = new Intent(getActivity(), TextureVodActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), TextureVideoActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                }
            }
        });
    }
}
