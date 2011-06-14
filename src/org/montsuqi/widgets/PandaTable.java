/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.widgets;

/**
 *
 * @author mihara
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.montsuqi.util.GtkColorMap;
import org.montsuqi.util.GtkStockIcon;

public class PandaTable extends JTable {

    private class ToggleCheckAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {
            PandaTable table = (PandaTable) e.getSource();
            int row = table.getSelectedRow();
            int column = table.getSelectedColumn();
            Object object = table.getModel().getValueAt(row, column);
            if (object instanceof Boolean) {
                table.getModel().setValueAt(!((Boolean) object).booleanValue(), row, column);
            }
        }
    }

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

    public class IconRenderer extends JLabel
            implements TableCellRenderer {

        private final DefaultTableCellRenderer adaptee =
                new DefaultTableCellRenderer();

        public IconRenderer() {
            this.setIconTextGap(0);
            this.setHorizontalAlignment(CENTER);
        }

        public Component getTableCellRendererComponent(
                JTable table, Object obj, boolean isSelected,
                boolean hasFocus, int row, int column) {
            // set the colours, etc. using the standard for that platform
            adaptee.getTableCellRendererComponent(table, obj,
                    isSelected, hasFocus, row, column);
            setForeground(adaptee.getForeground());
            setBackground(adaptee.getBackground());
            setBorder(adaptee.getBorder());
            setOpaque(true);
            if (obj != null) {
                if (obj instanceof Icon) {
                    Icon icon = (Icon) obj;
                    setIcon(icon);
                }
            } else {
                setIcon(null);
            }
            return this;
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
                    if (types[j].equals("icon")) {
                        /*
                        URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/print.png");
                        rowData[j] = new ImageIcon(iconURL);
                         */
                        this.setValueAt(null, i, j);
                    } else if (types[j].equals("check")) {
                        this.setValueAt(false, i, j);
                    } else {
                        this.setValueAt("", i, j);
                    }
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
                    if (types[i].equals("icon")) {
                        Icon icon = GtkStockIcon.get(rowdata[i]);
                        if (icon != null) {
                            int margin = 4;
                            setValueAt(icon, row, i);
                            PandaTable.this.setRowHeight(row,icon.getIconHeight()+margin);
                        } else {
                            setValueAt(null, row, i);
                        }
                    } else if (types[i].equals("check")) {
                        if (rowdata[i].startsWith("T")) {
                            setValueAt(true, row, i);
                        } else {
                            setValueAt(false, row, i);
                        }
                    } else {
                        setValueAt(rowdata[i], row, i);
                    }
                }
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (this.types[col].equals("text")
                    || this.types[col].equals("check")) {
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

        @Override
        public Class<?> getColumnClass(int column) {
            if (types[column].equals("icon")) {
                return ImageIcon.class;
            } else if (types[column].equals("text")
                    || types[column].equals("label")) {
                return String.class;
            } else if (types[column].equals("check")) {
                return Boolean.class;
            }
            return String.class;
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

        this.setSurrendersFocusOnKeystroke(true);
        this.setFocusable(true);

        DefaultCellEditor ce = (DefaultCellEditor) this.getDefaultEditor(Object.class);
        ce.setClickCountToStart(1);

        // action setting
        ActionMap actions = getActionMap();
        InputMap inputs = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        actions.put("startEditing", new StartEditingAction());
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing");
        actions.put("toggleCheck", new ToggleCheckAction());
        inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggleCheck");
    }

    public String getStringValueAt(int row, int col) {
        Object obj = getModel().getValueAt(row, col);
        if (obj == null) {
            return "";
        } else if (obj == java.lang.Boolean.TRUE) {
            return "TRUE";
        } else if (obj == java.lang.Boolean.FALSE) {
            return "FALSE";
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
        String[] t = model.getTypes();
        for (int i = 0; i < t.length; i++) {
            String s = t[i];
            if (s.equals("icon")) {
                columnModel.getColumn(i).setCellRenderer(new IconRenderer());
            }
        }
    }

    public void setFGColor(int row, String color) {
        if (0 <= row && row < model.getRowCount()) {
            fgColors[row] = GtkColorMap.getColor(color);
        }
    }

    public void setBGColor(int row, String color) {
        if (0 <= row && row < model.getRowCount()) {
            bgColors[row] = GtkColorMap.getColor(color);
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

        String[] types = {"icon", "text", "label", "check"};
        String[] titles = {"Icon", "Text", "Label", "Check"};
        final PandaTable table = new PandaTable();
        table.setRows(100);
        table.setColumns(4);
        table.setTitles(titles);
        table.setTypes(types);
        String[] str = {"aaa-zoom-in", "text", "label", "T"};
        table.setRow(10, str);
        table.setBGColor(10, "plum1");
        table.setFGColor(10, "red");
        String[] str2 = {"gtk-yes", "text", "label", "T"};
        table.setRow(10, str2);

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
                String[] str2 = {"gtk-yes", "text", "label", "T"};
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
