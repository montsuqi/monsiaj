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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * <
 * p>
 * A class that simulates Gtk+'s PandaCombo.</p>
 */
public class PandaCombo extends JComboBox {

    class MoveSelectionAction extends AbstractAction {

        private final int move;

        MoveSelectionAction(int move) {
            this.move = move;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            int items = getItemCount();
            if (items <= 1) {
                return;
            }
            int selected = getSelectedIndex();
            int newSelected = selected + move;
            newSelected = newSelected < 0 ? 0 : items <= newSelected ? items - 1 : newSelected;
            setSelectedIndex(newSelected);
            JTextField edit = (JTextField) getEditor().getEditorComponent();
            edit.setText(getSelectedItem().toString());
        }
    }

    public PandaCombo() {
        super();
        setEditor(new PandaComboBoxEditor(this));
        setMaximumRowCount(16);
        initActions();
    }

    private void initActions() {
        ActionMap actionMap = getActionMap();
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        for (InputMap parent = inputMap; parent != null; parent = parent.getParent()) {
            parent.remove(upKey);
            parent.remove(downKey);
            parent.remove(enterKey);
        }
        actionMap.put("moveSelectionUp", new MoveSelectionAction(-1));
        actionMap.put("moveSelectionDown", new MoveSelectionAction(1));

        inputMap.put(upKey, "moveSelectionUp");
        inputMap.put(downKey, "moveSelectionDown");

        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                // do nothing
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                if (getItemCount() > 1) {
                    JTextField text = (JTextField) getEditor().getEditorComponent();
                    text.postActionEvent();
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent arg0) {
                // do nothing
            }
        });
    }
}

class PandaComboBoxEditor extends BasicComboBoxEditor {

    JComboBox combo;

    public PandaComboBoxEditor(final JComboBox combo) {
        editor = new BorderlessPandaEntry("", 9);
        this.combo = combo;
        editor.putClientProperty("panda combo editor", Boolean.TRUE);
    }

    class BorderlessPandaEntry extends PandaEntry {

        public BorderlessPandaEntry(String value, int n) {
            super(value, n);
            InputMap inputMap = getInputMap();
            inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
            inputMap.remove(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
        }

        @Override
        protected void processKeyEvent(KeyEvent e) {
            if (!selectWithKey(e)) {
                super.processKeyEvent(e);
            }
        }

        private boolean selectWithKey(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_TYPED) {
                ComboBoxModel model = combo.getModel();
                int pos = getSelectionStart();
                if (pos == 0) {
                    pos = getCaretPosition();
                }
                String prefix = getText().substring(0, pos) + e.getKeyChar();
                for (int i = 0, n = model.getSize(); i < n; i++) {
                    Object o = model.getElementAt(i);
                    String s = o.toString();
                    if (s.startsWith(prefix)) {
                        combo.setSelectedIndex(i);
                        setText(s);
                        setSelectionStart(pos + 1);
                        setSelectionEnd(getText().length());
                        return true;
                    }
                }
            } else if (e.getID() == KeyEvent.KEY_PRESSED
                    && e.getKeyCode() == KeyEvent.VK_UNDEFINED) {
                ComboBoxModel model = combo.getModel();
                int pos = getText().length();
                String prefix = getText().substring(0, pos);
                for (int i = 0, n = model.getSize(); i < n; i++) {
                    Object o = model.getElementAt(i);
                    String s = o.toString();
                    if (s.startsWith(prefix)) {
                        combo.setSelectedIndex(i);
                        setText(s);
                        setCaretPosition(pos);
                        return true;
                    }
                }
            }
            return false;
        }

        // workaround for 4530952
        @Override
        public void setText(String s) {
            if (getText().equals(s)) {
                return;
            }
            super.setText(s);
        }
    }
}
