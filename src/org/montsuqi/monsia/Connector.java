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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.montsuqi.widgets.CalendarEvent;
import org.montsuqi.widgets.CalendarListener;
import org.montsuqi.widgets.PandaTimer;
import org.montsuqi.widgets.TimerEvent;
import org.montsuqi.widgets.TimerListener;

abstract class Connector {

	private static Map connectors;
	protected Logger logger;

	abstract void connect(Protocol con, Component target, SignalHandler handler, Object other);

	protected Connector() {
		logger = Logger.getLogger(Connector.class);
	}

	public static Connector getConnector(String signalName) throws NoSuchMethodException {
		if (connectors.containsKey(signalName)) {
			return (Connector)connectors.get(signalName);
		}
		String message = Messages.getString("Connector.connector_not_found"); //$NON-NLS-1$
		message = MessageFormat.format(message, new Object[] { signalName });
		throw new NoSuchMethodException(message);
	}

	static void invoke(Protocol con, SignalHandler handler, Component target, Object other) {
		try {
			handler.handle(con, target, other);
		} catch (IOException e) {
			throw new InterfaceBuildingException(e);
		}
	}

	private static void registerConnector(String signalName, Connector connector) {
		connectors.put(signalName, connector);
	}

	static {
		connectors = new HashMap();

		registerConnector("clicked", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if ( ! (target instanceof AbstractButton)) {
					return;
				}
				AbstractButton button = (AbstractButton)target;
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						System.out.println("invoking clicked for " + target.getName());
						invoke(con, handler, target, other);
					}
				});
			}
		});

		try {
			registerConnector("button_press_event", getConnector("clicked")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NoSuchMethodException e) {
			throw new ExceptionInInitializerError(e);
		}

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
				if (target instanceof JTextComponent) {
					JTextComponent text = (JTextComponent)target;
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
				} else if (target instanceof JComboBox) {
					JComboBox combo = (JComboBox)target;
					combo.addItemListener(new ItemListener() {
						public void itemStateChanged(ItemEvent e) {
							invoke(con, handler, target, other);
						}
					});
				}
			}
		});

		registerConnector("activate", new Connector() { //$NON-NLS-1$
			public void connect(final Protocol con, final Component target, final SignalHandler handler, final Object other) {
				if (target instanceof JTextField) {
					JTextField textField = (JTextField)target;
					textField.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							invoke(con, handler, target, other);
						}
					});
				} else if (target instanceof JComboBox) {
					JComboBox combo = (JComboBox)target;
					combo.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							invoke(con, handler, target, other);
						}
					});
				}
			}
		});

		try {
			registerConnector("enter", getConnector("activate")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NoSuchMethodException e) {
			throw new ExceptionInInitializerError(e);
		}

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
					} else if (target instanceof JTable) {
						JTable table = (JTable)target;
						ListSelectionModel model = table.getSelectionModel();
						model.addListSelectionListener(listener);
					}
				}
			}
		});

		registerConnector("unselect_row", new Connector() { //$NON-NLS-1$
			public void connect(Protocol con, Component target, SignalHandler handler, Object other) {
				// XxxSelectionModels don't care selection/unselection so use connectSelectRow
				// logger.debug("selection_change: target={0}, handler={1}, other={2}", new Object[]{target, handler, other});
				// connectSelectRow(target, handler, other);
			}
		});

		registerConnector("selection_changed", new Connector() { //$NON-NLS-1$
			public void connect(Protocol con, final Component target, final SignalHandler handler, final Object other) {
				logger.debug("selection_change: target={0}, handler={1}, other={2}", new Object[]{target, handler, other}); //$NON-NLS-1$
			}
		});

		registerConnector("click_column", new Connector() { //$NON-NLS-1$
			public void connect(Protocol con, Component target, SignalHandler handler, Object other) {
				logger.debug("click_column: target={0}, handler={1}, other={2}", new Object[]{target, handler, other}); //$NON-NLS-1$
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
				final JToggleButton toggleButton = (JToggleButton)target;
				toggleButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println("invoking toggled for " + target.getName() + " -> " + toggleButton.isSelected());
						invoke(con, handler, target, other);
					}
				});
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
				cal.addCalendarListener(new CalendarListener() {
					public void previousMonth(CalendarEvent e) {
						// do nothing
					}
					public void nextMonth(CalendarEvent e) {
						// do nothing
					}
					public void daySelected(CalendarEvent e) {
						invoke(con, handler, target, other);
					}
				});
			}
		});
	}
}
