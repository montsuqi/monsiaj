package org.montsuqi.monsia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class WidgetInfo {

	public WidgetInfo(String className, String name) {
		parent = null;
		this.className = className;
		this.name = name;
		properties = new ArrayList();
		atkProperties = new ArrayList();
		signals = new ArrayList();
		actions = new ArrayList();
		relations = new ArrayList();
		accels = new ArrayList();
		children = new LinkedList();
	}

	public WidgetInfo getParent() {
		return parent;
	}

	public void setParent(WidgetInfo parent) {
		this.parent = parent;
	}

	public String getClassName() {
		return className;
	}

	public String getName() {
		return name;
	}

	public int getPropertiesCount() {
		return properties.size();
	}

	public List getProperties() {
		return Collections.unmodifiableList(properties);
	}

	public Property getProperty(int i) {
		return (Property)properties.get(i);
	}

	public synchronized void setProperties(List properties) {
		this.properties.clear();
		this.properties.addAll(properties);
	}

	public int getATKPropertiesCount() {
		return atkProperties.size();
	}

	public synchronized void setATKProperties(List properties) {
		this.atkProperties.clear();
		this.atkProperties.addAll(properties);
	}

	public List getSignals() {
		return Collections.unmodifiableList(signals);
	}

	public synchronized void setSignals(List signals) {
		this.signals.clear();
		this.signals.addAll(signals);
	}

	public synchronized void setATKActions(List actions) {
		this.actions.clear();
		this.actions.addAll(actions);
	}

	public synchronized void setRelations(List relations) {
		this.relations.clear();
		this.relations.addAll(relations);
	}

	public int getAccelsCount() {
		return accels.size();
	}

	public List getAccels() {
		return Collections.unmodifiableList(accels);
	}

	public synchronized void setAccels(List accels) {
		this.accels.clear();
		this.accels.addAll(accels);
	}

	public int getChildrenCount() {
		return children.size();
	}
	
	public void removeLastChild() {
		children.removeLast();
	}

	public void addChild(ChildInfo info) {
		children.addLast(info);
	}

	public ChildInfo getChild(int i) {
		return (ChildInfo)children.get(i);
	}

	public ChildInfo getLastChild() {
		return (ChildInfo)children.getLast();
	}
	
	public List getChildren() {
		return Collections.unmodifiableList(children);
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
	private LinkedList children;       // <ChildInfo> 

}
