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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
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
import org.montsuqi.widgets.NumberEntry;
import org.montsuqi.widgets.PandaEntry;
import org.montsuqi.widgets.PandaHTML;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.Table;
import org.montsuqi.widgets.TableConstraints;
import org.montsuqi.widgets.TableLayout;

class WidgetOperation {

	static Logger logger = Logger.getLogger(WidgetOperation.class);

	static Method findMethod(String name) {
		try {
			Class[] argTypes = new Class[] { Interface.class,
											 Container.class,
											 String.class,
											 String.class };
			return WidgetOperation.class.getDeclaredMethod(name, argTypes);
		} catch (SecurityException e) {
			Logger.getLogger(WidgetOperation.class).fatal(e);
			throw new WidgetOperationException(e);
		} catch (NoSuchMethodException e) {
			Logger.getLogger(WidgetOperation.class).fatal(e);
			throw new WidgetOperationException(e);
		}
	}

	static void setVisible(Interface xml, Container widget, String name, String value) {
		boolean v = ParameterConverter.toBoolean(value);
		widget.setVisible(v);
	}

	static void setEditable(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JTextComponent)) {
			return;
		}
		JTextComponent text = (JTextComponent)widget;
		boolean v = ParameterConverter.toBoolean(value);
		text.setEditable(v);
	}

	static void setWindowTitle(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof Frame)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Frame_widget")); //$NON-NLS-1$
		}
		Frame frame = (Frame)widget;
		frame.setTitle(value);
	}

	static void setWidth(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof Component)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.attempt_to_set_width_to_non_component")); //$NON-NLS-1$
		}
		Component c = (Component)widget;
		Dimension size = c.getSize();
		try {
			size.width = Integer.parseInt(value);
			c.setSize(size);
			
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_number")); //$NON-NLS-1$
		}
	}

	static void setHeight(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof Component)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.attempt_to_set_height_to_non_component")); //$NON-NLS-1$
		}
		Component c = (Component)widget;
		Dimension size = c.getSize();
		try {
			size.height = Integer.parseInt(value);
			c.setSize(size);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_number")); //$NON-NLS-1$
		}
	}

	static void setJustify(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JLabel)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JLabel_widget")); //$NON-NLS-1$
		}
		int alignment = SwingConstants.CENTER;
		if ("JUSTIFY_CENTER".equals(value)) { //$NON-NLS-1$
			alignment = SwingConstants.CENTER;
		} else if ("JUSTIFY_LEFT".equals(value)) { //$NON-NLS-1$
			alignment = SwingConstants.LEFT;
		} else if ("JUSTIFY_RIGHT".equals(value)) { //$NON-NLS-1$
			alignment = SwingConstants.RIGHT;
		} else {
			Logger.getLogger(WidgetOperation.class).warn(Messages.getString("WidgetOperation.not_supported")); //$NON-NLS-1$
		}
		JLabel label = (JLabel)widget;
		label.setHorizontalAlignment(alignment);
	}

	static void setTooltip(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JComponent)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JComponent_widget")); //$NON-NLS-1$
		}
		JComponent c = (JComponent)widget;
		c.setToolTipText(value);
	}

	static void setHasDefault(Interface xml, Container widget, String name, String value) {
		if (ParameterConverter.toBoolean(value)) {
			xml.setDefaultWidget(widget);
		}
	}

	static void setHasFocus(Interface xml, Container widget, String name, String value) {
		if (ParameterConverter.toBoolean(value)) {
			xml.setFocusWidget(widget);
		}
	}

	static void setListItemLabel(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JLabel)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JLabel_widget")); //$NON-NLS-1$
		}
		JLabel label = (JLabel)widget;
		label.setText(value);
	}

	void setTextViewText(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JTextComponent)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JTextComponent_widget")); //$NON-NLS-1$
		}
		JTextComponent text = (JTextComponent)widget;
		text.setText(value);
	}

	static void setCListColumnWidth(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JTable)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JTable_widget")); //$NON-NLS-1$
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

	static void setCListSelectionMode(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JTable)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JTable_widget")); //$NON-NLS-1$
		}
		JTable table = (JTable)widget;
		value = normalizeSelectionMode(value);
		if ("SINGLE".equals(value)) { //$NON-NLS-1$
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		} else if ("MULTIPLE".equals(value)) { //$NON-NLS-1$
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		} else if ("EXTENDED".equals(value)) { //$NON-NLS-1$
			logger.info(Messages.getString("WidgetOperation.setCListSelectionMode_EXTENDED_is_not_supported")); //$NON-NLS-1$
		} else if ("BROWSE".equals(value)) { //$NON-NLS-1$
			logger.info(Messages.getString("WidgetOperation.setCListSelectionMode_BROWSE_is_not_supported")); //$NON-NLS-1$
		} else {
			throw new IllegalArgumentException(value);
		}
	}

	static void setCListShadowType(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setCListShadowType_is_not_supported")); //$NON-NLS-1$
	}

	static void setCListShowTitles(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JTable)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JTable_widget")); //$NON-NLS-1$
		}
		JTable table = (JTable)widget;
		JTableHeader header = table.getTableHeader();
		header.setVisible(ParameterConverter.toBoolean(value));
	}

	static void setTreeSelectionMode(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JTree)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JTree_widget")); //$NON-NLS-1$
		}
		JTree tree = (JTree)widget;
		TreeSelectionModel model = tree.getSelectionModel();
		value = normalizeSelectionMode(value);
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

	static void setTreeViewMode(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setTreeViewMode_is_not_supported")); //$NON-NLS-1$
	}

	static void setTreeViewLine(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setTreeViewLine_is_not_supported")); //$NON-NLS-1$
	}

	static void setListSelectionMode(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JList)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JList_widget")); //$NON-NLS-1$
		}
		JList list = (JList)widget;
		ListSelectionModel model = list.getSelectionModel();
		value = normalizeSelectionMode(value);
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

	private static String normalizeSelectionMode(String mode) {
		if (mode.startsWith("GTK_")) { //$NON-NLS-1$
			mode = mode.substring("GTK_".length()); //$NON-NLS-1$
		}
		if (mode.startsWith("SELECTION_")) { //$NON-NLS-1$
			mode = mode.substring("SELECTION_".length()); //$NON-NLS-1$
		}
		return mode;
	}

	static void setCheckMenuItemAlwaysShowToggle(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setCheckMenuItemAlwaysShowToggle_is_not_supported")); //$NON-NLS-1$
	}

	static void setTextText(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JTextComponent)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_JTextComponent_widget")); //$NON-NLS-1$
		}
		JTextComponent text = (JTextComponent)widget;
		text.setText(value);
	}

	static void setRadioMenuItemGroup(Interface xml, Container widget, String name, String value) {
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

	static void setToolbarTooltips(Interface xml, Container widget, String name, String value) {
		ToolTipManager manager = ToolTipManager.sharedInstance();
		if ( ! (widget instanceof JToolBar)) {
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
	
	static void setStatusBarHasResizeGrip(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setStatusBarResizeGrip_is_not_supported")); //$NON-NLS-1$
	}

	static void setRulerMetric(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setRulerMetric_is_not_supported")); //$NON-NLS-1$
	}

	static void setMenuItemLabel(Interface xml, Container widget, String name, String value) {
		JMenuItem item = (JMenuItem)widget;
		item.setText(value);
	}

	static void setMenuItemUseUnderline(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setMenuItemUseUnderline_is_not_supported")); //$NON-NLS-1$
	}

	static void setMenuItemUseStock(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setMenuItemUseStock_is_not_supported")); //$NON-NLS-1$
	}

	static void setWindowWMClassName(Interface xml, Container widget, String name, String value) {
		Logger.getLogger(WidgetOperation.class).info(Messages.getString("WidgetOperation.setWindowWMClassName_is_not_supported")); //$NON-NLS-1$
	}
	
	static void setEntryInvisibleChar(Interface xml, Container widget, String name, String value) {
		if ( !(widget instanceof JPasswordField)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Password_field")); //$NON-NLS-1$
		}
		JPasswordField password = (JPasswordField)widget;
		password.setEchoChar(value.charAt(0));
	}

	static void setTableLeftAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Table_widget")); //$NON-NLS-1$
		}
		
		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.leftAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	static void setTableRightAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.rightAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	static void setTableTopAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.topAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	static void setTableBottomAttach(Interface xml, Container widget, String name, String value) {
		Container parent = widget.getParent();
		if ( ! (parent instanceof Table)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_Table_widget")); //$NON-NLS-1$
		}

		TableLayout layout = (TableLayout)parent.getLayout();
		TableConstraints tc = new TableConstraints(layout.getConstraints(widget));
		tc.bottomAttach = ParameterConverter.toInteger(value);
		layout.setConstraints(widget, tc);
	}

	static void setNumberEntryFormat(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof NumberEntry)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_NumberEntry_widget")); //$NON-NLS-1$
		}
		NumberEntry entry = (NumberEntry)widget;
		entry.setFormat(value);		
	}

	static void setPandaHTMLURI(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof PandaHTML)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_PandaHTML_widget")); //$NON-NLS-1$
		}
		PandaHTML pane = (PandaHTML)widget;
		pane.setURI(value);
	}

	static void setPandaEntryInputMode(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof PandaEntry)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_PandaEntry_widget")); //$NON-NLS-1$
		}
		PandaEntry entry = (PandaEntry)widget;
		if (value.equals("ASCII")) { //$NON-NLS-1$
			entry.setInputMode(PandaEntry.ASCII);
		} else if (value.equals("KANA")) { //$NON-NLS-1$
			entry.setInputMode(PandaEntry.KANA);
		} else if (value.equals("XIM")) { //$NON-NLS-1$
			entry.setInputMode(PandaEntry.XIM);
		} else {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.invalide_input_mode")); //$NON-NLS-1$
		}
	}

	static void setPandaEntryXIMEnabled(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof PandaEntry)) {
			throw new IllegalArgumentException(Messages.getString("WidgetOperation.not_a_PandaEntry_widget")); //$NON-NLS-1$
		}
		PandaEntry entry = (PandaEntry)widget;
		boolean enabled = ParameterConverter.toBoolean(value);
		entry.setXIMEnabled(enabled);
	}

	static void setPandaTimerDuration(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof PandaTimer)) {
			throw new IllegalArgumentException("not a PandaTimer widget");
		}
		PandaTimer timer = (PandaTimer)widget;
		int duration = ParameterConverter.toInteger(value);
		timer.setDuration(duration);
	}

	static void setProgressBarLower(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException("not a ProgressBar widget");
		}
		JProgressBar progress = (JProgressBar)widget;
		BoundedRangeModel model = progress.getModel();
		model.setMinimum(ParameterConverter.toInteger(value));
	}

	static void setProgressBarUpper(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException("not a ProgressBar widget");
		}
		JProgressBar progress = (JProgressBar)widget;
		BoundedRangeModel model = progress.getModel();
		model.setMaximum(ParameterConverter.toInteger(value));
	}

	static void setProgressBarValue(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException("not a ProgressBar widget");
		}
		JProgressBar progress = (JProgressBar)widget;
		BoundedRangeModel model = progress.getModel();
		model.setValue(ParameterConverter.toInteger(value));
	}

	static void setProgressBarOrientation(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException("not a ProgressBar widget");
		}
		JProgressBar progress = (JProgressBar)widget;
		if (value.startsWith("GTK_")) {
			value = value.substring("GTK_".length());
		}
		if (value.startsWith("PROGRESS_")) {
			value = value.substring("PROGRESS_".length());
		}
		if ("LEFT_TO_RIGHT".equals(value)) {
			progress.setOrientation(JProgressBar.HORIZONTAL);
		} else if ("RIGHT_TO_LEFT".equals(value)) {
			progress.setOrientation(JProgressBar.HORIZONTAL);
		} else if ("TOP_TO_BOTTOM".equals(value)) {
			progress.setOrientation(JProgressBar.VERTICAL);
		} else if ("BOTTOM_TO_TOP".equals(value)) {
			progress.setOrientation(JProgressBar.VERTICAL);
		}
	}

	static void setProgressBarActivityMode(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException("not a ProgressBar widget");
		}
		JProgressBar progress = (JProgressBar)widget;
		boolean indeterminate = ParameterConverter.toBoolean(value);
		progress.setIndeterminate(indeterminate);
	}

	static void setProgressBarShowText(Interface xml, Container widget, String name, String value) {
		if ( ! (widget instanceof JProgressBar)) {
			throw new IllegalArgumentException("not a ProgressBar widget");
		}
		JProgressBar progress = (JProgressBar)widget;
		boolean showText = ParameterConverter.toBoolean(value);
		progress.setStringPainted(showText);
	}
}
