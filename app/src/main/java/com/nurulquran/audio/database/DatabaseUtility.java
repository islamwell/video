package com.nurulquran.audio.database;

import android.content.Context;

import com.nurulquran.audio.config.DatabaseConfig;
import com.nurulquran.audio.database.binder.OfflineDataBinder;
import com.nurulquran.audio.database.binder.PlaylistBinder;
import com.nurulquran.audio.database.binder.SongBinder;
import com.nurulquran.audio.database.mapper.OfflineDataMapper;
import com.nurulquran.audio.database.mapper.PlaylistMapper;
import com.nurulquran.audio.database.mapper.SongMapper;
import com.nurulquran.audio.object.OfflineData;
import com.nurulquran.audio.object.Playlist;
import com.nurulquran.audio.object.Song;

import java.util.ArrayList;
import java.util.List;

public final class DatabaseUtility {
	private PrepareStatement statement;

	public DatabaseUtility(Context context) {
		statement = new PrepareStatement(context);
	}

	private static String STRING_SQL_INSERT_INTO_TABLE_FAVORITE = "INSERT OR REPLACE INTO "
			+ DatabaseConfig.TABLE_FAVORITE
			+ "("
			+ DatabaseConfig.KEY_ID
			+ ","
			+ DatabaseConfig.KEY_NAME
			+ " ,"
			+ DatabaseConfig.KEY_URL
			+ " ,"
			+ DatabaseConfig.KEY_IMAGE
			+ " ,"
			+ DatabaseConfig.KEY_ARTIST
			+ " ,"
			+ DatabaseConfig.KEY_POSITION
			+ ") VALUES (?, ?, ?, ?, ?, ?)";

	private static String STRING_SQL_INSERT_INTO_TABLE_OFFLINE = "INSERT OR REPLACE INTO "
			+ DatabaseConfig.TABLE_OFFLINE
			+ "("
			+ DatabaseConfig.KEY_OFFLINE_LEVEL
			+ ","
			+ DatabaseConfig.KEY_OFFLINE_PARENT_ID
			+ " ,"
			+ DatabaseConfig.KEY_OFFLINE_DATA
			+ " ,"
			+ DatabaseConfig.KEY_OFFLINE_PAGE
			+ ") VALUES (?, ?, ?, ?)";


	public List<Song> getAllFavorite() {
		return statement.select(DatabaseConfig.TABLE_FAVORITE, "*", "",
				new SongMapper());
	}

	public boolean insertFavorite(Song song) {
		return statement.insert(STRING_SQL_INSERT_INTO_TABLE_FAVORITE, song,
				new SongBinder());
	}

	public boolean deleteFavorite(Song song) {
		return statement.query(
				"DELETE FROM " + DatabaseConfig.TABLE_FAVORITE + " where "
						+ DatabaseConfig.KEY_ID + "='" + song.getId() + "'"
						+ "and" + " " + DatabaseConfig.KEY_NAME + "='"
						+ song.getName() + "'" + "and" + " "
						+ DatabaseConfig.KEY_ARTIST + "='" + song.getArtist()
						+ "'", null);
	}
	public boolean deleteAllFavorite() {
		return statement.query(
				"DELETE FROM " + DatabaseConfig.TABLE_FAVORITE, null);
	}
	// We use Favorite table to save List Songs
	public void insertFavorite(List<Song> listSongs){
		int i = 0;
		for(i = 0; i <= listSongs.size() -1; i ++){
			insertFavorite(listSongs.get(i));			
		}
		
	}

	private static String STRING_SQL_INSERT_INTO_TABLE_PLAYLIST = "INSERT OR REPLACE INTO "
			+ DatabaseConfig.TABLE_PLAYLIST
			+ "("
			+ DatabaseConfig.KEY_ID
			+ ","
			+ DatabaseConfig.KEY_NAME
			+ " ,"
			+ DatabaseConfig.KEY_LIST_SONG
			+ ") VALUES (?, ?, ?)";

