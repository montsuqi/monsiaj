package org.montsuqi.monsia;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.montsuqi.client.Protocol;
import org.montsuqi.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

abstract class AbstractDocumentHandler extends DefaultHandler {

	protected final Logger logger;
	protected final StringBuffer content;
	protected final Map widgets;
	protected final List topLevels;
	protected final List properties;
	protected final List signals;
	protected final List atkActions;
	protected final List relations;
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
		logger = Logger.getLogger(AbstractDocumentHandler.class);
		content = new StringBuffer();
		widgets = new HashMap();
		topLevels = new ArrayList();
		properties = new ArrayList();
		signals = new ArrayList();
		atkActions = new ArrayList();
		relations = new ArrayList();
		accels = new ArrayList();
		
	}

	protected abstract boolean shouldAppendCharactersToContent();

	protected boolean isFinished() {
		return state == FINISH;
	}

	protected Interface getInterface(Protocol protocol) {
		if (isFinished()) {
			return new Interface(widgets, topLevels, protocol);
		} else {
			throw new IllegalStateException(Messages.getString("MonsiaHandler.parsing_is_not_finished_yet")); //$NON-NLS-1$
		}
	}

	public void startDocument() throws SAXException {
		state = startState;
		unknownDepth = 0;
		prevState = UNKNOWN;
	
		widgetDepth = 0;
		content.delete(0, content.length());
	
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
		content.delete(0, content.length());
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
			logger.warn(Messages.getString("AbstractHandler.there_should_be_no_elements_here"), localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			logger.warn(Messages.getString("AbstractHandler.should_not_be_closing_any_elements_in_this_state")); //$NON-NLS-1$
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

	protected void warnNotZero(String name, int actual) {
		logger.warn(Messages.getString("MonsiaHandler.not_zero"), new Object[] { name, new Integer(actual) }); //$NON-NLS-1$
	}

	protected void warnUnknownAttribute(String element, String attr) {
		logger.warn(Messages.getString("MonsiaHandler.unknown_attribute"), new Object[] { attr, element }); //$NON-NLS-1$
	}

	protected void warnUnexpectedElement(String outer, String inner) {
		logger.warn(Messages.getString("MonsiaHandler.unexpected_element"), new Object[] { outer, inner }); //$NON-NLS-1$
	}

	protected void warnShouldFindClosing(String element, String found) {
		logger.warn(Messages.getString("MonsiaHandler.should_find_closing"), new Object[] { element, found }); //$NON-NLS-1$
	}

	protected void warnShouldBeEmpty(String element, String found) {
		logger.warn(Messages.getString("MonsiaHandler.should_be_empty"), new Object[] { element, found }); //$NON-NLS-1$
	}

	protected void warnShouldHaveNoAttributes(String element) {
		logger.warn(Messages.getString("MonsiaHandler.should_have_no_attributes"), element); //$NON-NLS-1$
	}

	protected void warnMissingAttribute(String element) {
		logger.warn(Messages.getString("MonsiaHandler.missing_required_attribute"), element); //$NON-NLS-1$
	}

	protected void warnInvalidPropertiesDefinedHere(String element) {
		logger.warn(Messages.getString("MonsiaHandler.invalid_properties_defined_here"), element); //$NON-NLS-1$
	}

	protected void flushProperties() {
		if (propertyType == PropertyType.NONE) {
			// do nothing
		} else if (propertyType == PropertyType.WIDGET) {
			if (widget.getPropertiesCount() != 0) {
				logger.warn(Messages.getString("MonsiaHandler.we_already_read_all_the_props_for_this_key")); //$NON-NLS-1$
			}
			widget.setProperties(properties);
			properties.clear();
		} else if (propertyType == PropertyType.ATK) {
			if (widget.getATKPropertiesCount() != 0) {
				logger.warn(Messages.getString("MonsiaHandler.we_already_read_all_the_ATK_props_for_this_key")); //$NON-NLS-1$
			}
			widget.setATKProperties(properties);
			properties.clear();
		} else if (propertyType == PropertyType.CHILD) {
			if (widget.getChildrenCount() == 0) {
				logger.warn(Messages.getString("MonsiaHandler.no_children_but_have_child_properties")); //$NON-NLS-1$
				properties.clear();
			} else {
				ChildInfo info = widget.getLastChild();
				info.setProperties(properties);
				properties.clear();
			}
		} else {
			throw new IllegalStateException(Messages.getString("MonsiaHandler.unknown_property_type"));
		}
	
		propertyType = PropertyType.NONE;
		propertyName = null;
		properties.clear();
	}

	protected void flushSignals() {
		widget.setSignals(signals);
		signals.clear();
	}

	protected void flushActions() {
		widget.setATKActions(atkActions);
		atkActions.clear();
	}

	protected void flushRelations() {
		widget.setRelations(relations);
		relations.clear();
	}

	protected void flushAccels() {
		widget.setAccels(accels);
		accels.clear();
	}

	protected WidgetInfo createWidgetInfo(Attributes attrs) {
	
		String className = null;
		String name = null;
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("class")) { //$NON-NLS-1$
				className = value;
			} else if (attrName.equals("id")) { //$NON-NLS-1$
				name = value;
			} else {
				warnUnknownAttribute("widget", attrName); //$NON-NLS-1$
			}
		}
	
		if (className == null || name == null) {
			warnMissingAttribute("widget"); //$NON-NLS-1$
		}
		WidgetInfo info = new WidgetInfo(className, name);
		widgets.put(name, info);
		return info;
	}

	protected void handleATKRelation(Attributes attrs) {
	
		flushProperties();
	
		String target = null;
		String type = null;
	
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("target")) { //$NON-NLS-1$
				target = value;
			} else if (attrName.equals("type")) { //$NON-NLS-1$
				type = value;
			} else {
				warnUnknownAttribute("signal", attrName); //$NON-NLS-1$
			}
		}
		if (target == null || type == null) {
			warnMissingAttribute("atkrelation"); //$NON-NLS-1$
			return;
		}
	
		relations.add(new ATKRelationInfo(target, type));
	}

	protected void handleSignal(Attributes attrs) {
		flushProperties();
	
		String name = null;
		String handler = null;
		String object = null;
		boolean after = false;
	
		for (int i = 0, n = attrs.getLength(); i < n; i++) {  
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("name")) { //$NON-NLS-1$
				name = value;
			} else if (attrName.equals("handler")) { //$NON-NLS-1$
				handler = value;
			} else if (attrName.equals("after")) { //$NON-NLS-1$
				after = value.startsWith("y"); //$NON-NLS-1$
			} else if (attrName.equals("object")) { //$NON-NLS-1$
				object = value;
			} else if (attrName.equals("last_modification_time")) { //$NON-NLS-1$
				/* Do nothing. */;
			} else {
				warnUnknownAttribute("signal", attrName); //$NON-NLS-1$
			}
		}
	
		if (name == null || handler == null) {
			warnMissingAttribute("signal"); //$NON-NLS-1$
			return;
		}
		signals.add(new SignalInfo(name, handler, object, after));
	}

	protected String normalizeKeyName(String keyName) {
		if (keyName.startsWith("GDK_")) { //$NON-NLS-1$
			keyName = keyName.substring(4);
		}
		if ( ! keyName.startsWith("VK_")) { //$NON-NLS-1$
			keyName = "VK_" + keyName; //$NON-NLS-1$
		}
		return keyName;
	}

	protected int keyCode(String keyName) {
		final Field[] fields = KeyEvent.class.getFields();
		keyName = normalizeKeyName(keyName);
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
		return 0;
	}

	protected void handleAccel(Attributes attrs) {
		flushProperties();
		flushSignals();
		flushActions();
		flushRelations();
		int key = 0;
		int modifiers = 0;
		String signal = null;
		for (int i = 0, n = attrs.getLength(); i < n; i++) {  
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("key")) { //$NON-NLS-1$
				key = keyCode(value);
			} else if (attrName.equals("modifiers")) { //$NON-NLS-1$
				modifiers = parseModifiers(value);
			} else if (attrName.equals("signal")) { //$NON-NLS-1$
				signal = value;
			} else {
				warnUnknownAttribute("accelerator", attrName); //$NON-NLS-1$
			}
		}
		if (key == 0 || signal == null) {
			warnMissingAttribute("accelerator"); //$NON-NLS-1$
			return;
		}
		accels.add(new AccelInfo(key, modifiers, signal));
	}

	protected int parseModifiers(String modifierValue) {
		Reader reader = new StringReader(modifierValue);
		StreamTokenizer tokens = new StreamTokenizer(reader);
		tokens.ordinaryChars('\u0000', '\uffff');
		tokens.whitespaceChars('|', '|');
		tokens.whitespaceChars(' ', ' ');
	
		int modifiers = 0;
		try {
			while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
				String modifier = tokens.sval;
				if (modifier.equals("SHIFT_MASK")) { //$NON-NLS-1$
					modifiers |= KeyEvent.SHIFT_MASK;
				} else if (modifier.equals("LOCK_MASK")) { //$NON-NLS-1$
					logger.warn(Messages.getString("MonsiaHandler.not_supported_in_Java"), "LOCK_MASK"); //$NON-NLS-1$ $NON-NLS-2$
				} else if (modifier.equals("CONTROL_MASK")) { //$NON-NLS-1$
					modifiers |= KeyEvent.CTRL_MASK;
				} else if (modifier.startsWith("MOD_")) { //$NON-NLS-1$
					logger.warn(Messages.getString("MonsiaHandler.not_supported_in_Java"), "MOD_MASK"); //$NON-NLS-1$ $NON-NLS-2$
				} else if (modifier.startsWith("BUTTON") && modifier.length() == 7) { //$NON-NLS-1$
					modifiers |= parseButtonMask(modifier.substring(6));
				} else if (modifier.equals("RELEASE_MASK")) { //$NON-NLS-1$
					logger.warn(Messages.getString("MonsiaHandler.not_supported_in_Java"), "RELEASE_MASK"); //$NON-NLS-1$ $NON-NLS-2$
				}
			}
		} catch (IOException e) {
			logger.warn(e); // no recovery action is needed.
		}
		return modifiers;
	}

	protected int parseButtonMask(String mask) {
		try {
			int value = Integer.parseInt(mask);
			switch (value) {
			case 1:
				return KeyEvent.BUTTON1_MASK;
			case 2:
				return KeyEvent.BUTTON2_MASK;
			case 3:
				return KeyEvent.BUTTON3_MASK;
			default:
				logger.warn(Messages.getString("MonsiaHandler.only_BUTTON1-3_are_supported_in_Java")); //$NON-NLS-1$
				return 0;
			}
		} catch (NumberFormatException e) {
			logger.warn(Messages.getString("MonsiaHandler.unknown_BUTTON__number"), mask); //$NON-NLS-1$
			return 0;
		}
	}

	protected void handleChild(Attributes attrs) {
		flushProperties();
		flushSignals();
		flushActions();
		flushRelations();
		flushAccels();
	
		ChildInfo info = new ChildInfo();
		widget.addChild(info);
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("internal-child")) { //$NON-NLS-1$
				info.setInternalChild(value);
			} else {
				warnUnknownAttribute("child", attrName); //$NON-NLS-1$
			}
		}
	}

	/** returns name with all dashes converted to underscores. */
	protected String makePropertyName(String name) {
		return name.replace('-', '_');
	}

	protected void handleATKAction(Attributes attrs) {
		flushProperties();
	
		String actionName = null;
		String description = null;
	
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("action_name")) { //$NON-NLS-1$
				actionName = value;
			} else if (attrName.equals("description")) { //$NON-NLS-1$
				description = value;
			} else {
				warnUnknownAttribute("action", attrName); //$NON-NLS-1$
			}
		}
	
		if (actionName == null) {
			warnMissingAttribute("atkaction"); //$NON-NLS-1$
			return;
		}
	
		atkActions.add(new ATKActionInfo(actionName, description));
	}

	public void characters(char[] chars, int start, int length) throws SAXException {
		if (shouldAppendCharactersToContent()) {
			content.append(chars, start, length);
		} else {
			content.delete(0, content.length());
		}
	}
}
