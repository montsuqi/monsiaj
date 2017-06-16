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

class Option {

	private String name;
	private String message;
	private Object value;

	Option(String name, String message, Object value) {
		this.name = name;
		this.message = message;
		this.value = value;
	}

	Option(String name, String message, boolean defaultValue) {
		this(name, message, Boolean.valueOf(defaultValue));
	}

	Option(String name, String message, int defaultValue) {
		this(name, message, Integer.valueOf(defaultValue));
	}

	Option(String name, String message, String defaultValue) {
		this(name, message, (Object)defaultValue);
	}

	String getName() {
		return name;
	}

	String getMessage() {
		return message;
	}

	Object getValue() {
		return value;
	}

	Class getType() {
		return value.getClass();
	}

	public String toString() {
		Class type = value.getClass();
		if (type == Boolean.class) {
			if (((Boolean)value).booleanValue()) {
				return "ON"; 
			}
			return "OFF"; 
		}
		return value.toString();
	}

	void setValue(String arg) {
		arg = arg.trim();
		Class type = value.getClass();
		if (type == Boolean.class) {
			if (arg.length() > 0) {
				if (arg.charAt(0) == '+') {
					setValue(false);
				} else {
					if (arg.charAt(0) == '-') {
						setValue(true);
					} else {
						setValue(!((Boolean)value).booleanValue()); // toggle
					}
				}
			} else {
				setValue(!((Boolean)value).booleanValue()); // toggle
			}
		} else if (type == Integer.class) {
			setValue(Integer.parseInt(arg));
		} else if (type == String.class) {
			if (arg.length() != 0) {
				value = arg;
			} else {
				value = null;
			}
		}
	}

	void setValue(boolean arg) {
		if (value.getClass() != Boolean.class) {
			throw new IllegalArgumentException("option type mismatch"); 
		}
		value = Boolean.valueOf(arg);

	}

	void setValue(int value) {
		if (this.value.getClass() != Integer.class) {
			throw new IllegalArgumentException("option type mismatch"); 
		}
		this.value = new Integer(value);
	}
}

