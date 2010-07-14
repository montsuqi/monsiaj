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
package org.montsuqi.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.montsuqi.util.Logger;
import org.montsuqi.widgets.FileEntry;

/** <p>Class to perform an action for a widget.</p>
 */
public abstract class SignalHandler {

    protected static final Logger logger = Logger.getLogger(SignalHandler.class);
    private String signalName = ""; //$NON-NLS-1$

    @Override
    public String toString() {
        return getSignalName();
    }

    public void setSignalName(final String signalName) {
        this.signalName = signalName;
    }

    public String getSignalName() {
        return signalName;
    }

    public abstract void handle(Protocol con, Component widget, Object userData) throws IOException;

    /** <p>Returns signal handler for the given name. If such handler could not be found,
     * returns the fallback handler, which does nothing.</p>
     * @param handlerName name of a signal handler.
     * @return a SignalHandler instance.
     */
    public static SignalHandler getSignalHandler(String handlerName) {
        logger.enter(handlerName);
        if (handlers.containsKey(handlerName)) {
            final SignalHandler handler = (SignalHandler) handlers.get(handlerName);
            logger.leave();
            return handler;
        }
        logger.info("signal handler for {0} is not found", handlerName); //$NON-NLS-1$
        final SignalHandler handler = getSignalHandler(null);
        logger.leave();
        return handler;
    }
    static Map handlers;
    static Timer timer;
    static TimerTask timerTask;
    static boolean timerBlocked;
    static final String SYMBOLS;

    private static void registerHandler(String signalName, SignalHandler handler) {
        handler.setSignalName(signalName);
        handlers.put(signalName, handler);
    }

    static void blockChangedHandlers() {
        logger.enter();
        timerBlocked = true;
        logger.leave();
    }

    static void unblockChangedHandlers() {
        logger.enter();
        timerBlocked = false;
        logger.leave();
    }

    static private String getWidgetName(String str) {
        int index;

        index = str.lastIndexOf('.');
        if (index == -1) {
            return str;
        } else {
            return str.substring(index + 1);
        }
    }

