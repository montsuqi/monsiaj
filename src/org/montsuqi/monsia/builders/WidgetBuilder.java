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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import java.util.Iterator;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JViewport;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.InterfaceBuildingException;
import org.montsuqi.monsia.Messages;
import org.montsuqi.monsia.SignalData;
import org.montsuqi.monsia.SignalInfo;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.Logger;
import org.montsuqi.widgets.Calendar;
import org.montsuqi.widgets.Fixed;
import org.montsuqi.widgets.Frame;
import org.montsuqi.widgets.HBox;
import org.montsuqi.widgets.HSeparator;
import org.montsuqi.widgets.NumberEntry;
import org.montsuqi.widgets.PandaCombo;
import org.montsuqi.widgets.PandaEntry;
import org.montsuqi.widgets.PandaHTML;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.Table;
import org.montsuqi.widgets.VBox;
import org.montsuqi.widgets.VSeparator;
import org.montsuqi.widgets.Window;

public class WidgetBuilder {

	private static WidgetPropertySetter propertySetter;
	static {
		propertySetter = WidgetPropertySetter.getInstance();
	}
	
	private static Map classMap;
	private static Map builderMap;

	private static void registerWidgetClass(String genericClassName, Class clazz, WidgetBuilder builder) {
		classMap.put(genericClassName, clazz);
		builderMap.put(genericClassName, builder);
	}

	static {
		builderMap = new HashMap();
		classMap = new HashMap();

		WidgetBuilder defaultWidgetBuilder= new WidgetBuilder();
		WidgetBuilder defaultContainerBuilder = new ContainerBuilder();

		registerWidgetClass("Button",         JButton.class,       new ButtonBuilder()); //$NON-NLS-1$
		registerWidgetClass("Calendar",       Calendar.class,      defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("CheckButton",    JCheckBox.class,     defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Combo",          JComboBox.class,     new ComboBuilder()); //$NON-NLS-1$
		registerWidgetClass("CList",          JTable.class,        new CListBuilder()); //$NON-NLS-1$
		registerWidgetClass("Entry",          JTextField.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Fixed",          Fixed.class,         new FixedBuilder()); //$NON-NLS-1$
		registerWidgetClass("Frame",          Frame.class,         new FrameBuilder()); //$NON-NLS-1$
		registerWidgetClass("HBox",           HBox.class,          defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("HSeparator",     HSeparator.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Label",          JLabel.class,        defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("List",           JList.class,         new ListBuilder()); //$NON-NLS-1$
		registerWidgetClass("Notebook",       JTabbedPane.class,   new NotebookBuilder()); //$NON-NLS-1$
		registerWidgetClass("NumberEntry",    NumberEntry.class,   defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaCombo",     PandaCombo.class,    new PandaComboBuilder()); //$NON-NLS-1$
		registerWidgetClass("PandaCList",     JTable.class,        new CListBuilder()); //$NON-NLS-1$
		registerWidgetClass("PandaEntry",     PandaEntry.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaHTML",      PandaHTML.class,     defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaText",      JTextArea.class,     defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("PandaTimer",     PandaTimer.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("ProgressBar",    JProgressBar.class,  defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("RadioButton",    JRadioButton.class,  new RadioButtonBuilder()); //$NON-NLS-1$
		registerWidgetClass("ScrolledWindow", JScrollPane.class,   new ScrolledWindowBuilder()); //$NON-NLS-1$
		registerWidgetClass("Table",          Table.class,         new TableBuilder()); //$NON-NLS-1$
		registerWidgetClass("Text",           JTextArea.class,     defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("ToggleButton",   JToggleButton.class, defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("VBox",           VBox.class,          defaultContainerBuilder); //$NON-NLS-1$
		registerWidgetClass("VSeparator",     VSeparator.class,    defaultWidgetBuilder); //$NON-NLS-1$
		registerWidgetClass("Viewport",       JViewport.class,     new ViewportBuilder()); //$NON-NLS-1$
		registerWidgetClass("Window",         Window.class,        new WindowBuilder()); //$NON-NLS-1$
	}

	public static Component buildWidget(Interface xml, WidgetInfo info) {
		Component widget;
		String genericClassName = info.getClassName();
		WidgetBuilder builder = (WidgetBuilder)builderMap.get(genericClassName);
		Logger logger = Logger.getLogger(WidgetBuilder.class);
		if (builder == null) {
			logger.warn(Messages.getString("WidgetBuilder.Unknown_widget_class"), genericClassName); //$NON-NLS-1$
			String labelString = MessageFormat.format("[a {0}]", new Object[] { genericClassName }); //$NON-NLS-1$
			widget = new JLabel(labelString);
		} else {
			try {
				widget = builder.buildSelf(xml, info);
			} catch (Exception e) {
				logger.warn(e);
				widget = new JLabel('[' + e.toString() + ']');
			}
		}
		if (widget instanceof Window) {
			xml.setTopLevel(widget);
		}
		builder.setCommonParameters(xml, widget, info);
		if (widget instanceof Container) {
			builder.buildChildren(xml, (Container)widget, info);
		}
		return widget;
		
	}

	protected Logger logger;

	protected WidgetBuilder() {
		logger = Logger.getLogger(getClass());
	}
	
	Component buildSelf(Interface xml, WidgetInfo info) {
		String genericClassName = info.getClassName();
		Class clazz  = (Class)classMap.get(genericClassName);
		if (clazz == null) {
			throw new IllegalArgumentException(genericClassName);
		}
		Component widget = null;
		try {
			widget = (Component)clazz.newInstance();
			propertySetter.setProperties(xml, widget, info);
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
		/* do nothing */
	}

	private void setCommonParameters(Interface xml, Component widget, WidgetInfo info) {
		addSignals(xml, widget, info);
		widget.setName(info.getName());
		xml.setName(info.getName(), widget);
		xml.setLongName(info.getLongName(), widget);
	}	

	private void addSignals(Interface xml, Component widget, WidgetInfo info) {
		Iterator i = info.getSignals().iterator();
		while (i.hasNext()) {
			SignalInfo sInfo = (SignalInfo)i.next();
			xml.addSignal(sInfo.getHandler(), new SignalData(widget, sInfo));
		}
	}
}
