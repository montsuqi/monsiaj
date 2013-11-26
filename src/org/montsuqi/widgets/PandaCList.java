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
package org.montsuqi.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class PandaCList extends JTable implements PropertyChangeListener {

    class MoveAction extends AbstractAction {

        int rowMove;
        int columnMove;

        MoveAction(int rowMove, int columnMove) {
            this.rowMove = rowMove;
            this.columnMove = columnMove;
        }

        public void actionPerformed(final ActionEvent e) {
            int newRow = moveIndex((PandaCListSelectionModel) getSelectionModel(), rowMove, getRowCount() - 1);
            int newColumn = moveIndex((PandaCListSelectionModel) getColumnModel().getSelectionModel(), columnMove, getColumnCount() - 1);
            changeSelection(newRow, newColumn, false, true);
            repaint();
        }

        private int moveIndex(PandaCListSelectionModel selections, int move, int max) {
            boolean notify = selections.isNotifySelectionChange();
            selections.setNotifySelectionChange(false);
            int selected = selections.getMinSelectionIndex();
            selections.removeSelectionInterval(selected, selected);
            selected += move;
            selected = selected < 0 ? 0 : max < selected ? max : selected;
            selections.setSelectionInterval(selected, selected);
            selections.setNotifySelectionChange(notify);
            return selected;
        }
    }
    private Color[] bgColors;
    private Color[] fgColors;

    public void setBGColors(Color[] bgColors) {
        this.bgColors = bgColors;
    }

    public void setFGColors(Color[] fgColors) {
        this.fgColors = fgColors;
    }

    public PandaCList() {
        super();
        setFocusable(false);
        addPropertyChangeListener("model", this); //$NON-NLS-1$
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setAutoscrolls(true);
        initActions();
        if (System.getProperty("monsia.widget.pandaclist.showgrid") == null) {
            this.setShowGrid(false);
        }
    }

    private void initActions() {
        ActionMap actions = getActionMap();
        InputMap inputs = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        for (InputMap parent = inputs; parent != null; parent = parent.getParent()) {
            if (parent.get(enterKey) != null) {
                parent.remove(enterKey);
            }
        }

        actions.put("focusOutNext", new FocusOutNextAction()); //$NON-NLS-1$
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "focusOutNext"); //$NON-NLS-1$

        actions.put("focusOutPrevious", new FocusOutPreviousAction()); //$NON-NLS-1$
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "focusOutPrevious"); //$NON-NLS-1$

        actions.put("moveUp", new MoveAction(-1, 0)); //$NON-NLS-1$
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUp"); //$NON-NLS-1$

        actions.put("moveDown", new MoveAction(1, 0)); //$NON-NLS-1$
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDown"); //$NON-NLS-1$

        actions.put("moveLeft", new MoveAction(0, -1)); //$NON-NLS-1$
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft"); //$NON-NLS-1$

        actions.put("moveRight", new MoveAction(0, 1)); //$NON-NLS-1$
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight"); //$NON-NLS-1$

        actions.put("doAction", new AbstractAction() { //$NON-NLS-1$

            public void actionPerformed(ActionEvent e) {
                fireActionEvent(e);
            }
        });
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "doAction"); //$NON-NLS-1$
    }

    protected void fireActionEvent(ActionEvent e) {
        ActionListener[] listeners = (ActionListener[]) listenerList.getListeners(ActionListener.class);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].actionPerformed(e);
        }
    }

    @Override
    public Component prepareRenderer(
            TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (fgColors != null && row < fgColors.length && fgColors[row] != null) {
            c.setForeground(fgColors[row]);
        }
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
    protected TableColumnModel createDefaultColumnModel() {
        TableColumnModel model = super.createDefaultColumnModel();
        model.setSelectionModel(createDefaultSelectionModel());
        return model;
    }

    @Override
    protected ListSelectionModel createDefaultSelectionModel() {
        return new PandaCListSelectionModel();
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        super.changeSelection(rowIndex, columnIndex, true, extend);
    }

    public void registerHeaderComponent(int i, JComponent header) {
        TableCellRenderer renderer = new CListHeaderRenderer(header);
        TableColumn column = columnModel.getColumn(i);
        column.setHeaderRenderer(renderer);
    }

    public void addActionListener(ActionListener listener) {
        listenerList.add(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener) {
        listenerList.remove(ActionListener.class, listener);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if (!name.equals("model")) { //$NON-NLS-1$
            return;
        }
        TableModel oldModel = (TableModel) e.getOldValue();
        if (oldModel != null) {
            oldModel.removeTableModelListener(this);
        }
        TableModel newModel = (TableModel) e.getNewValue();
        if (newModel != null) {
            newModel.addTableModelListener(this);
        }
    }
}