	public List<Playlist> getAllPlaylist() {
		return statement.select(DatabaseConfig.TABLE_PLAYLIST, "*", "",
				new PlaylistMapper());
	}

	public Playlist getAPlaylist(String id) {
		List<Playlist> list = statement.select(DatabaseConfig.TABLE_PLAYLIST,
				"*",DatabaseConfig.KEY_ID + "='" + id + "'",
				new PlaylistMapper());
		if (list.size() > 0)
			return list.get(0);
		return null;
	}

	public boolean insertPlaylist(Playlist playlist) {
		return statement.insert(STRING_SQL_INSERT_INTO_TABLE_PLAYLIST,
				playlist, new PlaylistBinder());
	}

	public boolean deletePlaylist(Playlist playlist) {
		return statement.query("DELETE FROM " + DatabaseConfig.TABLE_PLAYLIST
				+ " where " + DatabaseConfig.KEY_ID + "='" + playlist.getId()
				+ "'", null);
	}

	public boolean updatePlaylist(Playlist playlist) {
		return statement
				.query("UPDATE " + DatabaseConfig.TABLE_PLAYLIST + " SET "
						+ DatabaseConfig.KEY_LIST_SONG + "='"
						+ playlist.getJsonArraySong() + "' where "
						+ DatabaseConfig.KEY_ID + "='" + playlist.getId() + "'",
						null);
	}
	public static boolean checkFavourite(Context context, String id) {
		ArrayList<Song> arr = null;
		PrepareStatement statement = new PrepareStatement(context);
		arr = statement.select(DatabaseConfig.TABLE_FAVORITE, "*",
				DatabaseConfig.KEY_ID + "='" + id + "'",
				new SongMapper());

		return arr.size() > 0;
	}
	public boolean deleteSong(String songId) {
		return statement.query(
				"DELETE FROM " + DatabaseConfig.TABLE_FAVORITE + " where "
						+ DatabaseConfig.KEY_ID + "='" + songId + "'", null);
	}

	public void addCategoryData(OfflineData offlineData) {
		 statement.insert(STRING_SQL_INSERT_INTO_TABLE_OFFLINE,
				offlineData,new OfflineDataBinder());
	}

	public OfflineData getOfflinedata(String level,String parentId,String page) {
		OfflineData balnkoffline=new OfflineData();

		List<OfflineData> list = statement.select(DatabaseConfig.TABLE_OFFLINE,
				"*",DatabaseConfig.KEY_OFFLINE_LEVEL + "='" + level + "' AND "+
						DatabaseConfig.KEY_OFFLINE_PARENT_ID + "='"+parentId+"'"
						+ " AND "+
						DatabaseConfig.KEY_OFFLINE_PAGE + "='"+page+"'",
				new OfflineDataMapper());
		if (list.size() > 0)
			return list.get(list.size()-1);
		return balnkoffline;
	}

	public OfflineData getOfflinedataCat(String level,String parentId,String page) {
		OfflineData balnkoffline=new OfflineData();

//		List<OfflineData> list = statement.select(DatabaseConfig.TABLE_OFFLINE,
//				"*",DatabaseConfig.KEY_OFFLINE_LEVEL + "='" + level + "' AND "+
//						DatabaseConfig.KEY_OFFLINE_PARENT_ID + "='"+parentId+"'",
//				new OfflineDataMapper());
		List<OfflineData> list = statement.select(DatabaseConfig.TABLE_OFFLINE,
				"*",DatabaseConfig.KEY_OFFLINE_LEVEL + "='" + level + "' AND "+
						DatabaseConfig.KEY_OFFLINE_PARENT_ID + "='"+parentId+"'"
						+ " AND "+
						DatabaseConfig.KEY_OFFLINE_PAGE + "='"+page+"'",
				new OfflineDataMapper());
		if (list.size() > 0)
			return list.get(list.size()-1);
		return balnkoffline;
	}
}
