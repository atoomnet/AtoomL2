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
package net.atoom.android.l2.feed;

import java.util.List;

import net.atoom.android.res.ResourceEntity;

public class L2FeedEntry {

	private String m_title;
	private String m_url;
	private String m_published;
	private String m_thumbnail;
	private List<L2FeedEntryContent> m_contents;
	private List<L2FeedEntryComment> m_comments;
	private ResourceEntity m_thumbnailResourceEntity;

	public L2FeedEntry() {
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getUrl() {
		return m_url;
	}

	public void setUrl(String url) {
		m_url = url;
	}

	public String getPublished() {
		return m_published;
	}

	public void setPublished(String published) {
		m_published = published;
	}

	public String getThumbnail() {
		return m_thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		m_thumbnail = thumbnail;
	}

	public List<L2FeedEntryComment> getComments() {
		return m_comments;
	}

	public void setComments(List<L2FeedEntryComment> comments) {
		m_comments = comments;
	}

	public List<L2FeedEntryContent> getContents() {
		return m_contents;
	}

	public void setContents(List<L2FeedEntryContent> contents) {
		m_contents = contents;
	}

	public ResourceEntity getThumbnailResourceEntity() {
		return m_thumbnailResourceEntity;
	}

	public void setThumbnailResourceEntity(ResourceEntity resourceEntity) {
		m_thumbnailResourceEntity = resourceEntity;
	}
}
