package com.nurulquran.audio.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.TimedText;
import android.net.Uri;
import android.net.rtp.AudioStream;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.activity.SplashActivity;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.database.MySharePreferences;
import com.nurulquran.audio.interfaces.MediaNotification;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.network.ControllerRequest;
import com.nurulquran.audio.object.Radio;
import com.nurulquran.audio.service.MusicService;
import com.nurulquran.audio.slidingmenu.SlidingMenu;
import com.nurulquran.audio.util.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Intent.ACTION_VIEW;


/**
 * Created by phamtuan on 06/05/2016.
 */
public class RadioFragment extends BaseFragment implements View.OnClickListener {
    private View view;
    private WebView wvRadio;
    private ImageView btnPlayRadio;
    private TextView tvTimeRadio;
    private SeekBar mSeekRadio;
    private MediaPlayer mMediaPlayer;
    private int currentPosition = 0;
    private boolean isLiveStreamPlaying = true;
    private boolean isMusicPlaying;
    private NetworkImageView ivBigBanner;
    private ImageLoader mImageLoader;
    private Timer mTimer;
    private int pos;
    private MainActivity mainActivity;
    private boolean ringPhone = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager mgr;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflaterView(inflater, container, R.layout.fragment_radio_livestreams);
        initRadioLiveStream();
        setButtonMenu(view);
        initUIBase(view);
        setHeaderTitle(R.string.radio);
        checkCall();
        if (NetworkUtil.checkNetworkAvailable(getActivity())) {
            getRadio();
        }
        return view;
    }

    private View inflaterView(LayoutInflater inflater, ViewGroup container, int resId) {
        View view = inflater.inflate(resId, container, false);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getMainActivity().menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            getMainActivity().hideMediaFooter();
            getMainActivity().hideBannerAd();
            if (radio != null) {
                if (getMainActivity().mService != null) {
                    if (getMainActivity().mService.isPlay()) {
                        getMainActivity().mService.pauseMusic();
                    }
                }
                if (mMediaPlayer == null) {
                    playLiveStream();
                } else if (!mMediaPlayer.isPlaying()) {
                    resumeLiveStream();
                }
            } else {
                if (getMainActivity().mService != null) {
                    if (getMainActivity().mService.isPlay()) {
                        getMainActivity().mService.pauseMusic();
                    }
                }

                if (NetworkUtil.checkNetworkAvailable(getActivity())) {
                    loadImageBanner();
                    getRadio();
                }
            }

        } else {
            if (radio != null) {
                if (getMainActivity().mService != null) {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        pauseLiveStream();
                    }
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (GlobalValue.currentMenu == MainActivity.RADIO) {
            if (radio != null) {
                if (getMainActivity().mService != null) {
                    getMainActivity().mService.pauseMusic();
                }
                if (isLiveStreamPlaying && isMusicPlaying == true) {
                    resumeLiveStream();
                }
            }
            getMainActivity().hideMediaFooter();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (radio != null) {
            stopLiveStream();
        }

    }

    private void initRadioLiveStream() {
        mImageLoader = ControllerRequest.getInstance().getImageLoader();
        mTimer = new Timer();
        ivBigBanner = (NetworkImageView) view.findViewById(R.id.ivBigBanner);
        loadImageBanner();
        btnPlayRadio = (ImageView) view.findViewById(R.id.btnPlayRadio);
        btnPlayRadio.setOnClickListener(this);
        tvTimeRadio = (TextView) view.findViewById(R.id.tvTimeRadio);
        mSeekRadio = (SeekBar) view.findViewById(R.id.seekRadio);
        ivBigBanner.setOnClickListener(this);
        mSeekRadio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(currentPosition);
            }
        });


    }

    public void showDataBigBanner() {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (pos == GlobalValue.mListBigBanner.size()) {
                                                        pos = 0;
                                                    }
                                                    if (pos < GlobalValue.mListBigBanner.size()) {
                                                        ivBigBanner.setImageUrl(GlobalValue.mListBigBanner.get(pos).getImage(), mImageLoader);
                                                        pos++;
                                                    }
                                                }
                                            }
                );

            }
        }, 0, 30000);
    }

    private void clickBigBanner() {
        if ((pos - 1) >= 0) {
            String url = GlobalValue.mListBigBanner.get((pos - 1)).getUrl();
            if (!url.equals("")) {
                if (checkUrl(url)) {

                } else {
                    url = "http://" + url;
                }
                pauseLiveStream();
                Intent intent = new Intent(ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }
    }

    /*
    play live stream when enter radio
     */
    private void playLiveStream() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setVolume(100, 100);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            String url = radio.getmLinkLiveStream();
            if (url != null && !url.equals("")) {
                mMediaPlayer.setDataSource(getActivity(), Uri.parse(url));
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (GlobalValue.currentMenu == MainActivity.RADIO) {
                            mMediaPlayer.start();
                            btnPlayRadio.setImageResource(R.drawable.ic_pause);
                            mSeekRadio.setMax(mMediaPlayer.getDuration());
                            mSeekRadio.setProgress(0);
                            tvTimeRadio.setText("Live");
                        }
                    }
                });

                mMediaPlayer.prepareAsync();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseLiveStream() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            currentPosition = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.pause();
            btnPlayRadio.setImageResource(R.drawable.ic_play);
        }
    }

    private void resumeLiveStream() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            btnPlayRadio.setImageResource(R.drawable.ic_pause);

        }
    }

    private void stopLiveStream() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPlayRadio:
                if (isLiveStreamPlaying) {
                    pauseLiveStream();
                    isLiveStreamPlaying = !isLiveStreamPlaying;
                } else {
                    resumeLiveStream();
                    if (getMainActivity().mService != null) {
                        if (getMainActivity().mService.isPlay()) {
                            getMainActivity().mService.pauseMusic();
                        }
                    }
                    isLiveStreamPlaying = !isLiveStreamPlaying;
                }
                break;
            case R.id.ivBigBanner:
                clickBigBanner();
                break;
        }
    }


    public static final class ParserM3UToURL {

        public static final String parse(String urlM3u) {

            String ligne = null;

            try {
                URL urlPage = new URL(urlM3u);
                HttpURLConnection connection = (HttpURLConnection) urlPage.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuffer stringBuffer = new StringBuffer();

                while ((ligne = bufferedReader.readLine()) != null) {
                    if (ligne.contains("http")) {
                        connection.disconnect();
                        bufferedReader.close();
                        inputStream.close();
                        return ligne;
                    }
                    stringBuffer.append(ligne);
                }

                connection.disconnect();
                bufferedReader.close();
                inputStream.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void loadImageBanner() {
        ModelManager.loadBanner(getActivity(), new ModelManagerListener() {
                    @Override
                    public void onError(VolleyError error) {

                    }

                    @Override
                    public void onSuccess(String json) {
                        try {
                            JSONObject jsonObject = new JSONObject(json.toString());
                            if (jsonObject.getString(WebserviceApi.KEY_STATUS).equalsIgnoreCase(WebserviceApi.KEY_SUCCESS)) {
                                CommonParser.parseBanner(json);
                                showDataBigBanner();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
        );
    }

    private void getRadio() {
        ModelManager.loadRadio(getActivity(), new ModelManagerListener() {
            @Override
            public void onError(VolleyError error) {
                error.printStackTrace();
            }

            @Override
            public void onSuccess(String json) {
                MySharePreferences.getInstance().getRadioStore(getActivity());
                MySharePreferences.getInstance().saveRadio(json);
                radio = CommonParser.parseRadio(json);
                if (radio != null) {
                    playLiveStream();
                } else {
                    btnPlayRadio.setImageResource(R.drawable.ic_play);
                }
            }
        });
    }

    private void checkCall() {
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (mMediaPlayer != null)
                        if (GlobalValue.currentMenu == MainActivity.RADIO)
                            pauseLiveStream();
                    ringPhone = true;
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    if (ringPhone == true) {
                        if (mMediaPlayer != null)
                            if (GlobalValue.currentMenu == MainActivity.RADIO)
                                resumeLiveStream();
                        ringPhone = false;
                    }
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (mMediaPlayer != null)
                        if (GlobalValue.currentMenu == MainActivity.RADIO)
                            pauseLiveStream();
                    ringPhone = true;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        mgr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }
}
