package org.montsuqi.monsia;

import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
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
import org.montsuqi.util.Logger;
import org.xml.sax.SAXException;

public class Interface {
	String fileName;
    List topLevels;
    Map infos;
    Map widgets;
    Map longNames;
	Map buttonGroups;
	Protocol protocol;

    JWindow topLevel;
    LinkedList accelGroup;
	Map signals;
    Container focusWidget;
	Container defaultWidget;
	Logger logger;
	
	public void setDefaultWidget(Container widget) {
		defaultWidget = widget;
	}

	public void setFocusWidget(Container widget) {
		focusWidget = widget;
	}

	public static Interface parseFile(String fileName, Protocol protocol) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			SAXParser parser = factory.newSAXParser();
			File file = new File(fileName);
			MonsiaHandler handler = new MonsiaHandler(fileName);
			parser.parse(file, handler);
			return handler.getInterface(protocol);
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

	Interface(String fileName, Map infos, List roots, Protocol protocol)
	{
		logger = Logger.getLogger(Interface.class);
		this.fileName = fileName;
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
		buildInterface(roots);
		signalAutoConnect();
	}

	private void signalAutoConnect() {
		Class[] argTypes = { Container.class, Object.class };
		Iterator entries = signals.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry)entries.next();
			String handlerName = (String)entry.getKey();
			Iterator i = ((List)entry.getValue()).iterator();
			try {
				Method handler = Protocol.class.getMethod(handlerName, argTypes);
				while (i.hasNext()) {
					SignalData data = (SignalData)i.next();
					Container target = (Container)data.getSignalObject();
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
	
	private void connect(Container target, String signalName, Method handler, Object other)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Class[] argTypes = new Class[] { Container.class, Method.class, Object.class };
		Method method = Interface.class.getDeclaredMethod(connectMethodName(signalName), argTypes);
		method.setAccessible(true);
		method.invoke(this, new Object[] { target, handler, other });
	}

	private void connectAfter(Container target, String signalName, Method handler, Object other)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		connect(target, signalName, handler, other);
	}

	private void invoke(Method handler, Container target, Object other) {
		try {
			handler.invoke(protocol, new Object[] { target, other });
		} catch (IllegalAccessException e) {
			logger.fatal(e);
			throw new HandlerInvocationException(e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			logger.fatal(cause);
			throw new HandlerInvocationException(e);
		}
	}
		 
	private void connectClicked(final Container target, final Method handler, final Object other) {
		if (target instanceof JButton || target instanceof JRadioButton) {
			((AbstractButton)target).addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					invoke(handler, target, other);
				}
			});
		}
	}

	private void connectChanged(final Container target, final Method handler, final Object other) {
		if (target instanceof JTextComponent) {
			((JTextComponent)target).getDocument().addDocumentListener(new DocumentListener() {
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
	}

	private void connectActivate(final Container target, final Method handler, final Object other) {
		if (target instanceof JTextField) {
			((JTextField)target).addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					invoke(handler, target, other);
				}
			});
		}
	}

	private void connectMapEvent(final Container target, final Method handler, final Object other) {
		if (target instanceof Window) {
			((Window)target).addWindowListener(new WindowAdapter() {
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

	private void connectDeleteEvent(final Container target, final Method handler, final Object other) {
		if (target instanceof Window) {
			((Window)target).addWindowListener(new WindowAdapter() {
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

	private void connectSetFocus(final Container target, final Method handler, final Object other) {
		target.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				invoke(handler, target, other);
			}
		});
	}

	private void connectSelectRow(final Container target, final Method handler, final Object other) {
		
		if (target instanceof JTree) {
			TreeSelectionModel model = ((JTree)target).getSelectionModel();
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
				ListSelectionModel model = ((JList)target).getSelectionModel();
				model.addListSelectionListener(listener);
			} else if (target instanceof JTable) {
				ListSelectionModel model = ((JTable)target).getSelectionModel();
				model.addListSelectionListener(listener);
			}
		}
	}

	private void connectUnselectRow(Container target, Method handler, Object other) {
		// XxxSelectionModels don't care selection/unselection so use connectSelectRow
		connectSelectRow(target, handler, other);
	}

	public Container getWidget(String name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		return (Container)widgets.get(name);
	}

	public Container getWidgetByLongName(String longName) {
		if (longName == null) {
			throw new IllegalArgumentException();
		}
		return (Container)longNames.get(longName);
	}

	public String relativeFile(String fileName) {
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

	public String getWidgetName(Container widget) {
		if (widget == null) {
			throw new IllegalArgumentException();
		}
		Iterator entries = widgets.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry e = (Map.Entry)entries.next();
			Container value = (Container)e.getValue();
			if (value == widget) {
				return (String)e.getKey();
			}
		}
		return null;
	}

	public String getWidgetLongName(Container widget) {
		if (widget == null) {
			throw new IllegalArgumentException();
		}
		Iterator entries = longNames.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry e = (Map.Entry)entries.next();
			Container value = (Container)e.getValue();
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
	
	/**
	 * This is used while the tree is being built to set the toplevel window that
	 * is currently being built.  It is mainly used to enable GtkAccelGroup's to
	 * be bound to the correct window, but could have other uses.
	 */
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

	public void handleWidgetProp(Container widget, String nameName, String valueName) {
//		value_widget = g_hash_table_lookup(self->priv->name_hash, value_name);
//		if (value_widget) {
//			g_object_set(G_OBJECT(widget), prop_name, value_widget, NULL);
//		} else {
//			DeferredProperty dprop = new DeferredProperty(valueName, DEFERRED_PROP);
//			dprop->d.prop.object = G_OBJECT(widget);
//			dprop->d.prop.prop_name = prop_name;
//			
//			self->priv->deferred_props = g_list_prepend(self->priv->deferred_props,
//														dprop);
//		}
	}
	
	public List ensureAccel() {
		if (accelGroup == null) {
			accelGroup = new LinkedList();
			if (topLevel != null) {
				//topLevel.addAccelGroup(accelGroup);
			}
		}
		return accelGroup;
	}

	public String getText(String msgid) {
		if (msgid == null || msgid.length() == 0) {
			return ""; //$NON-NLS-1$
		} else {
			return msgid;
		}
	}

	public void addSignal(String handler, SignalData sData) {
		if ( ! signals.containsKey(handler)) {
			signals.put(handler, new ArrayList());
		}
		List signals = (List)this.signals.get(handler);
		signals.add(0, sData);
	}

	protected void buildInterface(List roots) {
		if (roots == null || roots.isEmpty()) {
			return;
		}
		WidgetBuilder builder = new WidgetBuilder(this);
		Iterator i = roots.iterator();
		while (i.hasNext()) {
			WidgetInfo info = (WidgetInfo)i.next();
			Container widget = builder.buildWidget(info);
			widgets.put(info.getName(), widget);
		}
	}

	public String getLongName(Container widget) {
		Iterator i = longNames.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			if (widget == e.getValue()) {
				return (String)e.getKey();
			}
		}
		return null;
	}

	public void setLongName(String longName, Container widget) {
		logger.debug("longName: {0}", longName);
		longNames.put(longName, widget);
	}
}
