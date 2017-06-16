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
package org.montsuqi.monsiaj.monsia;

import org.montsuqi.monsiaj.widgets.ColorButton;
import org.montsuqi.monsiaj.widgets.FileChooserButton;
import org.montsuqi.monsiaj.widgets.PandaCombo;
import org.montsuqi.monsiaj.widgets.PandaCList;
import org.montsuqi.monsiaj.widgets.PandaTimer;
import org.montsuqi.monsiaj.widgets.PandaTable;
import org.montsuqi.monsiaj.widgets.TimerListener;
import org.montsuqi.monsiaj.widgets.TimerEvent;
import org.montsuqi.monsiaj.widgets.Calendar;
import org.montsuqi.monsiaj.widgets.Notebook;
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
import org.montsuqi.monsiaj.client.SignalHandler;
import org.montsuqi.monsiaj.client.UIControl;

/**
 * <p>
 * A class to connect Gtk+ signal names to signal hender objects.</p>
 */
abstract class Connector {

    private static Map<String, Connector> connectors;
    protected static final Logger logger = LogManager.getLogger(Connector.class);

    abstract void connect(UIControl uiControl, Component target, SignalHandler handler, Object other);

    public static Connector getConnector(String signalName) {
        if (connectors.containsKey(signalName)) {
            final Connector connector = connectors.get(signalName);
            logger.exit();
            return connector;
        }
        logger.debug("connector not found for signal {0}", signalName);
        final Connector connector = getConnector(null);
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
    static void invoke(final UIControl con, final SignalHandler handler, final Component target, final Object other) {
        try {
            handler.handle(con, target, other);
        } catch (IOException e) {
            con.exceptionOccured(e);
        }
    }

    private static void registerConnector(String signalName, Connector connector) {
        connectors.put(signalName, connector);
    }

    static {
        connectors = new HashMap<>();

        registerConnector(null, new Connector() {
            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                // do nothing
            }
        });

        registerConnector("clicked", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof AbstractButton)) {
                    return;
                }
                // RadioButton event happens only on toggled.
                if (target instanceof JRadioButton) {
                    return;
                }
                AbstractButton button = (AbstractButton) target;
                button.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent event) {
                        invoke(con, handler, target, other);
                    }
                });
            }
        });

        registerConnector("button_press_event", getConnector("clicked"));  //$NON-NLS-2$

        registerConnector("key_press_event", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                target.addKeyListener(new KeyAdapter() {

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            invoke(con, handler, target, other);
                        }
                    }
                });
            }
        });

        registerConnector("changed", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaCombo) {
                    final PandaCombo combo = (PandaCombo) target;
                    ComboBoxModel model = combo.getModel();
                    final Component c = combo.getEditor().getEditorComponent();
                    model.addListDataListener(new ListDataListener() {

                        @Override
                        public void contentsChanged(ListDataEvent e) {
                            invoke(con, handler, c, other);
                        }

                        @Override
                        public void intervalAdded(ListDataEvent e) {
                            // do nothing
                        }

                        @Override
                        public void intervalRemoved(ListDataEvent e) {
                            // do nothing
                        }
                    });
                    combo.addItemListener(new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            invoke(con, handler, c, other);
                        }
                    });
                } else if (target instanceof JTextComponent) {
                    final JTextComponent text = (JTextComponent) target;
                    text.getDocument().addDocumentListener(new DocumentListener() {

                        @Override
                        public void insertUpdate(DocumentEvent event) {
                            invoke(con, handler, target, other);
                        }

                        @Override
                        public void removeUpdate(DocumentEvent event) {
                            invoke(con, handler, target, other);
                        }

                        @Override
                        public void changedUpdate(DocumentEvent event) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("activate", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaCombo) {
                    PandaCombo combo = (PandaCombo) target;
                    Component c = combo.getEditor().getEditorComponent();
                    connect(con, c, handler, other);
                } else if (target instanceof JTextField) {
                    final JTextField textField = (JTextField) target;
                    textField.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent event) {
                            invoke(con, handler, target, other);
                        }
                    });
                } else if (target instanceof JTextArea) {
                    final JTextArea textArea = (JTextArea) target;
                    textArea.addKeyListener(new KeyListener() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                                invoke(con, handler, target, other);
                            }
                        }

                        @Override
                        public void keyReleased(KeyEvent e) {
                        }

                        @Override
                        public void keyTyped(KeyEvent e) {
                        }
                    });
                } else if (target instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) target;
                    item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent event) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("enter", getConnector("activate"));  //$NON-NLS-2$

        registerConnector("focus_in_event", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {

                    @Override
                    public void focusGained(FocusEvent e) {
                        invoke(con, handler, target, other);
                    }
                });
            }
        });

        registerConnector("focus_out_event", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {

                    @Override
                    public void focusLost(FocusEvent e) {
                        invoke(con, handler, target, other);
                    }
                });
            }
        });

        registerConnector("map_event", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowOpened(WindowEvent e) {
                            invoke(con, handler, target, other);
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {

                        @Override
                        public void componentShown(ComponentEvent e) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("delete_event", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) {
                            invoke(con, handler, target, other);
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {

                        @Override
                        public void componentHidden(ComponentEvent e) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("destroy", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosed(WindowEvent e) {
                            invoke(con, handler, target, other);
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {

                        @Override
                        public void componentHidden(ComponentEvent e) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("set_focus", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {

                    @Override
                    public void focusGained(FocusEvent e) {
                        invoke(con, handler, target, other);
                    }
                });
            }
        });

        registerConnector("select_row", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {

                if (target instanceof PandaCList) {
                    PandaCList table = (PandaCList) target;
                    table.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("unselect_row", new Connector() {

            @Override
            public void connect(UIControl con, Component target, SignalHandler handler, Object other) {
                // XxxSelectionModels don't care selection/unselection so use connectSelectRow
                // Object[] args = { target, handler, other};
                // logger.debug("unselect_row: target={0}, handler={1}, other={2}", args);
                // connectSelectRow(target, handler, other);
            }
        });

        registerConnector("selection_changed", getConnector("select_row"));  //$NON-NLS-2$

        registerConnector("click_column", new Connector() {

            @Override
            public void connect(UIControl con, Component target, SignalHandler handler, Object other) {
                Object[] args = {target, handler, other};
            }
        });

        registerConnector("switch_page", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
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

                    @Override
                    public void stateChanged(ChangeEvent event) {
                        invoke(con, handler, target, other);
                    }
                });
            }
        });

        registerConnector("toggled", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof JToggleButton)) {
                    return;
                }

                final JToggleButton toggle = (JToggleButton) target;
                if (target instanceof JRadioButton) {
                    toggle.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mousePressed(MouseEvent e) {
                            ButtonGroup g = (ButtonGroup) toggle.getClientProperty("group");
                            JRadioButton deselected = null;
                            if (g == null) {
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
                        }
                    });
                } else {
                    toggle.addChangeListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {
                            if (toggle.isSelected()) {
                                invoke(con, handler, target, other);
                            }
                        }
                    });
                }
            }
        });

        registerConnector("timeout", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof PandaTimer)) {
                    return;
                }
                PandaTimer timer = (PandaTimer) target;
                timer.addTimerListener(new TimerListener() {

                    @Override
                    public void timerSignaled(TimerEvent e) {
                        invoke(con, handler, target, other);
                    }
                });
            }
        });

        registerConnector("day_selected", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof Calendar)) {
                    return;
                }
                Calendar cal = (Calendar) target;
                cal.addChangeListener(new ChangeListener() {

                    @Override
                    public void stateChanged(ChangeEvent e) {
                        invoke(con, handler, target, other);
                    }
                });
            }
        });

        registerConnector("selection_get", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) target;
                    item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent event) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("file_set", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof FileChooserButton) {
                    FileChooserButton fcb = (FileChooserButton) target;
                    fcb.getBrowseButton().addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent event) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("color_set", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof ColorButton) {
                    ColorButton cb = (ColorButton) target;
                    cb.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent event) {
                            invoke(con, handler, target, other);
                        }
                    });
                }
            }
        });

        registerConnector("cell_edited", new Connector() {

            @Override
            public void connect(final UIControl con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaTable) {
                    final PandaTable table = (PandaTable) target;
                    table.getModel().addTableModelListener(
                            new TableModelListener() {

                                @Override
                                public void tableChanged(TableModelEvent te) {
                                    if (table.isEnterPressed()) {
                                        int row = te.getLastRow();
                                        int col = te.getColumn();
                                        table.setChangedRow(row);
                                        table.setChangedColumn(col);
                                        table.setChangedValue(table.getStringValueAt(row, col));
                                        invoke(con, handler, target, other);
                                    }
                                    table.setEnterPressed(false);
                                }
                            });
                }
            }
        });
    }
}
