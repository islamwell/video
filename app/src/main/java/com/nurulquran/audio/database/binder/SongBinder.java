package com.nurulquran.audio.database.binder;

import android.database.sqlite.SQLiteStatement;

import com.nurulquran.audio.database.ParameterBinder;
import com.nurulquran.audio.object.Song;

public class SongBinder implements ParameterBinder {
	public void bind(SQLiteStatement statement, Object object) {
		Song image = (Song) object;
		statement.bindString(1, image.getId());
		statement.bindString(2, image.getName());
		statement.bindString(3, image.getUrl());
		statement.bindString(4, image.getImage());
		statement.bindString(5, image.getDescription());
		statement.bindLong(6, image.getPosition());
	}
}
