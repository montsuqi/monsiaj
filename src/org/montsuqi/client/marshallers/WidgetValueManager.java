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

import java.awt.Component;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.montsuqi.util.Logger;
import org.montsuqi.client.Protocol;
import org.montsuqi.monsia.Style;

public final class WidgetValueManager {

	protected static final Logger logger = Logger.getLogger(WidgetValueManager.class);
	private Protocol con;
	private Map styles;
	private Map valueTable;

	public WidgetValueManager(Protocol con, Map styles) {
		this.con = con;
		this.styles = styles;
		valueTable = new HashMap();
	}

	Protocol getProtocol() {
		return con;
	}

	void registerValue(Component widget, String valueName, Object opt) {
		String longName = con.getInterface().getWidgetLongName(widget);
		ValueAttribute va;
		if ( ! valueTable.containsKey(longName)) {
			String widgetName = con.getWidgetNameBuffer().toString();
			va = new ValueAttribute(longName, valueName, widgetName, con.getLastDataType(), opt);
			valueTable.put(va.getKey(), va);
		} else {
			va = (ValueAttribute)valueTable.get(longName);
			va.setNameSuffix(valueName);
		}
		int lastType = con.getLastDataType();
		va.setType(lastType);
		va.setOpt(opt);
	}

	ValueAttribute getValue(String name) {
		if (valueTable.containsKey(name)) {
			return (ValueAttribute)valueTable.get(name);
		}
		Object[] args = { name };
		throw new IllegalArgumentException(MessageFormat.format("no such value name: {0}", args)); //$NON-NLS-1$
	}

	void setStyle(Component widget, String styleName) {
		Style style = (Style)styles.get(styleName);
		if (style != null) {
			style.apply(widget);
		} else {
			Style.DEFAULT_STYLE.apply(widget);
		}
	}

}
