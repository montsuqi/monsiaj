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

    public final static File tempDirRoot;
    public final static File tempDir;

    static {
        tempDirRoot = new File(new File(new File(System.getProperty("user.home")), ".monsiaj"), "tmp");
        tempDir = new File(tempDirRoot, UUID.randomUUID().toString());
        tempDir.mkdirs();
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
        for (File f : tempDirRoot.listFiles()) {
            try {
                long elaps = System.currentTimeMillis() - f.lastModified();
                if (elaps > 86400000) { /* 1day */
                    deleteAll(f);
                } else {
                }
            } catch (SecurityException e) {
            }
        }
    }
    
    public static File createTempFile(String prefix, String suffix) {
        final String filename = prefix + "_" + UUID.randomUUID().toString() + "_" + suffix;
        return new File(tempDir,filename);
    }
    
    public static void cleanTempDir() {
        if (tempDir.exists()) {
            deleteAll(tempDir);
        }
    }
}
