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

package org.montsuqi.client;

import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.montsuqi.util.Logger;
import org.montsuqi.widgets.Window;

public abstract class SignalHandler {

	protected static final Logger logger = Logger.getLogger(SignalHandler.class);

	public abstract void handle(Protocol con, Component widget, Object userData) throws IOException;

	protected boolean isWindowActive(Protocol con, Component widget) {
		return SwingUtilities.windowForComponent(widget) == con.getActiveWindow();
	}

	public static SignalHandler getSignalHandler(String handlerName) {
		if (handlers.containsKey(handlerName)) {
			return (SignalHandler)handlers.get(handlerName);
		}
		logger.warn("signal handler for {0} is not found", handlerName); //$NON-NLS-1$
		return getSignalHandler(null);
	}

	static Map handlers;
	static Timer timer;
	static TimerTask timerTask;
	static boolean timerBlocked;
	static final String SYMBOLS;
	
	private static void registerHandler(String signalName, SignalHandler handler) {
		handlers.put(signalName, handler);
	}

	static void blockChangedHandlers() {
		timerBlocked = true;
	}

	static void unblockChangedHandlers() {
		timerBlocked = false;
	}

	static {
		handlers = new HashMap();
	
		timer = new Timer();
		timerTask = null;
		timerBlocked = false;

		StringBuffer buf = new StringBuffer();
		buf.append("\u3000\uff01\u201d\uff03\uff04\uff05\uff06\u2019"); //$NON-NLS-1$
		buf.append("\uff08\uff09\uff0a\uff0b\uff0c\u30fc\uff0e\uff0f"); //$NON-NLS-1$
		buf.append("\uff10\uff11\uff12\uff13\uff14\uff15\uff16\uff17"); //$NON-NLS-1$
		buf.append("\uff18\uff19\uff1a\uff1b\uff1c\uff1d\uff1e\uff1f"); //$NON-NLS-1$
		buf.append("\uff20\uff21\uff22\uff23\uff24\uff25\uff26\uff27"); //$NON-NLS-1$
		buf.append("\uff28\uff29\uff2a\uff2b\uff2c\uff2d\uff2e\uff2f"); //$NON-NLS-1$
		buf.append("\uff30\uff31\uff32\uff33\uff34\uff35\uff36\uff37"); //$NON-NLS-1$
		buf.append("\uff38\uff39\uff3a\uff3b\uffe5\uff3d\uff3e\uff3f"); //$NON-NLS-1$
		buf.append("\u2018\u30a2\u30a8\u30a4\u30aa\u30a6\uff5b\uff5c"); //$NON-NLS-1$
		buf.append("\uff5d\uffe3"); //$NON-NLS-1$
		SYMBOLS = buf.toString();

		registerHandler(null, new SignalHandler() {
			public void handle(Protocol con, Component widget, Object userData) {
				// do nothing
			}
		});

		registerHandler("select_all", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				JTextField field = (JTextField)widget;
				field.selectAll();
			}
		});

		registerHandler("unselect_all", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				JTextField field = (JTextField)widget;
				field.select(0, 0);
			}
		});

		final SignalHandler sendEvent = new SignalHandler() {
			public void handle(Protocol con, Component widget, Object userData) throws IOException {
				if (con.isReceiving()) {
					return;
				}
				if ( ! isWindowActive(con, widget)) {
					return;
				}
				java.awt.Window window = SwingUtilities.windowForComponent(widget);
				try {
					if (window instanceof Window) {
						((Window)window).showBusyCursor();
					}
					con.sendEvent(window.getName(), widget.getName(), userData == null ? "" : userData.toString()); //$NON-NLS-1$
					con.sendWindowData();
					synchronized (this) {
						blockChangedHandlers();
						con.getScreenData();
						unblockChangedHandlers();
					}
				} finally {
					if (window instanceof Window) {
						((Window)window).hideBusyCursor();
					}
				}
			}
		};

		final SignalHandler changed = new SignalHandler() {
			public void handle(Protocol con, Component widget, Object userData) throws IOException {
				if ( ! isWindowActive(con, widget)) {
					return;
				}
				con.addChangedWidget(widget);
			}
		};

		SignalHandler sendEventWhenIdle = new SignalHandler() {
			public synchronized void handle(final Protocol con, final Component widget, final Object userData) {
				if (timerTask != null) {
					timerTask.cancel();
					timerTask = null;
				}
				if (timerBlocked) {
					return;
				}
				timerTask = new TimerTask() {
					public void run() {
						JTextComponent text = (JTextComponent)widget;
						String t = text.getText();
						int length = t.length();
						if (length > 0) {
							char c = t.charAt(length - 1);
							if (isKatakana(c) || isKanji(c) || isSymbol(c)) {
								try {
									changed.handle(con, widget, userData);
									sendEvent.handle(con, widget, userData);
								} catch (IOException e) {
									logger.warn(e);
								}
							}
						}
					}

					private boolean isKatakana(char c) {
						return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.KATAKANA;
					}

					private boolean isKanji(char c) {
						return Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS;
					}

					private boolean isSymbol(char c) {
						return SYMBOLS.indexOf(c) >= 0;
					}
				};
				timer.schedule(timerTask, 1000);
			}
		};
		registerHandler("send_event", sendEvent); //$NON-NLS-1$
		registerHandler("send_event_when_idle", sendEventWhenIdle); //$NON-NLS-1$
		registerHandler("send_event_on_focus_out", sendEvent); //$NON-NLS-1$

		registerHandler("clist_send_event", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) throws IOException {
				if ( ! isWindowActive(con, widget)) {
					return;
				}
				con.addChangedWidget(widget);
				sendEvent.handle(con, widget, "SELECT"); //$NON-NLS-1$
			}
		});

		registerHandler("activate_widget", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) throws IOException {
				sendEvent.handle(con, widget, "ACTIVATE"); //$NON-NLS-1$
			}
		});

		registerHandler("entry_next_focus", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				Node node = con.getNode(widget);
				if (node != null) {
					Component nextWidget = node.getInterface().getWidget(userData.toString());
					if (nextWidget != null) {
						nextWidget.requestFocus();
					}
				}
			}
		});

		registerHandler("changed", changed); //$NON-NLS-1$
		registerHandler("entry_changed", changed); //$NON-NLS-1$
		registerHandler("text_changed", changed); //$NON-NLS-1$
		registerHandler("button_toggled", changed); //$NON-NLS-1$
		registerHandler("selection_changed", changed); //$NON-NLS-1$
		registerHandler("click_column", changed); //$NON-NLS-1$
		registerHandler("day_selected", changed); //$NON-NLS-1$
		registerHandler("switch_page", changed); //$NON-NLS-1$

		registerHandler("entry_set_editable", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				// do nothing?
			}
		});

		registerHandler("map_event", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				con.clearWindowTable();
			}
		});

		registerHandler("set_focus", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				// Node node = con.getNode(widget);
				// FocusedScreen = node; // this variable is referred from nowhere.
			}
		});

		registerHandler("window_close", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				if ( ! isWindowActive(con, widget)) {
					return;
				}
				con.closeWindow(widget);
			}
		});

		registerHandler("window_destroy", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				if ( ! isWindowActive(con, widget)) {
					return;
				}
				con.exit();
			}
		});

		registerHandler("open_browser", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) throws IOException {
				if ( ! (widget instanceof JTextPane)) {
					return;
				}
				if ( ! isWindowActive(con, widget)) {
					return;
				}
				JTextPane pane = (JTextPane)widget;
				URL uri;
				uri = new URL((String)userData);
				pane.setPage(uri);
			}
		});

		registerHandler("keypress_filter", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				if ( ! isWindowActive(con, widget)) {
					return;
				}
				Component next = con.getInterface().getWidget((String)userData);
				next.requestFocus();
			}
		});

		registerHandler("press_filter", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				//logger.warn(Messages.getString("Protocol.press_filter_is_not_impremented_yet")); //$NON-NLS-1$
			}
		});

		registerHandler("gtk_true", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				// callback placeholder wich has no effect
			}
		});
	}
}
