package org.montsuqi.monsia;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JRadioButton;
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

public class WidgetBuilder {

	Interface xml;
	Logger logger;

	Map classMap;
	Map propertyMap;
	Map builderMap;

	static WidgetBuildData defaultBuildWidgetData;
	static WidgetBuildData defaultBuildContainerData;
	static {
		defaultBuildWidgetData = new WidgetBuildData("standardBuildWidget", null, null);
		defaultBuildContainerData = new WidgetBuildData("standardBuildWidget", "standardBuildChildren", null);
	}
	
	public WidgetBuilder(Interface xml) {
		this.xml = xml;
		builderMap = new HashMap();
		logger = Logger.getLogger(WidgetBuilder.class);
		initClassMap();
		initPropertyMap();
		initWidgetBuildData();
	}

	private void initClassMap() {
		classMap = new HashMap();
		registerClass("Button", javax.swing.JButton.class);
		//regsiterClass("Calendar", null);
		registerClass("CList", javax.swing.JTable.class);
		registerClass("CheckButton", javax.swing.JTable.class);
		registerClass("Combo", javax.swing.JComboBox.class);
		registerClass("Entry", javax.swing.JTextField.class);
		registerClass("Fixed", org.montsuqi.widgets.Fixed.class);
		registerClass("Frame", javax.swing.JLabel.class);
		//registerClass("HSeparator", null);
		registerClass("ImageMenuItem", javax.swing.JLabel.class);
		registerClass("Label", javax.swing.JLabel.class);
		registerClass("List", javax.swing.JList.class);
		registerClass("Menu", javax.swing.JMenu.class);
		registerClass("MenuBar", javax.swing.JMenuBar.class);
		registerClass("MenuItem", javax.swing.JLabel.class);
		registerClass("Notebook", javax.swing.JTabbedPane.class);
		registerClass("NumberEntry", org.montsuqi.widgets.NumberEntry.class);
		registerClass("OptionMenu", javax.swing.JMenu.class);
		registerClass("PandaCList", javax.swing.JTable.class);
		registerClass("PandaCombo", javax.swing.JComboBox.class);
		registerClass("PandaEntry", org.montsuqi.widgets.PandaEntry.class);
		//registerClass("PandaHTML", null);
		registerClass("PandaText", javax.swing.JTextArea.class);
		registerClass("RadioButton", javax.swing.JRadioButton.class);
		registerClass("ScrolledWindow", javax.swing.JScrollPane.class);
		registerClass("SeparatorMenuItem", javax.swing.JSeparator.class);
		registerClass("Table", org.montsuqi.widgets.Table.class);
		registerClass("Text", javax.swing.JTextArea.class);
		registerClass("TextView", javax.swing.JTextArea.class);
		registerClass("ToggleButton", javax.swing.JToggleButton.class);
		registerClass("Toolbar", javax.swing.JToolBar.class);
		registerClass("VBox", org.montsuqi.widgets.VBox.class);
		//registerClass("VSeparator", null);
		registerClass("Viewport", null);
		registerClass("Window", javax.swing.JFrame.class);
	}

