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

package org.montsuqi.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.InterfaceBuildingException;
import org.montsuqi.monsia.SignalData;
import org.montsuqi.monsia.SignalInfo;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.Button;
import org.montsuqi.widgets.CheckBox;
import org.montsuqi.widgets.Entry;
import org.montsuqi.widgets.FileEntry;
import org.montsuqi.widgets.HPaned;
import org.montsuqi.widgets.Notebook;
import org.montsuqi.widgets.OptionMenu;
import org.montsuqi.widgets.PandaCList;
import org.montsuqi.widgets.Calendar;
import org.montsuqi.widgets.Fixed;
import org.montsuqi.widgets.Frame;
import org.montsuqi.widgets.HBox;
import org.montsuqi.widgets.HSeparator;
import org.montsuqi.widgets.NumberEntry;
import org.montsuqi.widgets.PandaCombo;
import org.montsuqi.widgets.PandaEntry;
import org.montsuqi.widgets.PandaHTML;
import org.montsuqi.widgets.PandaHTMLWebKit;
import org.montsuqi.widgets.PandaPreviewPane;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.RadioButton;
import org.montsuqi.widgets.Table;
import org.montsuqi.widgets.ToggleButton;
import org.montsuqi.widgets.VBox;
import org.montsuqi.widgets.VPaned;
import org.montsuqi.widgets.VSeparator;
import org.montsuqi.widgets.Window;

public class WidgetBuilder {

	protected static final Logger logger = Logger.getLogger(WidgetBuilder.class);

	private static Map classMap;
	private static Map builderMap;

	private static void registerWidgetClass(String genericClassName, Class clazz, WidgetBuilder builder) {
		classMap.put(genericClassName, clazz);
		builderMap.put(genericClassName, builder);
	}

