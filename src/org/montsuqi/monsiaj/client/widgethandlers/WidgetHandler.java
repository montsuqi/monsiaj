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
package org.montsuqi.monsiaj.client.widgethandlers;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.json.JSONException;
import org.json.JSONObject;
import org.montsuqi.monsiaj.client.UIControl;
import org.montsuqi.monsiaj.monsia.Style;
import org.montsuqi.monsiaj.widgets.Calendar;
import org.montsuqi.monsiaj.widgets.ColorButton;
import org.montsuqi.monsiaj.widgets.FileChooserButton;
import org.montsuqi.monsiaj.widgets.Frame;
import org.montsuqi.monsiaj.widgets.NumberEntry;
import org.montsuqi.monsiaj.widgets.PandaCList;
import org.montsuqi.monsiaj.widgets.PandaDownload;
import org.montsuqi.monsiaj.widgets.PandaHTML;
import org.montsuqi.monsiaj.widgets.PandaPreview;
import org.montsuqi.monsiaj.widgets.PandaTable;
import org.montsuqi.monsiaj.widgets.PandaTimer;
import org.montsuqi.monsiaj.widgets.Pixmap;
import org.montsuqi.monsiaj.widgets.Window;

/**
 * <
 * p>
 * Superclass for all widget handlers.</p>
 */
public abstract class WidgetHandler {

    private static final Map<Class, WidgetHandler> classTable;

    static {
        classTable = new HashMap<>();
        registerHandler(JTextField.class, new EntryHandler());
        registerHandler(NumberEntry.class, new NumberEntryHandler());
        registerHandler(JTextArea.class, new TextHandler());
        registerHandler(JLabel.class, new LabelHandler());
        registerHandler(JComboBox.class, new ComboHandler());
        registerHandler(AbstractButton.class, new ButtonHandler());
        registerHandler(JTabbedPane.class, new NotebookHandler());
        registerHandler(Calendar.class, new CalendarHandler());
        registerHandler(JProgressBar.class, new ProgressBarHandler());
        registerHandler(PandaPreview.class, new PreviewHandler());
        registerHandler(Frame.class, new FrameHandler());
        registerHandler(FileChooserButton.class, new FileChooserButtonHandler());
        registerHandler(ColorButton.class, new ColorButtonHandler());
        registerHandler(PandaTimer.class, new TimerHandler());
        registerHandler(PandaDownload.class, new DownloadHandler());
        registerHandler(Pixmap.class, new PixmapHandler());
        registerHandler(PandaHTML.class, new PandaHTMLHandler());
        registerHandler(PandaCList.class, new CListHandler());
        registerHandler(PandaTable.class, new PandaTableHandler());
        registerHandler(Window.class, new WindowHandler());
    }

    public abstract void set(UIControl con, Component widget, JSONObject obj, Map styleMap) throws JSONException;

    public abstract void get(UIControl con, Component widget, JSONObject obj) throws JSONException;

    public void setStyle(Map styleMap, Component widget, String styleName) {
        Style style;
        if (styleMap.containsKey(styleName)) {
            style = (Style) styleMap.get(styleName);
        } else {
            style = Style.DEFAULT_STYLE;
        }
        style.apply(widget);
    }

    protected void setCommonAttribute(Component widget, JSONObject obj, Map styleMap) throws JSONException {
        if (obj.has("state")) {
            int state = obj.getInt("state");
            /* Widget states from gtkenums.h
             typedef enum
             {
             GTK_STATE_NORMAL,     => 0
             GTK_STATE_ACTIVE,     => 1
             GTK_STATE_PRELIGHT,   => 2
             GTK_STATE_SELECTED,   => 3
             GTK_STATE_INSENSITIVE => 4
             } GtkStateType;
             */
            boolean flag = state != 4;

            widget.setFocusable(flag);
            if (widget instanceof JTextComponent) {
                JTextComponent text = (JTextComponent) widget;
                text.setEditable(flag);
            } else {
                widget.setEnabled(flag);
            }
        }
        if (obj.has("style")) {
            String style = obj.getString("style");
            setStyle(styleMap, widget, style);
        }
        if (obj.has("visible")) {
            boolean visible = obj.getBoolean("visible");
            widget.setVisible(visible);
        }
    }

    protected boolean isCommonAttribute(String key) {
        if (key.matches("state")) {
            return true;
        } else if (key.matches("style")) {
            return true;
        } else if (key.matches("visible")) {
            return true;
        } else if (key.matches("__keys__")) {
            return true;
        }
        return false;
    }

    protected void setEditable(Component widget, JSONObject obj) throws JSONException {
        JTextField entry = (JTextField) widget;
        if (obj.has("editable")) {
            boolean editable = obj.getBoolean("editable");
            entry.setEditable(editable);
        }
    }

    protected boolean isEditable(String key) {
        return key.matches("editable");
    }

    private static void registerHandler(Class clazz, WidgetHandler marshaller) {
        classTable.put(clazz, marshaller);
    }

    public static WidgetHandler getHandler(Class clazz) {
        for (Class c = clazz; c != null; c = c.getSuperclass()) {
            if (classTable.containsKey(c)) {
                return (WidgetHandler) classTable.get(c);
            }
        }
        return null;
    }
}
