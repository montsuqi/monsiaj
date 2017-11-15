/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author mihara
 */
public class PrinterConfig {

    private static final Logger logger = LogManager.getLogger(PrinterConfig.class);

    private static final TreeMap<String, PrintService> PRINT_SERVICE_MAP;
    private static final ArrayList<String> PRINTER_LIST;
    private final HashMap<String, PrinterConfigEntry> PRINTER_CONFIG_MAP;
    private final int MAX_COPIES = 99;

    static {
        PRINT_SERVICE_MAP = new TreeMap<>();
        PRINTER_LIST = new ArrayList<>();
        if (System.getProperty("monsia.debug.printer_list") != null) {
        } else {
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
            PrintService[] pss = PrintServiceLookup.lookupPrintServices(flavor, null);
            logger.debug("-----");
            for (PrintService ps : pss) {
                logger.debug(ps.getName() + "#:#" + ps);
                PRINT_SERVICE_MAP.put(ps.getName(), ps);
                PRINTER_LIST.add(ps.getName());
            }
            logger.debug("-----");
            Collections.sort(PRINTER_LIST, (Object o1, Object o2) -> {
                String n1 = (String) o1;
                String n2 = (String) o2;
                return n1.compareTo(n2);
            });
        }
    }

    public PrinterConfig() {
        PRINTER_CONFIG_MAP = new HashMap<>();
    }

    /* for PrinterConfigPanel */
    public void updatePrinterConfig(Map<String, String> map) {
        PRINTER_CONFIG_MAP.clear();
        String regex = "^(.*)#(\\d+)$";
        Pattern p = Pattern.compile(regex);

        map.entrySet().forEach((e) -> {
            PrintService ps = PrinterConfig.PRINT_SERVICE_MAP.get(e.getValue());
            Matcher m = p.matcher(e.getKey());
            if (m.find()) {
                String pr = m.group(1);
                int cp = Integer.parseInt(m.group(2));
                if (cp > MAX_COPIES) {
                    cp = MAX_COPIES;
                    logger.warn("max copies limit; printer " + pr + " set #" + cp);
                }
                PRINTER_CONFIG_MAP.put(pr, new PrinterConfigEntry(ps, cp));
            } else {
                PRINTER_CONFIG_MAP.put(e.getKey(), new PrinterConfigEntry(ps, 1));
            }
        });
    }

    public Map<String, String> getPrinterConfigMap() {
        Map<String, String> map = new TreeMap<>();
        PRINTER_CONFIG_MAP.entrySet().forEach((Map.Entry<String, PrinterConfigEntry> e) -> {
            String k = e.getKey();
            PrinterConfigEntry ent = e.getValue();
            int cp = ent.getCopies();
            if (cp > 1) {
                k = k + "#" + cp;
            }
            PrintService ps = ent.getPrintService();
            if (ps != null) {
                map.put(k, ps.getName());
            }
        });
        return map;
    }

    /* for Config */
    public void loadPrinterConfig(String str) {
        PRINTER_CONFIG_MAP.clear();

        String regex = "^(.*)#(\\d+)$";
        Pattern p = Pattern.compile(regex);

        Map<String, String> map = new TreeMap<>();
        String[] set = str.split(",");
        for (String kv : set) {
            String[] e = kv.split(":=:");
            if (e.length == 2) {
                map.put(e[0], e[1]);
            } else {
                logger.warn("invalid printer config! skip this. [" + kv + "]");
            }
        }
        updatePrinterConfig(map);
    }

    public String savePrinterConfig() {
        String str = "";
        int j = 0;
        for (Map.Entry<String, PrinterConfigEntry> e : PRINTER_CONFIG_MAP.entrySet()) {
            String key = e.getKey();
            PrinterConfigEntry ent = e.getValue();
            if (key == null || ent == null) {
                continue;
            }
            int cp = ent.getCopies();
            if (cp > 1) {
                key = key + "#" + cp;
            }
            String add = key + ":=:" + ent.getPrintService().getName();
            if (j == 0) {
                str = str + add;
            } else {
                str = str + "," + add;
            }
            j++;
        }
        return str;
    }

    public ArrayList<String> getPrinterList() {
        return PRINTER_LIST;
    }

    public PrintService getPrintService(String name) {
        PrinterConfigEntry entry = PRINTER_CONFIG_MAP.get(name);
        if (entry == null) {
            entry = PRINTER_CONFIG_MAP.get("default");
        }
        if (entry == null) {
            return null;
        }
        return entry.getPrintService();
    }

    public int getCopies(String name) {
        PrinterConfigEntry entry = PRINTER_CONFIG_MAP.get(name);
        if (entry == null) {
            return 1;
        }
        return entry.getCopies();
    }

    private class PrinterConfigEntry {

        private PrintService printService;
        private int copies;

        public PrinterConfigEntry(PrintService ps, int cp) {
            this.printService = ps;
            this.copies = cp;
        }

        public PrintService getPrintService() {
            return printService;
        }

        public void setPrintService(PrintService printService) {
            this.printService = printService;
        }

        public int getCopies() {
            return copies;
        }

        public void setCopies(int copies) {
            this.copies = copies;
        }
    }
}
