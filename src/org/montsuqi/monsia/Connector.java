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

package org.montsuqi.monsia;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeSelectionModel;

import org.montsuqi.client.Protocol;
import org.montsuqi.client.SignalHandler;
import org.montsuqi.util.Logger;
import org.montsuqi.widgets.Calendar;
import org.montsuqi.widgets.PandaCombo;
import org.montsuqi.widgets.PandaCList;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.TimerEvent;
import org.montsuqi.widgets.TimerListener;

abstract class Connector {

	private static Map connectors;
	protected static final Logger logger = Logger.getLogger(Connector.class);

	abstract void connect(Protocol con, Component target, SignalHandler handler, Object other);

	public static Connector getConnector(String signalName) {
		if (connectors.containsKey(signalName)) {
			return (Connector)connectors.get(signalName);
		}
		logger.warn("connector not found for signal {0}", signalName); //$NON-NLS-1$
		return getConnector(null);
	}

	static void invoke(final Protocol con, final SignalHandler handler, final Component target, final Object other) {
		try {
			handler.handle(con, target, other);
		} catch (IOException e) {
			throw new HandlerInvocationException(e);
		}
	}

	private static void registerConnector(String signalName, Connector connector) {
		connectors.put(signalName, connector);
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
				if ( ! (target instanceof AbstractButton)) {
					return;
				}
				// RadioButton event happens only on toggled.
				if (target instanceof JRadioButton) {
					return;
				}
				AbstractButton button = (AbstractButton)target;
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						invoke(con, handler, target, other);
					}
				});
			}
		});

		registerConnector("button_press_event", getConnector("clicked")); //$NON-NLS-1$ //$NON-NLS-2$

		registerConnector("key_press_event", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				target.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							invoke(con, handler, target, other);
						}
					}
				});
			}
		});

		registerConnector("changed", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if (target instanceof PandaCombo) {
					final PandaCombo combo = (PandaCombo)target;
					ComboBoxModel model = combo.getModel();
					final Component c = combo.getEditor().getEditorComponent();
					model.addListDataListener(new ListDataListener() {
						public void contentsChanged(ListDataEvent e) {
							invoke(con, handler, c, other);
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
							invoke(con, handler, c, other);
						}
					});
				} else if (target instanceof JTextComponent) {
					final JTextComponent text = (JTextComponent)target;
					text.getDocument().addDocumentListener(new DocumentListener() {
						public void insertUpdate(DocumentEvent event) {
							invoke(con, handler, target, other);
						}
						public void removeUpdate(DocumentEvent event) {
							invoke(con, handler, target, other);
						}
						public void changedUpdate(DocumentEvent event) {
							invoke(con, handler, target, other);
						}
					});
				}
			}
		});

		registerConnector("activate", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if (target instanceof PandaCombo) {
					PandaCombo combo = (PandaCombo)target;
					Component c = combo.getEditor().getEditorComponent();
					connect(con, c, handler, other);
				} else if (target instanceof JTextField) {
					final JTextField textField = (JTextField)target;
					textField.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							invoke(con, handler, target, other);
						}
					});
				} else if (target instanceof JMenuItem) {
					JMenuItem item = (JMenuItem)target;
					item.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							invoke(con, handler, target, other);
						}
					});
				}
			}
		});

		registerConnector("enter", getConnector("activate")); //$NON-NLS-1$ //$NON-NLS-2$

		registerConnector("focus_in_event", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				target.addFocusListener(new FocusAdapter() {
					public void focusGained(FocusEvent e) {
						invoke(con, handler, target, other);
					}
				});
			}
		});

		registerConnector("focus_out_event", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				target.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent e) {
						invoke(con, handler, target, other);
					}
				});
			}
		});

		registerConnector("map_event", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if (target instanceof Window) {
					Window window = (Window)target;
					window.addWindowListener(new WindowAdapter() {
						public void windowOpened(WindowEvent e) {
							invoke(con, handler, target, other);
						}
					});
				} else {
					target.addComponentListener(new ComponentAdapter() {
						public void componentShown(ComponentEvent e) {
							invoke(con, handler, target, other);
						}
					});
				}
			}
		});

		registerConnector("delete_event", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if (target instanceof Window) {
					Window window = (Window)target;
					window.addWindowListener(new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							invoke(con, handler, target, other);
						}
					});
				} else {
					target.addComponentListener(new ComponentAdapter() {
						public void componentHidden(ComponentEvent e) {
							invoke(con, handler, target, other);
						}
					});
				}
			}
		});

		registerConnector("destroy", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if (target instanceof Window) {
					Window window = (Window)target;
					window.addWindowListener(new WindowAdapter() {
						public void windowClosed(WindowEvent e) {
							invoke(con, handler, target, other);
						}
					});
				} else {
					target.addComponentListener(new ComponentAdapter() {
						public void componentHidden(ComponentEvent e) {
							invoke(con, handler, target, other);
						}
					});
				}
			}
		});

		registerConnector("set_focus", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				target.addFocusListener(new FocusAdapter() {
					public void focusGained(FocusEvent e) {
						invoke(con, handler, target, other);
					}
				});
			}
		});

		registerConnector("select_row", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if (target instanceof JTree) {
					JTree tree = (JTree)target;
					TreeSelectionModel model = tree.getSelectionModel();
					model.addTreeSelectionListener(new TreeSelectionListener() {
						public void valueChanged(TreeSelectionEvent e) {
							invoke(con, handler, target, other);
						}
					});
				} else {
					ListSelectionListener listener = new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent e) {
							if ( ! e.getValueIsAdjusting()) {
								invoke(con, handler, target, other);
							}
						}
					};
					if (target instanceof JList) {
						JList list = (JList)target;
						ListSelectionModel model = list.getSelectionModel();
						model.addListSelectionListener(listener);
					} else if (target instanceof PandaCList) {
						PandaCList table = (PandaCList)target;
						ListSelectionModel model = table.getSelectionModel();
						model.addListSelectionListener(listener);
						table.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								invoke(con, handler, target, other);
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
				// logger.debug("selection_change: target={0}, handler={1}, other={2}", args);
				// connectSelectRow(target, handler, other);
			}
		});

		registerConnector("selection_changed", new Connector() { //$NON-NLS-1$
			public void connect(Protocol con, final Component target, final SignalHandler handler, final Object other) {
				Object[] args = { target, handler, other };
				logger.debug("selection_change: target={0}, handler={1}, other={2}", args); //$NON-NLS-1$
			}
		});

		registerConnector("click_column", new Connector() { //$NON-NLS-1$
			public void connect(Protocol con, Component target, SignalHandler handler, Object other) {
				Object[] args = { target, handler, other };
				logger.debug("click_column: target={0}, handler={1}, other={2}", args); //$NON-NLS-1$
			}
		});

		registerConnector("switch_page", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if ( ! (target instanceof JTabbedPane)) {
					return;
				}
				JTabbedPane tabbedPane = (JTabbedPane)target;
				tabbedPane.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent event) {
						invoke(con, handler, target, other);
					}
				});
			}
		});

		registerConnector("toggled", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if ( ! (target instanceof JToggleButton)) {
					return;
				}
				
				final JToggleButton toggle = (JToggleButton)target;
				if (target instanceof JRadioButton) {
					toggle.addMouseListener(new MouseAdapter() {
						public void mousePressed(MouseEvent e) {
							ButtonGroup g = (ButtonGroup)toggle.getClientProperty("group"); //$NON-NLS-1$
							JRadioButton deselected = null;
							if (g == null) {
								return;
							}
							Enumeration elements = g.getElements();
							while (elements.hasMoreElements()) {
								JRadioButton radio = (JRadioButton)elements.nextElement();
								if (radio.isSelected()) {
									deselected = radio;
									break;
								}
							}
							if (deselected == null) {
								return;
							}
							JRadioButton none = (JRadioButton)deselected.getClientProperty("none"); //$NON-NLS-1$
							none.setSelected(true);
							invoke(con, handler, deselected, other);
							SignalHandler sendEvent = SignalHandler.getSignalHandler("send_event"); //$NON-NLS-1$
							assert sendEvent != null;
							invoke(con, sendEvent, deselected, other);

							toggle.setSelected(true);
							invoke(con, handler, target, other);
							invoke(con, sendEvent, target, other);
						}
					});
				} else {
					toggle.addChangeListener(new ChangeListener() {
						public void stateChanged(ChangeEvent e) {
							if (toggle.isSelected()) {
								invoke(con, handler, target, other);
							}
						}
					});
				}
			}
		});

		registerConnector("timeout", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if ( ! (target instanceof PandaTimer)) {
					return;
				}
				PandaTimer timer = (PandaTimer)target;
				timer.addTimerListener(new TimerListener() {
					public void timerSignaled(TimerEvent e) {
						invoke(con, handler, target, other);
					}
				});
			}
		});

		registerConnector("day_selected", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if ( ! (target instanceof Calendar)) {
					return;
				}
				Calendar cal = (Calendar)target;
				cal.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						invoke(con, handler, target, other);
					}
				});
			}
		});

		registerConnector("selection_get", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				// do nothing
			}
		});
	}
}
