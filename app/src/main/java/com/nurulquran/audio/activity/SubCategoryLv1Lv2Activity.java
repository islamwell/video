package com.nurulquran.audio.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.nurulquran.audio.R;
import com.nurulquran.audio.adapter.CategoryMusicAdapter;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.fragment.CategoryMusicFragment;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.CategoryMusic;
import com.nurulquran.audio.object.OfflineData;
import com.nurulquran.audio.service.MusicService;
import com.nurulquran.audio.service.PlayerListener;
import com.nurulquran.audio.util.AppUtil;
import com.nurulquran.audio.util.NetworkUtil;
import com.nurulquran.audio.util.SmartLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by phamtuan on 29/04/2016.
 */
public class SubCategoryLv1Lv2Activity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private GridView mGridViewCategory;
    private CategoryMusicAdapter mCategoryMusicAdapter;
    private List<CategoryMusic> mListCategoryMusic;
    private int mPageNumber = 1;
    private SwipeRefreshLayout refreshData;
    private static final int SUB_CATEGORY = 112312312;
    private boolean LAST_PAGE=false;

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
        inFlaterLayOut(R.layout.activity_category_level_one_or_two);
        databaseUtility = new DatabaseUtility(SubCategoryLv1Lv2Activity.this);
        initUI();
        initControl();
        getData();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mUnBindService(mConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBindService(mConnection);
        MainActivity.toMusicPlayer=SUB_CATEGORY;
    }

    private void initUI() {
        mGridViewCategory = (GridView) findViewById(R.id.grvCategoryMusic);
        refreshData = (SwipeRefreshLayout) findViewById(R.id.swipeRfData);
        initUIMediaPlayerFooter();
    }

    private void initControl() {
        initControlMediaPlayerFooter();
        refreshData.setOnRefreshListener(this);
        mListCategoryMusic = new ArrayList<>();
        mCategoryMusicAdapter = new CategoryMusicAdapter(getApplicationContext(), mListCategoryMusic);
        mGridViewCategory.setAdapter(mCategoryMusicAdapter);
        setButtonBack();
        mGridViewCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoryMusic categoryMusic = mListCategoryMusic.get(position);
                if (categoryMusic.getLevel().equals("3") || categoryMusic.equals("4")) {
                    if (Integer.parseInt(categoryMusic.getCountSub()) > 0) {
                        openActivityOther(SubCategoryLv3Lv4Activity.class, categoryMusic);
                    } else if (Integer.parseInt(categoryMusic.getCountSub()) == 0) {
                        openActivityOther(SongCategoryActivity.class, categoryMusic);
                    }

                } else if (categoryMusic.getLevel().equals("2")) {
                    if (Integer.parseInt(categoryMusic.getCountSub()) > 0) {
                        openActivityOther(SubCategoryLv1Lv2Activity.class, categoryMusic);
                    } else if (Integer.parseInt(categoryMusic.getCountSub()) == 0) {
                        openActivityOther(SongCategoryActivity.class, categoryMusic);
                    }
                }
            }
        });
        mGridViewCategory.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.e("SCROLLLLLLLLLLLL","SRCollllllllllllllll succecccccccccccccccccccccc"+  "  fi: "+firstVisibleItem+"    vi:"+visibleItemCount+"    to:"+totalItemCount);
                if (firstVisibleItem + visibleItemCount >= totalItemCount && visibleItemCount != 0 && totalItemCount != 0&&LAST_PAGE==false) {
                    LAST_PAGE=true;
                    getData();
                }
            }
        });
    }

    private void getData() {
        refreshData.post(new Runnable() {
            @Override
            public void run() {
                refreshData.setVisibility(View.VISIBLE);
                refreshData.setRefreshing(true);

            }
        });
        Intent intent = getIntent();
        final CategoryMusic categoryMusic = (CategoryMusic) intent.getSerializableExtra(CategoryMusicFragment.BUNDLE_SUB_CATEGORY);
        setHeaderTitle(categoryMusic.getTitle());

        if (NetworkUtil.checkNetworkAvailable(SubCategoryLv1Lv2Activity.this)) {
            ModelManager.getSubCategory(this, String.valueOf(categoryMusic.getId()), String.valueOf(mPageNumber), new ModelManagerListener() {
                @Override
                public void onError(VolleyError error) {
                    refreshData.setRefreshing(false);
                    error.printStackTrace();
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
                                offlineData.setLevel((Integer.parseInt(categoryMusic.getLevel())+1)+"");
                                offlineData.setParentId(categoryMusic.getId()+"");
                                offlineData.setOfflinedata(json);
                                offlineData.setPageno(mPageNumber+"");
                                databaseUtility.addCategoryData(offlineData);
                            }
                        }
                    }catch (Exception e){

                    }

                    processCategoryResponse(json);
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
            offlineData=databaseUtility.getOfflinedataCat((Integer.parseInt(categoryMusic.getLevel())+1)+"",categoryMusic.getId()+"",mPageNumber+"");
            processCategoryResponse(offlineData.getOfflinedata());

        }
    }

    private void openActivityOther(Class<?> cls, CategoryMusic categoryMusic) {
        Intent intent = new Intent(getApplicationContext(), cls);
        intent.putExtra(CategoryMusicFragment.BUNDLE_SUB_CATEGORY, categoryMusic);
        startActivity(intent);
    }

    private void processCategoryResponse(String response) {
        String json = "";
        try {
            json = response;
            if (json == null) {
                AppUtil.alert(getApplicationContext(), getString(R.string.json_server_error));
                return;
            }
            SmartLog.log("", json);
            JSONObject entry = new JSONObject(json);
            LAST_PAGE=false;
            if (mPageNumber <= entry.getInt(WebserviceApi.ALL_PAGE)) {
                mPageNumber++;
                if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                        WebserviceApi.KEY_SUCCESS)) {
                    mListCategoryMusic.addAll(CommonParser.parseCategoryFromServer(json));
                    mCategoryMusicAdapter.notifyDataSetChanged();
                }
            } else {
                LAST_PAGE =true;
                Toast.makeText(getApplicationContext(), getString(R.string.endPage), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        mPageNumber = 1;
        mListCategoryMusic.clear();
        mCategoryMusicAdapter.notifyDataSetChanged();
        getData();
//        if (NetworkUtil.checkNetworkAvailable(SubCategoryLv1Lv2Activity.this)) {
//            mPageNumber = 1;
//            mListCategoryMusic.clear();
//            mCategoryMusicAdapter.notifyDataSetChanged();
//            getData();
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
