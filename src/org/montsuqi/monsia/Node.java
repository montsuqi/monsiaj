package org.montsuqi.monsia;

import java.awt.Container;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

public class Node {
	private Interface xml;
	private String name;
	public JFrame window;
	public Map updateWidget;

	public Node(Interface xml, String wName) {
		this.xml = xml;
		this.name = wName;
		Container widget = xml.getWidget(wName);
		this.window = (JFrame)widget;
		updateWidget = new HashMap();
	}

	public String getName() {
		return name;
	}

	public Interface getInterface() {
		return xml;
	}

	
}
