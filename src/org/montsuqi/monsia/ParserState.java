package org.montsuqi.monsia;

import org.xml.sax.Attributes;

abstract class ParserState {

	ParserState(String name) {
		this.name = name;
	}

	abstract void startElement(String uri, String localName, String qName, Attributes attrs);
	abstract void endElement(String uri, String localName, String qName);

	private final String name;
}
