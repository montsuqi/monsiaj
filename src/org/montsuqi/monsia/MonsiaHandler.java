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

public class MonsiaHandler extends DefaultHandler {

	Logger logger;

	String fileName;
	ParserState state;
    int unknownDepth;    /* handle recursive unrecognised tags */
    ParserState prevState; /* the last `known' state we were in */
    int widgetDepth;

    StringBuffer content;
    WidgetInfo widget;
    String propertyName;
	String propertyType;

	Map widgets;
	List topLevels;

    List properties; // List<Property>
    List signals;
    List atkActions;
    List relations;
    List accels;


	protected boolean isFinished() {
		return state == FINISH;
	}
	
	public Interface getInterface(Protocol protocol) {
		if (isFinished()) {
			return new Interface(fileName, widgets, topLevels, protocol);
		} else {
			throw new IllegalStateException("parsing is not finished yet.");
		}
	}
	
	public MonsiaHandler(String fileName) {
		super();
		this.fileName = fileName;
		logger = Logger.getLogger(MonsiaHandler.class);
		widgets = new HashMap();
		topLevels = new ArrayList();
		properties = new ArrayList();
		signals = new ArrayList();
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
		content = new StringBuffer();

		widget = null;

		propertyName = null;
	}

	public void endDocument() throws SAXException {
		if (unknownDepth != 0) {
			logger.warn("unknown_depth != 0 ({0})", new Integer(unknownDepth));
		}
		if (widgetDepth != 0) {
			logger.warn("widgetDepth != 0 ({0})", new Integer(widgetDepth));
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attrs)
		throws SAXException {

		logger.info("<{0}> in state {1}", new Object[] { localName, state.getName() });

		state.startElement(uri, localName, qName, attrs);

		/* truncate the content string ... */
		content.delete(0, content.length());
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		logger.info("</{0}> in state {1}", new Object[] { localName, state.getName() });

		state.endElement(uri, localName, qName);
	}

	public 
	
    final ParserState START = new ParserState("START") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("monsia-interface")) {
				state = MONSIA_INTERFACE;
				//				/* check for correct XML namespace */
				//				for (int i = attrs.getLength(); i >= 0; i--) {
				//					String attrName = attrs.getLocalName(i);
				//					String value = attrs.getValue(i);
				//					if (attrName.equals("xmlns") && value.equals("...")) {
				//						logger.warn("bad XML namespace `{0}'.", value);
				//					} else {
				//						logger.warn("unknown attribute `{0}' for <monsia-interface>", attrName);
				//					}
				//				}
			} else {
				logger.warn("Expected <monsia-interface>.  Got <{0}>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			logger.warn("should not be closing any elements in this state");
		}
	};
	
