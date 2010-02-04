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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.montsuqi.client.Protocol;
import org.montsuqi.client.SignalHandler;
import org.montsuqi.monsia.builders.WidgetBuilder;
import org.montsuqi.util.Logger;
import org.montsuqi.widgets.OptionMenu;
import org.montsuqi.widgets.PandaFocusManager;

/** <p>An class that represents the result of parsing Glade's interface definition.
 */
public class Interface {

    private Map widgets;
    private Map longNames;
    private Map buttonGroups;
    private Protocol protocol;
    private Component topLevel;
    private List signals;
    private Component focusWidget;
    private Component defaultWidget;
    private JMenuBar menuBar;
    private static final Logger logger = Logger.getLogger(Interface.class);
    private static Map accelHandlers;


    static {
        KeyboardFocusManager.setCurrentKeyboardFocusManager(new PandaFocusManager());
        accelHandlers = new HashMap();
    }
    private static final String OLD_HANDLER = "org.montsuqi.monsia.Glade1Handler"; //$NON-NLS-1$
    private static final String NEW_HANDLER = "org.montsuqi.monsia.MonsiaHandler"; //$NON-NLS-1$

    public void setDefaultWidget(Component widget) {
        defaultWidget = widget;
    }

    public void setFocusWidget(Component widget) {
        focusWidget = widget;
    }
    private static final SAXParser saxParser;


