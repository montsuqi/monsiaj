/*      PANDA -- a simple transaction monitor
                                                                                
Copyright (C) 1998-1999 Ogochan.
			  2000-2003 Ogochan & JMA (Japan Medical Association).
                                                                                
This module is part of PANDA.
                                                                                
		PANDA is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY.  No author or distributor accepts responsibility
to anyone for the consequences of using it or for whether it serves
any particular purpose or works at all, unless he says so in writing.
Refer to the GNU General Public License for full details.
                                                                                
		Everyone is granted permission to copy, modify and redistribute
PANDA, but only under the conditions described in the GNU General
Public License.  A copy of this license is supposed to have been given
to you along with PANDA so you can know your rights and
responsibilities.  It should be in a file named COPYING.  Among other
things, the copyright notice and this notice must be preserved on all
copies.
*/

package org.montsuqi.monsia;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.montsuqi.util.Logger;

class WidgetBuildData {
	private Logger logger;
	private Method buildMethod;
	private Method buildChildrenMethod;
	private Method findInternalChildMethod;

	WidgetBuildData(Method buildMethod,
	                       Method buildChildrenMethod,
	                       Method findInternalChildMethod) {
		this.buildMethod = buildMethod;
		this.buildChildrenMethod = buildChildrenMethod;
		this.findInternalChildMethod = findInternalChildMethod;
		logger = Logger.getLogger(WidgetBuildData.class);
	}

	WidgetBuildData(String build, String buildChildren, String findInternalChild) {
		this(findMethod(build, new Class[] { WidgetInfo.class }),
			 findMethod(buildChildren, new Class[] { Container.class, WidgetInfo.class }),
			 findMethod(findInternalChild, new Class[] { Container.class, String.class }));
	}

	Container build(WidgetBuilder builder, WidgetInfo info) {
		Object[] args = { info };
		return (Container)invoke(builder, buildMethod, args);
	}

	void buildChildren(WidgetBuilder builder, Container parent, WidgetInfo info) {
		Object[] args = { parent, info };
		invoke(builder, buildChildrenMethod, args);
	}

	Container findInternalChild(WidgetBuilder builder, Container parent, String name) {
		Object[] args = { builder, parent, name };
		return (Container)invoke(builder, findInternalChildMethod, args);
	}

	boolean hasBuildMethod() {
		return buildMethod != null;
	}

	boolean hasBuildChildrenMethod() {
		return buildChildrenMethod != null;
	}

	boolean hasFindInternalChildMethod() {
		return findInternalChildMethod != null;
	}
	
	private static Method findMethod(String methodName, Class[] argTypes) {
		if (methodName == null) {
			return null;
		}
		try {
			return WidgetBuilder.class.getDeclaredMethod(methodName, argTypes);
		} catch (NoSuchMethodException e) {
			Logger.getLogger(WidgetBuilder.class).fatal(e);
			throw new WidgetBuildingException(e);
		}
	}

	private Object invoke(WidgetBuilder builder, Method method, Object[] args) {
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
