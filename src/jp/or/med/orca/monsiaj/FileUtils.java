/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.or.med.orca.monsiaj;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author mihara
 */
public class FileUtils {

    public static void deleteDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            for(File f: file.listFiles()) {
                deleteDirectory(f);
            }
            file.delete();
        } else {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
