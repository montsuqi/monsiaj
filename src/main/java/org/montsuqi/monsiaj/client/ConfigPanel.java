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
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.text.JTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.util.ExtensionFileFilter;

public class ConfigPanel extends JPanel {

    private static final Logger logger = LogManager.getLogger(ConfigPanel.class);
    private final Config conf;

    // Basic Tab
    private final JPanel basicPanel;
    private JTextField userEntry;
    private JPasswordField passwordEntry;
    private JCheckBox savePasswordCheckbox;
    private JTextField authURIEntry;
    private JCheckBox useSSOCheckbox;
    // SSL Tab
    private final JPanel sslPanel;
    private JCheckBox useSSLCheckbox;
    private JButton caCertificateButton;
    private JTextField caCertificateEntry;
    private JButton clientCertificateButton;
    private JTextField clientCertificateEntry;
    private JPasswordField exportPasswordEntry;
    private JCheckBox saveClientCertificatePasswordCheckbox;
    private JCheckBox usePKCS11Checkbox;
    private JButton pkcs11LibButton;
    private JTextField pkcs11LibEntry;
    private JTextField pkcs11SlotEntry;
    // Print Tab
    private final PrinterConfigPanel printerConfigPanel;
    // Others Tab
    private final JPanel othersPanel;
    private JTextField styleEntry;
    private JComboBox<String> lookAndFeelCombo;
    private JTextField lafThemeEntry;
    private JButton lafThemeButton;
    private JCheckBox useTimerCheck;
    private JTextField timerPeriodEntry;
    private JCheckBox showStartupMessageCheck;
    private JTextArea propertiesText;
    private LookAndFeelInfo[] lafs;
    private static final int MAX_PANEL_ROWS = 12;
    private static final int MAX_PANEL_COLUMNS = 4;
    private final boolean doPadding;
    private final MetalTheme systemMetalTheme;
    // Info Tab
    protected JPanel infoPanel;

    /**
     * <p>
     * An action to pop a fiel selection dialog.</p>
     * <p>
     * When a file is selected, the path of the selected file is set to
     * specified text field.</p>
     */
    private final class FileSelectionAction extends AbstractAction {

        private final JTextComponent entry;
        private final String extension;
        private final String description;

        /**
         * <p>
         * Constructs a FileSelectionAction.</p>
         *
         * @param entry a text field to which the path of the selected file is
         * set.
         * @param extension a file name extension passed to an
         * ExtensionFIleFilter.
         * @param description ditto.
         */
        FileSelectionAction(JTextComponent entry, String extension, String description) {
            super(Messages.getString("ConfigurationPanel.browse"));
            this.entry = entry;
            this.extension = extension;
            this.description = description;
        }

        @Override
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

        private final JTextComponent entry;
        private final String extension;
        private final String description;

        ThemeSelectionAction(JTextComponent entry, String extension, String description) {
            super(Messages.getString("ConfigurationPanel.browse"));
            this.entry = entry;
            this.extension = extension;
            this.description = description;
        }

        @Override
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

    protected ConfigPanel(Config conf, boolean doPadding) {
        this.conf = conf;
        this.doPadding = doPadding;
        this.systemMetalTheme = MetalLookAndFeel.getCurrentTheme();
        basicPanel = createBasicPanel();
        sslPanel = createSSLPanel();
        printerConfigPanel = new PrinterConfigPanel(conf.getPrinterList());
        othersPanel = createOthersPanel();
        infoPanel = createInfoPanel();
    }

