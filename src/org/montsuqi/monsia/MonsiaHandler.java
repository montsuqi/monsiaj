package org.montsuqi.monsia;

import java.awt.event.KeyEvent;
import java.io.Reader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.montsuqi.client.Protocol;
import org.montsuqi.util.Logger;

class MonsiaHandler extends DefaultHandler {

	private Logger logger;

	private String fileName;
	private ParserState state;
    private int unknownDepth;    /* handle recursive unrecognised tags */
    private ParserState prevState; /* the lastg `known' state we were in */
    private int widgetDepth;

	private final StringBuffer content;
	private WidgetInfo widget;
    private String propertyName;
	private String propertyType;

	private final Map widgets;
	private final List topLevels;

	private final List properties; // List<Property>
	private final List signals;
	private final List atkActions;
	private final List relations;
	private final List accels;

	boolean isFinished() {
		return state == FINISH;
	}
	
	Interface getInterface(Protocol protocol) {
		if (isFinished()) {
			return new Interface(fileName, widgets, topLevels, protocol);
		} else {
			throw new IllegalStateException(Messages.getString("MonsiaHandler.parsing_is_not_finished_yet")); //$NON-NLS-1$
		}
	}
	
	MonsiaHandler(String fileName) {
		super();
		this.fileName = fileName;
		logger = Logger.getLogger(MonsiaHandler.class);
		content = new StringBuffer();
		widgets = new HashMap();
		topLevels = new ArrayList();
		properties = new ArrayList();
		signals = new ArrayList();
		atkActions = new ArrayList();
		relations = new ArrayList();
		accels = new ArrayList();
	}
	
	// overwrites: DefaultHandler#warning
	public void warning(SAXParseException e) throws SAXException {
		logger.warn(e);
	}

	// overwrites: DefaultHandler#error
	public void error(SAXParseException e) throws SAXException {
		logger.fatal(e);
	}

	// overwrites: DefaultHandler#fatalError
	public void fatalError(SAXParseException e) throws SAXException {
		logger.fatal(e);
	}

