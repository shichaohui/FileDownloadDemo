package com.example.test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.test.DownloadUtil.OnDownloadListener;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private ListView listView = null;
	private List<String> urls = null;
	private DownloadUtil downloadUtil = null;
	private final String TAG_PROGRESS = "_progress";
	private final String TAG_TOTAL = "_total";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.listview);

		urls = new ArrayList<String>();
		urls.add("http://pc1.gamedog.cn/big/game/dongzuo/102631/shenmiaotw2_yxdog.apk");
		urls.add("http://pc1.gamedog.cn/big/game/yizhi/67450/baoweiluobo_an_yxdog.apk");
		urls.add("http://pc1.gamedog.cn/big/game/yizhi/161623/zhiwudzjs2gqb_an.apk");

		listView.setAdapter(myAdapter);

		downloadUtil = DownloadUtil.getInstance(this);

		downloadUtil.setOnDownloadListener(new OnDownloadListener() {

			String text = "已下载%sM / 共%sM \n占比%s  \n下载速度%skb/s";
			DecimalFormat decimalFormat = new DecimalFormat("#.##"); // 小数格式化
			Timer timer = null;
			Map<String, DownloadingInfo> downloadingInfos = new HashMap<String, DownloadingInfo>();

			@Override
			public void downloadStart(String url, int fileSize) {
				DownloadingInfo info = new DownloadingInfo();
				info.setFileSize(fileSize);
				downloadingInfos.put(url, info);
				((ProgressBar) listView.findViewWithTag(url + TAG_PROGRESS))
						.setMax(fileSize);
			}

			@Override
			public synchronized void downloadProgress(String url,
					int downloadedSize, int length) {
				DownloadingInfo info = downloadingInfos.get(url);
				if (info != null) {
					((ProgressBar) listView.findViewWithTag(url + TAG_PROGRESS))
							.setProgress(downloadedSize);
					((TextView) listView.findViewWithTag(url + TAG_TOTAL)).setText(String.format(
							text,
							decimalFormat
									.format(downloadedSize / 1024.0 / 1024.0),
							decimalFormat.format(info.getFileSize() / 1024.0 / 1024.0),
							(int) (((float) downloadedSize / (float) info
									.getFileSize()) * 100) + "%", info
									.getKbps()));
					info.setSecondSize(info.getSecondSize() + length);
				}
				if (timer == null) {
					timer = new Timer();
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							DownloadingInfo info = null;
							for (Entry<String, DownloadingInfo> entry : downloadingInfos
									.entrySet()) {
								info = entry.getValue();
								if (info != null) {
									info.setKbps(decimalFormat.format(info
											.getSecondSize() / 1024.0));
									info.setSecondSize(0);
								}
							}
						}
					}, 0, 1000);
				}
			}

			@Override
			public void downloadEnd(String url) {
				DownloadingInfo info = downloadingInfos.get(url);
				if (info != null) {
					((ProgressBar) listView.findViewWithTag(url + TAG_PROGRESS))
							.setProgress(info.getFileSize());
					((TextView) listView.findViewWithTag(url + TAG_TOTAL))
							.setText(String.format(
									text,
									decimalFormat.format(info.getFileSize() / 1024.0 / 1024.0),
									decimalFormat.format(info.getFileSize() / 1024.0 / 1024.0),
									"100%", info.getKbps()));
					downloadingInfos.remove(url);
				}
			}

		});

	}

	BaseAdapter myAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Holder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(MainActivity.this).inflate(
						R.layout.list_item, null);
				holder = new Holder();
				holder.tv_url = (TextView) convertView.findViewById(R.id.url);
				holder.progressBar = (ProgressBar) convertView
						.findViewById(R.id.progressBar);
				holder.textView_total = (TextView) convertView
						.findViewById(R.id.textView_total);
				holder.button_start = (Button) convertView
						.findViewById(R.id.button_start);
				holder.button_pause = (Button) convertView
						.findViewById(R.id.button_pause);
				holder.button_resume = (Button) convertView
						.findViewById(R.id.button_resume);
				holder.button_delete = (Button) convertView
						.findViewById(R.id.button_delete);

				convertView.setTag(holder);

				setClick(holder);

			} else {
				holder = (Holder) convertView.getTag();
			}

			holder.tv_url.setText(urls.get(position));

			holder.progressBar.setTag(urls.get(position) + TAG_PROGRESS);
			holder.textView_total.setTag(urls.get(position) + TAG_TOTAL);
			holder.button_start.setTag(urls.get(position));
			holder.button_pause.setTag(urls.get(position));
			holder.button_resume.setTag(urls.get(position));
			holder.button_delete.setTag(urls.get(position));

			return convertView;
		}

		private void setClick(Holder holder) {
			holder.button_start.setOnClickListener(MainActivity.this);
			holder.button_pause.setOnClickListener(MainActivity.this);
			holder.button_resume.setOnClickListener(MainActivity.this);
			holder.button_delete.setOnClickListener(MainActivity.this);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return urls.get(position);
		}

		@Override
		public int getCount() {
			return urls.size();
		}

		class Holder {
			TextView tv_url = null;
			ProgressBar progressBar = null;
			TextView textView_total = null;
			Button button_start = null;
			Button button_pause = null;
			Button button_resume = null;
			Button button_delete = null;
		}
	};

	@Override
	public void onClick(View view) {
		String url = view.getTag() == null ? "" : view.getTag().toString();
		switch (view.getId()) {
		case R.id.button_start:
			downloadUtil.prepare(url);
			break;
		case R.id.button_pause:
			downloadUtil.pause(url);
			break;
		case R.id.button_resume:
			downloadUtil.resume(url);
			break;
		case R.id.button_delete:
			downloadUtil.delete(url);
			break;

		default:
			break;
		}
	}

}
