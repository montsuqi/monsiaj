package org.montsuqi.monsia;

import java.awt.Container;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
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

	public static void setJustify(Interface xml, Container widget, String name, String value) {
		int alignment = SwingConstants.CENTER;
		if ("JUSTIFY_CENTER".equals(value)) {
			alignment = SwingConstants.CENTER;
		} else if ("JUSTIFY_LEFT".equals(value)) {
			alignment = SwingConstants.LEFT;
		} else if ("JUSTIFY_RIGHT".equals(value)) {
			alignment = SwingConstants.RIGHT;
		} else {
			Logger.getLogger(WidgetOperation.class).warn("not supported");
		}
		if (widget instanceof JLabel) {
			((JLabel)widget).setHorizontalAlignment(alignment);
		}
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
		
		StringTokenizer tokens = new StringTokenizer(value, ","); //$NON-NLS-1$
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
		if ("SINGLE".equals(value)) { //$NON-NLS-1$
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} else if ("MULTIPLE".equals(value)) { //$NON-NLS-1$
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if ("EXTENDED".equals(value)) { //$NON-NLS-1$
			Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setCListSelectionMode_EXTENDED_is_not_supported")); //$NON-NLS-1$
		} else if ("BROWSE".equals(value)) { //$NON-NLS-1$
			Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setCListSelectionMode_BROWSE_is_not_supported")); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	public static void setCListShadowType(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setCListShadowType_is_not_supported")); //$NON-NLS-1$
	}

	public static void setCListShowTitles(Interface xml, Container widget, String name, String value) {
		JTable table = (JTable)widget;
		JTableHeader header = table.getTableHeader();
		header.setVisible(ParameterConverter.toBoolean(value));
	}

	public static void setTreeSelectionMode(Interface xml, Container widget, String name, String value) {
		JTree tree = (JTree)widget;
		TreeSelectionModel model = tree.getSelectionModel();
		if ("SINGLE".equals(value)) { //$NON-NLS-1$
			model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		} else if ("MULTIPLE".equals(value)) { //$NON-NLS-1$
			model.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		} else if ("EXTENDED".equals(value)) { //$NON-NLS-1$
			Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setTreeSelectionMode_EXTENDED_is_not_supported")); //$NON-NLS-1$
		} else if ("BROWSE".equals(value)) { //$NON-NLS-1$
			Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setTreeSelectionMode_BROWSE_is_not_supported")); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	public static void setTreeViewMode(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setTreeViewMode_is_not_supported")); //$NON-NLS-1$
	}

	public static void setTreeViewLine(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setTreeViewLine_is_not_supported")); //$NON-NLS-1$
	}

	public static void setListSelectionMode(Interface xml, Container widget, String name, String value) {
		JList list = (JList)widget;
		ListSelectionModel model = list.getSelectionModel();
		if ("SINGLE".equals(value)) { //$NON-NLS-1$
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} else if ("MULTIPLE".equals(value)) { //$NON-NLS-1$
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if ("EXTENDED".equals(value)) { //$NON-NLS-1$
			Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setListSelectionMode_EXTENDED_is_not_supported")); //$NON-NLS-1$
		} else if ("BROWSE".equals(value)) { //$NON-NLS-1$
			Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setListSelectionMode_BROWSE_is_not_supported")); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	public static void setCheckMenuItemAlwaysShowToggle(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setCheckMenuItemAlwaysShowToggle_is_not_supported")); //$NON-NLS-1$
	}

	public static void setTextText(Interface xml, Container widget, String name, String value) {
		JTextComponent text = (JTextComponent)widget;
		text.setText(value);
	}

	public static void setRadioMenuItemGroup(Interface xml, Container widget, String name, String value) {
		ButtonGroup group;
		group = xml.getButtonGroup(value);

		if (group == null) {
			Logger.getLogger(WidgetOperation.class).warn(Messages.getString("WidgetOperation.Radio_button_group_n_could_not_be_found"), value); //$NON-NLS-1$
			return;
		}

		if ((Object)group == (Object)widget) {
			Logger.getLogger(WidgetOperation.class).warn("Group is self, skipping."); //$NON-NLS-1$
			return;
		}

		group.add((AbstractButton)widget);
	}

	public static void setToolbarTooltips(Interface xml, Container widget, String name, String value) {
		ToolTipManager manager = ToolTipManager.sharedInstance();
		if ( !(widget instanceof JToolBar)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JToolBar")); //$NON-NLS-1$
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
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setStatusBarResizeGrip_is_not_supported")); //$NON-NLS-1$
	}

	public static void setRulerMetric(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setRulerMetric_is_not_supported")); //$NON-NLS-1$
	}

	public static void setMenuItemLabel(Interface xml, Container widget, String name, String value) {
		JMenuItem item = (JMenuItem)widget;
		item.setText(value);
	}

	public static void setMenuItemUseUnderline(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setMenuItemUseUnderline_is_not_supported")); //$NON-NLS-1$
	}

	public static void setMenuItemUseStock(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setMenuItemUseStock_is_not_supported")); //$NON-NLS-1$
	}

	public static void setWindowWMClassName(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setWindowWMClassName_is_not_supported")); //$NON-NLS-1$
	}
	
	public static void setEntryInvisibleChar(Interface xml, Container widget, String name, String value) {
		if ( !(widget instanceof JPasswordField)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Password_field")); //$NON-NLS-1$
		}
		JPasswordField password = (JPasswordField)widget;
		password.setEchoChar(value.charAt(0));
	}

	public static void setTableLeftAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Table_widget")); //$NON-NLS-1$
		}
		
		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.leftAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	public static void setTableRightAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.rightAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	public static void setTableTopAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.topAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	public static void setTableBottomAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.bottomAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}
}

