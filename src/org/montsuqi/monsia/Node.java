package org.montsuqi.monsia;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JWindow;

public class Node {
	public Interface xml;
	public String name;
	public JWindow window;
	public Map updateWidget;

	public Node(Interface xml, String wName) {
		this.xml = xml;
		this.name = wName;
		this.window = (JWindow)xml.getWidget(wName);
		updateWidget = new HashMap();
	}
}
