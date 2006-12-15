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

package org.montsuqi.util;

/** <p>An utility class to reverse the byte order of numbers.</p> */
public class ByteOrder {

	private ByteOrder() {
		// inhibit instantiation
	}

	/** <p>Reverse the byte order of given long integer.</p>
	 *
	 * @param l a long integer to revese its byte order.
	 */
	public static long reverse(long l) {
		return
			(((l >> 56) & 0xff) <<  0) |
			(((l >> 48) & 0xff) <<  8) |
			(((l >> 40) & 0xff) << 16) |
			(((l >> 32) & 0xff) << 24) |
			(((l >> 24) & 0xff) << 32) |
			(((l >> 16) & 0xff) << 40) |
			(((l >>  8) & 0xff) << 48) |
			(((l >>  0) & 0xff) << 56);
	}

	/** <p>Reverse the byte order of given int.</p>
	 *
	 * @param i an int to revese its byte order.
	 */
	public static int reverse(int i) {
		return
			(((i >> 24) & 0xff) <<  0) |
			(((i >> 16) & 0xff) <<  8) |
			(((i >>  8) & 0xff) << 16) |
			(((i >>  0) & 0xff) << 24);
	}

	/** <p>Reverse the byte order of given short integer.</p>
	 *
	 * @param s a short integer to revese its byte order.
	 */
	public static short reverse(short s) {
		return (short)
			((((s >> 8) & 0xff) << 0) |
			(((s >> 0) & 0xff) << 8));
	}

	/** <p>Reverse the byte order of given char.</p>
	 *
	 * @param c a char to revese its byte order.
	 */
	public static char reverse(char c) {
		return (char)
			((((c >> 8) & 0xff) << 0) |
			(((c >> 0) & 0xff) << 8));
	}

	/** <p>Reverse the byte order of given double.</p>
	 *
	 * @param d a double to revese its byte order.
	 */
	public static double reverse(double d) {
		return Double.longBitsToDouble(reverse(Double.doubleToLongBits(d)));
	}

	/** <p>Reverse the byte order of given float.</p>
	 *
	 * @param f a float to revese its byte order.
	 */
	public static float reverse(float f) {
		return Float.intBitsToFloat(reverse(Float.floatToIntBits(f)));
	}
}
