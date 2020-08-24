package com.nurulquran.audio.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.nurulquran.audio.R;
import com.nurulquran.audio.adapter.SongAdapter;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.fragment.CategoryMusicFragment;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.CategoryMusic;
import com.nurulquran.audio.object.OfflineData;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.service.MusicService;
import com.nurulquran.audio.service.PlayerListener;
import com.nurulquran.audio.util.AppUtil;
import com.nurulquran.audio.util.NetworkUtil;
import com.nurulquran.audio.util.SmartLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by phamtuan on 03/05/2016.
 */
public class SongCategoryActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private ArrayList<Song> mListSongCategory;
    private SongAdapter mSongAdapter;
    private ListView mLvListSongCategory;
    private SwipeRefreshLayout refreshData;
    private int mPageNumber = 1;
    private boolean LAST_PAGE = false;
    private DatabaseUtility databaseUtility;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.ServiceBinder mServiceBinder = (MusicService.ServiceBinder) service;
            mMusicService = mServiceBinder.getService();
            visibilityMediaFooter();
            mMusicService.setListener(new PlayerListener() {
                @Override
                public void onSeekChanged(int maxProgress, String lengthTime, String currentTime, int progress) {

                }

                @Override
                public void onChangeSong(int indexSong) {
                    showDataSong();
                    setButtonPlayerFooter();
                }

                @Override
                public void OnMusicPrepared() {

                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inFlaterLayOut(R.layout.activity_song_categoroy);
        databaseUtility = new DatabaseUtility(SongCategoryActivity.this);
        initUI();
        initControl();
        initData();
        loadMoreData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBindService(mConnection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUnBindService(mConnection);
    }

    private void initUI() {
        initUIMediaPlayerFooter();
        mLvListSongCategory = (ListView) findViewById(R.id.lvListSongCategory);
        refreshData = (SwipeRefreshLayout) findViewById(R.id.swipeRfData);
        setButtonBack();
    }

    private void initControl() {
        initControlMediaPlayerFooter();
        mListSongCategory = new ArrayList<>();
        mSongAdapter = new SongAdapter(getApplicationContext(), mListSongCategory);
        mLvListSongCategory.setAdapter(mSongAdapter);
        refreshData.setOnRefreshListener(this);
        mLvListSongCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                if (NetworkUtil.checkNetworkAvailable(SongCategoryActivity.this)) {
                    MainActivity.toMusicPlayer = MainActivity.FROM_LIST_SONG;
                    GlobalValue.currentSongPlay = (int) l;
                    if (GlobalValue.listSongPlay == null) {
                        GlobalValue.listSongPlay = new ArrayList<>();
                        GlobalValue.listSongPlay.addAll(mListSongCategory);
                    } else {
                        if (!GlobalValue.listSongPlay.isEmpty()) {
                            GlobalValue.listSongPlay.clear();
                        }
                        GlobalValue.listSongPlay.addAll(mListSongCategory);
                    }
                    MainActivity.isTapOnFooter = false;
                    gotoPlayer();
                }else{
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                            + getString(R.string.app_name) + "/", mListSongCategory.get(position).getName() + ".mp3");
                    Log.e("File path category",file.toString());
                    if (file.exists()) {
                        ArrayList<Song> mListSongDownLoad=new ArrayList<Song>();
                        MainActivity.toMusicPlayer = MainActivity.FROM_LIST_SONG;

                        mListSongDownLoad.add(mListSongCategory.get(position));
                        mListSongDownLoad.get(0).setDownloadCount(0);
                        mListSongDownLoad.get(0).setmTypePathFile(Song.PATH_FILE_DOWNLOAD);
                        mListSongDownLoad.get(0).setUrl(file.toString());

                        GlobalValue.currentSongPlay = (int) 0;
                        GlobalValue.listSongPlay.clear();
                        GlobalValue.listSongPlay.addAll(mListSongDownLoad);
                        MainActivity.isTapOnFooter = false;
                        gotoPlayer();
//                        MainActivity.toMusicPlayer = MainActivity.FROM_LIST_SONG;
//                        GlobalValue.currentSongPlay = (int) l;
//                        GlobalValue.listSongPlay.clear();
//                        GlobalValue.listSongPlay.addAll(mListSongCategory);
//                        MainActivity.isTapOnFooter = false;
//                        gotoPlayer();
                    } else {
                        Toast.makeText(SongCategoryActivity.this, "Data not available", Toast.LENGTH_SHORT).show();
                        //     databaseUtility.deleteSong(mListSongCategory.get(position).getId());

                    }

                }

            }
        });
    }

    private void loadMoreData() {
        mLvListSongCategory.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && visibleItemCount != 0 && totalItemCount != 0 && LAST_PAGE == false) {
                    LAST_PAGE = true;
                        initData();

                }
            }
        });
    }

    private void initData() {

        Intent intent = getIntent();
        final CategoryMusic categoryMusic = (CategoryMusic) intent.getSerializableExtra(CategoryMusicFragment.BUNDLE_SUB_CATEGORY);
        setHeaderTitle(categoryMusic.getTitle());
        if (NetworkUtil.checkNetworkAvailable(this)) {

            refreshData.post(new Runnable() {
                @Override
                public void run() {
                    refreshData.setRefreshing(true);
                }
            });
            ModelManager.getListSongCategory(this, String.valueOf(categoryMusic.getId()), String.valueOf(mPageNumber), new ModelManagerListener() {
                @Override
                public void onError(VolleyError error) {
                    refreshData.setRefreshing(false);
                }

                @Override
                public void onSuccess(String json) {
                    refreshData.setRefreshing(false);
                    try {
                        JSONObject entry = new JSONObject(json);
                        if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                                WebserviceApi.KEY_SUCCESS)) {
                            JSONArray items = entry.getJSONArray(WebserviceApi.KEY_DATA);
                            if (items.length()>0){
                                OfflineData offlineData=new OfflineData();
                                offlineData.setLevel("6");
                                offlineData.setParentId(categoryMusic.getId()+"");
                                offlineData.setOfflinedata(json
                                        .substring(json.indexOf("{")));
                                offlineData.setPageno(mPageNumber+"");
                                databaseUtility.addCategoryData(offlineData);

                            }
                        }
                    }catch (Exception e){

                    }

                    processListSongResponse(json);
                }
            });
        }else{
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    refreshData.setRefreshing(false);
                }
            }, 500);
            OfflineData offlineData= new OfflineData();
            offlineData=databaseUtility.getOfflinedataCat("6",categoryMusic.getId()+"",mPageNumber+"");
            processListSongResponse(offlineData.getOfflinedata());

        }
    }

    private void processListSongResponse(String response) {
        String json = "";
        try {
            json = response;
            if (json == null) {
                AppUtil.alert(this,
                        getString(R.string.json_server_error));
                return;
            }
            SmartLog.log("ListSongFragment", json);
            JSONObject entry = new JSONObject(json);
            LAST_PAGE = false;
            if (mPageNumber <= entry.getInt(WebserviceApi.ALL_PAGE)) {
                mPageNumber++;
                if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(WebserviceApi.KEY_SUCCESS)) {
                    mListSongCategory.addAll(CommonParser.parseSongFromServer(json));
                    mSongAdapter.notifyDataSetChanged();
                }
            } else {
                LAST_PAGE = true;
                Toast.makeText(getApplicationContext(), getString(R.string.endPage), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        mPageNumber = 1;
        mListSongCategory.clear();
        mSongAdapter.notifyDataSetChanged();
        initData();
//        if (NetworkUtil.checkNetworkAvailable(SongCategoryActivity.this)) {
//            mPageNumber = 1;
//            mListSongCategory.clear();
//            mSongAdapter.notifyDataSetChanged();
//            initData();
//        }else{
//            refreshData.post(new Runnable() {
//                @Override
//                public void run() {
//                    refreshData.setRefreshing(false);
//                }
//            });
//        }
    }
}
