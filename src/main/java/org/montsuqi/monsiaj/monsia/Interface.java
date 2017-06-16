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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Insets;
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
import java.util.StringTokenizer;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.MenuElement;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.client.SignalHandler;
import org.montsuqi.monsiaj.client.UIControl;
import org.montsuqi.monsiaj.monsia.builders.WidgetBuilder;
import org.montsuqi.monsiaj.util.ParameterConverter;
import org.montsuqi.monsiaj.widgets.PandaCList;
import org.montsuqi.monsiaj.widgets.PandaFocusManager;
import org.xml.sax.SAXException;

/**
 * <
 * p>
 * An class that represents the result of parsing Glade's interface definition.
 */
public class Interface {

    private Map<String, Component> widgetNameTable;
    private Map<String, Component> widgetLongNameTable;
    private Map<String, Map<String, String>> propertyTable;
    private Map<String, ButtonGroup> buttonGroups;
    private UIControl uiControl;
    private Component topLevel;
    private List<SignalData> signals;
    private Component focusWidget;
    private Component defaultWidget;
    private JMenuBar menuBar;
    private static final Logger logger = LogManager.getLogger(Interface.class);
    private static Map<String, AccelHandler> accelHandlers;
    private double phScale = 1.0;
    private double pvScale = 1.0;

