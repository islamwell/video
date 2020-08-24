package com.nurulquran.audio.config;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.androidquery.AQuery;
import com.nurulquran.audio.R;
import com.nurulquran.audio.activity.MainActivity;
import com.nurulquran.audio.object.Banner;
import com.nurulquran.audio.object.Song;
import com.nurulquran.audio.service.MusicService;

public class GlobalValue {
	public static int PLAYER_ACTIVITY=1;
	public static Bitmap bmImgNotFound;
	public static List<Song> listSongPlay ;
	public static int currentSongPlay;
	public static int currentMenu = MainActivity.CATEGORY_MUSIC;
	public static String DD;
	public static String dd;
	public static Bitmap bmNoImageAvailable;
	public static int currentCategoryId;
	public static int currentParentCategoryId = 0;
	public static String currentCategoryName;
	public static int currentAlbumId;
	public static String currentAlbumName;	
	public static MusicService currentMusicService;
	public static ArrayList<Banner> mListBanner = new ArrayList<>();
	public static ArrayList<Banner> mListBigBanner = new ArrayList<>();
	public static int BACK_ACTION_ID = -1;
	public static int NEXT_ACTION_ID = 2;
	public static int PLAY_OR_ACTION_ID = 1;
	public static int PAUSE_ACTION_ID = 0;

	public static void constructor(Activity activity) {
		bmImgNotFound = new AQuery(activity)
				.getCachedImage(R.drawable.img_not_found);
		if (listSongPlay == null) {
			listSongPlay = new ArrayList<Song>();
		}
		DD = activity.getString(R.string.DD);
		dd = activity.getString(R.string.dd);
		bmNoImageAvailable = BitmapFactory.decodeResource(
				activity.getResources(), R.drawable.img_not_found);
	}

	public static Song getCurrentSong() {
		return listSongPlay.get(currentSongPlay);
	}
}
