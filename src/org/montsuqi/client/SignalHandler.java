package org.montsuqi.client;

import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

public abstract class SignalHandler {

	public abstract void handle(Protocol con, Component widget, Object userData) throws IOException;

	public static SignalHandler getSignalHandler(String handlerName) throws NoSuchMethodException {
		if (handlers.containsKey(handlerName)) {
			return (SignalHandler)handlers.get(handlerName);
		} else {
			String message = Messages.getString("SignalHandler.handler_not_found"); //$NON-NLS-1$
			message = MessageFormat.format(message, new Object[] { handlerName });
			throw new NoSuchMethodException(message);
		}
	}

	static Map handlers;

	private static void registerHandler(String signalName, SignalHandler handler) {
		handlers.put(signalName, handler);
	}

	static {
		handlers = new HashMap();

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
				if ( ! con.isReceiving() && ! ignoreEvent) {
					con.sendEvent(SwingUtilities.windowForComponent(widget).getName(), widget.getName(), userData == null ? "" : userData.toString()); //$NON-NLS-1$
					con.sendWindowData();
//					blockChangedHanders();
					if (con.getScreenData()) {
						ignoreEvent = true;
//						while (gtk_events_pending()) {
//							gtk_main_iteration();
//						}
						ignoreEvent = false;
					}
//					unblockChangedHanders();
				}
			}
		};

		registerHandler("send_event", sendEvent); //$NON-NLS-1$
		registerHandler("send_event_when_idle", sendEvent);  //$NON-NLS-1$
		registerHandler("send_event_on_focus_out", sendEvent);  //$NON-NLS-1$

		registerHandler("clist_send_event", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) throws IOException {
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

		SignalHandler changed = new SignalHandler() {
			public void handle(Protocol con, Component widget, Object userData) {
				con.addChangedWidget(widget);
			}
		};

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
				con.closeWindow(widget);
			}
		});

		registerHandler("window_destroy", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) {
				con.exit();
			}
		});

		registerHandler("open_browser", new SignalHandler() { //$NON-NLS-1$
			public void handle(Protocol con, Component widget, Object userData) throws IOException {
				if ( ! (widget instanceof JTextPane)) {
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
