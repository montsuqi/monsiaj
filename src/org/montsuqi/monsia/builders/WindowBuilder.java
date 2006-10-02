package org.montsuqi.monsia.builders;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;

import org.montsuqi.monsia.Interface;
import org.montsuqi.monsia.WidgetInfo;
import org.montsuqi.widgets.Window;

public class WindowBuilder extends ContainerBuilder {

	Component buildSelf(Interface xml, Container parent, WidgetInfo info) {
		Component c = super.buildSelf(xml, parent, info);
		Window w = (Window)c;
		try {
			if (info.getClassName().equals("Dialog")) {
				Method setAlwaysOnTop = Window.class.getMethod("setAlwaysOnTop", new Class[] { Boolean.TYPE });
				setAlwaysOnTop.invoke(w, new Object[] { Boolean.TRUE });
			}
		} catch (NoSuchMethodException e) {
			// ignore
		} catch (Exception e) {
			throw new WidgetBuildingException(e);
		}
		return c;
	}
}
