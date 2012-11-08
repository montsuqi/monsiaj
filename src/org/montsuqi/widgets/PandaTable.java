/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.widgets;

/**
 *
 * @author mihara
 */
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.montsuqi.util.SafeColorDecoder;

public class PandaTable extends JTable {

    private class StartEditingAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            PandaTable table = (PandaTable) e.getSource();
            table.setSurrendersFocusOnKeystroke(true);
            int row = table.getSelectedRow();
            int column = table.getSelectedColumn();
            Object object = table.getModel().getValueAt(row, column);
            if (!table.isEditing()) {
                table.editCellAt(row, column);
            }
        }
    }

    private class PandaTableModel extends DefaultTableModel {

        private final int MAX_COLS = 100;
        private String[] types;
        private String[] titles;

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
            if (this.types[col].equals("label")) {
                return false;
            }
            return true;
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
    private Color[][] fgColors;
    private Color[][] bgColors;
    private PandaTableModel model;
    private boolean enterPressed;
    private int changedRow;
    private int changedColumn;
    private String changedValue;

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

        this.setSurrendersFocusOnKeystroke(true);
        this.setFocusable(true);

        enterPressed = false;

        DefaultCellEditor ce = (DefaultCellEditor) this.getDefaultEditor(Object.class);
        ce.setClickCountToStart(1);

        ce.getComponent().addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    PandaTable.this.setEnterPressed(true);
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });


        // action setting
        ActionMap actions = getActionMap();
        InputMap inputs = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        actions.put("startEditing", new StartEditingAction());
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing");

        changedRow = 0;
        changedColumn = 0;
        changedValue = "";
    }

    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        boolean retValue = super.processKeyBinding(ks, e, condition, pressed);
        if (KeyStroke.getKeyStroke('\t').equals(ks) || KeyStroke.getKeyStroke('\n').equals(ks)) {
System.out.println("here");            
            return retValue;
        }
System.out.println("ks:" + ks + " CompositionEnabled:" + getInputContext().isCompositionEnabled() + " isEditing:"+isEditing() + " pressed:"+pressed + " OnKeyRelease:"+ ks.isOnKeyRelease());        
        if (getInputContext().isCompositionEnabled() && !isEditing()
                && !pressed && !ks.isOnKeyRelease()) {
            int selectedRow = getSelectedRow();
            int selectedColumn = getSelectedColumn();
System.out.println("row:"+selectedRow + " column:"+selectedColumn);            
            if (selectedRow != -1 && selectedColumn != -1 && !editCellAt(selectedRow, selectedColumn)) {
System.out.println("editcell?");                
                return retValue;
            }
        }
        return retValue;
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
    public Component prepareEditor(
            TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (fgColors != null && bgColors != null) {
            c.setBackground(bgColors[row][column]);
            c.setForeground(fgColors[row][column]);
        }
        return c;
    }

    @Override
    public Component prepareRenderer(
            TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (fgColors != null && bgColors != null) {
            c.setBackground(bgColors[row][column]);
            c.setForeground(fgColors[row][column]);
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

        table.getModel().addTableModelListener(
                new TableModelListener() {

                    public void tableChanged(TableModelEvent te) {
                        int row = te.getLastRow();
                        int col = te.getColumn();
                        System.out.println("[" + row + "," + col + "] " + table.getModel().getValueAt(row, col) + " " + table.getModel().getValueAt(row, col).getClass());
                    }
                });

        //final URL iconURL = table.getClass().getResource("orca.png");
        //final ImageIcon icon = new ImageIcon(iconURL);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(400, 300));
        container.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        container.add(buttonPanel, BorderLayout.SOUTH);

        JButton button3 = new JButton(new AbstractAction("output") {

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
