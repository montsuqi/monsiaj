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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.montsuqi.client.Protocol;
import org.montsuqi.monsia.builders.WidgetBuilder;
import org.montsuqi.util.Logger;
import org.montsuqi.widgets.Calendar;
import org.montsuqi.widgets.CalendarEvent;
import org.montsuqi.widgets.CalendarListener;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.TimerEvent;
import org.montsuqi.widgets.TimerListener;
import org.xml.sax.SAXException;

public class Interface {
	private List topLevels;
    private Map infos;
    private Map widgets;
    private Map longNames;
	private Map buttonGroups;
	private Protocol protocol;

	private JWindow topLevel;
    private LinkedList accelGroup;
	private Map signals;
    private Component focusWidget;
	private Component defaultWidget;
	private Logger logger;

	private static final String OLD_HANDLER = "org.montsuqi.monsia.Glade1Handler"; //$NON-NLS-1$
	private static final String NEW_HANDLER = "org.montsuqi.monsia.MonsiaHandler"; //$NON-NLS-1$

	public void setDefaultWidget(Component widget) {
		defaultWidget = widget;
	}

	public void setFocusWidget(Component widget) {
		focusWidget = widget;
	}

	public static Interface parseInput(InputStream input, Protocol protocol) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			String handlerClassName = null;

			handlerClassName = System.getProperty("monsia.document.handler"); //$NON-NLS-1$

			if (handlerClassName == null) {
				input = new java.io.BufferedInputStream(input);
				String oldPrologue = "<?xml version=\"1.0\"?>\n<GTK-Interface>\n"; //$NON-NLS-1$
				int oldPrologueLength = oldPrologue.getBytes().length;
				input.mark(oldPrologueLength);
				byte[] bytes = new byte[oldPrologueLength];
				input.read(bytes);
				String head = new String(bytes);
				input.reset();
				if (head.indexOf("GTK-Interface") < 0) { //$NON-NLS-1$
					handlerClassName = NEW_HANDLER;
				}
			}
			if (handlerClassName == null) {
				handlerClassName = OLD_HANDLER;
			}

			Class handlerClass = Class.forName(handlerClassName);
			AbstractDocumentHandler handler;
			handler = (AbstractDocumentHandler)handlerClass.newInstance();

			if (handlerClassName.equals(OLD_HANDLER)) {
				input = new FakeEncodingInputStream(input);
			}

