/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.or.med.orca.monsiaj;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.prefs.Preferences;
import org.montsuqi.util.FileUtils;
import org.montsuqi.util.HttpClient;
import org.montsuqi.util.SystemEnvironment;
import org.montsuqi.util.ZipUtils;

/**
 *
 * @author mihara
 */
public class Loader {

    private final String VERSION_URL = "http://ftp.orca.med.or.jp/pub/java-client/version.txt";
    private final String DOWNLOAD_URL = "http://ftp.orca.med.or.jp/pub/java-client/";
    private Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private final String[] CACHE_DIR_PATH_ELEM = {System.getProperty("user.home"), ".monsiaj"};
    private final String CACHE_DIR = SystemEnvironment.createFilePath(CACHE_DIR_PATH_ELEM).getAbsolutePath();

    private void updateCache(String version) throws IOException {
        System.out.println("updateCache");
        /*
         * キャッシュディレクトリの削除と作成
         */
        File cacheDir = new File(CACHE_DIR);
        FileUtils.deleteDirectory(cacheDir);
        if (!cacheDir.mkdir()) {
            throw new IOException("cant make cachedir");
        }

        /*
         * zipファイルのダウンロード
         */
        String url = DOWNLOAD_URL + "monsiaj-bin-" + version + ".zip";
        InputStream is = HttpClient.get(url, null);
        File tmp = File.createTempFile("monsiaj-bin-" + version + "-", ".zip");
        tmp.deleteOnExit();
        BufferedInputStream in = new BufferedInputStream(is);
        int length;
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
        while ((length = in.read()) != -1) {
            out.write(length);
        }
        out.close();
        ZipUtils.unzip(tmp, cacheDir);
        tmp.delete();
        System.out.println("updateCache succeed");
    }

    private String getVersion() throws IOException {
        InputStream is = HttpClient.get(VERSION_URL, null);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader.readLine();
    }

    private void checkCache() {
        try {
            String cacheVersion = prefs.get("cacheVersion", null);
            String serverVersion = getVersion();

            System.out.println("cacheVersion:" + cacheVersion);
            System.out.println("serverVersion:" + serverVersion);

            if (cacheVersion == null || !cacheVersion.equals(serverVersion)) {
                updateCache(serverVersion);
                prefs.put("cacheVersion", serverVersion);
            } else {
                System.out.println("use cache");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("checkCache failure");
        }
    }

    private void loadCache(File file) throws Exception {
        URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                loadCache(f);
            }
        } else {
            if (file.getName().endsWith(".jar")) {
                if (JarVerifier.verify(new JarFile(file))) {
                    URL u = file.toURI().toURL();
                    Method m = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
                    m.setAccessible(true);
                    m.invoke(loader, new Object[]{u});
                } else {
                    System.out.println("invalid jar(code sign verification error) : " + file.getName());
                }
            }
        }
    }

    private void invokeLauncher(String[] args) throws Exception {
        String cacheVersion = prefs.get("cacheVersion", null);
        File file = new File(CACHE_DIR + "monsiaj-bin-" + cacheVersion + "/jmareceipt.jar");
        URLClassLoader loader = new URLClassLoader(new URL[]{file.toURL()});
        Class cobj = loader.loadClass("jp.or.med.orca.jmareceipt.JMAReceiptLauncher");
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
            loadCache(new File(CACHE_DIR));
            invokeLauncher(args);
        } catch (Exception ex) {
            prefs.remove("cacheVersion");
            throw ex;
        }
    }

    public static void main(String[] args) throws Exception {
        Loader loader = new Loader();
        loader.checkCache();
        loader.launch(args);
    }
}
