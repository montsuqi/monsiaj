package org.montsuqi.monsia;

import java.util.LinkedList;
import org.montsuqi.util.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Glade1Handler extends AbstractDocumentHandler {

	private LinkedList pendingWidgets;
	private String signalName;
	private String signalHandler;
	private String signalObject;
	private boolean signalAfter;

	private int accelKey;
	private int accelModifiers;
	private String accelSignal;

	private WidgetInfo getLastPendingWidget() {
		return (WidgetInfo)pendingWidgets.getLast();
	}

	private void initializeWidgetInfo() {
		WidgetInfo w = new WidgetInfo();
		WidgetInfo parent = null;
		if (pendingWidgets.size() > 0) {
			parent = getLastPendingWidget();
			final ChildInfo childInfo = new ChildInfo();
			childInfo.setWidgetInfo(w);
			parent.addChild(childInfo);
		}
		w.setParent(parent);
		pendingWidgets.add(w);
	}

	private void flushWidgetInfo() {
		WidgetInfo w = getLastPendingWidget();
		pendingWidgets.removeLast();
		widgets.put(w.getName(), w);
		if (pendingWidgets.size() == 0) {
			topLevels.add(w);
			state = GTK_INTERFACE;
		} else {
			WidgetInfo parent = getLastPendingWidget();
			ChildInfo childInfo = parent.getLastChild();
			// x, y property should go to childinfo
			for (int i = 0, c = w.getPropertiesCount(); i < c; i++) {
				Property p = w.getProperty(i);
				String name = p.getName();
				if ("x".equals(name) || "y".equals(name)) {
					childInfo.addProperty(p);
				}
			}
		}

		propertyName = null;
		properties.clear();
		signals.clear();
		accels.clear();
	}

	public Glade1Handler() {
		super();
		startState = START;
		logger = Logger.getLogger(Glade1Handler.class);
		pendingWidgets = new LinkedList();
	}

	public void startDocument() throws SAXException {
		super.startDocument();
	}

	protected boolean shouldAppendCharactersToContent() {
		return state == WIDGET_ATTRIBUTE ||
			state == WIDGET_CHILD_ATTRIBUTE ||
			state == SIGNAL_ATTRIBUTE ||
			state == ACCELERATOR_ATTRIBUTE ||
			state == STYLE_ATTRIBUTE;
	}

	private void noElementHere(String inner) {
		/* there should be no tags inside this types of tags */
		warnUnexpectedElement(state.getName(), inner);
		prevState = state;
		state = UNKNOWN;
		unknownDepth++;
	}

	final ParserState START = new ParserState("START") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if ( ! localName.equals("GTK-Interface")) {
				warnUnexpectedElement("<null>", localName);
			}
			state = GTK_INTERFACE;
		}

		void endElement(String uri, String localName, String qName) {
			logger.warn(Messages.getString("AbstractHandler.should_not_be_closing_any_elements_in_this_state")); //$NON-NLS-1$
		}
	};

	final ParserState GTK_INTERFACE = new ParserState("GTK_INTERFACE") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("widget")) {
				state = WIDGET;
				initializeWidgetInfo();
			} else if (localName.equals("style")) {
				state = STYLE;
				// ignore style stuff in Java
			} else {
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			state = FINISH;
		}
	};

	final ParserState WIDGET = new ParserState("WIDGET") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("style")) {
				state = STYLE;
				// ignore all style stuff in Java
			} else if (localName.equals("accelerator") || localName.equals("Accelerator")) {
				state = ACCELERATOR;
			} else if (localName.equals("signal") || localName.equals("Signal")) {
				state = SIGNAL;
			} else if (localName.equals("child")) {
				/* the child section */
				state = WIDGET_CHILD;
			} else if (localName.equals("widget")) {
				initializeWidgetInfo();
			} else {
				propertyType = PropertyType.WIDGET;
				propertyName = localName;
				state = WIDGET_ATTRIBUTE;
				clearContent();
			}
		}

		void endElement(String uri, String localName, String qName) {
			if (localName.equals("widget")) {
				flushWidgetInfo();
			}
		}
	};

	final ParserState WIDGET_ATTRIBUTE = new ParserState("WIDGET_ATTRIBUTE") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			state = WIDGET;
			String value = content.toString();
			WidgetInfo w = getLastPendingWidget();
			if (localName.equals("class")) {
				if (value.startsWith("Gtk")) {
					value = value.substring("Gtk".length());
				}
				w.setClassName(value);
			} else if (localName.equals("name")) {
				w.setName(value);
			} else if (localName.equals("visible")) {
				w.addProperty(new Property("visible", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("sensitive")) {
				w.addProperty(new Property("sensitive", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("can_default")) {
				w.addProperty(new Property("can_default", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("can_focus")) {
				w.addProperty(new Property("can_focus", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("has_default")) {
				w.addProperty(new Property("has_default", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("has_focus")) {
				w.addProperty(new Property("has_focus", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("style_name")) {
				/* ignore */
			} else {
				/* some other attribute */
				w.addProperty(new Property(propertyName, value));
			}
		}
	};

	final ParserState WIDGET_CHILD = new ParserState("WIDGET_CHILD") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			propertyType = PropertyType.CHILD;
			propertyName = localName;
			state = WIDGET_CHILD_ATTRIBUTE;
			clearContent();
		}

		void endElement(String uri, String localName, String qName) {
			WidgetInfo w = getLastPendingWidget();
			WidgetInfo parent = w.getParent();
			ChildInfo childInfo = parent.getLastChild();
			childInfo.setProperties(properties);
			properties.clear();
			state = WIDGET;
		}
	};

	final ParserState WIDGET_CHILD_ATTRIBUTE = new ParserState("WIDGET_CHILD_ATTRIBUTE") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			properties.add(new Property(propertyName, content.toString()));
			state = WIDGET_CHILD;
		}
	};

	final ParserState SIGNAL = new ParserState("SIGNAL") {

		void startElement(String uri, String localName, String qName, Attributes attrs) {
			state = SIGNAL_ATTRIBUTE;
			clearContent();
		}

		void endElement(String uri, String localName, String qName) {
			state = WIDGET;
			SignalInfo signal = new SignalInfo(signalName, signalHandler, signalObject, signalAfter);
			WidgetInfo w = getLastPendingWidget();
			w.addSignalInfo(signal);
		}
	};

	final ParserState SIGNAL_ATTRIBUTE = new ParserState("SIGNAL_ATTRIBUTE") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			state = SIGNAL;
			String value = content.toString();
			if (localName.equals("name")) {
				signalName = value;
			} else if (localName.equals("handler")) {
				signalHandler = value;
//			} else if (localName.equals("data")) {
//				signalData = value;
			} else if (localName.equals("object")) {
				signalObject = value;
			} else if (localName.equals("after")) {
				signalAfter = value.charAt(0) == 'T';
			}
		}
	};

	final ParserState ACCELERATOR = new ParserState("ACCELERATOR") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			state = ACCELERATOR_ATTRIBUTE;
			clearContent();
		}

		void endElement(String uri, String localName, String qName) {
			state = WIDGET;
			AccelInfo accel = new AccelInfo(accelKey, accelModifiers, accelSignal);
			WidgetInfo w = getLastPendingWidget();
			w.addAccelInfo(accel);
		}
	};

	final ParserState ACCELERATOR_ATTRIBUTE = new ParserState("ACCELERATOR_ATTRIBUTE") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			state = ACCELERATOR;
			String value = content.toString();
			if (localName.equals("key") && value.startsWith("GTK_")) {
				accelKey = keyCode(value);
			} else if (localName.equals("modifiers")) {
				accelModifiers = parseModifiers(value);
			} else if (localName.equals("signal")) {
				accelSignal = value;
			}
		}
	};

	final ParserState STYLE = new ParserState("STYLE") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			state = STYLE_ATTRIBUTE;
			clearContent();
		}

		void endElement(String uri, String localName, String qName) {
			// ignore all style stuff in Java
		}
	};

	final ParserState STYLE_ATTRIBUTE = new ParserState("STYLE_ATTRIBUTE") {
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			state = STYLE;
			// ignore all style stuff in Java
		}
	};
}
