package com.nurulquran.audio.activity;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nurulquran.audio.R;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.service.MusicService;
import com.nurulquran.audio.widget.AutoBgButton;

/**
 * Created by phamtuan on 29/04/2016.
 */
public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_CODE = 1;
    protected MusicService mMusicService;
    protected TextView tvSongNameFooter, tvDescriptionFooter;
    protected AutoBgButton btnPlayerFooter, btnPreviousFooter, btnNextFooter;
    protected LinearLayout mLayoutPlayerFooter;
    protected ImageView btnMenu;

    protected void setButtonBack() {
        btnMenu = (ImageView) findViewById(R.id.btnMenu);
        btnMenu.setImageResource(
                R.drawable.ic_arrow_back_white_36dp);
        btnMenu.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
    }

    protected void setHeaderTitle(String header) {
        TextView title = (TextView) findViewById(R.id.lblHeader);
        title.setText(header);
        title.setSelected(true);
    }

    protected void setHeaderTitle(int header) {
        TextView title = (TextView) findViewById(R.id.lblHeader);
        title.setText(header);
        title.setSelected(true);
    }

    protected void inFlaterLayOut(int layoutResId) {
        setContentView(layoutResId);
    }

    protected void showToast(int idString) {
        Toast.makeText(getApplicationContext(), idString, Toast.LENGTH_SHORT).show();
    }

    protected void gotoPlayer() {
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    // handle player music
    protected void initUIMediaPlayerFooter() {
        mLayoutPlayerFooter = (LinearLayout) findViewById(R.id.layoutPlayerFooter);
        tvSongNameFooter = (TextView) findViewById(R.id.lblSongNameFooter);
        tvDescriptionFooter = (TextView) findViewById(R.id.lblArtistFooter);
        btnPlayerFooter = (AutoBgButton) findViewById(R.id.btnPlayFooter);
        btnNextFooter = (AutoBgButton) findViewById(R.id.btnNextFooter);
        btnPreviousFooter = (AutoBgButton) findViewById(R.id.btnPreviousFooter);
    }

    protected void initControlMediaPlayerFooter() {
        mLayoutPlayerFooter.setOnClickListener(this);
        btnPlayerFooter.setOnClickListener(this);
        btnPreviousFooter.setOnClickListener(this);
        btnNextFooter.setOnClickListener(this);
    }

    /****
     * bind service
     *
     * @param serviceConnection
     */
    protected void mBindService(ServiceConnection serviceConnection) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

    }

    /*****
     * unbind service
     *
     * @param serviceConnection
     */
    protected void mUnBindService(ServiceConnection serviceConnection) {
        unbindService(serviceConnection);
    }

    /**
     * show / hide media footer
     */
    protected void visibilityMediaFooter() {
        try {
            if (mMusicService.isPause() || mMusicService.isPlay()) {
                mLayoutPlayerFooter.setVisibility(View.VISIBLE);
                showDataSong();
                setButtonPlayerFooter();
            } else {
                mLayoutPlayerFooter.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            mLayoutPlayerFooter.setVisibility(View.GONE);
        }
    }

    /***
     * show data song name + description
     */
    protected void showDataSong() {
        tvSongNameFooter.setText(GlobalValue.getCurrentSong().getName());
        if (GlobalValue.getCurrentSong().getDescription() != null) {
            tvDescriptionFooter.setText(Html.fromHtml(GlobalValue.getCurrentSong().getDescription()));
        } else {
            tvDescriptionFooter.setText("");
        }
    }

    /****
     * handle button play when music playing
     */
    protected void setButtonPlayerFooter() {
        if (mMusicService.isPause()) {
            btnPlayerFooter.setBackgroundResource(R.drawable.bg_btn_play_small);
        } else {
            btnPlayerFooter.setBackgroundResource(R.drawable.bg_btn_pause_small);
        }
    }

    /****
     * handle when click on button play footer
     */
    protected void onClickPlayerFooter() {
        mMusicService.playOrPauseMusic();
        setButtonPlayerFooter();
    }

    /***
     * handle when click on next song
     */
    protected void onClickNextFooter() {
        mMusicService.nextSongByOnClick();
    }

    /*****
     * handle when click on previuos song
     */
    protected void onClickPreviousFooter() {
        mMusicService.backSongByOnClick();
    }

    /****
     * handle when click on media player footer
     */
    protected void onClickLayoutPlayerFooter() {
        MainActivity.isTapOnFooter = true;
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        startActivityForResult(intent, PlayerActivity.REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutPlayerFooter:
                onClickLayoutPlayerFooter();
                break;
            case R.id.btnPlayFooter:
                onClickPlayerFooter();
                break;
            case R.id.btnNextFooter:
                onClickNextFooter();
                break;
            case R.id.btnPreviousFooter:
                onClickPreviousFooter();
                break;
        }
    }
}
