/*      PANDA -- a simple transaction monitor

 Copyright (C) 1998-1999 Ogochan.
 2000-2003 Ogochan & JMA (Japan Medical Association).
 2002-2006 OZAWA Sakuro.

 This module is part of PANDA.

 PANDA is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
 to anyone for the consequences of using it or for whether it serves
 any particular purpose or works at all, unless he says so in writing.
 Refer to the GNU General Public License for full details.

 Everyone is granted permission to copy, modify and redistribute
 PANDA, but only under the conditions described in the GNU General
 Public License.  A copy of this license is supposed to have been given
 to you along with PANDA so you can know your rights and
 responsibilities.  It should be in a file named COPYING.  Among other
 things, the copyright notice and this notice must be preserved on all
 copies.
 */
package org.montsuqi.monsiaj.util;

import java.io.File;
import java.util.Locale;

/**
 * <
 * p>
 * A class that represents the system(platform) environment.</p>
 */
public class SystemEnvironment {

    private SystemEnvironment() {
        // inhibit instantiation
    }

    /**
     * <
     * p>
     * Tests if the system is MacOS X.</p>
     *
     * @return true if the running system is MacOS X. false otherwise.
     */
    public static boolean isMacOSX() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os x");  //$NON-NLS-2$
    }
    
    public static boolean isJavaVersionMatch(String version) {
        return System.getProperty("java.version").startsWith(version);
    }

    /**
     * <
     * p>
     * Tests if the system is Windows.</p>
     *
     * @return true if the running system is Windows. false otherwise.
     */
    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("windows");
    }

    private final static boolean isMS932;

    static {
        if (Locale.getDefault().getLanguage().equals("ja")) {
            isMS932 = isWindows();
        } else {
            isMS932 = false;
        }
    }

    /**
     * <
     * p>
     * Tests if the system uses MS932 characters.</p>
     *
     * @return true if the running system uses MS932 characters. false
     * otherwise.
     */
    public static boolean isMS932() {
        return isMS932;
    }

    /**
     * <
     * p>
     * On MacOS X, sets the menu title.</p>
     * <p>
     * On other platforms, does nothing.</p>
     *
     * @param title Title to set.
     */
    public static void setMacMenuTitle(String title) {
        if (title != null && isMacOSX()) {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", title);
        }
    }

    /**
     * <
     * p>
     * Creates a file instance with the given path elements in platform
     * independent way.</p>
     *
     * @param elements path elements.
     * @return the File instance constructed using the given path elements.
     */
    public static File createFilePath(String[] elements) {
        File path = new File(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            path = new File(path, elements[i]);
        }
        return path;
    }
}
