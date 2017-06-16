/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.widgets;

/**
 *
 * @author mihara
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.im.InputContext;
import java.awt.im.InputSubset;
import java.util.Locale;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.montsuqi.monsiaj.util.SafeColorDecoder;
import org.montsuqi.monsiaj.util.SystemEnvironment;

public class PandaTable extends JTable {

    private class PandaTableModel extends DefaultTableModel {

        private final String[] types;
        private final String[] titles;

        public PandaTableModel() {
            super();

            this.setColumnCount(1);
            this.setRowCount(1);

            types = new String[MAX_COLS];
            titles = new String[MAX_COLS];
            for (int i = 0; i < MAX_COLS; i++) {
                types[i] = "text";
                titles[i] = "title" + i;
            }
        }

        public void setColumns(int cols) {
            if (cols < MAX_COLS) {
                this.setColumnCount(cols);

            } else {
                this.setColumnCount(MAX_COLS);
            }
            setRows(this.getRowCount());
        }

        public void setRows(int rows) {
            this.setRowCount(rows);
            for (int i = 0; i < rows; i++) {
                int cols = this.getColumnCount();
                for (int j = 0; j < cols; j++) {
                    this.setValueAt("", i, j);
                }
            }
        }

        public void setTypes(String[] types) {
            for (int i = 0; i < this.types.length && i < types.length; i++) {
                this.types[i] = types[i];
            }
            setColumns(this.getColumnCount());
        }

        public String[] getTypes() {
            int cols = this.getColumnCount();
            String[] ret = new String[cols];
            System.arraycopy(this.types, 0, ret, 0, cols);
            return ret;
        }

        public void setTitles(String[] titles) {
            for (int i = 0; i < this.titles.length && i < titles.length; i++) {
                this.titles[i] = titles[i];
            }
            setColumns(this.getColumnCount());
        }

        public void setRow(int row, String[] rowdata) {
            if (0 <= row && row < this.getRowCount()) {
                for (int i = 0; i < this.getColumnCount(); i++) {
                    setValueAt(rowdata[i], row, i);
                }
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return !this.types[col].equals("label");
        }

        @Override
        public String getColumnName(int column) {
            if (column < this.titles.length) {
                return this.titles[column];
            } else {
                return "";
            }
        }
    }

    private final int MAX_COLS = 100;

    private Color[][] fgColors;
    private Color[][] bgColors;
    private final PandaTableModel model;
    private boolean enterPressed;
    private int changedRow;
    private int changedColumn;
    private String changedValue;
    private boolean[] imControls;

    public int getChangedColumn() {
        return changedColumn;
    }

    public int getChangedRow() {
        return changedRow;
    }

    public String getChangedValue() {
        return changedValue;
    }

    public void setChangedColumn(int changedColumn) {
        this.changedColumn = changedColumn;
    }

    public void setChangedRow(int changedRow) {
        this.changedRow = changedRow;
    }

    public void setChangedValue(String changedValue) {
        this.changedValue = changedValue;
    }

    public boolean isEnterPressed() {
        return enterPressed;
    }

    public void setEnterPressed(boolean enterPressed) {
        this.enterPressed = enterPressed;
    }

    public void setImControls(String[] cs) {
        for (int i = 0; i < cs.length && i < imControls.length; i++) {
            imControls[i] = cs[i].startsWith("t") || cs[i].startsWith("T");
        }
    }

    public PandaTable() {
        this.setRowSelectionAllowed(false);
        JTableHeader header = this.getTableHeader();
        header.setVisible(true);
        this.setShowGrid(true);

        model = new PandaTableModel();
        this.setModel(model);

        /*
         * magic number
         */
        int rowheight = 25;
        if (System.getProperty("monsia.pandatable.rowheight") != null) {
            rowheight = Integer.parseInt(System.getProperty("monsia.pandatable.rowheight"));
        }
        this.setRowHeight(rowheight);

        enterPressed = false;
        imControls = new boolean[MAX_COLS];
        for (int i = 0; i < MAX_COLS; i++) {
            imControls[i] = false;
        }

        final DefaultCellEditor ce = (DefaultCellEditor) this.getDefaultEditor(Object.class);

        /*
         * Enterでの更新はSendEventするため
         */
        ce.getComponent().addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    PandaTable.this.setEnterPressed(true);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN
                        || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_TAB) {
                    Component parent = ((Component) e.getSource()).getParent();
                    KeyEvent pass = new KeyEvent(parent, e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar());
                    parent.dispatchEvent(pass);
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        /*
         * エディタをフォーカスアウトしたらセル編集キャンセルするため
         */
        ce.getComponent().addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                // do nothing                
                if (SystemEnvironment.isWindows()) {
                    if (imControls[getSelectedColumn()]) {
                        InputContext ic = getInputContext();
                        if (ic != null) {
                            ic.setCharacterSubsets(new Character.Subset[]{InputSubset.KANJI});
                            ic.endComposition();
                            ic.selectInputMethod(Locale.JAPANESE);
                        }
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (SystemEnvironment.isWindows()) {
                    InputContext ic = getInputContext();
                    if (ic != null) {
                        ic.setCharacterSubsets(null);
                        ic.endComposition();
                        ic.selectInputMethod(Locale.ENGLISH);
                    }
                }
                ce.stopCellEditing();                
            }
        });

        addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                //pns フォーカスを取ったら必ず編集する
                editCell();
            }

            @Override
            public void focusLost(FocusEvent e) {
                // do nothing
            }
        });

        changedRow = 0;
        changedColumn = 0;
        changedValue = "";
    }

    //pns 選択が起きたら必ず編集する
    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        if (isEditing()) {
            // selection が変わったので前の editor は消す
            getCellEditor().cancelCellEditing();
        } else {
            // 非編集状態なら editor を立ち上げる
            editCell();
        }
    }

    //pns セル編集時に CellEditor にフォーカスさせる editCell
    private void editCell() {
        editCellAt(getSelectedRow(), getSelectedColumn());
        this.setChangedRow(this.getSelectedRow());
        this.setChangedColumn(this.getSelectedColumn());

        final DefaultCellEditor ce = (DefaultCellEditor) getDefaultEditor(Object.class);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ce.getComponent().requestFocusInWindow();
            }
        });
    }

    public String getStringValueAt(int row, int col) {
        Object obj = getModel().getValueAt(row, col);
        if (obj == null) {
            return "";
        } else {
            return obj.toString();
        }
    }

    public int getRows() {
        return this.getModel().getRowCount();
    }

    public void setRows(int rows) {
        model.setRows(rows);
        int columns = model.getColumnCount();
        fgColors = new Color[rows][columns];
        bgColors = new Color[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                fgColors[i][j] = Color.BLACK;
                bgColors[i][j] = Color.WHITE;
            }
        }
    }

    public int getColumns() {
        return model.getColumnCount();
    }

    public void setColumns(int cols) {
        model.setColumns(cols);
        int rows = model.getRowCount();
        fgColors = new Color[rows][cols];
        bgColors = new Color[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                fgColors[i][j] = Color.BLACK;
                bgColors[i][j] = Color.WHITE;
            }
        }
    }

    public void setTitles(String[] titles) {
        model.setTitles(titles);
    }

    public String[] getTypes() {
        return model.getTypes();
    }

    public void setTypes(String[] types) {
        model.setTypes(types);
    }

    public void setFGColor(int row, int column, String _color) {
        if (0 <= row && row < model.getRowCount()
                && 0 <= column && column < model.getColumnCount()) {
            Color color = SafeColorDecoder.decode(_color);
            fgColors[row][column] = color != null ? color : Color.BLACK;
        }
    }

    public void setBGColor(int row, int column, String _color) {
        if (0 <= row && row < model.getRowCount()
                && 0 <= column && column < model.getColumnCount()) {
            Color color = SafeColorDecoder.decode(_color);
            bgColors[row][column] = color != null ? color : Color.WHITE;
        }
    }

    public void setCell(int row, int col, String data) {
        model.setValueAt(data, row, col);
    }

    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor,row,column);
        if (fgColors != null) {
            c.setForeground(fgColors[row][column]);
        }
        if (bgColors != null) {
            c.setBackground(bgColors[row][column]);
        }
        return c;
    }

    @Override
    public Component prepareRenderer(
            TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (fgColors != null) {
            c.setForeground(fgColors[row][column]);
        }
        if (bgColors != null) {
            c.setBackground(bgColors[row][column]);
        }
        return c;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ProtoPandaTable");
        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout(10, 5));

        String[] types = {"text", "label"};
        String[] titles = {"Text", "Label"};
        final PandaTable table = new PandaTable();
        table.setRows(100);
        table.setColumns(2);
        table.setTitles(titles);
        table.setTypes(types);
        table.setCell(0, 1, "hoge\nmoge\noge");

        table.getModel().addTableModelListener(
                new TableModelListener() {

                    @Override
                    public void tableChanged(TableModelEvent te) {
                        int row = te.getLastRow();
                        int col = te.getColumn();
                        System.out.println("[" + row + "," + col + "] " + table.getModel().getValueAt(row, col) + " " + table.getModel().getValueAt(row, col).getClass());
                    }
                });

        JScrollPane scroll = new JScrollPane(table);
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

        table.changeSelection(2, 2, false, true);
    }
}
