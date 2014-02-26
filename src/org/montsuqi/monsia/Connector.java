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
package org.montsuqi.monsia;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.*;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.SignalHandler;
import org.montsuqi.widgets.*;

/**
 * <p>
 * A class to connect Gtk+ signal names to signal hender objects.</p>
 */
abstract class Connector {

    private static Map connectors;
    protected static final Logger logger = LogManager.getLogger(Connector.class);

    abstract void connect(Protocol con, Component target, SignalHandler handler, Object other);

    public static Connector getConnector(String signalName) {
        logger.entry(signalName);
        if (connectors.containsKey(signalName)) {
            final Connector connector = (Connector) connectors.get(signalName);
            logger.exit();
            return connector;
        }
        logger.debug("connector not found for signal {0}", signalName); 
        final Connector connector = getConnector(null);
        logger.exit();
        return connector;
    }

    /**
     * <p>
     * A helper method which invokes handler's handle method.</p>
     * <p>
     * This method wraps the sequence of invocation of a handler's handle method
     * and its eception handling. All IOExceptions are catched and notified via
     * exceptionOccured.</p>
     */
    static void invoke(final Protocol con, final SignalHandler handler, final Component target, final Object other) {
        logger.entry(new Object[]{handler, target, other});
        try {
            handler.handle(con, target, other);
        } catch (IOException e) {
            con.exceptionOccured(e);
        }
        logger.exit();
    }

    private static void registerConnector(String signalName, Connector connector) {
        logger.entry(signalName, connector);
        connectors.put(signalName, connector);
        logger.exit();
    }

    static {
        connectors = new HashMap();

        registerConnector(null, new Connector() {

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                // do nothing
            }
        });

