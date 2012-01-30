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
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.montsuqi.util.GtkColorMap;

public class PandaTable extends JTable {

    private class StartEditingAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            PandaTable table = (PandaTable) e.getSource();
            table.setSurrendersFocusOnKeystroke(true);
            int row = table.getSelectedRow();
            int column = table.getSelectedColumn();
            Object object = table.getModel().getValueAt(row, column);
            if (object instanceof Boolean) {
                table.getModel().setValueAt(!((Boolean) object).booleanValue(), row, column);
            } else {
                if (!table.isEditing()) {
                    table.editCellAt(row, column);
                } else {
                    table.getCellEditor(row, column).stopCellEditing();
                }
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
            if (this.types[col].equals("text")) {
                return true;
            }
            return false;
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
    private Color[] fgColors;
    private Color[] bgColors;
    private PandaTableModel model;
    public int changedRow;
    public int changedColumn;
    public String changedValue;

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

        DefaultCellEditor ce = (DefaultCellEditor) this.getDefaultEditor(Object.class);
        ce.setClickCountToStart(1);

        // action setting
        //ActionMap actions = getActionMap();
        //InputMap inputs = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        //actions.put("startEditing", new StartEditingAction());
        //inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing");

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
        fgColors = new Color[rows];
        bgColors = new Color[rows];
        for (int i = 0; i < rows; i++) {
            fgColors[i] = Color.BLACK;
            bgColors[i] = Color.WHITE;
        }
    }

    public int getColumns() {
        return model.getColumnCount();
    }

    public void setColumns(int cols) {
        model.setColumns(cols);
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

    public void setFGColor(int row, String _color) {
        if (0 <= row && row < model.getRowCount()) {
            Color color = GtkColorMap.getColor(_color);
            fgColors[row] = color != null ? color : Color.BLACK;
        }
    }

    public void setBGColor(int row, String _color) {
        if (0 <= row && row < model.getRowCount()) {
            Color color = GtkColorMap.getColor(_color);
            bgColors[row] = color != null ? color : Color.WHITE;
        }
    }

    public void setRow(int row, String[] rowdata) {
        model.setRow(row, rowdata);
    }

    @Override
    public Component prepareEditor(
            TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        c.setBackground(bgColors[row]);
        c.setForeground(fgColors[row]);
        return c;
    }

    @Override
    public Component prepareRenderer(
            TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        c.setBackground(bgColors[row]);
        c.setForeground(fgColors[row]);
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
        String[] str = {"text", "label"};
        table.setRow(0, str);
        table.setBGColor(0, "plum1");
        table.setFGColor(0, "red");
        String[] str2 = {"text", "label"};
        table.setRow(1, str2);
        table.setBGColor(1, "green");
        table.setFGColor(1, "blue");        

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
                System.out.println("----");
                String[] str2 = { "text", "label"};
                table.setRow(3, str2);
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
