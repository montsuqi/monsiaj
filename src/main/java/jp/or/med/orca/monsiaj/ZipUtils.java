/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.or.med.orca.monsiaj;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 *
 * @author mihara
 */
public class ZipUtils {

    public static void unzip(File file, File dest) throws IOException, ZipException {
        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File entryDest = new File(dest, entry.getName());
            if (entry.isDirectory()) {
                entryDest.mkdir();
            } else {
                entryDest.getParentFile().mkdirs();
                BufferedInputStream bin = new BufferedInputStream(zipFile.getInputStream(entry));
                int length;
                BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(entryDest));
                while ((length = bin.read()) != -1) {
                    bout.write(length);
                }
                bout.close();
            }
        }
    }
}
