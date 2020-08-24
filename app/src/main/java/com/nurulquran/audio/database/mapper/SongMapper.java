package com.nurulquran.audio.database.mapper;

import android.database.Cursor;

import com.nurulquran.audio.config.DatabaseConfig;
import com.nurulquran.audio.database.CursorParseUtility;
import com.nurulquran.audio.database.IRowMapper;
import com.nurulquran.audio.object.Song;

public class SongMapper implements IRowMapper<Song> {
	@Override
	public Song mapRow(Cursor row, int rowNum) {
		Song song = new Song();
		song.setId(CursorParseUtility.getString(row, DatabaseConfig.KEY_ID));
		song.setName(CursorParseUtility.getString(row, DatabaseConfig.KEY_NAME));
		song.setmTypePathFile(Song.PATH_FILE_DOWNLOAD);
		song.setUrl(CursorParseUtility.getString(row, DatabaseConfig.KEY_URL));
		song.setImage(CursorParseUtility.getString(row, DatabaseConfig.KEY_IMAGE));
		song.setDescription(CursorParseUtility.getString(row, DatabaseConfig.KEY_ARTIST));
		song.setPosition(CursorParseUtility.getInt(row, DatabaseConfig.KEY_POSITION));
		return song;
	}
}