    final ParserState MONSIA_INTERFACE = new ParserState("MONSIA_INTERFACE") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("requires")) {
				for (int i = attrs.getLength(); i >= 0; i--) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("lib")) {
						/* add to the list of requirements for this module */
						/* DO NOTHING requires.add(value); */
					} else {
						logger.warn("unknown attribute `{0}' for <requires>.", value);
					}
				}
				state = REQUIRES;
			} else if (localName.equals("widget")) {
				widget = createWidgetInfo(attrs);
				topLevels.add(widget);
				widgetDepth++;
				propertyName = null;
				properties.clear();
				signals.clear();
				accels.clear();
				state = WIDGET;
			} else {
				logger.warn("Unexpected element <{0}> inside <monsia-interface>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}
		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("monsia-interface")) {
				logger.warn("should find </monsia-interface> here.  Found </{0}>", localName);
			}
			state = FINISH;
		}
	};

    final ParserState REQUIRES = new ParserState("REQUIRES") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<requires> element should be empty.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}
		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("requires")) {
				logger.warn("should find </requires> here.  Found </{0}>", localName);
			}
			state = MONSIA_INTERFACE;
		}
	};

    final ParserState WIDGET = new ParserState("WIDGET") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("property")) {
				boolean badAgent = false;
				
				if (propertyType != null && ! propertyType.equals("WIDGET")) {
					logger.warn("non widget properties defined here (oh no!)");
				}
				for (int i = attrs.getLength(); i >= 0; i--) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("name")) {
						propertyName = makePropertyName(value);
					} else if (attrName.equals("agent")) {
						badAgent = value.equals("libglade");
					} else {
						logger.warn("unknown attribute `{0}' for <property>.", attrName);
					}
				}
				if (badAgent) {
					/* ignore the property ... */
					prevState = state;
					state = UNKNOWN;
					unknownDepth++;
				} else {
					propertyType = "WIDGET";
					state = WIDGET_PROPERTY;
				}
			} else if (localName.equals("accessibility")) {
				flushProperties();
				
				if (attrs.getLength() != 0) {
					logger.warn("<accessibility> element should have no attributes");
				}
				state = WIDGET_ATK;
			} else if (localName.equals("signal")) {
				handleSignal(attrs);
				state = WIDGET_SIGNAL;
			} else if (localName.equals("accelerator")) {
				handleAccel(attrs);
				state = WIDGET_ACCEL;
			} else if (localName.equals("child")) {
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				logger.warn("Unexpected element <{0}> inside <widget>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}
		
		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) {
				logger.warn("should find </widget> here.  Found </{0}>", localName);
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

    final ParserState WIDGET_PROPERTY = new ParserState("WIDGET_PROPERTY") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<property> element should be empty.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("property")) {
				logger.warn("should find </property> here.  Found </{0}>", localName);
			}
			properties.add(new Property(propertyName, content.toString()));
			propertyName = null;
			state = WIDGET;
		}
	};

    final ParserState WIDGET_ATK = new ParserState("WIDGET_ATK") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("atkproperty")) {
				if (propertyType != null && ! propertyType.equals("ATK")) {
					logger.warn("non atk properties defined here (oh no!)");
				}
				propertyType = "ATK";
				for (int i = attrs.getLength(); i >= 0; i--) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("name")) {
						propertyName = makePropertyName(value);
					} else {
						logger.warn("unknown attribute `{0}' for <atkproperty>.", attrName);
					}
				}
				state = WIDGET_ATK_PROPERTY;
			} else if (localName.equals("atkaction")) {
				handleATKAction(attrs);
				state = WIDGET_ATK_ACTION;
			} else if (localName.equals("atkrelation")) {
				handleATKRelation(attrs);
				state = WIDGET_ATK_RELATION;
			} else {
				logger.warn("Unexpected element <{0}> inside <accessibility>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("accessibility")) {
				logger.warn("should find </accessibility> here.  Found </{0}>", localName);
			}
			
			flushProperties(); /* flush the ATK properties */
			state = WIDGET_AFTER_ATK;
		}
	};

    final ParserState WIDGET_ATK_PROPERTY = new ParserState("WIDGET_ATK_PROPERTY") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("accessibility")) {
				state = WIDGET_ATK;
			} else {
				logger.warn("Unexpected element <{0}> inside <atkproperty>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkproperty")) {
				logger.warn("should find </atkproperty> here.  Found </{0}>", localName);
			}
			properties.add(new Property(propertyName, content.toString()));
			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_ATK_ACTION = new ParserState("WIDGET_ATK_ACTION") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<atkaction> element should be empty.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkaction")) {
				logger.warn("should find </atkaction> here.  Found </{0}>", localName);
			}
			
			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_ATK_RELATION = new ParserState("WIDGET_ATK_RELATION") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<atkrelation> element should be empty.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkrelation")) {
				logger.warn("should find </atkrelation> here.  Found </{0}>", localName);
			}

			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_AFTER_ATK = new ParserState("WIDGET_AFTER_ATK") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("signal")) {
				handleSignal(attrs);
				state = WIDGET_SIGNAL;
			} else if (localName.equals("accelerator")) {
				handleAccel(attrs);
				state = WIDGET_ACCEL;
			} else if (localName.equals("child")) {
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				logger.warn("Unexpected element <{0}> inside <widget>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("widget")) {
				logger.warn("should find </widget> here.  Found </{0}>", localName);
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

    final ParserState WIDGET_SIGNAL = new ParserState("WIDGET_SIGNAL") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<signal> element should be empty.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("signal")) {
				logger.warn("should find </signal> here.  Found </{0}>", localName);
			}
			state = WIDGET_AFTER_ATK;
		}
	};

    final ParserState WIDGET_AFTER_SIGNAL = new ParserState("WIDGET_AFTER_SIGNAL") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("accelerator")) {
				handleAccel(attrs);
				state = WIDGET_ACCEL;
			} else if (localName.equals("child")) {
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				logger.warn("Unexpected element <{0}> inside <widget>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("widget")) {
				logger.warn("should find </widget> here.  Found </{0}>", localName);
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

    final ParserState WIDGET_ACCEL = new ParserState("WIDGET_ACCEL") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<accelerator> element should be empty.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("accelerator")) {
				logger.warn("should find </accelerator> here.  Found </{0}>", localName);
			}
			state = WIDGET_AFTER_SIGNAL;
		}
	};

    final ParserState WIDGET_AFTER_ACCEL = new ParserState("WIDGET_AFTER_ACCEL") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("child")) {
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				logger.warn("Unexpected element <{0}> inside <widget>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("widget")) {
				logger.warn("should find </widget> here.  Found </{0}>", localName);
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

    final ParserState WIDGET_CHILD = new ParserState("WIDGET_CHILD") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("widget")) {
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
			} else if (localName.equals("placeholder")) {
				/* this isn't a real child, so knock off  the last ChildInfo */
				state = WIDGET_CHILD_PLACEHOLDER;
			} else {
				logger.warn("Unexpected element <{0}> inside <child>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("child")) {
				logger.warn("should find </child> here.  Found </{0}>", localName);
			}
			/* if we are ending the element in this state, then there
			 * hasn't been a <widget> element inside this <child>
			 * element. (If there was, then we would be in
			 * WIDGET_CHILD_AFTER_WIDGET state. */
			logger.warn("no <widget> element found inside <child>.  Discarding");
			widget.removeLastChild();
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_WIDGET = new ParserState("WIDGET_CHILD_AFTER_WIDGET") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("packing")) {
				state = WIDGET_CHILD_PACKING;
			} else {
				logger.warn("Unexpected element <{0}> inside <child>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("child")) {
				logger.warn("should find </child> here.  Found </{0}>", localName);
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_PACKING = new ParserState("WIDGET_CHILD_PACKING") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("property")) {
				boolean badAgent = false;
				
				if (propertyType != null && ! propertyType.equals("CHILD")) {
					logger.warn("non child properties defined here (oh no!)");
				}
				for (int i = attrs.getLength(); i >= 0; i--) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("localName")) {
						propertyName = makePropertyName(value);
					} else if (attrName.equals("agent")) {
						badAgent = value.equals("libglade");
					} else {
						logger.warn("unknown attribute `{0}' for <property>.", attrName);
					}
				}
				if (badAgent) {
					/* ignore the property ... */
					prevState = state;
					state = UNKNOWN;
					unknownDepth++;
				} else {
					propertyType = "CHILD";
					state = WIDGET_CHILD_PACKING_PROPERTY;
				}
			} else {
				logger.warn("Unexpected element <{0}> inside <child>.", localName);
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("packing")) {
				logger.warn("should find </packing> here.  Found </{0}>", localName);
			}
			state = WIDGET_CHILD_AFTER_PACKING;
			flushProperties(); /* flush the properties. */
		}
	};

    final ParserState WIDGET_CHILD_PACKING_PROPERTY = new ParserState("WIDGET_CHILD_PACKING_PROPERTY") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<property> element should be empty.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("property")) {
				logger.warn("should find </property> here.  Found </{0}>", localName);
			}
			properties.add(new Property(propertyName, content.toString()));
			propertyName = null;
			state = WIDGET_CHILD_PACKING;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_PACKING = new ParserState("WIDGET_CHILD_AFTER_PACKING") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<child> should have no elements after <packing>.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("child")) {
				logger.warn("should find </child> here.  Found </{0}>", localName);
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_PLACEHOLDER = new ParserState("WIDGET_CHILD_PLACEHOLDER") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<placeholder> should be empty.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("placeholder")) {
				logger.warn("should find </placeholder> here.  Found </{0}>", localName);
			}
			state = WIDGET_CHILD_AFTER_PLACEHOLDER;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_PLACEHOLDER = new ParserState("WIDGET_CHILD_AFTER_PLACEHOLDER") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			/* this is a placeholder <child> element -- ignore extra elements */
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if (localName.equals("child")) {
				logger.warn("should find </child> here.  Found </{0}>", localName);
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState FINISH = new ParserState("FINISH") {
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("There should be no elements here.  Found <{0}>.", localName);
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			logger.warn("should not be closing any elements in this state");
		}
	};

    final ParserState UNKNOWN = new ParserState("UNKNOWN") {
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
			content.append(chars);
		} else {
			/* don't care about content in any other states */
		}
	}

	void flushProperties() {
		if (propertyType == null) {
			// do nothing
		} else if (propertyType.equals("WIDGET")) {
			if (widget.getPropertiesCount() != 0) {
				logger.warn("we already read all the props for this key.  Leaking");
			}
			widget.setProperties(properties);
			properties.clear();
		} else if (propertyType.equals("ATK")) {
			if (widget.getATKPropertiesCount() != 0) {
				logger.warn("we already read all the ATK props for this key.  Leaking");
			}
			widget.setATKProperties(properties);
			properties.clear();
		} else if (propertyType.equals("CHILD")) {
			if (widget.getChildrenCount() == 0) {
				logger.warn("no children, but have child properties!");
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

	void flushSignals() {
		widget.setSignals(signals);
		signals.clear();
	}

	void flushActions() {
		widget.setATKActions(atkActions);
		atkActions.clear();
	}

	void flushRelations() {
		widget.setRelations(relations);
		relations.clear();
	}

	void flushAccels() {
		widget.setAccels(accels);
		accels.clear();
	}

	WidgetInfo createWidgetInfo(Attributes attrs) {

		String className = null;
		String name = null;

		for (int i = attrs.getLength(); i >= 0; i--) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("class")) {
				className = value;
			} else if (attrName.equals("id")) {
				name = value;
			} else {
				logger.warn("unknown attribute `{0}' for <widget>.", attrName);
			}
		}

		if (className == null || name == null) {
			logger.warn("<widget> element missing required attributes!");
		}
		WidgetInfo info = new WidgetInfo(className, name);
		widgets.put(name, info);
		return info;
	}

	void handleATKAction(Attributes attrs) {
		flushProperties();

		String actionName = null;
		String description = null;

		for (int i = attrs.getLength(); i >= 0; i--) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("action_name")) {
				actionName = value;
			} else if (attrName.equals("description")) {
				description = value;
			} else {
				logger.warn("unknown attribute `{0}' for <action>.", attrName);
			}
		}
	
		if (actionName == null) {
			logger.warn("required <atkaction> attribute 'action_name' missing!!!");
			return;
		}

		if (atkActions == null) {
			atkActions = new ArrayList();
		}
		atkActions.add(new ATKActionInfo(actionName, description));
	}

	void handleATKRelation(Attributes attrs) {

		flushProperties();

		String target = null;
		String type = null;

		for (int i = attrs.getLength(); i >= 0; i--) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("target")) {
				target = value;
			} else if (attrName.equals("type")) {
				type = value;
			} else {
				logger.warn("unknown attribute `{0}' for <signal>.", attrName);
			}
		}
		if (target == null || type == null) {
			logger.warn("required <atkrelation> attributes ('target' and/or 'type') missing!!!");
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

		for (int i = attrs.getLength(); i >= 0; i--) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("name")) {
				name = value;
			} else if (attrName.equals("handler")) {
				handler = value;
			} else if (attrName.equals("after")) {
				after = value.startsWith("y");
			} else if (attrName.equals("object")) {
				object = value;
			} else if (attrName.equals("last_modification_time")) {
				/* Do nothing. */;
			} else {
				logger.warn("unknown attribute `%s' for <signal>.", attrName);
			}
		}

		if (name == null || handler == null) {
			logger.warn("required <signal> attributes missing!!!");
			return;
		}
		signals.add(new SignalInfo(name, handler, object, after));
	}

	private String normalizeKeyName(String keyName) {
		if (keyName.startsWith("GDK_")) {
			keyName = keyName.substring(4);
		}
		if ( ! keyName.startsWith("VK_")) {
			keyName = "VK_" + keyName;
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
		for (int i = attrs.getLength(); i >= 0; i--) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("key")) {
				key = keyCode(value);
			} else if (attrName.equals("modifiers")) {
				try {
					modifiers = parseModifiers(value);
				} catch (IOException e) {
					logger.warn(e);
				}
			} else if (attrName.equals("signal")) {
				signal = value;
			} else {
				logger.warn("unknown attribute {0} for <accelerator>.", attrName);
			}
		}
		if (key == 0 || signal == null) {
			logger.warn("required <accelerator> attributes missing!!!");
			return;
		}
		accels.add(new AccelInfo(key, modifiers, signal));
	}

	private int parseModifiers(String modifierValue) throws IOException {
		Reader reader = new StringReader(modifierValue);
		StreamTokenizer tokens = new StreamTokenizer(reader);
		tokens.ordinaryChars('\u0000', '\uffff');
		tokens.whitespaceChars('|', '|');
		tokens.whitespaceChars(' ', ' ');

		int modifiers = 0;
		while (tokens.nextToken() != StreamTokenizer.TT_EOF) {
			String modifier = tokens.sval;
			if (modifier.equals("SHIFT_MASK")) {
				modifiers |= KeyEvent.SHIFT_MASK;
			} else if (modifier.equals("LOCK_MASK")) {
				logger.warn("LOCK_MASK is not supported in Java.");
			} else if (modifier.equals("CONTROL_MASK")) {
				modifiers |= KeyEvent.CTRL_MASK;
			} else if (modifier.startsWith("MOD_")) {
				logger.warn("MOD_MASK is not supported in Java.");
			} else if (modifier.startsWith("BUTTON") && modifier.length() == 7) {
				modifiers |= parseButtonMask(modifier.substring(6));
			} else if (modifier.equals("RELEASE_MASK")) {
				logger.warn("Release mask not supported in Java.");
			}
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
				logger.warn("Only BUTTON[1-3] are supported in Java.");
				return 0;
			}
		} catch (NumberFormatException e) {
			logger.warn("Unknown BUTTON # in {0}", mask);
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
		for (int i = attrs.getLength(); i >= 0; i--) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("internal-child")) {
				info.setInternalChild(value);
			} else {
				logger.warn("unknown attribute `{0}' for <child>.", attrName);
			}
		}
	}
	/** returns name with all dashes converted to underscores. */
	private String makePropertyName(String name) {
		return name.replace('-', '_');
	}
}

