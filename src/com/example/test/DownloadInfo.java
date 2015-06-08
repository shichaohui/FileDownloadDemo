package com.example.test;

/**
 * 保存每个下载线程下载信息类
 * 
 * @author shichaohui@meiriq.com
 */
public class DownloadInfo {

	private int threadId; // 下载线程的id
	private int startPos; // 开始点
	private int endPos; // 结束点
	private int compeleteSize; // 完成度
	private String url; // 下载文件的URL地址

	/**
	 * 
	 * @param threadId
	 *            下载线程的id
	 * @param startPos
	 *            开始点
	 * @param endPos
	 *            结束点
	 * @param compeleteSize
	 *            // 已下载的大小
	 * @param url
	 *            下载地址
	 */
	public DownloadInfo(int threadId, int startPos, int endPos,
			int compeleteSize, String url) {
		this.threadId = threadId;
		this.startPos = startPos;
		this.endPos = endPos;
		this.compeleteSize = compeleteSize;
		this.url = url;
	}

	public DownloadInfo() {
	}

	/** 获取下载地址 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/** 获取下载线程的Id */
	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	/** 获取下载的开始位置 */
	public int getStartPos() {
		return startPos;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	/** 获取下载的结束位置 */
	public int getEndPos() {
		return endPos;
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	/** 获取已下载的大小 */
	public int getCompeleteSize() {
		return compeleteSize;
	}

	public void setCompeleteSize(int compeleteSize) {
		this.compeleteSize = compeleteSize;
	}

	@Override
	public String toString() {
		return "DownloadInfo [threadId=" + threadId + ", startPos=" + startPos
				+ ", endPos=" + endPos + ", compeleteSize=" + compeleteSize
				+ "]";
	}
}