    static {
        KeyboardFocusManager.setCurrentKeyboardFocusManager(new PandaFocusManager());
        accelHandlers = new HashMap<>();
    }
    private static final String OLD_HANDLER = "org.montsuqi.monsiaj.monsia.Glade1Handler";
    private static final String NEW_HANDLER = "org.montsuqi.monsiaj.monsia.MonsiaHandler";

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
        } catch (ParserConfigurationException | SAXException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    private static final int OLD_PROLOGUE_LENGTH = 128;

    /**
     * <
     * p>
     * A factory method that builds an Interface instance.</p>
     * <p>
     * This method takes its source XML from <var>input</var> InputStream and
     * parses it using a SAX parser.</p>
     * <p>
     * SAX parser is selected in following way:</p>
     * <ol>
     * <li>If system property "monsia.document.handler" is set, use it.</li>
     * <li>Otherwise, if the input's beginning looks like old interface
     * definition(root element is GTK-Interface and such), old handler is
     * used.</li>
     * <li>Otherwise, new handler is used.</li>
     * </ol>
     *
     * @param input source input stream from which the Glade file is read.
     * @param uiControl
     * @return an Interface instance.
     */
    public static Interface parseInput(InputStream input, UIControl uiControl) {
        try {
            if (!(input instanceof BufferedInputStream)) {
                input = new BufferedInputStream(input);
            }

            String handlerClassName = System.getProperty("monsia.document.handler");
            if (handlerClassName == null) {
                handlerClassName = isNewScreenDefinition(input) ? NEW_HANDLER : OLD_HANDLER;
            }

            Class handlerClass = Class.forName(handlerClassName);
            AbstractDocumentHandler handler = (AbstractDocumentHandler) handlerClass.newInstance();

            saxParser.parse(input, handler);
            return handler.getInterface(uiControl);
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | SAXException e) {
            throw new InterfaceBuildingException(e);
        }
    }

    public static Interface parseInput(InputStream input) {
        try {
            if (!(input instanceof BufferedInputStream)) {
                input = new BufferedInputStream(input);
            }

            String handlerClassName = System.getProperty("monsia.document.handler");
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
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | SAXException e) {
            throw new InterfaceBuildingException(e);
        }
    }

    private static boolean isNewScreenDefinition(InputStream input) throws IOException {
        byte[] bytes = new byte[OLD_PROLOGUE_LENGTH];
        input.mark(OLD_PROLOGUE_LENGTH);
        input.read(bytes);
        String head = new String(bytes);
        input.reset();
        return !head.contains("GTK-Interface");
    }

    private void initMember() {
        widgetNameTable = new HashMap<>();
        widgetLongNameTable = new HashMap<>();
        propertyTable = new HashMap<>();
        signals = new ArrayList<>();
        buttonGroups = new HashMap<>();
        topLevel = null;
        defaultWidget = null;
        focusWidget = null;
    }

    public Interface(List roots, UIControl uiControl) {
        initMember();
        this.uiControl = uiControl;
        buildWidgetTree(roots);
        signalAutoConnect();
    }

    public Interface(List roots) {
        initMember();
        buildWidgetTree(roots);
    }

    private void signalAutoConnect() {
        for (SignalData data : signals) {
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
        Connector connector = Connector.getConnector(data.getName());
        connector.connect(uiControl, target, handler, data.getObject());
    }

    private void connectAfter(SignalHandler handler, SignalData data) {
        connect(handler, data);
    }

    public Component getWidget(String name) {
        if (name == null) {
            throw new NullPointerException("name is null.");
        }
        return (Component) widgetNameTable.get(name);
    }

    public Component getAnyWidget() {
        for (Component c : widgetNameTable.values()) {
            if (c.isFocusable()) {
                return c;
            }
        }
        return null;
    }

    public Component getWidgetByLongName(String longName) {
        if (longName == null) {
            throw new NullPointerException("long name is null.");
        }
        return (Component) widgetLongNameTable.get(longName);
    }

    public void setButtonGroup(JRadioButton button, String groupName) {
        JRadioButton none;
        ButtonGroup group;
        if (!buttonGroups.containsKey(groupName)) {
            group = new ButtonGroup();
            buttonGroups.put(groupName, group);
            none = new JRadioButton();
            none.putClientProperty("none", none);
            group.add(none);
        } else {
            group = (ButtonGroup) buttonGroups.get(groupName);
            assert group.getButtonCount() > 0;
            JRadioButton first = (JRadioButton) group.getElements().nextElement();
            none = (JRadioButton) first.getClientProperty("none");
        }
        group.add(button);
        button.putClientProperty("group", group);
        button.putClientProperty("none", none);
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

    private void buildWidgetTree(List roots) {
        if (roots == null || roots.isEmpty()) {
            return;
        }
        Iterator i = roots.iterator();
        while (i.hasNext()) {
            WidgetInfo info = (WidgetInfo) i.next();
            Component widget = WidgetBuilder.buildWidget(this, info, null);
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
        for (MenuElement sub : subs) {
            JComponent c = (JComponent) sub.getComponent();
            c.putClientProperty("window", f);
            setWindowForMenuElements(f, sub);
        }
    }

    public void setWidgetNameTable(String name, Component widget) {
        widgetNameTable.put(name, widget);
    }

    public boolean containWidgetLongNameTable(String name) {
        return widgetLongNameTable.containsKey(name);
    }

    public void setWidgetLongNameTable(String longName, Component widget) {
        if (widgetLongNameTable.containsKey(longName)) {
            logger.warn("widget named \"{0}\" already exists, replaceing with new one.", longName);
        }
        widgetLongNameTable.put(longName, widget);
    }

    public void setProperties(String longName, Map<String, String> properties) {
        this.propertyTable.put(longName, properties);
    }

    public String getProperty(String longName, String key) {
        if (!propertyTable.containsKey(longName)) {
            return null;
        }
        return (String) propertyTable.get(longName).get(key);
    }

    public void setProperty(String longName, String key, String value) {
        if (!propertyTable.containsKey(longName)) {
            return;
        }
        propertyTable.get(longName).put(key, value);
    }

    public void scaleWidget(double hScale, double vScale, Insets insets) {

        if (hScale == phScale && vScale == pvScale) {
            return;
        }

        phScale = hScale;
        pvScale = vScale;

        for (Map.Entry<String, Component> e : widgetLongNameTable.entrySet()) {
            String key = e.getKey();
            Component component = e.getValue();

            String px = getProperty(key, "x");
            String py = getProperty(key, "y");
            String pwidth = getProperty(key, "width");
            String pheight = getProperty(key, "height");
            if (px != null && py != null) {
                int x, y;
                x = (int) (Integer.parseInt(px) * hScale);
                y = (int) (Integer.parseInt(py) * vScale);
                if (component instanceof JTextArea || component instanceof PandaCList) {
                    Component parent = component.getParent();
                    if (parent != null) {
                        Component grandParent = parent.getParent();
                        if (grandParent != null && grandParent instanceof JScrollPane) {
                            grandParent.setLocation(x, y);
                        }
                    }
                } else {
                    component.setLocation(x, y);
                }
            }
            if (pwidth != null && pheight != null) {
                int width, height;
                width = (int) (Integer.parseInt(pwidth) * hScale);
                height = (int) (Integer.parseInt(pheight) * vScale);
                if (component instanceof Window) {
                    width += insets.right + insets.left;
                    height += insets.top + insets.bottom;
                }
                if (component instanceof JTextArea || component instanceof PandaCList) {
                    Component parent = component.getParent();
                    if (parent != null) {
                        Component grandParent = parent.getParent();
                        if (grandParent != null && grandParent instanceof JScrollPane) {
                            grandParent.setSize(width, height);
                            grandParent.validate();
                        }
                    }
                } else {
                    component.setSize(width, height);
                }
            }
            String column_widths = getProperty(key, "column_widths");
            if (column_widths != null && component instanceof PandaCList) {
                StringTokenizer tokens = new StringTokenizer(column_widths, String.valueOf(','));
                TableColumnModel model = ((JTable) component).getColumnModel();
                for (int i = 0; tokens.hasMoreTokens() && i < model.getColumnCount(); i++) {
                    TableColumn column = model.getColumn(i);
                    int width = ParameterConverter.toInteger(tokens.nextToken());
                    width += 8;// FIXME do not use immediate value like this
                    width = (int) (width * hScale);
                    column.setPreferredWidth(width);
                    column.setWidth(width);
                }
            }
            component.validate();
        }
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
            logger.warn("menubar is already set, replacing with new one.");
        }
        this.menuBar = menuBar;
    }
}