	public void startDocument() throws SAXException {
		state = START;

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

	// warning utility methods
	private void warnNotZero(String name, int actual) {
		logger.warn(Messages.getString("MonsiaHandler.not_zero"), new Object[] { name, new Integer(actual) }); //$NON-NLS-1$
	}

	private void warnUnknownAttribute(String element, String attr) {
		logger.warn(Messages.getString("MonsiaHandler.unknown_attribute"), new Object[] { attr, element }); //$NON-NLS-1$
	}

	private void warnUnexpectedElement(String outer, String inner) {
		logger.warn(Messages.getString("MonsiaHandler.unexpected_element"), new Object[] { outer, inner }); //$NON-NLS-1$
	}

	private void warnShouldFindClosing(String element, String found) {
		logger.warn(Messages.getString("MonsiaHandler.should_find_closing"), new Object[] { element, found }); //$NON-NLS-1$
	}

	private void warnShouldBeEmpty(String element, String found) {
		logger.warn(Messages.getString("MonsiaHandler.should_be_empty"), new Object[] { element, found }); //$NON-NLS-1$
	}

	private void warnShouldHaveNoAttributes(String element) {
		logger.warn(Messages.getString("MonsiaHandler.should_have_no_attributes"), element); //$NON-NLS-1$
	}

	private void warnMissingAttribute(String element) {
		logger.warn(Messages.getString("MonsiaHandler.missing_required_attribute"), element); //$NON-NLS-1$
	}
		
	private void warnInvalidPropertiesDefinedHere(String element) {
		logger.warn(Messages.getString("MonsiaHandler.invalid_properties_defined_here"), element); //$NON-NLS-1$
	}

	public void startElement(String uri, String localName, String qName, Attributes attrs)
		throws SAXException {
		state.startElement(uri, localName, qName, attrs);
		content.delete(0, content.length());
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		state.endElement(uri, localName, qName);
	}

    final ParserState START = new ParserState("START") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("monsia-interface")) { //$NON-NLS-1$
				state = MONSIA_INTERFACE;
			} else {
				logger.warn(Messages.getString("MonsiaHandler.expected"), new Object[] { "monsia-interface", localName}); //$NON-NLS-2$ //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			logger.warn(Messages.getString("MonsiaHandler.should_not_be_closing_any_elements_in_this_state")); //$NON-NLS-1$
		}
	};
	
    final ParserState MONSIA_INTERFACE = new ParserState("MONSIA_INTERFACE") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("requires")) { //$NON-NLS-1$
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("lib")) { //$NON-NLS-1$
						/* add to the list of requirements for this module */
						/* DO NOTHING requires.add(value); */
					} else {
						warnUnknownAttribute("requires", value); //$NON-NLS-1$
					}
				}
				state = REQUIRES;
			} else if (localName.equals("widget")) { //$NON-NLS-1$
				widget = createWidgetInfo(attrs);
				topLevels.add(widget);
				widgetDepth++;
				propertyName = null;
				properties.clear();
				signals.clear();
				accels.clear();
				state = WIDGET;
			} else {
				warnUnexpectedElement("monsia-interface", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("monsia-interface")) { //$NON-NLS-1$
				warnShouldFindClosing("monsia-interface", localName); //$NON-NLS-1$
			}
			state = FINISH;
		}
	};

    final ParserState REQUIRES = new ParserState("REQUIRES") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("requires", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}
		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("requires")) { //$NON-NLS-1$
				warnShouldFindClosing("requires", localName); //$NON-NLS-1$
			}
			state = MONSIA_INTERFACE;
		}
	};

    final ParserState WIDGET = new ParserState("WIDGET") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("property")) { //$NON-NLS-1$
				boolean badAgent = false;
				
				if (propertyType != null && ! propertyType.equals("WIDGET")) { //$NON-NLS-1$
					warnInvalidPropertiesDefinedHere("widget"); //$NON-NLS-1$
				}
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("name")) { //$NON-NLS-1$
						propertyName = makePropertyName(value);
					} else if (attrName.equals("agent")) { //$NON-NLS-1$
						badAgent = value.equals("libglade"); //$NON-NLS-1$
					} else if (attrName.equals("translatable")) { //$NON-NLS-1$
						// ignore
					} else {
						warnUnknownAttribute("property", attrName); //$NON-NLS-1$
					}
				}
				if (badAgent) {
					/* ignore the property ... */
					prevState = state;
					state = UNKNOWN;
					unknownDepth++;
				} else {
					propertyType = "WIDGET"; //$NON-NLS-1$
					state = WIDGET_PROPERTY;
				}
			} else if (localName.equals("accessibility")) { //$NON-NLS-1$
				flushProperties();
				
				if (attrs.getLength() != 0) {
					warnShouldHaveNoAttributes("accessibility"); //$NON-NLS-1$
				}
				state = WIDGET_ATK;
			} else if (localName.equals("signal")) { //$NON-NLS-1$
				handleSignal(attrs);
				state = WIDGET_SIGNAL;
			} else if (localName.equals("accelerator")) { //$NON-NLS-1$
				handleAccel(attrs);
				state = WIDGET_ACCEL;
			} else if (localName.equals("child")) { //$NON-NLS-1$
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				warnUnexpectedElement("widget", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { //$NON-NLS-1$
				warnShouldFindClosing("widget", localName); //$NON-NLS-1$
			}
			flushProperties();
			flushSignals();
			flushActions();
			flushRelations();
			flushAccels();
			widget = widget.getParent();
			widgetDepth--;
			
			if (widgetDepth == 0) {
				state = MONSIA_INTERFACE;
			} else {
				state = WIDGET_CHILD_AFTER_WIDGET;
			}
		}
	};

    final ParserState WIDGET_PROPERTY = new ParserState("WIDGET_PROPERTY") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("property", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("property")) { //$NON-NLS-1$
				warnShouldFindClosing("property", localName); //$NON-NLS-1$
			}
			properties.add(new Property(propertyName, content.toString()));
			propertyName = null;
			state = WIDGET;
		}
	};

    final ParserState WIDGET_ATK = new ParserState("WIDGET_ATK") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("atkproperty")) { //$NON-NLS-1$
				if (propertyType != null && ! propertyType.equals("ATK")) { //$NON-NLS-1$
					warnInvalidPropertiesDefinedHere("atk"); //$NON-NLS-1$
				}
				propertyType = "ATK"; //$NON-NLS-1$
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("name")) { //$NON-NLS-1$
						propertyName = makePropertyName(value);
					} else {
						warnUnknownAttribute("atkproperty", attrName); //$NON-NLS-1$
					}
				}
				state = WIDGET_ATK_PROPERTY;
			} else if (localName.equals("atkaction")) { //$NON-NLS-1$
				handleATKAction(attrs);
				state = WIDGET_ATK_ACTION;
			} else if (localName.equals("atkrelation")) { //$NON-NLS-1$
				handleATKRelation(attrs);
				state = WIDGET_ATK_RELATION;
			} else {
				warnUnexpectedElement("accessibility", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("accessibility")) { //$NON-NLS-1$
				warnShouldFindClosing("accessibility", localName); //$NON-NLS-1$
			}
			
			flushProperties(); /* flush the ATK properties */
			state = WIDGET_AFTER_ATK;
		}
	};

    final ParserState WIDGET_ATK_PROPERTY = new ParserState("WIDGET_ATK_PROPERTY") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("accessibility")) { //$NON-NLS-1$
				state = WIDGET_ATK;
			} else {
				warnUnexpectedElement("atkproperty", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkproperty")) { //$NON-NLS-1$
				warnShouldFindClosing("atkproperty", localName); //$NON-NLS-1$
			}
			properties.add(new Property(propertyName, content.toString()));
			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_ATK_ACTION = new ParserState("WIDGET_ATK_ACTION") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("atkaction", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkaction")) { //$NON-NLS-1$
				warnShouldFindClosing("atkaction", localName); //$NON-NLS-1$
			}
			
			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_ATK_RELATION = new ParserState("WIDGET_ATK_RELATION") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("atkrelation", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkrelation")) { //$NON-NLS-1$
				warnShouldFindClosing("atkrelation", localName); //$NON-NLS-1$
			}

			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_AFTER_ATK = new ParserState("WIDGET_AFTER_ATK") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("signal")) { //$NON-NLS-1$
				handleSignal(attrs);
				state = WIDGET_SIGNAL;
			} else if (localName.equals("accelerator")) { //$NON-NLS-1$
				handleAccel(attrs);
				state = WIDGET_ACCEL;
			} else if (localName.equals("child")) { //$NON-NLS-1$
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				warnUnexpectedElement("widget", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { //$NON-NLS-1$
				warnShouldFindClosing("widget", localName); //$NON-NLS-1$
			}
			flushProperties();
			flushSignals();
			flushActions();
			flushRelations();
			flushAccels();
			widget = widget.getParent();
			widgetDepth--;
			
			if (widgetDepth == 0) {
				state = MONSIA_INTERFACE;
			} else {
				state = WIDGET_CHILD_AFTER_WIDGET;
			}
		}
	};

    final ParserState WIDGET_SIGNAL = new ParserState("WIDGET_SIGNAL") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("signal", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("signal")) { //$NON-NLS-1$
				warnShouldFindClosing("signal", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_ATK;
		}
	};

    final ParserState WIDGET_AFTER_SIGNAL = new ParserState("WIDGET_AFTER_SIGNAL") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("accelerator")) { //$NON-NLS-1$
				handleAccel(attrs);
				state = WIDGET_ACCEL;
			} else if (localName.equals("child")) { //$NON-NLS-1$
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				warnUnexpectedElement("wiget", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { //$NON-NLS-1$
				warnShouldFindClosing("widget", localName); //$NON-NLS-1$
			}
			flushProperties();
			flushSignals();
			flushActions();
			flushRelations();
			flushAccels();
			widget = widget.getParent();
			widgetDepth--;
			
			if (widgetDepth == 0) {
				state = MONSIA_INTERFACE;
			} else {
				state = WIDGET_CHILD_AFTER_WIDGET;
			}
		}
	};

    final ParserState WIDGET_ACCEL = new ParserState("WIDGET_ACCEL") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("accelerator", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("accelerator")) { //$NON-NLS-1$
				warnShouldFindClosing("accelerator", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_SIGNAL;
		}
	};

    final ParserState WIDGET_AFTER_ACCEL = new ParserState("WIDGET_AFTER_ACCEL") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("child")) { //$NON-NLS-1$
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				warnUnexpectedElement("widget", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { //$NON-NLS-1$
				warnShouldFindClosing("widget", localName); //$NON-NLS-1$
			}
			flushProperties();
			flushSignals();
			flushActions();
			flushRelations();
			flushAccels();
			widget = widget.getParent();
			widgetDepth--;
			
			if (widgetDepth == 0) {
				state = MONSIA_INTERFACE;
			} else {
				state = WIDGET_CHILD_AFTER_WIDGET;
			}
		}
	};

    final ParserState WIDGET_CHILD = new ParserState("WIDGET_CHILD") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("widget")) { //$NON-NLS-1$
				WidgetInfo parent = widget;
				ChildInfo info = (ChildInfo)(parent.getLastChild());
				widget = createWidgetInfo(attrs);
				info.setWidgetInfo(widget);
				widget.setParent(parent);
				widgetDepth++;
				propertyType = null;
				propertyName = null;
				properties.clear();
				signals.clear();
				accels.clear();
				state = WIDGET;
			} else if (localName.equals("placeholder")) { //$NON-NLS-1$
				/* this isn't a real child, so knock off  the last ChildInfo */
				state = WIDGET_CHILD_PLACEHOLDER;
			} else {
				warnUnexpectedElement("child", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { //$NON-NLS-1$
				warnShouldFindClosing("child", localName); //$NON-NLS-1$
			}
			/* if we are ending the element in this state, then there
			 * hasn't been a <widget> element inside this <child>
			 * element. (If there was, then we would be in
			 * WIDGET_CHILD_AFTER_WIDGET state. */
			logger.warn(Messages.getString("MonsiaHandler.no_widget_element_found_inside_child_Discarding")); //$NON-NLS-1$
			widget.removeLastChild();
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_WIDGET = new ParserState("WIDGET_CHILD_AFTER_WIDGET") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("packing")) { //$NON-NLS-1$
				state = WIDGET_CHILD_PACKING;
			} else {
				warnUnexpectedElement("child", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { //$NON-NLS-1$
				warnShouldFindClosing("child", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_PACKING = new ParserState("WIDGET_CHILD_PACKING") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("property")) { //$NON-NLS-1$
				boolean badAgent = false;
				
				if (propertyType != null && ! propertyType.equals("CHILD")) { //$NON-NLS-1$
					warnInvalidPropertiesDefinedHere("child"); //$NON-NLS-1$
				}
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("name")) { //$NON-NLS-1$
						propertyName = makePropertyName(value);
					} else if (attrName.equals("agent")) { //$NON-NLS-1$
						badAgent = value.equals("libglade"); //$NON-NLS-1$
					} else if (attrName.equals("translatable")) { //$NON-NLS-1$
						// ignore
					} else {
						warnUnknownAttribute("property", attrName); //$NON-NLS-1$
					}
				}
				if (badAgent) {
					/* ignore the property ... */
					prevState = state;
					state = UNKNOWN;
					unknownDepth++;
				} else {
					propertyType = "CHILD"; //$NON-NLS-1$
					state = WIDGET_CHILD_PACKING_PROPERTY;
				}
			} else {
				warnUnexpectedElement("child", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("packing")) { //$NON-NLS-1$
				warnShouldFindClosing("packing", localName); //$NON-NLS-1$
			}
			state = WIDGET_CHILD_AFTER_PACKING;
			flushProperties(); /* flush the properties. */
		}
	};

    final ParserState WIDGET_CHILD_PACKING_PROPERTY = new ParserState("WIDGET_CHILD_PACKING_PROPERTY") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("property", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("property")) { //$NON-NLS-1$
				warnShouldFindClosing("property", localName); //$NON-NLS-1$
			}
			properties.add(new Property(propertyName, content.toString()));
			propertyName = null;
			state = WIDGET_CHILD_PACKING;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_PACKING = new ParserState("WIDGET_CHILD_AFTER_PACKING") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn(Messages.getString("MonsiaHandler.child_should_have_no_elements_after_packing"), localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { //$NON-NLS-1$
				warnShouldFindClosing("child", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_PLACEHOLDER = new ParserState("WIDGET_CHILD_PLACEHOLDER") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("placeholder", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("placeholder")) { //$NON-NLS-1$
				warnShouldFindClosing("placeholder", localName); //$NON-NLS-1$
			}
			state = WIDGET_CHILD_AFTER_PLACEHOLDER;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_PLACEHOLDER = new ParserState("WIDGET_CHILD_AFTER_PLACEHOLDER") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			/* this is a placeholder <child> element -- ignore extra elements */
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { //$NON-NLS-1$
				warnShouldFindClosing("child", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState FINISH = new ParserState("FINISH") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn(Messages.getString("MonsiaHandler.there_should_be_no_elements_here"), localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			logger.warn(Messages.getString("MonsiaHandler.should_not_be_closing_any_elements_in_this_state")); //$NON-NLS-1$
		}
	};

    final ParserState UNKNOWN = new ParserState("UNKNOWN") { //$NON-NLS-1$
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			unknownDepth--;
			if (unknownDepth == 0) {
				state = prevState;
			}
		}
	};

	public void characters(char[] chars, int start, int length) throws SAXException {
		if (state == WIDGET_PROPERTY ||
			state == WIDGET_ATK_PROPERTY ||
			state == WIDGET_CHILD_PACKING_PROPERTY) {
			content.append(chars, start, length);
		} else {
			content.delete(0, content.length());
		}
	}

	private void flushProperties() {
		if (propertyType == null) {
			// do nothing
		} else if (propertyType.equals("WIDGET")) { //$NON-NLS-1$
			if (widget.getPropertiesCount() != 0) {
				logger.warn(Messages.getString("MonsiaHandler.we_already_read_all_the_props_for_this_key")); //$NON-NLS-1$
			}
			widget.setProperties(properties);
			properties.clear();
		} else if (propertyType.equals("ATK")) { //$NON-NLS-1$
			if (widget.getATKPropertiesCount() != 0) {
				logger.warn(Messages.getString("MonsiaHandler.we_already_read_all_the_ATK_props_for_this_key")); //$NON-NLS-1$
			}
			widget.setATKProperties(properties);
			properties.clear();
		} else if (propertyType.equals("CHILD")) { //$NON-NLS-1$
			if (widget.getChildrenCount() == 0) {
				logger.warn(Messages.getString("MonsiaHandler.no_children_but_have_child_properties")); //$NON-NLS-1$
				properties.clear();
			} else {
				ChildInfo info = widget.getLastChild();
				info.setProperties(properties);
				properties.clear();
			}
		}

		propertyType = null;
		propertyName = null;
		properties.clear();
	}

	private void flushSignals() {
		widget.setSignals(signals);
		signals.clear();
	}

	private void flushActions() {
		widget.setATKActions(atkActions);
		atkActions.clear();
	}

	private void flushRelations() {
		widget.setRelations(relations);
		relations.clear();
	}

	private void flushAccels() {
		widget.setAccels(accels);
		accels.clear();
	}

	private WidgetInfo createWidgetInfo(Attributes attrs) {

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

	void handleATKAction(Attributes attrs) {
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

	void handleATKRelation(Attributes attrs) {

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

	void handleSignal(Attributes attrs) {
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

	private String normalizeKeyName(String keyName) {
		if (keyName.startsWith("GDK_")) { //$NON-NLS-1$
			keyName = keyName.substring(4);
		}
		if ( ! keyName.startsWith("VK_")) { //$NON-NLS-1$
			keyName = "VK_" + keyName; //$NON-NLS-1$
		}
		return keyName;
	}
	
	private int keyCode(String keyName) {
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

	void handleAccel(Attributes attrs) {
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

	private int parseModifiers(String modifierValue) {
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

	private int parseButtonMask(String mask) {
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

	void handleChild(Attributes attrs) {
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
	private String makePropertyName(String name) {
		return name.replace('-', '_');
	}
}

