package com.nurulquran.audio.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;
import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.adapter.SongAdapter;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.slidingmenu.SlidingMenu;
import com.nurulquran.audio.util.AppUtil;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.util.SmartLog;
import com.nurulquran.audio.widget.AutoBgButton;
import com.nurulquran.audio.widget.SoftKeyboard;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshBase;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshListView;
import com.nurulquran.audio.widget.pulltorefresh.PullToRefreshBase.OnRefreshListener2;

public class SearchFragment extends BaseFragment {
    private AutoBgButton btnSearch;
    private EditText txtKeyword;
    // private ListView lsvResult;
    private PullToRefreshListView lsvResult;
    private ListView lsvActually;
    private View lblNoResult;
    private List<Song> listResult;
    private SongAdapter songAdapter;
    private ProgressDialog progressDialog;
    private int page = 0, totalPage;
    public String keyword = "";
    private boolean isSearch = false;
    private LinearLayout llParent;
    private InputMethodManager imm;
    private ImageView ivBackgroud;
    private ImageView btnClearText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater
                .inflate(R.layout.fragment_search, container, false);
        initUIBase(view);
        initControl(view);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getMainActivity().menu
                    .setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            getMainActivity().setVisibilityFooter();
            if (keyword.equals("")) {

                page = 0;
                totalPage = 0;
                clearList();
            }
            txtKeyword.setText(keyword);
        }
    }

    @Override
    protected void initUIBase(View view) {
        super.initUIBase(view);
        btnClearText = (ImageView) view.findViewById(R.id.btnClear);
        btnSearch = (AutoBgButton) view.findViewById(R.id.btnSearch);
        txtKeyword = (EditText) view.findViewById(R.id.txtKeyword);
        lsvResult = (PullToRefreshListView) view.findViewById(R.id.lsvResult);
        lsvActually = lsvResult.getRefreshableView();
        lblNoResult = view.findViewById(R.id.lblNoResult);
        llParent = (LinearLayout) view.findViewById(R.id.llParent);
        ivBackgroud = (ImageView) view.findViewById(R.id.ivBackground);
        imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initControl(View view) {
        btnClearText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtKeyword.setText("");
            }
        });
        llParent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().showBannerAd();
                getMainActivity().showMediaFooter();
                imm.hideSoftInputFromWindow(txtKeyword.getWindowToken(), 0);
            }
        });
        llParent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getMainActivity().showBannerAd();
                getMainActivity().showMediaFooter();
                imm.hideSoftInputFromWindow(txtKeyword.getWindowToken(), 0);
                return false;
            }
        });
        setButtonMenu(view,imm,txtKeyword);
        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSearch();
            }
        });

        txtKeyword.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onClickSearch();
                    return true;
                }
                return false;
            }
        });
        txtKeyword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    imm.hideSoftInputFromWindow(txtKeyword.getWindowToken(), 0);

                } else {
                    getMainActivity().hideBannerAd();
                    getMainActivity().hideMediaFooter();
                }


            }
        });
        txtKeyword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getMainActivity().hideBannerAd();
                getMainActivity().hideMediaFooter();
            }
        });
        txtKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnClearText.setVisibility(View.VISIBLE);
                } else {
                    btnClearText.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        setHeaderTitle(R.string.search);
        listResult = new ArrayList<Song>();
        songAdapter = new SongAdapter(getActivity(), listResult);
        lsvActually.setAdapter(songAdapter);
        lsvResult.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int position,
                                    long l) {
                // Log.e("SearchFragment", "position :"+position);
                ListSongsFragment.isShowing = false;
                GlobalValue.currentSongPlay = (int) l;
                GlobalValue.listSongPlay.clear();
                GlobalValue.listSongPlay.addAll(listResult);
                getMainActivity().toMusicPlayer = MainActivity.FROM_SEARCH;
                getMainActivity().isTapOnFooter = false;
                gotoPlayer();
            }
        });

        lsvResult.setOnRefreshListener(new OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                page = 1;
                if (txtKeyword.getText().length() > 0) {
                    getSongToSearch(true, true);
                } else {
                    lsvResult.onRefreshComplete();
                    return;
                }

            }

            @Override
            public void onPullUpToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                if (txtKeyword.getText().length() > 0) {
                    getSongToSearch(false, true);
                } else {
                    lsvResult.onRefreshComplete();
                    return;
                }
            }
        });
    }

    private void onClickSearch() {
        totalPage = 0;
        page = 0;
        clearList();
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(txtKeyword.getWindowToken(), 0);
        keyword = txtKeyword.getText().toString().trim();

        // showLoadingDialog();
        getSongToSearch(true, false);
    }

    private void getSongToSearch(boolean isRefresh, final boolean isPull) {
        Log.d("PAGE_FIRST", page + "-" + totalPage);
        if (++page > totalPage && totalPage > 0 && !isRefresh) {
            Log.d("PAGE", page + "-" + totalPage);
            showNoMoreData();
        } else {
            Log.d("PAGE", page + "-" + totalPage);
            String getUrl = WebserviceApi.getSearchSong(getActivity()) + "?song=" + keyword
                    + "&page=" + page;

            ModelManager.sendGetRequest(self, getUrl, null, !isPull, new ModelManagerListener() {
                @Override
                public void onError(VolleyError error) {
                    if (error instanceof NetworkError) {
                        AppUtil.alertNetworkUnavailableCommon(getActivity());
                    } else {
                        AppUtil.alert(getActivity(),
                                getString(R.string.server_error));
                    }
                    lsvResult.onRefreshComplete();
                }

                @Override
                public void onSuccess(String json) {
                    processListSongResponse(json.substring(json
                            .indexOf("{")));
                    lsvResult.onRefreshComplete();
                }
            });
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

                List<Song> arrSong = CommonParser.parseSongFromServer(json);
                for (Song song : arrSong) {
                    addSongToListResult(song);
                }

                if (listResult.size() > 0) {
                    lblNoResult.setVisibility(View.GONE);
                    lsvResult.setVisibility(View.VISIBLE);
                    songAdapter.notifyDataSetChanged();
                } else {
                    lblNoResult.setVisibility(View.VISIBLE);
                    lsvResult.setVisibility(View.GONE);
                    songAdapter.notifyDataSetChanged();
                }

            } else {
                lblNoResult.setVisibility(View.VISIBLE);
                lsvResult.setVisibility(View.GONE);
                songAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // hideLoadingDialog();
    }

    private void showNoMoreData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showToast(R.string.endPage);
                lsvResult.onRefreshComplete();
            }
        }, 100);
    }

    private void addSongToListResult(Song song) {
        listResult.add(song);
    }

    private void clearList() {
        if (listResult != null) {
            listResult.clear();
        }
        if (songAdapter != null) {
            songAdapter.notifyDataSetChanged();
        }
    }
public void hideSoft() {
    imm.hideSoftInputFromWindow(txtKeyword.getWindowToken(), 0);
}
}
