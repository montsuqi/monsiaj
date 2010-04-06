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

package org.montsuqi.client.marshallers;

import java.awt.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.montsuqi.client.Protocol;
import org.montsuqi.util.Logger;
import org.montsuqi.widgets.Calendar;
import org.montsuqi.widgets.FileEntry;
import org.montsuqi.widgets.Frame;
import org.montsuqi.widgets.NumberEntry;
import org.montsuqi.widgets.OptionMenu;
import org.montsuqi.widgets.PandaHTML;
import org.montsuqi.widgets.PandaPreview;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.PandaDownload;
import org.montsuqi.widgets.Pixmap;
import org.montsuqi.widgets.Window;

/** <p>Superclass for all widget marshallers.</p>
 */
public abstract class WidgetMarshaller {

	protected static final Logger logger = Logger.getLogger(WidgetMarshaller.class);

	private static Map classTable;

	static {
		classTable = new HashMap();
		registerMarshaller(JTextField.class, new EntryMarshaller());
		registerMarshaller(NumberEntry.class, new NumberEntryMarshaller());
		registerMarshaller(JTextArea.class, new TextMarshaller());
		registerMarshaller(JLabel.class, new LabelMarshaller());
		registerMarshaller(JComboBox.class, new ComboMarshaller());
		registerMarshaller(JTable.class, new CListMarshaller());
		registerMarshaller(AbstractButton.class, new ButtonMarshaller());
		registerMarshaller(JList.class, new ListMarshaller());
		registerMarshaller(JTabbedPane.class, new NotebookMarshaller());
		registerMarshaller(Calendar.class, new CalendarMarshaller());
		registerMarshaller(JProgressBar.class, new ProgressBarMarshaller());
		registerMarshaller(PandaPreview.class, new PreviewMarshaller());
		registerMarshaller(OptionMenu.class, new OptionMenuMarshaller());
		registerMarshaller(Frame.class, new FrameMarshaller());
		registerMarshaller(FileEntry.class, new FileEntryMarshaller());
		registerMarshaller(PandaTimer.class, new TimerMarshaller());
		registerMarshaller(PandaDownload.class, new DownloadMarshaller());
                registerMarshaller(Pixmap.class, new PixmapMarshaller());
		registerMarshaller(PandaHTML.class, new PandaHTMLMarshaller());
		registerMarshaller(Window.class, new WindowMarshaller());
	}

	public abstract void receive(WidgetValueManager manager, Component widget) throws IOException;
	public abstract void send(WidgetValueManager manager, String name, Component widget) throws IOException;

	/** <p>Handles parameters common for all widget types: "state" and "style".</p>
	 * @param manager value manager.
	 * @param widget target widget.
	 * @param name parameter name.
	 * @return true if handled(name was "state" or "style".
	 * @throws IOException on IO error.
	 */
	protected boolean handleStateStyle(WidgetValueManager manager, Component widget, String name) throws IOException {
        Protocol con = manager.getProtocol();
		if ("state".equals(name)) { //$NON-NLS-1$
			int state = con.receiveIntData();
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
			final boolean flag = state != 4;
			widget.setEnabled(flag);
			widget.setFocusable(flag);
			if (widget instanceof JTextComponent) {
				JTextComponent text = (JTextComponent)widget;
				text.setEditable(flag);
			}
			return true;
		} else if ("style".equals(name)) { //$NON-NLS-1$
			String buff = con.receiveStringData();
			manager.setStyle(widget, buff);
            return true;
		} else {
			return false;
		}
	}

	private static void registerMarshaller(Class clazz, WidgetMarshaller marshaller) {
		classTable.put(clazz, marshaller);
	}

	public static WidgetMarshaller getMarshaller(Class clazz) {
		for (Class c = clazz; c != null; c = c.getSuperclass()) {
			if (classTable.containsKey(c)) {
				return (WidgetMarshaller)classTable.get(c);
			}
		}
		return null;
	}
}
