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

	public abstract void info(String message);
	public abstract void warn(String message);
	public abstract void fatal(String message);

	protected final String formatMessage(String format, Object[] args) {
		return MessageFormat.format(format, args);
	}

	protected final String formatMessage(String format, Object arg) {
		return formatMessage(format, new Object[] { arg });
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

	public void info(String format, Object arg) {
		info(formatMessage(format, arg));
	}
	
	public void warn(String format, Object arg) {
		warn(formatMessage(format, arg));
	}
	
	public void fatal(String format, Object arg) {
		fatal(formatMessage(format, arg));
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
