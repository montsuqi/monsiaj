package org.montsuqi.monsia;

import java.awt.Container;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;

public class Node {
	public Interface xml;
	public String name;
	public JFrame window;
	public Map updateWidget;

	public Node(Interface xml, String wName) {
		this.xml = xml;
		this.name = wName;
		Container widget = xml.getWidget(wName);
		this.window = (JFrame)widget;
		updateWidget = new HashMap();
	}
}
