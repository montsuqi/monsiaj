package org.montsuqi.monsia;

import java.awt.Container;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeSelectionModel;
import org.montsuqi.util.Logger;
import org.montsuqi.util.ParameterConverter;
import org.montsuqi.widgets.Table;
import org.montsuqi.widgets.TableConstraints;
import org.montsuqi.widgets.TableLayout;

public class WidgetOperation {

	public static Method findMethod(String name) {
		try {
			Class[] argTypes = new Class[] { Interface.class,
											 Container.class,
											 String.class,
											 String.class };
			return WidgetOperation.class.getMethod(name, argTypes);
		} catch (Exception e) {
			Logger.getLogger(WidgetOperation.class).info(e);
			return null;
		}
	}

	public static void setVisible(Interface xml, Container widget, String name, String value) {
		boolean v = ParameterConverter.toBoolean(value);
		widget.setVisible(v);
	}

	public static void setTooltip(Interface xml, Container widget, String name, String value) {
		if (widget instanceof JComponent) {
			((JComponent)widget).setToolTipText(value);
		}
	}

	public static void setHasDefault(Interface xml, Container widget, String name, String value) {
		if (ParameterConverter.toBoolean(value)) {
			xml.setDefaultWidget(widget);
		}
	}

	public static void setHasFocus(Interface xml, Container widget, String name, String value) {
		if (ParameterConverter.toBoolean(value)) {
			xml.setFocusWidget(widget);
		}
	}

	public static void setListItemLabel(Interface xml, Container widget, String name, String value) {
		((JLabel)widget).setText(value);
	}

	public static void setTextViewText(Interface xml, Container widget, String name, String value) {
		if (widget instanceof JTextComponent) {
			((JTextComponent)widget).setText(value);
		}
	}

	public static void setCListColumnWidth(Interface xml, Container widget, String name, String value) {
		TableColumnModel model = ((JTable)widget).getColumnModel();
		
		StringTokenizer tokens = new StringTokenizer(value, ",");
		int col = 0;
		while (tokens.hasMoreTokens()) {
			int width = ParameterConverter.toInteger(tokens.nextToken());
			TableColumn column = model.getColumn(col);
			column.setPreferredWidth(width);
			col++;
		}
	}

