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

/** <p>Abstract base class for Glade1Handler and MonsiaHandler.</p>
 * <p>Thid document parser runs as a state transition machine.
 * Subclass must instantiate ParserState instances for each element to make this work.</p>
 */
abstract class AbstractDocumentHandler extends DefaultHandler {

	protected static final Logger logger = Logger.getLogger(AbstractDocumentHandler.class);

	protected final StringBuffer content;
	protected final Map widgets;
	protected final List topLevels;
	protected final HashMap properties;
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

	/** <p>Constructs and initializes the instance.</p>
	 */
	public AbstractDocumentHandler() {
		super();
		content = new StringBuffer();
		widgets = new HashMap();
		topLevels = new ArrayList();
		properties = new HashMap();
		signals = new ArrayList();
		accels = new ArrayList();

	}

	/** <p>Tests if current characters should be appended to the content.</p>
	 * 
	 * @return true if current characters are part of the con.tent. false otherwise.
	 */
	protected abstract boolean shouldAppendCharactersToContent();

	/** <p>Tests if the parsing has ended.</p>
	 * 
	 * @return true if parsing has ended(end tag for the root element is found). false otherwise.
	 */
	protected boolean isFinished() {
		return state == FINISH;
	}

	/** <p>Empties the content buffer.</p>
	 */
	protected void clearContent() {
		content.delete(0, content.length());
	}

	/** <p>Warning helper method that warns about an attribute which should be zero but not.</p>
	 * 
	 * @param name an attribute that is expected to be zero.
	 * @param actual the attribute's actual value.
	 */
	protected void warnNotZero(String name, int actual) {
		Object[] args = { name, new Integer(actual) };
		logger.warn("{0} is not  0, but was {1}", args); //$NON-NLS-1$
	}

	/** <p>Warning helper method that warns about an unknown attribute.</p>
	 * 
	 * @param element the element interested.
	 * @param attr an attribute detected but unknown.
	 */
	protected void warnUnknownAttribute(String element, String attr) {
		Object[] args = { attr, element };
		logger.warn("unknown attribute {0} for <{1}>", args); //$NON-NLS-1$
	}

	/** <p>Warning helper method that warns that some attributes are missing.</p>
	 * 
	 * @param element the element interested.
	 */
	protected void warnMissingAttribute(String element) {
		logger.warn("<{0}> element missing required attributes", element); //$NON-NLS-1$
	}

	/** <p>Warning helper method that warns unexpected element nesting is found.</p>
	 * 
	 * @param outer the outer method who does not expect the <var>inner</var> method.
	 * @param inner the inner method who is not expected to be a child of <var>outer</var>.
	 */
	protected void warnUnexpectedElement(String outer, String inner) {
		Object[] args = { outer, inner };
		logger.warn("unexpected element <{0}> inside <{1}>", args); //$NON-NLS-1$
	}

	/** <p>Warning helper method that warns about an attribute alue which is not supported in Java.</p>
	 * 
	 * @param value the unsupported value name.
	 */
	protected void warnNotSupported(String value) {
		logger.warn("{0} is not supported in Java", value); //$NON-NLS-1$
	}

	/** <p>Builds the interface as the result of parsing.</p>
	 * 
	 * @param protocol the protocol used for event binding(connecting).
	 * @return the Interface instance.
	 */
	protected Interface getInterface(Protocol protocol) {
		if (isFinished()) {
			return new Interface(topLevels, protocol);
		}
		throw new IllegalStateException("parsing is not finished yet"); //$NON-NLS-1$
	}
	
	/** <p>Builds the interface as the result of parsing.</p>
	 * 
	 * @return the Interface instance.
	 */
	protected Interface getInterface() {
		if (isFinished()) {
			return new Interface(topLevels);
		}
		throw new IllegalStateException("parsing is not finished yet"); //$NON-NLS-1$
	}	

	/** <p>SAX handler called at the start of a document.</p>
	 * <p>Initializes parser state and variables.</p>
	 */
    @Override
	public void startDocument() throws SAXException {
		state = startState;
		unknownDepth = 0;
		prevState = UNKNOWN;

		widgetDepth = 0;
		clearContent();
		widget = null;
		propertyName = null;
	}

