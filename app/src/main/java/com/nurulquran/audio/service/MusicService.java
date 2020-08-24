package com.nurulquran.audio.service;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.activity.PlayerActivity;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.interfaces.MediaNotification;
import com.nurulquran.audio.modelmanager.ModelManager;
import com.nurulquran.audio.modelmanager.ModelManagerListener;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {

    private static final String TAG = "MusicService";

    private final IBinder mBinder = new ServiceBinder();
    private List<Song> listSongs;
    private MediaPlayer mPlayer;
    private int length;
    private int lengthSong;
    private PlayerListener listener;
    public boolean isPause;
    private boolean isPreparing;
    private boolean isUpdatingSeek;
    private boolean isShuffle;
    private boolean isRepeat;
    private Handler mHandler;
    private boolean ringPhone = false;

    private Handler handler = new Handler();
    private static Runnable savedR = null;
    private static final int DELAY = 1000;
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            // Log.e("MusicService", "handle.run");
            updateSeekProgress();
        }
    };

    public class ServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isPause = false;
        isPreparing = false;
        setNewPlayer();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        cancelNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler = new Handler();
        return START_STICKY;
    }

    public void updateSeekProgress() {
        try {
            listener.onSeekChanged(lengthSong, getLengSong(),
                    getTime(mPlayer.getCurrentPosition()),
                    mPlayer.getCurrentPosition());
            handler.postDelayed(r, DELAY);
        } catch (Exception e) {

            handler.postDelayed(r, DELAY);
        }
    }

    private void updateSeekProgressWithPlayingCheck() {
        try {
            listener.onSeekChanged(lengthSong, getLengSong(),
                    getTime(mPlayer.getCurrentPosition()),
                    mPlayer.getCurrentPosition());

            if (isPlay())
                handler.postDelayed(r, DELAY);
        } catch (Exception e) {
            if (isPlay())
                handler.postDelayed(r, DELAY);
        }
    }

    public boolean isPause() {
        return isPause;
    }

    public boolean isPreparing() {
        return isPreparing;
    }

    public void setPause(boolean isPause) {
        this.isPause = isPause;
    }

    public void changeStatePause() {
        isPause = !isPause;
    }

    public boolean isPlay() {
        try {
            return mPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean isShuffle) {
        this.isShuffle = isShuffle;
    }

    public void setListener(PlayerListener listener) {
        this.listener = listener;
    }

    public void setListSongs(List<Song> listSongs) {
        if (this.listSongs == null) {
            this.listSongs = new ArrayList<Song>();
        }
        this.listSongs.clear();
        this.listSongs.addAll(listSongs);
    }

    public List<Song> getListSongs() {
        return listSongs;
    }

    private void plusNewListen() {
        String getUrl = WebserviceApi.getAddNewView(getBaseContext()) + "?id="
                + GlobalValue.getCurrentSong().getId();
        ModelManager.sendGetRequest(getApplicationContext(), getUrl, null, false, new ModelManagerListener() {
            @Override
            public void onError(VolleyError error) {

            }

            @Override
            public void onSuccess(String json) {

            }
        });
    }

    private void setNewPlayer() {
        mPlayer = new MediaPlayer();
        mPlayer.setVolume(100, 100);
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        checkCall();
        mPlayer.setOnErrorListener(new OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // onMediaPlayerError(mPlayer, what, extra);
                isPreparing = false;
                int newPosition;
                if (isShuffle) {
                    newPosition = new Random().nextInt(listSongs.size());
                } else {
                    if (isRepeat)
                        newPosition = GlobalValue.currentSongPlay;
                    else if (GlobalValue.currentSongPlay < listSongs.size() - 1) {
                        newPosition = GlobalValue.currentSongPlay + 1;
                    } else {
                        newPosition = 0;

                    }
                }
                mHandler.post(new ToastRunnable(getString(R.string.song_error) + " " + listSongs.get(newPosition).getName() + " will be play in 3s"));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nextSong();
                    }
                }, 3000);
                return true;
            }
        });

        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("musicService", "progress : completed");
                addListen();
                nextSong();

            }
        });

        mPlayer.setOnPreparedListener(new OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                lengthSong = mPlayer.getDuration();
                isPreparing = false;
                mPlayer.start();
//                plusNewListen();
                listener.OnMusicPrepared();
                if (!isUpdatingSeek) {
                    isUpdatingSeek = true;
                    updateSeekProgress();
                }
            }
        });

    }


    public void startMusic(int index) {
        Log.e("MusicService", "Start");
        if (PlayerActivity.btnPlay != null) {
            PlayerActivity.btnPlay.setBackgroundResource(R.drawable.ic_pause);
        }
        isPause = false;
        GlobalValue.currentSongPlay = index;
        if (PlayerActivity.lblTopHeader != null) {
            PlayerActivity.lblTopHeader.setText(listSongs.get(index).getName());
        }

        try {
            mPlayer.reset();
        } catch (Exception e) {
            // e.printStackTrace();
            setNewPlayer();
        }
        try {
            if (listSongs.get(index).getUrl() == null) {
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(listSongs.get(index).getUrl());
            } else {
                mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mPlayer.setDataSource(listSongs.get(index).getUrl());
            }
            mPlayer.prepareAsync();
            listener.onChangeSong(index);
            sendNotification();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void playOrPauseMusic() {
        if (isPause) {
            resumeMusic();
        } else {
            pauseMusic();
        }
    }

    public void pauseMusic() {
        pauseMusic(true);
    }

    public void pauseMusic(boolean doCancelNotification) {
        if (mPlayer.isPlaying()) {
            savedR = r;
            handler.removeCallbacks(r);
            isPause = true;
            length = mPlayer.getCurrentPosition();
            mPlayer.pause();
            isUpdatingSeek = false;
            PlayerActivity.btnPlay.setBackgroundResource(R.drawable.ic_play);
            MainActivity.btnPlayFooter.setBackgroundResource(R.drawable.bg_btn_play_small);
        }
        if (doCancelNotification)
            cancelNotification();
        else
            sendNotification();
        //
    }

    public void resumeMusic() {
        if (isPause) {
            r = savedR;
            handler.postDelayed(r, DELAY);
            mPlayer.seekTo(length);
            mPlayer.start();
            if (PlayerActivity.btnPlay != null) {
                PlayerActivity.btnPlay
                        .setBackgroundResource(R.drawable.ic_pause);
                MainActivity.btnPlayFooter
                        .setBackgroundResource(R.drawable.bg_btn_pause_small);
            }
            isPause = false;
            sendNotification();
            if (!isUpdatingSeek) {
                isUpdatingSeek = true;
                updateSeekProgress();
            }
        }
    }

    public void seekTo(int progress) {
        mPlayer.seekTo(progress);
        length = progress;
        updateSeekProgressWithPlayingCheck();
        if (progress >= lengthSong) {
            nextSongByOnClick();
        }
    }

    public void backSong() {
        int newPosition;
        if (isShuffle) {
            newPosition = new Random().nextInt(listSongs.size());
        } else {
            if (isRepeat)
                newPosition = GlobalValue.currentSongPlay;
            else if (GlobalValue.currentSongPlay > 0) {
                newPosition = GlobalValue.currentSongPlay - 1;
            } else {
                newPosition = listSongs.size() - 1;
            }
        }
        startMusic(newPosition);
        listener.onChangeSong(newPosition);
    }

    public void backSongByOnClick() {
        backSong();
    }

    public void nextSong() {
        int newPosition;
        if (isShuffle) {
            newPosition = new Random().nextInt(listSongs.size());
        } else {
            if (isRepeat)
                newPosition = GlobalValue.currentSongPlay;
            else if (GlobalValue.currentSongPlay < listSongs.size() - 1) {
                newPosition = GlobalValue.currentSongPlay + 1;
            } else {
                newPosition = 0;

            }
        }
        startMusic(newPosition);
        listener.onChangeSong(newPosition);
    }

    public void nextSongByOnClick() {
        nextSong();
    }

    public String getLengSong() {
        return getTime(lengthSong);
    }

    @SuppressLint("DefaultLocale")
    public String getTime(int millis) {
        long second = (millis / 1000) % 60;
        long minute = millis / (1000 * 60);
        return String.format("%02d:%02d", minute, second);
    }

    private void checkCall() {
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (listSongs != null) {
                        if (mPlayer != null)

                            if (mPlayer.isPlaying())
                                pauseMusic();
                    }
                    ringPhone = true;

                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    if (ringPhone == true) {
                        if (listSongs != null) {
                            if (GlobalValue.currentMenu != MainActivity.RADIO) {
                                if (mPlayer != null)
                                    if (!mPlayer.isPlaying()) {
                                       resumeMusic();
                                    }
                            } else {
                                pauseMusic();
                            }

                        }
                        ringPhone = false;
                    }
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (listSongs != null) {
                        if (mPlayer != null)
                            if (mPlayer.isPlaying())
                                pauseMusic();
                    }
                    ringPhone = true;
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
        TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private void sendNotification() {
        Song song = GlobalValue.getCurrentSong();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.notify_icon);
        // load custom view
        RemoteViews rmView = new RemoteViews(getApplicationContext()
                .getPackageName(), R.layout.layout_custom_notification);
        mBuilder.setContent(rmView);
        // set song name
        rmView.setTextViewText(R.id.lbl_song_name, song.getName());
        rmView.setTextViewText(R.id.lbl_singer, song.getArtist());
        // song controller
        rmView.setOnClickPendingIntent(R.id.btnBackward,
                createReceiverIntent(this, GlobalValue.BACK_ACTION_ID));
        rmView.setOnClickPendingIntent(R.id.btnForward,
                createReceiverIntent(this, GlobalValue.NEXT_ACTION_ID));
        rmView.setOnClickPendingIntent(R.id.btnPlay,
                createReceiverIntent(this, GlobalValue.PLAY_OR_ACTION_ID));
        if (GlobalValue.currentMusicService.isPause)
            rmView.setInt(R.id.btnPlay, "setBackgroundResource",
                    R.drawable.ic_play);
        else
            rmView.setInt(R.id.btnPlay, "setBackgroundResource",
                    R.drawable.ic_pause);

        // when swipe out the notification
        mBuilder.setDeleteIntent(createReceiverIntent(this,
                GlobalValue.PAUSE_ACTION_ID));
        // when tap on notification
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("notification_status", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
        );
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        // mNotificationManager.notify(MainActivity.NOTIFICATION_ID,
        // mBuilder.build());
        startForeground(MainActivity.NOTIFICATION_ID, mBuilder.build()); // startForeGround
        // so
        // it
        // will
        // never
        // be
        // killed

    }

    private PendingIntent createReceiverIntent(Context context,
                                               int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), notificationId, intent, 0);
        return pendingIntent;
    }

    private void cancelNotification() {
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy");
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
    }


    private class ToastRunnable implements Runnable {
        String mText;

        public ToastRunnable(String text) {
            mText = text;
        }

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void addListen() {
        String getUrl = WebserviceApi.getAddNewView(getBaseContext()) + "?id="
                + GlobalValue.getCurrentSong().getId();
        ModelManager.sendGetRequest(getApplicationContext(), getUrl, null, false, new ModelManagerListener() {
            @Override
            public void onError(VolleyError error) {

            }

            @Override
            public void onSuccess(String json) {

            }
        });
    }

    public static MediaNotification mediaNotification;

    public static MediaNotification listenerClickMediaNotification(MediaNotification mMediaNotification) {
        mediaNotification = mMediaNotification;
        return mediaNotification;
    }
}
