package org.montsuqi.monsia;

import java.util.ArrayList;
import java.util.List;

public class ChildInfo {
	WidgetInfo child;
    List properties;
    String internalChild;

//WidgetInfo info, List properties, String internalChild) {
	public ChildInfo() {
		this.child = null;
		this.properties = new ArrayList();
		this.internalChild = null;
	}
	
	public WidgetInfo getWidgetInfo() {
		return child;
	}
	public String getInternalChild() {
		return internalChild;
	}

	public void setProperties(List properties) {
		this.properties.clear();
		this.properties.addAll(properties);
	}

	public Property getProperty(int i) {
		return (Property)properties.get(i);
	}

	public int getPropertiesCount() {
		return properties.size();
	}

	public void setWidgetInfo(WidgetInfo child) {
		this.child = child;
	}

	public void setInternalChild(String child) {
		internalChild = child;
	}
}
