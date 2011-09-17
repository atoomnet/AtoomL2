package net.atoom.android.rss;

import java.util.concurrent.ExecutorService;

import net.atoom.android.l2.L2Activity;
import net.atoom.android.res.ResourceEntity;
import net.atoom.android.res.ResourceLoadListener;
import net.atoom.android.res.ResourceLoadPriority;
import net.atoom.android.res.ResourceLoader;
import net.atoom.android.util.LogBridge;

public class RSSFeedLoader {

	private final ResourceLoader m_resourceLoader;
	private final ExecutorService m_executorService;

	public RSSFeedLoader(ResourceLoader resourceLoader, ExecutorService executorService) {
		m_resourceLoader = resourceLoader;
		m_executorService = executorService;
	}

	public void loadFeed(final String feedUrl, final RSSFeedLoadListener handler, long acceptableStaleTime) {
		if (LogBridge.isLoggable())
			LogBridge.i("FeedLoader : loadFeed : start loading : " + feedUrl);
		m_resourceLoader.loadResource(feedUrl, ResourceLoadPriority.HIGH, new ResourceLoadListener() {
			public void resourceLoaded(final ResourceEntity resourceEntity) {
				m_executorService.submit(new RSSFeedParser(new RSSFeed(resourceEntity), new ThumbnailLoadHandler(
						m_resourceLoader, m_executorService, handler)));
			}
		}, acceptableStaleTime);
	}

	final static class ThumbnailLoadHandler implements RSSFeedParseListener {

		private final ResourceLoader m_resourceLoader;
		private final ExecutorService m_executorService;
		private final RSSFeedLoadListener m_feedLoaderListener;

		public ThumbnailLoadHandler(final ResourceLoader resourceLoader, final ExecutorService executorService,
				final RSSFeedLoadListener handler) {
			m_executorService = executorService;
			m_resourceLoader = resourceLoader;
			m_feedLoaderListener = handler;
		}

		@Override
		public void entryLoaded(final RSSFeedEntry feedEntry) {
			if (m_feedLoaderListener != null) {
				m_executorService.submit(new Runnable() {
					@Override
					public void run() {
						m_feedLoaderListener.entryLoaded(feedEntry);
					}
				});
			}

			m_resourceLoader.loadResource(feedEntry.getThumbnail(), ResourceLoadPriority.LOW,
					new ResourceLoadListener() {
						@Override
						public void resourceLoaded(ResourceEntity resource) {
							feedEntry.setThumbnailResourceEntity(resource);
							if (m_feedLoaderListener != null) {
								m_executorService.submit(new Runnable() {
									@Override
									public void run() {
										m_feedLoaderListener.entryThumbnailLoaded(feedEntry);
									}
								});
							}
						}
					}, L2Activity.THUMB_STALETIME);
		}

		@Override
		public void feedLoaded(final RSSFeed feed) {
			if (m_feedLoaderListener != null) {
				m_executorService.submit(new Runnable() {
					@Override
					public void run() {
						m_feedLoaderListener.feedLoaded(feed);
					}
				});
			}
		}
	}

}
