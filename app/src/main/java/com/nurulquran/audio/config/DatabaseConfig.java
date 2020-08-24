package com.nurulquran.audio.config;

import android.annotation.SuppressLint;
import android.content.Context;

public final class DatabaseConfig {
	private final int DB_VERSION = 4;
	private final String DB_NAME = "DbMusicOnline2.sqlite";

	// --------------------TABLE FAVORITE----------------------
	public static String TABLE_FAVORITE = "tbFavorite";
	public static String TABLE_PLAYLIST = "tbPlaylist";
	public static String KEY_ID = "id";
	public static String KEY_NAME = "name";
	public static String KEY_URL = "url";
	public static String KEY_IMAGE = "image";
	public static String KEY_ARTIST = "description";
	public static String KEY_POSITION = "position";
	public static String KEY_LIST_SONG = "list_song";



	// --------------------TABLE OFFLINE----------------------
	public static String TABLE_OFFLINE = "tboffline";
	public static String KEY_OFFLINE_LEVEL = "level";
	public static String KEY_OFFLINE_PARENT_ID = "parentid";
	public static String KEY_OFFLINE_DATA = "offlinedata";
	public static String KEY_OFFLINE_PAGE = "pageno";

	/**
	 * Get database version
	 * 
	 * @return
	 */
	public int getDatabaseVersion() {
		return DB_VERSION;
	}

	/**
	 * Get database name
	 * 
	 * @return
	 */
	public String getDatabaseName() {
		return DB_NAME;
	}

	/**
	 * Get database path
	 * 
	 * @return
	 */
	@SuppressLint("SdCardPath")
	public String getDatabasePath(Context context) {
		return context.getFilesDir().getParentFile() + "/databases/";
	}

	/**
	 * Get database path
	 * 
	 * @return
	 */
	public String getDatabaseFullPath(Context context) {
		return getDatabasePath(context) + DB_NAME;
	}
}
