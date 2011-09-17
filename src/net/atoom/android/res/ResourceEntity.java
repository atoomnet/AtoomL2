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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class ResourceEntity implements Serializable {

	private static final long serialVersionUID = -6174617000007667190L;

	private final String m_uri;
	private long m_created;
	private long m_expires;
	private String m_etag;

	private transient File m_cacheFile;

	public File getCacheFile() {
		return m_cacheFile;
	}

	public void setCacheFile(File cacheFile) {
		m_cacheFile = cacheFile;
	}

	public ResourceEntity(String uri) {
		m_uri = uri;
		m_created = m_expires = System.currentTimeMillis();
	}

	public String getUri() {
		return m_uri;
	}

	public long getCreated() {
		return m_created;
	}

	public void setCreated(long created) {
		m_created = created;
	}

	public long getExpires() {
		return m_expires;
	}

	public void setExpires(long expires) {
		m_expires = expires;
	}

	public String getETag() {
		return m_etag;
	}

	public void setETag(final String eTag) {
		m_etag = eTag;
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(m_cacheFile);
	}

	public OutputStream getOutputStream() throws IOException {
		if (m_cacheFile.exists())
			m_cacheFile.createNewFile();
		return new FileOutputStream(m_cacheFile);
	}
}
