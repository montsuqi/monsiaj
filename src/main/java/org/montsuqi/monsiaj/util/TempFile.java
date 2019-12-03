/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.montsuqi.monsiaj.util;

import java.io.File;
import java.util.UUID;

/**
 *
 * @author mihara
 */
public class TempFile {

    private final static long VALID_PERIOD = 24 * 3600 * 1000L; /* 1day millisec */
    public final static File TEMP_DIR_ROOT;
    public final static File TEMP_DIR;

    static {
        TEMP_DIR_ROOT = new File(new File(new File(System.getProperty("user.home")), ".monsiaj"), "tmp");
        TEMP_DIR = new File(TEMP_DIR_ROOT, UUID.randomUUID().toString());
        TEMP_DIR.mkdirs();
    }

    private TempFile() {
    }

    public static void deleteAll(File f) throws SecurityException {
        if (f.isDirectory()) {
            for (File g : f.listFiles()) {
                deleteAll(g);
            }
        }
        f.delete();
    }

    public static void cleanOld() {
        long now = System.currentTimeMillis();
        for (File f : TEMP_DIR_ROOT.listFiles()) {
            try {
                long elaps = now - f.lastModified();
                if (elaps > VALID_PERIOD) {
                    deleteAll(f);
                } else {
                }
            } catch (SecurityException e) {
            }
        }
    }

    public static File createTempFile(String prefix, String suffix) {
        final String filename = prefix + "_" + UUID.randomUUID().toString() + "_" + suffix;
        return new File(TEMP_DIR,filename);
    }

    public static void cleanTempDir() {
        if (TEMP_DIR.exists()) {
            deleteAll(TEMP_DIR);
        }
    }
}
