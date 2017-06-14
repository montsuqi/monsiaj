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
package org.montsuqi.monsiaj.widgets;

import javax.swing.JOptionPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <
 * p>
 * A Dialog to display information about an exception briefly.</p>
 */
public class ExceptionDialog extends JOptionPane {

    static final Logger logger = LogManager.getLogger(ExceptionDialog.class);

    // inhibit instantiation
    private ExceptionDialog() {
        // do nothing
    }

    /**
     * <
     * p>
     * Shows up a dialog that explains given exception.</p>
     *
     * <p>
     * If the exception is chained(nested), the root exception is looked
     * through.</p>
     *
     * @param e an exception to explain.
     */
    public static void showExceptionDialog(Throwable e) {
        logger.error(e,e);
        while (true) {
            Throwable cause = e.getCause();
            if (cause == null) {
                break;
            }
            e = cause;
        }
        Class clazz = e.getClass();
        String name = clazz.getName();
        String shortName = name.substring(name.lastIndexOf('.') + 1);
        String message = e.getMessage();
        if (message == null || message.length() == 0) {
            message = name;
        }
        JOptionPane.showMessageDialog(null, message, shortName, JOptionPane.ERROR_MESSAGE);
    }
}
