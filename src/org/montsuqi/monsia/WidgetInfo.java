package org.montsuqi.monsia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class WidgetInfo {

	WidgetInfo() {
		parent = null;
		properties = new ArrayList();
		atkProperties = new ArrayList();
		signals = new ArrayList();
		actions = new ArrayList();
		relations = new ArrayList();
		accels = new ArrayList();
		children = new LinkedList();
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

	String getLongName() {
		if (parent == null) {
			return getName();
		} else {
			return getParent().getLongName() + '.' + getName();
		}
	}

	String getClassName() {
		return className;
	}

	String getName() {
		return name;
	}

	void setClassName(String className) {
		this.className = className;
	}

	void setName(String name) {
		this.name = name;
	}

	int getPropertiesCount() {
		return properties.size();
	}

	List getProperties() {
		return Collections.unmodifiableList(properties);
	}

	Property getProperty(int i) {
		return (Property)properties.get(i);
	}

	synchronized void setProperties(List properties) {
		this.properties.clear();
		this.properties.addAll(properties);
	}

	int getATKPropertiesCount() {
		return atkProperties.size();
	}

	synchronized void setATKProperties(List properties) {
		this.atkProperties.clear();
		this.atkProperties.addAll(properties);
	}

	List getSignals() {
		return Collections.unmodifiableList(signals);
	}

	synchronized void setSignals(List signals) {
		this.signals.clear();
		this.signals.addAll(signals);
	}

	synchronized void setATKActions(List actions) {
		this.actions.clear();
		this.actions.addAll(actions);
	}

	synchronized void setRelations(List relations) {
		this.relations.clear();
		this.relations.addAll(relations);
	}

	int getAccelsCount() {
		return accels.size();
	}

	List getAccels() {
		return Collections.unmodifiableList(accels);
	}

	synchronized void setAccels(List accels) {
		this.accels.clear();
		this.accels.addAll(accels);
	}

	int getChildrenCount() {
		return children.size();
	}
	
	void removeLastChild() {
		children.removeLast();
	}

	void addChild(ChildInfo info) {
		children.addLast(info);
	}

	ChildInfo getChild(int i) {
		return (ChildInfo)children.get(i);
	}

	ChildInfo getLastChild() {
		return (ChildInfo)children.getLast();
	}
	
	List getChildren() {
		return Collections.unmodifiableList(children);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("WidgetInfo[class=");
		buf.append(className);
		buf.append(", name=");
		buf.append(name);
		if (parent != null) {
			buf.append(", parent=");
			buf.append(parent.getName());
		}
		buf.append("]");
		return buf.toString();
	}
	private String className;
	private String name;
	private WidgetInfo parent;
	private List properties;     // <Property>
	private List atkProperties;  // <Property>
	private List signals;        // <SignalInfo>
	private List actions;        // <ATKActionInfo>
	private List relations;      // <ATKRelationInfo>
	private List accels;         // <Accel>
	private LinkedList children; // <ChildInfo> 

	protected void addProperty(Property property) {
		properties.add(property);
	}
	protected void addSignalInfo(SignalInfo signalInfo) {
		signals.add(signalInfo);
	}
	protected void addAccelInfo(AccelInfo accelInfo) {
		accels.add(accelInfo);
	}
}
