package org.montsuqi.client;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.montsuqi.util.Logger;

public class Handler {

	private Method receiver;
	private Method sender;
	private Logger logger;

	public Handler(Method receiver, Method sender) {
		this.receiver = receiver;
		this.sender = sender;
		logger = Logger.getLogger(Handler.class);
	}
		
	public boolean sendWidget(String name, Container widget, Protocol con) {
		if (sender == null) {
			return false;
		}

		Object[] args = new Object[] { name, widget, con };
		try {
			Boolean reply = (Boolean)sender.invoke(null, args);
			return reply.booleanValue();
		} catch (IllegalAccessException e) {
			logger.fatal(e);
			return false;
		} catch (IllegalArgumentException e) {
			logger.fatal(e);
			return false;
		} catch (InvocationTargetException e) {
			logger.fatal(e);
			return false;
		}
	}

	public boolean receiveWidget(Container widget, Protocol con) {
		if (receiver == null) {
			return false;
		}
		
		Object[] args = new Object[] { widget, con };
		try {
			Boolean result = (Boolean)receiver.invoke(null, args);
			return result.booleanValue();
		} catch (Exception e) {
			logger.fatal(e);
			return false;
		}
	}
}