        registerConnector("clicked", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof AbstractButton)) {
                    return;
                }
                // RadioButton event happens only on toggled.
                if (target instanceof JRadioButton) {
                    return;
                }
                AbstractButton button = (AbstractButton) target;
                button.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        logger.entry();
                        invoke(con, handler, target, other);
                        logger.exit();
                    }
                });
            }
        });

        registerConnector("button_press_event", getConnector("clicked"));  //$NON-NLS-2$

        registerConnector("key_press_event", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                target.addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyPressed(KeyEvent e) {
                        logger.entry();
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            invoke(con, handler, target, other);
                        }
                        logger.exit();
                    }
                });
            }
        });

        registerConnector("changed", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaCombo) {
                    final PandaCombo combo = (PandaCombo) target;
                    ComboBoxModel model = combo.getModel();
                    final Component c = combo.getEditor().getEditorComponent();
                    model.addListDataListener(new ListDataListener() {

                        public void contentsChanged(ListDataEvent e) {
                            logger.entry();
                            invoke(con, handler, c, other);
                            logger.exit();
                        }

                        public void intervalAdded(ListDataEvent e) {
                            // do nothing
                        }

                        public void intervalRemoved(ListDataEvent e) {
                            // do nothing
                        }
                    });
                    combo.addItemListener(new ItemListener() {

                        public void itemStateChanged(ItemEvent e) {
                            logger.entry();
                            invoke(con, handler, c, other);
                            logger.exit();
                        }
                    });
                } else if (target instanceof JTextComponent) {
                    final JTextComponent text = (JTextComponent) target;
                    text.getDocument().addDocumentListener(new DocumentListener() {

                        public void insertUpdate(DocumentEvent event) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }

                        public void removeUpdate(DocumentEvent event) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }

                        public void changedUpdate(DocumentEvent event) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("activate", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaCombo) {
                    PandaCombo combo = (PandaCombo) target;
                    Component c = combo.getEditor().getEditorComponent();
                    connect(con, c, handler, other);
                } else if (target instanceof JTextField) {
                    final JTextField textField = (JTextField) target;
                    textField.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                } else if (target instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) target;
                    item.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("enter", getConnector("activate"));  //$NON-NLS-2$

        registerConnector("focus_in_event", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {

                    @Override
                    public void focusGained(FocusEvent e) {
                        logger.entry();
                        invoke(con, handler, target, other);
                        logger.exit();
                    }
                });
            }
        });

        registerConnector("focus_out_event", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {

                    @Override
                    public void focusLost(FocusEvent e) {
                        logger.entry();
                        invoke(con, handler, target, other);
                        logger.exit();
                    }
                });
            }
        });

        registerConnector("map_event", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowOpened(WindowEvent e) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {

                        @Override
                        public void componentShown(ComponentEvent e) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("delete_event", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {

                        @Override
                        public void componentHidden(ComponentEvent e) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("destroy", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosed(WindowEvent e) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {

                        @Override
                        public void componentHidden(ComponentEvent e) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("set_focus", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {

                    @Override
                    public void focusGained(FocusEvent e) {
                        logger.entry();
                        invoke(con, handler, target, other);
                        logger.exit();
                    }
                });
            }
        });

        registerConnector("select_row", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaCList) {
                    PandaCList table = (PandaCList) target;
                    table.addChangeListener(new ChangeListener() {
                        public void stateChanged(ChangeEvent e) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("unselect_row", new Connector() { 

            public void connect(Protocol con, Component target, SignalHandler handler, Object other) {
                // XxxSelectionModels don't care selection/unselection so use connectSelectRow
                // Object[] args = { target, handler, other};
                // logger.debug("unselect_row: target={0}, handler={1}, other={2}", args);
                // connectSelectRow(target, handler, other);
            }
        });

        registerConnector("selection_changed", getConnector("select_row"));  //$NON-NLS-2$

        registerConnector("click_column", new Connector() { 

            public void connect(Protocol con, Component target, SignalHandler handler, Object other) {
                Object[] args = {target, handler, other};
                logger.debug("click_column: target={0}, handler={1}, other={2}", args); 
            }
        });

        registerConnector("switch_page", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof JTabbedPane)) {
                    return;
                }

                if (handler.getSignalName().equals("no_switch_page")) {
                    Notebook notebook = (Notebook) target;
                    notebook.setSwitchPage(false);
                    return;
                }

                JTabbedPane tabbedPane = (JTabbedPane) target;
                tabbedPane.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent event) {
                        logger.entry();
                        invoke(con, handler, target, other);
                        logger.exit();
                    }
                });
            }
        });

        registerConnector("toggled", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof JToggleButton)) {
                    return;
                }

                final JToggleButton toggle = (JToggleButton) target;
                if (target instanceof JRadioButton) {
                    toggle.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mousePressed(MouseEvent e) {
                            logger.entry();
                            ButtonGroup g = (ButtonGroup) toggle.getClientProperty("group"); 
                            JRadioButton deselected = null;
                            if (g == null) {
                                logger.exit();
                                return;
                            }
                            Enumeration elements = g.getElements();
                            while (elements.hasMoreElements()) {
                                JRadioButton radio = (JRadioButton) elements.nextElement();
                                if (radio.isSelected()) {
                                    deselected = radio;
                                    break;
                                }
                            }
                            if (deselected == null) {
                                logger.exit();
                                return;
                            }
                            JRadioButton none = (JRadioButton) deselected.getClientProperty("none"); 
                            none.setSelected(true);
                            final Object o = "CLICKED"; 
                            invoke(con, handler, deselected, o);
                            SignalHandler sendEvent = SignalHandler.getSignalHandler("send_event"); 
                            assert sendEvent != null;
                            invoke(con, sendEvent, deselected, o);

                            toggle.setSelected(true);
                            invoke(con, handler, target, o);
                            invoke(con, sendEvent, target, o);
                            logger.exit();
                        }
                    });
                } else {
                    toggle.addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent e) {
                            logger.entry();
                            if (toggle.isSelected()) {
                                invoke(con, handler, target, other);
                            }
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("timeout", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof PandaTimer)) {
                    return;
                }
                PandaTimer timer = (PandaTimer) target;
                timer.addTimerListener(new TimerListener() {

                    public void timerSignaled(TimerEvent e) {
                        logger.entry();
                        invoke(con, handler, target, other);
                        logger.exit();
                    }
                });
            }
        });

        registerConnector("day_selected", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof Calendar)) {
                    return;
                }
                Calendar cal = (Calendar) target;
                cal.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        logger.entry();
                        invoke(con, handler, target, other);
                        logger.exit();
                    }
                });
            }
        });

        registerConnector("selection_get", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) target;
                    item.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("file_set", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof FileChooserButton) {
                    FileChooserButton fcb = (FileChooserButton) target;
                    fcb.getBrowseButton().addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("color_set", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof ColorButton) {
                    ColorButton cb = (ColorButton) target;
                    cb.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            logger.entry();
                            invoke(con, handler, target, other);
                            logger.exit();
                        }
                    });
                }
            }
        });

        registerConnector("cell_edited", new Connector() { 

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaTable) {
                    final PandaTable table = (PandaTable) target;
                    table.getModel().addTableModelListener(
                            new TableModelListener() {

                                public void tableChanged(TableModelEvent te) {
                                    logger.entry();
                                    if (table.isEnterPressed()) {
                                        int row = te.getLastRow();
                                        int col = te.getColumn();
                                        table.setChangedRow(te.getLastRow());
                                        table.setChangedColumn(te.getColumn());
                                        table.setChangedValue(table.getStringValueAt(row, col));
                                        invoke(con, handler, target, other);
                                    }
                                    table.setEnterPressed(false);
                                    logger.exit();
                                }
                            });
                }
            }
        });
    }
}
