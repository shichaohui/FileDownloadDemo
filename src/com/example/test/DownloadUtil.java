package com.example.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
 * 将下载方法封装在此类 提供开始、暂停、删除以及重置的方法
 */
public class DownloadUtil {

	private DownloadHttpTool mDownloadHttpTool = null;
	private OnDownloadListener onDownloadListener = null;

	private int fileSize = 0; // 目标文件大小
	private int downloadedSize = 0; // 已下载的大小

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int length = msg.arg1;
			synchronized (this) { // 加锁保证已下载的正确性
				downloadedSize += length;
			}
			if (onDownloadListener != null) {
				onDownloadListener.downloadProgress(downloadedSize, length);
			}
			if (downloadedSize >= fileSize) {
				mDownloadHttpTool.compelete();
				if (onDownloadListener != null) {
					onDownloadListener.downloadEnd();
				}
			}
		}

	};

	/**
	 * 
	 * @param threadCount
	 *            线程数
	 * @param filePath
	 *            文件路径
	 * @param filename
	 *            文件名
	 * @param urlString
	 *            下载地址
	 * @param context
	 *            上下文对象
	 */
	public DownloadUtil(int threadCount, String filePath, String filename,
			String urlString, Context context) {

		mDownloadHttpTool = new DownloadHttpTool(threadCount, urlString,
				filePath, filename, context, mHandler);
	}

	/** 开始下载 */
	public void start() {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				// 下载之前首先异步线程调用ready方法做下载的准备工作
				mDownloadHttpTool.ready();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				fileSize = mDownloadHttpTool.getFileSize();
				downloadedSize = mDownloadHttpTool.getTotalCompeleteSize();
				if (onDownloadListener != null) {
					onDownloadListener.downloadStart(fileSize);
				}
				// 开始下载
				mDownloadHttpTool.start();
			}
		}.execute();
	}

	/** 暂停当前下载任务 */
	public void pause() {
		mDownloadHttpTool.pause();
	}
	
	/** 恢复当前下载任务 */
	public void resume() {
		start();
	}
	
	/** 删除当前下载任务 */
	public void delete() {
		pause();
		mDownloadHttpTool.delete();
	}

	/** 重新下载 */
	public void reset() {
		mDownloadHttpTool.delete();
		start();
	}

	/** 设置下载监听 */
	public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
		this.onDownloadListener = onDownloadListener;
	}

	/** 下载回调接口 */
	public interface OnDownloadListener {
		/**
		 * 下载开始回调接口
		 * @param fileSize 目标文件大小
		 */
		public void downloadStart(int fileSize);

		/**
		 * 下载进度回调接口
		 * @param downloadedSize 已下载大小
		 * @param lenth 本次下载大小
		 */
		public void downloadProgress(int downloadedSize, int lenth);

		/** 下载结束回调接口 */
		public void downloadEnd();
	}
}