    public void loadConfig(int num) {
        // Basic tab
        String user = conf.getUser(num);
        String password = conf.getPassword(num);
        boolean savePassword = conf.getSavePassword(num);
        String authURI = conf.getAuthURI(num);
        boolean use_sso = conf.getUseSSO(num);
        useSSOCheckbox.setSelected(use_sso);

        userEntry.setText(user);
        // Save save_pass check field before the password itself,
        // since setPass fetches its value from the preferences internally.
        savePasswordCheckbox.setSelected(savePassword);
        passwordEntry.setText(password);
        authURIEntry.setText(authURI);

        // SSL tab
        boolean useSSL = conf.getUseSSL(num);
        String caCertificateFile = conf.getCACertificateFile(num);
        String clientCertificateFile = conf.getClientCertificateFile(num);
        boolean saveClientCertificatePassword = conf.getSaveClientCertificatePassword(num);
        String clientCertificatePassword = conf.getClientCertificatePassword(num);
        boolean usePKCS11 = conf.getUsePKCS11(num);
        String pkcs11Lib = conf.getPKCS11Lib(num);
        String slot = conf.getPKCS11Slot(num);

        useSSLCheckbox.setSelected(useSSL);
        caCertificateEntry.setText(caCertificateFile);
        clientCertificateEntry.setText(clientCertificateFile);
        exportPasswordEntry.setText(clientCertificatePassword);
        saveClientCertificatePasswordCheckbox.setSelected(saveClientCertificatePassword);
        usePKCS11Checkbox.setSelected(usePKCS11);
        pkcs11LibEntry.setText(pkcs11Lib);
        pkcs11SlotEntry.setText(slot);

        // Printer tab
        printerConfigPanel.setPrinterConfigMap(conf.getPrinterConfig(num));

        // Others tab
        String styleFile = conf.getStyleFile(num);
        String lookAndFeel = conf.getLookAndFeel(num);
        String lookAndFeelThemeFile = conf.getLookAndFeelThemeFile(num);
        boolean useTimer = conf.getUseTimer(num);
        long timerPeriod = conf.getTimerPeriod(num);
        boolean showStartupMessage = conf.getShowStartupMessage(num);
        String systemProperties = conf.getSystemProperties(num);

        styleEntry.setText(styleFile);
        lafThemeEntry.setText(lookAndFeelThemeFile);
        for (LookAndFeelInfo laf : lafs) {
            if (lookAndFeel.equals(laf.getClassName())) {
                lookAndFeelCombo.setSelectedItem(laf.getName());
            }
        }
        useTimerCheck.setSelected(useTimer);
        timerPeriodEntry.setText(String.valueOf(timerPeriod));
        showStartupMessageCheck.setSelected(showStartupMessage);
        propertiesText.setText(systemProperties);

        this.updatePKCS11PanelComponentsEnabled();
    }

    protected void saveConfig(int num, String desc) {
        conf.setDescription(num, desc);
        this.saveConfig(num);
    }

