package org.montsuqi.monsia;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Glade1Handler extends AbstractDocumentHandler {

	private String widgetClass;
	private String widgetName;

	private String signalName;
	private String signalHandler;
	private String signalObject;
	private boolean signalAfter;

	private int accelKey;
	private int accelModifiers;
	private String accelSignal;

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
				widgetDepth++;
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
				ChildInfo info = new ChildInfo();
				widget.addChild(info);
				widgetDepth++;
			} else {
				propertyType = PropertyType.WIDGET;
				propertyName = localName;
				state = WIDGET_ATTRIBUTE;
				clearContent();
			}
		}

		void endElement(String uri, String localName, String qName) {
			widget = new WidgetInfo(widgetClass, widgetName);
			widgets.put(widgetName, widget);
			flushProperties();
			flushSignals();
			flushAccels();

			propertyName = null;
			properties.clear();
			signals.clear();
			accels.clear();

			/* close the widget tag */
			widget = widget.getParent();
			widgetDepth--;
			if (widget == null) {
				state = GTK_INTERFACE;
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
			if (localName.equals("class")) {
				widgetClass = value;
			} else if (localName.equals("name")) {
				widgetName = value;
			} else if (localName.equals("visible")) {
				properties.add(new Property("visible", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("sensitive")) {
				properties.add(new Property("sensitive", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("can_default")) {
				properties.add(new Property("can_default", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("can_focus")) {
				properties.add(new Property("can_focus", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("has_default")) {
				properties.add(new Property("has_default", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("has_focus")) {
				properties.add(new Property("has_focus", content.charAt(0) == 'T' ? "true" : "false"));
			} else if (localName.equals("style_name")) {
				/* ignore */
			} else {
				/* some other attribute */
				properties.add(new Property(propertyName, value));
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
			flushProperties();
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
			signals.add(signal);
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
			accels.add(accel);
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
