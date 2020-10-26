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
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

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
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("時刻");
        model.addColumn("ウィンドウ");
        model.addColumn("ウィジェット");
        model.addColumn("イベント");
        model.addColumn("転送");
        model.addColumn("サーバ全体");
        model.addColumn("アプリ処理");
        table.setModel(model);

        String host = parseLog(model);

        JLabel label = new JLabel("host:" + host);
        container.add(label, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        container.add(scroll, BorderLayout.CENTER);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        f.setSize(640, 800);
        f.setVisible(true);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private String parseLog(DefaultTableModel model) {
        String host = "";
        return host;
    }
}
