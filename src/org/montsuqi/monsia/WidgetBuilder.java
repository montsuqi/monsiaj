package org.montsuqi.monsia;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.montsuqi.util.Logger;
import org.montsuqi.util.ParameterConverter;
import org.montsuqi.widgets.TableConstraints;
import org.montsuqi.widgets.TableLayout;

class WidgetBuilder {

	private Interface xml;
	private Logger logger;

	private Map classMap;
	private Map propertyMap;
	private Map builderMap;

	private static WidgetBuildData defaultBuildWidgetData;
	private static WidgetBuildData defaultBuildContainerData;

	static {
		defaultBuildWidgetData = new WidgetBuildData("standardBuildWidget", null, null); //$NON-NLS-1$
		defaultBuildContainerData = new WidgetBuildData("standardBuildWidget", "standardBuildChildren", null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	WidgetBuilder(Interface xml) {
		this.xml = xml;
		builderMap = new HashMap();
		logger = Logger.getLogger(WidgetBuilder.class);
		initClassMap();
		initPropertyMap();
		initWidgetBuildData();
	}

	private void initClassMap() {
		classMap = new HashMap();
		registerClass("Button", javax.swing.JButton.class); //$NON-NLS-1$
		//regsiterClass("Calendar", null);
		registerClass("CList", javax.swing.JTable.class); //$NON-NLS-1$
		registerClass("CheckButton", javax.swing.JTable.class); //$NON-NLS-1$
		registerClass("Combo", javax.swing.JComboBox.class); //$NON-NLS-1$
		registerClass("Entry", javax.swing.JTextField.class); //$NON-NLS-1$
		registerClass("Fixed", org.montsuqi.widgets.Fixed.class); //$NON-NLS-1$
		registerClass("Frame", javax.swing.JLabel.class); //$NON-NLS-1$
		//registerClass("HSeparator", null);
		registerClass("ImageMenuItem", javax.swing.JLabel.class); //$NON-NLS-1$
		registerClass("Label", javax.swing.JLabel.class); //$NON-NLS-1$
		registerClass("List", javax.swing.JList.class); //$NON-NLS-1$
		registerClass("Menu", javax.swing.JMenu.class); //$NON-NLS-1$
		registerClass("MenuBar", javax.swing.JMenuBar.class); //$NON-NLS-1$
		registerClass("MenuItem", javax.swing.JLabel.class); //$NON-NLS-1$
		registerClass("Notebook", javax.swing.JTabbedPane.class); //$NON-NLS-1$
		registerClass("NumberEntry", org.montsuqi.widgets.NumberEntry.class); //$NON-NLS-1$
		registerClass("OptionMenu", javax.swing.JMenu.class); //$NON-NLS-1$
		registerClass("PandaCList", javax.swing.JTable.class); //$NON-NLS-1$
		registerClass("PandaCombo", javax.swing.JComboBox.class); //$NON-NLS-1$
		registerClass("PandaEntry", org.montsuqi.widgets.PandaEntry.class); //$NON-NLS-1$
		//registerClass("PandaHTML", null);
		registerClass("PandaText", javax.swing.JTextArea.class); //$NON-NLS-1$
		registerClass("RadioButton", javax.swing.JRadioButton.class); //$NON-NLS-1$
		registerClass("ScrolledWindow", javax.swing.JScrollPane.class); //$NON-NLS-1$
		registerClass("SeparatorMenuItem", javax.swing.JSeparator.class); //$NON-NLS-1$
		registerClass("Table", org.montsuqi.widgets.Table.class); //$NON-NLS-1$
		registerClass("Text", javax.swing.JTextArea.class); //$NON-NLS-1$
		registerClass("TextView", javax.swing.JTextArea.class); //$NON-NLS-1$
		registerClass("ToggleButton", javax.swing.JToggleButton.class); //$NON-NLS-1$
		registerClass("Toolbar", javax.swing.JToolBar.class); //$NON-NLS-1$
		registerClass("VBox", org.montsuqi.widgets.VBox.class); //$NON-NLS-1$
		//registerClass("VSeparator", null);
		registerClass("Viewport", null); //$NON-NLS-1$
		registerClass("Window", javax.swing.JFrame.class); //$NON-NLS-1$
	}

	private void initPropertyMap() {
		propertyMap = new HashMap();
		registerProperty(java.awt.Frame.class, "title", "setWindowTitle"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(java.awt.Container.class, "visible", "setVisible"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.AbstractButton.class, "label", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JLabel.class, "label", "text"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JLabel.class, "justify", "setJustify");
		registerProperty(javax.swing.JTextField.class, "justify", "horizontalAlignment"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(java.awt.Component.class, "width_request", "setWidth");
		registerProperty(java.awt.Component.class, "height_request", "setHeight");
		registerProperty(java.awt.Component.class, "width", "setWidth");
		registerProperty(java.awt.Component.class, "height", "setHeight");

/*
		registerProperty("selectable", null);
		registerProperty("xalign", null);
		registerProperty("yalign", null);
		registerProperty("xpad", null);
		registerProperty("ypad", null);
		registerProperty("xalign", null);
		registerProperty("n_rows", null);
		registerProperty("n_columns", null);
		registerProperty("homogenous", null);
		registerProperty("row_spacing", null);
		registerProperty("column_spacing", null);
*/
	    registerProperty(java.awt.Container.class, "visible", "setVisible"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(java.awt.Container.class, "tooltip", "setTooltip"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(java.awt.Container.class, "has_default", "setHasDefault"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(java.awt.Container.class, "has_focus", "setHasFocus"); //$NON-NLS-1$ //$NON-NLS-2$
//		registerProperty(PIXMAP, "build_insensitive", pixmap_set_build_insensitive);
//		registerProperty(PIXMAP, "filename", pixmap_set_filename);
		registerProperty(javax.swing.text.JTextComponent.class, "text", "setTextViewText"); //$NON-NLS-1$ //$NON-NLS-2$
//		registerProperty(CALENDAR, "display_options", calendar_set_display_options);
		registerProperty(javax.swing.JTable.class, "column_widths", "setCListColumnWidth"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JTable.class, "selection_mode", "setCListSelectionMode"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JTable.class, "shadow_type", "setCListShadowType"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JTable.class, "show_titles", "setCListShowTitles"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JTree.class, "selection_mode", "setTreeSelectionMode"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JTree.class, "view_mode", "setTreeViewMode"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JTree.class, "view_line", "setTreeViewLine"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JList.class, "selection_mode", "setListSelectionMode"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JCheckBoxMenuItem.class, "always_show_toggle", "setCheckMenuItemAlwaysShowToggle"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.text.JTextComponent.class, "text", "setTextText"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JRadioButtonMenuItem.class, "group", "setRadioMenuItemGroup"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JToolBar.class, "tooltips", "setToolbarTooltips"); //$NON-NLS-1$ //$NON-NLS-2$
//		registerPrfoperty(STATUSBAR, "has_resize_grip", statusbar_set_has_resize_grip);
//		registerProperty(RULER, "metric", ruler_set_metric);
		registerProperty(javax.swing.JMenuItem.class, "label", "setMenuItemLabel"); //$NON-NLS-1$ //$NON-NLS-2$
		registerProperty(javax.swing.JTextField.class, "invisible_char", "setEntryInvisibleChar"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void initWidgetBuildData() {
		registerWidgetBuildData("Button", defaultBuildWidgetData); //$NON-NLS-1$
		//registerWidgetBuildData("Calendar", defaultBuildWidgetData);
		registerWidgetBuildData("CheckButton", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("CheckMenuItem", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildMenuItemChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("CList", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildCListChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("ColorSelection", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("ColorSelectionDialog", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"standardBuildChildren", //$NON-NLS-1$
								"colorSelectionDialogFindInternalChild"); //$NON-NLS-1$
		registerWidgetBuildData("Combo", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"standardBuildChildren", //$NON-NLS-1$
								"comboFindInternalChild"); //$NON-NLS-1$
		registerWidgetBuildData("CTree", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildCListChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("Dialog", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildDialogChildren", //$NON-NLS-1$
								"dialogFindInternalChild"); //$NON-NLS-1$
		registerWidgetBuildData("DrawingArea", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("Entry", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("FileSelection", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"standardBuildChildren", //$NON-NLS-1$
								"fileSelectionDialogFindInternalChild"); //$NON-NLS-1$
		registerWidgetBuildData("Fixed", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildLayoutChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("FontSelection", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("FontSelectionDialog", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"standardBuildChildren", //$NON-NLS-1$
								"fontSelectionDialogFindInternalChild"); //$NON-NLS-1$
		registerWidgetBuildData("Frame",	 //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildFrameChildren", //$NON-NLS-1$
								null);
	registerWidgetBuildData("HBox", defaultBuildContainerData); //$NON-NLS-1$
		registerWidgetBuildData("HPaned", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildPanedChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("HSeparator", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("Label", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("Layout", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildLayoutChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("List", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("ListItem", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("Menu", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("MenuBar", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("MenuItem", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildMenuItemChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("MessageDialog", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"standardBuildChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("Notebook", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildNotebookChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("OptionMenu", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildOptionMenuChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("Progress", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("ProgressBar", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("RadioButton", defaultBuildContainerData); //$NON-NLS-1$
		registerWidgetBuildData("RadioMenuItem", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildMenuItemChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("ScrolledWindow", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"standardBuildChildren", //$NON-NLS-1$
								"scrolledWindowFindInternalChild"); //$NON-NLS-1$
		registerWidgetBuildData("SeparatorMenuItem", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("SpinButton", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("StatusBar", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("Table", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildTableChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("TeaoffMenuItem", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("Text", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("TextView", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("ToggleButton", defaultBuildContainerData); //$NON-NLS-1$
		registerWidgetBuildData("ToolBar", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildToolBarChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("Tree", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("TreeView", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("VBox", defaultBuildContainerData); //$NON-NLS-1$
		registerWidgetBuildData("VPaned", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"buildPanedChildren", //$NON-NLS-1$
								null);
		registerWidgetBuildData("VSeparator", defaultBuildWidgetData); //$NON-NLS-1$
		registerWidgetBuildData("ViewPort", defaultBuildContainerData); //$NON-NLS-1$
		registerWidgetBuildData("Window", //$NON-NLS-1$
								"standardBuildWidget", //$NON-NLS-1$
								"standardBuildChildren", //$NON-NLS-1$
								null);
	}

	private void registerClass(String genericClassName, Class clazz) {
		classMap.put(genericClassName, clazz);
	}

	private void registerProperty(Class clazz, String propertyName, String setterName) {
		if ( ! propertyMap.containsKey(clazz)) {
			propertyMap.put(clazz, new HashMap());
		}
		Map map = (Map)propertyMap.get(clazz);
		Method setter = null;
		if (setterName != null) {
			if (setterName.startsWith("set")) { //$NON-NLS-1$
				setter = WidgetOperation.findMethod(setterName);
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

	private void registerWidgetBuildData(String genericClassName, WidgetBuildData data) {
		builderMap.put(genericClassName, data);
	}
	
	private void registerWidgetBuildData(String genericClassName,
										 String build, String buildChildren, String findInternalChild) {
		registerWidgetBuildData(genericClassName,
								new WidgetBuildData(build, buildChildren, findInternalChild));
	}

	private WidgetBuildData getBuildData(String genericClassName) {
		return (WidgetBuildData)builderMap.get(genericClassName);
	}

	private WidgetBuildData getBuildData(Class clazz) {
		Iterator i = classMap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			Class value = (Class)e.getValue();
			if (clazz == value) {
				return getBuildData((String)e.getKey());
			}
		}
		Object[] args = new Object[] { clazz };
		String format = Messages.getString("WidgetBuilder.Unknown_widget_class");
		String message = MessageFormat.format(format, args);
		throw new WidgetBuildingException(message);
	}
	
	//--------------------------------------------------------------------

	Container buildWidget(WidgetInfo info) {
		Container widget;
		String genericClassName = info.getClassName();
		WidgetBuildData data = getBuildData(genericClassName);
		if (data == null) {
			logger.warn(Messages.getString("WidgetBuilder.Unknown_widget_class"), genericClassName); //$NON-NLS-1$
			widget = new JLabel("[a " + genericClassName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			try {
				widget = data.build(this, info);
			} catch (Exception e) {
				logger.warn(e);
				widget = new JLabel('[' + e.toString() + ']');
			}
		}
		if (widget instanceof JWindow) {
			xml.setTopLevel((JWindow)widget);
		}
		setCommonParams(widget, info);
		if (widget instanceof JWindow) {
			xml.setTopLevel(null);
		}
		return widget;
	}

	Container standardBuildWidget(WidgetInfo info) {
		String genericClassName = info.getClassName();
		Class clazz  = (Class)classMap.get(genericClassName);
		if (clazz == null) {
			throw new IllegalArgumentException(genericClassName);
		}
		Container widget = null;
		try {
			widget = (Container)clazz.newInstance();
			setProperties(widget, info);
			return widget;
		} catch (InstantiationException e) {
			logger.fatal(e);
			throw new InterfaceBuildingException(e);
		} catch (IllegalAccessException e) {
			logger.fatal(e);
			throw new InterfaceBuildingException(e);
		}
	}

	Container buildPreview(WidgetInfo info) {
		logger.warn(Messages.getString("WidgetBuilder.not_implemented")); //$NON-NLS-1$
		return null;
	}

	//--------------------------------------------------------------------

	void standardBuildChildren(Container parent, WidgetInfo info) {
		Iterator i = info.getChildren().iterator();
		while (i.hasNext()) {
			ChildInfo cInfo = (ChildInfo)i.next();
			if (cInfo.getInternalChild() != null) {
				handleInternalChild(parent, cInfo);
			}
			Container child = buildWidget(cInfo.getWidgetInfo());
			if (parent instanceof JWindow) {
				parent = ((JWindow)parent).getContentPane();
			} else if (parent instanceof JFrame) {
				parent = ((JFrame)parent).getContentPane();
			} else if (parent instanceof JDialog) {
				parent = ((JDialog)parent).getContentPane();
			} else if (parent instanceof JApplet) {
				parent = ((JApplet)parent).getContentPane();
			}
			parent.add(child);
		}
	}

	void buildMenuItemChildren(Container parent, WidgetInfo info) {
		JMenu menu = (JMenu)parent;
		int cCount = info.getChildrenCount();
		for (int i = 0; i < cCount; i++) {
			Container child;
			ChildInfo cInfo = info.getChild(i);
			if (cInfo.getInternalChild() != null) {
				handleInternalChild(parent, cInfo);
				continue;
			}

			WidgetInfo wInfo = cInfo.getWidgetInfo();
			child = buildWidget(wInfo);
			if (child instanceof JMenu) {
				menu.add(child); // JMenu does not matter item types?
			} else {
				menu.add(child);
			}
		}
	}

	void buildDialogChildren(Container parent, WidgetInfo info) {
		standardBuildChildren(parent, info);
	}


	void buildFrameChildren(Container parent, WidgetInfo info) {
		int FRAME_ITEM = 0;
		int LABEL_ITEM = 1;
		int cCount = info.getChildrenCount();
		if (cCount != 2) {
			throw new IllegalStateException(Messages.getString("WidgetBuilder.there_should_really_only_be_2_children")); //$NON-NLS-1$
		}
		for (int i = 0; i < cCount; i++) {
			String label = null;
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			int type = FRAME_ITEM;
			int pCount = cInfo.getPropertiesCount();
			for (int j = 0; j < pCount; j++) {
				String pName = cInfo.getProperty(j).getName();
				String pValue = cInfo.getProperty(j).getValue();
				if ("type".equals(pName) && "label_item".equals(pValue)) { //$NON-NLS-1$ //$NON-NLS-2$
					type = LABEL_ITEM;
				} else if ("label".equals(pName)) { //$NON-NLS-1$
					label = pValue;
				}
			}

			if (type == LABEL_ITEM || label != null) {
				JComponent comp = (JComponent)parent;
				comp.setBorder(BorderFactory.createTitledBorder(label));
			} else {
				parent.add(buildWidget(cInfo.getWidgetInfo()));
			}
		}
	}

	void buildNotebookChildren(Container parent, WidgetInfo info) {
		int tab = 0;
		int PANE_ITEM = 0;
		int TAB_ITEM = 1;
		int MENU_ITEM = 2;
		int cCount = info.getChildrenCount();
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Container child = buildWidget(wInfo);
			int type = PANE_ITEM;
			int pCount = wInfo.getPropertiesCount();
			for (int j = 0; j < pCount; j++) {
				Property p = cInfo.getProperty(j);
				String pName = p.getName();
				if ("type".equals(pName)) { //$NON-NLS-1$
					String value = p.getValue();
					if ("tab".equals(value)) { //$NON-NLS-1$
						type = TAB_ITEM;
					}
					break;
				}
			}

			JTabbedPane tabbed = (JTabbedPane)parent;
			if (type == TAB_ITEM) { /* The GtkNotebook API blows */
				Component body = tabbed.getComponentAt(tab);
				//tabbed.setTitleAt(tab, child);
			} else {
				tabbed.add(child);
				tab++;
			}
		}
	}

	void buildOptionMenuChildren(Container parent, WidgetInfo info) {
		int history = 0;
		int cCount = info.getChildrenCount();
		
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();

			if ("Menu".equals(wInfo.getClassName())) { //$NON-NLS-1$
				logger.warn(Messages.getString("WidgetBuilder.the_child_of_the_option_menu_{0}_was_not_a_Menu_widget_2"), wInfo.getName()); //$NON-NLS-1$
				continue;
			}
			Container child = buildWidget(wInfo);

			JMenu menu = (JMenu)parent;
			menu.add(child);
		}
	}

	void buildCListChildren(Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Container child = null;

			/* treat Labels specially */
			if ("Label".equals(wInfo.getClassName())) { //$NON-NLS-1$
				String label = null;
				int pCount = wInfo.getPropertiesCount();
				for (int j = 0; j < pCount; j++) {
					Property p = wInfo.getProperty(j);
					if ("label".equals(p.getName())) { //$NON-NLS-1$
						label = p.getValue();
						break;
					} else {
						logger.warn(Messages.getString("WidgetBuilder.Unknown_CList_child_property__{0}_3"), p.getName()); //$NON-NLS-1$
					}
				}
				
				if (label != null) {
					/* FIXME: translate ? */
					JTable table = (JTable)parent;
					TableColumnModel model = table.getColumnModel();
					TableColumn column = model.getColumn(i);
					column.setHeaderValue(label);
//					child = gtk_clist_get_column_widget (GTK_CLIST (parent), i);
//					child = GTK_BIN(child)->child;
//					glade_xml_set_common_params(self, child, childinfo);
				}
			}

//			if (!child) {
//				child = glade_xml_build_widget (self, childinfo);
//				gtk_clist_set_column_widget (GTK_CLIST (parent), i, child);
//			}
		}
	}

	void buildToolBarChildren(Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		JToolBar toolBar = (JToolBar)parent;
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Container child = null;
			String className = wInfo.getClassName();
			if ("toggle".equals(className) || //$NON-NLS-1$
				"radio".equals(className) || //$NON-NLS-1$
				"button".equals(className)) { //$NON-NLS-1$
				String label = null;
				String stock = null;
				String groupName = null;
				String icon = null;
				boolean useStock = false;
				boolean active = false;
				boolean newGroup = false;
				Container iconw = null;
				int pCount = wInfo.getPropertiesCount();
				for (int j = 0; j < pCount; j++) {
					Property p = wInfo.getProperty(j);
					String pName = p.getName();
					String pValue = p.getValue();
					
					if ("label".equals(pName)) { //$NON-NLS-1$
						label = pValue;
					} else if ("use_stock".equals(pName)) { //$NON-NLS-1$
						useStock = true;
					} else if ("icon".equals(pName)) { //$NON-NLS-1$
						stock = null;
						icon = xml.relativeFile(pValue);
					} else if ("stock_pixmap".equals(pName)) { //$NON-NLS-1$
						icon = null;
						stock = pValue;
					} else if ("active".equals(pName)) { //$NON-NLS-1$
						active = ParameterConverter.toBoolean(pValue);
					} else  if ("group".equals(pName)) { //$NON-NLS-1$
						groupName = pValue;
					} else if ("new_group".equals(pName)) { //$NON-NLS-1$
						newGroup = ParameterConverter.toBoolean(pValue);
					} else if ("visible".equals(pName)) { //$NON-NLS-1$
						/* ignore for now */
					} else if ("tooltip".equals(pName)) { //$NON-NLS-1$
						/* ignore for now */
					} else if ("use_underline".equals(pName)) { //$NON-NLS-1$
						/* useUnderline = ParameterConverter.toBoolean(pValue); */
					} else if ("inconsistent".equals(pName)) { //$NON-NLS-1$
						/* ignore for now */
					} else {
						logger.warn(Messages.getString("WidgetBuilder.Unknown_Toolbar_child_property__{0}_4"), pName); //$NON-NLS-1$
					}
				}

				/* ignore stock icon stuff */
				if (newGroup) {
					toolBar.addSeparator();
				}

				if ("toggle".equals(className)) { //$NON-NLS-1$
					child = new JToggleButton(label);
					toolBar.add(child);
					child.setEnabled(active);
				} else if ("radio".equals(className)) { //$NON-NLS-1$
					child = new JRadioButton(label);
					toolBar.add(child);
					if (groupName != null) {
						ButtonGroup group = xml.getButtonGroup(groupName);
						group.add((AbstractButton)child);
					}
				} else {
					child = new JLabel(label);
					toolBar.add(child);
				}
				/* ignore underline stuff */
				setCommonParams(child, cInfo.getWidgetInfo());
			} else {
				child = buildWidget(cInfo.getWidgetInfo());
				toolBar.add(child);
			}
		}
	}

	void buildPanedChildren(Container parent, WidgetInfo info) {
		JSplitPane pane = (JSplitPane)parent;
		int cCount = info.getChildrenCount();
		if (cCount == 0) {
			return;
		}
		
		ChildInfo cInfo = info.getChild(0);
		WidgetInfo wInfo = cInfo.getWidgetInfo();
		Container child = buildWidget(wInfo);
		boolean resize = false;
		boolean shrink = true;

		int pCount = wInfo.getPropertiesCount();
		for (int i = 0; i < pCount; i++) {
			Property p = wInfo.getProperty(i);
			String name = p.getName();
			String value = p.getValue();
			
			if ("resize".equals(name)) { //$NON-NLS-1$
				resize = ParameterConverter.toBoolean(value);
			} else if ("shrink".equals(name)) { //$NON-NLS-1$
				shrink = ParameterConverter.toBoolean(value);
			} else {
				logger.warn(Messages.getString("WidgetBuilder.Unknown_Paned_child_property__{0}_5"), name); //$NON-NLS-1$
			}
		}

		pane.setLeftComponent(child); // resize and shrink are ignored.

		if (cCount == 1) {
			return;
		}

		cInfo = info.getChild(1);
		wInfo = cInfo.getWidgetInfo();
		child = buildWidget(wInfo);
		resize = true;
		shrink = true;
		pCount = wInfo.getPropertiesCount();

		for (int i = 0; i < pCount; i++) {
			Property p = wInfo.getProperty(i);
			String name = p.getName();
			String value = p.getValue();
			
			if ("resize".equals(name)) { //$NON-NLS-1$
				resize = ParameterConverter.toBoolean(value);
			} else if ("shrink".equals(name)) { //$NON-NLS-1$
				shrink = ParameterConverter.toBoolean(value);
			} else {
				logger.warn(Messages.getString("WidgetBuilder.Unknown_Paned_child_property__{0}_6"), name); //$NON-NLS-1$
			}
		}

		pane.setRightComponent(child); // resize and shrink are ignored.
	}

//		registerProperty("left_attach", "setTableLeftAttach");
//		registerProperty("right_attach", "setTableRightAttach");
//		registerProperty("top_attach", "setTableTopAttach");
//		registerProperty("bottom_attach", "setTableBottomAttach");

	void buildTableChildren(Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		TableLayout tl = (TableLayout)parent.getLayout();
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Container child = null;
			TableConstraints tc = new TableConstraints();

			String internalChild = cInfo.getInternalChild();
			if (internalChild != null) {
				handleInternalChild(parent, cInfo);
				continue;
			}

			child = buildWidget(wInfo);
			int pCount = wInfo.getPropertiesCount();
			for (int j = 0; j < pCount; j++) {
				Property p = wInfo.getProperty(j);
				String name = p.getName();
				String value = p.getValue();

				if ("left_attach".equals(name)) { //$NON-NLS-1$
					tc.leftAttach = ParameterConverter.toInteger(value);
				} else if ("right_attach".equals(name)) { //$NON-NLS-1$
					tc.rightAttach = ParameterConverter.toInteger(value);
				} else if ("top_attach".equals(name)) { //$NON-NLS-1$
					tc.topAttach = ParameterConverter.toInteger(value);
				} else if ("bottom_attach".equals(name)) { //$NON-NLS-1$
					tc.bottomAttach = ParameterConverter.toInteger(value);
				} else if ("x_options".equals(value)) { //$NON-NLS-1$
					// x_options = ParameterConverter.toInteger(value);
				} else if ("y_options".equals(value)) { //$NON-NLS-1$
					// y_options = ParameterConverter.toInteger(value);
				} else {
					logger.warn(Messages.getString("WidgetBuilder.unknown_child_packing_property_{0}_for_Table_7"), name); //$NON-NLS-1$
				}
			}
			parent.add(child);
			tl.setConstraints(child, tc);
		}
	}
	
	void buildLayoutChildren(Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Container child = null;
			int x = 0, y = 0;
			int w = 0, h = 0;

			String internalChild = cInfo.getInternalChild();
			if (internalChild != null) {
				handleInternalChild(parent, cInfo);
				continue;
			}

			child = buildWidget(wInfo);
			int pCount = cInfo.getPropertiesCount();
			for (int j = 0; j < pCount; j++) {
				Property p = cInfo.getProperty(j);
				String name = p.getName();
				String value = p.getValue();
				if ("x".equals(name)) { //$NON-NLS-1$
					x = ParameterConverter.toInteger(value);
				} else if ("y".equals(name)) { //$NON-NLS-1$
					y = ParameterConverter.toInteger(value);
				} else {
					logger.warn(Messages.getString("WidgetBuilder.unknown_child_packing_property_{0}_for_Layout_8"), name); //$NON-NLS-1$
				}
			}
			parent.add(child);
			child.setLocation(x, y);
		}
	}

	//--------------------------------------------------------------------

	Container dialogFindInternalChild(Container parent, String childName) {
		JDialog dialog = (JDialog)parent;
		if ("vbox".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("action_area".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		return null;
	}

	Container imageMenuFindInternalChild(Container parent, String childName) {
		if ("image".equals(childName)) { //$NON-NLS-1$
			return parent; // MenuItem itself is an AbstractButton and hase an icon with it.
		} else {
			return null;
		}
	}

	Container scrolledWindowFindInternalChild(Container parent, String childName) {
		JScrollPane scroll = (JScrollPane)parent;
		if ("vscrollbar".equals(childName)) { //$NON-NLS-1$
			return scroll.getVerticalScrollBar();
		}
		if ("hscrollbar".equals(childName)) { //$NON-NLS-1$
			return scroll.getHorizontalScrollBar();
		}
		
		return null;
	}

	Container fileSelectionDialogFindInternalChild(Container parent, String childName) {
		if ("vbox".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("action_area".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("ok_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("cancel_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("help_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		return null;
	}

	Container colorSelectionDialogFindInternalChild(Container parent, String childName) {
		if ("vbox".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("action_area".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("ok_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("cancel_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("help_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("color_selection".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		return null;
	}

	Container fontSelectionDialogFindInternalChild(Container parent, String childName) {
		if ("vbox".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("action_area".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("ok_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("cancel_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("apply_button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("font_selection".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		return null;
	}

	Container comboFindInternalChild(Container parent, String childName) {
		if ("entry".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("button".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("popup".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("popwin".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		if ("list".equals(childName)) { //$NON-NLS-1$
			return parent;
		}
		return null;
	}

	//--------------------------------------------------------------------

	private void handleInternalChild(Container parent, ChildInfo cInfo) {
		Container child;
		WidgetInfo info;
		Class oClass;
		int i;
		
		/* walk up the widget heirachy until we find a parent with a
		 * find_internal_child handler */
		WidgetBuildData parentBuildData = null;
		while (parentBuildData == null && parent != null) {
			parentBuildData = getBuildData(parent.getClass());
			if (parentBuildData.hasFindInternalChildMethod()) {
				break;
			}
			parentBuildData = null;
			parent = parent.getParent();
		}

		if (parentBuildData != null || parentBuildData.hasFindInternalChildMethod()) {
			logger.warn(Messages.getString("WidgetBuilder.could_not_find_a_parent_that_handles_internal_children_for_`{0}___9"), //$NON-NLS-1$
				 cInfo.getInternalChild());
			return;
		}

		child = parentBuildData.findInternalChild(this, parent, cInfo.getInternalChild());

		if (child == null) {
			logger.warn(Messages.getString("WidgetBuilder.could_not_find_internal_child_`{0}___in_parent_of_type_`{1}___10"), //$NON-NLS-1$
					new Object[] { cInfo.getInternalChild(), parent.getClass() });
			return;
		}

		WidgetInfo wInfo = cInfo.getWidgetInfo();
		setProperties(child, wInfo);
		setCommonParams(child, wInfo);
	}

	private void setProperties(Container widget, WidgetInfo info) {
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
					if (setter.getDeclaringClass() == WidgetOperation.class) {
						setter.invoke(null, new Object[] { xml, widget, pName, pValue });
					} else {
						setter.invoke(widget, new Object[] { pValue });
					}
					set = true;
				}
				if ( ! set) {
					logger.info(Messages.getString("WidgetBuilder.ignored_in_Java"), pName); //$NON-NLS-1$
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
			String format = Messages.getString("WidgetBuilder.Unknown_property");
			String message = MessageFormat.format(format, args);
			throw new WidgetBuildingException(message);
		}
		Method setter = pDesc.getWriteMethod();
		if (setter == null) {
			Object[] args = new Object[] { field, clazz };
			String format = Messages.getString("WidgetBuilder.Field_is_read_only");
			String message = MessageFormat.format(format, args);
			throw new WidgetBuildingException(message);
		}
		return setter;
	}

	private void setCommonParams(Container widget, WidgetInfo info) {
		addSignals(widget, info);
		addAccels(widget, info);
		widget.setName(info.getName());
		String className = info.getClassName();
		WidgetBuildData data = (WidgetBuildData)builderMap.get(className);
		if (data.hasBuildChildrenMethod() && info.getChildrenCount() > 0) {
			data.buildChildren(this, widget, info);
		}
		xml.setLongName(info.getLongName(), widget);
	}

	private void addSignals(Container widget, WidgetInfo info) {
		Iterator i = info.getSignals().iterator();
		while (i.hasNext()) {
			SignalInfo sInfo = (SignalInfo)i.next();
			xml.addSignal(sInfo.getHandler(), new SignalData(widget, sInfo));
		}
	}

	private void addAccels(Container widget, WidgetInfo info) {
		Iterator i = info.getAccels().iterator();
		while (i.hasNext()) {
			AccelInfo accel = (AccelInfo)i.next();
			widget.addKeyListener(new KeyAdapter() {
				public void keyPressed() {
					throw new UnsupportedOperationException(Messages.getString("WidgetBuilder.not_implemented_yet")); //$NON-NLS-1$
				}
			});
		}
	}
}
