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

package org.montsuqi.widgets;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/** <p>A JFrame wrapper.</p>
 */
public class Window extends JFrame {
	
	private String title = "";
	private boolean allow_grow;
	private boolean allow_shrink;

	/** <p>Constructs a Window instance.</p>
	 */
	public Window() {
		super();
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Prepare a glass pane with wait cursor.
		// This pane is usually invisible, but is visible and gains focus to
		// disalbe this window when it is busy.
		setGlassPane(new JPanel() {
			{
				// Make the pane transparent.
				setOpaque(false);
				Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
				setCursor(waitCursor);
				addMouseListener(new MouseAdapter() {
					// nothing overridden
				});
				addKeyListener(new KeyAdapter() {
					// nothing overridden
				});
			}
		});
		hideBusyCursor();
	}

	/** <p>Show the window is busy by changing the mouse cursor to wait cursor.
	 * Accepts no input.</p>
	 */
	public void showBusyCursor() {
		getGlassPane().setVisible(true);
		getGlassPane().requestFocus();
	}

	/** <p>Cancel the busy state of this window.</p>
	 */
	public void hideBusyCursor() {
		getGlassPane().setVisible(false);
	}

	/** <p>Test if this window is active(=accpets input)</p>
	 * 
	 * @return true if this window is active. false otherwise.
	 */
	public boolean isActive() {
		return ! getGlassPane().isVisible();
	}

	/** <p>Make <em>all</em> window busy.</p>
	 */
	public static void busyAllWindows() {
		Window[] windows = getMontsuqiWindows();
		for (int i = 0; i < windows.length; i++) {
			Window w = windows[i];
			w.showBusyCursor();
		}
	}

	/** <p>Returns all java.awt.Frames which are instances of this class.</p>
	 * 
	 * @return array of Frames which are instances of this class. 
	 */
	public static Window[] getMontsuqiWindows() {
		Frame[] frames = Frame.getFrames();
		List list = new ArrayList();
		for (int i = 0; i < frames.length; i++) {
			Frame f = frames[i];
			if (f instanceof Window) {
				list.add(f);
			}
		}
		return (Window[])list.toArray(new Window[list.size()]);
	}
	
	public void setTitleString(String title) {
		this.title = title;
	}
	
	public void setSessionTitle(String sessionTitle) {
		Frame frame = (Frame)this;
		if (sessionTitle.equals("")) {
			frame.setTitle(title);			
		} else {
			frame.setTitle(title + " - " + sessionTitle);
		}
	}
	public void setAllow_Grow(boolean value) {
		this.allow_grow = value;
	}
	public boolean getAllow_Grow() {
		return allow_grow;
	}
	public void setAllow_Shrink(boolean value) {
		this.allow_shrink = value;
	}
	public boolean getAllow_Shrink() {
		return allow_shrink;
	}
}
