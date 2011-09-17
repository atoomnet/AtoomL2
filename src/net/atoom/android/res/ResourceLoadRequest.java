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

public class ResourceLoadRequest implements Comparable<ResourceLoadRequest> {

	private final String m_pageUrl;
	private final long m_timestamp;
	private final ResourceLoadPriority m_resourceLoadPriority;
	private final ResourceLoadListener m_resourceLoadListener;
	private final long m_allowedStaleTime;

	public ResourceLoadRequest(String pageUrl, ResourceLoadPriority resourceLoadPriority,
			ResourceLoadListener resourceLoadListener, long allowedStaleTime) {
		m_pageUrl = pageUrl;
		m_timestamp = System.currentTimeMillis();
		m_resourceLoadPriority = resourceLoadPriority;
		m_resourceLoadListener = resourceLoadListener;
		m_allowedStaleTime = allowedStaleTime;
	}

	public String getPageUrl() {
		return m_pageUrl;
	}

	public ResourceLoadPriority getResourceLoadPriority() {
		return m_resourceLoadPriority;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public ResourceLoadListener getResourceLoadCompletionHandler() {
		return m_resourceLoadListener;
	}

	public long getAllowedStaleTime() {
		return m_allowedStaleTime;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PageLoadRequest{");
		sb.append(getResourceLoadPriority());
		sb.append(",");
		sb.append(getPageUrl());
		sb.append(",");
		sb.append(getTimestamp());
		sb.append("}");
		return sb.toString();
	}

	@Override
	public int compareTo(ResourceLoadRequest other) {
		if (getResourceLoadPriority() == ResourceLoadPriority.HIGH) {
			if (other.getResourceLoadPriority() == ResourceLoadPriority.LOW) {
				return -1;
			}
			return (int) (getTimestamp() - other.getTimestamp());
		}
		if (other.getResourceLoadPriority() == ResourceLoadPriority.HIGH) {
			return 1;
		}
		return (int) (getTimestamp() - other.getTimestamp());
	}
}
