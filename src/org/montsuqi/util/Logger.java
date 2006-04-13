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

package org.montsuqi.util;

import java.awt.Component;
import java.lang.reflect.Method;
import java.text.MessageFormat;

public abstract class Logger {

	public static final int FATAL = 0;
	public static final int WARNING = 10;
	public static final int INFO = 20;
	public static final int DEBUG = 30;
	public static final int TRACE = 40;

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
			Class[] types = { String.class };
			Method getter = factoryClass.getMethod("getLogger", types); //$NON-NLS-1$
			Object[] args = { name };
			return (Logger)getter.invoke(null, args);
		} catch (Exception e) {
			System.out.println(e);
			return StdErrLogger.getLogger(name);
		}
	}

	protected abstract int getLevel();

	protected String getLevelProperty() {
		final String s = System.getProperty("monsia.logger.level"); //$NON-NLS-1$
		if (s == null) {
			return "WARNING";
		} else {
			return s;
		}
	}

	public void enter() {
		enter(null);
	}

	public void enter(Object arg1) {
		enter(new Object[] { arg1 });
	}

	public void enter(Object arg1, Object arg2) {
		enter(new Object[] { arg1, arg2 });
	}

	public void enter(Object arg1, Object arg2, Object arg3) {
		enter(new Object[] { arg1, arg2, arg3 });
	}

	public void enter(Object[] args) {
		if (getLevel() < TRACE) {
			return;
		}
		StackTraceElement caller = getCaller();
		trace("enter: " + caller);
		if (args != null && args.length > 0) {
			final StringBuffer buf = new StringBuffer("\targs:");
			for (int i = 0; i < args.length; i++) {
				buf.append(' ');
				if (args[i] instanceof Byte) {
					final int value = ((Byte)args[i]).intValue();
					buf.append("0x");
					buf.append(Integer.toHexString((value >> 4) & 0x0f));
					buf.append(Integer.toHexString((value >> 0) & 0x0f));
				} else if (args[i] instanceof Component) {
					final Component c = (Component)args[i];
					String name = c.getName();
					String className = c.getClass().getName();
					if (name != null && name.length() > 0) {
						buf.append(className + ": " + name);
					} else {
						buf.append(args[i]);
					}
				} else {
					buf.append(args[i]);
				}
				if (i < args.length - 1) {
					buf.append(',');
				}
			}
			trace(buf.toString());
		}
	}

	public void leave() {
		if (getLevel() < TRACE) {
			return;
		}
		StackTraceElement caller = getCaller();
		trace("leave: " + caller);
	}

	private StackTraceElement getCaller() {
		Throwable t = new Exception();
		StackTraceElement[] st = t.getStackTrace();
		for (int i = 0; i < st.length; i++) {
			String className = st[i].getClassName();
			Class clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				return st[0];
			}
			if ( ! clazz.isAssignableFrom(Logger.class)) {
				return st[i];
			}
		}
		return st[0];
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
		Object[] args = { arg };
		return formatMessage(format, args);
	}

	public void trace(String format, Object[] args) {
		if (getLevel() >= TRACE) {
			trace(formatMessage(format, args));
		}
	}

	public void debug(String format, Object[] args) {
		if (getLevel() >= DEBUG) {
			debug(formatMessage(format, args));
		}
	}

	public void info(String format, Object[] args) {
		if (getLevel() >= INFO) {
			info(formatMessage(format, args));
		}
	}

	public void warn(String format, Object[] args) {
		if (getLevel() >= WARNING) {
			warn(formatMessage(format, args));
		}
	}

	public void fatal(String format, Object[] args) {
		if (getLevel() >= FATAL) {
			fatal(formatMessage(format, args));
		}
	}

	public void trace(String format, Object arg) {
		if (getLevel() >= TRACE) {
			trace(formatMessage(format, arg));
		}
	}

	public void debug(String format, Object arg) {
		if (getLevel() >= DEBUG) {
			debug(formatMessage(format, arg));
		}
	}

	public void info(String format, Object arg) {
		if (getLevel() >= INFO) {
			info(formatMessage(format, arg));
		}
	}

	public void warn(String format, Object arg) {
		if (getLevel() >= WARNING) {
			warn(formatMessage(format, arg));
		}
	}

	public void fatal(String format, Object arg) {
		if (getLevel() >= FATAL) {
			fatal(formatMessage(format, arg));
		}
	}

	public void trace(Throwable e) {
		if (getLevel() >= TRACE) {
			trace(e.toString());
		}
	}

	public void debug(Throwable e) {
		if (getLevel() >= DEBUG) {
			debug(e.toString());
		}
	}

	public void info(Throwable e) {
		if (getLevel() >= INFO) {
			info(e.toString());
		}
	}

	public void warn(Throwable e) {
		if (getLevel() >= WARNING) {
			warn(e.toString());
		}
	}

	public void fatal(Throwable e) {
		if (getLevel() >= FATAL) {
			fatal(e.toString());
		}
	}
}
