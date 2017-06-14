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
package org.montsuqi.monsiaj.monsia;

import java.util.LinkedList;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * SAX document handler which parses interface definition by Glade version 1.
 */
public class Glade1Handler extends AbstractDocumentHandler {

    private final LinkedList<WidgetInfo> pendingWidgets;
    String signalName;
    String signalHandler;
    String signalObject;
    boolean signalAfter;

    int accelKey;
    int accelModifiers;
    String accelSignal;

    WidgetInfo getLastPendingWidget() {
        return (WidgetInfo) pendingWidgets.getLast();
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
            if (widgetProperties.containsKey("x")) {
                childInfo.addProperty("x", (String) widgetProperties.get("x"));  //$NON-NLS-2$
                //widgetProperties.remove("x"); 
            }
            if (widgetProperties.containsKey("y")) {
                childInfo.addProperty("y", (String) widgetProperties.get("y"));  //$NON-NLS-2$
                //widgetProperties.remove("y"); 
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
        pendingWidgets = new LinkedList<>();
    }

    @Override
    protected boolean shouldAppendCharactersToContent() {
        return state == WIDGET_ATTRIBUTE
                || state == WIDGET_CHILD_ATTRIBUTE
                || state == SIGNAL_ATTRIBUTE
                || state == ACCELERATOR_ATTRIBUTE
                || state == STYLE_ATTRIBUTE
                || state == PREVIEW;
    }

    void noElementHere(String inner) {
        warnUnexpectedElement(state.getName(), inner);
        prevState = state;
        state = UNKNOWN;
        unknownDepth++;
    }

    final ParserState START = new ParserState("START") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (!localName.equals("GTK-Interface")) {
                warnUnexpectedElement("<null>", localName);
            }
            state = GTK_INTERFACE;
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            logger.warn("should not be closing any elements in this state");
        }
    };

    final ParserState GTK_INTERFACE = new ParserState("GTK_INTERFACE") {
        @Override
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

        @Override
        void endElement(String uri, String localName, String qName) {
            state = FINISH;
        }
    };

    final ParserState WIDGET = new ParserState("WIDGET") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            if (localName.equals("style")) {
                state = STYLE;
                // ignore all style stuff in Java
            } else if (localName.equals("accelerator") || localName.equals("Accelerator")) {  //$NON-NLS-2$
                state = ACCELERATOR;
            } else if (localName.equals("signal") || localName.equals("Signal")) {  //$NON-NLS-2$
                state = SIGNAL;
            } else if (localName.equals("child")) {
                // the child section
                state = WIDGET_CHILD;
            } else if (localName.equals("widget")) {
                initializeWidgetInfo();
            } else if (localName.equals("preview") || localName.equals("Preview")) {
                state = PREVIEW;
            } else {
                propertyType = PropertyType.WIDGET;
                propertyName = localName;
                state = WIDGET_ATTRIBUTE;
                clearContent();
            }
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            if (localName.equals("widget")) {
                flushWidgetInfo();
            }
        }
    };

    final ParserState WIDGET_ATTRIBUTE = new ParserState("WIDGET_ATTRIBUTE") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            noElementHere(localName);
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            state = WIDGET;
            String value = content.toString();
            WidgetInfo w = getLastPendingWidget();
            if (localName.equals("class")) {
                if (value.startsWith("Gtk")) {
                    value = value.substring("Gtk".length());
                } else if (value.startsWith("Gnome")) {
                    value = value.substring("Gnome".length());
                }
                w.setClassName(value);
            } else if (localName.equals("name")) {
                w.setName(value);
            } else if (localName.equals("visible")) {
                w.addProperty("visible", content.charAt(0) == 'T' ? "true" : "false");  //$NON-NLS-2$ //$NON-NLS-3$
            } else if (localName.equals("sensitive")) {
                w.addProperty("sensitive", content.charAt(0) == 'T' ? "true" : "false");  //$NON-NLS-2$ //$NON-NLS-3$
            } else if (localName.equals("can_default")) {
                w.addProperty("can_default", content.charAt(0) == 'T' ? "true" : "false");  //$NON-NLS-2$ //$NON-NLS-3$
            } else if (localName.equals("can_focus")) {
                w.addProperty("can_focus", content.charAt(0) == 'T' ? "true" : "false");  //$NON-NLS-2$ //$NON-NLS-3$
            } else if (localName.equals("has_default")) {
                w.addProperty("has_default", content.charAt(0) == 'T' ? "true" : "false");  //$NON-NLS-2$ //$NON-NLS-3$
            } else if (localName.equals("has_focus")) {
                w.addProperty("has_focus", content.charAt(0) == 'T' ? "true" : "false");  //$NON-NLS-2$ //$NON-NLS-3$
            } else if (localName.equals("style_name")) {
                // ignore
            } else {
                // some other attribute
                w.addProperty(propertyName, value);
            }
        }
    };

    final ParserState WIDGET_CHILD = new ParserState("WIDGET_CHILD") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            propertyType = PropertyType.CHILD;
            propertyName = localName;
            state = WIDGET_CHILD_ATTRIBUTE;
            clearContent();
        }

        @Override
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
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            noElementHere(localName);
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            properties.put(propertyName, content.toString());
            state = WIDGET_CHILD;
        }
    };

    final ParserState SIGNAL = new ParserState("SIGNAL") {

        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            state = SIGNAL_ATTRIBUTE;
            clearContent();
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            state = WIDGET;
            SignalInfo signal = new SignalInfo(signalName, signalHandler, signalObject, signalAfter);
            signalName = "";
            signalHandler = "";
            signalObject = "";
            signalAfter = false;
            WidgetInfo w = getLastPendingWidget();
            w.addSignalInfo(signal);
        }
    };

    final ParserState SIGNAL_ATTRIBUTE = new ParserState("SIGNAL_ATTRIBUTE") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            noElementHere(localName);
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            state = SIGNAL;
            String value = content.toString();
            if (localName.equals("name")) {
                signalName = value;
            } else if (localName.equals("handler")) {
                signalHandler = value;
            } else if (localName.equals("data")) {
                signalObject = value;
            } else if (localName.equals("after")) {
                signalAfter = value.charAt(0) == 'T';
            }
        }
    };

    final ParserState ACCELERATOR = new ParserState("ACCELERATOR") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            state = ACCELERATOR_ATTRIBUTE;
            clearContent();
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            state = WIDGET;
            if (accelKey != 0) {
                AccelInfo accel = new AccelInfo(accelKey, accelModifiers, accelSignal);
                WidgetInfo w = getLastPendingWidget();
                w.addAccelInfo(accel);
            } else {
                logger.warn("accelerator ignored");
            }
        }
    };

    final ParserState ACCELERATOR_ATTRIBUTE = new ParserState("ACCELERATOR_ATTRIBUTE") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            noElementHere(localName);
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            state = ACCELERATOR;
            String value = content.toString();
            if (localName.equals("key") && value.startsWith("GDK_")) {  //$NON-NLS-2$
                accelKey = keyCode(value);
            } else if (localName.equals("modifiers")) {
                accelModifiers = parseModifiers(value);
            } else if (localName.equals("signal")) {
                accelSignal = value;
            }
        }
    };

    final ParserState STYLE = new ParserState("STYLE") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            state = STYLE_ATTRIBUTE;
            clearContent();
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            // ignore all style stuff in Java
        }
    };

    final ParserState STYLE_ATTRIBUTE = new ParserState("STYLE_ATTRIBUTE") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            noElementHere(localName);
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            state = STYLE;
            // ignore all style stuff in Java
        }
    };

    final ParserState PREVIEW = new ParserState("PREVIEW") {
        @Override
        void startElement(String uri, String localName, String qName, Attributes attrs) {
            noElementHere(localName);
        }

        @Override
        void endElement(String uri, String localName, String qName) {
            WidgetInfo w = getLastPendingWidget();
            String value = content.toString();
            w.addProperty("do_preview", value.charAt(0) == 'T' ? "true" : "false");
            state = WIDGET;
        }
    };
}
