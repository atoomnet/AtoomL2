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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.atoom.android.util.LogBridge;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class FeedParser implements Runnable {

	private final FeedParseListener m_handler;
	private final Feed m_feed;

	public FeedParser(Feed youtubeFeed, FeedParseListener callbackHandler) {
		m_handler = callbackHandler;
		m_feed = youtubeFeed;
	}

	@Override
	public void run() {
		if (LogBridge.isLoggable())
			LogBridge.i("FeedParser : run : started : " + m_feed.getUrl());
		FeedParserHandler parserHandler = new FeedParserHandler(this);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(parserHandler);
			InputSource input = new InputSource(m_feed.getResourceEntity().getInputStream());
			xr.parse(input);
			if (m_handler != null) {
				m_handler.feedLoaded(m_feed);
			}
			if (LogBridge.isLoggable())
				LogBridge.i("FeedParser : run : completed : " + m_feed.getUrl());
		} catch (Exception e) {
		}
	}

	public void handleEntry(final FeedEntry entry) {
		if (entry.getUrl() == null) {
			//hotfix for restricted entries
			LogBridge.w("Entry url is empty : " + entry);
			return;
		}
		m_feed.addEntry(entry);
		if (m_handler != null) {
			m_handler.entryLoaded(entry);
		}
	}
}