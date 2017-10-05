/*      PANDA -- a simple transaction monitor

 Copyright (C) 1998-1999 Ogochan.
 2000-2003 Ogochan & JMA (Japan Medical Association).
 2002-2006 OZAWA Sakuro.

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
package org.montsuqi.monsiaj.monsia.builders;

import org.montsuqi.monsiaj.widgets.PandaCList;
import org.montsuqi.monsiaj.widgets.PandaEntry;
import org.montsuqi.monsiaj.widgets.PandaTimer;
import org.montsuqi.monsiaj.widgets.UIStock;
import org.montsuqi.monsiaj.widgets.PandaTable;
import org.montsuqi.monsiaj.widgets.Entry;
import org.montsuqi.monsiaj.widgets.PandaHTML;
import org.montsuqi.monsiaj.widgets.NumberEntry;
import org.montsuqi.monsiaj.widgets.PandaText;
import org.montsuqi.monsiaj.widgets.Window;
import org.montsuqi.monsiaj.widgets.Pixmap;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.util.ParameterConverter;

/**
 * <p>
 * WidgetPropertySetter is a class to help assigning properties to widgets.</p>
 * <p>
 * These setters exist one for a class(and subclass) and property name.</p>
 * <p>
 * When building a widget, a widget builder will look up a suitable setter for
 * each key in provided properties. When such a setter is found, its set method
 * is called to set a certain property of the widget.</p>
 */
abstract class WidgetPropertySetter {

    /**
     * <p>
     * This method does the actual work of setting a property. Subclasses must
     * implement this method.</p>
     *
     * @param xml glade screen definition
     * @param parent parent widget of the target widget.
     * @param widget target widget.
     * @param value the value to set in String.
     */
    abstract void set(Interface xml, Container parent, Component widget, String value);
    protected static final Logger logger = LogManager.getLogger(WidgetPropertySetter.class);

    protected void warnUnsupportedProperty(String value) {
        logger.debug("not supported: {0}", value);
    }
    private static Map<Class, Map<String, WidgetPropertySetter>> propertyMap;
    private static final WidgetPropertySetter nullWidgetPropertySetter;

    /**
     * <p>
     * Looks up a property setter for given class or its ancestors and property
     * name.</p>
     * <p>
     * When no setter is found, nullWidgetPropertySetter, which does nothing, is
     * returned.</p>
     *
     * @param clazz class of the widget whose property is to be set.
     * @param name the proeprty name to be set.
     * @return a setter.
     */
    static WidgetPropertySetter getSetter(Class clazz, String name) {
        for (/*
                 * 
                 */; clazz != null; clazz = clazz.getSuperclass()) {
            Map map = (Map) propertyMap.get(clazz);
            if (map == null || !map.containsKey(name)) {
                continue;
            }
            WidgetPropertySetter setter = (WidgetPropertySetter) map.get(name);
            if (setter != null) {
                return setter;
            }
        }
        return nullWidgetPropertySetter;
    }

    private static void registerProperty(Class clazz, String propertyName, WidgetPropertySetter setter) {
        if (!propertyMap.containsKey(clazz)) {
            propertyMap.put(clazz, new HashMap<String, WidgetPropertySetter>());
        }
        Map<String, WidgetPropertySetter> map = propertyMap.get(clazz);
        map.put(propertyName, setter);
    }

