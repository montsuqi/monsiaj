/*      PANDA -- a simple transaction monitor

Copyright (C) 1998-1999 Ogochan.
              2000-2003 Ogochan & JMA (Japan Medical Association).

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

package org.montsuqi.client;

import java.lang.reflect.Field;

public final class Type {
	public static final int CLASS = 0xF0;
	public static final int NULL = 0x00;

	public static final int NUMERIC = 0x10;
	public static final int INT = 0x11;
	public static final int BOOL = 0x12;
	public static final int FLOAT = 0x13;
	public static final int NUMBER = 0x14;

	public static final int STRING = 0x20;
	public static final int CHAR = 0x21;
	public static final int TEXT = 0x22;
	public static final int VARCHAR = 0x23;
	public static final int BYTE = 0x24;
	public static final int DBCODE = 0x25;
	public static final int BINARY = 0x26;

	public static final int OBJECT = 0x40;
	public static final int STRUCTURE = 0x80;
	public static final int ARRAY = 0x81;
	public static final int RECORD = 0x82;
	public static final int ALIAS = 0x83;
	public static final int VALUES = 0x84;

	private Type() {
		// inhibit instantiation
	}

	public static String getName(int type) {
		Field[] fields = Type.class.getDeclaredFields();
		for (int i = 0, n = fields.length; i < n; i++) {
			if (fields[i].getType() != Integer.TYPE) {
				continue;
			}
			try {
				if (fields[i].getInt(null) == type) {
					return fields[i].getName();
				}
			} catch (Exception e) {
				throw new InternalError();
			}
		}
		throw new IllegalArgumentException("type not found: " + type); //$NON-NLS-1$
	}

}
