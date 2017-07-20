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
package org.montsuqi.monsiaj.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.util.Map;

import org.montsuqi.monsiaj.monsia.ChildInfo;
import org.montsuqi.monsiaj.monsia.Interface;
import org.montsuqi.monsiaj.monsia.WidgetInfo;
import org.montsuqi.monsiaj.util.ParameterConverter;
import org.montsuqi.monsiaj.widgets.Notebook;
import org.montsuqi.monsiaj.widgets.NotebookDummyButton;

/**
 * <p>A builder to create Notebook widget.</p> <p>For each tab, a
 * NotebookDummyButton is created and mapped to handle events.</p>
 */
class NotebookBuilder extends ContainerBuilder {

    void buildChildren(Interface xml, Container parent, WidgetInfo info) {
        // create tabs first
        Notebook notebook = (Notebook) parent;
        int cCount = info.getChildren().size();
        if (cCount % 2 != 0) {
            throw new WidgetBuildingException("odd number of notebook children"); 
        }
        int tabCount = cCount / 2;
        String[] labels = new String[tabCount];
        Component[] bodies = new Component[tabCount];
        boolean[] enabled = new boolean[tabCount];
        int currentLabel = 0;
        int currentBody = 0;
        for (int i = 0; i < cCount; i++) {
            ChildInfo cInfo = info.getChild(i);
            WidgetInfo wInfo = cInfo.getWidgetInfo();
            Map properties = wInfo.getProperties();
            if (properties.containsKey("child_name")) { 
                if (properties.containsKey("label")) { 
                    labels[currentLabel] = (String) properties.get("label"); 
                    Component dummy = new NotebookDummyButton(labels[currentLabel], currentLabel, notebook);
                    setCommonParameters(xml, dummy, wInfo);
                    setSignals(xml, dummy, wInfo);
                    enabled[currentLabel] = true;
                    if (properties.containsKey("sensitive")) { 
                        enabled[currentLabel] = ParameterConverter.toBoolean((String) properties.get("sensitive")); 
                    }
                    currentLabel++;
                } else {
                    throw new WidgetBuildingException("no label for a tab"); 
                }
            } else {
                Component body = buildWidget(xml, wInfo, parent);
                bodies[currentBody] = body;
                currentBody++;
            }
        }
        if (currentBody != bodies.length || currentLabel != labels.length) {
            throw new WidgetBuildingException("tab/label count mismatch"); 
        }
        for (int i = 0; i < tabCount; i++) {
            notebook.add(labels[i], bodies[i]);
            notebook.setEnabledAt(i, enabled[i]);
        }
    }
}
