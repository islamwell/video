package com.nurulquran.audio.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.SongCategoryActivity;
import com.nurulquran.audio.activity.SubCategoryLv1Lv2Activity;
import com.nurulquran.audio.activity.SubCategoryLv3Lv4Activity;
import com.nurulquran.audio.adapter.CategoryMusicAdapter;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.CategoryMusic;
import com.nurulquran.audio.object.OfflineData;
import com.nurulquran.audio.slidingmenu.SlidingMenu;
import com.nurulquran.audio.util.AppUtil;
import com.nurulquran.audio.util.NetworkUtil;
import com.nurulquran.audio.util.SmartLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CategoryMusicFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    private GridView grvCategoryMusic;
    private CategoryMusicAdapter categoryMusicAdapter;
    private List<CategoryMusic> arrCategory;
    public static boolean isShowing = false;
    private View currentView;
    private int mPage = 1;
    public static final String BUNDLE_SUB_CATEGORY = "sub category";
    private SwipeRefreshLayout refreshData;
    private boolean LAST_PAGE = false;
    private int stateScroll;
    private boolean check;

    private DatabaseUtility databaseUtility;

    /**
     * the first running
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_music,
                container, false);
        initUIBase(view);
        setButtonMenu(view);
        currentView = view;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * the third running
     *
     * @param hidden
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (!isShowing) {
                loadCategory();
                isShowing = true;
            }
            getMainActivity().menu
                    .setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            getMainActivity().setVisibilityFooter();
            getMainActivity().hideBannerAd();
        }
    }

    /**
     * the second running
     *
     * @param view
     */
    @Override
    protected void initUIBase(final View view) {
        super.initUIBase(view);
        databaseUtility = new DatabaseUtility(getActivity());
        setHeaderTitle(R.string.categoryMusic);
        grvCategoryMusic = (GridView) view.findViewById(R.id.grvCategoryMusic);
        refreshData = (SwipeRefreshLayout) view.findViewById(R.id.swipeRfData);
        refreshData.setOnRefreshListener(this);
        arrCategory = new ArrayList<>();
        categoryMusicAdapter = new CategoryMusicAdapter(self,
                arrCategory);
        grvCategoryMusic.setAdapter(categoryMusicAdapter);
        grvCategoryMusic.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int position,
                                    long l) {
                CategoryMusic selectedCategory = arrCategory.get(position);
                if (selectedCategory.getLevel().equals("1") || selectedCategory.getLevel().equals("2")) {
                    if (Integer.parseInt(selectedCategory.getCountSub()) > 0) {
                        openActivityOther(SubCategoryLv1Lv2Activity.class, selectedCategory);
                    } else if (Integer.parseInt(selectedCategory.getCountSub()) == 0) {
                        openActivityOther(SongCategoryActivity.class, selectedCategory);
                    }

                } else if (selectedCategory.getLevel().equals("3") || selectedCategory.equals("4")) {
                    if (Integer.parseInt(selectedCategory.getCountSub()) > 0) {
                        openActivityOther(SubCategoryLv3Lv4Activity.class, selectedCategory);
                    } else if (Integer.parseInt(selectedCategory.getCountSub()) == 0) {
                        openActivityOther(SongCategoryActivity.class, selectedCategory);
                    }

                }
            }
        });
        loadMoreCategory();
    }

    private void openActivityOther(Class<?> cls, CategoryMusic categoryMusic) {
        Intent intent = new Intent(getActivity(), cls);
        intent.putExtra(BUNDLE_SUB_CATEGORY, categoryMusic);
        startActivity(intent);
    }

    private void loadMoreCategory() {
        grvCategoryMusic.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                stateScroll = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && visibleItemCount != 0 && totalItemCount != 0&&LAST_PAGE==false) {
                        LAST_PAGE =true;
                        loadCategory();}
                Log.e("SCROLLLLLLLLLLLL","SRCollllllllllllllll succecccccccccccccccccccccc"+firstVisibleItem+"    vi:"+visibleItemCount+"    to:"+totalItemCount);
            }
        });
    }


    private void loadCategory() {
        refreshData.post(new Runnable() {
            @Override
            public void run() {
                refreshData.setVisibility(View.VISIBLE);
                refreshData.setRefreshing(true);
            }
        });
        if (NetworkUtil.checkNetworkAvailable(getActivity())) {
            ModelManager.getCategory(getActivity(), String.valueOf(mPage), new ModelManagerListener() {
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
                                offlineData.setLevel("1");
                                offlineData.setPageno(mPage+"");
                                offlineData.setParentId("0");
                                offlineData.setOfflinedata(json);
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
            offlineData=databaseUtility.getOfflinedataCat("1","0",mPage+"");
            processCategoryResponse(offlineData.getOfflinedata());
        }


    }

    private void processCategoryResponse(String response) {
        String json = "";
        try {
            json = response;
            if (json == null) {
                AppUtil.alert(getActivity(), getString(R.string.json_server_error));
                return;
            }
            SmartLog.log(TAG, json);
            JSONObject entry = new JSONObject(json);
            LAST_PAGE=false;
            if (mPage <= entry.getInt(WebserviceApi.ALL_PAGE)) {
                mPage++;

                if (entry.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(
                        WebserviceApi.KEY_SUCCESS)) {
                    arrCategory.addAll(CommonParser.parseCategoryFromServer(json));
                    categoryMusicAdapter.notifyDataSetChanged();
                }
            } else {
                LAST_PAGE = true;
                Toast.makeText(getActivity(), getString(R.string.endPage), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        LAST_PAGE =false;
        arrCategory.clear();
        categoryMusicAdapter.notifyDataSetChanged();
        loadCategory();
//        if (NetworkUtil.checkNetworkAvailable(getActivity())) {
//            mPage = 1;
//            LAST_PAGE =false;
//            arrCategory.clear();
//            categoryMusicAdapter.notifyDataSetChanged();
//            loadCategory();
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
