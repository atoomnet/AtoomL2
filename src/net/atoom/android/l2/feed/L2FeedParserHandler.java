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

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import net.atoom.android.l2.feed.L2FeedEntryContent.ContentType;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class L2FeedParserHandler extends DefaultHandler {

	private final L2FeedParser m_feedParser;
	private HandlerState m_state;

	private StringBuilder m_buffer;

	private L2FeedEntry m_entry;
	private L2FeedEntryComment m_comment;

	private boolean isDisabled = false;

	public L2FeedParserHandler(L2FeedParser parser) {
		m_feedParser = parser;
		setStateHandler(HandlerState.FEED);
	}

	public void setDisabled() {
		isDisabled = true;
	}

	public void setStateHandler(HandlerState state) {
		m_buffer = new StringBuilder();
		m_state = state;
	}

	public StringBuilder getBuffer() {
		return m_buffer;
	}

	public void createNewEntry() {
		m_entry = new L2FeedEntry();
	}

	public void commitEntry() {
		m_feedParser.handleEntry(m_entry);
		m_entry = null;
	}

	public L2FeedEntry getEntry() {
		return m_entry;
	}

	public void createNewComment() {
		m_comment = new L2FeedEntryComment();
	}

	public void commitComment() {
		if (m_entry.getComments() == null)
			m_entry.setComments(new LinkedList<L2FeedEntryComment>());
		m_entry.getComments().add(m_comment);
		m_comment = null;
	}

	public L2FeedEntryComment getComment() {
		return m_comment;
	}

	public void addContent(L2FeedEntryContent c) {
		if (m_entry.getContents() == null)
			m_entry.setContents(new LinkedList<L2FeedEntryContent>());
		m_entry.getContents().add(c);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (isDisabled)
			return;
		m_state.startElement(this, uri, localName, qName, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (isDisabled)
			return;
		m_state.endElement(this, uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (isDisabled)
			return;
		m_state.characters(this, ch, start, length);
	}

	enum HandlerState {
		FEED("feed", null) {
			public void startElement(L2FeedParserHandler handler, String uri, String localName, String qName,
					Attributes attributes) throws SAXException {
				super.startElement(handler, uri, localName, qName, attributes);
				if (localName.equals(ITEM.getTag())) {
					handler.createNewEntry();
					handler.setStateHandler(ITEM);
				}
			}
		},
		ITEM("item", FEED) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				if (localName.equals(getTag()))
					handler.commitEntry();
				super.endElement(handler, uri, localName, qName);
			}
		},
		TITLE("title", ITEM) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				handler.getEntry().setTitle(handler.getBuffer().toString());
				super.endElement(handler, uri, localName, qName);
			}
		},
		TOPIMAGE("top_image", ITEM) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				String imgTag = handler.getBuffer().toString();
				imgTag = imgTag.substring(imgTag.indexOf("src=") + 12);
				imgTag = imgTag.substring(0, imgTag.indexOf("\""));
				imgTag = "http://images.weserv.nl/?url=" + URLEncoder.encode(imgTag) + "&h=200";

				handler.getEntry().setThumbnail(imgTag);
				super.endElement(handler, uri, localName, qName);
			}
		},
		TOPIMAGECAPTION("top_image_caption", ITEM) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				String contentpart = handler.getBuffer().toString().trim();
				contentpart = contentpart.replaceAll("<.+?>", "");
				L2FeedEntryContent c = new L2FeedEntryContent(ContentType.TEXT);
				c.setContent(contentpart);
				handler.addContent(c);
				super.endElement(handler, uri, localName, qName);
			}
		},
		EXTENDEDBODY("extended_body", ITEM) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				String content = handler.getBuffer().toString().trim();
				content = content.replaceAll("<img", "IMG_START");
				String[] contentparts = content.split("IMG_");
				for (String contentpart : contentparts) {
					if (contentpart.startsWith("START")) {
						String imgTag = contentpart.substring(contentpart.indexOf("src=") + 12);
						imgTag = imgTag.substring(0, imgTag.indexOf("\""));
						imgTag = "http://images.weserv.nl/?url=" + URLEncoder.encode(imgTag) + "&h=200";
						contentpart = contentpart.substring(contentpart.indexOf(">") + 1);
						L2FeedEntryContent c = new L2FeedEntryContent(ContentType.IMAGE);
						c.setContent(imgTag);
						handler.addContent(c);
					}
					L2FeedEntryContent c = new L2FeedEntryContent(ContentType.TEXT);
					contentpart = contentpart.trim();
					contentpart = contentpart.replaceAll("<.+?>", "");
					c.setContent(contentpart);
					handler.addContent(c);
				}
				super.endElement(handler, uri, localName, qName);
			}
		},
		DATE("date", ITEM) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				handler.getEntry().setPublished(handler.getBuffer().toString().trim());
				super.endElement(handler, uri, localName, qName);
			}
		},
		LINK("link", ITEM) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				handler.getEntry().setUrl(handler.getBuffer().toString().trim());
				super.endElement(handler, uri, localName, qName);
			}
		},
		COMMENTS("comments", ITEM) {
			@Override
			public void startElement(L2FeedParserHandler handler, String uri, String localName, String qName,
					Attributes attributes) throws SAXException {
				if (COMMENT.getTag().equals(localName)) {
					handler.createNewComment();
					handler.setStateHandler(COMMENT);
				}
			}
		},
		COMMENT("comment", COMMENTS) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				if (localName.equals(getTag()))
					handler.commitComment();
				super.endElement(handler, uri, localName, qName);
			}

		},
		COMMENTAUTHOR("comment_author", COMMENT) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				String author = handler.getBuffer().toString();
				author.trim();
				author = author.replaceAll("<.+?>", "");
				handler.getComment().setAuthor(author);
				super.endElement(handler, uri, localName, qName);
			}
		},
		COMMENTBODY("comment_body", COMMENT) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				String body = handler.getBuffer().toString();
				body.trim();
				body = body.replaceAll("<.+?>", "");
				handler.getComment().setBody(body);
				super.endElement(handler, uri, localName, qName);
			}
		},
		COMMENTDATE("comment_date", COMMENT) {
			@Override
			public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
					throws SAXException {
				String date = handler.getBuffer().toString();
				date.trim();
				handler.getComment().setDate(date);
				super.endElement(handler, uri, localName, qName);
			}
		};

		final static private Map<HandlerState, Map<String, HandlerState>> CHILDSTATELOOKUP = new HashMap<HandlerState, Map<String, HandlerState>>();
		static {
			for (HandlerState parentState : HandlerState.values()) {
				CHILDSTATELOOKUP.put(parentState, new HashMap<String, HandlerState>());
				for (HandlerState childState : HandlerState.values()) {
					if (childState.getParentState() == parentState) {
						CHILDSTATELOOKUP.get(parentState).put(childState.getTag(), childState);
					}
				}
			}
		}

		final private String m_tag;
		final private HandlerState m_parentState;

		private HandlerState(String tag, HandlerState parentState) {
			m_tag = tag;
			m_parentState = parentState;
		}

		public String getTag() {
			return m_tag;
		}

		public HandlerState getParentState() {
			return m_parentState;
		}

		public void startElement(L2FeedParserHandler handler, String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (CHILDSTATELOOKUP.get(this).containsKey(localName))
				handler.setStateHandler(CHILDSTATELOOKUP.get(this).get(localName));
		}

		public void endElement(L2FeedParserHandler handler, String uri, String localName, String qName)
				throws SAXException {
			if (localName.equals(m_tag) && m_parentState != null) {
				handler.setStateHandler(m_parentState);
			}
		}

		public void characters(L2FeedParserHandler handler, char[] ch, int start, int length) throws SAXException {
			for (int i = 0; i < length; i++) {
				handler.getBuffer().append(ch[start + i]);
			}
		}
	}
}
