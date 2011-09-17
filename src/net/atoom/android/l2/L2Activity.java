/**
 *    Copyright 2010 Bram de Kruijff <bdekruijff [at] gmail [dot] com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.atoom.android.l2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.atoom.android.res.ResourceLoader;
import net.atoom.android.rss.RSSFeed;
import net.atoom.android.rss.RSSFeedAdaptor;
import net.atoom.android.rss.RSSFeedEntry;
import net.atoom.android.rss.RSSFeedLoadListener;
import net.atoom.android.rss.RSSFeedLoader;
import net.atoom.android.youtube.Feed;
import net.atoom.android.youtube.FeedAdaptor;
import net.atoom.android.youtube.FeedEntry;
import net.atoom.android.youtube.FeedLoadListener;
import net.atoom.android.youtube.FeedLoader;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

public class L2Activity extends Activity {

	enum AppState {
		RSS, YOUTUBE
	}

	enum ViewState {
		POTRAIT, LANDSCAPE
	}

	enum SpinState {
		LIST, DETAIL
	}

	public final static String LOGGING_TAG = "L2";
	public final static String YOUTUBE_FEED = "http://gdata.youtube.com/feeds/api/users/louloublog/uploads";

	public final static long FEED_STALETIME = 60000 * 60;
	public final static long THUMB_STALETIME = 60000 * 60 * 24;

	private final static long MIN_SPLASHTIME = 5000;
	private final static DateFormat DATE_FORMAT_IN = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private final static DateFormat DATE_FORMAT_OUT = new SimpleDateFormat("MMM dd, yyyy '-' HH:mm");

	private Handler m_handler;
	private ExecutorService m_executorService;
	private ResourceLoader m_resourceLoader;

	private FeedAdaptor m_feedAdaptor;
	private Feed m_feed;

	private RSSFeedAdaptor m_rssFeedAdaptor;
	private RSSFeed m_rssFeed;

	private File m_cacheDir;

	private AppState m_appState;
	private ViewState m_viewState;
	private SpinState m_spinState;

	private long m_startTime = System.currentTimeMillis();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
		}

		createCacheDir();
		// for (File file : m_cacheDir.listFiles()) {
		// file.delete();
		// }

		m_startTime = System.currentTimeMillis();
		m_handler = new Handler();
		m_executorService = Executors.newFixedThreadPool(4);
		m_resourceLoader = new ResourceLoader(m_executorService, m_cacheDir);
		m_feedAdaptor = new FeedAdaptor(this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setTitle(getResources().getText(R.string.hello));
		getWindow().setSoftInputMode(1);

		Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		// int rotation = display.getRotation();
		int rotation = display.getOrientation();
		if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
			m_viewState = ViewState.POTRAIT;
			setContentView(R.layout.l2_portrait);
		} else {
			m_viewState = ViewState.LANDSCAPE;
			setContentView(R.layout.l2_landscape);
		}

		ImageView hv1 = (ImageView) findViewById(R.id.main_header_loulou);
		hv1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (m_spinState == SpinState.DETAIL) {
					spinToList();
				}
				if (m_appState != AppState.RSS) {
					showRSSView();
				}
			}
		});

		ImageView hv2 = (ImageView) findViewById(R.id.main_header_loutube);
		hv2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (m_spinState == SpinState.DETAIL) {
					spinToList();
				}
				if (m_appState != AppState.YOUTUBE) {
					showYouTubeView();
				}
			}
		});

		m_feedAdaptor = new FeedAdaptor(this);
		m_rssFeedAdaptor = new RSSFeedAdaptor(this);

		loadRSSFeed();
		showRSSView();
		loadYoutubeFeed();
		// initializeYoutubeView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("STATE", "blah blah blah");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && m_spinState == SpinState.DETAIL) {
			spinToList();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void spinToList() {
		m_spinState = SpinState.LIST;
		if (m_appState == AppState.YOUTUBE) {
			VideoView videoView = (VideoView) findViewById(R.id.entryview_video);
			videoView.stopPlayback();
		}
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.mainflipper);
		vf.setOutAnimation(vf.getContext(), R.anim.push_right_out);
		vf.setInAnimation(vf.getContext(), R.anim.push_left_in);
		vf.showPrevious();
	}

	private void spinToDetail() {
		m_spinState = SpinState.DETAIL;
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.mainflipper);
		vf.setOutAnimation(vf.getContext(), R.anim.push_left_out);
		vf.setInAnimation(vf.getContext(), R.anim.push_right_in);
		vf.showNext();
	}

	private void showRSSView() {

		m_appState = AppState.RSS;
		
		ViewFlipper vf = (ViewFlipper) findViewById(R.id.mainflipper);
		if (vf.getChildCount() == 2) {
			vf.removeViewAt(1);
		}

		LinearLayout ll;
		if (m_viewState == ViewState.POTRAIT) {
			ll = (LinearLayout) getLayoutInflater().inflate(R.layout.l2_entryview_rss, vf, false);
		} else {
			ll = (LinearLayout) getLayoutInflater().inflate(R.layout.l2_landscape_entryview_rss, vf, false);
		}
		vf.addView(ll);

		ListView listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(m_rssFeedAdaptor);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int index, long time) {

				RSSFeedEntry entry = m_rssFeed.getFeedEntries().get(index);

				// RelativeLayout rl = (RelativeLayout)
				// findViewById(R.id.entryview_rss_fullimageholder);
				// if (rl != null) {
				// rl.setVisibility(View.VISIBLE);
				// ImageView fiv = (ImageView)
				// rl.findViewById(R.id.entryview_rss_fullimage);
				// if (entry.getThumbnailResourceEntity() == null) {
				// fiv.setImageResource(R.drawable.icon);
				// } else {
				// InputStream is;
				// try {
				// is = entry.getThumbnailResourceEntity().getInputStream();
				// fiv.setImageBitmap(BitmapFactory.decodeStream(is));
				// is.close();
				// } catch (IOException e) {
				// }
				// }
				// }

				ImageView imageView = (ImageView) findViewById(R.id.entryview_rss_image);
				if (entry.getThumbnailResourceEntity() != null) {
					InputStream is;
					try {
						is = entry.getThumbnailResourceEntity().getInputStream();
						imageView.setImageBitmap(BitmapFactory.decodeStream(is));
						is.close();
					} catch (IOException e) {
					}
				}

				TextView titleView = (TextView) findViewById(R.id.entryview_rss_title);
				titleView.setText(entry.getTitle());

				TextView dateView = (TextView) findViewById(R.id.entryview_rss_date);
				dateView.setText(entry.getPublished());
				try {
					Date date = RSSFeedAdaptor.DATE_FORMAT_IN.parse(entry.getPublished());
					dateView.setText(DATE_FORMAT_OUT.format(date));
				} catch (ParseException e) {
				}

				TextView tv2 = (TextView) findViewById(R.id.entryview_rss_description);
				tv2.setText(entry.getContent());

				spinToDetail();
			}
		});
	}

	private void loadRSSFeed() {

		new RSSFeedLoader(m_resourceLoader, m_executorService).loadFeed("http://lou2.com/loulou.rss.php",
				new RSSFeedLoadListener() {

					@Override
					public void feedLoaded(RSSFeed feed) {
						m_rssFeed = feed;
						for (RSSFeedEntry feedEntry : m_rssFeed.getFeedEntries()) {
							m_rssFeedAdaptor.addEntry(feedEntry);
						}
						m_handler.post(new Runnable() {
							public void run() {
								m_rssFeedAdaptor.notifyDataSetChanged();
								hideSplashScreen();
							}
						});
					}

					@Override
					public void entryLoaded(final RSSFeedEntry feedEntry) {
					}

					@Override
					public void entryThumbnailLoaded(RSSFeedEntry feedEntry) {
						m_handler.post(new Runnable() {
							public void run() {
								m_rssFeedAdaptor.notifyDataSetChanged();
							}
						});
					}
				}, FEED_STALETIME);
	}

	private void showYouTubeView() {
		
		m_appState = AppState.YOUTUBE;

		ViewFlipper vf = (ViewFlipper) findViewById(R.id.mainflipper);
		if (vf.getChildCount() == 2) {
			vf.removeViewAt(1);
		}

		if (m_viewState == ViewState.POTRAIT) {
			getLayoutInflater().inflate(R.layout.l2_entryview_youtube, vf);
		} else {
			getLayoutInflater().inflate(R.layout.l2_landscape_entryview_youtube, vf);
		}

		ListView listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(m_feedAdaptor);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int index, long time) {

				FeedEntry entry = m_feed.getFeedEntries().get(index);

				TextView titleView = (TextView) findViewById(R.id.entryview_title);
				titleView.setText(entry.getTitle());

				TextView dateView = (TextView) findViewById(R.id.entryview_date);
				try {
					Date date = DATE_FORMAT_IN.parse(entry.getPublished());
					dateView.setText(DATE_FORMAT_OUT.format(date));
				} catch (ParseException e) {
				}

				TextView tv2 = (TextView) findViewById(R.id.entryview_description);
				tv2.setText(entry.getContent());

				VideoView videoView = (VideoView) findViewById(R.id.entryview_video);
				MediaController mc = new MediaController(L2Activity.this);
				mc.setAnchorView(videoView);
				Uri video = Uri.parse(entry.getUrl());
				videoView.setMediaController(mc);
				videoView.setVideoURI(video);
				videoView.requestFocus();
				videoView.start();

				spinToDetail();
			}
		});
	}

	private void loadYoutubeFeed() {

		new FeedLoader(m_resourceLoader, m_executorService).loadFeed(YOUTUBE_FEED, new FeedLoadListener() {

			@Override
			public void feedLoaded(Feed feed) {
				m_feed = feed;
				for (FeedEntry feedEntry : m_feed.getFeedEntries()) {
					m_feedAdaptor.addEntry(feedEntry);
				}
				m_handler.post(new Runnable() {
					public void run() {
						m_feedAdaptor.notifyDataSetChanged();
					}
				});
			}

			@Override
			public void entryLoaded(final FeedEntry feedEntry) {
			}

			@Override
			public void entryThumbnailLoaded(FeedEntry feedEntry) {
				m_handler.post(new Runnable() {
					public void run() {
						m_feedAdaptor.notifyDataSetChanged();
					}
				});
			}
		}, FEED_STALETIME);
	}

	private void hideSplashScreen() {
		long activeTime = System.currentTimeMillis() - m_startTime;
		long delayTime = 0l;
		if (activeTime < MIN_SPLASHTIME) {
			delayTime = MIN_SPLASHTIME - activeTime;
		}
		m_handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				ImageView splashImage = (ImageView) findViewById(R.id.splashscreen);
				splashImage.setVisibility(View.GONE);
			}
		}, delayTime);
	}

	private void createCacheDir() {
		File root = Environment.getExternalStorageDirectory();
		m_cacheDir = new File(root, LOGGING_TAG);
		m_cacheDir.mkdir();
	}
}