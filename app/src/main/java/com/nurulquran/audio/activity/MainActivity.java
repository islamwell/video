package com.nurulquran.audio.activity;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.nurulquran.audio.R;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.fragment.AlbumFragment;
import com.nurulquran.audio.fragment.ListSongsFragment;
import com.nurulquran.audio.fragment.SearchFragment;
import com.nurulquran.audio.gcm.Args;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.network.ControllerRequest;
import com.nurulquran.audio.object.Playlist;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.service.MusicService;
import com.nurulquran.audio.service.MusicService.ServiceBinder;
import com.nurulquran.audio.service.PlayerListener;
import com.nurulquran.audio.slidingmenu.SlidingMenu;
import com.nurulquran.audio.util.Logger;
import com.nurulquran.audio.util.NetworkUtil;
import com.nurulquran.audio.widget.AutoBgButton;
import com.splunk.mint.Mint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.ACTION_VIEW;

public class MainActivity extends FragmentActivity implements OnClickListener {
    public static final int MOST_FAVOURITE = 0;
    public static final int LASTEST = 1;
    public static final int SERIES = 2;
    public static final int CATEGORY_MUSIC = 3;
    public static final int PLAYLIST = 4;
    public static final int SEARCH = 5;
    public static final int RADIO = 6;
    public static final int MULTIPLE_YOUR_REWARD = 7;
    public static final int MY_DOWNLOAD = 8;
    public static final int ABOUT = 9;
    public static final int EXIT_APP = 10;

    public static final int LIST_SONG_FRAGMENT = 0;
    public static final int CATEGORY_MUSIC_FRAGMENT = 1;
    public static final int PLAYLIST_FRAGMENT = 2;
    public static final int SEARCH_FRAGMENT = 3;
    public static final int RADIO_FRAGMENT = 4;
    public static final int SETTING_FRAGMENT = 5;
    public static final int MULTIPLE_YOUR_WARD_FRAGMENT = 6;
    public static final int MY_DOWNLOAD_FRAGMENT = 7;
    public static final int ABOUT_FRAGMENT = 8;
    public static final int ALBUM_FRAGMENT = 9;

    public static final int FROM_LIST_SONG = 0;
    public static final int FROM_NOTICATION = 10000;
    public static final int FROM_SEARCH = 2;
    public static final int FROM_OTHER = 3;

    public static final int NOTIFICATION_ID = 231109;
    public static final int PICK_CONTACT_REQUEST = 120;

    private FragmentManager fm;
    private Fragment[] arrayFragments;
    public SlidingMenu menu;
    public static AutoBgButton btnPlayFooter;
    private AutoBgButton btnPreviousFooter, btnNextFooter;
    private View layoutPlayerFooter;
    private TextView lblSongNameFooter, lblArtistFooter;

    private TextView lblTopChart, lblAllSongs, lblAlbum, lblCategoryMusic,
            lblPlaylist, lblSearch, lblMultipleYourReward, lblAbout, lblExitApp, lblRadio, llMyDownLoad,lblCleatCache;

    private LinearLayout llAdview;

    public int currentFragment;
    public static int toMusicPlayer;
    public static boolean isTapOnFooter;
    public Playlist currentPlaylist;


    public List<Song> listNominations;
    public List<Song> listTopWeek;

    public DatabaseUtility databaseUtility;
    public MusicService mService;
    private Intent intentService;
    private NetworkImageView mImageBanner;
    private ImageLoader imageLoader;
    private Timer mTimer;
    private static final long PERIOD_BANNER = 30000;
    private int pos = 0;
    private boolean mIsPause;

    public static boolean isPlaylist = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServiceBinder binder = (ServiceBinder) service;
            mService = binder.getService();
            if (GlobalValue.currentMenu != RADIO) {
                setVisibilityFooter();
            }

