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

package org.montsuqi.client.marshallers;

class ValueAttribute {
	private String key;
	private String nameSuffix;
	private String valueName;
	private int type;
	private Object opt;

	String getKey() {
		return key;
	}

	ValueAttribute(String key, String nameSuffix, String valueName, int type, Object opt) {
		this.key = key;
		this.nameSuffix = nameSuffix;
		this.valueName = valueName;
		this.type = type;
		this.opt = opt;
	}

	void setOpt(int type, Object opt) {
		this.type = type;
		this.opt = opt;
	}

	void setNameSuffix(String nameSuffix) {
		this.nameSuffix = nameSuffix;
	}

	String getNameSuffix() {
		return nameSuffix;
	}

	String getValueName() {
		return valueName;
	}

	int getType() {
		return type;
	}

	void setOpt(Object opt) {
		this.opt = opt;
	}

	Object getOpt() {
		return opt;
	}
}
