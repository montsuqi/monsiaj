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

import org.montsuqi.monsiaj.util.Messages;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;
import java.awt.BorderLayout;
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
import java.util.Enumeration;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.montsuqi.monsiaj.util.LogFile;
import org.montsuqi.monsiaj.util.OptionParser;
import org.montsuqi.monsiaj.util.SystemEnvironment;
import org.montsuqi.monsiaj.util.TempFile;
import org.montsuqi.monsiaj.widgets.Button;
import org.montsuqi.monsiaj.widgets.ExceptionDialog;
import org.montsuqi.monsiaj.widgets.FileChooserButton;

public class Launcher {

    protected static final Logger logger = LogManager.getLogger(Launcher.class);
    protected String title;
    protected Config conf;
    protected ConfigPanel configPanel;
    protected JComboBox<String> configCombo;
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    public static void main(String[] args) {
        logger.info("---- start monsiaj");
        logger.info("version : {}", Launcher.class.getPackage().getImplementationVersion());
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
        initLookAndFeel();
        TempFile.cleanOld();
        LogFile.cleanOld();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (System.getProperty("monsia.use.loader") == null) {
                UIManager.installLookAndFeel("Nimrod", "com.nilo.plaf.nimrod.NimRODLookAndFeel");
            }
            UIManager.installLookAndFeel("InfoNode", "net.infonode.gui.laf.InfoNodeLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
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
            int n = conf.getCurrent();
            conf.applySystemProperties(n);

            /* set printer config  */
            conf.loadPrinterConfig(n);

            /*
             * set look and feel
             */
            this.setLookAndFeel();

            /*
             * confirm password when the password not preserved
             */
            if (!conf.getSavePassword(n)) {
                JPasswordField pwd = new JPasswordField();
                Object[] message = {Messages.getString("Launcher.input_password_message"), pwd};
                int resp = JOptionPane.showConfirmDialog(null, message, Messages.getString("Launcher.input_password_message"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp == JOptionPane.OK_OPTION) {
                    conf.setPassword(n, String.valueOf(pwd.getPassword()));
                } else {
                    return true;
                }
            }

            /*
             * confirm certificate password when the certificate password not
             * preserved
             */
            if (conf.getUseSSL(n)
                    && !conf.getClientCertificateFile(n).equals("")
                    && !conf.getSaveClientCertificatePassword(n)) {
                JPasswordField pwd = new JPasswordField();
                Object[] message = {Messages.getString("Launcher.input_certificate_password_message"), pwd};
                int resp = JOptionPane.showConfirmDialog(null, message, Messages.getString("Launcher.input_certificate_password_message"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (resp == JOptionPane.OK_OPTION) {
                    conf.setClientCertificatePassword(n, String.valueOf(pwd.getPassword()));
                } else {
                    return true;
                }
            }
            conf.save();
            connect();
            return true;
        }
        return false;
    }

    private void setLookAndFeel() {
        try {
            int n = conf.getCurrent();
            String lafName = conf.getLookAndFeel(n);
            if (lafName.startsWith("com.nilo.plaf.nimrod")) {
                System.setProperty("nimrodlf.themeFile", conf.getLookAndFeelThemeFile(n));
                UIManager.setLookAndFeel(new NimRODLookAndFeel());
            } else {
                UIManager.setLookAndFeel(lafName);
            }
            if (SystemEnvironment.isMacOSX() && (!lafName.startsWith("apple.laf.AquaLookAndFeel"))) {
                updateFont(new Font("Osaka", Font.PLAIN, 12));
            }
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.catching(Level.WARN, e);
        }
    }

    private void updateFont(final Font font) {
        Enumeration e = UIManager.getDefaults().keys();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
        }
    }

    protected JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints gbc;

        JLabel configLabel = new JLabel(Messages.getString("ConfigurationPanel.config_label"));
        configCombo = new JComboBox<>();
        updateConfigCombo();
        configCombo.addActionListener((ActionEvent evt) -> {
            java.util.List<Integer> list = conf.getList();
            int current = list.get(configCombo.getSelectedIndex());
            conf.setCurrent(current);
            configPanel.loadConfig(current);
        });
        configPanel = createConfigurationPanel();
        configPanel.loadConfig(conf.getCurrent());
        JTabbedPane tabbed = new JTabbedPane();
        tabbed.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbed.addTab(Messages.getString("ConfigurationPanel.basic_tab_label"), configPanel.getBasicPanel());
        tabbed.addTab(Messages.getString("ConfigurationPanel.ssl_tab_label"), configPanel.getSSLPanel());
        tabbed.addTab(Messages.getString("ConfigurationPanel.printer_config_tab_label"), configPanel.getPrinterConfigPanel());
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
        conf.getList().forEach((i) -> {
            configCombo.addItem(conf.getDescription(i));
        });
        for (ActionListener listener : listeners) {
            configCombo.addActionListener(listener);
        }
        configCombo.setSelectedItem(conf.getDescription(conf.getCurrent()));
    }

    public void launch(String[] args) {
        if (checkCommandLineOption(args)) {
            return;
        }
        final JFrame f = new JFrame(title);
        Container container = f.getContentPane();
        container.setLayout(new BorderLayout(10, 5));
        final JPanel mainPanel = createMainPanel();
        final ConfigViewer viewer = createConfigurationViewer();
        container.add(mainPanel, BorderLayout.CENTER);

        URL iconURL = getClass().getResource("/images/orcamo.png");
        f.setIconImage(Toolkit.getDefaultToolkit().createImage(iconURL));

        JLabel iconLabel = new JLabel("", createIcon(), JLabel.CENTER);
        iconLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
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
                Launcher.this.setLookAndFeel();
                connect();
                f.dispose();
            }
        });
        bar.add(run);

        Button saveButton = new Button(new AbstractAction(Messages.getString("Launcher.save_label")) {

            @Override
            public void actionPerformed(ActionEvent ev) {
                int num = conf.getCurrent();
                configPanel.saveConfig(num);
                conf.save();
            }
        });
        bar.add(saveButton);

        Button cancelButton = new Button(new AbstractAction(Messages.getString("Launcher.cancel_label")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("launcher canceled");
                System.exit(0);
            }
        });
        bar.add(cancelButton);

        Button configButton = new Button(new AbstractAction(Messages.getString("Launcher.config_label")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("view server configs");
                viewer.run(f);
                updateConfigCombo();
            }
        });
        bar.add(configButton);

        Button logViewerButton = new Button(new AbstractAction(Messages.getString("Launcher.logviewer_label")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String[] PATH_ELEM = {System.getProperty("user.home"), ".monsiaj", "logs"};
                final JFileChooser chooser = new JFileChooser(SystemEnvironment.createFilePath(PATH_ELEM).getAbsolutePath());
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    LogViewer lv = new LogViewer(chooser.getSelectedFile());
                    lv.run();
                }
            }
        });
        bar.add(logViewerButton);

        f.setSize(800, 480);
        f.setResizable(true);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.setVisible(true);

        run.requestFocus();
    }

    private static String toLowerCaseNullCheck(String str) {
        if (str == null) {
            str = "";
        }
        return str.toLowerCase();
    }

    private void connect() {
        conf.save();
        try {
            Client client = new Client(conf);
            client.connect();
        } catch (java.net.UnknownHostException e) {
            showErrorDialog(e, Messages.getString("Client.unknown_host_error"), Messages.getString("Client.unknown_host_error_msg") + e.getMessage());
        } catch (javax.net.ssl.SSLException e) {
            String msg = toLowerCaseNullCheck(e.getMessage());
            if (msg.contains("the trustAnchors parameter must be non-empty".toLowerCase())) {
                showErrorDialog(e, Messages.getString("Client.certificate_error"), Messages.getString("Client.invalid_ca_cert_format"));
            } else {
                showErrorDialog(e, Messages.getString("Client.other_error"), null);
            }
        } catch (IOException e) {
            switch (toLowerCaseNullCheck(e.getMessage())) {
                case "keystore password was incorrect":
                    showErrorDialog(e, Messages.getString("Client.certificate_error"), Messages.getString("Client.invalid_p12_pass"));
                    break;
                case "Detect premature EOF":
                case "toDerInputStream rejects tag type 45":
                    showErrorDialog(e, Messages.getString("Client.certificate_error"), Messages.getString("Client.invalid_p12_format"));
                    break;
                default:
                    showErrorDialog(e, Messages.getString("Client.other_error"), null);
                    break;
            }
        } catch (GeneralSecurityException | JSONException e) {
            showErrorDialog(e, Messages.getString("Client.other_error"), null);
        }
    }

    private void showErrorDialog(Exception e, String title, String message) {
        logger.catching(Level.FATAL, e);
        if (message == null) {
            message = Messages.getString("Client.other_error_see_log") + e.getLocalizedMessage();
        }
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    protected ConfigPanel createConfigurationPanel() {
        return new ConfigPanel(conf, true);
    }

    protected ConfigViewer createConfigurationViewer() {
        return new ConfigViewer(conf);
    }

    protected Icon createIcon() {
        URL iconURL = getClass().getResource("/images/orcamo-logo.png");
        if (iconURL != null) {
            return new ImageIcon(iconURL);
        }
        return null;
    }
}
