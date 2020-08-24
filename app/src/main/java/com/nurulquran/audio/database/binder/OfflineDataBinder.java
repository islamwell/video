package com.nurulquran.audio.database.binder;

import android.database.sqlite.SQLiteStatement;

import com.nurulquran.audio.database.ParameterBinder;
import com.nurulquran.audio.object.OfflineData;

public class OfflineDataBinder implements ParameterBinder {
	public void bind(SQLiteStatement statement, Object object) {
		OfflineData list = (OfflineData) object;
		statement.bindString(1, list.getLevel());
		statement.bindString(2, list.getParentId());
		statement.bindString(3, list.getOfflinedata());
		statement.bindString(4, list.getPageno());
	}
}