    static {
        propertyMap = new HashMap<>();

        nullWidgetPropertySetter = new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                // do nothing
            }
        };

        registerProperty(AbstractButton.class, "label", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                AbstractButton button = (AbstractButton) widget;
                button.setText(value.replaceAll("_", ""));
            }
        });

        registerProperty(Component.class, "width_request", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                Dimension size = widget.getSize();
                try {
                    size.width = Integer.parseInt(value);
                    int height = size.height;
                    size.height = height;
                    widget.setSize(size);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("not a number");
                }
            }
        });
        registerProperty(Component.class, "width", getSetter(Component.class, "width_request"));  //$NON-NLS-2$

        registerProperty(Component.class, "height_request", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                Dimension size = widget.getSize();
                try {
                    size.height = Integer.parseInt(value);
                    int width = size.width;
                    size.width = width;
                    widget.setSize(size);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("not a number");
                }
            }
        });
        registerProperty(Component.class, "height", getSetter(Component.class, "height_request"));  //$NON-NLS-2$

        registerProperty(Component.class, "visible", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                boolean visible = ParameterConverter.toBoolean(value);
                widget.setVisible(visible);
            }
        });

        registerProperty(Component.class, "tooltip", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JComponent c = (JComponent) widget;
                c.setToolTipText(value);
            }
        });

        registerProperty(Component.class, "has_default", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                if (ParameterConverter.toBoolean(value)) {
                    xml.setDefaultWidget(widget);
                }
            }
        });

        registerProperty(Component.class, "has_focus", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                if (ParameterConverter.toBoolean(value)) {
                    xml.setFocusWidget(widget);
                }
            }
        });

        registerProperty(AbstractButton.class, "can_focus", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                final boolean flag = ParameterConverter.toBoolean(value);
                widget.setFocusable(flag);
            }
        });

        registerProperty(java.awt.Frame.class, "title", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                java.awt.Frame frame = (java.awt.Frame) widget;
                frame.setTitle(value);
            }
        });

        registerProperty(JLabel.class, "label", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JLabel label = (JLabel) widget;
                value = value.replaceFirst("\\s+\\z", "");  //$NON-NLS-2$
                label.setText(value.contains("\n") ? makeHTML(value) : value);
            }

            // convert multi-line label value into HTML
            private String makeHTML(String value) {
                StringBuilder buf = new StringBuilder("<html>");
                StringTokenizer tokens = new StringTokenizer(value, "\n\"<>&", true);
                while (tokens.hasMoreTokens()) {
                    String token = tokens.nextToken();
                    if ("\n".equals(token)) {
                        buf.append("<br>");
                    } else if ("\"".equals(token)) {
                        buf.append("&dquot;");
                    } else if ("<".equals(token)) {
                        buf.append("&lt;");
                    } else if (">".equals(token)) {
                        buf.append("&gt;");
                    } else if ("&".equals(token)) {
                        buf.append("&amp;");
                    } else {
                        buf.append(token);
                    }
                }
                buf.append("</html>");
                value = buf.toString();
                return value;
            }
        });

        registerProperty(JLabel.class, "xalign", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JLabel label = (JLabel) widget;
                double align = Double.parseDouble(value);
                if (align < 0.5) {
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                } else if (0.5 < align) {
                    label.setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                }
            }
        });

        registerProperty(JLabel.class, "yalign", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JLabel label = (JLabel) widget;
                double align = Double.parseDouble(value);
                if (align < 0.5) {
                    label.setVerticalAlignment(SwingConstants.TOP);
                } else if (0.5 < align) {
                    label.setVerticalAlignment(SwingConstants.BOTTOM);
                } else {
                    label.setVerticalAlignment(SwingConstants.CENTER);
                }
            }
        });

        registerProperty(JList.class, "selection_mode", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JList list = (JList) widget;
                value = normalize(value, "SELECTION_");
                if ("SINGLE".equals(value)) {
                    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                } else if ("MULTIPLE".equals(value)) {
                    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                } else if ("EXTENDED".equals(value)) {
                    warnUnsupportedProperty(value);
                } else if ("BROWSE".equals(value)) {
                    warnUnsupportedProperty(value);
                } else {
                    throw new IllegalArgumentException("invalid selection mode");
                }
            }
        });

        registerProperty(JTextComponent.class, "editable", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JTextComponent text = (JTextComponent) widget;
                final boolean flag = ParameterConverter.toBoolean(value);
                text.setEditable(flag);
                text.setFocusable(flag);
            }
        });

        registerProperty(JTextComponent.class, "can_focus", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JTextComponent text = (JTextComponent) widget;
                final boolean flag = ParameterConverter.toBoolean(value);
                text.setEditable(flag);
                text.setFocusable(flag);
            }
        });

        registerProperty(JTextComponent.class, "text", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JTextComponent text = (JTextComponent) widget;
                text.setText(value);
            }
        });

        registerProperty(PandaText.class, "text", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaText text = (PandaText) widget;
                text.setText(value);
                if (!text.isEditable()) {
                    text.setCaretPosition(0);
                }
            }
        });

        registerProperty(JTextField.class, "justify", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JTextField text = (JTextField) widget;
                value = normalize(value, "JUSTIFY_");
                if ("CENTER".equals(value)) {
                    text.setHorizontalAlignment(SwingConstants.CENTER);
                } else if ("LEFT".equals(value)) {
                    text.setHorizontalAlignment(SwingConstants.LEFT);
                } else if ("RIGHT".equals(value)) {
                    text.setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    warnUnsupportedProperty(value);
                }
            }
        });

        registerProperty(JLabel.class, "wrap", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JLabel label = (JLabel) widget;
                // wrap=true is converted to left alighnemt
                if (ParameterConverter.toBoolean(value)) {
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                }
            }
        });

        registerProperty(JPasswordField.class, "invisible_char", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JPasswordField password = (JPasswordField) widget;
                password.setEchoChar(value.charAt(0));
            }
        });

        registerProperty(NumberEntry.class, "format", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                NumberEntry entry = (NumberEntry) widget;
                entry.setFormat(value);
            }
        });

        registerProperty(PandaEntry.class, "input_mode", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaEntry entry = (PandaEntry) widget;
                if (value.equals("ASCII")) {
                    entry.setInputMode(PandaEntry.ASCII);
                } else if (value.equals("KANA")) {
                    entry.setInputMode(PandaEntry.KANA);
                } else if (value.equals("XIM")) {
                    entry.setInputMode(PandaEntry.XIM);
                } else {
                    throw new IllegalArgumentException("invalid input mode");
                }
            }
        });

        registerProperty(Entry.class, "text_max_length", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                final Entry entry = (Entry) widget;
                final int limit = ParameterConverter.toInteger(value);
                entry.setLimit(limit);
            }
        });

        registerProperty(Entry.class, "max_length", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                final Entry entry = (Entry) widget;
                final int limit = ParameterConverter.toInteger(value);
                entry.setLimit(limit);
            }
        });

        registerProperty(PandaEntry.class, "xim_enabled", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaEntry entry = (PandaEntry) widget;
                entry.setXIMEnabled(ParameterConverter.toBoolean(value));
            }
        });

        registerProperty(PandaText.class, "xim_enabled", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaText text = (PandaText) widget;
                text.setXIMEnabled(ParameterConverter.toBoolean(value));
            }
        });

        registerProperty(JProgressBar.class, "lower", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JProgressBar progress = (JProgressBar) widget;
                BoundedRangeModel model = progress.getModel();
                model.setMinimum(ParameterConverter.toInteger(value));
            }
        });

        registerProperty(JProgressBar.class, "upper", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JProgressBar progress = (JProgressBar) widget;
                BoundedRangeModel model = progress.getModel();
                model.setMaximum(ParameterConverter.toInteger(value));
            }
        });

        registerProperty(JProgressBar.class, "value", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JProgressBar progress = (JProgressBar) widget;
                BoundedRangeModel model = progress.getModel();
                model.setValue(ParameterConverter.toInteger(value));
            }
        });

        registerProperty(JProgressBar.class, "orientation", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JProgressBar progress = (JProgressBar) widget;
                value = normalize(value, "PROGRESS_");
                if ("LEFT_TO_RIGHT".equals(value)) {
                    progress.setOrientation(SwingConstants.HORIZONTAL);
                } else if ("RIGHT_TO_LEFT".equals(value)) {
                    progress.setOrientation(SwingConstants.HORIZONTAL);
                } else if ("TOP_TO_BOTTOM".equals(value)) {
                    progress.setOrientation(SwingConstants.VERTICAL);
                } else if ("BOTTOM_TO_TOP".equals(value)) {
                    progress.setOrientation(SwingConstants.VERTICAL);
                }
            }
        });

        registerProperty(JProgressBar.class, "show_text", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JProgressBar progress = (JProgressBar) widget;
                progress.setStringPainted(ParameterConverter.toBoolean(value));
            }
        });

        /*
         * PandaTable
         */
        registerProperty(PandaTable.class, "columns", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaTable table = (PandaTable) widget;
                table.setColumns(ParameterConverter.toInteger(value));
                table.setRows(table.getRows());
            }
        });

        registerProperty(PandaTable.class, "rows", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaTable table = (PandaTable) widget;
                table.setRows(ParameterConverter.toInteger(value));
            }
        });

        registerProperty(PandaTable.class, "column_types", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaTable table = (PandaTable) widget;
                StringTokenizer tokens = new StringTokenizer(value, String.valueOf(','));
                String[] types = new String[tokens.countTokens()];
                for (int i = 0; tokens.hasMoreTokens(); i++) {
                    types[i] = tokens.nextToken();
                }
                table.setTypes(types);
            }
        });

        registerProperty(PandaTable.class, "im_controls", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaTable table = (PandaTable) widget;
                StringTokenizer tokens = new StringTokenizer(value, String.valueOf(','));
                String[] ics = new String[tokens.countTokens()];
                for (int i = 0; tokens.hasMoreTokens(); i++) {
                    ics[i] = tokens.nextToken();
                }
                table.setImControls(ics);
            }
        });

        registerProperty(PandaTable.class, "column_titles", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaTable table = (PandaTable) widget;
                StringTokenizer tokens = new StringTokenizer(value, String.valueOf(','));
                String[] titles = new String[tokens.countTokens()];
                for (int i = 0; tokens.hasMoreTokens(); i++) {
                    titles[i] = tokens.nextToken();
                }
                table.setTitles(titles);
            }
        });

        /*
         * PandaCList
         */
        registerProperty(PandaCList.class, "columns", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JTable table = (JTable) widget;
                TableColumnModel model = table.getColumnModel();
                int columns = ParameterConverter.toInteger(value);
                while (model.getColumnCount() < columns) {
                    model.addColumn(new TableColumn());
                }
            }
        });

        /*
         * PandaCList,PandaTable
         */
        registerProperty(JTable.class, "column_widths", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JTable table = (JTable) widget;
                TableColumnModel model = table.getColumnModel();

                String k = widget.getName() + ".column_widths";
                String v = System.getProperty(k);
                if (v != null) {
                    value = v;
                }

                StringTokenizer tokens = new StringTokenizer(value, String.valueOf(','));

                int totalWidth = 0;
                for (int i = 0; i < model.getColumnCount() && tokens.hasMoreTokens(); i++) {
                    TableColumn column = model.getColumn(i);
                    int width = ParameterConverter.toInteger(tokens.nextToken());
                    width += 8; // FIXME do not use immediate value like this
                    column.setPreferredWidth(width);
                    column.setWidth(width);
                    totalWidth += width;
                }

                int parentWidth = table.getWidth();
                if (parent instanceof JScrollPane) {
                    JScrollPane scroll = (JScrollPane) parent;
                    parentWidth = scroll.getWidth();
                    Insets insets = scroll.getInsets();
                    parentWidth -= insets.left + insets.right;
                    JScrollBar vScrollBar = scroll.getVerticalScrollBar();
                    if (vScrollBar != null) {
                        ComponentUI ui = vScrollBar.getUI();
                        parentWidth -= ui.getPreferredSize(vScrollBar).getWidth();
                    }
                }

                if (totalWidth < parentWidth) {
                    TableColumn lastColumn = model.getColumn(model.getColumnCount() - 1);
                    int width = lastColumn.getPreferredWidth();
                    width += parentWidth - totalWidth;
                    lastColumn.setPreferredWidth(width);
                    lastColumn.setWidth(width);
                    // replace column_withs property
                    String new_column_widths;
                    if (value.contains(",")) {
                        new_column_widths = value.substring(0, value.lastIndexOf(",")) + "," + width;
                    } else {
                        new_column_widths = "" + width;
                    }
                    xml.setProperty(widget.getName(), "column_widths", new_column_widths);
                }
                if (v != null) {
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        TableColumn column = model.getColumn(i);
                        column.setResizable(false);
                        column.setMaxWidth(column.getWidth());
                        column.setMinWidth(column.getWidth());
                    }
                }
            }
        });

        registerProperty(PandaCList.class, "selection_mode", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaCList clist = (PandaCList) widget;
                value = normalize(value, "SELECTION_");
                if ("SINGLE".equals(value)) {
                    clist.setMode(PandaCList.SELECTION_MODE_SINGLE);
                } else {
                    clist.setMode(PandaCList.SELECTION_MODE_MULTI);
                }
            }
        });

        registerProperty(JTable.class, "show_titles", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JTable table = (JTable) widget;
                JTableHeader header = table.getTableHeader();
                header.setVisible(ParameterConverter.toBoolean(value));
            }
        });

        registerProperty(PandaHTML.class, "uri", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaHTML pane = (PandaHTML) widget;
                try {
                    if (value.length() == 0) {
                        return;
                    }
                    URL uri = new URL(value);
                    pane.setURI(uri);
                } catch (MalformedURLException e) {
                    logger.warn(e);
                }
            }
        });

        registerProperty(PandaTimer.class, "duration", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                PandaTimer timer = (PandaTimer) widget;
                timer.setDuration(ParameterConverter.toInteger(value));
            }
        });

        registerProperty(org.montsuqi.monsiaj.widgets.Frame.class, "label", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                org.montsuqi.monsiaj.widgets.Frame frame = (org.montsuqi.monsiaj.widgets.Frame) widget;
                frame.setBorder(BorderFactory.createTitledBorder(value));
            }
        });

        registerProperty(JScrollPane.class, "hscrollbar_policy", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JScrollPane scroll = (JScrollPane) widget;
                value = normalize(value, "POLICY_");
                if ("ALWAYS".equals(value)) {
                    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                } else if (value.startsWith("AUTO")) {
                    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                } else if ("NEVER".equals(value)) {
                    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                } else {
                    throw new IllegalArgumentException(value);
                }
            }
        });

        registerProperty(JScrollPane.class, "vscrollbar_policy", new WidgetPropertySetter() {

            @Override
            public void set(Interface xml, Container parent, Component widget, String value) {
                JScrollPane scroll = (JScrollPane) widget;
                value = normalize(value, "POLICY_");
                if ("ALWAYS".equals(value)) {
                    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                } else if (value.startsWith("AUTO")) {
                    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                } else if ("NEVER".equals(value)) {
                    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                } else {
                    throw new IllegalArgumentException(value);
                }
            }
        });

        registerProperty(JMenuItem.class, "stock_item", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                JMenuItem item = (JMenuItem) widget;
                value = normalize(value, "GNOMEUIINFO_MENU_");
                UIStock stock = UIStock.get(value);
                if (stock == null) {
                    return;
                }
                String oldText = item.getText();
                String newText = stock.getText();
                assert newText != null;
                if (oldText == null || oldText.length() == 0) {
                    item.setText(newText);
                }
                Icon oldIcon = item.getIcon();
                Icon newIcon = stock.getIcon();
                if (oldIcon == null && newIcon != null) {
                    item.setIcon(newIcon);
                }
                KeyStroke oldAccelerator = item.getAccelerator();
                KeyStroke newAccelerator = stock.getAccelerator();
                if (oldAccelerator == null && newAccelerator != null) {
                    item.setAccelerator(newAccelerator);
                }
            }
        });

