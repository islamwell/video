package com.nurulquran.audio.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.nurulquran.audio.BaseFragment;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.adapter.PlaylistAdapter;
import com.nurulquran.audio.config.WebserviceApi;
import com.nurulquran.audio.object.Playlist;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.slidingmenu.SlidingMenu;
import com.nurulquran.audio.util.AppUtil;
import com.nurulquran.audio.modelmanager.CommonParser;
import com.nurulquran.audio.util.SmartLog;

public class PlaylistFragment extends BaseFragment {
	private ListView lsvPlaylist;
	private List<Playlist> listPlaylists;
	private PlaylistAdapter playlistAdapter;
	private List<Song> listSongs;
	private String[] arraySongName;
	private String playlistName;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_playlist, container,
				false);
		initUIBase(view);
		setHeaderTitle(getString(R.string.title_playlist));
		setButtonMenu(view);
		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {			
			getMainActivity().menu
					.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
			getMainActivity().setVisibilityFooter();

		}
	}

	@Override
	protected void initUIBase(View view) {
		super.initUIBase(view);
		view.findViewById(R.id.layoutCreatNewPlaylist).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						showDialogCreatNewPlaylist();
					}
				});

		listSongs = new ArrayList<Song>();

		lsvPlaylist = (ListView) view.findViewById(R.id.lsvPlaylist);
		listPlaylists = getMainActivity().databaseUtility.getAllPlaylist();
		playlistAdapter = new PlaylistAdapter(getActivity(), listPlaylists);
		lsvPlaylist.setAdapter(playlistAdapter);
		lsvPlaylist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int position,
					long l) {
				Playlist playlist = listPlaylists.get(position);
				getMainActivity().currentPlaylist = getMainActivity().databaseUtility
						.getAPlaylist(playlist.getId());
				ListSongsFragment.isShowing = false;
				getMainActivity().gotoFragment(MainActivity.LIST_SONG_FRAGMENT);
			}
		});
		lsvPlaylist.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				showConfirmDeletePlaylistDialog(position);
				return true;
			}
		});
	}




	private void showConfirmDeletePlaylistDialog(final int index) {

		final Playlist playlist = listPlaylists.get(index);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Delete Playlist :")
				.setMessage(
						"Do you want to delete '"
								+ playlist.getName() + " ' ?")
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								if (getMainActivity().databaseUtility
										.deletePlaylist(playlist)) {
									listPlaylists.remove(index);
									playlistAdapter.notifyDataSetChanged();
								}
							}
						}).setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}

	private void showDialogCreatNewPlaylist() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.dialog_new_playlist,
				null);
		builder.setView(dialogView)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								EditText txtPlaylistName = (EditText) dialogView
										.findViewById(R.id.txtPlaylistName);
								playlistName = txtPlaylistName.getText()
										.toString();
								Playlist playlist = new Playlist();
								int temp = 0;
								try {
									temp = Integer.parseInt(listPlaylists.get(
											listPlaylists.size() - 1).getId());
								} catch (Exception e) {
								}
								playlist.setId((temp + 1) + "");
								playlist.setName(playlistName);

								if (getMainActivity().databaseUtility
										.insertPlaylist(playlist)) {
									listPlaylists.add(playlist);
									playlistAdapter.notifyDataSetChanged();
								} else {
									Toast.makeText(
											getMainActivity(),
											"Have error when add new playlist! Please try again.",
											Toast.LENGTH_SHORT).show();
								}

							}
						}).setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}


}