            mService.setListener(new PlayerListener() {
                @Override
                public void onSeekChanged(int maxProgress, String lengthTime, String currentTime, int progress) {

                }

                @Override
                public void onChangeSong(int indexSong) {
                    lblSongNameFooter.setText(GlobalValue.getCurrentSong()
                            .getName());
                    if (GlobalValue.getCurrentSong()
                            .getDescription() != null) {
                        lblArtistFooter.setText(Html.fromHtml(GlobalValue.getCurrentSong()
                                .getDescription()));
                    } else {
                        lblArtistFooter.setText("");
                    }

                    setButtonPlay();
                }

                @Override
                public void OnMusicPrepared() {
                    if (GlobalValue.currentMenu != RADIO) {
                        setVisibilityFooter();
                    }
                }
            });
            GlobalValue.currentMusicService = mService;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//cactch error
        Mint.initAndStartSession(MainActivity.this, "124b3602");
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
/////////////////////////////////////////////////////////////////////

        NetworkUtil.enableStrictMode();
        initList();
        databaseUtility = new DatabaseUtility(this);
        setContentView(R.layout.activity_main);
        ControllerRequest.getInstance().trackEvent("Install", "Install App", "");
        initService();
        initMenu();
        initUI();
        initControl();
        initFragment();
        isTapOnFooter = false;
        setSelect(GlobalValue.currentMenu);
        toMusicPlayer = MainActivity.FROM_OTHER;
        ListSongsFragment.isShowing = false;
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelNotification();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            e.printStackTrace();
            cancelNotification();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mService != null) {
            if (GlobalValue.currentMenu != RADIO) {
                setVisibilityFooter();
            }
            setButtonPlay();
        }
        openPlayerFromNotification(getIntent());
        bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        openPlayerFromNotification(intent);

    }

    private void openPlayerFromNotification(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            if (intent.hasExtra(Args.NOTIFICATION)) {
                if (bundle.getBoolean(Args.NOTIFICATION)) {
                    if (intent.hasExtra("Song")) {
                        if (GlobalValue.listSongPlay == null) {
                            GlobalValue.constructor(MainActivity.this);
                        }
                        Intent intentPlayer = new Intent(getApplicationContext(), PlayerActivity.class);
                        Song song = intent.getParcelableExtra("Song");
                        MainActivity.toMusicPlayer = MainActivity.FROM_NOTICATION;
                        GlobalValue.listSongPlay.clear();
                        GlobalValue.listSongPlay.add(song);
                        GlobalValue.currentSongPlay = 0;
                        intentPlayer.putExtra("Song", song);
                        startActivity(intentPlayer);
                    }
                    intent.putExtra(Args.NOTIFICATION, false);
                }

            }

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Static variable do not persist after long time of idle -> Store those
        try {
            outState.putInt("currentSongPlay", GlobalValue.currentSongPlay);
            outState.putInt("currentMenu", GlobalValue.currentMenu);
            outState.putInt("currentCategoryId", GlobalValue.currentCategoryId);
            outState.putInt("currentParentCategoryId", GlobalValue.currentParentCategoryId);
            outState.putString("currentCategoryName",
                    GlobalValue.currentCategoryName);
            outState.putInt("currentAlbumId", GlobalValue.currentAlbumId);
            outState.putString("currentAlbumName", GlobalValue.currentAlbumName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            try {

                GlobalValue.listSongPlay = databaseUtility.getAllFavorite();
                GlobalValue.currentSongPlay = (int) savedInstanceState
                        .getInt("currentSongPlay");
                GlobalValue.currentMenu = (int) savedInstanceState
                        .getInt("currentMenu");
                GlobalValue.currentCategoryId = (int) savedInstanceState
                        .getInt("currentCategoryId");
                GlobalValue.currentParentCategoryId = (int) savedInstanceState
                        .getInt("currentParentCategoryId");
                GlobalValue.currentCategoryName = (String) savedInstanceState
                        .getString("currentCategoryName");
                GlobalValue.currentAlbumId = (int) savedInstanceState
                        .getInt("currentAlbumId");
                GlobalValue.currentAlbumName = (String) savedInstanceState
                        .getString("currentAlbumName");
                cancelNotification();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setVisibilityFooter() {
        try {
            if (mService.isPause() || mService.isPlay()) {
                layoutPlayerFooter.setVisibility(View.VISIBLE);
                lblSongNameFooter.setText(GlobalValue.getCurrentSong()
                        .getName());
                if (GlobalValue.getCurrentSong()
                        .getDescription() != null) {
                    lblArtistFooter.setText(Html.fromHtml(GlobalValue.getCurrentSong()
                            .getDescription()));
                } else {
                    lblArtistFooter.setText("");
                }
                setButtonPlay();
            } else {
                layoutPlayerFooter.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            layoutPlayerFooter.setVisibility(View.GONE);
        }
    }

    public void hideMediaFooter() {
        layoutPlayerFooter.setVisibility(View.GONE);
    }

    public void showMediaFooter() {
        if (mService.isPlay() || mService.isPause()) {
            layoutPlayerFooter.setVisibility(View.VISIBLE);
        }
    }

    private void initService() {
        intentService = new Intent(this, MusicService.class);
        startService(intentService);
        bindService(intentService, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void initUI() {
        btnPreviousFooter = (AutoBgButton) findViewById(R.id.btnPreviousFooter);
        btnPlayFooter = (AutoBgButton) findViewById(R.id.btnPlayFooter);
        btnNextFooter = (AutoBgButton) findViewById(R.id.btnNextFooter);
        layoutPlayerFooter = (LinearLayout) findViewById(R.id.layoutPlayerFooterMain);
        lblSongNameFooter = (TextView) findViewById(R.id.lblSongNameFooter);
        lblArtistFooter = (TextView) findViewById(R.id.lblArtistFooter);
        lblTopChart = (TextView) menu.findViewById(R.id.lblTopChart);
        lblAllSongs = (TextView) menu.findViewById(R.id.lblAllSong);
        lblAlbum = (TextView) menu.findViewById(R.id.lblNominations);
        lblCategoryMusic = (TextView) menu.findViewById(R.id.lblCategoryMusic);
        lblPlaylist = (TextView) menu.findViewById(R.id.lblPlaylist);
        lblSearch = (TextView) menu.findViewById(R.id.lblSearch);
        lblMultipleYourReward = (TextView) menu.findViewById(R.id.lblMultipleYourReward);
        lblAbout = (TextView) menu.findViewById(R.id.lblAbout);
        llMyDownLoad = (TextView) menu.findViewById(R.id.lblMyDownLoad);
        lblExitApp = (TextView) menu.findViewById(R.id.lblExitApp);
        llAdview = (LinearLayout) findViewById(R.id.adMod);
        mImageBanner = (NetworkImageView) findViewById(R.id.nwImageBanner);
        imageLoader = ControllerRequest.getInstance().getImageLoader();
        lblRadio = (TextView) findViewById(R.id.lblRadio);
        lblCleatCache=(TextView)findViewById(R.id.lblClear);
    }

    private void initControl() {
        btnPreviousFooter.setOnClickListener(this);
        btnPlayFooter.setOnClickListener(this);
        btnNextFooter.setOnClickListener(this);
        layoutPlayerFooter.setOnClickListener(this);
        lblTopChart.setOnClickListener(this);
        lblAllSongs.setOnClickListener(this);
        lblAlbum.setOnClickListener(this);
        lblCategoryMusic.setOnClickListener(this);
        lblPlaylist.setOnClickListener(this);
        lblSearch.setOnClickListener(this);
        lblRadio.setOnClickListener(this);
        lblMultipleYourReward.setOnClickListener(this);
        llMyDownLoad.setOnClickListener(this);
        lblAbout.setOnClickListener(this);
        lblExitApp.setOnClickListener(this);
        lblSongNameFooter.setSelected(true);
        lblArtistFooter.setSelected(true);
        lblCleatCache.setOnClickListener(this);
        mImageBanner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPageBanner();
            }
        });
        loadImageBanner();
    }

    /**
     * load image for banner with period 30s will change image different
     */
    private void loadImageBanner() {
        ModelManager.loadBanner(getApplicationContext(), new ModelManagerListener() {
            @Override
            public void onError(VolleyError error) {

            }

            @Override
            public void onSuccess(String json) {
                CommonParser.parseBanner(json);
                Collections.shuffle(GlobalValue.mListBanner);
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (pos == GlobalValue.mListBanner.size()) {
                                    pos = 0;
                                }
                                if (pos < GlobalValue.mListBanner.size()) {
                                    mImageBanner.setImageUrl(GlobalValue.mListBanner.get(pos).getImage(), imageLoader);
                                    pos++;

                                }

                            }
                        });

                    }
                }, 0, PERIOD_BANNER);

            }
        });

    }

    private void gotoPageBanner() {

        if ((pos - 1) >= 0) {
            String url = GlobalValue.mListBanner.get(pos - 1).getUrl();
            if (checkUrl(url)) {

            } else {
                url = "http://" + url;
            }

            Intent intent = new Intent(ACTION_VIEW, Uri.parse(url));
            startActivity(intent);

        }


    }

    protected boolean checkUrl(String url) {
        return (url.startsWith("http://") || url.startsWith("https://"));
    }

    private void initFragment() {
        fm = getSupportFragmentManager();
        arrayFragments = new Fragment[10];
        arrayFragments[
                LIST_SONG_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentListSongs);
        arrayFragments[CATEGORY_MUSIC_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentCategoryMusic);
        arrayFragments[PLAYLIST_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentPlaylist);
        arrayFragments[SEARCH_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentSearch);
        arrayFragments[RADIO_FRAGMENT] = fm.findFragmentById(R.id.frRadio);
        arrayFragments[SETTING_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentSetting);
        arrayFragments[MULTIPLE_YOUR_WARD_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentMultipleYourReward);
        arrayFragments[MY_DOWNLOAD_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentMyDownLoad);
        arrayFragments[ABOUT_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentAbout);
        arrayFragments[ALBUM_FRAGMENT] = fm
                .findFragmentById(R.id.fragmentAlbum);

        FragmentTransaction transaction = fm.beginTransaction();
        for (Fragment fragment : arrayFragments) {
            transaction.hide(fragment);
        }
        transaction.commit();
    }

    private void showFragment(int fragmentIndex) {
        currentFragment = fragmentIndex;
        FragmentTransaction transaction = fm.beginTransaction();
        for (Fragment fragment : arrayFragments) {
            transaction.hide(fragment);

        }
        transaction.show(arrayFragments[fragmentIndex]);
        transaction.commit();
        Logger.e(fragmentIndex);
    }

    private void initList() {
        listNominations = new ArrayList<Song>();
        listTopWeek = new ArrayList<Song>();
    }

    private void initMenu() {
        menu = new SlidingMenu(this);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidthRes(R.dimen.shadow_width);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setMenu(R.layout.layout_menu);
        menu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
            @Override
            public void onOpen() {
                SearchFragment searchFragment = (SearchFragment) arrayFragments[SEARCH_FRAGMENT];
                searchFragment.hideSoft();
            }
        });

    }

    public void hideBannerAd() {
        llAdview.setVisibility(View.GONE);
    }

    public void showBannerAd() {
        llAdview.post(new Runnable() {
            @Override
            public void run() {
                llAdview.setVisibility(View.VISIBLE);

            }
        });

    }

    public void gotoFragment(int fragment) {
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left,
                R.anim.slide_out_left);
        transaction.show(arrayFragments[fragment]);

        transaction.hide(arrayFragments[currentFragment]);
        transaction.commit();
        currentFragment = fragment;
    }


    public void backFragment(int fragment) {
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.setCustomAnimations(R.anim.slide_in_right,
                R.anim.slide_out_right);
        transaction.show(arrayFragments[fragment]);
        transaction.hide(arrayFragments[currentFragment]);
        transaction.commit();
        currentFragment = fragment;
    }

    private void setSelect(int select) {
        ListSongsFragment.isShowing = false;
        AlbumFragment.isShowing = false;
        GlobalValue.currentMenu = select;
        switch (select) {
            case MOST_FAVOURITE:
                lblTopChart.setBackgroundResource(R.drawable.bg_item_menu_select);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                showFragment(LIST_SONG_FRAGMENT);
                showBannerAd();
                break;

            case LASTEST:
                showBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundResource(R.drawable.bg_item_menu_select);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                showFragment(LIST_SONG_FRAGMENT);

                break;

            case SERIES:
                hideBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundResource(R.drawable.bg_item_menu_select);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                showFragment(ALBUM_FRAGMENT);

                break;

            case CATEGORY_MUSIC:
                hideBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic
                        .setBackgroundResource(R.drawable.bg_item_menu_select);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                showFragment(CATEGORY_MUSIC_FRAGMENT);

                break;

            case PLAYLIST:
                showBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundResource(R.drawable.bg_item_menu_select);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                showFragment(PLAYLIST_FRAGMENT);

                break;

            case SEARCH:
                showBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundResource(R.drawable.bg_item_menu_select);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                ((SearchFragment) arrayFragments[SEARCH_FRAGMENT]).keyword = "";
                showFragment(SEARCH_FRAGMENT);

                break;
            case RADIO:
                hideBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundResource(R.drawable.bg_item_menu_select);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                showFragment(RADIO_FRAGMENT);
                break;
            case ABOUT:
                hideBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundResource(R.drawable.bg_item_menu_select);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                showFragment(ABOUT_FRAGMENT);

                break;

            case MULTIPLE_YOUR_REWARD:
                hideBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundResource(R.drawable.bg_item_menu_select);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                showFragment(MULTIPLE_YOUR_WARD_FRAGMENT);

                break;
            case MY_DOWNLOAD:
                hideBannerAd();
                lblTopChart.setBackgroundColor(Color.TRANSPARENT);
                lblAllSongs.setBackgroundColor(Color.TRANSPARENT);
                lblAlbum.setBackgroundColor(Color.TRANSPARENT);
                lblCategoryMusic.setBackgroundColor(Color.TRANSPARENT);
                lblPlaylist.setBackgroundColor(Color.TRANSPARENT);
                lblSearch.setBackgroundColor(Color.TRANSPARENT);
                lblRadio.setBackgroundColor(Color.TRANSPARENT);
                lblMultipleYourReward.setBackgroundColor(Color.TRANSPARENT);
                lblAbout.setBackgroundColor(Color.TRANSPARENT);
                lblExitApp.setBackgroundColor(Color.TRANSPARENT);
                llMyDownLoad.setBackgroundResource(R.drawable.bg_item_menu_select);
                showFragment(MY_DOWNLOAD_FRAGMENT);
                break;
            case EXIT_APP:
                return;
        }
        menu.showContent();
    }

    public void setButtonPlay() {
        if (mService.isPause()) {
            mIsPause = true;
            btnPlayFooter.setBackgroundResource(R.drawable.bg_btn_play_small);
        } else {
            mIsPause = false;
            btnPlayFooter.setBackgroundResource(R.drawable.bg_btn_pause_small);
        }
    }

    public void cancelNotification() {
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPreviousFooter:
                onClickPreviousFooter();

                break;
            case R.id.btnPlayFooter:
                onClickPlayFooter();

                break;
            case R.id.btnNextFooter:
                onClickNextFooter();


                break;
            case R.id.layoutPlayerFooterMain:
                onClickPlayerFooter();

                break;
            case R.id.lblTopChart:
                isPlaylist=false;
                onClickTopChart();
                break;
            case R.id.lblAllSong:
                isPlaylist=false;
                onClickAllSongs();
                break;
            case R.id.lblNominations:
                isPlaylist=false;
                onClickAlbum();
                break;
            case R.id.lblCategoryMusic:
                isPlaylist=false;
                onClickCategoryMusic();
                break;
            case R.id.lblPlaylist:
                isPlaylist=true;
                onClickPlaylist();
                break;
            case R.id.lblSearch:
                isPlaylist=false;
                onClickSearch();
                break;
            case R.id.lblRadio:
                isPlaylist=false;
                onClickRadio();
                break;
            case R.id.lblMultipleYourReward:
                isPlaylist=false;
                onClickGoodApp();
                break;
            case R.id.lblMyDownLoad:
                isPlaylist=false;
                onClickMyDownload();
                break;
            case R.id.lblAbout:
                isPlaylist=false;
                onClickAbout();
                break;
            case R.id.lblExitApp:
                isPlaylist=false;
                onClickExitApp();
                break;
            case R.id.lblClear:
                isPlaylist=false;
                File fileOrDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                        + getString(R.string.app_name) );
                if (fileOrDirectory.exists()) {
                    String deleteCmd = "rm -r " + fileOrDirectory.toString();
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec(deleteCmd);
                    } catch (IOException e) { }
                }
                break;
        }
    }

    private void onClickClearCache() {
        File fileOrDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + getString(R.string.app_name) );
        if (fileOrDirectory.exists()) {
            String deleteCmd = "rm -r " + fileOrDirectory.toString();
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) { }
        }
    }

    private void onClickPreviousFooter() {
        mService.backSongByOnClick();
    }

    private void onClickPlayFooter() {
        mService.playOrPauseMusic();
        setButtonPlay();
    }

    private void onClickNextFooter() {
        mService.nextSongByOnClick();
    }

    private void onClickPlayerFooter() {
        toMusicPlayer = currentFragment;
        isTapOnFooter = true;
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        startActivityForResult(intent, PlayerActivity.REQUEST_CODE);
    }

    private void onClickTopChart() {
        setSelect(MOST_FAVOURITE);
    }

    private void onClickAllSongs() {
        setSelect(LASTEST);
    }

    private void onClickAlbum() {
        setSelect(SERIES);
    }

    private void onClickCategoryMusic() {
        setSelect(CATEGORY_MUSIC);
    }

    private void onClickPlaylist() {
        setSelect(PLAYLIST);
    }

    private void onClickSearch() {
        setSelect(SEARCH);
    }

    private void onClickRadio() {
        setSelect(RADIO);
    }

    private void onClickGoodApp() {
        setSelect(MULTIPLE_YOUR_REWARD);
    }

    private void onClickMyDownload() {
        setSelect(MY_DOWNLOAD);
    }

    private void onClickAbout() {
        setSelect(ABOUT);
    }

    private void onClickExitApp() {
        showQuitDialog();
    }

    @Override
    public void onBackPressed() {
        if (menu.isMenuShowing()) {
            menu.showContent();
        } else {
            switch (currentFragment) {
                case CATEGORY_MUSIC_FRAGMENT:
                    quitApp();
                    break;

                case LIST_SONG_FRAGMENT:
                    if (GlobalValue.currentMenu == CATEGORY_MUSIC) {
                        backFragment(CATEGORY_MUSIC_FRAGMENT);
                    } else if (GlobalValue.currentMenu == PLAYLIST) {
                        backFragment(PLAYLIST_FRAGMENT);
                    } else if (GlobalValue.currentMenu == SERIES) {
                        backFragment(ALBUM_FRAGMENT);
                    } else {
                        quitApp();
                    }
                    break;

                default:
                    quitApp();
                    break;
            }
        }
    }

    private void quitApp() {
        showQuitDialog();
    }

    private void showQuitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.msgQuitApp)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                dialog.dismiss();
                                stopService(intentService);
                                cancelNotification();
                                finish();
                                System.exit(0);
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create().show();
    }


}