	// set up the widget builder map
	static {
		builderMap = new HashMap();
		classMap = new HashMap();

		WidgetBuilder defaultWidgetBuilder= new WidgetBuilder();
		WidgetBuilder defaultContainerBuilder = new ContainerBuilder();

		registerWidgetClass("Button",         Button.class,        defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Calendar",       Calendar.class,      defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("CheckButton",    CheckBox.class,      defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Combo",          JComboBox.class,     new ComboBuilder()); //$NON-NLS-1$
		registerWidgetClass("CList",          PandaCList.class,    new CListBuilder()); //$NON-NLS-1$
		registerWidgetClass("Dialog",         Window.class,        defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("Entry",          Entry.class,         defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("FileEntry",      FileEntry.class,     defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Fixed",          Fixed.class,         new FixedBuilder()); //$NON-NLS-1$
		registerWidgetClass("Frame",          Frame.class,         new FrameBuilder()); //$NON-NLS-1$
		registerWidgetClass("HBox",           HBox.class,          defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("VPaned",         HPaned.class,        defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("HSeparator",     HSeparator.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Label",          JLabel.class,        defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("List",           JList.class,         new ListBuilder()); //$NON-NLS-1$
		registerWidgetClass("Menu",           JMenu.class,         defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("MenuBar",        JMenuBar.class,      new MenuBarBuilder()); //$NON-NLS-1$
		registerWidgetClass("MenuItem",       JMenuItem.class,     new MenuItemBuilder()); //$NON-NLS-1$
		registerWidgetClass("PixmapMenuItem", JMenuItem.class,     new MenuItemBuilder()); //$NON-NLS-1$
		registerWidgetClass("Notebook",       Notebook.class,      new NotebookBuilder()); //$NON-NLS-1$
		registerWidgetClass("NumberEntry",    NumberEntry.class,   defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("OptionMenu",     OptionMenu.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaCombo",     PandaCombo.class,    new PandaComboBuilder()); //$NON-NLS-1$
		registerWidgetClass("PandaCList",     PandaCList.class,    new CListBuilder()); //$NON-NLS-1$
		registerWidgetClass("PandaEntry",     PandaEntry.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaHTML",      PandaHTML.class,     defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaPS",        PandaPreviewPane.class,  defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaText",      JTextArea.class,     defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaTimer",     PandaTimer.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Placeholder",    JPanel.class,        defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("ProgressBar",    JProgressBar.class,  defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("RadioButton",    RadioButton.class,   new RadioButtonBuilder()); //$NON-NLS-1$
		registerWidgetClass("ScrolledWindow", JScrollPane.class,   new ScrolledWindowBuilder()); //$NON-NLS-1$
		registerWidgetClass("Table",          Table.class,         new TableBuilder()); //$NON-NLS-1$
		registerWidgetClass("Text",           JTextArea.class,     defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("ToggleButton",   ToggleButton.class,  defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("VBox",           VBox.class,          defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("VPaned",         VPaned.class,        defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("VSeparator",     VSeparator.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Viewport",       JViewport.class,     new ViewportBuilder()); //$NON-NLS-1$
		registerWidgetClass("Window",         Window.class,        defaultContainerBuilder); //$NON-NLS-1$

		// on MacOS replace PandaHTML with PandaHTMLWebKit.
		if (SystemEnvironment.isMacOSX()) {
			registerWidgetClass("PandaHTML", PandaHTMLWebKit.class, defaultWidgetBuilder); //$NON-NLS-1$
		}
	}

	private interface FontModifier {
		Font modifyFont(Font font);
	}
	// set up UI resources
	static {
		String[] classes = {
			"Button", //$NON-NLS-1$
			"ToggleButton", //$NON-NLS-1$
			"RadioButton", //$NON-NLS-1$
			"ComboBox", //$NON-NLS-1$
			"CheckBox", //$NON-NLS-1$
			"RadioButton", //$NON-NLS-1$
			"TabbedPane", //$NON-NLS-1$
			"Label" //$NON-NLS-1$
		};
		FontModifier makePlainFont = new FontModifier() {
			public Font modifyFont(Font font) {
				return font.deriveFont(font.getStyle() & ~Font.BOLD);
			}
		};
		for (int i = 0; i < classes.length; i++) {
			modifyFont(classes[i], makePlainFont);
		}
		if (SystemEnvironment.isMacOSX()) {
			classes = new String[] {
				"TextField", //$NON-NLS-1$
				"ComboBox" //$NON-NLS-1$
			};
			FontModifier makeOsakaFont = new FontModifier() {
				public Font modifyFont(Font font) {
					return new Font("Osaka", Font.PLAIN, font.getSize()); //$NON-NLS-1$
				}
			};
			for (int i = 0; i < classes.length; i++) {
				modifyFont(classes[i], makeOsakaFont);
			}
		}
	}

	private static void modifyFont(String className, FontModifier creator) {
		String key = className + ".font"; //$NON-NLS-1$
		Font font = (Font)UIManager.get(key);
		if (font != null) {
			font = creator.modifyFont(font);
			font = ScreenScale.scale(font);
			UIManager.put(key, new FontUIResource(font));
		}
	}

	public static Component buildWidget(Interface xml, WidgetInfo info, Container parent) {
		String genericClassName = info.getClassName();
		WidgetBuilder builder = (WidgetBuilder)builderMap.get(genericClassName);
		if (builder == null) {
			logger.warn("unknown widget class: {0}", genericClassName); //$NON-NLS-1$
			Object[] args = { genericClassName };
			return new JLabel(MessageFormat.format("[a {0}]", args)); //$NON-NLS-1$
		}
		try {
			Component widget = builder.buildSelf(xml, parent, info);
			if (widget instanceof Window) {
				xml.setTopLevel(widget);
			}
			if (widget instanceof Container) {
				builder.buildChildren(xml, (Container)widget, info);
			}
			builder.setCommonParameters(xml, widget, info);
			builder.setSignals(xml, widget, info);
			return widget;
		} catch (Exception e) {
			logger.warn(e);
			return new JLabel('[' + e.toString() + ']');
		}
	}

	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		String genericClassName = info.getClassName();
		Class clazz  = (Class)classMap.get(genericClassName);
		if (clazz == null) {
			throw new IllegalArgumentException(genericClassName);
		}
		try {
			Component widget = (Component)clazz.newInstance();
			setProperties(xml, parent, widget, info.getProperties());
			xml.addAccels(widget, info);
			return widget;
		} catch (InstantiationException e) {
			logger.fatal(e);
			throw new InterfaceBuildingException(e);
		} catch (IllegalAccessException e) {
			logger.fatal(e);
			throw new InterfaceBuildingException(e);
		}
	}

	void buildChildren(Interface xml, Container parent, WidgetInfo info) {
		// do nothing
	}

	protected void setCommonParameters(Interface xml, Component widget, WidgetInfo info) {
		widget.setName(info.getName());
		xml.setName(info.getName(), widget);
		xml.setLongName(info.getLongName(), widget);
	}

	protected void setSignals(Interface xml, Component widget, WidgetInfo info) {
		Iterator i = info.getSignals().iterator();
		while (i.hasNext()) {
			xml.addSignal(new SignalData(widget, ((SignalInfo)i.next())));
		}
	}

	void setProperties(Interface xml, Container parent, Component widget, Map properties) {
		Class clazz = widget.getClass();
		Iterator i = properties.entrySet().iterator();
		String positionValue = null;
		while (i.hasNext()) {
			Map.Entry ent = (Map.Entry)i.next();
			String name = (String)ent.getKey();
			String value = (String)ent.getValue();
			if ("position".equals(name)) { //$NON-NLS-1$
				positionValue = value;
				continue; // set position after size of this window is determined.
			}
			WidgetPropertySetter setter = WidgetPropertySetter.getSetter(clazz, name);
			setter.set(xml, parent, widget, value);
		}
		if (positionValue != null) {
			WidgetPropertySetter setter = WidgetPropertySetter.getSetter(clazz, "position"); //$NON-NLS-1$
			setter.set(xml, parent, widget, positionValue);
		}
	}
}
