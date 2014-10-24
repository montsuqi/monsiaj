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
package org.montsuqi.client;

import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.montsuqi.util.GtkStockIcon;
import org.montsuqi.util.OptionParser;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.widgets.Button;
import org.montsuqi.widgets.ExceptionDialog;

public class Launcher {

    protected static final Logger logger = LogManager.getLogger(Launcher.class);
    protected String title;
    protected Config conf;
    protected ConfigPanel configPanel;
    protected JComboBox<String> configCombo;
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    public static void main(String[] args) {
        logger.info("---- start monsiaj");
        logger.info("version : {}", Messages.getString("application.version"));
        logger.info("java : {}", System.getProperty("java.version"));
        logger.info("os : {}-{}-{}",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"));
        Launcher launcher = new Launcher(Messages.getString("application.title"));
        launcher.launch(args);
    }

    public Launcher(String title) {
        if (System.getProperty("monsia.log.level") != null) {
        } else {
            System.setProperty("monsia.log.level", "info");
        }
        this.title = title;
        SystemEnvironment.setMacMenuTitle(title);
        conf = new Config();
        installLookAndFeels();
    }

    private void installLookAndFeels() {
        try {
            UIManager.installLookAndFeel("Nimrod", "com.nilo.plaf.nimrod.NimRODLookAndFeel");
            UIManager.installLookAndFeel("InfoNode", "net.infonode.gui.laf.InfoNodeLookAndFeel");
        } catch (Exception e) {
            logger.catching(Level.WARN, e);
        }
    }

    public boolean checkCommandLineOption(String[] args) {
        OptionParser options = new OptionParser();
        options.add("config", Messages.getString("Launcher.config_option_message"), "");
        options.add("config-list", Messages.getString("Launcher.config_list_option_message"), false);
        options.parse(this.getClass().getName(), args);

        String configName = options.getString("config");
        boolean listConfigFlag = options.getBoolean("config-list");
        if (listConfigFlag) {
            conf.listConfig();
            return true;
        }
        if (!configName.equals("")) {
            conf.setCurrentByDescription(configName);
            /*
             * set properties
             */
            conf.applySystemProperties(conf.getCurrent());

            /*
             * set look and feel
             */
            try {
                String cname = conf.getLookAndFeel(conf.getCurrent());
                if (cname.startsWith("com.nilo.plaf.nimrod")) {
                    System.setProperty("nimrodlf.themeFile", conf.getLookAndFeelThemeFile(conf.getCurrent()));
                    UIManager.setLookAndFeel(new NimRODLookAndFeel());
                } else {
                    UIManager.setLookAndFeel(cname);
                }
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.catching(Level.WARN, e);
                return true;
            }

            /*
             * confirm password when the password not preserved
             */
            if (!conf.getSavePassword(conf.getCurrent())) {
                JPasswordField pwd = new JPasswordField();
                Object[] message = {Messages.getString("Launcher.input_password_message"), pwd};
                int resp = JOptionPane.showConfirmDialog(null, message, Messages.getString("Launcher.input_password_message"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp != JOptionPane.OK_OPTION) {
                    return true;
                }
            }

            /*
             * confirm certificate password when the certificate password not
             * preserved
             */
            if (conf.getUseSSL(conf.getCurrent())
                    && !conf.getClientCertificateFile(conf.getCurrent()).equals("")
                    && !conf.getSaveClientCertificatePassword(conf.getCurrent())) {
                JPasswordField pwd = new JPasswordField();
                Object[] message = {Messages.getString("Launcher.input_certificate_password_message"), pwd};
                int resp = JOptionPane.showConfirmDialog(null, message, Messages.getString("Launcher.input_certificate_password_message"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp != JOptionPane.OK_OPTION) {
                    return true;
                }
            }

            connect();
            return true;
        }
        return false;
    }

    protected JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc;

        JLabel configLabel = new JLabel(Messages.getString("ConfigurationPanel.config_label"));
        configCombo = new JComboBox<>();
        updateConfigCombo();
        configCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                java.util.List<Integer> list = conf.getList();
                int current = list.get(configCombo.getSelectedIndex());
                conf.setCurrent(current);
                configPanel.loadConfig(current);
            }
        });
        configPanel = createConfigurationPanel();
        configPanel.loadConfig(conf.getCurrent());
        JTabbedPane tabbed = new JTabbedPane();
        tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbed.addTab(Messages.getString("ConfigurationPanel.basic_tab_label"), configPanel.getBasicPanel());
        tabbed.addTab(Messages.getString("ConfigurationPanel.ssl_tab_label"), configPanel.getSSLPanel());
        tabbed.addTab(Messages.getString("ConfigurationPanel.others_tab_label"), configPanel.getOthersPanel());
        tabbed.addTab(Messages.getString("ConfigurationPanel.info_tab_label"), configPanel.getInfoPanel());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.ipadx = 5;
        gbc.insets = new Insets(2, 2, 2, 2);
        panel.add(configLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(configCombo, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        panel.add(tabbed, gbc);
        return panel;
    }

    private void updateConfigCombo() {
        ActionListener[] listeners = configCombo.getActionListeners();
        for (ActionListener listener : listeners) {
            configCombo.removeActionListener(listener);
        }
        configCombo.removeAllItems();
        for (int i : conf.getList()) {
            configCombo.addItem(conf.getDescription(i));
        }
        for (ActionListener listener : listeners) {
            configCombo.addActionListener(listener);
        }
        configCombo.setSelectedItem(conf.getDescription(conf.getCurrent()));
    }

    private void checkJavaVersion() {
        String ver = System.getProperty("java.version");
        boolean isOld = false;
        if (ver.startsWith("1.7")) {
            if (ver.compareToIgnoreCase("1.7.0_51") < 0) {
                isOld = true;
            }
        } else if (ver.startsWith("1.6")) {
            if (ver.compareToIgnoreCase("1.6.0_71") < 0) {
                isOld = true;
            }
        }
        if (isOld) {
            String contents = "";
            contents += "脆弱性のあるJavaを使用しています\n";
            contents += "\n";
            contents += "使用中のバージョン:" + ver + "\n\n";
            contents += "Javaをアップデートしてください\n";

            Color bgcolor = new Color(240, 240, 30);
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.setBackground(bgcolor);

            JPanel textPanel = new JPanel(new BorderLayout(5, 5));
            textPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            textPanel.setBackground(bgcolor);

            JLabel summaryLabel = new JLabel("monsiajセキュリティ警告");
            summaryLabel.setFont(new Font("Suns", Font.BOLD, 20));
            JTextPane bodyText = new JTextPane();
            bodyText.setFont(new Font("Suns", Font.PLAIN, 16));
            bodyText.setText(contents);
            bodyText.setOpaque(false);
            bodyText.setEditable(false);

            textPanel.add(summaryLabel, BorderLayout.NORTH);
            textPanel.add(bodyText, BorderLayout.CENTER);

            boolean checked = prefs.get(Launcher.class.getName() + ".security_risk_agreement", "no").startsWith("yes");
            final JCheckBox checkBox = new JCheckBox("危険性を理解した上で使用する");
            checkBox.setFont(new Font("Suns", Font.PLAIN, 16));
            checkBox.setBackground(bgcolor);
            checkBox.setSelected(checked);
            checkBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    prefs.put(Launcher.class.getName() + ".security_risk_agreement", checkBox.isSelected() ? "yes" : "no");
                }
            });
            textPanel.add(checkBox, BorderLayout.SOUTH);