    protected void saveConfig(int num) {
        // Basic Tab
        conf.setUser(num, userEntry.getText());
        // Save save_pass check field before the password itself,
        // since setPass fetches its value from the preferences internally.
        conf.setSavePassword(num, savePasswordCheckbox.isSelected());
        String password = new String(passwordEntry.getPassword());
        conf.setPassword(num, password.trim());
        conf.setAuthURI(num, authURIEntry.getText());
        conf.setUseSSO(num, useSSOCheckbox.isSelected());

        // SSL Tab
        conf.setUseSSL(num, useSSLCheckbox.isSelected());
        conf.setCACertificateFile(num, caCertificateEntry.getText());
        conf.setClientCertificateFile(num, clientCertificateEntry.getText());
        String exportPassword = new String(exportPasswordEntry.getPassword());
        conf.setClientCertificatePassword(num, exportPassword.trim());
        conf.setSaveClientCertificatePassword(num, saveClientCertificatePasswordCheckbox.isSelected());
        conf.setUsePKCS11(num, usePKCS11Checkbox.isSelected());
        conf.setPKCS11Lib(num, pkcs11LibEntry.getText());
        conf.setPKCS11Slot(num, pkcs11SlotEntry.getText());

        // Printer Tab
        conf.setPrinterConfig(num, printerConfigPanel.getPrinterConfigMap());

        // Others Tab
        conf.setStyleFile(num, styleEntry.getText());
        conf.setLookAndFeel(num, lafs[lookAndFeelCombo.getSelectedIndex()].getClassName());
        conf.setLookAndFeelThemeFile(num, lafThemeEntry.getText());
        conf.setUseTimer(num, useTimerCheck.isSelected());
        conf.setTimerPeriod(num, Integer.valueOf(timerPeriodEntry.getText()));
        conf.setShowStartupMessage(num, showStartupMessageCheck.isSelected());
        conf.setSystemProperties(num, propertiesText.getText());

        conf.save();
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
        label.setPreferredSize(new Dimension(200, 20));
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

        authURIEntry = createTextField();
        userEntry = createTextField();
        passwordEntry = createPasswordField();
        savePasswordCheckbox = new JCheckBox();
        useSSOCheckbox = new JCheckBox();

        y = 0;
        panel.add(createLabel(Messages.getString("ConfigurationPanel.authURI")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(authURIEntry,
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

        panel.add(createLabel(Messages.getString("ConfigurationPanel.use_sso_client_verification")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(useSSOCheckbox,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        if (doPadding) {
            for (int i = y; i < MAX_PANEL_ROWS; i++) {
                panel.add(new JLabel(" "), createConstraints(0, i, MAX_PANEL_COLUMNS, 1, 1.0, 1.0));
            }
        }
        return panel;
    }

    private void updatePKCS11PanelComponentsEnabled() {
        final boolean usePKCS11 = usePKCS11Checkbox.isSelected();
        pkcs11LibEntry.setEnabled(usePKCS11);
        pkcs11LibButton.setEnabled(usePKCS11);
        pkcs11SlotEntry.setEnabled(usePKCS11);
    }

    private JPanel createSSLPanel() {
        int y;
        final String clientCertificateDescription = Messages.getString("ConfigurationPanel.client_certificate_description");
        final String caCertificateDescription = Messages.getString("ConfigurationPanel.ca_certificate_description");

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        useSSLCheckbox = new JCheckBox();
        caCertificateEntry = createTextField();
        caCertificateButton = new JButton();
        caCertificateButton.setAction(new FileSelectionAction(caCertificateEntry, ".crt", caCertificateDescription));
        clientCertificateEntry = createTextField();
        clientCertificateButton = new JButton();
        clientCertificateButton.setAction(new FileSelectionAction(clientCertificateEntry, ".p12", clientCertificateDescription));
        exportPasswordEntry = createPasswordField();
        saveClientCertificatePasswordCheckbox = new JCheckBox();

        usePKCS11Checkbox = new JCheckBox();
        usePKCS11Checkbox.addActionListener((ActionEvent e) -> {
            updatePKCS11PanelComponentsEnabled();
        });
        pkcs11LibEntry = createTextField();
        pkcs11LibButton = new JButton();
        pkcs11LibButton.setAction(new FileSelectionAction(pkcs11LibEntry, "", Messages.getString("ConfigurationPanel.pkcs11_lib_description")));
        pkcs11SlotEntry = createTextField();

        y = 0;
        panel.add(createLabel(Messages.getString("ConfigurationPanel.use_ssl_client_verification")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(useSSLCheckbox,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.ca_certificate")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(caCertificateEntry,
                createConstraints(1, y, 2, 1, 1.0, 0.0));
        panel.add(caCertificateButton,
                createConstraints(3, y, 1, 1, 0.0, 0.0));
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

        panel.add(createLabel(Messages.getString("ConfigurationPanel.use_pkcs11")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(usePKCS11Checkbox,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.pkcs11_lib")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(pkcs11LibEntry,
                createConstraints(1, y, 2, 1, 1.0, 0.0));
        panel.add(pkcs11LibButton,
                createConstraints(3, y, 1, 1, 0.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.pkcs11_slot")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(pkcs11SlotEntry,
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

    private void updateLAFThemeEnabled() {
        String laf = (String) lookAndFeelCombo.getSelectedItem();
        boolean isNimrod = laf.equals("Nimrod");
        lafThemeEntry.setEnabled(isNimrod);
        lafThemeButton.setEnabled(isNimrod);
    }

    private JPanel createOthersPanel() {
        int y;
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));

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
        lookAndFeelCombo = new JComboBox<>();
        lookAndFeelCombo.setEditable(false);
        for (String lafName : lafNames) {
            lookAndFeelCombo.addItem(lafName);
        }
        lookAndFeelCombo.addActionListener((ActionEvent e) -> {
            updateLAFThemeEnabled();
        });
        lafThemeEntry = createTextField();
        lafThemeButton = new JButton();
        lafThemeButton.setAction(
                new ThemeSelectionAction(lafThemeEntry, ".theme",
                        Messages.getString("ConfigurationPanel.laf_theme_filter_pattern")));

        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.X_AXIS));
        timerPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        useTimerCheck = new JCheckBox();
        useTimerCheck.addActionListener((ActionEvent e) -> {
            timerPeriodEntry.setEnabled(useTimerCheck.isSelected());
        });
        timerPanel.add(useTimerCheck);
        JPanel timerPeriodEntryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timerPeriodEntryPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        timerPeriodEntryPanel.add(new JLabel(Messages.getString("ConfigurationPanel.timer_period")));
        timerPeriodEntry = new JTextField(5);
        timerPeriodEntryPanel.add(timerPeriodEntry);
        timerPanel.add(timerPeriodEntryPanel);

        showStartupMessageCheck = new JCheckBox();

        propertiesText = new JTextArea(10, 50);
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

        panel.add(createLabel(Messages.getString("ConfigurationPanel.use_timer")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(timerPanel,
                createConstraints(1, y, 3, 1, 1.0, 0.0));
        y++;

        panel.add(createLabel(Messages.getString("ConfigurationPanel.show_startup_message")),
                createConstraints(0, y, 1, 1, 0.0, 1.0));
        panel.add(showStartupMessageCheck,
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

        JLabel version = new JLabel("monsiaj ver." + ConfigPanel.class
                .getPackage().getImplementationVersion());
        version.setHorizontalAlignment(SwingConstants.CENTER);
        version.setFont(new Font(null, Font.BOLD, 20));
        JLabel copy = new JLabel("Copyright (C) 2017 ORCA Project");
        copy.setHorizontalAlignment(SwingConstants.CENTER);
        JButton orcaButton = new JButton("<html><a href=\"\">ORCA Project Website</a></html>");
        orcaButton.addActionListener((ActionEvent e) -> {
            Desktop d = Desktop.getDesktop();
            if (Desktop.isDesktopSupported() && d.isSupported(Desktop.Action.BROWSE)) {
                try {
                    d.browse(new URI(Messages.getString("ConfigurationPanel.info_orca_url")));
                } catch (URISyntaxException | IOException ex) {
                    logger.warn(ex);
                }
            }
        });
        JButton montsuqiButton = new JButton("<html><a href=\"\">montsuqi.org</a></html>");
        montsuqiButton.addActionListener((ActionEvent e) -> {
            Desktop d = Desktop.getDesktop();
            if (Desktop.isDesktopSupported() && d.isSupported(Desktop.Action.BROWSE)) {
                try {
                    d.browse(new URI(Messages.getString("ConfigurationPanel.info_montsuqi_url")));
                } catch (URISyntaxException | IOException ex) {
                    logger.warn(ex);
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

    public PrinterConfigPanel getPrinterConfigPanel() {
        return printerConfigPanel;
    }

    public JPanel getOthersPanel() {
        return othersPanel;
    }

    public JPanel getInfoPanel() {
        return infoPanel;
    }
}
