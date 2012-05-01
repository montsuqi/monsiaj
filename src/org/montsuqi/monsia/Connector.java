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
import javax.swing.tree.TreeSelectionModel;
import org.montsuqi.client.Protocol;
import org.montsuqi.client.SignalHandler;
import org.montsuqi.util.Logger;
import org.montsuqi.widgets.*;

/**
 * <p>A class to connect Gtk+ signal names to signal hender objects.</p>
 */
abstract class Connector {
    
    private static Map connectors;
    protected static final Logger logger = Logger.getLogger(Connector.class);
    
    abstract void connect(Protocol con, Component target, SignalHandler handler, Object other);
    
    public static Connector getConnector(String signalName) {
        logger.enter(signalName);
        if (connectors.containsKey(signalName)) {
            final Connector connector = (Connector) connectors.get(signalName);
            logger.leave();
            return connector;
        }
        logger.info("connector not found for signal {0}", signalName); //$NON-NLS-1$
        final Connector connector = getConnector(null);
        logger.leave();
        return connector;
    }

    /**
     * <p>A helper method which invokes handler's handle method.</p> <p>This
     * method wraps the sequence of invocation of a handler's handle method and
     * its eception handling. All IOExceptions are catched and notified via
     * exceptionOccured.</p>
     */
    static void invoke(final Protocol con, final SignalHandler handler, final Component target, final Object other) {
        logger.enter(new Object[]{handler, target, other});
        try {
            handler.handle(con, target, other);
        } catch (IOException e) {
            con.exceptionOccured(e);
        }
        logger.leave();
    }
    
    private static void registerConnector(String signalName, Connector connector) {
        logger.enter(signalName, connector);
        connectors.put(signalName, connector);
        logger.leave();
    }
    
    static {
        connectors = new HashMap();
        
        registerConnector(null, new Connector() {
            
            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                // do nothing
            }
        });
        
