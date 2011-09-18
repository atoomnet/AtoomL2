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


public class L2FeedEntryComment {

	private String m_author;
	private String m_body;
	private String m_date;

	public L2FeedEntryComment() {
	}

	public String getAuthor() {
		return m_author;
	}

	public void setAuthor(String author) {
		m_author = author;
	}

	public String getBody() {
		return m_body;
	}

	public void setBody(String body) {
		m_body = body;
	}

	public String getDate() {
		return m_date;
	}

	public void setDate(String date) {
		m_date = date;
	}
}
