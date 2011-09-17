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
package net.atoom.android.youtube;

import java.util.concurrent.ExecutorService;

import net.atoom.android.l2.L2Activity;
import net.atoom.android.res.ResourceEntity;
import net.atoom.android.res.ResourceLoadListener;
import net.atoom.android.res.ResourceLoadPriority;
import net.atoom.android.res.ResourceLoader;
import net.atoom.android.util.LogBridge;

public final class FeedLoader {

	private final ResourceLoader m_resourceLoader;
	private final ExecutorService m_executorService;

	public FeedLoader(ResourceLoader resourceLoader, ExecutorService executorService) {
		m_resourceLoader = resourceLoader;
		m_executorService = executorService;
	}

	public void loadFeed(final String feedUrl, final FeedLoadListener handler, long acceptableStaleTime) {
		if (LogBridge.isLoggable())
			LogBridge.i("FeedLoader : loadFeed : start loading : " + feedUrl);
		m_resourceLoader.loadResource(feedUrl, ResourceLoadPriority.HIGH, new ResourceLoadListener() {
			public void resourceLoaded(final ResourceEntity resourceEntity) {
				m_executorService.submit(new FeedParser(new Feed(resourceEntity), new ThumbnailLoadHandler(
						m_resourceLoader, m_executorService, handler)));
			}
		}, acceptableStaleTime);
	}

	final static class ThumbnailLoadHandler implements FeedParseListener {

		private final ResourceLoader m_resourceLoader;
		private final ExecutorService m_executorService;
		private final FeedLoadListener m_feedLoaderListener;

		public ThumbnailLoadHandler(final ResourceLoader resourceLoader, final ExecutorService executorService,
				final FeedLoadListener handler) {
			m_executorService = executorService;
			m_resourceLoader = resourceLoader;
			m_feedLoaderListener = handler;
		}

		@Override
		public void entryLoaded(final FeedEntry feedEntry) {
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
		public void feedLoaded(final Feed feed) {
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
