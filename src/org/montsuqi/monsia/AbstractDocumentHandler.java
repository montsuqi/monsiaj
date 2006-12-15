/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).
              2002-2006 OZAWA Sakuro.

This module is part of PANDA.

		PANDA is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
to anyone for the consequences of using it or for whether it serves
any particular purpose or works at all, unless he says so in writing.
Refer to the GNU General Public License for full details.

		Everyone is granted permission to copy, modify and redistribute
PANDA, but only under the conditions described in the GNU General
Public License.  A copy of this license is supposed to have been given
to you along with PANDA so you can know your rights and
responsibilities.  It should be in a file named COPYING.  Among other
things, the copyright notice and this notice must be preserved on all
copies.
*/

package org.montsuqi.monsia;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.montsuqi.client.Protocol;
import org.montsuqi.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

abstract class AbstractDocumentHandler extends DefaultHandler {

	protected static final Logger logger = Logger.getLogger(AbstractDocumentHandler.class);

	protected final StringBuffer content;
	protected final Map widgets;
	protected final List topLevels;
	protected final Map properties;
	protected final List signals;
	protected final List accels;

	protected ParserState state;
	protected ParserState prevState;
	protected ParserState startState;

	protected int unknownDepth;
	protected int widgetDepth;
	protected WidgetInfo widget;
	protected String propertyName;
	protected PropertyType propertyType = PropertyType.NONE;

	public AbstractDocumentHandler() {
		super();
		content = new StringBuffer();
		widgets = new HashMap();
		topLevels = new ArrayList();
		properties = new HashMap();
		signals = new ArrayList();
		accels = new ArrayList();

	}

	protected abstract boolean shouldAppendCharactersToContent();

	protected boolean isFinished() {
		return state == FINISH;
	}

	protected void clearContent() {
		content.delete(0, content.length());
	}

	protected void warnNotZero(String name, int actual) {
		Object[] args = { name, new Integer(actual) };
		logger.warn("{0} is not  0, but was {1}", args); //$NON-NLS-1$
	}

	protected void warnUnknownAttribute(String element, String attr) {
		Object[] args = { attr, element };
		logger.warn("unknown attribute {0} for <{1}>", args); //$NON-NLS-1$
	}

	protected void warnMissingAttribute(String element) {
		logger.warn("<{0}> element missing required attributes", element); //$NON-NLS-1$
	}

	protected void warnUnexpectedElement(String outer, String inner) {
		Object[] args = { outer, inner };
		logger.warn("unexpected element <{0}> inside <{1}>", args); //$NON-NLS-1$
	}

	protected void warnNotSupported(String value) {
		logger.warn("{0} is not supported in Java", value); //$NON-NLS-1$
	}

	protected Interface getInterface(Protocol protocol) {
		if (isFinished()) {
			return new Interface(topLevels, protocol);
		}
		throw new IllegalStateException("parsing is not finished yet"); //$NON-NLS-1$
	}

	public void startDocument() throws SAXException {
		state = startState;
		unknownDepth = 0;
		prevState = UNKNOWN;

		widgetDepth = 0;
		clearContent();
		widget = null;
		propertyName = null;
	}

