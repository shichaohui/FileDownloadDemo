package com.example.test;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 利用数据库来记录下载信息
 * 
 * @author shichaohui@meiriq.com
 */
public class DownLoadHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "download.db";
	private static final String TB_NAME = "download_info";
	private static final int DOWNLOAD_VERSION = 1;

	public DownLoadHelper(Context context) {
		super(context, DB_NAME, null, DOWNLOAD_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table "
				+ TB_NAME
				+ "(_id integer PRIMARY KEY AUTOINCREMENT, thread_id integer, "
				+ "start_pos integer, end_pos integer, compelete_size integer,url char)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
