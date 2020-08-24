package com.nurulquran.audio.database.binder;

import android.database.sqlite.SQLiteStatement;

import com.nurulquran.audio.database.ParameterBinder;
import com.nurulquran.audio.object.Playlist;

public class PlaylistBinder implements ParameterBinder {
	public void bind(SQLiteStatement statement, Object object) {
		Playlist playlist = (Playlist) object;
		statement.bindString(1, playlist.getId());
		statement.bindString(2, playlist.getName());
		statement.bindString(3, playlist.getJsonArraySong());
	}
}
