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
package org.montsuqi.monsiaj.monsia;

import java.util.HashMap;
import java.util.Map;

/**
 * <
 * p>
 * A value ofject that holds information about child widgets of a container
 * widget.</p>
 * <p>
 * Note that some Gtk+'s widgets have children even though their counterparts in
 * Swing are not conttainer widgets. ComboBox and Notebook are example of such
 * widget types.</p>
 */
public class ChildInfo {

    private WidgetInfo child;
    private Map<String,String> properties;

    ChildInfo() {
        this.child = null;
        this.properties = new HashMap<>();
    }

    public WidgetInfo getWidgetInfo() {
        return child;
    }

    void setWidgetInfo(WidgetInfo child) {
        this.child = child;
    }

    public Map getProperties() {
        return properties;
    }

    void setProperties(Map<String,String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }
}
