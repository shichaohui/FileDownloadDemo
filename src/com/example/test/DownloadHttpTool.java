package com.example.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * 利用Http协议进行多线程下载具体实现类
 */
public class DownloadHttpTool {

	private int threadCount; // 线程数量
	private String urlstr; // URL地址
	private Context mContext;
	private Handler mHandler;
	private List<DownloadInfo> downloadInfos; // 保存下载信息的类

	private String localPath; // 目录
	private String fileName; // 文件名
	private int fileSize; // 文件大小
	private DownlaodSqlTool sqlTool; // 文件信息保存的数据库操作类

	private enum Download_State {
		Downloading, Pause, Ready, Compelete; // 利用枚举表示下载的几种状态
	}

	private Download_State state = Download_State.Ready; // 当前下载状态

	private int totalCompelete = 0;// 所有线程已下载的总数

	/**
	 * 
	 * @param threadCount
	 *            线程数
	 * @param urlString
	 *            下载地址
	 * @param localPath
	 *            保存路径
	 * @param fileName
	 *            文件名
	 * @param context
	 *            上下文对象
	 * @param handler
	 *            用于处理UI
	 */
	public DownloadHttpTool(int threadCount, String urlString,
			String localPath, String fileName, Context context, Handler handler) {
		super();
		this.threadCount = threadCount;
		this.urlstr = urlString;
		this.localPath = localPath;
		this.mContext = context;
		this.mHandler = handler;
		this.fileName = fileName;
		sqlTool = new DownlaodSqlTool(mContext);
	}

	/** 在开始下载之前需要调用ready方法进行配置 */
	public void ready() {
		totalCompelete = 0;
		downloadInfos = sqlTool.getInfos(urlstr);
		if (downloadInfos.size() == 0) {
			initFirst();
		} else {
			File file = new File(localPath + "/" + fileName);
			if (!file.exists()) {
				sqlTool.delete(urlstr);
				initFirst();
			} else {
				fileSize = downloadInfos.get(downloadInfos.size() - 1)
						.getEndPos();
				for (DownloadInfo info : downloadInfos) {
					totalCompelete += info.getCompeleteSize();
				}
			}
		}
	}

	/** 开始下载 */
	public void start() {
		if (downloadInfos != null) {
			if (state == Download_State.Downloading) {
				return;
			}
			state = Download_State.Downloading;
			for (DownloadInfo info : downloadInfos) { // 开启线程下载
				new DownloadThread(info.getThreadId(), info.getStartPos(),
						info.getEndPos(), info.getCompeleteSize(),
						info.getUrl()).start();
			}
		}
	}

	/** 暂停当前下载任务 */
	public void pause() {
		state = Download_State.Pause;
		sqlTool.closeDb();
	}

	/** 删除当前下载任务 */
	public void delete() {
		compelete();
		File file = new File(localPath + "/" + fileName);
		file.delete();
	}

	/** 完成下载 */
	public void compelete() {
		sqlTool.delete(urlstr);
		sqlTool.closeDb();
		state = Download_State.Compelete;
	}

	/** 获取目标文件大小 */
	public int getFileSize() {
		return fileSize;
	}

	/** 获取当前下载的大小 */
	public int getTotalCompeleteSize() {
		return totalCompelete;
	}
	
	/** 累加当前下载的大小 */
	private void setTotalCompeleteSize(int length) {
		synchronized (this) { // 加锁保证已下载的正确性
			totalCompelete += length;
		}
	}

	/** 第一次下载时进行的初始化 */
	private void initFirst() {
		URL url = null;
		try {
			url = new URL(urlstr);
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setRequestMethod("GET");
			fileSize = connection.getContentLength();
			
			File fileParent = new File(localPath);
			if (!fileParent.exists()) {
				fileParent.mkdir();
			}
			File file = new File(fileParent, fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			// 随机访问文件
			RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			accessFile.setLength(fileSize);
			accessFile.close();
			connection.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 计算每个线程需要下载的大小
		int range = fileSize / threadCount;
		// 保存每个线程的下载信息
		downloadInfos = new ArrayList<DownloadInfo>();
		for (int i = 0; i < threadCount - 1; i++) {
			DownloadInfo info = new DownloadInfo(i, i * range, (i + 1) * range
					- 1, 0, urlstr);
			downloadInfos.add(info);
		}
		// 最后一个线程和前面的处理有点不一样
		DownloadInfo info = new DownloadInfo(threadCount - 1, (threadCount - 1)
				* range, fileSize - 1, 0, urlstr);
		downloadInfos.add(info);
		// 插入到数据库
		sqlTool.insertInfos(downloadInfos);
	}

	/** 自定义下载线程 */
	private class DownloadThread extends Thread {

		private int threadId = 0; // 线程Id
		private int startPos = 0; // 在文件中的开始的位置
		private int endPos = 0; // 在文件中的结束的位置
		private int compeleteSize = 0; // 已完成下载的大小
		private String urlstr = ""; // 下载地址

		/**
		 * 
		 * @param threadId 线程Id
		 * @param startPos 在文件中的开始的位置
		 * @param endPos 在文件中的结束的位置
		 * @param compeleteSize 已完成下载的大小
		 * @param urlstr 下载地址
		 */
		public DownloadThread(int threadId, int startPos, int endPos,
				int compeleteSize, String urlstr) {
			this.threadId = threadId;
			this.startPos = startPos;
			this.endPos = endPos;
			this.urlstr = urlstr;
			this.compeleteSize = compeleteSize;
		}

		@Override
		public void run() {
			HttpURLConnection connection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream is = null;
			try {
				randomAccessFile = new RandomAccessFile(localPath + "/"
						+ fileName, "rwd");
				randomAccessFile.seek(startPos + compeleteSize);
				URL url = new URL(urlstr);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				// 设置请求的数据的范围
				connection.setRequestProperty("Range", "bytes="
						+ (startPos + compeleteSize) + "-" + endPos);
				is = connection.getInputStream();
				byte[] buffer = new byte[6 * 1024]; // 6K的缓存
				int length = -1;
				while ((length = is.read(buffer)) != -1) {
					randomAccessFile.write(buffer, 0, length); // 写缓存数据到文件
					compeleteSize += length;
					Message message = Message.obtain();
					message.what = threadId;
					message.obj = urlstr;
					message.arg1 = length;
					mHandler.sendMessage(message);
					// 非正在下载状态时跳出循环
					if (state != Download_State.Downloading) {
						break;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 不管发生了什么事，都要保存下载信息到数据库
				sqlTool.updataInfos(threadId, compeleteSize, urlstr);
				try {
					if (is != null) {
						is.close();
					}
					randomAccessFile.close();
					connection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}