            panel.add(new JLabel(GtkStockIcon.get("gtk-dialog-warning")), BorderLayout.WEST);
            panel.add(textPanel, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(null, panel, "monsiajセキュリティ警告", JOptionPane.PLAIN_MESSAGE);
            if (!checkBox.isSelected()) {
                System.exit(0);
            }
        }
    }

    public void launch(String[] args) {
        checkJavaVersion();
        if (checkCommandLineOption(args)) {
            return;
        }
        final JFrame f = new JFrame(title);
        Container container = f.getContentPane();
        container.setLayout(new BorderLayout(10, 5));
        final JPanel mainPanel = createMainPanel();
        final ConfigViewer viewer = createConfigurationViewer();
        container.add(mainPanel, BorderLayout.CENTER);

        URL iconURL = getClass().getResource("/org/montsuqi/widgets/images/orca.png");
        f.setIconImage(Toolkit.getDefaultToolkit().createImage(iconURL));

        JLabel iconLabel = new JLabel("", createIcon(), JLabel.CENTER);
        container.add(iconLabel, BorderLayout.WEST);

        JPanel bar = new JPanel();
        bar.setLayout(new FlowLayout());
        container.add(bar, BorderLayout.SOUTH);

        Button run = new Button(new AbstractAction(Messages.getString("Launcher.run_label")) {

            @Override
            public void actionPerformed(ActionEvent ev) {
                int num = conf.getConfigByDescription((String) configCombo.getSelectedItem());
                configPanel.saveConfig(num);
                conf.setCurrent(num);
                conf.applySystemProperties(conf.getCurrent());
                connect();
                f.dispose();
            }
        });
        bar.add(run);

        Button cancel = new Button(new AbstractAction(Messages.getString("Launcher.cancel_label")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("launcher canceled");
                System.exit(0);
            }
        });
        bar.add(cancel);

        Button config = new Button(new AbstractAction(Messages.getString("Launcher.config_label")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("view server configs");
                viewer.run(f);
                updateConfigCombo();
            }
        });
        bar.add(config);

        f.setSize(760, 480);
        f.setResizable(true);

        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.setVisible(true);
        configPanel.changeLookAndFeel();
        run.requestFocus();
    }

    private void connect() {
        conf.save();
        try {
            Client client = new Client(conf);
            client.connect();
        } catch (IOException | GeneralSecurityException | JSONException e) {
            logger.catching(Level.FATAL, e);
            ExceptionDialog.showExceptionDialog(e);
            System.exit(1);
        }
    }

    protected ConfigPanel createConfigurationPanel() {
        return new ConfigPanel(conf, true, true);
    }

    protected ConfigViewer createConfigurationViewer() {
        return new ConfigViewer(conf);
    }

    protected Icon createIcon() {
        URL iconURL = getClass().getResource("/jp/or/med/orca/jmareceipt/standard60.png");
        if (iconURL != null) {
            return new ImageIcon(iconURL);
        }
        return null;
    }
}
