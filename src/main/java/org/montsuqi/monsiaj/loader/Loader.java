package org.montsuqi.monsiaj.loader;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loader {

    private static final Logger LOG = LogManager.getLogger(Loader.class);
    private final String VERSION_URL = "https://dl.orca.med.or.jp/java-client/version";
    private final String DOWNLOAD_URL = "https://dl.orca.med.or.jp/java-client/";
    private final String[] CACHE_DIR_PATH_ELEM = {System.getProperty("user.home"), ".monsiaj", "cache"};
    private final String CACHE_DIR = createPath(CACHE_DIR_PATH_ELEM).getAbsolutePath();
    private static final String[] PROP_PATH_ELEM = {System.getProperty("user.home"), ".monsiaj", "loader2.properties"};
    private static final String PROP_PATH = createPath(PROP_PATH_ELEM).getAbsolutePath();
    private Properties prop;
    private JDialog progress;
    private String orcaId;
    private String accessKey;
    private boolean saveAccessKey;
    private static final int MAX_TRY = 5;

    class UnAuthorized extends Exception {
    }

    private Loader() {
        prop = new Properties();
        try {
            prop.load(new FileInputStream(PROP_PATH));
        } catch (IOException ex) {
            // initial
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private static File createPath(String[] elements) {
        String path = "";
        for (String elem : elements) {
            if (path.isEmpty()) {
                path = elem;
            } else {
                path = path + File.separator + elem;
            }
        }
        return new File(path);
    }

    private String getProperty(String key) {
        if (prop.containsKey(key)) {
            return prop.getProperty(key);
        } else {
            return null;
        }
    }

    private String getProperty(String key, String def) {
        if (prop.containsKey(key)) {
            return prop.getProperty(key);
        } else {
            return def;
        }
    }

    private void removeProperty(String key) {
        setProperty(key, null);
    }

    private void setProperty(String key, String value) {
        if (value == null) {
            prop.remove(key);
        } else {
            prop.setProperty(key, value);
        }
        try {
            File file = new File(PROP_PATH);
            prop.store(new FileOutputStream(file), PROP_PATH);
            file.setExecutable(false);
            file.setReadable(false, false);
            file.setWritable(false, false);
            file.setReadable(true, true);
            file.setWritable(true, true);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private String getOrcaId() {
        return getProperty("orcaId", "");
    }

    private void setOrcaId(String val) {
        setProperty("orcaId", val);
    }

    private String getAccessKey() {
        return getProperty("accessKey", "");
    }

    private void setAccessKey(String val) {
        setProperty("accessKey", val);
    }

    private boolean getSaveAccessKey() {
        String sa = getProperty("saveAccessKey", "true");
        if (sa.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    private void setSaveAccessKey(boolean val) {
        if (val) {
            setProperty("saveAccessKey", "true");
        } else {
            setProperty("saveAccessKey", "false");
        }
    }

    private String getVersionURL() {
        return getProperty("versionURL", VERSION_URL);
    }

    private String getDownloadURL() {
        return getProperty("downloadURL", DOWNLOAD_URL);
    }

    private String getCacheVersion() {
        return getProperty("cacheVersion", "");
    }

    private void setCacheVersion(String val) {
        setProperty("cacheVersion", val);
    }

    private void createProgress(String msg) {
        progress = new JDialog();
        progress.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel label = new JLabel(msg);
        label.setFont(new Font("Suns", Font.BOLD, 14));
        panel.add(label);
        progress.getContentPane().add(panel);
        progress.pack();
        progress.setLocationRelativeTo(null);
    }

    private void showProgress() {
        progress.setVisible(true);
    }

    private void hideProgress() {
        progress.setVisible(false);
        progress = null;
    }

    private void updateCache(String version) throws IOException {
        LOG.debug("-- updateCache");
        /*
         * キャッシュディレクトリの作成
         */
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                throw new IOException("cant make cachedir");
            }
        }

        /*
         * jarファイルのダウンロード
         */
        String strJarFile = "monsiaj-" + version + "-all.jar";
        String strURL = getDownloadURL() + strJarFile;
        LOG.info("download start " + strURL);
        createProgress("バージョン" + version + "のダウンロード中...");
        EventQueue.invokeLater(() -> {
            showProgress();
        });

        try {
            HttpURLConnection con = httpGet(strURL);
            File jarFile = createPath(new String[]{CACHE_DIR, strJarFile});
            BufferedInputStream in = new BufferedInputStream(con.getInputStream());
            int length;
            try ( BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(jarFile))) {
                while ((length = in.read()) != -1) {
                    out.write(length);
                }
            }
            con.disconnect();
            EventQueue.invokeLater(() -> {
                hideProgress();
            });
            LOG.info("... complete");
            LOG.debug("-- updateCache end");
        } catch (UnAuthorized ex) {
            LOG.error("unexpected UnAuthorized Error");
            showErrorDialog("認証エラーです。医療機関ID、アクセスキーを確認してください。");
        }
    }

    private HttpURLConnection httpGet(String strURL) throws IOException, UnAuthorized {
        URL url = new URL(strURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        if (orcaId != null && accessKey != null) {
            con.setAuthenticator(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(orcaId, accessKey.toCharArray());
                }
            });
        }
        con.setInstanceFollowRedirects(true);
        con.setRequestMethod("GET");
        con.connect();
        switch (con.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                return con;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                con.disconnect();
                throw new UnAuthorized();
            default:
                throw new IOException("http status code:" + con.getResponseCode());
        }
    }

    private void getAccessKey(int n) {
        // 初回だけアクセスキーを設定ファイルから読み込む
        if (n == 0) {
            orcaId = getOrcaId();
            accessKey = getAccessKey();
            saveAccessKey = getSaveAccessKey();
            if (!accessKey.isEmpty()) {
                return;
            }
        }
        JTextField id = new JTextField(orcaId);
        JTextField ak = new JPasswordField();
        JCheckBox cak = new JCheckBox("アクセスキーを保存する", saveAccessKey);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
        panel.add(new JLabel("医療機関ID"));
        panel.add(id);
        panel.add(new JLabel("アクセスキー"));
        panel.add(ak);
        panel.add(new JLabel(""));
        panel.add(cak);
        Object[] options = {"OK", "キャンセル"};
        int select = JOptionPane.showOptionDialog(null, panel, "アクセスキー入力",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);
        if (select == 0) {
            orcaId = id.getText();
            accessKey = ak.getText();
            saveAccessKey = cak.isSelected();
            setSaveAccessKey(saveAccessKey);
        } else {
            LOG.info("cancel input accessKey");
            System.exit(0);
        }
    }

    private String getVersion() throws IOException {
        for (int i = 0; i < MAX_TRY; i++) {
            try {
                String url = getVersionURL();
                LOG.info("get version from " + url);
                getAccessKey(i);
                HttpURLConnection con = httpGet(url);
                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String version = reader.readLine();
                LOG.info("version: " + version);
                con.disconnect();
                // 認証が通った場合のみID、アクセスキーを保存
                setOrcaId(orcaId);
                if (saveAccessKey) {
                    setAccessKey(accessKey);
                } else {
                    setAccessKey(null);
                }
                return version;
            } catch (UnAuthorized ex) {
                LOG.info("UnAuthorized Error");
            }
        }
        LOG.info("UnAuthorized Error limit reached");
        showErrorDialog("規定回数失敗したため終了します。");
        return ""; // not reach
    }

    private void checkCache() {
        String useCache = getProperty("useCache");
        if (useCache != null && useCache.equals("true")) {
            LOG.info("skip cache check");
            return;
        }

        try {
            LOG.debug("-- checkCache start");
            String cacheVersion = getCacheVersion();
            String serverVersion = getVersion();

            LOG.info("cacheVersion : " + cacheVersion);
            LOG.info("serverVersion: " + serverVersion);

            if (cacheVersion.equals(serverVersion)) {
                LOG.info("use cache");
            } else {
                updateCache(serverVersion);
                setCacheVersion(serverVersion);
            }
            LOG.debug("-- checkCache end");
        } catch (IOException ex) {
            LOG.error("checkCache failure");
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void invokeLauncher(String[] args) throws Exception {
        String version = getCacheVersion();
        File file = createPath(new String[]{CACHE_DIR, "monsiaj-" + version + "-all.jar"});
        String noVerify = getProperty("noVerify");
        if (noVerify != null && noVerify.equals("true")) {
            LOG.info("no verify");
        } else {
            if (!JarVerifier.verify(new JarFile(file))) {
                setCacheVersion(null);
                LOG.error("jar sign verification error");
                showErrorDialog("ランチャーの署名の検証に失敗しました。詳細はログを確認してください。");
            }
        }
        System.setProperty("monsia.use.loader", "true");
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
        Class<?> cobj = loader.loadClass("org.montsuqi.monsiaj.client.Launcher");
        Method m = cobj.getMethod("main", new Class[]{args.getClass()});
        m.setAccessible(true);
        int mods = m.getModifiers();
        if (m.getReturnType() != void.class || !Modifier.isStatic(mods)
                || !Modifier.isPublic(mods)) {
            throw new NoSuchMethodException("main");
        }
        m.invoke(null, new Object[]{args});
    }

    private void launch(String[] args) throws Exception {
        try {
            LOG.debug("-- launch start");
            invokeLauncher(args);
        } catch (Exception ex) {
            setCacheVersion(null);
            LOG.error(ex.getMessage(), ex);
            LOG.info("remove cacheVersion");
            showErrorDialog("ランチャーの起動に失敗しました。詳細はログを確認してください。");
        }
    }

    public static void main(String[] args) throws Exception {
        LOG.info("---- Loader start");
        Loader loader = new Loader();
        loader.checkCache();
        loader.launch(args);
        LOG.info("---- Loader end");
    }
}
