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

public class PrecisionScale {
	// The hardcoded limits and defaults of the numeric data type
	public static final int MAX_PRECISION = 1000;
	public static final int DEFAULT_PRECISION = 30;
	public static final int DEFAULT_SCALE = 6;

	public final int precision;
	public final int scale;

	public PrecisionScale() {
		precision = DEFAULT_PRECISION;
		scale =  DEFAULT_SCALE;
	}

	public PrecisionScale(String format) {
		if (format == null) {
			precision = DEFAULT_PRECISION;
			scale = DEFAULT_SCALE;
		} else {
			int pr = 0;
			int sc = 0;
			boolean fSmall = false;
			boolean fSign = false;
			char[] f = format.toCharArray();
			for (int i = 0; i < f.length; i++) {
				switch (f[i]) {
				case '-': case '+':
					if ( ! fSmall) {
						fSign = true;
					}
				case 'Z': case '\\': case '9':
					if (fSmall) {
						sc++;
					}
					pr++;
					break;
				case '.':
					fSmall = true;
					break;
				default:
					break;
				}
			}
			if (fSign) {
				pr--;
			}
			precision = pr;
			scale = sc;
		}
	}
}