    static {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        try {
            saxParser = parserFactory.newSAXParser();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private static final String oldPrologue = "<?xml version=\"1.0\"?>\n<GTK-Interface>\n"; //$NON-NLS-1$
    private static final int OLD_PROLOGUE_LENGTH;


    static {
        OLD_PROLOGUE_LENGTH = oldPrologue.getBytes().length;
    }

    /** <p>A factory method that builds an Interface instance.</p>
     * <p>This method takes its source XML from <var>input</var> InputStream and parses
     * it using a SAX parser.</p>
     * <p>SAX parser is selected in following way:</p>
     * <ol>
     * <li>If system property "monsia.document.handler" is set, use it.</li>
     * <li>Otherwise, if the input's beginning looks like old interface definition(root element
     * is GTK-Interface and such), old handler is used.</li>
     * <li>Otherwise, new handler is used.</li>
     * </ol>
     * @param input source input stream from which the Glade file is read.
     * @param protocol protocol(connection) object passed to signal connectors.
     * @return an Interface instance.
     */
    public static Interface parseInput(InputStream input, Protocol protocol) {
        try {
            if (!(input instanceof BufferedInputStream)) {
                input = new BufferedInputStream(input);
            }

            String handlerClassName = System.getProperty("monsia.document.handler"); //$NON-NLS-1$
            if (handlerClassName == null) {
                handlerClassName = isNewScreenDefinition(input) ? NEW_HANDLER : OLD_HANDLER;
            }

            Class handlerClass = Class.forName(handlerClassName);
            AbstractDocumentHandler handler = (AbstractDocumentHandler) handlerClass.newInstance();

            if (handlerClassName.equals(OLD_HANDLER)) {
                if (protocol.getEncoding().equals("EUC-JP")) {
                    input = new FakeEncodingInputStream(input);
                }
            }
            saxParser.parse(input, handler);
            return handler.getInterface(protocol);
        } catch (Exception e) {
            throw new InterfaceBuildingException(e);
        }
    }

    public static Interface parseInput(InputStream input) {
        try {
            if (!(input instanceof BufferedInputStream)) {
                input = new BufferedInputStream(input);
            }

            String handlerClassName = System.getProperty("monsia.document.handler"); //$NON-NLS-1$
            if (handlerClassName == null) {
                handlerClassName = isNewScreenDefinition(input) ? NEW_HANDLER : OLD_HANDLER;
            }

            Class handlerClass = Class.forName(handlerClassName);
            AbstractDocumentHandler handler = (AbstractDocumentHandler) handlerClass.newInstance();

            if (handlerClassName.equals(OLD_HANDLER)) {
                input = new FakeEncodingInputStream(input);
            }
            saxParser.parse(input, handler);
            return handler.getInterface();
        } catch (Exception e) {
            throw new InterfaceBuildingException(e);
        }
    }

    private static boolean isNewScreenDefinition(InputStream input) throws IOException {
        byte[] bytes = new byte[OLD_PROLOGUE_LENGTH];
        input.mark(OLD_PROLOGUE_LENGTH);
        input.read(bytes);
        String head = new String(bytes);
        input.reset();
        return head.indexOf("GTK-Interface") < 0; //$NON-NLS-1$
    }

    Interface(List roots, Protocol protocol) {
        widgets = new HashMap();
        longNames = new HashMap();
        signals = new ArrayList();
        buttonGroups = new HashMap();
        topLevel = null;
        defaultWidget = null;
        focusWidget = null;
        this.protocol = protocol;
        buildWidgetTree(roots);
        signalAutoConnect();
    }

    public Interface(List roots) {
        widgets = new HashMap();
        longNames = new HashMap();
        signals = new ArrayList();
        buttonGroups = new HashMap();
        topLevel = null;
        defaultWidget = null;
        focusWidget = null;
        buildWidgetTree(roots);
    }

    private void signalAutoConnect() {
        Iterator entries = signals.iterator();
        while (entries.hasNext()) {
            SignalData data = (SignalData) entries.next();
            String handlerName = data.getHandler().toLowerCase();
            SignalHandler handler = SignalHandler.getSignalHandler(handlerName);
            if (data.isAfter()) {
                connectAfter(handler, data);
            } else {
                connect(handler, data);
            }
        }
    }

    private void connect(SignalHandler handler, SignalData data) {
        Component target = data.getTarget();
        if (target instanceof JTextField) {
            Component parent = target.getParent();
            if (parent instanceof JComboBox && !(parent instanceof OptionMenu)) {
                target = parent;
            }
        }
        Connector connector = Connector.getConnector(data.getName());
        connector.connect(protocol, target, handler, data.getObject());
    }

    private void connectAfter(SignalHandler handler, SignalData data) {
        connect(handler, data);
    }

    public Component getWidget(String name) {
        if (name == null) {
            throw new NullPointerException("name is null."); //$NON-NLS-1$
        }
        return (Component) widgets.get(name);
    }

    public Component getWidgetByLongName(String longName) {
        if (longName == null) {
            throw new NullPointerException("long name is null."); //$NON-NLS-1$
        }
        return (Component) longNames.get(longName);
    }

    public void setButtonGroup(JRadioButton button, String groupName) {
        JRadioButton none;
        ButtonGroup group;
        if (!buttonGroups.containsKey(groupName)) {
            group = new ButtonGroup();
            buttonGroups.put(groupName, group);
            none = new JRadioButton();
            none.putClientProperty("none", none); //$NON-NLS-1$
            group.add(none);
        } else {
            group = (ButtonGroup) buttonGroups.get(groupName);
            assert group.getButtonCount() > 0;
            JRadioButton first = (JRadioButton) group.getElements().nextElement();
            none = (JRadioButton) first.getClientProperty("none"); //$NON-NLS-1$
        }
        group.add(button);
        button.putClientProperty("group", group); //$NON-NLS-1$
        button.putClientProperty("none", none); //$NON-NLS-1$
    }

    public void setTopLevel(Component widget) {
        if (focusWidget != null) {
            focusWidget.requestFocus();
        }

        if (defaultWidget != null) {
            defaultWidget.requestFocus();
        }
        focusWidget = null;
        defaultWidget = null;
        topLevel = widget;
    }

    public void addSignal(SignalData sData) {
        signals.add(0, sData);
    }

    public void addAccels(Component widget, WidgetInfo info) {
        if (widget instanceof Window) {
            return;
        }
        AccelHandler handler = getAccelHandler(topLevel);
        handler.addAccels(widget, info.getAccels());
    }

    void buildWidgetTree(List roots) {
        if (roots == null || roots.isEmpty()) {
            return;
        }
        Iterator i = roots.iterator();
        while (i.hasNext()) {
            WidgetInfo info = (WidgetInfo) i.next();
            Component widget = WidgetBuilder.buildWidget(this, info, null);
            setName(info.getName(), widget);
            assert widget instanceof JFrame;
            JFrame f = (JFrame) widget;
            if (menuBar != null) {
                f.setJMenuBar(menuBar);
                setWindowForMenuElements(f, menuBar);
            }
        }
    }

    private void setWindowForMenuElements(JFrame f, MenuElement me) {
        MenuElement[] subs = me.getSubElements();
        for (int i = 0; i < subs.length; i++) {
            MenuElement sub = subs[i];
            JComponent c = (JComponent) sub.getComponent();
            c.putClientProperty("window", f); //$NON-NLS-1$
            setWindowForMenuElements(f, sub);
        }
    }

    public void setName(String name, Component widget) {
        widgets.put(name, widget);
    }

    public void setLongName(String longName, Component widget) {
        if (longNames.containsKey(longName)) {
            logger.warn("widget named \"{0}\" already exists, replaceing with new one.", longName); //$NON-NLS-1$
        }
        longNames.put(longName, widget);
    }

    public static boolean handleAccels(KeyEvent e) {
        Component c = (Component) e.getSource();
        while (c.getParent() != null && !(c instanceof Dialog)) {
            c = c.getParent();
        }
        AccelHandler handler = getAccelHandler(c);
        return handler.handleAccel(e);
    }

    private static AccelHandler getAccelHandler(Component c) {
        if (!accelHandlers.containsKey(c.getName())) {
            accelHandlers.put(c.getName(), new AccelHandler());
        }
        AccelHandler handler = (AccelHandler) accelHandlers.get(c.getName());
        return handler;
    }

    public void setMenuBar(JMenuBar menuBar) {
        if (this.menuBar != null && this.menuBar != menuBar) {
            logger.warn("menubar is already set, replacing with new one."); //$NON-NLS-1$
        }
        this.menuBar = menuBar;
    }
}
