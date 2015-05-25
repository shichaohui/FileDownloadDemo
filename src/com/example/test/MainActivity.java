package com.example.test;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.test.DownloadUtil.OnDownloadListener;

public class MainActivity extends FragmentActivity {

	private ProgressBar mProgressBar = null;
	private Button start = null;
	private Button pause = null;
	private Button resume = null;
	private Button delete = null;
	private Button reset = null;
	private TextView total = null;

	private int maxPb = 0;
	
	private DownloadUtil mDownloadUtil = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		start = (Button) findViewById(R.id.button_start);
		pause = (Button) findViewById(R.id.button_pause);
		resume = (Button) findViewById(R.id.button_resume);
		delete = (Button) findViewById(R.id.button_delete);
		reset = (Button) findViewById(R.id.button_reset);
		total = (TextView) findViewById(R.id.textView_total);
		
		// String urlString = "http://bbra.cn/Uploadfiles/imgs/20110303/fengjin/013.jpg";
		String urlString = "http://xz.cr173.com/soft1/shenmtwltb.apk";
//		 String urlString = "http://meiriq-file.b0.upaiyun.com/gamesbox-site/bugeitangjiudaodana.apk";
		String localPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/local";
		String[] ss = urlString.split("/");
		mDownloadUtil = new DownloadUtil(2, localPath, ss[ss.length - 1],
				urlString, this);
		mDownloadUtil.setOnDownloadListener(new OnDownloadListener() {

			@Override
			public void downloadStart(int fileSize) {
				maxPb = fileSize;
				mProgressBar.setMax(fileSize);
			}

			String text = "已下载%sM / 共%sM \n占比%s  \n下载速度%skb/s";
			DecimalFormat decimalFormat = new DecimalFormat("#.##"); // 小数格式化
			Timer timer = null;
			String kbps = "0"; // 每秒下载速度
			int size = 0; // 一秒钟累计下载量
			
			@Override
			public void downloadProgress(int downloadedSize, int length) {
				mProgressBar.setProgress(downloadedSize);
				total.setText(String.format(text,
						decimalFormat.format(downloadedSize / 1024.0 / 1024.0),
						decimalFormat.format(maxPb / 1024.0 / 1024.0),
						(int) (((float) downloadedSize / (float) maxPb) * 100) + "%",
						kbps));
				size += length;
				
				if (timer == null) {
					timer = new Timer();
					timer.schedule(new TimerTask() {
						
						@Override
						public void run() {
							kbps = decimalFormat.format(size / 1024.0);
							size = 0;
						}
					}, 0, 1000);
				}
			}

			@Override
			public void downloadEnd() {
			}
		});
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mDownloadUtil.start();
			}
		});
		pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mDownloadUtil.pause();
			}
		});
		resume.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mDownloadUtil.resume();
			}
		});
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mDownloadUtil.delete();
				mProgressBar.setProgress(0);
				total.setText("0%");
			}
		});
		reset.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mDownloadUtil.reset();
			}
		});
	}

}
