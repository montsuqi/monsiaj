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
	final int NULL = 0x00;
	final int INT = 0x10;
	final int BOOL = 0x11;
	final int FLOAT = 0x20;
	final int CHAR = 0x30;
	final int TEXT = 0x31;
	final int VARCHAR = 0x32;
	final int BYTE = 0x40;
	final int NUMBER = 0x50;
	final int DBCODE = 0x60;
	final int OBJECT = 0x61;
	final int STRUCTURE = 0x80;
	final int ARRAY = 0x90;
	final int RECORD = 0xA0;
}
