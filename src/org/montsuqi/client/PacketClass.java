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

public interface PacketClass {
	public static final byte Null = 0x00;
	public static final byte Connect = 0x01;
	public static final byte QueryScreen = 0x02;
	public static final byte GetScreen = 0x03;
	public static final byte GetData = 0x04;
	public static final byte Event = 0x05;
	public static final byte ScreenData = 0x06;
	public static final byte ScreenDefine = 0x07;
	public static final byte WindowName = 0x08;
	public static final byte FocusName = 0x09;
	public static final byte Auth = 0x0A;
	public static final byte Name = 0x0B;

	public static final byte OK = (byte)0x80;
	public static final byte END = (byte)0x81;
	public static final byte NOT = (byte)0x83;

	public static final byte E_VERSION = (byte)0xF1;
	public static final byte E_AUTH = (byte)0xF2;
	public static final byte E_APPL = (byte)0xF3;
}
