/**
 *    Copyright 2009 Bram de Kruijff <bdekruijff [at] gmail [dot] com>
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
package net.atoom.android.res;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;

import net.atoom.android.l2.L2Activity;
import net.atoom.android.util.LRUCache;
import net.atoom.android.util.LogBridge;
import android.util.Log;

public class ResourceLoader {

	private final static int OBJECT_CACHE_SIZE = 100;

	private final LRUCache<String, ResourceEntity> m_resourceCache = new LRUCache<String, ResourceEntity>(
			OBJECT_CACHE_SIZE);
	private final PriorityBlockingQueue<ResourceLoadRequest> m_resourceLoadRequests = new PriorityBlockingQueue<ResourceLoadRequest>();
	private final HttpConnection m_httpConnection = new HttpConnection();

	private final ExecutorService m_executorService;
	private final File m_baseDir;

	public ResourceLoader(ExecutorService executorService, File baseDir) {
		m_executorService = executorService;
		m_baseDir = baseDir;
		m_executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						final ResourceLoadRequest resourceLoadRequest = m_resourceLoadRequests.take();
						final ResourceEntity httpResource = doLoadResource(resourceLoadRequest);
						if (resourceLoadRequest.getResourceLoadCompletionHandler() != null) {
							m_executorService.submit(new Runnable() {
								@Override
								public void run() {
									resourceLoadRequest.getResourceLoadCompletionHandler().resourceLoaded(httpResource);
								}
							});
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void loadResource(String uri, ResourceLoadPriority resourceLoadPriority,
			ResourceLoadListener resourceLoadListener, long allowedStaleTime) {
		if (uri == null || uri.equals(""))
			return;
		m_resourceLoadRequests.offer(new ResourceLoadRequest(uri, resourceLoadPriority, resourceLoadListener,
				allowedStaleTime));
	}

	public void loadResource(String uri, ResourceLoadPriority resourceLoadPriority,
			ResourceLoadListener resourceLoadListener) {
		loadResource(uri, resourceLoadPriority, resourceLoadListener, 0l);
	}

	private ResourceEntity doLoadResource(final ResourceLoadRequest resourceLoadRequest) {

		ResourceEntity resourceEntity = m_resourceCache.get(resourceLoadRequest.getPageUrl());
		File resourceEntityFile = new File(m_baseDir, "re" + resourceLoadRequest.getPageUrl().hashCode() + ".ser");
		if (resourceEntity != null) {
			if (LogBridge.isLoggable())
				LogBridge.i("ResourceLoader : doLoadResource : entity in objectcache : " + resourceLoadRequest);
		} else {
			if (resourceEntityFile.exists()) {
				if (LogBridge.isLoggable())
					LogBridge.i("ResourceLoader : doLoadResource : entity in filecache : " + resourceLoadRequest);
				ObjectInputStream ois = null;
				try {
					ois = new ObjectInputStream(new FileInputStream(resourceEntityFile));
					resourceEntity = (ResourceEntity) ois.readObject();
					resourceEntity.setCacheFile(new File(m_baseDir, "re" + resourceLoadRequest.getPageUrl().hashCode()
							+ ".bin"));
				} catch (Exception e) {
					if (LogBridge.isLoggable())
						LogBridge.w("ResourceLoader : doLoadResource : entity load failed : " + resourceLoadRequest);
					resourceEntityFile.delete();
				} finally {
					if (ois != null) {
						try {
							ois.close();
						} catch (IOException e) {
							if (LogBridge.isLoggable())
								LogBridge.w("ResourceLoader : doLoadResource : entity load failed : "
										+ resourceLoadRequest);
						}
					}
				}
			}
		}

		if (resourceEntity != null) {
			if (System.currentTimeMillis() < resourceEntity.getExpires()) {
				if (LogBridge.isLoggable())
					LogBridge.i("ResourceLoader : doLoadResource : entity is FRESH : " + resourceLoadRequest);
				return resourceEntity;
			}
			if (System.currentTimeMillis() < (resourceEntity.getExpires() + resourceLoadRequest.getAllowedStaleTime())) {
				if (LogBridge.isLoggable())
					LogBridge.i("ResourceLoader : doLoadResource : entity is STALE : " + resourceLoadRequest);
				return resourceEntity;
			}

			if (m_httpConnection.isModified(resourceEntity)) {
				if (LogBridge.isLoggable())
					LogBridge.i("ResourceLoader : doLoadResource : entity is UNMODIFIED : " + resourceLoadRequest);
				resourceEntity.setCreated(System.currentTimeMillis());
				return resourceEntity;
			}
			if (LogBridge.isLoggable())
				LogBridge.i("ResourceLoader : doLoadResource : entity is EXPIRED : " + resourceLoadRequest);
			m_resourceCache.remove(resourceLoadRequest.getPageUrl());
		}

		resourceEntity = new ResourceEntity(resourceLoadRequest.getPageUrl());
		resourceEntity.setCacheFile(new File(m_baseDir, "re" + resourceLoadRequest.getPageUrl().hashCode() + ".bin"));

		m_httpConnection.loadResource(resourceEntity);
		if (resourceEntity != null) {
			m_resourceCache.put(resourceLoadRequest.getPageUrl(), resourceEntity);
			if (LogBridge.isLoggable())
				LogBridge.i("ResourceLoader : doLoadResource : entity loaded : " + resourceLoadRequest);

			// async?
			ObjectOutputStream oos = null;
			try {
				if (!resourceEntityFile.exists()) {
					resourceEntityFile.createNewFile();
				}
				oos = new ObjectOutputStream(new FileOutputStream(resourceEntityFile));
				oos.writeObject(resourceEntity);
				Log.i(L2Activity.LOGGING_TAG, "entity stored in filecache : " + resourceEntity.toString());
			} catch (Exception e) {
				if (LogBridge.isLoggable())
					LogBridge.w("ResourceLoader : doLoadResource : entity store failed : " + resourceLoadRequest);
			} finally {
				if (oos != null) {
					try {
						oos.close();
					} catch (IOException e) {
						if (LogBridge.isLoggable())
							LogBridge.w("ResourceLoader : doLoadResource : entity store failed : "
									+ resourceLoadRequest);
					}
				}
			}
		}
		if (LogBridge.isLoggable())
			LogBridge.w("ResourceLoader : doLoadResource : entity returned : " + resourceLoadRequest);
		return resourceEntity;
	}
}