			SAXParser parser = factory.newSAXParser();
			parser.parse(input, handler);
			return handler.getInterface(protocol);
		} catch (ClassNotFoundException e) {
			Logger.getLogger(Interface.class).fatal(e);
			throw new InterfaceBuildingException(e);
		} catch (IllegalAccessException e) {
			Logger.getLogger(Interface.class).fatal(e);
			throw new InterfaceBuildingException(e);
		} catch (InstantiationException e) {
			Logger.getLogger(Interface.class).fatal(e);
			throw new InterfaceBuildingException(e);
		} catch (IOException e) {
			Logger.getLogger(Interface.class).fatal(e);
			throw new InterfaceBuildingException(e);
		} catch (SAXException e) {
			Logger.getLogger(Interface.class).fatal(e);
			throw new InterfaceBuildingException(e);
		} catch (ParserConfigurationException e) {
			Logger.getLogger(Interface.class).fatal(e);
			throw new InterfaceBuildingException(e);
		}
	}

	Interface(Map infos, List roots, Protocol protocol)
	{
		logger = Logger.getLogger(Interface.class);
		this.infos = infos;
		widgets = new HashMap();
		longNames = new HashMap();
		signals = new HashMap();
		buttonGroups = new HashMap();
		topLevel = null;
		accelGroup = null;
		defaultWidget = null;
		focusWidget = null;
		this.protocol = protocol;
		buildWidgetTree(roots);
		signalAutoConnect();
	}

	// FIXME signal with accels
	private void signalAutoConnect() {
		Class[] argTypes = { Component.class, Object.class };
		Iterator entries = signals.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry)entries.next();
			String handlerName = (String)entry.getKey();
			handlerName = handlerName.toLowerCase();
			Iterator i = ((List)entry.getValue()).iterator();
			try {
				Method handler = Protocol.class.getMethod(handlerName, argTypes);
				while (i.hasNext()) {
					SignalData data = (SignalData)i.next();
					Component target = (Component)data.getSignalObject();
					String signalName = data.getName();
					Object other = data.getConnectObject();
					if (data.isAfter()) {
						connectAfter(target, signalName, handler, other);
					} else {
						connect(target, signalName, handler, other);
					}
				}
			} catch (Exception e) {
				logger.fatal(e);
				throw new InterfaceBuildingException(e);
			}
		}
	}

	private String connectMethodName(String signalName) {
		StringBuffer methodName = new StringBuffer("connect"); //$NON-NLS-1$
		char[] chars = signalName.toCharArray();
		boolean needUpperCase = true;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '_') {
				needUpperCase = true;
			} else {
				if (needUpperCase) {
					methodName.append(Character.toUpperCase(chars[i]));
					needUpperCase = false;
				} else {
					methodName.append(Character.toLowerCase(chars[i]));
				}
			}
		}
		return methodName.toString();
	}
	
	private void connect(Component target, String signalName, Method handler, Object other)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class[] argTypes = new Class[] { Component.class, Method.class, Object.class };
		Method method = Interface.class.getDeclaredMethod(connectMethodName(signalName), argTypes);
		method.setAccessible(true);
		method.invoke(this, new Object[] { target, handler, other });
	}

	private void connectAfter(Component target, String signalName, Method handler, Object other)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		connect(target, signalName, handler, other);
	}

	private void invoke(Method handler, Component target, Object other) {
		try {
			handler.invoke(protocol, new Object[] { target, other });
		} catch (IllegalAccessException e) {
			logger.fatal(e);
			throw new HandlerInvocationException(e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getTargetException(); // should use getCause() [J2SE 1.4+]
			logger.fatal(cause);
			throw new HandlerInvocationException(e);
		}
	}
		 
	private void connectClicked(final Component target, final Method handler, final Object other) {
		if ( ! (target instanceof JButton) && ! (target instanceof JRadioButton)) {
			return;
		}
		AbstractButton button = (AbstractButton)target;
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectButtonPressEvent(final Component target, final Method handler, final Object other) {
		connectClicked(target, handler, other);
	}

	private void connectKeyPressEvent(final Component target, final Method handler, final Object other) {
		target.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectChanged(final Component target, final Method handler, final Object other) {
		if ( ! (target instanceof JTextComponent)) {
			return;
		}
		JTextComponent text = (JTextComponent)target;
		text.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent event) {
				invoke(handler, target, other);
			}
			public void removeUpdate(DocumentEvent event) {
				invoke(handler, target, other);
			}
			public void changedUpdate(DocumentEvent event) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectActivate(final Component target, final Method handler, final Object other) {
		if ( ! (target instanceof JTextField)) {
			return;
		}
		JTextField textField = (JTextField)target;
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				invoke(handler, target, other);
			}
		});
	}
	
	private void connectEnter(final Component target, final Method handler, final Object other) {
		connectActivate(target, handler, other);
	}

	private void connectFocusInEvent(final Component target, final Method handler, final Object other) {
		target.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				invoke(handler, target, other);
			}
			public void focusLost(FocusEvent e) {}
		});
	}

	private void connectFocusOutEvent(final Component target, final Method handler, final Object other) {
		target.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {}
			public void focusLost(FocusEvent e) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectMapEvent(final Component target, final Method handler, final Object other) {
		if (target instanceof Window) {
			Window window = (Window)target;
			window.addWindowListener(new WindowAdapter() {
				public void windowOpened(WindowEvent e) {
					invoke(handler, target, other);
				}
			});
		} else {
			target.addComponentListener(new ComponentAdapter() {
				public void componentShown(ComponentEvent e) {
					invoke(handler, target, other);
				}
			});
		}
	}

	private void connectDeleteEvent(final Component target, final Method handler, final Object other) {
		if (target instanceof Window) {
			Window window = (Window)target;
			window.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					invoke(handler, target, other);
				}
			});
		} else {
			target.addComponentListener(new ComponentAdapter() {
				public void componentHidden(ComponentEvent e) {
					invoke(handler, target, other);
				}
			});
		}
	}

	private void connectDestroy(final Component target, final Method handler, final Object other) {
		if (target instanceof Window) {
			Window window = (Window)target;
			window.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					invoke(handler, target, other);
				}
			});
		} else {
			target.addComponentListener(new ComponentAdapter() {
				public void componentHidden(ComponentEvent e) {
					invoke(handler, target, other);
				}
			});
		}
	}

	private void connectSetFocus(final Component target, final Method handler, final Object other) {
		target.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectSelectRow(final Component target, final Method handler, final Object other) {
		if (target instanceof JTree) {
			JTree tree = (JTree)target;
			TreeSelectionModel model = tree.getSelectionModel();
			model.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					invoke(handler, target, other);
				}
			});
		} else {
			ListSelectionListener listener = new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					invoke(handler, target, other);
				}
			};
			if (target instanceof JList) {
				JList list = (JList)target;
				ListSelectionModel model = list.getSelectionModel();
				model.addListSelectionListener(listener);
			} else if (target instanceof JTable) {
				JTable table = (JTable)target;
				ListSelectionModel model = table.getSelectionModel();
				model.addListSelectionListener(listener);
			}
		}
	}

	private void connectUnselectRow(Component target, Method handler, Object other) {
		// XxxSelectionModels don't care selection/unselection so use connectSelectRow
		connectSelectRow(target, handler, other);
	}

	private void connectSelectionChanged(Component target, Method handler, Object other) {
		connectSelectRow(target, handler, other);
	}

	private void connectClickColumn(Component target, Method handler, Object other) {
		// NOTE need to implement this?
	}

	private void connectSwitchPage(final Component target, final Method handler, final Object other) {
		if ( ! (target instanceof JTabbedPane)) {
			return;
		}
		JTabbedPane tabbedPane = (JTabbedPane)target;
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectToggled(final Component target, final Method handler, final Object other) {
		if ( ! (target instanceof JToggleButton)) {
			return;
		}
		JToggleButton toggleButton = (JToggleButton)target;
		toggleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectTimeout(final Component target, final Method handler, final Object other) {
		if ( ! (target instanceof PandaTimer)) {
			return;
		}
		PandaTimer timer = (PandaTimer)target;
		timer.addTimerListener(new TimerListener() {
			public void timerSignaled(TimerEvent e) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectDaySelected(final Component target, final Method handler, final Object other) {
		if ( ! (target instanceof Calendar)) {
			return;
		}
		Calendar cal = (Calendar)target;
		cal.addCalendarListener(new CalendarListener() {

			public void previousMonth(CalendarEvent e) {}

			public void nextMonth(CalendarEvent e) {}

			public void daySelected(CalendarEvent e) {
				invoke(handler, target, other);
			}
		});
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
			buttonGroups.put(name, new ButtonGroup());
		}
		return (ButtonGroup)buttonGroups.get(name);
	}
	
	public void setTopLevel(JWindow window) {
		if (focusWidget != null) {
			focusWidget.requestFocus();
		}
		
		if (defaultWidget != null) {
			defaultWidget.requestFocus();
		}
		focusWidget = null;
		defaultWidget = null;
		topLevel = window;

		/* new toplevel needs new accel group */
		if (accelGroup != null) {
			accelGroup.removeLast();
		}
		accelGroup = null;
	}

	private List ensureAccel() {
		if (accelGroup == null) {
			accelGroup = new LinkedList();
			if (topLevel != null) {
				//topLevel.addAccelGroup(accelGroup);
			}
		}
		return accelGroup;
	}

	public void addSignal(String handler, SignalData sData) {
		if ( ! signals.containsKey(handler)) {
			signals.put(handler, new ArrayList());
		}
		List signals = (List)this.signals.get(handler);
		signals.add(0, sData);
	}

	void buildWidgetTree(List roots) {
		if (roots == null || roots.isEmpty()) {
			return;
		}
		Iterator i = roots.iterator();
		while (i.hasNext()) {
			WidgetInfo info = (WidgetInfo)i.next();
			Component widget = WidgetBuilder.buildWidget(this, info);
			widgets.put(info.getName(), widget);
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

	public void setLongName(String longName, Component widget) {
		longNames.put(longName, widget);
	}
}
