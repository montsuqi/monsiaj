/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.client;

import org.montsuqi.monsiaj.util.Messages;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.montsuqi.monsiaj.util.SystemEnvironment;

/**
 *
 * @author mihara
 */
public class Config {

    private String propPath;
    private Properties prop;
    private int current;

    private static final String PROP_FILENAME = "monsiaj.jsonrpc.properties";
    private static final String JARPATH = System.getProperty("java.class.path");
    private static final String DIRPATH = JARPATH.substring(0, JARPATH.lastIndexOf(File.separator) + 1);
    private static final String DIRECT_PROP_PATH = DIRPATH + PROP_FILENAME;

    private static final String[] PROP_PATH_ELEM = {System.getProperty("user.home"), ".monsiaj", PROP_FILENAME};
    private static final String PROP_PATH = SystemEnvironment.createFilePath(PROP_PATH_ELEM).getAbsolutePath();
    private static final String CONFIG_KEY = "monsiaj.config";
    private static final String CURRENT_KEY = "monsiaj.current";
    private static final String DEFAULT_STYLE_RESOURCE_NAME = "/style.properties";
    private static final Logger LOGGER = LogManager.getLogger(Config.class);

    private static final TreeMap<String, PrintService> PRINTER_SERVICE_MAP;
    private static final ArrayList<String> PRINTER_LIST;
    private final HashMap<String, PrintService> PRINTER_CONFIG_MAP;

