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
		
	public boolean sendWidget(WidgetMarshal marshal, String name, Container widget) {
		logger.enter("sendWidget");
		if (sender == null) {
			return false;
		}
		logger.debug("sender is {0}", sender);
		Object[] args = new Object[] { name, widget };
		try {
			logger.debug("->invoke {0}.sender({1}, {2})", new Object[] {marshal, name, widget});
			Boolean reply = (Boolean)sender.invoke(marshal, args);
			logger.debug("<-invoke sender done");
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

	public boolean receiveWidget(WidgetMarshal marshal, Container widget) {
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
