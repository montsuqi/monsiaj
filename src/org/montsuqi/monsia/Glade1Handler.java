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

import java.util.LinkedList;
import java.util.Map;

import org.xml.sax.Attributes;

/** SAX document handler which parses interface definition by Glade version 1.
 */
public class Glade1Handler extends AbstractDocumentHandler {

	private LinkedList pendingWidgets;
	String signalName;
	String signalHandler;
	String signalObject;
	boolean signalAfter;

	int accelKey;
	int accelModifiers;
	String accelSignal;

	WidgetInfo getLastPendingWidget() {
		return (WidgetInfo)pendingWidgets.getLast();
	}

	void initializeWidgetInfo() {
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

	void flushWidgetInfo() {
		WidgetInfo w = getLastPendingWidget();
		dialogHack(w);
		pendingWidgets.removeLast();
		widgets.put(w.getName(), w);
		if (pendingWidgets.size() == 0) {
			topLevels.add(w);
			state = GTK_INTERFACE;
		} else {
			WidgetInfo parent = getLastPendingWidget();
			ChildInfo childInfo = parent.getLastChild();
			// x, y property should go to childinfo
			Map widgetProperties = w.getProperties();
			if (widgetProperties.containsKey("x")) { //$NON-NLS-1$
				childInfo.addProperty("x", (String)widgetProperties.get("x")); //$NON-NLS-1$ //$NON-NLS-2$
				widgetProperties.remove("x"); //$NON-NLS-1$
			}
			if (widgetProperties.containsKey("y")) { //$NON-NLS-1$
				childInfo.addProperty("y", (String)widgetProperties.get("y")); //$NON-NLS-1$ //$NON-NLS-2$
				widgetProperties.remove("y"); //$NON-NLS-1$
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
		pendingWidgets = new LinkedList();
	}

	protected boolean shouldAppendCharactersToContent() {
		return state == WIDGET_ATTRIBUTE ||
			state == WIDGET_CHILD_ATTRIBUTE ||
			state == SIGNAL_ATTRIBUTE ||
			state == ACCELERATOR_ATTRIBUTE ||
			state == STYLE_ATTRIBUTE;
	}

	void noElementHere(String inner) {
		warnUnexpectedElement(state.getName(), inner);
		prevState = state;
		state = UNKNOWN;
		unknownDepth++;
	}

	final ParserState START = new ParserState("START") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if ( ! localName.equals("GTK-Interface")) { //$NON-NLS-1$
				warnUnexpectedElement("<null>", localName); //$NON-NLS-1$
			}
			state = GTK_INTERFACE;
		}

		void endElement(String uri, String localName, String qName) {
			logger.warn("should not be closing any elements in this state"); //$NON-NLS-1$
		}
	};

	final ParserState GTK_INTERFACE = new ParserState("GTK_INTERFACE") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("widget")) { //$NON-NLS-1$
				state = WIDGET;
				initializeWidgetInfo();
			} else if (localName.equals("style")) { //$NON-NLS-1$
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

	final ParserState WIDGET = new ParserState("WIDGET") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("style")) { //$NON-NLS-1$
				state = STYLE;
				// ignore all style stuff in Java
			} else if (localName.equals("accelerator") || localName.equals("Accelerator")) { //$NON-NLS-1$ //$NON-NLS-2$
				state = ACCELERATOR;
			} else if (localName.equals("signal") || localName.equals("Signal")) { //$NON-NLS-1$ //$NON-NLS-2$
				state = SIGNAL;
			} else if (localName.equals("child")) { //$NON-NLS-1$
				// the child section
				state = WIDGET_CHILD;
			} else if (localName.equals("widget")) { //$NON-NLS-1$
				initializeWidgetInfo();
			} else {
				propertyType = PropertyType.WIDGET;
				propertyName = localName;
				state = WIDGET_ATTRIBUTE;
				clearContent();
			}
		}

		void endElement(String uri, String localName, String qName) {
			if (localName.equals("widget")) { //$NON-NLS-1$
				flushWidgetInfo();
			}
		}
	};

	final ParserState WIDGET_ATTRIBUTE = new ParserState("WIDGET_ATTRIBUTE") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			state = WIDGET;
			String value = content.toString();
			WidgetInfo w = getLastPendingWidget();
			if (localName.equals("class")) { //$NON-NLS-1$
				if (value.startsWith("Gtk")) { //$NON-NLS-1$
					value = value.substring("Gtk".length()); //$NON-NLS-1$
				} else if (value.startsWith("Gnome")) {
					value = value.substring("Gnome".length()); //$NON-NLS-1$
				}
				w.setClassName(value);
			} else if (localName.equals("name")) { //$NON-NLS-1$
				w.setName(value);
			} else if (localName.equals("visible")) { //$NON-NLS-1$
				w.addProperty("visible", content.charAt(0) == 'T' ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (localName.equals("sensitive")) { //$NON-NLS-1$
				w.addProperty("sensitive", content.charAt(0) == 'T' ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (localName.equals("can_default")) { //$NON-NLS-1$
				w.addProperty("can_default", content.charAt(0) == 'T' ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (localName.equals("can_focus")) { //$NON-NLS-1$
				w.addProperty("can_focus", content.charAt(0) == 'T' ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (localName.equals("has_default")) { //$NON-NLS-1$
				w.addProperty("has_default", content.charAt(0) == 'T' ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (localName.equals("has_focus")) { //$NON-NLS-1$
				w.addProperty("has_focus", content.charAt(0) == 'T' ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else if (localName.equals("style_name")) { //$NON-NLS-1$
				// ignore
			} else {
				// some other attribute
				w.addProperty(propertyName, value);
			}
		}
	};

	final ParserState WIDGET_CHILD = new ParserState("WIDGET_CHILD") { //$NON-NLS-1$
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

	final ParserState WIDGET_CHILD_ATTRIBUTE = new ParserState("WIDGET_CHILD_ATTRIBUTE") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			properties.put(propertyName, content.toString());
			state = WIDGET_CHILD;
		}
	};

	final ParserState SIGNAL = new ParserState("SIGNAL") { //$NON-NLS-1$

		void startElement(String uri, String localName, String qName, Attributes attrs) {
			state = SIGNAL_ATTRIBUTE;
			clearContent();
		}

		void endElement(String uri, String localName, String qName) {
			state = WIDGET;
			SignalInfo signal = new SignalInfo(signalName, signalHandler, signalObject, signalAfter);
			signalName = ""; //$NON-NLS-1$
			signalHandler = ""; //$NON-NLS-1$
			signalObject = ""; //$NON-NLS-1$
			signalAfter = false;
			WidgetInfo w = getLastPendingWidget();
			w.addSignalInfo(signal);
		}
	};

	final ParserState SIGNAL_ATTRIBUTE = new ParserState("SIGNAL_ATTRIBUTE") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			state = SIGNAL;
			String value = content.toString();
			if (localName.equals("name")) { //$NON-NLS-1$
				signalName = value;
			} else if (localName.equals("handler")) { //$NON-NLS-1$
				signalHandler = value;
			} else if (localName.equals("data")) { //$NON-NLS-1$
				signalObject = value;
			} else if (localName.equals("after")) { //$NON-NLS-1$
				signalAfter = value.charAt(0) == 'T';
			}
		}
	};

	final ParserState ACCELERATOR = new ParserState("ACCELERATOR") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			state = ACCELERATOR_ATTRIBUTE;
			clearContent();
		}

		void endElement(String uri, String localName, String qName) {
			state = WIDGET;
			if (accelKey != 0) {
				AccelInfo accel = new AccelInfo(accelKey, accelModifiers, accelSignal);
				WidgetInfo w = getLastPendingWidget();
				w.addAccelInfo(accel);
			} else {
				logger.warn("accelerator ignored"); //$NON-NLS-1$
			}
		}
	};

	final ParserState ACCELERATOR_ATTRIBUTE = new ParserState("ACCELERATOR_ATTRIBUTE") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			state = ACCELERATOR;
			String value = content.toString();
			if (localName.equals("key") && value.startsWith("GDK_")) { //$NON-NLS-1$ //$NON-NLS-2$
				accelKey = keyCode(value);
			} else if (localName.equals("modifiers")) { //$NON-NLS-1$
				accelModifiers = parseModifiers(value);
			} else if (localName.equals("signal")) { //$NON-NLS-1$
				accelSignal = value;
			}
		}
	};

	final ParserState STYLE = new ParserState("STYLE") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			state = STYLE_ATTRIBUTE;
			clearContent();
		}

		void endElement(String uri, String localName, String qName) {
			// ignore all style stuff in Java
		}
	};

	final ParserState STYLE_ATTRIBUTE = new ParserState("STYLE_ATTRIBUTE") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			noElementHere(localName);
		}

		void endElement(String uri, String localName, String qName) {
			state = STYLE;
			// ignore all style stuff in Java
		}
	};
}
