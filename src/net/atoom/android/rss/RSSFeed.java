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
package net.atoom.android.rss;

import java.util.LinkedList;
import java.util.List;

import net.atoom.android.res.ResourceEntity;

public class RSSFeed {

	private ResourceEntity m_resourceEntity;
	private List<RSSFeedEntry> m_feedEntries;

	public RSSFeed(ResourceEntity resourceEntity) {
		m_resourceEntity = resourceEntity;
		m_feedEntries = new LinkedList<RSSFeedEntry>();
	}

	public String getUrl() {
		return m_resourceEntity.getUri();
	}

	public ResourceEntity getResourceEntity() {
		return m_resourceEntity;
	}

	public List<RSSFeedEntry> getFeedEntries() {
		return m_feedEntries;
	}

	public boolean addEntry(RSSFeedEntry feedEntry) {
		return m_feedEntries.add(feedEntry);
	}

	public boolean removeEntry(RSSFeedEntry feedEntry) {
		return m_feedEntries.remove(feedEntry);
	}
}
