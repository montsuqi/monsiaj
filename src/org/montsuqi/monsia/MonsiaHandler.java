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

import org.xml.sax.Attributes;

/** SAX document handler which parses interface definition by Glade version 2.
 */
class MonsiaHandler extends AbstractDocumentHandler {

	MonsiaHandler() {
		super();
		startState = START;
	}

	final ParserState START = new ParserState("START") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("monsia-interface")) { 
				state = MONSIA_INTERFACE;
			} else {
				Object[] args = { "monsia-interface", localName }; 
				logger.warn("expected <{0}>, but was <{1}>", args); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			logger.warn("should not be closing any elements in this state"); 
		}
	};

    final ParserState MONSIA_INTERFACE = new ParserState("MONSIA_INTERFACE") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("requires")) { 
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("lib")) { 
						// do nothing requires.add(value);
					} else {
						warnUnknownAttribute("requires", value); 
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
				warnUnexpectedElement("monsia-interface", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("monsia-interface")) { 
				warnShouldFindClosing("monsia-interface", localName); 
			}
			state = FINISH;
		}
	};

    final ParserState REQUIRES = new ParserState("REQUIRES") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("requires", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("requires")) { 
				warnShouldFindClosing("requires", localName); 
			}
			state = MONSIA_INTERFACE;
		}
	};

    final ParserState WIDGET = new ParserState("WIDGET") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("property")) { 
				boolean badAgent = false;

				if (propertyType != PropertyType.WIDGET) {
					warnInvalidPropertiesDefinedHere("widget"); 
				}
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("name")) { 
						propertyName = makePropertyName(value);
					} else if (attrName.equals("agent")) { 
						badAgent = value.equals("libglade"); 
					} else if (attrName.equals("translatable")) { 
						// ignore
					} else {
						warnUnknownAttribute("property", attrName); 
					}
				}
				if (badAgent) {
					// ignore the property
					prevState = state;
					state = UNKNOWN;
					unknownDepth++;
				} else {
					propertyType = PropertyType.WIDGET; 
					state = WIDGET_PROPERTY;
				}
			} else if (localName.equals("accessibility")) { 
				flushProperties();

				if (attrs.getLength() != 0) {
					warnShouldHaveNoAttributes("accessibility"); 
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
				warnUnexpectedElement("widget", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { 
				warnShouldFindClosing("widget", localName); 
			}
			flushProperties();
			flushSignals();
			flushAccels();
			dialogHack(widget);
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
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("property", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("property")) { 
				warnShouldFindClosing("property", localName); 
			}
			properties.put(propertyName, content.toString());
			propertyName = null;
			state = WIDGET;
		}
	};

    final ParserState WIDGET_ATK = new ParserState("WIDGET_ATK") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("atkproperty")) { 
				if (propertyType != PropertyType.ATK) { 
					warnInvalidPropertiesDefinedHere("atk"); 
				}
				propertyType = PropertyType.ATK;
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("name")) { 
						propertyName = makePropertyName(value);
					} else {
						warnUnknownAttribute("atkproperty", attrName); 
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
				warnUnexpectedElement("accessibility", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("accessibility")) { 
				warnShouldFindClosing("accessibility", localName); 
			}

			flushProperties(); // flush the ATK properties
			state = WIDGET_AFTER_ATK;
		}
	};

    final ParserState WIDGET_ATK_PROPERTY = new ParserState("WIDGET_ATK_PROPERTY") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("accessibility")) { 
				state = WIDGET_ATK;
			} else {
				warnUnexpectedElement("atkproperty", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkproperty")) { 
				warnShouldFindClosing("atkproperty", localName); 
			}
			properties.put(propertyName, content.toString());
			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_ATK_ACTION = new ParserState("WIDGET_ATK_ACTION") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("atkaction", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkaction")) { 
				warnShouldFindClosing("atkaction", localName); 
			}

			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_ATK_RELATION = new ParserState("WIDGET_ATK_RELATION") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("atkrelation", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkrelation")) { 
				warnShouldFindClosing("atkrelation", localName); 
			}

			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_AFTER_ATK = new ParserState("WIDGET_AFTER_ATK") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
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
				warnUnexpectedElement("widget", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { 
				warnShouldFindClosing("widget", localName); 
			}
			flushProperties();
			flushSignals();
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
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("signal", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("signal")) { 
				warnShouldFindClosing("signal", localName); 
			}
			state = WIDGET_AFTER_ATK;
		}
	};

    final ParserState WIDGET_AFTER_SIGNAL = new ParserState("WIDGET_AFTER_SIGNAL") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("accelerator")) { 
				handleAccel(attrs);
				state = WIDGET_ACCEL;
			} else if (localName.equals("child")) { 
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				warnUnexpectedElement("wiget", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { 
				warnShouldFindClosing("widget", localName); 
			}
			flushProperties();
			flushSignals();
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
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("accelerator", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("accelerator")) { 
				warnShouldFindClosing("accelerator", localName); 
			}
			state = WIDGET_AFTER_SIGNAL;
		}
	};

    final ParserState WIDGET_AFTER_ACCEL = new ParserState("WIDGET_AFTER_ACCEL") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("child")) { 
				handleChild(attrs);
				state = WIDGET_CHILD;
			} else {
				warnUnexpectedElement("widget", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { 
				warnShouldFindClosing("widget", localName); 
			}
			flushProperties();
			flushSignals();
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
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("widget")) { 
				WidgetInfo parent = widget;
				widget = createWidgetInfo(attrs);
				parent.getLastChild().setWidgetInfo(widget);
				widget.setParent(parent);
				widgetDepth++;
				propertyType = PropertyType.NONE;
				propertyName = null;
				properties.clear();
				signals.clear();
				accels.clear();
				state = WIDGET;
			} else if (localName.equals("placeholder")) { 
				// this isn't a real child, so knock off  the last ChildInfo
				state = WIDGET_CHILD_PLACEHOLDER;
			} else {
				warnUnexpectedElement("child", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { 
				warnShouldFindClosing("child", localName); 
			}
			logger.warn("no <widget> element found inside <child>, discarding"); 
			widget.removeLastChild();
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_WIDGET = new ParserState("WIDGET_CHILD_AFTER_WIDGET") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("packing")) { 
				state = WIDGET_CHILD_PACKING;
			} else {
				warnUnexpectedElement("child", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { 
				warnShouldFindClosing("child", localName); 
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_PACKING = new ParserState("WIDGET_CHILD_PACKING") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("property")) { 
				boolean badAgent = false;

				if (propertyType != PropertyType.CHILD) { 
					warnInvalidPropertiesDefinedHere("child"); 
				}
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("name")) { 
						propertyName = makePropertyName(value);
					} else if (attrName.equals("agent")) { 
						badAgent = value.equals("libglade"); 
					} else if (attrName.equals("translatable")) { 
						// ignore
					} else {
						warnUnknownAttribute("property", attrName); 
					}
				}
				if (badAgent) {
					// ignore the property ...
					prevState = state;
					state = UNKNOWN;
					unknownDepth++;
				} else {
					propertyType = PropertyType.CHILD; 
					state = WIDGET_CHILD_PACKING_PROPERTY;
				}
			} else {
				warnUnexpectedElement("child", localName); 
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("packing")) { 
				warnShouldFindClosing("packing", localName); 
			}
			state = WIDGET_CHILD_AFTER_PACKING;
			flushProperties();
		}
	};

    final ParserState WIDGET_CHILD_PACKING_PROPERTY = new ParserState("WIDGET_CHILD_PACKING_PROPERTY") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("property", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("property")) { 
				warnShouldFindClosing("property", localName); 
			}
			properties.put(propertyName, content.toString());
			propertyName = null;
			state = WIDGET_CHILD_PACKING;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_PACKING = new ParserState("WIDGET_CHILD_AFTER_PACKING") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn("<child> should have no elements after <packing>, found <{0}>", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { 
				warnShouldFindClosing("child", localName); 
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_PLACEHOLDER = new ParserState("WIDGET_CHILD_PLACEHOLDER") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("placeholder", localName); 
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("placeholder")) { 
				warnShouldFindClosing("placeholder", localName); 
			}
			state = WIDGET_CHILD_AFTER_PLACEHOLDER;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_PLACEHOLDER = new ParserState("WIDGET_CHILD_AFTER_PLACEHOLDER") { 
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			// this is a placeholder <child> element -- ignore extra elements
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { 
				warnShouldFindClosing("child", localName); 
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

	protected boolean shouldAppendCharactersToContent() {
		return state == WIDGET_PROPERTY ||
			state == WIDGET_ATK_PROPERTY ||
			state == WIDGET_CHILD_PACKING_PROPERTY;
	}

	protected WidgetInfo createWidgetInfo(Attributes attrs) {

		String className = null;
		String name = null;
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("class")) { 
				className = value;
			} else if (attrName.equals("id")) { 
				name = value;
			} else {
				warnUnknownAttribute("widget", attrName); 
			}
		}

		if (className == null || name == null) {
			warnMissingAttribute("widget"); 
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
			if (attrName.equals("target")) { 
				target = value;
			} else if (attrName.equals("type")) { 
				type = value;
			} else {
				warnUnknownAttribute("signal", attrName); 
			}
		}
		if (target == null || type == null) {
			warnMissingAttribute("atkrelation"); 
			return;
		}
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
			if (attrName.equals("name")) { 
				name = value;
			} else if (attrName.equals("handler")) { 
				handler = value;
			} else if (attrName.equals("after")) { 
				after = value.startsWith("y"); 
			} else if (attrName.equals("object")) { 
				object = value;
			} else if (attrName.equals("last_modification_time")) { 
				// do nothing
			} else {
				warnUnknownAttribute("signal", attrName); 
			}
		}

		if (name == null || handler == null) {
			warnMissingAttribute("signal"); 
			return;
		}
		signals.add(new SignalInfo(name, handler, object, after));
	}

	protected void handleAccel(Attributes attrs) {
		flushProperties();
		flushSignals();
		int key = 0;
		int modifiers = 0;
		String signal = null;
		for (int i = 0, n = attrs.getLength(); i < n; i++) {  
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("key")) { 
				key = keyCode(value);
			} else if (attrName.equals("modifiers")) { 
				modifiers = parseModifiers(value);
			} else if (attrName.equals("signal")) { 
				signal = value;
			} else {
				warnUnknownAttribute("accelerator", attrName); 
			}
		}
		if (key == 0 || signal == null) {
			warnMissingAttribute("accelerator"); 
			return;
		}
		accels.add(new AccelInfo(key, modifiers, signal));
	}

	protected void handleChild(Attributes attrs) {
		flushProperties();
		flushSignals();
		flushAccels();

		ChildInfo info = new ChildInfo();
		widget.addChild(info);
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			if (attrName.equals("internal-child")) { 
				// ignore
			} else {
				warnUnknownAttribute("child", attrName); 
			}
		}
	}

	protected void handleATKAction(Attributes attrs) {
		flushProperties();

		String actionName = null;

		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("action_name")) { 
				actionName = value;
			} else if (attrName.equals("description")) { 
				// ignore
			} else {
				warnUnknownAttribute("action", attrName); 
			}
		}

		if (actionName == null) {
			warnMissingAttribute("atkaction"); 
			return;
		}
	}
}
