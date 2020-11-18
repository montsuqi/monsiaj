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
package org.montsuqi.monsiaj.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import org.montsuqi.monsiaj.util.SystemEnvironment;

public class LogViewer {

    private final File logFile;

    public LogViewer(File logFile) {
        this.logFile = logFile;
    }

    public void run() {
        final JDialog f = new JDialog();
        Container container = f.getContentPane();
        container.setLayout(new BorderLayout(5, 5));
        final JTable table = new JTable();
        table.setRowSelectionAllowed(true);
        final DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("時刻");
        tableModel.addColumn("ウィンドウ");
        tableModel.addColumn("ウィジェット");
        tableModel.addColumn("イベント");
        tableModel.addColumn("通信時間");
        tableModel.addColumn("サーバ処理");
        tableModel.addColumn("アプリ処理");
        table.setModel(tableModel);

        String host = parseLog(tableModel);

        JLabel label = new JLabel("host: " + host);
        container.add(label, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        container.add(scroll, BorderLayout.CENTER);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton button = new JButton("CSV保存");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(logFile.getAbsolutePath() + ".csv"));
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    try ( BufferedWriter bw = new BufferedWriter(new FileWriter(chooser.getSelectedFile()))) {
                        int rowSize = tableModel.getRowCount();
                        int colSize = tableModel.getColumnCount();
                        for (int i = 0; i < rowSize; i++) {
                            for (int j = 0; j < colSize; j++) {
                                if (j != 0) {
                                    bw.write(",");
                                }
                                bw.write(tableModel.getValueAt(i, j).toString());
                            }
                            bw.newLine();
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        container.add(button, BorderLayout.SOUTH);

        f.setSize(900, 800);
        f.setVisible(true);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private String parseLog(DefaultTableModel model) {
        /*
          10/21 16:18:49 [INFO ] try connect http://localhost:8000/rpc/
          10/21 16:19:08 [INFO ] window:M00 widget:G01 event:CLICKED
          10/21 16:19:08 [INFO ] [send_event] total:472ms make_event_data:0ms rpc_total:244ms server_total:85ms server_app:33ms update_screen:228ms
         */
        String host = "";
        Pattern p1 = Pattern.compile("try connect (.*)");
        Pattern p2 = Pattern.compile("(\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}).*window\\:(.*) widget\\:(.*) event\\:(.*)");
        Pattern p3 = Pattern.compile("rpc_total\\:(\\d+)ms server_total\\:(\\d+)ms server_app\\:(\\d+)ms");
        try ( BufferedReader br = new BufferedReader(new FileReader(this.logFile))) {
            String line, datetime, window, widget, event, com, rpc, server, app;
            datetime = window = widget = event = com = rpc = server = app = "";
            while ((line = br.readLine()) != null) {
                if (host.isBlank()) {
                    Matcher m1 = p1.matcher(line);
                    if (m1.find()) {
                        host = m1.group(1);
                    }
                }
                Matcher m2 = p2.matcher(line);
                if (m2.find()) {
                    datetime = m2.group(1);
                    window = m2.group(2);
                    widget = m2.group(3);
                    event = m2.group(4);
                }
                Matcher m3 = p3.matcher(line);
                if (m3.find()) {
                    rpc = m3.group(1);
                    server = m3.group(2);
                    app = m3.group(3);
                    int icom = Integer.parseInt(rpc) - Integer.parseInt(server);
                    com = String.valueOf(icom);
                    Object[] row_data = new Object[]{datetime, window, widget, event, com, server, app};
                    model.addRow(row_data);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "エラー", JOptionPane.ERROR_MESSAGE);
        }
        return host;
    }
}
