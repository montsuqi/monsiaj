package org.montsuqi.monsia;

import org.xml.sax.Attributes;

public abstract class ParserState {

	public ParserState(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public abstract void startElement(String uri, String localName, String qName, Attributes attrs);
	public abstract void endElement(String uri, String localName, String qName);

	private final String name;
}
