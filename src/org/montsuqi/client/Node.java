package org.montsuqi.client;

import java.awt.Container;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

import org.montsuqi.monsia.Interface;

class Node {

	private Interface xml;
	private String name;
	private JFrame window;
	private Map changedWidgets;

	Node(Interface xml, String wName) {
		this.xml = xml;
		this.name = wName;
		Container widget = xml.getWidget(wName);
		this.window = (JFrame)widget;
		this.changedWidgets = new HashMap();
	}

	String getName() {
		return name;
	}

	Interface getInterface() {
		return xml;
	}

	JFrame getWindow() {
		return window;
	}

	void clearChangedWidgets() {
		changedWidgets.clear();
	}

	Map getChangedWidgets() {
		return changedWidgets;
	}

	void addChangedWidget(String name, Container widget) {
		if ( ! changedWidgets.containsKey(name)) {
			changedWidgets.put(name, widget);
		}
	}
}
