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
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeSelectionModel;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.InterfaceBuildingException;
import org.montsuqi.monsia.Property;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.util.Logger;
import org.montsuqi.util.ParameterConverter;
import org.montsuqi.widgets.NumberEntry;
import org.montsuqi.widgets.PandaEntry;
import org.montsuqi.widgets.PandaHTML;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.Table;
import org.montsuqi.widgets.TableConstraints;
import org.montsuqi.widgets.TableLayout;


class WidgetPropertySetter {

	private static WidgetPropertySetter theInstance;
	private Logger logger;
	private Map propertyMap;

	void setProperties(Interface xml, Component widget, WidgetInfo info) {
		for (int i = 0, pCount = info.getPropertiesCount(); i < pCount; i++) {
			try {
				Property p = info.getProperty(i);
				String pName = p.getName();
				String pValue = p.getValue();
				boolean set = false;
				for (Class clazz = widget.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
					Map map = (Map)propertyMap.get(clazz);
					if (map == null || ! map.containsKey(pName)) {
						continue;
					}
					Method setter = (Method)map.get(pName);
					if (setter.getDeclaringClass() == WidgetPropertySetter.class) {
						setter.invoke(this, new Object[] { xml, widget, pValue });
					} else {
						setter.invoke(widget, new Object[] { pValue });
					}
					set = true;
				}
				if ( ! set) {
//					logger.info(Messages.getString("WidgetPropertySetter.ignored_in_Java"), pName); //$NON-NLS-1$
				}
			} catch (IllegalArgumentException e) {
				throw new InterfaceBuildingException(e);
			} catch (IllegalAccessException e) {
				throw new InterfaceBuildingException(e);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getTargetException(); // should use getCause() [J2SE 1.4+]
				throw new InterfaceBuildingException(cause);
			}
		}
		
	}

