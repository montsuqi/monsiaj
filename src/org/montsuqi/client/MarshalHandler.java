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
