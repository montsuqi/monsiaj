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
package org.montsuqi.monsiaj.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.util.SafeColorDecoder;

public class PandaCList extends JTable {

    protected static final Logger logger = LogManager.getLogger(PandaCList.class);

    public static final int SELECTION_MODE_SINGLE = 1;
    public static final int SELECTION_MODE_MULTI = 2;

    private Color[] bgColors;
    private Color[] fgColors;
    private boolean[] selection;
    private int mode;

    /* mouse selection */
    private int msStartRow;
    private int msPrevRow;
    private boolean msDragged;
    private boolean msValue;

    private Color selectionBGColor;
    private Color selectionFGColor;

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setBGColors(Color[] bgColors) {
        this.bgColors = bgColors;
    }

    public void setFGColors(Color[] fgColors) {
        this.fgColors = fgColors;
    }

    public void setSelection(boolean[] s) {
        this.selection = s;
    }

    public boolean[] getSelection() {
        return this.selection;
    }

    public PandaCList() {
        super();
        setFocusable(true);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setAutoscrolls(true);
        setRowSelectionAllowed(false);
        initActions();

        if (System.getProperty("monsia.pandaclist.rowheight") != null) {
            int rowheight = Integer.parseInt(System.getProperty("monsia.pandaclist.rowheight"));
            this.setRowHeight(rowheight);
        }

        String strColor = System.getProperty("monsia.pandaclist.selection_bg_color");
        if (strColor != null) {
            selectionBGColor = SafeColorDecoder.decode(strColor);
        } else {
            selectionBGColor = new Color(0x33, 0x66, 0xFF);
        }

        strColor = System.getProperty("monsia.pandaclist.selection_fg_color");
        if (strColor != null) {
            selectionFGColor = SafeColorDecoder.decode(strColor);
        } else {
            selectionFGColor = new Color(0xFF, 0xFF, 0xFF);
        }

        if (System.getProperty("monsia.widget.pandaclist.showgrid") == null) {
            this.setShowGrid(false);
        }
        mode = PandaCList.SELECTION_MODE_SINGLE;

        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                msDragged = false;
                int row = PandaCList.this.rowAtPoint(e.getPoint());
                if (row == -1) {
                    return;
                }
                msStartRow = row;
                msPrevRow = row;
                msValue = !selection[row];
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int row = PandaCList.this.rowAtPoint(e.getPoint());
                if (row == -1) {
                    if (msDragged) {
                        PandaCList.this.resizeAndRepaint();
                        PandaCList.this.fireChangeEvent(null);
                        return;
                    } else {
                        return;
                    }
                }
                if (mode == PandaCList.SELECTION_MODE_MULTI) {
                    if (!msDragged) {
                        PandaCList.this.toggleSelection(row);
                    }
                } else {
                    PandaCList.this.singleSelection(row);
                }
                PandaCList.this.resizeAndRepaint();
                PandaCList.this.fireChangeEvent(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                int prev;
                if (mode != PandaCList.SELECTION_MODE_MULTI) {
                    return;
                }
                msDragged = true;
                int row = PandaCList.this.rowAtPoint(e.getPoint());
                if (row == -1) {
                    return;
                }
                if (row == msStartRow) {
                    selection[row] = msValue;
                    return;
                }
                if (row == msPrevRow) {
                    return;
                } else {
                    prev = msPrevRow;
                    msPrevRow = row;
                }

                if (row > msStartRow) {
                    if (row > prev) {
                        selection[row] = msValue;
                    } else {
                        selection[prev] = !msValue;
                    }
                } else {
                    if (row < prev) {
                        selection[row] = msValue;
                    } else {
                        selection[prev] = !msValue;
                    }
                }
                PandaCList.this.resizeAndRepaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
    }

    public void toggleSelection(int row) {
        selection[row] = !selection[row];
    }

    public void singleSelection(int row) {
        for (int i = 0; i < selection.length; i++) {
            selection[i] = i == row;
        }
    }

    private void initActions() {
        ActionMap actions = getActionMap();
        InputMap inputs = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        actions.put("doAction", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int row = PandaCList.this.getSelectedRow();
                if (mode == PandaCList.SELECTION_MODE_MULTI) {
                    PandaCList.this.toggleSelection(row);
                } else {
                    PandaCList.this.singleSelection(row);
                }
                PandaCList.this.resizeAndRepaint();
                fireChangeEvent(new ChangeEvent(PandaCList.this));
            }
        });
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "doAction");
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doAction");
    }

    protected void fireChangeEvent(ChangeEvent e) {
        ChangeListener[] listeners = (ChangeListener[]) listenerList.getListeners(ChangeListener.class);
        for (int i = 0, n = listeners.length; i < n; i++) {
            ChangeListener l = listeners[i];
            l.stateChanged(e);
        }
    }

    @Override
    public Component prepareRenderer(
            TableCellRenderer renderer, int row, int column) {

        Component c = super.prepareRenderer(renderer, row, column);
        if (fgColors != null && row < fgColors.length && fgColors[row] != null) {
            c.setForeground(fgColors[row]);
        } else {
            c.setForeground(Color.BLACK);
        }
        if (selection[row]) {
            c.setBackground(this.selectionBGColor);
            c.setForeground(this.selectionFGColor);
        } else {
            if (bgColors != null && row < bgColors.length && bgColors[row] != null) {
                if (this.isRowSelected(row)) {
                    int r = bgColors[row].getRed();
                    int g = bgColors[row].getGreen();
                    int b = bgColors[row].getBlue();
                    r = r - 0x30 < 0 ? 0 : r - 0x30;
                    g = g - 0x30 < 0 ? 0 : g - 0x30;
                    b = b - 0x30 < 0 ? 0 : b - 0x30;
                    c.setBackground(new Color(r, g, b));
                } else {
                    c.setBackground(bgColors[row]);
                }
            } else {
                c.setBackground(Color.white);
            }
        }
        return c;
    }

    @Override
    public void createDefaultColumnsFromModel() {
        TableColumnModel model = getColumnModel();
        int n = getColumnCount();
        TableCellRenderer[] renderers = new TableCellRenderer[n];
        int[] width = new int[n];
        for (int i = 0; i < n; i++) {
            TableColumn column = model.getColumn(i);
            renderers[i] = column.getHeaderRenderer();
            width[i] = column.getWidth();
        }
        super.createDefaultColumnsFromModel();
        for (int i = 0; i < n; i++) {
            TableColumn column = model.getColumn(i);
            column.setHeaderRenderer(renderers[i]);
            column.setPreferredWidth(width[i]);
            column.setWidth(width[i]);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void registerHeaderComponent(int i, JComponent header) {
        TableCellRenderer renderer = new CListHeaderRenderer(header);
        TableColumn column = columnModel.getColumn(i);
        column.setHeaderRenderer(renderer);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("PandaCList");
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout(10, 5));

        final PandaCList clist = new PandaCList();
        TableColumnModel columnModel = clist.getColumnModel();
        columnModel.addColumn(new TableColumn());
        columnModel.addColumn(new TableColumn());
        columnModel.addColumn(new TableColumn());

        DefaultTableModel tableModel = (DefaultTableModel) clist.getModel();
        tableModel.setColumnCount(3);

        for (int i = 0; i < 50; i++) {
            Object[] rowData = new String[3];
            rowData[0] = Integer.toString(i);
            rowData[1] = Integer.toString(i + 1);
            rowData[2] = Integer.toString(i + 2);
            tableModel.addRow(rowData);
        }

        clist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(clist);
        scroll.setPreferredSize(new Dimension(400, 300));
        container.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        container.add(buttonPanel, BorderLayout.SOUTH);

        JButton button3 = new JButton(new AbstractAction("output") {

            @Override
            public void actionPerformed(ActionEvent ev) {
            }
        });

        buttonPanel.add(button3);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(500, 500);
        frame.setVisible(true);
    }
}