	public static void setCListSelectionMode(Interface xml, Container widget, String name, String value) {
		JTable table = (JTable)widget;
		ListSelectionModel model = table.getSelectionModel();
		if ("SINGLE".equals(value)) {
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} else if ("MULTIPLE".equals(value)) {
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if ("EXTENDED".equals(value)) {
			Logger.getLogger(WidgetOperation.class).info("setCListSelectionMode(EXTENDED) is not supported.");
		} else if ("BROWSE".equals(value)) {
			Logger.getLogger(WidgetOperation.class).info("setCListSelectionMode(BROWSE) is not supported.");
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	public static void setCListShadowType(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setCListShadowType is not supported.");
	}

	public static void setCListShowTitles(Interface xml, Container widget, String name, String value) {
		JTable table = (JTable)widget;
		JTableHeader header = table.getTableHeader();
		header.setVisible(ParameterConverter.toBoolean(value));
	}

	public static void setTreeSelectionMode(Interface xml, Container widget, String name, String value) {
		JTree tree = (JTree)widget;
		TreeSelectionModel model = tree.getSelectionModel();
		if ("SINGLE".equals(value)) {
			model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		} else if ("MULTIPLE".equals(value)) {
			model.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		} else if ("EXTENDED".equals(value)) {
			Logger.getLogger(WidgetOperation.class).info("setTreeSelectionMode(EXTENDED) is not supported.");
		} else if ("BROWSE".equals(value)) {
			Logger.getLogger(WidgetOperation.class).info("setTreeSelectionMode(BROWSE) is not supported.");
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	public static void setTreeViewMode(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setTreeViewMode is not supported.");
	}

	public static void setTreeViewLine(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setTreeViewLine is not supported.");
	}

	public static void setListSelectionMode(Interface xml, Container widget, String name, String value) {
		JList list = (JList)widget;
		ListSelectionModel model = list.getSelectionModel();
		if ("SINGLE".equals(value)) {
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} else if ("MULTIPLE".equals(value)) {
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if ("EXTENDED".equals(value)) {
			Logger.getLogger(WidgetOperation.class).info("setListSelectionMode(EXTENDED) is not supported.");
		} else if ("BROWSE".equals(value)) {
			Logger.getLogger(WidgetOperation.class).info("setListSelectionMode(BROWSE) is not supported.");
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	public static void setCheckMenuItemAlwaysShowToggle(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setCheckMenuItemAlwaysShowToggle is not supported.");
	}

	public static void setTextText(Interface xml, Container widget, String name, String value) {
		JTextComponent text = (JTextComponent)widget;
		text.setText(value);
	}

	public static void setRadioMenuItemGroup(Interface xml, Container widget, String name, String value) {
		ButtonGroup group;
		group = xml.getButtonGroup(value);

		if (group == null) {
			Logger.getLogger(WidgetOperation.class).warn("Radio button group {0} could not be found", value);
			return;
		}

		if ((Object)group == (Object)widget) {
			Logger.getLogger(WidgetOperation.class).warn("Group is self, skipping.");
			return;
		}

		group.add((AbstractButton)widget);
	}

	public static void setToolbarTooltips(Interface xml, Container widget, String name, String value) {
		ToolTipManager manager = ToolTipManager.sharedInstance();
		if ( !(widget instanceof JToolBar)) {
			throw new IllegalArgumentException("not a JToolBar");
		}

		if (widget instanceof JComponent) {
			if (ParameterConverter.toBoolean(value)) {
				manager.registerComponent((JComponent)widget);
			} else {
				manager.unregisterComponent((JComponent)widget);
			}
		}
	}
	
	public static void setStatusBarHasResizeGrip(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setStatusBarResizeGrip is not supported.");
	}

	public static void setRulerMetric(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setRulerMetric is not supported.");
	}

	public static void setMenuItemLabel(Interface xml, Container widget, String name, String value) {
		JMenuItem item = (JMenuItem)widget;
		item.setText(value);
	}

	public static void setMenuItemUseUnderline(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setMenuItemUseUnderline is not supported.");
	}

	public static void setMenuItemUseStock(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setMenuItemUseStock is not supported.");
	}

	public static void setWindowWMClassName(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info("setWindowWMClassName is not supported.");
	}
	
	public static void setEntryInvisibleChar(Interface xml, Container widget, String name, String value) {
		if ( !(widget instanceof JPasswordField)) {
			throw new IllegalArgumentException("not a Password field.");
		}
		JPasswordField password = (JPasswordField)widget;
		password.setEchoChar(value.charAt(0));
	}

	public Container dialogFindInternalChild(Container parent, String childName) {
		JDialog dialog = (JDialog)parent;
		if ("vbox".equals(childName)) {
			return parent;
		}
		if ("action_area".equals(childName)) {
			return parent;
		}
		return null;
	}

	public Container imageMenuFindInternalChild(Container parent, String childName) {
		if ("image".equals(childName)) {
			return parent; // MenuItem itself is an AbstractButton and hase an icon with it.
		} else {
			return null;
		}
	}

	public Container scrolledWindowFindInternalChild(Container parent, String childName) {
		JScrollPane scroll = (JScrollPane)parent;
		if ("vscrollbar".equals(childName)) {
			return scroll.getVerticalScrollBar();
		}
		if ("hscrollbar".equals(childName)) {
			return scroll.getHorizontalScrollBar();
		}
		
		return null;
	}

	public Container fileSelectionDialogFindInternalChild(Container parent, String childName) {
		if ("vbox".equals(childName)) {
			return parent;
		}
		if ("action_area".equals(childName)) {
			return parent;
		}
		if ("ok_button".equals(childName)) {
			return parent;
		}
		if ("cancel_button".equals(childName)) {
			return parent;
		}
		if ("help_button".equals(childName)) {
			return parent;
		}
		return null;
	}

	public Container colorSelectionDialogFindInternalChild(Container parent, String childName) {
		if ("vbox".equals(childName)) {
			return parent;
		}
		if ("action_area".equals(childName)) {
			return parent;
		}
		if ("ok_button".equals(childName)) {
			return parent;
		}
		if ("cancel_button".equals(childName)) {
			return parent;
		}
		if ("help_button".equals(childName)) {
			return parent;
		}
		if ("color_selection".equals(childName)) {
			return parent;
		}
		return null;
	}

	public Container fontSelectionDialogFindInternalChild(Container parent, String childName) {
		if ("vbox".equals(childName)) {
			return parent;
		}
		if ("action_area".equals(childName)) {
			return parent;
		}
		if ("ok_button".equals(childName)) {
			return parent;
		}
		if ("cancel_button".equals(childName)) {
			return parent;
		}
		if ("apply_button".equals(childName)) {
			return parent;
		}
		if ("font_selection".equals(childName)) {
			return parent;
		}
		return null;
	}

	public Container comboFindInternalChild(Container parent, String childName) {
		if ("entry".equals(childName)) {
			return parent;
		}
		if ("button".equals(childName)) {
			return parent;
		}
		if ("popup".equals(childName)) {
			return parent;
		}
		if ("popwin".equals(childName)) {
			return parent;
		}
		if ("list".equals(childName)) {
			return parent;
		}
		return null;
	}

	public static void setTableLeftAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException("not a Table widget");
		}
		
		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.leftAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	public static void setTableRightAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException("not a Table widget");
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.rightAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	public static void setTableTopAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException("not a Table widget");
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.topAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	public static void setTableBottomAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException("not a Table widget");
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.bottomAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}
}

