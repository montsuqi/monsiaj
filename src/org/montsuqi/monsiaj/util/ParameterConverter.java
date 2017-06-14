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

/**
 * <
 * p>
 * An utility class for converting string to other types.</p>
 */
public class ParameterConverter {

    private ParameterConverter() {
        // inhibit instantiation
    }

    /**
     * <
     * p>
     * Converts the given string into integer.</p>
     *
     * @param s the string to convert.
     * @return
     */
    public static int toInteger(String s) {
        return Integer.parseInt(s);
    }

    /**
     * <
     * p>
     * Converts the given string into boolean.</p>
     *
     * @param s the string to convert.
     * @return true for strings which starts with 't', 'y', 'T' or 'Y'. false
     * for strings which starts with 'f', 'n', 'F' or 'N'. Otherwise the string
     * is converted to integer and returns true if it is not zero, false if it
     * is zero.
     */
    public static boolean toBoolean(String s) {
        if ("tyTY".indexOf(s.charAt(0)) >= 0) {
            return true;
        } else if ("fnFN".indexOf(s.charAt(0)) >= 0) {
            return false;
        } else {
            return toInteger(s) != 0;
        }
    }

    /**
     * <
     * p>
     * Converts the given string into double.</p>
     *
     * @param s the string to convert.
     * @return
     */
    public double toDouble(String s) {
        return Double.parseDouble(s);
    }
}
