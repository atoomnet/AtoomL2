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

import net.atoom.android.res.ResourceEntity;

public class L2FeedEntryContent {

	private final ContentType m_type;
	private String m_content;
	private ResourceEntity m_entity;

	public enum ContentType {
		TEXT, IMAGE
	}

	public L2FeedEntryContent(ContentType type) {
		m_type = type;
	}

	public ContentType getContentType() {
		return m_type;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public ResourceEntity getResourceEntity() {
		return m_entity;
	}

	public void setResourceEntity(ResourceEntity entity) {
		m_entity = entity;
	}
}
