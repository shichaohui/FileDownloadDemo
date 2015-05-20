package com.example.test;

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
		delete = (Button) findViewById(R.id.button_delete);
		reset = (Button) findViewById(R.id.button_reset);
		total = (TextView) findViewById(R.id.textView_total);
		// String urlString = "http://bbra.cn/Uploadfiles/imgs/20110303/fengjin/013.jpg";
		String urlString = "http://xz.cr173.com/soft1/shenmtwltb.apk";
		// String urlString = "http://meiriq-file.b0.upaiyun.com/gamesbox-site/bugeitangjiudaodana.apk";
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

			@Override
			public void downloadProgress(int downloadedSize) {
				mProgressBar.setProgress(downloadedSize);
				total.setText(downloadedSize + "\u0008/\u0008" + maxPb
						+ "\u0008\u0008\u0008\u0008" + (int) downloadedSize
						* 100 / maxPb + "%");
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
