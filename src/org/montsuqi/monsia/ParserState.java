package org.montsuqi.monsia;

import org.montsuqi.util.Logger;
import org.xml.sax.Attributes;

abstract class ParserState {

	private Logger logger;

	ParserState(String name) {
		this.name = name;
		logger = Logger.getLogger(ParserState.class);
	}

	abstract void startElement(String uri, String localName, String qName, Attributes attrs);
	abstract void endElement(String uri, String localName, String qName);

	private final String name;

	protected void warnShouldFindClosing(String element, String found) {
		logger.warn(Messages.getString("MonsiaHandler.should_find_closing"), new Object[] { element, found }); //$NON-NLS-1$
	}

	protected void warnShouldBeEmpty(String element, String found) {
		logger.warn(Messages.getString("MonsiaHandler.should_be_empty"), new Object[] { element, found }); //$NON-NLS-1$
	}

	protected void warnShouldHaveNoAttributes(String element) {
		logger.warn(Messages.getString("MonsiaHandler.should_have_no_attributes"), element); //$NON-NLS-1$
	}

	protected void warnInvalidPropertiesDefinedHere(String element) {
		logger.warn(Messages.getString("MonsiaHandler.invalid_properties_defined_here"), element); //$NON-NLS-1$
	}

	public String getName() {
		return name;
	}
}
