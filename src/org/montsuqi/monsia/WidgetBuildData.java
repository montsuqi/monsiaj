package org.montsuqi.monsia;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.montsuqi.util.Logger;

public class WidgetBuildData {
	protected Logger logger;
	protected Method buildMethod;
	protected Method buildChildrenMethod;
	protected Method findInternalChildMethod;

	public WidgetBuildData(Method buildMethod,
	                       Method buildChildrenMethod,
	                       Method findInternalChildMethod) {
		this.buildMethod = buildMethod;
		this.buildChildrenMethod = buildChildrenMethod;
		this.findInternalChildMethod = findInternalChildMethod;
		logger = Logger.getLogger(WidgetBuildData.class);
	}

	public WidgetBuildData(String build, String buildChildren, String findInternalChild) {
		this(findMethod(build, new Class[] { WidgetInfo.class }),
			 findMethod(buildChildren, new Class[] { Container.class, WidgetInfo.class }),
			 findMethod(findInternalChild, new Class[] { Container.class, String.class }));
	}

	public Container build(WidgetBuilder builder, WidgetInfo info) {
		Object[] args = { info };
		return (Container)invoke(builder, buildMethod, args);
	}

	public void buildChildren(WidgetBuilder builder, Container parent, WidgetInfo info) {
		Object[] args = { parent, info };
		invoke(builder, buildChildrenMethod, args);
	}

	public Container findInternalChild(WidgetBuilder builder, Container parent, String name) {
		Object[] args = { builder, parent, name };
		return (Container)invoke(builder, findInternalChildMethod, args);
	}

	public boolean hasBuildMethod() {
		return buildMethod != null;
	}

	public boolean hasBuildChildrenMethod() {
		return buildChildrenMethod != null;
	}

	public boolean hasFindInternalChildMethod() {
		return findInternalChildMethod != null;
	}
	
	protected static Method findMethod(String methodName, Class[] argTypes) {
		if (methodName == null) {
			return null;
		}
		try {
			return WidgetBuilder.class.getMethod(methodName, argTypes);
		} catch (NoSuchMethodException e) {
			Logger.getLogger(WidgetBuilder.class).fatal(e);
			throw new WidgetBuildingException(e);
		}
	}

	protected Object invoke(WidgetBuilder builder, Method method, Object[] args) {
		try {
			return method.invoke(builder, args);
		} catch (IllegalAccessException e) {
			logger.fatal(e);
			throw new WidgetBuildingException(e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getTargetException(); // should use getCause() [J2SE 1.4+]
			logger.fatal(cause);
			throw new WidgetBuildingException(cause);
		}
		
	}
}