	private void initPropertyMap() {
		propertyMap = new HashMap();
		registerProperty(java.awt.Container.class, "visible", "setVisible");
		registerProperty(javax.swing.AbstractButton.class, "label", "text");
		registerProperty(javax.swing.JLabel.class, "justify", "horizontalAlignment");
		registerProperty(javax.swing.JTextField.class, "justify", "horizontalAlignment");

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
	    registerProperty(java.awt.Container.class, "visible", "setVisible");
		registerProperty(java.awt.Container.class, "tooltip", "setTooltip");
		registerProperty(java.awt.Container.class, "has_default", "setHasDefault");
		registerProperty(java.awt.Container.class, "has_focus", "setHasFocus");
		
//		registerProperty(PIXMAP, "build_insensitive", pixmap_set_build_insensitive);
//		registerProperty(PIXMAP, "filename", pixmap_set_filename);
		registerProperty(javax.swing.text.JTextComponent.class, "text", "setTextViewText");
//		registerProperty(CALENDAR, "display_options", calendar_set_display_options);
		registerProperty(javax.swing.JTable.class, "column_widths", "setCListColumnWidth");
		registerProperty(javax.swing.JTable.class, "selection_mode", "setCListSelectionMode");
		registerProperty(javax.swing.JTable.class, "shadow_type", "setCListShadowType");
		registerProperty(javax.swing.JTable.class, "show_titles", "setCListSetShowTitles");
		registerProperty(javax.swing.JTree.class, "selection_mode", "setTreeSelectionMode");
		registerProperty(javax.swing.JTree.class, "view_mode", "setTreeViewMode");
		registerProperty(javax.swing.JTree.class, "view_line", "setTreeViewLine");
		registerProperty(javax.swing.JList.class, "selection_mode", "setListSelectionMode");
		registerProperty(javax.swing.JCheckBoxMenuItem.class, "always_show_toggle", "setCheckMenuItemAlwaysShowToggle");
		registerProperty(javax.swing.text.JTextComponent.class, "text", "setTextText");
		registerProperty(javax.swing.JRadioButtonMenuItem.class, "group", "setRadioMenuItemGroup");
		registerProperty(javax.swing.JToolBar.class, "tooltips", "setToolbarTooltips");
//		registerPrfoperty(STATUSBAR, "has_resize_grip", statusbar_set_has_resize_grip);
//		registerProperty(RULER, "metric", ruler_set_metric);
		registerProperty(javax.swing.JMenuItem.class, "label", "setMenuItemLabel");
		registerProperty(javax.swing.JTextField.class, "invisible_char", "setEntryInvisibleChar");
	}
	
