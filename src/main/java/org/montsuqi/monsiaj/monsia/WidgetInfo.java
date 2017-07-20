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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <
 * p>
 * A value ofject used to hold information of a widget in Glade interface
 * definition.</p>
 */
public class WidgetInfo {

    private String className;
    private String name;
    private WidgetInfo parent;
    private final LinkedHashMap<String, String> properties; // <Property>
    private final List<SignalInfo> signals; // <SignalInfo>
    private final List<AccelInfo> accels; // <Accel>
    private final LinkedList<ChildInfo> children; // <ChildInfo> public Object getChildren;

    WidgetInfo() {
        parent = null;
        properties = new LinkedHashMap<>();
        signals = new ArrayList<>();
        accels = new ArrayList<>();
        children = new LinkedList<>();
    }

    WidgetInfo(String className, String name) {
        this();
        this.className = className;
        this.name = name;
    }

    WidgetInfo getParent() {
        return parent;
    }

    void setParent(WidgetInfo parent) {
        this.parent = parent;
    }

    public String getLongName() {
        if (parent == null) {
            return getName();
        }
        return getParent().getLongName() + '.' + getName();
    }

    public void removeProperty(String key) {
        properties.remove(key);
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String,String> getProperties() {
        return properties;
    }

    public synchronized String getProperty(String key) {
        return (String) properties.get(key);
    }

    public synchronized void setProperties(Map<String,String> properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    protected void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public synchronized List getSignals() {
        return Collections.unmodifiableList(signals);
    }

    synchronized void setSignals(List<SignalInfo> signals) {
        this.signals.clear();
        this.signals.addAll(signals);
    }

    synchronized public List getAccels() {
        return accels;
    }

    synchronized void setAccels(List<AccelInfo> accels) {
        this.accels.clear();
        this.accels.addAll(accels);
    }

    void removeLastChild() {
        children.removeLast();
    }

    void addChild(ChildInfo info) {
        children.addLast(info);
    }

    public ChildInfo getChild(int i) {
        return (ChildInfo) children.get(i);
    }

    ChildInfo getLastChild() {
        return (ChildInfo) children.getLast();
    }

    public List getChildren() {
        return children;
    }

    public void addSignalInfo(SignalInfo signalInfo) {
        signals.add(signalInfo);
    }

    protected void addAccelInfo(AccelInfo accelInfo) {
        accels.add(accelInfo);
    }
}
