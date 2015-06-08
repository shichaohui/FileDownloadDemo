package com.example.test;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 数据库操作工具类
 * 
 * @author shichaohui@meiriq.com
 */
public class DownlaodSqlTool {
	
	private static DownlaodSqlTool instance = null;
	private DownLoadHelper dbHelper = null;

	private DownlaodSqlTool(Context context) {
		dbHelper = new DownLoadHelper(context);
	}

	private static synchronized void syncInit(Context context) {
		if (instance == null) {
			instance = new DownlaodSqlTool(context);
		}
	}

	public static DownlaodSqlTool getInstance(Context context) {
		if (instance == null) {
			syncInit(context);
		}
		return instance;
	}
	
	/** 将下载的进度等信息保存到数据库 */
	public void insertInfos(List<DownloadInfo> infos) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		for (DownloadInfo info : infos) {
			String sql = "insert into download_info(thread_id,start_pos, end_pos,compelete_size,url) values (?,?,?,?,?)";
			Object[] bindArgs = { info.getThreadId(), info.getStartPos(),
					info.getEndPos(), info.getCompeleteSize(), info.getUrl() };
			database.execSQL(sql, bindArgs);
		}
	}

	/** 获取下载的进度等信息 */
	public List<DownloadInfo> getInfos(String urlstr) {
		List<DownloadInfo> list = new ArrayList<DownloadInfo>();
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		String sql = "select thread_id, start_pos, end_pos,compelete_size,url from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		while (cursor.moveToNext()) {
			DownloadInfo info = new DownloadInfo(cursor.getInt(0),
					cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
					cursor.getString(4));
			list.add(info);
		}
		cursor.close();
		return list;
	}

	/** 更新数据库中的下载信息 */
	public void updataInfos(int threadId, int compeleteSize, String urlstr) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		String sql = "update download_info set compelete_size=? where thread_id=? and url=?";
		Object[] bindArgs = { compeleteSize, threadId, urlstr };
		database.execSQL(sql, bindArgs);
	}

	/** 关闭数据库 */
	public void closeDb() {
		dbHelper.close();
	}

	/** 删除数据库中的数据 */
	public void delete(String url) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete("download_info", "url=?", new String[] { url });
	}
}