    static {
        handlers = new HashMap();

        timer = new Timer();
        timerTask = null;
        timerBlocked = false;

        // In addition to kana/kanji, some special symbols can trigger sendEventWhenIdle.
        StringBuilder buf = new StringBuilder();
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

        /** <p>A signal handler which does nothing.</p>
         */
        registerHandler(null, new SignalHandler() {

            public void handle(Protocol con, Component widget, Object userData) {
                // do nothing
            }
        });

        /** <p>A signal handler which selects all text on a text field.</p>
         */
        registerHandler("select_all", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) {
                JTextField field = (JTextField) widget;
                field.selectAll();
            }
        });

        /** <p>A signal handler which unselect all text on a text field.</p>
         */
        registerHandler("unselect_all", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) {
                JTextField field = (JTextField) widget;
                field.select(0, 0);
            }
        });

        /** <p>A signal handler which sends event and parameters.</p>
         */
        final SignalHandler sendEvent = new SignalHandler() {

            public void handle(Protocol con, Component widget, Object userData) throws IOException {
                synchronized (con) {
                    if (timerTask != null) {
                        timerTask.cancel();
                        timerTask = null;
                    }
                    if (con.isReceiving()) {
                        return;
                    }
                    try {
                        con.setIsReceiving(true);
                        if (widget instanceof JComponent) {
                            if (((JComponent) widget).getClientProperty("panda combo editor") == Boolean.TRUE) { //$NON-NLS-1$
                                con._addChangedWidget(widget);
                            }
                        }
                        Window window = null;
                        if (widget instanceof JMenuItem) {
                            JComponent c = (JComponent) widget;
                            window = (Window) c.getClientProperty("window"); //$NON-NLS-1$
                        } else {
                            window = SwingUtilities.windowForComponent(widget);
                        }
                        if (window == null || widget == null) {
                            return;
                        }
                        String windowName = getWidgetName(window.getName());
                        String widgetName = getWidgetName(widget.getName());
                        String event;
                        if (userData == null) {
                            event = widgetName;
                        } else {
                            event = userData.toString();
                            if (event.length() == 0) {
                                event = widgetName;
                            }
                        }
                        org.montsuqi.widgets.Window.busyAllWindows();
                        con.sendEvent(windowName, widgetName, event);
                        con.sendWindowData();
                        synchronized (con) {
                            blockChangedHandlers();
                            con.getScreenData();
                            unblockChangedHandlers();
                        }
                    } finally {
                        con.setIsReceiving(false);
                    }
                }
            }
        };

        /** <p>A signal handler which registers the target widget as "changed."</p>
         */
        final SignalHandler changed = new SignalHandler() {

            public void handle(Protocol con, Component widget, Object userData) throws IOException {
                con.addChangedWidget(widget);
            }
        };

        /** <p>A signal handler which sends event only while no other action is performed.</p>
         * <p>System property monsia.send.event.delay can control this behavior.</p>
         */
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
                        synchronized (con) {
                            if (con.isReceiving()) {
                                return;
                            }
                            JTextComponent text = (JTextComponent) widget;
                            String t = text.getText();
                            int length = t.length();
                            if (length > 0) {
                                char c = t.charAt(length - 1);
                                if (isKana(c) || isKanji(c) || isSymbol(c)) {
                                    try {
                                        changed.handle(con, widget, userData);
                                        sendEvent.handle(con, widget, userData);
                                    } catch (IOException e) {
                                        con.exceptionOccured(e);
                                    }
                                }
                            }
                        }
                    }

                    private boolean isKana(char c) {
                        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                        return block == Character.UnicodeBlock.KATAKANA || block == Character.UnicodeBlock.HIRAGANA;
                    }

                    private boolean isKanji(char c) {
                        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
                    }

                    private boolean isSymbol(char c) {
                        return SYMBOLS.indexOf(c) >= 0;
                    }
                };
                long delay = con.getTimerPeriod();
                if (delay > 0) {
                    timer.schedule(timerTask, delay);
                } else {
                    timer.cancel();
                }
            }
        };

        registerHandler("send_event", sendEvent); //$NON-NLS-1$
        registerHandler("send_event_when_idle", sendEventWhenIdle); //$NON-NLS-1$
        registerHandler("send_event_on_focus_out", sendEvent); //$NON-NLS-1$

        /** <p>A signal handler which registers the taret widget as "changed" and
         * sends "SELECT" event.</p>
         */
        registerHandler("clist_send_event", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) throws IOException {
                con.addChangedWidget(widget);
                sendEvent.handle(con, widget, "SELECT"); //$NON-NLS-1$
            }
        });

        /** <p>A signal handler which sends an "ACTIVATE".</p>
         */
        registerHandler("activate_widget", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) throws IOException {
                sendEvent.handle(con, widget, "ACTIVATE"); //$NON-NLS-1$
            }
        });

        /** <p>A signal handler which moves the focus to the widget of given name.</p>
         */
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

        registerHandler("fileentry_changed", new SignalHandler() {

            public void handle(Protocol con, Component widget, Object userData) {
                Container parent;
                while ((parent = widget.getParent()) != null) {
                    if (parent instanceof FileEntry) {
                        con.addChangedWidget(parent);
                        con.addChangedWidget(widget);
                        break;
                    }
                }
            }
        });

        registerHandler("entry_set_editable", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) {
                // do nothing?
            }
        });

        /** <p>A signal handler which removes all changed widgets from all windows.</p>
         */
        registerHandler("map_event", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) {
                con.clearWindowTable();
            }
        });

        /** do nothing for now */
        registerHandler("set_focus", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) {
                // Node node = con.getNode(widget);
                // FocusedScreen = node; // this variable is referred from nowhere.
            }
        });

        /** <p>A signal handler to close the window on which target widget is.</p>
         */
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

        /** <p>A signal handler to open the given URL on target JTextPane widget.</p>
         */
        registerHandler("open_browser", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) throws IOException {
                if (!(widget instanceof JTextPane)) {
                    return;
                }
                JTextPane pane = (JTextPane) widget;
                URL uri;
                uri = new URL((String) userData);
                pane.setPage(uri);
            }
        });

        registerHandler("keypress_filter", new SignalHandler() { //$NON-NLS-1$

            public void handle(Protocol con, Component widget, Object userData) {
System.out.println("keypress_filter" + (String)userData);
                Component next = con.getInterface().getWidget((String) userData);
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