	private WidgetPropertySetter() {
		logger = Logger.getLogger(WidgetPropertySetter.class);
		propertyMap = new HashMap();
		registerProperty(java.awt.Frame.class, "title", "setWindowTitle"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "visible", "setVisible"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(AbstractButton.class, "label", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JLabel.class, "label", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JLabel.class, "justify", "setJustify"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTextComponent.class, "editable", "setEditable"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTextComponent.class, "text", "setTextText"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTextArea.class, "text", "setTextViewText"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTextField.class, "justify", "horizontalAlignment"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTextField.class, "invisible_char", "setEntryInvisibleChar"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "width_request", "setWidth"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "height_request", "setHeight"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "width", "setWidth"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "height", "setHeight"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "visible", "setVisible"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "tooltip", "setTooltip"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "has_default", "setHasDefault"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Component.class, "has_focus", "setHasFocus"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTable.class, "column_widths", "setCListColumnWidth"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTable.class, "selection_mode", "setCListSelectionMode"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTable.class, "shadow_type", "setCListShadowType"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JTable.class, "show_titles", "setCListShowTitles"); //$NON-NLS-1$ //$NON-NLS-2$
		//registerProperty(JTree.class, "selection_mode", "setTreeSelectionMode"); //$NON-NLS-1$ //$NON-NLS-2$
		//registerProperty(JTree.class, "view_mode", "setTreeViewMode"); //$NON-NLS-1$ //$NON-NLS-2$
		//registerProperty(JTree.class, "view_line", "setTreeViewLine"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JList.class, "selection_mode", "setListSelectionMode"); //$NON-NLS-1$ //$NON-NLS-2$
		//registerProperty(JCheckBoxMenuItem.class, "always_show_toggle", "setCheckMenuItemAlwaysShowToggle"); //$NON-NLS-1$ //$NON-NLS-2$
		//registerProperty(JRadioButtonMenuItem.class, "group", "setRadioMenuItemGroup"); //$NON-NLS-1$ //$NON-NLS-2$
		//registerProperty(JToolBar.class, "tooltips", "setToolbarTooltips"); //$NON-NLS-1$ //$NON-NLS-2$
		//registerProperty(JMenuItem.class, "label", "setMenuItemLabel"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(NumberEntry.class, "format", "setNumberEntryFormat"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(PandaEntry.class, "input_mode", "setPandaEntryInputMode"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(PandaEntry.class, "xim_enabled", "setPandaEntryXIMEnabled"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(PandaHTML.class, "uri", "setPandaHTMLURI"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(PandaTimer.class, "duration", "setPandaTimerDuration"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JProgressBar.class, "lower", "setProgressBarLower"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JProgressBar.class, "upper", "setProgressBarUpper"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JProgressBar.class, "value", "setProgressBarValue"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JProgressBar.class, "orientation", "setProgressBarOrientation"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(JProgressBar.class, "show_text", "setProgressBarShowText"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(Frame.class, "label", "setFrameLabel"); //$NON-NLS-1$ //$NON-NLS-2$
		//registerProperty(Table.class, "rows", "setTableRows");
		//registerProperty(Table.class, "columns", "setTableColumns");
		//registerProperty(Table.class, "row_spacing", "setTableRowSpacing");
		//registerProperty(Table.class, "column_spacing", "setTableColumnSpacing");
	}

	private void registerProperty(Class clazz, String propertyName, String setterName) {
		if ( ! propertyMap.containsKey(clazz)) {
			propertyMap.put(clazz, new HashMap());
		}
		Map map = (Map)propertyMap.get(clazz);
		Method setter = null;
		if (setterName != null) {
			if (setterName.startsWith("set")) { //$NON-NLS-1$
				setter = findSetter(setterName);
			} else { // assume that the setter name is field name.
				try {
					setter = findSetterByFieldName(clazz, setterName);
				} catch (IllegalAccessException e) {
					logger.fatal(e);
				} catch (NoSuchFieldException e) {
					logger.fatal(e);
				}
			}
			map.put(propertyName, setter);
		}
	}

	private Method findSetterByFieldName(Class clazz, String field)
		throws IllegalAccessException, NoSuchFieldException {
		BeanInfo bInfo = null;
		try {
			bInfo = Introspector.getBeanInfo(clazz, Introspector.USE_ALL_BEANINFO);
		} catch (IntrospectionException e) {
			logger.fatal(e);
			throw new WidgetBuildingException(e);
		}
		PropertyDescriptor[] pDescs = bInfo.getPropertyDescriptors();
		PropertyDescriptor pDesc = null;
		for (int i = 0; i < pDescs.length; i++) {
			if (pDescs[i].getName().equals(field)) {
				pDesc = pDescs[i];
				break;
			}
		}
		if (pDesc == null) {
			Object[] args = new Object[] { field, clazz };
			String format = Messages.getString("WidgetPropertySetter.Unknown_property"); //$NON-NLS-1$
			String message = MessageFormat.format(format, args);
			throw new WidgetBuildingException(message);
		}
		Method setter = pDesc.getWriteMethod();
		if (setter == null) {
			Object[] args = new Object[] { field, clazz };
			String format = Messages.getString(Messages.getString("WidgetPropertySetter.Field_is_read_only_30")); //$NON-NLS-1$
			String message = MessageFormat.format(format, args);
			throw new WidgetBuildingException(message);
		}
		return setter;
	}

	public static WidgetPropertySetter getInstance() {
		if (theInstance == null) {
			theInstance = new WidgetPropertySetter();
		}
		return theInstance;
	}

	private Method findSetter(String name) {
		try {
			Class[] argTypes = new Class[] { Interface.class,
											 Component.class,
											 String.class };
			Method setter = WidgetPropertySetter.class.getDeclaredMethod(name, argTypes);
			setter.setAccessible(true);
			return setter;
		} catch (SecurityException e) {
			logger.fatal(e);
			throw new WidgetPropertySettingException(e);
		} catch (NoSuchMethodException e) {
			logger.fatal(e);
			throw new WidgetPropertySettingException(e);
		}
	}

	// special setters
	private void setVisible(Interface xml, Component widget, String value) {
		boolean v = ParameterConverter.toBoolean(value);
		widget.setVisible(v);
	}

	private void setEditable(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JTextComponent)) {
			return;
		}
		JTextComponent text = (JTextComponent)widget;
		boolean v = ParameterConverter.toBoolean(value);
		text.setEditable(v);
	}

	private void setWindowTitle(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof Frame)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_Frame_widget")); //$NON-NLS-1$
		}
		Frame frame = (Frame)widget;
		frame.setTitle(value);
	}

	private void setWidth(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof Component)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.attempt_to_set_width_to_non_component")); //$NON-NLS-1$
		}
		Component c = (Component)widget;
		Dimension size = c.getSize();
		try {
			size.width = Integer.parseInt(value);
			c.setSize(size);
			
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_number")); //$NON-NLS-1$
		}
	}

	private void setHeight(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof Component)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.attempt_to_set_height_to_non_component")); //$NON-NLS-1$
		}
		Component c = (Component)widget;
		Dimension size = c.getSize();
		try {
			size.height = Integer.parseInt(value);
			c.setSize(size);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_number")); //$NON-NLS-1$
		}
	}

	private void setJustify(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JLabel)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JLabel_widget")); //$NON-NLS-1$
		}
		int alignment = SwingConstants.CENTER;
		value = normalize(value, "JUSTIFY_"); //$NON-NLS-1$
		if ("CENTER".equals(value)) { //$NON-NLS-1$
			alignment = SwingConstants.CENTER;
		} else if ("LEFT".equals(value)) { //$NON-NLS-1$
			alignment = SwingConstants.LEFT;
		} else if ("RIGHT".equals(value)) { //$NON-NLS-1$
			alignment = SwingConstants.RIGHT;
		} else {
			logger.warn(Messages.getString("WidgetPropertySetter.not_supported")); //$NON-NLS-1$
		}
		JLabel label = (JLabel)widget;
		label.setHorizontalAlignment(alignment);
	}

	private void setTooltip(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JComponent)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JComponent_widget")); //$NON-NLS-1$
		}
		JComponent c = (JComponent)widget;
		c.setToolTipText(value);
	}

	private void setHasDefault(Interface xml, Component widget, String value) {
		if (ParameterConverter.toBoolean(value)) {
			xml.setDefaultWidget(widget);
		}
	}

	private void setHasFocus(Interface xml, Component widget, String value) {
		if (ParameterConverter.toBoolean(value)) {
			xml.setFocusWidget(widget);
		}
	}

	private void setListItemLabel(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JLabel)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JLabel_widget")); //$NON-NLS-1$
		}
		JLabel label = (JLabel)widget;
		label.setText(value);
	}

	private void setCListColumnWidth(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JTable)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JTable_widget")); //$NON-NLS-1$
		}
		JTable table = (JTable)widget;
		TableColumnModel model = table.getColumnModel();

		StringTokenizer tokens = new StringTokenizer(value, String.valueOf(','));
		int columns = tokens.countTokens();
		int col = 0;
		while (model.getColumnCount() < columns) {
			model.addColumn(new TableColumn());
		}
		
		while (tokens.hasMoreTokens()) {
			int width = ParameterConverter.toInteger(tokens.nextToken());
			TableColumn column = model.getColumn(col);
			column.setPreferredWidth(width);
			col++;
		}
	}

	private void setCListSelectionMode(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JTable)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JTable_widget")); //$NON-NLS-1$
		}
		JTable table = (JTable)widget;
		value = normalize(value, "SELECTION_"); //$NON-NLS-1$
		if ("SINGLE".equals(value)) { //$NON-NLS-1$
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} else if ("MULTIPLE".equals(value)) { //$NON-NLS-1$
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if ("EXTENDED".equals(value)) { //$NON-NLS-1$
			logger.info(Messages.getString("WidgetPropertySetter.setCListSelectionMode_EXTENDED_is_not_supported")); //$NON-NLS-1$
		} else if ("BROWSE".equals(value)) { //$NON-NLS-1$
			logger.info(Messages.getString("WidgetPropertySetter.setCListSelectionMode_BROWSE_is_not_supported")); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	private void setCListShadowType(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setCListShadowType_is_not_supported")); //$NON-NLS-1$
	}

	private void setCListShowTitles(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JTable)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JTable_widget")); //$NON-NLS-1$
		}
		JTable table = (JTable)widget;
		JTableHeader header = table.getTableHeader();
		header.setVisible(ParameterConverter.toBoolean(value));
	}

	private void setTreeSelectionMode(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JTree)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JTree_widget")); //$NON-NLS-1$
		}
		JTree tree = (JTree)widget;
		TreeSelectionModel model = tree.getSelectionModel();
		value = normalize(value, "SELECTION_"); //$NON-NLS-1$
		if ("SINGLE".equals(value)) { //$NON-NLS-1$
			model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		} else if ("MULTIPLE".equals(value)) { //$NON-NLS-1$
			model.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		} else if ("EXTENDED".equals(value)) { //$NON-NLS-1$
			logger.info(Messages.getString("WidgetPropertySetter.setTreeSelectionMode_EXTENDED_is_not_supported")); //$NON-NLS-1$
		} else if ("BROWSE".equals(value)) { //$NON-NLS-1$
			logger.info(Messages.getString("WidgetPropertySetter.setTreeSelectionMode_BROWSE_is_not_supported")); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	private void setTreeViewMode(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setTreeViewMode_is_not_supported")); //$NON-NLS-1$
	}

	private void setTreeViewLine(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setTreeViewLine_is_not_supported")); //$NON-NLS-1$
	}

	private void setListSelectionMode(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JList)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JList_widget")); //$NON-NLS-1$
		}
		JList list = (JList)widget;
		ListSelectionModel model = list.getSelectionModel();
		value = normalize(value, "SELECTION_"); //$NON-NLS-1$
		if ("SINGLE".equals(value)) { //$NON-NLS-1$
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} else if ("MULTIPLE".equals(value)) { //$NON-NLS-1$
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if ("EXTENDED".equals(value)) { //$NON-NLS-1$
			logger.info(Messages.getString("WidgetPropertySetter.setListSelectionMode_EXTENDED_is_not_supported")); //$NON-NLS-1$
		} else if ("BROWSE".equals(value)) { //$NON-NLS-1$
			logger.info(Messages.getString("WidgetPropertySetter.setListSelectionMode_BROWSE_is_not_supported")); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	private void setCheckMenuItemAlwaysShowToggle(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setCheckMenuItemAlwaysShowToggle_is_not_supported")); //$NON-NLS-1$
	}

	private void setTextText(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JTextComponent)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JTextComponent_widget")); //$NON-NLS-1$
		}
		JTextComponent text = (JTextComponent)widget;
		text.setText(value);
	}

	private void setTextViewText(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JTextComponent)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JTextComponent_widget")); //$NON-NLS-1$
		}
		JTextComponent text = (JTextComponent)widget;
		text.setText(value);
	}

	private void setRadioMenuItemGroup(Interface xml, Component widget, String value) {
		ButtonGroup group;
		group = xml.getButtonGroup(value);

		if (group == null) {
			logger.warn(Messages.getString("WidgetPropertySetter.Radio_button_group_n_could_not_be_found"), value); //$NON-NLS-1$
			return;
		}

		if ((Object)group == (Object)widget) {
			logger.warn("Group is self, skipping."); //$NON-NLS-1$
			return;
		}

		group.add((AbstractButton)widget);
	}

	private void setToolbarTooltips(Interface xml, Component widget, String value) {
		ToolTipManager manager = ToolTipManager.sharedInstance();
		if ( ! (widget instanceof JToolBar)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_JToolBar")); //$NON-NLS-1$
		}

		if (widget instanceof JComponent) {
			if (ParameterConverter.toBoolean(value)) {
				manager.registerComponent((JComponent)widget);
			} else {
				manager.unregisterComponent((JComponent)widget);
			}
		}
	}
	
	private void setStatusBarHasResizeGrip(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setStatusBarResizeGrip_is_not_supported")); //$NON-NLS-1$
	}

	private void setRulerMetric(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setRulerMetric_is_not_supported")); //$NON-NLS-1$
	}

	private void setMenuItemLabel(Interface xml, Component widget, String value) {
		JMenuItem item = (JMenuItem)widget;
		item.setText(value);
	}

	private void setMenuItemUseUnderline(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setMenuItemUseUnderline_is_not_supported")); //$NON-NLS-1$
	}

	private void setMenuItemUseStock(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setMenuItemUseStock_is_not_supported")); //$NON-NLS-1$
	}

	private void setWindowWMClassName(Interface xml, Component widget, String value) {
		logger.info(Messages.getString("WidgetPropertySetter.setWindowWMClassName_is_not_supported")); //$NON-NLS-1$
	}
	
	private void setEntryInvisibleChar(Interface xml, Component widget, String value) {
		if ( !(widget instanceof JPasswordField)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_Password_field")); //$NON-NLS-1$
		}
		JPasswordField password = (JPasswordField)widget;
		password.setEchoChar(value.charAt(0));
	}

	private void setTableLeftAttach(Interface xml, Component widget, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_Table_widget")); //$NON-NLS-1$
		}
		
		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.leftAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	private void setTableRightAttach(Interface xml, Component widget, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.rightAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	private void setTableTopAttach(Interface xml, Component widget, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.topAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	private void setTableBottomAttach(Interface xml, Component widget, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.bottomAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	private void setNumberEntryFormat(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof NumberEntry)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_NumberEntry_widget")); //$NON-NLS-1$
		}
		NumberEntry entry = (NumberEntry)widget;
		entry.setFormat(value);		
	}

	private void setPandaHTMLURI(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof PandaHTML)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_PandaHTML_widget")); //$NON-NLS-1$
		}
		PandaHTML pane = (PandaHTML)widget;
		pane.setURI(value);
	}

	private void setPandaEntryInputMode(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof PandaEntry)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_PandaEntry_widget")); //$NON-NLS-1$
		}
		PandaEntry entry = (PandaEntry)widget;
		if (value.equals("ASCII")) { //$NON-NLS-1$
			entry.setInputMode(PandaEntry.ASCII);
		} else if (value.equals("KANA")) { //$NON-NLS-1$
			entry.setInputMode(PandaEntry.KANA);
		} else if (value.equals("XIM")) { //$NON-NLS-1$
			entry.setInputMode(PandaEntry.XIM);
		} else {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.invalide_input_mode")); //$NON-NLS-1$
		}
	}

	private void setPandaEntryXIMEnabled(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof PandaEntry)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_PandaEntry_widget")); //$NON-NLS-1$
		}
		PandaEntry entry = (PandaEntry)widget;
		boolean enabled = ParameterConverter.toBoolean(value);
		entry.setXIMEnabled(enabled);
	}

	private void setPandaTimerDuration(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof PandaTimer)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_PandaTimer_widget")); //$NON-NLS-1$
		}
		PandaTimer timer = (PandaTimer)widget;
		int duration = ParameterConverter.toInteger(value);
		timer.setDuration(duration);
	}

	private void setProgressBarLower(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_ProgressBar_widget")); //$NON-NLS-1$
		}
		JProgressBar progress = (JProgressBar)widget;
		BoundedRangeModel model = progress.getModel();
		model.setMinimum(ParameterConverter.toInteger(value));
	}

	private void setProgressBarUpper(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_ProgressBar_widget")); //$NON-NLS-1$
		}
		JProgressBar progress = (JProgressBar)widget;
		BoundedRangeModel model = progress.getModel();
		model.setMaximum(ParameterConverter.toInteger(value));
	}

	private void setProgressBarValue(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_ProgressBar_widget")); //$NON-NLS-1$
		}
		JProgressBar progress = (JProgressBar)widget;
		BoundedRangeModel model = progress.getModel();
		model.setValue(ParameterConverter.toInteger(value));
	}

	private void setProgressBarOrientation(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_ProgressBar_widget")); //$NON-NLS-1$
		}
		JProgressBar progress = (JProgressBar)widget;
		value = normalize(value, "PROGRESS_"); //$NON-NLS-1$
		if ("LEFT_TO_RIGHT".equals(value)) { //$NON-NLS-1$
			progress.setOrientation(JProgressBar.HORIZONTAL);
		} else if ("RIGHT_TO_LEFT".equals(value)) { //$NON-NLS-1$
			progress.setOrientation(JProgressBar.HORIZONTAL);
		} else if ("TOP_TO_BOTTOM".equals(value)) { //$NON-NLS-1$
			progress.setOrientation(JProgressBar.VERTICAL);
		} else if ("BOTTOM_TO_TOP".equals(value)) { //$NON-NLS-1$
			progress.setOrientation(JProgressBar.VERTICAL);
		}
	}

	private void setProgressBarShowText(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_ProgressBar_widget")); //$NON-NLS-1$
		}
		JProgressBar progress = (JProgressBar)widget;
		boolean showText = ParameterConverter.toBoolean(value);
		progress.setStringPainted(showText);
	}

	private void setFrameLabel(Interface xml, Component widget, String value) {
		if ( ! (widget instanceof org.montsuqi.widgets.Frame)) {
			throw new IllegalArgumentException(Messages.getString("WidgetPropertySetter.not_a_Frame_widget")); //$NON-NLS-1$
		}
		org.montsuqi.widgets.Frame frame = (org.montsuqi.widgets.Frame)widget;
		Border border = BorderFactory.createTitledBorder(value);
		frame.setBorder(border);
	}

	private String normalize(String value, String prefixToRemove) {
		if (value.startsWith("GTK_")) { //$NON-NLS-1$
			value = value.substring("GTK_".length()); //$NON-NLS-1$
		}
		if (value.startsWith(prefixToRemove)) {
			value = value.substring(prefixToRemove.length());
		}
		return value;
	}
}
