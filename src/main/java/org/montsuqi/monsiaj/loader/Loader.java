package org.montsuqi.monsiaj.loader;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.jar.JarFile;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loader {

    private static Logger log = LogManager.getLogger(Loader.class);
    private final String VERSION_URL = "http://ftp.orca.med.or.jp/pub/java-client2/version";
    private final String DOWNLOAD_URL = "http://ftp.orca.med.or.jp/pub/java-client2/";
    private final String[] CACHE_DIR_PATH_ELEM = {System.getProperty("user.home"), ".monsiaj", "cache"};
    private final String CACHE_DIR = createPath(CACHE_DIR_PATH_ELEM).getAbsolutePath();
    private static final String[] PROP_PATH_ELEM = {System.getProperty("user.home"), ".monsiaj", "loader.properties"};
    private static final String PROP_PATH = createPath(PROP_PATH_ELEM).getAbsolutePath();
    private Properties prop;
    private JDialog progress;

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
            log.error(ex.getMessage(), ex);
        }
    }

    private String getCacheVersion() {
        return getProperty("cacheVersion");
    }

    private void setCacheVersion(String v) {
        setProperty("cacheVersion", v);
    }

    private String getVersionURL() {
        String url = getProperty("versionURL");
        if (url == null) {
            url = VERSION_URL;
            setProperty("versionURL", VERSION_URL);
        }
        return url;
    }

    private String getDownloadURL() {
        String url = getProperty("downloadURL");
        if (url == null) {
            url = DOWNLOAD_URL;
            setProperty("downloadURL", DOWNLOAD_URL);
        }
        return url;
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
        log.debug("-- updateCache");
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
        log.info("download start " + strURL);
        createProgress("バージョン" + version + "のダウンロード中...");
        EventQueue.invokeLater(() -> {
            showProgress();
        });
        HttpURLConnection con = httpGet(strURL);
        File jarFile = createPath(new String[]{CACHE_DIR, strJarFile});
        BufferedInputStream in = new BufferedInputStream(con.getInputStream());
        int length;
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(jarFile))) {
            while ((length = in.read()) != -1) {
                out.write(length);
            }
        }
        con.disconnect();
        EventQueue.invokeLater(() -> {
            hideProgress();
        });
        log.info("... complete");
        log.debug("-- updateCache end");
    }

    private static HttpURLConnection httpGet(String strURL) throws IOException {
        URL url = new URL(strURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setInstanceFollowRedirects(false);
        con.setRequestMethod("GET");
        con.connect();
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            con.disconnect();
            throw new IOException("" + con.getResponseCode());
        }
        return con;
    }

    private String getVersion() throws IOException {
        HttpURLConnection con = httpGet(getVersionURL());
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String version = reader.readLine();
        con.disconnect();
        return version;
    }

    private void checkCache() {
        String useCache = getProperty("useCache");
        if (useCache != null && useCache.equals("true")) {
            log.info("skip cache check");
            return;
        }

        try {
            log.debug("-- checkCache start");
            String cacheVersion = getCacheVersion();
            String serverVersion = getVersion();

            log.info("cacheVersion : " + cacheVersion);
            log.info("serverVersion: " + serverVersion);

            if (cacheVersion != null && cacheVersion.equals(serverVersion)) {
                log.info("use cache");
            } else {
                updateCache(serverVersion);
                setCacheVersion(serverVersion);
            }
            log.debug("-- checkCache end");
        } catch (IOException ex) {
            log.error("checkCache failure");
            log.error(ex.getMessage(), ex);
        }
    }

    private void invokeLauncher(String[] args) throws Exception {
        String version = getCacheVersion();
        File file = createPath(new String[]{CACHE_DIR, "monsiaj-" + version + "-all.jar"});
        String noVerify = getProperty("noVerify");
        if (noVerify != null && noVerify.equals("true")) {
            log.info("no verify");
        } else {
            if (!JarVerifier.verify(new JarFile(file))) {
                log.error("jar sign verification error");
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
            log.debug("-- launch start");
            invokeLauncher(args);
        } catch (Exception ex) {
            setCacheVersion(null);
            log.error(ex.getMessage(), ex);
            log.info("remove cacheVersion");
            showErrorDialog("ランチャーの起動に失敗しました。詳細はログを確認してください。");
        }
    }

    public static void main(String[] args) throws Exception {
        log.info("---- Loader start");
        Loader loader = new Loader();
        loader.checkCache();
        loader.launch(args);
        log.info("---- Loader end");
    }
}
