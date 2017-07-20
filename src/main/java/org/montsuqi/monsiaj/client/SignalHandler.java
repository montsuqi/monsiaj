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
package org.montsuqi.monsiaj.client;

import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * Class to perform an action for a widget.</p>
 */
public abstract class SignalHandler {

    protected static final Logger logger = LogManager.getLogger(SignalHandler.class);
    private String signalName = "";

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

    public abstract void handle(UIControl uiControl, Component widget, Object userData) throws IOException;

    /**
     * <p>
     * Returns signal handler for the given name. If such handler could not be
     * found, returns the fallback handler, which does nothing.</p>
     *
     * @param handlerName name of a signal handler.
     * @return a SignalHandler instance.
     */
    public static SignalHandler getSignalHandler(String handlerName) {
        logger.entry(handlerName);
        if (handlers.containsKey(handlerName)) {
            final SignalHandler handler = (SignalHandler) handlers.get(handlerName);
            logger.exit();
            return handler;
        }
        logger.debug("signal handler for {0} is not found", handlerName);
        final SignalHandler handler = getSignalHandler(null);
        logger.exit();
        return handler;
    }
    static Map<String, SignalHandler> handlers;
    static Timer timer;
    static TimerTask timerTask;
    static boolean timerBlocked;
    static final String SYMBOLS;

    private static void registerHandler(String signalName, SignalHandler handler) {
        handler.setSignalName(signalName);
        handlers.put(signalName, handler);
    }

    static void blockChangedHandlers() {
        timerBlocked = true;
    }

    static void unblockChangedHandlers() {
        timerBlocked = false;
    }

