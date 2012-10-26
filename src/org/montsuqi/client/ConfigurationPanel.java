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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.net.URI;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.text.JTextComponent;
import org.montsuqi.util.ExtensionFileFilter;
import org.montsuqi.util.Logger;
import org.montsuqi.util.SystemEnvironment;

public class ConfigurationPanel extends JPanel {

    protected static final Logger logger = Logger.getLogger(ConfigurationPanel.class);
    protected Configuration conf;
    protected JPanel basicPanel;
    protected JPanel sslPanel;
    protected JPanel othersPanel;
    protected JPanel infoPanel;
    // Basic Tab
    protected JTextField userEntry;
    protected JPasswordField passwordEntry;
    protected JCheckBox savePasswordCheckbox;
    protected JTextField hostEntry;
    protected JTextField portEntry;
    protected JTextField appEntry;
    // SSL Tab
    protected JCheckBox useSSLCheckbox;
    protected JButton clientCertificateButton;
    protected JTextField clientCertificateEntry;
    protected JPasswordField exportPasswordEntry;
    protected JCheckBox saveClientCertificatePasswordCheckbox;
    // Others Tab
    protected JTextField styleEntry;
    protected JComboBox lookAndFeelCombo;
    protected JTextField lafThemeEntry;
    protected JButton lafThemeButton;
    protected JCheckBox useLogViewerCheck;
    protected JCheckBox useTimerCheck;
    protected JTextField timerPeriodEntry;
    protected JTextArea propertiesText;
    protected LookAndFeelInfo[] lafs;
    private static final int MAX_PANEL_ROWS = 12;
    private static final int MAX_PANEL_COLUMNS = 4;
    private boolean doPadding;
    private boolean doChangeLookAndFeel;
    private MetalTheme systemMetalTheme;

    /**
     * <p>An action to warn vulnerability of saving password.</p>
     */
    private final class ConfirmSavePasswordAction implements ActionListener {

        final JCheckBox checkbox;

        public ConfirmSavePasswordAction(JCheckBox checkbox) {
            this.checkbox = checkbox;
        }

        public void actionPerformed(ActionEvent e) {
            if (checkbox.isSelected()) {
                int result = JOptionPane.showConfirmDialog(ConfigurationPanel.this, Messages.getString("ConfigurationPanel.save_password_confirm"), Messages.getString("ConfigurationPanel.confirm"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
                if (result != JOptionPane.YES_OPTION) {
                    checkbox.setSelected(false);
                }
            }
        }
    }

    /**
     * <p>An action to pop a fiel selection dialog.</p> <p>When a file is
     * selected, the path of the selected file is set to specified text
     * field.</p>
     */
    private final class FileSelectionAction extends AbstractAction {

        private JTextComponent entry;
        private String extension;
        private String description;

        /**
         * <p>Constructs a FileSelectionAction.</p>
         *
         * @param entry a text field to which the path of the selected file is
         * set.
         * @param extension a file name extension passed to an
         * ExtensionFIleFilter.
         * @param description ditto.
         */
        FileSelectionAction(JTextComponent entry, String extension, String description) {
            super(Messages.getString("ConfigurationPanel.browse")); //$NON-NLS-1$
            this.entry = entry;
            this.extension = extension;
            this.description = description;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(entry.getText());
            fileChooser.setFileFilter(new ExtensionFileFilter(extension, description));
            int ret = fileChooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                entry.setText(file.getAbsolutePath());
            }
        }
    }

    private final class ThemeSelectionAction extends AbstractAction {

        private JTextComponent entry;
        private String extension;
        private String description;

        /**
         * <p>Constructs a FileSelectionAction.</p>
         *
         * @param entry a text field to which the path of the selected file is
         * set.
         * @param extension a file name extension passed to an
         * ExtensionFIleFilter.
         * @param description ditto.
         */
        ThemeSelectionAction(JTextComponent entry, String extension, String description) {
            super(Messages.getString("ConfigurationPanel.browse")); //$NON-NLS-1$
            this.entry = entry;
            this.extension = extension;
            this.description = description;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(entry.getText()); //$NON-NLS-1$
            fileChooser.setFileFilter(new ExtensionFileFilter(extension, description));
            int ret = fileChooser.showOpenDialog(null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                entry.setText(file.getAbsolutePath());
                changeLookAndFeel();
            }
        }
    }