//		registerProperty(Table.class, "rows", new WidgetPropertySetter() { 
//			void set(Interface xml, Container parent, Component widget, String value) {
//				Table table = (Table)widget;
//				TableLayout layout = (TableLayout)table.getLayout();
//				layout.setRows(ParameterConverter.toInteger(value));
//			}
//		});
//
//		registerProperty(Table.class, "columns", new WidgetPropertySetter() { 
//			void set(Interface xml, Container parent, Component widget, String value) {
//				Table table = (Table)widget;
//				TableLayout layout = (TableLayout)table.getLayout();
//				layout.setColumns(ParameterConverter.toInteger(value));
//			}
//		});
//		registerProperty(Table.class, "homogeneous", new WidgetPropertySetter() { 
//			void set(Interface xml, Container parent, Component widget, String value) {
//				Table table = (Table)widget;
//				TableLayout tl = (TableLayout)table.getLayout();
//				tl.setHomogeneous(ParameterConverter.toBoolean(value));
//			}
//		});
//
//		registerProperty(Table.class, "row_spacing", new WidgetPropertySetter() { 
//			void set(Interface xml, Container parent, Component widget, String value) {
//				Table table = (Table)widget;
//				TableLayout tl = (TableLayout)table.getLayout();
//				tl.setRowSpacing(ParameterConverter.toInteger(value));
//			}
//		});
//
//		registerProperty(Table.class, "column_spacing", new WidgetPropertySetter() { 
//			void set(Interface xml, Container parent, Component widget, String value) {
//				Table table = (Table)widget;
//				TableLayout tl = (TableLayout)table.getLayout();
//				tl.setRowSpacing(ParameterConverter.toInteger(value));
//			}
//		});
        registerProperty(Window.class, "allow_grow", new WidgetPropertySetter() { //$NON-NLS-1

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                Window window = (Window) widget;
                window.setAllow_Grow(ParameterConverter.toBoolean(value));
            }
        });

        registerProperty(Window.class, "allow_shrink", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                Window window = (Window) widget;
                window.setAllow_Shrink(ParameterConverter.toBoolean(value));
            }
        });

        registerProperty(java.awt.Window.class, "position", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                value = normalize(value, null);
                if (value.equals("WIN_POS_CENTER")) {
                    java.awt.Window window = (java.awt.Window) widget;
                    window.setLocationRelativeTo(null);
                }
            }
        });

        registerProperty(java.awt.Window.class, "x", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                java.awt.Window window = (java.awt.Window) widget;
                int x = ParameterConverter.toInteger(value);
                int y = window.getY();
                window.setLocation(x, y);
            }
        });

        registerProperty(java.awt.Window.class, "y", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                java.awt.Window window = (java.awt.Window) widget;
                int x = window.getX();
                int y = ParameterConverter.toInteger(value);
                window.setLocation(x, y);
            }
        });

        registerProperty(JDialog.class, "modal", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                JDialog dialog = (JDialog) widget;
                dialog.setModal(ParameterConverter.toBoolean(value));
                dialog.setModal(false);
            }
        });

        registerProperty(Pixmap.class, "scaled", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                Pixmap pixmap = (Pixmap) widget;
                pixmap.setScaled(ParameterConverter.toBoolean(value));
            }
        });

        registerProperty(Pixmap.class, "scaled_width", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                Pixmap pixmap = (Pixmap) widget;
                pixmap.setScaledWidth(ParameterConverter.toInteger(value));
            }
        });

        registerProperty(Pixmap.class, "scaled_height", new WidgetPropertySetter() {

            @Override
            void set(Interface xml, Container parent, Component widget, String value) {
                Pixmap pixmap = (Pixmap) widget;
                pixmap.setScaledHeight(ParameterConverter.toInteger(value));
            }
        });

    }

    /**
     * <p>
     * Removes given prefix along with "GTK_" and "GTK_". </p>
     *
     * @param value target string to be normalized.
     * @param prefixToRemove a prefix to remove. Ignored if it is null or its
     * length is zero.
     * @return normalized string.
     */
    static String normalize(String value, String prefixToRemove) {
        if (value.startsWith("GDK_")) {
            value = value.substring("GDK_".length());
        }
        if (value.startsWith("GTK_")) {
            value = value.substring("GTK_".length());
        }
        if (prefixToRemove != null) {
            int length = prefixToRemove.length();
            if (length > 0 && value.startsWith(prefixToRemove)) {
                value = value.substring(length);
            }
        }
        return value;
    }
}
