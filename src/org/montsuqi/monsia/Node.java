package org.montsuqi.monsia;

import java.awt.Component;
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

	
	public String toString() {
		return toStringRecursive(window, 0);
	}

	private String indentSpace(int depth) {
		int width = 4;
		StringBuffer buf = new StringBuffer(depth * width);
		for (int i = 0; i < width; i++) {
			buf.append(' ');
		}
		return buf.toString();
	}

	private String toStringRecursive(Container c, int depth) {
		StringBuffer buf = new StringBuffer();
		buf.append(indentSpace(depth));
		buf.append(c.toString());
		buf.append("\n");
		for (int i = 0, n = c.getComponentCount(); i < n; i++) {
			Component comp = c.getComponent(i);
			if (comp instanceof Container) {
				buf.append(toStringRecursive((Container)comp, depth + 1));
			} else {
				buf.append(indentSpace(depth + 1));
				buf.append(comp.toString());
				buf.append("\n");
			}
		}
		return buf.toString();
	}

}