    final class TextAreaSelected extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent e) {
            Object o = e.getSource();
            if (!(o instanceof JTextComponent)) {
                return;
            }
            JTextComponent tc = (JTextComponent) o;
            tc.setCaretPosition(tc.getText().length());
            tc.selectAll();
        }
    }

    protected void changeLookAndFeel() {
        changeLookAndFeel(lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
    }

    protected void changeLookAndFeel(String className) {
        if (doChangeLookAndFeel) {
            try {
                MetalLookAndFeel.setCurrentTheme(systemMetalTheme);
                if (className.startsWith("com.nilo.plaf.nimrod")) {
                    System.setProperty("nimrodlf.themeFile", lafThemeEntry.getText());
                    UIManager.setLookAndFeel(new NimRODLookAndFeel());
                } else {
                    UIManager.setLookAndFeel(className);
                }
                if (SystemEnvironment.isMacOSX() && (!className.startsWith("apple.laf.AquaLookAndFeel"))) {
                    updateFont(new Font("Osaka", Font.PLAIN, 12));
                }
            } catch (Exception e) {
                logger.warn(e);
            }
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Component root = SwingUtilities.getRoot(basicPanel);
                    try {
                        if (root != null) {
                            SwingUtilities.updateComponentTreeUI(root);
                        }

                    } catch (Exception e) {
                        logger.warn(e);
                    }
                }
            });
        }
    }

    protected ConfigurationPanel(Configuration conf, boolean doPadding, boolean doChangeLookAndFeel) {
        this.conf = conf;
        this.doPadding = doPadding;
        this.doChangeLookAndFeel = doChangeLookAndFeel;
        this.systemMetalTheme = MetalLookAndFeel.getCurrentTheme();
        basicPanel = createBasicPanel();
        sslPanel = createSSLPanel();
        othersPanel = createOthersPanel();
        infoPanel = createInfoPanel();
    }

    public void loadConfiguration(String configName, boolean newFlag) {
        // Basic tab
        String user = newFlag ? Configuration.DEFAULT_USER : conf.getUser(configName);
        String password = newFlag ? Configuration.DEFAULT_PASSWORD : conf.getPassword(configName);
        boolean savePassword = newFlag ? Configuration.DEFAULT_SAVE_PASSWORD : conf.getSavePassword(configName);
        String host = newFlag ? Configuration.DEFAULT_HOST : conf.getHost(configName);
        int port = newFlag ? Configuration.DEFAULT_PORT : conf.getPort(configName);
        String application = newFlag ? Configuration.DEFAULT_APPLICATION : conf.getApplication(configName);

        // SSL tab
        boolean useSSL = newFlag ? Configuration.DEFAULT_USE_SSL : conf.getUseSSL(configName);
        String clientCertificate = newFlag ? Configuration.DEFAULT_CLIENT_CERTIFICATE : conf.getClientCertificateFileName(configName);
        boolean saveClientCertificatePassword = newFlag ? Configuration.DEFAULT_SAVE_CLIENT_CERTIFICATE_PASSWORD : conf.getSaveClientCertificatePassword(configName);
        String clientCertificatePassword = newFlag ? Configuration.DEFAULT_CLIENT_CERTIFICATE_PASSWORD : conf.getClientCertificatePassword(configName);

        // Others tab
        String styleFileName = newFlag ? Configuration.DEFAULT_STYLES : conf.getStyleFileName(configName);
        String lookAndFeelClassName = newFlag ? Configuration.DEFAULT_LOOK_AND_FEEL_CLASS_NAME : conf.getLookAndFeelClassName(configName);
        String lafThemeFileName = newFlag ? Configuration.DEFAULT_LAF_THEME : conf.getLAFThemeFileName(configName);
        boolean useLogViewer = newFlag ? Configuration.DEFAULT_USE_LOG_VIEWER : conf.getUseLogViewer(configName);
        boolean useTimer = newFlag ? Configuration.DEFAULT_USE_TIMER : conf.getUseTimer(configName);
        long timerPeriod = newFlag ? Configuration.DEFAULT_TIMER_PERIOD : conf.getTimerPeriod(configName);
        String properties = newFlag ? Configuration.DEFAULT_PROPERTIES : conf.getProperties(configName);

        // Basic Tab
        userEntry.setText(user);
        // Save save_pass check field before the password itself,
        // since setPass fetches its value from the preferences internally.
        savePasswordCheckbox.setSelected(savePassword);
        passwordEntry.setText(password);
        hostEntry.setText(host);
        portEntry.setText(String.valueOf(port));
        appEntry.setText(application);

        // SSL Tab
        useSSLCheckbox.setSelected(useSSL);
        clientCertificateEntry.setText(clientCertificate);
        exportPasswordEntry.setText(clientCertificatePassword);
        saveClientCertificatePasswordCheckbox.setSelected(saveClientCertificatePassword);

        // Others Tab
        styleEntry.setText(styleFileName);
        lafThemeEntry.setText(lafThemeFileName);
        for (int i = 0; i < lafs.length; i++) {
            if (lookAndFeelClassName.equals(lafs[i].getClassName())) {
                lookAndFeelCombo.setSelectedItem(lafs[i].getName());
            }
        }
        useLogViewerCheck.setSelected(useLogViewer);
        useTimerCheck.setSelected(useTimer);
        timerPeriodEntry.setText(String.valueOf(timerPeriod));
        propertiesText.setText(properties);
        updateSSLPanelComponentsEnabled();
    }

    protected void saveConfiguration(String configName) {
        // Basic Tab
        conf.setUser(configName, userEntry.getText());
        // Save save_pass check field before the password itself,
        // since setPass fetches its value from the preferences internally.
        conf.setSavePassword(configName, savePasswordCheckbox.isSelected());
        conf.setPassword(configName, new String(passwordEntry.getPassword()));
        conf.setHost(configName, hostEntry.getText());
        conf.setPort(configName, Integer.parseInt(portEntry.getText()));
        conf.setApplication(configName, appEntry.getText());

        // SSL Tab
        conf.setUseSSL(configName, useSSLCheckbox.isSelected());
        conf.setClientCertificateFileName(configName, clientCertificateEntry.getText());
        conf.setClientCertificatePassword(configName, new String(exportPasswordEntry.getPassword()));
        conf.setSaveClientCertificatePassword(configName, saveClientCertificatePasswordCheckbox.isSelected());

        // Others Tab
        conf.setStyleFileName(configName, styleEntry.getText());
        conf.setLookAndFeelClassName(configName, lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
        conf.setLAFThemeFileName(configName, lafThemeEntry.getText());
        conf.setUseLogViewer(configName, useLogViewerCheck.isSelected());
        conf.setUseTimer(configName, useTimerCheck.isSelected());
        long period;
        try {
            period = Long.parseLong(timerPeriodEntry.getText());
        } catch (java.lang.NumberFormatException ex) {
            period = Configuration.DEFAULT_TIMER_PERIOD;
        }
        conf.setTimerPeriod(configName, Long.parseLong(timerPeriodEntry.getText()));
        conf.setProperties(configName, propertiesText.getText());
    }

    public static GridBagConstraints createConstraints(int x, int y, int width, int height, double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    public static JLabel createLabel(String str) {
        JLabel label = new JLabel(str);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setPreferredSize(new Dimension(170, 20));
        label.setMinimumSize(new Dimension(170, 20));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        return label;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setHorizontalAlignment(SwingConstants.LEFT);
        tf.setText("");
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setHorizontalAlignment(SwingConstants.LEFT);
        pf.setText("");
        return pf;
    }

    private JPanel createBasicPanel() {
        int y;
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        hostEntry = createTextField();
        portEntry = createTextField();
        portEntry.setColumns(5);
        appEntry = createTextField();
        userEntry = createTextField();
        passwordEntry = createPasswordField();
        savePasswordCheckbox = new JCheckBox();
        savePasswordCheckbox.addActionListener(new ConfirmSavePasswordAction(savePasswordCheckbox));

        y = 0;
        panel.add(createLabel(Messages.getString("ConfigurationPanel.host")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(hostEntry,
                createConstraints(1, y, 2, 1, 1.0, 0.0));
        panel.add(portEntry,
                createConstraints(3, y, 1, 1, 0.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.application")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(appEntry,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.user")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(userEntry,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.password")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(passwordEntry,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.save_password")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(savePasswordCheckbox,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        if (doPadding) {
            for (int i = y; i < MAX_PANEL_ROWS; i++) {
                panel.add(new JLabel(" "),
                        createConstraints(0, i, MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
            }
        }
        return panel;
    }

    private void updateSSLPanelComponentsEnabled() {
        final boolean useSsl = useSSLCheckbox.isSelected();
        clientCertificateEntry.setEnabled(useSsl);
        exportPasswordEntry.setEnabled(useSsl);
        clientCertificateButton.setEnabled(useSsl);
        saveClientCertificatePasswordCheckbox.setEnabled(useSsl);
    }

    private JPanel createSSLPanel() {
        int y;
        final String clientCertificateDescription = Messages.getString("ConfigurationPanel.client_certificate_description"); //$NON-NLS-1$

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        useSSLCheckbox = new JCheckBox();
        useSSLCheckbox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateSSLPanelComponentsEnabled();
            }
        });
        clientCertificateEntry = createTextField();
        clientCertificateButton = new JButton();
        clientCertificateButton.setAction(new FileSelectionAction(clientCertificateEntry, ".p12", clientCertificateDescription));
        exportPasswordEntry = createPasswordField();
        saveClientCertificatePasswordCheckbox = new JCheckBox();
        saveClientCertificatePasswordCheckbox.addActionListener(new ConfirmSavePasswordAction(saveClientCertificatePasswordCheckbox));

        y = 0;
        panel.add(createLabel(Messages.getString("ConfigurationPanel.use_ssl")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(useSSLCheckbox,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.client_certificate")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(clientCertificateEntry,
                createConstraints(1, y, 2, 1, 1.0, 0.0));
        panel.add(clientCertificateButton,
                createConstraints(3, y, 1, 1, 0.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.cert_password")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(exportPasswordEntry,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.save_cert_password")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(saveClientCertificatePasswordCheckbox,
                createConstraints(1, y, 3, 1, 0.0, 0.0));
        y++;

        if (doPadding) {
            for (int i = y; i < MAX_PANEL_ROWS; i++) {
                panel.add(new JLabel(" "),
                        createConstraints(0, i, MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
            }
        }

        return panel;
    }

    private void updateLAFThemeEnabled() {
        String laf = (String) lookAndFeelCombo.getSelectedItem();
        boolean isNimrod = laf.equals("Nimrod");
        lafThemeEntry.setEnabled(isNimrod);
        lafThemeButton.setEnabled(isNimrod);
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

    private JPanel createOthersPanel() {
        int y;
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        styleEntry = createTextField();
        JButton styleButton = new JButton();
        styleButton.setAction(
                new FileSelectionAction(styleEntry, ".properties",
                Messages.getString("ConfigurationPanel.style_filter_pattern")));
        lafs = UIManager.getInstalledLookAndFeels();
        String[] lafNames = new String[lafs.length];
        for (int i = 0; i < lafNames.length; i++) {
            lafNames[i] = lafs[i].getName();
        }
        lookAndFeelCombo = new JComboBox();
        lookAndFeelCombo.setEditable(false);
        for (int i = 0; i < lafNames.length; i++) {
            lookAndFeelCombo.addItem(lafNames[i]);
        }
        lookAndFeelCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateLAFThemeEnabled();
                changeLookAndFeel(lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
            }
        });
        lafThemeEntry = createTextField();
        lafThemeButton = new JButton();
        lafThemeButton.setAction(
                new ThemeSelectionAction(lafThemeEntry, ".theme",
                Messages.getString("ConfigurationPanel.laf_theme_filter_pattern")));

        useLogViewerCheck = new JCheckBox();

        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.X_AXIS));
        useTimerCheck = new JCheckBox();
        useTimerCheck.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                timerPeriodEntry.setEnabled(useTimerCheck.isSelected());
            }
        });
        timerPanel.add(useTimerCheck);
        JPanel timerPeriodEntryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timerPeriodEntryPanel.add(new JLabel(Messages.getString("ConfigurationPanel.timer_period")));
        timerPeriodEntry = new JTextField(5);
        timerPeriodEntryPanel.add(timerPeriodEntry);
        timerPanel.add(timerPeriodEntryPanel);

        propertiesText = new JTextArea(10, 30);
        propertiesText.addFocusListener(new TextAreaSelected());
        JScrollPane propertiesScroll = new JScrollPane(propertiesText, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        propertiesScroll.setMinimumSize(new Dimension(0, 100));

        y = 0;
        panel.add(createLabel(Messages.getString("ConfigurationPanel.style")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(styleEntry,
                createConstraints(1, y, 2, 1, 1.0, 0.0));
        panel.add(styleButton,
                createConstraints(3, y, 1, 1, 0.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.look_and_feel")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(lookAndFeelCombo,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.laf_theme")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(lafThemeEntry,
                createConstraints(1, y, 2, 1, 1.0, 0.0));
        panel.add(lafThemeButton,
                createConstraints(3, y, 1, 1, 0.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.use_log_viewer")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(useLogViewerCheck,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.use_timer")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(timerPanel,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.additional_system_properties")),
                createConstraints(0, y, 1, 6, 0.0, 1.0));
        panel.add(propertiesScroll,
                createConstraints(1, y, 3, 6, 1.0, 1.0));
        y += 6;
        if (doPadding) {
            for (int i = y; i < MAX_PANEL_ROWS; i++) {
                panel.add(new JLabel(" "),
                        createConstraints(0, i, MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
            }
        }
        return panel;
    }

    private JPanel createInfoPanel() {
        int y;
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel version = new JLabel("monsiaj ver." + Messages.getString("application.version"));
        version.setHorizontalAlignment(SwingConstants.CENTER);
        version.setFont(new Font(null, Font.BOLD, 20));
        JLabel copy = new JLabel("Copyright (C) 2007 ORCA Project");
        copy.setHorizontalAlignment(SwingConstants.CENTER);
        JButton orcaButton = new JButton("<html><a href=\"\">ORCA Project Website</a></html>");
        orcaButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Desktop d = Desktop.getDesktop();
                if (Desktop.isDesktopSupported() && d.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        d.browse(new URI(Messages.getString("ConfigurationPanel.info_orca_url")));
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
            }
        });
        JButton montsuqiButton = new JButton("<html><a href=\"\">montsuqi.org</a></html>");
        montsuqiButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Desktop d = Desktop.getDesktop();
                if (Desktop.isDesktopSupported() && d.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        d.browse(new URI(Messages.getString("ConfigurationPanel.info_montsuqi_url")));
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }                
            }
        });

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBorder(BorderFactory.createLineBorder((Color) SystemColor.controlDkShadow));
        JLabel javaVersion = new JLabel(
                Messages.getString("ConfigurationPanel.info_java_version")
                + System.getProperty("java.version"));
        javaVersion.setHorizontalAlignment(SwingConstants.LEFT);
        JLabel osVersion = new JLabel(
                Messages.getString("ConfigurationPanel.info_os_version")
                + System.getProperty("os.name") + "-"
                + System.getProperty("os.version") + "-"
                + System.getProperty("os.arch"));
        osVersion.setHorizontalAlignment(SwingConstants.LEFT);
        innerPanel.add(javaVersion);
        innerPanel.add(osVersion);
        innerPanel.add(new JLabel(" "));
        innerPanel.add(new JLabel(" "));
        innerPanel.add(new JLabel(" "));

        y = 0;
        panel.add(version,
                createConstraints(0, y, 4, 3, 1.0, 1.0));
        y += 3;

        panel.add(copy,
                createConstraints(0, y, 4, 1, 1.0, 1.0));
        y++;

        panel.add(orcaButton,
                createConstraints(0, y, 4, 1, 0.0, 1.0));
        y++;

        panel.add(montsuqiButton,
                createConstraints(0, y, 4, 1, 0.0, 1.0));
        y++;

        panel.add(innerPanel,
                createConstraints(0, y, 4, 1, 1.0, 1.0));
        y += 1;


        if (doPadding) {
            for (int i = y; i < MAX_PANEL_ROWS; i++) {
                panel.add(new JLabel(" "),
                        createConstraints(0, i, MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
            }
        }
        return panel;
    }

    public JPanel getBasicPanel() {
        return basicPanel;
    }

    public JPanel getSSLPanel() {
        return sslPanel;
    }

    public JPanel getOthersPanel() {
        return othersPanel;
    }

    public JPanel getInfoPanel() {
        return infoPanel;
    }
}
