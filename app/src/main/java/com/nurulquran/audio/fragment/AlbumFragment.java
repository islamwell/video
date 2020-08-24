package com.nurulquran.audio.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;
import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.adapter.AlbumAdapter;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.Album;
import com.nurulquran.audio.object.OfflineData;
import com.nurulquran.audio.slidingmenu.SlidingMenu;
import com.nurulquran.audio.util.AppUtil;
import com.nurulquran.audio.util.NetworkUtil;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshBase;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshGridView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends BaseFragment {

    private PullToRefreshGridView grvAlbum;
    private GridView grvActually;
    private AlbumAdapter albumAdapter;
    private List<Album> arrAlbum = new ArrayList<Album>();
    public static boolean isShowing = false;
    private int page = 0;
    private boolean isLoadMore = true;

    private DatabaseUtility databaseUtility;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_album, container, false);
        initUIBase(view);
        setButtonMenu(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // loadAlbum();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            page = 0;
            loadAlbum(true, false);
            getMainActivity().menu
                    .setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            getMainActivity().setVisibilityFooter();
        }
    }

    @Override
    protected void initUIBase(View view) {
        super.initUIBase(view);
        setHeaderTitle(R.string.series);
        databaseUtility = new DatabaseUtility(getActivity());
        grvAlbum = (PullToRefreshGridView) view.findViewById(R.id.grvAlbum);
        grvActually = grvAlbum.getRefreshableView();
        albumAdapter = new AlbumAdapter(self, arrAlbum);
        grvActually.setAdapter(albumAdapter);

        initControl(view);

    }

    private void initControl(View view) {
        grvAlbum.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int position,
                                    long l) {
                ListSongsFragment.isShowing = false;
                GlobalValue.currentAlbumId = arrAlbum.get(position).getId();
                GlobalValue.currentAlbumName = arrAlbum.get(position).getName();
                getMainActivity().gotoFragment(MainActivity.LIST_SONG_FRAGMENT);
            }
        });
        grvAlbum.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                return true;
            }
        });

        grvAlbum.setOnRefreshListener(new OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<GridView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                loadAlbum(true, true);
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<GridView> refreshView) {
                loadAlbum(false, true);
            }
        });
    }

    private void loadAlbum(final boolean isRefresh, final boolean isPull) {

        if (isRefresh) {
            isLoadMore = true;
            arrAlbum.clear();
            page = 0;
        }

        if (!isLoadMore) {
//            showNoMoreData();
        } else {
            page++;
            if (NetworkUtil.checkNetworkAvailable(getActivity())) {
            ModelManager.sendGetRequest(self, WebserviceApi.getAlbumApi(getActivity()) + "?page=" + page, null, !isPull, new ModelManagerListener() {
                @Override
                public void onError(VolleyError error) {
                    if (error instanceof NetworkError) {
                        AppUtil.alertNetworkUnavailableCommon(getActivity());
                        return;
                    } else {
                        AppUtil.alert(getActivity(),
                                getString(R.string.server_error));
                    }

                }

                @Override
                public void onSuccess(String json) {

                    OfflineData offlineData=new OfflineData();
                    offlineData.setLevel("5");
                    offlineData.setParentId("5");
                    offlineData.setPageno(page+"");
                    offlineData.setOfflinedata(json
                            .substring(json.indexOf("{")));
                    databaseUtility.addCategoryData(offlineData);

                    processAlbumResponse(json
                                    .substring(json.indexOf("{")),
                            isRefresh);
                }
            });
            }else{
                OfflineData offlineData= new OfflineData();
               offlineData=databaseUtility.getOfflinedata("5","5",page+"");
                processAlbumResponse(offlineData.getOfflinedata(),
                        isRefresh);
            }
        }
    }

    private void showNoMoreData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToast(R.string.endPage);
                grvAlbum.onRefreshComplete();
            }
        }, 100);
    }

    private void processAlbumResponse(String json, boolean isRefresh) {
        try {
            if (json == null) {
                AppUtil.alert(getActivity(),
                        getString(R.string.json_server_error));
                return;
            }

            JSONObject entry = new JSONObject(json);

            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                    WebserviceApi.KEY_SUCCESS)) {

                List<Album> tempList = CommonParser.parseAlbumFromServer(json);
                if (tempList.size() > 0) {
                    arrAlbum.addAll(tempList);
                    if (!isRefresh)
                        showToast(R.string.loadmore_success);
                    isLoadMore = true;
                } else {
                    isLoadMore = false;
                    showToast(R.string.endPage);
                }

                albumAdapter.notifyDataSetChanged();
                //   grvActually.setSelection(arrAlbum.size() - 1);
                grvAlbum.onRefreshComplete();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
