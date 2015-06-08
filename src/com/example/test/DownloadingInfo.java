package com.example.test;

/**
 * 某一任务正在下载时的信息
 * 
 * @author shichaohui@meiriq.com
 * 
 */
public class DownloadingInfo {

	private String kbps = "0"; // 每秒下载速度
	private int secondSize = 0; // 一秒钟累计下载量
	private int fileSize = 0; // 文件大小

	public String getKbps() {
		return kbps;
	}

	public void setKbps(String kbps) {
		this.kbps = kbps;
	}

	public int getSecondSize() {
		return secondSize;
	}

	public void setSecondSize(int secondSize) {
		this.secondSize = secondSize;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public String toString() {
		return "DownloadingInfo [kbps=" + kbps + ", secondSize=" + secondSize
				+ ", fileSize=" + fileSize + "]";
	}

}
