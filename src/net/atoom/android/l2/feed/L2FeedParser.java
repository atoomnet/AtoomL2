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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.atoom.android.util.LogBridge;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class L2FeedParser implements Runnable {

	private final L2Feed m_feed;
	private final L2FeedParseListener m_handler;
	private L2FeedParserHandler m_parseHandler;
	
	public L2FeedParser(L2Feed youtubeFeed, L2FeedParseListener callbackHandler) {
		m_feed = youtubeFeed;
		m_handler = callbackHandler;
		m_parseHandler = new L2FeedParserHandler(this);
	}

	@Override
	public void run() {
		if (LogBridge.isLoggable())
			LogBridge.i("RSSFeedParser : run : started : " + m_feed.getUrl());
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(m_parseHandler);
			InputSource input = new InputSource(m_feed.getResourceEntity().getInputStream());
			input.setEncoding("ISO-8859-1");
			xr.parse(input);
			if (m_handler != null) {
				m_handler.feedLoaded(m_feed);
			}
			if (LogBridge.isLoggable())
				LogBridge.i("RSSFeedParser : run : completed : " + m_feed.getUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int count = 0;
	public void handleEntry(final L2FeedEntry entry) {
		if(count >= 30) {
			m_parseHandler.setDisabled();
			return;
		}
		count++;
		m_feed.addEntry(entry);
		if (m_handler != null) {
			m_handler.entryLoaded(entry);
		}
	}
}