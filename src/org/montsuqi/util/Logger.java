package org.montsuqi.util;

import java.lang.reflect.Method;
import java.text.MessageFormat;

public abstract class Logger {

	public static Logger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}

	public static Logger getLogger(String name) {
		String factory = System.getProperty("monsia.logger.factory"); //$NON-NLS-1$
		if (factory == null || factory.length() == 0) {
			factory = "org.montsuqi.util.StdErrLogger"; //$NON-NLS-1$
		}
		try {
			Class factoryClass = Class.forName(factory);
			Method getter = factoryClass.getMethod("getLogger", new Class[] { String.class }); //$NON-NLS-1$
			return (Logger)getter.invoke(null, new Object[] { name });
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public void enter(String methodName) {
		trace("entering " + methodName);
	}
	
	public void leave(String methodName) {
		trace("leaving " + methodName);
	}

	public abstract void trace(String message);
	public abstract void debug(String message);
	public abstract void info(String message);
	public abstract void warn(String message);
	public abstract void fatal(String message);

	protected final String formatMessage(String format, Object[] args) {
		return MessageFormat.format(format, args);
	}

	protected final String formatMessage(String format, Object arg) {
		return formatMessage(format, new Object[] { arg });
	}
	
	public void trace(String format, Object[] args) {
		trace(formatMessage(format, args));
	}

	public void debug(String format, Object[] args) {
		debug(formatMessage(format, args));
	}

	public void info(String format, Object[] args) {
		info(formatMessage(format, args));
	}
	
	public void warn(String format, Object[] args) {
		warn(formatMessage(format, args));
	}
	
	public void fatal(String format, Object[] args) {
		fatal(formatMessage(format, args));
	}

	public void trace(String format, Object arg) {
		trace(formatMessage(format, arg));
	}

	public void debug(String format, Object arg) {
		debug(formatMessage(format, arg));
	}

	public void info(String format, Object arg) {
		info(formatMessage(format, arg));
	}
	
	public void warn(String format, Object arg) {
		warn(formatMessage(format, arg));
	}
	
	public void fatal(String format, Object arg) {
		fatal(formatMessage(format, arg));
	}

	public void trace(Throwable e) {
		trace(e.toString());
	}

	public void debug(Throwable e) {
		debug(e.toString());
	}

	public void info(Throwable e) {
		info(e.toString());
	}

	public void warn(Throwable e) {
		warn(e.toString());
	}

	public void fatal(Throwable e) {
		fatal(e.toString());
	}
}
