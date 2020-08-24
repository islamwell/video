package com.nurulquran.audio.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;
import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.adapter.SongAdapter;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.OfflineData;
import com.nurulquran.audio.object.Playlist;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.slidingmenu.SlidingMenu;
import com.nurulquran.audio.util.AppUtil;
import com.nurulquran.audio.util.Logger;
import com.nurulquran.audio.util.NetworkUtil;
import com.nurulquran.audio.util.SmartLog;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshBase;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListSongsFragment extends BaseFragment {
    public static final String SORT_BY_VIEWS = "listen";
    public static final String SORT_BY_DOWNLOAD = "download";

    private PullToRefreshListView lsvSong;
    private ListView lsvActually;
    private List<Song> arrSong = new ArrayList<Song>();
    private SongAdapter songAdapter;
    private View view, btnSortBy;
    private int page;
    private int totalPage;
    private static String currentSortBy = "";
    public static boolean isShowing = false;
    private DatabaseUtility databaseUtility;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_song, container, false);
        databaseUtility = new DatabaseUtility(getActivity());
        initUIBase(view);
        initControl(view);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (!isShowing) {
                page = 0;
                totalPage = 0;
                initData();
                isShowing = true;
            }
            // getData(true);
            getMainActivity().menu
                    .setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            getMainActivity().setVisibilityFooter();
        }
    }

    @Override
    protected void initUIBase(View view) {
        super.initUIBase(view);
        lsvSong = (PullToRefreshListView) view.findViewById(R.id.lsvSong);
        lsvActually = lsvSong.getRefreshableView();

        songAdapter = new SongAdapter(getActivity(), arrSong);
        lsvActually.setAdapter(songAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initControl(View view) {
        setButtonMenu(view);
        initRightButton(view);
        lsvSong.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int position,
                                    long l) {
                if (NetworkUtil.checkNetworkAvailable(getActivity())) {
                    Logger.e("currentFragment: "
                            + getMainActivity().currentFragment);
                    getMainActivity().toMusicPlayer = MainActivity.FROM_LIST_SONG;
                    GlobalValue.currentSongPlay = (int) l;
                    GlobalValue.listSongPlay.clear();
                    GlobalValue.listSongPlay.addAll(arrSong);
                    getMainActivity().isTapOnFooter = false;
                    gotoPlayer();
                }else{
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                            + getString(R.string.app_name) + "/", arrSong.get(position-1).getName() + ".mp3");
                    Log.e("File path List",file.toString());
                    if (file.exists()) {
                        ArrayList<Song> mListSongDownLoad=new ArrayList<Song>();
                        MainActivity.toMusicPlayer = MainActivity.FROM_LIST_SONG;
                        mListSongDownLoad.add(arrSong.get(position-1));
                        mListSongDownLoad.get(0).setDownloadCount(0);
                        mListSongDownLoad.get(0).setmTypePathFile(Song.PATH_FILE_DOWNLOAD);
                        mListSongDownLoad.get(0).setUrl(file.toString());
                        GlobalValue.currentSongPlay = (int) 0;
                        GlobalValue.listSongPlay.clear();
                        GlobalValue.listSongPlay.addAll(mListSongDownLoad);
                        MainActivity.isTapOnFooter = false;
                        gotoPlayer();
                    } else {
                        Toast.makeText(getActivity(), "Data not available", Toast.LENGTH_SHORT).show();
                       // databaseUtility.deleteSong(arrSong.get(position).getId());

                    }
                }


            }
        });
        lsvSong.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                showConfirmDeleteItemFromPlaylistDialog(position);
                return true;
            }
        });

        lsvSong.setOnRefreshListener(new OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                page = 0;
                arrSong.clear();
                getData(true, true);
            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity(),
                        System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
                                | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                getData(false, true);
                if (NetworkUtil.checkNetworkAvailable(getActivity())) {

                }
            }
        });
    }

    private void showConfirmDeleteItemFromPlaylistDialog(final int index) {
        if (GlobalValue.currentMenu == getMainActivity().PLAYLIST) {
            final Playlist playlist = getMainActivity().currentPlaylist;

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final String nameSong =getMainActivity().currentPlaylist.getListSongs().get(index-1).getName();
            builder.setTitle("Remove "+nameSong)
                    .setMessage(
                            "Do you want to remove "+nameSong+" "+ "from '"
                                    + playlist.getName() + " ' ?")
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    boolean result = playlist
                                            .removeSong(index - 1);
                                    // update play list
                                    if (getMainActivity().databaseUtility
                                            .updatePlaylist(playlist)) {
                                        Toast.makeText(
                                                getMainActivity(),
                                                "Remove "+nameSong+" "+"from "
                                                        + playlist.getName()
                                                        + " successfully!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    if (result == true) {
                                        arrSong.clear();
                                        arrSong.addAll(getMainActivity().currentPlaylist
                                                .getListSongs());
                                        songAdapter.notifyDataSetChanged();
                                    }
                                }
                            }).setNegativeButton(android.R.string.cancel, null);
            builder.create().show();
        }
    }

    private void initRightButton(View view) {
        btnSortBy = view.findViewById(R.id.btnRightButton);
        btnSortBy.setVisibility(View.INVISIBLE);
        btnSortBy.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                showPopupMenu(btnSortBy);
            }
        });
    }

    private void getData(boolean isRefresh, boolean isPull) {
        //
        switch (GlobalValue.currentMenu) {
            case MainActivity.MOST_FAVOURITE:
                getTopWeekMusic(isRefresh, isPull);
                break;
            case MainActivity.LASTEST:
                getAllMusic(isRefresh, isPull);
                break;

            case MainActivity.CATEGORY_MUSIC:
                getSongByCategory(isRefresh, isPull);
                break;
            case MainActivity.SERIES:
                getSongByAlbum(isRefresh, isPull);
                break;
            case MainActivity.PLAYLIST:
                arrSong.clear();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        arrSong.addAll(getMainActivity().currentPlaylist
                                .getListSongs());
                        songAdapter.notifyDataSetChanged();
                        lsvSong.onRefreshComplete();
                    }
                }, 500);

                break;
        }
    }

    private void getTopWeekMusic(final boolean isRefresh, final boolean isPull) {
        if (++page > totalPage && totalPage > 0 && !isRefresh) {
            showNoMoreData();
        } else {
            String getUrl = WebserviceApi.getTopSong(getActivity()) + "?page=" + page;
            Logger.e(TAG, "URL : " + getUrl);
            if (NetworkUtil.checkNetworkAvailable(getActivity())) {
            ModelManager.sendGetRequest(self, getUrl, null, !isPull, new ModelManagerListener() {
                @Override
                public void onError(VolleyError error) {
                    if (error instanceof NetworkError) {
                        AppUtil.alertNetworkUnavailableCommon(getActivity());
                    } else {
                        AppUtil.alert(getActivity(),
                                getString(R.string.server_error));
                    }
                    lsvSong.onRefreshComplete();

                }

                @Override
                public void onSuccess(String json) {

                    try {
                        JSONObject entry = new JSONObject(json);
                        if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                                WebserviceApi.KEY_SUCCESS)) {
                            JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                            if (items.length()>0){
                                OfflineData offlineData=new OfflineData();
                                offlineData.setLevel("All");
                                offlineData.setParentId("TopWeek");
                                offlineData.setOfflinedata(json
                                        .substring(json.indexOf("{")));
                                offlineData.setPageno(page+"");
                                databaseUtility.addCategoryData(offlineData);
                            }
                        }
                    }catch (Exception e){

                    }

                    processListSongResponse(json.substring(json
                            .indexOf("{")));
                    lsvSong.onRefreshComplete();
                }
            });}else{
                OfflineData offlineData= new OfflineData();
                offlineData=databaseUtility.getOfflinedata("All","TopWeek",page+"");
                processListSongResponse(offlineData.getOfflinedata());
                lsvSong.onRefreshComplete();
            }

        }
    }

    private void getAllMusic(final boolean isRefresh, final boolean isPull) {
        if (++page > totalPage && totalPage > 0 && !isRefresh) {
            showNoMoreData();
        } else {
//            currentSortBy = MySharePreferences.getInstance().getTypeSort(getActivity());
            String getUrl = WebserviceApi.getSongs(getActivity()) + "?page=" + page
                    + "&type=" + currentSortBy;
            Logger.e(TAG, "URL : " + getUrl);
            if (NetworkUtil.checkNetworkAvailable(getActivity())) {
            ModelManager.sendGetRequest(self, getUrl, null, !isPull, new ModelManagerListener() {
                @Override
                public void onError(VolleyError error) {
                    if (error instanceof NetworkError) {
                        AppUtil.alertNetworkUnavailableCommon(getActivity());
                    } else {
                        AppUtil.alert(getActivity(),
                                getString(R.string.server_error));
                    }
                    lsvSong.onRefreshComplete();
                }

                @Override
                public void onSuccess(String json) {
                    try {
                        JSONObject entry = new JSONObject(json);
                        if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                                WebserviceApi.KEY_SUCCESS)) {
                            JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                            if (items.length()>0){
                                OfflineData offlineData=new OfflineData();
                                offlineData.setLevel("All");
                                offlineData.setParentId("Latest");
                                offlineData.setOfflinedata(json
                                        .substring(json.indexOf("{")));
                                offlineData.setPageno(page+"");
                                databaseUtility.addCategoryData(offlineData);
                            }
                        }
                    }catch (Exception e){

                    }

                    processListSongResponse(json.substring(json
                            .indexOf("{")));
                    lsvSong.onRefreshComplete();
                }
            });
            }else {
                OfflineData offlineData= new OfflineData();
                offlineData=databaseUtility.getOfflinedata("All","Latest",page+"");
                processListSongResponse(offlineData.getOfflinedata());
                lsvSong.onRefreshComplete();
            }

        }
    }

    private void processListSongResponse(String response) {
        String json = "";
        try {

            json = response;
            if (json == null) {
                AppUtil.alert(getActivity(),
                        getString(R.string.json_server_error));
                return;
            }

            SmartLog.log("ListSongFragment", json);
            JSONObject entry = new JSONObject(json);

            if (CommonParser.isInteger(entry
                    .getString(WebserviceApi.KEY_ALL_PAGE))) {
                totalPage = entry.getInt(WebserviceApi.KEY_ALL_PAGE);
            }

            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                    WebserviceApi.KEY_SUCCESS)) {

                List<Song> tempList = CommonParser.parseSongFromServer(json);
                arrSong.addAll(tempList);
                songAdapter.notifyDataSetChanged();
                lsvSong.onRefreshComplete();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNoMoreData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToast(R.string.endPage);
                lsvSong.onRefreshComplete();
            }
        }, 100);
    }

    private void initData() {
        getMainActivity().currentFragment = MainActivity.LIST_SONG_FRAGMENT;
        if (arrSong != null) {
            arrSong.clear();
            refreshList();
        }
        switch (GlobalValue.currentMenu) {
            case MainActivity.MOST_FAVOURITE:
                setHeaderTitle(R.string.most_favourite);
                if (getMainActivity().listTopWeek.size() == 0) {
                    getData(true, false);
                } else {
                    getData(false, false);
                }
                btnSortBy.setVisibility(View.INVISIBLE);
                setButtonMenu(view);
                break;
            case MainActivity.LASTEST:
                setHeaderTitle(R.string.lastest);
                getData(true, false);
                btnSortBy.setVisibility(View.VISIBLE);
                setButtonMenu(view);

                break;

            case MainActivity.SERIES:
                setHeaderTitle(GlobalValue.currentAlbumName);
                getData(true, false);
                btnSortBy.setVisibility(View.INVISIBLE);
                setButtonBack(view);
                break;

            case MainActivity.CATEGORY_MUSIC:
                setHeaderTitle(GlobalValue.currentCategoryName);
                getData(true, false);
                setButtonBack(view);
                btnSortBy.setVisibility(View.INVISIBLE);
                break;

            case MainActivity.PLAYLIST:
                setHeaderTitle(getMainActivity().currentPlaylist.getName());
                arrSong.clear();
                arrSong.addAll(getMainActivity().currentPlaylist.getListSongs());
                songAdapter.notifyDataSetChanged();
                btnSortBy.setVisibility(View.INVISIBLE);
                setButtonBack(view);
                break;
        }
    }

    private void getSongByCategory(boolean isRefresh, final boolean isPull) {
        if (++page > totalPage && totalPage > 0 && !isRefresh) {
            showNoMoreData();
        } else {

            String getUrl = WebserviceApi.getSongByCategory(getActivity()) + "?categoryId="
                    + GlobalValue.currentCategoryId + "&page=" + page;
            if (NetworkUtil.checkNetworkAvailable(getActivity())) {
                ModelManager.sendGetRequest(self, getUrl, null, !isPull, new ModelManagerListener() {
                    @Override
                    public void onError(VolleyError error) {
                        if (error instanceof NetworkError) {
                            AppUtil.alertNetworkUnavailableCommon(getActivity());
                        } else {
                            AppUtil.alert(getActivity(),
                                    getString(R.string.server_error));
                        }
                        lsvSong.onRefreshComplete();
                    }

                    @Override
                    public void onSuccess(String json) {
                        try {
                            JSONObject entry = new JSONObject(json);
                            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                                    WebserviceApi.KEY_SUCCESS)) {
                                JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                                if (items.length()>0){
                                    OfflineData offlineData=new OfflineData();
                                    offlineData.setLevel(GlobalValue.currentCategoryId+"");
                                    offlineData.setParentId("6");
                                    offlineData.setOfflinedata(json);
                                    offlineData.setPageno(page+"");
                                    databaseUtility.addCategoryData(offlineData);
                                }
                            }
                        }catch (Exception e){

                        }

                        processListSongResponse(json.substring(json
                                .indexOf("{")));
                        lsvSong.onRefreshComplete();
                    }
                });
            }else{
                OfflineData offlineData= new OfflineData();
                offlineData=databaseUtility.getOfflinedata(GlobalValue.currentCategoryId+"","6",page+"");
                processListSongResponse(offlineData.getOfflinedata());
            }

        }
    }

    private void getSongByAlbum(boolean isRefresh, final boolean isPull) {

        if (++page > totalPage && totalPage > 0 && !isRefresh) {
            showNoMoreData();
        } else {

            String getUrl = WebserviceApi.getSongByAlbum(getActivity()) + "?albumId="
                    + GlobalValue.currentAlbumId + "&page=" + page;
            if (NetworkUtil.checkNetworkAvailable(getActivity())) {
                ModelManager.sendGetRequest(self, getUrl, null, !isPull, new ModelManagerListener() {
                    @Override
                    public void onError(VolleyError error) {
                        if (error instanceof NetworkError) {
                            AppUtil.alertNetworkUnavailableCommon(getActivity());
                        } else {
                            AppUtil.alert(getActivity(),
                                    getString(R.string.server_error));
                        }
                        lsvSong.onRefreshComplete();
                    }

                    @Override
                    public void onSuccess(String json) {
                        try {
                            JSONObject entry = new JSONObject(json);
                            if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                                    WebserviceApi.KEY_SUCCESS)) {
                                JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                                if (items.length()>0){
                                    OfflineData offlineData=new OfflineData();
                                    offlineData.setLevel(GlobalValue.currentAlbumId+"");
                                    offlineData.setParentId("6");
                                    offlineData.setOfflinedata(json);
                                    offlineData.setPageno(page+"");
                                    databaseUtility.addCategoryData(offlineData);
                                }
                            }
                        }catch (Exception e){

                        }

                        processListSongResponse(json.substring(json
                                .indexOf("{")));
                        lsvSong.onRefreshComplete();
                    }
                });
            }else{
                OfflineData offlineData= new OfflineData();
                offlineData=databaseUtility.getOfflinedata(GlobalValue.currentAlbumId+"","6",page+"");
                processListSongResponse(offlineData.getOfflinedata());
                lsvSong.onRefreshComplete();
            }


        }
    }

    private void refreshList() {
        songAdapter.notifyDataSetChanged();
        lsvSong.onRefreshComplete();
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getMainActivity(), v);
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().equals(
                        getMainActivity().getString(R.string.sort_by_views))) {
                    currentSortBy = SORT_BY_VIEWS;

                } else if (item.getTitle().equals(
                        getMainActivity().getString(R.string.sort_by_download))) {
                    currentSortBy = SORT_BY_DOWNLOAD;

                } else {
                    currentSortBy = "";
                }
//                MySharePreferences.getInstance().saveTypeSort(getActivity(),currentSortBy);
                page = 0;
                arrSong.clear();
                getAllMusic(true, false);
                return false;
            }
        });
        popupMenu.inflate(R.menu.popupmenu);
        popupMenu.show();
    }
}
