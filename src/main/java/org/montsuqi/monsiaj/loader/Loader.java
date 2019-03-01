package org.montsuqi.monsiaj.loader;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.jar.JarFile;
import org.apache.logging.log4j.Level;
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

    private String loadCacheVersion() {
        prop = new Properties();
        try {
            prop.load(new FileInputStream(PROP_PATH));
        } catch (IOException ex) {
            // initial
        }
        if (prop.containsKey("cacheVersion")) {
            return prop.getProperty("cacheVersion");
        } else {
            return null;
        }
    }

    private void saveCacheVersion(String v) {
        prop.setProperty("cacheVersion", v);
        try {
            prop.store(new FileOutputStream(Loader.PROP_PATH), PROP_PATH);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void removeCacheVersion() {
        prop.clear();
        try {
            prop.store(new FileOutputStream(Loader.PROP_PATH), PROP_PATH);
        } catch (IOException ex) {
            log.catching(Level.ERROR, ex);
        }
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
        String strURL = DOWNLOAD_URL + strJarFile;
        log.info("download " + strURL);
        HttpURLConnection con = httpGet(strURL);
        File jarFile = createPath(new String[]{CACHE_DIR,strJarFile});
        BufferedInputStream in = new BufferedInputStream(con.getInputStream());
        int length;
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(jarFile))) {
            while ((length = in.read()) != -1) {
                out.write(length);
            }
        }
        con.disconnect();
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
        HttpURLConnection con = httpGet(VERSION_URL);
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String version = reader.readLine();
        con.disconnect();
        return version;
    }

    private void checkCache() {
        try {
            log.debug("-- checkCache start");
            String cacheVersion = loadCacheVersion();
            String serverVersion = getVersion();

            log.info("cacheVersion : " + cacheVersion);
            log.info("serverVersion: " + serverVersion);

            if (cacheVersion == null || !cacheVersion.equals(serverVersion)) {
                updateCache(serverVersion);
                saveCacheVersion(serverVersion);
            } else {
                log.info("use cache");
            }
            log.debug("-- checkCache end");
        } catch (IOException ex) {
            log.error("checkCache failure");
            log.error(ex.getMessage(), ex);
        }
    }

    private void invokeLauncher(String[] args) throws Exception {
        String version = loadCacheVersion();
        File file = createPath(new String[]{CACHE_DIR,"monsiaj-" + version + "-all.jar"});
        if (!JarVerifier.verify(new JarFile(file))) {
            throw new Exception("jar verification error");
        }
        URLClassLoader loader = new URLClassLoader(new URL[]{new URL("file://"+file.getAbsolutePath())});
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
            removeCacheVersion();
            log.error(ex.getMessage(), ex);
            log.info("remove cacheVersion");
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