	public void endDocument() throws SAXException {
		if (unknownDepth != 0) {
			warnNotZero("unknownDepth", unknownDepth); //$NON-NLS-1$
		}
		if (widgetDepth != 0) {
			warnNotZero("widgetDepth", widgetDepth); //$NON-NLS-1$
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		state.startElement(uri, localName, qName, attrs);
		clearContent();
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		state.endElement(uri, localName, qName);
	}

	protected final ParserState UNKNOWN = new ParserState("UNKNOWN") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			unknownDepth--;
			if (unknownDepth == 0) {
				state = prevState;
			}
		}
	};

	final ParserState FINISH = new ParserState("FINISH") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("there should be no elements here, but found <{0}>", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			logger.warn("should not be closing any elements in this state"); //$NON-NLS-1$
		}
	};

	public void warning(SAXParseException e) throws SAXException {
		logger.warn(e);
	}

	public void error(SAXParseException e) throws SAXException {
		logger.fatal(e);
	}

	public void fatalError(SAXParseException e) throws SAXException {
		logger.fatal(e);
	}

	protected void flushProperties() {
		if (propertyType == PropertyType.NONE) {
			// do nothing
		} else if (propertyType == PropertyType.WIDGET) {
			if (widget.getProperties().size() != 0) {
				logger.warn("we already read all the props for this key, leaking"); //$NON-NLS-1$
			}
			widget.setProperties(properties);
			properties.clear();
		} else if (propertyType == PropertyType.ATK) {
			properties.clear();
		} else if (propertyType == PropertyType.CHILD) {
			if (widget.getChildren().size() == 0) {
				logger.warn("no children, but have child properties"); //$NON-NLS-1$
			} else {
				ChildInfo info = widget.getLastChild();
				info.setProperties(properties);
			}
			properties.clear();
		} else {
			throw new IllegalStateException("unknown property type"); //$NON-NLS-1$
		}

		propertyType = PropertyType.NONE;
		propertyName = null;
		properties.clear();
	}

	protected void dialogHack(WidgetInfo info) {
		String genericClassName = info.getClassName();
		// handle Window/Dialog specially
		if ("Window".equals(genericClassName)) { //$NON-NLS-1$
			String type = info.getProperty("type"); //$NON-NLS-1$
			if ("WINDOW_DIALOG".equals(removePrefix(type))) { //$NON-NLS-1$
				info.setClassName("Dialog"); //$NON-NLS-1$
			}
		}
	}

	protected void flushSignals() {
		widget.setSignals(signals);
		signals.clear();
	}

	protected void flushAccels() {
		widget.setAccels(accels);
		accels.clear();
	}

	private String removePrefix(String name) {
		if (name.startsWith("GDK_")) { //$NON-NLS-1$
			name = name.substring("GDK_".length()); //$NON-NLS-1$
		}
		if (name.startsWith("GTK_")) { //$NON-NLS-1$
			name = name.substring("GTK_".length()); //$NON-NLS-1$
		}
		return name;
	}

	protected int keyCode(String keyName) {
		final Field[] fields = KeyEvent.class.getDeclaredFields();
		keyName = removePrefix(keyName);
		keyName = keyName.toUpperCase(Locale.ENGLISH);
		if ( ! keyName.startsWith("VK_")) { //$NON-NLS-1$
			keyName = "VK_" + keyName; //$NON-NLS-1$
		}
		if (keyName.length() > 0) {
			for (int i = 0; i < fields.length; i++) {
				if (keyName.equals(fields[i].getName())) {
					try {
						return fields[i].getInt(null);
					} catch (Exception e) {
						logger.warn(e);
						return 0;
					}
				}
			}
		}
		logger.warn("key not found: {0}", keyName); //$NON-NLS-1$
		return 0;
	}

	protected int parseModifiers(String modifierValue) {
		StringTokenizer tokens = new StringTokenizer(modifierValue, " \t\n\r\f|"); //$NON-NLS-1$
		int modifiers = 0;
		while (tokens.hasMoreTokens()) {
			String modifier = tokens.nextToken();
			modifier = removePrefix(modifier);
			if (modifier.equals("SHIFT_MASK")) { //$NON-NLS-1$
				modifiers |= InputEvent.SHIFT_MASK;
			} else if (modifier.equals("LOCK_MASK")) { //$NON-NLS-1$
				warnNotSupported("LOCK_MASK"); //$NON-NLS-1$
			} else if (modifier.equals("CONTROL_MASK")) { //$NON-NLS-1$
				modifiers |= InputEvent.CTRL_MASK;
			} else if (modifier.startsWith("MOD_")) { //$NON-NLS-1$
				warnNotSupported("MOD_MASK"); //$NON-NLS-1$
			} else if (modifier.startsWith("BUTTON") && modifier.length() == 7) { //$NON-NLS-1$
				modifiers |= parseButtonMask(modifier.substring(6));
			} else if (modifier.equals("RELEASE_MASK")) { //$NON-NLS-1$
				warnNotSupported("RELEASE_MASK"); //$NON-NLS-1$
			}
		}
		return modifiers;
	}

	protected int parseButtonMask(String mask) {
		try {
			int value = Integer.parseInt(mask);
			switch (value) {
			case 1:
				return InputEvent.BUTTON1_MASK;
			case 2:
				return InputEvent.BUTTON2_MASK;
			case 3:
				return InputEvent.BUTTON3_MASK;
			default:
				logger.warn("only BUTTON[1-3] are supported in Java"); //$NON-NLS-1$
				return 0;
			}
		} catch (NumberFormatException e) {
			logger.warn("unknown BUTTON #: {0}", mask); //$NON-NLS-1$
			return 0;
		}
	}

	// returns name with all dashes converted to underscores.
	protected String makePropertyName(String name) {
		return name.replace('-', '_');
	}

	public void characters(char[] chars, int start, int length) throws SAXException {
		if (shouldAppendCharactersToContent()) {
			content.append(chars, start, length);
		} else {
			clearContent();
		}
	}
}
