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
package org.montsuqi.client.marshallers;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.montsuqi.client.Protocol;
import org.montsuqi.monsia.Style;

public final class WidgetValueManager {

    private Protocol con;
    private Map styles;
    private Map attrTable;
    private Map valueTable;

    public WidgetValueManager(Protocol con, Map styles) {
        this.con = con;
        this.styles = styles;
        attrTable = new HashMap();
        valueTable = new HashMap();
    }

    Protocol getProtocol() {
        return con;
    }
    
    void registerValue(Component widget,String name,Object obj) {
        String key = widget.getName() + "." + name;
        valueTable.put(key,obj);
    }
    
    Object getValue(Component widget,String name) {
        String key = widget.getName() + "." + name;
        return valueTable.get(key);
    }

    void registerAttribute(Component widget, String valueName, Object opt) {
        String longName = widget.getName();
        ValueAttribute va;
        if (!attrTable.containsKey(longName)) {
            String widgetName = con.getWidgetNameBuffer().toString();
            va = new ValueAttribute(longName, valueName, widgetName, con.getLastDataType(), opt);
            attrTable.put(va.getKey(), va);
        } else {
            va = (ValueAttribute) attrTable.get(longName);
            va.setNameSuffix(valueName);
        }
        int lastType = con.getLastDataType();
        va.setType(lastType);
        va.setOpt(opt);
    }

    Object getAttributeOpt(String name) {
        if (attrTable.containsKey(name)) {
            ValueAttribute va = (ValueAttribute) attrTable.get(name);
            return va.getOpt();
        }
        Object[] args = {name};
        throw new IllegalArgumentException(MessageFormat.format("no such value name: {0}", args));         
    }

    ValueAttribute getAttribute(String name) {
        if (attrTable.containsKey(name)) {
            return (ValueAttribute) attrTable.get(name);
        }
        Object[] args = {name};
        throw new IllegalArgumentException(MessageFormat.format("no such value name: {0}", args)); 
    }

    void setStyle(Component widget, String styleName) {
        final Style style;
        if (styles.containsKey(styleName)) {
            style = (Style) styles.get(styleName);
        } else {
            style = Style.DEFAULT_STYLE;
        }
        style.apply(widget);
    }
}
