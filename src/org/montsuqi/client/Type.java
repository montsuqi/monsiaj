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

public interface Type {
	final int CLASS = 0xF0;
	final int NULL = 0x00;

	final int NUMERIC = 0x10;
	final int INT = 0x11;
	final int BOOL = 0x12;
	final int FLOAT = 0x13;
	final int NUMBER = 0x14;

	final int STRING = 0x20;
	final int CHAR = 0x21;
	final int TEXT = 0x22;
	final int VARCHAR = 0x23;
	final int BYTE = 0x24;
	final int DBCODE = 0x25;
	final int BINARY = 0x26;

	final int OBJECT = 0x40;
	final int STRUCTURE = 0x80;
	final int ARRAY = 0x81;
	final int RECORD = 0x82;
	final int ALIAS = 0x83;
	final int VALUES = 0x84;
}