    static {
        handlers = new HashMap<>();

        timer = new Timer();
        timerTask = null;
        timerBlocked = false;

        // In addition to kana/kanji, some special symbols can trigger sendEventWhenIdle.
        StringBuilder buf = new StringBuilder();
        buf.append("\u3000\uff01\u201d\uff03\uff04\uff05\uff06\u2019");
        buf.append("\uff08\uff09\uff0a\uff0b\uff0c\u30fc\uff0e\uff0f");
        buf.append("\uff10\uff11\uff12\uff13\uff14\uff15\uff16\uff17");
        buf.append("\uff18\uff19\uff1a\uff1b\uff1c\uff1d\uff1e\uff1f");
        buf.append("\uff20\uff21\uff22\uff23\uff24\uff25\uff26\uff27");
        buf.append("\uff28\uff29\uff2a\uff2b\uff2c\uff2d\uff2e\uff2f");
        buf.append("\uff30\uff31\uff32\uff33\uff34\uff35\uff36\uff37");
        buf.append("\uff38\uff39\uff3a\uff3b\uffe5\uff3d\uff3e\uff3f");
        buf.append("\u2018\u30a2\u30a8\u30a4\u30aa\u30a6\uff5b\uff5c");
        buf.append("\uff5d\uffe3");
        SYMBOLS = buf.toString();

        /**
         * <p>
         * A signal handler which does nothing.</p>
         */
        registerHandler(null, new SignalHandler() {

            @Override
            public void handle(UIControl con, Component widget, Object userData) {
                // do nothing
            }
        });

        /**
         * <p>
         * A signal handler which selects all text on a text field.</p>
         */
        registerHandler("select_all", new SignalHandler() {

            @Override
            public void handle(UIControl con, Component widget, Object userData) {
                JTextField field = (JTextField) widget;
                field.selectAll();
            }
        });

        /**
         * <p>
         * A signal handler which unselect all text on a text field.</p>
         */
        registerHandler("unselect_all", new SignalHandler() {

            @Override
            public void handle(UIControl con, Component widget, Object userData) {
                JTextField field = (JTextField) widget;
                field.select(0, 0);
            }
        });

        /**
         * <p>
         * A signal handler which sends event and parameters.</p>
         */
        final SignalHandler sendEvent = new SignalHandler() {
            @Override
            public void handle(UIControl con, Component widget, Object userData) throws IOException {
                if (timerTask != null) {
                    timerTask.cancel();
                    timerTask = null;
                }                
                widget.requestFocusInWindow();                
                blockChangedHandlers();
                con.sendEvent(widget, userData);
                unblockChangedHandlers();
            }
        };

        final SignalHandler changed = new SignalHandler() {

            @Override
            public void handle(UIControl con, Component widget, Object userData) throws IOException {
                con.addChangedWidget(widget);
            }
        };
        registerHandler("changed", changed);
        registerHandler("entry_changed", changed);
        registerHandler("text_changed", changed);
        registerHandler("button_toggled", changed);
        registerHandler("selection_changed", changed);
        registerHandler("click_column", changed);
        registerHandler("day_selected", changed);
        registerHandler("switch_page", changed);
        registerHandler("no_switch_page", changed);        

        /**
         * <p>
         * A signal handler which sends event only while no other action is
         * performed.</p>
         * <p>
         * System property monsia.send.event.delay can control this
         * behavior.</p>
         */
        SignalHandler sendEventWhenIdle = new SignalHandler() {

            @Override
            public void handle(final UIControl con, final Component widget, final Object userData) {
                if (timerTask != null) {
                    timerTask.cancel();
                    timerTask = null;
                }
                if (timerBlocked) {
                    return;
                }
                timerTask = new TimerTask() {

                    @Override
                    public void run() {
                        JTextComponent text = (JTextComponent) widget;
                        String t = text.getText();
                        int length = t.length();
                        if (length > 0) {
                            char c = t.charAt(length - 1);
                            if (isKana(c) || isKanji(c) || isSymbol(c)) {
                                try {
                                    con.addChangedWidget(widget);
                                    sendEvent.handle(con, widget, userData);
                                } catch (IOException e) {
                                    con.exceptionOccured(e);
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

        registerHandler("send_event", sendEvent);
        registerHandler("send_event_when_idle", sendEventWhenIdle);
        registerHandler("send_event_on_focus_out", sendEvent);      
        
        /**
         * <p>
         * A signal handler which registers the taret widget as "changed" and
         * sends "SELECT" event.</p>
         */
        registerHandler(
                "clist_send_event", new SignalHandler() {

                    @Override
                    public void handle(UIControl con, Component widget, Object userData) throws IOException {
                        con.addChangedWidget(widget);
                        sendEvent.handle(con, widget, "SELECT");
                    }
                }
        );

        registerHandler(
                "notebook_send_event", new SignalHandler() {

                    @Override
                    public void handle(UIControl con, Component widget, Object userData) throws IOException {
                        con.addChangedWidget(widget);
                        sendEvent.handle(con, widget, "SWITCH");
                    }
                }
        );

        registerHandler(
                "table_send_event", new SignalHandler() {

                    @Override
                    public void handle(UIControl con, Component widget, Object userData) throws IOException {
                        con.addChangedWidget(widget);
                        sendEvent.handle(con, widget, userData);
                    }
                }
        );

        /**
         * <p>
         * A signal handler which sends an "ACTIVATE".</p>
         */
        registerHandler(
                "activate_widget", new SignalHandler() {

                    @Override
                    public void handle(UIControl con, Component widget, Object userData) throws IOException {
                        sendEvent.handle(con, widget, "ACTIVATE");
                    }
                }
        );

        /**
         * <p>
         * A signal handler which moves the focus to the widget of given
         * name.</p>
         */
        registerHandler(
                "entry_next_focus", new SignalHandler() {

                    @Override
                    public void handle(UIControl con, Component widget, Object userData
                    ) {
                        Node node = con.getNode(widget);
                        if (node != null) {
                            Component nextWidget = node.getInterface().getWidget(userData.toString());
                            if (nextWidget != null) {
                                nextWidget.requestFocus();
                            }
                        }
                    }
                }
        );

        registerHandler(
                "window_destroy", new SignalHandler() {

                    @Override
                    public void handle(UIControl con, Component widget, Object userData
                    ) {
                        con.getClient().disconnect();
                    }
                }
        );

        /**
         * <p>
         * A signal handler to open the given URL on target JTextPane
         * widget.</p>
         */
        registerHandler(
                "open_browser", new SignalHandler() {

                    @Override
                    public void handle(UIControl con, Component widget, Object userData) throws IOException {
                        if (!(widget instanceof JTextPane)) {
                            return;
                        }
                        JTextPane pane = (JTextPane) widget;
                        URL uri;
                        uri = new URL((String) userData);
                        pane.setPage(uri);
                    }
                }
        );

        registerHandler(
                "keypress_filter", new SignalHandler() {

                    @Override
                    public void handle(UIControl con, Component widget, Object userData
                    ) {
                        Component next = con.getInterface().getWidget((String) userData);
                        next.requestFocus();
                    }
                }
        );

        final SignalHandler doNothing = new SignalHandler() {
            @Override
            public void handle(UIControl con, Component widget, Object userData) throws IOException {
                con.addChangedWidget(widget);
            }
        };

        registerHandler("entry_set_editable", doNothing);
        registerHandler("map_event", doNothing);
        registerHandler("set_focus", doNothing);
        registerHandler("window_close", doNothing);
        registerHandler("press_filter", doNothing);
        registerHandler("gtk_true", doNothing);
    }
}
