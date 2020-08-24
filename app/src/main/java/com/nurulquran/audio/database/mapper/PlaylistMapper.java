package com.nurulquran.audio.database.mapper;

import android.database.Cursor;

import com.nurulquran.audio.config.DatabaseConfig;
import com.nurulquran.audio.database.CursorParseUtility;
import com.nurulquran.audio.database.IRowMapper;
import com.nurulquran.audio.object.Playlist;

public class PlaylistMapper implements IRowMapper<Playlist> {
	@Override
	public Playlist mapRow(Cursor row, int rowNum) {
		Playlist song = new Playlist();
		song.setId(CursorParseUtility.getString(row, DatabaseConfig.KEY_ID));
		song.setName(CursorParseUtility.getString(row, DatabaseConfig.KEY_NAME));
		song.setListSongs(CursorParseUtility.getString(row, DatabaseConfig.KEY_LIST_SONG));
		return song;
	}
}