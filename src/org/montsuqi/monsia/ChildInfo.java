package org.montsuqi.monsia;

import java.util.ArrayList;
import java.util.List;

class ChildInfo {

	private WidgetInfo child;
    private List properties;
    private String internalChild;

	ChildInfo() {
		this.child = null;
		this.properties = new ArrayList();
		this.internalChild = null;
	}
	
	WidgetInfo getWidgetInfo() {
		return child;
	}

	String getInternalChild() {
		return internalChild;
	}

	void setProperties(List properties) {
		this.properties.clear();
		this.properties.addAll(properties);
	}

	Property getProperty(int i) {
		return (Property)properties.get(i);
	}

	int getPropertiesCount() {
		return properties.size();
	}

	void setWidgetInfo(WidgetInfo child) {
		this.child = child;
	}

	void setInternalChild(String child) {
		internalChild = child;
	}

	void addProperty(Property p) {
		properties.add(p);
	}
}
