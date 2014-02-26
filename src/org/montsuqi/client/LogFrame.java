package org.montsuqi.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import javax.swing.*;
import org.montsuqi.widgets.Button;
import org.montsuqi.widgets.ConsolePane;

/*
 * Copyright (C) 2010 JMA (Japan Medical Association)
 */
/**
 *
 * @author mihara
 */
public class LogFrame extends Thread {

    @Override
    public void run() {
        final JFrame f = new JFrame(Messages.getString("Launcher.log_title")); 
        URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/orca.png");
        f.setIconImage(Toolkit.getDefaultToolkit().createImage(iconURL));
        Container container = f.getContentPane();
        container.setLayout(new BorderLayout());
        final ConsolePane console = new ConsolePane();
        System.setOut(console.getOut());
        System.setErr(console.getErr());

        JScrollPane scroll = new JScrollPane(console);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(640, 480));
        container.add(scroll, BorderLayout.CENTER);

        JPanel bar = new JPanel();
        bar.setLayout(new FlowLayout());
        container.add(bar, BorderLayout.SOUTH);
        Button clear = new Button(new AbstractAction(Messages.getString("Launcher.log_clear")) { 

            public void actionPerformed(ActionEvent e) {
                console.setText(""); 
            }
        });
        bar.add(clear);

        Button save = new Button(new AbstractAction(Messages.getString("Launcher.log_save_log_as")) { 

            public void actionPerformed(ActionEvent ev) {
                JFileChooser chooser = new JFileChooser();
                int ret = chooser.showSaveDialog(f);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    try {
                        FileWriter fw = new FileWriter(file);
                        fw.write(console.getText());
                        fw.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
        bar.add(save);

        Button quit = new Button(new AbstractAction(Messages.getString("Launcher.log_quit")) { 

            public void actionPerformed(ActionEvent e) {
                // do nothing
            }
        });
        bar.add(quit);

        f.setSize(640, 480);
        int state = f.getExtendedState();
        f.setExtendedState(state | Frame.ICONIFIED);
        f.setVisible(true);

        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                // do nothing
            }
        });
    }
}
