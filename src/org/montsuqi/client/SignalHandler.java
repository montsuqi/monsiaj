package org.montsuqi.client;

import java.awt.Component;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.montsuqi.util.Logger;

public abstract class SignalHandler {

	public abstract void handle(Protocol con, Component widget, Object userData) throws IOException;
	Logger logger;

	SignalHandler() {
		logger = Logger.getLogger(SignalHandler.class);
	}

	protected boolean isWindowActive(Protocol con, Component widget) {
		Window window = SwingUtilities.windowForComponent(widget);
		return window == con.getActiveWindow();
	}

	public static SignalHandler getSignalHandler(String handlerName) throws NoSuchMethodException {
		if (handlers.containsKey(handlerName)) {
			return (SignalHandler)handlers.get(handlerName);
		}
		String message = Messages.getString("SignalHandler.handler_not_found"); //$NON-NLS-1$
		message = MessageFormat.format(message, new Object[] { handlerName });
		Logger.getLogger(SignalHandler.class).warn(message);
		return getSignalHandler(null);
	}

	static Map handlers;

	private static void registerHandler(String signalName, SignalHandler handler) {
		handlers.put(signalName, handler);
	}

	static {
		handlers = new HashMap();

		registerHandler(null, new SignalHandler() {
			public void handle(Protocol con, Component widget, Object userData) {
				// do nothing
			}
		});

		registerHandler("select_all", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				JTextField field = (JTextField)widget;
				field.selectAll();
				field.setCaretPosition(0);
			}
		});

		registerHandler("unselect_all", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				JTextField field = (JTextField)widget;
				field.select(0, 0);
			}
		});

		final SignalHandler sendEvent = new SignalHandler() {
			boolean ignoreEvent = false;
			public void handle(Protocol con, Component widget, Object userData) throws IOException {
				if (con.isReceiving() || ignoreEvent) {
					return;
				}
				if ( ! isWindowActive(con, widget)) {
					return;
				}
				con.sendEvent(SwingUtilities.windowForComponent(widget).getName(), widget.getName(), userData == null ? "" : userData.toString()); //$NON-NLS-1$
				con.sendWindowData();
//				blockChangedHanders();
				if (con.getScreenData()) {
					ignoreEvent = true;
//					while (gtk_events_pending()) {
//						gtk_main_iteration();
//					}
					ignoreEvent = false;
				}
//				unblockChangedHanders();
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
			public void handle(final Protocol con, final Component widget, final Object userData) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JTextComponent text = (JTextComponent)widget;
						String t = text.getText();
						int length = t.length();
						if (length != 0 && Character.UnicodeBlock.of(t.charAt(length - 1)) != Character.UnicodeBlock.KATAKANA) {
							return;
						}
						try {
							changed.handle(con, widget, userData);
							sendEvent.handle(con, widget, userData);
						} catch (IOException e) {
							logger.warn(e);
						}
					}
				});
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
