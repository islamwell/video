package com.nurulquran.audio.database.mapper;

import android.database.Cursor;

import com.nurulquran.audio.config.DatabaseConfig;
import com.nurulquran.audio.database.CursorParseUtility;
import com.nurulquran.audio.database.IRowMapper;
import com.nurulquran.audio.object.OfflineData;

public class OfflineDataMapper implements IRowMapper<OfflineData> {
	@Override
	public OfflineData mapRow(Cursor row, int rowNum) {
		OfflineData data = new OfflineData();
		data.setLevel(CursorParseUtility.getString(row, DatabaseConfig.KEY_OFFLINE_LEVEL));
		data.setParentId(CursorParseUtility.getString(row, DatabaseConfig.KEY_OFFLINE_PARENT_ID));
		data.setOfflinedata(CursorParseUtility.getString(row, DatabaseConfig.KEY_OFFLINE_DATA));
		return data;
	}
}