    static {
        PRINTER_SERVICE_MAP = new TreeMap<>();
        PRINTER_LIST = new ArrayList<>();
        if (System.getProperty("monsia.debug.printer_list") != null) {
        } else {
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
            PrintService[] pss = PrintServiceLookup.lookupPrintServices(flavor, null);
            LOGGER.debug("-----");             
            for (PrintService ps : pss) {
                LOGGER.debug(ps.getName() + "#:#" + ps);                
                PRINTER_SERVICE_MAP.put(ps.getName(), ps);
                PRINTER_LIST.add(ps.getName());
            }
            LOGGER.debug("-----");                         
            Collections.sort(PRINTER_LIST, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    String n1 = (String) o1;
                    String n2 = (String) o2;
                    return n1.compareTo(n2);
                }
            });
        }
    }

    public Config() {
        PRINTER_CONFIG_MAP = new HashMap<>();
        initProp();
        readProp();
    }

    public ArrayList<String> getPrinterList() {
        return PRINTER_LIST;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
        this.save();
    }

    public void setCurrentByDescription(String desc) {
        for (int i : this.getList()) {
            if (desc.equals(this.getDescription(i))) {
                this.current = i;
                return;
            }
        }
    }

    public int getConfigByDescription(String desc) {
        for (int i : this.getList()) {
            if (desc.equals(this.getDescription(i))) {
                return i;
            }
        }
        return 0;
    }

    public int getNext() {
        int max = 0;
        ArrayList<Integer> list = new ArrayList<>();
        Pattern p = Pattern.compile(Config.CONFIG_KEY + "\\.(\\d+)\\.");
        for (Enumeration e = prop.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            if (k.startsWith(Config.CONFIG_KEY)) {
                Matcher m = p.matcher(k);
                if (!m.find()) {
                    continue;
                }
                int i = Integer.valueOf(m.group(1));
                max = i > max ? i : max;
                if (!list.contains(i)) {
                    list.add(i);
                }
            }
        }
        return max + 1;
    }

    private void initProp() {
        propPath = null;
        current = 0;
        prop = new Properties();
        try {
            prop.load(new FileInputStream(DIRECT_PROP_PATH));
            if (prop.size() > 0) {
                propPath = DIRECT_PROP_PATH;
            }
        } catch (IOException ex) {
            // do nothing
        }

        if (propPath == null) {
            try {
                prop.load(new FileInputStream(PROP_PATH));
            } catch (IOException ex) {
                // initial
            }
            propPath = PROP_PATH;
        }
    }

    private void readProp() {
        current = Integer.valueOf(prop.getProperty(Config.CURRENT_KEY, "0"));
        List<Integer> list = this.getList();
        if (list.isEmpty()) {
            setValue(0, "description", "default");
            list.add(0);
        }
    }

    public void save() {
        Properties tmp = new Properties() {
            @Override
            public Set<Object> keySet() {
                return Collections.unmodifiableSet(new TreeSet<>(super.keySet()));
            }

            @Override
            public synchronized Enumeration<Object> keys() {
                return Collections.enumeration(new TreeSet<>(super.keySet()));
            }
        };
        prop.setProperty(Config.CURRENT_KEY, Integer.toString(current));
        tmp.putAll(prop);
        try {
            tmp.store(new FileOutputStream(propPath), "monsiaj setting");
        } catch (IOException ex) {
            LOGGER.catching(ex);
        }
    }

    public int copyConfig(int num) {
        int ret = this.getNext();
        Pattern p = Pattern.compile(Config.CONFIG_KEY + "\\." + num + "\\.(.*)");
        for (Enumeration e = prop.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            if (k.startsWith(Config.CONFIG_KEY)) {
                Matcher m = p.matcher(k);
                if (!m.find()) {
                    continue;
                }
                String key = m.group(1);
                String k2 = Config.CONFIG_KEY + "." + ret + "." + key;
                String value = prop.getProperty(k);
                if (key.equals("description")) {
                    value = value + "_" + ret;
                }
                prop.setProperty(k2, value);
            }
        }
        return ret;
    }

    public ArrayList<Integer> getList() {
        ArrayList<Integer> list = new ArrayList<>();
        Pattern p = Pattern.compile(Config.CONFIG_KEY + "\\.(\\d+)\\.");
        for (Enumeration e = prop.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            if (k.startsWith(Config.CONFIG_KEY)) {
                Matcher m = p.matcher(k);
                if (!m.find()) {
                    continue;
                }
                int i = Integer.valueOf(m.group(1));
                if (!list.contains(i)) {
                    list.add(i);
                }
            }
        }
        Collections.sort(list, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                String n1 = Config.this.getDescription((Integer) o1);
                String n2 = Config.this.getDescription((Integer) o2);
                return n1.compareTo(n2);
            }
        });
        return list;
    }

    private void setValue(int i, String key, String value) {
        prop.setProperty(Config.CONFIG_KEY + "." + i + "." + key, value);
    }

    private String getValue(int i, String key) {
        return prop.getProperty(Config.CONFIG_KEY + "." + i + "." + key, "");
    }

    // desc
    public String getDescription(int i) {
        String value = getValue(i, "description");
        if (value.isEmpty()) {
            return "new";
        }
        return value;
    }

    public void setDescription(int i, String v) {
        setValue(i, "description", v);
    }

    // authuri
    public String getAuthURI(int i) {
        String value = getValue(i, "authuri");
        if (value.isEmpty()) {
            return "http://localhost:8000/rpc/";
        } else {
            if (!value.endsWith("/")) {
                value += "/";
            }
        }
        return value;
    }

    public void setAuthURI(int i, String v) {
        setValue(i, "authuri", v);
    }

    // user
    public String getUser(int i) {
        String value = getValue(i, "user");
        return value;
    }

    public void setUser(int i, String v) {
        setValue(i, "user", v);
    }

    // password
    public String getPassword(int i) {
        String value = getValue(i, "password");
        return value;
    }

    public void setPassword(int i, String v) {
        setValue(i, "password", v);
    }

    // savePassword
    public boolean getSavePassword(int i) {
        String value = getValue(i, "savePassword");
        if (value.isEmpty()) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    public void setSavePassword(int i, boolean v) {
        setValue(i, "savePassword", Boolean.toString(v));
        if (!v) {
            setPassword(i, "");
        }
    }

    // useSSL
    public boolean getUseSSL(int i) {
        String value = getValue(i, "useSSL");
        if (value.isEmpty()) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    public void setUseSSL(int i, boolean v) {
        setValue(i, "useSSL", Boolean.toString(v));
    }

    // clientCertificateFile
    public String getCACertificateFile(int i) {
        String value = getValue(i, "caCertificateFile");
        return value;
    }

    public void setCACertificateFile(int i, String v) {
        setValue(i, "caCertificateFile", v);
    }

    // clientCertificateFile
    public String getClientCertificateFile(int i) {
        String value = getValue(i, "clientCertificateFile");
        return value;
    }

    public void setClientCertificateFile(int i, String v) {
        setValue(i, "clientCertificateFile", v);
    }

    // clientCertificatePassword
    public String getClientCertificatePassword(int i) {
        String value = getValue(i, "clientCertificatePassword");
        return value;
    }

    public void setClientCertificatePassword(int i, String v) {
        setValue(i, "clientCertificatePassword", v);
    }

    // saveClientCertificatePassword
    public boolean getSaveClientCertificatePassword(int i) {
        String value = getValue(i, "saveClientCertificatePassword");
        if (value.isEmpty()) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    public void setSaveClientCertificatePassword(int i, boolean v) {
        setValue(i, "saveClientCertificatePassword", Boolean.toString(v));
        if (!v) {
            this.setClientCertificatePassword(i, "");
        }
    }

    public boolean getUsePKCS11(int i) {
        String value = getValue(i, "usePKCS11");
        if (value.isEmpty()) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    public void setUsePKCS11(int i, boolean v) {
        setValue(i, "usePKCS11", Boolean.toString(v));
    }

    public String getPKCS11Lib(int i) {
        String value = getValue(i, "pkcs11Lib");
        return value;
    }

    public void setPKCS11Lib(int i, String v) {
        setValue(i, "pkcs11Lib", v);
    }

    public String getPKCS11Slot(int i) {
        String value = getValue(i, "pkcs11Slot");
        return value;
    }

    public void setPKCS11Slot(int i, String v) {
        setValue(i, "pkcs11Slot", v);
    }

    public boolean getSavePIN(int i) {
        String value = getValue(i, "savePIN");
        if (value.isEmpty()) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    public void setSavePIN(int i, boolean v) {
        setValue(i, "savePIN", Boolean.toString(v));
    }

    public String getPIN(int i) {
        String value = getValue(i, "pin");
        return value;
    }

    public void setPIN(int i, String v) {
        setValue(i, "pin", v);
    }

    // styleFile
    public String getStyleFile(int i) {
        String value = getValue(i, "styleFile");
        return value;
    }

    public void setStyleFile(int i, String v) {
        setValue(i, "styleFile", v);
    }

    public URL getStyleURL(int i) {
        String value = getStyleFile(i);
        if (!value.isEmpty()) {
            File file = new File(value);
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                //logger.warn(e);
            }
        }
        return Config.class.getResource(DEFAULT_STYLE_RESOURCE_NAME);
    }

    // LookAndFeel
    public String getLookAndFeel(int i) {
        String value = getValue(i, "lookAndFeel");
        return value;
    }

    public void setLookAndFeel(int i, String v) {
        setValue(i, "lookAndFeel", v);
    }

    // LookAndFeelThemeFile
    public String getLookAndFeelThemeFile(int i) {
        String value = getValue(i, "lookAndFeelThemeFile");
        return value;
    }

    public void setLookAndFeelThemeFile(int i, String v) {
        setValue(i, "lookAndFeelThemeFile", v);
    }

    // useTimer
    public boolean getUseTimer(int i) {
        String value = getValue(i, "useTimer");
        if (value.isEmpty()) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    public void setUseTimer(int i, boolean v) {
        setValue(i, "useTimer", Boolean.toString(v));
    }

    // timerPeriod
    public int getTimerPeriod(int i) {
        String value = getValue(i, "timerPeriod");
        if (value.isEmpty()) {
            return 8000;
        }
        return Integer.valueOf(value);
    }

    public void setTimerPeriod(int i, int p) {
        setValue(i, "timerPeriod", Integer.toString(p));
    }

    // LookAndFeelThemeFile
    public String getSystemProperties(int i) {
        String value = getValue(i, "systemProperties");
        return value;
    }

    public void setSystemProperties(int i, String v) {
        setValue(i, "systemProperties", v);
    }

    public void LoadPrinterConfig(int i) {
        PRINTER_CONFIG_MAP.clear();
        String confStr = getValue(i, "printerConfig");
        String[] set = confStr.split(",");
        for (String kv : set) {
            String[] e = kv.split(":=:");
            if (e.length == 2) {
                PRINTER_CONFIG_MAP.put(e[0], PRINTER_SERVICE_MAP.get(e[1]));
            } else {
                LOGGER.warn("invalid printer config! skip this. [" + kv + "]");
            }
        }
    }

    public Map<String, String> getPrinterConfig(int i) {
        TreeMap<String, String> map = new TreeMap<>();
        String confStr = getValue(i, "printerConfig");
        String[] set = confStr.split(",");
        for (String kv : set) {
            String[] e = kv.split(":=:");
            if (e.length == 2) {
                map.put(e[0], e[1]);
            } else {
                LOGGER.warn("invalid printer config! skip this. [" + kv + "]");
            }
        }
        return map;
    }

    public void setPrinterConfig(int i, Map<String, String> map) {
        PRINTER_CONFIG_MAP.clear();
        String str = "";
        int j = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            String key = e.getKey();
            String val = e.getValue();
            if (key == null || val == null) {
                continue;
            }
            if (j == 0) {
                str = str + e.getKey() + ":=:" + e.getValue();
            } else {
                str = str + "," + e.getKey() + ":=:" + e.getValue();
            }
            j++;
            PRINTER_CONFIG_MAP.put(e.getKey(), PRINTER_SERVICE_MAP.get(e.getValue()));
        }
        setValue(i, "printerConfig", str);
    }

    public PrintService getPrintService(String printer) {
        PrintService ps = PRINTER_CONFIG_MAP.get(printer);
        if (ps == null) {
            ps = PRINTER_CONFIG_MAP.get("default");
        } 
        return ps;
    }

    public void list() {
        System.out.println("----");
        for (Enumeration e = prop.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            System.out.println(k + " : " + prop.getProperty(k));
        }
    }

    public void listConfig() {
        System.out.println(Messages.getString("Configuration.list_title"));
        System.out.println("------------------");
        for (int i : this.getList()) {
            System.out.println(this.getDescription(i));
            System.out.println(Messages.getString("Configuration.list_authURI") + getAuthURI(i));
            System.out.println(Messages.getString("Configuration.list_user") + getUser(i));
        }
    }

    public void applySystemProperties(int i) {
        this.updateSystemProperties(this.getSystemProperties(i));
    }

    private void updateSystemProperties(String properties) {
        StringReader sr = new StringReader(properties);
        BufferedReader br = new BufferedReader(sr);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String[] pair = line.split("\\s*=\\s*");
                if (pair.length == 2) {
                    String key = pair[0].trim();
                    String value = pair[1].trim();
                    System.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            //logger.warn(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    public void deleteConfig(int i) {
        String keyPrefix = Config.CONFIG_KEY + "." + i + ".";
        for (Enumeration e = prop.keys(); e.hasMoreElements();) {
            String k = (String) e.nextElement();
            if (k.startsWith(keyPrefix)) {
                prop.remove(k);
            }
        }
        save();
    }

    static public void main(String[] argv) {
        Config conf = new Config();
        conf.list();
        conf.save();
    }
}
