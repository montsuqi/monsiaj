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

import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.FocusManager;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.montsuqi.client.Protocol;
import org.montsuqi.client.SignalHandler;
import org.montsuqi.monsia.builders.WidgetBuilder;
import org.montsuqi.util.Logger;
import org.montsuqi.widgets.PandaFocusManager;

public class Interface {
	private Map widgets;
    private Map longNames;
	private Map buttonGroups;
	private Protocol protocol;

	private Component topLevel;
	private List signals;
    private Component focusWidget;
	private Component defaultWidget;

	private static Map accelHandlers;
	static {
		FocusManager.setCurrentManager(new PandaFocusManager());
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
			Logger.getLogger(Interface.class).fatal(e);
			throw new ExceptionInInitializerError(e);
		}
	}

	private static final String oldPrologue = "<?xml version=\"1.0\"?>\n<GTK-Interface>\n"; //$NON-NLS-1$
	private static final int OLD_PROLOGUE_LENGTH;
	static {
		OLD_PROLOGUE_LENGTH = oldPrologue.getBytes().length;
	}

	public static Interface parseInput(InputStream input, Protocol protocol) {
		try {
			if ( ! (input instanceof BufferedInputStream)) {
				input = new BufferedInputStream(input);
			}

			String handlerClassName = System.getProperty("monsia.document.handler"); //$NON-NLS-1$
			if (handlerClassName == null) {
				handlerClassName = isNewScreenDefinition(input) ? NEW_HANDLER : OLD_HANDLER;
			}

			Class handlerClass = Class.forName(handlerClassName);
			AbstractDocumentHandler handler = (AbstractDocumentHandler)handlerClass.newInstance();

			if (handlerClassName.equals(OLD_HANDLER)) {
				input = new FakeEncodingInputStream(input);
			}
			saxParser.parse(input, handler);
			return handler.getInterface(protocol);
		} catch (Exception e) {
			Logger.getLogger(Interface.class).fatal(e);
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

	private void signalAutoConnect() {
		Iterator entries = signals.iterator();
		while (entries.hasNext()) {
			SignalData data = (SignalData)entries.next();
			String handlerName = data.getHandler().toLowerCase();
			try {
				SignalHandler handler = SignalHandler.getSignalHandler(handlerName);
				if (data.isAfter()) {
					connectAfter(handler, data);
				} else {
					connect(handler, data);
				}
			} catch (NoSuchMethodException e) {
				throw new InterfaceBuildingException(e);
			}
		}
	}

	private void connect(SignalHandler handler, SignalData data) {
		Component target = data.getTarget();
		if (target instanceof JComboBox) {
			JComboBox combo = (JComboBox)target;
			Object prop = combo.getClientProperty("editor"); //$NON-NLS-1$
			if (prop != null) {
				target = (JTextField)prop;
			} else {
				String message = Messages.getString("Interface.no_such_combo"); //$NON-NLS-1$
				message = MessageFormat.format(message, new Object[] { combo.getName() });
				throw new IllegalArgumentException(message);
			}
		}
		try {
			Connector connector = Connector.getConnector(data.getName());
			connector.connect(protocol, target, handler, data.getObject());
		} catch (NoSuchMethodException e) {
			throw new InterfaceBuildingException(e);
		}
	}

	private void connectAfter(SignalHandler handler, SignalData data) {
		connect(handler, data);
	}

	public Component getWidget(String name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		return (Component)widgets.get(name);
	}

	public Component getWidgetByLongName(String longName) {
		if (longName == null) {
			throw new IllegalArgumentException();
		}
		return (Component)longNames.get(longName);
	}

	String relativeFile(String fileName) {
		if (fileName == null) {
			throw new IllegalArgumentException();
		}

		File targetFile = new File(fileName);
		if (targetFile.isAbsolute()) {
			return fileName;
		}
		File xmlFile = new File(fileName);
		File relativeFile = new File(xmlFile.getParent(), fileName);
		return relativeFile.toString();
	}

	public String getWidgetName(Component widget) {
		if (widget == null) {
			throw new IllegalArgumentException();
		}
		Iterator entries = widgets.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry e = (Map.Entry)entries.next();
			Component value = (Component)e.getValue();
			if (value == widget) {
				return (String)e.getKey();
			}
		}
		return null;
	}

	public String getWidgetLongName(Component widget) {
		if (widget == null) {
			throw new IllegalArgumentException();
		}
		Iterator entries = longNames.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry e = (Map.Entry)entries.next();
			Component value = (Component)e.getValue();
			if (value == widget) {
				return (String)e.getKey();
			}
		}
		return null;
	}

	public ButtonGroup getButtonGroup(String name) {
		if ( ! buttonGroups.containsKey(name)) {
			ButtonGroup group = new ButtonGroup();
			JRadioButton dummy = new JRadioButton();
			group.add(dummy);
			buttonGroups.put(name, new ButtonGroup());
		}
		return (ButtonGroup)buttonGroups.get(name);
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
			WidgetInfo info = (WidgetInfo)i.next();
			Component widget = WidgetBuilder.buildWidget(this, info, null);
			setName(info.getName(), widget);
		}
	}

	public String getLongName(Component widget) {
		Iterator i = longNames.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			if (widget == e.getValue()) {
				return (String)e.getKey();
			}
		}
		return null;
	}

	public void setName(String name, Component widget) {
		widgets.put(name, widget);
	}

	public void setLongName(String longName, Component widget) {
		longNames.put(longName, widget);
	}

	public static boolean handleAccels(KeyEvent e) {
		Component c = (Component)e.getSource();
		while (c.getParent() != null) {
			c = c.getParent();
		}
		AccelHandler handler = getAccelHandler(c);
		return handler.handleAccel(e);
	}

	private static AccelHandler getAccelHandler(Component c) {
		if ( ! accelHandlers.containsKey(c)) {
			accelHandlers.put(c, new AccelHandler());
		}
		AccelHandler handler = (AccelHandler)accelHandlers.get(c);
		return handler;
	}
}
