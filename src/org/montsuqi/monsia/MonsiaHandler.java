/*      PANDA -- a simple transaction monitor
                                                                                
Copyright (C) 1998-1999 Ogochan.
			  2000-2003 Ogochan & JMA (Japan Medical Association).
                                                                                
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

class MonsiaHandler extends AbstractDocumentHandler {

	MonsiaHandler() {
		super();
		startState = START;
	}

	final ParserState START = new ParserState("START") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("monsia-interface")) { //$NON-NLS-1$
				state = MONSIA_INTERFACE;
			} else {
				logger.warn(Messages.getString("MonsiaHandler.expected"), new Object[] { "monsia-interface", localName}); //$NON-NLS-2$ //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			logger.warn(Messages.getString("AbstractHandler.should_not_be_closing_any_elements_in_this_state")); //$NON-NLS-1$
		}
	};

    final ParserState MONSIA_INTERFACE = new ParserState("MONSIA_INTERFACE") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("requires")) { //$NON-NLS-1$
				for (int i = 0, n = attrs.getLength(); i < n; i++) {
					String attrName = attrs.getLocalName(i);
					String value = attrs.getValue(i);
					if (attrName.equals("lib")) { //$NON-NLS-1$
						// do nothing requires.add(value);
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

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("monsia-interface")) { //$NON-NLS-1$
				warnShouldFindClosing("monsia-interface", localName); //$NON-NLS-1$
			}
			state = FINISH;
		}
	};

    final ParserState REQUIRES = new ParserState("REQUIRES") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("requires", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("requires")) { //$NON-NLS-1$
				warnShouldFindClosing("requires", localName); //$NON-NLS-1$
			}
			state = MONSIA_INTERFACE;
		}
	};

    final ParserState WIDGET = new ParserState("WIDGET") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("property")) { //$NON-NLS-1$
				boolean badAgent = false;
				
				if (propertyType != PropertyType.WIDGET) {
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
					// ignore the property
					prevState = state;
					state = UNKNOWN;
					unknownDepth++;
				} else {
					propertyType = PropertyType.WIDGET; //$NON-NLS-1$
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

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { //$NON-NLS-1$
				warnShouldFindClosing("widget", localName); //$NON-NLS-1$
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

    final ParserState WIDGET_PROPERTY = new ParserState("WIDGET_PROPERTY") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("property", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("property")) { //$NON-NLS-1$
				warnShouldFindClosing("property", localName); //$NON-NLS-1$
			}
			properties.put(propertyName, content.toString());
			propertyName = null;
			state = WIDGET;
		}
	};

    final ParserState WIDGET_ATK = new ParserState("WIDGET_ATK") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("atkproperty")) { //$NON-NLS-1$
				if (propertyType != PropertyType.ATK) { //$NON-NLS-1$
					warnInvalidPropertiesDefinedHere("atk"); //$NON-NLS-1$
				}
				propertyType = PropertyType.ATK;
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

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("accessibility")) { //$NON-NLS-1$
				warnShouldFindClosing("accessibility", localName); //$NON-NLS-1$
			}
			
			flushProperties(); // flush the ATK properties
			state = WIDGET_AFTER_ATK;
		}
	};

    final ParserState WIDGET_ATK_PROPERTY = new ParserState("WIDGET_ATK_PROPERTY") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("accessibility")) { //$NON-NLS-1$
				state = WIDGET_ATK;
			} else {
				warnUnexpectedElement("atkproperty", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkproperty")) { //$NON-NLS-1$
				warnShouldFindClosing("atkproperty", localName); //$NON-NLS-1$
			}
			properties.put(propertyName, content.toString());
			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_ATK_ACTION = new ParserState("WIDGET_ATK_ACTION") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("atkaction", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkaction")) { //$NON-NLS-1$
				warnShouldFindClosing("atkaction", localName); //$NON-NLS-1$
			}
			
			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_ATK_RELATION = new ParserState("WIDGET_ATK_RELATION") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("atkrelation", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if (localName.equals("atkrelation")) { //$NON-NLS-1$
				warnShouldFindClosing("atkrelation", localName); //$NON-NLS-1$
			}

			propertyName = null;
			state = WIDGET_ATK;
		}
	};

    final ParserState WIDGET_AFTER_ATK = new ParserState("WIDGET_AFTER_ATK") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
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

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { //$NON-NLS-1$
				warnShouldFindClosing("widget", localName); //$NON-NLS-1$
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

    final ParserState WIDGET_SIGNAL = new ParserState("WIDGET_SIGNAL") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("signal", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("signal")) { //$NON-NLS-1$
				warnShouldFindClosing("signal", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_ATK;
		}
	};

    final ParserState WIDGET_AFTER_SIGNAL = new ParserState("WIDGET_AFTER_SIGNAL") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
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

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { //$NON-NLS-1$
				warnShouldFindClosing("widget", localName); //$NON-NLS-1$
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

    final ParserState WIDGET_ACCEL = new ParserState("WIDGET_ACCEL") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("accelerator", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("accelerator")) { //$NON-NLS-1$
				warnShouldFindClosing("accelerator", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_SIGNAL;
		}
	};

    final ParserState WIDGET_AFTER_ACCEL = new ParserState("WIDGET_AFTER_ACCEL") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
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

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("widget")) { //$NON-NLS-1$
				warnShouldFindClosing("widget", localName); //$NON-NLS-1$
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

    final ParserState WIDGET_CHILD = new ParserState("WIDGET_CHILD") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("widget")) { //$NON-NLS-1$
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
			} else if (localName.equals("placeholder")) { //$NON-NLS-1$
				// this isn't a real child, so knock off  the last ChildInfo
				state = WIDGET_CHILD_PLACEHOLDER;
			} else {
				warnUnexpectedElement("child", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
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
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("packing")) { //$NON-NLS-1$
				state = WIDGET_CHILD_PACKING;
			} else {
				warnUnexpectedElement("child", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { //$NON-NLS-1$
				warnShouldFindClosing("child", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_PACKING = new ParserState("WIDGET_CHILD_PACKING") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equals("property")) { //$NON-NLS-1$
				boolean badAgent = false;
				
				if (propertyType != PropertyType.CHILD) { //$NON-NLS-1$
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
					// ignore the property ...
					prevState = state;
					state = UNKNOWN;
					unknownDepth++;
				} else {
					propertyType = PropertyType.CHILD; //$NON-NLS-1$
					state = WIDGET_CHILD_PACKING_PROPERTY;
				}
			} else {
				warnUnexpectedElement("child", localName); //$NON-NLS-1$
				prevState = state;
				state = UNKNOWN;
				unknownDepth++;
			}
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("packing")) { //$NON-NLS-1$
				warnShouldFindClosing("packing", localName); //$NON-NLS-1$
			}
			state = WIDGET_CHILD_AFTER_PACKING;
			flushProperties();
		}
	};

    final ParserState WIDGET_CHILD_PACKING_PROPERTY = new ParserState("WIDGET_CHILD_PACKING_PROPERTY") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("property", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("property")) { //$NON-NLS-1$
				warnShouldFindClosing("property", localName); //$NON-NLS-1$
			}
			properties.put(propertyName, content.toString());
			propertyName = null;
			state = WIDGET_CHILD_PACKING;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_PACKING = new ParserState("WIDGET_CHILD_AFTER_PACKING") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			logger.warn(Messages.getString("MonsiaHandler.child_should_have_no_elements_after_packing"), localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { //$NON-NLS-1$
				warnShouldFindClosing("child", localName); //$NON-NLS-1$
			}
			state = WIDGET_AFTER_ACCEL;
		}
	};

    final ParserState WIDGET_CHILD_PLACEHOLDER = new ParserState("WIDGET_CHILD_PLACEHOLDER") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			warnShouldBeEmpty("placeholder", localName); //$NON-NLS-1$
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("placeholder")) { //$NON-NLS-1$
				warnShouldFindClosing("placeholder", localName); //$NON-NLS-1$
			}
			state = WIDGET_CHILD_AFTER_PLACEHOLDER;
		}
	};

    final ParserState WIDGET_CHILD_AFTER_PLACEHOLDER = new ParserState("WIDGET_CHILD_AFTER_PLACEHOLDER") { //$NON-NLS-1$
		void startElement(String uri, String localName, String qName, Attributes attrs) {
			// this is a placeholder <child> element -- ignore extra elements
			prevState = state;
			state = UNKNOWN;
			unknownDepth++;
		}

		void endElement(String uri, String localName, String qName) {
			if ( ! localName.equals("child")) { //$NON-NLS-1$
				warnShouldFindClosing("child", localName); //$NON-NLS-1$
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
				// do nothing
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

	protected void handleAccel(Attributes attrs) {
		flushProperties();
		flushSignals();
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

	protected void handleChild(Attributes attrs) {
		flushProperties();
		flushSignals();
		flushAccels();
	
		ChildInfo info = new ChildInfo();
		widget.addChild(info);
		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			if (attrName.equals("internal-child")) { //$NON-NLS-1$
				// ignore
			} else {
				warnUnknownAttribute("child", attrName); //$NON-NLS-1$
			}
		}
	}

	protected void handleATKAction(Attributes attrs) {
		flushProperties();
	
		String actionName = null;

		for (int i = 0, n = attrs.getLength(); i < n; i++) {
			String attrName = attrs.getLocalName(i);
			String value = attrs.getValue(i);
			if (attrName.equals("action_name")) { //$NON-NLS-1$
				actionName = value;
			} else if (attrName.equals("description")) { //$NON-NLS-1$
				// ignore
			} else {
				warnUnknownAttribute("action", attrName); //$NON-NLS-1$
			}
		}
	
		if (actionName == null) {
			warnMissingAttribute("atkaction"); //$NON-NLS-1$
			return;
		}
	}
}
