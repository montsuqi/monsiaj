package org.montsuqi.monsia;

import java.awt.Container;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

public class Node {

	private Interface xml;
	private String name;
	private JFrame window;
	private Map changedWidgets;

	public Node(Interface xml, String wName) {
		this.xml = xml;
		this.name = wName;
		Container widget = xml.getWidget(wName);
		this.window = (JFrame)widget;
		this.changedWidgets = new HashMap();
	}

	public String getName() {
		return name;
	}

	public Interface getInterface() {
		return xml;
	}

	public JFrame getWindow() {
		return window;
	}

	public String toString() {
		StringWriter s = new StringWriter();
		PrintWriter p = new PrintWriter(s);
		window.list(p);
		return s.toString();
	}

	public void clearChangedWidgets() {
		changedWidgets.clear();
	}

	public Map getChangedWidgets() {
		return changedWidgets;
	}

	public void addChangedWidget(String name, Container widget) {
		if ( ! changedWidgets.containsKey(name)) {
			changedWidgets.put(name, widget);
		}
	}
}
