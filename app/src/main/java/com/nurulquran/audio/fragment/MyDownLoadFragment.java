package com.nurulquran.audio.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.adapter.SongAdapter;
import com.nurulquran.audio.config.GlobalValue;
import com.nurulquran.audio.database.DatabaseUtility;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.slidingmenu.SlidingMenu;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by phamtuan on 20/05/2016.
 */
public class MyDownLoadFragment extends BaseFragment {
    private View view;
    private ListView lvListSongDownLoad;
    private SongAdapter mSongAdapter;
    public ArrayList<Song> mListSongDownLoad;
    private DatabaseUtility databaseUtility;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mydownload, container, false);
        initUI();
        initControl();
        initUIBase(view);
        setButtonMenu(view);
        setHeaderTitle(R.string.my_download);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getMainActivity().menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
            getMainActivity().setVisibilityFooter();
            getMainActivity().hideBannerAd();
            if (mListSongDownLoad != null) {
                if (!mListSongDownLoad.isEmpty()) {
                    mListSongDownLoad.clear();
                    mListSongDownLoad.addAll(databaseUtility.getAllFavorite());
                    mSongAdapter.notifyDataSetChanged();
                } else {
                    mListSongDownLoad.addAll(databaseUtility.getAllFavorite());
                    mSongAdapter.notifyDataSetChanged();
                }
            }

        }
    }

    private void initUI() {
        databaseUtility = new DatabaseUtility(getActivity());
        mListSongDownLoad = new ArrayList<>();
        mListSongDownLoad.addAll(databaseUtility.getAllFavorite());
        lvListSongDownLoad = (ListView) view.findViewById(R.id.lvListSongDownLoad);
        mSongAdapter = new SongAdapter(getActivity(), mListSongDownLoad);
        lvListSongDownLoad.setAdapter(mSongAdapter);
    }

    private void initControl() {
        lvListSongDownLoad.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                        + getString(R.string.app_name) + "/", mListSongDownLoad.get(position).getName() + ".mp3");
                Log.e("File path doenload",file.toString());
                if (file.exists()) {
                    getMainActivity().toMusicPlayer = MainActivity.FROM_LIST_SONG;
                    GlobalValue.currentSongPlay = (int) id;
                    GlobalValue.listSongPlay.clear();
                    GlobalValue.listSongPlay.addAll(mListSongDownLoad);
                    getMainActivity().isTapOnFooter = false;
                    gotoPlayer();
                } else {
                    Toast.makeText(getActivity(), "This song has been deleted in the SD card!", Toast.LENGTH_SHORT).show();
                    databaseUtility.deleteSong(mListSongDownLoad.get(position).getId());

                }

            }
        });
        lvListSongDownLoad.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showConfirmDeleteSongDownloadDialog(mListSongDownLoad.get(position), position);
                return true;
            }
        });
    }

    private void showConfirmDeleteSongDownloadDialog(final Song song, final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.delete_song) + " " + song.getName())
                .setMessage(getString(R.string.confirm_delete_song) + " " + song.getName())
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                deleteFileFromSDCard(Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                                        + getString(R.string.app_name) + "/" + song.getName() + ".mp3");
                                if (mListSongDownLoad.get(pos).getUrl().equals(GlobalValue.getCurrentSong().getUrl())) {
                                    getMainActivity().mService.pauseMusic();
                                    getMainActivity().hideMediaFooter();
                                }
                                databaseUtility.deleteSong(song.getId());
                                mListSongDownLoad.remove(pos);
                                mListSongDownLoad.clear();
                                mListSongDownLoad.addAll(databaseUtility.getAllFavorite());
                                mSongAdapter.notifyDataSetChanged();
//                                GlobalValue.listSongPlay.clear();
//                                GlobalValue.listSongPlay.addAll(mListSongDownLoad);
//                                GlobalValue.currentSongPlay = pos;

                                onResume();
                            }
                        }).setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    public static boolean deleteFileFromSDCard(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        } else {
            return true;
        }
    }
}
