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

package org.montsuqi.client;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.montsuqi.util.Logger;

class MarshalHandler {

	private Method receiver;
	private Method sender;
	private Logger logger;

	MarshalHandler(Method receiver, Method sender) {
		this.receiver = receiver;
		this.sender = sender;
		logger = Logger.getLogger(MarshalHandler.class);
	}
		
	boolean sendWidget(WidgetMarshal marshal, String name, Container widget) {
		if (sender == null) {
			return false;
		}
		Object[] args = new Object[] { name, widget };
		try {
			Boolean reply = (Boolean)sender.invoke(marshal, args);
			return reply.booleanValue();
		} catch (IllegalAccessException e) {
			logger.fatal(e);
			return false;
		} catch (IllegalArgumentException e) {
			logger.fatal(e);
			return false;
		} catch (InvocationTargetException e) {
			logger.fatal(e.getTargetException()); // should use getCause() [J2SE 1.4+]
			return false;
		}
	}

	boolean receiveWidget(WidgetMarshal marshal, Container widget) {
		if (receiver == null) {
			return false;
		}
		
		Object[] args = new Object[] { widget };
		try {
			Boolean result = (Boolean)receiver.invoke(marshal, args);
			return result.booleanValue();
		} catch (Exception e) {
			logger.fatal(e);
			return false;
		}
	}
}