	private void initWidgetBuildData() {
		registerWidgetBuildData("Button", defaultBuildWidgetData);
		//registerWidgetBuildData("Calendar", defaultBuildWidgetData);
		registerWidgetBuildData("CheckButton", defaultBuildWidgetData);
		registerWidgetBuildData("CheckMenuItem",
								"standardBuildWidget",
								"buildMenuItemChildren",
								null);
		registerWidgetBuildData("CList",
								"standardBuildWidget",
								"buildCListChildren",
								null);
		registerWidgetBuildData("ColorSelection", defaultBuildWidgetData);
		registerWidgetBuildData("ColorSelectionDialog",
								"standardBuildWidget",
								"standardBuildChildren",
								"colorSelectionDialogFindInternalChild");
		registerWidgetBuildData("Combo",
								"standardBuildWidget",
								"standardBuildChildren",
								"comboFindInternalChild");
		registerWidgetBuildData("CTree",
								"standardBuildWidget",
								"buildCListChildren",
								null);
		registerWidgetBuildData("Dialog",
								"standardBuildWidget",
								"buildDialogChildren",
								"dialogFindInternalChild");
		registerWidgetBuildData("DrawingArea", defaultBuildWidgetData);
		registerWidgetBuildData("Entry", defaultBuildWidgetData);
		registerWidgetBuildData("FileSelection",
								"standardBuildWidget",
								"standardBuildChildren",
								"fileSelectionDialogFindInternalChild");
		registerWidgetBuildData("Fixed",
								"standardBuildWidget",
								"buildLayoutChildren",
								null);
		registerWidgetBuildData("FontSelection", defaultBuildWidgetData);
		registerWidgetBuildData("FontSelectionDialog",
								"standardBuildWidget",
								"standardBuildChildren",
								"fontSelectionDialogFindInternalChild");
		registerWidgetBuildData("Frame",	
								"standardBuildWidget",
								"buildFrameChildren",
								null);
	registerWidgetBuildData("HBox", defaultBuildContainerData);
		registerWidgetBuildData("HPaned",
								"standardBuildWidget",
								"buildPanedChildren",
								null);
		registerWidgetBuildData("HSeparator", defaultBuildWidgetData);
		registerWidgetBuildData("Label", defaultBuildWidgetData);
		registerWidgetBuildData("Layout",
								"standardBuildWidget",
								"buildLayoutChildren",
								null);
		registerWidgetBuildData("List", defaultBuildWidgetData);
		registerWidgetBuildData("ListItem", defaultBuildWidgetData);
		registerWidgetBuildData("Menu", defaultBuildWidgetData);
		registerWidgetBuildData("MenuBar", defaultBuildWidgetData);
		registerWidgetBuildData("MenuItem",
								"standardBuildWidget",
								"buildMenuItemChildren",
								null);
		registerWidgetBuildData("MessageDialog",
								"standardBuildWidget",
								"standardBuildChildren",
								null);
		registerWidgetBuildData("Notebook",
								"standardBuildWidget",
								"buildNotebookChildren",
								null);
		registerWidgetBuildData("OptionMenu",
								"standardBuildWidget",
								"buildOptionMenuChildren",
								null);
		registerWidgetBuildData("Progress", defaultBuildWidgetData);
		registerWidgetBuildData("ProgressBar", defaultBuildWidgetData);
		registerWidgetBuildData("RadioButton", defaultBuildContainerData);
		registerWidgetBuildData("RadioMenuItem",
								"standardBuildWidget",
								"buildMenuItemChildren",
								null);
		registerWidgetBuildData("ScrolledWindow",
								"standardBuildWidget",
								"standardBuildChildren",
								"scrolledWindowFindInternalChild");
		registerWidgetBuildData("SeparatorMenuItem", defaultBuildWidgetData);
		registerWidgetBuildData("SpinButton", defaultBuildWidgetData);
		registerWidgetBuildData("StatusBar", defaultBuildWidgetData);
		registerWidgetBuildData("Table",
								"standardBuildWidget",
								"buildTableChildren",
								null);
		registerWidgetBuildData("TeaoffMenuItem", defaultBuildWidgetData);
		registerWidgetBuildData("Text", defaultBuildWidgetData);
		registerWidgetBuildData("TextView", defaultBuildWidgetData);
		registerWidgetBuildData("ToggleButton", defaultBuildContainerData);
		registerWidgetBuildData("ToolBar",
								"standardBuildWidget",
								"buildToolBarChildren",
								null);
		registerWidgetBuildData("Tree", defaultBuildWidgetData);
		registerWidgetBuildData("TreeView", defaultBuildWidgetData);
		registerWidgetBuildData("VBox", defaultBuildContainerData);
		registerWidgetBuildData("VPaned",
								"standardBuildWidget",
								"buildPanedChildren",
								null);
		registerWidgetBuildData("VSeparator", defaultBuildWidgetData);
		registerWidgetBuildData("ViewPort", defaultBuildContainerData);
		registerWidgetBuildData("Window",
								"standardBuildWidget",
								"standardBuildChildren",
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
			try {
				if (setterName.startsWith("set")) {
					setter = WidgetOperation.findMethod(setterName);
				} else { // assume that the setter name is field name.
					setter = findSetterByFieldName(clazz, setterName);
				}
			} catch (Exception e) {
				logger.warn(e);
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

	public WidgetBuildData getBuildData(String genericClassName) {
		return (WidgetBuildData)builderMap.get(genericClassName);
	}

	public WidgetBuildData getBuildData(Class clazz) {
		Iterator i = classMap.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry e = (Map.Entry)i.next();
			Class value = (Class)e.getValue();
			if (clazz == value) {
				return getBuildData((String)e.getKey());
			}
		}
		throw new IllegalArgumentException();
	}
	
	//--------------------------------------------------------------------

	public Container buildWidget(WidgetInfo info) {
		Container widget;
		String genericClassName = info.getClassName();
		WidgetBuildData data = getBuildData(genericClassName);
		if (data == null) {
			logger.warn("Unknown widget class {0}", genericClassName);
			widget = new JLabel("[a " + genericClassName + "]");
		} else {
			try {
				widget = data.build(this, info);
			} catch (Exception e) {
				logger.warn(e);
				widget = new JLabel("[" + e.toString() + "]");
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

	public Container standardBuildWidget(WidgetInfo info) {
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
		} catch (Exception e) {
			logger.warn(e);
			return null;
		}
	}

	public Container buildPreview(WidgetInfo info) {
		logger.warn("not implemented");
		return null;
	}

	//--------------------------------------------------------------------

	public void standardBuildChildren(Container parent, WidgetInfo info) {
		Iterator i = info.getChildren().iterator();
		while (i.hasNext()) {
			ChildInfo cInfo = (ChildInfo)i.next();
			if (cInfo.getInternalChild() != null) {
				handleInternalChild(parent, cInfo);
			}
			Container child = buildWidget(cInfo.getWidgetInfo());
			if (parent instanceof JWindow) {
				parent = ((JWindow)parent).getContentPane();
			}
			parent.add(child);
		}
	}

	public void buildMenuItemChildren(Container parent, WidgetInfo info) {
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

	public void buildDialogChildren(Container parent, WidgetInfo info) {
		standardBuildChildren(parent, info);
	}


	public void buildFrameChildren(Container parent, WidgetInfo info) {
		int FRAME_ITEM = 0;
		int LABEL_ITEM = 1;
		int cCount = info.getChildrenCount();
		if (cCount != 2) {
			throw new IllegalStateException("there should really only be 2 children");
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
				if ("type".equals(pName) && "label_item".equals(pValue)) {
					type = LABEL_ITEM;
				} else if ("label".equals(pName)) {
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

	public void buildNotebookChildren(Container parent, WidgetInfo info) {
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
				if ("type".equals(pName)) {
					String value = p.getValue();
					if ("tab".equals(value)) {
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

	public void buildOptionMenuChildren(Container parent, WidgetInfo info) {
		int history = 0;
		int cCount = info.getChildrenCount();
		
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();

			if ("Menu".equals(wInfo.getClassName())) {
				logger.warn("the child of the option menu {0} was not a Menu widget", wInfo.getName());
				continue;
			}
			Container child = buildWidget(wInfo);

			JMenu menu = (JMenu)parent;
			menu.add(child);
		}
	}

	public void buildCListChildren(Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Container child = null;

			/* treat Labels specially */
			if ("Label".equals(wInfo.getClassName())) {
				String label = null;
				int pCount = wInfo.getPropertiesCount();
				for (int j = 0; j < pCount; j++) {
					Property p = wInfo.getProperty(j);
					if ("label".equals(p.getName())) {
						label = p.getValue();
						break;
					} else {
						logger.warn("Unknown CList child property: {0}", p.getName());
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

	public void buildToolbarChildren(Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		JToolBar toolBar = (JToolBar)parent;
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Container child = null;
			String className = wInfo.getClassName();
			if ("toggle".equals(className) ||
				"radio".equals(className) ||
				"button".equals(className)) {
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
					
					if ("label".equals(pName)) {
						label = pValue;
					} else if ("use_stock".equals(pName)) {
						useStock = true;
					} else if ("icon".equals(pName)) {
						stock = null;
						icon = xml.relativeFile(pValue);
					} else if ("stock_pixmap".equals(pName)) {
						icon = null;
						stock = pValue;
					} else if ("active".equals(pName)) {
						active = ParameterConverter.toBoolean(pValue);
					} else  if ("group".equals(pName)) {
						groupName = pValue;
					} else if ("new_group".equals(pName)) {
						newGroup = ParameterConverter.toBoolean(pValue);
					} else if ("visible".equals(pName)) {
						/* ignore for now */
					} else if ("tooltip".equals(pName)) {
						/* ignore for now */
					} else if ("use_underline".equals(pName)) {
						/* useUnderline = ParameterConverter.toBoolean(pValue); */
					} else if ("inconsistent".equals(pName)) {
						/* ignore for now */
					} else {
						logger.warn("Unknown Toolbar child property: {0}", pName);
					}
				}

				/* ignore stock icon stuff */
				if (newGroup) {
					toolBar.addSeparator();
				}

				if ("toggle".equals(className)) {
					child = new JToggleButton(label);
					toolBar.add(child);
					child.setEnabled(active);
				} else if ("radio".equals(className)) {
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

	public void buildPanedChildren(Container parent, WidgetInfo info) {
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
			
			if ("resize".equals(name)) {
				resize = ParameterConverter.toBoolean(value);
			} else if ("shrink".equals(name)) {
				shrink = ParameterConverter.toBoolean(value);
			} else {
				logger.warn("Unknown Paned child property: {0}", name);
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
			
			if ("resize".equals(name)) {
				resize = ParameterConverter.toBoolean(value);
			} else if ("shrink".equals(name)) {
				shrink = ParameterConverter.toBoolean(value);
			} else {
				logger.warn("Unknown Paned child property: {0}", name);
			}
		}

		pane.setRightComponent(child); // resize and shrink are ignored.
	}

//		registerProperty("left_attach", "setTableLeftAttach");
//		registerProperty("right_attach", "setTableRightAttach");
//		registerProperty("top_attach", "setTableTopAttach");
//		registerProperty("bottom_attach", "setTableBottomAttach");

	public void buildTableChildren(Container parent, WidgetInfo info) {
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

				if ("left_attach".equals(name)) {
					tc.leftAttach = ParameterConverter.toInteger(value);
				} else if ("right_attach".equals(name)) {
					tc.rightAttach = ParameterConverter.toInteger(value);
				} else if ("top_attach".equals(name)) {
					tc.topAttach = ParameterConverter.toInteger(value);
				} else if ("bottom_attach".equals(name)) {
					tc.bottomAttach = ParameterConverter.toInteger(value);
				} else if ("x_options".equals(value)) {
					// x_options = ParameterConverter.toInteger(value);
				} else if ("y_options".equals(value)) {
					// y_options = ParameterConverter.toInteger(value);
				} else {
					logger.warn("unknown child packing property {0} for Table", name);
				}
			}
			parent.add(child);
			tl.setConstraints(child, tc);
		}
	}
	
	public void buildLayoutChildren(Container parent, WidgetInfo info) {
		int cCount = info.getChildrenCount();
		for (int i = 0; i < cCount; i++) {
			ChildInfo cInfo = info.getChild(i);
			WidgetInfo wInfo = cInfo.getWidgetInfo();
			Container child = null;
			int x = 0, y = 0;

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

				if ("x".equals(name)) {
					x = ParameterConverter.toInteger(value);
				} else if ("y".equals(value)) {
					y = ParameterConverter.toInteger(value);
				} else {
					logger.warn("unknown child packing property {0} for Layout", name);
				}
			}
			parent.add(child);
			child.setLocation(x, y);
		}
	}

	//--------------------------------------------------------------------

	public void handleInternalChild(Container parent, ChildInfo cInfo) {
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
			logger.warn("could not find a parent that handles internal children for `{0}'",
				 cInfo.getInternalChild());
			return;
		}

		child = parentBuildData.findInternalChild(this, parent, cInfo.getInternalChild());

		if (child == null) {
			logger.warn("could not find internal child `{0}' in parent of type `{1}'",
					new Object[] { cInfo.getInternalChild(), parent.getClass() });
			return;
		}

		WidgetInfo wInfo = cInfo.getWidgetInfo();
		setProperties(child, wInfo);
		setCommonParams(child, wInfo);
	}
	protected void setProperties(Container widget, WidgetInfo info) {
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
					logger.info("{0} is ignored in Java.", pName);
				}
			} catch (Exception e) {
				logger.warn(e);
			}
		}
	}

	protected Method findSetterByFieldName(Class clazz, String field)
		throws IllegalAccessException, NoSuchFieldException {
		BeanInfo bInfo = null;
		try {
			bInfo = Introspector.getBeanInfo(clazz, Introspector.USE_ALL_BEANINFO);
		} catch (IntrospectionException e) {
			logger.warn(e);
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
			throw new NoSuchFieldException("unknown property " + field + " for " + clazz);
		}
		Method setter = pDesc.getWriteMethod();
		if (setter == null) {
			throw new IllegalAccessException(field + " of " + clazz + " is read only.");
		}
		return setter;
	}

	public void setCommonParams(Container widget, WidgetInfo info) {
		addSignals(widget, info);
		addAccels(widget, info);
		widget.setName(info.getName());
		String className = info.getClassName();
		WidgetBuildData data = (WidgetBuildData)builderMap.get(className);
		if (data.hasBuildChildrenMethod() && info.getChildrenCount() > 0) {
			data.buildChildren(this, widget, info);
		}
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
					throw new UnsupportedOperationException("not implemented yet.");
				}
			});
		}
	}
}
