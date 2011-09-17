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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FeedParserHandler extends DefaultHandler {

	private final FeedParser m_feedParser;

	private boolean m_entryFlag = false;
	private boolean m_titleFlag = false;
	private boolean m_contentFlag = false;
	private boolean m_publishedFlag = false;

	private FeedEntry m_entry;

	public FeedParserHandler(FeedParser feedParser) {
		m_feedParser = feedParser;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (localName.equals("entry")) {
			m_entryFlag = true;
			m_entry = new FeedEntry();
		} else if (m_entryFlag) {
			if (uri.equals("http://www.w3.org/2005/Atom") && localName.equals("title")) {
				m_titleFlag = true;
			} else if (uri.equals("http://www.w3.org/2005/Atom") && localName.equals("content")) {
				m_contentFlag = true;
			} else if (uri.equals("http://search.yahoo.com/mrss/") && localName.equals("content")) {
				String type = attributes.getValue("type");
				if (type != null && type.equals("video/3gpp")) {
					m_entry.setUrl(attributes.getValue("url"));
				}
			} else if (uri.equals("http://www.w3.org/2005/Atom") && localName.equals("published")) {
				m_publishedFlag = true;
			} else if (uri.equals("http://search.yahoo.com/mrss/") && localName.equals("thumbnail")) {
				if (m_entry.getThumbnail() == null) {
					m_entry.setThumbnail(attributes.getValue("url"));
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("entry")) {
			final FeedEntry entry = m_entry;
			m_entry = null;
			m_entryFlag = false;
			m_feedParser.handleEntry(entry);
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
				m_titleFlag = false;
				m_entry.setTitle(b.toString());
			} else if (m_contentFlag) {
				m_contentFlag = false;
				m_entry.setContent(b.toString());
			} else if (m_publishedFlag) {
				m_publishedFlag = false;
				m_entry.setPublished(b.toString());
			}
		}
	}
}
