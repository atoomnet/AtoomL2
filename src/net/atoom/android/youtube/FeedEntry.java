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

import net.atoom.android.res.ResourceEntity;

public class FeedEntry {

	private String m_title;
	private String m_content;
	private String m_url;
	private String m_published;
	private String m_thumbnail;
	private ResourceEntity m_thumbnailResourceEntity;

	public FeedEntry() {
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
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

	public ResourceEntity getThumbnailResourceEntity() {
		return m_thumbnailResourceEntity;
	}

	public void setThumbnailResourceEntity(ResourceEntity resourceEntity) {
		m_thumbnailResourceEntity = resourceEntity;
	}
}
