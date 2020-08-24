package com.nurulquran.audio.fragment;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.PlayerActivity;
import com.nurulquran.audio.adapter.SongPlayingAdapter;
import com.nurulquran.audio.config.GlobalValue;
public class PlayerListPlayingFragment extends BaseFragment {
    private ListView lsvSongPlaying;
    private SongPlayingAdapter songPlayingAdapter;
    private PlayerActivity mPlayerActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPlayerActivity = (PlayerActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_list_playing, container, false);
        if (GlobalValue.listSongPlay == null) {
            GlobalValue.constructor(getActivity());
        }
        songPlayingAdapter = new SongPlayingAdapter(getActivity(), GlobalValue.listSongPlay);
        lsvSongPlaying = (ListView) view.findViewById(R.id.lsvSongPlaying);
        lsvSongPlaying.setAdapter(songPlayingAdapter);
        lsvSongPlaying.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int position, long l) {
                mPlayerActivity.mService.startMusic(position);
            }
        });
        //setButtonMenu(view);
        return view;
    }

    public void refreshListPlaying() {
        if (songPlayingAdapter != null) {
            songPlayingAdapter.setIndex(GlobalValue.currentSongPlay);
            songPlayingAdapter.notifyDataSetChanged();
            lsvSongPlaying.smoothScrollToPosition(GlobalValue.currentSongPlay);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    refreshListPlaying();
                }
            }, 500);
        }
    }
}
