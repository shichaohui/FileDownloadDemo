package com.example.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.example.test.DownloadHttpTool.DownloadComplated;

/**
 * 将下载方法封装在此类 提供开始、暂停、删除以及重置的方法。<br>
 * 通过修改常量{@link DownloadUtil#MAX_COUNT}可改变最大并行下载任务量
 * 
 * @author shichaohui@meiriq.com
 */
public class DownloadUtil {

	private static DownloadUtil instance = null;
	private Context context = null;
	private List<String> downloadList = null;
	private Map<String, DownloadHttpTool> downloadMap = null;
	private int currentUrlIndex = -1;
	private final int MAX_COUNT = 2; // 最大并行下载量
	private int currentCount = 0; // 当前并行下载量
	private final String FLAG_FREE = "free"; // 标记downloadMap中空闲的DownloadHttpTool实例
	private OnDownloadListener onDownloadListener = null;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String url = msg.obj.toString();
			if (msg.what == 0) {
				if (onDownloadListener != null) {
					onDownloadListener
							.downloadProgress(url, msg.arg2, msg.arg1);
				}
			} else if (msg.what == 1) {
				if (onDownloadListener != null) {
					onDownloadListener.downloadStart(url, msg.arg1);
				}
			} else if (msg.what == 2) {
				onDownloadListener.downloadEnd(url);
			}
		}

	};

	private DownloadUtil(Context context) {
		this.context = context;
		downloadList = new ArrayList<String>();
		downloadMap = new HashMap<String, DownloadHttpTool>();
	}

	private static synchronized void syncInit(Context context) {
		if (instance == null) {
			instance = new DownloadUtil(context);
		}
	}

	public static DownloadUtil getInstance(Context context) {
		if (instance == null) {
			syncInit(context);
		}
		return instance;
	}

	/**
	 * 下载之前的准备工作，并自动开始下载
	 * 
	 * @param context
	 */
	public void prepare(String urlString) {
		downloadList.add(urlString);
		if (currentCount < MAX_COUNT) {
			start();
		} else {
			System.out.println("等待下载____" + urlString);
		}
	}

	/**
	 * 开始下载
	 */
	private synchronized void start() {
		if (++currentUrlIndex >= downloadList.size()) {
			currentUrlIndex--;
			return;
		}
		currentCount++;
		String urlString = downloadList.get(currentUrlIndex);
		System.out.println("开始下载____" + urlString);
		DownloadHttpTool downloadHttpTool = null;
		if (downloadMap.size() < MAX_COUNT) { // 保证downloadMap.size() <= 2
			downloadHttpTool = new DownloadHttpTool(context, mHandler,
					downloadComplated);
			if (downloadMap.containsKey(urlString)) {
				downloadMap.remove(urlString);
			}
			downloadMap.put(urlString, downloadHttpTool);
		} else {
			downloadHttpTool = downloadMap.get(FLAG_FREE);
			downloadMap.remove(FLAG_FREE);
			downloadMap.put(urlString, downloadHttpTool);
		}
		downloadHttpTool.start(urlString);
	}

	/** 暂停当前下载任务 */
	public void pause(String urlString) {
		paused(urlString, new Paused() {

			@Override
			public void onPaused(DownloadHttpTool downloadHttpTool) {
				downloadHttpTool.pause();
			}
		});
	}

	/** 暂停所有的下载任务 */
	public void pauseAll() {
		// 如果需要边遍历集合边删除数据，需要从后向前遍历，否则会出异常（Caused by:
		// java.util.ConcurrentModificationException）
		String[] keys = new String[downloadMap.size()];
		downloadMap.keySet().toArray(keys);
		for (int i = keys.length - 1; i >= 0; i--) {
			pause(keys[i]);
		}
		instance = null;
	}

	/**
	 * 恢复当前下载任务
	 * 
	 * @param urlString
	 *            要恢复下载的文件的地址
	 */
	public void resume(String urlString) {
		prepare(urlString);
	}

	/** 恢复所有的下载任务 */
	public void resumeAll() {
		for (Entry<String, DownloadHttpTool> entity : downloadMap.entrySet()) {
			prepare(entity.getKey());
		}
	}

	/** 删除当前下载任务 */
	public void delete(String urlString) {
		boolean bool = paused(urlString, new Paused() {

			@Override
			public void onPaused(DownloadHttpTool downloadHttpTool) {
				downloadHttpTool.pause();
				downloadHttpTool.delete();
			}
		});
		if (!bool) { // 下载任务不存在，直接删除临时文件
			File file = new File(DownloadHttpTool.filePath + "/"
					+ urlString.split("/")[urlString.split("/").length - 1]
					+ DownloadHttpTool.FILE_TMP_SUFFIX);
			System.out.println(file.delete());
		}
	}

	interface Paused {

		void onPaused(DownloadHttpTool downloadHttpTool);

	}

	/**
	 * 暂停
	 * 
	 * @param urlString
	 * @param paused
	 * @return 下载任务是否存在的标识
	 */
	private boolean paused(String urlString, Paused paused) {
		if (downloadMap.containsKey(urlString)) {
			currentCount--;
			DownloadHttpTool downloadHttpTool = downloadMap.get(urlString);
			paused.onPaused(downloadHttpTool);
			if (!downloadMap.containsKey(FLAG_FREE)) { // 保证key == FLAG_FREE的数量
														// = 1
				downloadMap.put(FLAG_FREE, downloadHttpTool);
			}
			downloadMap.remove(urlString);
			start();
			return true;
		}
		return false;
	}

	DownloadComplated downloadComplated = new DownloadComplated() {

		@Override
		public void onComplated(String urlString) {
			System.out.println("下载完成____" + urlString);
			Message msg = new Message();
			msg.what = 2;
			msg.obj = urlString;
			mHandler.sendMessage(msg);
			pause(urlString);
			// 满足此条件说明全部下载结束
			if (downloadMap.size() == 1 && downloadMap.containsKey(FLAG_FREE)) {
				System.out.println("全部下载结束");
			}
		}
	};

	/** 设置下载监听 */
	public void setOnDownloadListener(OnDownloadListener onDownloadListener) {
		this.onDownloadListener = onDownloadListener;
	}

	/** 下载回调接口 */
	public interface OnDownloadListener {

		/**
		 * 下载开始回调接口
		 * 
		 * @param url
		 * @param fileSize
		 *            目标文件大小
		 */
		public void downloadStart(String url, int fileSize);

		/**
		 * 下载进度回调接口
		 * 
		 * @param
		 * @param downloadedSize
		 *            已下载大小
		 * @param lenth
		 *            本次下载大小
		 */
		public void downloadProgress(String url, int downloadedSize, int length);

		/**
		 * 下载完成回调
		 * 
		 * @param url
		 */
		public void downloadEnd(String url);

	}

}