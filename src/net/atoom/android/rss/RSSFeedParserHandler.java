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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RSSFeedParserHandler extends DefaultHandler {

	private final RSSFeedParser m_feedParser;

	private boolean m_entryFlag = false;
	private boolean m_titleFlag = false;
	private boolean m_contentFlag = false;
	private int m_contentState = 0;
	private boolean m_linkFlag = false;
	private boolean m_publishedFlag = false;

	private RSSFeedEntry m_entry;

	public RSSFeedParserHandler(RSSFeedParser feedParser) {
		m_feedParser = feedParser;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (localName.equals("item")) {
			m_entryFlag = true;
			m_entry = new RSSFeedEntry();
		} else if (m_entryFlag) {
			if (localName.equals("title")) {
				m_titleFlag = true;
			} else if (localName.equals("description")) {
				m_contentFlag = true;
				m_contentState = 0;
			} else if (localName.equals("link")) {
				m_linkFlag = true;
			} else if (localName.equals("pubDate")) {
				m_publishedFlag = true;
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("item")) {
			final RSSFeedEntry entry = m_entry;
			m_entry = null;
			m_entryFlag = false;
			m_feedParser.handleEntry(entry);
		} else if (m_entryFlag) {
			if (localName.equals("title")) {
				m_titleFlag = false;
			} else if (localName.equals("description")) {
				m_contentFlag = false;
				m_contentState = 0;
			} else if (localName.equals("link")) {
				m_linkFlag = false;
			} else if (localName.equals("pubDate")) {
				m_publishedFlag = false;
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (m_entryFlag) {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < length; i++) {
				b.append(ch[start + i]);
			}
			if (m_titleFlag) {
				m_entry.setTitle(b.toString());
			} else if (m_contentFlag) {
				if (m_contentState == 0) {
					String imgTag = b.toString();
					imgTag = imgTag.substring(imgTag.indexOf("src=") + 5);
					imgTag = imgTag.substring(0, imgTag.indexOf("\""));
					m_entry.setThumbnail(imgTag);
					m_contentState = 1;
				} else if (m_contentState == 1) {
					m_contentState = 2;
					m_entry.setContent("");
				} else if (m_contentState == 2) {
					m_entry.setContent(m_entry.getContent() + b.toString());
				}
			} else if (m_publishedFlag) {
				m_entry.setPublished(b.toString());
			} else if (m_linkFlag) {
				m_entry.setUrl(b.toString());
			}
		}
	}
}