        registerConnector("clicked", new Connector() { //$NON-NLS-1$

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
                        logger.enter();
                        invoke(con, handler, target, other);
                        logger.leave();
                    }
                });
            }
        });
        
        registerConnector("button_press_event", getConnector("clicked")); //$NON-NLS-1$ //$NON-NLS-2$

        registerConnector("key_press_event", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                target.addKeyListener(new KeyAdapter() {
                    
                    @Override
                    public void keyPressed(KeyEvent e) {
                        logger.enter();
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            invoke(con, handler, target, other);
                        }
                        logger.leave();
                    }
                });
            }
        });
        
        registerConnector("changed", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaCombo) {
                    final PandaCombo combo = (PandaCombo) target;
                    ComboBoxModel model = combo.getModel();
                    final Component c = combo.getEditor().getEditorComponent();
                    model.addListDataListener(new ListDataListener() {
                        
                        public void contentsChanged(ListDataEvent e) {
                            logger.enter();
                            invoke(con, handler, c, other);
                            logger.leave();
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
                            logger.enter();
                            invoke(con, handler, c, other);
                            logger.leave();
                        }
                    });
                } else if (target instanceof JTextComponent) {
                    final JTextComponent text = (JTextComponent) target;
                    text.getDocument().addDocumentListener(new DocumentListener() {
                        
                        public void insertUpdate(DocumentEvent event) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                        
                        public void removeUpdate(DocumentEvent event) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                        
                        public void changedUpdate(DocumentEvent event) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                }
            }
        });
        
        registerConnector("activate", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaCombo) {
                    PandaCombo combo = (PandaCombo) target;
                    Component c = combo.getEditor().getEditorComponent();
                    connect(con, c, handler, other);
                } else if (target instanceof JTextField) {
                    final JTextField textField = (JTextField) target;
                    textField.addActionListener(new ActionListener() {
                        
                        public void actionPerformed(ActionEvent event) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                } else if (target instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) target;
                    item.addActionListener(new ActionListener() {
                        
                        public void actionPerformed(ActionEvent event) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                }
            }
        });
        
        registerConnector("enter", getConnector("activate")); //$NON-NLS-1$ //$NON-NLS-2$

        registerConnector("focus_in_event", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {
                    
                    @Override
                    public void focusGained(FocusEvent e) {
                        logger.enter();
                        invoke(con, handler, target, other);
                        logger.leave();
                    }
                });
            }
        });
        
        registerConnector("focus_out_event", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {
                    
                    @Override
                    public void focusLost(FocusEvent e) {
                        logger.enter();
                        invoke(con, handler, target, other);
                        logger.leave();
                    }
                });
            }
        });
        
        registerConnector("map_event", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {
                        
                        @Override
                        public void windowOpened(WindowEvent e) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {
                        
                        @Override
                        public void componentShown(ComponentEvent e) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                }
            }
        });
        
        registerConnector("delete_event", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {
                        
                        @Override
                        public void windowClosing(WindowEvent e) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {
                        
                        @Override
                        public void componentHidden(ComponentEvent e) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                }
            }
        });
        
        registerConnector("destroy", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof Window) {
                    Window window = (Window) target;
                    window.addWindowListener(new WindowAdapter() {
                        
                        @Override
                        public void windowClosed(WindowEvent e) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                } else {
                    target.addComponentListener(new ComponentAdapter() {
                        
                        @Override
                        public void componentHidden(ComponentEvent e) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                }
            }
        });
        
        registerConnector("set_focus", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                target.addFocusListener(new FocusAdapter() {
                    
                    @Override
                    public void focusGained(FocusEvent e) {
                        logger.enter();
                        invoke(con, handler, target, other);
                        logger.leave();
                    }
                });
            }
        });
        
        registerConnector("select_row", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof JTree) {
                    JTree tree = (JTree) target;
                    TreeSelectionModel model = tree.getSelectionModel();
                    model.addTreeSelectionListener(new TreeSelectionListener() {
                        
                        public void valueChanged(TreeSelectionEvent e) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                } else {
                    ListSelectionListener listener = new ListSelectionListener() {
                        
                        public void valueChanged(ListSelectionEvent e) {
                            logger.enter();
                            if (!e.getValueIsAdjusting()) {
                                invoke(con, handler, target, other);
                            }
                            logger.leave();
                        }
                    };
                    if (target instanceof JList) {
                        JList list = (JList) target;
                        ListSelectionModel model = list.getSelectionModel();
                        model.addListSelectionListener(listener);
                    } else if (target instanceof PandaCList) {
                        PandaCList table = (PandaCList) target;
                        ListSelectionModel model = table.getSelectionModel();
                        model.addListSelectionListener(listener);
                        table.addActionListener(new ActionListener() {
                            
                            public void actionPerformed(ActionEvent arg0) {
                                logger.enter();
                                invoke(con, handler, target, other);
                                logger.leave();
                            }
                        });
                    }
                }
            }
        });
        
        registerConnector("unselect_row", new Connector() { //$NON-NLS-1$

            public void connect(Protocol con, Component target, SignalHandler handler, Object other) {
                // XxxSelectionModels don't care selection/unselection so use connectSelectRow
                // Object[] args = { target, handler, other};
                // logger.debug("unselect_row: target={0}, handler={1}, other={2}", args);
                // connectSelectRow(target, handler, other);
            }
        });
        
        registerConnector("selection_changed", getConnector("select_row")); //$NON-NLS-1$ //$NON-NLS-2$

        registerConnector("click_column", new Connector() { //$NON-NLS-1$

            public void connect(Protocol con, Component target, SignalHandler handler, Object other) {
                Object[] args = {target, handler, other};
                logger.debug("click_column: target={0}, handler={1}, other={2}", args); //$NON-NLS-1$
            }
        });
        
        registerConnector("switch_page", new Connector() { //$NON-NLS-1$

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
                        logger.enter();
                        invoke(con, handler, target, other);
                        logger.leave();
                    }
                });
            }
        });
        
        registerConnector("toggled", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof JToggleButton)) {
                    return;
                }
                
                final JToggleButton toggle = (JToggleButton) target;
                if (target instanceof JRadioButton) {
                    toggle.addMouseListener(new MouseAdapter() {
                        
                        @Override
                        public void mousePressed(MouseEvent e) {
                            logger.enter();
                            ButtonGroup g = (ButtonGroup) toggle.getClientProperty("group"); //$NON-NLS-1$
                            JRadioButton deselected = null;
                            if (g == null) {
                                logger.leave();
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
                                logger.leave();
                                return;
                            }
                            JRadioButton none = (JRadioButton) deselected.getClientProperty("none"); //$NON-NLS-1$
                            none.setSelected(true);
                            final Object o = "CLICKED"; //$NON-NLS-1$
                            invoke(con, handler, deselected, o);
                            SignalHandler sendEvent = SignalHandler.getSignalHandler("send_event"); //$NON-NLS-1$
                            assert sendEvent != null;
                            invoke(con, sendEvent, deselected, o);
                            
                            toggle.setSelected(true);
                            invoke(con, handler, target, o);
                            invoke(con, sendEvent, target, o);
                            logger.leave();
                        }
                    });
                } else {
                    toggle.addChangeListener(new ChangeListener() {
                        
                        public void stateChanged(ChangeEvent e) {
                            logger.enter();
                            if (toggle.isSelected()) {
                                invoke(con, handler, target, other);
                            }
                            logger.leave();
                        }
                    });
                }
            }
        });
        
        registerConnector("timeout", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof PandaTimer)) {
                    return;
                }
                PandaTimer timer = (PandaTimer) target;
                timer.addTimerListener(new TimerListener() {
                    
                    public void timerSignaled(TimerEvent e) {
                        logger.enter();
                        invoke(con, handler, target, other);
                        logger.leave();
                    }
                });
            }
        });
        
        registerConnector("day_selected", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (!(target instanceof Calendar)) {
                    return;
                }
                Calendar cal = (Calendar) target;
                cal.addChangeListener(new ChangeListener() {
                    
                    public void stateChanged(ChangeEvent e) {
                        logger.enter();
                        invoke(con, handler, target, other);
                        logger.leave();
                    }
                });
            }
        });
        
        registerConnector("selection_get", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof JMenuItem) {
                    JMenuItem item = (JMenuItem) target;
                    item.addActionListener(new ActionListener() {
                        
                        public void actionPerformed(ActionEvent event) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                }
            }
        });
        
        registerConnector("file_set", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof FileChooserButton) {
                    FileChooserButton fcb = (FileChooserButton) target;
                    fcb.getBrowseButton().addActionListener(new ActionListener() {
                        
                        public void actionPerformed(ActionEvent event) {
                            logger.enter();
                            invoke(con, handler, target, other);
                            logger.leave();
                        }
                    });
                }
            }
        });
        
        registerConnector("cell_edited", new Connector() { //$NON-NLS-1$

            public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
                if (target instanceof PandaTable) {
                    final PandaTable table = (PandaTable) target;
                    table.getModel().addTableModelListener(
                            new TableModelListener() {
                                
                                public void tableChanged(TableModelEvent te) {
                                    logger.enter();
                                    if (table.isEnterPressed()) {
                                        int row = te.getLastRow();
                                        int col = te.getColumn();
                                        table.changedRow = te.getLastRow();
                                        table.changedColumn = te.getColumn();
                                        table.changedValue = table.getStringValueAt(row, col);
                                        invoke(con, handler, target, other);
                                    }
                                    table.setEnterPressed(false);
                                    logger.leave();
                                }
                            });
                }
            }
        });
    }
}