	/** <p>SAX handler called at te end of a document.</p>
	 */
    @Override
	public void endDocument() throws SAXException {
		if (unknownDepth != 0) {
			warnNotZero("unknownDepth", unknownDepth); //$NON-NLS-1$
		}
		if (widgetDepth != 0) {
			warnNotZero("widgetDepth", widgetDepth); //$NON-NLS-1$
		}
	}

	/** <p>SAX handler called at the start of an element.</p.
	 * <p>This method delegates its work to current <var>state</var>'s startElement.</p>
	 */
    @Override
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		state.startElement(uri, localName, qName, attrs);
		clearContent();
	}

	/** <p>SAX handler called at the end of an element.</p>
	 * <p>This method delegates its work to current <var>state</var>'s endElement.</p>
	 */
    @Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		state.endElement(uri, localName, qName);
	}

	/** <p>A ParserState instance that represents the "UNKNOWN" state.</p>
	 * <p>Parser starts parsing at this state.</p>
	 * <p>Parser transits to this state when it detects an unknown element.</p>
	 */
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

	/** <p>A ParserState instance that represents that parsing is end.</p>
	 */
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

    @Override
	public void warning(SAXParseException e) throws SAXException {
		logger.warn(e);
	}

    @Override
	public void error(SAXParseException e) throws SAXException {
		logger.fatal(e);
	}

    @Override
	public void fatalError(SAXParseException e) throws SAXException {
		logger.fatal(e);
	}

	/** <p>Assigns pending properties to the appropriate variable depending on
	 * current value of <var>propertyType</var>.</p>
	 */
	protected void flushProperties() {
		if (propertyType == PropertyType.NONE) {
			// do nothing
		} else if (propertyType == PropertyType.WIDGET) {
			if (!widget.getProperties().isEmpty()) {
				logger.warn("we already read all the props for this key, leaking"); //$NON-NLS-1$
			}
			widget.setProperties(properties);
			properties.clear();
		} else if (propertyType == PropertyType.ATK) {
			properties.clear();
		} else if (propertyType == PropertyType.CHILD) {
			if (widget.getChildren().isEmpty()) {
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

	/** <p>Replace className in a WidgetInfo to "Dialog" if it is a dialog.</p>
	 * <p>Since Gtk+ treats a Dialog as a kind of Window while Swing treats these two in different way,
	 * this hack is required.</p>
	 * @param info the WidgetInfo interested.
	 */
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

	/** <p>Assigns pending signal info to current WidgetInfo.</p>
	 */
	protected void flushSignals() {
		widget.setSignals(signals);
		signals.clear();
	}

	/** <p>Assigns pending accel info to current WidgetInfo.</p>
	 */
	protected void flushAccels() {
		widget.setAccels(accels);
		accels.clear();
	}

	/** <p>Remove Gtk+'s/Gdk's prefix in constant names.</p>
	 * 
	 * @param name raw name
	 * @return normalized name
	 */
	private String removePrefix(String name) {
		if (name.startsWith("GDK_")) { //$NON-NLS-1$
			name = name.substring("GDK_".length()); //$NON-NLS-1$
		}
		if (name.startsWith("GTK_")) { //$NON-NLS-1$
			name = name.substring("GTK_".length()); //$NON-NLS-1$
		}
		return name;
	}

	/** <p>Converts symbolic key names to AWT key code.</p>
	 * 
	 * @param keyName Key name
	 * @return key code
	 */
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

	/** <p>Converts a set of symbolic modifier names to an integer.</p>
	 * 
	 * @param modifierValue symbolic modifier names. Multiple modifiers can be OR-ed with | operator.
	 * @return algebraically or-ed modifier masks.
	 */
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

	/** <p>Converts button mask to integer.</p>
	 * @param mask a button mask
	 * @return integer value of the button mask.
	 */
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

	/** <p>Returns a string all dashes in given <var>name</var> replaced to underscores.</p>
	 * 
	 * @param name dash-separated-parameter-name.
	 * @return underscore_separated_parameter_name.
	 */
	protected String makePropertyName(String name) {
		return name.replace('-', '_');
	}

	/** <p>A SAX hander called on character chunks.</p>
	 */
    @Override
	public void characters(char[] chars, int start, int length) throws SAXException {
		if (shouldAppendCharactersToContent()) {
			content.append(chars, start, length);
		} else {
			clearContent();
		}
	}
}
